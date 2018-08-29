简介

本项目用来动态切换多个数据源，原则上用户只要引入本项目，在相关配置文件中加入相关数据源的配置即可。目前是对slave数据源进行动态切换。

配置信息如下

```
slave:
  hosts: slave1,slave2
dynamic:
  hikari:
    - master:
      jdbc-url: jdbc:mysql://10.10.20.127:3306/authcenter?useUnicode=true&characterEncoding=utf8&useSSL=true&allowMultiQueries=true&verifyServerCertificate=false
      username: root
      password: root
      pool-name: master
    - slave1:
      jdbc-url: jdbc:mysql://10.10.20.127:3306/authcenter?useUnicode=true&characterEncoding=utf8&useSSL=true&allowMultiQueries=true&verifyServerCertificate=false
      username: root
      password: root
      pool-name: slave1
      read-only: true
    - slave2:
      jdbc-url: jdbc:mysql://10.10.20.127:3306/authcenter?useUnicode=true&characterEncoding=utf8&useSSL=true&allowMultiQueries=true&verifyServerCertificate=false
      username: root
      password: root
      pool-name: slave2
      read-only: true
```
slave.hosts指定所有要动态切换的数据源名称，这里要跟相应的pool-name保持一致。
pool-name指定数据源的名称，在项目中根据pool-name来区分每个数据源。
其它的配置参数可以自行添加，这里只给出了最简单的信息。

在项目中通过@ConfigurationProperties来读取所有数据源的信息，具体如下:

```
@Component
@ConfigurationProperties(prefix = "dynamic")
public class DBProperties {
    //一次性从配置文件中读取所有数据源的配置
    private Map<String, HikariDataSource> hikari;

    public Map<String, HikariDataSource> getHikari() {
        return hikari;
    }

    public void setHikari(Map<String, HikariDataSource> hikari) {
        this.hikari = hikari;
    }
}
```

通过集成AbstractRoutingDataSource来指定要使用的数据源

```
public class DynamicDataSource extends AbstractRoutingDataSource {
    /**
     * 数据源路由，此方用于产生要选取的数据源逻辑名称
     */
    @Override
    protected Object determineCurrentLookupKey() {
        //从共享线程中获取数据源名称
        return DynamicDataSourceHolder.getDataSource();
    }
}
```
数据源的设置部分在DataSourceConfig下

```
@Bean(name = "dataSource")
    public DataSource dataSource() {
        //按照目标数据源名称和目标数据源对象的映射存放在Map中
        Map<Object, Object> targetDataSources = new HashMap<>();
        //获取配置文件中的数据源
        Map<String, HikariDataSource> hikaris = properties.getHikari();
        Set<String> keys = hikaris.keySet();
        HikariDataSource hikariDataSource = null;
        HikariDataSource masterDataSource = null;
        String poolName = "";
        for (String key : keys){
            hikariDataSource = hikaris.get(key);
            poolName = hikariDataSource.getPoolName();
            targetDataSources.put(hikariDataSource.getPoolName(),hikariDataSource);
            if (poolName.equals(KEY_MASTER)){
                masterDataSource = hikariDataSource;
            }
        }

        DynamicDataSource dataSource = new DynamicDataSource();
        dataSource.setTargetDataSources(targetDataSources);
        //设置默认的数据源，当拿不到数据源时，使用此配置
        if (null != masterDataSource){
            dataSource.setDefaultTargetDataSource(masterDataSource);
        }else {
            logger.error("Can't find master db, project will be exit");
            System.exit(0);
        }
        return dataSource;
    }
```
目前是将master设为默认数据源，后期会设置为可配置的。
另外如果找不到master数据源会退出项目，这主要是跟我现在的项目是读写分离有关，后期会去掉这块，改为可选项。

数据源的引用是通过AOP来实现，相关代码在DataSourceAspect中

```
/**
     * 这里用来拦截有{@link TargetDataSource}注解的方法
     * */
    @Before("@annotation(targetDataSource)")
    public void before(JoinPoint joinPoint ,TargetDataSource targetDataSource) {
        try {
            String dataSourceName = targetDataSource.value();
            //判断指定的数据源类型，如果是slave，则调用LB方法，随机分配slave数据库
            if (dataSourceName.equals("slave")){
                dataSourceName = DBLoadBalance.getDBWithRandom(slaveHosts);
            }
            //设置要使用的数据源
            DynamicDataSourceHolder.putDataSource(dataSourceName);
            log.debug("current thread " + Thread.currentThread().getName() + " add " + dataSourceName + " to ThreadLocal");
        } catch (Exception e) {
            log.error("current thread " + Thread.currentThread().getName() + " add data to ThreadLocal error", e);
        }
    }
```
这里是通过监听TargetDataSource 注解来实现，如果传入的是slave，则进行数据源的随机指定，这个参数后面会改为可动态指定。
DBLoadBalance是我写的一个简单的随机指定数据源的工具类，目前仅实现了随机获取的功能

```
    /**
     * 随机获取db
     * */
    public static String getDBWithRandom(String dbs){
        String[] slaves = dbs.split(",");
        int num = new Random().nextInt(slaves.length);
        return slaves[num];
    }
```

使用

目前仅在GitHub上，要使用的话先下载到本地，然后通过install的方式安装到本地maven仓库，然后在项目中引入。

 - 下载到本地
 - 导入IDE，执行mvn package生成jar包
 - install jar到本地maven仓库
 - 项目引入

install参考指令，进入生成的jar所在目录，执行如下指令

```
mvn install:install-file -Dfile=dynamic-datasource-0.1.0.jar -DgroupId=com.pk -DartifactId=dynamic-datasource -Dversion=0.1.0 -Dpackaging=jar
```
在项目中引入

```
		<dependency>
			<groupId>com.pk</groupId>
			<artifactId>dynamic-datasource</artifactId>
			<version>0.1.0</version>
		</dependency>
```

在启动类加载本项目的所需的类，使用@EnableDynamicDataSource

```
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDynamicDataSource
public class StartApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartApplication .class, args);
	}
}
```
exclude = DataSourceAutoConfiguration.class这里是防止在没有spring.datasource配置的时候报错。

在项目中根据需求在相应的方法上添加@TargetDataSource("slave")来使用数据源的动态切换。建议在service下注入。如果在mapper中可能会不生效。

```
    @Override
    @TargetDataSource("slave")
    public User getByUserId(String userId) {
        return userMapper.getByUserId(userId);
    }
```

说明，项目中数据源切换这块很多是参考网上的代码，很感谢他们提供的宝贵资源。之所以在有这么多参考的情况下还做这个插件，个人感觉网上的参考都不够灵活（至少我搜到的文章是这样的），还是那句话我的目标是做一个足够灵活的可插拔的只需要在配置文件中修改就可以使用的组件。

这算是我第一次比较相对正式的提交项目，有什么不足还请大家批评指正，后面我会对这个小插件持续更新优化。

另外附上我在CSDN上相关的文章，仅供大家参考[Spring Boot HikariCP 一 ——集成多数据源](https://blog.csdn.net/qq_35981283/article/details/78846892)。
