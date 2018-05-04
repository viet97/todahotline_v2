package org.linphone.network;



import org.linphone.network.models.AboutRespon;
import org.linphone.network.models.ContactResponse;
import org.linphone.network.models.DSCongTyResponse;
import org.linphone.network.models.DetailMessageListResponse;
import org.linphone.network.models.LoginRespon;
import org.linphone.network.models.MessagesListResponse;
import org.linphone.network.models.NonTodaContactsResponse;
import org.linphone.network.models.VoidRespon;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * Created by hanhi on 7/14/2017.
 */

public interface Service {
    @Multipart
    @POST
    Call<VoidRespon> sendSMSWithAttachment(@Url String url, @Part MultipartBody.Part[] file);

    @POST
    Call<VoidRespon> sendSMS(@Url String url);

    @GET
    Call<LoginRespon> login(@Url String url);

    @GET
    Call<MessagesListResponse> getListMessages(@Url String url);

    @GET
    Call<DetailMessageListResponse> getDetailListMessages(@Url String url);

    @GET
    Call<Void> test(@Url String url);

    @GET
    Call<AboutRespon> getAbout(@Url String url);

    @GET
    Call<VoidRespon> doiMatKhau(@Url String url);

    @GET
    Call<VoidRespon> dangxuat(@Url String url);

    @GET
    Call<VoidRespon> xoaDanhBa(@Url String url);

    @GET
    Call<VoidRespon> suaDanhBa(@Url String url);

    @GET
    Call<ContactResponse> getDanhBa(@Url String url);

    @GET
    Call<VoidRespon> addNonTodaDanhBa(@Url String url);

    @GET
    Call<NonTodaContactsResponse> getNonTodaDanhBa(@Url String url);

    @GET
    Call<DSCongTyResponse> getDsCongTy(@Url String url);

    @GET
    Call<VoidRespon> addCusContact(@Url String url);
}
