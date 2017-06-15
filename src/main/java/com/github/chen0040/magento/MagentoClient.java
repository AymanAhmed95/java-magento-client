package com.github.chen0040.magento;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.chen0040.magento.models.*;
import com.github.chen0040.magento.services.MagentoInventoryStockManager;
import com.github.chen0040.magento.services.MagentoCategoryManager;
import com.github.chen0040.magento.services.MagentoHttpComponent;
import com.github.chen0040.magento.services.MagentoProductManager;
import com.github.chen0040.magento.utils.HttpClient;
import com.github.chen0040.magento.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by xschen on 12/6/2017.
 */
@Getter
@Setter
public class MagentoClient extends MagentoHttpComponent implements Serializable {
   private static final long serialVersionUID = 3001998767951271632L;
   private static final String relativePath4LoginAsClient = "rest/V1/integration/customer/token";
   private static final String relativePath4LoginAsAdmin = "rest/V1/integration/admin/token";




   private static final Logger logger = LoggerFactory.getLogger(MagentoClient.class);

   private String token = null;

   private String baseUri = "";

   private boolean admin = false;

   private boolean authenticated = false;

   private MagentoProductManager products;
   private MagentoCategoryManager categories;
   private MagentoInventoryStockManager inventory;

   public MagentoClient(String baseUri) {
      this.baseUri = baseUri;
      this.products = new MagentoProductManager(this);
      this.categories = new MagentoCategoryManager(this);
      this.inventory = new MagentoInventoryStockManager(this);
   }

   public Account getMyAccount() {
      if(admin){
         logger.warn("my account access api is not supported for admin rest call");
         return null;
      }

      //"http://magento.ll/index.php/rest/V1/customers/me" -H "Authorization: Bearer asdf3hjklp5iuytre"
      String uri = this.baseUri + "/rest/V1/customers/me";
      String json = getSecured(uri);

      if(!validate(json)) {
         return null;
      }

      return JSON.parseObject(json, Account.class);
   }

   public Map<String, Object> getAccountById(long id) {
      if(!admin){
         logger.warn("other account access api is not supported for client rest call");
         return new HashMap<>();
      }

      String uri = this.baseUri + "/rest/V1/customers/" + id;
      String json = getSecured(uri);
      Map<String, Object> data = JSON.parseObject(json, new TypeReference<Map<String, Object>>(){}.getType());
      return data;
   }

   public String loginAsClient(String username, String password) {
      String uri = baseUri + "/" + relativePath4LoginAsClient;
      Map<String, String> data = new HashMap<>();
      data.put("username", username);
      data.put("password", password);
      this.token = StringUtils.stripQuotation(HttpClient.jsonPost(uri, data));
      logger.info("loginAsClient returns: {}", token);
      if(!token.contains("Invalid login or password")){
         authenticated = true;
      }
      return token;
   }

   public String loginAsAdmin(String username, String password) {
      String uri = baseUri + "/" + relativePath4LoginAsAdmin;
      Map<String, String> data = new HashMap<>();
      data.put("username", username);
      data.put("password", password);
      token = StringUtils.stripQuotation(HttpClient.jsonPost(uri, data));
      logger.info("loginAsClient returns: {}", token);
      if(!token.contains("Invalid login or password")){
         authenticated = true;
      }
      return token;
   }

   public MagentoCategoryManager categories() {
      return categories;
   }

   public MagentoProductManager products() {
      return products;
   }

   public MagentoInventoryStockManager inventory() {
      return inventory;
   }


   @Override public String token() {
      return this.token;
   }


   @Override public String baseUri() {
      return this.baseUri;
   }
}