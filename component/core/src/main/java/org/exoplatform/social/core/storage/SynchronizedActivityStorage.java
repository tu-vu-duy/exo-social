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

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.SimpleCacheData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SynchronizedActivityStorage extends ActivityStorage {

  private final ExoCache<ActivityKey, ActivityData> activityCacheById;
  private final ExoCache<IdentityKey, SimpleCacheData<Integer>> activityCountCacheByIdentity;

  public SynchronizedActivityStorage(
      final RelationshipStorage relationshipStorage,
      final IdentityStorage identityStorage,
      final SpaceStorage spaceStorage) {

    super(relationshipStorage, identityStorage, spaceStorage);
    activityCacheById = caches.getActivityCacheById();
    activityCountCacheByIdentity = caches.getActivityCountCacheByIdentity();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExoSocialActivity getActivity(final String activityId) throws ActivityStorageException {

    ActivityKey key = new ActivityKey(activityId);

    ActivityData got = activityCacheById.get(key);
    if (got != null) {
      return got.build();
    }

    boolean created = startSynchronization();
    try {
      ExoSocialActivity activity = super.getActivity(activityId);
      activityCacheById.put(key, new ActivityData(activity));
      return activity;
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getUserActivities(final Identity owner, final long offset, final long limit)
      throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getUserActivities(owner, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveComment(final ExoSocialActivity activity, final ExoSocialActivity comment)
      throws ActivityStorageException {

    ActivityKey key = new ActivityKey(activity.getId());
    activityCacheById.remove(key);

    boolean created = startSynchronization();
    try {
      super.saveComment(activity, comment);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExoSocialActivity saveActivity(final Identity owner, final ExoSocialActivity activity)
      throws ActivityStorageException {

    activityCountCacheByIdentity.remove(new IdentityKey(owner));

    boolean created = startSynchronization();
    try {
      return super.saveActivity(owner, activity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteActivity(final String activityId) throws ActivityStorageException {

    IdentityKey userKey = new IdentityKey(new Identity(getActivity(activityId).getUserId()));
    activityCountCacheByIdentity.remove(userKey);
    activityCacheById.remove(new ActivityKey(activityId));

    boolean created = startSynchronization();
    try {
      super.deleteActivity(activityId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteComment(final String activityId, final String commentId) throws ActivityStorageException {

    ActivityKey key = new ActivityKey(activityId);
    activityCacheById.remove(key);

    boolean created = startSynchronization();
    try {
      super.deleteComment(activityId, commentId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getActivitiesOfIdentities(
      final List<Identity> connectionList, final long offset, final long limit) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getActivitiesOfIdentities(connectionList, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfUserActivities(final Identity owner) throws ActivityStorageException {

    IdentityKey key = new IdentityKey(owner);

    SimpleCacheData<Integer> data = activityCountCacheByIdentity.get(key);

    if (data != null) {
      return data.build();
    }

    boolean created = startSynchronization();
    try {
      int got = super.getNumberOfUserActivities(owner);
      activityCountCacheByIdentity.put(key, new SimpleCacheData<Integer>(got));
      return got;
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void updateActivity(final ExoSocialActivity existingActivity) throws ActivityStorageException {

    activityCacheById.remove(new ActivityKey(existingActivity.getId()));

    boolean created = startSynchronization();
    try {
      super.updateActivity(existingActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }
}
