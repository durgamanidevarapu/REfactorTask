package com.epam.engx.cleancode.finaltask.task1;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.Command;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DataSet;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DatabaseManager;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.View;


public class Print implements Command {

    private static final String EQUAL_SIGN = "═";
    private static final String SPACE = " ";
    private static final int EVEN_PREFIX = 2;
    private static final int COMMAND_PARTS_NUMBER = 2;
    private static final String TABLE_STRING = "║ Table '";
    private static final String IS_EMPTY_OR_DOES_NOT_EXIST_MESSAGE = "' is empty or does not exist ║";
    private static final String LINE = "║";
    private static final String NEW_LINE = "\n";
    private static final String PRINT_MESSAGE = "print ";
    public static final String INCORRECT_NUMBER_OF_PARAMETERS_ERROR_MESSAGE =
            "incorrect number of parameters. Expected 1, but is %s";

    private final View view;
    private final DatabaseManager databaseManager;

    private enum LevelBoundary {
        UPPER("╔", "╦", "╗"),
        MIDDLE("╠", "╬", "╣"),
        BOTTOM("╚", "╩", "╝");

        private final String leftBoundary;
        private final String middleBoundary;
        private final String rightBoundary;

        LevelBoundary(String leftBoundary, String middleBoundary, String rightBoundary) {
            this.leftBoundary = leftBoundary;
            this.middleBoundary = middleBoundary;
            this.rightBoundary = rightBoundary;
        }
    }

    public Print(View view, DatabaseManager databaseManager) {
        this.view = view;
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean canProcess(String command) {
        return command.startsWith(PRINT_MESSAGE);
    }

    @Override
    public void process(String input) {
        String[] command = input.split(SPACE);
        validateCommandPartsNumber(command.length);
        String tableName = command[1];
        List<DataSet> data = databaseManager.getTableData(tableName);
        view.write(convertTableDataToString(data,tableName));
    }

    private void validateCommandPartsNumber(int commandLength) {
        if (commandLength != COMMAND_PARTS_NUMBER) {
            throw new IllegalArgumentException(String.format(INCORRECT_NUMBER_OF_PARAMETERS_ERROR_MESSAGE,
                    (commandLength - 1)));
        }
    }

    private String convertTableDataToString(List<DataSet> data,String tableName) {
        int maxColumnSize;
        maxColumnSize = getMaxColumnSize(data);
        return maxColumnSize == 0 ? getEmptyTable(tableName) : (createHeaderForTable(data).
                                                                append(convertTableData(data))).toString();
    }

    private String getEmptyTable(String tableName) {
        String textEmptyTable = TABLE_STRING + tableName + IS_EMPTY_OR_DOES_NOT_EXIST_MESSAGE;

        StringBuilder result = new StringBuilder(LevelBoundary.UPPER.leftBoundary);
        int length = textEmptyTable.length() - EVEN_PREFIX;
        calculateColumn(result, length, EQUAL_SIGN);
        result.append(LevelBoundary.UPPER.rightBoundary + NEW_LINE);
        result.append(textEmptyTable + NEW_LINE);
        result.append(LevelBoundary.BOTTOM.leftBoundary);
        calculateColumn(result, length, EQUAL_SIGN);
        result.append(LevelBoundary.BOTTOM.rightBoundary + NEW_LINE);
        return result.toString();
    }

    private int getMaxColumnSize(List<DataSet> dataSets) {
        int maxLength = 0;

        if (isNotEmptyDataset(dataSets)) {
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

    private StringBuilder convertTableData(List<DataSet> dataSets) {
        int rowsCount = dataSets.size();
        int maxColumnSize = getMaxColumnSize(dataSets);
        StringBuilder result = new StringBuilder();
        maxColumnSize = updateMaxColumnSize(maxColumnSize);
        int columnCount = getColumnCount(dataSets);
        caluclateRowResult(dataSets, rowsCount, maxColumnSize, result, columnCount);
        result.append(LevelBoundary.BOTTOM.leftBoundary);
        createMiddleMidBoundary(maxColumnSize, result, columnCount, LevelBoundary.BOTTOM);
        calculateColumn(result, maxColumnSize, EQUAL_SIGN);
        result.append(LevelBoundary.BOTTOM.rightBoundary + NEW_LINE);
        return result;
    }

    private StringBuilder caluclateRowResult(List<DataSet> dataSets, int rowsCount, int maxColumnSize,
                                             StringBuilder result, int columnCount) {
        for (int row = 0; row < rowsCount; row++) {
            List<Object> values = dataSets.get(row).getValues();
            result.append(LINE);
            calculateColumnResult(maxColumnSize, result, columnCount, values);
            result.append(NEW_LINE);
            createMiddleLevel(rowsCount, maxColumnSize, result, columnCount, row);
        }
        return result;
    }

    private StringBuilder createMiddleLevel(int rowsCount, int maxColumnSize, StringBuilder result, int columnCount,
                                            int row) {
        if (row < rowsCount - 1) {
            result.append(LevelBoundary.MIDDLE.leftBoundary);
            createMiddleMidBoundary(maxColumnSize, result, columnCount, LevelBoundary.MIDDLE);
            calculateColumn(result, maxColumnSize, EQUAL_SIGN);
            result.append(LevelBoundary.MIDDLE.rightBoundary + NEW_LINE);
        }
        return result;
    }

    private StringBuilder calculateColumnResult(int maxColumnSize, StringBuilder result, int columnCount,
                                                List<Object> values) {
        for (int column = 0; column < columnCount; column++) {
            int valuesLength = String.valueOf(values.get(column)).length();
            calculateForColumns(maxColumnSize, result, valuesLength,
                    String.valueOf(values.get(column)));
            result = (valuesLength % EVEN_PREFIX == 0) ? result.append(LINE) : result.append(" "+LINE);
        }
        return result;
    }

    private StringBuilder createMiddleMidBoundary(int maxColumnSize, StringBuilder result, int columnCount,
                                                  LevelBoundary middle) {
        for (int j = 1; j < columnCount; j++) {
            calculateColumn(result, maxColumnSize, EQUAL_SIGN);
            result.append(middle.middleBoundary);
        }
        return result;
    }

    private int updateMaxColumnSize(int maxColumnSize) {
        return maxColumnSize % EVEN_PREFIX == 0 ? maxColumnSize+EVEN_PREFIX : maxColumnSize + 3;
    }

    private StringBuilder calculateForColumns(int maxColumnSize, StringBuilder result, int valuesLength, String s) {
        calculateColumn(result, (maxColumnSize - valuesLength) / EVEN_PREFIX, SPACE);
        result.append(s);
        calculateColumn(result, (maxColumnSize - valuesLength) / EVEN_PREFIX, SPACE);
        return result;
    }

    private StringBuilder calculateColumn(StringBuilder result, int i2, String s) {
        for (int j = 0; j < i2; j++) {
            result.append(s);
        }
        return result;
    }

    private int getColumnCount(List<DataSet> dataSets) {
        int result = 0;
        if (isNotEmptyDataset(dataSets)) {
            return dataSets.get(0).getColumnNames().size();
        }
        return result;
    }

    private StringBuilder createHeaderForTable(List<DataSet> dataSets) {
        int maxColumnSize = getMaxColumnSize(dataSets);
        StringBuilder result = new StringBuilder();
        int columnCount = getColumnCount(dataSets);
        maxColumnSize = getMaxColumnSize(maxColumnSize % EVEN_PREFIX == 0, maxColumnSize + EVEN_PREFIX, maxColumnSize + 3);
        createBottomLevel(maxColumnSize, result, columnCount, LevelBoundary.UPPER);
        calculteColumnHeaderResult(dataSets, maxColumnSize, result, columnCount);
        result.append(LINE+NEW_LINE);

        //last string of the header
        result = isNotEmptyDataset(dataSets) ? calculateLastStringOfHeader(maxColumnSize, result, columnCount) :
                createBottomLevel(maxColumnSize, result, columnCount, LevelBoundary.BOTTOM);
        return result;
    }

    private StringBuilder createBottomLevel(int maxColumnSize, StringBuilder result, int columnCount,
                                            LevelBoundary bottom) {
        result.append(bottom.leftBoundary);
        for (int j = 1; j < columnCount; j++) {
            calculateColumn(result, maxColumnSize, EQUAL_SIGN);
            result.append(bottom.middleBoundary);
        }
        calculateColumn(result, maxColumnSize, EQUAL_SIGN);
        result.append(bottom.rightBoundary+NEW_LINE);
        return result;
    }

    private boolean isNotEmptyDataset(List<DataSet> dataSets) {
        return !dataSets.isEmpty();
    }

    private StringBuilder calculteColumnHeaderResult(List<DataSet> dataSets, int maxColumnSize, StringBuilder result,
                                                     int columnCount) {
        List<String> columnNames = dataSets.get(0).getColumnNames();
        for (int column = 0; column < columnCount; column++) {
            result.append(LINE);
            int columnNamesLength = columnNames.get(column).length();
            if(columnNamesLength % EVEN_PREFIX == 0){
                calculateForColumns(maxColumnSize, result, columnNamesLength,
                        columnNames.get(column));
            }else {
                calForOddColumnNames(maxColumnSize, result, columnNames, column, columnNamesLength);
            }
        }
        return result;
    }

    private int getMaxColumnSize(boolean isValid, int i, int i2) {
        return isValid ? i : i2;
    }

    private StringBuilder calForOddColumnNames(int maxColumnSize, StringBuilder result, List<String> columnNames, int column,
                                        int columnNamesLength) {
        calculateColumn(result, (maxColumnSize - columnNamesLength) / EVEN_PREFIX, SPACE);
        result.append(columnNames.get(column));
        calculateColumn(result, (maxColumnSize - columnNamesLength) / EVEN_PREFIX+1, SPACE);
        return result;
    }

    private StringBuilder calculateLastStringOfHeader(int maxColumnSize, StringBuilder result, int columnCount) {
        createBottomLevel(maxColumnSize, result, columnCount, LevelBoundary.MIDDLE);
        return result;
    }
}
