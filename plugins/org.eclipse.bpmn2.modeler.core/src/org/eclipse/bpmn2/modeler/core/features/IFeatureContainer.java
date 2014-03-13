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
package org.eclipse.bpmn2.modeler.core.features;

import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IDirectEditingFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;

// TODO: Auto-generated Javadoc
/**
 * The Interface IFeatureContainer.
 */
public interface IFeatureContainer {
	
	/**
	 * Checks if is available.
	 *
	 * @param fp the fp
	 * @return true, if is available
	 */
	boolean isAvailable(IFeatureProvider fp);
	
	/**
	 * Gets the apply object.
	 *
	 * @param context the context
	 * @return the apply object
	 */
	Object getApplyObject(IContext context);
	
	/**
	 * Can apply to.
	 *
	 * @param o the o
	 * @return true, if successful
	 */
	boolean canApplyTo(Object o);
	
	/**
	 * Gets the adds the feature.
	 *
	 * @param fp the fp
	 * @return the adds the feature
	 */
	IAddFeature getAddFeature(IFeatureProvider fp);
	
	/**
	 * Gets the update feature.
	 *
	 * @param fp the fp
	 * @return the update feature
	 */
	IUpdateFeature getUpdateFeature(IFeatureProvider fp);
	
	/**
	 * Gets the direct editing feature.
	 *
	 * @param fp the fp
	 * @return the direct editing feature
	 */
	IDirectEditingFeature getDirectEditingFeature(IFeatureProvider fp);
	
	/**
	 * Gets the layout feature.
	 *
	 * @param fp the fp
	 * @return the layout feature
	 */
	ILayoutFeature getLayoutFeature(IFeatureProvider fp);
	
	/**
	 * Gets the removes the feature.
	 *
	 * @param fp the fp
	 * @return the removes the feature
	 */
	IRemoveFeature getRemoveFeature(IFeatureProvider fp);
	
	/**
	 * Gets the delete feature.
	 *
	 * @param fp the fp
	 * @return the delete feature
	 */
	IDeleteFeature getDeleteFeature(IFeatureProvider fp);
	
	/**
	 * Gets the custom features.
	 *
	 * @param fp the fp
	 * @return the custom features
	 */
	ICustomFeature[] getCustomFeatures(IFeatureProvider fp);

}
