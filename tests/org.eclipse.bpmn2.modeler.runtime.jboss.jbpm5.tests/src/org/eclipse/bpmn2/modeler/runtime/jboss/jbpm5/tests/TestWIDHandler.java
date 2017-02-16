/*******************************************************************************
 * Copyright (c) 2011, 2012 Red Hat, Inc.
 *  All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.wid.WIDException;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.wid.WIDParser;
import org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.wid.WorkItemDefinition;
import org.junit.Test;
import org.junit.Assert;


/**
 * Basic tests for the WIDHandler
 * @author bfitzpat
 *
 */
public class TestWIDHandler {

	private String getFile( String filepath ) {
		if (filepath == null) {
			filepath = "widfiles/logemail.wid";
		}
		URL url = this.getClass().getClassLoader().getResource(filepath);
		filepath = url.getPath().toString();
		
		StringBuilder text = new StringBuilder();
	    String NL = System.getProperty("line.separator");
	    Scanner scanner = null;
	    try {
	    	scanner = new Scanner(new FileInputStream(filepath), "UTF-8");
	    	while (scanner.hasNextLine()){
	    		text.append(scanner.nextLine() + NL);
	    	}
	    	return text.toString();
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();
	    } finally {
	    	if (scanner != null)
	    		scanner.close();
	    }	
	    return null;
	}
	
	@Test
	public void testBasic() {
		System.out.println("testBasic: logemail.wid");
		String content = getFile(null);
		HashMap<String, WorkItemDefinition> widMap = new HashMap<String, WorkItemDefinition>();
		try {
			widMap = WIDParser.parse(content);
		} catch (WIDException e) {
			Assert.fail("Failed with exception " + e.getMessage());
		}
		Assert.assertTrue(!widMap.isEmpty());
		java.util.Iterator<WorkItemDefinition> widIterator = widMap.values().iterator();
		while(widIterator.hasNext())
			System.out.println(widIterator.next().toString());
	}
	
	@Test
	public void testComplex() {
		System.out.println("testComplex: widfiles/Email.wid");
		String content = getFile("widfiles/Email.wid");
		HashMap<String, WorkItemDefinition> widMap = new HashMap<String, WorkItemDefinition>();
		try {
			widMap = WIDParser.parse(content);
		} catch (WIDException e) {
			Assert.fail("Failed with exception " + e.getMessage());
		}
		Assert.assertTrue(!widMap.isEmpty());
		java.util.Iterator<WorkItemDefinition> widIterator = widMap.values().iterator();
		while(widIterator.hasNext()) {
			WorkItemDefinition wid = widIterator.next();
			Assert.assertTrue(wid.getEclipseCustomEditor() != null &&
					wid.getEclipseCustomEditor().trim().length() > 0);
			System.out.println(wid.toString());
		};
	}

	@Test
	public void testResults() {
		System.out.println("testResults: widfiles/java.wid");
		String content = getFile("widfiles/java.wid");
		HashMap<String, WorkItemDefinition> widMap = new HashMap<String, WorkItemDefinition>();
		try {
			widMap = WIDParser.parse(content);
		} catch (WIDException e) {
			Assert.fail("Failed with exception " + e.getMessage());
		}
		Assert.assertTrue(!widMap.isEmpty());
		java.util.Iterator<WorkItemDefinition> widIterator = widMap.values().iterator();
		while(widIterator.hasNext()) {
			WorkItemDefinition wid = widIterator.next();
			Assert.assertTrue(!wid.getResults().isEmpty());
			System.out.println(wid.toString());
		}
	}

	@Test
	public void testFail() {
		System.out.println("testFail: no wid");
		HashMap<String, WorkItemDefinition> widMap = new HashMap<String, WorkItemDefinition>();
		try {
			widMap = WIDParser.parse(null);
		} catch (WIDException e) {
			Assert.assertTrue(e != null);
		}
	}
}
