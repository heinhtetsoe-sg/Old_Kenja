package nao_package;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class CalendarFrame extends JDialog {
  JPanel contentPane;
  BorderLayout borderLayout = new BorderLayout();
  CalendarModel calendarModel;
  JPanel hedPanel = new JPanel();   // 次月・前月ボタンなどが入るパネル
  JPanel bottomPanel = new JPanel();    // OK, Cancel
  
  JScrollPane jScrollPane = new JScrollPane();
  TitledBorder titledBorder1;
  JTable jTable = new JTable();
  JTableHeader header = jTable.getTableHeader();
  
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JTextField YearField = new JTextField();
  JTextField MonthField = new JTextField();
  JButton beforeMonthButton = new JButton();
  JButton nextMonthButton = new JButton();
  JButton beforeYearButton = new JButton();
  JButton nextYearButton = new JButton();
  
  JButton submitButton = new JButton("OK");
  JButton cancelButton = new JButton("Cancel");
  //
  public Date ymd;

  //フレームの構築
  public CalendarFrame() {
    this(new java.util.Date(), null, false);
  }

  public CalendarFrame(Frame parent, boolean modal) {
    this(new java.util.Date(), parent, modal);
  }

  public CalendarFrame(java.util.Date ddd, Frame parent, boolean modal) {
    super(parent, modal);
    ymd = ddd;
    calendarModel = new CalendarModel(ymd);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
      fieldInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  // 日付をセットする。
  public void setDate( Date ddd ){
    ymd = ddd;
    calendarModel = new CalendarModel(ymd);
    jTable.setModel( calendarModel );
    // 日曜は赤、土曜は青のフォントで
    DefaultTableColumnModel dtcm = (DefaultTableColumnModel)jTable.getColumnModel();    // カラムモデルの抽出
    for( int i=0; i<jTable.getColumnCount(); i++ ){
        TableColumn tColumn = dtcm.getColumn(i);
        tColumn.setCellRenderer( new DayRenderer2() );
    }
    fieldInit();
    this.update(this.getGraphics()); // フレームの再描画
  }

  // 日付を返す
  public java.util.Date getDate(){
    return ymd;
  }

  //コンポーネントの初期化
  private void jbInit() throws Exception  {
    contentPane = (JPanel) this.getContentPane();
    titledBorder1 = new TitledBorder("");
    contentPane.setLayout(borderLayout);
    this.setSize(new Dimension(400, 320));
    this.setTitle("カレンダー");

    hedPanel.setMaximumSize(new Dimension(32767, 100));
    hedPanel.setMinimumSize(new Dimension(10, 100));
    hedPanel.setPreferredSize(new Dimension(10, 30));
    jScrollPane.setMinimumSize(new Dimension(24, 200));
    jScrollPane.setPreferredSize(new Dimension(4, 200));
    MonthField.setMinimumSize(new Dimension(50, 21));
    MonthField.setPreferredSize(new Dimension(50, 21));
    MonthField.setEditable(false);
    jLabel1.setText("年");
    jLabel2.setText("月");
    
    // 前月移動ボタン
    beforeMonthButton.setBorder(BorderFactory.createRaisedBevelBorder());
    beforeMonthButton.setText("　< 前月　");
    beforeMonthButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        beforeMonth_actionPerformed(e);
      }
    });

    // 来月移動ボタン
    nextMonthButton.setBorder(BorderFactory.createRaisedBevelBorder());
    nextMonthButton.setText("　次月 >　");
    nextMonthButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nextMonth_actionPerformed(e);
      }
    });

    // 前年移動ボタン
    beforeYearButton.setBorder(BorderFactory.createRaisedBevelBorder());
    beforeYearButton.setText("<< 前年");
    beforeYearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        beforeYearButton_actionPerformed(e);
      }
    });

    // 来年移動ボタン
    nextYearButton.setBorder(BorderFactory.createRaisedBevelBorder());
    nextYearButton.setText("次年 >>");
    nextYearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nextYearButton_actionPerformed(e);
      }
    });

    // OKボタン押下
    submitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        submit();
      }
    });

    // キャンセルボタン押下
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hide();
      }
    });

    contentPane.setPreferredSize(new Dimension(10, 30));

    header.setReorderingAllowed(false);    // 列の順序を不許可に
    YearField.setMinimumSize(new Dimension(50, 21));
    YearField.setPreferredSize(new Dimension(50, 21));
    YearField.setEditable(false);
    jTable.setModel(calendarModel);

    // takaesu
    jTable.setCellSelectionEnabled(true);   // takaesu
    jTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    jTable.addMouseListener( new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent evnt){
            // ダブルクリックされたか？
            if( evnt.getClickCount()==2 ){
                submit();
            }
        }
        
    });
    
    jTable.setRowHeight(40);
    contentPane.add(jScrollPane, BorderLayout.CENTER);
    jScrollPane.getViewport().add(jTable, null);
    contentPane.add(hedPanel, BorderLayout.NORTH);
    hedPanel.add(beforeYearButton, null);
    hedPanel.add(beforeMonthButton, null);
    hedPanel.add(YearField, null);
    hedPanel.add(jLabel1, null);
    hedPanel.add(MonthField, null);
    hedPanel.add(jLabel2, null);
    hedPanel.add(nextMonthButton, null);
    hedPanel.add(nextYearButton, null);

    bottomPanel.add(submitButton);
    bottomPanel.add(cancelButton);
    contentPane.add(bottomPanel, BorderLayout.SOUTH);

    // 日曜は赤、土曜は青のフォントで
    DefaultTableColumnModel dtcm = (DefaultTableColumnModel)jTable.getColumnModel();    // カラムモデルの抽出
    for( int i=0; i<jTable.getColumnCount(); i++ ){
        TableColumn tColumn = dtcm.getColumn(i);
        tColumn.setCellRenderer( new DayRenderer2() );
    }

  }


  //ウィンドウが閉じられたときに終了するようにオーバーライド
/***
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      System.exit(0);
    }
  }
***/

  /**
 * 
 */
protected void submit() {
    // 選択された行、列をもとに年月日を得る
    int row, col;
    row = jTable.getSelectedRow();
    col = jTable.getSelectedColumn();
    ymd = (Date)calendarModel.getValueAt(row, col);
    // ダイアログを非表示
    hide();
}

protected void fieldInit() {
    // 注：getYear()、getMonth()はCalendarModel独自に定義されたメソッドです。
    YearField.setText(String.valueOf(((CalendarModel )calendarModel).getYear()));
    MonthField.setText(String.valueOf(((CalendarModel )calendarModel).getMonth()));
  }

  void nextMonth_actionPerformed(ActionEvent e) {
    calendarModel.addMonth(1);
    fieldInit();
    this.update(this.getGraphics()); // フレームの再描画
  }

  void beforeMonth_actionPerformed(ActionEvent e) {
    calendarModel.addMonth(-1);
    fieldInit();
    this.update(this.getGraphics()); // フレームの再描画
  }

  void beforeYearButton_actionPerformed(ActionEvent e) {
      calendarModel.addMonth(-12);
      fieldInit();
      this.update(this.getGraphics()); // フレームの再描画
    }

  void nextYearButton_actionPerformed(ActionEvent e) {
      calendarModel.addMonth(12);
      fieldInit();
      this.update(this.getGraphics()); // フレームの再描画
  }

  // モーダレスにしない。（継承メソッドのオーバーライド）
  // takaesu
  public boolean isModal(){
    return true;
  }

    /*
     * 日付型から日だけをセットするレンダラ
     */
    class DayRenderer2 extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row, int column )
        {
            if( column==0 ){
                setForeground(Color.red);
            }
            else if( column==6 ){
                setForeground(Color.blue);
            }

            setHorizontalAlignment(SwingConstants.CENTER);

            return( super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) );
        }

        public void setValue( Object value ){
            SimpleDateFormat sdf = new SimpleDateFormat("dd");
            super.setText( sdf.format((Date)value) );
        }
    }
}

