/*******************************************************************************
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of Eclipse/VDT plug-in.
 * Eclipse/VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * Eclipse/VDT plug-in is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Eclipse VDT plug-in; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/
package com.elphel.vdt.ui.options.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.types.ParamTypeEnum;

public class ComboComponent extends GeneralComponent {

    private Combo comboField;

    public ComboComponent(Parameter param) {
        super(param);
    }
    
    public void createControl(Composite parent) {
        super.createControl(parent);

        GridData  gridData = new GridData( GridData.HORIZONTAL_ALIGN_FILL 
                                         | GridData.GRAB_HORIZONTAL );
         
        ParamTypeEnum type = (ParamTypeEnum)param.getType();

        comboField = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboField.setLayoutData(gridData);
//        comboField.setItems(type.getLabels());
        comboField.setItems(type.getValues());
        StringBuilder sb= new StringBuilder();
        String [] values=type.getValues();
        String [] labels=type.getLabels();
        
        for (int i=0;i<type.getValues().length;i++){
        	sb.append("\"");
        	sb.append(values[i]);
        	sb.append("\" - ");
        	sb.append(labels[i]);
        	if (i<(values.length-1)){
        		sb.append("\n\n");
        	}
        }
        comboField.setToolTipText(sb.toString());
        
        comboField.setMenu(createPopupMenu(comboField.getShell()));        
        endCreateControl();
    }    
    
    public Combo getComboField() {
        return comboField;
    }
    
    public void setSelection(String value) {
        ParamTypeEnum type = (ParamTypeEnum)param.getType();
        int pos = type.getValueIndex(param.getValue().get(0));

        if(pos < 0)
            pos = 0;

        comboField.select(pos);
    }
    
    protected String getSelection() {
        String item = comboField.getText();
        
        if (item.length() == 0)
            item = param.getDefaultValue().get(0);

        ParamTypeEnum type = (ParamTypeEnum)param.getType();
        int pos = type.getLabelIndex(item);
        String[] values = type.getValues();
        
        return values[pos];
    }
    
    protected boolean isDisposed() {
        return (comboField == null)
            ||  comboField.isDisposed();
    }
    
    public void setEnabled (boolean enabled) {
        super.setEnabled(enabled);
        comboField.setEnabled(enabled);
    }
    
    public void setVisible (boolean visible) {
        super.setVisible(visible);
        comboField.setVisible(visible);
    }
    
    public void setFocus() {
        comboField.setFocus();
    }
    
    protected void addListeners() {
        comboField.addModifyListener(modifyListener);
    }

    protected void removeListeners() {
        comboField.removeModifyListener(modifyListener);
    }
    
    protected void switchState(boolean defaulted) {
        comboField.setBackground(defaulted ? colorBackgroundDefault
                                           : colorBackground );     
    }
    
} // class ComboComponent
