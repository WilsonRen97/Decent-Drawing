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
    private listener lsner = new listener();
    private boolean isWorking = false;

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
        sr.setRecognitionListener(lsner);

        setContentView(R.layout.activity_main);

        paintView = findViewById(R.id.paint_view);
        mText = findViewById(R.id.textView3);
        // mText.setText("Voice Detection here."); // Get This one back later

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);

        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
                if (!isWorking) {
                    sr.startListening(intent);
                    System.out.println("Start Listening....");
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         return true;
        // Get This one back later
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.options_menu, menu);
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         return true;
        // Get This one back later
//        switch (item.getItemId()){
//            case R.id.pen :
//                paintView.changeMode(1); // this line is added for testing
//                paintView.pen();
//                Toast.makeText(this, "Pen Active!", Toast.LENGTH_SHORT).show();
//                return true;
//
//            case R.id.eraser :
//                paintView.changeMode(-1);
//                Toast.makeText(this, "Fill Mode Active!", Toast.LENGTH_SHORT).show();
//                return true;
//
//            case R.id.clear :
//                paintView.clear();
//                Toast.makeText(this, "Canvas Empty!", Toast.LENGTH_SHORT).show();
//                return true;
//        }


//        return super.onOptionsItemSelected(item);
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
        public void onBeginningOfSpeech()
        {
            isWorking = true;
            // mText.setText("Start listening..."); // Get This one back later
            System.out.println("Start listening...");
        }
        public void onEndOfSpeech()
        {
            isWorking = false;
            // mText.setText("End listening..."); // Get This one back later
            System.out.println("End listening...");
        }
        public void onRmsChanged(float rmsdB) {}
        public void onBufferReceived(byte[] buffer) {}
        public void onReadyForSpeech(Bundle params) {}
        public void onError(int error) {}
        public void onPartialResults(Bundle partialResults) {}
        public void onEvent(int eventType, Bundle params) {}

        public void onResults(Bundle results)
        {
            isWorking = false;
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            // we will get 5 different possible results
            /*
            for (int i = 0; i < data.size(); i++) {
                System.out.println("result: " + data.get(i));
                str += data.get(i);
            }
            */

            // here, we just take the first result
            all = String.valueOf(data.get(0));
            // mText.setText(all); // Get This one back later
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
                } else if (currentString.equals("fill") ||
                        currentString.equals("fell") ||
                        currentString.equals("feel") ||
                        currentString.equals("fail") ||
                        currentString.equals("phil") ||
                        currentString.equals("chill")
                ) {
                    paintView.changeMode(-1);
                } else if (currentString.equals("pen")
                        ||currentString.equals("pain")
                        || currentString.equals("pane")) {
                    paintView.changeMode(1);
                    Toast t = Toast.makeText(getApplicationContext(), "Pen Active!", Toast.LENGTH_SHORT);
                    t.show();
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
                Toast t = Toast.makeText(getApplicationContext(), "No color detected.", Toast.LENGTH_LONG);
                t.show();
            } else if (colors.size() == 1) {
                if (colors.get(0).equals("eraser")) {
                    paintView.changeBrushColor("white");
                } else {
                    paintView.changeBrushColor(colors.get(0));
                    // dispatch event, so that we can change the pen color while we draw
                    // Obtain MotionEvent object
                    if (paintView.mode > 0) {
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
                }
            } else {
                Toast t = Toast.makeText(getApplicationContext(), "Multiple color detected. Please provide only one color in your sentence.", Toast.LENGTH_LONG);
                t.show();
            }
        }
    }
}