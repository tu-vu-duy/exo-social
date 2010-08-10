/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.application;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.relationship.RelationshipEvent;
import org.exoplatform.social.core.relationship.RelationshipListenerPlugin;
import org.exoplatform.social.core.relationship.model.Relationship;

/**
 * Publish a status update in activity streams of 2 confirmed relations.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class RelationshipPublisher extends RelationshipListenerPlugin {

  private static final Log LOG = ExoLogger.getLogger(RelationshipPublisher.class);

  private ActivityManager  activityManager;

  private IdentityManager identityManager;

  public RelationshipPublisher(InitParams params, ActivityManager activityManager, IdentityManager identityManager) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
  }


  /**
   * Publish an activity on both user's steam to indicate their new connection
   */
  public void confirmed(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    try {
      Identity id1 = relationship.getSender();
      reloadIfNeeded(id1);
      Identity id2 = relationship.getReceiver();
      reloadIfNeeded(id2);
      String user1 = "@" + id1.getRemoteId();
      String user2 = "@" + id2.getRemoteId();

      Activity activity = new Activity(id1.getId(), PeopleService.PEOPLE_APP_ID, "I am now connected to " + user2, null);
      activity.setTitleId("CONNECTION_CONFIRMED");
      Map<String,String> params = new HashMap<String,String>();
      params.put("Requester", user1);
      params.put("Accepter", user2);
      activity.setTemplateParams(params);
      activityManager.saveActivity(id1, activity);

      Activity activity2 = new Activity(id2.getId(), PeopleService.PEOPLE_APP_ID, "I am now connected to " +  user1, null);
      activity2.setTitleId("CONNECTION_CONFIRMED");
      Map<String,String> params2 = new HashMap<String,String>();
      params2.put("Requester", user2);
      params2.put("Accepter", user1);
      activity2.setTemplateParams(params2);
      activityManager.saveActivity(id2, activity2);

    } catch (Exception e) {
      LOG.warn("Failed to publish event " + event + ": " + e.getMessage());
    }
  }

  private void reloadIfNeeded(Identity identity) throws Exception {
    if (identity.getId() == null || identity.getProfile().getFullName().length() == 0) {
      identity = identityManager.getIdentity(identity.getGlobalId().toString(), true);
    }
  }

  @Override
  public void ignored(RelationshipEvent event) {
    ;// void on purpose
  }

  @Override
  public void removed(RelationshipEvent event) {
    ;// void on purpose
  }

  public void denied(RelationshipEvent event) {
    ;// void on purpose

  }

  /**
   * Publish an activity on invited member to show the invitation to connect
   */
  public void requested(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    try {
      Identity id1 = relationship.getSender();
      reloadIfNeeded(id1);
      Identity id2 = relationship.getReceiver();
      reloadIfNeeded(id2);
      String user1 = "@" + id1.getRemoteId();
      String user2 = "@" + id2.getRemoteId();
      //TODO hoatle a quick fix for activities gadget to allow deleting this activity
      Activity activity2 = new Activity(id2.getId(), PeopleService.PEOPLE_APP_ID, user1 + " has invited " +  user2 + " to connect", null);
      activity2.setTitleId("CONNECTION_REQUESTED");
      Map<String,String> params2 = new HashMap<String,String>();
      params2.put("Requester", user1);
      params2.put("Invited", user2);
      activity2.setTemplateParams(params2);
      activityManager.saveActivity(id2, activity2);

    } catch (Exception e) {
      LOG.warn("Failed to publish event " + event + ": " + e.getMessage());
    }
  }

}
