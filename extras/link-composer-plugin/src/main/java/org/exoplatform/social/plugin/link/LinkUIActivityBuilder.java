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
package org.exoplatform.social.plugin.link;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.BaseUIActivityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 23, 2010
 */
public class LinkUIActivityBuilder extends BaseUIActivityBuilder {
  private static final Log LOG = ExoLogger.getLogger(LinkUIActivityBuilder.class);

  @Override
  protected void extendUIActivity(BaseUIActivity uiActivity, Activity activity) {
    UILinkActivity uiLinkActivity = (UILinkActivity) uiActivity;
    Validate.notNull(activity.getTitle(), "activity_.getTitle() must not be null.");
    try {
      getLinkDataFromJSON(activity, uiLinkActivity);
    } catch (JSONException e) {
      uiLinkActivity.setLinkSource(activity.getUrl());
      uiLinkActivity.setLinkTitle(activity.getTitle());
      Map<String,String> params = activity.getTemplateParams(); 
      String description = params.get(UILinkActivityComposer.DESCRIPTION_PARAM);
      String comment = params.get(UILinkActivityComposer.COMMENT_PARAM);
      uiLinkActivity.setLinkDescription(description);
      uiLinkActivity.setLinkComment(comment);
    } catch (Exception e){
      LOG.error(e);
    }
  }

  private void getLinkDataFromJSON(Activity activity, UILinkActivity uiLinkActivity) throws JSONException {
    JSONObject titleData = new JSONObject(activity.getTitle());
    if (titleData != null) {
      if(!titleData.isNull(UILinkActivityComposer.LINK_PARAM)){
        uiLinkActivity.setLinkSource(titleData.getString(UILinkActivityComposer.LINK_PARAM));
      }

      if(!titleData.isNull(UILinkActivityComposer.TITLE_PARAM)){
        uiLinkActivity.setLinkTitle(titleData.getString(UILinkActivityComposer.TITLE_PARAM));
      }

      if(!titleData.isNull(UILinkActivityComposer.IMAGE_PARAM)){
        uiLinkActivity.setLinkImage(titleData.getString(UILinkActivityComposer.IMAGE_PARAM));
      }

      if(!titleData.isNull(UILinkActivityComposer.DESCRIPTION_PARAM)){
        String desp = titleData.getString(UILinkActivityComposer.DESCRIPTION_PARAM);
        if ((desp == null) || (desp.length() == 0)) {
          desp = activity.getTemplateParams().get(UILinkActivityComposer.DESCRIPTION_PARAM); 
        }
        uiLinkActivity.setLinkDescription(desp);
      }

      if(!titleData.isNull(UILinkActivityComposer.COMMENT_PARAM)){
        uiLinkActivity.setLinkComment(titleData.getString(UILinkActivityComposer.COMMENT_PARAM));
      }
    }
  }
}
