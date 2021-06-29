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

    private JPanel panelStaff, panelClient, panelAccount, panelDebt, panelBusiness;
    private BranchManager brMng;
    private StaffManager stMng;
    private ClientManager clMng;
    private AccountManager acMng;
    private DebtManager deMng;
    private BusinessManager buMng;

    private String dbURL, port, sid, address;
    private String dbDriver = "com.mysql.cj.jdbc";
    private String userName, password;
    protected Connection conn = null;
    protected ResultSet res = null;
    protected PreparedStatement pstmt = null;

    private String[] title = {"支行管理", "员工管理", "客户管理", "账户管理", "贷款管理", "业务统计"};

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
        tp.add(brMng.panelBranch);
        tp.add(stMng.panelStaff);
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
            //TODO: handle exception
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(dbURL, userName, password);
        } catch (SQLException e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }

    public void closeAll() throws SQLException {
        if (res != null) {
            try {
                res.close();
            } catch (SQLException e) {
                //TODO: handle exception
                e.printStackTrace();
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //TODO: handle exception
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                //TODO: handle exception
                e.printStackTrace();
            }
        }
    }

    public void loginFunc() {
        JTextField urlField = new JTextField(15);
        JTextField portField = new JTextField(15);
//		JTextField sidField = new JTextField(15);
        JCheckBox usedeaultField = new JCheckBox("使用内置服务器", Boolean.TRUE);
        JTextField userField = new JTextField(15);
        JTextField passwdField = new JTextField(15);

        JPanel myPanel = new JPanel();
        myPanel.setPreferredSize(new Dimension(100, 250));


        myPanel.add(usedeaultField);
        myPanel.add(new JLabel("DataBase URL:"));
        myPanel.add(urlField);
        myPanel.add(new JLabel("Port:"));
        myPanel.add(portField);
//		myPanel.add(Box.createVerticalStrut(2)); // a spacer
//		myPanel.add(new JLabel("SID:"));
//		myPanel.add(sidField);
        myPanel.add(Box.createVerticalStrut(2)); // a spacer
        myPanel.add(new JLabel("User Name:"));
        myPanel.add(userField);
        myPanel.add(Box.createVerticalStrut(2)); // a spacer
        myPanel.add(new JLabel("Password:"));
        myPanel.add(passwdField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, "Log In", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {

            if (usedeaultField.isSelected()) {
                port = "33060";
                address = "192.168.174.146";
                userName = "root";
                password = "totoroyyw";
            } else {
                address = urlField.getText();
                port = portField.getText();
//				sid = sidField.getText();
                userName = userField.getText();
                password = passwdField.getText();
            }
//			sid = "orcl"; // ?
            dbURL = "jdbc:mysql://" + address + ":" + port + "/lab3?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            System.out.println(dbURL);
        }
        if (userName != null && password != null && dbURL != null) {
            try {
                getConnection();
                login.setEnabled(false);
                logout.setEnabled(true);
                brMng.setConn(conn);
                stMng.setConn(conn);
                clMng.setConn(conn);
                acMng.setConn(conn);
                deMng.setConn(conn);
                buMng.setConn(conn);
                brMng.pBrInit();
                stMng.pStInit();
                clMng.pClInit();
                acMng.pAcInit();
                deMng.pDeInit();
                buMng.pBuInit();
            } catch (SQLException sqle) {
                //TODO: handle exception
                JOptionPane.showMessageDialog(null, "连接失败！\n请重试！", "错误！", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void logoutFunc() {
        try {
            closeAll();
            JOptionPane.showMessageDialog(null, "登出成功！", "成功！", JOptionPane.PLAIN_MESSAGE);
            login.setEnabled(true);
            logout.setEnabled(false);
            brMng.panelBranch.removeAll();
            stMng.panelStaff.removeAll();
            clMng.panelClient.removeAll();
            acMng.panelAccount.removeAll();
            deMng.panelDebt.removeAll();
            buMng.panelBusiness.removeAll();
        } catch (SQLException e) {
            //TODO: handle exception
            JOptionPane.showMessageDialog(null, "登出失败！\n请重试！", "错误！", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void menuInit() {
        this.setJMenuBar(menuBar);    //添加菜单栏
        menuBar.add(menuFile);      //添加 “文件” 菜单
        login.addActionListener(this);
        menuFile.add(login);
        logout.addActionListener(this);
        logout.setEnabled(false);
        menuFile.add(logout);
        itemExit.addActionListener(this);
        menuFile.add(itemExit);
    }

    public void draw() {
        brMng = new BranchManager();
        stMng = new StaffManager();
        clMng = new ClientManager();
        acMng = new AccountManager();
        deMng = new DebtManager();
        buMng = new BusinessManager();
        tabInit();
        menuInit();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//点X关闭窗口
        setLocation(300, 200); //初始化时定位
        setSize(1000, 650);
        //setBackground(deepGrey);
        setResizable(true);   //禁止拖曳改变窗口大小
        setVisible(true);  //显示窗口
    }

    public static void main(String args[]) {
        new BankDBManager();
    }
}
