/*
 * Copyright (c) 1998-2007 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Sam
 */

package com.caucho.netbeans.core;

import com.caucho.netbeans.PluginL10N;
import com.caucho.netbeans.PluginLogger;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

public class ResinConfiguration
{
  private static final PluginL10N L = new PluginL10N(ResinConfiguration.class);
  private static final PluginLogger log = new PluginLogger(ResinConfiguration.class);

  private static final String URI_TOKEN_HOME = ":home=";
  private static final String URI_TOKEN_CONF = ":conf=";
  private static final String URI_TOKEN_SERVER_ID = ":server-id=";
  private static final String URI_TOKEN_SERVER_PORT = ":server-port=";
  private static final String URI_TOKEN_SERVER_ADDRESS = ":server-address=";

  private File _resinHome;
  private File _resinConf;
  private String _serverId;
  private String _serverAddress;
  private int _serverPort;

  private String _displayName = "Resin";
  private String _username = "Username";
  private String _password = "Password";

  public ResinConfiguration()
  {
  }

  public ResinConfiguration(InstanceProperties ip)
  {
    String uri = ip.getProperty(InstanceProperties.URL_ATTR);

    parseURI(uri);

    setUsername(ip.getProperty(InstanceProperties.USERNAME_ATTR));
    setPassword(ip.getProperty(InstanceProperties.PASSWORD_ATTR));
    setDisplayName(ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR));
  }

  public InstanceProperties toInstanceProperties()
    throws InstanceCreationException
  {
    String username = _username;
    String password = _password;
    String displayName = _displayName;

    if (username == null)
      username = "resin";

    if (password == null)
      password = "resin";

    if (displayName == null)
      displayName = "Resin";

    InstanceProperties instanceProperties
      = InstanceProperties.createInstanceProperties(getURI(),
                                                    username,
                                                    password,
                                                    displayName);


    return instanceProperties;
  }

  public File getResinConf()
  {
    return _resinConf;
  }

  public void setResinConf(File resinConf)
  {
    if (!resinConf.isAbsolute()) {
      if (_resinHome == null)
        throw new IllegalArgumentException(L.l("no resin-home set for relative conf {0}", resinConf));

      resinConf = new File(_resinHome, resinConf.getPath());
    }

    _resinConf = resinConf;
  }

  public File getResinHome()
  {
    return _resinHome;
  }

  public void setResinHome(File resinHome)
  {
    _resinHome = resinHome;
  }

  public String getServerId()
  {
    return _serverId;
  }

  public void setServerId(String serverId)
  {
    _serverId = serverId;
  }

  public String getServerAddress()
  {
    return _serverAddress == null ? "127.0.0.1" : _serverAddress;
  }

  private void setServerAddress(String serverAddress)
    throws IllegalArgumentException

  {
    try {
      InetAddress.getByName(serverAddress);

      _serverAddress = serverAddress;
    }
    catch (UnknownHostException e) {
      throw new IllegalArgumentException(L.l("The address ''{0}'' is not valid: {1}",
                                             serverAddress, e.getLocalizedMessage()));
    }
  }

  public int getServerPort()
  {
    return _serverPort;
  }

  public void setServerPort(int serverPort)
  {
    _serverPort = serverPort;
  }

  public void setServerPort(String serverPort)
    throws IllegalArgumentException
  {
    int port = 0;

    try {
      port = Integer.parseInt(serverPort);
    }
    catch (NumberFormatException ex) {
      // no-op
    }

    if (!(0 < port && port < 65536))
      throw new IllegalArgumentException(L.l("server-port must have a value between 0 and 65536"));

    setServerPort(port);
  }

  public String getDisplayName()
  {
    return _displayName;
  }

  public void setDisplayName(String displayName)
  {
    _displayName = displayName;
  }

  public String getPassword()
  {
    return _password;
  }

  public void setPassword(String password)
  {
    _password = password;
  }

  public String getUsername()
  {
    return _username;
  }

  public void setUsername(String username)
  {
    _username = username;
  }

  private void requiredFile(String name, File file)
    throws IllegalStateException
  {
    if (file == null)
      throw new IllegalStateException(L.l("''{0}'' is required", name));

    if (!file.exists())
      throw new IllegalStateException(L.l("''{0}'' does not exist", file));
  }

  public void validate()
    throws IllegalStateException
  {
    requiredFile("resin-home", _resinHome);
    requiredFile("resin-conf", _resinConf);

    try {
      InetAddress.getByName(getServerAddress());
    }
    catch (UnknownHostException e) {
      throw new IllegalStateException(L.l("server-address ''{0}'' is not valid: {1}",
                                             getServerAddress(),
                                             e.getLocalizedMessage()));
    }

    if (!(0 < _serverPort && _serverPort < 65536))
      throw new IllegalStateException(L.l("''server-port'' must have a value between 0 and 65536"));

  }

  void parseURI(String uri)
    throws IllegalArgumentException
  {
    if (!uri.startsWith("resin"))
      throw new IllegalArgumentException(L.l("''{0}'' is not a Resin URI", uri));

    String token = null;
    int i  = "resin".length();
    int lexemeStart = i;

    try {
      while (true)
      {
        String nextToken = parseURIToken(uri, i);

        if (nextToken != null || i == uri.length()) {
          if (token != null && i > lexemeStart) {
            String lexeme = uri.substring(lexemeStart, i);

            if (token == URI_TOKEN_HOME)
              setResinHome(new File(lexeme));
            else if (token == URI_TOKEN_CONF)
              setResinConf(new File(lexeme));
            else if (token == URI_TOKEN_SERVER_ID)
              setServerId(lexeme);
            else if (token == URI_TOKEN_SERVER_PORT)
              setServerPort(lexeme);
            else if (token == URI_TOKEN_SERVER_ADDRESS)
              setServerAddress(lexeme);
            else
              throw new AssertionError(token);
          }

          if (i == uri.length())
            break;

          token = nextToken;
          i += token.length();
          lexemeStart = i;
        }
        else
          i++;
      }
    }
    catch (Exception ex) {
      log.log(Level.FINER, ex);

      throw new IllegalArgumentException(L.l("problem parsing URI ''{0}'': {1}",
                                             uri, ex));
    }
  }

  private String parseURIToken(String uri, int i)
  {
   if (uri.regionMatches(i, URI_TOKEN_HOME, 0, URI_TOKEN_HOME.length()))
     return URI_TOKEN_HOME;
    if (uri.regionMatches(i, URI_TOKEN_CONF, 0, URI_TOKEN_CONF.length()))
      return URI_TOKEN_CONF;
    if (uri.regionMatches(i, URI_TOKEN_SERVER_ID, 0, URI_TOKEN_SERVER_ID.length()))
      return URI_TOKEN_SERVER_ID;
    if (uri.regionMatches(i, URI_TOKEN_SERVER_PORT, 0, URI_TOKEN_SERVER_PORT.length()))
      return URI_TOKEN_SERVER_PORT;
    if (uri.regionMatches(i, URI_TOKEN_SERVER_ADDRESS, 0, URI_TOKEN_SERVER_ADDRESS.length()))
      return URI_TOKEN_SERVER_ADDRESS;
    else
      return null;
  }

  public String getURI()
  {
    StringBuilder uri = new StringBuilder();

    uri.append("resin");

    if (_resinHome != null) {
      uri.append(URI_TOKEN_HOME);
      uri.append(_resinHome.getAbsolutePath());
    }

    if (_resinConf != null) {
      uri.append(":conf=");
      uri.append(_resinConf.getAbsolutePath());
    }


    if (_serverId != null) {
      uri.append(URI_TOKEN_SERVER_ID);
      uri.append(_serverId);
    }

    if ((0 < _serverPort && _serverPort < 65536)) {
      uri.append(URI_TOKEN_SERVER_PORT);
      uri.append(_serverPort);
    }

    if (_serverAddress != null) {
      uri.append(URI_TOKEN_SERVER_ADDRESS);
      uri.append(_serverAddress);
    }

    return uri.toString();
  }

  public String toString()
  {
    return getClass().getSimpleName() + "[" + getURI() + "]";
  }

  public static void main(String[] argv)
  {
  }

}
