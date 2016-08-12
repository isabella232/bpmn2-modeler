/*******************************************************************************
 * Copyright (c) 2011, 2012 Red Hat, Inc.
 *  All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 *
 * @author Bob Brodt
 ******************************************************************************/
package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.features;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.ItemKind;
import org.eclipse.bpmn2.OutputSet;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.modeler.core.IBpmn2RuntimeExtension;
import org.eclipse.bpmn2.modeler.core.adapters.ExtendedPropertiesAdapter;
import org.eclipse.bpmn2.modeler.core.features.CustomElementFeatureContainer;
import org.eclipse.bpmn2.modeler.core.features.CustomShapeFeatureContainer;
import org.eclipse.bpmn2.modeler.core.features.IShapeFeatureContainer;
import org.eclipse.bpmn2.modeler.core.model.Bpmn2ModelerFactory;
import org.eclipse.bpmn2.modeler.core.model.ModelDecorator;
import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor.Property;
import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
import org.eclipse.bpmn2.modeler.core.utils.JavaProjectClassLoader;
import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
import org.eclipse.bpmn2.modeler.core.utils.ShapeDecoratorUtil;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.JBPM5RuntimeExtension;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.customeditor.SampleCustomEditor;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.ParameterDefinition;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.Work;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.WorkDefinition;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.WorkEditor;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.datatype.DataTypeFactory;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.datatype.DataTypeRegistry;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.impl.ParameterDefinitionImpl;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.impl.WorkDefinitionImpl;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.drools.process.core.impl.WorkImpl;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.features.JbpmTaskFeatureContainer.JbpmAddTaskFeature;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.util.JbpmModelUtil;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.wid.WorkItemDefinition;
import org.eclipse.bpmn2.modeler.ui.ImageProvider;
import org.eclipse.bpmn2.modeler.ui.editor.BPMN2Editor;
import org.eclipse.bpmn2.modeler.ui.features.activity.task.TaskFeatureContainer;
import org.eclipse.bpmn2.modeler.ui.features.activity.task.TaskFeatureContainer.CreateTaskFeature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.swt.widgets.Display;

public class JbpmCustomTaskFeatureContainer extends CustomShapeFeatureContainer {
	
	@Override
	protected IShapeFeatureContainer createFeatureContainer(IFeatureProvider fp) {

		return new TaskFeatureContainer() {
			@Override
			public ICreateFeature getCreateFeature(IFeatureProvider fp) {
				return new JbpmCreateCustomTaskFeature(fp);
			}
			
			@Override
			public IAddFeature getAddFeature(IFeatureProvider fp) {
				return new JbpmAddCustomTaskFeature(fp);
			}
		};
	}
	
	protected class JbpmCreateCustomTaskFeature extends CreateTaskFeature {

		public JbpmCreateCustomTaskFeature(IFeatureProvider fp) {
			super(fp);
		}

		@Override
		public String getCreateImageId() {
			final String iconPath = (String) customTaskDescriptor.getPropertyValue("icon");  //$NON-NLS-1$
			if (iconPath != null && iconPath.trim().length() > 0) {
				return iconPath.trim();
			}
			return null;
		}

		@Override
		public String getCreateLargeImageId() {
			return getCreateImageId();
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Task createBusinessObject(ICreateContext context) {
			Task task = super.createBusinessObject(context);
			final String name = customTaskDescriptor.getName();
			if (name!=null && !name.isEmpty()) {
				task.setName(name.trim());
			}
			
			// make sure the ioSpecification has both a default InputSet and OutputSet
			InputOutputSpecification ioSpecification = task.getIoSpecification();
			if (ioSpecification!=null) {
				Resource resource = getResource(context);
				if (ioSpecification.getInputSets().size()==0) {
					InputSet is = Bpmn2ModelerFactory.createObject(resource, InputSet.class);
					ioSpecification.getInputSets().add(is);
				}
				if (ioSpecification.getOutputSets().size()==0) {
					OutputSet os = Bpmn2ModelerFactory.createObject(resource, OutputSet.class);
					ioSpecification.getOutputSets().add(os);
				}
			}
			
			// Create the ItemDefinitions for each I/O parameter if needed
			JBPM5RuntimeExtension rx = (JBPM5RuntimeExtension)customTaskDescriptor.getRuntime().getRuntimeExtension();
			String id = customTaskDescriptor.getId();
			WorkItemDefinition wid = rx.getWorkItemDefinition(id);
			if (ioSpecification!=null && wid!=null) {
				for (DataInput input : ioSpecification.getDataInputs()) {
					for (Entry<String, String> entry : wid.getParameters().entrySet()) {
						if (input.getName().equals(entry.getKey())) {
							if (entry.getValue()!=null)
								input.setItemSubjectRef(JbpmModelUtil.getDataType(context.getTargetContainer(), entry.getValue()));
							break;
						}
					}
				}
				for (DataOutput output : ioSpecification.getDataOutputs()) {
					for (Entry<String, String> entry : wid.getResults().entrySet()) {
						if (output.getName().equals(entry.getKey())) {
							if (entry.getValue()!=null)
								output.setItemSubjectRef(JbpmModelUtil.getDataType(context.getTargetContainer(), entry.getValue()));
							break;
						}
					}
				}
			}
			return task;
		}
	}
	
	protected class JbpmAddCustomTaskFeature extends JbpmAddTaskFeature {

		public JbpmAddCustomTaskFeature(IFeatureProvider fp) {
			super(fp);
		}
		
		@Override
		protected void decorateShape(IAddContext context, ContainerShape containerShape, Task businessObject) {
			super.decorateShape(context, containerShape, businessObject);
			final String iconPath = (String) customTaskDescriptor.getPropertyValue("icon");  //$NON-NLS-1$
			ShapeDecoratorUtil.createActivityImage(containerShape, iconPath);
		}
	}
	
	public String getId(EObject object) {
		if (object==null)
			return null;
		
		List<EStructuralFeature> features = ModelDecorator.getAnyAttributes(object);
		for (EStructuralFeature f : features) {
			if ("taskName".equals(f.getName())) { //$NON-NLS-1$
				Object attrValue = object.eGet(f);
				if (attrValue!=null) {
					// search the extension attributes for a "taskName" and compare it
					// against the new object's taskName value
					for (Property p : customTaskDescriptor.getProperties()) {
						String propValue = p.getFirstStringValue();
						if (attrValue.equals(propValue))
							return getId();
					}
				}
			}
		}
		return null;
	}

	@Override
	public ICustomFeature[] getCustomFeatures(IFeatureProvider fp) {
		ICustomFeature[] superFeatures = super.getCustomFeatures(fp);
		ICustomFeature[] thisFeatures = new ICustomFeature[superFeatures.length + 1];
		int i = 0;
		while (i<superFeatures.length)
			thisFeatures[i] = superFeatures[i++];
		thisFeatures[i] = new ConfigureWorkItemFeature(fp);
		return thisFeatures;
	}

	/**
	 * Returns the Java class that implements a Work Item Editor dialog for the
	 * given BaseElement if the WID file defines one.
	 * 
	 * @param baseElement
	 * @return a Work Item Editor dialog class or null if the BaseElement is not
	 *         a custom task (defined by a WID file) or if the WID file does
	 *         declare a "eclipse:customEditor" class.
	 *
	 * TODO: make this return an object instance and make sure it implements
	 * the {@code WorkEditor} interface.
	 */
	public static WorkEditor getWorkItemEditor(BaseElement baseElement) {
		String customTaskId = CustomElementFeatureContainer.findId(baseElement);
    	TargetRuntime rt = TargetRuntime.getRuntime(baseElement);
    	JBPM5RuntimeExtension rte = (JBPM5RuntimeExtension)rt.getRuntimeExtension();
    	WorkItemDefinition wid = ((JBPM5RuntimeExtension)rte).getWorkItemDefinition(customTaskId);
    	if (wid!=null) {
	    	String customEditor = wid.getEclipseCustomEditor();
	    	if (customEditor!=null && !customEditor.isEmpty()) {
				try {
					Resource res = ExtendedPropertiesAdapter.getResource(baseElement);
					URI uri = res.getURI();
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.segment(1));
		    		JavaProjectClassLoader cl = new JavaProjectClassLoader(project);
		    		if (cl!=null) {
		    			return new SampleCustomEditor(Display.getDefault().getActiveShell());
		    		}
				} catch (Exception ignore) {
				}
			}
    	}
    	return null;
	}
	
	public class ConfigureWorkItemFeature implements ICustomFeature {

		protected IFeatureProvider fp;
		boolean hasChanges = false;
		
		/**
		 * @param fp
		 */
		public ConfigureWorkItemFeature(IFeatureProvider fp) {
			this.fp = fp;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.features.IFeature#isAvailable(org.eclipse.graphiti.features.context.IContext)
		 */
		@Override
		public boolean isAvailable(IContext context) {
			PictogramElement[] pes = ((ICustomContext)context).getPictogramElements();
			if (pes.length==1) {
				BaseElement be = BusinessObjectUtil.getFirstBaseElement(pes[0]);
				String id = getId(be);
				return id!=null;
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.features.custom.ICustomFeature#canExecute(org.eclipse.graphiti.features.context.ICustomContext)
		 */
		@Override
		public boolean canExecute(ICustomContext context) {
			return canExecute((IContext)context);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.features.IFeature#canExecute(org.eclipse.graphiti.features.context.IContext)
		 */
		@Override
		public boolean canExecute(IContext context) {
			// TODO: clean this mess up: use {@code getWorkItemEditor()) to check if the selected task
			// has a WID and if the WID defines a customEditor
			BPMN2Editor editor = (BPMN2Editor)getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getDiagramContainer();
			IBpmn2RuntimeExtension rte = editor.getTargetRuntime().getRuntimeExtension();
			if (rte instanceof JBPM5RuntimeExtension && context instanceof ICustomContext) {
				PictogramElement[] pes = ((ICustomContext) context).getPictogramElements();
				if (pes.length==1) {
					Object o = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pes[0]);
					if (o instanceof Task) {
						Task task = (Task)o;
						List<EStructuralFeature> features = ModelDecorator.getAnyAttributes(task);
						for (EStructuralFeature f : features) {
							if ("taskName".equals(f.getName())) { //$NON-NLS-1$
								// make sure the Work Item Definition exists
								String taskName = (String)task.eGet(f);
								return ((JBPM5RuntimeExtension)rte).getWorkItemDefinition(taskName) != null;
							}
						}
					}
				}
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.features.custom.ICustomFeature#execute(org.eclipse.graphiti.features.context.ICustomContext)
		 */
		@Override
		public void execute(ICustomContext context) {
			execute((IContext)context);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.features.IFeature#execute(org.eclipse.graphiti.features.context.IContext)
		 */
		@Override
		public void execute(IContext context) {
			// TODO: clean this mess up: use {@code getWorkItemEditor()) to check if the selected task
			// has a WID and if the WID defines a customEditor
			BPMN2Editor editor = (BPMN2Editor)getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getDiagramContainer();
			PictogramElement pe = ((ICustomContext) context).getPictogramElements()[0];
			final Task task = (Task)Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
			String taskName = ""; //$NON-NLS-1$
			List<EStructuralFeature> features = ModelDecorator.getAnyAttributes(task);
			for (EStructuralFeature f : features) {
				if ("taskName".equals(f.getName())) { //$NON-NLS-1$
					taskName = (String)task.eGet(f);
					break;
				}
			}
			
			IBpmn2RuntimeExtension rte = editor.getTargetRuntime().getRuntimeExtension();
			WorkItemDefinition workItemDefinition = ((JBPM5RuntimeExtension)rte).getWorkItemDefinition(taskName);
			
			WorkDefinitionImpl wd = new WorkDefinitionImpl();
			for (String name : workItemDefinition.getParameters().keySet()) {
				String type = workItemDefinition.getParameters().get(name);
				DataTypeFactory factory = DataTypeRegistry.getFactory(type);
				wd.addParameter( new ParameterDefinitionImpl(name,factory.createDataType()) );
			}
			
			WorkImpl w = new WorkImpl();
			w.setName(taskName);
			w.setParameterDefinitions(wd.getParameters());
			for (DataInputAssociation dia : task.getDataInputAssociations()) {
				DataInput dataInput = (DataInput)dia.getTargetRef();
				if (dataInput!=null) {
					String name = dataInput.getName();
					ItemDefinition itemDefinition = dataInput.getItemSubjectRef();
					if (itemDefinition!=null) {
						Object structureRef = itemDefinition.getStructureRef();
						if (ModelUtil.isStringWrapper(structureRef)) {
							ParameterDefinition parameterDefinition = w.getParameterDefinition(name);
							try {
								Object value = parameterDefinition.getType().readValue(ModelUtil.getStringWrapperTextValue(structureRef));
								w.setParameter(name, value);
							}
							catch (Exception e) {
							}
						}
					}
				}
			}

			/*
			 * Load the class defined in the WID's "eclipse:customEditor" field.
			 * This means that the containing Project must be a JavaProject, and
			 * the class must exist and must implement the WorkEditor interface.
		try {
			Resource res = ExtendedPropertiesAdapter.getResource(task);
			URI uri = res.getURI();
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.segment(1));
			JavaProjectClassLoader cl = new JavaProjectClassLoader(project);
			Class c = cl.loadClass("org.bpmn2.java.Calculator");
			Object o = c.newInstance();
			String s = o.toString();
			System.out.println(s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			 */
			
			WorkEditor dialog = getWorkItemEditor(task);
			dialog.setWorkDefinition(wd);
			dialog.setWork(w);
			dialog.show();
			
			hasChanges = dialog.getWork() != w;
			if (hasChanges) {
				w = (WorkImpl) dialog.getWork();
				for (DataInputAssociation dia : task.getDataInputAssociations()) {
					DataInput dataInput = (DataInput)dia.getTargetRef();
					if (dataInput!=null) {
						String name = dataInput.getName();
						ItemDefinition itemDefinition = dataInput.getItemSubjectRef();
						// this always comes back as a String from the SampleCustomEditor dialog
						String value = (String)w.getParameter(name);
						if (value!=null && !value.isEmpty()) {
							EObject structureRef = ModelUtil.createStringWrapper(value);
							if (itemDefinition==null) {
								itemDefinition = Bpmn2ModelerFactory.createObject(task.eResource(), ItemDefinition.class);
								ModelUtil.getDefinitions(task).getRootElements().add(itemDefinition);
								ModelUtil.setID(itemDefinition);
							}
							itemDefinition.setItemKind(ItemKind.INFORMATION);
							itemDefinition.setStructureRef(structureRef);
							dataInput.setItemSubjectRef(itemDefinition);
						}
						else if (itemDefinition!=null) {
							// TODO: remove Item Definition if it is on longer referenced anywhere
//							ModelUtil.getDefinitions(task).getRootElements().remove(itemDefinition);
							dataInput.setItemSubjectRef(null);
						}
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.features.IFeature#canUndo(org.eclipse.graphiti.features.context.IContext)
		 */
		@Override
		public boolean canUndo(IContext context) {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.features.IFeature#hasDoneChanges()
		 */
		@Override
		public boolean hasDoneChanges() {
			return hasChanges;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.IName#getName()
		 */
		@Override
		public String getName() {
			return Messages.JbpmCustomTaskFeatureContainer_Name;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.IDescription#getDescription()
		 */
		@Override
		public String getDescription() {
			return Messages.JbpmCustomTaskFeatureContainer_Description;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.features.IFeatureProviderHolder#getFeatureProvider()
		 */
		@Override
		public IFeatureProvider getFeatureProvider() {
			return fp;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.graphiti.features.custom.ICustomFeature#getImageId()
		 */
		@Override
		public String getImageId() {
			return ImageProvider.IMG_16_CONFIGURE;
		}
	}
}
