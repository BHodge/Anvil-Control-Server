import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;

/**
 * This class contains all Control functionality
 * @author Bryan Hodge
 *
 */
public class ACSController {

	private ACSModel model;
	private ACSView view;
	
	public ACSController(ACSModel model, ACSView view){
		
		this.model = model;
		this.view = view;
		
		// Add listeners + drop target functionality to view
		view.addQLButtonListener(new QLButtonListener());
		view.addQLDropTargets(new QLDropTargetFactory());
		view.addToggleButtonListener(new ToggleButtonListener());
		view.addSaveButtonListener(new SaveButtonListener());
	}
	
	
	/**
	 * Listener for Quick Launch buttons in the view
	 * @author Bryan Hodge
	 *
	 */
	class QLButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			//Open File Dialog, then set file
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(view);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	File f =  fc.getSelectedFile();
		       
		    	//put icon generation code in model
		       
		    	model.setQuickLaunchFile(f, Integer.parseInt(e.getActionCommand().substring(2))); 
		    }
		}
	}
	
	/**
	 * Listener for the Server Toggle button in the view
	 * @author Bryan Hodge
	 *
	 */
	class ToggleButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			model.toggleServer();
		}
	}
	
	/**
	 * Collect settings from view, make changes to model, save configuration
	 * @author Bryan Hodge
	 *
	 */
	class SaveButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			model.changeServerName(view.serv_name.getText());
			if(view.passwordChkbox.isSelected()){
				if(view.passChanged){
					model.changePassword(true, view.serv_pswd.getPassword());
				}
			}else{
				model.changePassword(false, null);
			}
			
			model.setBroadcastEnable(view.broadcastChkbox.isSelected());
			model.saveConfig();
		}
	}

	
	/**
	 * Factory used by a view to build our custom Quick Launch Bar DropTargets
	 * @author Bryan Hodge
	 *
	 */
	class QLDropTargetFactory implements DropTargetFactory {
		public QLDropTarget makeDropTarget(){
			return new QLDropTarget();
		}
		
	}
	
	/**
	 * Custom DropTarget functionality to handle dropping files
	 * to the Quick Launch bar
	 * @author Bryan Hodge
	 *
	 */
	@SuppressWarnings("serial")
	class QLDropTarget extends DropTarget{ 				
		@Override
		public synchronized void drop(DropTargetDropEvent dtde){
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            Transferable t = dtde.getTransferable();
			
			File f = null;
			Object data = null;
		
			try {
				data = t.getTransferData(DataFlavor.javaFileListFlavor);
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}

			if(data instanceof java.util.List){
				if( ((List<?>)data).get(0) instanceof File){
					f = (File) ((List<?>)data).get(0);
				}
			}
            
            //How to find which QL Button
            JButton temp = (JButton) dtde.getDropTargetContext().getComponent();
            
            model.setQuickLaunchFile(f, Integer.parseInt(temp.getActionCommand().substring(2)));
		}
	}
	
}
