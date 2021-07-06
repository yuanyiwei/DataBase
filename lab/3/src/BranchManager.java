import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchManager extends JFrame implements ActionListener {
    public JPanel panelBranch;
    private JLabel condLabel;
    private JTextField condText;
    private JTable resTable;
    private JLabel nameLabel, moneyLabel;
    private JTextField nameText, moneyText;
    private JComboBox moneySymBox;
    private JButton insertBtn, deleteBtn, updateBtn, searchBtn;

    protected Connection conn = null;
    protected PreparedStatement pstmt = null;
    protected Statement stmt = null;
    protected ResultSet res = null;
    private String[] errMsgs = {
            "无数据库连接，请先连接至数据库！",
            "存在着关联信息，不能删除！"
    };
    private String[] infoMsgs = {
            "查询无结果"
    };
    private String[] colName = {"", "支行名", "资产"};
    private String[] syms = {"=", ">", "<", ">=", "<=", "<>"};
    private Object[][] data = {{Boolean.FALSE, "", ""}};
    private Object[][] dataTable;
    private int INSERT = 1, DELETE = 2, UPDATE = 3, SEARCH = 4;

    public BranchManager() {
        panelBranch = new JPanel();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == insertBtn) {
            String newRow = insertInfo();
            if (newRow != null) {
                String asql = "Insert Into 支行(支行名, 资产) Values (" + newRow + ")";
                System.out.println(asql);
                try {
                    ResultSet aRSet = exeSQL(conn, asql, INSERT);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    showError(e1.getMessage());
                }
            }
        }
        if (e.getSource() == deleteBtn) {
            int rowCnt = resTable.getRowCount();
            System.out.println(rowCnt);
            for (int i = 0; i < rowCnt; i++) {
                String asql;
                if (resTable.getValueAt(i, 0).equals(true)) {
                    String name = resTable.getValueAt(i, 1).toString();
                    if (!allowDelete(name)) {
                        showError(errMsgs[1]);
                        continue;
                    }
                    asql = "Delete From 支行 Where 支行名 = '" + name + "'";
                    System.out.println(asql);
                    try {
                        exeSQL(conn, asql, DELETE);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        showError(e1.getMessage());
                    }
                }
                System.out.println("here");
            }
            initTable(resTable, data);
        }
        if (e.getSource() == updateBtn) {
            int rowCnt = resTable.getRowCount();
            for (int i = 0; i < rowCnt; i++) {
                String asql, cond, change;
                cond = "支行名 = '" + dataTable[i][1] + "'";
                change = "支行名 = '" + resTable.getValueAt(i, 1) + "' , 资产 = " + resTable.getValueAt(i, 2);
                if (resTable.getValueAt(i, 0).equals(true)) {
                    asql = "Update 支行 Set " + change + " Where " + cond;
                    System.out.println(asql);
                    try {
                        exeSQL(conn, asql, UPDATE);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        showError(e1.getMessage());
                    }
                }
            }
            initTable(resTable, data);
        }
        if (e.getSource() == searchBtn) {
            String cond = "";
            if (!nameText.getText().equals("")) {
                cond += "支行名 = '" + nameText.getText() + "'";
            }
            if (!moneyText.getText().equals("")) {
                if (!cond.equals("")) {
                    cond += " and ";
                }
                cond += "资产" + syms[moneySymBox.getSelectedIndex()] + "'" + moneyText.getText() + "'";
            }
            if (!condText.getText().equals("")) {
                if (!cond.equals("")) {
                    cond += " and ";
                }
                cond += condText.getText();
            }
            if (null == conn) {
                showError(0);
            } else {
                String asql;
                if (cond.equals("")) {
                    asql = "select * from 支行";
                } else {
                    asql = "select * from 支行 where " + cond;
                }
                System.out.println(asql);
                try {
                    ResultSet aRSet = exeSQL(conn, asql, SEARCH);
                    List<Object[]> resList = new ArrayList<Object[]>();
                    while (aRSet != null && aRSet.next()) {
                        Object[] line = new Object[3];
                        String name = aRSet.getString(1);
                        double money = aRSet.getDouble(2);
                        line[0] = Boolean.FALSE;
                        line[1] = name;
                        line[2] = money;
                        resList.add(line);
                    }
                    try {
                        Object[][] res = new Object[resList.size()][3];
                        resList.toArray(res);
                        dataTable = res;
                        initTable(resTable, res);
                    } catch (NullPointerException e1) {
                        showInfo(0);
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    showError(e1.getMessage());
                }
                nameText.setText("");
                moneyText.setText("");
                condText.setText("");
            }
        }
    }

    public String insertInfo() {
        JTextField nNameField = new JTextField(15);
        JTextField nMoneyField = new JTextField(15);
        String newRow = null, name = null, money = null;

        JPanel myPanel = new JPanel();
        myPanel.setPreferredSize(new Dimension(100, 200));
        myPanel.add(new JLabel("支行名："));
        myPanel.add(nNameField);
        myPanel.add(Box.createVerticalStrut(2)); // a spacer
        myPanel.add(new JLabel("资产："));
        myPanel.add(nMoneyField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, "输入要插入的行的信息：", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            name = nNameField.getText();
            money = nMoneyField.getText();
        }
        if (name != null && money != null) {
            newRow = "'" + name + "', '" + money + "'";
            return newRow;
        } else if (result == JOptionPane.OK_OPTION) {
            showError(3);
        }
        return null;
    }

    public boolean allowDelete(String name) {
        boolean tag = false;
        ResultSet set1, set2;
        int res1, res2;
        String asql1 = "select * from 员工 where 支行名 = '" + name + "'";
        try {
            set1 = exeSQL(conn, asql1, SEARCH);
            res1 = 0;
            while (set1 != null && set1.next()) {
                res1++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError(e.getMessage());
            return false;
        }

        String asql2 = "select * from 拥有账户 where 支行名 = '" + name + "'";
        try {
            set2 = exeSQL(conn, asql2, SEARCH);
            res2 = 0;
            while (set2 != null && set2.next()) {
                res2++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError(e.getMessage());
            return false;
        }
        if (res1 == 0 && res2 == 0) {
            tag = true;
        }
        return tag;
    }

    public void setConn(Connection aconn) {
        conn = aconn;
    }

    public void showError(int msgIdx) {
        JOptionPane.showMessageDialog(null, errMsgs[msgIdx], "错误！", JOptionPane.ERROR_MESSAGE);
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "错误！", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(int msgIdx) {
        JOptionPane.showMessageDialog(null, infoMsgs[msgIdx], "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    protected ResultSet exeSQL(Connection conn, String sql, int mode) throws SQLException {
        System.out.println(sql);
        if (conn == null) {
            showError(0);
        }
        ResultSet resSet = null;
        try {
            if (mode == DELETE || mode == UPDATE || mode == INSERT) {
                stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                return null;
            } else if (mode == SEARCH) {
                pstmt = conn.prepareStatement(sql);
                resSet = pstmt.executeQuery();
            } else System.out.println("Err mode"); // TODO: try catch
        } catch (SQLException e) {
            //TODO: handle exception
            e.printStackTrace();
            showError(e.getMessage());
        }
        return resSet;
    }

    private void initTable(JTable table, Object[][] data) {
        DefaultTableModel dtm = new DefaultTableModel(
                colName, 0);
        for (int i = 0; i < data.length; i++) {
            dtm.addRow(data[i]);
        }

        table.setModel(dtm);
        TableColumnModel tcm = table.getColumnModel();

        tcm.getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        tcm.getColumn(0).setCellRenderer(new MyTableRenderer());

        tcm.getColumn(0).setPreferredWidth(20);
        tcm.getColumn(0).setWidth(20);
        tcm.getColumn(0).setMaxWidth(20);
    }

    private class MyTableRenderer extends JCheckBox implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            Boolean b = (Boolean) value;
            this.setSelected(b.booleanValue());
            return this;
        }
    }

    public void pBrInit() {
        JLabel space;
        panelBranch.setLayout(new FlowLayout(FlowLayout.LEFT));
        nameLabel = new JLabel("支行名：");
        nameText = new JTextField(10);
        moneyLabel = new JLabel("资产：");
        moneyText = new JTextField(10);
        insertBtn = new JButton("添加");
        condLabel = new JLabel("其他条件：");
        condText = new JTextField(30);
        space = new JLabel("");
        nameLabel.setPreferredSize(new Dimension(100, 25));
        nameLabel.setOpaque(true);
        nameLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        panelBranch.add(nameLabel);
        nameText.setPreferredSize(new Dimension(400, 25));
        nameText.setOpaque(true);
        panelBranch.add(nameText);
        moneyLabel.setPreferredSize(new Dimension(100, 25));
        moneyLabel.setOpaque(true);
        moneyLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        panelBranch.add(moneyLabel);
        moneySymBox = new JComboBox(syms);
        panelBranch.add(moneySymBox);
        moneyText.setPreferredSize(new Dimension(400, 25));
        moneyText.setOpaque(true);
        panelBranch.add(moneyText);
        space.setPreferredSize(new Dimension(100, 25));
        panelBranch.add(space);
        condLabel.setPreferredSize(new Dimension(100, 25));
        condLabel.setOpaque(true);
        condLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        panelBranch.add(condLabel);
        condText.setOpaque(true);
        condText.setPreferredSize(new Dimension(800, 25));
        panelBranch.add(condText);
        //功能
        JPanel funcPane = new JPanel();
        funcPane.setPreferredSize(new Dimension(1170, 50));
        funcPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        funcPane.setOpaque(true);
        panelBranch.add(funcPane);
        //添加
        insertBtn = new JButton("添加");
        insertBtn.setPreferredSize(new Dimension(200, 40));
        insertBtn.setOpaque(true);
        insertBtn.addActionListener(this);
        funcPane.add(insertBtn);
        insertBtn.setFont(new Font("微软雅黑", Font.PLAIN, 30));
        //删除
        deleteBtn = new JButton("删除");
        deleteBtn.setPreferredSize(new Dimension(200, 40));
        deleteBtn.setOpaque(true);
        deleteBtn.addActionListener(this);
        funcPane.add(deleteBtn);
        deleteBtn.setFont(new Font("微软雅黑", Font.PLAIN, 30));
        //修改
        updateBtn = new JButton("修改");
        updateBtn.setPreferredSize(new Dimension(200, 40));
        updateBtn.setOpaque(true);
        updateBtn.addActionListener(this);
        funcPane.add(updateBtn);
        updateBtn.setFont(new Font("微软雅黑", Font.PLAIN, 30));
        //查询
        searchBtn = new JButton("查询");
        searchBtn.setPreferredSize(new Dimension(200, 40));
        searchBtn.setOpaque(true);
        searchBtn.addActionListener(this);
        funcPane.add(searchBtn);
        searchBtn.setFont(new Font("微软雅黑", Font.PLAIN, 30));
        //结果表格
        resTable = new JTable();
        //resTable.setPreferredSize(new Dimension(600, 500));
        initTable(resTable, data);
        resTable.setOpaque(true);
        resTable.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        JScrollPane resPane = new JScrollPane(resTable);
        resPane.setPreferredSize(new Dimension(1170, 400));
        panelBranch.add(resPane);
    }

}