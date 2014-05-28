/*
 * Source https://github.com/evanx by @evanxsummers

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
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.storage.StorageException;
import vellum.storage.StorageExceptionType;

/**
 *
 * @author evan.summers
 */
public class PostgraEntityService implements AutoCloseable {

    static Logger logger = LoggerFactory.getLogger(PostgraEntityService.class);

    PostgraApp app;
    EntityManagerFactory emf;
    EntityManager em;

    public PostgraEntityService(PostgraApp app) {
        this.app = app;
    }

    public PostgraEntityService(PostgraApp app, EntityManagerFactory emf) {
        this.app = app;
        this.emf = emf;
    }

    public void begin() {
        if (em != null && em.isOpen()) {
            em.close();
            throw new PersistenceException("entity manager is open");
        }
        if (emf == null) {
            emf = app.emf;
        }
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    public void commit() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    public void rollback() {
        if (em != null) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
    }

    @Override
    public void close() {
        if (em != null) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void persist(Object entity) {
        em.persist(entity);
    }

    public Person findPerson(String email) throws StorageException {
        List<Person> list = list(email);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new StorageException(StorageExceptionType.MULTIPLE_FOUND, Person.class, email);
        }
        return list.get(0);
    }

    public void remove(Object entity) throws StorageException {
        em.remove(entity);
    }

    private List<Person> list(String email) {
        return em.createQuery("select p from Person p"
                + " where p.email = :email").
                setParameter("email", email).
                getResultList();
    }

    public Person persistPerson(String email) throws StorageException {
        logger.info("persistPerson {}", email);
        Person person = em.find(Person.class, email);
        if (person == null) {
            person = new Person(email);
            em.persist(person);
            logger.info("persistPerson {} {}", email, person);
        }
        return person;
    }

}
