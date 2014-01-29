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
package com.elphel.vdt.core.launching;


import java.util.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;

import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.dialogs.PackageLocationDialog;
import com.elphel.vdt.ui.dialogs.ToolLocationDialog;
import com.elphel.vdt.ui.preferences.PreferencePage;

/**
 * Support for launching verilog development tools programmatically.
 * 
 * Created: 22.12.2005
 * @author  Lvov Konstantin
 */

public class LaunchCore {

    /**
     * Construct attribute name for value of tool parameter. This attribute
     * is used to show value in tab group.
     * 
     * @return an attribute name for value of tool parameter
     */  
    public static String getValueAttributeName(Parameter toolParameter) {
        return "ATTR_VALUE_" + toolParameter.getID();     
    }
        

    public static void setResource( ILaunchConfigurationWorkingCopy workingCopy
                                  , String resource ) 
    {
        workingCopy.setAttribute(VDT.ATTR_RESOURCE_TO_LAUNCH, resource);
    } // setResource()

    public static void setWorkingDirectory( ILaunchConfigurationWorkingCopy workingCopy
                                          , IProject project ) 
    {
        workingCopy.setAttribute(VDT.ATTR_WORKING_DIRECTORY, project.getLocation().toOSString());
    } // setWorkingDirectory()
    /* TODO: For now they are the same, implement build directory differnt from project path */
    /* Currently project path is only used to find launched project by it */
    public static void setProjectPath ( ILaunchConfigurationWorkingCopy workingCopy
    		, IProject project ) 
    {
    	workingCopy.setAttribute(VDT.ATTR_PROJECT_PATH, project.getLocation().toOSString());
    } // setProjectPath()
    
    public static void setToolToLaunch( ILaunchConfigurationWorkingCopy workingCopy
                                      , Tool tool ) throws CoreException
    {
        workingCopy.setAttribute(VDT.ATTR_TOOL_ID, tool.getName());
        String launchName = getToolLaunchName(tool);
        workingCopy.setAttribute(VDT.ATTR_TOOL_TO_LAUNCH, launchName);
 /* TODO: Will it be used  ? probably not as the whole Tool object is cloned by the named */

        workingCopy.setAttribute(VDT.ATTR_TOOL_IS_SHELL, tool.getIsShell());
        workingCopy.setAttribute(VDT.ATTR_TOOL_ERRORS,   tool.getPatternErrors());
        workingCopy.setAttribute(VDT.ATTR_TOOL_WARNINGS, tool.getPatternWarnings());
        workingCopy.setAttribute(VDT.ATTR_TOOL_INFO,     tool.getPatternInfo());
        
        
    }

    
    public static void updateLaunchConfiguration( ILaunchConfigurationWorkingCopy workingCopy
                                                , Tool tool ) throws CoreException
    {
        for (Iterator i = tool.getParams().iterator(); i.hasNext(); ) {
            Parameter param = (Parameter)i.next();
            String valueAttrName = LaunchCore.getValueAttributeName(param);

            if(param.getType().isList())
                workingCopy.setAttribute(valueAttrName, param.getValue());
            else
                workingCopy.setAttribute(valueAttrName, param.getValue().get(0));
        }

        setToolToLaunch(workingCopy, tool);
        
        String launchType;
        try {
            launchType = workingCopy.getType().getIdentifier();
        } catch (Exception e) {
            launchType = VDT.ID_DEFAULT_LAUNCH_TYPE;
        }
        
        if (VDT.ID_DEFAULT_LAUNCH_TYPE.equals(launchType))
            workingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
        workingCopy.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
    } // updateLaunchConfiguration()
    
    public static void updateContextOptions(Tool tool, IProject project) throws ToolException, CoreException {
        PackageContext packageContext = tool.getParentPackage();
        if (packageContext != null) {
            OptionsCore.doLoadContextOptions(packageContext);
            String location = OptionsCore.getPackageLocation(packageContext);
            if (location == null) {
                PackageLocationDialog dialog = new PackageLocationDialog(VerilogPlugin.getActiveWorkbenchShell(), SWT.OPEN, packageContext);
                location = dialog.open();
                if (location == null) {
                    throw new DebugException(new Status(IStatus.CANCEL, VDT.ID_VDT, IStatus.CANCEL, "Launching was cancelled", null)); 
                }
                OptionsCore.doStorePackageLocation(packageContext, location);
            }
            packageContext.setWorkingDirectory(project.getLocation().toOSString()); /* TODO - Modify for actual directory */
            packageContext.buildParams();    
        }
        Context context = tool.getParentProject(); // for iverilog - "project_settings"
        if (context != null) {
            OptionsCore.doLoadContextOptions(context, project); // stored values
            context.setWorkingDirectory(project.getLocation().toOSString());
            context.buildParams(); // correct context, but nothing got - Should it be? What the sense of the output - some control files?
        }
        OptionsCore.doLoadContextOptions(tool, project);
        OptionsCore.doLoadLocation(tool); // here it resolves condition with OS
    } // updateContextOptions()
    
    public static ILaunchConfiguration createLaunchConfiguration( Tool tool
                                                                , IProject project
                                                                , String resource 
                                                                ) throws CoreException
    {
        // get tools launch configuration
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
//        String configName = tool.getLabel();
        String configName = tool.getEscapedLabel();
        String launchType = null;//tool.getLaunchType();
        if (launchType == null)
                launchType = VDT.ID_DEFAULT_LAUNCH_TYPE;
        ILaunchConfigurationType type = manager.getLaunchConfigurationType(launchType);

        // delete any existing launch configuration with the same name 
        ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);
        for (int i = 0; i < configurations.length; i++) {
            ILaunchConfiguration configuration = configurations[i];
            if (configName.equals(configuration.getName())) {
                configuration.delete();         
                break; 
            }
        }        
        
        // set tools launch configuration
        try {
            updateContextOptions(tool, project);
        } catch (ToolException e) {
        	        	
//            MessageUI.error( "Cannot save configuration of \""+tool.getLabel()+"\"\n"
            MessageUI.error( "Cannot save configuration of \""+tool.getEscapedLabel()+"\"\n"
                           + e.getMessage(), e );
        }
        ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, configName);
        updateLaunchConfiguration(workingCopy, tool);
        setResource(workingCopy, resource);
        setWorkingDirectory(workingCopy, project);
        setProjectPath(workingCopy, project);
        ILaunchConfiguration launchConfig = workingCopy.doSave();
        return launchConfig;
    } // createLaunchConfiguration()
    
    
    public static void launch(Tool tool, IProject project) throws CoreException {
        launch(tool, project, VDT.VARIABLE_RESOURCE_NAME);
    }

    public static void launch(Tool tool, IProject project, String resource) throws CoreException {
        try {
            ILaunchConfiguration launchConfig = createLaunchConfiguration(tool, project, resource);
            DebugUITools.launch(launchConfig, ILaunchManager.RUN_MODE);
        } catch (CoreException e) {
            IStatus status = e.getStatus();
            if (status.getSeverity() != IStatus.CANCEL)
                throw e; 
        }
    } // launch()

   
    
    private static String getToolLaunchName(Tool tool) throws CoreException {
        String location = tool.getExeName();
        if (isValidLaunchName(location))
            return location;

        location = PreferencePage.getLocation(tool);

        tool.setLocation(location); 
        location = tool.getExeName();
        if (isValidLaunchName(location))
            return location;

        ToolLocationDialog dialog = new ToolLocationDialog(VerilogPlugin.getActiveWorkbenchShell(), SWT.OPEN, tool);
        location = dialog.open();
        if (location == null) {
            throw new DebugException(new Status(IStatus.CANCEL, VDT.ID_VDT, IStatus.CANCEL, "Launching was cancelled", null)); 
        }
        PreferencePage.setLocation(tool, location);
        return tool.getExeName();
    } // getToolLaunchName()

    private static boolean isValidLaunchName(final String location) {
        if ((location == null) || (location.length() == 0))
            return false;
        File file = new File(location);
        return file.isFile() && file.exists();
    }
    
    
    
    private static final String JOB_NAME = Txt.s("Launch.JobName");
    
    /**
     * Launches the given run configuration in a background Job with progress 
     * reported via the Job. Exceptions are reported in the Progress view.
     */
    public static void launchInBackground(final VDTRunnerConfiguration configuration) {
        if (!saveAllEditors(true)) {
            return;
        }
    System.out.println("launchInBackground, JOB_NAME="+JOB_NAME);    
        Job job = new Job(JOB_NAME) {
            public IStatus run(final IProgressMonitor monitor) {
                try {
                    if (!monitor.isCanceled()) {
                        launch(configuration, monitor);
                    }
                } catch (CoreException e) {
                    final IStatus status= e.getStatus();
                    return status;
                }       
                return Status.OK_STATUS;
            }
        };      

        job.setPriority(Job.INTERACTIVE);
        job.schedule();
    } // launchInBackground()
        
    /**
     * Launches the given run configuration in the foreground with a progress 
     * dialog. Reports any exceptions that occur in an error dialog.
     */
    public static void launchInForeground(final VDTRunnerConfiguration configuration) {
        if (!saveAllEditors(true)) {
            return;
        }
        System.out.println("launchInForeground, JOB_NAME="+JOB_NAME);    

        IRunnableWithProgress runnable = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    launch(configuration, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }
            }               
        };
        
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
        } catch (InvocationTargetException e) {
            handleInvocationTargetException(e);
        } catch (InterruptedException e) {
        }                                                       
        
    } // launchInForeground()
        
//      public static void launch( final VDTRunnerConfiguration configuration
//                             ) throws CoreException 
//      {
//              launch(configuration, null);
//      }
        
    private static void launch( final VDTRunnerConfiguration configuration
                              , IProgressMonitor monitor 
                              ) throws CoreException 
    {
        ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
        launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, null);
        DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);                                       
        VDTRunner runner = VDTLaunchUtil.getRunner();
		int numItem=configuration.getBuildStep();
        runner.run(configuration,
        		VDTRunner.renderProcessLabel(configuration.getToolName()), // toolname + (date)
        		launch,
        		null,
        		numItem);
    } // launch()
        
        
    /**
     * Save all dirty editors in the workbench.
     *  Returns whether the operation succeeded.
     * 
     * @return whether all saving was completed
     */
    protected static boolean saveAllEditors(boolean confirm) {
        if (VerilogPlugin.getActiveWorkbenchWindow() == null) {
            return false;
        }
        return PlatformUI.getWorkbench().saveAllEditors(confirm);
    }       
    
    private static void handleInvocationTargetException( InvocationTargetException e) {
        Throwable targetException = e.getTargetException();
        Throwable t = e;
        if (targetException instanceof CoreException) {
            t = targetException;
        }
        if (t instanceof CoreException) {
            CoreException ce = (CoreException)t;
            if ((ce.getStatus().getSeverity() & (IStatus.ERROR | IStatus.WARNING)) == 0) {
                // If the exception is a CoreException with a status other
                // than ERROR or WARNING, don't open an error dialog.
                return;
            }
        }
        MessageUI.error(t);
    } // handleInvocationTargetException()

} // class LaunchCore
