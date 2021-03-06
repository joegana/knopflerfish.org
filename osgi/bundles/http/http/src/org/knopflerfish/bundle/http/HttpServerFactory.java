/*
 * Copyright (c) 2003-2012, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.knopflerfish.bundle.http;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import org.knopflerfish.service.log.LogRef;

public class HttpServerFactory
  implements ManagedServiceFactory
{
  final static String DEFAULT_PID = Activator.FACTORY_PID + ".default";

  // private fields

  private final BundleContext bc;

  private final LogRef log;

  private final Dictionary<String, HttpServer> servers =
    new Hashtable<String, HttpServer>();

  // constructors

  HttpServerFactory(final BundleContext bc, final LogRef log)
  {
    this.bc = bc;
    this.log = log;
  }

  // public methods

  public void destroy()
  {
    final Enumeration<String> e = servers.keys();
    while (e.hasMoreElements()) {
      deleted(e.nextElement());
    }
  }

  // implements ManagedServiceFactory

  Object updateLock = new Object();

  public void updated(String pid, Dictionary<String, ?> configuration)
      throws ConfigurationException
  {
    synchronized (updateLock) {
      if (log.doDebug()) {
        log.debug("Updated pid=" + pid);
      }

      if (DEFAULT_PID.equals(pid) && servers.size() > 0) {
        if (log.doDebug()) {
          log.debug("Skip default since we already have something");
        }
        return;
      }

      // As soon as we get a "non-default"-config, delete default
      HttpConfig newConfig = null;
      final HttpServer defaultServer = servers.get(DEFAULT_PID);
      if (!DEFAULT_PID.equals(pid) && (null != defaultServer)) {
        if (log.doDebug()) {
          log.debug("Overriding default instance with new pid " + pid);
        }
        // Check if configured server has same as default, reuse
        newConfig = new HttpConfig(bc, configuration);
        if (defaultServer.portConfigMatch(newConfig)) {
          servers.put(pid, defaultServer);
        } else {
          deleted(DEFAULT_PID);
        }
      }

      HttpServer httpServer = servers.get(pid);
      if (httpServer == null) {
        if (log.doDebug()) {
          log.debug("create pid=" + pid);
        }
        if (newConfig == null) {
          newConfig = new HttpConfig(bc, configuration);
        }
        httpServer = new HttpServer(bc, newConfig, log);
        servers.put(pid, httpServer);

        // registration is moved to HttpServer.update()
      } else {
        // TODO should we really merge configs?
        httpServer.getHttpConfig().updated(configuration);
      }

      // this will setup and possibly register the actual service
      httpServer.updated();
    }
  }

  public void deleted(String pid)
  {
    final HttpServer httpServer = servers.remove(pid);
    if (httpServer != null) {
      if (log.doDebug()) {
        log.debug("delete pid=" + pid);
      }
      httpServer.destroy();
    }
  }

  public String getName()
  {
    return "Knopflerfish HTTP Service"; // NYI
  }

  public Enumeration<String> getServerPids()
  {
    return servers.keys();
  }

  public Enumeration<HttpServer> getServers()
  {
    return servers.elements();
  }

  public HttpServer getServer(String pid)
  {
    return servers.get(pid);
  }

} // HttpServerFactory
