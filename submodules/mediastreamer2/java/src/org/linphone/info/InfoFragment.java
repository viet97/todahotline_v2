package org.linphone.info;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.linphone.R;
import org.linphone.database.DbContext;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.AboutRespon;
import org.linphone.ultils.StringUltils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {




    private String idnv;
    private int idct;
    private String IPSV;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_info, container, false);
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
            Log.d("Info", "Exception: " + e);
        }
        return view;
    }
    public void setDataNotEmpty(AboutRespon aboutRespon){
        try {
            if (aboutRespon.getData().getTenlienhe().length()!=0) {
                tenlienhe.setText(aboutRespon.getData().getTenlienhe());
                tenlienhe.setVisibility(View.VISIBLE);
            }
            else tenlienhe.setVisibility(View.GONE);
        }catch (Exception e){

        }

        try {
            if (aboutRespon.getData().getDiachi().length()!=0) {
                diachi.setText(aboutRespon.getData().getDiachi());
                diachi.setVisibility(View.VISIBLE);
            }
            else diachi.setVisibility(View.GONE);
        }catch (Exception e){

        }

        try {
            if (aboutRespon.getData().getEmail().length()!=0) {
                email.setText(aboutRespon.getData().getEmail());
                email.setVisibility(View.VISIBLE);
            }
            else email.setVisibility(View.GONE);
        }catch (Exception e){

        }

        try {
            if (aboutRespon.getData().getHotline().length()!=0) {
                hotline.setText(aboutRespon.getData().getHotline());
                hotline.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            }
            else hotline.setVisibility(View.GONE);
        }catch (Exception e){

        }

        try {
            if (aboutRespon.getData().getDienthoai1().length()!=0) {
                dienthoai1.setText(aboutRespon.getData().getDienthoai1());
                dienthoai1.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            }
            else dienthoai1.setVisibility(View.GONE);
        }catch (Exception e){

        }

        try {
            if (aboutRespon.getData().getDienthoai2().length()!=0) {
                dienthoai2.setText(aboutRespon.getData().getDienthoai2());
                dienthoai2.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            }
            else dienthoai2.setVisibility(View.GONE);
        }catch (Exception e){

        }

        try {

            if (aboutRespon.getData().getCskh().length()!=0) {
                cskh.setText(aboutRespon.getData().getCskh());
                cskh.setVisibility(View.VISIBLE);
            }
            else cskh.setVisibility(View.GONE);
        }catch (Exception e){

        }

        try {
            if (aboutRespon.getData().getWebsite().length()!=0) {
                website.setText(aboutRespon.getData().getWebsite());
                website.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            }
            else website.setVisibility(View.GONE);
        }catch (Exception e){

        }

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
                        }catch (Exception e){

                        }

                    } else {
                        Toast.makeText(getActivity(), "Lấy thông tin thất bại!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AboutRespon> call, Throwable t) {
                    Toast.makeText(getActivity(), "Lỗi! Không thể kết nối tới máy chủ, vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                    Log.d("LoggerInterceptor", "onFailure: " + t.toString());
                    Log.d("LoggerInterceptor", "onFailure: " + call.toString());
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e);
        }
    }

}
