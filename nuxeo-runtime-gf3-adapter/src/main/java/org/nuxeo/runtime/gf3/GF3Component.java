/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.runtime.gf3;

import java.io.File;
import java.util.Collections;

import org.glassfish.embed.ScatteredWar;
import org.jboss.util.property.jmx.SystemPropertyClassValue;
import org.nuxeo.common.Environment;
import org.nuxeo.common.server.WebApplication;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.DefaultComponent;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;

// BUG:       in InjectionManager  if (!isOptional(inject)) { return always true - isOptional return always false

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class GF3Component extends DefaultComponent {

    public static final ComponentName NAME = new ComponentName("org.nuxeo.runtime.server");

    public static final String XP_WEB_APP = "webapp";
    public static final String XP_DATA_SOURCE = "datasource";

    protected GlassFishServer server;

    public GlassFishServer getServer() {
        return server;
    }

    @Override
    public void activate(ComponentContext context) throws Exception {
        Environment env = Environment.getDefault();
//this is handled in GlassFishServer.createTempDir()        
//        String gf3Root = env.getHome().getAbsolutePath()+"/glassfish";
//        System.out.println("Using GlassFish home: "+gf3Root); 
//        System.setProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY, gf3Root);
//        System.setProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY, gf3Root);
        File file = new File(env.getConfig(), "domain.xml");
        if (file.exists()) {
            server = new GlassFishServer(file.toURI().toURL());
        } else {
            server = new GlassFishServer(8080);
        }
        file = new File(env.getConfig(), "default-web.xml");
        if (file.exists()) {
            server.setDefaultWebXml(file.toURI().toURL());
        }
    }

    @Override
    public void deactivate(ComponentContext context) throws Exception {
        server.stop();
        server = null;
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (XP_WEB_APP.equals(extensionPoint)) {
            WebApplication app = (WebApplication)contribution;
            File home = Environment.getDefault().getHome();
            File webRoot = new File(home, app.getWebRoot());
            File webXmlFile = null;
            String webXml = app.getConfigurationFile();
            if (webXml == null) {
                webXmlFile = new File(webRoot, "WEB-INF/web.xml");
            } else {
                webXmlFile = new File(home, webXml);
            }
            File webClasses = new File(webRoot, "WEB-INF/classes");
            ScatteredWar war = new ScatteredWar(
                    app.getName(),
                    webRoot,
                    webXmlFile,
                    Collections.singleton(webClasses.toURI().toURL()));
            server.deployWar(war, app.getContextPath());
        } else if (XP_DATA_SOURCE.equals(extensionPoint)) {

        }
    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (XP_WEB_APP.equals(extensionPoint)) {

        } else if (XP_DATA_SOURCE.equals(extensionPoint)) {

        }
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == GlassFishServer.class) {
            return adapter.cast(server);
        }
        return null;
    }

}