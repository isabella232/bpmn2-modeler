package org.eclipse.bpmn2.modeler.ui.editor;

import java.util.List;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.modeler.core.di.DIUtils;
import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.graphiti.ui.editor.DiagramEditorContextMenuProvider;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;

public class DesignEditor extends BPMN2Editor {
	
	protected ResourceSetListener resourceSetListener = null;
	private BPMNDiagram bpmnDiagramDeleted = null;
	protected boolean debug;
	protected CTabFolder tabFolder;
	private int defaultTabHeight;

	public DesignEditor(BPMN2MultiPageEditor bpmn2MultiPageEditor, BPMN2MultiPageEditor mpe) {
		super(mpe);
	}

	public void deleteBpmnDiagram(BPMNDiagram bpmnDiagram) {
		this.bpmnDiagramDeleted = bpmnDiagram;
	}

	public void dispose() {
		if (bpmnDiagramDeleted == null) {
			getEditingDomain().removeResourceSetListener(resourceSetListener);
			resourceSetListener = null;
			super.dispose();
		} else {
			bpmnDiagramDeleted = null;
		}
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		if (resourceSetListener == null) {
			resourceSetListener = new AddRemoveDiagramListener();
			getEditingDomain().addResourceSetListener(resourceSetListener);
		}
	}
	
	@Override
    protected void setPartName(String partName) {
		IEditorInput input = getEditorInput();
		if (input instanceof DiagramEditorInput) {
			URI uri = ((DiagramEditorInput)input).getUri();
			partName = URI.decode(uri.trimFileExtension().lastSegment());
	
		}
		super.setPartName(partName);
    }
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		super.selectionChanged(part, selection);
		EObject bo = BusinessObjectUtil.getBusinessObjectForSelection(selection);
		if (bo != null) {
			tabFolder.setSelection(0);
	
			tabFolder.setLayoutDeferred(true);
			for (int i=tabFolder.getItemCount()-1; i>0; --i) {
				tabFolder.getItem(i).setControl(null);
				tabFolder.getItem(i).dispose();
			}
	
			if (!(bo instanceof BPMNDiagram)) {
				if (tabFolder.getItemCount()==1) {
					CTabFolder tf = tabFolder;
			
					Composite subPlanePage = new Composite(tf, SWT.NONE);
					subPlanePage.setLayout(new FillLayout());
					Label label = new Label(subPlanePage, SWT.BORDER);
					CTabItem item = new CTabItem(tf, SWT.NONE);
					item.setControl(subPlanePage);
					item.setText(ModelUtil.getDisplayName(bo)+" Plane");
					item.setData(label);
				}
	
				CTabItem item = tabFolder.getItem(1);
				Label label = (Label)item.getData();
				label.setText("Selected object: "
						+ModelUtil.getDisplayName(bo)+"\n"
						+"If the object contained BPMNPlanes, the activities in each Plane would be displayed as a new tab at the bottom of the page.");
			}
			
			tabFolder.setLayoutDeferred(false);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					updateTabs();
				}
			});
		}					
	}
	
	public void createPartControl(Composite parent) {
		if (getGraphicalViewer()==null) {
			tabFolder = new CTabFolder(parent, SWT.BOTTOM);
			tabFolder.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int newPageIndex = tabFolder.indexOf((CTabItem) e.item);
					CTabItem item = tabFolder.getItem(1);
				}
			});
			tabFolder.addTraverseListener(new TraverseListener() { 
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=199499 : Switching tabs by Ctrl+PageUp/PageDown must not be caught on the inner tab set
				public void keyTraversed(TraverseEvent e) {
					switch (e.detail) {
						case SWT.TRAVERSE_PAGE_NEXT:
						case SWT.TRAVERSE_PAGE_PREVIOUS:
							int detail = e.detail;
							e.doit = true;
							e.detail = SWT.TRAVERSE_NONE;
							Control control = tabFolder.getParent();
							control.traverse(detail, new Event());
					}
				}
			});
			defaultTabHeight = 3 * tabFolder.getTabHeight() / 4;

			Composite topPlanePage = new Composite(tabFolder, SWT.NONE);
			topPlanePage.setLayout(new FillLayout());
			CTabItem item = new CTabItem(tabFolder, SWT.NONE, 0);
			item.setText("Diagram Plane");
			item.setControl(topPlanePage);

			super.createPartControl(topPlanePage);
		}
	}
	
	public void updateTabs() {
		if (!tabFolder.isLayoutDeferred()) {
			if (tabFolder.getItemCount()==1) {
				tabFolder.setTabHeight(0);
			}
			else
				tabFolder.setTabHeight(defaultTabHeight);
		}
		tabFolder.layout();
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action = new WorkbenchPartAction(multipageEditor.getDesignEditor()) {

			@Override
			protected void init() {
				super.init();
				setId("show.or.hide.source.view");
			}

			@Override
			public String getText() {
				return multipageEditor.getSourceViewer() == null ? "Show Source View" : "Hide Source View";
			}

			@Override
			protected boolean calculateEnabled() {
				return true;
			}

			public void run() {
				if (multipageEditor.getSourceViewer() == null) {
					multipageEditor.createSourceViewer();
				} else {
					multipageEditor.removeSourceViewer();
				}
			}
		};
		registry.registerAction(action);

		action = new WorkbenchPartAction(multipageEditor.getDesignEditor()) {

			@Override
			protected void init() {
				super.init();
				setId("delete.page");
			}

			@Override
			public String getText() {
				int pageIndex = multipageEditor.getActivePage();
				return "Delete Diagram \"" + multipageEditor.getTabItem(pageIndex).getText() + "\"";
			}

			@Override
			public boolean isEnabled() {
				return calculateEnabled();
			}

			@Override
			protected boolean calculateEnabled() {
				BPMNDiagram bpmnDiagram = getBpmnDiagram();
				BPMNPlane plane = bpmnDiagram.getPlane();
				BaseElement process = plane.getBpmnElement();
				List<Participant> participants = getModelHandler().getAll(Participant.class);
				for (Participant p : participants) {
					if (p.getProcessRef() == process)
						return false;
				}
				return true;
			}

			public void run() {
				int pageIndex = multipageEditor.getActivePage();
				boolean result = MessageDialog.openQuestion(getSite().getShell(),
						"Delete Page",
						"Are you sure you want to delete the page \""
						+ multipageEditor.getTabItem(pageIndex).getText()
						+ "\"?");
				if (result) {
					final BPMNDiagram bpmnDiagram = getBpmnDiagram();
					TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(bpmnDiagram);
					// removeDesignPage(bpmnDiagram);

					if (domain != null) {
						domain.getCommandStack().execute(new RecordingCommand(domain) {
							@Override
							protected void doExecute() {
								BPMNPlane plane = bpmnDiagram.getPlane();
								BaseElement process = plane.getBpmnElement();
								DIUtils.deleteDiagram(DesignEditor.this, bpmnDiagram);
								EcoreUtil.delete(process);
							}
						});
					}
				}
			}
		};
		registry.registerAction(action);
	}

	@Override
	protected ContextMenuProvider createContextMenuProvider() {
		return new DiagramEditorContextMenuProvider(getGraphicalViewer(), getActionRegistry(), getDiagramTypeProvider()) {
			@Override
			public void buildContextMenu(IMenuManager manager) {
				super.buildContextMenu(manager);
				IAction action = getActionRegistry().getAction("show.or.hide.source.view");
				action.setText(action.getText());
				manager.add(action);

				int pageIndex = multipageEditor.getActivePage();
				int lastPage = multipageEditor.getDesignPageCount();
				if (pageIndex > 0 && pageIndex < lastPage) {
					action = getActionRegistry().getAction("delete.page");
					action.setText(action.getText());
					action.setEnabled(action.isEnabled());
					manager.add(action);
				}
			}
		};
	}

	public class AddRemoveDiagramListener implements ResourceSetListener {
		@Override
		public NotificationFilter getFilter() {
			return null;
		}

		@Override
		public Command transactionAboutToCommit(ResourceSetChangeEvent event) throws RollbackException {
			return null;
		}

		@Override
		public void resourceSetChanged(ResourceSetChangeEvent event) {
			for (Notification n : event.getNotifications()) {
				int et = n.getEventType();
				Object notifier = n.getNotifier();
				Object newValue = n.getNewValue();
				Object oldValue = n.getOldValue();
				Object feature = n.getFeature();

				if (debug) {
					if (et == Notification.ADD || et == Notification.REMOVE) {
						System.out.print("event: " + et + "\t");
						if (notifier instanceof EObject) {
							System.out.print("notifier: $" + ((EObject) notifier).eClass().getName());
						} else
							System.out.print("notifier: " + notifier);
					}
				}

				if (et == Notification.ADD) {
					if (debug) {
						if (newValue instanceof EObject) {
							System.out.println("\t\tvalue:    " + ((EObject) newValue).eClass().getName());
						} else
							System.out.println("\t\tvalue:    " + newValue);
					}

					if (notifier instanceof Definitions
							&& newValue instanceof BPMNDiagram
							&& feature == Bpmn2Package.eINSTANCE.getDefinitions_Diagrams()) {
						final BPMNDiagram bpmnDiagram = (BPMNDiagram) newValue;
						multipageEditor.addDesignPage(bpmnDiagram);
						break;
					}
				} else if (et == Notification.REMOVE) {
					if (debug) {
						if (oldValue instanceof EObject) {
							System.out.println("\t\tvalue:    " + ((EObject) oldValue).eClass().getName());
						} else
							System.out.println("\t\tvalue:    " + oldValue);
					}

					if (notifier instanceof Definitions
							&& oldValue instanceof BPMNDiagram
							&& feature == Bpmn2Package.eINSTANCE.getDefinitions_Diagrams()) {
						final BPMNDiagram bpmnDiagram = (BPMNDiagram) oldValue;
						multipageEditor.removeDesignPage(bpmnDiagram);
						break;
					}
				}
			}
		}

		@Override
		public boolean isAggregatePrecommitListener() {
			return false;
		}

		@Override
		public boolean isPrecommitOnly() {
			return false;
		}

		@Override
		public boolean isPostcommitOnly() {
			return true;
		}
	}

}
