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

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.extras.migraiton.io.NodeData;
import org.exoplatform.social.extras.migraiton.io.NodeStreamHandler;
import org.exoplatform.social.extras.migraiton.io.WriterContext;

import javax.jcr.Session;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class NodeWriter12x implements NodeWriter {

  private final IdentityStorage identityStorage;
  private final RelationshipStorage relationshipStorage;
  private final SpaceStorage spaceStorage;
  private final ActivityStorage activityStorage;
  private final OrganizationService organizationService;

  private final Session session;

  public NodeWriter12x(final IdentityStorage identityStorage, final RelationshipStorage relationshipStorage, final SpaceStorage spaceStorage, final ActivityStorage activityStorage, final OrganizationService organizationService, final Session session) {
    this.identityStorage = identityStorage;
    this.relationshipStorage = relationshipStorage;
    this.spaceStorage = spaceStorage;
    this.activityStorage = activityStorage;
    this.organizationService = organizationService;
    this.session = session;
  }

  public void writeIdentities(final InputStream is, final WriterContext ctx) {

    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {
      String provider = (String) currentData.getProperties().get("exo:providerId");

      if ("space".equals(provider)) {
        ctx.put((String) currentData.getProperties().get("jcr:uuid"), (String) currentData.getProperties().get("exo:remoteId"));
        continue;
      }

      String remote = (String) currentData.getProperties().get("exo:remoteId");
      Identity identity = new Identity(provider, remote);
      identityStorage.saveIdentity(identity);

      ctx.put((String) currentData.getProperties().get("jcr:uuid"), (String) currentData.getProperties().get("exo:remoteId"));
      
    }
    
  }

  public void writeSpaces(final InputStream is, final WriterContext ctx) {

    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {

      String name = (String) currentData.getProperties().get("exo:name");
      String description = (String) currentData.getProperties().get("exo:description");
      String groupId = (String) currentData.getProperties().get("exo:groupId");
      String priority = (String) currentData.getProperties().get("exo:priority");
      String registration = (String) currentData.getProperties().get("exo:registration");
      String type = (String) currentData.getProperties().get("exo:type");
      String url = (String) currentData.getProperties().get("exo:url");
      String visibility = (String) currentData.getProperties().get("exo:visibility");

      String[] pendingUsers = (String[]) currentData.getProperties().get("exo:pendingUsers");
      String[] invitedUsers = (String[]) currentData.getProperties().get("exo:invitedUsers");
      String[] members = null;
      String[] managers = null;

      try {
        Group group = organizationService.getGroupHandler().findGroupById(groupId);
        Collection<Membership> memberships = organizationService.getMembershipHandler().findMembershipsByGroup(group);
        List<String> membersList = new ArrayList<String>();
        List<String> managersList = new ArrayList<String>();
        for (Membership membership : memberships) {
          if ("member".equals(membership.getMembershipType())) {
            membersList.add(membership.getUserName());
          }
          else if ("manager".equals(membership.getMembershipType())) {
            managersList.add(membership.getUserName());
          }
        }
        if (membersList.size() > 0) {
          members = membersList.toArray(new String[]{});
        }
        if (managersList.size() > 0) {
          managers = managersList.toArray(new String[]{});
        }
      }
      catch (Exception e) {
        e.printStackTrace(); // TODO : manage
      }

      Space space = new Space();
      space.setDisplayName(name);
      space.setDescription(description);
      space.setGroupId(groupId);
      space.setPriority(priority);
      space.setRegistration(registration);
      space.setType(type);
      space.setUrl(url);
      space.setVisibility(visibility);

      space.setPendingUsers(pendingUsers);
      space.setInvitedUsers(invitedUsers);
      space.setMembers(members);
      space.setManagers(managers);

      Identity identity = new Identity("space", space.getPrettyName());

      identityStorage.saveIdentity(identity);
      spaceStorage.saveSpace(space, true);

      ctx.put((String) currentData.getProperties().get("jcr:uuid"), space.getPrettyName());
      
    }

  }

  public void writeProfiles(final InputStream is, final WriterContext ctx) {

  }

  public void writeActivities(final InputStream is, final WriterContext ctx) {


    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {

      String ownerId = extractOwner(currentData);
      Identity owner;

      if (isOrganizationActivity(currentData)) {
        owner = identityStorage.findIdentity("organization", ownerId);
      }
      else if (isSpaceActivity(currentData)) {
        String spaceName = ctx.get(ownerId);
        owner = identityStorage.findIdentity("space", spaceName);
      }
      else {
        continue;
      }

      ExoSocialActivity activity = new ExoSocialActivityImpl();

      String title = (String) currentData.getProperties().get("exo:title");
      String titleTemplate = (String) currentData.getProperties().get("exo:titleTemplate");
      String type = (String) currentData.getProperties().get("exo:type");
      String userId = (String) currentData.getProperties().get("exo:userId");
      String postedTime = (String) currentData.getProperties().get("exo:postedTime");
      String updatedTimestamp = (String) currentData.getProperties().get("exo:updatedTimestamp");

      String[] params = (String[]) currentData.getProperties().get("exo:params");
      if (params != null) {
        Map<String, String> paramMap = new HashMap<String, String>();
        for(String param : params) {
          String[] keyValue = param.split("=");
          paramMap.put(keyValue[0], keyValue[1]);
        }
        if (paramMap.size() > 0) {
          activity.setTemplateParams(paramMap);
        }
      }

      activity.setTitle(title);
      activity.setTitleId(titleTemplate);
      activity.setType(type);
      activity.setPostedTime(Long.parseLong(postedTime));
      activity.setUpdated(new Date(Long.parseLong(updatedTimestamp)));


      String userName = ctx.get(userId);
      Identity userIdentity = identityStorage.findIdentity("organization", userName);
      activity.setUserId(userIdentity.getId());

      activityStorage.saveActivity(owner, activity);

    }

  }

  public void writeRelationships(final InputStream is, final WriterContext ctx) {

    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {

      String id1 = (String) currentData.getProperties().get("exo:identity1Id");
      String id2 = (String) currentData.getProperties().get("exo:identity2Id");
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

  private boolean isOrganizationActivity(NodeData data) {
    return data.getPath().startsWith("/exo:applications/Social_Activity/organization");
  }

  private boolean isSpaceActivity(NodeData data) {
    return data.getPath().startsWith("/exo:applications/Social_Activity/space");
  }

  private String extractOwner(NodeData data) {
    return data.getPath().split("/")[4];
  }

}
