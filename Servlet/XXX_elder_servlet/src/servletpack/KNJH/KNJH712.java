// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/04/23
 * 作成者: Nutec
 *
 */
package servletpack.KNJH;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＨ７１２ ＞  学力テスト生徒確認書
 */
public class KNJH712 {

    private static final Log log = LogFactory.getLog(KNJH712.class);
    private boolean nonedata = false; //該当データなしフラグ
    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    private static final String ATTR_UNDERLINE = "UnderLine=(0,1,1)";
    private Param _param;
    private Student _student;
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        final Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                    //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //svf設定
            svf.VrInit();                                         //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            sd.setSvfInit(request, response, svf);
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            _param = getParam(db2, request);
            _student = new Student();
            _student.studentData(db2, _param);
            printSvfMainStaff(db2, svf, _student.studentData);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();     //DBを閉じる
            outstrm.close(); //ストリームを閉じる
        }

    }//doGetの括り

    private void setForm(final Vrw32alp svf, final Param _param, final String form, final int data) {
        svf.VrSetForm(form, data);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    /** 帳票出力 **/
    private void printSvfMainStaff(
            final DB2UDB db2,
            final Vrw32alp svf,
            final ArrayList<HashMap<String, String>> _student
        ) {
            try {
                setForm(svf, _param, "KNJH712.xml", 1);

                int line = 1;          //印字する行数
                int col  = 1;          //印字する列数
                int maxLine = 5;      //最大行数
                int maxCol = 2;       //最大列数
                String hrClass = ""; //クラス情報保管用
                int staffNameLen;
                String staffNameField;

                for (int i = 0; i < _student.size(); i++) {
                    if (col > maxCol) {
                        //列と行の切り替え
                        col = 1;
                        line++;
                    }

                    if (line > maxLine || !hrClass.equals(_student.get(i).get("hrClass"))) {
                        //改ページ処理
                        svf.VrEndPage();
                        col = 1;
                        line = 1;
                        svf.VrsOut("DUMMY", "　");
                        svf.VrEndPage();
                    }
                    //クラス情報を保管
                    hrClass = _student.get(i).get("hrClass");
                    //生徒氏名
                    staffNameLen = KNJ_EditEdit.getMS932ByteLength(_student.get(i).get("name"));
                    staffNameField = (staffNameLen <= 30)? "_1": "_2";
                    svf.VrsOutn("NAME" + col + staffNameField, line, _student.get(i).get("name"));
                    svf.VrAttributen("NAME" + col + staffNameField, line, ATTR_UNDERLINE);
                    //出力日
                    svf.VrsOutn("DATE" + col, line, _student.get(i).get("today"));
                    //対象年度
                    svf.VrsOutn("YEAR" + col, line, _student.get(i).get("year") + "年度");
                    //年組番号
                    svf.VrsOutn("HR_NAME" + col, line, "HR:" + _student.get(i).get("hrClass") + " No.:" + StringUtils.stripStart(_student.get(i).get("attendNo"), "0"));
                    svf.VrAttributen("HR_NAME" + col, line, ATTR_UNDERLINE);
                    //文理区分
                    svf.VrsOutn("BUNRIDIV" + col, line, _student.get(i).get("bunridiv") == null ? "" : _student.get(i).get("bunridiv"));
                    //選択科目
                    staffNameLen = KNJ_EditEdit.getMS932ByteLength(_student.get(i).get("subClassName"));
                    staffNameField = (staffNameLen <= 14)? "_1": (staffNameLen <= 20)? "_2": "_3";
                    svf.VrsOutn("SUBCLASS_NAME" + col + staffNameField, line, _student.get(i).get("subClassName") == null ? "" : _student.get(i).get("subClassName"));

                    col++;
                    nonedata = true;
                }

                if (nonedata) {
                    svf.VrEndPage();
                    svf.VrsOut("DUMMY", "　");
                    svf.VrEndPage();
                }

            } catch (Exception ex) {
                log.error("setSvfout set error!", ex);
            }
            if (nonedata) {
                svf.VrEndPage();
            }
        }

    /**生徒の選択科目情報を取得**/
    private static class Student {
        private ArrayList<HashMap<String, String>> studentData;

        public Student() {
            studentData = new ArrayList<HashMap<String, String>>();
        }

        public ArrayList<HashMap<String, String>> studentData(final DB2UDB db2, final Param _param) {

            HashMap<String, String> map;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getStudentDateSql(_param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                //生徒の選択科目情報を格納
                while (rs.next()) {
                    map = new HashMap<String, String>();

                    map.put("today"       , _param._date);
                    map.put("schregno"    , rs.getString("SCHREGNO"));
                    map.put("year"        , rs.getString("YEAR"));
                    map.put("bunridiv"    , rs.getString("NAME1"));
                    map.put("semester"    , rs.getString("SEMESTER"));
                    map.put("name"        , rs.getString("NAME"));
                    map.put("grade"       , rs.getString("GRADE"));
                    map.put("hrClass"     , rs.getString("HR_CLASS"));
                    map.put("attendNo"    , rs.getString("ATTENDNO"));
                    map.put("subClassCd"  , rs.getString("SUBCLASSCD"));
                    map.put("subClassName", rs.getString("SUBCLASSNAME"));

                    studentData.add(map);
                }
            } catch (Exception e) {
                log.error("getStudentDateSql exception!", e);
            }
            return studentData;

        }
    }

    /**学籍番号情報取得**/
    private static String getSchregno(String year, String semester, String _gradeHrclass)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" ");
        stb.append("  WITH SAD AS (  ");
        stb.append("    SELECT SCHREGNO ");
        stb.append("         , DECLINE_FLG ");
        stb.append("         , BUNRIDIV ");
        stb.append("         , YEAR ");
        stb.append("      FROM SCHREG_ACADEMICTEST_DAT ");
        stb.append("  GROUP BY SCHREGNO ");
        stb.append("         , DECLINE_FLG ");
        stb.append("         , BUNRIDIV ");
        stb.append("         , YEAR ");
        stb.append("         ) ");
        stb.append("    SELECT SRD.SCHREGNO || '-' || SRD.GRADE || SRD.HR_CLASS || SRD.ATTENDNO AS SCHREGNO ");
        stb.append("      FROM SCHREG_REGD_DAT SRD ");
        stb.append(" LEFT JOIN  SAD ");
        stb.append("        ON  (SRD.YEAR = SAD.YEAR ");
        stb.append("       AND  SRD.SCHREGNO = SAD.SCHREGNO) ");
        stb.append("     WHERE SRD.YEAR = '" + year + "' ");
        stb.append("       AND SRD.SEMESTER = '" + semester + "' ");
        stb.append("       AND SRD.GRADE || SRD.HR_CLASS IN( '" + _gradeHrclass.replace(",", "','") +"') ");
        stb.append("       AND SAD.DECLINE_FLG  <> '1'");
        stb.append("  ORDER BY SRD.GRADE ");
        stb.append("         , SRD.HR_CLASS ");
        stb.append("         , SRD.ATTENDNO ");

        return stb.toString();
    }

    /**生徒選択科目情報**/
    private static String getStudentDateSql(final Param _param)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT SBM.SCHREGNO ");
        stb.append("          , SRD.YEAR ");
        stb.append("          , SAD.BUNRIDIV ");
        stb.append("          , SRD.SEMESTER ");
        stb.append("          , SBM.NAME ");
        stb.append("          , SRD.GRADE ");
        stb.append("          , SRD.HR_CLASS ");
        stb.append("          , SRD.ATTENDNO ");
        stb.append("          , SAD.SUBCLASSCD ");
        stb.append("          , ASD.SUBCLASSNAME ");
        stb.append("          , NM.NAME1 ");
        stb.append("       FROM SCHREG_REGD_DAT SRD ");
        stb.append("  LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("         ON (SRD.SCHREGNO = SBM.SCHREGNO) ");
        stb.append("  LEFT JOIN SCHREG_ACADEMICTEST_DAT SAD ");
        stb.append("         ON (SAD.SCHREGNO = SBM.SCHREGNO) ");
        stb.append("  LEFT JOIN ACADEMICTEST_SUBCLASS_DAT ASD ");
        stb.append("         ON (SAD.YEAR      = ASD.YEAR ");
        stb.append("        AND SAD.BUNRIDIV   = ASD.BUNRIDIV ");
        stb.append("        AND SAD.CLASSCD    = ASD.CLASSCD ");
        stb.append("        AND SAD.SUBCLASSCD = ASD.SUBCLASSCD) ");
        stb.append("  LEFT JOIN NAME_MST NM ");
        stb.append("         ON (NM.NAMECD1 = 'H319' ");
        stb.append("        AND NM.NAMECD2  = SAD.BUNRIDIV) ");
        stb.append("      WHERE ASD.ELECTDIV = '2' ");
        stb.append("        AND SRD.YEAR     = '" + _param._year + "' ");
        stb.append("        AND SRD.SEMESTER = '" + _param._semester + "' ");
        stb.append("        AND SBM.SCHREGNO || '-' || SRD.GRADE || SRD.HR_CLASS || SRD.ATTENDNO IN ('" + _param._schregnos.replace(",", "','") + "') ");
        stb.append("   ORDER BY SRD.GRADE ");
        stb.append("          , SRD.HR_CLASS ");
        stb.append("          , SRD.ATTENDNO ");

        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _date;
        private final String _kubun;
        private final String _schregnos;
        private final String _documentRoot;
        private final String _schoolKind;
        private final String[] _categorySelected;
        private String _imagepath;
        private String _extension;
        final boolean _isOutputDebug;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year             = request.getParameter("CTRL_YEAR");                   //年度
            _semester         = request.getParameter("CTRL_SEMESTER");               //学期
            _date             = request.getParameter("CTRL_DATE").replace("-", "/"); //ログイン日付
            _kubun            = request.getParameter("KUBUN");                       //1：個人選択時、2：クラス選択時
            _schoolKind       = request.getParameter("SCHOOLKIND");                  //SCHOOLKIND
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");     //クラス選択時SQL用学年クラス
            _documentRoot     = request.getParameter("DOCUMENTROOT");

            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = null;
            String _studentDate = "";
            String _categorySelectes = "";

            //カンマ区切りに修正
            for (int i = 0; i < _categorySelected.length; i++ ) {
                if (i == 0) {
                    _studentDate = _categorySelected[i];
                } else {
                    _studentDate += "," + _categorySelected[i];
                }
            }

            if ("2".equals(_kubun) == true) {
                //クラス選択時学籍番号を取得
                sql = getSchregno(_year, _semester, _studentDate);
                try {
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    int count = 0;
                    while (rs.next()) {
                        if (count == 0) {
                            _categorySelectes = rs.getString("SCHREGNO");
                        } else {
                            _categorySelectes += "," + rs.getString("SCHREGNO");
                        }
                        count++;
                    }
                } catch (SQLException e) {
                    log.error("getSchregno set error!", e);
                }
                _schregnos = _categorySelectes;  //学籍番号-年組番
            } else {
                //個人選択時
                _schregnos = _studentDate;       //学籍番号-年組番
            }

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJH712' AND NAME = '" + propName + "' "));
        }

    }
}//クラスの括り
