package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Image;
import java.lang.reflect.*;
import java.text.Collator;
import java.util.*;
import java.util.List;

/**
 * A menu for perspective selection.  
 * <p>
 * A <code>PerspectiveMenu</code> is used to populate a menu with
 * perspective shortcut items.  If the user selects one of these items 
 * an action is performed for the selected perspective.
 * </p><p>
 * The visible perspective items within the menu are dynamic and reflect the
 * available set generated by each subclass. The default available set consists
 * of a limited combination of the perspective shortcut list of the current
 * perspective, and the most recently used perspective list.
 * </p><p>
 * This class is abstract.  Subclasses must implement the <code>run</code> method,
 * which performs a specialized action for the selected perspective.
 * </p>
 */
public abstract class PerspectiveMenu extends ContributionItem {
	private static final int MAX_PERSPECTIVE_ITEMS = 9;
	private static IPerspectiveRegistry reg;

	private IWorkbenchWindow window;
	private boolean showActive = false;

	private Comparator comparator = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object ob1, Object ob2) {
			IPerspectiveDescriptor d1 = (IPerspectiveDescriptor) ob1;
			IPerspectiveDescriptor d2 = (IPerspectiveDescriptor) ob2;
			return collator.compare(d1.getLabel(), d2.getLabel());
		}
	};
	
	private static Hashtable imageCache = new Hashtable(11);

	/**
	 * Constructs a new instance of <code>PerspectiveMenu</code>.  
	 *
	 * @param window the window containing this menu
	 * @param id the menu id
	 */
	public PerspectiveMenu(IWorkbenchWindow window, String id) {
		super(id);
		this.window = window;
		if (reg == null)
			reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
	}
	
	/* (non-Javadoc)
	 * Creates a menu item for a perspective.
	 */
	/* package */
	void createMenuItem(
		Menu menu,
		int index,
		final IPerspectiveDescriptor desc,
		boolean bCheck) {
			
		MenuItem mi = new MenuItem(menu, bCheck ? SWT.RADIO : SWT.PUSH, index);
		mi.setText(desc.getLabel());
		Image image = getImage(desc);
		if (image != null) {
			mi.setImage(image);
		}
		mi.setSelection(bCheck);
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				run(desc, e);
			}
		});
	}
	
	/* (non-Javadoc)
	 * Creates a menu item for "Other...".
	 */
	/* package */
	void createOtherItem(Menu menu, int index) {
		MenuItem mi = new MenuItem(menu, SWT.PUSH, index);
		mi.setText(WorkbenchMessages.getString("PerspectiveMenu.otherItem"));  //$NON-NLS-1$
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runOther(e);
			}
		});
	}
	
	/* (non-Javadoc)
	 * Fills the menu with perspective items.
	 */
	public void fill(Menu menu, int index) {

		// Get the checked persp.
		String checkID = null;
		if (showActive) {
			IWorkbenchPage activePage = window.getActivePage();
			if ((activePage != null) && (activePage.getPerspective() != null))
				checkID = activePage.getPerspective().getId();
		}

		// Collect and sort perspective items.
		ArrayList persps = getPerspectiveItems();
		Collections.sort(persps, comparator);

		// Add perspective shortcut
		for (int i = 0; i < persps.size(); i++) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor) persps.get(i);
			createMenuItem(menu, index++, desc, desc.getId().equals(checkID));
		}

		// Add others item..
		if (persps.size() > 0) {
			new MenuItem(menu, SWT.SEPARATOR, index++);
		}
		createOtherItem(menu, index);
	}
	
	/**
	 * Returns an image to show for the corresponding perspective descriptor.
	 *
	 * @param perspDesc the perspective descriptor
	 * @return the image or null
	 */
	private Image getImage(IPerspectiveDescriptor perspDesc) {
		ImageDescriptor imageDesc = perspDesc.getImageDescriptor();
		if (imageDesc == null) {
			imageDesc =
				WorkbenchImages.getImageDescriptor(
					IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE_HOVER);
		}
		if (imageDesc == null) {
			return null;
		}
		Image image = (Image) imageCache.get(imageDesc);
		if (image == null) {
			image = imageDesc.createImage();
			imageCache.put(imageDesc, image);
		}
		return image;
	}
	
	/* (non-Javadoc)
	 * Returns the perspective shortcut items for the active perspective.
	 * 
	 * @return a list of <code>IPerspectiveDescriptor</code> items
	 */
	private ArrayList getPerspectiveShortcuts() {
		ArrayList list = new ArrayList();

		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return list;

		ArrayList ids = ((WorkbenchPage) page).getPerspectiveActionIds();
		if (ids == null)
			return list;

		for (int i = 0; i < ids.size(); i++) {
			String perspID = (String) ids.get(i);
			IPerspectiveDescriptor desc = reg.findPerspectiveWithId(perspID);
			if (desc != null && !list.contains(desc))
				list.add(desc);
		}

		return list;
	}
	
	/* (non-Javadoc)
	 * Gets the most recently used (MRU) shortcut perspectives
	 * (<code>IPerspectiveDescriptor</code> items)
	 * <p>
	 * The list is formed from the global perspective history
	 * in the workbench.
	 * </p>
	 * @param dest destination list to contain the items
	 * @param destStart index in destination list to start copying items at
	 * @param count number of items to copy from history
	 * @return the number of items actually copied
	 */
	private int getPerspectiveMru(List dest, int destStart, int count) {
		Workbench wb = (Workbench) WorkbenchPlugin.getDefault().getWorkbench();
		return wb.getPerspectiveHistory().copyItems(dest, destStart, count);
	}
	
	/**
	 * Returns the available list of perspectives to display
	 * in the menu.
	 * <p>
	 * By default, the list contains the perspective shortcuts
	 * for the current perspective, the most recently used
	 * perspectives, and the default perspective.
	 * </p><p>
	 * Subclasses can override this method to return a different list.
	 * Care should be taken to keep this list to a minimum (7 +/- 2 items
	 * is a good guideline to follow).
	 * </p>
	 * 
	 * @return an <code>ArrayList<code> of perspective items <code>IPerspectiveDescriptor</code>
	 */
	protected ArrayList getPerspectiveItems() {
		/* Allow the user to see all the perspectives they have 
	 	 * selected via Customize Perspective. Bugzilla bug #23445 */
		ArrayList shortcuts = getPerspectiveShortcuts();		
		int emptySlots = shortcuts.size();
		if (emptySlots < MAX_PERSPECTIVE_ITEMS)
			emptySlots = MAX_PERSPECTIVE_ITEMS;
		ArrayList list = new ArrayList(emptySlots);

		// Add default perspective.
		String id = reg.getDefaultPerspective();
		IPerspectiveDescriptor desc = reg.findPerspectiveWithId(id);
		if (desc != null) {
			list.add(desc);
			emptySlots--;
		}

		// Add perspective shortcuts from the active perspective
		int size = shortcuts.size();
		for (int i = 0; i < size && emptySlots > 0; i++) {
			if (!list.contains(shortcuts.get(i))) {
				list.add(shortcuts.get(i));
				emptySlots--;
			}
		}

		// Add perspectives from MRU list
		if (emptySlots > 0) {
			ArrayList mru = new ArrayList(MAX_PERSPECTIVE_ITEMS);
			int count = getPerspectiveMru(mru, 0, MAX_PERSPECTIVE_ITEMS);
			for (int i = 0; i < count && emptySlots > 0; i++) {
				if (!list.contains(mru.get(i))) {
					list.add(mru.get(i));
					emptySlots--;
				}
			}
		}
				

		return list;
	}

	/**
	 * Returns whether the menu item representing the active perspective
	 * will have a check mark.
	 *
	 * @return <code>true</code> if a check mark is shown, <code>false</code> otherwise
	 */
	protected boolean getShowActive() {
		return showActive;
	}
	
	/**
	 * Returns the window for this menu.
	 *
	 * @returns the window 
	 */
	protected IWorkbenchWindow getWindow() {
		return window;
	}
	
	/* (non-Javadoc)
	 * Returns whether this menu is dynamic.
	 */
	public boolean isDynamic() {
		return true;
	}
	
	/**
	 * Runs an action for a particular perspective.  The behavior of the
	 * action is defined by the subclass.
	 *
	 * @param desc the selected perspective
	 */
	protected abstract void run(IPerspectiveDescriptor desc);
	
	/**
	 * Runs an action for a particular perspective.  The behavior of the
	 * action is defined by the subclass.
	 *
	 * @param desc the selected perspective
	 * @param event SelectionEvent - the event send along with the selection callback
	 */
	protected void run(IPerspectiveDescriptor desc, SelectionEvent event) {
		//Do a run without the event by default
		run(desc);
	}
	
	/* (non-Javadoc)
	 * Show the "other" dialog, select a perspective, and run it. Pass on the selection
	 * event should the meny need it.
	 */
	void runOther(SelectionEvent event) {
		SelectPerspectiveDialog dlg =
			new SelectPerspectiveDialog(window.getShell(), reg);
		dlg.open();
		if (dlg.getReturnCode() == Window.CANCEL)
			return;
		IPerspectiveDescriptor desc = dlg.getSelection();
		if (desc != null) {
			run(desc, event);
		}
	}
	
	/**
	 * Sets the showActive flag.  If <code>showActive == true</code> then the
	 * active perspective is hilighted with a check mark.
	 *
	 * @param the new showActive flag
	 */
	protected void showActive(boolean b) {
		showActive = b;
	}
}