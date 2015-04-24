package com.hirebigdata.spider.zhilian.utils;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/23
 */
public class AccessExcel {
    final public static boolean CONTAIN_BIG_WORD = true;
    final public static boolean NOT_CONTAIN_BIG_WORD = false;

    public static void main(String[] args) {
//        System.out.println(getBigWords("keywords.xls"));
//        System.out.println(getSmallWords("keywords.xls", 16, CONTAIN_BIG_WORD).size());
//        while (true)
//        System.out.println(getRandomKeywords("keywords.xls"));
        System.out.println(getRandomKeywords("keywords.xls", "战略"));

    }

    public static HashMap<String, String> getRandomKeywords(String filePath, String bigWord) {
        HashMap<String, String> result = new HashMap<>();
        List<String> bigWords = getBigWords(filePath);
        int index = bigWords.indexOf(bigWord);
        List<String> smallWords = getSmallWords(filePath, index, CONTAIN_BIG_WORD);
        result.put(bigWord, smallWords.get(new Random().nextInt(smallWords.size())));
        return result;
    }

    public static HashMap<String, String> getRandomKeywords(String filePath) {
        HashMap<String, String> result = new HashMap<>();
        HashMap<String, List<String>> keywords = new HashMap<>();
        List<String> bigWords = getBigWords(filePath);
        for (int i = 0; i < bigWords.size(); i++) {
            List<String> smallWords = getSmallWords(filePath, i, CONTAIN_BIG_WORD);
            keywords.put(bigWords.get(i), smallWords);
        }
        int j = new Random().nextInt(bigWords.size());
        result.put(bigWords.get(j), keywords.get(bigWords.get(j)).get(new Random().nextInt(keywords.get(bigWords.get(j)).size())));
        return result;
    }

    public static List<String> getSmallWords(String filePath, int bigWordIndex, boolean containBigWord) {
        List<String> smallWords = new ArrayList<>();
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filePath));
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(bigWordIndex);
            for (Cell cell : row) {
                if (cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                    String cellStr = cell.toString();
                    for (String s : cellStr.split("/")) {
                        smallWords.add(s);
                    }
                    if (!containBigWord)
                        smallWords.remove(row.getCell(0).toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return smallWords;
    }

    public static List<String> getBigWords(String filePath) {
        List<String> bigWords = new LinkedList<String>();
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            POIFSFileSystem fs = new POIFSFileSystem(fileInputStream);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                bigWords.add(row.getCell(0).toString());
            }
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bigWords;
    }

    public static void getAllWords(String filePath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            POIFSFileSystem fs = new POIFSFileSystem(fileInputStream);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                        System.out.println(cell);
                    }
                }
            }
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
