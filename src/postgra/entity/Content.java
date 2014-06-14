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
package postgra.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import vellum.entity.ComparableEntity;
import vellum.jx.JMap;
import vellum.type.Enabled;

/**
 *
 * @author evan.summers
 */
@Entity
public class Content extends ComparableEntity implements Enabled, Serializable {

    @Id
    @Column(name = "path_", length = 128)
    String path;
    
    @Column()    
    String label;

    @Column(name = "created")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date created;

    @Column(name = "modified")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date modified;
    
    @Column()    
    boolean enabled = false;

    @Column(name = "external_")
    boolean external = false;
    
    @Column(name = "type", length = 32)
    String contentType;
    
    @Lob
    @Column(name = "content", length = 100000)
    private byte[] content;

    public Content() {
    }

    public Content(String path) {
        this.path = path;
    }
    
    @Override
    public Comparable getId() {
        return path;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }
            
    public JMap getMap() {
        JMap map = new JMap();
        map.put("url", path);
        map.put("label", label);
        map.put("enabled", enabled);
        return map;
    }

    @Override
    public String toString() {
        return getMap().toJson();
    }
}
