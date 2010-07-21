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
package org.fedoraproject.candlepin.controller;

import java.util.ArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.fedoraproject.candlepin.audit.Event;
import org.fedoraproject.candlepin.audit.EventFactory;
import org.fedoraproject.candlepin.audit.EventSink;
import org.fedoraproject.candlepin.model.ConsumerCurator;
import org.fedoraproject.candlepin.model.Entitlement;
import org.fedoraproject.candlepin.model.EntitlementCertificateCurator;
import org.fedoraproject.candlepin.model.EntitlementCurator;
import org.fedoraproject.candlepin.model.Owner;
import org.fedoraproject.candlepin.model.Pool;
import org.fedoraproject.candlepin.model.PoolCurator;
import org.fedoraproject.candlepin.model.Product;
import org.fedoraproject.candlepin.policy.Enforcer;
import org.fedoraproject.candlepin.policy.js.entitlement.PostEntHelper;
import org.fedoraproject.candlepin.service.EntitlementCertServiceAdapter;
import org.fedoraproject.candlepin.service.ProductServiceAdapter;
import org.fedoraproject.candlepin.service.SubscriptionServiceAdapter;
import org.fedoraproject.candlepin.test.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EntitlerMockTest {

    private Entitler entitler;
    private Owner o;
    private Pool pool;
    private Product product;

    @Mock private PoolCurator poolCuratorMock;
    @Mock private EntitlementCurator entCuratorMock;
    @Mock private ConsumerCurator consumerCuratorMock;
    @Mock private Enforcer enforcerMock;
    @Mock private EntitlementCertServiceAdapter entCertAdapterMock;
    @Mock private SubscriptionServiceAdapter subAdapterMock;
    @Mock private ProductServiceAdapter productAdapterMock;
    @Mock private EventFactory eventFactoryMock;
    @Mock private EventSink sinkMock;
    @Mock private PostEntHelper postEntHelperMock;
    @Mock private EntitlementCertificateCurator certCuratorMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        product = TestUtil.createProduct();
        o = new Owner("key", "displayname");
        pool = TestUtil.createPool(o, product);

        entitler = new Entitler(poolCuratorMock, entCuratorMock, consumerCuratorMock,
            enforcerMock, entCertAdapterMock, subAdapterMock, productAdapterMock, 
            eventFactoryMock, sinkMock, postEntHelperMock, certCuratorMock);

    }

    @Test
    public void testRevokeCleansUpPoolsWithSourceEnt() throws Exception {
        Entitlement e = new Entitlement(pool, TestUtil.createConsumer(o),
            pool.getStartDate(), pool.getEndDate(), 1);
        List<Pool> poolsWithSource = createPoolsWithSourceEntitlement(e, product);
        when(poolCuratorMock.listBySourceEntitlement(e)).thenReturn(poolsWithSource);

        entitler.revokeEntitlement(e);

        verify(entCertAdapterMock).revokeEntitlementCertificates(e);
        verify(entCuratorMock).delete(e);

        for (Pool p : poolsWithSource) {
            verify(poolCuratorMock).delete(p);
        }
    }

    private List<Pool> createPoolsWithSourceEntitlement(Entitlement e, Product p) {
        List<Pool> pools = new LinkedList<Pool>();
        Pool pool1 = TestUtil.createPool(e.getOwner(), p);
        pools.add(pool1);
        Pool pool2 = TestUtil.createPool(e.getOwner(), p);
        pools.add(pool2);
        return pools;
    }

    @Test
    public void testCleanup() throws Exception {
        Pool p = createPoolWithEntitlements();

        when(poolCuratorMock.entitlementsIn(p)).thenReturn(
                new ArrayList<Entitlement>(p.getEntitlements()));

        entitler.deletePool(p);

        // And the pool should be deleted:
        verify(poolCuratorMock).delete(p);

        // Check that appropriate events were sent out:
        verify(eventFactoryMock).poolDeleted(p);
        verify(sinkMock, times(3)).sendEvent((Event) any());
    }

    private Pool createPoolWithEntitlements() {
        Pool newPool = TestUtil.createPool(o, product);
        Entitlement e1 = new Entitlement(newPool, TestUtil.createConsumer(o),
            newPool.getStartDate(), newPool.getEndDate(), 1);
        Entitlement e2 = new Entitlement(newPool, TestUtil.createConsumer(o),
            newPool.getStartDate(), newPool.getEndDate(), 1);
        newPool.getEntitlements().add(e1);
        newPool.getEntitlements().add(e2);
        return newPool;
    }

}