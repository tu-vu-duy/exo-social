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

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import java.util.List;

/**
 * {@link SynchronizedRelationshipStorage} as a decorator to {@link org.exoplatform.social.core.storage.api.RelationshipStorage} for synchronization
 * management.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SynchronizedRelationshipStorage extends RelationshipStorageImpl {

  /*private final ExoCache<RelationshipKey, RelationshipData> relationshipCacheById;
  private final ExoCache<RelationshipIdentityKey, RelationshipKey> relationshipCacheByIdentity;
  private final ExoCache<IdentityKey, SimpleCacheData<Integer>> relationshipCountConnection;

  private static final RelationshipKey RELATIONSHIP_NOT_FOUND = new RelationshipKey(null);*/

  /**
   * {@inheritDoc}
   */
  public SynchronizedRelationshipStorage(final IdentityStorage identityStorage) {
    super(identityStorage);

    /*relationshipCacheById = caches.getRelationshipCacheById();
    relationshipCacheByIdentity = caches.getRelationshipCacheByIdentity();
    relationshipCountConnection = caches.getRelationshipCountConnection();*/
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Relationship saveRelationship(final Relationship relationship) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {

      /*Relationship saved = super.saveRelationship(relationship);

      RelationshipIdentityKey identityKey1 = new RelationshipIdentityKey(saved.getSender().getId(), saved.getReceiver().getId());
      RelationshipIdentityKey identityKey2 = new RelationshipIdentityKey(saved.getReceiver().getId(), saved.getSender().getId());
      RelationshipKey key = new RelationshipKey(relationship.getId());

      relationshipCacheById.put(key, new RelationshipData(saved));
      relationshipCacheByIdentity.put(identityKey1, key);
      relationshipCacheByIdentity.put(identityKey2, key);
      relationshipCountConnection.remove(identityKey1);
      relationshipCountConnection.remove(identityKey2);

      return saved;*/
      return super.saveRelationship(relationship);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeRelationship(final Relationship relationship) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {

      super.removeRelationship(relationship);
      /*relationshipCacheById.remove(new RelationshipKey(relationship.getId()));

      RelationshipIdentityKey identityKey1 = new RelationshipIdentityKey(relationship.getSender().getId(), relationship.getReceiver().getId());
      RelationshipIdentityKey identityKey2 = new RelationshipIdentityKey(relationship.getReceiver().getId(), relationship.getSender().getId());
      relationshipCountConnection.remove(identityKey1);
      relationshipCountConnection.remove(identityKey2);*/

    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Relationship getRelationship(final String uuid) throws RelationshipStorageException {

    /*RelationshipKey key = new RelationshipKey(uuid);
    RelationshipData data = relationshipCacheById.get(key);

    if (data != null) {
      return data.build();
    }*/

    boolean created = startSynchronization();
    try {
      /*Relationship relationship = super.getRelationship(uuid);
      if (relationship != null) {
        relationshipCacheById.put(key, new RelationshipData(relationship));
      }
      return relationship;*/
      return super.getRelationship(uuid);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Relationship> getSenderRelationships(final Identity sender, final Relationship.Type type,
                                                   final List<Identity> listCheckIdentity)
                                                   throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getSenderRelationships(sender, type, listCheckIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Relationship> getSenderRelationships(final String senderId, final Relationship.Type type,
                                                   final List<Identity> listCheckIdentity)
                                                   throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getSenderRelationships(senderId, type, listCheckIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Relationship> getReceiverRelationships(final Identity receiver, final Relationship.Type type,
                                                     final List<Identity> listCheckIdentity)
                                                     throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getReceiverRelationships(receiver, type, listCheckIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Relationship getRelationship(final Identity identity1, final Identity identity2)
                                      throws RelationshipStorageException {

    /*RelationshipIdentityKey identityKey = new RelationshipIdentityKey(identity1.getId(), identity2.getId());
    RelationshipKey key = relationshipCacheByIdentity.get(identityKey);

    if (key == RELATIONSHIP_NOT_FOUND) {
      return null;
    }

    if (key != null) {
      RelationshipData data = relationshipCacheById.get(key);
      if (data != null) {
        return data.build();
      }
      else {
        relationshipCacheByIdentity.remove(identityKey);
      }
    }*/

    boolean created = startSynchronization();
    try {
      /*Relationship relationship = super.getRelationship(identity1, identity2);
      if (relationship != null) {
        relationshipCacheByIdentity.put(identityKey, key);
        key = new RelationshipKey(relationship.getId());
        relationshipCacheById.put(key, new RelationshipData(relationship));
      }
      else {
        relationshipCacheByIdentity.put(identityKey, RELATIONSHIP_NOT_FOUND);
      }
      return relationship;*/
      return super.getRelationship(identity1, identity2);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Relationship> getRelationships(final Identity identity, final Relationship.Type type,
                                             final List<Identity> listCheckIdentity)
                                             throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getRelationships(identity, type, listCheckIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Identity> getRelationships(final Identity identity, final long offset, final long limit)
                                         throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getRelationships(identity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Identity> getIncomingRelationships(final Identity receiver, final long offset, final long limit)
                                                 throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getIncomingRelationships(receiver, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getIncomingRelationshipsCount(final Identity receiver) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getIncomingRelationshipsCount(receiver);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Identity> getOutgoingRelationships(final Identity sender, final long offset, final long limit)
                                                 throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getOutgoingRelationships(sender, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getOutgoingRelationshipsCount(final Identity sender) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getOutgoingRelationshipsCount(sender);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getRelationshipsCount(final Identity identity) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getRelationshipsCount(identity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Identity> getConnections(final Identity identity, final long offset, final long limit)
                                       throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getConnections(identity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getConnectionsCount(final Identity identity) throws RelationshipStorageException {

    /*IdentityKey key = new IdentityKey(identity);
    SimpleCacheData<Integer> data = relationshipCountConnection.get(key);

    if (data != null) {
      return data.build();
    }*/

    boolean created = startSynchronization();
    try {
      /*int got = super.getConnectionsCount(identity);
      relationshipCountConnection.put(key, new SimpleCacheData<Integer>(got));
      return got;*/
      return super.getConnectionsCount(identity);
    }
    finally {
      stopSynchronization(created);
    }

  }
}
