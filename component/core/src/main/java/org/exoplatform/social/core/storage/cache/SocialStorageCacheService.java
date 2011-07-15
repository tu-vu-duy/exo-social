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
import org.exoplatform.social.core.storage.cache.model.data.ListActivitiesData;
import org.exoplatform.social.core.storage.cache.model.data.ListIdentitiesData;
import org.exoplatform.social.core.storage.cache.model.data.ProfileData;
import org.exoplatform.social.core.storage.cache.model.data.RelationshipData;
import org.exoplatform.social.core.storage.cache.model.data.SpaceData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityCountKey;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityCompositeKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.ListActivitiesKey;
import org.exoplatform.social.core.storage.cache.model.key.ListIdentitiesKey;
import org.exoplatform.social.core.storage.cache.model.key.ListRelationshipsKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipCountKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipIdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceRefKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SocialStorageCacheService {

  // IdentityStorage
  private final ExoCache<IdentityKey, IdentityData> identityCache;
  private final ExoCache<IdentityCompositeKey, IdentityKey> identityIndexCache;
  private final ExoCache<IdentityKey, ProfileData> profileCache;
  private final ExoCache<IdentityFilterKey, IntegerData> countIdentitiesCache;
  private final ExoCache<ListIdentitiesKey, ListIdentitiesData> identitiesCache;

  // RelationshipStorage
  private final ExoCache<RelationshipKey, RelationshipData> relationshipCache;
  private final ExoCache<RelationshipIdentityKey, RelationshipKey> relationshipCacheByIdentity;
  private final ExoCache<RelationshipCountKey, IntegerData> relationshipsCount;
  private final ExoCache<ListRelationshipsKey, ListIdentitiesData> relationshipsCache;

  // ActivityStorage
  private final ExoCache<ActivityKey, ActivityData> activityCacheById;
  private final ExoCache<ActivityCountKey, IntegerData> activityCountCacheByIdentity;
  private final ExoCache<ListActivitiesKey, ListActivitiesData> activitiesCache;

  // SpaceStorage
  private final ExoCache<SpaceKey, SpaceData> spaceCacheById;
  private final ExoCache<SpaceRefKey, SpaceKey> spaceRefCache;
  private final ExoCache<SpaceFilterKey, IntegerData> spaceCountCache;

  public SocialStorageCacheService(CacheService cacheService) {
    
    this.identityCache = CacheType.IDENTITY.getFromService(cacheService);
    this.identityIndexCache = CacheType.IDENTITY_INDEX.getFromService(cacheService);
    this.profileCache = CacheType.PROFILE.getFromService(cacheService);
    this.countIdentitiesCache = CacheType.IDENTITIES_COUNT.getFromService(cacheService);
    this.identitiesCache = CacheType.IDENTITIES.getFromService(cacheService);

    this.relationshipCache = CacheType.RELATIONSHIP.getFromService(cacheService);
    this.relationshipCacheByIdentity = CacheType.RELATIONSHIP_FROM_IDENTITY.getFromService(cacheService);
    this.relationshipsCount = CacheType.RELATIONSHIPS_COUNT.getFromService(cacheService);
    this.relationshipsCache = CacheType.RELATIONSHIPS.getFromService(cacheService);

    this.activityCacheById = CacheType.ACTIVITY.getFromService(cacheService);
    this.activityCountCacheByIdentity = CacheType.ACTIVITIES_COUNT.getFromService(cacheService);
    this.activitiesCache = CacheType.ACTIVITIES.getFromService(cacheService);

    this.spaceCacheById = cacheService.getCacheInstance("SpaceCacheById");
    this.spaceRefCache = cacheService.getCacheInstance("SpaceRefCache");
    this.spaceCountCache = cacheService.getCacheInstance("SpaceCount");

  }

  public ExoCache<IdentityKey, IdentityData> getIdentityCache() {
    return identityCache;
  }

  public ExoCache<IdentityCompositeKey, IdentityKey> getIdentityIndexCache() {
    return identityIndexCache;
  }

  public ExoCache<IdentityKey, ProfileData> getProfileCache() {
    return profileCache;
  }

  public ExoCache<IdentityFilterKey, IntegerData> getCountIdentitiesCache() {
    return countIdentitiesCache;
  }

  public ExoCache<ListIdentitiesKey, ListIdentitiesData> getIdentitiesCache() {
    return identitiesCache;
  }

  public ExoCache<RelationshipKey, RelationshipData> getRelationshipCache() {
    return relationshipCache;
  }

  public ExoCache<RelationshipIdentityKey, RelationshipKey> getRelationshipCacheByIdentity() {
    return relationshipCacheByIdentity;
  }

  public ExoCache<RelationshipCountKey, IntegerData> getRelationshipCount() {
    return relationshipsCount;
  }

  public ExoCache<ListRelationshipsKey, ListIdentitiesData> getRelationshipsCache() {
    return relationshipsCache;
  }

  public ExoCache<ActivityKey, ActivityData> getActivityCacheById() {
    return activityCacheById;
  }

  public ExoCache<ActivityCountKey, IntegerData> getActivityCountCache() {
    return activityCountCacheByIdentity;
  }

  public ExoCache<ListActivitiesKey, ListActivitiesData> getActivitiesCache() {
    return activitiesCache;
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
