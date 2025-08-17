package ru.dmitartur.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "core")
public class CoreProperties {
    private String serviceName = "unknown";
    private String orderServiceAddress = "localhost:7068";
    private String ozonServiceAddress = "localhost:7097";

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getOrderServiceAddress() { return orderServiceAddress; }
    public void setOrderServiceAddress(String orderServiceAddress) { this.orderServiceAddress = orderServiceAddress; }

    public String getOzonServiceAddress() { return ozonServiceAddress; }
    public void setOzonServiceAddress(String ozonServiceAddress) { this.ozonServiceAddress = ozonServiceAddress; }
}


