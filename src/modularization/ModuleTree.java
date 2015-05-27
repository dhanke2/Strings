package modularization;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import parallelization.Action;
import parallelization.CallbackReceiverImpl;

/**
 * Allows the construction of a tree of modules.
 * @author Marcel Boeing
 *
 */
public class ModuleTree extends CallbackReceiverImpl {
	
	// The treemodel used to organize the modules
	private TreeModel moduleTree;
	
	// List of started threads
	private List<Thread> startedThreads = new ArrayList<Thread>();
	
	public ModuleTree(){
		super();
	}
	
	public ModuleTree(Module module){
		super();
		this.setRootModule(module);
	}
	
	/**
	 * @return the moduleTree
	 */
	public TreeModel getModuleTree() {
		return moduleTree;
	}

	/**
	 * @return the startedThreads
	 */
	public List<Thread> getStartedThreads() {
		return startedThreads;
	}

	public void setRootModule(Module module){
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(module);
		this.moduleTree = new DefaultTreeModel(rootNode);
	}
	
	private DefaultMutableTreeNode locateModuleInTree(Module module){
		// Determine the tree's root node
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.moduleTree.getRoot();
		
		return this.locateModuleInTree(module, rootNode);
	}
	
	private DefaultMutableTreeNode locateModuleInTree(Module module, DefaultMutableTreeNode parentNode){
		if (parentNode.getUserObject() != null && parentNode.getUserObject().equals(module))
			return parentNode;
		
		// Recursively run this method for the tree node's children
		Enumeration<?> childNodes = parentNode.children();
		while (childNodes.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
			DefaultMutableTreeNode foundNode = this.locateModuleInTree(module, childNode);
			if (foundNode != null)
				return foundNode;
		}
		
		return null;
	}
	
	/**
	 * Appends a module to the given tree node. Tries to guess the best pipe and connects the modules.
	 * @param newModule Module to add
	 * @param parentModule Module the new module should be a child of
	 * @return True if successful
	 * @throws NotSupportedException Thrown if the pipe is not compatible with both the new and the parent module
	 * @throws Exception Thrown if the method argument values are not right
	 */
	public boolean addModule(Module newModule, Module parentModule) throws NotSupportedException, Exception{
		
		Pipe pipe = new BytePipe();
		if (!(newModule.supportsInputPipe(pipe) && parentModule.supportsOutputPipe(pipe))){
			pipe = new CharPipe();
			if (!(newModule.supportsInputPipe(pipe) && parentModule.supportsOutputPipe(pipe))){
				throw new Exception("I'm very sorry, but the I/O of those two modules does not seem to be compatible.");
			}
		}
		
		// Jump to more detailed method
		return this.addModule(newModule, parentModule, pipe);
	}
	
	/**
	 * Appends a module to the given tree node
	 * @param newModule Module to add
	 * @param parentModule Module the new module should be a child of
	 * @param connectingPipe Pipe to connect the new module to its parent
	 * @return True if successful
	 * @throws NotSupportedException Thrown if the pipe is not compatible with both the new and the parent module
	 * @throws Exception Thrown if the method argument values are not right
	 */
	public boolean addModule(Module newModule, Module parentModule, Pipe pipe) throws NotSupportedException, Exception{
		// Determine the location of the parent module within the tree
		DefaultMutableTreeNode parentModuleNode = this.locateModuleInTree(parentModule);
		
		// If the parent module isn't found, we're done
		if (parentModuleNode==null) return false;
		
		// Jump to more detailed method
		return this.addModule(newModule, parentModuleNode, pipe);
	}
	
	/**
	 * Appends a module to the given tree node
	 * @param newModule Module to add
	 * @param parentNode Node the new module should be a child of
	 * @param connectingPipe Pipe to connect the new module to its parent
	 * @return True if successful
	 * @throws NotSupportedException Thrown if the pipe is not compatible with both the new and the parent module
	 * @throws Exception Thrown if the method argument values are not right
	 */
	public boolean addModule(Module newModule, DefaultMutableTreeNode parentNode, Pipe connectingPipe) throws NotSupportedException, Exception {
		
		// Check whether the parent node holds a module as expected (throw an exception otherwise)
		if (parentNode.getUserObject()==null || !Module.class.isAssignableFrom(parentNode.getUserObject().getClass()))
			throw new Exception("Excuse me, but this tree node does not hold a module -- I am afraid I cannot continue the operation.");
		
		// Determine parent module
		Module parentModule = (Module) parentNode.getUserObject();
		
		// Make sure the I/O pipe is compatible to both modules
		if (!newModule.supportsInputPipe(connectingPipe) || !parentModule.supportsOutputPipe(connectingPipe))
			throw new NotSupportedException("Terribly sorry, but this pipe cannot be used for I/O between those modules.");
		
		// Connect modules
		newModule.setInputPipe(connectingPipe);
		parentModule.addOutputPipe(connectingPipe);
		
		// Create new tree node
		DefaultMutableTreeNode newModuleNode = new DefaultMutableTreeNode(newModule);
		
		// Insert new tree node
		parentNode.add(newModuleNode);
		
		// Set module's callback receiver
		newModule.setCallbackReceiver(this);
		
		return true;
	}
	
	/**
	 * Runs the module tree
	 */
	public void runModules() throws Exception {
		
		// Determine the tree's root node
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.moduleTree.getRoot();
		
		// Run modules
		this.runModules(rootNode);
		
		// Wait for threads to finish
		while (!this.startedThreads.isEmpty()) {
			try {
				// Sleep for one second
				Thread.sleep(250l);

				// Print pretty overview
				System.out.print(this.prettyPrint() + "\r");

				// Test which threads are still active and remove the rest from
				// the list
				for (int i = this.startedThreads.size(); i > 0; i--) {
					if (!this.startedThreads.get(i - 1).isAlive()) {
						// Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO,
						// "Thread "+this.startedThreads.get(i-1).getName()+" is done.");
						Thread removedThread = this.startedThreads
								.remove(i - 1);
						if (removedThread != null)
							Logger.getLogger(this.getClass().getSimpleName())
									.log(Level.FINEST,
											"Removed thread "
													+ removedThread.getName()
													+ ".");
						else
							Logger.getLogger(this.getClass().getSimpleName())
									.log(Level.WARNING,
											"Could not remove thread.");
					} else {
						// Logger.getLogger(this.getClass().getSimpleName()).log(Level.FINEST,
						// "Thread "+this.startedThreads.get(i-1).getName()+" is still active.");
					}
				}

			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	/**
	 * Runs the modules from a given tree node on.
	 * @param parentNode Root of tree branch from which to run the modules
	 * @throws Exception Thrown if the method argument values are not right
	 */
	private void runModules(DefaultMutableTreeNode parentNode) throws Exception {
		
		// Check whether the parent node holds a module as expected (throw an exception otherwise)
		if (parentNode.getUserObject()==null || !Module.class.isAssignableFrom(parentNode.getUserObject().getClass()))
			throw new Exception("Excuse me, but this tree node does not hold a module -- I am afraid I cannot continue the operation.");
			
		// Determine the module
		final Module m = (Module) parentNode.getUserObject();

		// Define action to perform on success
		Action successAction = new Action() {
			@Override
			public void perform(Object processResult) {
				Boolean result = Boolean.parseBoolean(processResult.toString());
				if (result)
					Logger.getLogger(this.getClass().getSimpleName()).log(
							Level.INFO,
							"Module " + m.getName()
									+ " has successfully finished processing.");
				else
					Logger.getLogger(this.getClass().getSimpleName())
							.log(Level.WARNING,
									"Module "
											+ m.getName()
											+ " did not finish processing successfully.");
			}
		};

		// Define action to perform on failure
		Action failureAction = new Action() {
			@Override
			public void perform(Object processResult) {
				Exception e = new Exception("(no error message received)");
				if (processResult.getClass().isAssignableFrom(e.getClass())) {
					e = (Exception) processResult;
				}
				Logger.getLogger(this.getClass().getSimpleName()).log(
						Level.SEVERE,
						"Module " + m.getName() + " encountered an error.", e);
			}
		};

		// register callback actions
		this.registerSuccessCallback(m, successAction);
		this.registerFailureCallback(m, failureAction);

		Thread t1 = new Thread(m);
		t1.setName(m.getName());
		this.startedThreads.add(t1);
		t1.start();
		
		// Recursively run this method for the tree node's children
		Enumeration<?> childNodes = parentNode.children();
		while (childNodes.hasMoreElements()) {
		  DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
		  this.runModules(childNode);
		}
		
	}
	
	/**
	 * Prints a pretty representation of the module chain
	 * @return String
	 * @throws Exception
	 */
	public String prettyPrint() throws Exception {
		// Determine the tree's root node
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.moduleTree.getRoot();
		
		return this.prettyPrint(rootNode);
	}
	
	private String prettyPrint(DefaultMutableTreeNode parentNode) throws Exception {
		
		if (parentNode.getUserObject()==null || !Module.class.isAssignableFrom(parentNode.getUserObject().getClass()))
			throw new Exception("This tree node does not hold a module -- I am sorry, but I cannot print it.");
		
		// Instantiate string buffer to concatenate the result
		StringBuffer result = new StringBuffer();
		
		Module module = (Module) parentNode.getUserObject();
		Pipe pipe = module.getInputBytePipe();
		if (pipe == null)
			pipe = module.getInputCharPipe();
		
		// Print pipe details
		if (pipe != null){
			result.append("--"+pipe.getClass().getSimpleName()+"--> ");
		}
		
		// Print module details
		result.append(module.getName()+"["+module.getStatus()+"] ");
		
		// Recursively run this method for the tree node's children
		Enumeration<?> childNodes = parentNode.children();
		while (childNodes.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
			result.append(this.prettyPrint(childNode));
		}
		
		return result.toString();
	}
	
}