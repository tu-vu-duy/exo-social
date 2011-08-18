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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.extras.migraiton.io.NodeData;
import org.exoplatform.social.extras.migraiton.io.NodeStreamHandler;
import org.exoplatform.social.extras.migraiton.io.WriterContext;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
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

  private static final Log LOG = ExoLogger.getLogger(NodeWriter12x.class);

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
      try {
        LOG.info("Write identity " + provider + "/" + remote);
        identityStorage.saveIdentity(identity);
      }
      catch (Exception e) {
        LOG.error(e.getMessage());
      }

      ctx.put((String) currentData.getProperties().get("jcr:uuid"), (String) currentData.getProperties().get("exo:remoteId"));
      ctx.put(currentData.getProperties().get("jcr:uuid") + "-newId", identity.getId());

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



      System.out.println("Write space/" + space.getPrettyName());
      try {
        identityStorage.saveIdentity(identity);
        System.out.println("Write space " + space.getPrettyName());
        spaceStorage.saveSpace(space, true);
      }
      catch (Exception e) {
        System.out.println("ERROR");
      }

      ctx.put((String) currentData.getProperties().get("jcr:uuid"), space.getPrettyName());
      ctx.put(currentData.getProperties().get("jcr:uuid") + "-newId", identity.getId());
      
    }

  }

  public void writeProfiles(final InputStream is, final WriterContext ctx) {

    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {

      String url = (String) currentData.getProperties().get("Url");
      String firstName = (String) currentData.getProperties().get("firstName");
      String lastName = (String) currentData.getProperties().get("lastName");
      String position = (String) currentData.getProperties().get("position");
      String username = (String) currentData.getProperties().get("username");
      String identityOld = (String) currentData.getProperties().get("exo:identity");

      String identityId = ctx.get(identityOld + "-newId");
      if (identityId != null) {
        Identity i = identityStorage.findIdentityById(identityId);

        Profile profile = new Profile(i);
        profile.setProperty(Profile.URL, url);
        profile.setProperty(Profile.FIRST_NAME, firstName);
        profile.setProperty(Profile.LAST_NAME, lastName);
        profile.setProperty(Profile.POSITION, position);
        profile.setProperty(Profile.USERNAME, username);
        i.setProfile(profile);

        try {
          LOG.info("Write profile " + i.getProviderId() + "/" + i.getRemoteId());
          identityStorage.saveProfile(profile);
        }
        catch (Exception e) {
          LOG.error(e.getMessage());
        }
      }

    }

  }

  public void writeActivities(final InputStream is, final WriterContext ctx) {

    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {

      String replyToId = (String) currentData.getProperties().get("exo:replyToId");

      if ("IS_COMMENT".equals(replyToId)) {
        continue;
      }

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
      String[] likes = (String[]) currentData.getProperties().get("exo:like");

      Map<String, String> paramMap = readParams(params);
      if (paramMap != null) {
        activity.setTemplateParams(paramMap);
      }


      activity.setTitle(title);
      activity.setTitleId(titleTemplate);
      activity.setType(type);
      activity.setPostedTime(Long.parseLong(postedTime));
      activity.setUpdated(new Date(Long.parseLong(updatedTimestamp)));

      if (likes != null) {
        String[] newLikes = new String[likes.length];
        for (int i = 0; i < likes.length; ++i) {
          newLikes[i] = ctx.get(likes[i] + "-newId");
        }
        activity.setLikeIdentityIds(newLikes);
      }

      try {
        String userName = ctx.get(userId);
        Identity userIdentity = identityStorage.findIdentity("organization", userName);
        activity.setUserId(userIdentity.getId());
      }
      catch (IdentityStorageException e) {
        try {
          Node oldSpaceIdentity = session.getNodeByUUID(userId);
          String oldSpaceId = oldSpaceIdentity.getProperty("exo:remoteId").getString();
          String spaceName = ctx.get(oldSpaceId);
          Identity spaceIdentity = identityStorage.findIdentity("space", spaceName);
          activity.setUserId(spaceIdentity.getId());
        }
        catch (RepositoryException e1) {
          e1.printStackTrace();
        }
      }


      System.out.println("Write activity " + owner.getRemoteId() + " - " + activity.getPostedTime() + " - " + type);
      try {
        activityStorage.saveActivity(owner, activity);
      }
      catch (Exception e) {
        System.out.println("ERROR");
      }
      
      if (replyToId != null) {
        String[] ids = replyToId.split(",");

        for (String id : ids) {

          try {
            Node node = session.getNodeByUUID(id);
            ExoSocialActivity comment = buildActivityFromNode(node, ctx);

            System.out.println("Write comment " + activity.getPostedTime() + " - " + comment.getPostedTime() + " - " + type);
            try {
              activityStorage.saveComment(activity, comment);
            }
            catch (Exception e) {
              System.out.println("ERROR");
            }
          }
          catch (RepositoryException e) {
            e.printStackTrace();
          }
        }
      }

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

      
      System.out.println("Write relationship " + i1.getRemoteId() + " - " + i2.getRemoteId() + " - " + type);
      try {
        relationshipStorage.saveRelationship(relationship);
      }
      catch (Exception e) {
        System.out.println("ERROR");
      }

    }

  }

  public void generateOrganization() {

    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      Node identities = session.getRootNode().getNode("production/soc:providers/soc:organization");
      NodeIterator it = identities.getNodes();
      UserHandler userHandler = organizationService.getUserHandler();
      GroupHandler groupHandler = organizationService.getGroupHandler();
      MembershipHandler membershipHandler = organizationService.getMembershipHandler();
      MembershipTypeHandler membershipTypeHandler = organizationService.getMembershipTypeHandler();

      Group group = groupHandler.findGroupById("/platform/users");
      MembershipType type = membershipTypeHandler.findMembershipType("member");
      while (it.hasNext()) {
        Node currentNode = it.nextNode();
        User user = userHandler.createUserInstance(currentNode.getProperty("soc:remoteId").getString());
        user.setPassword("gtn");

        userHandler.createUser(user, false);
        membershipHandler.linkMembership(user, group, type, false);
        LOG.info("Create user " + user.getUserName());

      }
    }
    catch (Exception e) {
      LOG.error(e.getMessage());
    }
    RequestLifeCycle.end();

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

  private Map<String, String> readParams(String[] params) {

    if (params != null) {
      Map<String, String> paramMap = new HashMap<String, String>();
      for(String param : params) {
        String[] keyValue = param.split("=");
        paramMap.put(keyValue[0], keyValue[1]);
      }
      if (paramMap.size() > 0) {
        return paramMap;
      }
    }
    return null;
  }

  private ExoSocialActivity buildActivityFromNode(Node node, WriterContext ctx) {

    ExoSocialActivity comment = new ExoSocialActivityImpl();

    String title = getPropertyValye(node, "exo:title");
    String titleTemplate = getPropertyValye(node, "exo:titleTemplate");
    String type = getPropertyValye(node, "exo:type");
    String userId = getPropertyValye(node, "exo:userId");
    String postedTime = getPropertyValye(node, "exo:postedTime");
    String updatedTimestamp = getPropertyValye(node, "exo:updatedTimestamp");

    comment.setTitle(title);
    comment.setTitleId(titleTemplate);
    comment.setType(type);
    comment.setPostedTime(Long.parseLong(postedTime));
    comment.setUpdated(new Date(Long.parseLong(updatedTimestamp)));

    String userName = ctx.get(userId);
    String newUserId = identityStorage.findIdentity("organization", userName).getId();
    comment.setUserId(newUserId);

    return comment;
  }

  private String getPropertyValye(Node node, String propertyName) {
    try {
      return node.getProperty(propertyName).getString();
    }
    catch (RepositoryException e) {
      return null;
    }
  }

}
