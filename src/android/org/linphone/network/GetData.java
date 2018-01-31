package org.linphone.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;

/**
 * Created by hanhi on 11/22/2017.
 */

public class GetData extends AsyncTask<String, Void, ArrayList<String>> {
    private static final String KEYLACHONG = "!lac@hong#media$";
    Context context;

    public GetData(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList doInBackground(String... strings) {
        //cái này nó thực hiện ở trong back ground. khi nào chạy xong nó sẽ gọi hàm onPostExecute ở dưới
        ArrayList<String> str = new ArrayList<>();
        try {
            URL url = new URL(strings[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();

            String inputString;
            while ((inputString = bufferedReader.readLine()) != null) {
                builder.append(inputString);
            }
            JSONObject topLevel = new JSONObject(builder.toString());
            //toplevel là data trả về ở dạng JSONObject
            //lấy status add vào str - str là 1 arraylist
            str.add(topLevel.get("status").toString());
            //check xem status có true hay ko
            if (str.get(0).equals("true")) {
                //get data ra
                Object data1 = topLevel.get("data");
                JSONObject jsonObject = new JSONObject(data1.toString());
                //lấy cái đoạn mã hóa ra
                String user = (String) jsonObject.get("usertoda");
                user = new StringBuilder(user).reverse().toString();
                //add dãy mã hóa vào str
                str.add(user);
            } else {
                Toast.makeText(context, "Sai thông tin tài khoản!", Toast.LENGTH_SHORT).show();
            }
            urlConnection.disconnect();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return str;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        //strings la cai arraylít tra ve o tren han doINBackGround
        byte[] data = new byte[0];
        try {
            data = KEYLACHONG.getBytes("UTF-8");
            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
            try {
                Jws<Claims> claims = Jwts.parser()
                        .setSigningKey(base64)
                        .parseClaimsJws(strings.get(1));
                //3 cai can de dang nhap vao toda
                String user = (String) claims.getBody().get("user");
                String server = (String)claims.getBody().get("server");
                String pass = (String)claims.getBody().get("matkhautoda");
            } catch (MissingClaimException e) {

                // we get here if the required claim is not present

            } catch (IncorrectClaimException e) {

                // we get here if the required claim has the wrong value

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }
}
