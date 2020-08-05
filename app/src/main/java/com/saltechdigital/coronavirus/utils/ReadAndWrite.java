package com.saltechdigital.coronavirus.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ReadAndWrite {

    public static void writeToFile(String data, Context context, String filename) throws IOException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
        outputStreamWriter.write(data);
        outputStreamWriter.close();
    }

    public static String readFromFile(Context context, String filename) throws IOException {
        String ret = "";
        InputStream inputStream = context.openFileInput(filename);
        if (inputStream != null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receivereader = "";
            StringBuilder builder = new StringBuilder();
            while ( (receivereader = bufferedReader.readLine()) != null){
                builder.append("\n").append(receivereader);
            }

            inputStream.close();
            ret = builder.toString();
        }

        return ret;
    }
}
