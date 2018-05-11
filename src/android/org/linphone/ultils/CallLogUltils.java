package org.linphone.ultils;

import android.content.Context;

import org.linphone.CallActivity;
import org.linphone.CallIncomingActivity;
import org.linphone.CallOutgoingActivity;
import org.linphone.MyCallLogs;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallLog;
import org.linphone.database.DbContext;

import java.util.ArrayList;

/**
 * Created by QuocVietDang1 on 5/10/2018.
 */

public class CallLogUltils {
    public static final CallLogUltils instance = new CallLogUltils();

    public void addOutGoingLog(LinphoneCall linphoneCall, int status, Context context) {
        LinphoneCallLog linphoneCallLog = linphoneCall.getCallLog();
        ArrayList<MyCallLogs.CallLog> currentCallLogs = DbContext.getInstance().getMyCallLogs(context).getCallLogs();

        int id = 0;
        if (currentCallLogs.size() > 0)
            id = currentCallLogs.get(0).getId() + 1;


        MyCallLogs.CallLog callLog = new MyCallLogs.CallLog(
                id,
                ContactUltils.instance.getContactName(linphoneCallLog.getTo().getUserName(), context),
                linphoneCallLog.getTo().getUserName(),
                linphoneCallLog.getTimestamp(),
                linphoneCallLog.getCallDuration(),
                status);
        MyCallLogs myCallLogs = DbContext.getInstance().getMyCallLogs(context);
        ArrayList<MyCallLogs.CallLog> callLogs = myCallLogs.getCallLogs();
        callLogs.add(0, callLog);
        myCallLogs.setCallLogs(callLogs);
        DbContext.getInstance().setMyCallLogs(myCallLogs, context);
    }

    public void addIncomingLog(LinphoneCall linphoneCall, int status, Context context) {
        LinphoneCallLog linphoneCallLog = linphoneCall.getCallLog();
        ArrayList<MyCallLogs.CallLog> currentCallLogs = DbContext.getInstance().getMyCallLogs(context).getCallLogs();

        int id = 0;
        if (currentCallLogs.size() > 0)
            id = currentCallLogs.get(0).getId() + 1;
        MyCallLogs.CallLog callLog = new MyCallLogs.CallLog(
                id,
                ContactUltils.instance.getContactName(linphoneCallLog.getFrom().getUserName(), context),
                linphoneCallLog.getFrom().getUserName(),
                linphoneCallLog.getTimestamp(),
                linphoneCallLog.getCallDuration(),
                status);
        MyCallLogs myCallLogs = DbContext.getInstance().getMyCallLogs(context);
        ArrayList<MyCallLogs.CallLog> callLogs = myCallLogs.getCallLogs();
        callLogs.add(0, callLog);
        myCallLogs.setCallLogs(callLogs);
        DbContext.getInstance().setMyCallLogs(myCallLogs, context);
    }

    public void addLog(LinphoneCall linphoneCall, int status, Context context) {
        LinphoneCallLog linphoneCallLog = linphoneCall.getCallLog();
        ArrayList<MyCallLogs.CallLog> currentCallLogs = DbContext.getInstance().getMyCallLogs(context).getCallLogs();

        int id = 0;
        if (currentCallLogs.size() > 0)
            id = currentCallLogs.get(0).getId() + 1;
        String userName = "";
        if (status == MyCallLogs.CallLog.CUOC_GOI_DI) {
            userName = linphoneCallLog.getTo().getUserName();
        } else {
            userName = linphoneCallLog.getFrom().getUserName();
        }
        MyCallLogs.CallLog callLog = new MyCallLogs.CallLog(
                id,
                ContactUltils.instance.getContactName(userName, context),
                userName,
                linphoneCallLog.getTimestamp(),
                linphoneCallLog.getCallDuration(),
                status);
        MyCallLogs myCallLogs = DbContext.getInstance().getMyCallLogs(context);
        ArrayList<MyCallLogs.CallLog> callLogs = myCallLogs.getCallLogs();
        callLogs.add(0, callLog);
        myCallLogs.setCallLogs(callLogs);
        DbContext.getInstance().setMyCallLogs(myCallLogs, context);
    }
}
