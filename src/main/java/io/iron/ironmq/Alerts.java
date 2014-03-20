package io.iron.ironmq;

import java.util.ArrayList;

public class Alerts {
    private ArrayList<Alert> alerts;

    public Alerts(ArrayList<Alert> alerts) {
        this.alerts = alerts;
    }

    public Alert getAlert(int i) {
        return alerts.get(i);
    }

    public ArrayList<Alert> getAlerts() {
        return alerts;
    }
}
