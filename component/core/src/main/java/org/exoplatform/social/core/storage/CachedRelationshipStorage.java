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
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.RelationshipData;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipCountKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipIdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedRelationshipStorage implements RelationshipStorage {


  private final ExoCache<RelationshipKey, RelationshipData> eXoCacheRelationshipId;
  private final ExoCache<RelationshipIdentityKey, RelationshipKey> eXoCacheRelationshipIdentity;
  private final ExoCache<RelationshipCountKey, IntegerData> eXoCacheRelationshipCount;

  private final FutureExoCache<RelationshipKey, RelationshipData, ServiceContext<RelationshipData>> relationshipCache;
  private final FutureExoCache<RelationshipIdentityKey, RelationshipKey, ServiceContext<RelationshipKey>> relationshipCacheIdentity;
  private final FutureExoCache<RelationshipCountKey, IntegerData, ServiceContext<IntegerData>> relationshipCount;

  private final RelationshipStorageImpl storage;

  private static final RelationshipKey RELATIONSHIP_NOT_FOUND = new RelationshipKey(null);

  public CachedRelationshipStorage(final RelationshipStorageImpl storage, final SocialStorageCacheService cacheService) {

    this.storage = storage;

    eXoCacheRelationshipId = cacheService.getRelationshipCacheById();
    eXoCacheRelationshipIdentity = cacheService.getRelationshipCacheByIdentity();
    eXoCacheRelationshipCount = cacheService.getRelationshipCount();

    relationshipCache = cacheService.createRelationshipCacheById();
    relationshipCacheIdentity = cacheService.createRelationshipCacheByIdentity();
    relationshipCount = cacheService.createRelationshipCacheCount();

  }

  public Relationship saveRelationship(final Relationship relationship) throws RelationshipStorageException {

    Relationship r = storage.saveRelationship(relationship);

    RelationshipIdentityKey identityKey1 = new RelationshipIdentityKey(r.getSender().getId(), r.getReceiver().getId());
    RelationshipIdentityKey identityKey2 = new RelationshipIdentityKey(r.getReceiver().getId(), r.getSender().getId());
    RelationshipKey key = new RelationshipKey(relationship.getId());

    eXoCacheRelationshipId.put(key, new RelationshipData(r));
    eXoCacheRelationshipIdentity.put(identityKey1, key);
    eXoCacheRelationshipIdentity.put(identityKey2, key);

    return r;

  }

  public void removeRelationship(final Relationship relationship) throws RelationshipStorageException {

    storage.removeRelationship(relationship);

    eXoCacheRelationshipId.remove(new RelationshipKey(relationship.getId()));
    
  }

  public Relationship getRelationship(final String uuid) throws RelationshipStorageException {

    //
    RelationshipKey key = new RelationshipKey(uuid);

    //
    RelationshipData data = relationshipCache.get(
        new ServiceContext<RelationshipData>() {
          public RelationshipData execute() {
            Relationship got = storage.getRelationship(uuid);
            if (got != null) {
              return new RelationshipData(storage.getRelationship(uuid));
            }
            return null;
          }
        },
        key
    );

    //
    if (data != null) {
      return data.build();
    }
    return null;
    
  }

  public List<Relationship> getSenderRelationships(
      final Identity sender, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {
    return storage.getSenderRelationships(sender, type, listCheckIdentity);
  }

  public List<Relationship> getSenderRelationships(
      final String senderId, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {
    return storage.getSenderRelationships(senderId, type, listCheckIdentity);
  }

  public List<Relationship> getReceiverRelationships(
      final Identity receiver, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {
    return storage.getReceiverRelationships(receiver, type, listCheckIdentity);
  }

  public Relationship getRelationship(final Identity identity1, final Identity identity2)
      throws RelationshipStorageException {

    //
    final RelationshipIdentityKey key = new RelationshipIdentityKey(identity1.getId(), identity2.getId());

    //
    RelationshipKey gotKey = relationshipCacheIdentity.get(
        new ServiceContext<RelationshipKey>() {
          public RelationshipKey execute() {
            Relationship got = storage.getRelationship(identity1, identity2);
            if (got != null) {
              RelationshipKey k = new RelationshipKey(got.getId());
              eXoCacheRelationshipIdentity.put(key, k);
              return k;
            }
            else {
              eXoCacheRelationshipIdentity.put(key, RELATIONSHIP_NOT_FOUND);
              return RELATIONSHIP_NOT_FOUND;
            }
          }
        },
        key
    );

    //
    if (gotKey != null && !gotKey.equals(RELATIONSHIP_NOT_FOUND)) {
      return getRelationship(gotKey.getId());
    }
    else {
      return null;
    }

  }

  public List<Relationship> getRelationships(
      final Identity identity, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {
    return storage.getRelationships(identity, type, listCheckIdentity);
  }

  public List<Identity> getRelationships(final Identity identity, final long offset, final long limit)
      throws RelationshipStorageException {
    return storage.getRelationships(identity, offset, limit);
  }

  public List<Identity> getIncomingRelationships(final Identity receiver, final long offset, final long limit)
      throws RelationshipStorageException {
    return storage.getIncomingRelationships(receiver, offset, limit);
  }

  public int getIncomingRelationshipsCount(final Identity receiver) throws RelationshipStorageException {

    //
    IdentityKey iKey = new IdentityKey(receiver);
    RelationshipCountKey key = new RelationshipCountKey(iKey, RelationshipCountKey.CountType.INCOMMING);

    //
    return relationshipCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getIncomingRelationshipsCount(receiver));
          }
        },
        key)
        .build();

  }

  public List<Identity> getOutgoingRelationships(final Identity sender, final long offset, final long limit)
      throws RelationshipStorageException {

    return storage.getOutgoingRelationships(sender, offset, limit);

  }

  public int getOutgoingRelationshipsCount(final Identity sender) throws RelationshipStorageException {

    //
    IdentityKey iKey = new IdentityKey(sender);
    RelationshipCountKey key = new RelationshipCountKey(iKey, RelationshipCountKey.CountType.OUTGOING);

    //
    return relationshipCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getOutgoingRelationshipsCount(sender));
          }
        },
        key)
        .build();

  }

  public int getRelationshipsCount(final Identity identity) throws RelationshipStorageException {

    //
    IdentityKey iKey = new IdentityKey(identity);
    RelationshipCountKey key = new RelationshipCountKey(iKey, RelationshipCountKey.CountType.RELATIONSHIP);

    //
    return relationshipCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getRelationshipsCount(identity));
          }
        },
        key)
        .build();

  }

  public List<Identity> getConnections(final Identity identity, final long offset, final long limit)
      throws RelationshipStorageException {
    return storage.getConnections(identity, offset, limit);
  }

  public List<Identity> getConnections(final Identity identity) throws RelationshipStorageException {
    return storage.getConnections(identity);
  }

  public int getConnectionsCount(final Identity identity) throws RelationshipStorageException {

    //
    IdentityKey iKey = new IdentityKey(identity);
    RelationshipCountKey key = new RelationshipCountKey(iKey, RelationshipCountKey.CountType.CONNECTION);

    //
    return relationshipCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getConnectionsCount(identity));
          }
        },
        key)
        .build();

  }
}
