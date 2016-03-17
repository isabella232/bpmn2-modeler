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

package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.property;


import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.Message;
import org.eclipse.bpmn2.Operation;
import org.eclipse.bpmn2.modeler.core.adapters.InsertionAdapter;
import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractBpmn2PropertySection;
import org.eclipse.bpmn2.modeler.core.model.Bpmn2ModelerFactory;
import org.eclipse.bpmn2.modeler.ui.property.tasks.DataAssociationDetailComposite.MapType;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Bob Brodt
 *
 */
public class JbpmServiceTaskDetailComposite extends JbpmTaskDetailComposite {

	public final static String INPUT_NAME = "Parameter"; //$NON-NLS-1$
	public final static String OUTPUT_NAME = "Result"; //$NON-NLS-1$
	
	public JbpmServiceTaskDetailComposite(AbstractBpmn2PropertySection section) {
		super(section);
	}

	public JbpmServiceTaskDetailComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	protected void createMessageAssociations(Composite container, Activity serviceTask,
			EReference operationRef, Operation operation,
			EReference messageRef, Message message) {
		Operation oldOperation = (Operation) serviceTask.eGet(operationRef);
		boolean changed = (oldOperation != operation);

		super.createMessageAssociations(container, serviceTask,
				operationRef, operation,
				messageRef, message);
		
		Resource resource = serviceTask.eResource();
		InputOutputSpecification ioSpec = serviceTask.getIoSpecification();
		if (ioSpec==null) {
			ioSpec = Bpmn2ModelerFactory.createObject(resource, InputOutputSpecification.class);
			if (changed) {
				serviceTask.setIoSpecification(ioSpec);
			}
			else {
				InsertionAdapter.add(serviceTask,
						PACKAGE.getActivity_IoSpecification(),
						ioSpec);
			}
		}

		if (ioSpec.getDataInputs().isEmpty()) {
			if (ioSpec.getDataInputs().isEmpty()) {
				final DataInput dataInput =  Bpmn2ModelerFactory.createObject(resource, DataInput.class);
				dataInput.setName(INPUT_NAME);
				if (changed) {
					ioSpec.getDataInputs().add(dataInput);
				}
				else {
					final InputOutputSpecification ios = ioSpec;
					TransactionalEditingDomain domain = getDiagramEditor().getEditingDomain();
					domain.getCommandStack().execute(new RecordingCommand(domain) {
						@Override
						protected void doExecute() {
							ios.getDataInputs().add(dataInput);
						}
					});
				}
			}
		}
		if (ioSpec.getDataOutputs().isEmpty()) {
			final DataOutput dataOutput =  Bpmn2ModelerFactory.createObject(resource, DataOutput.class);
			dataOutput.setName(OUTPUT_NAME);
			if (changed) {
				ioSpec.getDataOutputs().add(dataOutput);
			}
			else {
				final InputOutputSpecification ios = ioSpec;
				TransactionalEditingDomain domain = getDiagramEditor().getEditingDomain();
				domain.getCommandStack().execute(new RecordingCommand(domain) {
					@Override
					protected void doExecute() {
						ios.getDataOutputs().add(dataOutput);
					}
				});
			}
		}

		if (!INPUT_NAME.equals(ioSpec.getDataInputs().get(0).getName())) {
			if (changed) {
				ioSpec.getDataInputs().get(0).setName(INPUT_NAME);
			}
			else {
				final InputOutputSpecification ios = ioSpec;
				TransactionalEditingDomain domain = getDiagramEditor().getEditingDomain();
				domain.getCommandStack().execute(new RecordingCommand(domain) {
					@Override
					protected void doExecute() {
						ios.getDataInputs().get(0).setName(INPUT_NAME);
					}
				});
			}
		}

		if (!OUTPUT_NAME.equals(ioSpec.getDataOutputs().get(0).getName())) {
			if (changed) {
				ioSpec.getDataOutputs().get(0).setName(OUTPUT_NAME);
			}
			else {
				final InputOutputSpecification ios = ioSpec;
				TransactionalEditingDomain domain = getDiagramEditor().getEditingDomain();
				domain.getCommandStack().execute(new RecordingCommand(domain) {
					@Override
					protected void doExecute() {
						ios.getDataOutputs().get(0).setName(OUTPUT_NAME);
					}
				});
			}
		}

		outputComposite.setAllowedMapTypes(MapType.Property.getValue());
		inputComposite.setAllowedMapTypes(MapType.Property.getValue() | MapType.SingleAssignment.getValue());
	}
	
}
