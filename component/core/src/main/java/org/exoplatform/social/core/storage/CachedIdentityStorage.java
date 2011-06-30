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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;
import org.exoplatform.social.core.storage.cache.loader.CacheLoader;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.IdentityData;
import org.exoplatform.social.core.storage.cache.model.data.ProfileData;
import org.exoplatform.social.core.storage.cache.model.data.SimpleCacheData;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedIdentityStorage implements IdentityStorage {

  private ExoCache<IdentityKey, IdentityData> exoCacheIdentity;
  private ExoCache<IdentityKey, ProfileData> exoCacheProfile;
  private ExoCache<IdentityFilterKey, SimpleCacheData<Integer>> exoCacheFilterNumber;

  private FutureExoCache<IdentityKey, IdentityData, ServiceContext<IdentityData>> identityCache;
  private FutureExoCache<IdentityKey, ProfileData, ServiceContext<ProfileData>> profileCache;
  private FutureExoCache<IdentityFilterKey, SimpleCacheData<Integer>, ServiceContext<SimpleCacheData<Integer>>> filterNumberCache;

  private final IdentityStorageImpl storage;

  public CachedIdentityStorage(final IdentityStorageImpl storage, final SocialStorageCacheService cacheService) {
    this.storage = storage;

    exoCacheIdentity = cacheService.getIdentityCacheById();
    exoCacheProfile = cacheService.getProfileCacheById();
    exoCacheFilterNumber = cacheService.getCountCacheByFilter();

    this.identityCache = new FutureExoCache<IdentityKey, IdentityData, ServiceContext<IdentityData>>(new CacheLoader<IdentityKey, IdentityData>(), exoCacheIdentity);
    this.profileCache = new FutureExoCache<IdentityKey, ProfileData, ServiceContext<ProfileData>>(new CacheLoader<IdentityKey, ProfileData>(), exoCacheProfile);
    this.filterNumberCache = new FutureExoCache<IdentityFilterKey, SimpleCacheData<Integer>, ServiceContext<SimpleCacheData<Integer>>>(new CacheLoader<IdentityFilterKey, SimpleCacheData<Integer>>(), exoCacheFilterNumber);
  }

  public void saveIdentity(final Identity identity) throws IdentityStorageException {
    
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    exoCacheIdentity.remove(key);
    exoCacheFilterNumber.clearCache();

    storage.saveIdentity(identity);

  }

  public Identity updateIdentity(final Identity identity) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    exoCacheIdentity.remove(key);

    return storage.updateIdentity(identity);
  }

  public Identity findIdentityById(final String nodeId) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(new Identity(nodeId));
    return identityCache.get(
        new ServiceContext<IdentityData>() {

          public IdentityData execute() {
            return new IdentityData(storage.findIdentityById(nodeId));
          }
        },
        key)
        .build();
  }

  public void deleteIdentity(final Identity identity) throws IdentityStorageException {

    storage.deleteIdentity(identity);

    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    exoCacheIdentity.remove(key);
    exoCacheFilterNumber.clearCache();
  }

  public Profile loadProfile(final Profile profile) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(new Identity(profile.getIdentity().getId()));
    return profileCache.get(
        new ServiceContext<ProfileData>() {

          public ProfileData execute() {
            return new ProfileData(storage.loadProfile(profile));
          }
        },
        key)
        .build();
  }

  public Identity findIdentity(final String providerId, final String remoteId) throws IdentityStorageException {
    return storage.findIdentity(providerId, remoteId);
  }

  public void saveProfile(final Profile profile) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(new Identity(profile.getIdentity().getId()));
    exoCacheProfile.remove(key);

    storage.saveProfile(profile);

  }

  public void updateProfile(final Profile profile) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(new Identity(profile.getIdentity().getId()));
    exoCacheProfile.remove(key);

    storage.updateProfile(profile);

  }

  public int getIdentitiesCount(final String providerId) throws IdentityStorageException {
    return storage.getIdentitiesCount(providerId);
  }

  public List<Identity> getIdentitiesByProfileFilter(final String providerId, final ProfileFilter profileFilter, final long offset, final long limit, final boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    return storage.getIdentitiesByProfileFilter(providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
  }

  public int getIdentitiesByProfileFilterCount(final String providerId, final ProfileFilter profileFilter) throws IdentityStorageException {

    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);
    return filterNumberCache.get(
        new ServiceContext<SimpleCacheData<Integer>>() {

          public SimpleCacheData<Integer> execute() {
            return new SimpleCacheData<Integer>(storage.getIdentitiesByProfileFilterCount(providerId, profileFilter));
          }
        },
        key)
        .build();
  }

  public int getIdentitiesByFirstCharacterOfNameCount(final String providerId, final ProfileFilter profileFilter) throws IdentityStorageException {
    
    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);
    return filterNumberCache.get(
        new ServiceContext<SimpleCacheData<Integer>>() {

          public SimpleCacheData<Integer> execute() {
            return new SimpleCacheData<Integer>(storage.getIdentitiesByFirstCharacterOfNameCount(providerId, profileFilter));
          }
        },
        key)
        .build();
  }

  public List<Identity> getIdentitiesByFirstCharacterOfName(final String providerId, final ProfileFilter profileFilter, final long offset, final long limit, final boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    return storage.getIdentitiesByFirstCharacterOfName(providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
  }

  public String getType(final String nodetype, final String property) {
    return storage.getType(nodetype, property);
  }

  public void addOrModifyProfileProperties(final Profile profile) throws IdentityStorageException {
    storage.addOrModifyProfileProperties(profile);
  }
}
