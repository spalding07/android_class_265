package com.example.user.simpleui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    EditText editText;
    RadioGroup radioGroup;
    String sex = "";
    String selectedSex = "Male";
    String name = "";
    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        checkBox = (CheckBox) findViewById(R.id.hideCheckBox);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
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

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.maleRadioButton) {
                    selectedSex = "Male";
                } else if (checkedId == R.id.femaleRadioButton) {
                    selectedSex = "Female";
                }

            }
        });


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (name != "") {
                    changeTextView();
                }
            }
        });
    }

    public void click(View view) {
        name = editText.getText().toString();
        sex = selectedSex;
        changeTextView();
        editText.setText("");
    }

    public void changeTextView() {
        if (checkBox.isChecked()) {
            String text = name;
            textView.setText(text);
        } else {
            String text = name + " sex: " + sex;
            textView.setText(text);
        }
    }
}