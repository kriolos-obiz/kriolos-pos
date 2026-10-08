/*
 * Copyright (C) 2026 KriolOS POS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.forms;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BeanContainer Thread-Safe IoC Test Suite")
class BeanContainerTest {

    private AppView appViewProxy;

    @BeforeEach
    void setUp() {
        BeanContainer.cleanAll();
        appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );
    }

    @AfterEach
    void tearDown() {
        BeanContainer.cleanAll();
    }

    @Test
    @DisplayName("Should resolve bean cleanly using class literal")
    void shouldResolveBeanByClass() {
        ResourceService service = BeanContainer.getBean(ResourceService.class, appViewProxy);
        assertNotNull(service, "ResourceService should be resolved");
        assertInstanceOf(DataLogicSystem.class, service, "ResourceService should map to DataLogicSystem");
    }

    @Test
    @DisplayName("Should resolve bean using string key and class casting")
    void shouldResolveBeanByStringKeyAndClass() {
        ResourceService service = BeanContainer.getBean(
                "com.openbravo.pos.forms.ResourceService",
                ResourceService.class,
                appViewProxy
        );
        assertNotNull(service, "ResourceService should be resolved via string key");
        assertInstanceOf(DataLogicSystem.class, service);
    }

    @Test
    @DisplayName("Should resolve bean using raw string key")
    void shouldResolveBeanByStringKey() {
        Object bean = BeanContainer.getBean("com.openbravo.pos.forms.SystemService", appViewProxy);
        assertNotNull(bean, "SystemService bean should be resolved");
        assertInstanceOf(SystemService.class, bean);
    }

    @Test
    @DisplayName("Should cache bean instances across calls")
    void shouldCacheBeanInstances() {
        ResourceService first = BeanContainer.getBean(ResourceService.class, appViewProxy);
        ResourceService second = BeanContainer.getBean(ResourceService.class, appViewProxy);

        assertSame(first, second, "Subsequent resolutions should return cached instance");
    }

    @Test
    @DisplayName("Should evict all cached beans when cleanAll is invoked")
    void shouldEvictCacheOnCleanAll() {
        ResourceService beforeClean = BeanContainer.getBean(ResourceService.class, appViewProxy);
        assertNotNull(beforeClean);

        BeanContainer.cleanAll();

        ResourceService afterClean = BeanContainer.getBean(ResourceService.class, appViewProxy);
        assertNotNull(afterClean);
        assertNotSame(beforeClean, afterClean, "Instance after cleanAll should be a new instantiation");
    }

    @Test
    @DisplayName("Should throw NullPointerException when beanClass is null")
    void shouldThrowNpeOnNullClass() {
        assertThrows(NullPointerException.class, () -> BeanContainer.getBean((Class<?>) null, appViewProxy));
    }

    @Test
    @DisplayName("Should throw NullPointerException when bean key string is null")
    void shouldThrowNpeOnNullKey() {
        assertThrows(NullPointerException.class, () -> BeanContainer.getBean((String) null, appViewProxy));
    }

    @Test
    @DisplayName("Should return null when class cast fails")
    void shouldReturnNullOnClassCastMismatch() {
        // DataLogicSystem is not an Integer
        Integer result = BeanContainer.getBean("com.openbravo.pos.forms.ResourceService", Integer.class, appViewProxy);
        assertNull(result, "Incompatible type cast should safely return null and log warning");
    }

    @Test
    @DisplayName("Should throw BeanFactoryException when class cannot be found")
    void shouldThrowBeanFactoryExceptionOnMissingClass() {
        assertThrows(BeanFactoryException.class, () ->
                BeanContainer.getBean("com.openbravo.nonexistent.NoSuchBean", appViewProxy)
        );
    }

    @Test
    @DisplayName("Private constructor should throw UnsupportedOperationException")
    void shouldPreventInstantiationOfUtilityClass() throws NoSuchMethodException {
        Constructor<BeanContainer> constructor = BeanContainer.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, thrown.getCause());
    }

    @Test
    @DisplayName("Should safely handle concurrent bean resolutions across multiple threads")
    void shouldSupportConcurrentResolutions() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ResourceService service = BeanContainer.getBean(ResourceService.class, appViewProxy);
                    if (service != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "All threads should complete within timeout");
        assertEquals(threadCount, successCount.get(), "All concurrent resolutions must succeed");
    }
}
