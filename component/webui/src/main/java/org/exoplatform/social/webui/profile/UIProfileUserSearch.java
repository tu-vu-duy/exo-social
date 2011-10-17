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
package org.exoplatform.social.webui.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.common.ResourceBundleUtil;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Searches users in profile by user name and some other filter condition.<br>
 * - Search action is listened and information for search user is processed.<br>
 * - After users is requested is returned, the search process is completed, -
 * Search event is broadcasted to the form that added search form as child.<br>
 * Author : hanhvi hanhvq@gmail.com
 * Sep 25, 2009
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/webui/profile/UIProfileUserSearch.gtmpl",
  events = {
    @EventConfig(listeners = UIProfileUserSearch.SearchActionListener.class)
  }
)
public class UIProfileUserSearch extends UIForm {

  /**
   * SEARCH.
   */
  public static final String SEARCH = "Search";

  /**
   * USER CONTACT.
   */
  public static final String USER_CONTACT = "name";

  /**
   * DEFAULT GENDER.
   */
  public static final String GENDER_DEFAULT = "Gender";

  /**
   * MALE.
   */
  public static final String MALE = "male";

  /**
   * FEMALE.
   */
  public static final String FEMALE = "female";

  /**
   * REGEX FOR SPLIT STRING
   */
  public static final String REG_FOR_SPLIT = "[^_A-Za-z0-9-.\\s[\\n]]";

  /**
   * PATTERN FOR CHECK RIGHT INPUT VALUE
   */
  static final String RIGHT_INPUT_PATTERN = "^[\\p{L}][\\p{L}._\\- \\d]+$";

  /**
   * REGEX EXPRESSION OF POSITION FIELD.
   */
  public static final String POSITION_REGEX_EXPRESSION = "^\\p{L}[\\p{L}\\d._,\\s]+\\p{L}$";

  /**
   * ADD PREFIX TO ENSURE ALWAY RIGHT THE PATTERN FOR CHECKING
   */
  static final String PREFIX_ADDED_FOR_CHECK = "PrefixAddedForCheck";

  /** Empty character. */
  private static final char EMPTY_CHARACTER = '\u0000';
  
  /** All people filter. */
  private static final String ALL_FILTER = "All";
  
  /**
   * List used for identities storage.
   */
  private List<Identity> identityList = null;

  /**
   * Stores selected character when search by alphabet
   */
  String selectedChar = null;

  /**
   * Used stores filter information.
   */
  ProfileFilter profileFilter = null;

  /**
   * Used stores type of relation with current user information.
   */
  String typeOfRelation = null;

  /**
   * URL of space that this UIComponent is used in member searching.
   */
  String spaceURL = null;

  /**
   * The flag notifies a new search when clicks search icon or presses enter.
   */
  private boolean isNewSearch;

  /**
   * Number of identities.
   */
  long identitiesCount;

  /**
   * Clarifies that Spaces tab is included or not.
   */
  private boolean hasPeopleTab;
  
  /**
   * Number of matching people.
   */
  private int peopleNum;
  
  /**
   * Gets the flags to clarify including people tab or not. 
   * @return
   */
  public boolean isHasPeopleTab() {
	return hasPeopleTab;
  }

  /**
   * Sets the flags to clarify including people tab or not.
   * @param hasPeopleTab
   */
  public void setHasPeopleTab(boolean hasPeopleTab) {
	this.hasPeopleTab = hasPeopleTab;
  }

  /**
   * Gets number of matching people.
   * 
   * @return
   * @since 1.2.2
   */
  public int getPeopleNum() {
	return peopleNum;
  }
  
  /**
   * Sets number of matching people.
   * 
   * @param peopleNum
   * @since 1.2.2
   */
  public void setPeopleNum(int peopleNum) {
	this.peopleNum = peopleNum;
  }

/**
   * Sets list identity.
   *
   * @param identityList <code>List</code>
   * @throws Exception
   */
  public void setIdentityList(List<Identity> identityList) throws Exception {
    if (identityList.contains(Utils.getViewerIdentity())) {
      identityList.remove(Utils.getViewerIdentity());
    }
    this.identityList = identityList;
  }

  /**
   * Gets identity list result search.
   *
   * @return List of identity.
   */
  public final List<Identity> getIdentityList() {
    return identityList;
  }

  /**
   * Gets selected character when search by alphabet.
   *
   * @return The selected character.
   */
  public final String getSelectedChar() {
    return selectedChar;
  }

  /**
   * Sets selected character to variable.
   *
   * @param selectedChar <code>char</code>
   */
  public final void setSelectedChar(final String selectedChar) {
    this.selectedChar = selectedChar;
  }

  /**
   * Gets type of relation with current user.
   */
  public String getTypeOfRelation() {
    return typeOfRelation;
  }

  /**
   * Sets type of relation with current user to variable.
   *
   * @param typeOfRelation <code>char</code>
   */
  public void setTypeOfRelation(String typeOfRelation) {
    this.typeOfRelation = typeOfRelation;
  }

  /**
   * Gets space url.
   */
  public String getSpaceURL() {
    return spaceURL;
  }

  /**
   * Sets space url.
   *
   * @param spaceURL <code>char</code>
   */
  public void setSpaceURL(String spaceURL) {
    this.spaceURL = spaceURL;
  }

  /**
   * Gets current user's name.
   *
   * @return
   */
  protected String getCurrentUserName() {
    return RequestContext.getCurrentInstance().getRemoteUser();
  }

  /**
   * Gets current Rest context name of portal container.
   */
  protected String getRestContextName() {
    return PortalContainer.getCurrentRestContextName();
  }

  /**
   * Gets filter object.
   *
   * @return The object that contain filter information.
   */
  public final ProfileFilter getProfileFilter() {
    return profileFilter;
  }

  /**
   * Sets filter object.
   *
   * @param profileFilter <code>Object<code>
   */
  public final void setProfileFilter(final ProfileFilter profileFilter) {
    this.profileFilter = profileFilter;
  }

  /**
   * Initializes user search form fields. Initials and adds components as children to search form.
   *
   * @throws Exception
   */
  public UIProfileUserSearch() throws Exception {
	ResourceBundle resourceBudle = PortalRequestContext.getCurrentInstance().getApplicationResourceBundle();
	
	String defaultName = resourceBudle.getString("UIProfileUserSearch.label.Name");
	String defaultPos = resourceBudle.getString("UIProfileUserSearch.label.Position");
	String defaultSkills = resourceBudle.getString("UIProfileUserSearch.label.Skills");
	String defaultGender = resourceBudle.getString("UIProfileUserSearch.label.AllGender");
	String defaultMale = resourceBudle.getString("UIProfileUserSearch.label.Male");
	String defaultFeMale = resourceBudle.getString("UIProfileUserSearch.label.FeMale");
	
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(defaultGender));
    options.add(new SelectItemOption<String>(defaultMale));
    options.add(new SelectItemOption<String>(defaultFeMale));

    addUIFormInput(new UIFormStringInput(SEARCH, USER_CONTACT, defaultName));
    addUIFormInput(new UIFormStringInput(Profile.POSITION, Profile.POSITION, defaultPos));
    addUIFormInput(new UIFormStringInput(Profile.EXPERIENCES_SKILLS, Profile.EXPERIENCES_SKILLS, defaultSkills));
    addUIFormInput(new UIFormSelectBox(Profile.GENDER, Profile.GENDER, options)).setId("GenderList");
    profileFilter = new ProfileFilter();
    setHasPeopleTab(false);
    setSelectedChar(ALL_FILTER);
  }

  protected void resetUIComponentValues() {
	UIFormStringInput uiName = getChildById(SEARCH);
	UIFormStringInput uiPos = getChildById(Profile.POSITION);
	UIFormStringInput uiSkills = getChildById(Profile.EXPERIENCES_SKILLS);
	UIFormSelectBox uiGender = getChildById(Profile.GENDER);

	//
	ResourceBundle resourceBudle = PortalRequestContext.getCurrentInstance().getApplicationResourceBundle();
	
	String defaultName = resourceBudle.getString("UIProfileUserSearch.label.Name");
	String defaultPos = resourceBudle.getString("UIProfileUserSearch.label.Position");
	String defaultSkills = resourceBudle.getString("UIProfileUserSearch.label.Skills");
	String defaultGender = resourceBudle.getString("UIProfileUserSearch.label.AllGender");
	String defaultMale = resourceBudle.getString("UIProfileUserSearch.label.Male");
	String defaultFeMale = resourceBudle.getString("UIProfileUserSearch.label.FeMale");
	
	//
	uiName.setValue(defaultName);
	uiPos.setValue(defaultPos);
	uiSkills.setValue(defaultSkills);
	uiGender.setDefaultValue(defaultGender);
	uiGender.getOptions().get(0).setValue(defaultGender);
	uiGender.getOptions().get(1).setValue(defaultMale);
	uiGender.getOptions().get(2).setValue(defaultFeMale);
	uiGender.getOptions().get(0).setLabel(defaultGender);
	uiGender.getOptions().get(1).setLabel(defaultMale);
	uiGender.getOptions().get(2).setLabel(defaultFeMale);
  }
  
  /**
   * Returns the current selected node.<br>
   *
   * @return selected node.
   * @since 1.2.2
   */
  public String getSelectedNode() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String currentPath = pcontext.getControllerContext().getParameter(QualifiedName.parse("gtn:path"));
    if (currentPath.split("/").length >= 2) {
      return  currentPath.split("/")[1];
    }
    return currentPath;
  }

  /**
   * Listens to search event from search form, then processes search condition and set search result
   * to the result variable.<br> - Gets user name and other filter information from request.<br> -
   * Searches user that matches the condition.<br> - Sets matched users into result list.<br>
   */
  public static class SearchActionListener extends EventListener<UIProfileUserSearch> {
    @Override
    public final void execute(final Event<UIProfileUserSearch> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIProfileUserSearch uiSearch = event.getSource();
      String charSearch = ctx.getRequestParameter(OBJECTID);
      ProfileFilter filter = new ProfileFilter();
      List<Identity> excludedIdentityList = new ArrayList<Identity>();
      excludedIdentityList.add(Utils.getViewerIdentity());
      filter.setExcludedIdentityList(excludedIdentityList);
      
      uiSearch.invokeSetBindingBean(filter);
      ResourceBundle resApp = ctx.getApplicationResourceBundle();

      String defaultNameVal = resApp.getString(uiSearch.getId() + ".label.Name");
      String defaultPosVal = resApp.getString(uiSearch.getId() + ".label.Position");
      String defaultSkillsVal = resApp.getString(uiSearch.getId() + ".label.Skills");
      String defaultGenderVal = resApp.getString(uiSearch.getId() + ".label.AllGender");
      try {
        uiSearch.setSelectedChar(charSearch);
        if (charSearch != null) { // search by alphabet
          ((UIFormStringInput) uiSearch.getChildById(SEARCH)).setValue(defaultNameVal);
          ((UIFormStringInput) uiSearch.getChildById(Profile.POSITION)).setValue(defaultPosVal);
          ((UIFormStringInput) uiSearch.getChildById(Profile.EXPERIENCES_SKILLS)).setValue(defaultSkillsVal);
          ((UIFormStringInput) uiSearch.getChildById(Profile.GENDER)).setValue(defaultGenderVal);
          filter.setName(charSearch);
          filter.setPosition("");
          filter.setSkills("");
          filter.setGender("");
          filter.setFirstCharacterOfName(charSearch.toCharArray()[0]);
          if (ALL_FILTER.equals(charSearch)) {
            filter.setFirstCharacterOfName(EMPTY_CHARACTER);
            filter.setName("");
          }
          
          uiSearch.setProfileFilter(filter);
        } else {
          uiSearch.setSelectedChar(null);
          if (!isValidInput(filter)) { // is invalid condition input
            uiSearch.setIdentityList(new ArrayList<Identity>());
          } else {
            if ((filter.getName() == null) || filter.getName().equals(defaultNameVal)) {
              filter.setName("");
            }
            if ((filter.getPosition() == null) || filter.getPosition().equals(defaultPosVal)) {
              filter.setPosition("");
            }
            if ((filter.getSkills() == null) || filter.getSkills().equals(defaultSkillsVal)) {
              filter.setSkills("");
            }
            if (filter.getGender().equals(defaultGenderVal)) {
              filter.setGender("");
            }

            String skills = null;

            filter.setFirstCharacterOfName(EMPTY_CHARACTER);
            uiSearch.setProfileFilter(filter);
            // Using regular expression for search
            skills = filter.getSkills();
            if (skills.length() > 0) {
              skills = skills.isEmpty() ? "*" : skills;
              skills = (skills.charAt(0) != '*') ? "*" + skills : skills;
              skills = (skills.charAt(skills.length() - 1) != '*') ? skills += "*" : skills;
              skills = (skills.indexOf("*") >= 0) ? skills.replace("*", ".*") : skills;
              skills = (skills.indexOf("%") >= 0) ? skills.replace("%", ".*") : skills;
              Pattern.compile(skills);
              filter.setSkills(skills);
              uiSearch.setProfileFilter(filter);
            }
          }
        }
        uiSearch.setNewSearch(true);
      } catch (Exception e) {
        uiSearch.setIdentityList(new ArrayList<Identity>());
      }
      Event<UIComponent> searchEvent = uiSearch.<UIComponent>getParent()
              .createEvent(SEARCH, Event.Phase.DECODE, ctx);
      if (searchEvent != null) {
        searchEvent.broadcast();
      }
    }

    /**
     * Checks input values follow regular expression.
     *
     * @param input <code>Object</code>
     * @return true if the input is properly to regular expression else return false.
     */
    private boolean isValidInput(final ProfileFilter input) {
      // Check contact name
      String contactName = input.getName();
      // Eliminate '*' and '%' character in string for checking
      String contactNameForCheck = null;
      if (contactName != null) {
        contactNameForCheck = contactName.trim().replace("*", "");
        contactNameForCheck = contactNameForCheck.replace("%", "");
        // Make sure string for checking is started by alphabet character
        contactNameForCheck = PREFIX_ADDED_FOR_CHECK + contactNameForCheck;
        if (!contactNameForCheck.matches(RIGHT_INPUT_PATTERN)) {
          return false;
        }
      }

      return true;
    }
  }

  /**
   * Filter identity follow skills information.
   *
   * @param skills     <code>String</code>
   * @param identities <code>Object</code>
   * @return List of identities that has skills information match.
   */
  @SuppressWarnings("unchecked")
  public List<Identity> getIdentitiesBySkills(final List<Identity> identities) {
    List<Identity> identityLst = new ArrayList<Identity>();
    String prof = null;
    ArrayList<HashMap<String, Object>> experiences;
    String skill = getProfileFilter().getSkills().trim().toLowerCase();

    if (identities.size() == 0) {
      return identityLst;
    }

    for (Identity id : identities) {
      Profile profile = id.getProfile();
      experiences = (ArrayList<HashMap<String, Object>>) profile.getProperty(Profile.EXPERIENCES);
      if (experiences == null) {
        continue;
      }
      for (HashMap<String, Object> exp : experiences) {
        prof = (String) exp.get(Profile.EXPERIENCES_SKILLS);
        if (prof == null) {
          continue;
        }
        Pattern p = Pattern.compile(REG_FOR_SPLIT);
        String[] items = p.split(prof);
        for (String item : items) {
          if (item.toLowerCase().matches(skill)) {
            identityLst.add(id);
            break;
          }
        }
      }
    }

    return GetUniqueIdentities(identityLst);
  }

  /**
   * Gets label to display the number of matching spaces.
   * 
   * @return
   * @since 1.2.2
   */
  protected String getPeopleFoundLabel() {
    String labelArg = "UIProfileUserSearch.label.FoundPersonFilter"; 
    
    if (getPeopleNum() > 1) {
      labelArg = "UIProfileUserSearch.label.FoundPeopleFilter";
    }
    
    String searchCondition = getSelectedChar();
    if (selectedChar == null) {
      labelArg = "UIProfileUserSearch.label.FoundPersonSearch";
      if (getPeopleNum() > 1) {
        labelArg = "UIProfileUserSearch.label.FoundPeopleSearch";
      }
      searchCondition = getProfileFilter().getName();
    }
    if(ALL_FILTER.equals(searchCondition)) {
      searchCondition = WebuiRequestContext.getCurrentInstance()
          .getApplicationResourceBundle().getString("UIProfileUserSearch.label.SearchAll");
    }
    
    return ResourceBundleUtil.
    replaceArguments(WebuiRequestContext.getCurrentInstance()
                     .getApplicationResourceBundle().getString(labelArg), new String[] {
                 Integer.toString(getPeopleNum()), searchCondition != null ? searchCondition : " " });
  }
  
  /**
   * Unions to collection to make one collection.
   *
   * @param identities1 <code>Object</code>
   * @param identities2 <code>Object</code>
   * @return One new collection that the union result of two input collection.
   */
  private static Collection<Identity> Union(final Collection<Identity> identities1,
                                            final Collection<Identity> identities2) {
    Set<Identity> identities = new HashSet<Identity>(identities1);
    identities.addAll(new HashSet<Identity>(identities2));
    return new ArrayList<Identity>(identities);
  }

  /**
   * Gets unique identities from one collection of identities.
   *
   * @param identities <code>Object</code>
   * @return one list that contains unique identities.
   */
  private static ArrayList<Identity> GetUniqueIdentities(final Collection<Identity> identities) {
    return (ArrayList<Identity>) Union(identities, identities);
  }

  public final boolean isNewSearch() {
    return isNewSearch;
  }

  public final void setNewSearch(final boolean isNewSearch) {
    this.isNewSearch = isNewSearch;
  }
}