package athelas.javableapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import athelas.javableapp.R;
import athelas.javableapp.utils.*;

/**
 * Receive simple data of varying types from the paired device and plot the data on a single plot area
 * based on time.
 */
public class ReceiveDataActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    public static String TAG = "ReceiveDataActivity";

    TextView tvIncomingMessages;
    StringBuilder readMessages;

    BluetoothConnectionService mBTConnection;

    private LineGraphSeries<DataPoint> dataSeries;
    private ArrayList<XYValue> currXYValues;
    private ArrayList<XYValue> heartValues, bloodO2Values, tempValues, lungValues;
    GraphView mLinePlot;

    Spinner dataSelect;
    int currTestColor;
    Map<String, Integer> dataTypeToColor;
    ArrayAdapter<CharSequence> dataSelectAdapter;

    double currX, currY;
    double startTimeMs = 0;

    final int xAxisSize = 20;

    private BroadcastReceiver mReadReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("data");

            // asynchronized problems require some kind of "end of file" message
            // to make sure the whole message gets through and is displayed properly.
            // Make sure that when you are sending messages to the app, you are
            // ending them with "EOF", or whatever you specify here
            //TODO: you might want to have your robot --> phone comms formatted differently
            //  here is where you should fill in the blanks to make sure that parsing is done
            boolean completedMessage = false;
            readMessages.append(text);
            if(readMessages.toString().endsWith("EOF")){
                // BUGFIX: sometimes two files still collide as one, then their numbers
                // get concat-ed in the regex
                readMessages.setLength(readMessages.length() - 3);
                completedMessage = true;
            }

            tvIncomingMessages.setText(readMessages);

            if(completedMessage) {
                try {
                    // gets rid of all none-numeric values
                    currY = Double.parseDouble(readMessages.toString().replaceAll("\\D+", ""));
                    currX = (System.currentTimeMillis() - startTimeMs) / 1000.0f;

                    currXYValues.add(new XYValue(currX, currY));
                    Log.d(TAG, "mReadReciever: plotting data to graph (" + currY + ", " + currX + ")");

                    initGraph();

                    readMessages = new StringBuilder();
                } catch (NumberFormatException e) {
                    Log.d(TAG, "mReadReciever: bad data given for vitals graph: " + readMessages.toString());
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_vitals);
        onCreateActSwitch();
        setTitle("Bluetooth Receiving Data");
        mBTConnection = MainActivity.getBluetoothConnection();

        readMessages = new StringBuilder();
        tvIncomingMessages = (TextView) findViewById(R.id.incomingMessages);

        //Use local broadcast manager to recieve incoming messages
        LocalBroadcastManager.getInstance(this).registerReceiver(mReadReciever, new IntentFilter("incomingMessage"));

        dataSelect = (Spinner) findViewById(R.id.testSelection);
        dataSelectAdapter =
                ArrayAdapter.createFromResource(this, R.array.data_types, android.R.layout.simple_spinner_item);
        dataSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataSelect.setAdapter(dataSelectAdapter);

        dataTypeToColor = new HashMap<>();
        dataTypeToColor.put("heart rate", Color.argb(255,233,30,90));
        dataTypeToColor.put("blood o2", Color.argb(255, 3, 169, 244));
        dataTypeToColor.put("temperature", Color.argb(255, 255, 87, 34));
        dataTypeToColor.put("lung audio", Color.argb(255, 104, 89, 84));
        currTestColor = Color.argb(100,233,30,90);

        mLinePlot = (GraphView) findViewById(R.id.dataPlot);
        heartValues = new ArrayList<>();
        bloodO2Values = new ArrayList<>();
        tempValues = new ArrayList<>();
        lungValues = new ArrayList<>();

        currXYValues = heartValues;

        dataSelect.setOnItemSelectedListener(ReceiveDataActivity.this);

        startTimeMs = System.currentTimeMillis();
        currX = 0;
        initGraph();
    }

    private void initGraph(){
        dataSeries = new LineGraphSeries<>();

        if(currXYValues.size() != 0) {
            createScatterPlot();
        } else {
            Log.d(TAG, "onCreate: No data to plot");
        }
    }

    private void createScatterPlot() {
        Log.d(TAG, "createScatterPlot: Creating scatter plot.");

        currXYValues = Utils.sortArrayByX(currXYValues);

        double maxX = 0;
        for(int ii = 0; ii < currXYValues.size(); ii++) {
            try {
                double x = currXYValues.get(ii).getX();
                double y = currXYValues.get(ii).getY();
                dataSeries.appendData(new DataPoint(x,y), true, 1000);
                maxX = Math.max(x, maxX);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "createScatterPlot: IllegalArgumentException: " + e.getMessage());
            }
        }

        //set some properties
        dataSeries.setDrawDataPoints(true);
        dataSeries.setDataPointsRadius(5f);
        dataSeries.setColor(currTestColor);

        //set Scrollable and Scaleable
        mLinePlot.getViewport().setScalable(true);
        mLinePlot.getViewport().setScalableY(true);
        mLinePlot.getViewport().setScrollable(true);
        mLinePlot.getViewport().setScrollableY(true);

        //set manual x bounds
        mLinePlot.getViewport().setYAxisBoundsManual(true);
        mLinePlot.getViewport().setMaxY(100);
        mLinePlot.getViewport().setMinY(0);

        //set manual y bounds
        mLinePlot.getViewport().setXAxisBoundsManual(true);
        mLinePlot.getViewport().setMaxX(maxX + 1);
        mLinePlot.getViewport().setMinX(Math.max(maxX - xAxisSize, 0));

        mLinePlot.addSeries(dataSeries);

        mLinePlot.addSeries(dataSeries);
    }


    ///////////////////////////////////////////////////////////////
    ///// Activity Switching //////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    public ImageButton toConnectBtn;
    public ImageButton toRobotCtrlBtn;

    public void onCreateActSwitch() {
        toConnectBtn = (ImageButton) findViewById(R.id.toConnectBtn);
        toConnectBtn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        toRobotCtrlBtn  = (ImageButton) findViewById(R.id.toRobotBtn);
        toRobotCtrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loadNewACt = new Intent(ReceiveDataActivity.this, RobotControlActivity.class);
                startActivityForResult(loadNewACt, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            // do anything from the LiveVitalActivity
        } else if(resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Robot Controlling Failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemSelected: changing currentTest");
        String target = dataSelectAdapter.getItem(i).toString().toLowerCase();
        currTestColor = dataTypeToColor.get(target);
        tvIncomingMessages.setBackgroundColor(currTestColor);
        switch (target) {
            case "heart rate": currXYValues = heartValues;
            break;
            case "blood o2": currXYValues = bloodO2Values;
            break;
            case "temperature": currXYValues = tempValues;
            break;
            case "lung audio": currXYValues = lungValues;
            break;
        }
        initGraph();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}