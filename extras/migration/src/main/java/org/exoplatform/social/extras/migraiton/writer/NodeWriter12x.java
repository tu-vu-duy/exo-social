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

package org.exoplatform.social.extras.migraiton.writer;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.extras.migraiton.io.NodeData;
import org.exoplatform.social.extras.migraiton.io.NodeStreamHandler;
import org.exoplatform.social.extras.migraiton.io.WriterContext;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class NodeWriter12x implements NodeWriter {

  private final IdentityStorage identityStorage;
  private final RelationshipStorage relationshipStorage;
  private final SpaceStorage spaceStorage;
  private final ActivityStorage activityStorage;

  private final Session session;

  public NodeWriter12x(final IdentityStorage identityStorage, final RelationshipStorage relationshipStorage, final SpaceStorage spaceStorage, final ActivityStorage activityStorage, final Session session) {
    this.identityStorage = identityStorage;
    this.relationshipStorage = relationshipStorage;
    this.spaceStorage = spaceStorage;
    this.activityStorage = activityStorage;
    this.session = session;
  }

  public void writeIdentities(final InputStream is, final WriterContext ctx) {

    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {
      String provider = (String) currentData.getProperties().get("exo:providerId");
      String remote = (String) currentData.getProperties().get("exo:remoteId");
      Identity identity = new Identity(provider, remote);
      identityStorage.saveIdentity(identity);

      ctx.put((String) currentData.getProperties().get("jcr:uuid"), (String) currentData.getProperties().get("exo:remoteId"));
      
    }
    
  }

  public void writeSpaces(final InputStream is, final WriterContext ctx) {

  }

  public void writeProfiles(final InputStream is, final WriterContext ctx) {

  }

  public void writeActivities(final InputStream is, final WriterContext ctx) {

  }

  public void writeRelationships(final InputStream is, final WriterContext ctx) {

    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {

      String id1 = ((String) currentData.getProperties().get("exo:identity1Id"));
      String id2 = ((String) currentData.getProperties().get("exo:identity2Id"));
      String status = (String) currentData.getProperties().get("exo:status");

      String remoteId1 = ctx.get(id1);
      String remoteId2 = ctx.get(id2);

      Identity i1 = identityStorage.findIdentity("organization", remoteId1);
      Identity i2 = identityStorage.findIdentity("organization", remoteId2);

      Relationship.Type type = null;
      if ("CONFIRM".equals(status)) {
        type = Relationship.Type.CONFIRMED;
      }
      else if ("PENDING".equals(status)) {
        type = Relationship.Type.PENDING;
      }

      Relationship relationship = new Relationship(i1, i2, type);
      relationshipStorage.saveRelationship(relationship);

    }

  }

}
