package cn.tanzhou.starter.apollo.client;

/**
 * @author 敖癸
 * @date 2020/12/8 - 16:58
 */
public class TzMetaAddress {

    public static final String DEV_META_ADDRESS = "http://10.0.217.70:8080/,http://10.0.216.217:8080/";

    public static final String TEST_META_ADDRESS = "http://10.17.217.192:8080/,http://10.17.216.178:8080/";

    public static final String PRE_META_ADDRESS = "http://10.18.216.27:8080/,http://10.18.217.103:8080/";

    public static final String PROD_META_ADDRESS = "http://10.19.217.236:8080/,http://10.19.217.237:8080/,http://10.19.216.152:8080/,http://10.19.216.151:8080/";

    public static final String DEFAULT_ENV = "dev";

    public static String getTzMetaAddress(String env) {
        switch (env) {
            case "test":
                return TzMetaAddress.TEST_META_ADDRESS;
            case "pre":
                return TzMetaAddress.PRE_META_ADDRESS;
            case "prod":
                return TzMetaAddress.PROD_META_ADDRESS;
            default:
                return TzMetaAddress.DEV_META_ADDRESS;
        }
    }
}
