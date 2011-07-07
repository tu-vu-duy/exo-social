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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.IdentityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.ProfileData;
import org.exoplatform.social.core.storage.cache.model.key.IdentityCompositeKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedIdentityStorage implements IdentityStorage {

  private final ExoCache<IdentityKey, IdentityData> exoCacheIdentity;
  private final ExoCache<IdentityCompositeKey, IdentityKey> exoCacheCompositeIdentity;
  private final ExoCache<IdentityKey, ProfileData> exoCacheProfile;
  private final ExoCache<IdentityFilterKey, IntegerData> exoCacheFilterNumber;

  private final FutureExoCache<IdentityKey, IdentityData, ServiceContext<IdentityData>> identityCache;
  private final FutureExoCache<IdentityCompositeKey, IdentityKey, ServiceContext<IdentityKey>> identityIndexCache;
  private final FutureExoCache<IdentityKey, ProfileData, ServiceContext<ProfileData>> profileCache;
  private final FutureExoCache<IdentityFilterKey, IntegerData, ServiceContext<IntegerData>> filterNumberCache;

  private final IdentityStorageImpl storage;

  public CachedIdentityStorage(final IdentityStorageImpl storage, final SocialStorageCacheService cacheService) {

    //
    this.storage = storage;
    this.storage.setStorage(this);

    //
    this.exoCacheIdentity = cacheService.getIdentityCacheById();
    this.exoCacheCompositeIdentity = cacheService.getIdentityIndexCache();
    this.exoCacheProfile = cacheService.getProfileCacheById();
    this.exoCacheFilterNumber = cacheService.getCountCacheByFilter();

    //
    this.identityCache = cacheService.createIdentityCacheById();
    this.identityIndexCache = cacheService.createIdentityCacheByCompositeId();
    this.profileCache = cacheService.createProfileCacheById();
    this.filterNumberCache = cacheService.createRelationshipCountCacheById();

  }

  public void saveIdentity(final Identity identity) throws IdentityStorageException {

    //
    storage.saveIdentity(identity);

    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    exoCacheIdentity.put(key, new IdentityData(identity));
    exoCacheFilterNumber.clearCache();
  }

  public Identity updateIdentity(final Identity identity) throws IdentityStorageException {

    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    exoCacheIdentity.remove(key);

    //
    return storage.updateIdentity(identity);
  }

  public Identity findIdentityById(final String nodeId) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(new Identity(nodeId));
    Identity i = identityCache.get(
        new ServiceContext<IdentityData>() {

          public IdentityData execute() {
            return new IdentityData(storage.findIdentityById(nodeId));
          }
        },
        key)
        .build();

    //
    if (i != null) {
      i.setProfile(loadProfile(i.getProfile()));
    }

    //
    return i;

  }

  public void deleteIdentity(final Identity identity) throws IdentityStorageException {

    //
    storage.deleteIdentity(identity);

    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    IdentityData data = exoCacheIdentity.remove(key);
    if (data != null) {
      exoCacheCompositeIdentity.remove(new IdentityCompositeKey(data.getProviderId(), data.getRemoteId()));
    }
    exoCacheProfile.remove(key);
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

    //
    IdentityCompositeKey key = new IdentityCompositeKey(providerId, remoteId);

    //
    IdentityKey k = identityIndexCache.get(
        new ServiceContext<IdentityKey>() {

          public IdentityKey execute() {
            Identity i = storage.findIdentity(providerId, remoteId);
            if (i == null) return null;
            IdentityKey key = new IdentityKey(i);
            exoCacheIdentity.put(key, new IdentityData(i));
            return key;
          }
        },
        key);
    
    //
    if (k != null) {
      return findIdentityById(k.getId());
    }
    else {
      return null;
    }
  }

  public void saveProfile(final Profile profile) throws IdentityStorageException {

    //
    storage.saveProfile(profile);

    //
    IdentityKey key = new IdentityKey(new Identity(profile.getIdentity().getId()));
    exoCacheProfile.remove(key);

  }

  public void updateProfile(final Profile profile) throws IdentityStorageException {

    //
    storage.updateProfile(profile);

    //
    IdentityKey key = new IdentityKey(new Identity(profile.getIdentity().getId()));
    exoCacheProfile.remove(key);

  }

  public int getIdentitiesCount(final String providerId) throws IdentityStorageException {

    return storage.getIdentitiesCount(providerId);

  }

  public List<Identity> getIdentitiesByProfileFilter(final String providerId, final ProfileFilter profileFilter,
      final long offset, final long limit, final boolean forceLoadOrReloadProfile) throws IdentityStorageException {

    return storage.getIdentitiesByProfileFilter(providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);

  }

  public int getIdentitiesByProfileFilterCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    //
    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);

    //
    return filterNumberCache.get(
        new ServiceContext<IntegerData>() {

          public IntegerData execute() {
            return new IntegerData(storage.getIdentitiesByProfileFilterCount(providerId, profileFilter));
          }
        },
        key)
        .build();

  }

  public int getIdentitiesByFirstCharacterOfNameCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    //
    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);

    //
    return filterNumberCache.get(
        new ServiceContext<IntegerData>() {

          public IntegerData execute() {
            return new IntegerData(storage.getIdentitiesByFirstCharacterOfNameCount(providerId, profileFilter));
          }
        },
        key)
        .build();

  }

  public List<Identity> getIdentitiesByFirstCharacterOfName(final String providerId, final ProfileFilter profileFilter,
      final long offset, final long limit, final boolean forceLoadOrReloadProfile) throws IdentityStorageException {

    return storage.getIdentitiesByFirstCharacterOfName(providerId, profileFilter, offset, limit,
        forceLoadOrReloadProfile);

  }

  public String getType(final String nodetype, final String property) {

    return storage.getType(nodetype, property);

  }

  public void addOrModifyProfileProperties(final Profile profile) throws IdentityStorageException {

    storage.addOrModifyProfileProperties(profile);
    
  }
}
