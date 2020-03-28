package com.saltechdigital.coronavirus.decorator;

import android.content.Context;

import com.jjoe64.graphview.series.DataPoint;
import com.saltechdigital.coronavirus.utils.CSVParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DataSourceDecorator implements DataSource {
    private DataSource wrapper;
    private Context context;
    protected List csv;

    public DataSourceDecorator(DataSource wrapper, Context context) {
        this.wrapper = wrapper;
        this.context = context;
    }

    @Override
    public List writeData(String source) throws IOException {
        wrapper.writeData(source);
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("yesterday", "json", outputDir);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(source);
        writer.close();

        CSVParser parser = new CSVParser(context);
        csv = parser.read(outputFile);
        return wrapper.writeData(source);
    }

    @Override
    public DataPoint[] readData() {
        return wrapper.readData();
    }
}
