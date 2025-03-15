package org.woofdb.core.models;

import org.woofdb.core.exceptions.MaxTableSizeReachedException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private Map<String, Table> tables;
    private String baseDirectory;
    public Database(String baseDirectory) {
        this.tables = new HashMap<>();
        this.baseDirectory = baseDirectory;
        File file = new File(baseDirectory);
        if (file.exists()) {
            try {
                loadTables();
            }
            catch (Exception e) {
                throw new IllegalStateException("Failed to load tables into database: " + Arrays.toString(e.getStackTrace()));
            }
        }
        else {
            file.mkdir();
        }
    }

    public Table createTable(String tableName, List<Column> columns) throws IOException {
        if (tables.containsKey(tableName)) {
            return tables.get(tableName);
        }
        Table table = new Table();
        table.setTableName(tableName);
        table.setColumns(columns);
        this.tables.put(tableName, table);
        saveTable(table);
        return table;
    }

    private void saveTable(final Table table) throws IOException {
        String filePath = baseDirectory + File.separator + table.getTableName() + ".tbl";
        table.saveToFile(filePath);
    }

    public Table getTable(final String tableName) {
        return this.tables.get(tableName);
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(final String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public void loadTables() throws IOException, MaxTableSizeReachedException {
        File dir = new File(baseDirectory);
        File[] tableFiles = dir.listFiles((d, filename) -> filename.endsWith(".tbl"));
        if (tableFiles != null) {
            for (File file: tableFiles) {
                String tableName = file.getName().replace(".tbl", "");
                Table table = Table.loadFromFile(file.getPath());
                tables.put(tableName, table);
            }
        }
    }
}
