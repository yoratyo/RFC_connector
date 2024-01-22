import com.sap.conn.jco.ext.DestinationDataProvider;

import java.util.Properties;
import java.util.Timer;

public class properties {
    static String destination = "ESQ";
    static String getFunctionName = "YFMWB_GET_DETAIL_SO";
    static String postFunctionName = "YFMWB_SEND_SO_WB";
    static Properties getDestinationProperties() {
        //adapt parameters in order to configure a valid destination
        Properties connectProperties = new Properties();
        connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "10.88.1.186"); //185 - 186
        connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00");
        connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "280"); //381 - 280
        connectProperties.setProperty(DestinationDataProvider.JCO_USER, "RFCWBRIDGE"); //RFCWBRIDGE - WILLIAMP
        connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "Abc123456"); //123456 - abcd1234
        connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "EN");
        connectProperties.setProperty(DestinationDataProvider.JCO_SAPROUTER, "/H/66.96.249.139/S/3299/H/"); //3299 - 3200
        return connectProperties;
    }

    public static void main(String[] args) {
        sap_connection sap_connection = new sap_connection(destination, getDestinationProperties(), getFunctionName, postFunctionName);
        dao model = new dao();

        Timer timer = new Timer(); // creating timer
        get_task task1 = new get_task(sap_connection.getDestination(), sap_connection.getFunctionGet(), model); // creating get task
        post_task task2 = new post_task(sap_connection.getDestination(), sap_connection.getFunctionPost(), model); // creating get task

        // scheduling the task for repeated fixed-delay execution, beginning after the specified delay
        timer.schedule(task1, 1000, 10000);
        timer.schedule(task2, 5000, 10000);
    }
}
