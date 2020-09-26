package org.nesc.ec.bigdata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Truman.P.Du
 * @date 2020/08/12
 * @description
 */
@Configuration
public class AlertaConfig {
    @Value("${alert.enable:false}")
    private boolean alterEnable;
    @Value("${alert.threshold:''}")
    private String alertThreshold;

    @Value("${alert.dispause:''}")
    private String alertDispause;
    @Value("${alert.service:''}")
    private String alertServiceUrl;
    @Value("${alter.env:''}")
    private String alterEvn;
    @Value("${alert.alarm.group.api}")
    private String alarmGroupApi;

    public boolean isAlterEnable() {
        return alterEnable;
    }

    public void setAlterEnable(boolean alterEnable) {
        this.alterEnable = alterEnable;
    }

    public String getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(String alertThreshold) {
        this.alertThreshold = alertThreshold;
    }


    public String getAlertDispause() {
        return alertDispause;
    }

    public void setAlertDispause(String alertDispause) {
        this.alertDispause = alertDispause;
    }

    public String getAlertServiceUrl() {
        return alertServiceUrl;
    }

    public void setAlertServiceUrl(String alertServiceUrl) {
        this.alertServiceUrl = alertServiceUrl;
    }

    public String getAlterEvn() {
        return alterEvn;
    }

    public void setAlterEvn(String alterEvn) {
        this.alterEvn = alterEvn;
    }

    public String getAlarmGroupApi() {
        return alarmGroupApi;
    }
}
