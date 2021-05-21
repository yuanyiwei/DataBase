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
import java.util.List;

public class DebtManager extends JFrame implements ActionListener {
	public JPanel panelDebt;
	private JLabel condLabel;
	private JTextField condText;
	private JTable resTable;
	private JLabel[] paraLabel;
	private JComboBox[] paraSymBox;
	private JTextField[] paraText;
	
	private JButton insertBtn, deleteBtn, sendBtn, searchBtn;

	protected Connection conn = null;
	protected PreparedStatement pstmt = null;
	protected Statement stmt = null;
	protected ResultSet res = null;
	private String[] errMsgs = {
		"无数据库连接，请先连接至数据库！",
		"请先选中贷款项！",
		"请只选中一项！",
		"不可删除发放中的贷款！"
	};
	private String[] infoMsgs = {
		"查询无结果"
	};
	private String dbName = "贷款";
	private String[] labelName = {"贷款号：","支行名：","金额：", "日期：", "身份证号："};
	private String[] colName = {"", "贷款号","支行名","金额", "日期", "身份证号", "发放状态"};
	private String[] syms = {"=", ">", "<", ">=", "<=", "<>"};
	private Object[][] data = {{new Boolean(false), "", "", "", ""}};
	private int INSERT = 1, DELETE = 2, UPDATE = 3, SEARCH = 4;
	private Object[][] oldTable;
	int length = labelName.length;
	private String[] status = {"未开始发放", "发放中", "已全部发放"};
	
	public DebtManager() {
		panelDebt = new JPanel();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == insertBtn ) {
			String[] info = insertInfo();
			String newRow = info[0];
			String paraNames = "";
			for (int i = 0; i < length - 1; i ++) {
				paraNames += colName[i + 1];
				if (i < length - 2) {
					paraNames += ",";
				}
			}
			if (newRow != null) {
				String asql = "Insert Into " + dbName + " (" + paraNames + ") Values (" + newRow + ")";
				try {
					ResultSet aRSet = exeSQL(conn, asql, INSERT);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					showError(e1.getMessage());
				}
				paraNames = colName[1] + "," + colName[length];
				asql = "Insert Into 拥有贷款 (" + paraNames + ") Values (" + info[1] + ")";
				try {
					ResultSet aRSet = exeSQL(conn, asql, INSERT);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					showError(e1.getMessage());
				}
			}
		}
		if (e.getSource() == deleteBtn) {
			int rowCnt = resTable.getRowCount();
			for (int i = 0; i < rowCnt; i ++) {
				String asql;
				if (resTable.getValueAt(i, 6).equals("发放中")) {
					showError(3);
					continue;
				}
				if (resTable.getValueAt(i, 0).equals(true)) {
					asql = "Delete From 支付情况  Where 贷款号 = '" + resTable.getValueAt(i, 1) + "'";
					try {
						exeSQL(conn, asql, DELETE);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						showError(e1.getMessage());
					}
					asql = "Delete From 拥有贷款  Where 贷款号 = '" + resTable.getValueAt(i, 1) + "'";
					try {
						exeSQL(conn, asql, DELETE);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						showError(e1.getMessage());
					}
					asql = "Delete From " + dbName + "  Where 贷款号 = '" + resTable.getValueAt(i, 1) + "'";
					try {
						exeSQL(conn, asql, DELETE);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						showError(e1.getMessage());
					}
				}
			}
			initTable(resTable, data, colName);
		}
		if (e.getSource() == searchBtn) {
			String cond = "";
			for (int i = 0; i < length - 2; i ++) {
				if (!paraText[i].getText().equals("")) {
					if (!cond.equals(""))  {
						cond += " and ";
					}
					if (i == 3) {
						cond += colName[i + 1] + syms[paraSymBox[i].getSelectedIndex()] + "to_date('" + paraText[i].getText() + "', 'yyyy/mm/dd')";
					}
					else {
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
				asql = "select * from " + dbName+ ", 拥有贷款 where " + dbName
						+ ".贷款号 = 拥有贷款.贷款号";
			}
			else {
				asql = "select * from " + dbName + " where " + ", 拥有贷款" + " where "
						+ dbName + ".贷款号 = 拥有贷款.贷款号 and " + cond;
			}
			try {
				ResultSet aRSet = exeSQL(conn, asql, SEARCH);
				List<Object[]> resList = new ArrayList<Object[]>();
				while(aRSet != null && aRSet.next()) {
					Object[] line = new Object[length + 2];
					line[0] = new Boolean(false);
					for (int j = 1; j < line.length - 2; j ++) {
						line[j] = aRSet.getObject(j);
					}
					line[length] = aRSet.getObject(length + 1);
					String moneysql = "select sum(金额) from 支付情况 where 支付情况.贷款号 = '" + aRSet.getObject(1) + "'";
					ResultSet moneyres = exeSQL(conn, moneysql, SEARCH);
					moneyres.next();
					double sendmoney = moneyres.getDouble(1);
					double total = Double.parseDouble(line[3].toString());
					System.out.println(sendmoney + " " + total);
					if (sendmoney == 0) {
						line[length + 1] = status[0];
					}
					else if (sendmoney < total) {
						line[length + 1] = status[1];
					}
					else {
						line[length + 1] = status[2];
					}
					resList.add(line);
				}
				try {
					Object[][] res = new Object[resList.size()][length + 1];
					resList.toArray(res);
					if (resList.size() == 0) {
						initTable(resTable, data, colName);
					}
					else {
						initTable(resTable, res, colName);
					}
				} catch (NullPointerException e1) {
					showInfo(0);
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				showError(e1.getMessage());
			}
			for (int i = 0; i < length; i ++) {
				paraText[i].setText("");
			}
			paraText[3].setText("yyyy/mm/dd");
			condText.setText("");
	        for (int i = 0; i < resTable.getRowCount(); i ++) {
	        	if (resTable.getValueAt(i, 1).equals("")) {
	        		break;
	        	}
	        	for (int j = 0; j < resTable.getColumnCount(); j ++) {
	        		oldTable[i][j] = resTable.getValueAt(i, j);
	        	}
	        }
		}
		if (e.getSource() == sendBtn) {
			sendMoney();
		}
	}

	public String[] insertInfo() {
		String[] info = new String[2];
		JLabel[] nparaLabel = new JLabel[length];
		JTextField[] nparaText = new JTextField[length];
		String newRow = "";
		String[] paras = {"", "", "", "", "", "", "", ""};

		JPanel myPanel = new JPanel();
		myPanel.setPreferredSize(new Dimension(250, 400));
		for (int i = 0; i < length; i ++) {
			nparaLabel[i] = new JLabel(labelName[i]);
			nparaText[i] = new JTextField(20);
			myPanel.add(nparaLabel[i]);
			myPanel.add(Box.createVerticalStrut(2)); // a spacer
			myPanel.add(nparaText[i]);
		}
		nparaText[3].setText("yyyy/mm/dd");
		nparaText[3].addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				if (nparaText[3].getText().equals("yyyy/mm/dd")) {
					nparaText[3].setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (nparaText[3].getText().equals("")) {
					nparaText[3].setText("yyyy/mm/dd");
				}
			}
			
		});

		int result = JOptionPane.showConfirmDialog(null, myPanel, "输入要插入的行的信息：", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			for (int i = 0; i < length - 1; i ++) {
				paras[i] = nparaText[i].getText();
				if (i == 3) {
					newRow += "to_date('" + paras[i] + "', 'yyyy/mm/dd')";
				}
				else {
					newRow += "'" + paras[i] + "'";
				}
				if (i < length - 2) {
					newRow += ",";
				}
			}
			info[0] = newRow;
			info[1] = "'" + nparaText[0].getText() + "','" + nparaText[length - 1].getText() + "'";
		}
		return info;
	}
	
	public void sendMoney() {
		int selNum = 0, selCnt = 0;
		int rowCnt = resTable.getRowCount();
		for (int i = 0; i < rowCnt; i ++) {
			if (resTable.getValueAt(i, 0).equals(true)) {
				selNum = i;
				selCnt ++;
			}
		}
		if (selCnt == 0) {
			showError(errMsgs[1]);
		}
		else if (selCnt > 1) {
			showError(errMsgs[2]);
		}
		else {
			String deltID = resTable.getValueAt(selNum, 1).toString();
			JTextField nDateField = new JTextField(15);
			JTextField nMoneyField = new JTextField(15);
			String asql = null, date = null, money = null;

			JPanel myPanel = new JPanel();
			myPanel.setPreferredSize(new Dimension(100, 200));
			myPanel.add(new JLabel("日期："));
			nDateField.setText("yyyy/mm/dd");
			nDateField.addFocusListener(new FocusListener() {

				@Override
				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub
					if (nDateField.getText().equals("yyyy/mm/dd")) {
						nDateField.setText("");
					}
				}

				@Override
				public void focusLost(FocusEvent e) {
					// TODO Auto-generated method stub
					if (nDateField.getText().equals("")) {
						nDateField.setText("yyyy/mm/dd");
					}
				}
				
			});
			myPanel.add(nDateField);
			myPanel.add(Box.createVerticalStrut(2)); // a spacer
			myPanel.add(new JLabel("金额："));
			myPanel.add(nMoneyField);

			int result = JOptionPane.showConfirmDialog(null, myPanel, "输入要插入的支付情况信息：", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				date = nDateField.getText();
				money = nMoneyField.getText();
			}
			if (date != null && money != null) {
				asql = "insert into 支付情况 (贷款号, 日期, 金额) Values('" + deltID + "', to_date('" + date + "', 'yyyy/mm/dd'), '" + money +"')";
				String moneysql = "select sum(金额) from 支付情况 where 支付情况.贷款号 = '" + resTable.getValueAt(selNum, 1) + "'";
				ResultSet moneyres;
				try {
					exeSQL(conn, asql, SEARCH);
					moneyres = exeSQL(conn, moneysql, SEARCH);
					moneyres.next();
					double sendmoney = moneyres.getDouble(1);
					double total = Double.parseDouble(resTable.getValueAt(selNum, 3).toString());
					String newstatus;
					if (sendmoney == 0) {
						newstatus = status[0];
					}
					else if (sendmoney < total) {
						newstatus = status[1];
					}
					else {
						newstatus = status[2];
					}
					oldTable[selNum][length + 1] = newstatus;
					initTable(resTable, oldTable, colName);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if (result == JOptionPane.OK_OPTION) {
				showError(3);
			}
		}
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
			if (mode == DELETE || mode == UPDATE) {
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				return null;
			}
			else {
				pstmt = conn.prepareStatement(sql);
				resSet = pstmt.executeQuery();
			}
		} catch (SQLException e) {
			//TODO: handle exception
			e.printStackTrace();
			showError(e.getMessage());
		}
		return resSet;
	}

	private void initTable(JTable table, Object[][] data, String[] colName) {   
        DefaultTableModel dtm = new DefaultTableModel(colName,0){
        	public boolean isCellEditable(int row, int col) {//设置账户号不允许修改
        		if (col == 1 || col == length) {
        			return false;
        		}
        		else {
        			return true;
        		}
        	}
        };
        for (int i = 0; i < data.length; i ++) {
            dtm.addRow(data[i]);
        }
           
        table.setModel(dtm);   
        TableColumnModel tcm = table.getColumnModel();   
  
        tcm.getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));   
        tcm.getColumn(0).setCellRenderer(new MyTableRenderer());   
  
        tcm.getColumn(0).setPreferredWidth(20);   
        tcm.getColumn(0).setWidth(100);   
        tcm.getColumn(0).setMaxWidth(100);
        for (int i = 1; i < colName.length; i ++) {
        	tcm.getColumn(i).setWidth(100);
        }
        oldTable = new Object[data.length][data[0].length];
        for (int i = 0; i < data.length; i ++) {
        	for (int j = 0; j < data[i].length; j ++) {
        		oldTable[i][j] = data[i][j];
        	}
        }
    }   
	
	private class MyTableRenderer extends JCheckBox implements TableCellRenderer {   
        public Component getTableCellRendererComponent( JTable table,   
                Object value,   
                boolean isSelected,   
                boolean hasFocus,   
                int row,   
                int column ) {   
            Boolean b = (Boolean) value;   
            this.setSelected(b.booleanValue());   
            return this;   
        }   
	} 
	
	public void pDeInit() {
		paraLabel = new JLabel[length];
		paraText = new JTextField[length];
		paraSymBox = new JComboBox[length];
		panelDebt.setLayout(new FlowLayout(FlowLayout.LEFT));
		for (int i = 0; i < length; i ++) {
			paraLabel[i] = new JLabel(labelName[i]);
			paraLabel[i].setPreferredSize(new Dimension(120, 25));
			paraLabel[i].setOpaque(true);
			paraLabel[i].setFont(new Font("Dialog", 1, 16));
			panelDebt.add(paraLabel[i]);
			paraSymBox[i] = new JComboBox(syms);
			panelDebt.add(paraSymBox[i]);
			paraText[i] = new JTextField(10);
			paraText[i].setPreferredSize(new Dimension(100, 25));
			paraText[i].setOpaque(true);
			panelDebt.add(paraText[i]);
		}
		paraText[3].setText("yyyy/mm/dd");
		paraText[3].addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				if (paraText[3].getText().equals("yyyy/mm/dd")) {
					paraText[3].setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (paraText[3].getText().equals("")) {
					paraText[3].setText("yyyy/mm/dd");
				}
			}
			
		});
		condLabel = new JLabel("其他条件：");
		condText = new JTextField(80);
		condLabel.setPreferredSize(new Dimension(100, 25));
		condLabel.setOpaque(true);
		condLabel.setFont(new Font("Dialog", 1, 18));
		panelDebt.add(condLabel);
		condText.setOpaque(true);
		condText.setPreferredSize(new Dimension(800, 25));
		panelDebt.add(condText);
		//功能
		JPanel funcPane = new JPanel();
		funcPane.setPreferredSize(new Dimension(970, 50));
		funcPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		funcPane.setOpaque(true);
		panelDebt.add(funcPane);
		//添加
		insertBtn = new JButton("添加");
		insertBtn.setPreferredSize(new Dimension(235, 40));
		insertBtn.setOpaque(true);
		insertBtn.addActionListener(this);
		funcPane.add(insertBtn);
		insertBtn.setFont(new Font("Dialog", 1, 30));
		//删除
		deleteBtn = new JButton("删除");
		deleteBtn.setPreferredSize(new Dimension(235, 40));
		deleteBtn.setOpaque(true);
		deleteBtn.addActionListener(this);
		funcPane.add(deleteBtn);
		deleteBtn.setFont(new Font("Dialog", 1, 30));
		//查询
		searchBtn = new JButton("查询");
		searchBtn.setPreferredSize(new Dimension(235, 40));
		searchBtn.setOpaque(true);
		searchBtn.addActionListener(this);
		funcPane.add(searchBtn);
		searchBtn.setFont(new Font("Dialog", 1, 30));
		//发放贷款
		sendBtn = new JButton("发放");
		sendBtn.setPreferredSize(new Dimension(235, 40));
		sendBtn.setOpaque(true);
		sendBtn.addActionListener(this);
		funcPane.add(sendBtn);
		sendBtn.setFont(new Font("Dialog", 1, 30));
		//结果表格
		resTable = new JTable();
		//resTable.setPreferredSize(new Dimension(500, 500));
		initTable(resTable, data, colName);
		resTable.setOpaque(true);
		resTable.setFont(new Font("Dialog", 1, 18));
		JScrollPane resPane = new JScrollPane(resTable);
		resPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resPane.setPreferredSize(new Dimension(970, 400));
		panelDebt.add(resPane);
	}

}