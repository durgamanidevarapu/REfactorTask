package com.epam.engx.cleancode.finaltask.task1;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.Command;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DataSet;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DatabaseManager;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.View;


public class Print implements Command {

    public static final String EQUAL_SIGN = "═";
    public static final String SPACE = " ";
    public static final int EVEN_LENGTH = 2;
    public static final String TABLE_STRING = "║ Table '";
    public static final String IS_EMPTY_OR_DOES_NOT_EXIST_MESSAGE = "' is empty or does not exist ║";
    public static final String LINE_START_MSG = "╔";
    public static final String LINE_END_STRING = "╗";
    public static final String LAST_LINE_START_STRING = "╚";
    public static final String LAST_LINE_END_STRING = "╝";
    public static final String LINE_MIDDLE_STRING = "║";
    public static final String MIDDLE_LINE_STRING = "╩";
    public static final String SIDE_LINE_START_STRING = "╠";
    public static final String MIDDLE_STRING = "╬";
    public static final String SIDE_LINE_END_STRING = "╣";
    public static final String MIDDLE_LAST_STRING = "╦";
    public static final String NEW_LINE = "\n";
    public static final String PRINT_MESSAGE = "print ";
    private View view;
    private DatabaseManager databaseManager;
    private String tableName;

    public Print(View view, DatabaseManager databaseManager) {
        this.view = view;
        this.databaseManager = databaseManager;
    }

    public boolean canProcess(String command) {
        return command.startsWith(PRINT_MESSAGE);
    }

    public void process(String input) {
        String[] command = input.split(SPACE);
        isValidCommand(command);
        tableName = command[1];
        List<DataSet> data = databaseManager.getTableData(tableName);
        view.write(convertTableDataToString(data));
    }

    private void isValidCommand(String[] command) {
        if (command.length != EVEN_LENGTH) {
            throw new IllegalArgumentException("incorrect number of parameters. Expected 1, but is " + (command.length - 1));
        }
    }

    private String convertTableDataToString(List<DataSet> data) {
        int maxColumnSize;
        maxColumnSize = getMaxColumnSize(data);
        return maxColumnSize == 0 ? getEmptyTable(tableName) : (createHeaderForTable(data) + convertTableData(data));
    }

    private String getEmptyTable(String tableName) {
        String textEmptyTable = TABLE_STRING + tableName + IS_EMPTY_OR_DOES_NOT_EXIST_MESSAGE;
        String result = LINE_START_MSG;
        int length = textEmptyTable.length() - EVEN_LENGTH;
        result = calculateColumn(result, length, EQUAL_SIGN);
        result += LINE_END_STRING + NEW_LINE;
        result += textEmptyTable + NEW_LINE;
        result += LAST_LINE_START_STRING;
        result = calculateColumn(result, length, EQUAL_SIGN);
        result += LAST_LINE_END_STRING + NEW_LINE;
        return result;
    }

    private int getMaxColumnSize(List<DataSet> dataSets) {
        int maxLength = 0;

        if (!dataSets.isEmpty()) {
            List<String> columnNames = dataSets.get(0).getColumnNames();
            maxLength = Collections.max(columnNames).length();
            for (DataSet dataSet : dataSets) {
                List<Object> dataSetValues = dataSet.getValues();
                Object dataSetValue  = getMaxDataSetValue(dataSetValues);
                int maxValue = String.valueOf(dataSetValue).length();
                maxLength = getMaxColumnSize(maxValue>maxLength, maxValue, maxLength);
            }
        }
        return maxLength;
    }

    private Object getMaxDataSetValue(List<Object> values) {
        return Collections.max(values, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return String.valueOf(o1).length() - String.valueOf(o2).length();
            }
        });
    }

    private String convertTableData(List<DataSet> dataSets) {
        int rowsCount;
        rowsCount = dataSets.size();
        int maxColumnSize = getMaxColumnSize(dataSets);
        String result = "";
        if (maxColumnSize % EVEN_LENGTH == 0) {
            maxColumnSize += EVEN_LENGTH;
        } else {
            maxColumnSize += 3;
        }
        int columnCount = getColumnCount(dataSets);
        for (int row = 0; row < rowsCount; row++) {
            List<Object> values = dataSets.get(row).getValues();
            result = result.concat(LINE_MIDDLE_STRING);
            for (int column = 0; column < columnCount; column++) {
                int valuesLength = String.valueOf(values.get(column)).length();
                result = calculateForColumns(maxColumnSize, result, valuesLength,
                        String.valueOf(values.get(column)));
                result= (valuesLength % EVEN_LENGTH == 0) ? result+LINE_MIDDLE_STRING : result+" "+LINE_MIDDLE_STRING;
            }
            result = result.concat(NEW_LINE);
            if (row < rowsCount - 1) {
                result = result.concat(SIDE_LINE_START_STRING);
                for (int j = 1; j < columnCount; j++) {
                    result = calculateColumn(result, maxColumnSize, EQUAL_SIGN);
                    result = result.concat(MIDDLE_STRING);
                }
                result = calculateColumn(result, maxColumnSize, EQUAL_SIGN);
                result = result.concat(SIDE_LINE_END_STRING + NEW_LINE);
            }
        }
        result = result.concat(LAST_LINE_START_STRING);
        for (int j = 1; j < columnCount; j++) {
            result = calculateColumn(result, maxColumnSize, EQUAL_SIGN);
            result = result.concat(MIDDLE_LINE_STRING);
        }
        result = calculateColumn(result, maxColumnSize, EQUAL_SIGN);
        result = result.concat(LAST_LINE_END_STRING + NEW_LINE);
        return result;
    }

    private String calculateForColumns(int maxColumnSize, String result, int valuesLength, String s) {
        result = calculateColumn(result, (maxColumnSize - valuesLength) / EVEN_LENGTH, SPACE);
        result += s;
        result = calculateColumn(result, (maxColumnSize - valuesLength) / EVEN_LENGTH, SPACE);
        return result;
    }

    private String calculateColumn(String result, int i2, String s) {
        for (int j = 0; j < i2; j++) {
            result = result.concat(s);
        }
        return result;
    }

    private int getColumnCount(List<DataSet> dataSets) {
        int result = 0;
        if (!dataSets.isEmpty()) {
            return dataSets.get(0).getColumnNames().size();
        }
        return result;
    }

    private String createHeaderForTable(List<DataSet> dataSets) {
        int maxColumnSize = getMaxColumnSize(dataSets);
        String result = "";
        int columnCount = getColumnCount(dataSets);
        maxColumnSize = getMaxColumnSize(maxColumnSize % EVEN_LENGTH == 0, maxColumnSize + EVEN_LENGTH, maxColumnSize + 3);
        result += LINE_START_MSG;
        for (int j = 1; j < columnCount; j++) {
            result = calculateColumn(result, maxColumnSize, EQUAL_SIGN);
            result = result.concat(MIDDLE_LAST_STRING);
        }
        result = calculateColumn(result, maxColumnSize, EQUAL_SIGN);
        result += LINE_END_STRING + NEW_LINE;
        List<String> columnNames = dataSets.get(0).getColumnNames();
        for (int column = 0; column < columnCount; column++) {
            result = result.concat(LINE_MIDDLE_STRING);
            int columnNamesLength = columnNames.get(column).length();
            result = (columnNamesLength % EVEN_LENGTH == 0) ? calculateForColumns(maxColumnSize, result, columnNamesLength,
                    columnNames.get(column)) :
                    calForOddColumnNames(maxColumnSize, result, columnNames, column, columnNamesLength);
        }
        result = result.concat(LINE_MIDDLE_STRING+NEW_LINE);

        //last string of the header
        if (!dataSets.isEmpty()) {
            result = calculateLastStringOfHeader(maxColumnSize, result, columnCount);
        } else {
            result = result.concat(LAST_LINE_START_STRING);
            for (int j = 1; j < columnCount; j++) {
                result = calculateColumn(result, maxColumnSize, EQUAL_SIGN);
                result = result.concat(MIDDLE_LINE_STRING);
            }
            result = calculateColumn(result, maxColumnSize, EQUAL_SIGN);
            result = result.concat(LAST_LINE_END_STRING + NEW_LINE);
        }
        return result;
    }

    private int getMaxColumnSize(boolean isValid, int i, int i2) {
        return isValid ? i : i2;
    }

    private String calForOddColumnNames(int maxColumnSize, String result, List<String> columnNames, int column,
                                        int columnNamesLength) {
        result = calculateColumn(result, (maxColumnSize - columnNamesLength) / EVEN_LENGTH, SPACE);
        result += columnNames.get(column);
        result = calculateColumn(result, (maxColumnSize - columnNamesLength) / EVEN_LENGTH+1, SPACE);
        return result;
    }

    private String calculateLastStringOfHeader(int maxColumnSize, String result, int columnCount) {
        result = result.concat(SIDE_LINE_START_STRING);
        for (int j = 1; j < columnCount; j++) {
            result = calculateColumn(result, maxColumnSize, EQUAL_SIGN);
            result = result.concat(MIDDLE_STRING);
        }
        result = calculateColumn(result, maxColumnSize, EQUAL_SIGN);
        result = result.concat(SIDE_LINE_END_STRING + NEW_LINE);
        return result;
    }
}
