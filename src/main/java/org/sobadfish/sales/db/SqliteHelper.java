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

    private Statement statement;

    private final String dbFilePath;

    /**
     * 构造函数
     * @param dbFilePath sqlite db 文件路径
     */
    public SqliteHelper(String dbFilePath) throws ClassNotFoundException, SQLException {
        this.dbFilePath = dbFilePath;
        connection = getConnection(dbFilePath);
    }

    /**
     * 获取数据库连接
     * @param dbFilePath db文件路径
     * @return 数据库连接
     */
    private Connection getConnection(String dbFilePath) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        // 1、加载驱动
        Class.forName("org.sqlite.JDBC");
        // 2、建立连接
        // 注意：此处有巨坑，如果后面的 dbFilePath 路径太深或者名称太长，则建立连接会失败
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        return conn;
    }



    public boolean exists(String table) {
        try {
            getStatement().executeQuery(
                    "select * from "+table
            );
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public void addTable(String tableName,DBTable tables){
        if(!exists(tableName)) {
            String sql = "create table " + tableName + "(" + tables.asSql() + ")";
            try {
                getStatement().executeQuery(sql);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public String getDbFilePath() {
        return dbFilePath;
    }

    /**
     * 增加数据
     * */
    public <T> void add(String tableName,T values){
        try{
            if(statement != null){
//                ContentValues contentValues = new ContentValues();
                SqlData sqlData = SqlData.classToSqlData(values);
                add(tableName, sqlData);
            }
        }catch (Exception ignore){}


    }



    /**
     * 增加数据
     * */
    public SqliteHelper add(String tableName,SqlData values){
        try {
            if (statement != null) {
                String sql = "insert into " + tableName + "(" + values.getColumnToString() + ") values (" + values.getObjectToString() + ")";
                statement.execute(sql);
            }
        }catch (Exception ignore){}
        return this;
    }


    /**
     * 删除数据
     * */
    public SqliteHelper remove(String tableName,int id){
        try {
            if (statement != null) {
                String sql = "delete from " + tableName + " where id = " + id;
                statement.execute(sql);
            }
        }catch (Exception ignore){}
        return this;
    }

    public SqliteHelper remove(String tableName,String key,String value){
        try {
            if (statement != null) {
                String sql = "delete from " + tableName + " where "+key+" = '" + value+"'";
                statement.execute(sql);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return this;
    }

    public SqliteHelper removeAll(String tableName){
        try {
            if (statement != null) {
                String sql = "delete from " + tableName;
                statement.execute(sql);
            }
        }catch (Exception ignore){}
        return this;
    }



    public <T> SqliteHelper set(String tableName,T values){
        SqlData contentValues = SqlData.classToSqlDataAsId(values);
        if(contentValues.getInt("id") == -1){
            throw new NullPointerException("无 id 信息");
        }
        return set(tableName,contentValues.getInt("id"),contentValues);
    }

    public <T> SqliteHelper set(String tableName,String key,String value,T values){
        SqlData sqlData = SqlData.classToSqlData(values);
        return  set(tableName,key,value,sqlData);
    }


    /**
     * 更新数据
     * */
    public SqliteHelper set(String tableName,int id,SqlData values){
        try {
            if(statement != null) {
                statement.execute("update "+tableName+" set "+values.toUpdateValue()+" where id = "+id);
            }

        }catch (Exception ignore){}

        return this;
    }


    public void addColumns(String table,String columns,Field type){
        try {
            if (statement != null) {
                statement.execute("alter table " + table + " add column '"+columns+"' "+classTypeAsSql(type)+"");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 获取字段名称
     * */
    public List<String> getColumns(String table){
        List<String> strings = new ArrayList<>();
        if(statement != null) {
            try{
                ResultSet resultSet = statement.executeQuery("pragma  table_info("+table+")");
                while (resultSet.next()){
                    strings.add(resultSet.getString("name"));
                }
                resultSet.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return strings;
    }

    /**
     * 更新数据
     * */
    public SqliteHelper set(String tableName,String key,String value,SqlData values){

        try {
            if(statement != null) {
                statement.execute("update "+tableName+" set "+values.toUpdateValue()+" where "+key+" = "+value);
            }

        }catch (Exception ignore){}

        return this;

    }

    public <T> SqliteHelper set(String tableName,SqlData key,T values){
        SqlData sqlData = SqlData.classToSqlData(values);
        try {
            if(statement != null) {

                String sql = "update "+tableName+" set "+sqlData.toUpdateValue()+" where "+getUpDataWhere(key);
                PreparedStatement statement = connection.prepareStatement(sql);

                int i = 1;
                for (Object type : key.getObjects()) {
                    statement.setString(i, type.toString());
                    i++;
                }
                statement.execute();

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return this;

//        return  set(tableName,key,value,sqlData);
    }


    public boolean hasData(String tableName, String key, String value) {
        try {
            if (statement != null) {
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName + " WHERE " + key + " = '" + value + "'");
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    resultSet.close();
                    return count > 0;
                }
            }
        } catch (SQLException e) {
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
     * */
    public int countAllData(String tableName){
        if (statement != null) {
            try{
                ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM " + tableName);
                return resultSet.getInt(1);
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return 0;

    }
    /**
     * 查找条数
     * */
    public int countData(String tableName,String key,String value){
        if (statement != null) {
            try{
                ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM " + tableName+" WHERE " + key+" = '"+value+"'");
                return resultSet.getInt(1);
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return 0;

    }

    /**
     * 根据数量排行
     * */
    public <T> List<DataCount<T>> sortDataCount(String tableName,String groupBy,String where,int count,Class<T> tClass){
        if (statement != null) {
            try{
                LinkedList<DataCount<T>> datas = new LinkedList<>();
                String sql = "SELECT "+tableName+".*, COUNT(*) AS group_count FROM " + tableName +" WHERE "+where+" "+
                        "GROUP BY "+groupBy+" ORDER BY group_count DESC LIMIT "+count;
                ResultSet resultSet = statement.executeQuery(sql);

                while (resultSet.next()) {
                    T t = tClass.newInstance();
                    int ct = resultSet.getInt("group_count");
                    explainClass(resultSet,tClass,t);
                    DataCount<T> dc = new DataCount<>();
                    dc.data = t;
                    dc.count = ct;
                    datas.add(dc);
                }
                resultSet.close();
                return datas;

            }catch (SQLException | InstantiationException | IllegalAccessException e){
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }



    public <T> T get(String tableName,int id, Class<T> clazz){
        T instance = null;
        try {
            String query = "SELECT * FROM " + tableName + " WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            instance = explainClass(resultSet,clazz,clazz.newInstance());
            resultSet.close();
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;

    }

    public <T> T get(String tableName,String key,String value, Class<T> clazz){
        T instance = null;
        try {
            // 准备 SQL 查询语句
            String query = "SELECT * FROM " + tableName + " WHERE " + key + " = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, value);

            ResultSet resultSet = statement.executeQuery();
            T t = clazz.newInstance();

            explainClass(resultSet,clazz,t);
            resultSet.close();

        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;

    }

    public <T> LinkedList<T> getDataByString(String tableName, String selection, String[] key, Class<T> clazz) {
        LinkedList<T> datas = new LinkedList<>();
        try {
            // 准备 SQL 查询语句
            String query = "SELECT * FROM " + tableName + " WHERE " + selection;
            PreparedStatement statement = connection.prepareStatement(query);

            // 设置查询条件
            for (int i = 0; i < key.length; i++) {
                statement.setString(i + 1, key[i]);
            }

            // 执行查询
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                T t = clazz.newInstance();
                datas.add(explainClass(resultSet,clazz,t));
            }
            resultSet.close();

        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return datas;
    }



    public <T> LinkedList<T> getAll(String tableName,Class<T> clazz){
        LinkedList<T> datas = new LinkedList<>();
        try {
            // 准备 SQL 查询语句
            String query = "SELECT * FROM " + tableName;
            PreparedStatement statement = connection.prepareStatement(query);

            // 执行查询
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                T t = clazz.newInstance();
                datas.add(explainClass(resultSet,clazz,t));
            }

            resultSet.close();

        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return datas;
    }

    private String classTypeAsSql(Field type){
        if(type.getType() == int.class ){
            return "integer";
        }
        if(type.getType() == float.class || type.getType() == double.class){
            return type.getType().getName();
        }else{
            return "text";
        }

    }

    private <T> T explainClass(ResultSet cursor, Class<?> tc, T t){
        try {
            ResultSetMetaData rsmd = cursor.getMetaData();
            for(int i = 0;i < rsmd.getColumnCount();i++){
                String name = rsmd.getColumnName(i + 1);
                Field field = null;
                try {
                    field = tc.getField(name);
                }catch (Exception ignore){
                }
                if(field == null){
                    continue;
                }
                if(field.getType() == int.class){
                    field.set(t, cursor.getInt(name));
                }else
                if(field.getType() == float.class || field.getType() == double.class){
                    field.set(t, cursor.getFloat(name));
                }else
                if(field.getType() == boolean.class){
                    field.set(t, Boolean.valueOf(cursor.getString(name)));

                }else
                if(field.getType() == long.class){
                    field.set(t, cursor.getLong(name));

                }else{
                    String v = cursor.getString(name);
                    if("null".equalsIgnoreCase(v)){
                        v = null;
                    }
                    field.set(t,v);
                }

            }

            } catch (Exception e) {
                e.printStackTrace();
            }
        return t;
    }



    public static class DBTable {
        LinkedHashMap<String,String> tables = new LinkedHashMap<>();

        public DBTable(String key,String value){
            tables.put(key, value);
        }

        public DBTable(Map<String,String> m){
            tables.putAll(m);
        }

        public DBTable put(String key,String value){
            tables.put(key, value);
            return this;
        }

        public String asSql(){
            StringBuilder s = new StringBuilder();
            for (Map.Entry<String,String> e:tables.entrySet()) {
                s.append(e.getKey()).append(" ").append(e.getValue()).append(",");
            }
            return s.substring(0,s.length() - 1);

        }



        public static DBTable asDbTable(Class<?> t){
            Field[] fields = t.getFields();
            LinkedHashMap<String,String> stringStringLinkedHashMap = new LinkedHashMap<>();
            boolean isId = false;
            // 先找自增id
            for(Field field: fields){
                if ("id".equalsIgnoreCase(field.getName()) && (field.getType() == long.class
                        || field.getType() == int.class)) {
                    //找到了
                    isId = true;
                    break;
                }
            }
            if(!isId){
                throw new NullPointerException("数据库类需要一个id");
            }
            stringStringLinkedHashMap.put("id","integer primary key autoincrement");
            for(Field field: fields){
                if("id".equalsIgnoreCase(field.getName()) && field.getType() == int.class){
                    //找到了
                    continue;
                }
                if(field.getType() == float.class || field.getType() == double.class){
                    stringStringLinkedHashMap.put(field.getName().toLowerCase(),field.getType().getName());
                }else if(field.getType() == int.class){
                    stringStringLinkedHashMap.put(field.getName().toLowerCase(),"integer");
                }else{
                    stringStringLinkedHashMap.put(field.getName().toLowerCase(),"text");
                }
            }
            return new DBTable(stringStringLinkedHashMap);

        }

    }


    private Connection getConnection() throws ClassNotFoundException, SQLException {
        if (null == connection) {
            connection = getConnection(dbFilePath);
        }
        return connection;
    }

    private Statement getStatement() throws SQLException, ClassNotFoundException {
        if (null == statement) {
            statement = getConnection().createStatement();
        }
        return statement;
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

            if (null != statement) {
                statement.close();
                statement = null;
            }

        } catch (SQLException e) {
            System.out.println("Sqlite数据库关闭时异常 "+ e);
        }
    }

    public static class DataCount<T>{

        public T data;

        public int count;
    }


}
