/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.oracle.tools;

import org.jkiss.dbeaver.ext.oracle.model.OracleConstants;
import org.jkiss.dbeaver.ext.oracle.model.OracleSchema;
import org.jkiss.dbeaver.ext.oracle.oci.OCIUtils;
import org.jkiss.dbeaver.ext.oracle.oci.OracleHomeDescriptor;
import org.jkiss.dbeaver.model.DBPConnectionInfo;
import org.jkiss.dbeaver.ui.dialogs.tools.AbstractScriptExecuteWizard;
import org.jkiss.utils.CommonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class OracleScriptExecuteWizard extends AbstractScriptExecuteWizard<OracleSchema> {

    private OracleScriptExecuteWizardPageSettings mainPage;

    public OracleScriptExecuteWizard(OracleSchema oracleSchema)
    {
        super(oracleSchema, "Execute Script");
        this.mainPage = new OracleScriptExecuteWizardPageSettings(this);
    }

    @Override
    public void addPages()
    {
        addPage(mainPage);
        super.addPages();
    }

    @Override
    public void fillProcessParameters(List<String> cmd)
    {
        String dumpPath = new File(getClientHome().getHomePath(), "bin/sqlplus").getAbsolutePath();
        cmd.add(dumpPath);
    }

    @Override
    public OracleHomeDescriptor findServerHome(String clientHomeId)
    {
        return OCIUtils.getOraHome(clientHomeId);
    }

    @Override
    protected List<String> getCommandLine()
    {
        List<String> cmd = new ArrayList<String>();
        fillProcessParameters(cmd);
        DBPConnectionInfo conInfo = getConnectionInfo();
        String url = null;
        if ("TNS".equals(conInfo.getProperties().get(OracleConstants.PROP_CONNECTION_TYPE))) {
            url = conInfo.getServerName();
        }
        else {
            String port = conInfo.getHostPort();
            url = "//" + conInfo.getHostName() + (port != null ? ":" + port : "") + "/" + conInfo.getDatabaseName();
        }
        cmd.add(conInfo.getUserName() + "/" + conInfo.getUserPassword() + "@" + url);
/*

        if (toolWizard.isVerbose()) {
            cmd.add("-v");
        }
        cmd.add("-q");

        cmd.add(toolWizard.getDatabaseObject().getName());
*/
        return cmd;
    }
}
