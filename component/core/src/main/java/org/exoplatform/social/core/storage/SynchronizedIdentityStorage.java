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

import java.util.List;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.storage.cache.IdentityCompositeKey;
import org.exoplatform.social.core.storage.cache.IdentityData;
import org.exoplatform.social.core.storage.cache.IdentityKey;
import org.exoplatform.social.core.storage.cache.ProfileData;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SynchronizedIdentityStorage extends IdentityStorage {

  private final ExoCache<IdentityKey, IdentityData> identityCacheById;
  private final ExoCache<IdentityCompositeKey, IdentityKey> identityIndexCache;
  private final ExoCache<IdentityKey, ProfileData> profileCacheById;

  public SynchronizedIdentityStorage() {
    super();
    identityCacheById = caches.getIdentityCacheById();
    identityIndexCache = caches.getIdentityIndexCache();
    profileCacheById = caches.getProfileCacheById();
  }

  @Override
  public void saveIdentity(final Identity identity) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      super.saveIdentity(identity);
      IdentityKey key = new IdentityKey(identity.getId());
      IdentityCompositeKey compositeKey = new IdentityCompositeKey(identity.getProviderId(), identity.getProviderId());
      identityCacheById.put(key, new IdentityData(identity));
      identityIndexCache.put(compositeKey, key);

    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Identity updateIdentity(final Identity identity) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.updateIdentity(identity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Identity findIdentityById(final String nodeId) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(nodeId);

    IdentityData got = identityCacheById.get(key);

    if (got != null) {
      return got.build();
    }

    boolean created = startSynchronization();
    try {
      Identity identity = super.findIdentityById(nodeId);
      if (identity != null) {
        identityCacheById.put(key, new IdentityData(identity));
      }
      return identity;
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void deleteIdentity(final Identity identity) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      IdentityKey key = new IdentityKey(identity.getId());
      IdentityData data = identityCacheById.remove(key);
      if (data != null) {
        identityIndexCache.remove(new IdentityCompositeKey(data.getProviderId(), data.getRemoteId()));
      }
      super.deleteIdentity(identity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Profile loadProfile(Profile profile) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(profile.getIdentity().getId());
    ProfileData data = profileCacheById.get(key);

    if (data != null) {
      return data.build();
    }

    boolean created = startSynchronization();
    try {
      profile = super.loadProfile(profile);
      profileCacheById.put(key, new ProfileData(profile));
      return profile;
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Identity findIdentity(final String providerId, final String remoteId) throws IdentityStorageException {

    IdentityCompositeKey compositeKey = new IdentityCompositeKey(providerId, remoteId);
    IdentityKey gotKey = identityIndexCache.get(compositeKey);

    if (gotKey != null) {
      IdentityData got = identityCacheById.get(gotKey);
      if (got != null) {
        return got.build();
      }
      else {
        identityIndexCache.remove(gotKey);
      }
    }

    boolean created = startSynchronization();
    try {

      Identity identity = super.findIdentity(providerId, remoteId);

      if (identity != null) {

        IdentityKey key = new IdentityKey(identity.getId());

        identityCacheById.put(key, new IdentityData(identity));
        identityIndexCache.put(compositeKey, key);

      }

      return identity;
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void saveProfile(final Profile profile) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      super.saveProfile(profile);
      IdentityKey key = new IdentityKey(profile.getIdentity().getId());
      profileCacheById.put(key, new ProfileData(profile));
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void updateProfile(final Profile profile) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      super.updateProfile(profile);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getIdentitiesCount(final String providerId) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getIdentitiesCount(providerId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public List<Identity> getIdentitiesByProfileFilter(
      final String providerId, final ProfileFilter profileFilter, final long offset, final long limit,
      final boolean forceLoadOrReloadProfile)
      throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
       return super.getIdentitiesByProfileFilter(providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getIdentitiesByProfileFilterCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getIdentitiesByProfileFilterCount(providerId, profileFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getIdentitiesByFirstCharacterOfNameCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getIdentitiesByFirstCharacterOfNameCount(providerId, profileFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public List<Identity> getIdentitiesByFirstCharacterOfName(
      final String providerId, final ProfileFilter profileFilter, final long offset, final long limit,
      final boolean forceLoadOrReloadProfile)
      throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getIdentitiesByFirstCharacterOfName(
          providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public String getType(final String nodetype, final String property) {

    boolean created = startSynchronization();
    try {
      return super.getType(nodetype, property);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void addOrModifyProfileProperties(final Profile profile) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      super.addOrModifyProfileProperties(profile);
    }
    finally {
      stopSynchronization(created);
    }

  }
}
