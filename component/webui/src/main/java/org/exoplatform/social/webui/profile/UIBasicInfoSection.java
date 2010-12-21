/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.webui.profile;

import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

/**
 * Component manages basic information. This is one part of profile management
 * beside contact and experience.<br>
 * Modified : dang.tung tungcnw@gmail.com Aug 11, 2009
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/webui/profile/UIBasicInfoSection.gtmpl",
  events = {
    @EventConfig(listeners = UIBasicInfoSection.EditActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIBasicInfoSection.SaveActionListener.class),
    @EventConfig(listeners = UIBasicInfoSection.CancelActionListener.class, phase = Phase.DECODE)
  }
)
public class UIBasicInfoSection extends UIProfileSection {

  /** REGEX EXPRESSION. */
  final public static String REGEX_EXPRESSION     = "^\\p{L}[\\p{L}\\d._,\\s]+\\p{L}$";

  /** INVALID CHARACTER MESSAGE. */
  final public static String INVALID_CHAR_MESSAGE = "UIBasicInfoSection.msg.Invalid-char";

  public UIBasicInfoSection() throws Exception {
    String username = Util.getPortalRequestContext().getRemoteUser();
    OrganizationService service = this.getApplicationComponent(OrganizationService.class);
    User useraccount = service.getUserHandler().findUserByName(username);

    addChild(UITitleBar.class, null, null);

    UIFormStringInput userName = new UIFormStringInput(Profile.USERNAME, Profile.USERNAME, username);
    userName.setEditable(false);
    addUIFormInput(userName);
    addUIFormInput(new UIFormStringInput(Profile.FIRST_NAME,
                                         Profile.FIRST_NAME,
                                         useraccount.getFirstName()).
                   addValidator(MandatoryValidator.class).
                   addValidator(StringLengthValidator.class,3,45).
                   addValidator(ExpressionValidator.class, REGEX_EXPRESSION, INVALID_CHAR_MESSAGE));

    addUIFormInput(new UIFormStringInput(Profile.LAST_NAME,
                                         Profile.LAST_NAME,
                                         useraccount.getLastName()).
                   addValidator(MandatoryValidator.class).
                   addValidator(StringLengthValidator.class, 3, 45).
                   addValidator(ExpressionValidator.class, REGEX_EXPRESSION, INVALID_CHAR_MESSAGE));

    addUIFormInput(new UIFormStringInput(Profile.EMAIL, Profile.EMAIL, useraccount.getEmail()).
                   addValidator(MandatoryValidator.class).
                   addValidator(EmailAddressValidator.class));
  }

  /**
   * Gets and sort all uicomponents.<br>
   *
   * @return All children in order.
   */
  public List<UIComponent> getChilds() {
    return getChildren();
  }

  /**
   * Changes form into edit mode when user click eddit button.<br>
   */
  public static class EditActionListener extends UIProfileSection.EditActionListener {

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);
      UIProfileSection sect = event.getSource();
      UIBasicInfoSection uiForm = (UIBasicInfoSection) sect;
      String username = Util.getPortalRequestContext().getRemoteUser();
      OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
      User user = service.getUserHandler().findUserByName(username);

      uiForm.getUIStringInput(Profile.FIRST_NAME).setValue(user.getFirstName());
      uiForm.getUIStringInput(Profile.LAST_NAME).setValue(user.getLastName());
      uiForm.getUIStringInput(Profile.EMAIL).setValue(user.getEmail());
      WebuiRequestContext requestContext = event.getRequestContext();
      requestContext.addUIComponentToUpdateByAjax(uiForm);
      requestContext.addUIComponentToUpdateByAjax(sect);
    }
  }

  /**
   * Stores profile information into database when form is submitted.<br>
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {
    private static final String MSG_KEY_UI_ACCOUNT_INPUT_SET_EMAIL_EXIST   = "UIAccountInputSet.msg.email-exist";
//    private static final String MSG_KEY_UI_ACCOUNT_PROFILES_UPDATE_SUCCESS = "UIAccountProfiles.msg.update.success";
    private static final String PORTLET_NAME_USER_PROFILE_TOOLBAR_PORTLET  = "UserProfileToolBarPortlet";
//    private static final String PORTLET_NAME_USER_PROFILE_PORTLET          = "ProfilePortlet";

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);

      UIBasicInfoSection uiForm = (UIBasicInfoSection) event.getSource();

      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UIApplication uiApp = context.getUIApplication();

      String userName = uiForm.getUIStringInput(Profile.USERNAME).getValue();
      OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
      User user = service.getUserHandler().findUserByName(userName);
      String oldEmail = user.getEmail();
      String newEmail = uiForm.getUIStringInput(Profile.EMAIL).getValue();

      // Check if mail address is already used
      Query query = new Query();
      query.setEmail(newEmail);
      if (service.getUserHandler().findUsers(query).getAll().size() > 0
          && !oldEmail.equals(newEmail)) {
        // Be sure it keep old value
        user.setEmail(oldEmail);
        Object[] args = { userName };
        uiApp.addMessage(new ApplicationMessage(MSG_KEY_UI_ACCOUNT_INPUT_SET_EMAIL_EXIST, args));
        return;
      }

      ExoContainer container = ExoContainerContext.getCurrentContainer();

      Profile p = uiForm.getProfile(true);
      Profile updateProfile = new Profile(p.getIdentity());
      updateProfile.setId(p.getId());

      updateProfile.setProperty(Profile.FIRST_NAME, uiForm.getUIStringInput(Profile.FIRST_NAME).getValue());
      updateProfile.setProperty(Profile.LAST_NAME, uiForm.getUIStringInput(Profile.LAST_NAME).getValue());
      updateProfile.setProperty(Profile.EMAIL, newEmail);

      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      im.updateBasicInfo(updateProfile);

      user.setFirstName((String) updateProfile.getProperty(Profile.FIRST_NAME));
      user.setLastName((String) updateProfile.getProperty(Profile.LAST_NAME));
      user.setEmail((String) updateProfile.getProperty(Profile.EMAIL));
      service.getUserHandler().saveUser(user, true);
      ConversationState.getCurrent().setAttribute(CacheUserProfileFilter.USER_PROFILE,user);

      UIProfile uiProfile = uiForm.getParent();
      context.addUIComponentToUpdateByAjax(uiProfile.getChild(UIHeaderSection.class));
      context.addUIComponentToUpdateByAjax(uiProfile.getChild(UIBasicInfoSection.class));

      UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication()
                                           .getChild(UIWorkingWorkspace.class);
      uiWorkingWS.updatePortletsByName(PORTLET_NAME_USER_PROFILE_TOOLBAR_PORTLET);
    }
  }
}
