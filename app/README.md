This is a setup for 2-way bluetooth communication between an arduino and an android phone. There is a sample
arduino script for the arduino end, and an android studio project for the android phone app.

This README will go over what you need to do to make this app work for you. There are also comments
and TODO's that describe what you will probably have to change for your application.

## The android app

This is an android app that allows you to connect to any other device (specifically a BT-enabled Arduino device),
and participate in 2-way communications with it. There are three activities (what Android calls pages in the app).

### 1. MainActivity
The main activity is what shows up when you launch the app. It allows you to turn on the bluetooth
pairing and discovery of your phone so that you can see the Arduino and the Arduino can see you. The page
has instructions on how to use it. Once you are done, click the button in the top right corner to move on.

#### What you need to do to make this Activity work for you.

The app needs to know what UUID it should be looking for. The app will be able to see all BT devices
near by, but can't expose itself to all of them, or be ready to connect to all of them.


### 2. ReceiveDataActivity
This activity displays data that the phone is receiving from the device. When I originally made this,
I was making it for a healthcare related project, so I have the data being plotted, and there are
different "data types" which basically just means the plots change color.

#### What you need to do to make this Activity work for you.
The data is coming in as an endless byte stream over the Bluetooth Serial commuincation stream of the phone.
So to actually differentiate from message to message, I included "EOF" at the end of every message
that I send to the phone. You should probably do something similar, and account for it. Also, I only
care about the numbers for my applications, so I Regex everything out. To really understand what is
being read, look at the `public void onReceive(Context context, Intent intent)` of the
`BroadcastReceiver mReadReciever`.

Feel free to play around with this activity to make it work for you!

### 3. RobotControlActivity
This activity allows you to send data from the phone to the Arduino. It is currently set up as a manual
input from text boxes to the bluetooth receiver. **This is where you would interface with your control
program to determine what the velocity commands the robot should be getting.**

#### What you need to do to make this Activity work for you.
Right now, the android app and the Arduino code are working under an assumed command string language:
`VEL:1.0:-1.0` is essentially `robot->set_vel(1.0,-1.0)`. and `STP` essentially means `robot->set_vel(0,0)`.
You can edit that paradigm or add your own.

### Changing the app GUI
In the `res/layout` folder, the three XML files that start with `activity_` define the layout and GUI
elements. Use Android Studio to easily play around with the app's visuals.

## The Arduino Script
The C++ for Arduino script has 2 methods. One method checks every tick whether anything has been sent
to it, and displays that on an LCD screen. The other method sends a message through its BT serial stream
once a button has been clicked. The [mBot 
github repo](https://github.com/Makeblock-official/Makeblock-Libraries/blob/master/examples/Me_Bluetooth/SlaveBluetoothBySoftSerialTest/SlaveBluetoothBySoftSerialTest.ino)
also has some good examples of their own bluetooth communication modules, but the one provided here
works well with this app.

## Robot Control Strings

The app controls the robot arm by sending formatted control strings. The formats are as follows:

Set Velocity: `VEL:[leftS]:[rightS]`
            `leftS` : the left speed (max 3 digits)
            `rightS` : the right speed (max 3 digits)
Stop: `STP`


## Credit

I partially went through [this tutorial](https://www.youtube.com/watch?v=y8R2C86BIUc&list=PLgCYzUzKIBE8KHMzpp6JITZ2JxTgWqDH2)
But to be honest, the tutorial wasn't really teaching anything, just telling you what to type out. I
used the [android docs](https://developer.android.com/guide/topics/connectivity/bluetooth) to learn about
what everything was doing in terms of the actual Java classes.