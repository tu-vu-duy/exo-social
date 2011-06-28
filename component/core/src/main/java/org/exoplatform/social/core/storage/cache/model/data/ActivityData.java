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

package org.exoplatform.social.core.storage.cache.model.data;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.storage.cache.CacheData;

import java.util.Collections;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ActivityData implements CacheData<ExoSocialActivity> {

  private final String id;
  private final String title;
  private final String body;
  private final String[] likes;
  private final boolean isComment;
  private final Long postedTime;
  private final String[] replyIds;
  private final String userId;
  private final String streamId;
  private final String streamOwner;
  private final String streamFaviconUrl;
  private final String streamSourceUrl;
  private final String streamTitle;
  private final String streamUrl;

  public ActivityData(final ExoSocialActivity activity) {

    this.id = activity.getId();
    this.title = activity.getTitle();
    this.body = activity.getBody();
    this.likes = activity.getLikeIdentityIds();
    this.isComment = activity.isComment();
    this.postedTime = activity.getPostedTime();
    this.replyIds = activity.getReplyToId();
    this.userId = activity.getUserId();
    this.streamId = activity.getStreamId();
    this.streamOwner = activity.getStreamOwner();
    this.streamFaviconUrl = activity.getStreamFaviconUrl();
    this.streamSourceUrl = activity.getStreamSourceUrl();
    this.streamTitle = activity.getStreamTitle();
    this.streamUrl = activity.getStreamUrl();

  }

  public ExoSocialActivity build() {

    ExoSocialActivity activity = new ExoSocialActivityImpl();

    activity.setId(id);
    activity.setTitle(title);
    activity.setBody(body);
    if (likes != null) { activity.setLikeIdentityIds(likes); }
    activity.setReplyToId(replyIds);
    activity.isComment(isComment);
    activity.setPostedTime(postedTime);
    activity.setUserId(userId);
    activity.setStreamId(streamId);
    activity.setStreamOwner(streamOwner);
    activity.setStreamFaviconUrl(streamFaviconUrl);
    activity.setStreamSourceUrl(streamSourceUrl);
    activity.setStreamTitle(streamTitle);
    activity.setStreamUrl(streamUrl);

    return activity;

  }

}
