import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.util.HashMap;
import java.util.Properties;

public class connect_sap_example {

    static class MyDestinationDataProvider implements DestinationDataProvider {

        private DestinationDataEventListener eL;
        private HashMap<String, Properties> secureDBStorage = new HashMap<String, Properties>();

        public Properties getDestinationProperties(String destinationName) {
            try {
                //read the destination from DB
                Properties p = secureDBStorage.get(destinationName);

                if (p != null) {
                    //check if all is correct, for example
                    if (p.isEmpty()) {
                        throw new DataProviderException(DataProviderException.Reason.INVALID_CONFIGURATION, "destination configuration is incorrect", null);
                    }

                    return p;
                }

                return null;
            } catch (RuntimeException re) {
                throw new DataProviderException(DataProviderException.Reason.INTERNAL_ERROR, re);
            }
        }

        public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
            this.eL = eventListener;
        }

        public boolean supportsEvents() {
            return true;
        }

        //implementation that saves the properties in a very secure way
        void changeProperties(String destName, Properties properties) {
            synchronized (secureDBStorage) {
                if (properties == null) {
                    if (secureDBStorage.remove(destName) != null) {
                        eL.deleted(destName);
                    }
                } else {
                    secureDBStorage.put(destName, properties);
                    eL.updated(destName); // create or updated
                }
            }
        }
    } // end of MyDestinationDataProvider

    JCoDestination destination;
    JCoFunction function;

    static Properties getDestinationPropertiesFromUI() {
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

    void executeGetSO(String destinationName, String functionName) {
        //Execute
        try {
            destination = JCoDestinationManager.getDestination(destinationName);
            destination.ping();
            System.out.println("Destination " + destinationName + " works");
            System.out.println("Dest>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(destination.getRepository().getCachedClassMetaDataNames());
            System.out.println("#>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(destination.getRepository().getCachedFunctionTemplateNames());
            System.out.println("#>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(destination.getRepository().getCachedRecordMetaDataNames());
            System.out.println("#>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(destination.getAttributes());

            function = destination.getRepository().getFunction(functionName);
            if (function == null) {
                throw new RuntimeException("BAPI_COMPANYCODE_GETLIST not found in SAP.");
            }

            //Testing get SO ============================================================
            System.out.println("Func >>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println("Import >>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(function.getImportParameterList().getMetaData());

            // Get by SO
            JCoTable tableImport = function.getImportParameterList().getTable("IM_VBELN");
            System.out.println(tableImport.getMetaData());

            tableImport.appendRow();
            tableImport.setValue("SALES_DOC", "2110000120");
            tableImport.appendRow();
            tableImport.setValue("SALES_DOC", "2210000120");
            tableImport.appendRow();
            tableImport.setValue("SALES_DOC", "2306000210");

//            2110000012
//            2110000013
//            2110000014

            //2110000120
            //2210000120

            //2306000210

            try {
                function.execute(destination);
            } catch (AbapException e) {
                System.out.println(e.toString());
                //System.out.println("ABAP EXCEPTION");
                return;
            } catch (JCoException ex) {
                //Logger.getLogger(RFC_Class.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("JCO EXCEPTION");
            }

            //Get SO =================================================================
            JCoTable get = function.getTableParameterList().getTable("T_DETSO");
            System.out.println(get.getMetaData());
            System.out.println("Rows : " + get.getNumRows());
            System.out.println(get);

            System.out.println(function.getExportParameterList().getMetaData());
            JCoTable export = function.getExportParameterList().getTable(0);
            System.out.println(export.getMetaData());
            System.out.println("Rows : " + export.getNumRows());
            System.out.println(export);

        } catch (JCoException e) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>> E 2");
            e.printStackTrace();
            System.out.println("Execution on destination " + destinationName + " failed");
        }

    }

    void executePostSO(String destinationName, String functionName) {
        //Execute
        try {
            destination = JCoDestinationManager.getDestination(destinationName);
            destination.ping();
            System.out.println("Destination " + destinationName + " works");
            System.out.println("Dest>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(destination.getRepository().getCachedClassMetaDataNames());
            System.out.println("#>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(destination.getRepository().getCachedFunctionTemplateNames());
            System.out.println("#>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(destination.getRepository().getCachedRecordMetaDataNames());
            System.out.println("#>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(destination.getAttributes());

            function = destination.getRepository().getFunction(functionName);
            if (function == null) {
                throw new RuntimeException("BAPI_COMPANYCODE_GETLIST not found in SAP.");
            }

            // Testing post SO ==========================================================
            System.out.println(function.getImportParameterList().getMetaData());
            JCoTable tableImport = function.getImportParameterList().getTable("IM_DETSO");
            System.out.println(tableImport.getMetaData());
//
//            System.out.println(function.getExportParameterList().getMetaData());
//            JCoTable tableExport = function.getExportParameterList().getTable("EX_RET");
//            System.out.println(tableExport.getMetaData());

            tableImport.appendRow();
            tableImport.setValue("SALES_DOC", "2306000210");
            tableImport.setValue("ITEM_SALES_DOC", "000010");
            tableImport.setValue("PURCHASE_DOC", "3600000191");
            tableImport.setValue("ITEM_PURCHASE", "000010");
            tableImport.setValue("CODE_CONTAINER", "ISMU2927909");
            tableImport.setValue("SEAL_ID", "ILS145124");
            tableImport.setValue("B2B", "X");
            tableImport.setValue("QTY_NET", "1000");

            try {
                function.execute(destination);
            } catch (AbapException e) {
                System.out.println(e.toString());
                //System.out.println("ABAP EXCEPTION");
                return;
            } catch (JCoException ex) {
                //Logger.getLogger(RFC_Class.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("JCO EXCEPTION");
            }

            System.out.println(function.getExportParameterList().getMetaData());
            JCoTable tableExport = function.getExportParameterList().getTable("EX_RET");
            System.out.println(tableExport.getMetaData());
            System.out.println("Rows : " + tableExport.getNumRows());
            System.out.println(tableExport);

        } catch (JCoException e) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>> E 2");
            e.printStackTrace();
            System.out.println("Execution on destination " + destinationName + " failed");
        }

    }

    public connect_sap_example(String destination, String function) {

        //register the provider with the JCo environment;
        //catch IllegalStateException if an instance is already registered
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>> 1");
            if (!com.sap.conn.jco.ext.Environment.isDestinationDataProviderRegistered()) {
                MyDestinationDataProvider myProvider = new MyDestinationDataProvider();
                System.out.println(">>>>>>>>>>>>>>>>>>>>> 1 a");
                com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(myProvider);
                System.out.println(">>>>>>>>>>>>>>>>>>>>> 1 b");
                myProvider.changeProperties(destination, getDestinationPropertiesFromUI());
                System.out.println(">>>>>>>>>>>>>>>>>>>>> 1 c");
            }
        } catch (IllegalStateException providerAlreadyRegisteredException) {
            //somebody else registered its implementation,
            //stop the execution
            System.out.println(">>>>>>>>>>>>>>>>>>>>> E 1");
            throw new Error(providerAlreadyRegisteredException);
        }

        //set properties for the destination and ...
        //... work with it
        System.out.println(">>>>>>>>>>>>>>>>>>>>> 2");
        if (function.equalsIgnoreCase("YFMWB_GET_DETAIL_SO")) {
            executeGetSO(destination, function);
        } else {
            executePostSO(destination, function);
        }
    }

    private static int count = 0;
    public static void main(String[] args) throws InterruptedException {
        System.out.println("init..");
        //new connect_sap("ESQ", "YFMWB_SEND_SO_WB");

        // Destination = ESQ
        // DO = ZFM_ID_OUTGOING_PROC_WB
        // PO = ZFM_ID_INCOMING_PROC_PO

        // Destination = ESQ
        // GET SO  = YFMWB_GET_DETAIL_SO
        // POST SO = YFMWB_SEND_SO_WB

        dao model = new dao();
        System.out.println(model.getNeedToSyncSO());


//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
//
//        CountDownClock clock1 = new CountDownClock("A");
////        CountDownClock clock2 = new CountDownClock("B");
////        CountDownClock clock3 = new CountDownClock("C");
//
//        scheduler.scheduleWithFixedDelay(clock1, 0, 2000, TimeUnit.SECONDS);
//        scheduler.scheduleWithFixedDelay(clock2, 3, 15, TimeUnit.SECONDS);
//        scheduler.scheduleWithFixedDelay(clock3, 3, 20, TimeUnit.SECONDS);

    }
}

