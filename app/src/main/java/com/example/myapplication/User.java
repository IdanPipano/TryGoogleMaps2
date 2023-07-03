package com.example.myapplication;

import android.content.Context;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.maps.MapsInitializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class User {
    String email, password, userName;
    List<List<Double>> matrix = new ArrayList<>();
    List<List<Double>> ata_inverse;
    List<Double> atb;

    public User(String email, String password, String userName, Context context) {
        this.email = email;
        this.password = password;
        this.userName = userName;
        List<Double> row1 = Arrays.asList(1.1, 2., 3.);
        List<Double> row2 = Arrays.asList(-4.5, -5.7, 6.3);
        matrix.add(row1);
        matrix.add(row2);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        Python py = Python.getInstance();
        List<PyObject> pyMatVec = py.getModule("test").callAttr("randomMatVec", MapsActivity.num_features).asList();
        ata_inverse = MapsActivity.numpyMatrixToJavaListList(pyMatVec.get(0));
        atb = MapsActivity.numpyVectorToJavaList(pyMatVec.get(1));
    }
    
    public User() {

    }

    public List<List<Double>> getAta_inverse() {
        return ata_inverse;
    }

    public List<Double> getAtb() {
        return atb;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public List<List<Double>> getMatrix() {
        return matrix;
    }
}
