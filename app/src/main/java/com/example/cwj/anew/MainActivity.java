package com.example.cwj.anew;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String FFYFFY_971226 = "ffyffy971226";
    private EditText name;
    private EditText passwd;
    private EditText verification_code;
    private ImageView code;
    private Button submit_button;
    private TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        getCheckCode();
    }

    private void initView() {
        name = (EditText) findViewById(R.id.name);
        passwd = (EditText) findViewById(R.id.passwd);
        verification_code = (EditText) findViewById(R.id.verification_code);
        code = (ImageView) findViewById(R.id.code);
        submit_button = (Button) findViewById(R.id.submit_button);

        name.setText("04163072");
        passwd.setText(FFYFFY_971226);
        submit_button.setOnClickListener(this);
        output = (TextView) findViewById(R.id.output);
        output.setOnClickListener(this);
        code.setOnClickListener(this);
    }

    public void getCheckCode() {
        /*SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        final String cookie = sharedPreferences.getString("cookie", null);
*/

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        formBodyBuilder.add("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.78 Safari/537.36");
        RequestBody requestBody = formBodyBuilder.build();
        requestBuilder.url("http://222.24.62.120/CheckCode.aspx");
        requestBuilder.post(requestBody);
        final Request request = requestBuilder.build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("cookie", "failed");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    Log.d("checkcode", " success");
                    String cookie = response.headers("Set-Cookie").get(0).split(";")[0].toString();
                    SharedPreferences.Editor userInfo_editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                    userInfo_editor.putString("cookie", cookie);
                    userInfo_editor.apply();
                    final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            code.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {

                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_button:
                submit();
                break;
            case R.id.code:
                getCheckCode();
                break;
        }
    }

    private void submit() {
        // validate
        String nameString = name.getText().toString().trim();
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, "nameString不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String passwdString = passwd.getText().toString().trim();
        if (TextUtils.isEmpty(passwdString)) {
            Toast.makeText(this, "passwdString不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = verification_code.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "code不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        login(nameString, passwdString, code);
    }

    /**
     * __VIEWSTATE:dDwxNTMxMDk5Mzc0Ozs%2BlYSKnsl%2FmKGQ7CKkWFJpv0btUa8%3D
     * txtUserName:04163072
     * Textbox1:ffyffy971226
     * TextBox2:ffyffy971226
     * txtSecretCode:swna
     * RadioButtonList1:%D1%A7%C9%FA
     * Button1:
     * lbLanguage:
     * c:
     * hidsc:
     */
    void login(String name, String password, String code) {
        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        FormBody body = new FormBody.Builder()
                .add("__VIEWSTATE", "dDwxNTMxMDk5Mzc0Ozs+lYSKnsl/mKGQ7CKkWFJpv0btUa8=")
                .add("txtUserName", name)
                .add("Textbox1", name)
                .add("TextBox2", password)
                .add("txtSecretCode", code)
                .add("RadioButtonList1", "%D1%A7%C9%FA")
                .add("Button1", "")
                .add("lbLanguage", "")
                .add("hidPdrs", "")
                .add("c", "")
                .add("hidsc", "")
                .build();
        final String cookie = sharedPreferences.getString("cookie", "");

        Log.d("login", name + " " + password + " " + code + " " + cookie);
        Request request = new Request.Builder()
                .addHeader("cookie", cookie)
                .url("http://222.24.62.120/default2.aspx")
                .post(body)
                .build();

        Log.d("login", request.headers().toString());
        Call call2 = okHttpClient.newCall(request);
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("login", "falied");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Log.d("login", "success");

                final String string = new String(response.body().bytes(), "gb2312");
                Log.d("response", string);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(string.contains("验证码不正确")){
                            Toast.makeText(getApplicationContext(),"验证码不正确",Toast.LENGTH_LONG).show();
                            getCheckCode();
                            return;
                        }

                        output.setText(response.message().toString() + "\n\n"
                                + response.toString() + "\n\n"
                                + response.headers().toString() + "\n\n"
                                + cookie + "\n\n"
                                + string
                        );
                    }

                });
                getTimeTable(cookie);
            }
        });
    }

    void getTimeTable(String cookie) {
        String url = "http://222.24.62.120/xskbcx.aspx?xh=04163072&xm=%B7%EB%B7%BD%F4%E0&gnmkdm=N121603";
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    .header("cookie", cookie)
                    .header("Referer", "http://222.24.62.120/xs_main.aspx?xh=04163072")
                    .header("Host", "222.24.62.120")
                    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.167 Safari/537.36")
                    .header("__EVENTTARGET", "xqd")
                    .header("__EVENTARGUMENT", "")
                    .header("__VIEWSTATE", "dDwtMTgxNTE1MDA0NDt0PDtsPGk8MT47PjtsPHQ8O2w8aTwxPjtpPDI%2BO2k8ND47aTw3PjtpPDk%2BO2k8MTE%2BO2k8MTM%2BO2k8MTU%2BO2k8MjQ%2BO2k8MjY%2BO2k8Mjg%2BO2k8MzA%2BO2k8MzI%2BO2k8MzQ%2BOz47bDx0PHA8cDxsPFRleHQ7PjtsPFxlOz4%2BOz47Oz47dDx0PHA8cDxsPERhdGFUZXh0RmllbGQ7RGF0YVZhbHVlRmllbGQ7PjtsPHhuO3huOz4%2BOz47dDxpPDI%2BO0A8MjAxNy0yMDE4OzIwMTYtMjAxNzs%2BO0A8MjAxNy0yMDE4OzIwMTYtMjAxNzs%2BPjtsPGk8MD47Pj47Oz47dDx0PDs7bDxpPDA%2BOz4%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w85a2m5Y%2B377yaMDQxNjMwNzI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOWnk%2BWQje%2B8muWGr%2BaWuee%2Bvzs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w85a2m6Zmi77ya6K6h566X5py65a2m6ZmiOz4%2BOz47Oz47dDxwPHA8bDxUZXh0Oz47bDzkuJPkuJrvvJrova%2Fku7blt6XnqIs7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOihjOaUv%2BePre%2B8mui9r%2BS7tjE2MDM7Pj47Pjs7Pjt0PDtsPGk8MT47PjtsPHQ8QDA8Ozs7Ozs7Ozs7Oz47Oz47Pj47dDxwPGw8VmlzaWJsZTs%2BO2w8bzxmPjs%2BPjtsPGk8MT47PjtsPHQ8QDA8Ozs7Ozs7Ozs7Oz47Oz47Pj47dDxAMDxwPHA8bDxQYWdlQ291bnQ7XyFJdGVtQ291bnQ7XyFEYXRhU291cmNlSXRlbUNvdW50O0RhdGFLZXlzOz47bDxpPDE%2BO2k8MD47aTwwPjtsPD47Pj47Pjs7Ozs7Ozs7Ozs%2BOzs%2BO3Q8QDA8cDxwPGw8UGFnZUNvdW50O18hSXRlbUNvdW50O18hRGF0YVNvdXJjZUl0ZW1Db3VudDtEYXRhS2V5czs%2BO2w8aTwxPjtpPDI%2BO2k8Mj47bDw%2BOz4%2BOz47Ozs7Ozs7Ozs7PjtsPGk8MD47PjtsPHQ8O2w8aTwxPjtpPDI%2BOz47bDx0PDtsPGk8MD47aTwxPjtpPDI%2BO2k8Mz47aTw0PjtpPDU%2BO2k8Nj47PjtsPHQ8cDxwPGw8VGV4dDs%2BO2w86YeR5bel5a6e5LmgQiA7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOiUoeengOaihTs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w8MS4wOz4%2BOz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwMS0xODs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w8Jm5ic3BcOzs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w8Jm5ic3BcOzs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w8Jm5ic3BcOzs%2BPjs%2BOzs%2BOz4%2BO3Q8O2w8aTwwPjtpPDE%2BO2k8Mj47aTwzPjtpPDQ%2BO2k8NT47aTw2Pjs%2BO2w8dDxwPHA8bDxUZXh0Oz47bDzmlbDmja7nu5PmnoTor77nqIvorr7orqE7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOeOi%2Bilv%2Bm%2BmTs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w8MS4wOz4%2BOz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwMS0xODs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w8Jm5ic3BcOzs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w8Jm5ic3BcOzs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w8Jm5ic3BcOzs%2BPjs%2BOzs%2BOz4%2BOz4%2BOz4%2BO3Q8QDA8cDxwPGw8UGFnZUNvdW50O18hSXRlbUNvdW50O18hRGF0YVNvdXJjZUl0ZW1Db3VudDtEYXRhS2V5czs%2BO2w8aTwxPjtpPDA%2BO2k8MD47bDw%2BOz4%2BOz47Ozs7Ozs7Ozs7Pjs7Pjt0PEAwPHA8cDxsPFBhZ2VDb3VudDtfIUl0ZW1Db3VudDtfIURhdGFTb3VyY2VJdGVtQ291bnQ7RGF0YUtleXM7PjtsPGk8MT47aTwzPjtpPDM%2BO2w8Pjs%2BPjs%2BOzs7Ozs7Ozs7Oz47bDxpPDA%2BOz47bDx0PDtsPGk8MT47aTwyPjtpPDM%2BOz47bDx0PDtsPGk8MD47aTwxPjtpPDI%2BO2k8Mz47aTw0Pjs%2BO2w8dDxwPHA8bDxUZXh0Oz47bDwyMDE3LTIwMTg7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDE7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOWkp%2BWtpueJqeeQhuWunumqjEI7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOWtmeWuh%2BiIqjs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w8Mi4wOz4%2BOz47Oz47Pj47dDw7bDxpPDA%2BO2k8MT47aTwyPjtpPDM%2BO2k8ND47PjtsPHQ8cDxwPGw8VGV4dDs%2BO2w8MjAxNy0yMDE4Oz4%2BOz47Oz47dDxwPHA8bDxUZXh0Oz47bDwxOz4%2BOz47Oz47dDxwPHA8bDxUZXh0Oz47bDzph5Hlt6Xlrp7kuaBCIDs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w86JSh56eA5qKFOz4%2BOz47Oz47dDxwPHA8bDxUZXh0Oz47bDwxLjA7Pj47Pjs7Pjs%2BPjt0PDtsPGk8MD47aTwxPjtpPDI%2BO2k8Mz47aTw0Pjs%2BO2w8dDxwPHA8bDxUZXh0Oz47bDwyMDE3LTIwMTg7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPDE7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOaVsOaNrue7k%2BaehOivvueoi%2BiuvuiuoTs%2BPjs%2BOzs%2BO3Q8cDxwPGw8VGV4dDs%2BO2w8546L6KW%2F6b6ZOz4%2BOz47Oz47dDxwPHA8bDxUZXh0Oz47bDwxLjA7Pj47Pjs7Pjs%2BPjs%2BPjs%2BPjs%2BPjs%2BPjs%2B%2Fo7UWfmfgr%2BZYHLV%2FzpqQQAqKzk%3D")
                    .header("xnd", "2017-2018")
                    .header("xqd", "2")
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (document == null) return;
        final String response = document.text().toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                output.setText(response);
                Log.d("success", response);
            }
        });
    }
}
