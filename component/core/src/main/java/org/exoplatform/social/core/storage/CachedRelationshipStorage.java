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

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedRelationshipStorage implements RelationshipStorage {

  private final RelationshipStorageImpl storage;

  public CachedRelationshipStorage(final RelationshipStorageImpl storage) {
    this.storage = storage;
  }

  public Relationship saveRelationship(final Relationship relationship) throws RelationshipStorageException {
    return storage.saveRelationship(relationship);
  }

  public void removeRelationship(final Relationship relationship) throws RelationshipStorageException {
    storage.removeRelationship(relationship);
  }

  public Relationship getRelationship(final String uuid) throws RelationshipStorageException {
    return storage.getRelationship(uuid);
  }

  public List<Relationship> getSenderRelationships(final Identity sender, final Relationship.Type type, final List<Identity> listCheckIdentity) throws RelationshipStorageException {
    return storage.getSenderRelationships(sender, type, listCheckIdentity);
  }

  public List<Relationship> getSenderRelationships(final String senderId, final Relationship.Type type, final List<Identity> listCheckIdentity) throws RelationshipStorageException {
    return storage.getSenderRelationships(senderId, type, listCheckIdentity);
  }

  public List<Relationship> getReceiverRelationships(final Identity receiver, final Relationship.Type type, final List<Identity> listCheckIdentity) throws RelationshipStorageException {
    return storage.getReceiverRelationships(receiver, type, listCheckIdentity);
  }

  public Relationship getRelationship(final Identity identity1, final Identity identity2) throws RelationshipStorageException {
    return storage.getRelationship(identity1, identity2);
  }

  public List<Relationship> getRelationships(final Identity identity, final Relationship.Type type, final List<Identity> listCheckIdentity) throws RelationshipStorageException {
    return storage.getRelationships(identity, type, listCheckIdentity);
  }

  public List<Identity> getRelationships(final Identity identity, final long offset, final long limit) throws RelationshipStorageException {
    return storage.getRelationships(identity, offset, limit);
  }

  public List<Identity> getIncomingRelationships(final Identity receiver, final long offset, final long limit) throws RelationshipStorageException {
    return storage.getIncomingRelationships(receiver, offset, limit);
  }

  public int getIncomingRelationshipsCount(final Identity receiver) throws RelationshipStorageException {
    return storage.getIncomingRelationshipsCount(receiver);
  }

  public List<Identity> getOutgoingRelationships(final Identity sender, final long offset, final long limit) throws RelationshipStorageException {
    return storage.getOutgoingRelationships(sender, offset, limit);
  }

  public int getOutgoingRelationshipsCount(final Identity sender) throws RelationshipStorageException {
    return storage.getOutgoingRelationshipsCount(sender);
  }

  public int getRelationshipsCount(final Identity identity) throws RelationshipStorageException {
    return storage.getRelationshipsCount(identity);
  }

  public List<Identity> getConnections(final Identity identity, final long offset, final long limit) throws RelationshipStorageException {
    return storage.getConnections(identity, offset, limit);
  }

  public List<Identity> getConnections(final Identity identity) throws RelationshipStorageException {
    return storage.getConnections(identity);
  }

  public int getConnectionsCount(final Identity identity) throws RelationshipStorageException {
    return storage.getConnectionsCount(identity);
  }
}
