package org.linphone.myactivity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.util.Log;
import android.view.View;
import android.view.Window;
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

public class InfoActivityMain extends Activity {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_info_main);
        try {

            getAbout();
            tenlienhe = (TextView) findViewById(R.id.tv_tenlienhe);
            diachi = (TextView) findViewById(R.id.tv_diachi);
            dienthoai1 = (TextView) findViewById(R.id.tv_dienthoai1);
            dienthoai2 = (TextView) findViewById(R.id.tv_dienthoai2);
            hotline = (TextView) findViewById(R.id.tv_hotline);
            cskh = (TextView) findViewById(R.id.tv_cskh);
            website = (TextView) findViewById(R.id.tv_website);
            email = (TextView) findViewById(R.id.tv_email);
        } catch (Exception e) {
            Log.d("Info", "Exception: " + e);
        }
    }

    public void setDataNotEmpty(AboutRespon aboutRespon) {
        try {
            if (aboutRespon.getData().getTenlienhe().length() != 0) {
                tenlienhe.setText(aboutRespon.getData().getTenlienhe());
                tenlienhe.setVisibility(View.VISIBLE);
            } else tenlienhe.setVisibility(View.GONE);
        } catch (Exception e) {

        }

        try {
            if (aboutRespon.getData().getDiachi().length() != 0) {
                diachi.setText(aboutRespon.getData().getDiachi());
                diachi.setVisibility(View.VISIBLE);
            } else diachi.setVisibility(View.GONE);
        } catch (Exception e) {

        }

        try {
            if (aboutRespon.getData().getEmail().length() != 0) {
                email.setText(aboutRespon.getData().getEmail());
                email.setVisibility(View.VISIBLE);
            } else email.setVisibility(View.GONE);
        } catch (Exception e) {

        }

        try {
            if (aboutRespon.getData().getHotline().length() != 0) {
                hotline.setText(aboutRespon.getData().getHotline());
                hotline.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            } else hotline.setVisibility(View.GONE);
        } catch (Exception e) {

        }

        try {
            if (aboutRespon.getData().getDienthoai1().length() != 0) {
                dienthoai1.setText(aboutRespon.getData().getDienthoai1());
                dienthoai1.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            } else dienthoai1.setVisibility(View.GONE);
        } catch (Exception e) {

        }

        try {
            if (aboutRespon.getData().getDienthoai2().length() != 0) {
                dienthoai2.setText(aboutRespon.getData().getDienthoai2());
                dienthoai2.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            } else dienthoai2.setVisibility(View.GONE);
        } catch (Exception e) {

        }

        try {

            if (aboutRespon.getData().getCskh().length() != 0) {
                cskh.setText(aboutRespon.getData().getCskh());
                cskh.setVisibility(View.VISIBLE);
            } else cskh.setVisibility(View.GONE);
        } catch (Exception e) {

        }

        try {
            if (aboutRespon.getData().getWebsite().length() != 0) {
                website.setText(aboutRespon.getData().getWebsite());
                website.setVisibility(View.VISIBLE);
                StringUltils.getInstance().removeUnderlines((Spannable) hotline.getText());
            } else website.setVisibility(View.GONE);
        } catch (Exception e) {

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
                            DbContext.getInstance().setAboutRespon(aboutRespon, InfoActivityMain.this);
                            setDataNotEmpty(DbContext.getInstance().getAboutRespon(InfoActivityMain.this));
                        } catch (Exception e) {

                        }

                    } else {
                        Toast.makeText(InfoActivityMain.this, "Lấy thông tin thất bại!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AboutRespon> call, Throwable t) {
                    Toast.makeText(InfoActivityMain.this, "Lỗi! Không thể kết nối tới máy chủ, vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                    Log.d("LoggerInterceptor", "onFailure: " + t.toString());
                    Log.d("LoggerInterceptor", "onFailure: " + call.toString());
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e);
        }
    }
}

