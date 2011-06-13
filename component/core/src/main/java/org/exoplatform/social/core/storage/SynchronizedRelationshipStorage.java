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

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.cache.RelationshipData;
import org.exoplatform.social.core.storage.cache.RelationshipIdentityKey;
import org.exoplatform.social.core.storage.cache.RelationshipKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SynchronizedRelationshipStorage extends RelationshipStorage {

  private final ExoCache<RelationshipKey, RelationshipData> relationshipCacheById;
  private final ExoCache<RelationshipIdentityKey, RelationshipKey> relationshipCacheByIdentity;

  private static final RelationshipKey RELATIONSHIP_NOT_FOUND = new RelationshipKey(null);

  public SynchronizedRelationshipStorage() {
    super();

    relationshipCacheById = caches.getRelationshipCacheById();
    relationshipCacheByIdentity = caches.getRelationshipCacheByIdentity();
  }

  @Override
  public Relationship saveRelationship(final Relationship relationship) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {

      Relationship saved = super.saveRelationship(relationship);

      RelationshipIdentityKey identityKey1 = new RelationshipIdentityKey(saved.getSender().getId(), saved.getReceiver().getId());
      RelationshipIdentityKey identityKey2 = new RelationshipIdentityKey(saved.getReceiver().getId(), saved.getSender().getId());
      RelationshipKey key = new RelationshipKey(relationship.getId());

      relationshipCacheById.put(key, new RelationshipData(saved));
      relationshipCacheByIdentity.put(identityKey1, key);
      relationshipCacheByIdentity.put(identityKey2, key);
      
      return saved;
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void removeRelationship(final Relationship relationship) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      super.removeRelationship(relationship);
      relationshipCacheById.remove(new RelationshipKey(relationship.getId()));
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Relationship getRelationship(final String uuid) throws RelationshipStorageException {

    RelationshipKey key = new RelationshipKey(uuid);
    RelationshipData data = relationshipCacheById.get(key);

    if (data != null) {
      return data.build();
    }

    boolean created = startSynchronization();
    try {
      Relationship relationship = super.getRelationship(uuid);
      if (relationship != null) {
        relationshipCacheById.put(key, new RelationshipData(relationship));
      }
      return relationship;
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public List<Relationship> getSenderRelationships(final Identity sender, final Relationship.Type type, final List<Identity> listCheckIdentity) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getSenderRelationships(sender, type, listCheckIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public List<Relationship> getReceiverRelationships(final Identity receiver, final Relationship.Type type, final List<Identity> listCheckIdentity) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getReceiverRelationships(receiver, type, listCheckIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Relationship getRelationship(final Identity identity1, final Identity identity2) throws RelationshipStorageException {

    RelationshipIdentityKey identityKey = new RelationshipIdentityKey(identity1.getId(), identity2.getId());
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
    }

    boolean created = startSynchronization();
    try {
      Relationship relationship = super.getRelationship(identity1, identity2);
      if (relationship != null) {
        relationshipCacheByIdentity.put(identityKey, key);
        key = new RelationshipKey(relationship.getId());
        relationshipCacheById.put(key, new RelationshipData(relationship));
      }
      else {
        relationshipCacheByIdentity.put(identityKey, RELATIONSHIP_NOT_FOUND);
      }
      return relationship;
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public List<Relationship> getRelationships(final Identity identity, final Relationship.Type type, final List<Identity> listCheckIdentity) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getRelationships(identity, type, listCheckIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getConnectionsCount(final Identity identity) throws RelationshipStorageException {

    boolean created = startSynchronization();
    try {
      return super.getConnectionsCount(identity);
    }
    finally {
      stopSynchronization(created);
    }

  }
}
