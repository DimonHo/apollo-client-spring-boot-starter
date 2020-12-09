# apollo-client-spring-boot-starter

潭州平台apollo配置中心快速启动器

用法：
```
<dependency>
    <groupId>cn.tanzhou.starter</groupId>
    <artifactId>apollo-client-spring-boot-starter</artifactId>
    <version>1.8.0-SNAPSHOT</version>
</dependency>
```

引入后无需任何配置，即可直接接入潭州平台apollo配置中心。
即默认`apollo.bootstrap.enabled`=`true`



`spring.application.name` 等价于 `app.id`，二者必选其一。
二者同时配置时，`app.id`优先级高于`spring.application.name`

`spring.profiles.active` 等价于 `app.env`, 可以两个都不配，默认环境：dev
二者同时配置时，`app.env`优先级高于`spring.profiles.active`


