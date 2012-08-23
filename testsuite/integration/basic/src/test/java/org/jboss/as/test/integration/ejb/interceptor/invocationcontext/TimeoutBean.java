/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.integration.ejb.interceptor.invocationcontext;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.interceptor.AroundTimeout;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Ondrej Chaloupka
 */
@Stateless
@Interceptors({ClassInterceptor.class})
public class TimeoutBean {
    
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static boolean timerServiceCalled = false;
    public static String interceptorResults = "";

    @Resource
    private TimerService timerService;
    
    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    public void createTimer() {
        timerService.createTimer(100, null);
    }

    @AroundTimeout
    public Object aroundTimeoutParent(final InvocationContext ctx) throws Exception {
        String ret = InvocationContextChecker.checkTimeoutInterceptorContext(ctx, "Method", "Bean");
        TimeoutBean.interceptorResults += ret;
        return ctx.proceed();
    }

    @Timeout
    @Interceptors(MethodInterceptor.class)
    private void timeout(Timer timer) {
        timerServiceCalled = true;
        interceptorResults += "@Timeout";
        latch.countDown();
    }

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    public static boolean awaitTimerCall() {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return timerServiceCalled;
    }
}
