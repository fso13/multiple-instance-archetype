#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.osgi.context.BundleContextAware;
import ${package}.impl.SpringContextInstance;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class InstanceManager implements ManagedServiceFactory, ApplicationContextAware, BundleContextAware {
    private static final Logger log = LoggerFactory.getLogger(InstanceManager.class);
    private static final String COLLECTOR_INSTANCE_NAME = "osgi.service.InstanceName";
    private final Map<String, Instance> instanceMap = new HashMap<String, Instance>(2);
    private final String pathInstanceContext;
    private ApplicationContext applicationContext;
    private BundleContext bundleContext;

    public InstanceManager(String pathInstanceContext) {
        this.pathInstanceContext = pathInstanceContext;
    }

    @Override
    public String getName() {
        return "Instance manager";
    }

    @Override
    public void updated(String pid, Dictionary properties) throws ConfigurationException {
        synchronized (instanceMap) {
            String instanceName = (String) properties.get(COLLECTOR_INSTANCE_NAME);
            if (instanceName == null || instanceName.trim().isEmpty()) {
                throw new ConfigurationException(COLLECTOR_INSTANCE_NAME, "Cannot start instance because of lack of instance name.");
            }
            try {
                if (instanceMap.containsKey(pid)) {
                    log.info("Updating configuration of DB Frontend instance: {}, pid: {}", instanceName, pid);
                    Instance instance = instanceMap.get(pid);
                    if (!instanceName.equals(instance.getName())) {
                        for (Instance inst : instanceMap.values()) {
                            if (!instance.equals(inst) && inst.getName().equals(instanceName)) {
                                throw new ConfigurationException(COLLECTOR_INSTANCE_NAME, "Cannot start instance because of non unique instance name:" + instanceName);
                            }
                        }
                        instance.setName(instanceName);
                    }
                    instance.reconfigure(properties);
                } else {
                    log.info("Starting instance: {}, pid: {}", instanceName, pid);
                    for (Instance instance : instanceMap.values()) {
                        if (instance.getName().equals(instanceName)) {
                            throw new ConfigurationException(COLLECTOR_INSTANCE_NAME, "Cannot start instance because of non unique instance name:" + instanceName);
                        }
                    }
                    Instance instance = new SpringContextInstance(bundleContext, applicationContext, instanceName, pathInstanceContext);
                    instance.init(properties);
                    instanceMap.put(pid, instance);
                    log.info("Start the instance: {}, pid: {}", instanceName, pid);
                }
            } catch (ConfigurationException e) {
                throw e;
            } catch (Exception e) {
                throw new ConfigurationException(null, "Cannot start instance", e);
            }
        }
    }

    @Override
    public void deleted(String pid) {
        synchronized (instanceMap) {
            log.info("Removing pid: {}", pid);
            if (instanceMap.containsKey(pid)) {
                instanceMap.remove(pid).stop();
                log.info("Remove the pid: {}", pid);
            }
        }
    }

    public void destroy() {
        synchronized (instanceMap) {
            for (Iterator<Instance> iterator = instanceMap.values().iterator(); iterator.hasNext(); ) {
                Instance instance = iterator.next();
                try {
                    log.info("Stopping instance: {}", instance);

                    instance.stop();
                } catch (Exception ex) {
                    log.error("Can't stop instance " + instance.getName(), ex);
                }
                iterator.remove();
                log.info("Stop instance: {}", instance);
            }
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
