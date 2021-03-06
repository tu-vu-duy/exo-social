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
package org.exoplatform.social.service.rest;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * The security manager helper class for Social Rest APIs.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @author <a href="http://phuonglm.net">phuonglm</a>
 * @since 1.2.0-GA
 * @since Jun 17, 2011
 */
public class SecurityManager {

  /**
   * <p>Checks if an authenticated remoteId of user can access an existing activity.</p>
   *
   * If the authenticated identity is the one who posted that existing activity, return true.<br />
   * If the existing activity belongs to that authenticated identity's activity stream, return true.<br />
   * If the existing activity belongs to that authenticated identity's connections' activity stream, return true.<br />
   * If the existing activity belongs to a space stream that the authenticated is a space member, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the authenticated identity to check
   * @param existingActivity the existing activity to check
   * @return true or false
   */
  public static boolean canAccessActivity(PortalContainer portalContainer, String userIdentity,
                                          ExoSocialActivity existingActivity) {
    //currently, anyone can access an existing activity.
    if(userIdentity !=null && existingActivity !=null){
      return true;
    }
    return false;
  }
  /**
   * <p>Checks if an authenticated identity can access an existing activity.</p>
   *
   * If the authenticated identity is the one who posted that existing activity, return true.<br />
   * If the existing activity belongs to that authenticated identity's activity stream, return true.<br />
   * If the existing activity belongs to that authenticated identity's connections' activity stream, return true.<br />
   * If the existing activity belongs to a space stream that the authenticated is a space member, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the authenticated identity to check
   * @param existingActivity the existing activity to check
   * @return true or false
   */
  public static boolean canAccessActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                          ExoSocialActivity existingActivity) {
    //currently, anyone can access an existing activity.
    if(authenticatedIdentity !=null && existingActivity !=null){
      return true;
    }
    return false;
  }
  
  /**
   * <p>Checks if an poster identity has the permission to post activities on an owner identity stream.</p>
   *
   * If posterIdentity is the same as ownerIdentityStream, return true.<br />
   * If ownerIdentityStream is a user identity, and poster identity is connected to owner identity stream, return true.
   * <br />
   * If ownerIdentityStream is a space identity, and poster identity is a member of that space, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity  the authenticated identity to check
   * @param ownerIdentityStream the identity of an existing activity stream.
   * @return true or false
   */
  public static boolean canPostActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                        Identity ownerIdentityStream) {
    SpaceService spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);
    
    RelationshipManager relationshipManager = (RelationshipManager) portalContainer.getComponentInstanceOfType(RelationshipManager.class);    
    String posterID =  authenticatedIdentity.getId();
    String ownerID = ownerIdentityStream.getId();
    
    // if poserIdentity is the same as ownerIdentityStream, return true
    if(ownerID.equals(posterID)){
      return true;
    }
    
    // Check if owner identity stream is a user identity or space identity
    if(ownerIdentityStream.getProviderId().equals(SpaceIdentityProvider.NAME)){
      //if space identity, check if is a member of
      Space space = spaceService.getSpaceByPrettyName(ownerIdentityStream.getRemoteId());
      if(spaceService.isMember(space, authenticatedIdentity.getRemoteId())){
        return true;
      }
    } else {
      // if user identity, check if connected
      Relationship relationship = relationshipManager.get(authenticatedIdentity, ownerIdentityStream);
      if(relationship!=null && Relationship.Type.CONFIRMED.equals(relationship.getStatus())){
        return true;
      }
    }
    return false;
  }

  
  /**
   * <p>Checks if an authenticated identity has the permission to delete an existing activity.</p>
   *
   * If the authenticated identity is the identity who creates that existing activity, return true.<br />
   * If the authenticated identity is the stream owner of that existing activity, return true. <br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the identity to check
   * @param existingActivity the existing activity
   * @return true or false
   */
  public static boolean canDeleteActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                          ExoSocialActivity existingActivity) {
    SpaceService spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);
    IdentityManager identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);

    String removerUserId =  authenticatedIdentity.getId();
    String ownerId = existingActivity.getUserId();    

    
    if(removerUserId.equals(ownerId)){
      return true;
    }
    
    String streamOwnerRemoteID = existingActivity.getStreamOwner();
    Identity streamOwnerIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, streamOwnerRemoteID, false);
    String streamOwnerId  = null;
    if(streamOwnerIdentity!=null){
      streamOwnerId = streamOwnerIdentity.getId();
    }
    
    if(removerUserId.equals(streamOwnerId)){
      return true;
    }
    
    Space spaceOfActivity = spaceService.getSpaceByPrettyName(existingActivity.getActivityStream().getPrettyId());
    if(spaceOfActivity != null){
      String[] adminsOfSpaceArray = spaceOfActivity.getManagers();
      
      for (int i = 0; i < adminsOfSpaceArray.length; i++) {
        if(adminsOfSpaceArray[i].equals(authenticatedIdentity.getRemoteId())){
          return true;
        }
      }
    }
    return false;
  }

  /**
   * <p>Checks if an authenticated identity has the permission to comment on an existing activity.</p>
   *
   * If commenterIdentity is the one who creates the existing activity, return true.<br />
   * If commenterIdentity is the one who is connected to existing activity's user identity, return true.<br />
   * If commenterIdentity is the one who is a member of the existing activity's space identity, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the authenticated identity to check
   * @param existingActivity the existing activity
   * @return true or false
   */
  public static boolean canCommentToActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                       ExoSocialActivity existingActivity) {
    IdentityManager identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    Identity ownerIdentityStream = identityManager.getIdentity(existingActivity.getUserId(),false);
    if(canPostActivity(portalContainer, authenticatedIdentity, ownerIdentityStream)){
      return true;
    }
    return false;
  }
  
  /**
   * <p>Checks if an authenticated identity has the permission to delete an existing comment.</p>
   *
   * If authenticatedIdentity is the one who creates the existing comment, return true.<br />
   * If authenticatedIdentity is the one who create the activity for that existing comment, return true.
   * If authenticatedIdentity is the one who is the stream owner of that comment to an activity, return true.<br />
   * If authenticatedIdentity is the one who is a manager of the existing activity's space identity, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the authenticated identity to check
   * @param existingComment the existing comment
   * @return true or false
   */
  public static boolean canDeleteComment(PortalContainer portalContainer, Identity authenticatedIdentity,
                                         ExoSocialActivity existingComment) {
    //TODO if the author of Activity which comment belong to want to delete comment return true
    
    if(canDeleteActivity(portalContainer, authenticatedIdentity, existingComment)){
      return true;
    }
    return false;
  }
}