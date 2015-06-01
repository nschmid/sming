package ch.bfh.sming.gatt;

import java.util.UUID;

import static java.util.UUID.fromString;

/**
 * Contains all the UUIDs, the UUID strings and the corresponding descriptions
 * of the TXW51 device.
 *
 * @author Daniel Meer
 * @version 1.0, 22.12.2014
 */
public class Txw51Gatt extends StdGatt {
	// UUID strings
	public final static String
        DEVICE_INFO_SERVICE           = "8EDF0100-67E5-DB83-F85B-A1E2AB1C9E7A",
        DEVICE_INFO_CHAR_MANUFACTURER = "8EDF0101-67E5-DB83-F85B-A1E2AB1C9E7A",
        DEVICE_INFO_CHAR_MODEL        = "8EDF0102-67E5-DB83-F85B-A1E2AB1C9E7A",
        DEVICE_INFO_CHAR_SERIAL       = "8EDF0103-67E5-DB83-F85B-A1E2AB1C9E7A",
        DEVICE_INFO_CHAR_HW_REV       = "8EDF0104-67E5-DB83-F85B-A1E2AB1C9E7A",
        DEVICE_INFO_CHAR_FW_REV       = "8EDF0105-67E5-DB83-F85B-A1E2AB1C9E7A",
        DEVICE_INFO_CHAR_DEVICE_NAME  = "8EDF0106-67E5-DB83-F85B-A1E2AB1C9E7A",
        DEVICE_INFO_CHAR_SAVE_VALUES  = "8EDF0107-67E5-DB83-F85B-A1E2AB1C9E7A",

        LSM330_SERVICE           = "8EDF0200-67E5-DB83-F85B-A1E2AB1C9E7A",
        LSM330_CHAR_ACC_EN       = "8EDF0201-67E5-DB83-F85B-A1E2AB1C9E7A",
        LSM330_CHAR_GYRO_EN      = "8EDF0202-67E5-DB83-F85B-A1E2AB1C9E7A",
        LSM330_CHAR_TEMP_SAMPLE  = "8EDF0203-67E5-DB83-F85B-A1E2AB1C9E7A",
        LSM330_CHAR_ACC_FSCALE   = "8EDF0204-67E5-DB83-F85B-A1E2AB1C9E7A",
        LSM330_CHAR_GYRO_FSCALE  = "8EDF0205-67E5-DB83-F85B-A1E2AB1C9E7A",
        LSM330_CHAR_ACC_ODR      = "8EDF0206-67E5-DB83-F85B-A1E2AB1C9E7A",
        LSM330_CHAR_GYRO_ODR     = "8EDF0207-67E5-DB83-F85B-A1E2AB1C9E7A",
        LSM330_CHAR_TRIGGER_VAL  = "8EDF0208-67E5-DB83-F85B-A1E2AB1C9E7A",
        LSM330_CHAR_TRIGGER_AXIS = "8EDF0209-67E5-DB83-F85B-A1E2AB1C9E7A",

        MEASURE_SERVICE         = "8EDF0300-67E5-DB83-F85B-A1E2AB1C9E7A",
        MEASURE_CHAR_START      = "8EDF0301-67E5-DB83-F85B-A1E2AB1C9E7A",
        MEASURE_CHAR_STOP       = "8EDF0302-67E5-DB83-F85B-A1E2AB1C9E7A",
        MEASURE_CHAR_DURATION   = "8EDF0303-67E5-DB83-F85B-A1E2AB1C9E7A",
        MEASURE_CHAR_DATASTREAM = "8EDF0304-67E5-DB83-F85B-A1E2AB1C9E7A";

	// UUID values
	public final static UUID
		UUID_DIS_SERVICE      = fromString(DEVICE_INFO_SERVICE),
        UUID_DIS_MANUFACTURER = fromString(DEVICE_INFO_CHAR_MANUFACTURER),
        UUID_DIS_MODEL        = fromString(DEVICE_INFO_CHAR_MODEL),
        UUID_DIS_SERIAL       = fromString(DEVICE_INFO_CHAR_SERIAL),
        UUID_DIS_HW_REV       = fromString(DEVICE_INFO_CHAR_HW_REV),
        UUID_DIS_FW_REV       = fromString(DEVICE_INFO_CHAR_FW_REV),
        UUID_DIS_DEVICE_NAME  = fromString(DEVICE_INFO_CHAR_DEVICE_NAME),
        UUID_DIS_SAVE_VALUES  = fromString(DEVICE_INFO_CHAR_SAVE_VALUES),

        UUID_LSM330_SERVICE      = fromString(LSM330_SERVICE),
        UUID_LSM330_ACC_EN       = fromString(LSM330_CHAR_ACC_EN),
        UUID_LSM330_GYRO_EN      = fromString(LSM330_CHAR_GYRO_EN),
        UUID_LSM330_TEMP_SAMPLE  = fromString(LSM330_CHAR_TEMP_SAMPLE),
        UUID_LSM330_ACC_FSCALE   = fromString(LSM330_CHAR_ACC_FSCALE),
        UUID_LSM330_GYRO_FSCALE  = fromString(LSM330_CHAR_GYRO_FSCALE),
        UUID_LSM330_ACC_ODR      = fromString(LSM330_CHAR_ACC_ODR),
        UUID_LSM330_GYRO_ODR     = fromString(LSM330_CHAR_GYRO_ODR),
        UUID_LSM330_TRIGGER_VAL  = fromString(LSM330_CHAR_TRIGGER_VAL),
        UUID_LSM330_TRIGGER_AXIS = fromString(LSM330_CHAR_TRIGGER_AXIS),

        UUID_MEASURE_SERVICE    = fromString(MEASURE_SERVICE),
        UUID_MEASURE_START      = fromString(MEASURE_CHAR_START),
        UUID_MEASURE_STOP       = fromString(MEASURE_CHAR_STOP),
        UUID_MEASURE_DURATION   = fromString(MEASURE_CHAR_DURATION),
        UUID_MEASURE_DATASTREAM = fromString(MEASURE_CHAR_DATASTREAM);
	
	// Lookup table for the description of the UUIDs
	static {
        // Device Information Service
        attributes.put(DEVICE_INFO_SERVICE,           "Device Information Service");
        attributes.put(DEVICE_INFO_CHAR_MANUFACTURER, "Device Information Manufacturer");
        attributes.put(DEVICE_INFO_CHAR_MODEL,        "Device Information Model Name");
        attributes.put(DEVICE_INFO_CHAR_SERIAL,       "Device Information Serial Number");
        attributes.put(DEVICE_INFO_CHAR_HW_REV,       "Device Information Hardware Version");
        attributes.put(DEVICE_INFO_CHAR_FW_REV,       "Device Information Firmware Version");
        attributes.put(DEVICE_INFO_CHAR_DEVICE_NAME,  "Device Information Device Name");
        attributes.put(DEVICE_INFO_CHAR_SAVE_VALUES,  "Device Information Save Values");

        // LSM330 Service
        attributes.put(LSM330_SERVICE,           "LSM330 Service");
        attributes.put(LSM330_CHAR_ACC_EN,       "LSM330 Turn on Accel");
        attributes.put(LSM330_CHAR_GYRO_EN,      "LSM330 Turn on Gyro");
        attributes.put(LSM330_CHAR_TEMP_SAMPLE,  "LSM330 Temperature Sample");
        attributes.put(LSM330_CHAR_ACC_FSCALE,   "LSM330 Accel Full-Scale");
        attributes.put(LSM330_CHAR_GYRO_FSCALE,  "LSM330 Gyro Full-Scale");
        attributes.put(LSM330_CHAR_ACC_ODR,      "LSM330 Accel ODR");
        attributes.put(LSM330_CHAR_GYRO_ODR,     "LSM330 Gyro ODR");
        attributes.put(LSM330_CHAR_TRIGGER_VAL,  "LSM330 Trigger Value");
        attributes.put(LSM330_CHAR_TRIGGER_AXIS, "LSM330 Trigger Axis");

        // Measurement Service
        attributes.put(MEASURE_SERVICE,         "Measurement Service");
        attributes.put(MEASURE_CHAR_START,      "Measurement Start");
        attributes.put(MEASURE_CHAR_STOP,       "Measurement Stop");
        attributes.put(MEASURE_CHAR_DURATION,   "Measurement Duration");
        attributes.put(MEASURE_CHAR_DATASTREAM, "Measurement Read Data");
	}
	
	/**
     * Uses the internal lookup table to get the description of a given UUID.
     * Child classes need to implement their own lookup method to update their
     * UUID table. However, they can call this method.
     *
     * @param uuid        The UUID to look up.
     * @param defaultName The default name if no entry could be found.
     * @return The description of the UUID or the default name if no entry could be found.
     */
	public static String lookup(String uuid, String defaultName) {
        return StdGatt.lookup(uuid, defaultName);
    }
}
