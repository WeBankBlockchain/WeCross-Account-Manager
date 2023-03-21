package com.webank.wecross.account.service.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.service.exception.ConfigurationException;
import com.webank.wecross.account.service.utils.FileUtility;
import java.util.List;
import java.util.Map;
import lombok.Data;
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
    username = 'org1-admin'
    password = '123456'

[auth]
    # for issuing token
    username = 'org1'
    expires = 18000 # 5 h
    noActiveExpires = 600 # 10 min

[encrypt]
    # for http request data encrypt
    privateKeyFile = 'classpath:private.pem'
    publicKeyFile = classpath:public.pem'

[db]
    # for connect database
    url = 'jdbc:mysql://localhost:3306/wecross_account_manager'
    username = 'root'
    password = ''

[ext]
    # for image auth code
    allowImageAuthCodeEmpty = true
*/

public class ApplicationConfig {
    private static Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    private Service service;
    private Admin admin;
    private Encrypt encrypt;
    private Auth auth;
    private Mail mail;
    private DB db;
    private Ext ext;

    public static ApplicationConfig parseFromFile(String filePath) throws ConfigurationException {
        Toml toml = FileUtility.readToml(filePath);
        ApplicationConfig config = new ApplicationConfig(toml);
        return config;
    }

    public ApplicationConfig(Toml toml) throws ConfigurationException {
        this.service = new Service(toml);
        this.admin = new Admin(toml);
        this.auth = new Auth(toml);
        this.mail = new Mail(toml);
        this.db = new DB(toml);
        this.encrypt = new Encrypt(toml);
        this.ext = new Ext(toml);
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Encrypt getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(Encrypt encrypt) {
        this.encrypt = encrypt;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    public Ext getExt() {
        return ext;
    }

    public void setExt(Ext ext) {
        this.ext = ext;
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    @Override
    public String toString() {
        return "ApplicationConfig{"
                + "service="
                + service
                + ", admin="
                + admin
                + ", encrypt="
                + encrypt
                + ", auth="
                + auth
                + ", mail="
                + mail
                + ", db="
                + db
                + ", ext="
                + ext
                + '}';
    }

    class Service {
        private String address;
        private int port;
        private String sslKey;
        private String sslCert;
        private String caCert;
        private boolean sslOn;

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

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getSslKey() {
            return sslKey;
        }

        public void setSslKey(String sslKey) {
            this.sslKey = sslKey;
        }

        public String getSslCert() {
            return sslCert;
        }

        public void setSslCert(String sslCert) {
            this.sslCert = sslCert;
        }

        public String getCaCert() {
            return caCert;
        }

        public void setCaCert(String caCert) {
            this.caCert = caCert;
        }

        public boolean isSslOn() {
            return sslOn;
        }

        public void setSslOn(boolean sslOn) {
            this.sslOn = sslOn;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
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

    class Encrypt {

        public Encrypt(Toml toml) {
            try {
                this.privateKey = parseString(toml, "encrypt.privateKeyFile");
                this.publicKey = parseString(toml, "encrypt.publicKeyFile");
            } catch (ConfigurationException e) {
                logger.debug("e: ", e);
            }
            logger.info(" rsa private: {}", this.privateKey);
            logger.info(" rsa public: {}", this.publicKey);
        }

        private String privateKey;
        private String publicKey;

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        @Override
        public String toString() {
            return "Encrypt{"
                    + "privateKey='"
                    + privateKey
                    + '\''
                    + ", publicKey='"
                    + publicKey
                    + '\''
                    + '}';
        }
    }

    public class Admin {
        private String username;
        private String password;

        Admin(Toml toml) throws ConfigurationException {
            try {
                this.username = parseString(toml, "admin.username");
            } catch (ConfigurationException e) {
                this.username = parseString(toml, "admin.name");
            }
            this.password = parseString(toml, "admin.password");

            if (this.username.length() >= 256) {
                throw new ConfigurationException(
                        "admin.username(length:"
                                + this.username.length()
                                + ") must smaller than 256");
            }

            if (this.password.length() >= 256) {
                throw new ConfigurationException("admin.password must smaller than 256");
            }

            logger.info("Load configuration: " + this.username);
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "Admin{" + "username='" + username + '\'' + '}';
        }
    }

    class Auth {
        private static final long EXPIRES_LIMIT = 300000000; // second
        private String name;
        private long expires;
        private long noActiveExpires;
        private boolean needMailAuth;

        Auth(Toml toml) throws ConfigurationException {
            this.name = parseString(toml, "auth.name");
            this.expires = parseULong(toml, "auth.expires", 18000); // default 5h
            this.noActiveExpires = parseULong(toml, "auth.noActiveExpires", 600); // default 600s
            this.needMailAuth = parseBoolean(toml, "auth.needMailAuth", false);
            if (this.name.length() >= 256) {
                throw new ConfigurationException(
                        "auth.name(length:" + this.name.length() + ") must smaller than 256");
            }

            if (this.expires > EXPIRES_LIMIT) {
                throw new ConfigurationException(
                        "auth.expires(" + this.expires + ") must no more than " + EXPIRES_LIMIT);
            }

            if (this.noActiveExpires > EXPIRES_LIMIT) {
                throw new ConfigurationException(
                        "auth.noActiveExpires("
                                + this.noActiveExpires
                                + ") must no more than "
                                + EXPIRES_LIMIT);
            }

            logger.info("Load configuration: " + this.toString());
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getExpires() {
            return expires;
        }

        public void setExpires(long expires) {
            this.expires = expires;
        }

        public long getNoActiveExpires() {
            return noActiveExpires;
        }

        public void setNoActiveExpires(long noActiveExpires) {
            this.noActiveExpires = noActiveExpires;
        }

        public boolean isNeedMailAuth() {
            return needMailAuth;
        }

        public void setNeedMailAuth(boolean needMailAuth) {
            this.needMailAuth = needMailAuth;
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
                    + ", needMailAuth="
                    + needMailAuth
                    + '}';
        }
    }

    @Data
    public static class Mail {
        private String address;
        private String password;
        private String smtpPort;

        Mail(Toml toml) throws ConfigurationException {
            this.address = parseString(toml, "mail.address", "");
            this.password = parseString(toml, "mail.password", "");
            this.smtpPort = parseString(toml, "mail.smtpPort", "");
            logger.info("Load mail configuration: " + this);
        }
    }

    class DB {
        private String url;
        private String username;
        private String password;
        private String encryptKey;

        DB(Toml toml) throws ConfigurationException {
            this.url = parseString(toml, "db.url");
            this.username = parseString(toml, "db.username");
            this.password = parseString(toml, "db.password");
            try {
                this.encryptKey = parseString(toml, "db.encryptKey");
            } catch (Exception e) {

            }

            logger.info("Load configuration: " + this.toString());
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEncryptKey() {
            return encryptKey;
        }

        public void setEncryptKey(String encryptKey) {
            this.encryptKey = encryptKey;
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
                    + ", encryptKey='"
                    + encryptKey
                    + '\''
                    + '}';
        }
    }

    /** Ext configurations */
    public class Ext {

        private boolean allowImageAuthCodeEmpty = true;
        private String routerLoginAccountPassword;

        public Ext(Toml toml) {
            this.allowImageAuthCodeEmpty = parseBoolean(toml, "ext.allowImageAuthCodeEmpty", true);
            // default 12345678
            this.routerLoginAccountPassword =
                    parseString(toml, "ext.routerLoginAccountPassword", "12345678");
        }

        public boolean isAllowImageAuthCodeEmpty() {
            return allowImageAuthCodeEmpty;
        }

        public void setAllowImageAuthCodeEmpty(boolean allowImageAuthCodeEmpty) {
            this.allowImageAuthCodeEmpty = allowImageAuthCodeEmpty;
        }

        public String getRouterLoginAccountPassword() {
            return routerLoginAccountPassword;
        }

        public void setRouterLoginAccountPassword(String routerLoginAccountPassword) {
            this.routerLoginAccountPassword = routerLoginAccountPassword;
        }

        @Override
        public String toString() {
            return "Ext{"
                    + "allowImageAuthCodeEmpty="
                    + allowImageAuthCodeEmpty
                    + ", routerLoginAccountPassword='"
                    + routerLoginAccountPassword
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

    private static long parseULong(Toml toml, String key, long defaultReturn)
            throws ConfigurationException {
        long res = parseLong(toml, key, defaultReturn);
        if (res < 0) {
            throw new ConfigurationException("key " + key + " must no less than 0");
        }
        return res;
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

    /*
    private static String parseString(Map<String, String> map, String key)
            throws ConfigurationException {
        String res = map.get(key);

        if (res == null) {
            String errorMessage = "\"" + key + "\" item not found";
            throw new ConfigurationException(errorMessage);
        }
        return res;
    }
    */

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
