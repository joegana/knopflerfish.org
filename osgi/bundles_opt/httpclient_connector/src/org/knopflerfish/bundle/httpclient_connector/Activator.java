/*
 * Copyright (c) 2006-2012, KNOPFLERFISH project
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
package org.knopflerfish.bundle.httpclient_connector;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.io.ConnectionFactory;

import org.knopflerfish.service.log.LogRef;

public class Activator implements BundleActivator
{
  static LogRef log;

  private ServiceRegistration reg;

  public void start(BundleContext bc) throws Exception {
    log = new LogRef(bc);

    final Dictionary properties = new Hashtable();

    try {
      Class.forName("javax.net.ssl.HttpsURLConnection");
      properties.put(ConnectionFactory.IO_SCHEME,
                     new String[] { "http", "https" });
    } catch (ClassNotFoundException cnfe) {
      properties.put(ConnectionFactory.IO_SCHEME, new String[] { "http" });
    }

    // this implementation should be preferred over the standard implementation
    properties.put(Constants.SERVICE_RANKING, new Integer(10));
    reg = bc.registerService(ConnectionFactory.class.getName(),
                             new HttpClientFactory(bc), properties);
  }

  public void stop(BundleContext context) throws Exception {
    reg.unregister();
    reg = null;
  }

}
