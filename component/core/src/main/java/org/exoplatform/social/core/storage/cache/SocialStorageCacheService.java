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

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.IdentityData;
import org.exoplatform.social.core.storage.cache.model.data.ProfileData;
import org.exoplatform.social.core.storage.cache.model.data.RelationshipData;
import org.exoplatform.social.core.storage.cache.model.data.SimpleCacheData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityCompositeKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipIdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipKey;

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
  private final ExoCache<IdentityFilterKey, SimpleCacheData<Integer>> countCacheByFilter;
  
  // RelationshipStorage
  private final ExoCache<RelationshipKey, RelationshipData> relationshipCacheById;
  private final ExoCache<RelationshipIdentityKey, RelationshipKey> relationshipCacheByIdentity;
  private final ExoCache<IdentityKey, SimpleCacheData<Integer>> relationshipCountConnection;

  // ActivityStorage
  private final ExoCache<ActivityKey, ActivityData> activityCacheById;
  private final ExoCache<IdentityKey, SimpleCacheData<Integer>> activityCountCacheByIdentity;

  public SocialStorageCacheService(CacheService cacheService) {
    this.identityCacheById = cacheService.getCacheInstance("org.exoplatform.social.core.storage.IdentityCacheById");
    this.identityIndexCache = cacheService.getCacheInstance("org.exoplatform.social.core.storage.IdentityIndexCache");
    this.profileCacheById = cacheService.getCacheInstance("org.exoplatform.social.core.storage.ProfileCacheById");
    this.countCacheByFilter = cacheService.getCacheInstance("org.exoplatform.social.core.storage.CountCacheByFilter");

    this.relationshipCacheById = cacheService.getCacheInstance("org.exoplatform.social.core.storage.RelationshipCacheById");
    this.relationshipCacheByIdentity = cacheService.getCacheInstance("org.exoplatform.social.core.storage.RelationshipCacheByIdentity");
    this.relationshipCountConnection = cacheService.getCacheInstance("org.exoplatform.social.core.storage.RelationshipCountConnection");
    
    this.activityCacheById = cacheService.getCacheInstance("org.exoplatform.social.core.storage.ActivityCacheById");
    this.activityCountCacheByIdentity = cacheService.getCacheInstance("org.exoplatform.social.core.storage.ActivityCountCacheByIdentity");
  }

  public ExoCache<IdentityKey, IdentityData> getIdentityCacheById() {
    return identityCacheById;
  }

  public ExoCache<IdentityCompositeKey, IdentityKey> getIdentityIndexCache() {
    return identityIndexCache;
  }

  public ExoCache<IdentityKey, ProfileData> getProfileCacheById() {
    return profileCacheById;
  }

  public ExoCache<IdentityFilterKey, SimpleCacheData<Integer>> getCountCacheByFilter() {
    return countCacheByFilter;
  }

  public ExoCache<RelationshipKey, RelationshipData> getRelationshipCacheById() {
    return relationshipCacheById;
  }

  public ExoCache<RelationshipIdentityKey, RelationshipKey> getRelationshipCacheByIdentity() {
    return relationshipCacheByIdentity;
  }

  public ExoCache<IdentityKey, SimpleCacheData<Integer>> getRelationshipCountConnection() {
    return relationshipCountConnection;
  }

  public ExoCache<ActivityKey, ActivityData> getActivityCacheById() {
    return activityCacheById;
  }

  public ExoCache<IdentityKey, SimpleCacheData<Integer>> getActivityCountCacheByIdentity() {
    return activityCountCacheByIdentity;
  }

  public int numberBlocks(List l) {
    return l.size() / BLOCK_SIZE;
  }

}
