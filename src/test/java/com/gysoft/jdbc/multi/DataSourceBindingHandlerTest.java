package com.gysoft.jdbc.multi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class DataSourceBindingHandlerTest {

    interface Sample {
        String ok();

        void boom();
    }

    interface CustomSample extends Sample {
        String extra();
    }

    static class SampleImpl implements CustomSample {
        @Override
        public String ok() {
            return "ok";
        }

        @Override
        public void boom() {
            throw new RuntimeException("boom before reaching jdbcTemplate");
        }

        @Override
        public String extra() {
            return "extra";
        }
    }

    @Test
    public void clearsBindingOnNormalReturn() {
        Sample proxy = (Sample) DataSourceBindingHandler.wrap(new SampleImpl(), DataSourceBind.bindKey("k1"));
        assertEquals("ok", proxy.ok());
        assertNull("binding must not leak after a normal call", DataSourceBindHolder.getDataSource());
    }

    @Test
    public void clearsBindingWhenTargetThrowsBeforeConsumption() {
        Sample proxy = (Sample) DataSourceBindingHandler.wrap(new SampleImpl(), DataSourceBind.bindKey("k1"));
        try {
            proxy.boom();
            fail("expected exception to propagate");
        } catch (RuntimeException e) {
            assertEquals("boom before reaching jdbcTemplate", e.getMessage());
        }
        assertNull("binding must not leak when the wrapped call throws", DataSourceBindHolder.getDataSource());
    }

    @Test
    public void sameProxyCanBeReusedAcrossMultipleCalls() {
        Sample proxy = (Sample) DataSourceBindingHandler.wrap(new SampleImpl(), DataSourceBind.bindKey("k1"));
        assertEquals("ok", proxy.ok());
        assertEquals("ok", proxy.ok());
    }

    @Test
    public void proxyDispatchesCustomSubInterfaceMethods() {
        CustomSample proxy = (CustomSample) DataSourceBindingHandler.wrap(new SampleImpl(), DataSourceBind.bindKey("k1"));
        assertEquals("extra", proxy.extra());
    }

    interface ConsumingSample {
        String consume();
    }

    static class ConsumingSampleImpl implements ConsumingSample {
        @Override
        public String consume() {
            // simulates JdbcRoutingDataSource.determineCurrentLookupKey() consuming the binding
            return DataSourceBindHolder.getDataSource();
        }
    }

    @Test
    public void doesNotClobberRestoredOuterBindingAfterInnerConsumption() {
        DataSourceBind outer = new DataSourceBind("outerKey", DataSourceBind.BindType.byAnno, null, null);
        DataSourceBindHolder.setDataSource(outer); // simulates BindPointAspect entering an outer @BindPoint scope
        try {
            ConsumingSample proxy = (ConsumingSample) DataSourceBindingHandler.wrap(
                    new ConsumingSampleImpl(), DataSourceBind.bindKey("innerKey"));
            assertEquals("innerKey", proxy.consume());
            assertEquals("outer binding must still be active for the rest of the enclosing @BindPoint method",
                    "outerKey", DataSourceBindHolder.getDataSource());
        } finally {
            DataSourceBindHolder.clearDataSource();
        }
    }

    @Test
    public void restoresOuterBindingWhenInnerCallThrowsBeforeConsumption() {
        DataSourceBind outer = new DataSourceBind("outerKey", DataSourceBind.BindType.byAnno, null, null);
        DataSourceBindHolder.setDataSource(outer); // simulates BindPointAspect entering an outer @BindPoint scope
        try {
            Sample proxy = (Sample) DataSourceBindingHandler.wrap(new SampleImpl(), DataSourceBind.bindKey("innerKey"));
            try {
                proxy.boom();
                fail("expected exception to propagate");
            } catch (RuntimeException e) {
                assertEquals("boom before reaching jdbcTemplate", e.getMessage());
            }
            assertEquals("outer binding must be restored even when the inner call never reached jdbcTemplate",
                    "outerKey", DataSourceBindHolder.getDataSource());
        } finally {
            DataSourceBindHolder.clearDataSource();
        }
    }
}
