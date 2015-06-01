package ch.bfh.sming.service;

public interface Txw51Service {

	/**
	 * Initialize the BLED112 and the SMING.
	 * 
	 * @param serialPort
	 *            the serialPort of the BLED112. If null, a selection dialog
	 *            will be shown.
	 */
	public void initialize(String serialPort);

	/**
	 * Print the available services and their handles to the console.
	 */
	public void listServices();

	/**
	 * Read an attribute on the SMING by its handle.
	 * 
	 * @param handle
	 */
	public void readAttribute(int handle);

}
