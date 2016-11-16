package com.dianping.pigeon.monitor;

import com.dianping.pigeon.util.MapUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/11/03  下午9:07.
 */
public class ApplicationStatisData extends AbstractStatisData implements ProviderChainable, InvokerStatisable, ProviderStatisable {

    private long startMillis = System.currentTimeMillis();

    private final String appName;

    private static transient final ThreadLocal<MethodKey> methodKeys = new ThreadLocal<MethodKey>();

    private final ConcurrentMap<MethodKey, ProviderStatisData> statisDatas = new ConcurrentHashMap<MethodKey, ProviderStatisData>();

    private final ConcurrentMap<MethodKey, InvokerMonitorData> invokerDatas = new ConcurrentHashMap<MethodKey, InvokerMonitorData>();

    public ApplicationStatisData(String appName) {
        this.appName = appName;
    }


    @Override
    public void startProvider(MethodKey methodKey) {
        methodKeys.set(methodKey);
    }

    @Override
    public void completeProvider(MethodKey methodKey) {
        methodKeys.remove();
    }

    @Override
    public void startInvoker(MethodKey methodKey) {
        MethodKey sourceKey = methodKeys.get();

        if (sourceKey != null) {
            ProviderStatisData data = MapUtils.getOrCreate(statisDatas, methodKey, ProviderStatisData.class);
            data.startInvoker(methodKey);
        } else {
            super.startInvoker(methodKey);
        }
    }

    @Override
    public void completeInvoker(MethodKey methodKey) {
        MethodKey sourceKey = methodKeys.get();

        if (sourceKey != null) {
            ProviderStatisData data = MapUtils.getOrCreate(statisDatas, methodKey, ProviderStatisData.class);
            data.completeInvoker(methodKey);
        } else {
            super.completeInvoker(methodKey);
        }

    }

    @Override
    public void addProviderData(MethodKey methodKey, String appName, byte callType, byte serialize, int timeout) {
        ProviderStatisData data = MapUtils.getOrCreate(statisDatas, methodKey, ProviderStatisData.class);
        data.addProviderData(methodKey, appName, callType, serialize, timeout);
    }

    @Override
    public void updateProviderData(MethodKey methodKey, String appName, long elapsed, boolean isSuccess) {
        ProviderStatisData data = MapUtils.getOrCreate(statisDatas, methodKey, ProviderStatisData.class);
        data.updateProviderData(methodKey, appName, elapsed, isSuccess);
    }

    @Override
    public void updateProviderTotalCount(MethodKey methodKey, String appName) {
        ProviderStatisData data = MapUtils.getOrCreate(statisDatas, methodKey, ProviderStatisData.class);
        data.updateProviderTotalCount(methodKey, appName);
    }

    @Override
    public void addInvokerData(MethodKey methodKey, byte callMethod, byte serialize, int timeout) {
        InvokerMonitorData data = MapUtils.getOrCreate(invokerDatas, methodKey, InvokerMonitorData.class);
        data.setCallMethod(callMethod);
        data.setSerialize(serialize);
        data.setTimeout(timeout);
    }

    @Override
    public void updateInvokerTotalCount(MethodKey methodKey) {
        InvokerMonitorData data = MapUtils.getOrCreate(invokerDatas, methodKey, InvokerMonitorData.class);
        data.incTotalCount();
    }

    @Override
    public void updateInvokerData(MethodKey methodKey, long elapsed, boolean isSuccess) {
        InvokerMonitorData data = MapUtils.getOrCreate(invokerDatas, methodKey, InvokerMonitorData.class);
        data.setElapsed(elapsed);
        if (isSuccess) {
            data.incTotalSuccess();
        } else {
            data.incTotalFailed();
        }
    }

    public String getAppName() {
        return appName;
    }

    public ConcurrentMap<MethodKey, ProviderStatisData> getStatisDatas() {
        return statisDatas;
    }

    public ConcurrentMap<MethodKey, InvokerMonitorData> getInvokerDatas() {
        return invokerDatas;
    }

    public long getStartMillis() {
        return startMillis;
    }
}
