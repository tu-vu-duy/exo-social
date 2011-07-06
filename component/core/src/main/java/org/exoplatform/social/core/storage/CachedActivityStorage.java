/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.storage;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.ExceptionWrapper;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityCountKey;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;

import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedActivityStorage implements ActivityStorage {

  private final ExoCache<ActivityKey, ActivityData> eXoCacheActivityId;
  private final ExoCache<ActivityCountKey, IntegerData> eXoCacheActivityCount;

  private final FutureExoCache<ActivityKey, ActivityData, ServiceContext<ActivityData>> activityCache;
  private final FutureExoCache<ActivityCountKey, IntegerData, ServiceContext<IntegerData>> activityCountCache;

  private final ActivityStorageImpl storage;

  public CachedActivityStorage(final ActivityStorageImpl storage, final SocialStorageCacheService cacheService) {

    this.storage = storage;
    this.storage.setStorage(this);

    eXoCacheActivityId = cacheService.getActivityCacheById();
    eXoCacheActivityCount = cacheService.getActivityCountCache();

    activityCache = cacheService.createActivityCacheById();
    activityCountCache = cacheService.createActivityCountCache();

  }

  public ExoSocialActivity getActivity(final String activityId) throws ActivityStorageException {

    //
    ActivityKey key = new ActivityKey(activityId);
    final ExceptionWrapper<ActivityStorageException> wrapper = new ExceptionWrapper<ActivityStorageException>();

    //
    ActivityData activity = activityCache.get(
        new ServiceContext<ActivityData>() {
          public ActivityData execute() {
            try {
              return new ActivityData(storage.getActivity(activityId));
            }
            catch (ActivityStorageException e) {
              wrapper.setException(e);
              return null;
            }
          }
        },
        key);

    //
    if (wrapper.getException() != null) {
      throw wrapper.getException();
    }
    else {
      return activity.build();
    }

  }

  public List<ExoSocialActivity> getUserActivities(final Identity owner) throws ActivityStorageException {
    return storage.getUserActivities(owner);
  }

  public List<ExoSocialActivity> getUserActivities(final Identity owner, final long offset, final long limit) throws ActivityStorageException {
    return storage.getUserActivities(owner, offset, limit);
  }

  public void saveComment(final ExoSocialActivity activity, final ExoSocialActivity comment) throws ActivityStorageException {

    //
    storage.saveComment(activity, comment);

    //
    eXoCacheActivityId.remove(new ActivityKey(activity.getId()));

  }

  public ExoSocialActivity saveActivity(final Identity owner, final ExoSocialActivity activity) throws ActivityStorageException {

    //
    ExoSocialActivity a = storage.saveActivity(owner, activity);

    //
    ActivityKey key = new ActivityKey(a.getId());
    eXoCacheActivityId.remove(key);
    invalidate();

    //
    return a;

  }

  public ExoSocialActivity getParentActivity(final ExoSocialActivity comment) throws ActivityStorageException {
    return storage.getParentActivity(comment);
  }


  public void deleteActivity(final String activityId) throws ActivityStorageException {

    //
    storage.deleteActivity(activityId);

    //
    ActivityKey key = new ActivityKey(activityId);
    eXoCacheActivityId.remove(key);
    invalidate();

  }

  public void deleteComment(final String activityId, final String commentId) throws ActivityStorageException {
    
    //
    storage.deleteComment(activityId, commentId);

    //
    eXoCacheActivityId.remove(new ActivityKey(commentId));
    eXoCacheActivityId.remove(new ActivityKey(activityId));

  }

  public List<ExoSocialActivity> getActivitiesOfIdentities(final List<Identity> connectionList, final long offset, final long limit) throws ActivityStorageException {
    return storage.getActivitiesOfIdentities(connectionList, offset, limit);
  }

  public List<ExoSocialActivity> getActivitiesOfIdentities(final List<Identity> connectionList, final TimestampType type, final long offset, final long limit) throws ActivityStorageException {
    return storage.getActivitiesOfIdentities(connectionList, type, offset, limit);
  }

  public int getNumberOfUserActivities(final Identity owner) throws ActivityStorageException {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), ActivityCountKey.CountType.USER);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfUserActivities(owner));
          }
        },
        key)
        .build();
    
  }

  public int getNumberOfNewerOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityCountKey.CountType.NEWER_USER);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfNewerOnUserActivities(ownerIdentity, baseActivity));
          }
        },
        key)
        .build();
    
  }

  public List<ExoSocialActivity> getNewerOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity, final int limit) {
    return storage.getNewerOnUserActivities(ownerIdentity, baseActivity, limit);
  }

  public int getNumberOfOlderOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityCountKey.CountType.OLDER_USER);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfOlderOnUserActivities(ownerIdentity, baseActivity));
          }
        },
        key)
        .build();
    
  }

  public List<ExoSocialActivity> getOlderOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity, final int limit) {
    return storage.getOlderOnUserActivities(ownerIdentity, baseActivity, limit);
  }

  public List<ExoSocialActivity> getActivityFeed(final Identity ownerIdentity, final int offset, final int limit) {
    return storage.getActivityFeed(ownerIdentity, offset, limit);
  }

  public int getNumberOfActivitesOnActivityFeed(final Identity ownerIdentity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityCountKey.CountType.FEED);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfActivitesOnActivityFeed(ownerIdentity));
          }
        },
        key)
        .build();

  }

  public int getNumberOfNewerOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityCountKey.CountType.NEWER_FEED);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfNewerOnActivityFeed(ownerIdentity, baseActivity));
          }
        },
        key)
        .build();

  }

  public List<ExoSocialActivity> getNewerOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity, final int limit) {
    return storage.getNewerOnActivityFeed(ownerIdentity, baseActivity, limit);
  }

  public int getNumberOfOlderOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityCountKey.CountType.OLDER_FEED);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfOlderOnActivityFeed(ownerIdentity, baseActivity));
          }
        },
        key)
        .build();

  }

  public List<ExoSocialActivity> getOlderOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity, final int limit) {
    return storage.getOlderOnActivityFeed(ownerIdentity, baseActivity, limit);
  }

  public List<ExoSocialActivity> getActivitiesOfConnections(final Identity ownerIdentity, final int offset, final int limit) {
    return storage.getActivitiesOfConnections(ownerIdentity, offset, limit);
  }

  public int getNumberOfActivitiesOfConnections(final Identity ownerIdentity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityCountKey.CountType.CONNECTION);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfActivitiesOfConnections(ownerIdentity));
          }
        },
        key)
        .build();

  }

  public List<ExoSocialActivity> getActivitiesOfIdentity(final Identity ownerIdentity, final long offset, final long limit) throws ActivityStorageException {
    return storage.getActivitiesOfIdentity(ownerIdentity, offset, limit);
  }

  public int getNumberOfNewerOnActivitiesOfConnections(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityCountKey.CountType.NEWER_CONNECTION);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfNewerOnActivitiesOfConnections(ownerIdentity, baseActivity));
          }
        },
        key)
        .build();

  }

  public List<ExoSocialActivity> getNewerOnActivitiesOfConnections(final Identity ownerIdentity, final ExoSocialActivity baseActivity, final long limit) {
    return storage.getNewerOnActivitiesOfConnections(ownerIdentity, baseActivity, limit);
  }

  public int getNumberOfOlderOnActivitiesOfConnections(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityCountKey.CountType.OLDER_CONNECTION);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfOlderOnActivitiesOfConnections(ownerIdentity, baseActivity));
          }
        },
        key)
        .build();

  }

  public List<ExoSocialActivity> getOlderOnActivitiesOfConnections(final Identity ownerIdentity, final ExoSocialActivity baseActivity, final int limit) {
    return storage.getOlderOnActivitiesOfConnections(ownerIdentity, baseActivity, limit);
  }

  public List<ExoSocialActivity> getUserSpacesActivities(final Identity ownerIdentity, final int offset, final int limit) {
    return storage.getUserSpacesActivities(ownerIdentity, offset, limit);
  }

  public int getNumberOfUserSpacesActivities(final Identity ownerIdentity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityCountKey.CountType.SPACE);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfUserSpacesActivities(ownerIdentity));
          }
        },
        key)
        .build();
    
  }

  public int getNumberOfNewerOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityCountKey.CountType.NEWER_SPACE);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfNewerOnUserSpacesActivities(ownerIdentity, baseActivity));
          }
        },
        key)
        .build();

  }

  public List<ExoSocialActivity> getNewerOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity, final int limit) {
    return storage.getNewerOnUserSpacesActivities(ownerIdentity, baseActivity, limit);
  }

  public int getNumberOfOlderOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityCountKey.CountType.OLDER_SPACE);

    //
    return activityCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getNumberOfOlderOnUserSpacesActivities(ownerIdentity, baseActivity));
          }
        },
        key)
        .build();

  }

  public List<ExoSocialActivity> getOlderOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity, final int limit) {
    return storage.getOlderOnUserSpacesActivities(ownerIdentity, baseActivity, limit);
  }

  public List<ExoSocialActivity> getComments(final ExoSocialActivity existingActivity, final int offset, final int limit) {
    return storage.getComments(existingActivity, offset, limit);
  }

  public int getNumberOfComments(final ExoSocialActivity existingActivity) {
    return storage.getNumberOfComments(existingActivity);
  }

  public int getNumberOfNewerComments(final ExoSocialActivity existingActivity, final ExoSocialActivity baseComment) {
    return storage.getNumberOfNewerComments(existingActivity, baseComment);
  }

  public List<ExoSocialActivity> getNewerComments(final ExoSocialActivity existingActivity, final ExoSocialActivity baseComment, final int limit) {
    return storage.getNewerComments(existingActivity, baseComment, limit);
  }

  public int getNumberOfOlderComments(final ExoSocialActivity existingActivity, final ExoSocialActivity baseComment) {
    return storage.getNumberOfOlderComments(existingActivity, baseComment);
  }

  public List<ExoSocialActivity> getOlderComments(final ExoSocialActivity existingActivity, final ExoSocialActivity baseComment, final int limit) {
    return storage.getOlderComments(existingActivity, baseComment, limit);
  }

  public SortedSet<ActivityProcessor> getActivityProcessors() {
    return storage.getActivityProcessors();
  }

  public void updateActivity(final ExoSocialActivity existingActivity) throws ActivityStorageException {

    //
    storage.updateActivity(existingActivity);
    
    //
    ActivityKey key = new ActivityKey(existingActivity.getId());
    eXoCacheActivityId.remove(key);

  }

  public void invalidate() {
    eXoCacheActivityCount.clearCache();
  }
  
}
