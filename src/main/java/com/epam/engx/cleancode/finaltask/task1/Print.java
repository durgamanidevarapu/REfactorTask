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
    private static final String IS_EMPTY_OR_DOES_NOT_EXIST_TABLE_MESSAGE = "║ Table '%s' is empty or does not exist ║";
    private static final String LINE = "║";
    private static final String NEW_LINE = "\n";
    private static final String PRINT_MESSAGE = "print ";
    private static final String INCORRECT_NUMBER_OF_PARAMETERS_ERROR_MESSAGE ="incorrect number of parameters." +
            " Expected %d, but is %s";
    private static final int ODD_PREFIX = 3;

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
        String[] commands = input.split(SPACE);
        isValidCommand(commands);
        String tableName = commands[1];
        List<DataSet> data = databaseManager.getTableData(tableName);
        view.write(convertTableDataToString(data,tableName));
    }

    private void isValidCommand(String[] command) {
        if (command.length != COMMAND_PARTS_NUMBER) {
            throw new IllegalArgumentException(String.format(INCORRECT_NUMBER_OF_PARAMETERS_ERROR_MESSAGE, 1,
                    command.length - 1));
        }
    }

    private String convertTableDataToString(List<DataSet> data,String tableName) {
        int maxColumnSize;
        maxColumnSize = getMaxColumnSize(data);
        return maxColumnSize == 0 ? getEmptyTable(tableName) : (createHeaderForTable(data) + convertTableData(data));
    }

    private String getEmptyTable(String tableName) {
        String textEmptyTable = String.format(IS_EMPTY_OR_DOES_NOT_EXIST_TABLE_MESSAGE,tableName);
        String result = LevelBoundary.UPPER.leftBoundary;
        int length = textEmptyTable.length() - EVEN_PREFIX;
        result = createBoundary(textEmptyTable, result, length);
        return result;
    }

    private String createBoundary(String textEmptyTable, String result, int length) {
        StringBuilder resultBuilder = new StringBuilder(result);
        for (int j = 0; j < length; j++) {
            resultBuilder.append(EQUAL_SIGN);
        }
        resultBuilder.append(LevelBoundary.UPPER.rightBoundary.concat(NEW_LINE));
        resultBuilder.append(textEmptyTable.concat(NEW_LINE));
        resultBuilder.append(LevelBoundary.BOTTOM.leftBoundary);
        for (int j = 0; j < length; j++) {
            resultBuilder.append(EQUAL_SIGN);
        }
        resultBuilder.append(LevelBoundary.BOTTOM.rightBoundary.concat(NEW_LINE));
        return resultBuilder.toString();
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

    private String convertTableData(List<DataSet> dataSets) {
        int rowsCount = dataSets.size();
        int maxColumnSize = getMaxColumnSize(dataSets);
        String result = "";
        maxColumnSize=  maxColumnSize % EVEN_PREFIX == 0 ? maxColumnSize+EVEN_PREFIX : maxColumnSize + ODD_PREFIX;
        int columnCount = getColumnCount(dataSets);
        result = calculateRowResult(dataSets, rowsCount, maxColumnSize, result, columnCount);
        result = createBoundaryLevel(maxColumnSize, result, columnCount, LevelBoundary.BOTTOM);
        return result;
    }

    private String calculateRowResult(List<DataSet> dataSets, int rowsCount, int maxColumnSize, String result,
                                      int columnCount) {
        for (int row = 0; row < rowsCount; row++) {
            List<Object> values = dataSets.get(row).getValues();
            result = createColumnResult(maxColumnSize, result, columnCount, values);
            if (row < rowsCount - 1) {
                result = createBoundaryLevel(maxColumnSize, result, columnCount, LevelBoundary.MIDDLE);
            }
        }
        return result;
    }

    private String createBoundaryLevel(int maxColumnSize, String result, int columnCount, LevelBoundary boundary) {
        StringBuilder resultBuilder = new StringBuilder(result);
        resultBuilder.append(boundary.leftBoundary);
        for (int j = 1; j < columnCount; j++) {
            for (int i = 0; i < maxColumnSize; i++) {
                resultBuilder.append(EQUAL_SIGN);
            }
            resultBuilder.append(boundary.middleBoundary);
        }
        for (int j = 0; j < maxColumnSize; j++) {
            resultBuilder.append(EQUAL_SIGN);
        }
        resultBuilder.append(boundary.rightBoundary.concat(NEW_LINE));
        return resultBuilder.toString();
    }

    private String createColumnResult(int maxColumnSize, String result, int columnCount, List<Object> values) {
        result = result.concat(LINE);
        for (int column = 0; column < columnCount; column++) {
            int valuesLength = String.valueOf(values.get(column)).length();
            result = calculateForColumns(getLength(maxColumnSize, valuesLength), result,
                    String.valueOf(values.get(column)));
            result = (valuesLength % EVEN_PREFIX == 0) ? result +LINE : result +" "+LINE;
        }
        result = result.concat(NEW_LINE);
        return result;
    }

    private String calculateForColumns(int length, String result,String column) {
        result = calculateColumn(result, length, SPACE,column);
        result = calculateColumn(result, length, SPACE);
        return result;
    }
    private int getLength(int maxColumnSize, int valuesLength) {
        return (maxColumnSize - valuesLength) / EVEN_PREFIX;
    }

    private String calculateColumn(String result, int length, String column) {
        StringBuilder resultBuilder = new StringBuilder(result);
        for (int j = 0; j < length; j++) {
            resultBuilder.append(column);
        }
        return resultBuilder.toString();
    }

    private String calculateColumn(String result, int length, String column,String boundary) {
        StringBuilder resultBuilder = new StringBuilder(result);
        for (int j = 0; j < length; j++) {
            resultBuilder.append(column);
        }
        resultBuilder.append(boundary);
        return resultBuilder.toString();
    }

    private int getColumnCount(List<DataSet> dataSets) {
        int result = 0;
        if (isNotEmptyDataset(dataSets)) {
            return dataSets.get(0).getColumnNames().size();
        }
        return result;
    }

    private boolean isNotEmptyDataset(List<DataSet> dataSets) {
        return !dataSets.isEmpty();
    }

    private String createHeaderForTable(List<DataSet> dataSets) {
        int maxColumnSize = getMaxColumnSize(dataSets);
        String result = "";
        int columnCount = getColumnCount(dataSets);
        maxColumnSize = getMaxColumnSize(maxColumnSize % EVEN_PREFIX == 0, maxColumnSize + EVEN_PREFIX,
                maxColumnSize + ODD_PREFIX);
        result = createUpperLevel(dataSets, maxColumnSize, result, columnCount);

        return  isNotEmptyDataset(dataSets) ? calculateLastStringOfHeader(maxColumnSize, result, columnCount) :
                createBottomLevel(maxColumnSize, result, columnCount);
    }

    private String createUpperLevel(List<DataSet> dataSets, int maxColumnSize, String result, int columnCount) {
        StringBuilder resultBuilder = new StringBuilder(result);
       resultBuilder.append(LevelBoundary.UPPER.leftBoundary);
        for (int j = 1; j < columnCount; j++) {
            for (int i = 0; i < maxColumnSize; i++) {
                resultBuilder.append(EQUAL_SIGN);
            }
            resultBuilder.append(LevelBoundary.UPPER.middleBoundary);
        }
        for (int j = 0; j < maxColumnSize; j++) {
            resultBuilder.append(EQUAL_SIGN);
        }
        resultBuilder.append(LevelBoundary.UPPER.rightBoundary.concat(NEW_LINE));
        result = resultBuilder.toString();
        List<String> columnNames = dataSets.get(0).getColumnNames();
        result = createUpperLevel(maxColumnSize, result, columnCount, columnNames);
        result = result.concat(LINE+NEW_LINE);
        return result;
    }

    private String createBottomLevel(int maxColumnSize, String result, int columnCount) {
        result = result.concat(LevelBoundary.BOTTOM.leftBoundary);
        for (int j = 1; j < columnCount; j++) {
            result = calculateColumn(result, maxColumnSize, EQUAL_SIGN,LevelBoundary.BOTTOM.middleBoundary);
        }
        result = calculateColumn(result, maxColumnSize, EQUAL_SIGN,LevelBoundary.BOTTOM.rightBoundary + NEW_LINE);
        return result;
    }

    private String createUpperLevel(int maxColumnSize, String result, int columnCount, List<String> columnNames) {
        for (int column = 0; column < columnCount; column++) {
            result = result.concat(LINE);
            int columnNamesLength = columnNames.get(column).length();
            int prefixLength = getLength(maxColumnSize, columnNamesLength);
            result =calculateForColumns(prefixLength, result, columnNames.get(column));
            if((columnNamesLength % EVEN_PREFIX != 0)) {
                result = result.concat(SPACE);
            }
        }
        return result;
    }

    private int getMaxColumnSize(boolean isValid, int max1, int max2) {
        return isValid ? max1 : max2;
    }

    private String calculateLastStringOfHeader(int maxColumnSize, String result, int columnCount) {
        result += LevelBoundary.MIDDLE.leftBoundary;
        result = createColumnHeader(maxColumnSize, result, columnCount, LevelBoundary.MIDDLE);
        result = calculateColumn(result, maxColumnSize, EQUAL_SIGN,LevelBoundary.MIDDLE.rightBoundary + NEW_LINE);
        return result;
    }

    private String createColumnHeader(int maxColumnSize, String result, int columnCount, LevelBoundary middle) {
        StringBuilder resultBuilder = new StringBuilder(result);
        for (int j = 1; j < columnCount; j++) {
            for (int i = 0; i < maxColumnSize; i++) {
                resultBuilder.append(EQUAL_SIGN);
            }
            resultBuilder.append(middle.middleBoundary);
        }
        return resultBuilder.toString();
    }
}
