package athelas.javableapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import athelas.javableapp.utils.*;
import athelas.javableapp.R;

/**
 * Send command strings (or bytes) to the other device/bluetooth robot.
 * The command strings are manually inputted. If your control program
 */
public class RobotControlActivity extends AppCompatActivity {
    private static String TAG = "RobotControlActivity";

    BluetoothConnectionService mBTConnection;
    EditText etLeftSpeed, etRightSpeed;

    /*
        This is for a robotic arm, can be updated to be for any kind of
        robot you have, just make sure your arduino-end comms know what these mean

        Command String: sss[:][arg0][:][arg1]

        Set Velocity: VEL:[leftS]:[rightS]
            leftS : the left speed (max 3 digits)
            rightS : the right speed (max 3 digits)
        Stop: STP
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_control);
        mBTConnection = MainActivity.getBluetoothConnection();

        setTitle("Bluetooth Robot Control");
        onCreateActSwitch();

        etLeftSpeed = (EditText) findViewById(R.id.editLeftSpeed);
        etRightSpeed = (EditText) findViewById(R.id.editRightSpeed);

        Button sendVelBtn = (Button) findViewById(R.id.ctrlBtn_sendVel);
        sendVelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: instead of manually inputting speed, use your control program to
                //   generate the desired speed

                if(etLeftSpeed.getText().length() > 0 && etRightSpeed.getText().length() > 0) {
                    double lSpeed = Double.parseDouble(etLeftSpeed.getText().toString());
                    double rSpeed = Double.parseDouble(etRightSpeed.getText().toString());

//                Utils.toastMessage(getApplicationContext(), "Moving (" + lSpeed + ", " + rSpeed + ")");
//                writeCommand("VEL", lSpeed, rSpeed);
                    boolean red = lSpeed  > 0;
                    boolean blue = rSpeed > 0;
                    Utils.toastMessage(getApplicationContext(), "Color (r=" + red + ", b=" + blue + ")");
                    writeColor(red, false, blue);
                } else {
                    Utils.toastMessage(getApplicationContext(), "Please Enter speeds.");
                }

            }
        });

        Button btnStop = (Button) findViewById(R.id.ctrlBtn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeCommand("STP");
                Utils.toastMessage(getApplicationContext(), "Stopping");
            }
        });

    }

    private void writeCommand(String cmdString) {
        Log.d(TAG, "writeCommand: writing to bt connection");
        byte[] bytes = cmdString.getBytes(Charset.defaultCharset());
        mBTConnection.write(bytes);
    }

    private void writeCommand(String label, double lSpeed, double rSpeed) {

        //https://www.makeblock.com/project/makeblock-orion-protocol
        String lSpeedStr = lSpeed + "";
        lSpeedStr = lSpeedStr.substring(0, 3);
        String rSpeedStr = rSpeed + "";
        rSpeedStr = rSpeedStr.substring(0, 3);

        writeCommand("VEL:" + lSpeedStr + ":" + rSpeedStr);
    }

    private void writeColor(boolean red, boolean green, boolean blue){
        byte[] bytes = new byte[]{(byte)0xff, (byte)0x55, (byte)0x09, (byte)0x09, (byte)0x00, (byte)0x02, (byte)0x08, (byte)0x07, (byte)0x00, (byte)0x0a, (byte)0x00, (byte)0x00};

        bytes[9] = red ? (byte)0x0a : (byte)0x00;
        bytes[10] = green ? (byte)0x0a : (byte)0x00;
        bytes[11] = blue ? (byte)0x0a : (byte)0x00;
        mBTConnection.write(bytes);
    }

    ///////////////////////////////////////////////////////////////
    ///// Activity Switching //////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    public ImageButton toReceivingBtn;

    public void onCreateActSwitch() {
        Log.d(TAG, "onCreateActSwitch: returning from robot ctr activity");
        toReceivingBtn = (ImageButton) findViewById(R.id.rbtToDataBtn);
        toReceivingBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }


}