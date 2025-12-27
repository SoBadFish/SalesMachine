package org.sobadfish.sales.db;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * @author Sobadfish
 * @date 2024/3/29
 */
public class SqliteHelper {

    private Connection connection;

    private final String dbFilePath;

    /**
     * 构造函数
     *
     * @param dbFilePath sqlite db 文件路径
     */
    public SqliteHelper(String dbFilePath) throws ClassNotFoundException, SQLException {
        this.dbFilePath = dbFilePath;
        this.connection = createConnection(dbFilePath);
    }

    /**
     * 获取数据库连接
     *
     * @param dbFilePath db文件路径
     * @return 数据库连接
     */
    private Connection createConnection(String dbFilePath) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        // 1、加载驱动
        Class.forName("org.sqlite.JDBC");
        // 2、建立连接
        // 注意：此处有巨坑，如果后面的 dbFilePath 路径太深或者名称太长，则建立连接会失败
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        return conn;
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        if (null == connection) {
            connection = createConnection(dbFilePath);
        }
        return connection;
    }

    public boolean exists(String table) {
        try (Statement stmt = getConnection().createStatement();
             ResultSet resultSet = stmt.executeQuery("select * from " + table)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void addTable(String tableName, DBTable tables) {
        if (!exists(tableName)) {
            String sql = "create table " + tableName + "(" + tables.asSql() + ")";
            try (Statement stmt = getConnection().createStatement()) {
                stmt.execute(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getDbFilePath() {
        return dbFilePath;
    }

    /**
     * 增加数据
     *
     */
    public <T> void add(String tableName, T values) {
        try {
            SqlData sqlData = SqlData.classToSqlData(values);
            add(tableName, sqlData);
        } catch (Exception ignore) {
        }
    }

    /**
     * 增加数据
     *
     */
    public SqliteHelper add(String tableName, SqlData values) {
        try {
            String sql = "insert into " + tableName + "(" + values.getColumnToString() + ") values (" + values.getObjectToString() + ")";
            try (Statement stmt = getConnection().createStatement()) {
                stmt.execute(sql);
            }
        } catch (Exception ignore) {
        }
        return this;
    }

    /**
     * 删除数据
     *
     */
    public SqliteHelper remove(String tableName, int id) {
        try {
            String sql = "delete from " + tableName + " where id = " + id;
            try (Statement stmt = getConnection().createStatement()) {
                stmt.execute(sql);
            }
        } catch (Exception ignore) {
        }
        return this;
    }

    public SqliteHelper remove(String tableName, String key, String value) {
        try {
            String sql = "delete from " + tableName + " where " + key + " = '" + value + "'";
            try (Statement stmt = getConnection().createStatement()) {
                stmt.execute(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public SqliteHelper removeAll(String tableName) {
        try {
            String sql = "delete from " + tableName;
            try (Statement stmt = getConnection().createStatement()) {
                stmt.execute(sql);
            }
        } catch (Exception ignore) {
        }
        return this;
    }

    public <T> SqliteHelper set(String tableName, T values) {
        SqlData contentValues = SqlData.classToSqlDataAsId(values);
        if (contentValues.getInt("id") == -1) {
            throw new NullPointerException("无 id 信息");
        }
        return set(tableName, contentValues.getInt("id"), contentValues);
    }

    public <T> SqliteHelper set(String tableName, String key, String value, T values) {
        SqlData sqlData = SqlData.classToSqlData(values);
        return set(tableName, key, value, sqlData);
    }


    /**
     * 更新数据
     *
     */
    public SqliteHelper set(String tableName, int id, SqlData values) {
        try {
            String sql = "update " + tableName + " set " + values.toUpdateValue() + " where id = " + id;
            try (Statement stmt = getConnection().createStatement()) {
                stmt.execute(sql);
            }
        } catch (Exception ignore) {
        }
        return this;
    }


    public void addColumns(String table, String columns, Field type) {
        try {
            String sql = "alter table " + table + " add column '" + columns + "' " + classTypeAsSql(type) + "";
            try (Statement stmt = getConnection().createStatement()) {
                stmt.execute(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取字段名称
     *
     */
    public List<String> getColumns(String table) {
        List<String> strings = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet resultSet = stmt.executeQuery("pragma  table_info(" + table + ")")) {
            while (resultSet.next()) {
                strings.add(resultSet.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strings;
    }

    /**
     * 更新数据
     *
     */
    public SqliteHelper set(String tableName, String key, String value, SqlData values) {
        try {
            String sql = "update " + tableName + " set " + values.toUpdateValue() + " where " + key + " = " + value;
            try (Statement stmt = getConnection().createStatement()) {
                stmt.execute(sql);
            }
        } catch (Exception ignore) {
        }
        return this;
    }

    public <T> SqliteHelper set(String tableName, SqlData key, T values) {
        SqlData sqlData = SqlData.classToSqlData(values);
        try {
            String sql = "update " + tableName + " set " + sqlData.toUpdateValue() + " where " + getUpDataWhere(key);
            try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
                int i = 1;
                for (Object type : key.getObjects()) {
                    preparedStatement.setString(i, type.toString());
                    i++;
                }
                preparedStatement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }


    public boolean hasData(String tableName, String key, String value) {
        try (Statement stmt = getConnection().createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName + " WHERE " + key + " = '" + value + "'")) {
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getUpDataWhere(SqlData data) {
        StringBuilder builder = new StringBuilder();
        for (String column : data.getData().keySet()) {
            builder.append(column).append(" = ? and");
        }
        String str = builder.toString();
        return str.substring(0, str.length() - 3);
    }

    /**
     * 查找条数
     *
     */
    public int countAllData(String tableName) {
        try (Statement stmt = getConnection().createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT count(*) FROM " + tableName)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 查找条数
     *
     */
    public int countData(String tableName, String key, String value) {
        try (Statement stmt = getConnection().createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT count(*) FROM " + tableName + " WHERE " + key + " = '" + value + "'")) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 根据数量排行
     *
     */
    public <T> List<DataCount<T>> sortDataCount(String tableName, String groupBy, String where, int count, Class<T> tClass) {
        try {
            LinkedList<DataCount<T>> datas = new LinkedList<>();
            String sql = "SELECT " + tableName + ".*, COUNT(*) AS group_count FROM " + tableName + " WHERE " + where + " " +
                    "GROUP BY " + groupBy + " ORDER BY group_count DESC LIMIT " + count;
            try (Statement stmt = getConnection().createStatement();
                 ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    T t = tClass.getDeclaredConstructor().newInstance();
                    int ct = resultSet.getInt("group_count");
                    explainClass(resultSet, tClass, t);
                    DataCount<T> dc = new DataCount<>();
                    dc.data = t;
                    dc.count = ct;
                    datas.add(dc);
                }
            }
            return datas;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    public <T> T get(String tableName, int id, Class<T> clazz) {
        T instance = null;
        try {
            String query = "SELECT * FROM " + tableName + " WHERE id = ?";
            try (PreparedStatement preparedStatement = getConnection().prepareStatement(query)) {
                preparedStatement.setInt(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    instance = explainClass(resultSet, clazz, clazz.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public <T> T get(String tableName, String key, String value, Class<T> clazz) {
        T instance = null;
        try {
            String query = "SELECT * FROM " + tableName + " WHERE " + key + " = ?";
            try (PreparedStatement preparedStatement = getConnection().prepareStatement(query)) {
                preparedStatement.setString(1, value);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    T t = clazz.getDeclaredConstructor().newInstance();
                    instance = explainClass(resultSet, clazz, t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public <T> LinkedList<T> getDataByString(String tableName, String selection, String[] key, Class<T> clazz) {
        LinkedList<T> datas = new LinkedList<>();
        try {
            String query = "SELECT * FROM " + tableName + " WHERE " + selection;
            try (PreparedStatement preparedStatement = getConnection().prepareStatement(query)) {
                // 设置查询条件
                for (int i = 0; i < key.length; i++) {
                    preparedStatement.setString(i + 1, key[i]);
                }
                // 执行查询
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        T t = clazz.getDeclaredConstructor().newInstance();
                        datas.add(explainClass(resultSet, clazz, t));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datas;
    }


    public <T> LinkedList<T> getAll(String tableName, Class<T> clazz) {
        LinkedList<T> datas = new LinkedList<>();
        try {
            String query = "SELECT * FROM " + tableName;
            try (PreparedStatement preparedStatement = getConnection().prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    T t = clazz.getDeclaredConstructor().newInstance();
                    datas.add(explainClass(resultSet, clazz, t));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datas;
    }

    private String classTypeAsSql(Field type) {
        if (type.getType() == int.class) {
            return "integer default 0";
        }
        if (type.getType() == float.class || type.getType() == double.class) {
            return type.getType().getName();
        } else {
            return "text";
        }

    }

    private <T> T explainClass(ResultSet cursor, Class<?> tc, T t) {
        try {
            ResultSetMetaData rsmd = cursor.getMetaData();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                String name = rsmd.getColumnName(i + 1);
                Field field = null;
                try {
                    field = tc.getField(name);
                } catch (Exception ignore) {
                }
                if (field == null) {
                    continue;
                }
                if (field.getType() == int.class) {
                    field.set(t, cursor.getInt(name));
                } else if (field.getType() == float.class || field.getType() == double.class) {
                    field.set(t, cursor.getFloat(name));
                } else if (field.getType() == boolean.class) {
                    field.set(t, Boolean.valueOf(cursor.getString(name)));

                } else if (field.getType() == long.class) {
                    field.set(t, cursor.getLong(name));

                } else {
                    String v = cursor.getString(name);
                    if ("null".equalsIgnoreCase(v)) {
                        v = null;
                    }
                    field.set(t, v);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }


    public static class DBTable {
        LinkedHashMap<String, String> tables = new LinkedHashMap<>();

        public DBTable(String key, String value) {
            tables.put(key, value);
        }

        public DBTable(Map<String, String> m) {
            tables.putAll(m);
        }

        public DBTable put(String key, String value) {
            tables.put(key, value);
            return this;
        }

        public String asSql() {
            StringBuilder s = new StringBuilder();
            for (Map.Entry<String, String> e : tables.entrySet()) {
                s.append(e.getKey()).append(" ").append(e.getValue()).append(",");
            }
            return s.substring(0, s.length() - 1);

        }


        public static DBTable asDbTable(Class<?> t) {
            Field[] fields = t.getFields();
            LinkedHashMap<String, String> stringStringLinkedHashMap = new LinkedHashMap<>();
            boolean isId = false;
            // 先找自增id
            for (Field field : fields) {
                if ("id".equalsIgnoreCase(field.getName()) && (field.getType() == long.class
                        || field.getType() == int.class)) {
                    //找到了
                    isId = true;
                    break;
                }
            }
            if (!isId) {
                throw new NullPointerException("数据库类需要一个id");
            }
            stringStringLinkedHashMap.put("id", "integer primary key autoincrement");
            for (Field field : fields) {
                if ("id".equalsIgnoreCase(field.getName()) && field.getType() == int.class) {
                    //找到了
                    continue;
                }
                if (field.getType() == float.class || field.getType() == double.class) {
                    stringStringLinkedHashMap.put(field.getName().toLowerCase(), field.getType().getName());
                } else if (field.getType() == int.class) {
                    stringStringLinkedHashMap.put(field.getName().toLowerCase(), "integer");
                } else {
                    stringStringLinkedHashMap.put(field.getName().toLowerCase(), "text");
                }
            }
            return new DBTable(stringStringLinkedHashMap);

        }

    }

    /**
     * 数据库资源关闭和释放
     */
    public void destroyed() {
        try {
            if (null != connection) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.out.println("Sqlite数据库关闭时异常 " + e);
        }
    }

    public static class DataCount<T> {

        public T data;

        public int count;
    }


}
