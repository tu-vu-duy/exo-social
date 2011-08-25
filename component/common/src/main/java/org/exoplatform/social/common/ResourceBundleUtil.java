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
package org.exoplatform.social.common;

import java.util.List;

/**
 * ResourceBundleUtil
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Aug 31, 2010
 * @copyright eXo SAS
 */
public class ResourceBundleUtil {

  /**
   * Replace convention arguments of pattern {index} with messageArguments[index].
   * @param message
   * @param messageArguments
   * @return expected message with replaced arguments
   */
  public static String replaceArguments(String message, String[] messageArguments) {
    for (int i = 0; i < messageArguments.length; i++) {
      message = message.replace("{" + i + "}", messageArguments[i]);
    }
    return message;
  }
  
  /**
   * Replace convention arguments of pattern {index} with messageArguments[index].
   * @param message
   * @param messageArguments
   * @return expected message with replaced arguments
   * @since 1.2.2
   */
  public static String replaceArguments(String message, List<String> messageArguments) {
    for (int i = 0; i < messageArguments.size(); i++) {
      message = message.replace("{" + i + "}", messageArguments.get(i));
    }
    return message;
  }

}
