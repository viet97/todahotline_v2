package org.linphone;

/*
ContactPickerActivity.java
Copyright (C) 2017  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 *
 * Intercept network state changes and update linphone core through LinphoneManager.
 *
 */
public class NetworkManager extends BroadcastReceiver {

	private String TAG="NetworkManager";

	@Override
	public void onReceive(Context context, Intent intent) {

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Boolean lNoConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,false);
		Log.d(TAG, "onReceive: "+lNoConnectivity);
		if (LinphoneManager.isInstanciated()) {
			LinphoneManager.getInstance().connectivityChanged(cm, lNoConnectivity);
		}
	}

}
