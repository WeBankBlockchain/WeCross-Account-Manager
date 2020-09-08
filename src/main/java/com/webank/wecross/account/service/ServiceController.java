package com.webank.wecross.account.service;

import com.webank.wecross.account.service.config.ServiceConfig;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {
    @Resource ServiceConfig serviceConfig;

    @RequestMapping("/test")
    private Object test() {
        return "OK!";
    }

    /*
    @RequestMapping(value = "/**", method = RequestMethod.POST, produces = "application/json")
    private Object echo(@RequestBody String params) {
        return params;
    }
    */

    @RequestMapping(
            value = "/auth/register",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object register(@RequestBody String params) {
        return "{  \n"
                + "    \"version\":\"1\",\n"
                + "    \"errorCode\":0,\n"
                + "    \"message\":\"success\",\n"
                + "    \"data\":{\n"
                + "        \"errorCode\": 0,\n"
                + "        \"message\": \"success\"\n"
                + "\t}\n"
                + "}";
    }

    @RequestMapping(value = "/auth/logout", produces = "application/json")
    private Object logout(@RequestBody String params) {
        return "{  \n"
                + "    \"version\":\"1\",\n"
                + "    \"errorCode\":0,\n"
                + "    \"message\":\"success\",\n"
                + "    \"data\":{\n"
                + "        \"errorCode\": 0,\n"
                + "        \"message\": \"success\"\n"
                + "\t}\n"
                + "}";
    }

    @RequestMapping(
            value = "/auth/addChainAccount",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object addChainAccount(@RequestBody String params) {
        return "{  \n"
                + "    \"version\":\"1\",\n"
                + "    \"errorCode\":0,\n"
                + "    \"message\":\"success\",\n"
                + "    \"data\":{\n"
                + "        \"errorCode\": 0,\n"
                + "        \"message\": \"success\"\n"
                + "\t}\n"
                + "}";
    }

    @RequestMapping(
            value = "/auth/setDefaultAccount",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object setDefaultAccount(@RequestBody String params) {
        return "{  \n"
                + "    \"version\":\"1\",\n"
                + "    \"errorCode\":0,\n"
                + "    \"message\":\"success\",\n"
                + "    \"data\":{\n"
                + "        \"errorCode\": 0,\n"
                + "        \"message\": \"success\"\n"
                + "\t}\n"
                + "}";
    }

    @RequestMapping(value = "/auth/listAccount", produces = "application/json")
    private Object listAccount(@RequestBody String params) {
        return "{  \n"
                + "    \"version\":\"1\",\n"
                + "    \"errorCode\":0,\n"
                + "    \"message\":\"success\",\n"
                + "    \"data\":{\n"
                + "        \"name\":\"xxxxx\",\n"
                + "        \"uaId\":\"xxxx\",\n"
                + "        \"pubKey\": \"xxxx\",\n"
                + "        \"chainAccounts\" : [\n"
                + "            {\n"
                + "                \"type\": \"BCOS2.0\",\n"
                + "                \"pubKey\": \"xxxxx\",\n"
                + "                \"address\": \"xxxxxx\",\n"
                + "                \"UAProof\":\"xxxxxx\",\n"
                + "                \"isDefault\": true       \n"
                + "            },\n"
                + "            {\n"
                + "                \"type\": \"BCOS2.0\",\n"
                + "                \"pubKey\": \"xxxxx\",\n"
                + "                \"address\": \"xxxxxx\",\n"
                + "                \"UAProof\":\"xxxxxx\",\n"
                + "                \"isDefault\": false       \n"
                + "            },\n"
                + "            {\n"
                + "                \"type\": \"GM_BCOS2.0\",\n"
                + "                \"pubKey\": \"xxxxx\",\n"
                + "                \"address\": \"xxxxxx\",\n"
                + "                \"UAProof\":\"xxxxxx\",\n"
                + "                \"isDefault\": true\n"
                + "            },\n"
                + "            {\n"
                + "                \"type\": \"Fabric1.4\",\n"
                + "                \"cert\": \"xxxxx\",\n"
                + "                \"UAProof\":\"xxxxxx\",\n"
                + "                \"isDefault\": true\n"
                + "            }\n"
                + "        ]\n"
                + "    }\n"
                + "}";
    }
}
