package org.linphone;

/*
HistoryDetailFragment.java
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

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.database.DbContext;
import org.linphone.mediastream.Log;
import org.linphone.ultils.ContactUltils;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class HistoryDetailFragment extends Fragment implements OnClickListener {
	private ImageView dialBack, chat, addToContacts, goToContact, back;
	private View view;
	private ListView lvDetailHistory;
	private ImageView contactPicture, callDirection;
	private TextView tvTitle, contactName, contactAddress, time, date, statusCall;
	private String sipUri, displayName, pictureUri;
	private LinphoneContact contact;
	private String TAG = "HistoryDetailFragment";
	private LayoutInflater mInflater;
	private ArrayList<MyCallLogs.CallLog> listCallogs;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		sipUri = getArguments().getString("SipUri");
		displayName = getArguments().getString("DisplayName");
		pictureUri = getArguments().getString("PictureUri");
		android.util.Log.d(TAG, "onCreateView: " + displayName);
		String status = getArguments().getString("CallStatus");
		String callTime = getArguments().getString("CallTime");
		String callDate = getArguments().getString("CallDate");
		mInflater = inflater;

		view = inflater.inflate(R.layout.history_detail, container, false);
		lvDetailHistory = view.findViewById(R.id.lv_history_detail);
		addAllCallByAddress();
		HistoryDetailAdapter historyDetailAdapter = new HistoryDetailAdapter(getActivity());
		lvDetailHistory.setAdapter(historyDetailAdapter);
		((BaseAdapter) lvDetailHistory.getAdapter()).notifyDataSetChanged();

		tvTitle = view.findViewById(R.id.tv_title);
		tvTitle.setText(displayName);

		dialBack = (ImageView) view.findViewById(R.id.call);
		dialBack.setOnClickListener(this);

		back = (ImageView) view.findViewById(R.id.back);
//		if(getResources().getBoolean(R.bool.isTablet)){
//			back.setVisibility(View.INVISIBLE);
//		} else {
		back.setOnClickListener(this);
//		}

		chat = (ImageView) view.findViewById(R.id.chat);
		chat.setOnClickListener(this);
		if (getResources().getBoolean(R.bool.disable_chat))
			view.findViewById(R.id.chat).setVisibility(View.GONE);

		addToContacts = (ImageView) view.findViewById(R.id.add_contact);
		addToContacts.setOnClickListener(this);

		goToContact = (ImageView) view.findViewById(R.id.goto_contact);
		goToContact.setOnClickListener(this);
		tvTitle = view.findViewById(R.id.tv_title);
		contactPicture = (ImageView) view.findViewById(R.id.contact_picture);

		contactName = (TextView) view.findViewById(R.id.contact_name);
		contactAddress = (TextView) view.findViewById(R.id.contact_address);
		statusCall = (TextView) view.findViewById(R.id.status_call);
		callDirection = (ImageView) view.findViewById(R.id.direction);

		time = (TextView) view.findViewById(R.id.time);
		date = (TextView) view.findViewById(R.id.date);

		displayHistory(status, callTime, callDate);

		return view;
	}

	public void addAllCallByAddress() {
		listCallogs = new ArrayList<>();
		for (MyCallLogs.CallLog c : DbContext.getInstance().getMyCallLogs(getActivity()).getCallLogs()) {
			if (c.getPhoneNumber().equals(sipUri)) listCallogs.add(c);
		}
		android.util.Log.d(TAG, "addAllCallByAddress: ");
	}
	private void displayHistory(String status, String callTime, String callDate) {
		if (status.equals(getResources().getString(R.string.missed))) {
			statusCall.setText("Cuộc gọi nhỡ");
			callDirection.setImageResource(R.drawable.call_missed);
		} else if (status.equals(getResources().getString(R.string.incoming))) {
			statusCall.setText("Cuộc gọi đến");
			callDirection.setImageResource(R.drawable.call_incoming);
		} else if (status.equals(getResources().getString(R.string.outgoing))) {
			statusCall.setText("Cuộc gọi đi");
			callDirection.setImageResource(R.drawable.call_outgoing);
		} else if (status.equals(getResources().getString(R.string.busy))) {
			statusCall.setText("Máy bận");
			callDirection.setImageResource(R.drawable.detail_busy_phone);
		} else if (status.equals(getResources().getString(R.string.offline))) {
			statusCall.setText("Offline");
			callDirection.setImageResource(R.drawable.offline);
		}

		time.setText(callTime == null ? "" : callTime);
		Long longDate = Long.parseLong(callDate);
		String datetime = LinphoneUtils.timestampToHumanDate(getActivity(), longDate, getString(R.string.history_detail_date_format));

		android.util.Log.d(TAG, "displayHistory: " + datetime);
		date.setText(datetime);

		LinphoneAddress lAddress = null;
		try {
			lAddress = LinphoneCoreFactory.instance().createLinphoneAddress(sipUri);
		} catch (LinphoneCoreException e) {
			Log.e(e);
		}

		if (lAddress != null) {
			android.util.Log.d(TAG, "displayHistory: 114");
			contactAddress.setText(lAddress.getUserName());
			contact = ContactsManager.getInstance().findContactFromAddress(lAddress);
			if (contact != null) {
				android.util.Log.d(TAG, "displayHistory: 119");
				contactName.setText(displayName);
				LinphoneUtils.setImagePictureFromUri(view.getContext(),contactPicture,contact.getPhotoUri(),contact.getThumbnailUri());
//				addToContacts.setVisibility(View.GONE);
				goToContact.setVisibility(View.VISIBLE);
			} else {
				contactName.setText(displayName == null ? "" : displayName);
				Bitmap imageBitmap = BitmapFactory.decodeResource(LinphoneService.instance().getResources(), R.drawable.avatar);
				contactPicture.setImageBitmap(imageBitmap);
//				addToContacts.setVisibility(View.VISIBLE);
				goToContact.setVisibility(View.GONE);
			}
		} else {
			android.util.Log.d(TAG, "displayHistory:" + sipUri);
			contactAddress.setText(sipUri);
			contactName.setText(displayName == null ? LinphoneUtils.getAddressDisplayName(sipUri) : displayName);
		}
	}

	public void changeDisplayedHistory(String sipUri, String displayName, String pictureUri, String status, String callTime, String callDate) {
		if (displayName == null ) {
			displayName = LinphoneUtils.getUsernameFromAddress(sipUri);
		}

		this.sipUri = sipUri;
		this.displayName = displayName;
		this.pictureUri = pictureUri;
		displayHistory(status, callTime, callDate);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (LinphoneActivity.isInstanciated()) {
			LinphoneActivity.instance().selectMenu(FragmentsAvailable.HISTORY_DETAIL);
			LinphoneActivity.instance().hideTabBar(false);
		}
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.back) {
			getFragmentManager().popBackStackImmediate();
		} if (id == R.id.call) {
			String address = "sip:" + sipUri + "@" + LinphonePreferences.instance().getAccountDomain(0);

			LinphoneActivity.instance().setAddresGoToDialerAndCall(address, displayName, pictureUri == null ? null : Uri.parse(pictureUri));
		} else if (id == R.id.chat) {
			LinphoneActivity.instance().displayChat(sipUri, null, null);
		} else if (id == R.id.add_contact) {
			String uri = sipUri;
			LinphoneAddress addr = null;
			try {
				addr = LinphoneCoreFactory.instance().createLinphoneAddress(sipUri);
				uri = addr.asStringUriOnly();
			} catch (LinphoneCoreException e) {
				Log.e(e);
			}
			if (addr != null && addr.getDisplayName() != null)
				LinphoneActivity.instance().displayContactsForEdition(addr.asStringUriOnly(), addr.getDisplayName());
			else
				LinphoneActivity.instance().displayContactsForEdition(uri);
		} else if (id == R.id.goto_contact) {
			LinphoneActivity.instance().displayContact(contact, false);
		}
	}

	class HistoryDetailAdapter extends BaseAdapter {
		private class ViewHolder {
			TextView tvDate, tvType, tvDuration;
			ImageView imgType;

			public ViewHolder(View view) {
				tvDate = view.findViewById(R.id.date);
				tvDuration = view.findViewById(R.id.duration);
				tvType = view.findViewById(R.id.type);
				imgType = view.findViewById(R.id.icon);
			}
		}

		HistoryDetailAdapter(Context aContext) {

		}

		public int getCount() {
			return listCallogs.size();
		}

		public Object getItem(int position) {
			return listCallogs.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("SimpleDateFormat")
		private String timestampToHumanDate(Calendar cal, MyCallLogs.CallLog callLog) {
			SimpleDateFormat dateFormat;
			String callDate = String.valueOf(callLog.getTime());
			Long longDate = Long.parseLong(callDate);

			String time = LinphoneUtils.timestampToHumanDate(getActivity(), longDate, getString(R.string.history_item_date_format));

			if (isToday(cal)) {
				return getString(R.string.today) + " " + time;
			} else if (isYesterday(cal)) {
				return getString(R.string.yesterday) + " " + time;
			} else {
				dateFormat = new SimpleDateFormat(getResources().getString(R.string.history_item_date_format));
			}

			return dateFormat.format(cal.getTime());
		}

		private boolean isSameDay(Calendar cal1, Calendar cal2) {
			if (cal1 == null || cal2 == null) {
				return false;
			}

			return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
					cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
					cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
		}

		private boolean isToday(Calendar cal) {
			return isSameDay(cal, Calendar.getInstance());
		}

		private boolean isYesterday(Calendar cal) {
			Calendar yesterday = Calendar.getInstance();
			yesterday.roll(Calendar.DAY_OF_MONTH, -1);
			return isSameDay(cal, yesterday);
		}


		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHolder holder = null;

			if (convertView != null) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = mInflater.inflate(R.layout.history_detail_item, parent, false);
				holder = new ViewHolder(view);
				view.setTag(holder);
			}
			MyCallLogs.CallLog callLog = listCallogs.get(position);
			if (callLog.getStatus() == MyCallLogs.CallLog.CUOC_GOI_NHO) {
				holder.tvDuration.setVisibility(View.GONE);
				holder.tvType.setText("Cuộc gọi nhỡ");
				holder.imgType.setImageResource(R.drawable.my_missed_call);
			} else if (callLog.getStatus() == MyCallLogs.CallLog.CUOC_GOI_DEN) {
				holder.tvType.setText("Cuộc gọi đến");
				holder.imgType.setImageResource(R.drawable.my_incoming_call);
			} else if (callLog.getStatus() == MyCallLogs.CallLog.CUOC_GOI_DI) {
				holder.tvType.setText("Cuộc gọi đi");
				holder.imgType.setImageResource(R.drawable.my_outgoing_call);
			} else if (callLog.getStatus() == MyCallLogs.CallLog.MAY_BAN) {
				holder.tvType.setText("Máy bận");
				holder.imgType.setImageResource(R.drawable.my_busy_call);
				holder.tvDuration.setVisibility(View.GONE);
			} else if (callLog.getStatus() == MyCallLogs.CallLog.OFFLINE) {
				holder.tvType.setText("Offline");
				holder.imgType.setImageResource(R.drawable.offline_ext);
				holder.tvDuration.setVisibility(View.GONE);
			}

			String datetime;

			Calendar logTime = Calendar.getInstance();
			logTime.setTimeInMillis(callLog.getTime());
			datetime = timestampToHumanDate(logTime, callLog);
			holder.tvDate.setText(datetime);


			holder.tvDuration.setText(LinphoneActivity.instance.secondsToDisplayableString(callLog.getDuration()));
			return view;
		}
	}
}
