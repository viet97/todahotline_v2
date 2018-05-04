package org.linphone;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.linphone.database.DbContext;
import org.linphone.network.NetContext;
import org.linphone.network.Service;
import org.linphone.network.models.CusContactModel;
import org.linphone.network.models.EditCusContactModel;
import org.linphone.network.models.VoidRespon;

import java.net.URLEncoder;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CusContactsActivity extends Activity implements View.OnClickListener {
    EditText etTenLienHe;
    EditText etSoDienThoai;
    ImageView imgBack, imgDone;
    private String TAG = "CusContactsActivity";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cus_contacts);
        etTenLienHe = findViewById(R.id.et_tenlienhe);
        etSoDienThoai = findViewById(R.id.et_sdt);
        imgBack = findViewById(R.id.back_cus_contact);
        imgDone = findViewById(R.id.complete_add_cus_contact);
        imgDone.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        getPutExtra();

    }

    private void getPutExtra() {
        String tenlienhe = this.getIntent().getStringExtra("tenlienhe");
        String sodienthoai = this.getIntent().getStringExtra("sodienthoai");
        if (tenlienhe != null && sodienthoai != null) {
            etSoDienThoai.setText(sodienthoai);
            etTenLienHe.setText(tenlienhe);
        }

    }

    private boolean isPutExtra() {
        String tenlienhe = this.getIntent().getStringExtra("tenlienhe");
        String sodienthoai = this.getIntent().getStringExtra("sodienthoai");
        int iddanhba = this.getIntent().getIntExtra("iddanhba", 0);
        if (tenlienhe != null && sodienthoai != null && iddanhba != 0) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.d(TAG, "onClick: ");
        if (id == R.id.complete_add_cus_contact) {
            if (!isPutExtra())
                addCusComtactAct();
            else
                editCusContactAct();
        } else if (id == R.id.back_cus_contact) {
            onBackPressed();
        }
    }

    private void editCusContactAct() {
        if (etTenLienHe.getText().toString().equals("") || etSoDienThoai.getText().toString().equals("")) {
            Toast.makeText(this, "Thông tin liên hệ không được bỏ trống", Toast.LENGTH_SHORT).show();
        } else {
            try {
                progressDialog = ProgressDialog.show(CusContactsActivity.this, "", "Đang sửa...", true, false);
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                final EditCusContactModel CusContact = new EditCusContactModel(etTenLienHe.getText().toString(), etSoDienThoai.getText().toString(), this.getIntent().getIntExtra("iddanhba", 0));
                String editCusContact = ow.writeValueAsString(CusContact);

                Log.d(TAG, "onClick: " + editCusContact);
//                addCusContact = addCusContact.replaceAll(" ", "");
                try {
                    editCusContact = URLEncoder.encode(editCusContact);

                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.toString());
                }
                String urlAddCusContact = "/AppSuaDanhBaKhachHang.aspx?idnhanvien=" + DbContext.getInstance().getLoginRespon(CusContactsActivity.this).getData().getIdnhanvien() +
                        "&idct=" + DbContext.getInstance().getLoginRespon(CusContactsActivity.this).getData().getIdct() + "&dulieudanhba=" + editCusContact;
                Log.d(TAG, "onClick: " + urlAddCusContact);
                Service addContacts = NetContext.getInstance().create(Service.class);
                addContacts.addCusContact(urlAddCusContact).enqueue(new Callback<VoidRespon>() {

                    @Override
                    public void onResponse(Call<VoidRespon> call, Response<VoidRespon> response) {
                        Log.d(TAG, "onResponse: " + response);
                        try {
                            progressDialog.cancel();
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }
                        if (response != null) {
                            Log.d(TAG, "onResponse: " + response.body());
                            VoidRespon respon = response.body();

                            if (respon.getStatus()) {
                                Intent intent = new Intent("AddContacts");
                                intent.putExtra("AddContacts", "reloadContacts");
                                sendBroadcast(intent);
                                HashMap<String, String> itemCusContactName = DbContext.getInstance().getListCusContactTodaName(CusContactsActivity.this);

                                itemCusContactName.put(CusContact.getSodienthoai(), CusContact.getTenlienhe());
                                DbContext.getInstance().setListContactTodaName(itemCusContactName, CusContactsActivity.this);
                                try {
                                    Toast.makeText(CusContactsActivity.this,
                                            "Sửa danh bạ thành công",
                                            Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.d(TAG, "Exception: " + e.toString());
                                }
                                onBackPressed();
                            } else {
                                try {
                                    Toast.makeText(CusContactsActivity.this,
                                            getString(R.string.adminstrator_error),
                                            Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.d(TAG, "Exception: " + e.toString());
                                }
                            }
                        } else {
                            Log.d(TAG, "onResponse: " + response.body());
                            try {
                                Toast.makeText(CusContactsActivity.this,
                                        getString(R.string.adminstrator_error),
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.d(TAG, "Exception: " + e.toString());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<VoidRespon> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.toString());
                        try {
                            progressDialog.cancel();
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }
                        try {
                            Toast.makeText(CusContactsActivity.this,
                                    getString(R.string.network_error),
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }
                    }
                });
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e);
            }
        }
    }

    private void addCusComtactAct() {
        if (etTenLienHe.getText().toString().equals("") || etSoDienThoai.getText().toString().equals("")) {
            Toast.makeText(this, "Thông tin liên hệ không được bỏ trống", Toast.LENGTH_SHORT).show();
        } else {
            try {
                progressDialog = ProgressDialog.show(CusContactsActivity.this, "", "Đang Thêm...", true, false);
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                final CusContactModel cusContact = new CusContactModel(etTenLienHe.getText().toString(), etSoDienThoai.getText().toString());
                String addCusContact = ow.writeValueAsString(cusContact);

                Log.d(TAG, "onClick: " + addCusContact);
//                addCusContact = addCusContact.replaceAll(" ", "");
                try {
                    addCusContact = URLEncoder.encode(addCusContact);

                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.toString());
                }
                String urlAddCusContact = "/AppThemDanhBaKhachHang.aspx?idnhanvien=" + DbContext.getInstance().getLoginRespon(CusContactsActivity.this).getData().getIdnhanvien() +
                        "&idct=" + DbContext.getInstance().getLoginRespon(CusContactsActivity.this).getData().getIdct() + "&dulieudanhba=" + addCusContact;
                Log.d(TAG, "onClick: " + urlAddCusContact);
                Service addContacts = NetContext.getInstance().create(Service.class);
                addContacts.addCusContact(urlAddCusContact).enqueue(new Callback<VoidRespon>() {

                    @Override
                    public void onResponse(Call<VoidRespon> call, Response<VoidRespon> response) {
                        Log.d(TAG, "onResponse: " + response);
                        try {
                            progressDialog.cancel();
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }
                        if (response != null) {
                            Log.d(TAG, "onResponse: " + response.body());
                            VoidRespon respon = response.body();

                            if (respon.getStatus()) {
                                Intent intent = new Intent("AddContacts");
                                intent.putExtra("AddContacts", "reloadContacts");
                                sendBroadcast(intent);
                                HashMap<String, String> itemCusContactName = DbContext.getInstance().getListCusContactTodaName(CusContactsActivity.this);

                                itemCusContactName.put(cusContact.getSodienthoai(), cusContact.getTenlienhe());

                                DbContext.getInstance().setListContactTodaName(itemCusContactName, CusContactsActivity.this);
                                try {
                                    Toast.makeText(CusContactsActivity.this,
                                            "Thêm danh bạ thành công",
                                            Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.d(TAG, "Exception: " + e.toString());
                                }
                                onBackPressed();
                            } else {
                                try {
                                    Toast.makeText(CusContactsActivity.this,
                                            getString(R.string.adminstrator_error),
                                            Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.d(TAG, "Exception: " + e.toString());
                                }
                            }
                        } else {
                            Log.d(TAG, "onResponse: " + response.body());
                            try {
                                Toast.makeText(CusContactsActivity.this,
                                        getString(R.string.adminstrator_error),
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.d(TAG, "Exception: " + e.toString());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<VoidRespon> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.toString());
                        try {
                            progressDialog.cancel();
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }
                        try {
                            Toast.makeText(CusContactsActivity.this,
                                    getString(R.string.network_error),
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
                        }
                    }
                });
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e);
            }
        }
    }
}
