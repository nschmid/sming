/**
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingml.bglib;

import java.util.ArrayList;

import javax.swing.AbstractListModel;

/**
 *
 * @author ffl
 */
public class BLEDeviceList extends AbstractListModel<BLEDevice> {

	private static final long serialVersionUID = 1L;

	protected ArrayList<BLEDevice> devices = new ArrayList<BLEDevice>();

	/*
	 * public ArrayList<BLEDevice> getDevices() { return devices; }
	 */

	public void clear() {
		int idx = devices.size() - 1;
		if (idx < 0)
			return;
		devices.clear();
		fireIntervalRemoved(this, 0, idx);
	}

	public void add(BLEDevice d) {
		devices.add(d);
		fireIntervalAdded(this, 0, devices.size() - 1);
	}

	public void changed(BLEDevice d) {
		if (devices.isEmpty())
			return;
		fireContentsChanged(this, 0, getSize() - 1);
	}

	public BLEDevice getFromAddress(String address) {
		for (BLEDevice d : devices) {
			if (d.address.equals(address))
				return d;
		}
		return null;
	}

	public int getSize() {
		return devices.size();
	}

	public BLEDevice getElementAt(int index) {
		return devices.get(index);
	}

}
