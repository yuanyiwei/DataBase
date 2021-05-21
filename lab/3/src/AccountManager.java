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

public class AccountManager extends JFrame implements ActionListener {
	public JPanel panelAccount;
	private JLabel condLabel;
	private JTextField condText;
	private JTable resTable;
	private JLabel[] paraLabel;
	private JComboBox[] paraSymBox;
	private JTextField[] paraText;
	private JLabel catLabel;
	private JComboBox catBox;
	private JButton insertBtn, deleteBtn, updateBtn, searchBtn;

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
	private String catStr = "账户类型";
	private String[] dbName = {"储蓄账户", "支票账户"};
	private String[][] labelName = {{"账户号", "账户类型", "余额", "开户日期", "最近访问日期", "利率", "货币类型", "支行名：", "身份证号："},
									{"账户号", "账户类型", "余额", "开户日期", "最近访问日期", "透支额", "支行名：", "身份证号："}};
	private String[][] colName = {{"", "账户号", "账户类型", "余额", "开户日期", "最近访问日期", "利率", "货币类型", "支行名", "身份证号"},
								{"", "账户号", "账户类型", "余额", "开户日期", "最近访问日期", "透支额", "支行名", "身份证号"}};
	private String[] syms = {"=", ">", "<", ">=", "<=", "<>"};
	private Object[][][] data = {{{new Boolean(false), "", "", "", "", "", "", ""}},
								{{new Boolean(false), "", "", "", "", "", ""}}};
	private int INSERT = 1, DELETE = 2, UPDATE = 3, SEARCH = 4;
	private Object[][] oldTable;
	private int catSel = 0;
	private int length = labelName[catSel].length;
	
	public AccountManager() {
		panelAccount = new JPanel();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == insertBtn ) {
			String[] info = insertInfo();
			String newRow = info[0];
			String paraNames = "";
			for (int i = 0; i < length - 2; i ++) {
				paraNames += colName[catSel][i + 1];
				if (i < length - 3) {
					paraNames += ",";
				}
			}
			if (newRow != null) {
				String asql = "Insert Into " + dbName[catSel] + " (" + paraNames + ") Values (" + newRow + ")";
				try {
					ResultSet aRSet = exeSQL(conn, asql, INSERT);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					showError(e1.getMessage());
				}
				paraNames = colName[catSel][length - 1] + "," + colName[catSel][1] + ", " + colName[catSel][2] + "," + colName[catSel][length];
				asql = "Insert Into 拥有账户 (" + paraNames + ") Values (" + info[1] + ")";
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
				if (resTable.getValueAt(i, 0).equals(true)) {
					asql = "Delete From 拥有账户  Where 账户号 = '" + resTable.getValueAt(i, 1) + "'";
					try {
						exeSQL(conn, asql, DELETE);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						showError(e1.getMessage());
					}
					asql = "Delete From " + dbName[catSel] + "  Where 账户号 = '" + resTable.getValueAt(i, 1) + "'";
					try {
						exeSQL(conn, asql, DELETE);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						showError(e1.getMessage());
					}
				}
			}
			initTable(resTable, data[catSel], colName[catSel]);
		}
		if (e.getSource() == updateBtn) {
			int rowCnt = resTable.getRowCount();
			int colCnt = resTable.getColumnCount() - 2;
			for (int i = 0; i < rowCnt; i ++) {
				String asql, cond = "", change = "";
				for (int j = 0; j < colCnt - 1; j ++) {
					if (j == 4 || j == 5) {
						cond += colName[catSel][j + 1] + " = " + "to_date('" + oldTable[i][j + 1].toString().substring(0, 10) + "', 'yyyy/mm/dd')";
						change += colName[catSel][j + 1] + " = " + "to_date('" + resTable.getValueAt(i, j + 1).toString().substring(0, 10) + "', 'yyyy/mm/dd')";
					}
					else {
						if (oldTable[i][j + 1] == null || oldTable[i][j + 1].equals("")) {
							cond += colName[catSel][j + 1] + " is null ";
						}
						else {
							cond += colName[catSel][j + 1] + " = '" + oldTable[i][j + 1] + "' ";
						}
						change += colName[catSel][j + 1] + " = '" + resTable.getValueAt(i, j + 1) + "' ";
					}
					if (j < colCnt - 2) {
						cond += " and ";
						change += ",";
					}
				}
				if (resTable.getValueAt(i, 0).equals(true)) {
					asql = "Update " + dbName[catSel] + " Set " + change + " Where " + cond;
					try {
						exeSQL(conn, asql, DELETE);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						showError(e1.getMessage());
					}
				}
			}
			initTable(resTable, data[catSel], colName[catSel]);
		}
		if (e.getSource() == searchBtn) {
			String cond = "";
			for (int i = 0; i < length; i ++) {
				if (!paraText[i].getText().equals("") && !paraText[i].getText().equals("yyyy/mm/dd")) {
					if (!cond.equals(""))  {
						cond += " and ";
					}
					if (i == 3 || i == 4) {
						cond += colName[catSel][i + 1] + syms[paraSymBox[i].getSelectedIndex()] + "to_date('" + paraText[i].getText() + "', 'yyyy/mm/dd')";
					}
					else {
						cond += colName[catSel][i + 1] + syms[paraSymBox[i].getSelectedIndex()] + " '" + paraText[i].getText() + "'";
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
				asql = "select * from " + dbName[catSel] + ", 拥有账户 where " + dbName[catSel]
						+ ".账户号 = 拥有账户.账户号 and " + dbName[catSel] + ".账户类型 = 拥有账户.账户类型";
			}
			else {
				asql = "select * from " + dbName[catSel] + ", 拥有账户" + " where " + dbName[catSel]
						+ ".账户号 = 拥有账户.账户号 and " + dbName[catSel] + ".账户类型 = 拥有账户.账户类型 and " + cond;
			}
			try {
				ResultSet aRSet = exeSQL(conn, asql, SEARCH);
				List<Object[]> resList = new ArrayList<Object[]>();
				while(aRSet != null && aRSet.next()) {
					Object[] line = new Object[length + 1];
					line[0] = new Boolean(false);
					for (int j = 1; j < line.length; j ++) {
						line[j] = aRSet.getObject(j);
					}
					resList.add(line);
				}
				try {
					Object[][] res = new Object[resList.size()][length + 1];
					resList.toArray(res);
					if (resList.size() == 0) {
						initTable(resTable, data[catSel], colName[catSel]);
					}
					else {
						initTable(resTable, res, colName[catSel]);
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
			paraText[4].setText("yyyy/mm/dd");
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
	}

	public String[] insertInfo() {
		String[] info = new String[2];
		JLabel[] nparaLabel = new JLabel[length];
		JTextField[] nparaText = new JTextField[length];
		String newRow = "";
		String[] paras = new String[length];

		JPanel myPanel = new JPanel();
		myPanel.setPreferredSize(new Dimension(250, 425));
		for (int i = 0; i < length; i ++) {
			nparaLabel[i] = new JLabel(labelName[catSel][i]);
			nparaText[i] = new JTextField(20);
			myPanel.add(nparaLabel[i]);
			myPanel.add(Box.createVerticalStrut(2)); // a spacer
			myPanel.add(nparaText[i]);
		}
		nparaText[1].setText(dbName[catSel]);
		nparaText[1].setEditable(false);
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
		nparaText[4].setText("yyyy/mm/dd");
		nparaText[4].addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				if (nparaText[4].getText().equals("yyyy/mm/dd")) {
					nparaText[4].setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (nparaText[4].getText().equals("")) {
					nparaText[4].setText("yyyy/mm/dd");
				}
			}
			
		});
		
		int result = JOptionPane.showConfirmDialog(null, myPanel, "输入要插入的行的信息：", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			for (int i = 0; i < length - 2; i ++) {
				paras[i] = nparaText[i].getText();
				if (i == 3 || i == 4) {
					newRow += "to_date('" + paras[i] + "', 'yyyy/mm/dd')";
				}
				else {
					newRow += "'" + paras[i] + "'";
				}
				if (i < length - 3) {
					newRow += ",";
				}
			}
			info[0] = newRow;
			info[1] = "'" + nparaText[length - 2].getText() + "','" + nparaText[0].getText() + "','" 
			+ nparaText[1].getText() + "','" + nparaText[length - 1].getText() + "'";
		}
		return info;
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
        DefaultTableModel dtm = new DefaultTableModel(colName,0) {
        	public boolean isCellEditable(int row, int col) {//设置账户号不允许修改
        		if (col == 1 || col == 2 || col == length - 1 || col == length) {
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
	
	public void pAcInit() {
		panelAccount.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel space;
		catLabel = new JLabel(catStr);
		catLabel.setPreferredSize(new Dimension(100, 25));
		catLabel.setOpaque(true);
		catLabel.setFont(new Font("Dialog", 1, 18));
		catBox = new JComboBox(Arrays.copyOfRange(dbName, 0, dbName.length));
		catBox.setSelectedIndex(catSel);
		catBox.addItemListener(e->{
			catSel = catBox.getSelectedIndex();
			length = labelName[catSel].length;
			panelAccount.removeAll();
			pAcInit();
		});
		panelAccount.add(catLabel);
		panelAccount.add(catBox);
		space = new JLabel("");
		space.setPreferredSize(new Dimension(700, 25));
		panelAccount.add(space);
		paraLabel = new JLabel[length];
		paraText = new JTextField[length];
		paraSymBox = new JComboBox[length];
		for (int i = 0; i < length; i ++) {
			paraLabel[i] = new JLabel(labelName[catSel][i]);
			paraLabel[i].setPreferredSize(new Dimension(120, 25));
			paraLabel[i].setOpaque(true);
			paraLabel[i].setFont(new Font("Dialog", 1, 16));
			panelAccount.add(paraLabel[i]);
			paraSymBox[i] = new JComboBox(syms);
			panelAccount.add(paraSymBox[i]);
			paraText[i] = new JTextField(10);
			paraText[i].setPreferredSize(new Dimension(100, 25));
			paraText[i].setOpaque(true);
			panelAccount.add(paraText[i]);
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
		paraText[4].setText("yyyy/mm/dd");
		paraText[4].addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				if (paraText[4].getText().equals("yyyy/mm/dd")) {
					paraText[4].setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (paraText[4].getText().equals("")) {
					paraText[4].setText("yyyy/mm/dd");
				}
			}
			
		});
		condLabel = new JLabel("其他条件：");
		condText = new JTextField(80);
		condLabel.setPreferredSize(new Dimension(100, 25));
		condLabel.setOpaque(true);
		condLabel.setFont(new Font("Dialog", 1, 18));
		panelAccount.add(condLabel);
		condText.setOpaque(true);
		condText.setPreferredSize(new Dimension(800, 25));
		panelAccount.add(condText);
		//功能
		JPanel funcPane = new JPanel();
		funcPane.setPreferredSize(new Dimension(970, 50));
		funcPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		funcPane.setOpaque(true);
		panelAccount.add(funcPane);
		//添加
		insertBtn = new JButton("开户");
		insertBtn.setPreferredSize(new Dimension(235, 40));
		insertBtn.setOpaque(true);
		insertBtn.addActionListener(this);
		funcPane.add(insertBtn);
		insertBtn.setFont(new Font("Dialog", 1, 30));
		//删除
		deleteBtn = new JButton("销户");
		deleteBtn.setPreferredSize(new Dimension(235, 40));
		deleteBtn.setOpaque(true);
		deleteBtn.addActionListener(this);
		funcPane.add(deleteBtn);
		deleteBtn.setFont(new Font("Dialog", 1, 30));
		//修改
		updateBtn = new JButton("修改");
		updateBtn.setPreferredSize(new Dimension(235, 40));
		updateBtn.setOpaque(true);
		updateBtn.addActionListener(this);
		funcPane.add(updateBtn);
		updateBtn.setFont(new Font("Dialog", 1, 30));
		//查询
		searchBtn = new JButton("查询");
		searchBtn.setPreferredSize(new Dimension(235, 40));
		searchBtn.setOpaque(true);
		searchBtn.addActionListener(this);
		funcPane.add(searchBtn);
		searchBtn.setFont(new Font("Dialog", 1, 30));
		//结果表格
		resTable = new JTable();
		//resTable.setPreferredSize(new Dimension(500, 500));
		initTable(resTable, data[catSel], colName[catSel]);
		resTable.setOpaque(true);
		resTable.setFont(new Font("Dialog", 1, 18));
		JScrollPane resPane = new JScrollPane(resTable);
		resPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resPane.setPreferredSize(new Dimension(970, 340));
		panelAccount.add(resPane);
	}

}