#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.impl;

import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import ${package}.Instance;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;


public class SpringContextInstance implements Instance {
    private final OsgiBundleXmlApplicationContext context;
    private final Properties configProps = new Properties();
    private String name;

    public SpringContextInstance(BundleContext bc, ApplicationContext parent, String aName, String pathInstanceContext) {
        name = aName;
        this.context = new OsgiBundleXmlApplicationContext(new String[]{pathInstanceContext}, parent);
        context.setBundleContext(bc);
        context.addBeanFactoryPostProcessor(beanFactory -> beanFactory.registerSingleton("configProps", configProps));
    }

    @Override
    public void init(Dictionary props) {
        reconfigure(props);
    }

    @Override
    public void reconfigure(Dictionary props) {
        configProps.clear();
        Enumeration keys = props.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            configProps.put(key, props.get(key));
        }
        context.refresh();
    }

    @Override
    public void stop() {
        context.stop();
        context.close();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
