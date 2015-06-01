/**
 * Copyright (C) 2015 Nicolas Schmid
 * Code heavily based on BGLIB:
 * Copyright (C) 2012 SINTEF <franck.fleurey@sintef.no>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.bfh.sming.service;

import static org.thingml.bglib.ByteUtils.bytesToString;
import gnu.io.SerialPort;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.thingml.bglib.BDAddr;
import org.thingml.bglib.BGAPI;
import org.thingml.bglib.BGAPIDefaultListener;
import org.thingml.bglib.BGAPIPacketLogger;
import org.thingml.bglib.BGAPITransport;
import org.thingml.bglib.BLEAttribute;
import org.thingml.bglib.BLED112;
import org.thingml.bglib.BLEDevice;
import org.thingml.bglib.BLEDeviceList;
import org.thingml.bglib.BLEService;
import org.thingml.bglib.ByteUtils;

import ch.bfh.sming.exceptions.Txw51Exception;
import ch.bfh.sming.gatt.GattHelper;
import ch.bfh.sming.gatt.Txw51Gatt;

public class Txw51ServiceImpl extends BGAPIDefaultListener implements Txw51Service {

	private static final Logger LOG = Logger.getLogger(Txw51Service.class.getName());

	protected BGAPI bgapi;
	protected SerialPort port;

	private BGAPIPacketLogger logger = new BGAPIPacketLogger();
	protected BLEDeviceList devList = new BLEDeviceList();
	private boolean debug;
	protected int connection = -1;
	private BLEDevice txw51Device;
	private Integer ccidHandle;

	private boolean bled112Found = false;
	private boolean twx51Connected;

	private static final String TXW51_NAME = "TXW51";

	private Map<String, Integer> handles = new HashMap<String, Integer>();
	/*
	 * GATT DISCOVERY
	 */
	private static final int IDLE = 0;
	private static final int SERVICES = 1;
	private static final int ATTRIBUTES = 2;
	private Iterator<BLEService> discoveryIt = null;
	private BLEService discoverySrv = null;
	private int discoveryState = IDLE;
	private boolean servicesDiscovered;

	private static Txw51ServiceImpl instance = null;

	public static Txw51Service getInstance() {
		if (instance == null) {
			instance = new Txw51ServiceImpl();
		}
		return instance;
	}

	// Hide constructor
	private Txw51ServiceImpl() {

	}

	public void initialize(String serialPort) {
		initialize(serialPort, false);
	}

	public void initialize(String serialPort, boolean debug) {
		this.debug = debug;

		// connect with BLE-Dongle
		System.out.println("Connecting with BLED112 ...");
		connectBLED112(serialPort);

		// wait until response was received
		while (!bled112Found) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		// Discover BLE Devices
		System.out.println("Discovering BLE Devices ...");
		discoverBLEDevices();

		// wait until the TWX51 Device was found
		while (txw51Device == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		// pause discovery
		stopBLEDeviceDiscovery();

		// Connect TWX51
		System.out.println("Connecting TWX51 ...");
		connectTwx51();

		while (!twx51Connected) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		System.out.println("Discovering Services on TWX51 ...");
		discoverServices();

		// Show the Services
		while (!servicesDiscovered) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		// initialize SMING
		initializeMeasuring();
		System.out.println("SMING setup completed.");
	}

	/**
	 * Connect the BLED112.
	 */
	private void connectBLED112(String serialPort) {

		if (serialPort == null) {
			serialPort = BLED112.selectSerialPort();
		}

		port = BLED112.connectSerial(serialPort);
		if (port != null) {
			try {
				System.out.println("Connected on " + port);

				bgapi = new BGAPI(new BGAPITransport(port.getInputStream(), port.getOutputStream()));
				bgapi.addListener(this);
				Thread.sleep(250);
				bgapi.send_system_get_info();

				if (debug)
					bgapi.getLowLevelDriver().addListener(logger);

			} catch (Exception ex) {
				Logger.getLogger(Txw51ServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
				System.out.println("Exception while connecting to " + port);
			}
		} else {
			System.out.println("Not Connected.");
		}
	}

	/**
	 * Response for {@link BGAPI#send_system_get_info()}
	 */
	public void receive_system_get_info(int major, int minor, int patch, int build, int ll_version,
			int protocol_version, int hw) {

		System.out.println("Connected. BLED112:" + major + "." + minor + "." + patch + " (" + build + ") " + "ll="
				+ ll_version + " hw=" + hw);

		bled112Found = true;
	}

	/**
	 * Discover BLE Devices.
	 */
	private void discoverBLEDevices() {
		devList.clear();
		bgapi.send_gap_set_scan_parameters(10, 250, 1);
		// bgapi.send_gap_set_scan_parameters(0xC8, 0xC8, 0);
		bgapi.send_gap_discover(1);
	}

	/**
	 * Response for {@link Txw51ServiceImpl#discoverBLEDevices()}
	 */
	public void receive_gap_scan_response(int rssi, int packet_type, BDAddr sender, int address_type, int bond,
			byte[] data) {
		BLEDevice device = devList.getFromAddress(sender.toString());
		if (device == null) {
			device = new BLEDevice(sender.toString());
			devList.add(device);
			System.out.println("Create device: " + device.toString());
		}

		String name = new String(data).trim();
		if (device.getName().length() < name.length()) {
			device.setName(name);
			// The twx51 device was found
			if (TXW51_NAME.equals(device.getName())) {
				txw51Device = device;
			}
		}
		device.setRssi(rssi);
		devList.changed(device);
	}

	private void disconnectBLED112() {
		if (bgapi != null) {
			bgapi.removeListener(this);
			bgapi.getLowLevelDriver().removeListener(logger);
			bgapi.send_system_reset(0);
			bgapi.disconnect();
		}
		if (port != null) {
			port.close();
		}
		bgapi = null;
		port = null;
		System.out.println("Disconnected.");
	}

	/**
	 * Connect the TWX51.
	 */
	private void connectTwx51() {

		BLEDevice device = (BLEDevice) txw51Device;
		if (device == null) {
			throw new Txw51Exception("TWX51 Device can not be connected. Run 'discoverBLEDevices()' first.");
		}

		// FIXME
		// bgapi.send_gap_connect_direct(BDAddr.fromString(device.getAddress()),
		// 1, 0x3C, 0x3C, 0x64, 0);
		bgapi.send_gap_connect_direct(BDAddr.fromString(device.getAddress()), 1, 60, 76, 100, 9);
	}

	/**
	 * Response for {@link BGAPI#send_gap_connect_direct()}.
	 */
	public void receive_gap_connect_direct(int result, int connection_handle) {
		twx51Connected = true;
	}

	/**
	 * Discover BLE Services.
	 */
	private void discoverServices() {
		discoveryState = SERVICES;
		bgapi.send_attclient_read_by_group_type(connection, 0x0001, 0xFFFF, new byte[] { 0x00, 0x28 });
	}

	/**
	 * Response for {@link BGAPI#send_attclient_read_by_group_type()}
	 */
	public void receive_attclient_group_found(int connection, int start, int end, byte[] uuid) {
		if (txw51Device != null) {
			BLEService srv = new BLEService(uuid, start, end);
			txw51Device.getServices().put(GattHelper.getUuidString(srv.getUuid()), srv);
		}
	}

	/**
	 * Response for {@link BGAPI#send_attclient_read_by_group_type()}
	 */
	public void receive_attclient_procedure_completed(int connection, int result, int chrhandle) {
		if (discoveryState != IDLE && txw51Device != null) {
			// services have been discovered
			if (discoveryState == SERVICES) {
				discoveryIt = txw51Device.getServices().values().iterator();
				discoveryState = ATTRIBUTES;
			}
			if (discoveryState == ATTRIBUTES) {
				if (discoveryIt.hasNext()) {
					discoverySrv = discoveryIt.next();
					bgapi.send_attclient_find_information(connection, discoverySrv.getStart(), discoverySrv.getEnd());
				}
				// Discovery is done
				else {
					System.out.println("Discovery completed.");
					discoveryState = IDLE;
					servicesDiscovered = true;
				}
			}
		}
		if (result != 0) {
			System.err.println("ERROR: Attribute Procedure Completed with error code 0x" + Integer.toHexString(result));
		}
	}

	/**
	 * Response for {@link BGAPI#send_attclient_find_information()}
	 */
	public void receive_attclient_find_information_found(int connection, int chrhandle, byte[] uuid) {
		if (discoveryState == ATTRIBUTES && discoverySrv != null) {

			BLEAttribute att = new BLEAttribute(uuid, chrhandle);
			discoverySrv.getAttributes().add(att);

			// store the handles for the TXW51 Device
			String uuidString = att.getUidAsString();
			switch (uuidString) {
			case Txw51Gatt.DEVICE_INFO_SERVICE:
				handles.put(Txw51Gatt.DEVICE_INFO_SERVICE, chrhandle);
				break;
			case Txw51Gatt.DEVICE_INFO_CHAR_MANUFACTURER:
				handles.put(Txw51Gatt.DEVICE_INFO_CHAR_MANUFACTURER, chrhandle);
				break;
			case Txw51Gatt.DEVICE_INFO_CHAR_MODEL:
				handles.put(Txw51Gatt.DEVICE_INFO_CHAR_MODEL, chrhandle);
				break;
			case Txw51Gatt.DEVICE_INFO_CHAR_SERIAL:
				handles.put(Txw51Gatt.DEVICE_INFO_CHAR_SERIAL, chrhandle);
				break;
			case Txw51Gatt.DEVICE_INFO_CHAR_HW_REV:
				handles.put(Txw51Gatt.DEVICE_INFO_CHAR_HW_REV, chrhandle);
				break;
			case Txw51Gatt.DEVICE_INFO_CHAR_FW_REV:
				handles.put(Txw51Gatt.DEVICE_INFO_CHAR_FW_REV, chrhandle);
				break;
			case Txw51Gatt.DEVICE_INFO_CHAR_DEVICE_NAME:
				handles.put(Txw51Gatt.DEVICE_INFO_CHAR_DEVICE_NAME, chrhandle);
				break;
			case Txw51Gatt.DEVICE_INFO_CHAR_SAVE_VALUES:
				handles.put(Txw51Gatt.DEVICE_INFO_CHAR_SAVE_VALUES, chrhandle);
				break;
			case Txw51Gatt.LSM330_SERVICE:
				handles.put(Txw51Gatt.LSM330_SERVICE, chrhandle);
				break;
			case Txw51Gatt.LSM330_CHAR_ACC_EN:
				handles.put(Txw51Gatt.LSM330_CHAR_ACC_EN, chrhandle);
				break;
			case Txw51Gatt.LSM330_CHAR_GYRO_EN:
				handles.put(Txw51Gatt.LSM330_CHAR_GYRO_EN, chrhandle);
				break;
			case Txw51Gatt.LSM330_CHAR_TEMP_SAMPLE:
				handles.put(Txw51Gatt.LSM330_CHAR_TEMP_SAMPLE, chrhandle);
				break;
			case Txw51Gatt.LSM330_CHAR_ACC_FSCALE:
				handles.put(Txw51Gatt.LSM330_CHAR_ACC_FSCALE, chrhandle);
				break;
			case Txw51Gatt.LSM330_CHAR_GYRO_FSCALE:
				handles.put(Txw51Gatt.LSM330_CHAR_GYRO_FSCALE, chrhandle);
				break;
			case Txw51Gatt.LSM330_CHAR_ACC_ODR:
				handles.put(Txw51Gatt.LSM330_CHAR_ACC_ODR, chrhandle);
				break;
			case Txw51Gatt.LSM330_CHAR_GYRO_ODR:
				handles.put(Txw51Gatt.LSM330_CHAR_GYRO_ODR, chrhandle);
				break;
			case Txw51Gatt.LSM330_CHAR_TRIGGER_VAL:
				handles.put(Txw51Gatt.LSM330_CHAR_TRIGGER_VAL, chrhandle);
				break;
			case Txw51Gatt.LSM330_CHAR_TRIGGER_AXIS:
				handles.put(Txw51Gatt.LSM330_CHAR_TRIGGER_AXIS, chrhandle);
				break;
			case Txw51Gatt.MEASURE_SERVICE:
				handles.put(Txw51Gatt.MEASURE_SERVICE, chrhandle);
				break;
			case Txw51Gatt.MEASURE_CHAR_START:
				handles.put(Txw51Gatt.MEASURE_CHAR_START, chrhandle);
				break;
			case Txw51Gatt.MEASURE_CHAR_STOP:
				handles.put(Txw51Gatt.MEASURE_CHAR_STOP, chrhandle);
				break;
			case Txw51Gatt.MEASURE_CHAR_DURATION:
				handles.put(Txw51Gatt.MEASURE_CHAR_DURATION, chrhandle);
				break;
			case Txw51Gatt.MEASURE_CHAR_DATASTREAM:
				handles.put(Txw51Gatt.MEASURE_CHAR_DATASTREAM, chrhandle);
				break;
			default:
				break;
			}

			// CCID-Handle 0x2902:
			if ("0x2902".equals(att.getUuidString())) {
				System.out.println("CCID-Handle found: " + chrhandle);
				ccidHandle = chrhandle;
			}
		}
	}

	private void initializeMeasuring() {
		System.out.println("Initialize measuring...");
		writeAttribute(handles.get(Txw51Gatt.LSM330_CHAR_GYRO_EN).intValue(), "1");
		writeAttribute(handles.get(Txw51Gatt.LSM330_CHAR_ACC_EN).intValue(), "1");
		if (ccidHandle == null) {
			throw new Txw51Exception("CCID-Handle was not set!");
		}
		writeAttribute(ccidHandle, "0001");
		writeAttribute(handles.get(Txw51Gatt.MEASURE_CHAR_START).intValue(), "1");
	}

	public void listServices() {

		StringBuilder sb = new StringBuilder();

		for (BLEService service : txw51Device.getServices().values()) {

			String serviceGattName = GattHelper.lookupGattName(service.getUuid());
			sb.append("\n" + serviceGattName);

			// attributes
			for (BLEAttribute a : service.getAttributes()) {
				sb.append("\n\t" + a.getHandle() + ": " + GattHelper.lookupGattName(a.getUuid()));
			}
		}
		System.out.println(sb.toString());
	}

	private void readAllAttributes() {

		for (Entry<String, Integer> entry : handles.entrySet()) {
			System.out.println("\nInvoking:\n" + Txw51Gatt.lookup(entry.getKey(), "[ not defined ]"));
			readAttribute(entry.getValue());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void stopBLEDeviceDiscovery() {
		bgapi.send_gap_end_procedure();
	}

	private void disconnectTwx51() {
		txw51Device = null;
		if (connection >= 0) {
			bgapi.send_connection_disconnect(connection);
		}
		System.out.println("Disconnected.");
	}

	private void refresh() {
		if (connection < 0) {
			System.out.println("Not Connected.");
		} else {
			bgapi.send_connection_get_status(connection);
		}
	}

	private void subscribeAll() {
		// bgapi.send_attclient_attribute_write(connection, 0x24, new
		// byte[]{0x0B, 0x10} );
		// bgapi.send_attclient_attribute_write(connection, 0x20, new
		// byte[]{0x03});
		// bgapi.send_attributes_read(0x24, 0);
		bgapi.send_attclient_read_by_handle(connection, 0x24);
	}

	/**
	 * Read an attribute by its handle
	 */
	public void readAttribute(int chrhandle) {
		bgapi.send_attclient_read_by_handle(connection, chrhandle);
	}

	private void writeAttribute(int handle, String value) {
		bgapi.send_attclient_attribute_write(connection, handle, ByteUtils.bytesFromString(value));
	}

	// Callbacks for class system (index = 0)

	// Callbacks for class attributes (index = 2)
	public void receive_attributes_read(int handle, int offset, int result, byte[] value) {
		System.out.println("\n receive_attributes_read \n" + Integer.toHexString(result) + " val = "
				+ bytesToString(value));
	}

	public void receive_attributes_read_type(int handle, int result, byte[] value) {
		System.out.println("receive_attributes_read_type: " + result + ", " + bytesToString(value));
	}

	public void receive_attributes_value(int connection, int reason, int handle, int offset, byte[] value) {
		System.out.println("Attribute Value att=" + Integer.toHexString(handle) + " val = " + bytesToString(value));
	}

	// Callbacks for class attclient (index = 4)
	public void receive_attclient_attribute_value(int connection, int atthandle, int type, byte[] value) {
		System.out.println("Attclient Value atthandle=" + Integer.toHexString(atthandle) + ", type=" + type
				+ ", val = " + bytesToString(value));
	}

	// Callbacks for class connection (index = 3)
	public void receive_connection_status(int conn, int flags, BDAddr address, int address_type, int conn_interval,
			int timeout, int latency, int bonding) {

		System.out.println("[" + address.toString() + "] Conn = " + conn + " Flags = " + flags);

		if (flags != 0) {
			txw51Device = devList.getFromAddress(address.toString());
			this.connection = conn;
		} else {
			System.out.println("Connection lost!");
			connection = -1;
			txw51Device = null;
		}
	}

}
