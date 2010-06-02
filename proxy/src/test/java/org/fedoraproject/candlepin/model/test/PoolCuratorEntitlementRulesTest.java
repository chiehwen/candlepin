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
package org.fedoraproject.candlepin.model.test;

import static org.junit.Assert.*;

import java.util.List;

import org.fedoraproject.candlepin.controller.Entitler;
import org.fedoraproject.candlepin.model.Attribute;
import org.fedoraproject.candlepin.model.Consumer;
import org.fedoraproject.candlepin.model.Entitlement;
import org.fedoraproject.candlepin.model.Owner;
import org.fedoraproject.candlepin.model.Pool;
import org.fedoraproject.candlepin.model.Product;
import org.fedoraproject.candlepin.policy.Enforcer;
import org.fedoraproject.candlepin.policy.EntitlementRefusedException;
import org.fedoraproject.candlepin.policy.js.JavascriptEnforcer;
import org.fedoraproject.candlepin.test.DatabaseTestFixture;
import org.fedoraproject.candlepin.test.TestUtil;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * PoolCuratorEntitlementRulesTest
 */
public class PoolCuratorEntitlementRulesTest extends DatabaseTestFixture {
    private Owner owner;
    private Product product;
    private Consumer consumer;

    private static final String CPU_LIMITED_PRODUCT = "CPULIMITED001";

    @Before
    public void setUp() {
        owner = createOwner();
        ownerCurator.create(owner);

        product = TestUtil.createProduct();
        productCurator.create(product);

        consumer = TestUtil.createConsumer(owner);
        consumer.setMetadataField("cpu_cores", "4");
        consumerTypeCurator.create(consumer.getType());
        consumerCurator.create(consumer);
    }

    @Test
    public void testLookupRuleFiltering() {

        Product p = new Product(CPU_LIMITED_PRODUCT, CPU_LIMITED_PRODUCT);
        p.addAttribute(new Attribute(CPU_LIMITED_PRODUCT, ""));
        productCurator.create(p);

        Pool pool = createPoolAndSub(owner, p.getId(), new Long(100),
            TestUtil.createDate(2000, 3, 2), TestUtil.createDate(2050, 3, 2));
        poolCurator.create(pool);

        List<Pool> results =
            poolCurator.listAvailableEntitlementPools(consumer);
        assertEquals(0, results.size());
    }
    
    @Test
    public void concurrentCreationOfEntitlementsShouldWorkIfUnderMaxMemberLimit()
        throws Exception {
        Long numAvailEntitlements = new Long(2);

        Product newProduct = TestUtil.createProduct();
        productCurator.create(newProduct);

        Pool consumerPool = createPoolAndSub(owner, newProduct
                .getId(), numAvailEntitlements, TestUtil
                .createDate(2009, 11, 30), TestUtil.createDate(2050, 11, 30));
        consumerPool = poolCurator.create(consumerPool);

        Entitler anotherEntitler = injector.getInstance(Entitler.class);

        entitler.entitle(consumer, newProduct, new Integer("1"));
        anotherEntitler.entitle(consumer, newProduct, new Integer("1"));

        assertFalse(poolCurator.find(consumerPool.getId())
                .entitlementsAvailable(new Integer(1)));
    }

    @Test(expected = EntitlementRefusedException.class)
    public void concurrentCreationOfEntitlementsShouldFailIfOverMaxMemberLimit()
        throws Exception {
        Long numAvailEntitlements = new Long(1);

        Product newProduct = TestUtil.createProduct();
        productCurator.create(newProduct);

        Pool consumerPool = createPoolAndSub(owner, newProduct
                .getId(), numAvailEntitlements, TestUtil
                .createDate(2009, 11, 30), TestUtil.createDate(2050, 11, 30));
        consumerPool = poolCurator.create(consumerPool);

        Entitler anotherEntitler = injector.getInstance(Entitler.class);

        Entitlement e1 = entitler.entitle(consumer, newProduct, new Integer("1"));
        assertNotNull(e1);

        @SuppressWarnings("unused")
        Entitlement e2 = anotherEntitler.entitle(consumer, newProduct, new Integer("1"));
    }

    @Override
    protected Module getGuiceOverrideModule() {
        return new AbstractModule() {
            
            @Override
            protected void configure() {
                bind(Enforcer.class).to(JavascriptEnforcer.class);
            }
        };
    }
}