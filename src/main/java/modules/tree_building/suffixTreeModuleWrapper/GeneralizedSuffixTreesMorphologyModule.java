package modules.tree_building.suffixTreeModuleWrapper;



import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.OutputPort;
import modules.ModuleImpl;
import modules.tree_building.suffixTree.BranchedStringBufferElement;
import modules.tree_building.suffixTree.GST;
import common.logicBits.LogOpOR;
import modules.tree_building.suffixTree.ResultToMorphListListener;

import modules.tree_building.suffixTree.SuffixTree;
import modules.tree_building.suffixTree.TreeWalker;

import common.parallelization.CallbackReceiver;

/**
 * 
 * @author rols
 *
 */
import base.workbench.ModuleRunner;

public class GeneralizedSuffixTreesMorphologyModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(GeneralizedSuffixTreesMorphologyModule.class, args);
	}

	
	
	
	
	// Define property keys (every setting has to have a unique key to associate it with)
	/*JRpublic static final String PROPERTYKEY_DELIMITER_A = "delimiter A";
	public static final String PROPERTYKEY_DELIMITER_B = "delimiter B";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "delimiter out";
	
	*/
	
	// Variables for the module
	private static final String MODULE_NAME = "GeneralisedSuffixTreesMorphologyModule";
	private static final String MODULE_DESCRIPTION = "Module reads two inputs, one reversed into suffix trees."
			;

	// Variables describing I/O
	private static final String INPUT_TEXT1_ID = "plain";
	private static final String INPUT_TEXT2_ID = "reversed";
	private static final String OUTPUTID = "output";
	
	private static final String OUTPUTID_DESC = "[text/plain] Takes a plaintext representation of the result.";
	
	// Define I/O IDs (must be unique for every input or output)
	//JRprivate static final String ID_INPUT_A = "input A";
	//private static final String ID_INPUT_B = "input B";
	//JR
	// private static final String ID_OUTPUT/*_ENTWINED*/ = "output";
	//JRprivate static final String ID_OUTPUT_ENTWINED_CAPITALISED = "capitals";
	
	// Local variables
	/*private String inputdelimiter_a;
	private String inputdelimiter_b;
	private String outputdelimiter;
	*/
	
	public GeneralizedSuffixTreesMorphologyModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		
		// Call parent constructor
		super(callbackReceiver, properties);

		// Set the modules name and description
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, MODULE_NAME);
		this.setDescription(MODULE_DESCRIPTION);

		// Add module category

		// Setup I/O, reads from char input 
		/*JRInputPort inputTextPort = new InputPort(INPUT_TEXT_ID, INPUT_TEXT_DESC, this);
		inputTextPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputTextPort);
		*/
		// add input and output ports
		InputPort in1 = new InputPort(INPUT_TEXT1_ID, "First character input.", this);
		in1.addSupportedPipe(CharPipe.class);
		this.addInputPort(in1);

		InputPort in2 = new InputPort(INPUT_TEXT2_ID, "Reversed character input.", this);
		in2.addSupportedPipe(CharPipe.class);
		this.addInputPort(in2);
		
		OutputPort out = new OutputPort(OUTPUTID,OUTPUTID_DESC,this);
		out.addSupportedPipe(CharPipe.class);
		//this.addOutputPort(out);
		
		
		//this.setupOutputPorts();
		
		super.addOutputPort(out);
		
		
	}
	
	
	/**
	 * This builds a generalised suffix tree and constructs output for all
	 * connected output ports
	 */
	@Override
	public boolean process() throws Exception {
		

		//ResultToMorphListListener test=new ResultToMorphListListener(null,false);

		boolean result=true;
		StringBuffer outputBuffer=null;

		try {			

			// actually build the trees
			final BufferedReader textReader1 = new BufferedReader(
					this.getInputPorts().get(INPUT_TEXT1_ID).getInputReader());
			final SuffixTree suffixTree1 = GST.buildGST(textReader1,null);
			
			final BufferedReader textReader2 = new BufferedReader(
					this.getInputPorts().get(INPUT_TEXT2_ID).getInputReader());
			final SuffixTree suffixTree2 = GST.buildGST(textReader2,null);
			
			System.out.println("SuffixTrees Built");
			
			
			// 
			ResultToMorphListListener resultToMorphListListener1=
					new ResultToMorphListListener(suffixTree1,false);
			TreeWalker.walk(suffixTree1.getRoot(), suffixTree1, 
			resultToMorphListListener1);
			ArrayList<BranchedStringBufferElement> branchedStringElementList1=
			resultToMorphListListener1.results();
			System.out.println();System.out.println();System.out.println();
			ResultToMorphListListener resultToMorphListListener2=
					new ResultToMorphListListener(suffixTree2,true);
			TreeWalker.walk(suffixTree2.getRoot(), suffixTree2, 
					resultToMorphListListener2);
			ArrayList<BranchedStringBufferElement> branchedStringElementList2=
			resultToMorphListListener2.results();
			// print resulted branchedStringLists
			System.out.print("branchedStringElementList1 ");
			resultToMorphListListener1.printBranchedStringElementList(branchedStringElementList1);
			System.out.print("branchedStringElementList2 ");
			resultToMorphListListener2.printBranchedStringElementList(branchedStringElementList2);
			ArrayList<BranchedStringBufferElement> branchedStringElementListOr=
				resultToMorphListListener1.logOp(branchedStringElementList1,
				branchedStringElementList2,new LogOpOR());	
			System.out.print("branchedStringElementListOr ");
			outputBuffer=
			resultToMorphListListener1.printBranchedStringElementList(branchedStringElementListOr);
			
			String outputString = outputBuffer.toString();
			System.out.println(" outputString:");
			System.out.println(outputString);
			// Write to outputs
			this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(outputString);
			
			// Close outputs (important!)
			this.closeAllOutputs();
			
			
		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			this.closeAllOutputs();
		}
		
	
		return result;
	}


	
	
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		/*JR
		this.inputdelimiter_a = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_A, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_A));
		this.inputdelimiter_b = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_B, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_B));
		this.outputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT));
		*/
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}

 
	



