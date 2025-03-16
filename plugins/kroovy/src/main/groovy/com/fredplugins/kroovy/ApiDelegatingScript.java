package com.fredplugins.kroovy;

import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;

public abstract class ApiDelegatingScript extends Script
{
	private Object apiDelegate;
	private MetaClass apiMetaClass;

	protected ApiDelegatingScript()
	{
		super();
	}

	protected ApiDelegatingScript(Binding binding)
	{
		super(binding);
	}

	public Object event = null;

	public void hook()
	{
		hook(null);
	}

	public void hook(Object e)
	{
		this.event = e;
		this.run();
		this.event = null;
	}

	/**
	 * Sets the delegation target.
	 */
	public void setApiDelegate(Object delegate, MetaClass metaClass)
	{
		this.apiDelegate = delegate;
		if (metaClass == null)
		{
			this.apiMetaClass = InvokerHelper.getMetaClass(delegate.getClass());
		}
		else
		{
			this.apiMetaClass = metaClass;
		}
	}

	@Override
	public Object invokeMethod(String name, Object args)
	{
		try
		{
			if (apiDelegate instanceof GroovyObject)
			{
				return ((GroovyObject) apiDelegate).invokeMethod(name, args);
			}
			return apiMetaClass.invokeMethod(apiDelegate, name, args);
		}
		catch (MissingMethodException mme)
		{
			return super.invokeMethod(name, args);
		}
	}

	@Override
	public Object getProperty(String property)
	{
		try
		{
			return apiMetaClass.getProperty(apiDelegate, property);
		}
		catch (MissingPropertyException e)
		{
			return super.getProperty(property);
		}
	}

	@Override
	public void setProperty(String property, Object newValue)
	{
		try
		{
			apiMetaClass.setProperty(apiDelegate, property, newValue);
		}
		catch (MissingPropertyException e)
		{
			super.setProperty(property, newValue);
		}
	}

	public Object getApiDelegate()
	{
		return apiDelegate;
	}

	public MetaClass getApiMetaClass()
	{
		return apiMetaClass;
	}
}
