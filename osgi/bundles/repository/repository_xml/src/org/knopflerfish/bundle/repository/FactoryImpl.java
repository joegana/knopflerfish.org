package org.knopflerfish.bundle.repository;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.knopflerfish.bundle.repository.xml.RepositoryXmlParser;
import org.knopflerfish.service.repository.XmlBackedRepositoryFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.resource.Resource;
import org.osgi.service.repository.Repository;

public class FactoryImpl implements XmlBackedRepositoryFactory {
  private final BundleContext bc;
  private final HashMap<Object, ServiceRegistration<Repository>> repositoryRegistrations = new HashMap<Object, ServiceRegistration<Repository>>();

  FactoryImpl(BundleContext bc) {
    this.bc = bc;
  }
  
  @Override
  public ServiceReference<Repository> create(String url, Dictionary<String, ?> properties, Object handle) throws Exception {
    if(url != null && !"".equals(url) && !repositoryRegistrations.containsKey(url)) {
      Collection<Resource> rs = RepositoryXmlParser.parse(url);
      RepositoryXmlParser.debug(rs);
      if(!rs.isEmpty()) {
        RepositoryImpl repo = new RepositoryImpl(bc, rs);
        Hashtable<String, Object> h = new Hashtable<String, Object>();
        h.put(Constants.SERVICE_PID, "org.knopflerfish.repository.xml");
        h.put(Constants.SERVICE_DESCRIPTION, "XML repository from URL: " + url);
        //h.put("repository.url", url);
        if (properties != null) {
          for (Enumeration<String> e = properties.keys(); e.hasMoreElements(); ) {
            String key = e.nextElement();
            h.put(key, properties.get(key));
          }
        }
        ServiceRegistration<Repository> sr = bc.registerService(Repository.class, repo, h);

        repositoryRegistrations.put(url, sr);
        if(handle != null) {
          // User provided non-url custom handle
          repositoryRegistrations.put(handle, sr);
        }
        return sr.getReference();
      }
    }
    return null;
  }

  @Override
  public void destroy(Object handle) {
    ServiceRegistration<Repository> sr = repositoryRegistrations.remove(handle);
    if(sr != null) {
      sr.unregister();
      while(repositoryRegistrations.values().remove(sr)) {}; // Remove all mappings in case user provided custom handle
    }
  }
  
  void destroyAll() {
    while(!repositoryRegistrations.isEmpty()) {
      ServiceRegistration<Repository> sr = repositoryRegistrations.values().iterator().next();
      sr.unregister();
      while(repositoryRegistrations.values().remove(sr)) {}; // Remove all mappings in case user provided custom handle
    }
  }

}