import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StaffManager extends JFrame implements ActionListener {
    public JPanel panelStaff;
    private JLabel condLabel;
    private JTextField condText;
    private JTable resTable;
    private JLabel[] paraLabel = new JLabel[6];
    private JTextField[] paraText = new JTextField[6];
    private JComboBox[] paraSymBox;
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
    private String dbName = "员工";
    private String[] labelName = {"身份证号：", "支行名：", "姓名：", "电话号码：", "家庭住址：", "开始工作日期："};
    private String[] colName = {"", "身份证号", "支行名", "姓名", "电话号码", "家庭住址", "开始工作日期"};
    private String[] syms = {"=", ">", "<", ">=", "<=", "<>"};
    private Object[][] data = {{Boolean.FALSE, "", "", "", "", "", ""}};
    private Object[][] dataTable;
    private int INSERT = 1, DELETE = 2, UPDATE = 3, SEARCH = 4;
    private int labelNum = labelName.length;
    Object[][] oldTable;

    public StaffManager() {
        panelStaff = new JPanel();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == insertBtn) {
            String newRow = insertInfo();
            String paraNames = "";
            for (int i = 0; i < labelNum; i++) {
                paraNames += colName[i + 1];
                if (i < labelNum - 1) {
                    paraNames += ",";
                }
            }
            if (newRow != null) {
                String asql = "Insert Into " + dbName + "(" + paraNames + ") Values (" + newRow + ")";
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
                    String staffID = resTable.getValueAt(i, 1).toString();
                    if (!allowDelete(staffID)) {
                        showError(errMsgs[1]);
                        continue;
                    }
                    asql = "Delete From " + dbName + " Where 身份证号 = '" + staffID + "'";
                    System.out.println(asql);
                    try {
                        exeSQL(conn, asql, DELETE);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        showError(e1.getMessage());
                    }
                }
            }
            initTable(resTable, data, Arrays.copyOfRange(colName, 0, labelNum + 1));
        }
        if (e.getSource() == updateBtn) {
            int rowCnt = resTable.getRowCount();
            int colCnt = resTable.getColumnCount();
            //System.out.println(rowCnt + " " + colCnt);
            for (int i = 0; i < rowCnt; i++) {
                String asql;
                StringBuilder cond = new StringBuilder();
                StringBuilder change = new StringBuilder();
                for (int j = 0; j < colCnt - 1; j++) {
                    if (j == 5) {
                        cond.append(colName[j + 1]).append(" = ").append("'").append(oldTable[i][j + 1].toString().substring(0, 10)).append("'");
                        change.append(colName[j + 1]).append(" = ").append("'").append(resTable.getValueAt(i, j + 1).toString().substring(0, 10)).append("'");
                    } else if (oldTable[i][j + 1] == null || oldTable[i][j + 1].equals("")) {
                        cond.append(colName[j + 1]).append(" is null ");
                    } else {
                        cond.append(colName[j + 1]).append(" = '").append(oldTable[i][j + 1]).append("' ");
                        change.append(colName[j + 1]).append(" = '").append(resTable.getValueAt(i, j + 1)).append("' ");
                    }
                    if (j < colCnt - 2) {
                        cond.append(" and ");
                        change.append(",");
                    }
                }
                if (resTable.getValueAt(i, 0).equals(true)) {
                    asql = "Update " + dbName + " Set " + change + " Where " + cond;
                    System.out.println(i + asql);
                    try {
                        exeSQL(conn, asql, DELETE);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        showError(e1.getMessage());
                    }
                }
            }
            initTable(resTable, data, Arrays.copyOfRange(colName, 0, labelNum + 1));
        }
        if (e.getSource() == searchBtn) {
            String cond = "";
            for (int i = 0; i < labelNum; i++) {
                if (!paraText[i].getText().equals("") && !paraText[i].getText().equals("yyyy/mm/dd")) {
                    if (!cond.equals("")) {
                        cond += " and ";
                    }
                    if (i == 5) {
                        cond += colName[i + 1] + syms[paraSymBox[i].getSelectedIndex()] + "STR_TO_DATE('" + paraText[i].getText() + "', '%Y/%m/%d')";
                    } else {
                        cond += colName[i + 1] + syms[paraSymBox[i].getSelectedIndex()] + " '" + paraText[i].getText() + "'";
                    }
                }
            }
            if (!condText.getText().equals("")) {
                if (!cond.equals("")) {
                    cond += " and ";
                }
                cond += condText.getText();
            }
            String asql;
            if (cond.equals("")) {
                asql = "select * from " + dbName;
            } else {
                asql = "select * from " + dbName + " where " + cond;
            }
            System.out.println(asql);
            try {
                ResultSet aRSet = exeSQL(conn, asql, SEARCH);
                List<Object[]> resList = new ArrayList<Object[]>();
                while (aRSet != null && aRSet.next()) {
                    Object[] line = new Object[labelNum + 1];
                    line[0] = Boolean.FALSE;
                    for (int j = 1; j < line.length; j++) {
                        line[j] = aRSet.getObject(j);
                    }
                    resList.add(line);
                }
                try {
                    Object[][] res = new Object[resList.size()][labelNum + 1];
                    resList.toArray(res);
                    dataTable = res;
                    if (resList.size() == 0) {
                        initTable(resTable, data, Arrays.copyOfRange(colName, 0, labelNum + 1));
                    } else {
                        initTable(resTable, res, Arrays.copyOfRange(colName, 0, labelNum + 1));
                    }
                } catch (NullPointerException e1) {
                    showInfo(0);
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
                showError(e1.getMessage());
            }
            for (int i = 0; i < labelNum; i++) {
                paraText[i].setText("");
            }
            condText.setText("");
            for (int i = 0; i < resTable.getRowCount(); i++) {
                if (resTable.getValueAt(i, 1).equals("")) {
                    break;
                }
                for (int j = 0; j < resTable.getColumnCount(); j++) {
                    oldTable[i][j] = resTable.getValueAt(i, j);
                }
            }
        }
    }

    public boolean allowDelete(String staffID) {
        boolean tag = false;
        ResultSet set;
        int res;
        String asql = "select * from 客户 where 员工_身份证号 = '" + staffID + "'";
        try {
            set = exeSQL(conn, asql, SEARCH);
            res = 0;
            while (set != null && set.next()) {
                res++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError(e.getMessage());
            return false;
        }

        if (res == 0) {
            tag = true;
        }
        return tag;
    }

    public String insertInfo() {
        JLabel[] nparaLabel = new JLabel[labelNum];
        JTextField[] nparaText = new JTextField[labelNum];
        String newRow = "";
        String[] paras = {"", "", "", "", "", ""};

        JPanel myPanel = new JPanel();
        myPanel.setPreferredSize(new Dimension(250, 400));
        for (int i = 0; i < labelNum; i++) {
            nparaLabel[i] = new JLabel(labelName[i]);
            nparaText[i] = new JTextField(20);
            myPanel.add(nparaLabel[i]);
            myPanel.add(Box.createVerticalStrut(2)); // a spacer
            myPanel.add(nparaText[i]);
        }
        nparaText[5].setText("yyyy/mm/dd");
        nparaText[5].addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                // TODO Auto-generated method stub
                if (nparaText[5].getText().equals("yyyy/mm/dd")) {
                    nparaText[5].setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // TODO Auto-generated method stub
                if (nparaText[5].getText().equals("")) {
                    nparaText[5].setText("yyyy/mm/dd");
                }
            }

        });

        int result = JOptionPane.showConfirmDialog(null, myPanel, "输入要插入的行的信息：", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            for (int i = 0; i < labelNum; i++) {
                paras[i] = nparaText[i].getText();
                if (i == 5) {
                    newRow += "STR_TO_DATE('" + paras[i] + "', '%Y/%m/%d')";
                } else {
                    newRow += "'" + paras[i] + "'";
                }
                if (i < labelNum - 1) {
                    newRow += ",";
                }
            }
            return newRow;
        }
        return null;
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

    private void initTable(JTable table, Object[][] data, String[] colName) {
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
        tcm.getColumn(0).setWidth(100);
        tcm.getColumn(0).setMaxWidth(100);
        for (int i = 1; i < colName.length; i++) {
            tcm.getColumn(i).setWidth(100);
        }
        oldTable = new Object[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                oldTable[i][j] = data[i][j];
            }
        }
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

    public void pStInit() {
        JLabel space;
        panelStaff.setLayout(new FlowLayout(FlowLayout.LEFT));
        paraSymBox = new JComboBox[labelNum];
        for (int i = 0; i < labelNum; i++) {
            paraLabel[i] = new JLabel(labelName[i]);
            paraLabel[i].setPreferredSize(new Dimension(120, 25));
            paraLabel[i].setOpaque(true);
            paraLabel[i].setFont(new Font("Dialog", 1, 16));
            panelStaff.add(paraLabel[i]);
            paraSymBox[i] = new JComboBox(syms);
            panelStaff.add(paraSymBox[i]);
            paraText[i] = new JTextField(10);
            paraText[i].setPreferredSize(new Dimension(100, 25));
            paraText[i].setOpaque(true);
            panelStaff.add(paraText[i]);
        }
        paraLabel[5].setPreferredSize(new Dimension(120, 25));
        paraText[5].setText("yyyy/mm/dd");
        paraText[5].addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                // TODO Auto-generated method stub
                if (paraText[5].getText().equals("yyyy/mm/dd")) {
                    paraText[5].setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // TODO Auto-generated method stub
                if (paraText[5].getText().equals("")) {
                    paraText[5].setText("yyyy/mm/dd");
                }
            }

        });
        condLabel = new JLabel("其他条件：");
        condText = new JTextField(40);
        condLabel.setPreferredSize(new Dimension(100, 25));
        condLabel.setOpaque(true);
        condLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        panelStaff.add(condLabel);
        condText.setOpaque(true);
        condText.setPreferredSize(new Dimension(800, 25));
        panelStaff.add(condText);
        //功能
        JPanel funcPane = new JPanel();
        funcPane.setPreferredSize(new Dimension(1170, 50));
        funcPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        funcPane.setOpaque(true);
        panelStaff.add(funcPane);
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
        //resTable.setPreferredSize(new Dimension(500, 500));
        initTable(resTable, data, Arrays.copyOfRange(colName, 0, labelNum + 1));
        resTable.setOpaque(true);
        resTable.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        JScrollPane resPane = new JScrollPane(resTable);
        resPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        resPane.setPreferredSize(new Dimension(1170, 370));
        panelStaff.add(resPane);
    }

}