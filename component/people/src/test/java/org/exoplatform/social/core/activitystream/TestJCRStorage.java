package org.exoplatform.social.core.activitystream;

import java.util.Date;
import java.util.List;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.impl.SocialDataLocation;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

@ConfiguredBy( {
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.people.configuration.xml") })
public class TestJCRStorage extends AbstractJCRTestCase {
  
  @Test
  public void testGetStreamInfo() throws Exception {
    
    SocialDataLocation dataLocation = getComponent(SocialDataLocation.class);
    JCRStorage storage = new JCRStorage(dataLocation);
    
    // root save on john's stream
    Activity activity = new Activity();
    activity.setTitle("blabla");
    activity.setUserId("root");
    activity.setUpdated(new Date());
    Identity john = new Identity(OrganizationIdentityProvider.NAME, "john");
    storage.save(john, activity);
    
    String streamId = activity.getStreamId();
    assertNotNull(streamId);
    assertEquals(activity.getStreamOwner(), "john");
    
    
    List<Activity> activities = storage.getActivities(john);
    assertEquals(activities.size(), 1);
    assertEquals(activities.get(0).getStreamOwner(), "john");
    assertEquals(activities.get(0).getStreamId(), streamId);
    
    Activity loaded = storage.load(activity.getId());
    assertEquals(loaded.getStreamOwner(), "john");
    assertEquals(loaded.getStreamId(), streamId);
  }

}