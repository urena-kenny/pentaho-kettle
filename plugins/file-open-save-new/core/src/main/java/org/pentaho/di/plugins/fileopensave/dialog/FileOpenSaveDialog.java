/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2021 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.plugins.fileopensave.dialog;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.plugins.fileopensave.api.file.FileDetails;
import org.pentaho.di.plugins.fileopensave.api.providers.Directory;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.cache.FileCache;
import org.pentaho.di.plugins.fileopensave.controllers.FileController;
import org.pentaho.di.plugins.fileopensave.providers.ProviderService;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.util.HelpUtils;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

public class FileOpenSaveDialog extends Dialog implements FileDetails {
  private static final Class<?> PKG = FileOpenSaveDialog.class;

  public static final String STATE_SAVE = "save";
  public static final String STATE_OPEN = "open";
  public static final String SELECT_FOLDER = "selectFolder";
  // private Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX;
  private static final String HELP_URL =
      Const.getDocUrl( "Products/Work_with_transformations#Open_a_transformation" );

  public static final String PATH_PARAM = "path";
  public static final String USE_SCHEMA_PARAM = "useSchema";
  public static final String CONNECTION_PARAM = "connection";
  public static final String PROVIDER_PARAM = "provider";
  public static final String PROVIDER_FILTER_PARAM = "providerFilter";
  public static final String FILTER_PARAM = "filter";
  public static final String DEFAULT_FILTER_PARAM = "defaultFilter";
  public static final String CONNECTION_FILTER_PARAM = "connectionTypes";
  public static final String ORIGIN_PARAM = "origin";
  public static final String FILENAME_PARAM = "filename";
  public static final String FILE_TYPE_PARM = "fileType";
  public static final String OBJECT_ID_PARAM = "objectId";
  public static final String NAME_PARAM = "name";
  public static final String PARENT_PARAM = "parent";
  public static final String TYPE_PARAM = "type";

  private String objectId;
  private String name;
  private String path;
  private String parentPath;
  private String type;
  private String connection;
  private String provider;

  private LogChannelInterface log;
  private int width;
  private int height;

  private static final FileController FILE_CONTROLLER;

  static {
    LocalFileProvider localProvider = new LocalFileProvider();
    ProviderService providerService = new ProviderService( Arrays.asList( localProvider, localProvider ) );
    FILE_CONTROLLER = new FileController( new FileCache(), providerService );
  }

  public FileOpenSaveDialog( Shell parentShell, int width, int height, LogChannelInterface logger ) {
    super( parentShell );
    this.log = logger;
    this.width = width;
    this.height = height;
    setShellStyle( OPTIONS );
  }

  public void open( FileDialogOperation fileDialogOperation ) {

  }

  @Override
  protected void configureShell( Shell newShell ) {
    // newShell.setImage( LOGO );
    newShell.setText( "Command Line" );
    PropsUI.getInstance().setLook( newShell );
    newShell.setMinimumSize( 545, 458 );
  }

  @Override
  protected Point getInitialSize() {
    return new Point( width, height );
  }

  @Override
  protected Control createContents( Composite parent ) {
    FormLayout formLayout = new FormLayout();
    formLayout.marginTop = 20;
    formLayout.marginBottom = 25;

    parent.setLayout( formLayout );
    Composite header = createHeader( parent );
    header.setLayoutData( new FormDataBuilder().top( 0, 0 ).left( 0, 0 ).right( 100, 0 ).result() );
    Composite buttons = createButtonsBar( parent );
    buttons.setLayoutData( new FormDataBuilder().top( header, 25 ).left( 0, 0 ).right( 100, 0 ).result() );

    FlatButton helpButton =
        new FlatButton( parent, SWT.NONE )
            .setEnabledImage( rasterImage( "img/help.svg", 24, 24 ) )
            .setDisabledImage( rasterImage( "img/help.svg", 24, 24 ) )
            .setEnabled( true )
            .setLayoutData( new FormDataBuilder().bottom( 100, 0 ).left( 0, 20 ).result() );
    helpButton.getLabel().setText( "Help" );

    Composite select = createFilesBrowser( parent );
    select.setLayoutData( new FormDataBuilder().top( buttons, 15 ).left( 0, 0 ).right( 100, 0 )
        .bottom( helpButton.getLabel(), -20 ).result() );

    return parent;
  }

  public Composite createHeader( Composite parent ) {
    Composite headerComposite = new Composite( parent, SWT.NONE );

    FormLayout formLayout = new FormLayout();
    formLayout.marginLeft = 20;
    formLayout.marginRight = 20;

    headerComposite.setLayout( formLayout );
    PropsUI.getInstance().setLook( headerComposite );

    Label lblSelect = new Label( headerComposite, SWT.LEFT );
    PropsUI.getInstance().setLook( lblSelect );

    // TODO: Set text dynamically
    lblSelect.setText( "Select a file or folder" );

    FontData[] fontData = lblSelect.getFont().getFontData();
    Arrays.stream( fontData ).forEach( fd -> fd.height = 20 );
    final Font bigFont = new Font( getShell().getDisplay(), fontData );
    lblSelect.setFont( bigFont );
    lblSelect.addDisposeListener( ( e ) -> bigFont.dispose() );
    lblSelect.setLayoutData( new FormDataBuilder().result() );

    // TODO: A whole bunch more with search function
    final Color WHITE = new Color( getShell().getDisplay(), 255, 255, 255 );
    Composite searchComp = new Composite( headerComposite, SWT.BORDER );
    PropsUI.getInstance().setLook( searchComp );
    searchComp.addDisposeListener( ( e ) -> WHITE.dispose() );
    searchComp.setLayoutData( new FormDataBuilder().right( 100, 0 ).result() );
    searchComp.setBackground( WHITE );

    RowLayout searchLayout = new RowLayout();
    searchLayout.center = true;
    searchComp.setLayout( searchLayout );

    Label lblSearch = new Label( searchComp, SWT.NONE );
    PropsUI.getInstance().setLook( lblSearch );
    lblSearch.setLayoutData( new RowData() );
    lblSearch.setBackground( WHITE );
    lblSearch.setImage( rasterImage( "img/Search.S_D.svg", 25, 25 ) );

    RowData rd = new RowData();
    rd.width = 200;
    Text txtSearch = new Text( searchComp, SWT.NONE );
    PropsUI.getInstance().setLook( txtSearch );
    txtSearch.setBackground( WHITE );
    txtSearch.setLayoutData( rd );

    headerComposite.layout();

    return headerComposite;
  }

  private Composite createButtonsBar( Composite parent ) {
    Composite buttons = new Composite( parent, SWT.NONE );
    PropsUI.getInstance().setLook( buttons );

    FormLayout formLayout = new FormLayout();
    formLayout.marginLeft = 20;
    formLayout.marginRight = 20;
    buttons.setLayout( formLayout );

    FlatButton backButton =
        new FlatButton( buttons, SWT.NONE )
            .setEnabledImage( rasterImage( "img/Backwards.S_D.svg", 32, 32 ) )
            .setDisabledImage( rasterImage( "img/Backwards.S_D_disabled.svg", 32, 32 ) )
            .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.back.button" ) )
            .setEnabled( false );
    FlatButton forwardButton =
        new FlatButton( buttons, SWT.NONE )
            .setEnabledImage( rasterImage( "img/Forwards.S_D.svg", 32, 32 ) )
            .setDisabledImage( rasterImage( "img/Forwards.S_D_disabled.svg", 32, 32 ) )
            .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.forward.button" ) )
            .setEnabled( true )
            .setLayoutData( new FormDataBuilder().left( backButton.getLabel(), 0 ).result() );

    Composite fileButtons = new Composite( buttons, SWT.NONE );
    PropsUI.getInstance().setLook( fileButtons );
    fileButtons.setLayout( new RowLayout() );
    fileButtons.setLayoutData( new FormDataBuilder().right( 100, 0 ).result() );

    FlatButton upButton =
        new FlatButton( fileButtons, SWT.NONE )
            .setEnabledImage( rasterImage( "img/Up_Folder.S_D.svg", 32, 32 ) )
            .setDisabledImage( rasterImage( "img/Up_Folder.S_D_disabled.svg", 32, 32 ) )
            .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.up-directory.button" ) )

            .setLayoutData( new RowData() )
            .setEnabled( true );

    FlatButton addButton =
        new FlatButton( fileButtons, SWT.NONE )
            .setEnabledImage( rasterImage( "img/New_Folder.S_D.svg", 32, 32 ) )
            .setDisabledImage( rasterImage( "img/New_Folder.S_D_disabled.svg", 32, 32 ) )
            .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.add-folder.button" ) )
            .setLayoutData( new RowData() )
            .setEnabled( false );

    FlatButton deleteButton =
        new FlatButton( fileButtons, SWT.NONE )
            .setEnabledImage( rasterImage( "img/Close.S_D.svg", 32, 32 ) )
            .setDisabledImage( rasterImage( "img/Close.S_D_disabled.svg", 32, 32 ) )
            .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.delete.button" ) )
            .setLayoutData( new RowData() )
            .setEnabled( false );

    FlatButton refreshButton =
        new FlatButton( fileButtons, SWT.NONE )
            .setEnabledImage( rasterImage( "img/Refresh.S_D.svg", 32, 32 ) )
            .setDisabledImage( rasterImage( "img/Refresh.S_D_disabled.svg", 32, 32 ) )
            .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.refresh.button" ) )
            .setLayoutData( new RowData() )
            .setEnabled( true );

    Composite navComposite = new Composite( buttons, SWT.BORDER );
    PropsUI.getInstance().setLook( navComposite );
    navComposite.setBackground( getShell().getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
    navComposite.setLayoutData(
      new FormDataBuilder().left( forwardButton.getLabel(), 10 ).right( fileButtons, -10 ).height( 32 ).result() );

    return buttons;
  }

  private Composite createFilesBrowser( Composite parent ) {
    Composite browser = new Composite( parent, SWT.NONE );
    PropsUI.getInstance().setLook( browser );
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginRight = 0;
    gridLayout.marginLeft = 0;
    browser.setLayout( gridLayout );

    SashForm sashForm = new SashForm( browser, SWT.HORIZONTAL );
    PropsUI.getInstance().setLook( sashForm );
    sashForm.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    TreeViewer treeViewer = new TreeViewer( sashForm, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION );
    PropsUI.getInstance().setLook( treeViewer.getTree() );

    
    Image imgFolder = rasterImage( "img/file_icons/Archive.S_D.svg", 25, 25 );
    Image imgDisk = rasterImage( "img/Disk.S_D.svg", 25, 25 );
    
    treeViewer.setLabelProvider( new LabelProvider() {
      @Override
      public String getText( Object element ) {
        if ( element instanceof Tree ) {
          return ( (Tree) element ).getName();
        } else if ( element instanceof Directory ) {
          return ( (Directory) element ).getName();
        } else if ( element instanceof File ) {
          return ( (File) element ).getName();
        }
        return null;
      }

      @Override
      public Image getImage( Object element ) {
        if( element instanceof Tree ) {
          return imgDisk; 
        } else if( element instanceof Directory ) {
          return imgFolder;
        }
        return null;
      }
    } );

    treeViewer.setContentProvider( new FileTreeContentProvider( FILE_CONTROLLER ) );
    treeViewer.setInput( "Dummy" );
    treeViewer.refresh();

    treeViewer.addDoubleClickListener( e -> {
      IStructuredSelection selection = (IStructuredSelection) e.getSelection();
      Object selectedNode = selection.getFirstElement();
      treeViewer.setExpandedState( selectedNode, !treeViewer.getExpandedState( selectedNode ) );
    });
    
    
    TableViewer tableViewer = new TableViewer( sashForm, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION );
    PropsUI.getInstance().setLook( tableViewer.getTable() );
    tableViewer.getTable().setHeaderVisible( true );;

    TableViewerColumn tvcName = new TableViewerColumn( tableViewer, SWT.NONE );
    tvcName.getColumn().setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.name.header" ) );
    tvcName.getColumn().setWidth( 250 );

   

    ColumnLabelProvider clp = new ColumnLabelProvider() {

      @Override
      public String getText( Object element ) {

        return element.toString();
      }

      @Override
      public Image getImage( Object element ) {
        return imgFolder;
      }

    };

    tvcName.setLabelProvider( clp );

    TableViewerColumn tvcType = new TableViewerColumn( tableViewer, SWT.NONE );
    tvcType.getColumn().setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.type.header" ) );
    tvcType.getColumn().setWidth( 80 );
    tvcType.getColumn().setResizable( false );
    tvcType.setLabelProvider( clp );

    TableViewerColumn tvcModified = new TableViewerColumn( tableViewer, SWT.NONE );
    tvcModified.getColumn().setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.modified.header" ) );
    tvcModified.getColumn().setWidth( 110 );
    tvcModified.getColumn().setResizable( false );

    tvcModified.setLabelProvider( clp );

    tableViewer.getTable().addListener( SWT.Resize, ( e ) -> {
      Rectangle r = tableViewer.getTable().getClientArea();
      tvcName.getColumn()
          .setWidth( Math.max( 150, r.width - tvcType.getColumn().getWidth() - tvcModified.getColumn().getWidth() ) );

    } );

    tableViewer.setContentProvider( ArrayContentProvider.getInstance() );

    Object[] entries = IntStream.range( 0, 100 ).mapToObj( i -> new Integer( i ) ).toArray();
    tableViewer.setInput( entries );

    sashForm.setWeights( new int[] {
      1, 2 } );

    return browser;
  }

  private Image rasterImage( String path, int width, int height ) {
    SwtUniversalImage img =
        SwtSvgImageUtil.getUniversalImage( getShell().getDisplay(), getClass().getClassLoader(), path );
    Image image = img.getAsBitmapForSize( getShell().getDisplay(), width, height );
    getShell().addDisposeListener( ( e ) -> {
      img.dispose();
      image.dispose();
    } );
    return image;
  }

  private void openHelpDialog() {
    HelpUtils.openHelpDialog( getShell(), "", HELP_URL );
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId( String objectId ) {
    this.objectId = objectId;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getConnection() {
    return connection;
  }

  public void setConnection( String connection ) {
    this.connection = connection;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider( String provider ) {
    this.provider = provider;
  }

  public String getParentPath() {
    return parentPath;
  }

  public void setParentPath( String parentPath ) {
    this.parentPath = parentPath;
  }

  protected static class FlatButton {

    private CLabel label;

    private AtomicBoolean enabled = new AtomicBoolean( true );

    private Color hoverColor;
    private Image enabledImage;
    private Image disabledImage;

    public FlatButton( Composite parent, int style ) {
      label = new CLabel( parent, style );
      PropsUI.getInstance().setLook( label );
      setEnabled( true );
      setHoverColor( parent.getDisplay().getSystemColor( SWT.COLOR_GRAY ) );

      label.addMouseTrackListener( new MouseTrackAdapter() {

        private Color origColor;

        @Override
        public void mouseEnter( MouseEvent arg0 ) {
          origColor = label.getBackground();
          if ( enabled.get() ) {
            label.setBackground( hoverColor );
          }
        }

        @Override
        public void mouseExit( MouseEvent e ) {
          if ( origColor != null ) {
            label.setBackground( origColor );
          }
        }

      } );
    }

    public CLabel getLabel() {
      return label;
    }

    public boolean isEnabled() {
      return enabled.get();
    }

    public FlatButton setEnabled( boolean enabled ) {

      if ( disabledImage != null && enabledImage != null ) {
        label.setImage( enabled ? enabledImage : disabledImage );
      } else if ( enabledImage != null && disabledImage == null ) {
        label.setImage( enabledImage );
      } else if ( enabledImage == null && disabledImage != null ) {
        label.setImage( disabledImage );
      }
      label.redraw();

      this.enabled.set( enabled );
      return this;

    }

    public Image getEnabledImage() {
      return enabledImage;
    }

    public FlatButton setEnabledImage( Image enabledImage ) {
      this.enabledImage = enabledImage;
      return this;
    }

    public Image getDisabledImage() {
      return disabledImage;
    }

    public FlatButton setDisabledImage( Image disabledImage ) {
      this.disabledImage = disabledImage;
      return this;
    }

    public FlatButton setToolTipText( String toolTipText ) {
      label.setToolTipText( toolTipText );
      return this;
    }

    public Color getHoverColor() {
      return hoverColor;
    }

    public FlatButton setHoverColor( Color hoverColor ) {
      this.hoverColor = hoverColor;
      return this;
    }

    public FlatButton setLayoutData( Object o ) {
      label.setLayoutData( o );
      return this;
    }

  }

  protected static class FileTreeContentProvider implements ITreeContentProvider {

    private final FileController fileController;

    public FileTreeContentProvider( FileController fileController ) {
      this.fileController = fileController;
    }

    @Override
    public Object[] getElements( Object inputElement ) {
      System.out.println( "here " + inputElement );
      if ( inputElement.equals( "Dummy" ) ) {
        Object[] results = fileController.load( "" ).toArray();
        for ( Object o : results ) {
          System.out.println( o );
        }
        return results;

      } else if ( inputElement instanceof Tree ) {

        Tree tree = (Tree) inputElement;

      }
      return null;
    }

    @Override
    public Object[] getChildren( Object parentElement ) {
      System.out.println( "Get Children "  + parentElement ); 
      if ( parentElement instanceof Tree ) {
        return ( (Tree) parentElement ).getChildren().toArray();
      } else if ( parentElement instanceof Directory ) {
        try {
          return fileController.getFiles( (Directory) parentElement, "", true ).toArray();
        } catch ( FileException e ) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      return new Object[0];
    }

    @Override
    public Object getParent( Object element ) {

      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean hasChildren( Object element ) {
      if ( element instanceof Tree ) {
        return ( (Tree) element ).isHasChildren();
      } else if ( element instanceof Directory ) {
        return ( (Directory) element ).isHasChildren();
      }
      return false;
    }

  }

}
