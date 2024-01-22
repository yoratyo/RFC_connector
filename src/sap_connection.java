import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.util.HashMap;
import java.util.Properties;

public class sap_connection {
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

    private JCoDestination destination;
    private JCoFunction get_function;
    private JCoFunction post_function;

    public sap_connection(String destination_name, Properties properties, String get_function_name, String post_function_name) {
        //register the provider with the JCo environment;
        //catch IllegalStateException if an instance is already registered
        try {
            if (!com.sap.conn.jco.ext.Environment.isDestinationDataProviderRegistered()) {
                connect_sap_example.MyDestinationDataProvider myProvider = new connect_sap_example.MyDestinationDataProvider();
                com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(myProvider);
                myProvider.changeProperties(destination_name, properties);
            }
        } catch (IllegalStateException providerAlreadyRegisteredException) {
            //somebody else registered its implementation,
            //stop the execution
            throw new Error(providerAlreadyRegisteredException);
        }

        //set properties for the destination and ...
        try{
            destination = JCoDestinationManager.getDestination(destination_name);
            destination.ping();

            get_function = destination.getRepository().getFunction(get_function_name);
            if (get_function == null) {
                throw new RuntimeException("BAPI_COMPANYCODE_GETLIST " + get_function_name + " not found in SAP.");
            }

            post_function = destination.getRepository().getFunction(post_function_name);
            if (post_function_name == null) {
                throw new RuntimeException("BAPI_COMPANYCODE_GETLIST " + post_function_name + " not found in SAP.");
            }

        } catch (JCoException e) {
            e.printStackTrace();
            System.out.println("Execution on destination " + destination_name + " failed");
        }
    }

    public JCoDestination getDestination() {
        return this.destination;
    }

    public JCoFunction getFunctionGet() {
        return this.get_function;
    }

    public JCoFunction getFunctionPost() {
        return this.post_function;
    }
}
