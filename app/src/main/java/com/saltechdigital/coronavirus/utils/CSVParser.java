package com.saltechdigital.coronavirus.utils;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {
    private Context context;

    public CSVParser(Context context) {
        this.context = context;
    }

    public List read(File file) {
        List resultList = new ArrayList();
        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String csvLine;
            final char Separator = ',';
            final char Delimiter = '"';
            final char LF = '\n';
            final char CR = '\r';
            boolean quote_open = false;
            while ((csvLine = reader.readLine()) != null) {
                //String[] row = csvLine.split(",");// simple way
                StringDArray a = new StringDArray();
                String token = "";
                csvLine += Separator;
                for (char c : csvLine.toCharArray()) {
                    switch (c) {
                        case LF:
                        case CR:// not required as we are already read line
                            quote_open = false;
                            a.add(token);
                            token = "";
                            break;
                        case Delimiter:
                            quote_open = !quote_open;
                            break;
                        case Separator:
                            if (quote_open == false) {
                                a.add(token);
                                token = "";
                            } else {
                                token += c;
                            }
                            break;
                        default:
                            token += c;
                            break;
                    }
                }
                if (a.length() > 0) {
                    if (resultList.size() > 0) {
                        String[] header_row = (String[]) resultList.get(0);
                        if (a.length() >= header_row.length) {
                            String[] row = a.get_araay();
                            resultList.add(row);
                        }
                    } else {
                        String[] row = a.get_araay();
                        resultList.add(row);//header row
                    }
                }
            }
            inputStream.close();
        } catch (Exception e) {
            Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return resultList;
    }

    private class StringDArray {
        private String[] data = new String[0];
        private int used = 0;

        public void add(String str) {
            if (used >= data.length) {
                int new_size = used + 1;
                String[] new_data = new String[new_size];
                java.lang.System.arraycopy(data, 0, new_data, 0, used);
                data = new_data;
            }
            data[used++] = str;
        }

        public int length() {
            return used;
        }

        public String[] get_araay() {
            return data;
        }
    }
}
