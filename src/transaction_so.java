public class transaction_so {
    private String so_type, so_number, item_so, po_number, item_po;
    private int id;
    private String code_container, seal_id;
    private boolean is_b2b;
    private double qty_net;
    private boolean is_success;
    private String remarks;

    public transaction_so(String so_type, String so_number, String item_so, String po_number, String item_po) {
        this.so_type = so_type;
        this.so_number = so_number;
        this.item_so = item_so;
        this.po_number = po_number;
        this.item_po = item_po;
    }

    public transaction_so(int id, String so_type, String so_number, String item_so, String po_number, String item_po, String code_container, String seal_id, boolean is_b2b, double qty_net) {
        this.so_type = so_type;
        this.so_number = so_number;
        this.item_so = item_so;
        this.po_number = po_number;
        this.item_po = item_po;
        this.id = id;
        this.code_container = code_container;
        this.seal_id = seal_id;
        this.is_b2b = is_b2b;
        this.qty_net = qty_net;
    }

    public void setIsSuccess(boolean is_success) {
        this.is_success = is_success;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getSOType() {
        return this.so_type;
    }
    public String getSONumber() {
        return this.so_number;
    }
    public String getItemSO() {
        return this.item_so;
    }
    public String getPONumber() {
        return this.po_number;
    }
    public String getItemPO() {
        return this.item_po;
    }
    public int getId() {
        return this.id;
    }
    public String getCodeContainer() {
        return this.code_container;
    }
    public String getSealId() {
        return this.seal_id;
    }
    public boolean getIsB2B() {
        return this.is_b2b;
    }
    public String getIsB2BString() {
        if (getIsB2B()){
            return "X";
        }
        return "";
    }
    public double getQtyNet() {
        return this.qty_net;
    }
    public boolean getIsSuccess() {
        return this.is_success;
    }
    public String getRemarks() {
        return this.remarks;
    }
}
