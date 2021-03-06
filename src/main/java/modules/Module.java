package modules;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import common.parallelization.CallbackProcess;

/**
 * Defines an abstract view of any processing module.
 * @author Marcel Boeing
 *
 */
public interface Module extends CallbackProcess {
	
	public static final int STATUSCODE_SUCCESS = 0;
	public static final int STATUSCODE_FAILURE = 1;
	public static final int STATUSCODE_RUNNING = 2;
	public static final int STATUSCODE_NOTYETRUN = 3;
	public static final String[] STATUSMESSAGES = new String[]{"successful","failed","running","idle"};
	
	/**
	 * Returns a map of available input ports (key=identifier).
	 * @return List of ports
	 */
	public Map<String,InputPort> getInputPorts();
	
	/**
	 * Returns a map of available output ports (key=identifier).
	 * @return List of ports
	 */
	public Map<String,OutputPort> getOutputPorts();
	
	/**
	 * Starts the process.
	 * @return True if the process ended successfully.
	 * @throws Exception When something goes wrong, duh.
	 */
	public boolean process() throws Exception;
	
	/**
	 * Outputs the name of the module.
	 * @return Name
	 */
	public String getName();
	
	/**
	 * Sets the name of the module.
	 * @param name Name
	 */
	public void setName(String name);
	
	/**
	 * Outputs the description of the module.
	 * @return Description
	 */
	public String getDescription();
	
	/**
	 * Sets the description of the module.
	 * @param desc Description
	 */
	public void setDescription(String desc);
	
	/**
	 * Outputs the properties used by this module instance.
	 * @return properties
	 */
	public Properties getProperties();
	
	/**
	 * Sets the properties used by this module instance.
	 * @param properties properties to set
	 * @throws Exception when properties are invalid
	 */
	public void setProperties(Properties properties) throws Exception;
	
	/**
	 * Returns a map containing all valid property keys of this module
	 * with a short description as value.
	 * @return Map
	 */
	public Map<String,String> getPropertyDescriptions();
	
	/**
	 * Returns a map containing available default values for properties of this module.
	 * @return Map
	 */
	public Map<String,String> getPropertyDefaultValues();
	
	/**
	 * Returns a code indicating the status of the module (see static vars in this class)
	 * @return status code
	 */
	public int getStatus();
	
	/**
	 * Applies all relevant properties to this instance. Subclasses should
	 * override this, apply the properties they use themselves and call
	 * super().applyProperties() afterwards.
	 * 
	 * @throws Exception
	 *             when something goes wrong (property cannot be applied etc.)
	 */
	public void applyProperties() throws Exception;
	
	/**
	 * Resets all outputs.
	 * @throws IOException Thrown if something goes wrong
	 */
	public void resetOutputs() throws IOException;
	
	/**
	 * Returns the category name associated with this module.
	 * @return Category name
	 */
	public String getCategory();

	/**
	 * Set category name.
	 * @param category Category name
	 */
	public void setCategory(String category);
	
	/**
	 * Gives detail information about the module's current status (may be null).
	 * @return String with status details or null
	 */
	public String getStatusDetail();
	
	/**
	 * Set detail information about the module's current status (may be null).
	 * @param statusDetail String with status details or null
	 */
	public void setStatusDetail(String statusDetail);
	
	/**
	 * Returns a map containing metadata.
	 * @return Map
	 */
	public Map<String,Object> getMetadata();
	
	/**
	 * Set map containing metadata.
	 * @param metadata Map to set
	 */
	public void setMetadata(Map<String,Object> metadata);

}
