package com.saltechdigital.coronavirus.decorator;

import android.content.Context;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.saltechdigital.coronavirus.factory.CountryFactory;
import com.saltechdigital.coronavirus.models.ContaminatedCountry;
import com.saltechdigital.coronavirus.utils.CSVParser;
import com.saltechdigital.coronavirus.utils.Final;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecoverDataDecorator extends DataSourceDecorator {

    private String translatedName;
    private Context context;

    public RecoverDataDecorator(DataSource wrapper, Context context, String translatedName) {
        super(wrapper, context);
        this.context = context;
        this.translatedName = translatedName;
    }

    @Override
    public List writeData(String source) throws IOException {
        return super.writeData(source);
    }

    @Override
    public DataPoint[] readData() {
        List<DataPoint> pointList = new ArrayList<>();
        if (csv.size() > 0) {
            String[] header_row = (String[]) csv.get(0);
            for (int j = 1; j < csv.size(); j++) {
                String[] data = (String[]) csv.get(j);
                if (data[1].equals(translatedName)) {
                    for (int i = 0; i < header_row.length; i++) {
                        if (i > 3) {
                            //Log.d(Final.TAG, "Pays : " + data[1] + " Ensuite" + header_row[i] + "onResponse: " + data[i]);

                            String name = header_row[i];

                            CountryFactory factory = new CountryFactory(name, null, 0, 0, Integer.parseInt(data[i]));
                            ContaminatedCountry ntry = (ContaminatedCountry) factory.getCountry(Final.CONTAMINATED);

                            String dateInString = ntry.getName();
                            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                            try {
                                Date parsedDate = formatter.parse(dateInString);
                                if (parsedDate != null && ntry.getHealing() != 0) {
                                    DataPoint point = new DataPoint(parsedDate, ntry.getHealing());
                                    pointList.add(point);
                                }
                            } catch (ParseException e) {
                                //Log.d(Final.TAG, "readData: ", e);
                            }
                        }
                    }
                }
            }
        }
        //Log.d(Final.TAG, "pointlist: "+pointList.size());
        DataPoint[] points = pointList.toArray(new DataPoint[]{});
        //Log.d(Final.TAG, "readData: "+points.length);
        return points;
    }
}
