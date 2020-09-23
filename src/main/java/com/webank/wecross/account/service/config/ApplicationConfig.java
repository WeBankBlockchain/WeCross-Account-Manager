package com.webank.wecross.account.service.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.service.exception.ConfigurationException;
import com.webank.wecross.account.service.utils.FileUtility;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
[service]
    address = '0.0.0.0'
    port = 8340
    sslKey = 'classpath:ssl.key'
    sslCert = 'classpath:ssl.crt'
    caCert = 'classpath:ca.crt'
    sslOn = true

[admin]
    name = 'org1-admin'
    password = '123456'

[auth]
    # for issuing token
    name = 'org1'
    expires = 18000 # 5 h
    noActiveExpires = 600 # 10 min

[db]
    # for connect database
    url = 'jdbc:mysql://localhost:3306/wecross_account_manager'
    username = 'root'
    password = ''
*/

public class ApplicationConfig {
    private static Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    public Service service;
    public Admin admin;
    public Auth auth;
    public DB db;

    public static ApplicationConfig parseFromFile(String filePath) throws ConfigurationException {
        Toml toml = FileUtility.readToml(filePath);
        ApplicationConfig config = new ApplicationConfig(toml);
        return config;
    }

    public ApplicationConfig(Toml toml) throws ConfigurationException {
        this.service = new Service(toml);
        this.admin = new Admin(toml);
        this.auth = new Auth(toml);
        this.db = new DB(toml);
    }

    @Override
    public String toString() {
        return "ApplicationConfig{" + "service=" + service + ", auth=" + auth + ", db=" + db + '}';
    }

    class Service {
        public String address;
        public int port;
        public String sslKey;
        public String sslCert;
        public String caCert;
        public boolean sslOn;

        Service(Toml toml) throws ConfigurationException {
            this.address = parseString(toml, "service.address");
            this.port = parseInt(toml, "service.port");
            this.sslOn = parseBoolean(toml, "service.sslOn", true); // default true
            if (this.sslOn) {
                this.sslKey = parseString(toml, "service.sslKey");
                this.sslCert = parseString(toml, "service.sslCert");
                this.caCert = parseString(toml, "service.caCert");
            }
            logger.info("Load configuration: " + this.toString());
        }

        @Override
        public String toString() {
            return "Service{"
                    + "address='"
                    + address
                    + '\''
                    + ", port="
                    + port
                    + ", sslKey='"
                    + sslKey
                    + '\''
                    + ", sslCert='"
                    + sslCert
                    + '\''
                    + ", caCert='"
                    + caCert
                    + '\''
                    + ", sslOn="
                    + sslOn
                    + '}';
        }
    }

    class Admin {
        public String name;
        public String password;

        Admin(Toml toml) throws ConfigurationException {
            this.name = parseString(toml, "admin.name");
            this.password = parseString(toml, "admin.password");
            logger.info("Load configuration: " + this.name);
        }

        @Override
        public String toString() {
            return "Admin{" + "name='" + name + '\'' + '}';
        }
    }

    class Auth {
        public String name;
        public long expires;
        public long noActiveExpires;

        Auth(Toml toml) throws ConfigurationException {
            this.name = parseString(toml, "auth.name");
            this.expires = parseLong(toml, "auth.expires", 18000); // default 5h
            this.noActiveExpires = parseLong(toml, "auth.noActiveExpires", 600); // default 600s

            logger.info("Load configuration: " + this.toString());
        }

        @Override
        public String toString() {
            return "Auth{"
                    + "name='"
                    + name
                    + '\''
                    + ", expires="
                    + expires
                    + ", noActiveExpires="
                    + noActiveExpires
                    + '}';
        }
    }

    class DB {
        public String url;
        public String username;
        public String password;

        DB(Toml toml) throws ConfigurationException {
            this.url = parseString(toml, "db.url");
            this.username = parseString(toml, "db.username");
            this.password = parseString(toml, "db.password");

            logger.info("Load configuration: " + this.toString());
        }

        @Override
        public String toString() {
            return "DB{"
                    + "url='"
                    + url
                    + '\''
                    + ", username='"
                    + username
                    + '\''
                    + ", password='"
                    + password
                    + '\''
                    + '}';
        }
    }

    private static boolean parseBoolean(Toml toml, String key, boolean defaultReturn) {
        Boolean res = toml.getBoolean(key);

        if (res == null) {
            logger.info(key + " has not set, default to " + defaultReturn);
            return defaultReturn;
        }
        return res.booleanValue();
    }

    private static int parseInt(Toml toml, String key, int defaultReturn) {
        Long res = toml.getLong(key);

        if (res == null) {
            logger.info(key + " has not set, default to " + defaultReturn);
            return defaultReturn;
        }
        return res.intValue();
    }

    private static int parseInt(Toml toml, String key) throws ConfigurationException {
        Long res = toml.getLong(key);

        if (res == null) {
            String errorMessage = "\"" + key + "\" item not found";
            throw new ConfigurationException(errorMessage);
        }
        return res.intValue();
    }

    private static long parseLong(Toml toml, String key, long defaultReturn) {
        Long res = toml.getLong(key);

        if (res == null) {
            logger.info(key + " has not set, default to " + defaultReturn);
            return defaultReturn;
        }
        return res.longValue();
    }

    private static String parseString(Toml toml, String key, String defaultReturn) {
        try {
            return parseString(toml, key);
        } catch (ConfigurationException e) {
            return defaultReturn;
        }
    }

    private static String parseString(Toml toml, String key) throws ConfigurationException {
        String res = toml.getString(key);

        if (res == null) {
            String errorMessage = "\"" + key + "\" item not found";
            throw new ConfigurationException(errorMessage);
        }
        return res;
    }

    private static String parseString(Map<String, String> map, String key)
            throws ConfigurationException {
        String res = map.get(key);

        if (res == null) {
            String errorMessage = "\"" + key + "\" item not found";
            throw new ConfigurationException(errorMessage);
        }
        return res;
    }

    private static String parseStringBase(Map<String, Object> map, String key)
            throws ConfigurationException {
        @SuppressWarnings("unchecked")
        String res = (String) map.get(key);

        if (res == null) {
            String errorMessage = "\"" + key + "\" item not found";
            throw new ConfigurationException(errorMessage);
        }
        return res;
    }

    private static List<String> parseStringList(Map<String, Object> map, String key)
            throws ConfigurationException {
        @SuppressWarnings("unchecked")
        List<String> res = (List<String>) map.get(key);

        if (res == null) {
            String errorMessage = "\"" + key + "\" item illegal";
            throw new ConfigurationException(errorMessage);
        }
        return res;
    }
}
