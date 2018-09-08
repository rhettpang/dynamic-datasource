package dynamic.datasource.gray;

/**
 * @author Created by pangkunkun on 2018/8/28.
 */
public class DynamicDataSourceHolder {

    private static  String USEFUL_DB = null;

    public static void putDataSource(String name) {
        USEFUL_DB = name;
    }

    public static String getDataSource() {
        return USEFUL_DB;
    }

}
