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

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SocialStorageCacheService {

  // IdentityStorage
  private final ExoCache<IdentityKey, IdentityData> identityCacheById;
  private final ExoCache<IdentityCompositeKey, IdentityKey> identityIndexCache;
  private final ExoCache<IdentityKey, ProfileData> profileCacheById;
  
  // IdentityRelationship
  private final ExoCache<RelationshipKey, RelationshipData> relationshipCacheById;
  private final ExoCache<RelationshipIdentityKey, RelationshipKey> relationshipCacheByIdentity;

  public SocialStorageCacheService(CacheService cacheService) {
    this.identityCacheById = cacheService.getCacheInstance("org.exoplatform.social.core.storage.IdentityCacheById");
    this.identityIndexCache = cacheService.getCacheInstance("org.exoplatform.social.core.storage.IdentityIndexCache");
    this.profileCacheById = cacheService.getCacheInstance("org.exoplatform.social.core.storage.ProfileCacheById");

    this.relationshipCacheById = cacheService.getCacheInstance("org.exoplatform.social.core.storage.RelationshipCacheById");
    this.relationshipCacheByIdentity = cacheService.getCacheInstance("org.exoplatform.social.core.storage.RelationshipCacheByIdentity");
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

  public ExoCache<RelationshipKey, RelationshipData> getRelationshipCacheById() {
    return relationshipCacheById;
  }

  public ExoCache<RelationshipIdentityKey, RelationshipKey> getRelationshipCacheByIdentity() {
    return relationshipCacheByIdentity;
  }
}
