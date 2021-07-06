import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class BankDBManager extends JFrame implements ActionListener {
    //JFrame jf = new JFrame("Bank Batabase Manager");
    private JTabbedPane tp = new JTabbedPane();
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu("设置");
    private JMenuItem itemExit = new JMenuItem("退出");
    private JMenuItem login = new JMenuItem("登陆");
    private JMenuItem logout = new JMenuItem("登出");

    private ClientManager clMng;
    private AccountManager acMng;
    private DebtManager deMng;
    private BusinessManager buMng;

    private String dbURL, port, address;
    private String dbDriver = "com.mysql.cj.jdbc.Driver";
    private String userName, password;
    protected Connection conn = null;
    protected ResultSet res = null;
    protected PreparedStatement pstmt = null;

    private String[] title = {"客户管理", "账户管理", "贷款管理", "业务统计"};

    public BankDBManager() {
        super("Bank Batabase Manager");
        draw();
    }

    public void actionPerformed(ActionEvent e) {
        //Menu
        if (e.getSource() == itemExit) {
            System.exit(0);
        }
        if (e.getSource() == login) {
            loginFunc();
        }
        if (e.getSource() == logout) {
            logoutFunc();
        }
    }

    public void tabInit() {
//        tp.add(brMng.panelBranch);
//        tp.add(stMng.panelStaff);
        tp.add(clMng.panelClient);
        tp.add(acMng.panelAccount);
        tp.add(deMng.panelDebt);
        tp.add(buMng.panelBusiness);
        for (int i = 0; i < title.length; i++) {
            tp.setTitleAt(i, title[i]);
        }
        this.add(tp);
    }

    public void getConnection() throws SQLException {
        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            showError(e.getMessage());
            System.out.println(e);
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(dbURL, userName, password);
        } catch (SQLException e) {
            showError(e.getMessage());
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void closeAll() throws SQLException {
        if (res != null) {
            try {
                res.close();
            } catch (SQLException e) {
                showError(e.getMessage());
                System.out.println(e);
                e.printStackTrace();
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                showError(e.getMessage());
                System.out.println(e);
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                showError(e.getMessage());
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

    public void loginFunc() {
        JTextField urlField = new JTextField(15);
        JTextField portField = new JTextField(15);
        JCheckBox usedeaultField = new JCheckBox("使用内置服务器", Boolean.TRUE);
        JTextField userField = new JTextField(15);
        JTextField passwdField = new JTextField(15);

        JPanel myPanel = new JPanel();
        myPanel.setPreferredSize(new Dimension(100, 250));


        myPanel.add(usedeaultField);
        myPanel.add(new JLabel("DataBase Address:"));
        myPanel.add(urlField);
        myPanel.add(Box.createVerticalStrut(2));
        myPanel.add(new JLabel("DataBase Port:"));
        myPanel.add(portField);
        myPanel.add(Box.createVerticalStrut(2));
        myPanel.add(new JLabel("User Name:"));
        myPanel.add(userField);
        myPanel.add(Box.createVerticalStrut(2));
        myPanel.add(new JLabel("Password:"));
        myPanel.add(passwdField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, "Log In", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (usedeaultField.isSelected()) {
                port = "52000";
                address = "206.yyw.moe";
                userName = "root";
                password = "totoroyyw";
            } else {
                address = urlField.getText();
                port = portField.getText();
                userName = userField.getText();
                password = passwdField.getText();
            }
            dbURL = "jdbc:mysql://" + address + ":" + port + "/lab3?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            System.out.println(dbURL);
        }
        if (userName != null && password != null && dbURL != null) {
            try {
                getConnection();
                login.setEnabled(false);
                logout.setEnabled(true);
                clMng.setConn(conn);
                acMng.setConn(conn);
                deMng.setConn(conn);
                buMng.setConn(conn);
                clMng.pClInit();
                acMng.pAcInit();
                deMng.pDeInit();
                buMng.pBuInit();
            } catch (SQLException sqle) {
                showError("连接失败！\n请重试！");
                System.out.println("连接失败！\n请重试！");
                sqle.printStackTrace();
            }
        }
    }

    public void logoutFunc() {
        try {
            closeAll();
            JOptionPane.showMessageDialog(null, "登出成功！", "成功！", JOptionPane.PLAIN_MESSAGE);
            login.setEnabled(true);
            logout.setEnabled(false);
            clMng.panelClient.removeAll();
            acMng.panelAccount.removeAll();
            deMng.panelDebt.removeAll();
            buMng.panelBusiness.removeAll();
        } catch (SQLException sqle) {
            showError("登出失败！\n请重试！");
            System.out.println("登出失败！\n请重试！");
            sqle.printStackTrace();
        }
    }

    public void menuInit() {
        this.setJMenuBar(menuBar);
        menuBar.add(menuFile);
        login.addActionListener(this);
        menuFile.add(login);
        logout.addActionListener(this);
        logout.setEnabled(false);
        menuFile.add(logout);
        itemExit.addActionListener(this);
        menuFile.add(itemExit);
    }

    public void draw() {
        clMng = new ClientManager();
        acMng = new AccountManager();
        deMng = new DebtManager();
        buMng = new BusinessManager();
        tabInit();
        menuInit();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//点X关闭窗口
        setLocation(100, 100); //初始化时定位
        setSize(1200, 650);
        //setBackground(deepGrey);
        setResizable(false);   //禁止拖曳改变窗口大小
        setVisible(true);  //显示窗口
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "错误！", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String args[]) {
        new BankDBManager();
    }
}
