package com.example.skdj.beta1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class FormActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    EditText fullName;
    EditText vehicleno;
    EditText phoneNumber_1;
    EditText phoneNumber_2;
    String vehicleType;
    EditText misc;
    EditText countryCode;
    EditText stateCode;
    String name,vehcno,pno1,pno2,vehctype,Smisc, conCode, stCode, vehType;
    TextView textShow;
    Button updateButton, backButton;
    String country, state;
    int flag=0;
    ProgressDialog mprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        flag=0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);

        updateButton = (Button) findViewById(R.id.updateButton);
        mprogress = new ProgressDialog(this);

        Spinner dropdown_1 = findViewById(R.id.countrySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_1.setAdapter(adapter);
        dropdown_1.setSelection(1);

        dropdown_1.setOnItemSelectedListener(this);

        Spinner dropdown_2 = findViewById(R.id.statesSpinner);
        ArrayAdapter<CharSequence> adapter_2 = ArrayAdapter.createFromResource(this,
                R.array.india_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_2.setAdapter(adapter_2);
        dropdown_2.setSelection(36);

        dropdown_2.setOnItemSelectedListener(this);

        //get the spinner from the xml.
        Spinner dropdown_3 = findViewById(R.id.vehicleType);
        ArrayAdapter<CharSequence> adapter_3 = ArrayAdapter.createFromResource(this,
                R.array.vehicle_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_3.setAdapter(adapter_3);
        //dropdown_2.setSelection(vtype-1);
        dropdown_3.setOnItemSelectedListener(this);


       /* backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FormActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });*/
    }
       public void hero(View v) {

           if(CheckNetwork.isInternetAvailable(FormActivity.this)) //returns true if internet available
           {

               //do something. loadwebview.
               //Toast.makeText(FormActivity.this,"Internet Connection",Toast.LENGTH_LONG).show();
               fullName = (EditText) findViewById(R.id.fullName);
               phoneNumber_1 = (EditText) findViewById(R.id.phoneNumber_1);
               phoneNumber_2 = (EditText) findViewById(R.id.phoneNumber_2);
               vehicleno = (EditText) findViewById(R.id.vehicleNo);
               //vehicleType = (EditText) findViewById(R.id.vehicleType);
               misc = (EditText) findViewById(R.id.misc);

               name= fullName.getText().toString();
               String toAppend = countryCode.getText().toString() + stateCode.getText().toString();
               vehcno= toAppend + vehicleno.getText().toString();
               pno1= phoneNumber_1.getText().toString();
               pno2= phoneNumber_2.getText().toString();
               vehctype= vehicleType;
               Smisc=misc.getText().toString();
               conCode = countryCode.getText().toString();
               stCode = stateCode.getText().toString();

               if (TextUtils.isEmpty(name)) {
                   fullName.setError("You can't leave empty");
                   return;
               }
               else if(TextUtils.isEmpty(pno1))
               {
                   phoneNumber_1.setError("you can't leave empty");
                   return;
               }
               else if(TextUtils.isEmpty(pno2))
               {
                   phoneNumber_2.setError("you can't leave empty");
                   return;
               }
               else if(TextUtils.isEmpty(vehicleno.getText().toString()))
               {
                   vehicleno.setError("You can't leave empty");
                   return;
               }

               else if(pno1.length() != 10)
               {
                   phoneNumber_1.setError("enter 10 digit number");
                   return;
               }
               else if(pno2.length() != 10)
               {
                   phoneNumber_2.setError("enter 10 digit number");
                   return;
               }
               else if(TextUtils.isEmpty(conCode))
               {
                   countryCode.setError("Choose country from top");
                   return;
               }
               else if(TextUtils.isEmpty(stCode))
               {
                   stateCode.setError("Choose state from top");
                   return;
               }


               mprogress.setTitle("Updating User");
               mprogress.setMessage("please wait while registering the user");
               mprogress.setCanceledOnTouchOutside(false);
               mprogress.show();
               Communicate();
           }
           else
           {
               View parentlayout = findViewById(android.R.id.content);
               Snackbar.make(parentlayout, "No internet Connection", Snackbar.LENGTH_INDEFINITE)
                       .setAction("Try Again", new View.OnClickListener() {;
                           @Override
                           public void onClick(View view) {

                               Intent intent=new Intent(getApplicationContext(),FormActivity.class);
                               startActivity(intent);

                           }
                       })
                       .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                       .show();
           }



        }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    @Override
    public void onClick(View v) {

    }

    public void Communicate(){
          new SendPostRequest().execute();
    }
    private class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://192.168.43.29/cgi-bin/Pune/DriverRegister/DriverRegister.out"); // here is your URL path

              //  String tosend= "reg=4uwx&phoneNumber=fdsfdsa&type=fsdfds&name=fdsf&misc=fdsf";
               /* JSONObject postDataParams = new JSONObject();

                postDataParams.put("plateno", vehcno+"_"+pno1);
                postDataParams.put("altno", pno2);
                postDataParams.put("type", vehctype);
                postDataParams.put("name", name);
                postDataParams.put("misc", Smisc);
                Log.e("params",postDataParams.toString());*/
               int vt;
                if(vehicleType.equals("Bike")) {
                    vt = 1;
                }else if(vehicleType.equals("Auto")) {
                    vt = 2;
                }else if(vehicleType.equals("Cab")) {
                    vt= 3;
                }else if(vehicleType.equals("Pink Cab") ){
                    vt = 4;
                }else if(vehicleType.equals("Sedan")) {
                    vt= 5;
                }else if(vehicleType.equals("Truck")) {
                    vt= 6;
                }
                else
                    vt=0;
                String tosend="reg="+vehcno+"_"+pno1+"&phoneNumber="+pno2+"&type="+vt+"&name="+name+"&misc="+Smisc;
                tosend = tosend.replace(" ","+");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(tosend);

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK)

                {   flag=1;

                        InputStream is = null;
                        try {
                            is = conn.getInputStream();
                            int ch;
                            StringBuffer sb = new StringBuffer();
                            while ((ch = is.read()) != -1) {
                                sb.append((char) ch);
                            }
                            return sb.toString();
                        } catch (IOException e) {
                            throw e;
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }
                    }

                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            if(flag==1) {
               SharedPreferences.Editor editor = getSharedPreferences(new MainActivity().MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("name", name);
                editor.putString("vehicleno", vehicleno.getText().toString());
                editor.putString("phoneno1", pno1);
                editor.putString("phoneno2", pno2);
                Log.d("spinner", "in Form post exe"+vehicleType);
                editor.putString("vehicletype", vehicleType);
                editor.putString("misc", Smisc);
                editor.putString("countryCode", conCode);
                editor.putString("stateCode", stCode);

                editor.apply();
                Log.d("updateX",result);

                Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                mprogress.dismiss();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
            else
            {
                Toast.makeText(getApplicationContext(),"error, tryagain",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.countrySpinner:
                country = (String) parent.getItemAtPosition(position);
                countryCode = (EditText) findViewById(R.id.countryCode);
                if(position == 0){
                    countryCode.setText("IND");
                }
                break;
            case R.id.vehicleType:
                vehType = (String) parent.getItemAtPosition(position);
                Log.d("spinner", "in Form listener "+vehType);
                vehicleType=vehType;
                break;
            case R.id.statesSpinner:
                state = (String) parent.getItemAtPosition(position);
                stateCode = (EditText) findViewById(R.id.stateCode);

                switch (position) {
                    case 0: {
                        stateCode.setText("AN");
                        break;
                    }
                    case 1: {
                        stateCode.setText("AP");
                        break;
                    }
                    case 2: {
                        stateCode.setText("AR");
                        break;
                    }
                    case 3: {
                        stateCode.setText("AS");
                        break;
                    }
                    case 4: {
                        stateCode.setText("BR");
                        break;
                    }
                    case 5: {
                        stateCode.setText("CH");
                        break;
                    }
                    case 6: {
                        stateCode.setText("CG");
                        break;
                    }
                    case 7: {
                        stateCode.setText("DN");
                        break;
                    }
                    case 8: {
                        stateCode.setText("DD");
                        break;
                    }
                    case 9: {
                        stateCode.setText("DL");
                        break;
                    }
                    case 10: {
                        stateCode.setText("GA");
                        break;
                    }
                    case 11: {
                        stateCode.setText("GJ");
                        break;
                    }
                    case 12: {
                        stateCode.setText("HR");
                        break;
                    }
                    case 13: {
                        stateCode.setText("HP");
                        break;
                    }
                    case 14: {
                        stateCode.setText("JK");
                        break;
                    }
                    case 15: {
                        stateCode.setText("JH");
                        break;
                    }
                    case 16: {
                        stateCode.setText("KA");
                        break;
                    }
                    case 17: {
                        stateCode.setText("KL");
                        break;
                    }
                    case 18: {
                        stateCode.setText("LD");
                        break;
                    }
                    case 19: {
                        stateCode.setText("MP");
                        break;
                    }
                    case 20: {
                        stateCode.setText("MH");
                        break;
                    }
                    case 21: {
                        stateCode.setText("MN");
                        break;
                    }
                    case 22: {
                        stateCode.setText("ML");
                        break;
                    }
                    case 23: {
                        stateCode.setText("MZ");
                        break;
                    }
                    case 24: {
                        stateCode.setText("NL");
                        break;
                    }
                    case 25: {
                        stateCode.setText("OD");
                        break;
                    }
                    case 26: {
                        stateCode.setText("PY");
                        break;
                    }
                    case 27: {
                        stateCode.setText("PB");
                        break;
                    }
                    case 28: {
                        stateCode.setText("RJ");
                        break;
                    }
                    case 29: {
                        stateCode.setText("SK");
                        break;
                    }
                    case 30: {
                        stateCode.setText("TN");
                        break;
                    }
                    case 31: {
                        stateCode.setText("TS");
                        break;
                    }
                    case 32: {
                        stateCode.setText("TR");
                        break;
                    }
                    case 33: {
                        stateCode.setText("UA");
                        break;
                    }
                    case 34: {
                        stateCode.setText("UP");
                        break;
                    }
                    case 35: {
                        stateCode.setText("WB");
                        break;
                    }
                    case 36:{
                        break;
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
