package servletpack.KNJA;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.pdf.AlpPdf;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ２３３Ｃ＞  講座別名列
 */

public class KNJA233C implements AlpPdf.IOutputPdf {
    
    private static final Log log = LogFactory.getLog(KNJA233C.class);
    
    private final String OUTPUT1 = "1";
    private static final String OUTPUT2 = "2";
//    private final String OUTPUT3 = "3";
//    private final String OUTPUT4 = "4";

    private boolean hasdata;
    private Param _param;
    
    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        
        //  ＳＶＦ作成処理
        IPdf ipdf = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            
            ipdf = SvfPdf.init(response.getOutputStream());

            outputPdf(ipdf, request);
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != ipdf) {
                ipdf.close(hasdata);
            }
        }
    }
    
    public void outputPdf(
            final IPdf ipdf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            _param = new Param(request, db2);

            hasdata = false;
            printMain(ipdf, db2);

        } catch (final Exception ex) {
            log.error("error! ", ex);
            throw ex;
        } finally {
            if (null != _param) {
                for (final Iterator it = _param._psMap.values().iterator(); it.hasNext();) {
                    final PreparedStatement ps = (PreparedStatement) it.next();
                    DbUtils.closeQuietly(ps);
                }
            }
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }

    private static String getFormattedHrClass(final String hrClass) {
        return NumberUtils.isDigits(hrClass) ? keta(3, Integer.parseInt(hrClass)) : StringUtils.defaultString(hrClass);
    }

    private static String keta(final int keta, final int attendno) {
        return StringUtils.repeat(" ", keta - String.valueOf(attendno).length()) + String.valueOf(attendno);
    }
    
    private static int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
            }
        }
        return ret;
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final IPdf svf, final DB2UDB db2) {
        final int maxCol = 2; //OUTPUT3.equals(param._output) ? 3 : OUTPUT4.equals(param._output) ? 1 : 2;
        final int maxGyo = 50; // OUTPUT4.equals(param._output) && "2".equals(param._output4AB) ? 48 : OUTPUT4.equals(param._output) ? 45 : 50;

        final List chairList = Chair.getChairList(db2, _param);

        final List columnAllList = new ArrayList();
        for (int chi = 0; chi < chairList.size(); chi++) {
            final Chair chair = (Chair) chairList.get(chi);

            final List chairStudentColumnList = getPageList(chair._studentList, maxGyo);
            for (int c = 0; c < Integer.parseInt(_param._kensuu); c++) {
                columnAllList.addAll(chairStudentColumnList);
            }
        }
        final List pageList = getPageList(columnAllList, maxCol);

        String formName = null;
        if (_param._output.equals(OUTPUT1)) {
//            formName = param._isTokyoto ?  "KNJA233_1_2" : "KNJA233_1";
//            formName += param._isPatternB ?  "B" : "";
//            formName += ".frm";
            formName = "KNJA233C_1.frm";
        } else if (_param._output.equals(OUTPUT2)) {
//            formName = param._isTokyoto ?  "KNJA233_2_2" : "KNJA233_2";
//            formName += param._isPatternB ?  "B" : "";
//            formName += ".frm";
            formName = "KNJA233C_2.frm";

//        } else if (param._output.equals(OUTPUT4)) {
//            if ("2".equals(param._output4AB)) {
//                formName = "KNJA233_5.frm";
//            } else {
//                formName = "KNJA233_4.frm";
//            }
//        } else { // param._output.equals(OUTPUT3)
//            final String width = param._width != null ? param._width : "25";
//            final String height = param._height != null ? param._height : "4";
//            formName = "KNJA233_3_" + height + width+ ".frm";
        }

        log.debug("form = " + formName);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List columnList = (List) pageList.get(pi);

            svf.VrSetForm(formName, 1);

            for (int ci = 0; ci < columnList.size(); ci++) {
                final List studentList = (List) columnList.get(ci);
                
                for (int li = 0; li < studentList.size(); li++) {
                    final Student student = (Student) studentList.get(li);
                    
                      svf.VrsOut("SUBCLASS" + String.valueOf(ci + 1), student._chair._chairname);

                      svf.VrsOut("STAFFNAME" + String.valueOf(ci + 1), student._chair._staffname);
                      
                      printStudent(svf, student._chair, ci + 1, li + 1, student);

                }
            }
            
            svf.VrEndPage(); //SVFフィールド出力
            hasdata = true;
        }
    }
    
    private void printStudent(final IPdf svf, final Chair chair, final int column, final int gyo, final Student student) {
        final String scolumn = String.valueOf(column);
        //  連番・組略称・出席番号・かな出力
        if (OUTPUT1.equals(_param._output)) { // || OUTPUT3.equals(param._output)) {
            svf.VrsOutn("NUMBER" + scolumn     ,gyo, String.valueOf(student._ban));
        } else {
            svf.VrsOutn("NUMBER" + scolumn     ,gyo, student._schregno);
            
//                if (OUTPUT4.equals(param._output) && "2".equals(param._output4AB)) {
//                    svf.VrsOut("CHAIR_CD", chair._attendclasscd);
//                } else {
            svf.VrsOut("CHAIRCD" + scolumn, chair._attendclasscd);
//                }
        }
        
//        if (OUTPUT3.equals(param._output)) {
//                if (param._isPatternB) {
//                    svf.VrsOutn("HR_NAME" + slen, gyo, keta(2, Integer.parseInt(student._grade)) + "-" + getFormattedHrClass(student._hrClass) + "-" + keta(3, Integer.parseInt(student._attendno)));
//                } else {
//            svf.VrsOutn("HR_NAME" + scolumn, gyo, student._hrNameabbv + "-" + keta(3, Integer.parseInt(student._attendno)));
//                }
//        } else {
//                if (param._isPatternB) {
//                    svf.VrsOutn("GRADE" + slen, gyo, keta(2, Integer.parseInt(student._grade)));
//                    svf.VrsOutn("CLASS" + slen, gyo, getFormattedHrClass(student._hrClass));
//                } else {
            svf.VrsOutn("HR_CLASS" + scolumn, gyo, student._hrNameabbv);
//            svf.VrsOutn("CLASS" + scolumn, gyo, student._hrClass);
//                }
            svf.VrsOutn("ATTENDNO" + scolumn, gyo, NumberUtils.isDigits(student._attendno) ? keta(3, Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno));
//        }
//        if (OUTPUT4.equals(param._output)) {
//            svf.VrsOut("HR_NAME", student._hrName);
//        }
        
        if ("1".equals(_param._huriganaOutput)) {
            final String nameKana = student._nameKana;
            final int kanaLen = getMS932ByteCount(nameKana);
//            if (OUTPUT4.equals(param._output)) {
//                final String field;
//                if (kanaLen <= 26) {
//                    field = "KANA1";
//                } else if (kanaLen <= 30) {
//                    field = "KANA2";
//                } else {
//                    field = "KANA3";
//                }
//                svf.VrsOutn(field, gyo, nameKana);
//            } else {
                final String field;
                if (_param._isTokyoto && kanaLen > 22) {
                    field = "KANA" + scolumn + "_2";
                } else {
                    field = "KANA" + scolumn;
                }
                svf.VrsOutn(field, gyo, nameKana);
            }
//        }
        svf.VrsOutn("MARK" + scolumn, gyo, student._sex); //NO001 男:空白、女:'*'
        
        final String name = StringUtils.defaultString(student._name);
        final int nameLen = getMS932ByteCount(name);
        //  生徒漢字・規則に従って出力
        final int idxSpace = name.indexOf("　");                  //空白文字の位置
//        if (OUTPUT4.equals(param._output)) {
//            String sei = "";
//            String mei = "";
//            String field1 = null;
//            String field2 = null;
//            if (idxSpace >= 0) {
//                sei = name.substring(0, idxSpace); // 姓
//                mei = name.substring(idxSpace + 1); // 名
//                if (sei.length() == 1) {
//                    field1 = "LNAME" + scolumn + "_2"; // 姓１文字
//                } else {
//                    field1 = "LNAME" + scolumn + "_1"; // 姓２文字以上
//                }
//                if (mei.length() == 1) {
//                    field2 = "FNAME" + scolumn + "_2"; // 名１文字
//                } else {
//                    field2 = "FNAME" + scolumn + "_1"; // 名２文字以上
//                }
//            }
//            if (nameLen <= 18 && sei.length() <= 4 && mei.length() <= 4 && null != field1 && null != field2) {
//                svf.VrsOutn(field1, gyo, sei);
//                svf.VrsOutn(field2, gyo, mei);
//            } else if (nameLen <= 20) {
//                svf.VrsOutn("NAME4", gyo, name);                   //空白がない
//            } else if (nameLen <= 30) {
//                svf.VrsOutn("NAME5", gyo, name);                   //空白がない
//            } else {
//                svf.VrsOutn("NAME6", gyo, name);                   //空白がない
//            }
//        } else {
            
            // 学籍番号表記
            String slenNo = "";
//            if (OUTPUT3.equals(param._output) && "1".equals(param._printSchregno)) {
//                svf.VrsOutn("SCHREGNO" + scolumn, gyo, student._schregno);
//                slenNo = "_2"; // 学籍番号表示用の氏名フィールド
//            }
            
            if (_param._isTokyoto && nameLen > 18) {
                svf.VrsOutn("NAME" + scolumn + "_2", gyo, name);
            } else if (idxSpace < 0) {
                svf.VrsOutn("NAME" + scolumn + slenNo, gyo, name);                 //空白がない
            } else {
                final String sei = name.substring(0, idxSpace);
                final String mei = name.substring(idxSpace + 1);
                if (0 <= mei.indexOf("　")) {
                    svf.VrsOutn("NAME" + scolumn + slenNo, gyo, name); // 空白が２つ以上
                } else {
                    final String field1;
                    final String field2;
                    if (sei.length() == 1) {
                        field1 = "LNAME" + scolumn + "_2";       //姓１文字
                    } else {
                        field1 = "LNAME" + scolumn + "_1";       //姓２文字以上
                    }
                    if (mei.length() == 1) {
                        field2 = "FNAME" + scolumn + "_2";       //名１文字
                    } else {
                        field2 = "FNAME" + scolumn + "_1";       //名２文字以上
                    }
                    svf.VrsOutn(field1 + slenNo, gyo, sei);
                    svf.VrsOutn(field2 + slenNo, gyo, mei);
                }
            }
//        }
    }
    
    private static class Student {
        private Chair _chair;
        private int _ban;
        private String _schregno;
        private String _name;
        private String _nameKana;
        private String _sex;
        private String _grade;
        private String _hrClass;
        private String _hrName;
        private String _hrNameabbv;
        private String _attendno;
        
        public static List getChairStudentList(final DB2UDB db2, final Param param, final Chair chair) {
            final List studentList = new ArrayList();
            ResultSet rs = null;
            try {
                final String psKey = "ps1";
                if (null == param._psMap.get(psKey)) {
                    param._psMap.put(psKey, db2.prepareStatement(Student.sql(param)));       //生徒preparestatement
                }
                final PreparedStatement ps1 = (PreparedStatement) param._psMap.get(psKey);

                int pp = 0;
                ps1.setString(++pp, chair._attendclasscd);  //講座コード
                ps1.setDate(++pp, Date.valueOf(chair._appdate));    //適用開始日付
                rs = ps1.executeQuery();
                
                int ban = 1;
                while (rs.next()) {
                    final Student student = new Student();
                    student._ban = ban++;
                    student._chair = chair;
                    student._schregno = rs.getString("SCHREGNO");
                    student._name = rs.getString("NAME");
                    student._nameKana = rs.getString("NAME_KANA");
                    student._sex = rs.getString("SEX");
                    student._grade = rs.getString("GRADE");
                    student._hrClass = rs.getString("HR_CLASS");
                    student._hrName = rs.getString("HR_NAME");
                    student._hrNameabbv = rs.getString("HR_NAMEABBV");
                    student._attendno = rs.getString("ATTENDNO");
                    studentList.add(student);
                }

            } catch (Exception ex) {
                log.error("Set_Detail_1 read error!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return studentList;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("    T1.SCHREGNO, ");
            // 文京の場合、性別の＊を表記しない。
            if (param._isBunkyo) {
                stb.append(" '' AS SEX, ");  // 男:空白、女:''
            } else {
                stb.append("    CASE WHEN T1.SEX = '2' THEN '*' ELSE '' END AS SEX, ");
            }
            stb.append("    value(T1.NAME,'') NAME, ");
            stb.append("    value(T1.NAME_KANA,'') NAME_KANA, ");
            stb.append("    value(T6.HR_NAMEABBV,'') HR_NAMEABBV, ");
            stb.append("    value(T6.HR_NAME,'') HR_NAME, ");
            stb.append("    value(T2.GRADE,'') GRADE, ");
            stb.append("    value(T2.HR_CLASS,'') HR_CLASS, ");
            stb.append("    value(T2.ATTENDNO,'') ATTENDNO ");
            stb.append("FROM ");
            stb.append("    CHAIR_STD_DAT T7 ");
            stb.append("    INNER JOIN SCHREG_BASE_MST T1 ON T1.SCHREGNO = T7.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T7.SCHREGNO ");
            stb.append("        AND T2.YEAR = T7.YEAR ");
            stb.append("        AND T2.SEMESTER = T7.SEMESTER ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T2.YEAR ");
            stb.append("        AND T6.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T6.GRADE = T2.GRADE ");
            stb.append("        AND T6.HR_CLASS = T2.HR_CLASS ");
            stb.append("WHERE ");
            stb.append("    T7.YEAR = '" + param._year + "' AND ");
            stb.append("    T7.SEMESTER = '" + param._semester + "' AND ");
            stb.append("    T7.CHAIRCD = ? AND ");
            stb.append("    T7.APPDATE = ? ");
            stb.append("ORDER BY ");
            if (param._output.equals(OUTPUT2)) {
                stb.append("    T1.SCHREGNO ");
            }else {
                stb.append("    T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
            }
            return stb.toString();
        }
    }
    
    private static class Chair {
        String _attendclasscd;    //講座コード
        String _nameShow; //職員コード
        String _chargeDiv;    //担任区分
        String _appdate;  //適用開始日付
        String _chairname;
        String _staffname;
        List _studentList = Collections.EMPTY_LIST;
     
        private static List getChairList(final DB2UDB db2, final Param param) {
            final List chairList = new ArrayList();
            //SVF出力
            final StringTokenizer stz1 = new StringTokenizer(param._attendclasscd, ",", false);     //講座コード
            final StringTokenizer stz2 = new StringTokenizer(param._nameShow, ",", false);      //職員コード
            final StringTokenizer stz3 = new StringTokenizer(param._chargeDiv, ",", false);     //担任区分
            final StringTokenizer stz4 = new StringTokenizer(param._appdate, ",", false);       //適用開始日付
            while (stz1.hasMoreTokens()){
                final Chair chair = new Chair();
                chair._attendclasscd = stz1.nextToken();    //講座コード
                chair._nameShow = stz2.nextToken(); //職員コード
                chair._chargeDiv = stz3.nextToken();    //担任区分
                chair._appdate = stz4.nextToken();  //適用開始日付
                chair._chairname = Chair.getChairname(db2, param, chair._attendclasscd);                         //講座出力のメソッド
                chair._staffname = Chair.getStaffname(db2, param, chair._nameShow);                         //担任出力のメソッド
                chair._studentList = Student.getChairStudentList(db2, param, chair);

                chairList.add(chair);
            }
            return chairList;
        }

        private static String getStaffname(final DB2UDB db2, final Param param, final String nameShow) {
            final String psKey = "ps3";
            if (null == param._psMap.get(psKey)) {
                try {
                    final StringBuffer stb = new StringBuffer();
                    stb.append("SELECT value(STAFFNAME,'') AS STAFFNAME FROM STAFF_MST WHERE STAFFCD = ? ");

                    param._psMap.put(psKey, db2.prepareStatement(stb.toString()));       //担任preparestatement
                } catch (Exception ex) {
                    log.error("Set_Detail_3 read error!", ex);
                }
            }
            
            final PreparedStatement ps3 = (PreparedStatement) param._psMap.get(psKey);

            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, ps3, new String[] {nameShow})));
        }
        
        private static String getChairname(final DB2UDB db2, final Param param, final String attendclasscd) {
            final String psKey = "ps2";
            if (null == param._psMap.get(psKey)) {
                    try {
                    final StringBuffer stb = new StringBuffer();
                    stb.append("SELECT value(CHAIRNAME,'') AS CHAIRNAME FROM CHAIR_DAT ");
                    stb.append("WHERE YEAR = '" + param._year + "' AND SEMESTER = '" + param._semester + "' AND CHAIRCD = ? ");

                    param._psMap.put(psKey, db2.prepareStatement(stb.toString()));       //講座preparestatement
                    
                } catch (Exception ex) {
                    log.error("Set_Detail_2 read error!", ex);
                }
            }
            final PreparedStatement ps2 = (PreparedStatement) param._psMap.get(psKey);

            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, ps2, new String[] {attendclasscd})));
        }
    }
    
    private static class Param {
        final String _year; //年度
        final String _semester; //学期
        final String _attendclasscd; //講座コード
        final String _nameShow; //科目担任名（職員コード）
        final String _chargeDiv; //担任区分1:正担任,0:副担任
        final String _appdate;  //適用開始日付
        final String _kensuu; //出力件数
//        final String _print_div; // 帳票種別　1:講座名簿（OUTPUT=1,2,3）、2:教務手帳（OUTPUT=4）
//        final String _pattern; // 講座名簿（OUTPUT=1,2,3）の出力パターン 1:Aパターン 2:Bパターン(年組を年と組に分ける)
//        final boolean _isPatternB; // 教務手帳（OUTPUT=4）の出力パターン 1:Aパターン 2:Bパターン
        final String _output; //出力順
//        final String _output4AB;
//        final String _width; //列の長さ
//        final String _height; //行の高さ
//        final String _printSchregno;
        final String _huriganaOutput;  //ふりがな出力
        final boolean _isTokyoto;
        final boolean _isBunkyo;
        final Map _psMap = new HashMap();
        public Param(final HttpServletRequest request, final DB2UDB db2) {
            
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _attendclasscd = request.getParameter("ATTENDCLASSCD");
            _nameShow = request.getParameter("NAME_SHOW");
            _chargeDiv = request.getParameter("CHARGEDIV");
            _appdate = request.getParameter("APPDATE");
            _kensuu = request.getParameter("KENSUU");

//            _print_div = request.getParameter("PRINT_DIV");
//            _pattern = request.getParameter("PATTERN");
//            _isPatternB = "1".equals(_print_div) && "2".equals(_pattern);

//            _output = "2".equals(_print_div) ? "4" : request.getParameter("OUTPUT");
//            _output4AB = request.getParameter("OUTPUT4AB");
            _output = request.getParameter("OUTPUT");
//            _width = request.getParameter("WIDTH");
//            _height = request.getParameter("HEIGHT");
//            _printSchregno = request.getParameter("PRINT_SCHREGNO");
            _huriganaOutput = request.getParameter("HURIGANA_OUTPUT");
            
            final String z010Name1 = getNameMstZ010(db2);
            _isTokyoto = "tokyoto".equals(z010Name1);
            _isBunkyo = "bunkyo".equals(z010Name1);
        }
        
        /**
         * 中高一貫か?
         * @param db2 DB2UDB
         * @return 中高一貫ならtrue
         */
        private String getNameMstZ010(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
        }
    }
    
}//クラスの括り
