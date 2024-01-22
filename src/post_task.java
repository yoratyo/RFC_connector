import com.sap.conn.jco.*;

import java.util.ArrayList;
import java.util.TimerTask;

public class post_task extends TimerTask {
    private JCoDestination destination;
    private JCoFunction function;
    private dao model;

    public post_task(JCoDestination destination, JCoFunction function, dao model) {
        this.destination = destination;
        this.function = function;
        this.model = model;
    }

    public void run() {
        ArrayList<transaction_so> list_so = model.getNeedToPushSO();
        if (list_so.size() == 0) {
            System.out.println(">>> No SO to push");
            return;
        }

        System.out.println(function.getImportParameterList().getMetaData());
        JCoTable tableImport = function.getImportParameterList().getTable("IM_DETSO");
        System.out.println(tableImport.getMetaData());
        tableImport.deleteAllRows();

        for (transaction_so so: list_so) {
            tableImport.appendRow();
            tableImport.setValue("SALES_DOC", so.getSONumber());
            tableImport.setValue("ITEM_SALES_DOC", so.getItemSO());
            tableImport.setValue("PURCHASE_DOC", so.getPONumber());
            tableImport.setValue("ITEM_PURCHASE", so.getItemPO());
            tableImport.setValue("CODE_CONTAINER", so.getCodeContainer());
            tableImport.setValue("SEAL_ID", so.getSealId());
            tableImport.setValue("B2B", so.getIsB2BString());
            tableImport.setValue("QTY_NET", so.getQtyNet());
        }

        try {
            function.execute(destination);
        } catch (AbapException e) {
            e.printStackTrace();
            return;
        } catch (JCoException ex) {
            ex.printStackTrace();
            return;
        }

        System.out.println(function.getExportParameterList().getMetaData());
        JCoTable tableExport = function.getExportParameterList().getTable("EX_RET");
        System.out.println(tableExport.getMetaData());
        System.out.println("Rows : " + tableExport.getNumRows());
        System.out.println(tableExport);

        for (int i = 0; i < tableExport.getNumRows(); i++) {
            transaction_so match_so = null;

            searchSO:
            for (transaction_so so: list_so) {
                if (tableExport.getString("SALES_DOC").equalsIgnoreCase(so.getSONumber()) &&
                        tableExport.getString("ITEM_SALES_DOC").equalsIgnoreCase(so.getItemSO()) &&
                        tableExport.getString("PURCHASE_DOC").equalsIgnoreCase(so.getPONumber()) &&
                        tableExport.getString("ITEM_PURCHASE").equalsIgnoreCase(so.getItemPO()) &&
                        tableExport.getString("CODE_CONTAINER").equalsIgnoreCase(so.getCodeContainer()) &&
                        tableExport.getString("SEAL_ID").equalsIgnoreCase(so.getSealId()) &&
                        tableExport.getString("B2B").equalsIgnoreCase(so.getIsB2BString())){
                    match_so = so;
                    break searchSO;
                }
            }

            if (match_so != null) {
                match_so.setIsSuccess(tableExport.getString("STATUS").equalsIgnoreCase("S"));
                match_so.setRemarks(tableExport.getString("REMARKS"));
            }

            tableExport.nextRow();
        }

        model.updateTransactionSO(list_so);
        model.insertPostSOLogs(list_so);
    }
}
