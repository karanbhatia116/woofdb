package org.woofdb.core.models;

import org.woofdb.core.exceptions.MaxTableSizeReachedException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Table {
    private static final int MAX_ROWS = 3;
    private int numOfRows;
    private String tableName;
    private List<Column> columns;
    private List<Row> rows;
    private String tableFilePath;

    public Table() {
        this.columns = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public int getNumOfRows() {
        return numOfRows;
    }

    public void setNumOfRows(final int numOfRows) {
        this.numOfRows = numOfRows;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(final List<Column> columns) {
        this.columns = columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(final List<Row> rows) {
        this.rows = rows;
    }

    public String getTableFilePath() {
        return tableFilePath;
    }

    public void setTableFilePath(final String tableFilePath) {
        this.tableFilePath = tableFilePath;
    }

    public void addColumn(final Column column) {
        columns.add(column);
        if (!rows.isEmpty()) {
            for (Row row : rows) {
                Object[] newValues = new Object[columns.size()];
                System.arraycopy(row.getValues(), 0, newValues, 0, row.getValues().length);
                row.setValues(newValues);
            }
        }
    }

    public void addRow(final Row row) throws MaxTableSizeReachedException, IOException {
        if (rows.size() >= MAX_ROWS) {
            throw new MaxTableSizeReachedException("Table is full!");
        }
        if (row.getValues().length != columns.size()) {
            throw new IllegalArgumentException("Values count in the row doesn't match column count");
        }
        rows.add(row);
        File file = new File(this.tableFilePath);
        if (!file.exists()) {
            this.saveToFile(this.tableFilePath);
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(getRowCountPosition(raf));
            int currentRowCount = raf.readInt();

            raf.seek(getRowCountPosition(raf));
            raf.writeInt(currentRowCount + 1);

            raf.seek(file.length());
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(raf.getFD()))) {
                writeRow(row, out);
            }
        }
    }

    public void addRow(final Object[] values) throws MaxTableSizeReachedException, IOException {
        Row row = new Row(columns.size());
        int index = 0;
        for (Object value: values) {
            row.setValue(index++, value);
        }
        addRow(row);
    }

    public void printTableData() {
        for (Column column: columns) {
            System.out.print(column.getName() + " | ");
        }
        System.out.println();
        for (Row row: rows) {
            for (Object value: row.getValues()) {
                System.out.print(value + " | ");
            }
            System.out.println();
        }
    }

    public static Table loadFromFile(final String path) throws IOException, MaxTableSizeReachedException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(path))) {
            String tableName = in.readUTF();
            Table table = new Table();
            table.setTableName(tableName);
            table.setTableFilePath(path);

            int columnCount = in.readInt();
            for (int i = 0; i < columnCount; i ++) {
                String name = in.readUTF();
                DataType dataType = DataType.values()[in.readInt()];
                boolean nullable = in.readBoolean();
                table.addColumn(new Column(name, dataType, nullable));
            }

            int rowCount = in.readInt();

            for (int i = 0; i < rowCount; i ++) {
                Object[] values = new Object[columnCount];
                for (int j = 0; j < columnCount; j ++) {
                    boolean isNull = in.readBoolean();
                    if (isNull) {
                        values[j] = null;
                    } else {
                        DataType dataType = table.getColumns().get(j).getDataType();
                        switch (dataType) {
                            case INT:
                                values[j] = in.readInt();
                                break;
                            case FLOAT:
                                values[j] = in.readFloat();
                                break;
                            case DOUBLE:
                                values[j] = in.readDouble();
                                break;
                            case VARCHAR:
                                values[j] = in.readUTF();
                                break;
                            default:
                                break;
                        }
                    }
                }
                Row row = new Row(columnCount);
                row.setValues(values);
                table.rows.add(row);
            }

            return table;
        }
    }

    public void saveToFile(final String path) throws IOException {
        this.setTableFilePath(path);
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(tableFilePath))) {
            out.writeUTF(tableName);
            out.writeInt(columns.size());
            for (Column column: columns) {
                out.writeUTF(column.getName());
                out.writeInt(column.getDataType().ordinal());
                out.writeBoolean(column.isNullable());
            }

            out.writeInt(rows.size());
            for (Row row: rows) {
                writeRow(row, out);
            }
        }
    }

    private void writeRow(final Row row, final DataOutputStream out) throws IOException {
        Object[] values = row.getValues();
        int i = 0;
        for (Object value: values) {
            out.writeBoolean(value == null);
            if (value != null) {
                switch (columns.get(i ++).getDataType()) {
                    case INT -> out.writeInt((Integer) value);
                    case DOUBLE -> out.writeDouble((double) value);
                    case FLOAT -> out.writeFloat((float) value);
                    case VARCHAR -> out.writeUTF((String) value);
                }
            }
        }
    }

    private long getRowCountPosition(RandomAccessFile raf) throws IOException {
        raf.seek(0);

        raf.readUTF(); // skip tablename

        raf.readInt(); // skip columns count

        // skip column definitions
        for (Column col: columns) {
            raf.readUTF(); // skip colName
            raf.readInt(); // skip data type ordinal
            raf.readBoolean(); // skip nullable flag
        }

        return raf.getFilePointer();
    }
}
