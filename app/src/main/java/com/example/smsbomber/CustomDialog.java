package com.example.smsbomber;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import org.w3c.dom.Text;

public class CustomDialog extends Dialog implements android.view.View.OnClickListener {

    private Activity c;
    private Dialog d;
    private Button bomb;
    private EditText smsBody;
    private int smsCount;

    public CustomDialog(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        bomb = (Button) findViewById(R.id.dialogbutton);
        bomb.setOnClickListener(this);
        smsBody = (EditText) findViewById(R.id.smsBody);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        try {
            Log.i("bombinput", String.valueOf(((EditText) findViewById(R.id.editTextNumber)).getText()));
            smsCount = Integer.parseInt(String.valueOf(((EditText) findViewById(R.id.editTextNumber)).getText()));
        } catch (NumberFormatException e) {
            smsCount = 0;
        }

        MainActivity.getInstance().bomb(smsCount, smsBody.getText().toString());
        dismiss();
    }
}
