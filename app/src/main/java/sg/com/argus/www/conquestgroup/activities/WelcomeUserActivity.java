package sg.com.argus.www.conquestgroup.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sg.com.argus.www.conquestgroup.R;
import sg.com.argus.www.conquestgroup.adapters.ConnectionDetector;
import sg.com.argus.www.conquestgroup.adapters.Stateadapter1;
import sg.com.argus.www.conquestgroup.models.AppController;
import sg.com.argus.www.conquestgroup.models.HttpsTrustManager;
import sg.com.argus.www.conquestgroup.models.MenuCategories;
import sg.com.argus.www.conquestgroup.utils.BluetoothUtil;
import sg.com.argus.www.conquestgroup.utils.DialogScreens;
import sg.com.argus.www.conquestgroup.utils.ESCUtil;
import sg.com.argus.www.conquestgroup.utils.SunmiPrintHelper;

public class WelcomeUserActivity extends AppCompatActivity {
    private Button next;
    private final static String TAG = WelcomeUserActivity.class.getSimpleName();

    private boolean mConnected = false;
    ConnectionDetector cd;
    Boolean isInternetPresent = false;
    ImageView searchbtn;
    private AutoCompleteTextView searchLotDetails;
    private String username, loginid, password, orgid, userid, bagTypeId, lotId, caName, lotRate, traderName, actualBags;
    private SharedPreferences savedata;
    private TextView sellerName, commodity, lotPrice, tradderName, welcomeuser, logout, onlyBagWeight;
    private Spinner bagType, fee_category;
    private double bagtypecal, newbagTypeValue;
    private String bagtypeRel;
    String oprId;
    ArrayList<String> stringLotArray = new ArrayList<String>();
    private ArrayList<MenuCategories> hotelCnstsesList;
    String feeCategoryId;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_user);
        init_printer();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        savedata = getSharedPreferences("weighing_scale", MODE_PRIVATE);

        searchbtn = (ImageView) findViewById(R.id.searchbtn);
        next = (Button) findViewById(R.id.next);
        sellerName = (TextView) findViewById(R.id.sellerName);
        commodity = (TextView) findViewById(R.id.commodity);
        bagType = (Spinner) findViewById(R.id.bagType);
        fee_category = (Spinner) findViewById(R.id.fee_category);
        lotPrice = (TextView) findViewById(R.id.lotPrice);
        tradderName = (TextView) findViewById(R.id.tradderName);
        welcomeuser = (TextView) findViewById(R.id.welcomeuser);
        logout = (TextView) findViewById(R.id.logout);
        searchLotDetails = (AutoCompleteTextView) findViewById(R.id.searchLotDetails);

        final Intent intent = getIntent(); //Get the Intent that launched this activity
        if (intent != null) {
            loginid = intent.getStringExtra("u_name");
            username = intent.getStringExtra("username");
            password = intent.getStringExtra("u_pass");
            orgid = intent.getStringExtra("u_orgid");
            userid = intent.getStringExtra("u_id");
            oprId = intent.getStringExtra("opr_id");
        }

        welcomeuser.setText("Welcome " + username);
        cd = new ConnectionDetector(this);
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            new GetLotIdDetails().execute();
            new GetFeeDetails().execute();
            //getfeecategory("https://train.enam.gov.in/NamWebSrv/rest/FeeCategory/getFeeCategoryListAutoSaleAgreement");
        } else {
            showInternetAlert();
        }

        handler = new Handler();
        initPrinterstyle();


        logout.setOnClickListener(v -> {
            showLogoutAlert(WelcomeUserActivity.this);
        });
        searchbtn.setOnClickListener(v -> ShowData(searchLotDetails.getText().toString()));
        searchLotDetails.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    ShowData(searchLotDetails.getText().toString());
                }
                return false;
            }
        });
        next.setOnClickListener(v -> {
            try {
                if (sellerName.getText().toString().equals("")) {
                    Toast.makeText(WelcomeUserActivity.this, "Please Search Lot Details First", Toast.LENGTH_SHORT).show();
                } else {
                    BagTypeRelation();
                    Intent i = new Intent(WelcomeUserActivity.this, BluetoothActivity.class);
                    i.putExtra("u_name", loginid.toString());
                    i.putExtra("u_pass", password.toString());
                    i.putExtra("u_orgid", orgid.toString());
                    i.putExtra("u_id", userid.toString());
                    i.putExtra("opr_id", oprId);
                    i.putExtra("lotId", lotId.toString());
                    i.putExtra("bagTypeId", bagTypeId.toString());
                    i.putExtra("commodityName", commodity.getText().toString());
                    i.putExtra("farmerName", sellerName.getText().toString());
                    i.putExtra("caName", caName.toString());
                    i.putExtra("lotRate", lotRate.toString());
                    i.putExtra("traderName", traderName.toString());
                    i.putExtra("actualBags", actualBags.toString());
                    i.putExtra("newBagTypeValue", Double.toString(newbagTypeValue));
                    i.putExtra("BagTypeDesc", bagType.getSelectedItem().toString());
                    i.putExtra("feeCategoryId", feeCategoryId.toString());
                    startActivity(i);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(WelcomeUserActivity.this, "BagType not selected", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public void showLogoutAlert(Context context) {

        //Inflate Logout AlertDialog
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View logoutPromptView = layoutInflater.inflate(R.layout.dialog_logout, null);

        final  Button logoutNoBtn = logoutPromptView.findViewById(R.id.logout_no_btn);
        final Button logoutYesBtn = logoutPromptView.findViewById(R.id.logout_yes_btn);
        final TextView logoutTextPrompt = logoutPromptView.findViewById(R.id.logout_prompt_textview);

        logoutTextPrompt.setText(R.string.logout_dialog_prompt);

        /**
         * Alert Dialog Builder
         */
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(logoutPromptView).setCancelable(true);

        final AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        logoutNoBtn.setOnClickListener(v -> {
            // cancel dialog
            alert.cancel();
        });

        logoutYesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context,LoginActivity.class);
            intent.putExtra("screen", "1");
            startActivity(intent);

            finish();

        });



    }


    private void init_printer() {
        SunmiPrintHelper.getInstance().initSunmiPrinterService(this);
    }

    private void initPrinterstyle() {
        if (BluetoothUtil.isBlueToothPrinter) {
            BluetoothUtil.sendData(ESCUtil.init_printer());
        } else {
            SunmiPrintHelper.getInstance().initPrinter();
        }

    }


    void showInternetAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You don't have Internet Connection.")
                .setCancelable(false)
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        }).setNegativeButton("Retry",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        startActivity(getIntent());
                    }
                });

        // Creating dialog box
        AlertDialog alert = builder.create();
        // Setting the title manually
        alert.setTitle("Internet Connection");
        alert.show();

    }

    public void getfeecategory(String url) {
        hotelCnstsesList = new ArrayList<MenuCategories>();
        final Map<String, String> postParameters = new HashMap<String, String>();
        postParameters.put("orgId", orgid);
        postParameters.put("oprId", oprId);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(postParameters),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        // TODO Auto-generated method stub
                        Log.e("jsonObject123", "jsonObject" + jsonObject);

                        try {

                            String result = jsonObject.getString("statusMsg");
                            if (result.equals("S")) {
                                JSONArray array = jsonObject.getJSONArray("listFeeCategoryForAutoSaleModel");
                                if (array.length() != 0) {
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject job = array.getJSONObject(i);
                                        final String feeCategoryId = job.getString("feeCategoryId");
                                        String feeCategoryName = job.getString("feeCategoryName");

                                        MenuCategories offersCnsts = new MenuCategories(feeCategoryId, feeCategoryName);
                                        offersCnsts.setFeeCategoryId(feeCategoryId);
                                        offersCnsts.setFeeCategoryName(feeCategoryName);
                                        hotelCnstsesList.add(offersCnsts);
                                    }
                                    fee_category.setAdapter(new Stateadapter1(WelcomeUserActivity.this, hotelCnstsesList));
                                    fee_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            MenuCategories constants = (MenuCategories) hotelCnstsesList.get(position);
                                            feeCategoryId = constants.getFeeCategoryId();
                                            Log.e("feeCategoryId", "feeCategoryId" + feeCategoryId);

                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(WelcomeUserActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }


        }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> postParameters = new HashMap<String, String>();
                postParameters.put("orgId", orgid);
                postParameters.put("oprId", oprId);
                return postParameters;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(
                90000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(req);

    }

    private class GetFeeDetails extends AsyncTask<String, Void, String> {
        String text = "";
        String uname = "", pass = "";
        ProgressDialog p = new ProgressDialog(WelcomeUserActivity.this);

        @Override
        protected void onPreExecute() {

            p.setMessage("Please Wait...");
            p.setCancelable(false);
            p.show();

        }

        protected String doInBackground(String... params) {

            try {
                HttpsTrustManager.allowAllSSL();
                String urlParameters = "orgId=" + orgid + "&oprId=" + oprId;
                Log.e("params", "" + urlParameters);
                byte[] postData = new byte[0];

                postData = urlParameters.getBytes(StandardCharsets.UTF_8);

                URL url = new URL("https://train.enam.gov.in/NamWebSrv/rest/FeeCategory/getFeeCategoryListAutoSaleAgreement");
                //URL url = new URL("http://www.Train.enam.gov.in/NamWebSrv/rest/verifyUser");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
                connection.getOutputStream().write(postData);
                int respo = connection.getResponseCode();
                String res = connection.getResponseMessage();

                Log.e("ressss", "ressss==>" + res);
                BufferedReader br;
                if (respo != 200) {

                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader((connection.getInputStream())));

                }

                String line = "";
                while ((line = br.readLine()) != null) {
                    text += line;
                    //  return sb.toString();
                }
                connection.disconnect();
            } catch (Exception exc) {
                exc.getMessage();

            }
            return text;
        }

        protected void onPostExecute(String result) {
            Log.e("result", "result" + result);
            hotelCnstsesList = new ArrayList<MenuCategories>();
            p.dismiss();
            try {
                JSONObject object = new JSONObject(result);
                String StatusMsg = object.getString("statusMsg");
                if (StatusMsg.equals("S")) {
                    JSONArray Jarray = object.getJSONArray("listFeeCategoryForAutoSaleModel");
                    for (int i = 0; i < Jarray.length(); i++) {
                        JSONObject json = Jarray.getJSONObject(i);
                        String feeCategoryId = json.getString("feeCategoryId");
                        String feeCategoryName = json.getString("feeCategoryName");
                        MenuCategories offersCnsts = new MenuCategories(feeCategoryId, feeCategoryName);
                        offersCnsts.setFeeCategoryId(feeCategoryId);
                        offersCnsts.setFeeCategoryName(feeCategoryName);
                        hotelCnstsesList.add(offersCnsts);
                    }
                    fee_category.setAdapter(new Stateadapter1(WelcomeUserActivity.this, hotelCnstsesList));
                    fee_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            MenuCategories constants = (MenuCategories) hotelCnstsesList.get(position);
                            feeCategoryId = constants.getFeeCategoryId();
                            Log.e("feeCategoryId", "feeCategoryId" + feeCategoryId);

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

                } else {
                    Toast.makeText(WelcomeUserActivity.this, "Something Went Worng!....", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                p.dismiss();
                e.printStackTrace();

            }

        }
    }

    private class GetLotIdDetails extends AsyncTask<String, Void, String[]> {
        String[] text = {"", ""};
        ProgressDialog p = new ProgressDialog(WelcomeUserActivity.this);

        @Override
        protected void onPreExecute() {

            p.setMessage("Please Wait...");
            p.setCancelable(false);
            p.show();

        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        protected String[] doInBackground(String... params) {

            try {
                // String urlParameters = "orgId=1&oprId=30&loginId=TS014A00001&password=Pass@123";
                HttpsTrustManager.allowAllSSL();
                String urlParameters = "orgId=" + orgid + "&oprId=" + oprId + "&loginId=" + loginid + "&password=" + password + "";

                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                byte[] postData1 = urlParameters.getBytes(StandardCharsets.UTF_8);

                URL url = new URL("https://train.enam.gov.in/NamWebSrv/rest/GetLotDetails");
                URL url1 = new URL("https://train.enam.gov.in/NamWebSrv/rest/MastersUpdate/getBagTypes");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();

                connection.setRequestMethod("POST");
                connection1.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection1.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                connection1.setRequestProperty("charset", "utf-8");

                connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
                connection.setRequestProperty("Content-Length", Integer.toString(postData1.length));

                connection.getOutputStream().write(postData);
                connection1.getOutputStream().write(postData1);

                int respo = connection.getResponseCode();
                int respo1 = connection1.getResponseCode();

                String res = connection.getResponseMessage();
                String res1 = connection.getResponseMessage();

                BufferedReader br, br1;
                if (respo != 200) {
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader((connection.getInputStream())));

                }
                if (respo1 != 200) {
                    br1 = new BufferedReader(new InputStreamReader(connection1.getErrorStream()));
                } else {
                    br1 = new BufferedReader(new InputStreamReader((connection1.getInputStream())));

                }


                String line = "", line1 = "";
                while ((line = br.readLine()) != null) {
                    text[0] += line;
                }

                while ((line1 = br1.readLine()) != null) {
                    text[1] += line1;

                }

                connection.disconnect();

            } catch (Exception exc) {
                String excep = exc.getMessage();

            }
            return text;
        }

        protected void onPostExecute(String result[]) {
            p.dismiss();
            try {
                if (result[0].contains("statusMsg\":\"S\"") && result[1].contains("statusMsg\":\"S\"")) {
                    try {
                        JSONObject object = new JSONObject(result[0]);
                        SharedPreferences.Editor edit = savedata.edit();
                        edit.putString("LotIdDetails", object.toString());
                        edit.commit();
                    } catch (Exception e) {
                        e.getMessage();
                    }
                    List<String> bagTypeValue = new ArrayList<String>();
                    JSONObject jsonShow = new JSONObject(result[1]);
                    JSONArray Jarray = jsonShow.getJSONArray("listBagType");
                    for (int i = 0; i < Jarray.length(); i++) {
                        JSONObject json = Jarray.getJSONObject(i);
                        String bagTypeDesc = json.getString("bagTypeDesc");
                        bagTypeValue.add(bagTypeDesc);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(WelcomeUserActivity.this,
                                android.R.layout.simple_list_item_1, bagTypeValue);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        bagType.setAdapter(adapter);
                    }
                    AutoCompleteLotId();
                } else {
                    Toast.makeText(WelcomeUserActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                p.dismiss();
                e.getMessage();
            }

        }
    }

    public void BagTypeRelation() {
        try {
            bagtypeRel = bagType.getSelectedItem().toString();
            bagtypeRel = bagtypeRel.replace(" KG BAG", "");
            bagtypeRel = bagtypeRel.replace(" KG", "");
            bagtypeRel = bagtypeRel.replace(" KG CRATES", "");
            bagtypeRel = bagtypeRel.replace(" KG BOXES", "");
           /* if (bagtypeRel.equals("LOOSE IN KG") || bagtypeRel.equals("LOOSE IN QUINTLE")) {
                bagtypeRel = "100";
            }*/
            bagtypecal = Double.parseDouble(bagtypeRel);
            double valincby = bagtypecal * 10 / 100;
            newbagTypeValue = bagtypecal + valincby;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void AutoCompleteLotId() {
        try {

            String getArray = savedata.getString("LotIdDetails", "");
            JSONObject jsonGet = new JSONObject(getArray);
            JSONArray jarrayGet = jsonGet.getJSONArray("getListofLotDtl");
            for (int j = 0; j < jarrayGet.length(); j++) {
                JSONObject json = jarrayGet.getJSONObject(j);
                stringLotArray.add(json.getString("lotId"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (this, android.R.layout.select_dialog_item, stringLotArray);
            searchLotDetails.setThreshold(1);//will start working from first character
            searchLotDetails.setAdapter(adapter);
        } catch (JSONException je) {
            Log.d("RECKON_ ", "error :" + je);
        }
    }

    public void ShowData(String SearchLotId) {

        String shareddata = savedata.getString("LotIdDetails", "");
        //if (!shareddata.equals("")) {
        if (shareddata.contains(SearchLotId) && !SearchLotId.equals("")) {
            try {
                JSONObject jsonShow = new JSONObject(shareddata);
                JSONArray Jarray = jsonShow.getJSONArray("getListofLotDtl");

                for (int i = 0; i < Jarray.length(); i++) {
                    JSONObject json = Jarray.getJSONObject(i);

                    if (json.getString("lotId").equals(SearchLotId)) {
                        lotId = json.getString("lotId");
                        sellerName.setText("" + json.getString("farmerName"));
                        commodity.setText("" + json.getString("commodityName"));
                        lotPrice.setText("" + json.getString("lotRate"));
                        tradderName.setText("" + json.getString("traderName"));
                        bagTypeId = json.getString("typeOfBag");
                        actualBags = json.getString("noOfBags");
                        caName = json.getString("caName");
                        lotRate = json.getString("lotRate");
                        traderName = json.getString("traderName");
                    }

                }
            } catch (JSONException je) {
                Log.d("RECKON_ ", "error :" + je);
            }
        } else {
            Toast.makeText(WelcomeUserActivity.this, "Please Check Lot Id", Toast.LENGTH_SHORT).show();
            clearAll();
        }
    }

    public void clearAll() {
        sellerName.setText("");
        commodity.setText("");
        lotPrice.setText("");
        tradderName.setText("");
    }

    @Override
    public void onBackPressed() {

        showLogoutAlert(WelcomeUserActivity.this);
//        Intent i = new Intent(WelcomeUserActivity.this, LoginActivity.class);
//        i.putExtra("u_name", loginid.toString());
//        i.putExtra("u_pass", password.toString());
//        i.putExtra("u_orgid", orgid.toString());
//        i.putExtra("opr_id", oprId.toString());
//        startActivity(i);

    }
}


