package com.saltechdigital.coronavirus.decorator;

import android.content.Context;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.saltechdigital.coronavirus.utils.CSVParser;
import com.saltechdigital.coronavirus.utils.Final;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileDataSource implements DataSource {
    private String source;
    private Context context;

    public FileDataSource(Context context, String source) {
        super();
        this.context = context;
        this.source = source;
    }

    @Override
    public List writeData(String source) throws IOException {
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("yesterday", "json", outputDir);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(source);
        writer.close();

        CSVParser parser = new CSVParser(context);
        return parser.read(outputFile);
    }

    @Override
    public DataPoint[] readData() {
        try {
            this.writeData(source);
        } catch (IOException e) {
            Log.d(Final.TAG, "FileDataSource -> une erreur est survenue: ", e);
        }
        return null;
    }
}
