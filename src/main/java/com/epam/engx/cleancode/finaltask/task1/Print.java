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
        StringBuilder builder = new StringBuilder(LevelBoundary.UPPER.leftBoundary);
        int length = textEmptyTable.length() - EVEN_PREFIX;
        builder.append(createBoundary(textEmptyTable, length));
        return builder.toString();
    }

    private String createBoundary(String textEmptyTable, int length) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(duplicateSymbol(EQUAL_SIGN,length));
        resultBuilder.append(LevelBoundary.UPPER.rightBoundary.concat(NEW_LINE));
        resultBuilder.append(textEmptyTable.concat(NEW_LINE));
        resultBuilder.append(LevelBoundary.BOTTOM.leftBoundary);
        resultBuilder.append(duplicateSymbol(EQUAL_SIGN,length));
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
        StringBuilder builder = new StringBuilder();
        maxColumnSize=  maxColumnSize % EVEN_PREFIX == 0 ? maxColumnSize+EVEN_PREFIX : maxColumnSize + ODD_PREFIX;
        int columnCount = getColumnCount(dataSets);
        builder.append(calculateRowResult(dataSets, rowsCount, maxColumnSize, columnCount));
        builder.append(createBoundaryLevel(maxColumnSize, columnCount, LevelBoundary.BOTTOM));
        return builder.toString();
    }

    private String calculateRowResult(List<DataSet> dataSets, int rowsCount, int maxColumnSize,
                                      int columnCount) {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < rowsCount; row++) {
            List<Object> values = dataSets.get(row).getValues();
            builder.append(createColumnResult(maxColumnSize, columnCount, values));
            if (row < rowsCount - 1) {
                builder.append(createBoundaryLevel(maxColumnSize, columnCount, LevelBoundary.MIDDLE));
            }
        }
        return builder.toString();
    }

    private String createBoundaryLevel(int maxColumnSize, int columnCount, LevelBoundary boundary) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(boundary.leftBoundary);
        for (int j = 1; j < columnCount; j++) {
            resultBuilder.append(duplicateSymbol(EQUAL_SIGN, maxColumnSize)).append(boundary.middleBoundary);
        }
        resultBuilder.append(duplicateSymbol(EQUAL_SIGN, maxColumnSize));
        resultBuilder.append(boundary.rightBoundary.concat(NEW_LINE));
        return resultBuilder.toString();
    }

    private String createColumnResult(int maxColumnSize, int columnCount, List<Object> values) {
        StringBuilder builder = new StringBuilder();
        builder.append(LINE);
        for (int column = 0; column < columnCount; column++) {
            int valuesLength = String.valueOf(values.get(column)).length();
            int length = getLength(maxColumnSize, valuesLength);
            builder.append(duplicateSymbol(SPACE, length)).append((values.get(column)))
                    .append(duplicateSymbol(SPACE, length));
            String lastString = (valuesLength % EVEN_PREFIX == 0) ? LINE : (SPACE)+(LINE);
            builder.append(lastString);
        }

        return builder.append(NEW_LINE).toString();
    }

    private int getLength(int maxColumnSize, int valuesLength) {
        return (maxColumnSize - valuesLength) / EVEN_PREFIX;
    }



    private String duplicateSymbol(String symbol, int times) {
        StringBuilder builder= new StringBuilder();
        for (int j = 0; j < times; j++) {
            builder.append(symbol);
        }
        return builder.toString();
    }

    private int getColumnCount(List<DataSet> dataSets) {
        int result = 0;
       return (isNotEmptyDataset(dataSets)) ? dataSets.get(0).getColumnNames().size() : result;
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
        result += createUpperLevel(dataSets, maxColumnSize, columnCount);

        return  isNotEmptyDataset(dataSets) ? result+calculateLastStringOfHeader(maxColumnSize, columnCount) :
                result+createBottomLevel(maxColumnSize, columnCount);
    }

    private String createUpperLevel(List<DataSet> dataSets, int maxColumnSize, int columnCount) {
        StringBuilder resultBuilder = new StringBuilder();
       resultBuilder.append(LevelBoundary.UPPER.leftBoundary);
        for (int j = 1; j < columnCount; j++) {
            resultBuilder.append(duplicateSymbol(EQUAL_SIGN,maxColumnSize)).append(LevelBoundary.UPPER.middleBoundary);
        }

        resultBuilder.append(duplicateSymbol(EQUAL_SIGN,maxColumnSize));
        resultBuilder.append(LevelBoundary.UPPER.rightBoundary.concat(NEW_LINE));

        List<String> columnNames = dataSets.get(0).getColumnNames();
        resultBuilder.append(createUpperLevel(maxColumnSize, columnCount, columnNames));
        resultBuilder.append(LINE+NEW_LINE);
        return resultBuilder.toString();
    }

    private String createBottomLevel(int maxColumnSize, int columnCount) {
        StringBuilder builder = new StringBuilder();
        builder.append(LevelBoundary.BOTTOM.leftBoundary);
        for (int j = 1; j < columnCount; j++) {
            builder.append(builder.append(duplicateSymbol(EQUAL_SIGN,maxColumnSize)).append(LevelBoundary.BOTTOM.middleBoundary));
        }
        builder.append(builder.append(duplicateSymbol(EQUAL_SIGN,maxColumnSize)).append(LevelBoundary.BOTTOM.rightBoundary + NEW_LINE));
        return builder.toString();
    }

    private String createUpperLevel(int maxColumnSize, int columnCount, List<String> columnNames) {
        StringBuilder builder = new StringBuilder();
        for (int column = 0; column < columnCount; column++) {
            builder.append(LINE);
            int columnNamesLength = columnNames.get(column).length();
            int length = getLength(maxColumnSize, columnNamesLength);
            String columnName = columnNames.get(column);
            builder.append(duplicateSymbol(SPACE, length)).append(columnName).append(duplicateSymbol(SPACE, length));
            if((columnNamesLength % EVEN_PREFIX != 0)) {
                builder.append(SPACE);
            }
        }
        return builder.toString();
    }

    private int getMaxColumnSize(boolean isValid, int max1, int max2) {
        return isValid ? max1 : max2;
    }

    private String calculateLastStringOfHeader(int maxColumnSize, int columnCount) {
        StringBuilder builder = new StringBuilder();
        builder.append(LevelBoundary.MIDDLE.leftBoundary);
        builder.append(createColumnHeader(maxColumnSize, columnCount, LevelBoundary.MIDDLE));
        builder.append(duplicateSymbol(EQUAL_SIGN,maxColumnSize)).append(LevelBoundary.MIDDLE.rightBoundary + NEW_LINE);
        return builder.toString();
    }

    private String createColumnHeader(int maxColumnSize, int columnCount, LevelBoundary middle) {
        StringBuilder resultBuilder = new StringBuilder();
        for (int j = 1; j < columnCount; j++) {
            resultBuilder.append(duplicateSymbol(EQUAL_SIGN, maxColumnSize)).append(middle.middleBoundary);
        }
        return resultBuilder.toString();
    }
}
