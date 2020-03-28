package com.saltechdigital.coronavirus.decorator;

import com.jjoe64.graphview.series.DataPoint;

import java.io.IOException;
import java.util.List;

public interface DataSource {
    List writeData(String source) throws IOException;
    DataPoint[] readData();
}
