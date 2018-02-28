package org.linphone.ui;

/*
CallButton.java
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

import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.R;
import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneProxyConfig;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.Toast;

public class CallButton extends ImageView implements OnClickListener, AddressAware {

	private AddressText mAddress;
	private String TAG="CallButton";

	public void setAddressWidget(AddressText a) { mAddress = a; }

	public void setExternalClickListener(OnClickListener e) { setOnClickListener(e); }
	public void resetClickListener() { setOnClickListener(this); }

	public CallButton(Context context, AttributeSet attrs) {

		super(context, attrs);
		setOnClickListener(this);
	}

	public void onClick(View v) {
		AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
		alphaAnimation.setFillAfter(true);
		alphaAnimation.setDuration(10);
		this.startAnimation(alphaAnimation);
		Log.d(TAG, "onClickAddress: "+mAddress.getText().toString());
		AddressText add = new AddressText(getContext(),null) ;
		add.setText(mAddress.getText());
		String phoneNumber = add.getText().toString();
		if (phoneNumber.contains("+84")) {
			phoneNumber = "0" + phoneNumber.substring(3);
			add.setText(phoneNumber);
			Log.d(TAG, "onClickAddress: "+add.getText().toString());
		}

		try {
			if (!LinphoneManager.getInstance().acceptCallIfIncomingPending()) {
				Log.d(TAG, "onClick: 69");
				if (add.getText().length() > 0) {
					LinphoneManager.getInstance().newOutgoingCall(add);
				} else {

					if (LinphonePreferences.instance().isBisFeatureEnabled()) {
						Log.d(TAG, "onClick: 75");
						LinphoneCallLog[] logs = LinphoneManager.getLc().getCallLogs();
						LinphoneCallLog log = null;
						for (LinphoneCallLog l : logs) {
							if (l.getDirection() == CallDirection.Outgoing) {
								log = l;
								break;
							}
						}
						if (log == null) {
							return;
						}
						Log.d(TAG, "onClick: 87");
						LinphoneProxyConfig lpc = LinphoneManager.getLc().getDefaultProxyConfig();
						if (lpc != null && log.getTo().getDomain().equals(lpc.getDomain())) {
							Log.d(TAG, "onClick: 90");
							mAddress.setText(log.getTo().getUserName());
						} else {
							Log.d(TAG, "onClick: 93");
							mAddress.setText(log.getTo().asStringUriOnly());
						}
						mAddress.setSelection(mAddress.getText().toString().length());
						mAddress.setDisplayedName(log.getTo().getDisplayName());
					}
				}
			}
		} catch (LinphoneCoreException e) {
			LinphoneManager.getInstance().terminateCall();
			onWrongDestinationAddress();
		}
	}

	protected void onWrongDestinationAddress() {
		Log.d(TAG, "onWrongDestinationAddress: ");
		Toast.makeText(getContext()
				,String.format(getResources().getString(R.string.warning_wrong_destination_address),mAddress.getText().toString())
				,Toast.LENGTH_LONG).show();
	}
}
