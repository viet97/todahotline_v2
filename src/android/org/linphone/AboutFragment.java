package org.linphone;
/*
AboutFragment.java
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

import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.LogCollectionUploadState;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.database.DbContext;
import org.linphone.mediastream.Log;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.AboutRespon;
import org.linphone.ultils.StringUltils;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AboutFragment extends Fragment implements OnClickListener {
	View sendLogButton = null;
	View resetLogButton = null;
	ImageView cancel;
	LinphoneCoreListenerBase mListener;
	private ProgressDialog progress;
	private boolean uploadInProgress;
	TextView tenlienhe;
	TextView diachi;
	TextView dienthoai1;
	TextView dienthoai2;
	TextView hotline;
	TextView cskh;
	TextView website;
	TextView email;
	private String TAG = "InfoFragment";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.about, container, false);
		try {
			getAbout();
			tenlienhe = (TextView) view.findViewById(R.id.tv_tenlienhe);
			diachi = (TextView) view.findViewById(R.id.tv_diachi);
			dienthoai1 = (TextView) view.findViewById(R.id.tv_dienthoai1);
			dienthoai2 = (TextView) view.findViewById(R.id.tv_dienthoai2);
			hotline = (TextView) view.findViewById(R.id.tv_hotline);
			cskh = (TextView) view.findViewById(R.id.tv_cskh);
			website = (TextView) view.findViewById(R.id.tv_website);
			email = (TextView) view.findViewById(R.id.tv_email);
		} catch (Exception e) {
			android.util.Log.d("Info", "Exception: " + e);
		}

		TextView aboutVersion = (TextView) view.findViewById(R.id.about_android_version);
		TextView aboutLiblinphoneVersion = (TextView) view.findViewById(R.id.about_liblinphone_version);
//		aboutLiblinphoneVersion.setText(String.format(getString(R.string.about_liblinphone_version), LinphoneManager.getLc().getVersion()));
		try {
			aboutVersion.setText(String.format(getString(R.string.about_version), getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName));
		} catch (NameNotFoundException e) {
			Log.e(e, "cannot get version name");
		}

		cancel = (ImageView) view.findViewById(R.id.cancel);
		cancel.setOnClickListener(this);

		sendLogButton = view.findViewById(R.id.send_log);
		sendLogButton.setOnClickListener(this);
		sendLogButton.setVisibility(LinphonePreferences.instance().isDebugEnabled() ? View.VISIBLE : View.GONE);

		resetLogButton = view.findViewById(R.id.reset_log);
		resetLogButton.setOnClickListener(this);
		resetLogButton.setVisibility(LinphonePreferences.instance().isDebugEnabled() ? View.VISIBLE : View.GONE);

		mListener = new LinphoneCoreListenerBase() {
			@Override
			public void uploadProgressIndication(LinphoneCore lc, int offset, int total) {
			}

			@Override
			public void uploadStateChanged(LinphoneCore lc, LogCollectionUploadState state, String info) {
				if (state == LogCollectionUploadState.LogCollectionUploadStateInProgress) {
					displayUploadLogsInProgress();
				} else if (state == LogCollectionUploadState.LogCollectionUploadStateDelivered || state == LogCollectionUploadState.LogCollectionUploadStateNotDelivered) {
					uploadInProgress = false;
					if (progress != null) progress.dismiss();
					if (state == LogCollectionUploadState.LogCollectionUploadStateDelivered) {
						sendLogs(LinphoneService.instance().getApplicationContext(), info);
					}
				}
			}
		};

		return view;
	}
	public void setDataNotEmpty(AboutRespon aboutRespon){
		if (aboutRespon.getData().getTenlienhe().length()!=0) {
			tenlienhe.setText(aboutRespon.getData().getTenlienhe());
			tenlienhe.setVisibility(View.VISIBLE);
		}
		else tenlienhe.setVisibility(View.GONE);
		if (aboutRespon.getData().getDiachi().length()!=0) {
			diachi.setText(aboutRespon.getData().getDiachi());
			diachi.setVisibility(View.VISIBLE);
		}
		else diachi.setVisibility(View.GONE);
		if (aboutRespon.getData().getEmail().length()!=0) {
			email.setText(aboutRespon.getData().getEmail());
			email.setVisibility(View.VISIBLE);
		}
		else email.setVisibility(View.GONE);
		if (aboutRespon.getData().getHotline().length()!=0) {
			hotline.setText(aboutRespon.getData().getHotline());
			hotline.setVisibility(View.VISIBLE);
			StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
		}
		else hotline.setVisibility(View.GONE);
		if (aboutRespon.getData().getDienthoai1().length()!=0) {
			dienthoai1.setText(aboutRespon.getData().getDienthoai1());
			dienthoai1.setVisibility(View.VISIBLE);
			StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
		}
		else dienthoai1.setVisibility(View.GONE);
		if (aboutRespon.getData().getDienthoai2().length()!=0) {
			dienthoai2.setText(aboutRespon.getData().getDienthoai2());
			dienthoai2.setVisibility(View.VISIBLE);
			StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
		}
		else dienthoai2.setVisibility(View.GONE);
		if (aboutRespon.getData().getCskh().length()!=0) {
			cskh.setText(aboutRespon.getData().getCskh());
			cskh.setVisibility(View.VISIBLE);
		}
		else cskh.setVisibility(View.GONE);
		if (aboutRespon.getData().getWebsite().length()!=0) {
			website.setText(aboutRespon.getData().getWebsite());
			website.setVisibility(View.VISIBLE);
			StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
		}
		else website.setVisibility(View.GONE);
	}

	public String getURLAbout() {
		String URL = "AppLienHe.aspx";
		return URL;

	}

	public void getAbout() {
		try {
			Service userService = NetContext.instance.create(Service.class);
			userService.getAbout(getURLAbout()).enqueue(new Callback<AboutRespon>() {
				@Override
				public void onResponse(Call<AboutRespon> call, Response<AboutRespon> response) {

					AboutRespon aboutRespon = response.body();
					if (aboutRespon.getStatus()) {
                        try {
                            DbContext.getInstance().setAboutRespon(aboutRespon, getActivity());
                            setDataNotEmpty(DbContext.getInstance().getAboutRespon(getActivity()));
                        } catch (Exception e) {

                        }

					} else {
						Toast.makeText(getActivity(), "Lấy thông tin thất bại!", Toast.LENGTH_LONG).show();
					}
				}

				@Override
				public void onFailure(Call<AboutRespon> call, Throwable t) {
                    try {
                        Toast.makeText(getActivity(), "Lỗi! Không thể kết nối tới máy chủ, vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                        android.util.Log.d("LoggerInterceptor", "onFailure: " + t.toString());
                        android.util.Log.d("LoggerInterceptor", "onFailure: " + call.toString());
                    } catch (Exception e) {

                    }

				}
			});
		} catch (Exception e) {
			android.util.Log.d(TAG, "Exception: " + e);
		}
	}
	private void displayUploadLogsInProgress() {
		if (uploadInProgress) {
			return;
		}
		uploadInProgress = true;

		progress = ProgressDialog.show(LinphoneActivity.instance(), null, null);
		Drawable d = new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.colorE));
		d.setAlpha(200);
		progress.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
		progress.getWindow().setBackgroundDrawable(d);
		progress.setContentView(R.layout.progress_dialog);
		progress.show();
	}

	private void sendLogs(Context context, String info){
		final String appName = context.getString(R.string.app_name);

		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_EMAIL, new String[]{ context.getString(R.string.about_bugreport_email) });
		i.putExtra(Intent.EXTRA_SUBJECT, appName + " Logs");
		i.putExtra(Intent.EXTRA_TEXT, info);
		i.setType("application/zip");

		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Log.e(ex);
		}
	}

	@Override
	public void onPause() {
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.removeListener(mListener);
		}

		super.onPause();
	}

	@Override
	public void onResume() {
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.addListener(mListener);
		}

		if (LinphoneActivity.isInstanciated()) {
			LinphoneActivity.instance().selectMenu(FragmentsAvailable.ABOUT);
			LinphoneActivity.instance().hideTabBar(false);
		}
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (LinphoneActivity.isInstanciated()) {
			LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
			if (v == sendLogButton) {
				if (lc != null) {
					lc.uploadLogCollection();
				}
			} else if (v == resetLogButton) {
				if (lc != null) {
					lc.resetLogCollection();
				}
			} else if (v == cancel) {
				LinphoneActivity.instance().goToDialerFragment();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
