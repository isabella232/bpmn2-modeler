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
				if (object.eContainer() instanceof SubProcess) {
					SubProcess subprocess = (SubProcess) object.eContainer();
					InputOutputSpecification ioSpec = subprocess.getIoSpecification();
					Resource resource = getResource();
					if (value instanceof ItemAwareElement) {
						ItemAwareElement element = (ItemAwareElement) value;
						DataInput input = null;
						InputSet inputSet = null;
						DataInputAssociation inputAssociation = null;
						if (ioSpec==null) {
							ioSpec = Bpmn2ModelerFactory.create(resource, InputOutputSpecification.class);
							subprocess.setIoSpecification(ioSpec);
						}
						else {
							for (DataInput din : ioSpec.getDataInputs()) {
								if (din == object.getLoopDataInputRef()) {
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
						
						value = input;
					}
					else if (value==null) {
						ItemAwareElement input = object.getLoopDataInputRef();
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
				}
				super.internalSet(object, feature, value, index);
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
				if (object.eContainer() instanceof SubProcess) {
					SubProcess subprocess = (SubProcess) object.eContainer();
					InputOutputSpecification ioSpec = subprocess.getIoSpecification();
					Resource resource = getResource();
					if (value instanceof ItemAwareElement) {
						ItemAwareElement element = (ItemAwareElement) value;
						DataOutput output = null;
						OutputSet outputSet = null;
						DataOutputAssociation outputAssociation = null;
						if (ioSpec==null) {
							ioSpec = Bpmn2ModelerFactory.create(resource, InputOutputSpecification.class);
							subprocess.setIoSpecification(ioSpec);
						}
						else {
							for (DataOutput dout : ioSpec.getDataOutputs()) {
								if (dout == object.getLoopDataOutputRef()) {
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

						for (DataOutputAssociation doa : subprocess.getDataOutputAssociations()) {
							if (doa.getSourceRef().size()==1 && doa.getSourceRef().get(0)==output) {
								outputAssociation = doa;
								break;
							}
						}
						if (outputAssociation == null) {
							outputAssociation = Bpmn2ModelerFactory.create(resource, DataOutputAssociation.class);
							subprocess.getDataOutputAssociations().add(outputAssociation);
						}
						
						outputAssociation.getSourceRef().clear();
						outputAssociation.getSourceRef().add(output);
						outputAssociation.setTargetRef(element);
						
						value = output;
					}
					else if (value==null) {
						ItemAwareElement output = object.getLoopDataOutputRef();
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
								for (DataOutputAssociation doa : subprocess.getDataOutputAssociations()) {
									if (doa.getSourceRef().size()>0 && doa.getSourceRef().get(0) == output) {
										subprocess.getDataOutputAssociations().remove(i);
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
				}
				super.internalSet(object, feature, value, index);
			}
		});
	}

}
