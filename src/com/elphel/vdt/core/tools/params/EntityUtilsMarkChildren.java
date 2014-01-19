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
package com.elphel.vdt.core.tools.params;

import java.util.*;

import com.elphel.vdt.core.tools.Updateable;
import com.elphel.vdt.core.tools.config.ConfigException;

public class EntityUtilsMarkChildren {
// Updating tool context (to) from project context (from), items in to have now null context, in from - ProjectContext
/*
 * Andrey: Seems there should be only one instance of each parameter, no cloning?
 */
		
    public static <T extends Updateable> void update(List<Parameter> from, List<Parameter> to)
        throws ConfigException 
    {
        List<Parameter> updated = new ArrayList<Parameter>();
        
        // scan the base list; for each item, check if it is in the 
        // destination list. if so, update it; otherwise, just clone it
        for(Parameter baseItem : from) {
        	Parameter itemToUpdate = null;

            for(Parameter toItem : to) {                
                if(baseItem.matches(toItem)) {
                    itemToUpdate = toItem;          
                    break;
                }    
            }
                        
            if(itemToUpdate != null) {
                // this item is inherited but changed
                // so just update its internals in the way it needs
                itemToUpdate.update(baseItem);  /* Statically updated */                  

                updated.add(itemToUpdate);
            } else {
                // the item is purely inherited, so just copy it
            	Parameter child=(Parameter) baseItem.clone();
            	child.setIsChild(true);
                updated.add(child);
            	// No, reference the same instance
            }
        }

        // now what we should do is to add new items that are in the 
        // destination list and are't in the base list to the updated list
        for(Parameter toItem : to) {
            boolean found = false;

            for(Parameter updatedItem : updated) {
                if(updatedItem.matches(toItem)) {
                    found = true;
                    break;
                }
            }
         
            if(!found)
                updated.add(toItem);
        }
        
        to.clear();
        to.addAll(updated);
    }
} // EntityUtilsMarkChildren