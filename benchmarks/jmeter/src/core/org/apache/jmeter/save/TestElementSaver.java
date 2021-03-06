package org.apache.jmeter.save;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestElementTraverser;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * @version $Revision: 1.1 $
 */
public class TestElementSaver
    implements TestElementTraverser, SaveServiceConstants
{
    String name;
    LinkedList stack = new LinkedList();

    DefaultConfiguration rootConfig = null;

    public TestElementSaver(String name)
    {
        this.name = name;
    }

    public Configuration getConfiguration()
    {
        return rootConfig;
    }

    /* (non-Javadoc)
     * @see TestElementTraverser#startTestElement(TestElement)
     */
    public void startTestElement(TestElement el)
    {
        DefaultConfiguration config =
            new DefaultConfiguration("testelement", "testelement");
        config.setAttribute("class", el.getClass().getName());
        if (rootConfig == null)
        {
            rootConfig = config;
            if (name != null && name.length() > 0)
            {
                rootConfig.setAttribute("name", name);
            }
        }
        else
        {
            setConfigName(config);
        }
        stack.add(config);
    }

    public void setConfigName(DefaultConfiguration config)
    {
        if (!(stack.getLast() instanceof Configuration))
        {
            Object key = stack.removeLast();
            config.setAttribute("name", key.toString());
        }
    }

    /* (non-Javadoc)
     * @see TestElementTraverser#endTestElement(TestElement)
     */
    public void endTestElement(TestElement el)
    {
    }

    /* (non-Javadoc)
     * @see TestElementTraverser#simplePropertyValue(JMeterProperty)
     */
    public void simplePropertyValue(JMeterProperty value)
    {
        try
        {
            Object parent = stack.getLast();
            if (!(parent instanceof Configuration))
            {
                DefaultConfiguration config =
                    new DefaultConfiguration("property", "property");
                config.setValue(value != null ? value.toString() : "");
                config.setAttribute("name", parent.toString());
                config.setAttribute(XML_SPACE, PRESERVE);
                stack.removeLast();
                stack.add(config);
            }

            if (parent instanceof DefaultConfiguration
                && value instanceof Configuration)
            {
                ((DefaultConfiguration) parent).addChild((Configuration) value);
            }
            else if (
                parent instanceof DefaultConfiguration
                    && !(value instanceof Configuration))
            {
                DefaultConfiguration config =
                    new DefaultConfiguration("string", "string");
                config.setValue(value.toString());
                config.setAttribute(XML_SPACE, PRESERVE);
                ((DefaultConfiguration) parent).addChild(config);
            }
        }
        catch (NoSuchElementException e)
        {}
    }

    /* (non-Javadoc)
     * @see TestElementTraverser#startMap(MapProperty)
     */
    public void startMap(MapProperty map)
    {
        DefaultConfiguration config = new DefaultConfiguration("map", "map");
        config.setAttribute("class", map.getObjectValue().getClass().getName());
        config.setAttribute("name",map.getName());
        config.setAttribute("propType",map.getClass().getName());
        stack.add(config);
    }

    /* It appears that this method is no longer used.
     * jeremy_a@bigfoot.com  02 May 2003 
    public void endMap(MapProperty map)
    {
        finishConfig();
    }
     */
     
    /* (non-Javadoc)
     * @see TestElementTraverser#startCollection(CollectionProperty)
     */
    public void startCollection(CollectionProperty col)
    {
        DefaultConfiguration config =
            new DefaultConfiguration("collection", "collection");
        config.setAttribute("class", col.getObjectValue().getClass().getName());
        config.setAttribute("name",col.getName());
        config.setAttribute("propType",col.getClass().getName());
        stack.add(config);
    }

    /* It appears that this method is no longer used.
     * jeremy_a@bigfoot.com  02 May 2003 
    public void endCollection(CollectionProperty col)
    {
        finishConfig();
    }
     */
     
    /* (non-Javadoc)
     * @see TestElementTraverser#endProperty(JMeterProperty)
     */
    public void endProperty(JMeterProperty key)
    {
        finishConfig();
    }

    private void finishConfig()
    {
        if (stack.size() > 1)
        {
            Configuration config = (Configuration) stack.removeLast();
            ((DefaultConfiguration) stack.getLast()).addChild(config);
        }
    }

    /* (non-Javadoc)
     * @see TestElementTraverser#startProperty(JMeterProperty)
     */
    public void startProperty(JMeterProperty key)
    {
        if (key instanceof CollectionProperty)
        {
            startCollection((CollectionProperty) key);
        }
        else if (key instanceof MapProperty)
        {
            startMap((MapProperty) key);
        }
        else if (key instanceof TestElementProperty)
        {
            stack.addLast(key.getName());
        }
        else
        {
            DefaultConfiguration config =
                new DefaultConfiguration("property", "property");
            config.setValue(key.getStringValue());
            config.setAttribute("name", key.getName());
            config.setAttribute("propType", key.getClass().getName());
            config.setAttribute(XML_SPACE, PRESERVE);
            stack.addLast(config);
        }

    }

}
