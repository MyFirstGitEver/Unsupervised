package org.example;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class ExcelReader {
    private final XSSFWorkbook workbook;

    ExcelReader(String path) throws IOException {
        File file = new File(path);

        FileInputStream fIn = new FileInputStream(file);
        workbook = new XSSFWorkbook(fIn);

        fIn.close();
    }

    private Object[] getRow(int number, int sheetNum, List<String> features, int labelCol) {
        XSSFSheet sheet = workbook.getSheetAt(sheetNum);

        XSSFRow headers = sheet.getRow(0);
        Object[] data = new Object[features.size() + 1];

        XSSFRow row = sheet.getRow(number);

        int featureIndex = 0;
        for(int i=0;i<headers.getLastCellNum();i++) {
            if(!features.contains(headers.getCell(i).getStringCellValue().toLowerCase())) {
                continue; // we don't need this feature
            }

            if(row.getCell(i) == null) {
                data[featureIndex] = null;
                continue;
            }


            if(row.getCell(i).getCellType() == CellType.STRING) {
                data[featureIndex] = row.getCell(i).getStringCellValue();
            }
            else {
                data[featureIndex] = row.getCell(i).getNumericCellValue();
            }

            featureIndex++;
        }

        if(labelCol == Integer.MAX_VALUE) {
            data[data.length - 1] = 0.0;
        }
        else if(row.getCell(labelCol).getCellType() == CellType.STRING) {
            data[data.length - 1] = row.getCell(labelCol).getStringCellValue();
        }
        else {
            data[data.length - 1] = row.getCell(labelCol).getNumericCellValue();
        }

        return data;
    }

    public Pair<Vector, Float>[] createLabeledDataset(
            int labelCol,
            int sheetNum,
            List<String> features) {

        Pair<Vector, Float>[] dataset = new Pair[getRowCount() - 1];
        HashMap<String, Integer>[] hms = new HashMap[features.size() + 1];

        for (int i = 0; i < dataset.length; i++) {
            Object[] data;
            try {
                data = getRow(i + 1, sheetNum, features, labelCol);
            } catch (Exception e) {
                break;
            }

            float[]  points = new float[data.length - 1];
            float label = 0;

            for(int j=0;j<data.length;j++) {
                float numericValue;

                if(data[j] == null) {
                    numericValue = 0;
                }
                else if(data[j] instanceof String){
                    if(hms[j] == null){
                        hms[j] = new HashMap<>();
                    }

                    if(hms[j].get(data[j]) == null){
                        hms[j].put((String) data[j], hms[j].size());
                    }

                    numericValue = hms[j].get(data[j]);
                }
                else {
                    numericValue = ((Double)data[j]).floatValue();
                }

                if(j == data.length - 1) {
                    label = numericValue;
                    break;
                }

                points[j] = numericValue;
            }

            dataset[i] = new Pair<>(new Vector(points), label);
        }

        return dataset;
    }

    public int getRowCount() {
        return workbook.getSheetAt(0).getLastRowNum() + 1;
    }
}
