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
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.Message;
import org.eclipse.bpmn2.Operation;
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
public class JbpmSendTaskDetailComposite extends JbpmTaskDetailComposite {

	public final static String MESSAGE_NAME = "Message"; //$NON-NLS-1$

	/**
	 * @param section
	 */
	public JbpmSendTaskDetailComposite(AbstractBpmn2PropertySection section) {
		super(section);
	}

	public JbpmSendTaskDetailComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	protected void createMessageAssociations(Composite container, Activity activity,
			EReference operationRef, Operation operation,
			EReference messageRef, Message message) {
		Operation oldOperation = (Operation) activity.eGet(operationRef);
		boolean changed = (oldOperation != operation);

		super.createMessageAssociations(container, activity,
				operationRef, operation,
				messageRef, message);
		
		Resource resource = activity.eResource();
		InputOutputSpecification ioSpec = activity.getIoSpecification();
		if (ioSpec!=null) {
			if (ioSpec.getDataInputs().isEmpty()) {
				final DataInput dataInput =  Bpmn2ModelerFactory.createObject(resource, DataInput.class);
				dataInput.setName(MESSAGE_NAME);
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
			
			if (!MESSAGE_NAME.equals(ioSpec.getDataInputs().get(0).getName())) {
				if (changed) {
					ioSpec.getDataInputs().get(0).setName(MESSAGE_NAME);
				}
				else {
					final InputOutputSpecification ios = ioSpec;
					TransactionalEditingDomain domain = getDiagramEditor().getEditingDomain();
					domain.getCommandStack().execute(new RecordingCommand(domain) {
						@Override
						protected void doExecute() {
							ios.getDataInputs().get(0).setName(MESSAGE_NAME);
						}
					});
				}
			}
		}
		
		outputComposite.setAllowedMapTypes(MapType.Property.getValue());
		inputComposite.setAllowedMapTypes(MapType.Property.getValue() | MapType.SingleAssignment.getValue());
	}
}
