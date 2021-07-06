import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.*;

public class BusinessManager extends JFrame implements ActionListener {
    public JPanel panelBusiness;
    private JLabel catLabel, timeLabel, branchLabel;
    private JComboBox catBox, timeBox, branchBox;
    private JButton refreshBtn, searchBtn, plotMoneyBtn, plotClientBtn;
    private JTable resTable;

    protected Connection conn = null;
    protected PreparedStatement pstmt = null;
    protected Statement stmt = null;
    protected ResultSet res = null;
    private String[] errMsgs = {
            "无数据库连接，请先连接至数据库！",
            "请填写条件！",
            "查询出现错误，请重试！",
            "请完整填写信息！",
            "添加出现错误！请重试！",
            "删除出现错误！请重试！"
    };
    private String[] infoMsgs = {
            "查询无结果"
    };
    private String[] catStr = {"储蓄账户", "贷款"};
    private int catSel = 0;
    private String[] timeStr = {"月", "季", "年"};
    private int timeSel = 0;
    private String[] seasons = {"第一季度", "第二季度", "第三季度", "第四季度"};
    private String[] branchStr = {""};
    private int branchSel = 0;
    private Calendar now = Calendar.getInstance();
    private Calendar start = Calendar.getInstance();
    private String[] labelName = {"时间：", "业务金额：", "用户数："};
    private String[] colName = {"时间", "业务金额", "用户数"};
    private Object[][] data = {{"", "", ""}};
    private Object[][] dataTable;
    private int INSERT = 1, DELETE = 2, UPDATE = 3, SEARCH = 4;
    private int labelNum = colName.length;
    Object[][] oldTable;

    public BusinessManager() {
        panelBusiness = new JPanel();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchBtn) {
            String[] timepoints = getTimePoints();
            String[] timeInt = getTimeInt(timepoints);
            Object[][] adata = new Object[timepoints.length][labelNum];
            List<Object[]> resList = new ArrayList<Object[]>();
            try {
                for (int i = 0; i < timepoints.length - 1; i++) {
                    String asql, cond;
                    if (catSel == 0) {
                        asql = "select sum(余额), count(拥有账户.身份证号) from " + catStr[catSel] + ", 拥有账户"
                                + " where 拥有账户.账户号 = " + catStr[catSel] + ".账户号";
//                        cond = " and " + catStr[catSel] + ".开户日期 > " + "STR_TO_DATE('" + timepoints[i] + "', '%Y/%m/%d') "
//                                + "and " + catStr[catSel] + ".开户日期 <= " + "STR_TO_DATE('" + timepoints[i + 1] + "', '%Y/%m/%d') "
//                                + "and 拥有账户.支行名 = '" + branchStr[branchSel] + "' "
//                                + "and 拥有账户.账户类型 = '储蓄账户'";
                        cond = " and " + catStr[catSel] + ".开户日期 <= " + "STR_TO_DATE('" + timepoints[i + 1] + "', '%Y/%m/%d') "
                                + "and 拥有账户.支行名 = '" + branchStr[branchSel] + "' "
                                + "and 拥有账户.账户类型 = '储蓄账户'";
                        asql += cond;
                    } else {
                        asql = "select sum(金额), count(拥有贷款.身份证号) from " + catStr[catSel] + ", 拥有贷款"
                                + " where 拥有贷款.贷款号 = " + catStr[catSel] + ".贷款号";
//                        cond = " and " + catStr[catSel] + ".日期 > " + "STR_TO_DATE('" + timepoints[i] + "', '%Y/%m/%d')"
//                                + " and " + catStr[catSel] + ".日期 <= " + "STR_TO_DATE('" + timepoints[i + 1] + "', '%Y/%m/%d')"
//                                + "and 贷款.支行名 = '" + branchStr[branchSel] + "'";
                        cond = " and " + catStr[catSel] + ".日期 <= " + "STR_TO_DATE('" + timepoints[i + 1] + "', '%Y/%m/%d')"
                                + "and 贷款.支行名 = '" + branchStr[branchSel] + "'";
                        asql += cond;
                    }
                    // TODO: err sql
                    ResultSet aRSet = exeSQL(conn, asql, SEARCH);

                    while (aRSet != null && aRSet.next()) {
                        Object[] line = new Object[labelNum];
                        line[0] = timeInt[i];
                        for (int j = 1; j < line.length; j++) {
                            line[j] = aRSet.getObject(j);
                        }
                        if (line[1] == null || line[1].equals("")) {
                            line[1] = "0";
                        }
                        resList.add(line);
                    }
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
                showError(e1.getMessage());
            }
            Object[][] res = new Object[resList.size()][labelNum + 1];
            resList.toArray(res);
            setTable(resTable, res, colName);
        }
        if (e.getSource() == plotMoneyBtn) {
            plotDraw(1);
        }
        if (e.getSource() == plotClientBtn) {
            plotDraw(2);
        }
        if (e.getSource() == refreshBtn) {
            panelBusiness.removeAll();
            pBuInit();
        }
    }

    public String[] getTimePoints() {
        int nyears, nmonths;
        int year, month;
        String[] tpoints;
        nyears = now.get(Calendar.YEAR) - start.get(Calendar.YEAR);
        nmonths = now.get(Calendar.MONTH) - start.get(Calendar.MONTH) + 1;
        if (timeSel == 0) {
            int len = 12 * nyears + nmonths + 2;
            if (len == 1) {
                len = 2;
            }
            tpoints = new String[len];
            year = start.get(Calendar.YEAR);
            month = start.get(Calendar.MONTH);
            tpoints[0] = String.valueOf(year) + "/" + String.format("%02d", month) + "/01";
            for (int i = 1; i < tpoints.length; i++) {
                month++;
                if (month > 12) {
                    month = 1;
                    year++;
                }
                tpoints[i] = String.valueOf(year) + "/" + String.format("%02d", month) + "/01";
            }
        } else if (timeSel == 1) {
            int len = (12 * nyears + nmonths + 2) / 3 + 1;
            if (len == 1) {
                len = 2;
            }
            tpoints = new String[len];
            year = start.get(Calendar.YEAR);
            month = start.get(Calendar.MONTH) / 3 * 3 + 1;
            tpoints[0] = String.valueOf(year) + "/" + String.format("%02d", month) + "/01";
            for (int i = 1; i < tpoints.length; i++) {
                month += 3;
                if (month > 12) {
                    month = 1;
                    year++;
                }
                tpoints[i] = String.valueOf(year) + "/" + String.format("%02d", month) + "/01";
            }
        } else {
            int len = nyears + 2;
            if (len == 1) {
                len = 2;
            }
            tpoints = new String[len];
            year = start.get(Calendar.YEAR);
            month = 1;
            tpoints[0] = String.valueOf(year) + "/" + String.format("%02d", month) + "/01";
            for (int i = 1; i < tpoints.length; i++) {
                year++;
                tpoints[i] = String.valueOf(year) + "/" + String.format("%02d", month) + "/01";
            }
        }
        return tpoints;
    }

    public String[] getTimeInt(String[] points) {
        String[] timeint = new String[points.length - 1];
        for (int i = 0; i < timeint.length; i++) {
            if (timeSel == 0) {
                timeint[i] = points[i].substring(0, 4) + "年" + points[i].substring(5, 7) + "月";
            } else if (timeSel == 1) {
                timeint[i] = points[i].substring(0, 4) + "年" + seasons[(Integer.parseInt(points[i].substring(5, 7)) - 1) / 3];
            } else {
                timeint[i] = points[i].substring(0, 4) + "年";
            }
        }
        return timeint;
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

    public void getBranchName() {
        String asql = "select 支行名 from 支行";
        try {
            ResultSet rSet = exeSQL(conn, asql, SEARCH);
            List<String> resList = new ArrayList<String>();
            while (rSet != null && rSet.next()) {
                String line = rSet.getString(1);
                resList.add(line);
            }
            try {
                branchStr = new String[resList.size()];
                resList.toArray(branchStr);
            } catch (NullPointerException e1) {
                showError(e1.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError(e.getMessage());
        }
    }

    private void setTable(JTable table, Object[][] data, String[] colName) {
        DefaultTableModel dtm = new DefaultTableModel(
                colName, 0);
        for (Object[] datum : data) {
            dtm.addRow(datum);
        }

        table.setModel(dtm);
        TableColumnModel tcm = table.getColumnModel();

        for (int i = 0; i < colName.length; i++) {
            tcm.getColumn(i).setWidth(100);
        }
        oldTable = new Object[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                oldTable[i][j] = data[i][j];
            }
        }
    }

    public void plotDraw(int mode) {
        //创建主题样式
        StandardChartTheme mChartTheme = new StandardChartTheme("CN");
        //设置标题字体  
        mChartTheme.setExtraLargeFont(new Font("黑体", Font.BOLD, 20));
        //设置轴向字体  
        mChartTheme.setLargeFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        //设置图例字体  
        mChartTheme.setRegularFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        //应用主题样式  
        ChartFactory.setChartTheme(mChartTheme);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < oldTable.length; i++) {
            dataset.addValue(Double.parseDouble(oldTable[i][mode].toString()), colName[mode], oldTable[i][0].toString());
        }
        JFreeChart lineChart = ChartFactory.createLineChart(
                colName[mode] + "曲线图",
                "时间", colName[mode],
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        ChartFrame mChartFrame = new ChartFrame(colName[mode] + "曲线图如下", lineChart);
        mChartFrame.pack();
        mChartFrame.setVisible(true);
    }

    public void pBuInit() {
        now.setTime(new Date());
        //System.out.println(now.get(Calendar.YEAR) + " " + now.get(Calendar.MONTH) + " " + now.get(Calendar.DATE));
        start.set(2015, 01, 01, 0, 0, 0);
        panelBusiness.setLayout(new FlowLayout(FlowLayout.LEFT));
        refreshBtn = new JButton("刷新");
        refreshBtn.setPreferredSize(new Dimension(80, 25));
        refreshBtn.setOpaque(true);
        refreshBtn.setFont(new Font("Dialog", Font.BOLD, 18));
        refreshBtn.addActionListener(this);
        panelBusiness.add(refreshBtn);
        catLabel = new JLabel("账户类型：");
        catLabel.setPreferredSize(new Dimension(100, 25));
        catLabel.setOpaque(true);
        catLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        panelBusiness.add(catLabel);
        catBox = new JComboBox(Arrays.copyOfRange(catStr, 0, catStr.length));
        catBox.setSelectedIndex(catSel);
        catBox.addItemListener(e -> {
            catSel = catBox.getSelectedIndex();
        });
        panelBusiness.add(catBox);
        timeLabel = new JLabel("时间跨度：");
        timeLabel.setPreferredSize(new Dimension(100, 25));
        timeLabel.setOpaque(true);
        timeLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        panelBusiness.add(timeLabel);
        timeBox = new JComboBox(Arrays.copyOfRange(timeStr, 0, timeStr.length));
        timeBox.setSelectedIndex(timeSel);
        timeBox.addItemListener(e -> {
            timeSel = timeBox.getSelectedIndex();
        });
        panelBusiness.add(timeBox);
        getBranchName();
        branchLabel = new JLabel("支行：");
        branchLabel.setPreferredSize(new Dimension(70, 25));
        branchLabel.setOpaque(true);
        branchLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        panelBusiness.add(branchLabel);
        branchBox = new JComboBox(Arrays.copyOfRange(branchStr, 0, branchStr.length));
        if (branchStr.length > 0) {
            branchBox.setSelectedIndex(branchSel);
        }
        branchBox.addItemListener(e -> {
            branchSel = branchBox.getSelectedIndex();
        });
        panelBusiness.add(branchBox);
        searchBtn = new JButton("查询");
        searchBtn.setPreferredSize(new Dimension(80, 25));
        searchBtn.setOpaque(true);
        searchBtn.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        searchBtn.addActionListener(this);
        panelBusiness.add(searchBtn);
        plotMoneyBtn = new JButton("业务金额曲线");
        plotMoneyBtn.setPreferredSize(new Dimension(150, 25));
        plotMoneyBtn.setOpaque(true);
        plotMoneyBtn.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        plotMoneyBtn.addActionListener(this);
        panelBusiness.add(plotMoneyBtn);
        plotClientBtn = new JButton("客户数曲线");
        plotClientBtn.setPreferredSize(new Dimension(140, 25));
        plotClientBtn.setOpaque(true);
        plotClientBtn.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        plotClientBtn.addActionListener(this);
        panelBusiness.add(plotClientBtn);
        //结果表格
        resTable = new JTable();
        //resTable.setPreferredSize(new Dimension(500, 500));
        setTable(resTable, data, colName);
        resTable.setOpaque(true);
        resTable.setFont(new Font("Dialog", Font.BOLD, 18));
        JScrollPane resPane = new JScrollPane(resTable);
        resPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        resPane.setPreferredSize(new Dimension(1170, 515));
        panelBusiness.add(resPane);
    }
}
