package com.example.user.simpleui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MENU_ACTIVITY = 0;    //final的東西,一定要寫成大寫
    private static final int REQUEST_CODE_CAMERA_ACTIVITY = 1;    //final的東西,一定要寫成大寫

    private boolean hasPhoto = false;   //是否有拍照片

    TextView textView;
    EditText editText;
    RadioGroup radioGroup;
    ArrayList<Order> orders;
    String drinkName;
    String note = "";
    CheckBox checkBox;
    Spinner spinner;
    ProgressBar progressBar;
    ProgressDialog progressDialog;
    ImageView photoImageView;

    String menuResults = "";
    ListView listView;

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("debug", "Main Activity On Created");
        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        checkBox = (CheckBox) findViewById(R.id.hideCheckBox);
        listView = (ListView) findViewById(R.id.listView);
        spinner = (Spinner) findViewById(R.id.spinner);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        photoImageView = (ImageView) findViewById(R.id.imageView);
        progressDialog = new ProgressDialog(this);
        orders = new ArrayList<>();

        sp = getSharedPreferences("setting", Context.MODE_PRIVATE); //取得 setting 這本字典
        editor = sp.edit(); //把setting這本字典專用的筆拿出來

        // Create a RealmConfiguration which is to locate Realm file in package's "files" directory.
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();

        Realm.setDefaultConfiguration(realmConfig);

        // Get a Realm instance for this thread
        realm = Realm.getDefaultInstance();

        editText.setText(sp.getString("editText", ""));

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String text = editText.getText().toString();
                editor.putString("editText", text);
                editor.apply();
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    click(v);
                    return true;    //true -> 攔截下來，結束，不會回傳到EditText上面  ； false 會繼續執行，介面就會接到 Enter
                }
                return false;
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {    //介面要設定single line，不然enter的意思是換行
                    click(v);
                    return true;
                }
                return false;
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    photoImageView.setVisibility(View.GONE);
                } else {
                    photoImageView.setVisibility(View.VISIBLE);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order = (Order) parent.getAdapter().getItem(position);
                //Toast.makeText(MainActivity.this, order.note, Toast.LENGTH_SHORT).show();
                Snackbar.make(view, order.getNote(), Snackbar.LENGTH_LONG).show();
            }
        });
        setupListView();
        setupSpinner();

        // ------ 作業1 start ------
        int spinnerSelected = sp.getInt("spinnerSelected", 0);
        spinner.setSelection(spinnerSelected);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("spinnerSelected", position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // ------ 作業1 end ------
    }

    public void setupSpinner() {
        //從 設定檔 取得清單
//        String[] data = getResources().getStringArray(R.array.storeInfo);
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, data);
//        spinner.setAdapter(arrayAdapter);

        //從 parse Server 取得清單
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("StoreInfo");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {    //錯誤訊息
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                int size = objects.size();
                String[] list = new String[size];
                for (int i = 0; i < size; i++) {
                    list[i] = objects.get(i).getString("name");
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, list);
                spinner.setAdapter(arrayAdapter);
            }
        });

    }

    public void setupListView() {
        //進度條 顯示
        progressBar.setVisibility(View.VISIBLE);

        final RealmResults results = realm.allObjects(Order.class);
        OrderAdapter orderAdapter = new OrderAdapter(MainActivity.this, results.subList(0, results.size()));
        listView.setAdapter(orderAdapter);

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Order");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    //進度條 隱藏
                    progressBar.setVisibility(View.GONE);

                    return;
                }
                List<Order> orders = new ArrayList<Order>();

                Realm realm = Realm.getDefaultInstance();

                for (int i = 0; i < objects.size(); i++) {
                    Order order = new Order();
                    order.setNote(objects.get(i).getString("note"));
                    order.setStoreInfo(objects.get(i).getString("storeInfo"));
                    order.setMenuResults(objects.get(i).getString("menuResults"));
                    orders.add(order);

                    if (results.size() <= i) {
                        realm.beginTransaction();
                        realm.copyToRealm(order);
                        realm.commitTransaction();
                    }
                }
                realm.close();

                progressBar.setVisibility(View.GONE);

                OrderAdapter adapter = new OrderAdapter(MainActivity.this, orders);
                listView.setAdapter(adapter);
            }
        });
    }

    public void click(View view) {

        progressDialog.setTitle("Loading...");
        progressDialog.show();

        note = editText.getText().toString();
        String text = note;
        textView.setText(text);

        Order order = new Order();
        order.setMenuResults(menuResults);
        order.setNote(note);
        order.setStoreInfo((String) spinner.getSelectedItem());

        if (hasPhoto) {
            //讀取圖片
            Uri uri = Utils.getPhotoURI();
            byte[] photo = Utils.uriToBytes(this, uri);
            if (photo == null) {
                Log.d("Debug", "Read Photo Fail");
            } else {
                order.photo = photo;
            }
        }

        SaveCallBackWithRealm saveCallBackWithRealm = new SaveCallBackWithRealm(order, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, "Save Fail", Toast.LENGTH_LONG).show();
                }
                editText.setText("");
                menuResults = "";

                //圖檔上傳成功後，刪除圖檔，flag設定回false
                photoImageView.setImageResource(0);
                hasPhoto = false;

                progressDialog.dismiss();

                setupListView();
            }
        }
        );

        //儲存到遠端
        order.saveToRemote(saveCallBackWithRealm);


    }

    public void goToMenu(View view) {
        Intent intent = new Intent();   //呼叫Activity的媒介

        intent.setClass(this, DrinkMenuActivity.class);

        startActivityForResult(intent, REQUEST_CODE_MENU_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MENU_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                menuResults = data.getStringExtra("result");
            }
        } else if (requestCode == REQUEST_CODE_CAMERA_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                photoImageView.setImageURI(Utils.getPhotoURI());
                hasPhoto = true;
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_take_photo) {
            Toast.makeText(this, "Take Photo", Toast.LENGTH_LONG).show();
            goToCamera();
        }
        return super.onOptionsItemSelected(item);
    }

    public void goToCamera() {
        //.確定是否有同意使用SD卡
        if (Build.VERSION.SDK_INT >= 23) {  //確定版本 (>23版)，是否有使用者允許
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {    //若尚未允許
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                return;
            }
        }
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getPhotoURI());  //內存資源使用URI
        startActivityForResult(intent, REQUEST_CODE_CAMERA_ACTIVITY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("debug", "Main Activity OnStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("debug", "Main Activity OnResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("debug", "Main Activity OnPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("debug", "Main Activity OnStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        Log.d("debug", "Main Activity OnDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("debug", "Main Activity OnRestart");
    }
}