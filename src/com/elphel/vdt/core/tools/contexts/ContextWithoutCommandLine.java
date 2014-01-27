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
package com.elphel.vdt.core.tools.contexts;

import java.util.List;
import java.util.Iterator;

import com.elphel.vdt.core.tools.params.CommandLinesBlock;
import com.elphel.vdt.core.tools.params.ParamGroup;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.config.ConfigException;

public abstract class ContextWithoutCommandLine extends Context {
    protected ContextWithoutCommandLine(String name,
                                        String controlInterfaceName,
                                        String label,
                                        String iconName,
                                        String inputDialogLabel,
                                        List<Parameter> params,
                                        List<ParamGroup> paramGroups,
                                        List<CommandLinesBlock> commandLinesBlocks)
        throws ConfigException
    {
        super(name, 
              controlInterfaceName, 
              label,
              iconName,
              inputDialogLabel,
              params, 
              paramGroups, 
              commandLinesBlocks);
        
        checkCmdLine();
    }

    public void setCommandLinesBlocks(List<CommandLinesBlock> commandLinesBlocks) throws ConfigException {
        super.setCommandLinesBlocks(commandLinesBlocks);

        checkCmdLine();
    }

    private void checkCmdLine() throws ConfigException {
        if(commandLinesBlocks != null) {
            for(Iterator<CommandLinesBlock> i = commandLinesBlocks.iterator(); i.hasNext();) {
                CommandLinesBlock block = (CommandLinesBlock)i.next();
                String destination = block.getDestination();
                
                if(destination == null || destination.equals(""))
                    throw new ConfigException("Context '" + name + 
                                              "' cannot contain command line, but destination of its '" +
                                              block.getName() + 
                                              "' command block is not specified");
                if(!block.isFileKind())
                    throw new ConfigException("Context '" + name + 
                                              "' cannot contain commands for console "+
                    		                  destination);
            }
        }
    }
}
