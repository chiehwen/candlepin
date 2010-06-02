/**
 * Copyright (c) 2009 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.fedoraproject.candlepin.audit;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.fedoraproject.candlepin.auth.Principal;
import org.fedoraproject.candlepin.model.Persisted;

/**
 * Event - Base class for Candlepin events.
 *
 * Servers as both our semi-permanent audit history in the database, as well as an
 * integral part of the event queue.
 */
@Entity
@Table(name = "cp_event")
@SequenceGenerator(name = "seq_event", sequenceName = "seq_event", allocationSize = 1)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Event implements Persisted {

    private static final long serialVersionUID = 1L;
    
    /**
     * Type - Constant representing the type of this event.
     */
    public enum Type { CREATED, MODIFIED, DELETED };

    /**
     * Target the type of entity operated on.
     */
    public enum Target { CONSUMER, OWNER, ENTITLEMENT, POOL };
    
    // Uniquely identifies the event:
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_event")
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Target target;

    // String representation of the principal. We probably should not be reconstructing
    // any stored principal object.
    @Column(nullable = false)
    private String principal;

    @Column(nullable = false)
    private Date timestamp;

    // Uniquely identifies the entity's ID when combined with the event type.
    @Column(nullable = false)
    private Long entityId;
    
    @Column(nullable = false)
    private Long ownerId;

    // Both old/new may be null for creation/deletion events. These are marked
    // Transient as we decided we do not necessarily want to store the object state
    // in our Events table. The Event passing through the message queue will still
    // carry them.
    @Transient
    private String oldEntity;
    @Transient
    private String newEntity;

    public Event() {
    }

    public Event(Type type, Target target, Principal principal,
        Long ownerId, Long entityId, String oldEntity, String newEntity) {
        this.type = type;
        this.target = target;

        // TODO: toString good enough? Need something better?
        this.principal = principal.toString();
        this.ownerId = ownerId;
        
        this.entityId = entityId;
        this.oldEntity = oldEntity;
        this.newEntity = newEntity;

        // Set the timestamp to the current date and time.
        this.timestamp = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }
    
    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    @XmlTransient
    public String getOldEntity() {
        return oldEntity;
    }
    public void setOldEntity(String oldEntity) {
        this.oldEntity = oldEntity;
    }

    @XmlTransient
    public String getNewEntity() {
        return newEntity;
    }
    public void setNewEntity(String newEntity) {
        this.newEntity = newEntity;
    }

    public String toString() {
        return "Event [" + "id=" + getId() + ", type=" + getType() + ", time=" +
            getTimestamp() + ", entity=" + getEntityId() + "]";
    }
}