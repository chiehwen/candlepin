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
package org.fedoraproject.candlepin.model;

import org.fedoraproject.candlepin.auth.Access;
import org.hibernate.criterion.Restrictions;

/**
 * PermissionCurator
 */
public class PermissionCurator extends AbstractHibernateCurator<OwnerPermission> {
    
    protected PermissionCurator() {
        super(OwnerPermission.class);
    }

    /**
     * Return the permission for this owner and verb if it exists, create it otherwise.
     * @param owner Owner
     * @param verb Verb
     * @return Permission object.
     */
    public OwnerPermission findOrCreate(Owner owner, Access verb) {
        OwnerPermission p = findByOwnerAndVerb(owner, verb);
        if (p == null) {
            p = new OwnerPermission(owner, verb);
            create(p);
        }
        return p;
    }

    public OwnerPermission findByOwnerAndVerb(Owner owner, Access verb) {
        return (OwnerPermission) currentSession().createCriteria(OwnerPermission.class)
            .add(Restrictions.eq("owner", owner))
            .add(Restrictions.eq("verb", verb)).uniqueResult();
    }
}
