/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of VDT plug-in.
 * VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VDT plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Additional permission under GNU GPL version 3 section 7:
 * If you modify this Program, or any covered work, by linking or combining it
 * with Eclipse or Eclipse plugins (or a modified version of those libraries),
 * containing parts covered by the terms of EPL/CPL, the licensors of this
 * Program grant you additional permission to convey the resulting work.
 * {Corresponding Source for a non-source form of such a combination shall
 * include the source code for the parts of Eclipse or Eclipse plugins used
 * as well as that of the covered work.}
 *******************************************************************************/
package com.elphel.vdt.core.tools.generators;


import java.io.File;
import java.nio.file.Paths;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import com.elphel.vdt.VDT;
import com.elphel.vdt.ui.variables.SelectedResourceManager;

/**
 * Generate the absolute path of the current project. 
 * 
 * Created: 21.02.2006
 * @author  Lvov Konstantin
 */

public class ProjectPathGenerator extends AbstractGenerator {
    public static final String NAME = VDT.GENERATOR_ID_PROJECT_PATH; 
    public ProjectPathGenerator()
    {
    	super(null); // null for topFormatProcessor - this generator can not reference other parameters
    }
    
    public String getName() {
        return NAME;
    }

    protected String[] getStringValues() {
        String[] value = null;
        IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
        if (resource != null) {
        	/*
            String workspaceRoot=resource.getWorkspace().getRoot().getLocation().toString();
            String project_name = workspaceRoot+resource.getProject().getFullPath().toString()+File.separator;
            String projectRoot=resource.getWorkspace().getRoot().getLocation().toString();
            IPath apath = resource.getProject().getRawLocation().makeAbsolute();
            value = new String[]{project_name};
            */
        	try {
        		value = new String[]{resource.getProject().getRawLocation().makeAbsolute().toString()};
        	} catch (NullPointerException npe){
        		return new String[] {""};
        	}
        } else {
//            fault("There is no selected project");
            System.out.println(getName()+": no project selected");
            return new String[] {""};
        }
        return value;
    }

} // class ProjectPathGenerator
