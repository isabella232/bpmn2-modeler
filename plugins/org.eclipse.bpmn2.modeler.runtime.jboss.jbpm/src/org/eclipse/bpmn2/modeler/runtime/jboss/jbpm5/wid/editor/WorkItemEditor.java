package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.wid.editor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.modeler.core.adapters.ExtendedPropertiesAdapter;
import org.eclipse.bpmn2.modeler.core.utils.JavaProjectClassLoader;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.wid.WorkItemDefinition;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class WorkItemEditor {
	public WorkItemDefinition wid;
	public Task task;
	public Dialog dialog;
	public WorkDefinition workDefinition;
	public DroolsProxy drools;
	public Work work;
	boolean done = false;
	
	private WorkItemEditor() {
	}
	
	public static WorkItemEditor create(WorkItemDefinition wid, Task task) {
    	WorkItemEditor workItemEditor = new WorkItemEditor();
    	workItemEditor.wid = wid;
    	workItemEditor.task = task;
		final Shell shell = Display.getDefault().getActiveShell();
    	final String customEditor = wid.getEclipseCustomEditor();
    	
		try {
			JavaProjectClassLoader cl = getProjectClassLoader(task);
			workItemEditor.drools = new DroolsProxy(cl);
			Class editorClass = cl.loadClass(customEditor);
			if (editorClass==null) {
				throw new ClassNotFoundException();
			}
	    	if (customEditor!=null && !customEditor.isEmpty()) {
	    		workItemEditor.dialog = (Dialog)editorClass.getConstructor(Shell.class).newInstance(shell);
	    	}
		}
		catch (IllegalArgumentException iae) {
			MessageDialog.openError(shell, "Custom Editor", "Cannot load the Custom Editor\n"+
					customEditor+
					"\nbecause the project is not a Java project.");
			return null;
		}
		catch (ClassNotFoundException cnfe) {
			MessageDialog.openError(shell, "Custom Editor", "Cannot find the Custom Editor\n"+
					customEditor+
					"\nin the class path.");
			return null;
		}
		catch (NoSuchMethodException nsme) {
			MessageDialog.openError(shell, "Custom Editor", "Cannot create the Custom Editor\n"+
					customEditor+
					"\nbecause the class does not have a constructor.");
			return null;
		}
		catch (ClassCastException cce) {
			MessageDialog.openError(shell, "Custom Editor", "Cannot create the Custom Editor\n"+
					customEditor+
					"\nbecause the class does not implement the WorkEditor interface.");
			return null;
		}
		catch (Exception ex) {
			MessageDialog.openError(shell, "Custom Editor", "Cannot create the Custom Editor\n"+
					customEditor+
					"\nbecause of an unknown error:\n"+ex.getMessage());
			ex.printStackTrace();
			return null;
		}
		return workItemEditor;
	}

	private static JavaProjectClassLoader getProjectClassLoader(BaseElement baseElement) {
		Resource res = ExtendedPropertiesAdapter.getResource(baseElement);
		URI uri = res.getURI();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.segment(1));
		JavaProjectClassLoader cl = new JavaProjectClassLoader(project);
		return cl;
	}
	
	public boolean show() {
		if (!done) {
			done = true;
			Object result = drools.invoke(dialog, "show");
			if (result instanceof Boolean && (Boolean)result) {
				Object workObject = drools.invoke(dialog, "getWork");
				work = new Work(this, workObject);
				return true;
			}
		}
		return false;
	}
	
    public Work getWork() {
    	if (work==null) {
    		work = new Work(this);
			drools.invokeWithTypes(dialog, "setWork", drools.loadClass("Work"), work.getObject());
    	}
    	return work;
    }

    public WorkDefinition getWorkDefinition() {
    	if (workDefinition==null) {
    		workDefinition = new WorkDefinition(this);
    		drools.invokeWithTypes(dialog, "setWorkDefinition", drools.loadClass("WorkDefinition"), workDefinition.getObject());
    	}
    	return workDefinition;
    }

	public ParameterDefinition getParameter(String name) {
		ParameterDefinition pd = getWorkDefinition().getParameter(name);
		if (pd==null) {
			pd = new ParameterDefinition(this);
			pd.setName(name);
			getWorkDefinition().addParameter(pd);
			getWork().addParameterDefinition(pd);
		}
		return pd;
	}

	public ParameterDefinition getResult(String name) {
		ParameterDefinition pd = getWorkDefinition().getResult(name);
		if (pd==null) {
			pd = new ParameterDefinition(this);
			pd.setName(name);
			getWorkDefinition().addResult(pd);
			getWork().addParameterDefinition(pd);
		}
		return pd;
	}
}