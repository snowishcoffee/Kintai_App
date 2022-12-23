package com.kanrimanagement.kintaikanri.ui.login;

import static android.nfc.NfcAdapter.EXTRA_DATA;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kanrimanagement.kintaikanri.MainActivity;
import com.kanrimanagement.kintaikanri.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 勤怠管理システム：ログイン画面
 */
public class LoginActivity extends AppCompatActivity {

    // 設定
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private EditText etLoginId;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginId = (EditText) findViewById(R.id.login_id);
        etPassword = (EditText) findViewById(R.id.password);

    }

    // ログインボタンクリック処理
    public String checkLogin(View arg0) {

        // ログイン画面で入力された情報をセット
        final String loginId = etLoginId.getText().toString();
        final String password = etPassword.getText().toString();

        // ソフトキーボードを隠す。
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(arg0.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        // ログインID空白チェック
        if(loginId == null || loginId.equals("")) {
            Toast toast = Toast.makeText(LoginActivity.this,"ログインIDが入力されておりません。",Toast.LENGTH_LONG);
            toast.show();
            return "inputError";
        }

        // パスワード空白チェック
        if(password == null || password.equals("")) {
            Toast toast = Toast.makeText(LoginActivity.this,"パスワードが入力されておりません。",Toast.LENGTH_LONG);
            toast.show();
            return "inputError";
        }

        // ログイン処理
        new AsyncLogin().execute(loginId,password);
        return "finish";

    }

    private class AsyncLogin extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(LoginActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //処理中の表示
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // 送信先URL（向き先ローカルAPI）
                url = new URL("http://10.0.2.2:8080/api/login");

            } catch (MalformedURLException e) {

                e.printStackTrace();
                return "exception";
            }
            try {
                // HTTPコネクション情報をセット
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // URLにパラメータ追加
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("loginId", params[0])
                        .appendQueryParameter("password", params[1]);
                String query = builder.build().getEncodedQuery();

                // 送るデータのコネクションを開く
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {

                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                if (response_code == HttpURLConnection.HTTP_OK) {

                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    return(result.toString());

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            // doInBackgroundの戻り値を使用して処理
            pdLoading.dismiss();

            if(result.equalsIgnoreCase("true"))
            {

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                final String loginId = etLoginId.getText().toString();
                intent.putExtra(EXTRA_DATA, loginId);
                startActivity(intent);
                LoginActivity.this.finish();

            }
            else
            {
                // ログイン不可であった場合のメッセージ
                Toast.makeText(LoginActivity.this, "ログインIDもしくはパスワードが間違っているためログインできません。", Toast.LENGTH_SHORT).show();
            }
        }
    }
}