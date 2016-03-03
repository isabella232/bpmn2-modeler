/*******************************************************************************
 * Copyright (c) 2011, 2012, 2013 Red Hat, Inc.
 * All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 	Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.property.adapters;

import java.util.Hashtable;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.DataOutputAssociation;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.ItemAwareElement;
import org.eclipse.bpmn2.MultiInstanceLoopCharacteristics;
import org.eclipse.bpmn2.OutputSet;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.modeler.core.adapters.FeatureDescriptor;
import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.JavaVariableNameObjectEditor;
import org.eclipse.bpmn2.modeler.core.model.Bpmn2ModelerFactory;
import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
import org.eclipse.bpmn2.modeler.ui.adapters.properties.MultiInstanceLoopCharacteristicsPropertiesAdapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

public class JbpmMultiInstanceLoopCharacteristicsPropertiesAdapter extends MultiInstanceLoopCharacteristicsPropertiesAdapter {

	public JbpmMultiInstanceLoopCharacteristicsPropertiesAdapter(AdapterFactory adapterFactory, MultiInstanceLoopCharacteristics object) {
		super(adapterFactory, object);

		EStructuralFeature feature = Bpmn2Package.eINSTANCE.getMultiInstanceLoopCharacteristics_InputDataItem();
		setProperty(feature, UI_OBJECT_EDITOR_CLASS, JavaVariableNameObjectEditor.class);
		
		feature = Bpmn2Package.eINSTANCE.getMultiInstanceLoopCharacteristics_OutputDataItem();
		setProperty(feature, UI_OBJECT_EDITOR_CLASS, JavaVariableNameObjectEditor.class);

		setFeatureDescriptor(LOOP_DATA_INPUT_REF, new LoopCharacteristicsDataIoFeatureDescriptor(this, object, LOOP_DATA_INPUT_REF) {
			
			
			/**
			 * For the Combobox selection strings we want to select all of the in-scope Properties;
			 * this includes so-called "local variables" defined in containers such as SubProcesses.
			 *  
			 * @see org.eclipse.bpmn2.modeler.ui.adapters.properties.MultiInstanceLoopCharacteristicsPropertiesAdapter.LoopCharacteristicsDataIoFeatureDescriptor#getChoiceOfValues()
			 */
			@Override
			public Hashtable<String, Object> getChoiceOfValues() {
				Hashtable<String, Object> choices = new Hashtable<String, Object>();
				EObject container = ModelUtil.getContainer(object);
				// get the Property instances (a.k.a. "local variables") of the containing Process or SubProcess
				for (EObject p : ModelUtil.collectAncestorObjects(object, "properties", new Class[] {Process.class, SubProcess.class})) { 
					choices.put( getChoiceString(p), p);
				}
				return choices;
			}
			
			@Override
			protected void internalSet(MultiInstanceLoopCharacteristics object, EStructuralFeature feature, Object value, int index) {
				ItemAwareElement element = null;
				if (value instanceof ItemAwareElement)
					element = (ItemAwareElement) value;
				
				if (object.eContainer() instanceof Activity) {
					value = setInputCollection(object, (Activity) object.eContainer(), element);
				}
				super.internalSet(object, feature, value, index);
			}
			
			@Override
			public String getTextValue() {
				Object value = getValue();
				if (value!=null) {
					return super.getChoiceString(value);
				}					
				return "";
			}

			@Override
			public Object getValue() {
				ItemAwareElement din = object.getLoopDataInputRef();
				if (din==null)
					return null;
				// this is the LoopDataInputRef in the MI Loop Characteristics
				if (object.eContainer() instanceof SubProcess) {
					// get the name from the DataInput itself
					return super.getValue();
				}
				else if (object.eContainer() instanceof Task) {
					// get the name from the Task's mapped DataInput
					Task task = (Task) object.eContainer();
					for (DataInputAssociation dia : task.getDataInputAssociations()) {
						if (din == dia.getTargetRef() && dia.getSourceRef().size()>0 && dia.getSourceRef().get(0) instanceof ItemAwareElement) {
							return dia.getSourceRef().get(0);
						}
					}
				}				
				return null;
			}
		});

		setFeatureDescriptor(INPUT_DATA_ITEM, new FeatureDescriptor<MultiInstanceLoopCharacteristics>(this, object, INPUT_DATA_ITEM) {

			@Override
			public Hashtable<String, Object> getChoiceOfValues() {
				Hashtable<String, Object> choices = new Hashtable<String, Object>();
				EObject container = ModelUtil.getContainer(object);
				// get the Property instances (a.k.a. "local variables") of the containing Process or SubProcess
				if (container instanceof Activity) {
					Activity activity = (Activity) container;
					InputOutputSpecification iospec = activity.getIoSpecification();
					if (iospec!=null) {
						for (DataInput din : iospec.getDataInputs()) {
							if (object.getLoopDataInputRef()!=din)
								choices.put(getChoiceString(din), din);
						}
					}
				}
				return choices;
			}

			@Override
			protected void internalSet(MultiInstanceLoopCharacteristics object, EStructuralFeature feature, Object value, int index) {
				ItemAwareElement element = null;
				if (value instanceof ItemAwareElement)
					element = (ItemAwareElement) value;
				
				if (object.eContainer() instanceof SubProcess) {
					value = setSubProcessInputItem(object, (SubProcess) object.eContainer(), element);
				}
				else if (object.eContainer() instanceof Task) {
					value = setTaskInputItem(object, (Task) object.eContainer(), element);
				}				
				super.internalSet(object, feature, value, index);
			}
			
			@Override
			public String getTextValue() {
				Object value = getValue();
				if (value!=null) {
					return super.getChoiceString(value);
				}					
				return "";
			}

			@Override
			public Object getValue() {
				DataInput din = object.getInputDataItem();
				if (din==null)
					return null;
				// this is the DataInput in the MI Loop Characteristics
				if (object.eContainer() instanceof SubProcess) {
					// get the name from the DataInput itself
					return din;
				}
				else if (object.eContainer() instanceof Task) {
					// get the name from the Task's mapped DataInput
					Task task = (Task) object.eContainer();
					for (DataInputAssociation dia : task.getDataInputAssociations()) {
						if (dia.getSourceRef().contains(din) && dia.getTargetRef() instanceof DataInput) {
							return dia.getTargetRef();
						}
					}
				}				
				return null;
			}
		});
		
		setFeatureDescriptor(LOOP_DATA_OUTPUT_REF, new LoopCharacteristicsDataIoFeatureDescriptor(this, object, LOOP_DATA_OUTPUT_REF) {
			@Override
			public Hashtable<String, Object> getChoiceOfValues() {
				Hashtable<String, Object> choices = new Hashtable<String, Object>();
				EObject container = ModelUtil.getContainer(object);
				// get the Property instances (a.k.a. "local variables") of the containing Process or SubProcess
				for (EObject p : ModelUtil.collectAncestorObjects(object, "properties", new Class[] {Process.class, SubProcess.class})) { 
					choices.put( getChoiceString(p), p);
				}
				return choices;
			}
			
			@Override
			protected void internalSet(MultiInstanceLoopCharacteristics object, EStructuralFeature feature, Object value, int index) {
				ItemAwareElement element = null;
				if (value instanceof ItemAwareElement)
					element = (ItemAwareElement) value;
				
				if (object.eContainer() instanceof Activity) {
					value = setOutputCollection(object, (Activity) object.eContainer(), element);
				}
				super.internalSet(object, feature, value, index);
			}
			
			@Override
			public String getTextValue() {
				Object value = getValue();
				if (value!=null) {
					return super.getChoiceString(value);
				}					
				return "";
			}

			@Override
			public Object getValue() {
				ItemAwareElement dout = object.getLoopDataOutputRef();
				if (dout==null)
					return null;
				// this is the DataInput in the MI Loop Characteristics
				if (object.eContainer() instanceof SubProcess) {
					// get the name from the DataInput itself
					return super.getValue();
				}
				else if (object.eContainer() instanceof Task) {
					// get the name from the Task's mapped DataInput
					Task task = (Task) object.eContainer();
					for (DataOutputAssociation doa : task.getDataOutputAssociations()) {
						if (doa.getSourceRef().contains(dout) && doa.getTargetRef() instanceof ItemAwareElement) {
							return doa.getTargetRef();
						}
					}
				}				
				return null;
			}
		});
		
		setFeatureDescriptor(OUTPUT_DATA_ITEM, new FeatureDescriptor<MultiInstanceLoopCharacteristics>(this, object, OUTPUT_DATA_ITEM) {

			@Override
			public Hashtable<String, Object> getChoiceOfValues() {
				Hashtable<String, Object> choices = new Hashtable<String, Object>();
				EObject container = ModelUtil.getContainer(object);
				// get the Property instances (a.k.a. "local variables") of the containing Process or SubProcess
				if (container instanceof Activity) {
					Activity activity = (Activity) container;
					InputOutputSpecification iospec = activity.getIoSpecification();
					if (iospec!=null) {
						for (DataOutput dout : iospec.getDataOutputs()) {
							if (object.getLoopDataOutputRef()!=dout)
								choices.put(getChoiceString(dout), dout);
						}
					}
				}
				return choices;
			}

			@Override
			protected void internalSet(MultiInstanceLoopCharacteristics object, EStructuralFeature feature, Object value, int index) {
				ItemAwareElement element = null;
				if (value instanceof ItemAwareElement)
					element = (ItemAwareElement) value;
				
				if (object.eContainer() instanceof SubProcess) {
					value = setSubProcessOutputItem(object, (SubProcess) object.eContainer(), element);
				}
				else if (object.eContainer() instanceof Task) {
					value = setTaskOutputItem(object, (Task) object.eContainer(), element);
				}				
				super.internalSet(object, feature, value, index);
			}
			
			@Override
			public String getTextValue() {
				Object value = getValue();
				if (value!=null) {
					return super.getChoiceString(value);
				}					
				return "";
			}

			@Override
			public Object getValue() {
				DataOutput dout = object.getOutputDataItem();
				if (dout==null)
					return null;
				// this is the DataOutput in the MI Loop Characteristics
				if (object.eContainer() instanceof SubProcess) {
					// get the name from the DataOutput itself
					return dout;
				}
				else if (object.eContainer() instanceof Task) {
					// get the name from the Task's mapped DataOutput
					Task task = (Task) object.eContainer();
					for (DataOutputAssociation dia : task.getDataOutputAssociations()) {
						if (dia.getTargetRef()==dout && dia.getSourceRef().size()>0 && dia.getSourceRef().get(0) instanceof DataOutput) {
							return dia.getSourceRef().get(0);
						}
					}
				}				
				return null;
			}
		});

	}

	/**
	 * Set or clear the Loop Data Input Reference feature of the given
	 * MultiInstancLoopCharacteristics object.
	 * 
	 * This also manages the Activity's Data Inputs and Input Sets in the
	 * IOSpecificaiont as well as the Activity's DataInputAssociations.
	 * 
	 * @param milc
	 *            the MultiInstancLoopCharacteristics object
	 * @param subprocess
	 *            the affected Activity
	 * @param element
	 *            the new value for the Loop Data Input
	 * @see MultiInstanceLoopCharacteristics#setLoopDataOutputRef(ItemAwareElement)
	 */
	private ItemAwareElement setInputCollection(MultiInstanceLoopCharacteristics milc, Activity subprocess, ItemAwareElement element) {
		InputOutputSpecification ioSpec = subprocess.getIoSpecification();
		Resource resource = getResource();
		if (element!=null) {
			DataInput input = null;
			InputSet inputSet = null;
			DataInputAssociation inputAssociation = null;
			if (ioSpec==null) {
				ioSpec = Bpmn2ModelerFactory.create(resource, InputOutputSpecification.class);
				subprocess.setIoSpecification(ioSpec);
			}
			else {
				for (DataInput din : ioSpec.getDataInputs()) {
					if (din == milc.getLoopDataInputRef()) {
						input = din;
						break;
					}
				}
			}
			if (input == null)
				input = Bpmn2ModelerFactory.create(resource, DataInput.class);
			input.setName(element.getId());
			input.setItemSubjectRef(element.getItemSubjectRef());
			input.setIsCollection(true);
			if (!ioSpec.getDataInputs().contains(input))
				ioSpec.getDataInputs().add(input);
			
			for (InputSet is : ioSpec.getInputSets()) {
				if (is.getDataInputRefs().contains(input)) {
					inputSet = is;
					break;
				}
			}
			if (inputSet == null) {
				if (ioSpec.getInputSets().size()==0) {
					inputSet = Bpmn2ModelerFactory.create(resource, InputSet.class);
					ioSpec.getInputSets().add(inputSet);
				}
				else
					inputSet = ioSpec.getInputSets().get(0);
			}
			if (!inputSet.getDataInputRefs().contains(input))
				inputSet.getDataInputRefs().add(input);

			for (DataInputAssociation dia : subprocess.getDataInputAssociations()) {
				if (dia.getTargetRef()==input) {
					inputAssociation = dia;
					break;
				}
			}
			if (inputAssociation == null) {
				inputAssociation = Bpmn2ModelerFactory.create(resource, DataInputAssociation.class);
				subprocess.getDataInputAssociations().add(inputAssociation);
			}
			
			inputAssociation.setTargetRef(input);
			inputAssociation.getSourceRef().clear();
			inputAssociation.getSourceRef().add(element);
			
			element = input;
		}
		else {
			ItemAwareElement input = milc.getLoopDataInputRef();
			if (ioSpec!=null) {
				if (input!=null) {
					for (DataInput din : ioSpec.getDataInputs()) {
						if (din == input) {
							ioSpec.getDataInputs().remove(din);
							if (ioSpec.getInputSets().size()>0) {
								ioSpec.getInputSets().get(0).getDataInputRefs().remove(din);
								if (ioSpec.getInputSets().get(0).getDataInputRefs().size()==0)
									ioSpec.getInputSets().remove(0);
							}
							break;
						}
					}
					int i = 0;
					for (DataInputAssociation dia : subprocess.getDataInputAssociations()) {
						if (dia.getTargetRef() == input) {
							subprocess.getDataInputAssociations().remove(i);
							break;
						}
						++i;
					}
				}
				if (ioSpec.getDataInputs().size()==0 && ioSpec.getDataOutputs().size()==0) {
					subprocess.setIoSpecification(null);
				}
			}
		}
		
		return element;
	}
	
	/**
	 * Set or clear the Loop Data Output Reference feature of the given
	 * MultiInstancLoopCharacteristics object.
	 * 
	 * This also manages the Activity's Data Outputs and Output Sets in the
	 * IOSpecificaiont as well as the Activity's DataOutputAssociations.
	 * 
	 * @param milc
	 *            the MultiInstancLoopCharacteristics object
	 * @param activity
	 *            the affected Activity
	 * @param element
	 *            the new value for the Loop Data Output
	 * @see MultiInstanceLoopCharacteristics#setLoopDataOutputRef(ItemAwareElement)
	 */
	private ItemAwareElement setOutputCollection(MultiInstanceLoopCharacteristics milc, Activity activity, ItemAwareElement element) {
		InputOutputSpecification ioSpec = activity.getIoSpecification();
		Resource resource = getResource();
		if (element!=null) {
			DataOutput output = null;
			OutputSet outputSet = null;
			DataOutputAssociation outputAssociation = null;
			if (ioSpec==null) {
				ioSpec = Bpmn2ModelerFactory.create(resource, InputOutputSpecification.class);
				activity.setIoSpecification(ioSpec);
			}
			else {
				for (DataOutput dout : ioSpec.getDataOutputs()) {
					if (dout == milc.getLoopDataOutputRef()) {
						output = dout;
						break;
					}
				}
			}
			if (output == null)
				output = Bpmn2ModelerFactory.create(resource, DataOutput.class);
			output.setName(element.getId());
			output.setItemSubjectRef(element.getItemSubjectRef());
			output.setIsCollection(true);
			if (!ioSpec.getDataOutputs().contains(output))
				ioSpec.getDataOutputs().add(output);
			
			for (OutputSet os : ioSpec.getOutputSets()) {
				if (os.getDataOutputRefs().contains(output)) {
					outputSet = os;
					break;
				}
			}
			if (outputSet == null) {
				if (ioSpec.getOutputSets().size()==0) {
					outputSet = Bpmn2ModelerFactory.create(resource, OutputSet.class);
					ioSpec.getOutputSets().add(outputSet);
				}
				else
					outputSet = ioSpec.getOutputSets().get(0);
			}
			if (!outputSet.getDataOutputRefs().contains(output))
				outputSet.getDataOutputRefs().add(output);

			for (DataOutputAssociation doa : activity.getDataOutputAssociations()) {
				if (doa.getSourceRef().size()==1 && doa.getSourceRef().get(0)==output) {
					outputAssociation = doa;
					break;
				}
			}
			if (outputAssociation == null) {
				outputAssociation = Bpmn2ModelerFactory.create(resource, DataOutputAssociation.class);
				activity.getDataOutputAssociations().add(outputAssociation);
			}
			
			outputAssociation.getSourceRef().clear();
			outputAssociation.getSourceRef().add(output);
			outputAssociation.setTargetRef(element);
			
			element = output;
		}
		else {
			ItemAwareElement output = milc.getLoopDataOutputRef();
			if (ioSpec!=null) {
				if (output!=null) {
					for (DataOutput dout : ioSpec.getDataOutputs()) {
						if (dout == output) {
							ioSpec.getDataOutputs().remove(dout);
							if (ioSpec.getOutputSets().size()>0) {
								ioSpec.getOutputSets().get(0).getDataOutputRefs().remove(dout);
								if (ioSpec.getOutputSets().get(0).getDataOutputRefs().size()==0)
									ioSpec.getOutputSets().remove(0);
							}
							break;
						}
					}
					int i = 0;
					for (DataOutputAssociation doa : activity.getDataOutputAssociations()) {
						if (doa.getSourceRef().size()>0 && doa.getSourceRef().get(0) == output) {
							activity.getDataOutputAssociations().remove(i);
							break;
						}
						++i;
					}
				}
				if (ioSpec.getDataInputs().size()==0 && ioSpec.getDataOutputs().size()==0) {
					activity.setIoSpecification(null);
				}
			}

		}
		return element;
	}
	
	private ItemAwareElement setSubProcessInputItem(MultiInstanceLoopCharacteristics milc, SubProcess subprocess, ItemAwareElement element) {
		return element;
	}

	private ItemAwareElement setSubProcessOutputItem(MultiInstanceLoopCharacteristics milc, SubProcess subprocess, ItemAwareElement element) {
		return element;
	}

	private ItemAwareElement setTaskInputItem(MultiInstanceLoopCharacteristics milc, Task task, ItemAwareElement element) {
		Resource resource = task.eResource();
		// find the old DataInputAssociation for the current MI Input Data Item
		// and delete it because it will be replaced by a new one.
		DataInput oldDin = milc.getInputDataItem();
		if (oldDin!=null) {
			for (DataInputAssociation dia : task.getDataInputAssociations()) {
				if (dia.getSourceRef().contains(oldDin)) {
					task.getDataInputAssociations().remove(dia);
					break;
				}
			}
		}		
		
		// find the DataInputAssociation for this DataInput, if it exists
		DataInputAssociation dataInputAssociation = null;
		for (DataInputAssociation dia : task.getDataInputAssociations()) {
			if (dia.getTargetRef() == element) {
				dataInputAssociation = dia;
				break;
			}
		}
		if (dataInputAssociation==null) {
			// not found? create one!
			dataInputAssociation = Bpmn2ModelerFactory.create(resource, DataInputAssociation.class);
			task.getDataInputAssociations().add(dataInputAssociation);
			dataInputAssociation.setTargetRef(element);
		}
		// create a new DataInput for the MI loop input item
		// and map it to the given element
		DataInput din = Bpmn2ModelerFactory.create(resource, DataInput.class);
		din.setName(((DataInput)element).getName());
		din.setItemSubjectRef(element.getItemSubjectRef());
		dataInputAssociation.getSourceRef().clear();
		dataInputAssociation.getSourceRef().add(din);
		return din;
	}

	private ItemAwareElement setTaskOutputItem(MultiInstanceLoopCharacteristics milc, Task task, ItemAwareElement element) {
		Resource resource = task.eResource();
		// find the old DataOutputAssociation for the current MI Output Data Item
		// and delete it because it will be replaced by a new one.
		DataOutput oldDout = milc.getOutputDataItem();
		if (oldDout!=null) {
			for (DataOutputAssociation doa : task.getDataOutputAssociations()) {
				if (doa.getTargetRef() == oldDout) {
					task.getDataOutputAssociations().remove(doa);
					break;
				}
			}
		}		
		
		// find the DataOutputAssociation for this DataOutput, if it exists
		DataOutputAssociation dataOutputAssociation = null;
		for (DataOutputAssociation doa : task.getDataOutputAssociations()) {
			if (doa.getSourceRef().contains(element)) {
				dataOutputAssociation = doa;
				break;
			}
		}
		if (dataOutputAssociation==null) {
			// not found? create one!
			dataOutputAssociation = Bpmn2ModelerFactory.create(resource, DataOutputAssociation.class);
			task.getDataOutputAssociations().add(dataOutputAssociation);
			dataOutputAssociation.getSourceRef().clear();
			dataOutputAssociation.getSourceRef().add(element);
		}
		// create a new DataOutput for the MI loop input item
		// and map it to the given element
		DataOutput dout = Bpmn2ModelerFactory.create(resource, DataOutput.class);
		dout.setName(((DataOutput)element).getName());
		dout.setItemSubjectRef(element.getItemSubjectRef());
		dataOutputAssociation.setTargetRef(dout);
		return dout;
	}
}
