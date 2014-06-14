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
package postgra.app;

import postgra.entity.Person;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.entity.Content;

/**
 *
 * @author evan.summers
 */
public class PostgraEntityService implements AutoCloseable {

    static Logger logger = LoggerFactory.getLogger(PostgraEntityService.class);

    EntityManager em;

    public PostgraEntityService(EntityManager em) {
        this.em = em;
    }

    public void begin() {
        em.getTransaction().begin();
    }

    public void commit() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    public void rollback() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    @Override
    public void close() {
        if (em.isOpen()) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public void merge(Object entity) {
        em.merge(entity);
    }
    
    public void persist(Object entity) {
        em.persist(entity);
    }

    public void remove(Object entity) {
        em.remove(entity);
    }

    public <T> T find(Class type, Object id) {
        return (T) em.find(type, id);
    }

    public Content findContent(String path) {
        List<Content> list = listContent(path);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new PersistenceException("Multiple results: " + path);
        }
        return list.get(0);
    }
    
    public Person findPerson(String email) {
        List<Person> list = listPerson(email);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new PersistenceException("Multiple results for email: " + email);
        }
        return list.get(0);
    }

    private List<Person> listPerson(String email) {
        return em.createQuery("select p from person p"
                + " where p.email = :email").
                setParameter("email", email).
                getResultList();
    }
    
    private List<Content> listContent(String path) {
        return em.createQuery("select c from content c"
                + " where c.path = :path").
                setParameter("path", path).
                getResultList();
    }
    
}
