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

package org.exoplatform.social.core.storage.cache;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.cache.model.data.ListActivitiesData;
import org.exoplatform.social.core.storage.cache.model.data.ListIdentitiesData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityType;
import org.exoplatform.social.core.storage.cache.model.key.ListActivitiesKey;
import org.exoplatform.social.core.storage.cache.model.key.ListIdentitiesKey;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityCountKey;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedActivityStorage implements ActivityStorage {

  private final ExoCache<ActivityKey, ActivityData> eXoCacheActivityId;
  private final ExoCache<ActivityCountKey, IntegerData> eXoCacheActivityCount;
  private final ExoCache<ListActivitiesKey, ListActivitiesData> eXoCacheActivities;

  private final FutureExoCache<ActivityKey, ActivityData, ServiceContext<ActivityData>> activityCache;
  private final FutureExoCache<ActivityCountKey, IntegerData, ServiceContext<IntegerData>> activityCountCache;
  private final FutureExoCache<ListActivitiesKey, ListActivitiesData, ServiceContext<ListActivitiesData>> activitiesCache;

  private final ActivityStorageImpl storage;

  public CachedActivityStorage(final ActivityStorageImpl storage, final SocialStorageCacheService cacheService) {

    //
    this.storage = storage;
    this.storage.setStorage(this);

    //
    this.eXoCacheActivityId = cacheService.getActivityCacheById();
    this.eXoCacheActivityCount = cacheService.getActivityCountCache();
    this.eXoCacheActivities = cacheService.getActivitiesCache();

    //
    this.activityCache = CacheType.ACTIVITY.createFutureCache(eXoCacheActivityId);
    this.activityCountCache = CacheType.ACTIVITIES_COUNT.createFutureCache(eXoCacheActivityCount);
    this.activitiesCache = CacheType.ACTIVITIES.createFutureCache(eXoCacheActivities);

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

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), ActivityType.USER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getUserActivities(owner, offset, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;

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

    //
    List<IdentityKey> keyskeys = new ArrayList<IdentityKey>();
    for (Identity i : connectionList) {
      keyskeys.add(new IdentityKey(i));
    }
    ListActivitiesKey listKey = new ListActivitiesKey(new ListIdentitiesData(keyskeys), 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getActivitiesOfIdentities(connectionList, offset, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;

  }

  public List<ExoSocialActivity> getActivitiesOfIdentities(final List<Identity> connectionList, final TimestampType type, final long offset, final long limit) throws ActivityStorageException {
    return storage.getActivitiesOfIdentities(connectionList, type, offset, limit);
  }

  public int getNumberOfUserActivities(final Identity owner) throws ActivityStorageException {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), ActivityType.USER);

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
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_USER);

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

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.NEWER_USER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getNewerOnUserActivities(ownerIdentity, baseActivity, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;

  }

  public int getNumberOfOlderOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_USER);

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

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_USER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getOlderOnUserActivities(ownerIdentity, baseActivity, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;

  }

  public List<ExoSocialActivity> getActivityFeed(final Identity ownerIdentity, final int offset, final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.FEED);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getActivityFeed(ownerIdentity, offset, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;

  }

  public int getNumberOfActivitesOnActivityFeed(final Identity ownerIdentity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.FEED);

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
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_FEED);

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

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_FEED);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getNewerOnActivityFeed(ownerIdentity, baseActivity, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;

  }

  public int getNumberOfOlderOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_FEED);

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

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_FEED);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getOlderOnActivityFeed(ownerIdentity, baseActivity, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;
    
  }

  public List<ExoSocialActivity> getActivitiesOfConnections(final Identity ownerIdentity, final int offset, final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.CONNECTION);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getActivitiesOfConnections(ownerIdentity, offset, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;
    
  }

  public int getNumberOfActivitiesOfConnections(final Identity ownerIdentity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.CONNECTION);

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
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_CONNECTION);

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

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_CONNECTION);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getNewerOnActivitiesOfConnections(ownerIdentity, baseActivity, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;
    
  }

  public int getNumberOfOlderOnActivitiesOfConnections(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_CONNECTION);

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

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_CONNECTION);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getOlderOnActivitiesOfConnections(ownerIdentity, baseActivity, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;

  }

  public List<ExoSocialActivity> getUserSpacesActivities(final Identity ownerIdentity, final int offset, final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.SPACE);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getUserSpacesActivities(ownerIdentity, offset, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;

  }

  public int getNumberOfUserSpacesActivities(final Identity ownerIdentity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.SPACE);

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
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_SPACE);

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

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_SPACE);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getNewerOnUserSpacesActivities(ownerIdentity, baseActivity, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;

  }

  public int getNumberOfOlderOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
        new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_SPACE);

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

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_SPACE);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
        new ServiceContext<ListActivitiesData>() {
          public ListActivitiesData execute() {
            List<ExoSocialActivity> got = storage.getOlderOnUserSpacesActivities(ownerIdentity, baseActivity, limit);

            List<ActivityKey> data = new ArrayList<ActivityKey>();
            for (ExoSocialActivity a : got) {
              ActivityKey k = new ActivityKey(a.getId());
              eXoCacheActivityId.put(k, new ActivityData(a));
              data.add(k);
            }
            return new ListActivitiesData(data);
          }
        },
        listKey);

    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : keys.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }

    //
    return activities;

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
    eXoCacheActivities.clearCache();
  }
  
}
