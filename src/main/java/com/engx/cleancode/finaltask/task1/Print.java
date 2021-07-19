package com.engx.cleancode.finaltask.task1;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.engx.cleancode.finaltask.task1.thirdpartyjar.Command;
import com.engx.cleancode.finaltask.task1.thirdpartyjar.DataSet;
import com.engx.cleancode.finaltask.task1.thirdpartyjar.DatabaseManager;
import com.engx.cleancode.finaltask.task1.thirdpartyjar.View;

public class Print implements Command {

    private static final String EQUAL_SIGN = "═";
    private static final String SPACE = " ";
    private static final int EVEN_PREFIX = 2;
    private static final int COMMAND_PARTS_NUMBER = 2;
    private static final String EMPTY_TABLE_TEMPLATE = "║ Table '%s' is empty or does not exist ║";
    private static final String LINE = "║";
    private static final String NEW_LINE = "\n";
    private static final String PRINT_MESSAGE = "print ";
    private static final String INCORRECT_NUMBER_OF_PARAMETERS_ERROR_MESSAGE = "incorrect number of parameters." +
            " Expected %d, but is %s";
    private static final int ODD_PREFIX = 3;
    private static final int TABLE = 1;
    private static final String BOUNDARY_TEMPLATE = "╔%s╗\n%s\n╚%s╝\n";
    private static final String UPPER_SPACE_TEMPLATE = "║%s%s%s";
    private static final int EMPTY = 0;

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
        String[] commandParts = input.split(SPACE);
        validateCommandPartsNumber(commandParts.length);
        String tableName = commandParts[TABLE];
        List<DataSet> data = databaseManager.getTableData(tableName);
        view.write(convertTableDataToString(data, tableName));
    }

    private void validateCommandPartsNumber(int commandlength) {
        if (commandlength != COMMAND_PARTS_NUMBER) {
            throw new IllegalArgumentException(String.format(INCORRECT_NUMBER_OF_PARAMETERS_ERROR_MESSAGE, 1,
                    commandlength - 1));
        }
    }

    private String convertTableDataToString(List<DataSet> data, String tableName) {
        int maxColumnSize;
        maxColumnSize = getMaxColumnSize(data);
        return maxColumnSize == 0 ? getEmptyTable(tableName) : (createHeaderForTable(data) + convertTableData(data));
    }

    private String getEmptyTable(String tableName) {
        String textEmptyTable = String.format(EMPTY_TABLE_TEMPLATE, tableName);
        int length = textEmptyTable.length() - EVEN_PREFIX;
        String line = duplicateSymbol(EQUAL_SIGN, length);
        return String.format(BOUNDARY_TEMPLATE, line, textEmptyTable, line);

    }

    private int getMaxColumnSize(List<DataSet> dataSets) {
        int maxLength = 0;
        if (isNotEmptyDataset(dataSets)) {
            List<String> columnNames = dataSets.get(0).getColumnNames();
            maxLength = Collections.max(columnNames).length();
            for (DataSet dataSet : dataSets) {
                List<Object> dataSetValues = dataSet.getValues();
                Object dataSetValue = getMaxDataSetValue(dataSetValues);
                int maxValue = String.valueOf(dataSetValue).length();
                maxLength = Math.max(maxValue, maxLength);
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
        maxColumnSize = getPrefix(maxColumnSize);
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

    private String createColumnResult(int maxColumnSize, int columnCount, List<Object> values) {
        StringBuilder builder = new StringBuilder(LINE);
        for (int column = 0; column < columnCount; column++) {
            int valuesLength = String.valueOf(values.get(column)).length();
            int length = getLength(maxColumnSize, valuesLength);
            String line = duplicateSymbol(SPACE, length);
            builder.append(line).append((values.get(column)))
                    .append(line);
            String lastString = (valuesLength % EVEN_PREFIX == 0) ? LINE : SPACE + LINE;
            builder.append(lastString);
        }
        return builder.append(NEW_LINE).toString();
    }

    private int getLength(int maxColumnSize, int valuesLength) {
        return (maxColumnSize - valuesLength) / EVEN_PREFIX;
    }

    private String duplicateSymbol(String symbol, int times) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < times; j++) {
            builder.append(symbol);
        }
        return builder.toString();
    }

    private boolean isNotEmptyDataset(List<DataSet> dataSets) {
        return !dataSets.isEmpty();
    }

    private String createHeaderForTable(List<DataSet> dataSets) {
        int maxColumnSize = getMaxColumnSize(dataSets);
        StringBuilder builder = new StringBuilder();
        int columnCount = getColumnCount(dataSets);
        maxColumnSize = getPrefix(maxColumnSize);
        builder.append(createBoundaryLevel(maxColumnSize, columnCount, LevelBoundary.UPPER));
        List<String> columnNames = dataSets.get(0).getColumnNames();
        builder.append(createUpperLevel(maxColumnSize, columnCount, columnNames));
        builder.append(LINE).append(NEW_LINE);
        LevelBoundary boundary = isNotEmptyDataset(dataSets) ? LevelBoundary.MIDDLE : LevelBoundary.BOTTOM;
        builder.append(createBoundaryLevel(maxColumnSize, columnCount, boundary));
        return builder.toString();
    }

    private int getPrefix(int maxColumnSize) {
        return maxColumnSize % EVEN_PREFIX == 0? maxColumnSize + EVEN_PREFIX:
                maxColumnSize + ODD_PREFIX;
    }

    private int getColumnCount(List<DataSet> dataSets) {
        return (isNotEmptyDataset(dataSets)) ? dataSets.get(0).getColumnNames().size() : EMPTY;
    }

    private String createBoundaryLevel(int maxColumnSize, int columnCount, LevelBoundary boundary) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(boundary.leftBoundary);
        String equalLine = duplicateSymbol(EQUAL_SIGN, maxColumnSize);
        resultBuilder.append(duplicateSymbol(equalLine+boundary.middleBoundary, columnCount-1));
        resultBuilder.append(equalLine);
        resultBuilder.append(boundary.rightBoundary).append(NEW_LINE);
        return resultBuilder.toString();
    }

    private String createUpperLevel(int maxColumnSize, int columnCount, List<String> columnNames) {
        StringBuilder builder = new StringBuilder();
        for (int column = 0; column < columnCount; column++) {
            int columnNamesLength = columnNames.get(column).length();
            int length = getLength(maxColumnSize, columnNamesLength);
            String columnName = columnNames.get(column);
            String spaceLine = duplicateSymbol(SPACE, length);
            builder.append(String.format(UPPER_SPACE_TEMPLATE,spaceLine,columnName,spaceLine));
            if ((columnNamesLength % EVEN_PREFIX != 0)) {
                builder.append(SPACE);
            }
        }
        return builder.toString();
    }

}
