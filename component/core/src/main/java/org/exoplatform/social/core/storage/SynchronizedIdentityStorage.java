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
import org.exoplatform.social.core.storage.cache.model.data.IdentityData;
import org.exoplatform.social.core.storage.cache.model.data.ProfileData;
import org.exoplatform.social.core.storage.cache.model.data.SimpleCacheData;
import org.exoplatform.social.core.storage.cache.model.key.IdentityCompositeKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SynchronizedIdentityStorage extends IdentityStorage {

  private final ExoCache<IdentityKey, IdentityData> identityCacheById;
  private final ExoCache<IdentityCompositeKey, IdentityKey> identityIndexCache;
  private final ExoCache<IdentityKey, ProfileData> profileCacheById;
  private final ExoCache<IdentityFilterKey, SimpleCacheData<Integer>> countCacheByFilter;

  public SynchronizedIdentityStorage() {
    super();
    identityCacheById = caches.getIdentityCacheById();
    identityIndexCache = caches.getIdentityIndexCache();
    profileCacheById = caches.getProfileCacheById();
    countCacheByFilter = caches.getCountCacheByFilter();
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
      countCacheByFilter.clearCache();

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
      countCacheByFilter.clearCache();
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
      profileCacheById.remove(key);
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
      /*
            List<Relationship> got = super.getSenderRelationships(sender, type, listCheckIdentity);
      int nb = caches.numberBlocks(got);

      for (int i = nb; nb > 0; --i) {

        // Compute current block
        int currentBlock = i * caches.BLOCK_SIZE;

        // Prepare block data
        List<RelationshipData> dataList = new ArrayList<RelationshipData>();
        for (Relationship relationship : got.subList(currentBlock, got.size())) {
          dataList.add(new RelationshipData(relationship));
        }

        RelationshipListKey key = new RelationshipListKey(i, new IdentityKey(sender.getId()), type);
        RelationshipListData data = new RelationshipListData(null, dataList);

      }
      return got;
       */
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getIdentitiesByProfileFilterCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);

    SimpleCacheData<Integer> data = countCacheByFilter.get(key);

    if (data != null) {
      return data.build();
    }

    boolean created = startSynchronization();
    try {
      int count = super.getIdentitiesByProfileFilterCount(providerId, profileFilter);
      countCacheByFilter.put(key, new SimpleCacheData<Integer>(count));
      return count;
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getIdentitiesByFirstCharacterOfNameCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);

    SimpleCacheData<Integer> data = countCacheByFilter.get(key);

    if (data != null) {
      return data.build();
    }

    boolean created = startSynchronization();
    try {
      int count = super.getIdentitiesByFirstCharacterOfNameCount(providerId, profileFilter);
      countCacheByFilter.put(key, new SimpleCacheData<Integer>(count));
      return count;
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
