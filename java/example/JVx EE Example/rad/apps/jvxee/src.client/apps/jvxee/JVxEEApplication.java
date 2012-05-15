/*
 * Copyright 2012 SIB Visions GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 * History
 *
 * 09.05.2012 - [SW] - creation
 */
package apps.jvxee;

import javax.rad.application.genui.UILauncher;
import javax.rad.genui.UIImage;
import javax.rad.genui.component.UIButton;
import javax.rad.genui.container.UIToolBar;
import javax.rad.genui.menu.UIMenu;
import javax.rad.genui.menu.UIMenuItem;
import javax.rad.remote.IConnection;

import apps.jvxee.frames.CustomerEditFrame;

import com.sibvisions.rad.application.Application;
import com.sibvisions.rad.server.DirectServerConnection;

/**
 * First application with JVx EE and JVx.
 * 
 * @author Stefan Wurm
 */
public class JVxEEApplication extends Application 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Creates a new instance of <code>JVxEEApplication</code> with a technology
	 * dependent launcher.
	 * 
	 * @param pLauncher the technology dependent launcher
	 */
	public JVxEEApplication(UILauncher pLauncher)
	{
		super(pLauncher);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Overwritten methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IConnection createConnection() throws Exception
	{
		return new DirectServerConnection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getApplicationName()
	{
		return "jvxee";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void afterLogin()
	{
		super.afterLogin();

		// configure MenuBar

		UIMenu menuMasterData = new UIMenu();
		menuMasterData.setText("Master data");

		UIMenuItem miJPAEdit = createMenuItem("doOpenFrame", null, "Customer Edit", UIImage.getImage(UIImage.SEARCH_LARGE));

		menuMasterData.add(miJPAEdit);

		// insert before Help
		getMenuBar().add(menuMasterData, 1);

		// configure ToolBar

		UIToolBar tbMasterData = new UIToolBar();

		UIButton butJPAEdit = createToolBarButton("doOpenFrame", null, "Customer Edit", UIImage.getImage(UIImage.SEARCH_LARGE));

		tbMasterData.add(butJPAEdit);

		getLauncher().addToolBar(tbMasterData);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Actions
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Opens the edit screen.
	 * 
	 * @throws Throwable if open fails
	 */
	public void doOpenFrame() throws Throwable
	{
		CustomerEditFrame frame = new CustomerEditFrame(this);

		configureFrame(frame);

		frame.setVisible(true);
	}

}	// JVxEEApplication
