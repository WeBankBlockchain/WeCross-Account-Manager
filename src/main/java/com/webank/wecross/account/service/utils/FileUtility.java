package com.webank.wecross.account.service.utils;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.account.service.exception.ConfigurationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class FileUtility {

    private static Logger logger = LoggerFactory.getLogger(FileUtility.class);

    public static String readFileContent(String fileName) throws ConfigurationException {
        try {
            Path path;

            if (fileName.indexOf("classpath:") != 0) {
                path = Paths.get(fileName);
            } else {
                // Start with "classpath:"
                PathMatchingResourcePatternResolver resolver =
                        new PathMatchingResourcePatternResolver();
                path = Paths.get(resolver.getResource(fileName).getURI());
            }

            String content = new String(Files.readAllBytes(path));
            if (logger.isDebugEnabled()) {
                logger.debug("readFileContent, fileName: {}", fileName);
            }
            return content;
        } catch (Exception e) {
            logger.error("fileName: {}, e:", fileName, e);
            throw new ConfigurationException("Read file error: " + e);
        }
    }

    public static Toml readToml(String fileName) throws ConfigurationException {
        return new Toml().read(readFileContent(fileName));
    }
}
