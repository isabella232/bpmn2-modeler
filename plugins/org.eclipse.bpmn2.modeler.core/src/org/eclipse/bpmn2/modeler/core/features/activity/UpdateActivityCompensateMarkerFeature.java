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
 * @author Ivar Meikas
 ******************************************************************************/
package org.eclipse.bpmn2.modeler.core.features.activity;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.modeler.core.utils.GraphicsUtil;
import org.eclipse.bpmn2.modeler.core.utils.GraphicsUtil.Compensation;
import org.eclipse.bpmn2.modeler.core.utils.StyleUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;

// TODO: Auto-generated Javadoc
/**
 * The Class UpdateActivityCompensateMarkerFeature.
 */
public class UpdateActivityCompensateMarkerFeature extends AbstractUpdateMarkerFeature<Activity> {
	
	/** The is compensate property. */
	public static String IS_COMPENSATE_PROPERTY = "marker.compensate"; //$NON-NLS-1$
	
	/**
	 * Instantiates a new update activity compensate marker feature.
	 *
	 * @param fp the fp
	 */
	public UpdateActivityCompensateMarkerFeature(IFeatureProvider fp) {
	    super(fp);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.bpmn2.modeler.core.features.activity.AbstractUpdateMarkerFeature#getPropertyKey()
	 */
	@Override
	protected String getPropertyKey() {
	    return IS_COMPENSATE_PROPERTY;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.bpmn2.modeler.core.features.activity.AbstractUpdateMarkerFeature#isPropertyChanged(org.eclipse.bpmn2.FlowElement, java.lang.String)
	 */
	@Override
	protected boolean isPropertyChanged(Activity activity, String propertyValue) {
		return activity.isIsForCompensation() != new Boolean(propertyValue);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.bpmn2.modeler.core.features.activity.AbstractUpdateMarkerFeature#doUpdate(org.eclipse.bpmn2.FlowElement, org.eclipse.graphiti.mm.pictograms.ContainerShape)
	 */
	@Override
	protected void doUpdate(Activity activity, ContainerShape markerContainer) {
		if (activity.isIsForCompensation()) {
			GraphicsUtil.showActivityMarker(markerContainer, GraphicsUtil.ACTIVITY_MARKER_COMPENSATE);
		} else {
			GraphicsUtil.hideActivityMarker(markerContainer, GraphicsUtil.ACTIVITY_MARKER_COMPENSATE);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.bpmn2.modeler.core.features.activity.AbstractUpdateMarkerFeature#convertPropertyToString(org.eclipse.bpmn2.FlowElement)
	 */
	@Override
	protected String convertPropertyToString(Activity activity) {
	    return Boolean.toString(activity.isIsForCompensation());
    }
}