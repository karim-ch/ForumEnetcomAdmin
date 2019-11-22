package com.example.fatma.forumadmin;


        import android.*;
        import android.Manifest;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.os.Vibrator;
        import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.SparseArray;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.Spinner;
        import android.widget.TextView;

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.android.gms.vision.CameraSource;
        import com.google.android.gms.vision.Detector;
        import com.google.android.gms.vision.barcode.Barcode;
        import com.google.android.gms.vision.barcode.BarcodeDetector;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;

        import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private SurfaceView cameraPreview;
    private  BarcodeDetector barcodeDetector;
    private  CameraSource cameraSource;
    private  final int RequestCameraPermissionID = 1001;

    private  TextView txtResult;
    private TextView mName;
    private TextView mPack;
    private TextView mRestau;
    private TextView mWorkshop;

    private Button mSetPack;
    private Button mSetResateu;
    private Button mLogout;
    private Button mRestart;
    private Button setPosition;
    private DatabaseReference mUserRef;

    private Boolean found_Id=false;
    private String id="";
    private Spinner spinner;

    //**************CHECK AUTH*********************\\
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            SendToLogin();

        } else {}

    }

    private void SendToLogin() {
        Intent Startintent = new Intent(MainActivity.this, WelcomActivity.class);
        startActivity(Startintent);
        finish();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.position, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);






        mAuth = FirebaseAuth.getInstance();



        mSetPack =(Button)findViewById(R.id.pack_btn);
        mSetResateu=(Button)findViewById(R.id.restau_ok);
        mLogout=(Button)findViewById(R.id.logout_btn);
        mRestart=(Button)findViewById(R.id.restart_btn);

        txtResult = (TextView) findViewById(R.id.txtResult);
        mName = (TextView) findViewById(R.id.nameResult);
        mPack = (TextView) findViewById(R.id.pack);
        mRestau = (TextView) findViewById(R.id.restauration);
        mWorkshop = (TextView) findViewById(R.id.RegistredWorkshop);


        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .build();
        //Add Event
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //Request permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},RequestCameraPermissionID);
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                if(qrcodes.size() != 0 && (!found_Id))
                {
                    txtResult.post(new Runnable() {
                        @Override
                        public void run() {
                            //Create vibrate
                            Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(300);
                            id= qrcodes.valueAt(0).displayValue;
                            found_Id=true;



                            if(found_Id){
                                mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(id);
                                mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String name = dataSnapshot.child("name").getValue().toString();
                                        mName.setText(name);

                                        String workshop = dataSnapshot.child("workshop").getValue().toString();
                                        mWorkshop.setText(workshop);

                                        String restau = dataSnapshot.child("restauration").getValue().toString();
                                        mRestau.setText(restau);

                                        String pack = dataSnapshot.child("pack").getValue().toString();
                                        mPack.setText(pack);

                                        String cuurent_selected_spinner = dataSnapshot.child("position").getValue().toString();
                                        if(cuurent_selected_spinner.equals("dev")) {
                                        }
                                        else{
                                            txtResult.setText(cuurent_selected_spinner);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                mSetPack.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mUserRef.child("pack").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                mPack.setText("ok");
                                            }
                                        });
                                    }
                                });
                                mSetResateu.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mUserRef.child("restauration").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                mRestau.setText("ok");
                                            }
                                        });
                                    }
                                });

                                setPosition = (Button) findViewById(R.id.setPositions);
                                setPosition.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mUserRef.child("position").setValue( spinner.getSelectedItem().toString());

                                    }
                                });




                            }



                        }
                    });
                }
            }
        });




            mRestart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    found_Id=false;
                    mWorkshop.setText("workshop");
                    mRestau.setText("Restauration");
                    mName.setText("Name");
                    mPack.setText("Pack");
                    txtResult.setText("Position");

                }
            });

            mLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    Intent sendToLogin = new Intent(MainActivity.this , WelcomActivity.class);
                    startActivity(sendToLogin);
                    finish();
                }
            });





    }
}
