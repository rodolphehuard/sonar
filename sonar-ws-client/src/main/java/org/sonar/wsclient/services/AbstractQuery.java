/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.wsclient.services;

import java.util.Date;

/**
 * @since 2.2
 */
public abstract class AbstractQuery<MODEL extends Model> {

  /**
   * Must start with a slash, for example: /api/metrics
   * <p>
   * IMPORTANT: In implementations of this method we must use helper methods to construct URL.
   * </p>
   * 
   * @see #encode(String)
   * @see #appendUrlParameter(StringBuilder, String, Object)
   * @see #appendUrlParameter(StringBuilder, String, Object[])
   * @see #appendUrlParameter(StringBuilder, String, Date, boolean)
   */
  public abstract String getUrl();

  /**
   * Request body. By default it is empty but it can be overridden.
   */
  public String getBody() {
    return null;
  }

  /**
   * Encodes single parameter value.
   */
  protected static String encode(String value) {
    return WSUtils.getINSTANCE().encodeUrl(value);
  }

  protected static void appendUrlParameter(StringBuilder url, String paramKey, Object paramValue) {
    if (paramValue != null) {
      url.append(paramKey)
          .append('=')
          .append(encode(paramValue.toString()))
          .append('&');
    }
  }

  protected static void appendUrlParameter(StringBuilder url, String paramKey, Object[] paramValues) {
    if (paramValues != null) {
      url.append(paramKey).append('=');
      for (int index = 0; index < paramValues.length; index++) {
        if (index > 0) {
          url.append(',');
        }
        if (paramValues[index] != null) {
          url.append(encode(paramValues[index].toString()));
        }
      }
      url.append('&');
    }
  }

  protected static void appendUrlParameter(StringBuilder url, String paramKey, Date paramValue, boolean includeTime) {
    if (paramValue != null) {
      String format = (includeTime ? "yyyy-MM-dd'T'HH:mm:ssZ" : "yyyy-MM-dd");
      url.append(paramKey)
          .append('=')
          .append(encode(WSUtils.getINSTANCE().format(paramValue, format)))
          .append('&');
    }
  }
}