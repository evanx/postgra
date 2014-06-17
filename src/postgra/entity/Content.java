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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Entity(name = "content")
public class Content extends ComparableEntity implements Enabled, Serializable {

    @Id    
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "content_id")
    Long id;
    
    @Column(name = "path_", length = 128, unique = true)
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
    
    @Column(name = "content_type", length = 32)
    String contentType;

    @Column(name = "content_length")
    int contentLength;

    @Column(name = "cache_secs")
    int cacheSeconds = 300;
    
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
        return id;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }
    
    public void setContent(byte[] content) {
        this.content = content;
        contentLength = content.length; 
    }

    public byte[] getContent() {
        return content;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getCreated() {
        return created;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getModified() {
        return modified;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setCacheSeconds(int cacheSeconds) {
        this.cacheSeconds = cacheSeconds;
    }

    public int getCacheSeconds() {
        return cacheSeconds;
    }
    
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
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
        return getClass().getName();
    }
}
