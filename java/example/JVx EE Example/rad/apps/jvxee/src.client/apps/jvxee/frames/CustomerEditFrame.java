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
package apps.jvxee.frames;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.rad.genui.UIDimension;
import javax.rad.genui.UIImage;
import javax.rad.genui.UIInsets;
import javax.rad.genui.celleditor.UIChoiceCellEditor;
import javax.rad.genui.celleditor.UIDateCellEditor;
import javax.rad.genui.celleditor.UIImageViewer;
import javax.rad.genui.component.UIButton;
import javax.rad.genui.component.UILabel;
import javax.rad.genui.container.UIGroupPanel;
import javax.rad.genui.container.UIInternalFrame;
import javax.rad.genui.container.UIPanel;
import javax.rad.genui.container.UISplitPanel;
import javax.rad.genui.control.UIEditor;
import javax.rad.genui.control.UITable;
import javax.rad.genui.layout.UIBorderLayout;
import javax.rad.genui.layout.UIFormLayout;
import javax.rad.io.IFileHandle;
import javax.rad.model.ColumnDefinition;
import javax.rad.model.ColumnView;
import javax.rad.model.ModelException;
import javax.rad.model.RowDefinition;
import javax.rad.model.condition.ICondition;
import javax.rad.model.condition.LikeIgnoreCase;
import javax.rad.model.datatype.StringDataType;
import javax.rad.model.reference.ReferenceDefinition;
import javax.rad.remote.AbstractConnection;
import javax.rad.remote.MasterConnection;

import com.sibvisions.rad.application.Application;
import com.sibvisions.rad.model.mem.DataRow;
import com.sibvisions.rad.model.remote.RemoteDataBook;
import com.sibvisions.rad.model.remote.RemoteDataSource;
import com.sibvisions.util.type.FileUtil;
import com.sibvisions.util.type.ImageUtil;

/**
 * The frame to edit the customers.
 * 
 * @author Stefan Wurm
 */
public class CustomerEditFrame extends UIInternalFrame 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** the default image. */
	private static final String	NO_IMAGE				= "/apps/jvxee/images/nobody.gif";
	

	/** the application. */
	private Application			application;

	/** the communication connection to the server. */
	private AbstractConnection	connection;

	/** the DataSource for fetching table data. */
	private RemoteDataSource	dataSource				= new RemoteDataSource();

	/** the customer tabl. */
	private RemoteDataBook		rdbCustomer				= new RemoteDataBook();

	/** storage for addresses. */
	private RemoteDataBook		rdbAddress				= new RemoteDataBook();

	/** storage for CustomerEducation. */
	private RemoteDataBook		rdbCustomerEducation	= new RemoteDataBook();

	/** search row. */
	private DataRow				drSearch				= null;

	/** Label. */
	private UILabel				lblSalutation			= new UILabel();

	/** Label. */
	private UILabel				lblHealthInsurance		= new UILabel();

	/** Label. */
	private UILabel				lblFirstName			= new UILabel();

	/** Label. */
	private UILabel				lblLastName				= new UILabel();

	/** Label. */
	private UILabel				lblBirthday				= new UILabel();

	/** Label. */
	private UILabel				lblFilename				= new UILabel();

	/** Label. */
	private UILabel				lblTelephonePrivate		= new UILabel();

	/** Label. */
	private UILabel				lblTelephoneOffice		= new UILabel();

	/** Label. */
	private UILabel				lblEmail				= new UILabel();

	/** Label. */
	private UILabel				lblPrivateCustomer		= new UILabel();

	/** label for Search. */
	private UILabel				lblSearch				= new UILabel();

	/** Editor. */
	private UIEditor			edtSalutation			= new UIEditor();

	/** Editor. */
	private UIEditor			edtHealthInsurance		= new UIEditor();

	/** Editor. */
	private UIEditor			edtFirstName			= new UIEditor();

	/** Editor. */
	private UIEditor			edtLastName				= new UIEditor();

	/** Editor. */
	private UIEditor			edtBirthday				= new UIEditor();

	/** Editor. */
	private UIEditor			edtFilename				= new UIEditor();

	/** Editor. */
	private UIEditor			edtTelephonePrivate		= new UIEditor();

	/** Editor. */
	private UIEditor			edtTelephoneOffice		= new UIEditor();

	/** Editor. */
	private UIEditor			edtEmail				= new UIEditor();

	/** Editor. */
	private UIEditor			edtPrivateCustomer		= new UIEditor();

	/** editSuchen. */
	private UIEditor			edtSearch				= new UIEditor();

	/** contact image. */
	private UIEditor			icoImage				= new UIEditor();

	/** load image button. */
	private UIButton			butLoadImage			= new UIButton();		
	  
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Creates a new instance of CustomerEditFrame for a specific application.
	 * 
	 * @param pApp the application
	 * @throws Throwable if the remote access fails
	 */
	public CustomerEditFrame(Application pApp) throws Throwable
	{
		super(pApp.getDesktopPane());

		application = pApp;

		initializeModel();
		initializeUI();
	}

	/**
	 * Initializes the model.
	 * 
	 * @throws Throwable if the initialization throws an error
	 */
	private void initializeModel() throws Throwable
	{
		// we use a new "session" for the screen
		connection = ((MasterConnection)application.getConnection()).createSubConnection("apps.jvxee.frames.CustomerEdit");
		connection.open();

		// data connection
		dataSource.setConnection(connection);
		dataSource.open();

		rdbCustomer.setDataSource(dataSource);
		rdbCustomer.setName("customer");
		rdbCustomer.open();
		
		rdbCustomer.getRowDefinition().setColumnView(null, new ColumnView("FIRSTNAME", "LASTNAME"));

		rdbAddress.setDataSource(dataSource);
		rdbAddress.setName("address");
		ReferenceDefinition rdAddress = new ReferenceDefinition();
		rdAddress.setColumnNames(new String[] { "CUSTOMER_ID" });
		rdAddress.setReferencedDataBook(rdbCustomer);
		rdAddress.setReferencedColumnNames(new String[] { "ID" });
		rdbAddress.setMasterReference(rdAddress);
		rdbAddress.open();

		rdbCustomerEducation.setDataSource(dataSource);
		rdbCustomerEducation.setName("customerEducation");
		ReferenceDefinition rdCustomerEducation = new ReferenceDefinition();
		rdCustomerEducation.setColumnNames(new String[] { "CUSTOMER_ID" });
		rdCustomerEducation.setReferencedDataBook(rdbCustomer);
		rdCustomerEducation.setReferencedColumnNames(new String[] { "ID" });
		rdbCustomerEducation.setMasterReference(rdCustomerEducation);
		rdbCustomerEducation.open();

		UIImageViewer imageViewer = new UIImageViewer();
		imageViewer.setDefaultImageName(NO_IMAGE);

		rdbCustomer.getRowDefinition().getColumnDefinition("FILENAME").setReadOnly(true);
		rdbCustomer.getRowDefinition().getColumnDefinition("IMAGE").getDataType().setCellEditor(imageViewer);

		UIChoiceCellEditor editor = new UIChoiceCellEditor(new Object[] { Boolean.TRUE, Boolean.FALSE }, new String[] { UIImage.CHECK_YES_SMALL, UIImage.CHECK_SMALL },
				UIImage.CHECK_SMALL);

		rdbCustomer.getRowDefinition().getColumnDefinition("PRIVATECUSTOMER").getDataType().setCellEditor(editor);

		rdbCustomer.getRowDefinition().getColumnDefinition("BIRTHDAY").getDataType().setCellEditor(new UIDateCellEditor("dd.MM.yyyy"));

		RowDefinition definition = new RowDefinition();
		definition.addColumnDefinition(new ColumnDefinition("SEARCH", new StringDataType()));

		drSearch = new DataRow(definition);
		drSearch.eventValuesChanged().addListener(this, "doFilter");
	}

	/**
	 * Initializes the UI.
	 * 
	 * @throws Exception if the initialization throws an error
	 */
	private void initializeUI() throws Exception
	{

		UISplitPanel splitMain = new UISplitPanel();

		UIPanel panLeftSpan = new UIPanel();

		UIPanel panRightSpan = new UIPanel();

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Initialize Tables, Labels, Editors
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		UITable tabAddress = new UITable();
		tabAddress.setDataBook(rdbAddress);
		tabAddress.setPreferredSize(new UIDimension(150, 150));

		UITable tabEducation = new UITable();
		tabEducation.setDataBook(rdbCustomerEducation);
		tabEducation.setPreferredSize(new UIDimension(150, 150));

		UITable tabCustomer = new UITable();
		tabCustomer.setDataBook(rdbCustomer);

		lblSearch.setText("Search");
		edtSearch.setDataRow(drSearch);
		edtSearch.setColumnName("SEARCH");

		icoImage.setPreferredSize(new UIDimension(75, 75));
		icoImage.setDataRow(rdbCustomer);
		icoImage.setColumnName("IMAGE");

		lblSalutation.setText("Salutation");
		lblFirstName.setText("First name");
		lblLastName.setText("Last name");
		lblBirthday.setText("Birthday");
		lblHealthInsurance.setText("Health insurance");
		lblFilename.setText("Filename");
		lblTelephonePrivate.setText("Telephone private");
		lblTelephoneOffice.setText("Telephone office");
		lblEmail.setText("E-Mail");
		lblPrivateCustomer.setText("Is private customer");

		edtSalutation.setDataRow(rdbCustomer);
		edtSalutation.setColumnName("SALUTATION_SALUTATION");
		edtSalutation.setPreferredSize(new UIDimension(75, 21));
		edtFirstName.setDataRow(rdbCustomer);
		edtFirstName.setColumnName("FIRSTNAME");
		edtLastName.setDataRow(rdbCustomer);
		edtLastName.setColumnName("LASTNAME");
		edtBirthday.setDataRow(rdbCustomer);
		edtBirthday.setColumnName("BIRTHDAY");
		edtHealthInsurance.setDataRow(rdbCustomer);
		edtHealthInsurance.setColumnName("HEALTHINSURANCE_HEALTHINSURANCE");
		edtFilename.setDataRow(rdbCustomer);
		edtFilename.setColumnName("FILENAME");
		edtTelephonePrivate.setDataRow(rdbCustomer);
		edtTelephonePrivate.setColumnName("TELEPHONEPRIVATE");
		edtTelephoneOffice.setDataRow(rdbCustomer);
		edtTelephoneOffice.setColumnName("TELEPHONEOFFICE");
		edtEmail.setDataRow(rdbCustomer);
		edtEmail.setColumnName("EMAIL");
		edtPrivateCustomer.setDataRow(rdbCustomer);
		edtPrivateCustomer.setColumnName("PRIVATECUSTOMER");

		butLoadImage.setText("Upload");
		butLoadImage.eventAction().addListener(this, "doUpload");
		butLoadImage.setFocusable(false);

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Initialize Layouts and Panels
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		UIFormLayout layoutSearch = new UIFormLayout();

		UIPanel panSearch = new UIPanel();

		panSearch.setLayout(layoutSearch);
		panSearch.add(lblSearch, layoutSearch.getConstraints(0, 0));
		panSearch.add(edtSearch, layoutSearch.getConstraints(1, 0, -1, 0));

		UIFormLayout layoutAddress = new UIFormLayout();

		UIGroupPanel gpanAddress = new UIGroupPanel();

		gpanAddress.setText("Addresses");
		gpanAddress.setLayout(layoutAddress);
		gpanAddress.add(tabAddress, layoutAddress.getConstraints(0, 0, -1, -1));

		panLeftSpan.setLayout(new UIBorderLayout());
		panLeftSpan.add(panSearch, UIBorderLayout.NORTH);
		panLeftSpan.add(tabCustomer, UIBorderLayout.CENTER);
		panLeftSpan.add(gpanAddress, UIBorderLayout.SOUTH);

		UIFormLayout flDetails = new UIFormLayout();

		flDetails.setMargins(new UIInsets(20, 20, 20, 20));
		flDetails.setHorizontalGap(5);

		UIGroupPanel gpanDetails = new UIGroupPanel();

		gpanDetails.setText("Contact");

		gpanDetails.setLayout(flDetails);
		gpanDetails.add(icoImage, flDetails.getConstraints(0, 0, 1, 7));
		gpanDetails.add(butLoadImage, flDetails.getConstraints(0, 8));
		gpanDetails.add(edtFilename, flDetails.getConstraints(1, 8));

		gpanDetails.add(lblSalutation, flDetails.getConstraints(2, 0));
		gpanDetails.add(edtSalutation, flDetails.getConstraints(3, 0));
		gpanDetails.add(lblFirstName, flDetails.getConstraints(2, 2));
		gpanDetails.add(edtFirstName, flDetails.getConstraints(3, 2, -1, 2));
		gpanDetails.add(lblLastName, flDetails.getConstraints(2, 3));
		gpanDetails.add(edtLastName, flDetails.getConstraints(3, 3, -1, 3));
		gpanDetails.add(lblBirthday, flDetails.getConstraints(2, 4));
		gpanDetails.add(edtBirthday, flDetails.getConstraints(3, 4));
		gpanDetails.add(lblHealthInsurance, flDetails.getConstraints(4, 4));
		gpanDetails.add(edtHealthInsurance, flDetails.getConstraints(5, 4, -1, 4));
		gpanDetails.add(lblTelephonePrivate, flDetails.getConstraints(2, 5));
		gpanDetails.add(edtTelephonePrivate, flDetails.getConstraints(3, 5, -1, 5));
		gpanDetails.add(lblTelephoneOffice, flDetails.getConstraints(2, 6));
		gpanDetails.add(edtTelephoneOffice, flDetails.getConstraints(3, 6, -1, 6));
		gpanDetails.add(lblEmail, flDetails.getConstraints(2, 7));
		gpanDetails.add(edtEmail, flDetails.getConstraints(3, 7, -1, 7));
		gpanDetails.add(lblPrivateCustomer, flDetails.getConstraints(2, 8));
		gpanDetails.add(edtPrivateCustomer, flDetails.getConstraints(3, 8, -1, 8));

		UIFormLayout layoutEducation = new UIFormLayout();

		UIGroupPanel gpanEducations = new UIGroupPanel();

		gpanEducations.setText("Schooling");
		gpanEducations.setLayout(layoutEducation);
		gpanEducations.add(tabEducation, layoutEducation.getConstraints(0, 0, -1, -1));

		UIFormLayout layout = new UIFormLayout();

		panRightSpan.setLayout(layout);
		panRightSpan.add(gpanDetails, layout.getConstraints(0, 0, -1, 0));
		panRightSpan.add(gpanEducations, layout.getConstraints(0, 1, -1, -1));

		splitMain.setDividerPosition(250);
		splitMain.setDividerAlignment(UISplitPanel.DIVIDER_TOP_LEFT);
		splitMain.setFirstComponent(panLeftSpan);
		splitMain.setSecondComponent(panRightSpan);

		setTitle("Customers");
		setLayout(new UIBorderLayout());
		add(splitMain, UIBorderLayout.CENTER);

		pack();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Overwritten methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Closes the communication connection and disposes the frame.
	 */
	@Override
	public void dispose()
	{
		try
		{
			connection.close();
		}
		catch (Throwable th)
		{
			// nothing to be done
		}
		finally
		{
			super.dispose();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Saves the image to the contact.
	 * 
	 * @param pFileHandle the file.
	 * @throws Throwable if an error occures.
	 */
	public void storeFile(IFileHandle pFileHandle) throws Throwable
	{
		String sFormat = FileUtil.getExtension(pFileHandle.getFileName().toLowerCase());

		if ("png".equals(sFormat) || "jpg".equals(sFormat) || "gif".equals(sFormat))
		{
			ByteArrayOutputStream stream = new ByteArrayOutputStream();

			ImageUtil.createScaledImage(pFileHandle.getInputStream(), 140, 185, true, stream, sFormat);

			stream.close();

			rdbCustomer.setValue("FILENAME", pFileHandle.getFileName());
			rdbCustomer.setValue("IMAGE", stream.toByteArray());

			try
			{
				rdbCustomer.saveSelectedRow();
			}
			catch (Exception pException)
			{
				// Silent Save of current row.
			}
		}
		else
		{
			throw new IOException("Image format '" + sFormat + "' not supported. Use 'png', 'jpg' or 'gif'!");
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Actions
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Searches the contacts with the search text.
	 * 
	 * @throws ModelException if the search fails
	 */
	public void doFilter() throws ModelException
	{
		String suche = (String)drSearch.getValue("SEARCH");
		
		if (suche == null)
		{
			rdbCustomer.setFilter(null);
		}
		else
		{
			ICondition filter = new LikeIgnoreCase("FIRSTNAME", "*" + suche + "*").or(new LikeIgnoreCase("LASTNAME", "*" + suche + "*").or(new LikeIgnoreCase("EMAIL",
					"*" + suche + "*")));

			rdbCustomer.setFilter(filter);
		}
	}

	/**
	 * Starts the image upload.
	 * 
	 * @throws Throwable if an error occures.
	 */
	public void doUpload() throws Throwable
	{
		if (rdbCustomer.getSelectedRow() >= 0)
		{
			application.getLauncher().getFileHandle(this, "storeFile");
		}
	}

}	// CustomerEditFrame
