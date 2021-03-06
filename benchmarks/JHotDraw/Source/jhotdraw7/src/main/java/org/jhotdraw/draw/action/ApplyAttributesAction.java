/*
 * @(#)ApplyAttributesAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.undo.*;
import java.util.*;
import org.jhotdraw.draw.*;
import static org.jhotdraw.draw.AttributeKeys.*;

/**
 * ApplyAttributesAction.
 *
 * @author Werner Randelshofer
 * @version $Id: ApplyAttributesAction.java 658 2010-06-26 11:31:53Z rawcoder $
 */
public class ApplyAttributesAction extends AbstractSelectedAction {

    private Set<AttributeKey> excludedAttributes = new HashSet<AttributeKey>(
            Arrays.asList(new AttributeKey[]{TRANSFORM, TEXT}));

    /** Creates a new instance. */
    public ApplyAttributesAction(DrawingEditor editor) {
        super(editor);
        labels.configureAction(this, "edit.applyAttributes");
        setEnabled(true);
    }

    /**
     * Set of attributes that is excluded when applying default attributes.
     */
    public void setExcludedAttributes(Set<AttributeKey> a) {
        this.excludedAttributes = a;
    }

    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        applyAttributes();
    }

    @SuppressWarnings("unchecked")
    public void applyAttributes() {
        DrawingEditor editor = getEditor();

        CompositeEdit edit = new CompositeEdit(labels.getString("edit.applyAttributes.text"));
        DrawingView view = getView();
        view.getDrawing().fireUndoableEditHappened(edit);

        for (Figure figure : view.getSelectedFigures()) {
            figure.willChange();
            for (Map.Entry<AttributeKey, Object> entry : editor.getDefaultAttributes().entrySet()) {
                if (!excludedAttributes.contains(entry.getKey())) {
                    figure.set(entry.getKey(), entry.getValue());
                }
            }
            figure.changed();
        }
        view.getDrawing().fireUndoableEditHappened(edit);
    }

    public void selectionChanged(FigureSelectionEvent evt) {
        setEnabled(getView().getSelectionCount() == 1);
    }
}
