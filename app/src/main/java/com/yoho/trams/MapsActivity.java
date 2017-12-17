package com.yoho.trams;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.TimerTask;

import static android.animation.ObjectAnimator.ofObject;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private String bestProvider;
    LatLng latLng;

    HashMap<Integer, Button> tramBtnsList = new HashMap<>();                                        // tram buttons

    HashMap<Long, Integer> linesTrams = new HashMap<>();                                            // tram ZIKiT long ID => line nr
    HashMap<Long, Marker> markers = new HashMap<>();                                                // tram ZIKiT long ID => marker
    HashMap<Integer, Integer> linesAct = new HashMap<>();                                           // tram line nr => 0/1 active/unactive

    HashMap<Integer, String> lowText = new HashMap<>();                                             // tram low => low text

    private ViewGroup hiddenPanel;                                                                  // filter panel
    private boolean isPanelShown;
    private Button btm_btn;

    private ViewGroup menuPanel;                                                                    // menu panel
    private boolean isMenuShown;

    private boolean filtr = false;

    Toolbar myToolbar;

    int req_interval = 5000;
    int animation_time = 2000;

    int[] aLines = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,16,18,19,20,21,22,23,24,50,52,62,64,69};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        hiddenPanel = (ViewGroup) findViewById(R.id.hidden_panel);
        hiddenPanel.setVisibility(View.INVISIBLE);
        isPanelShown = false;
        btm_btn = (Button) findViewById(R.id.btm_btn);

        menuPanel = (ViewGroup) findViewById(R.id.menu_panel);
        menuPanel.setVisibility(View.INVISIBLE);
        isMenuShown = false;

        Typeface oswaldBold = Typeface.createFromAsset(getAssets(), "fonts/Oswald-Bold.ttf");
        Typeface oswaldRegular = Typeface.createFromAsset(getAssets(), "fonts/Oswald-Regular.ttf");

        for (int i = 0; i < aLines.length; i++) {
            String res = "tram_btn_" + (aLines[i]);
            int resID = getResources().getIdentifier(res, "id", getPackageName());
            tramBtnsList.put(aLines[i], (Button) findViewById(resID));
        }

        for (Integer key : tramBtnsList.keySet()) {
            Button tramBtn = tramBtnsList.get(key);
            tramBtn.setTypeface(oswaldBold);
        }

        TextView menu_opt_1 = (TextView) findViewById(R.id.menu_opt_1);
        menu_opt_1.setTypeface(oswaldRegular);
        TextView menu_opt_2 = (TextView) findViewById(R.id.menu_opt_2);
        menu_opt_2.setTypeface(oswaldRegular);

        for (int i = 0; i < aLines.length; i++) {
            linesAct.put(aLines[i], 1);
        }

        lowText.put(0, "");
        lowText.put(1, "wysokopodłogowy");
        lowText.put(2, "częściowo niskopodłogowy");
        lowText.put(3, "niskopodłogowy");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {                                            // customization marker label

                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setGravity(Gravity.CENTER);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        startApp();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startApp();

            } else {
                Toast.makeText(this, "Brak pozwolenia na lokalizację", Toast.LENGTH_SHORT).show();
            }
            return;
        }

    }

    void startApp() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

        } else {

            mMap.setMyLocationEnabled(true);

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            bestProvider = locationManager.getBestProvider(criteria, false);

            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());

            } else {

                locationManager.requestLocationUpdates(bestProvider, 2000, 10, new LocationListener() {
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }

                    @Override
                    public void onLocationChanged(Location location) {
                    }
                });

                location = locationManager.getLastKnownLocation(bestProvider);
                if (location != null) {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                } else {
                    latLng = new LatLng(50.06465, 19.94498);
                }
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

            String stopsUrl = "http://www.ttss.krakow.pl/internetservice/geoserviceDispatcher/services/stopinfo/stops?left=-648000000&bottom=-324000000&right=648000000&top=324000000";
            volleyRequest("stops", stopsUrl);

            java.util.Timer t = new java.util.Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    getTramsJsonData();
                }
            }, 0, req_interval);
        }
    }


    void getTramsJsonData() {
        String url = "http://www.ttss.krakow.pl/internetservice/geoserviceDispatcher/services/vehicleinfo/vehicles?positionType=CORRECTED&colorType=ROUTE_BASED&lastUpdate=" + System.currentTimeMillis() / 1000L;
        System.out.print(url);
        volleyRequest("trams", url);
    }


    void volleyRequest(final String reqType, String url) {

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                switch (reqType) {
                    case "trams":
                        parseTramsJsonData(string);
                        break;
                    case "stops":
                        parseStopsJsonData(string);
                        break;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                String errorMsg = "";

                if (volleyError instanceof NetworkError) {
                    errorMsg = "Brak połączenia z internetem";

                } else if (volleyError instanceof ServerError) {
                    errorMsg = "Serwer nie odpowiada";

                } else if (volleyError instanceof AuthFailureError) {
                    errorMsg = "Brak połączenia z internetem";

                } else if (volleyError instanceof ParseError) {
                    errorMsg = "Błąd, spróbuj ponownie za kilka minut";

                } else if (volleyError instanceof NoConnectionError) {
                    errorMsg = "Brak połączenia z internetem";

                } else if (volleyError instanceof TimeoutError) {
                    errorMsg = "Brak połączenia z internetem";
                }

                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(MapsActivity.this);
//        rQueue.getCache().clear();
        request.setShouldCache(false);
        rQueue.add(request);
    }


    void parseTramsJsonData(String jsonString) {

        try {
            JSONObject tramsObject = new JSONObject(jsonString);
//            String lastUpdate = tramsObject.getString("lastUpdate");                              // last update
            JSONArray tramsArray = tramsObject.getJSONArray("vehicles");                            // vehicles array

            for(int i = 0; i < tramsArray.length(); ++i) {

                JSONObject item = tramsArray.getJSONObject(i);

                if (item.has("category")) {                                                         // add only no deleted trams

                    double longitude = Double.parseDouble(item.getString("longitude")) / 3600000.0; // new tram position
                    double latitude = Double.parseDouble(item.getString("latitude")) / 3600000.0;
                    final LatLng latLng = new LatLng(latitude, longitude);

                    String name = item.getString("name");
                    String tramLineStr = name.substring(0, name.indexOf(' '));

                    if (tramLineStr != "") {
                        int tramLine = tryParse(tramLineStr);
                        String imageName = "tram_" + tramLineStr;                                            // name of tram image

                        long id = parseLong(item.getString("id"));                                           // ZIKiT id of tram
                        linesTrams.put(id, tramLine);


                        JSONObject tramParams = checkTramType(id);
                        String low = tramParams.getString("low");
                        String tramId = tramParams.getString("tramId");
                        String prefix = tramParams.getString("prefix");
                        String type = tramParams.getString("type");

                        if (getDrawableId(imageName) != -1 && linesAct.get(tramLine) != null) {              // check if isset img of this tram

                                Float tramAngle = 0.0f;
                                try {
                                    JSONObject obj = new JSONObject(item.toString());
                                    if (obj.has("path")) {
                                        JSONArray arr = obj.getJSONArray("path");
                                        if (arr != null) {
                                            JSONObject lastPath = (JSONObject) arr.get(arr.length() - 1);
                                            tramAngle = ((Number) lastPath.get("angle")).floatValue();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (markers.containsKey(id)) {

                                    Marker marker = markers.get(id);

                                    ValueAnimator markerAnimator = ofObject(marker, "position", new LatLngEvaluator(), marker.getPosition(), latLng);
                                    ValueAnimator markerAnimator2 = ObjectAnimator.ofObject(marker, "rotation", new FloatEvaluator(), marker.getRotation(), tramAngle);

                                    AnimatorSet animatorSet = new AnimatorSet();
                                    animatorSet.playTogether(markerAnimator, markerAnimator2);
                                    animatorSet.setDuration(animation_time);
                                    animatorSet.start();

                                    if (linesAct.get(tramLine) != 1) {                                               // check if tram line is set as active
                                        marker.setVisible(false);
                                    }

                                } else {

                                    Marker usersMarker = mMap.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .title(name)
                                            .snippet(prefix + tramId + " " + type + "\n" + lowText.get(parseInt(low)) + "\n" + id)
                                            .anchor(0.5f, 0.5f)
                                            .rotation(tramAngle)
                                            .icon(BitmapDescriptorFactory.fromResource(getDrawableId(imageName))));

                                    markers.put(id, usersMarker);
                                }
                        }
                    }
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    void parseStopsJsonData(String jsonString) {

        try {

            JSONObject stopsObject = new JSONObject(jsonString);
            JSONArray stopsArray = stopsObject.getJSONArray("stops");

            for(int i = 0; i < stopsArray.length(); ++i) {

                JSONObject item = stopsArray.getJSONObject(i);

                if (! item.getString("id").equals("6350927454370005182")) {

                    double longitude = Double.parseDouble(item.getString("longitude")) / 3600000.0; // new tram position
                    double latitude = Double.parseDouble(item.getString("latitude")) / 3600000.0;
                    final LatLng latLng = new LatLng(latitude, longitude);

                    String name = item.getString("name");

                    Marker stopMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(name)
                            .snippet("przystanek")
                            .zIndex(-1)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.tram_stop)));

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public JSONObject checkTramType (long id) throws JSONException {

        String strId = Long.toString(id);

        String prefix = "";
        String type = "";
        int low = 0;                                                                                // low floor: 1 = no, 2 - semi, 3 - full

        int tramId = parseInt(strId.substring(15)) - 736;

        if(strId.substring(0, 15).equals("635218529567218")) {

            // Single exception - old id used in one case
            if (tramId == 831) {
                tramId = 216;
            }

            if (101 <= tramId && tramId <= 174) {
                prefix = "HW";
                type = "E1";
                low = 1;

                if ((108 <= tramId && tramId <= 113) || tramId == 127 || tramId == 131 || tramId == 132 || tramId == 134 || (137 <= tramId && tramId <= 139) || (148 <= tramId && tramId <= 150) || (153 <= tramId && tramId <= 155)) {
                    prefix = "RW";
                }
            } else if (201 <= tramId && tramId <= 293) {
                prefix = "RZ";
                type = "105Na";
                low = 1;

                if (246 <= tramId) {
                    prefix = "HZ";
                }
                if (tramId == 290) {
                    type = "105Nb";
                }
            } else if (301 <= tramId && tramId <= 328) {
                prefix = "RF";
                type = "GT8S";
                low = 1;

                if (tramId == 313) {
                    type = "GT8C";
                    low = 2;
                } else if (tramId == 323) {
                    low = 2;
                }
            } else if (401 <= tramId && tramId <= 440) {
                prefix = "HL";
                type = "EU8N";
                low = 2;
            } else if (451 <= tramId && tramId <= 462) {
                prefix = "HK";
                type = "N8S-NF";
                low = 2;

                if ((451 <= tramId && tramId <= 456) || tramId == 462) {
                    type = "N8C-NF";
                }
            } else if (601 <= tramId && tramId <= 650) {
                prefix = "RP";
                type = "NGT6(3)";
                low = 3;

                if (tramId <= 613) {
                    type = "NGT6(1)";
                } else if (tramId <= 626) {
                    type = "NGT6(2)";
                }
            } else if (801 <= tramId && tramId <= 824) {
                prefix = "RY";
                type = "NGT8";
                low = 3;
            } else if (tramId == 899) {
                prefix = "RY";
                type = "126N";
                low = 3;
            } else if (901 <= tramId && tramId <= 936) {
                prefix = "RG";
                type = "2014N";
                low = 3;

                if (915 <= tramId) {
                    prefix = "HG";
                }
            } else if (tramId == 999) {
                prefix = "HG";
                type = "405N-Kr";
                low = 2;
            }
        }

        JSONObject json = new JSONObject("{prefix: " + prefix + ", type: " + type + ", low: " + low + ", tramId: " + tramId + "}");
        return json;
    }

    // check if tram image exist
    public int getDrawableId(String name){
        try {
            Field fld = R.drawable.class.getField(name);
            return fld.getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    // animation of change tram position
    private class LatLngEvaluator implements TypeEvaluator<LatLng> {

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            LatLng mLatLng = new LatLng(
                startValue.latitude + (endValue.latitude - startValue.latitude) * fraction,
                startValue.longitude + (endValue.longitude - startValue.longitude) * fraction
            );
            return mLatLng;
        }
    }

    // animation of change tram rotation
    public class FloatEvaluator implements TypeEvaluator<Number> {
        public Float evaluate(float fraction, Number startValue, Number endValue) {

            float normalizeEnd = (float)endValue - (float)startValue;               // rotate start to 0
            float normalizedEndAbs = (normalizeEnd + 360) % 360;

            float direction = (normalizedEndAbs > 180) ? -1 : 1;                    // -1 = anticlockwise, 1 = clockwise
            float rotation;
            if (direction > 0) {
                rotation = normalizedEndAbs;
            } else {
                rotation = normalizedEndAbs - 360;
            }

            float result = fraction * rotation + (float)startValue;
            return (result + 360) % 360;
        }
    }


    // bottom filter sliding
    public void slideUpDown(final View view) {
        if(!isPanelShown) {
            // Show the panel

            if (!filtr) {
                for (Integer key : tramBtnsList.keySet()) {
                    Button tramBtn = tramBtnsList.get(key);
                    if (key == 62 || key == 64 || key == 69) {
                        tramBtn.setBackground(getResources().getDrawable(R.drawable.button_night));
                    } else {
                        tramBtn.setBackground(getResources().getDrawable(R.drawable.button_unactive));
                    }
                }
            }

            btm_btn.setVisibility(View.INVISIBLE);
            Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
            hiddenPanel.startAnimation(bottomUp);
            hiddenPanel.setVisibility(View.VISIBLE);
            isPanelShown = true;

        } else {
            // Hide the Panel
            Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
            hiddenPanel.startAnimation(bottomDown);
            hiddenPanel.setVisibility(View.INVISIBLE);
            isPanelShown = false;
            btm_btn.setVisibility(View.VISIBLE);
        }
    }


    // menu sliding
    public void slideMenu(final View view) {
        if(!isMenuShown) {

            myToolbar.animate().translationY(-myToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();

            // Show the menu
            Animation menuShow = AnimationUtils.loadAnimation(this, R.anim.menu_show);
            menuPanel.startAnimation(menuShow);
            menuPanel.setVisibility(View.VISIBLE);
            isMenuShown = true;
        } else {

            myToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();

            // Hide the Panel
            Animation menuHide = AnimationUtils.loadAnimation(this, R.anim.menu_hide);
            menuPanel.startAnimation(menuHide);
            menuPanel.setVisibility(View.INVISIBLE);
            isMenuShown = false;
        }
    }

    // click on tram number
    public void tramNrClick (View view) {

        switch(view.getId()) {
            case R.id.tram_btn_1:
                changeTramAct(1);
                break;
            case R.id.tram_btn_2:
                changeTramAct(2);
                break;
            case R.id.tram_btn_3:
                changeTramAct(3);
                break;
            case R.id.tram_btn_4:
                changeTramAct(4);
                break;
            case R.id.tram_btn_5:
                changeTramAct(5);
                break;
            case R.id.tram_btn_6:
                changeTramAct(6);
                break;
            case R.id.tram_btn_7:
                changeTramAct(7);
                break;
            case R.id.tram_btn_8:
                changeTramAct(8);
                break;
            case R.id.tram_btn_9:
                changeTramAct(9);
                break;
            case R.id.tram_btn_10:
                changeTramAct(10);
                break;
            case R.id.tram_btn_11:
                changeTramAct(11);
                break;
            case R.id.tram_btn_12:
                changeTramAct(12);
                break;
            case R.id.tram_btn_13:
                changeTramAct(13);
                break;
            case R.id.tram_btn_14:
                changeTramAct(14);
                break;
            case R.id.tram_btn_16:
                changeTramAct(16);
                break;
            case R.id.tram_btn_18:
                changeTramAct(18);
                break;
            case R.id.tram_btn_19:
                changeTramAct(19);
                break;
            case R.id.tram_btn_20:
                changeTramAct(20);
                break;
            case R.id.tram_btn_21:
                changeTramAct(21);
                break;
            case R.id.tram_btn_22:
                changeTramAct(22);
                break;
            case R.id.tram_btn_23:
                changeTramAct(23);
                break;
            case R.id.tram_btn_24:
                changeTramAct(24);
                break;
            case R.id.tram_btn_50:
                changeTramAct(50);
                break;
            case R.id.tram_btn_52:
                changeTramAct(52);
                break;
            case R.id.tram_btn_62:
                changeTramAct(62);
                break;
            case R.id.tram_btn_64:
                changeTramAct(64);
                break;
            case R.id.tram_btn_69:
                changeTramAct(69);
                break;
        }
    }

    // change tram active
    void changeTramAct(int lineNr) {

        if (!filtr) {                                                                               // if filtr isn't active yet
            for ( int key : linesAct.keySet() ) {
                linesAct.put(key, 0);
            }
            for ( long key : markers.keySet() ) {
                Marker marker = markers.get(key);
                marker.setVisible(false);
            }
        }

        int active = linesAct.get(lineNr);                                                          // return 1-active / 0-unactive

        String buttonID = "tram_btn_" + (lineNr);
        int resID = getResources().getIdentifier(buttonID, "id", getPackageName());

        Button tram_btn = (Button) findViewById(resID);
        if (active == 1) {                                                                          // unactivation this line
            if (lineNr == 62 || lineNr == 64 || lineNr == 69) {
                tram_btn.setBackground(getResources().getDrawable(R.drawable.button_night));
            } else {
                tram_btn.setBackground(getResources().getDrawable(R.drawable.button_unactive));
            }
            linesAct.put(lineNr, 0);

            boolean tramsInFilter = false;
            for ( long key : markers.keySet() ) {
                if (linesTrams.get(key) == lineNr) {
                    Marker marker = markers.get(key);
                    marker.setVisible(false);
                };
            }

            for ( int key : linesAct.keySet() ) {
                if (linesAct.get(key) == 1) {
                    tramsInFilter = true;
                }
            }

            if (!tramsInFilter) {
                for ( int key : linesAct.keySet() ) {
                    linesAct.put(key, 1);
                }
                for ( long key : markers.keySet() ) {
                    Marker marker = markers.get(key);
                    marker.setVisible(true);
                }
                filtr = false;
            }

        } else {                                                                                    // activation this line
            tram_btn.setBackground(getResources().getDrawable(R.drawable.button));
            linesAct.put(lineNr, 1);

            for ( long key : markers.keySet() ) {
                if (linesTrams.get(key) == lineNr) {
                    Marker marker = markers.get(key);
                    marker.setVisible(true);
                };
            }

            filtr = true;                                                                           // activate filtr
        }

    }


    public static Integer tryParse(String text) {
        try {
            return parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    public void onActAboutClick (View view) {
        Intent aboutAct = new Intent(MapsActivity.this, AboutActivity.class);
        startActivity(aboutAct);
    }

    public void onActContactClick (View view) {
        Intent contactAct = new Intent(MapsActivity.this, ContactActivity.class);
        startActivity(contactAct);
    }

}
