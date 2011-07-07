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
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.SpaceData;
import org.exoplatform.social.core.storage.cache.model.key.SpaceKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceRefKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedSpaceStorage implements SpaceStorage {

  private final ExoCache<SpaceKey, SpaceData> eXoCacheSpaceId;
  private final ExoCache<SpaceRefKey, SpaceKey> eXoCacheRefSpace;

  private final FutureExoCache<SpaceKey, SpaceData, ServiceContext<SpaceData>> spaceCache;
  private final FutureExoCache<SpaceRefKey, SpaceKey, ServiceContext<SpaceKey>> spaceRefCache;

  private final SpaceStorageImpl storage;

  public CachedSpaceStorage(final SpaceStorageImpl storage, final SocialStorageCacheService cacheService) {

    this.storage = storage;

    this.eXoCacheSpaceId = cacheService.getSpaceCacheById();
    this.eXoCacheRefSpace = cacheService.getSpaceRefCache();

    this.spaceCache = cacheService.createSpaceCacheById();
    this.spaceRefCache = cacheService.createSpaceRefCache();

  }

  private void cleanRef(SpaceData removed) {
    eXoCacheRefSpace.remove(new SpaceRefKey(removed.getDisplayName()));
    eXoCacheRefSpace.remove(new SpaceRefKey(null, removed.getPrettyName()));
    eXoCacheRefSpace.remove(new SpaceRefKey(null, null, removed.getGroupId()));
    eXoCacheRefSpace.remove(new SpaceRefKey(null, null, null, removed.getUrl()));
  }

  public Space getSpaceByDisplayName(final String spaceDisplayName) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(spaceDisplayName);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = storage.getSpaceByDisplayName(spaceDisplayName);
            if (space != null) {
              SpaceKey key = new SpaceKey(space.getId());
              eXoCacheSpaceId.put(key, new SpaceData(space));
              return key;
            }
            else {
              return null;
            }
          }
        },
        refKey);

    //
    if (key != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }

  }

  public void saveSpace(final Space space, final boolean isNew) throws SpaceStorageException {

    //
    storage.saveSpace(space, isNew);

    //
    SpaceData removed = eXoCacheSpaceId.remove(new SpaceKey(space.getId()));
    if (removed != null) {
      cleanRef(removed);
    }

  }

  public void deleteSpace(final String id) throws SpaceStorageException {

    //
    storage.deleteSpace(id);

    //
    SpaceData removed = eXoCacheSpaceId.remove(new SpaceKey(id));
    if (removed != null) {
      cleanRef(removed);
    }

  }

  public int getMemberSpacesCount(final String userId) throws SpaceStorageException {
    return storage.getMemberSpacesCount(userId);
  }

  public int getMemberSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    return storage.getMemberSpacesByFilterCount(userId, spaceFilter);
  }

  public List<Space> getMemberSpaces(final String userId) throws SpaceStorageException {
    return storage.getMemberSpaces(userId);
  }

  public List<Space> getMemberSpaces(final String userId, final long offset, final long limit) throws SpaceStorageException {
    return storage.getMemberSpaces(userId, offset, limit);
  }

  public List<Space> getMemberSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {
    return storage.getMemberSpacesByFilter(userId, spaceFilter, offset, limit);
  }

  public int getPendingSpacesCount(final String userId) throws SpaceStorageException {
    return storage.getPendingSpacesCount(userId);
  }

  public int getPendingSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    return storage.getPendingSpacesByFilterCount(userId, spaceFilter);
  }

  public List<Space> getPendingSpaces(final String userId) throws SpaceStorageException {
    return storage.getPendingSpaces(userId);
  }

  public List<Space> getPendingSpaces(final String userId, final long offset, final long limit) throws SpaceStorageException {
    return storage.getPendingSpaces(userId, offset, limit);
  }

  public List<Space> getPendingSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {
    return storage.getPendingSpacesByFilter(userId, spaceFilter, offset, limit);
  }

  public int getInvitedSpacesCount(final String userId) throws SpaceStorageException {
    return storage.getInvitedSpacesCount(userId);
  }

  public int getInvitedSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    return storage.getInvitedSpacesByFilterCount(userId, spaceFilter);
  }

  public List<Space> getInvitedSpaces(final String userId) throws SpaceStorageException {
    return storage.getInvitedSpaces(userId);
  }

  public List<Space> getInvitedSpaces(final String userId, final long offset, final long limit) throws SpaceStorageException {
    return storage.getInvitedSpaces(userId, offset, limit);
  }

  public List<Space> getInvitedSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {
    return storage.getInvitedSpacesByFilter(userId, spaceFilter, offset, limit);
  }

  public int getPublicSpacesCount(final String userId) throws SpaceStorageException {
    return storage.getPublicSpacesCount(userId);
  }

  public int getPublicSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    return storage.getPublicSpacesByFilterCount(userId, spaceFilter);
  }

  public List<Space> getPublicSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {
    return storage.getPublicSpacesByFilter(userId, spaceFilter, offset, limit);
  }

  public List<Space> getPublicSpaces(final String userId) throws SpaceStorageException {
    return storage.getPublicSpaces(userId);
  }

  public List<Space> getPublicSpaces(final String userId, final long offset, final long limit) throws SpaceStorageException {
    return storage.getPublicSpaces(userId, offset, limit);
  }

  public int getAccessibleSpacesCount(final String userId) throws SpaceStorageException {
    return storage.getAccessibleSpacesCount(userId);
  }

  public int getAccessibleSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    return storage.getAccessibleSpacesByFilterCount(userId, spaceFilter);
  }

  public List<Space> getAccessibleSpaces(final String userId) throws SpaceStorageException {
    return storage.getAccessibleSpaces(userId);
  }

  public List<Space> getAccessibleSpaces(final String userId, final long offset, final long limit) throws SpaceStorageException {
    return storage.getAccessibleSpaces(userId, offset, limit);
  }

  public List<Space> getAccessibleSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {
    return storage.getAccessibleSpacesByFilter(userId, spaceFilter, offset, limit);
  }

  public int getEditableSpacesCount(final String userId) throws SpaceStorageException {
    return storage.getEditableSpacesCount(userId);
  }

  public int getEditableSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    return storage.getEditableSpacesByFilterCount(userId, spaceFilter);
  }

  public List<Space> getEditableSpaces(final String userId) throws SpaceStorageException {
    return storage.getEditableSpaces(userId);
  }

  public List<Space> getEditableSpaces(final String userId, final long offset, final long limit) throws SpaceStorageException {
    return storage.getEditableSpaces(userId, offset, limit);
  }

  public List<Space> getEditableSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {
    return storage.getEditableSpacesByFilter(userId, spaceFilter, offset, limit);
  }

  public int getAllSpacesCount() throws SpaceStorageException {
    return storage.getAllSpacesCount();
  }

  public List<Space> getAllSpaces() throws SpaceStorageException {
    return storage.getAllSpaces();
  }

  public int getAllSpacesByFilterCount(final SpaceFilter spaceFilter) {
    return storage.getAllSpacesByFilterCount(spaceFilter);
  }

  public List<Space> getSpaces(final long offset, final long limit) throws SpaceStorageException {
    return storage.getSpaces(offset, limit);
  }

  public List<Space> getSpacesByFilter(final SpaceFilter spaceFilter, final long offset, final long limit) {
    return storage.getSpacesByFilter(spaceFilter, offset, limit);
  }

  public Space getSpaceById(final String id) throws SpaceStorageException {

    //
    SpaceKey key = new SpaceKey(id);

    //
    SpaceData data = spaceCache.get(
        new ServiceContext<SpaceData>() {
          public SpaceData execute() {
            Space space = storage.getSpaceById(id);
            if (space != null) {
              return new SpaceData(space);
            }
            else {
              return null;
            }
          }
        },
        key);

    if (data != null) {
      return data.build();
    }
    else {
      return null;
    }
    
  }

  public Space getSpaceByPrettyName(final String spacePrettyName) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, spacePrettyName);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = storage.getSpaceByPrettyName(spacePrettyName);
            if (space != null) {
              SpaceKey key = new SpaceKey(space.getId());
              eXoCacheSpaceId.put(key, new SpaceData(space));
              return key;
            }
            else {
              return null;
            }
          }
        },
        refKey);

    //
    if (key != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }

  }

  public Space getSpaceByGroupId(final String groupId) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, null, groupId);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = storage.getSpaceByGroupId(groupId);
            if (space != null) {
              SpaceKey key = new SpaceKey(space.getId());
              eXoCacheSpaceId.put(key, new SpaceData(space));
              return key;
            }
            else {
              return null;
            }
          }
        },
        refKey);

    //
    if (key != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }

  }

  public Space getSpaceByUrl(final String url) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, null, null, url);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = storage.getSpaceByUrl(url);
            if (space != null) {
              SpaceKey key = new SpaceKey(space.getId());
              eXoCacheSpaceId.put(key, new SpaceData(space));
              return key;
            }
            else {
              return null;
            }
          }
        },
        refKey);

    //
    if (key != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }
    
  }
}
