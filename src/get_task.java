import com.sap.conn.jco.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class get_task extends TimerTask {
    private JCoDestination destination;
    private JCoFunction function;
    private dao model;

    public get_task(JCoDestination destination, JCoFunction function, dao model){
        this.destination = destination;
        this.function = function;
        this.model = model;
    }

    public void run() {
        ArrayList<String> list_so = model.getNeedToSyncSO();
        if (list_so.size() == 0){
            System.out.println(">>> No SO to sync");
            return;
        }

        System.out.println(">>> Get task is start running");
        JCoTable tableImport = function.getImportParameterList().getTable("IM_VBELN");
        System.out.println(tableImport.getMetaData());
        tableImport.deleteAllRows();

        for (String so : list_so) {
            tableImport.appendRow();
            tableImport.setValue("SALES_DOC", so);
        }

        JCoTable table = function.getTableParameterList().getTable("T_DETSO");
        table.deleteAllRows();

        try {
            function.execute(destination);
        } catch (AbapException e) {
            e.printStackTrace();
            return;
        } catch (JCoException ex) {
            ex.printStackTrace();
            return;
        }

        //Get SO =================================================================
        System.out.println(table.getMetaData());
        System.out.println("Rows : " + table.getNumRows());
        System.out.println(table);

        System.out.println(function.getExportParameterList().getMetaData());
        JCoTable tableExport = function.getExportParameterList().getTable(0);
        System.out.println(tableExport.getMetaData());
        System.out.println("Rows : " + tableExport.getNumRows());
        System.out.println(tableExport);

        Map<String,String> list_failed_so = new HashMap<>();
        for (int i = 0; i < tableExport.getNumRows(); i++) {
            list_failed_so.put(tableExport.getString("VBELN"),tableExport.getString("REMARKS"));
            tableExport.nextRow();
        }

        ArrayList<String> list_success_so = new ArrayList<>();
        for (String so : list_so) {
            if (!list_failed_so.containsKey(so)) {
                list_success_so.add(so);
            }
        }

        ArrayList<transaction_so> transactions = new ArrayList<>();
        for (int i = 0; i < table.getNumRows(); i++) {
            transaction_so so = new transaction_so(
                    table.getString("TYPE"),
                    table.getString("SALES_DOC"),
                    table.getString("ITEM_SALES_DOC"),
                    table.getString("PURCHASE_DOC"),
                    table.getString("ITEM_PURCHASE")
            );
            transactions.add(so);
            table.nextRow();
        }

        model.syncSOSuccess(list_success_so);
        model.syncSOFailed(list_failed_so);
        model.insertTransactionSO(transactions);
    }
}