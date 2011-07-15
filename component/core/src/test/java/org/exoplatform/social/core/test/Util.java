package org.exoplatform.social.core.test;

public class Util {

  public static String escapeJCRSpecialCharacters(String string) {
    if (string == null) {
      return null;
    }
    return string.replace("[", "%5B").replace("]", "%5D").replace(":", "%3A");
  }

}
