package com.example.hw522exportinternalfile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private static final String FILE_NAME = "user_data.txt";
    private static final String SHARED_PREF = "sharedPref";
    private static final String STATE_CHB_STORAGE_KEY = "stateChbStorage";

    boolean stateChbStorage;
    private Button btnLogin;
    private Button btnRegistration;
    private EditText edtLogin;
    private EditText edtPassword;
    private CheckBox chbStorage;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        sharedPref = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegistration = findViewById(R.id.btnRegistration);
        edtLogin = findViewById(R.id.edtLogin);
        edtPassword = findViewById(R.id.edtPassword);

        chbStorage = findViewById(R.id.chbStorage);
        stateChbStorage = sharedPref.getBoolean(STATE_CHB_STORAGE_KEY, false);
        chbStorage.setChecked(stateChbStorage);
        chbStorage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                stateChbStorage = !stateChbStorage;
                sharedPref.edit().putBoolean(STATE_CHB_STORAGE_KEY, stateChbStorage).apply();

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edtLogin.getText()) ||
                        TextUtils.isEmpty(edtPassword.getText())) {
                    showToast(getString(R.string.error_empty_fields));
                    return;
                }

                String login = edtLogin.getText().toString();
                String password = edtPassword.getText().toString();

                if (stateChbStorage) {
                    loginExternalFile(login, password);
                } else {
                    loginInternalFile(login, password);
                }

            }
        });

        btnRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(edtLogin.getText()) ||
                        TextUtils.isEmpty(edtPassword.getText())) {
                    showToast(getString(R.string.error_empty_fields));
                    return;
                }

                String login = edtLogin.getText().toString();
                String password = edtPassword.getText().toString();

                if (stateChbStorage) {
                    registrationExternalFile(login, password);
                } else {
                    registrationInternalFile(login, password);
                }

            }
        });
    }

    private void loginExternalFile(String login, String password) {
        File userDataFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), FILE_NAME);

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(userDataFile);
        } catch (IOException e) {
            showToast(getString(R.string.error));
            e.printStackTrace();
            return;
        }

        Scanner scanner = new Scanner(fileReader);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            String[] splitLine = line.split(";");
            String curLogin = splitLine[0];
            String curPassword = splitLine[1];

            if (curLogin.equals(login) && curPassword.equals(password)) {
                showToast(getString(R.string.successful_login));
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        try {
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        showToast(getString(R.string.error_user_not_found));
    }

    private void registrationExternalFile(String login, String password) {
        File userDataFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), FILE_NAME);

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(userDataFile);
        } catch (IOException e) {
            showToast(getString(R.string.error));
            e.printStackTrace();
            return;
        }

        if (userDataFile.exists()) {
            try {
                fileWriter.append(login + ";" + password + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                fileWriter.write(login + ";" + password + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        showToast(getString(R.string.successful_registration));


    }

    private void registrationInternalFile(String login, String password) {
        FileOutputStream fileOutputStream = null;
        try {
            if (new File(getFilesDir(), FILE_NAME).exists()) {
                fileOutputStream = openFileOutput(FILE_NAME, MODE_APPEND);
            } else {
                fileOutputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fileOutputStream == null) {
            showToast(getString(R.string.error_file_not_found));
            return;
        }

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        try {
            bufferedWriter.write(login + ";" + password + "\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            showToast(getString(R.string.error));
            e.printStackTrace();
        }

        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        showToast(getString(R.string.successful_registration));

    }

    private void loginInternalFile(String login, String password) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fileInputStream == null) {
            showToast(getString(R.string.error_file_not_found));
            return;
        }

        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line = null;
        try {
            line = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (line != null) {
            String[] splitLine = line.split(";");
            String curLogin = splitLine[0];
            String curPassword = splitLine[1];

            if (curLogin.equals(login) && curPassword.equals(password)) {
                showToast(getString(R.string.successful_login));
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        showToast(getString(R.string.error_user_not_found));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
