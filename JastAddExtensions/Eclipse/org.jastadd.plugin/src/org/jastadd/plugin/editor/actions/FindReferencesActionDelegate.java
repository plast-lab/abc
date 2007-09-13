package org.jastadd.plugin.editor.actions;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jastadd.plugin.JastAddModel;
import org.jastadd.plugin.search.JastAddSearchQuery;

import AST.ASTNode;
import AST.TypeDecl;

public class FindReferencesActionDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {

	private IEditorPart editorPart;
	private TextSelection selection;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPart = targetEditor;
	}

	public void run(IAction action) {
		if (editorPart != null && editorPart.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput)editorPart.getEditorInput();
			IFile file = fileEditorInput.getFile();
			if(selection != null && file != null) {
				ASTNode selectedNode = JastAddModel.getInstance().findNodeInDocument(file, selection.getOffset());
				
				Collection references = selectedNode.findReferences();
				StringBuffer s = new StringBuffer();
				s.append("Find references of ");
				if(selectedNode instanceof TypeDecl)
					s.append(((TypeDecl)selectedNode).typeName());
				JastAddSearchQuery query = new JastAddSearchQuery(references, s.toString());
				NewSearchUI.runQueryInForeground(null, (ISearchQuery)query);				
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof TextSelection) {
			this.selection = (TextSelection) selection;
		}
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow window) {
		editorPart = window.getActivePage().getActiveEditor();
	}
}
