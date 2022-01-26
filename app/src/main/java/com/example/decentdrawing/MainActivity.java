package com.example.decentdrawing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int AUDIO_RECORD_REQUEST_CODE = 300;
    private PaintView paintView;
    private TextView mText;
    private SpeechRecognizer sr;
    private static final String TAG = "tryspeech";
    private String all = "";
    final Handler handler = new Handler();
    final int delay = 3000; // 1000 milliseconds == 1 second
    public static boolean EraserMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  get audio permission at the beginning
        if(!isRecordAudioPermissionGranted())
        {
            Toast.makeText(getApplicationContext(), "Need to request permission", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "No need to request permission", Toast.LENGTH_SHORT).show();
        }

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());

        setContentView(R.layout.activity_main);

        paintView = findViewById(R.id.paint_view);
        mText = findViewById(R.id.textView3);
        mText.setText("Voice Detection here.");

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);

        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
                sr.startListening(intent);
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.pen :
                paintView.changeMode(1); // this line is added for testing
                paintView.pen();
                Toast.makeText(this, "Pen Active!", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.eraser :
                paintView.changeMode(-1);
                Toast.makeText(this, "Fill Mode Active!", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.clear :
                paintView.clear();
                Toast.makeText(this, "Canvas Empty!", Toast.LENGTH_SHORT).show();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private boolean isRecordAudioPermissionGranted()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this,
                            "App required access to audio", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO
                },AUDIO_RECORD_REQUEST_CODE);
                return false;
            }

        } else {
            return true;
        }
    }

    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
            //Log.d(TAG, "on Ready ForSpeech");
            // all = "Ready for speech. \n";
            // all = "listening...";
            mText.setText(all);
            System.out.println(all);
        }
        public void onBeginningOfSpeech()
        {
            //Log.d(TAG, "on Beginning Of Speech");
            // all += "Beginning of speech. \n";
            //  = "Voice detected. \n";
            // all = "listening...";
            mText.setText(all);
            System.out.println(all);
        }
        public void onRmsChanged(float rmsdB)
        {
            //Log.d(TAG, "on Rms Changed");
            // all += "on Rms Changed \n";
//            all += "Listening ... \n";
//            // mText.setText(all);
            System.out.println(all);
        }
        public void onBufferReceived(byte[] buffer)
        {
            //Log.d(TAG, "on Buffer Received");
            // all = "on Buffer Received \n";
            // all = "listening...";
            // mText.setText(all);
            System.out.println(all);
        }
        public void onEndOfSpeech()
        {
            //Log.d(TAG, "on End of Speech");
            //all = "End of speech \n";
            // all = "listening...";
            // mText.setText(all);
            // System.out.println(all);
        }
        public void onError(int error)
        {
            //Log.d(TAG,  "error " +  error);
            // mText.setText("No voice detected.");
            // all = "No voice detected... \n";a
            // all = "listening...";
            // mText.setText(all);
            // System.out.println(all);
        }
        public void onResults(Bundle results)
        {
            String str = new String();
            //Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }
            // mText.setText("results: "+String.valueOf(data.size()));
            all = String.valueOf(data.get(0));
            mText.setText(all);
            System.out.println(all);

            // making the string command change the pen color
            String command = (String) data.get(0);
            String[] commandString = command.split(" ");
            ArrayList<String> colors = new ArrayList<>();
            for (int i = 0; i < commandString.length; i++) {
                String currentString = commandString[i].toLowerCase();
                if (currentString.equals("eraser")){
                    colors.add(currentString);
                    paintView.changeMode(1);
                    EraserMode = true;
                }
                if (currentString.equals("blue") ||
                        currentString.equals("black") ||
                        currentString.equals("cyan") ||
                        currentString.equals("gray") ||
                        currentString.equals("green") ||
                        currentString.equals("magenta") ||
                        currentString.equals("orange") ||
                        currentString.equals("red") ||
                        currentString.equals("white") ||
                        currentString.equals("yellow")) {
                    colors.add(currentString);
                    EraserMode = false;
                }
            }
            if (colors.size() == 0) {
                Toast t = Toast.makeText(getApplicationContext(), "No color detected.", Toast.LENGTH_SHORT);
                t.show();
            } else if (colors.size() == 1) {
                if (colors.get(0).equals("eraser")) {
                    paintView.changeBrushColor("white");
                } else {
                    paintView.changeBrushColor(colors.get(0));
                    // dispatch event
                    // Obtain MotionEvent object
                    long downTime = SystemClock.uptimeMillis();
                    long eventTime = SystemClock.uptimeMillis() + 100;
                    float x = paintView.currentX;
                    float y = paintView.currentY;
                    int metaState = 0;
                    MotionEvent motionEvent = MotionEvent.obtain(
                            downTime,
                            eventTime,
                            MotionEvent.ACTION_UP,
                            x,
                            y,
                            metaState
                    );
                    paintView.dispatchTouchEvent(motionEvent);
                    downTime = SystemClock.uptimeMillis();
                    eventTime = SystemClock.uptimeMillis() + 1000;
                    MotionEvent motionEvent2 = MotionEvent.obtain(
                            downTime,
                            eventTime,
                            MotionEvent.ACTION_DOWN,
                            x + 1,
                            y + 1,
                            metaState
                    );
                    paintView.dispatchTouchEvent(motionEvent2);
                }
            } else {
                Toast t = Toast.makeText(getApplicationContext(), "Multiple color detected. Please provide only one color in your sentence.", Toast.LENGTH_LONG);
                t.show();
            }
        }
        public void onPartialResults(Bundle partialResults) {
            //Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params) {
            //Log.d(TAG, "onEvent " + eventType);
        }
    }
}