package org.eclipse.bpmn2.modeler.core.merrimac.dialogs;

import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractDetailComposite;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.dialogs.MessageDialog;

public class ReadonlyTextObjectEditor extends TextAndButtonObjectEditor {

	public ReadonlyTextObjectEditor(AbstractDetailComposite parent,
			EObject object, EStructuralFeature feature) {
		super(parent, object, feature);
	}

	@Override
	protected void buttonClicked() {
		Object value = object.eGet(feature);
		if (value == null || value instanceof EObject) {
			ObjectEditingDialog dialog = new FeatureEditingDialog(getDiagramEditor(), object, feature, (EObject)value);
			dialog.open();
		}
		else {
			String msg = "Can not display "+feature.getName()+" for "+object.eClass().getName();
			MessageDialog.openError(getDiagramEditor().getSite().getShell(), "Internal Error!", msg);
		}
	}

}
