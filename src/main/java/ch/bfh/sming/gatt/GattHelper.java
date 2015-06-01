package ch.bfh.sming.gatt;

public class GattHelper {

	public static String lookupGattName(byte[] uuid) {
		String result = "";
		for (int i = 0; i < uuid.length; i++) {
			result = String.format("%02X", uuid[i]) + result;
		}

		return Txw51Gatt.lookup(result.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"),
				getUuidString(uuid));
	}

	public static String getUuidString(byte[] uuid) {
		String result = "";
		for (int i = 0; i < uuid.length; i++) {
			result = String.format("%02X", uuid[i]) + result;
		}
		result = "0x" + result;
		return result;
	}
}
