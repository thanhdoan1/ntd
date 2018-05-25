package com.nguyenanh.q2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private EditText mount;
    private Spinner fromCast;
    private Spinner toCast;
    private EditText output;
    private Button convert;
    public static InputStream is = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mount = findViewById(R.id.mount);
        fromCast = findViewById(R.id.fromcast);
        toCast = findViewById(R.id.tocast);
        output = findViewById(R.id.output);
        convert = findViewById(R.id.convert);


        getAdressF("https://free.currencyconverterapi.com/api/v5/currencies").subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                ArrayList<String> listCast = new ArrayList<>();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONObject js = jsonObject.getJSONObject("results");

                    for (Iterator i = js.keys(); i.hasNext(); ) {
                        listCast.add(String.valueOf(i.next()));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(),android.R.layout.simple_spinner_item,listCast);
                fromCast.setAdapter(adapter);
                toCast.setAdapter(adapter);
            }
        });


        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output.setText("");
                final String from = fromCast.getSelectedItem().toString();
                final String to = toCast.getSelectedItem().toString();
                if (!mount.getText().toString().equals("")) {
                    if (!from.equals("ALL") && !to.equals("ALL")) {
                        output.setText("Waiting...");
                        getAdressF("https://free.currencyconverterapi.com/api/v5/convert?q=" + from + "_" + to + "&compact=y").subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(String s) {
                                try {
                                    JSONObject jsonObject = new JSONObject(s);
                                    String js = jsonObject.getJSONObject(from + "_" + to).getString("val");
                                    Double out = Double.parseDouble(mount.getText().toString()) * Double.parseDouble(js);
                                    output.setText(out.toString() + "   " + to);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Vui long chon loai tien quy doi", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Vui long nhap so tien", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public static Observable<String> getAdressF(final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                StringBuilder builder = new StringBuilder();
                try {

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = client.newCall(request).execute();

                    is = response.body().source().inputStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    subscriber.onNext(builder.toString());
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
