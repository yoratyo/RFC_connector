import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

public class dao {
    Connection connection;
    Statement stmt;
    String query;

    private static Connection getConnection() throws SQLException, ClassNotFoundException {
        Connection con = db_connection.getInstance().getConnection();
        return con;
    }

    public Connection getConn() {
        Connection con = null;
        try {
            con = db_connection.getInstance().getConnection();
        } catch (SQLException ex) {
            System.out.println(ex);
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
        }
        return con;
    }

    public ArrayList<String> getNeedToSyncSO() {
        query = "SELECT so_number FROM master_so WHERE is_sync = 0";
        ArrayList<String> so = new ArrayList<>();
        try {
            connection = getConn();
            stmt = connection.createStatement();
            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    so.add(rs.getString(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return so;
    }

    public void syncSOSuccess(ArrayList<String> list_so) {
        try {
            connection = getConn();
            PreparedStatement preparedStatement;

            query = "UPDATE master_so SET is_sync = 1, is_success = 1, date_sync = NOW() WHERE so_number IN (?)";
            preparedStatement = connection.prepareStatement(query);
            connection.setAutoCommit(false);

            for (String so: list_so) {
                preparedStatement.setString(1, so);
                preparedStatement.addBatch();
            }

            int[] rows = preparedStatement.executeBatch();
            System.out.println("Update "+rows.length+" success SO");
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void syncSOFailed(Map<String, String> list_so) {
        try {
            connection = getConn();
            PreparedStatement preparedStatement;

            query = "UPDATE master_so SET is_sync = 1, is_success = 0, remarks = ?, date_sync = NOW() WHERE so_number = ?";
            preparedStatement = connection.prepareStatement(query);
            connection.setAutoCommit(false);

            for (Map.Entry<String, String> so : list_so.entrySet()) {
                preparedStatement.setString(1, so.getValue());
                preparedStatement.setString(2, so.getKey());
                preparedStatement.addBatch();
            }

            int[] rows = preparedStatement.executeBatch();
            System.out.println("Update "+rows.length+" failed SO");
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkTransactionSO(Connection connection, transaction_so so) {
        boolean found = false;
        query = "SELECT id FROM get_so_sap WHERE so_type = ? AND so_number = ? AND item_so = ? AND po_number = ? AND item_po = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, so.getSOType());
            ps.setString(2, so.getSONumber());
            ps.setString(3, so.getItemSO());
            ps.setString(4, so.getPONumber());
            ps.setString(5, so.getItemPO());

            ResultSet rs = ps.executeQuery();
            while ( rs.next() ) {
                found = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return found;
    }

    public void insertTransactionSO(ArrayList<transaction_so> transactions) {
        try {
            connection = getConn();
            PreparedStatement preparedStatement;

            query = "INSERT INTO get_so_sap (so_type, so_number, item_so, po_number, item_po) VALUES (?,?,?,?,?)";
            preparedStatement = connection.prepareStatement(query);
            connection.setAutoCommit(false);

            for (transaction_so trx : transactions) {
                if (checkTransactionSO(connection, trx)) {
                    continue;
                }
                preparedStatement.setString(1, trx.getSOType());
                preparedStatement.setString(2, trx.getSONumber());
                preparedStatement.setString(3, trx.getItemSO());
                preparedStatement.setString(4, trx.getPONumber());
                preparedStatement.setString(5, trx.getItemPO());
                preparedStatement.addBatch();
            }

            int[] rows = preparedStatement.executeBatch();
            System.out.println("Insert "+rows.length+" transaction SO");
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<transaction_so> getNeedToPushSO() {
        query = "SELECT id, so_type, so_number, item_so, po_number, item_po, code_container, seal_id, is_b2b, net_qty_sap " +
                "FROM transaction_customer WHERE is_complete = 1 AND is_sync = 0 AND is_sap = 1";
        ArrayList<transaction_so> list_so = new ArrayList<>();
        try {
            connection = getConn();
            stmt = connection.createStatement();
            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    transaction_so so = new transaction_so(
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getString(7),
                            rs.getString(8),
                            rs.getBoolean(9),
                            rs.getDouble(10)
                    );
                    list_so.add(so);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list_so;
    }

    public void updateTransactionSO(ArrayList<transaction_so> list_so) {
        try {
            connection = getConn();
            PreparedStatement preparedStatement;

            query = "UPDATE transaction_customer SET is_sync = 1, is_success = ?, remarks = ?, date_sync = NOW() WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            connection.setAutoCommit(false);

            for (transaction_so so : list_so) {
                preparedStatement.setBoolean(1, so.getIsSuccess());
                preparedStatement.setString(2, so.getRemarks());
                preparedStatement.setInt(3, so.getId());
                preparedStatement.addBatch();
            }

            int[] rows = preparedStatement.executeBatch();
            System.out.println("Update "+rows.length+" rows transaction SO");
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertPostSOLogs(ArrayList<transaction_so> transactions) {
        try {
            connection = getConn();
            PreparedStatement preparedStatement;

            query = "INSERT INTO post_so_sap_log (transaction_customer_id, request, is_success, remarks, exec_date) VALUES (?,?,?,?,NOW())";
            preparedStatement = connection.prepareStatement(query);
            connection.setAutoCommit(false);

            for (transaction_so trx : transactions) {
                String request = String.format("{\"so_number\"=%s, \"item_so\"=%s, \"po_number\"=%s, \"item_po\"=%s, \"code_container\"=%s, \"seal_id\"=%s, \"is_b2b\"=%b, \"net_qty_sap\"=%f}",
                        trx.getSONumber(),
                        trx.getItemSO(),
                        trx.getPONumber(),
                        trx.getItemPO(),
                        trx.getCodeContainer(),
                        trx.getSealId(),
                        trx.getIsB2B(),
                        trx.getQtyNet()
                );

                preparedStatement.setInt(1, trx.getId());
                preparedStatement.setString(2, request);
                preparedStatement.setBoolean(3, trx.getIsSuccess());
                preparedStatement.setString(4, trx.getRemarks());
                preparedStatement.addBatch();
            }

            int[] rows = preparedStatement.executeBatch();
            System.out.println("Insert "+rows.length+" transaction SO Log");
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
