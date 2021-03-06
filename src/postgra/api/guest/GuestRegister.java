/*
    Source https://github.com/evanx by @evanxsummers

        Licensed to the Apache Software Foundation (ASF) under one
        or more contributor license agreements. See the NOTICE file
        distributed with this work for additional information
        regarding copyright ownership. The ASF licenses this file to
        you under the Apache License, Version 2.0 (the "License").
        You may not use this file except in compliance with the
        License. You may obtain a copy of the License at:

           http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing,
        software distributed under the License is distributed on an
        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied.  See the License for the
        specific language governing permissions and limitations
        under the License.  
 */
package postgra.api.guest;

import java.security.GeneralSecurityException;
import java.util.Date;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraApp;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import postgra.entity.Person;
import vellum.crypto.hmac.Hmacs;
import vellum.crypto.topt.Totps;
import vellum.jx.JMap;
import vellum.jx.JMapException;
import vellum.jx.JMapsException;

/**
 *
 * @author evan.summers
 */
public class GuestRegister implements PostgraHttpxHandler {
    
    static Logger logger = LoggerFactory.getLogger(GuestRegister.class); 

    JMap responseMap = new JMap();
    
    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        final JMap requestMap = httpx.parseJsonMap();
        PostgraEntityService es = app.beginEntityService();
        try {
            String email = requestMap.getString("email");
            String password = requestMap.getString("password");
            Person person = es.findPerson(email);
            if (person != null) {
                throw new PersistenceException("Email already exists: " + email);
            }
            Date registerTime = new Date();
            String totpSecret = Totps.generateSecret();
            String hmacSecret = Hmacs.generateSecret();
            person = new Person(email);
            person.setPassword(password.toCharArray());
            person.setHmacSecret(hmacSecret);
            person.setTotpSecret(totpSecret);
            person.setRegisterTime(registerTime);
            person.setLoginTime(registerTime);
            es.persist(person);
            es.commit();
            responseMap.put("email", email);
            responseMap.put("loginTime", registerTime.getTime());
            responseMap.put("hmacSecret", hmacSecret);
            String token = app.encrypt(responseMap);
            responseMap = app.decrypt(token);
            int totpCode = Totps.getCurrentCode(totpSecret);
            assert Totps.verifyCode(totpSecret, totpCode);
            responseMap.put("totpSecret", totpSecret);
            responseMap.put("totpNow", totpCode);
            responseMap.put("totpQRCodeUrl", Totps.getQRBarcodeURL("postgra." + email, totpSecret));
            responseMap.put("authToken", token);
            responseMap.put("registerTime", registerTime.getTime());
            return responseMap;
        } catch (JMapsException | PersistenceException e) {
            throw new JMapException(responseMap, e.getMessage());
        } finally {
            es.close();
        }
    }
}
