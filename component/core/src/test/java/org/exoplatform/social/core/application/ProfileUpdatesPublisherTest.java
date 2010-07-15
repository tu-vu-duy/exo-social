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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent.Type;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class ProfileUpdatesPublisherTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(ProfileUpdatesPublisher.class);
  private List<Activity> tearDownActivityList;
  private ActivityManager activityManager;
  private IdentityManager identityManager;
  private ProfileUpdatesPublisher publisher;
  private String userName = "root";
  @Override
  public void setUp() throws Exception {
    super.setUp();
    tearDownActivityList = new ArrayList<Activity>();
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull("activityManager must not be null", activityManager);
    identityManager =  (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull("identityManager must not be null", identityManager);
    publisher = (ProfileUpdatesPublisher) getContainer().getComponentInstanceOfType(ProfileUpdatesPublisher.class);
    assertNotNull("profileUpdatesPublisher must not be null", publisher);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    for (Activity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }
  }

  public void testPublishActivity() throws Exception {
    // create an identity
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    Profile profile = identity.getProfile();
    assertNotNull("profile can not be null", profile);
    profile.setProperty(Profile.FIRST_NAME, "First Name");
    ProfileLifeCycleEvent event = new ProfileLifeCycleEvent(Type.BASIC_UPDATED, "root", profile);
    publisher.basicInfoUpdated(event);
    // check that the activity was created and that it contains what we expect
    List<Activity> activities = activityManager.getActivities(identity);
    assertEquals(1, activities.size());
    tearDownActivityList.add(activities.get(0));
    assertTrue(activities.get(0).getTitle().contains("basic"));
  }
}
