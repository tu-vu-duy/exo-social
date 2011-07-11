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
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.storage.cache.loader.CacheLoader;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.IdentityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.ProfileData;
import org.exoplatform.social.core.storage.cache.model.data.RelationshipData;
import org.exoplatform.social.core.storage.cache.model.data.SpaceData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityCountKey;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityCompositeKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipCountKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipIdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceRefKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SocialStorageCacheService {

  public final static int BLOCK_SIZE = 10;

  // IdentityStorage
  private final ExoCache<IdentityKey, IdentityData> identityCacheById;
  private final ExoCache<IdentityCompositeKey, IdentityKey> identityIndexCache;
  private final ExoCache<IdentityKey, ProfileData> profileCacheById;
  private final ExoCache<IdentityFilterKey, IntegerData> countCacheByFilter;

  // RelationshipStorage
  private final ExoCache<RelationshipKey, RelationshipData> relationshipCacheById;
  private final ExoCache<RelationshipIdentityKey, RelationshipKey> relationshipCacheByIdentity;
  private final ExoCache<RelationshipCountKey, IntegerData> relationshipCountConnection;

  // ActivityStorage
  private final ExoCache<ActivityKey, ActivityData> activityCacheById;
  private final ExoCache<ActivityCountKey, IntegerData> activityCountCacheByIdentity;

  // SpaceStorage
  private final ExoCache<SpaceKey, SpaceData> spaceCacheById;
  private final ExoCache<SpaceRefKey, SpaceKey> spaceRefCache;
  private final ExoCache<SpaceFilterKey, IntegerData> spaceCountCache;

  public SocialStorageCacheService(CacheService cacheService) {
    
    this.identityCacheById = cacheService.getCacheInstance("IdentityCacheById");
    this.identityIndexCache = cacheService.getCacheInstance("IdentityIndexCache");
    this.profileCacheById = cacheService.getCacheInstance("ProfileCacheById");
    this.countCacheByFilter = cacheService.getCacheInstance("CountCacheByFilter");

    this.relationshipCacheById = cacheService.getCacheInstance("RelationshipCacheById");
    this.relationshipCacheByIdentity = cacheService.getCacheInstance("RelationshipCacheByIdentity");
    this.relationshipCountConnection = cacheService.getCacheInstance("RelationshipCount");
    
    this.activityCacheById = cacheService.getCacheInstance("ActivityCacheById");
    this.activityCountCacheByIdentity = cacheService.getCacheInstance("ActivityCountCache");

    this.spaceCacheById = cacheService.getCacheInstance("SpaceCacheById");
    this.spaceRefCache = cacheService.getCacheInstance("SpaceRefCache");
    this.spaceCountCache = cacheService.getCacheInstance("SpaceCount");

  }

  public FutureExoCache<IdentityKey, IdentityData, ServiceContext<IdentityData>> createIdentityCacheById() {
    return new FutureExoCache<IdentityKey, IdentityData, ServiceContext<IdentityData>>(
        new CacheLoader<IdentityKey, IdentityData>(),
        getIdentityCacheById()
    );
  }

  public ExoCache<IdentityKey, IdentityData> getIdentityCacheById() {
    return identityCacheById;
  }

  public FutureExoCache<IdentityCompositeKey, IdentityKey, ServiceContext<IdentityKey>> createIdentityCacheByCompositeId() {
    return new FutureExoCache<IdentityCompositeKey, IdentityKey, ServiceContext<IdentityKey>>(
        new CacheLoader<IdentityCompositeKey, IdentityKey>(),
        getIdentityIndexCache()
    );
  }

  public ExoCache<IdentityCompositeKey, IdentityKey> getIdentityIndexCache() {
    return identityIndexCache;
  }

  public FutureExoCache<IdentityKey, ProfileData, ServiceContext<ProfileData>> createProfileCacheById() {
    return new FutureExoCache<IdentityKey, ProfileData, ServiceContext<ProfileData>>(
        new CacheLoader<IdentityKey, ProfileData>(),
        getProfileCacheById()
    );
  }

  public ExoCache<IdentityKey, ProfileData> getProfileCacheById() {
    return profileCacheById;
  }

  public FutureExoCache<IdentityFilterKey, IntegerData, ServiceContext<IntegerData>> createRelationshipCountCacheById() {
    return new FutureExoCache<IdentityFilterKey, IntegerData, ServiceContext<IntegerData>>(
        new CacheLoader<IdentityFilterKey, IntegerData>(),
        getCountCacheByFilter()
    );
  }

  public ExoCache<IdentityFilterKey, IntegerData> getCountCacheByFilter() {
    return countCacheByFilter;
  }

  /*public FutureExoCache<IdentitiesFilterKey, IdentitiesKey, ServiceContext<IdentitiesKey>> createIdentityCacheByFilter() {
    return new FutureExoCache<IdentitiesFilterKey, IdentitiesKey, ServiceContext<IdentitiesKey>>(
        new CacheLoader<IdentitiesFilterKey, IdentitiesKey>(),
        getIdentityCacheByFilter()
    );
  }

  public ExoCache<IdentitiesFilterKey, IdentitiesKey> getIdentityCacheByFilter() {
    return identityCacheByFilter;
  }*/

  public FutureExoCache<RelationshipKey, RelationshipData, ServiceContext<RelationshipData>> createRelationshipCacheById() {
    return new FutureExoCache<RelationshipKey, RelationshipData, ServiceContext<RelationshipData>>(
        new CacheLoader<RelationshipKey, RelationshipData>(),
        getRelationshipCacheById()
    );
  }

  public ExoCache<RelationshipKey, RelationshipData> getRelationshipCacheById() {
    return relationshipCacheById;
  }

  public FutureExoCache<RelationshipIdentityKey, RelationshipKey, ServiceContext<RelationshipKey>> createRelationshipCacheByIdentity() {
    return new FutureExoCache<RelationshipIdentityKey, RelationshipKey, ServiceContext<RelationshipKey>>(
        new CacheLoader<RelationshipIdentityKey, RelationshipKey>(),
        getRelationshipCacheByIdentity()
    );
  }

  public ExoCache<RelationshipIdentityKey, RelationshipKey> getRelationshipCacheByIdentity() {
    return relationshipCacheByIdentity;
  }

  public FutureExoCache<RelationshipCountKey, IntegerData, ServiceContext<IntegerData>> createRelationshipCacheCount() {
    return new FutureExoCache<RelationshipCountKey, IntegerData, ServiceContext<IntegerData>>(
        new CacheLoader<RelationshipCountKey, IntegerData>(),
        getRelationshipCount()
    );
  }

  public ExoCache<RelationshipCountKey, IntegerData> getRelationshipCount() {
    return relationshipCountConnection;
  }

  public FutureExoCache<ActivityKey, ActivityData, ServiceContext<ActivityData>> createActivityCacheById() {
    return new FutureExoCache<ActivityKey, ActivityData, ServiceContext<ActivityData>>(
        new CacheLoader<ActivityKey, ActivityData>(),
        getActivityCacheById()
    );
  }

  public ExoCache<ActivityKey, ActivityData> getActivityCacheById() {
    return activityCacheById;
  }

  public FutureExoCache<ActivityCountKey, IntegerData, ServiceContext<IntegerData>> createActivityCountCache() {
    return new FutureExoCache<ActivityCountKey, IntegerData, ServiceContext<IntegerData>>(
        new CacheLoader<ActivityCountKey, IntegerData>(),
        getActivityCountCache()
    );
  }

  public ExoCache<ActivityCountKey, IntegerData> getActivityCountCache() {
    return activityCountCacheByIdentity;
  }

  public FutureExoCache<SpaceKey, SpaceData, ServiceContext<SpaceData>> createSpaceCacheById() {
    return new FutureExoCache<SpaceKey, SpaceData, ServiceContext<SpaceData>>(
        new CacheLoader<SpaceKey, SpaceData>(),
        getSpaceCacheById()
    );
  }

  public ExoCache<SpaceKey, SpaceData> getSpaceCacheById() {
    return spaceCacheById;
  }

  public FutureExoCache<SpaceRefKey, SpaceKey, ServiceContext<SpaceKey>> createSpaceRefCache() {
    return new FutureExoCache<SpaceRefKey, SpaceKey, ServiceContext<SpaceKey>>(
        new CacheLoader<SpaceRefKey, SpaceKey>(),
        getSpaceRefCache()
    );
  }

  public ExoCache<SpaceRefKey, SpaceKey> getSpaceRefCache() {
    return spaceRefCache;
  }

  public FutureExoCache<SpaceFilterKey, IntegerData, ServiceContext<IntegerData>> createSpaceCount() {
    return new FutureExoCache<SpaceFilterKey, IntegerData, ServiceContext<IntegerData>>(
        new CacheLoader<SpaceFilterKey, IntegerData>(),
        getSpaceCount()
    );
  }

  public ExoCache<SpaceFilterKey, IntegerData> getSpaceCount() {
    return spaceCountCache;
  }


}
