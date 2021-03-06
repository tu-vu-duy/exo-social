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
package org.exoplatform.social.service.rest.api.models;

import org.exoplatform.social.core.activity.model.ActivityStream.Type;

/**
 * The Activity Stream model for Social Rest APIs.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 17, 2011
 */
public class ActivityStream {

  /**
   * The type : either "user" or "space"
   */
  private Type type;

  /**
   * The pretty id.
   */
  private String prettyId;

  /**
   * The favorite icon URL.
   */
  private String faviconUrl;

  /**
   * The title.
   */
  private String title;

  /**
   * The permanent link.
   */
  private String permaLink;

  /**
   * Constructor.
   *
   * @param type The type.
   * @param prettyId The pretty id.
   * @param faviconUrl The favorite icon URL.
   * @param title The title.
   * @param permalink The permanent link.
   */
  public ActivityStream(
      final Type type,
      final String prettyId,
      final String faviconUrl,
      final String title,
      final String permaLink) {

    this.type = type;
    this.prettyId = prettyId;
    this.faviconUrl = faviconUrl;
    this.title = title;
    this.permaLink = permaLink;

  }
  
  public ActivityStream(org.exoplatform.social.core.activity.model.ActivityStream activityStream ) {
    type = activityStream.getType();
    prettyId = activityStream.getPrettyId();
    faviconUrl = activityStream.getFaviconUrl();
    title = activityStream.getTitle();
    permaLink =  activityStream.getPermaLink();
  }
  
  public Type getType() {
    return type;
  }

  public void setType(final Type type) {
    this.type = type;
  }

  public String getPrettyId() {
    return prettyId;
  }

  public void setPrettyId(final String prettyId) {
    this.prettyId = prettyId;
  }

  public String getFaviconUrl() {
    return faviconUrl;
  }

  public void setFaviconUrl(final String faviconUrl) {
    this.faviconUrl = faviconUrl;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getPermalink() {
    return permaLink;
  }

  public void setPermalink(final String permalink) {
    this.permaLink = permalink;
  }
}
