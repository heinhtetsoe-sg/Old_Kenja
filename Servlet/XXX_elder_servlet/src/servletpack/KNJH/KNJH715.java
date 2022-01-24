// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/04/29
 * 作成者: Nutec
 *
 */
package servletpack.KNJH;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＨ７１５ ＞ 学力テスト名票（HR別全生徒）
 */
public class KNJH715 {

    private static final Log log = LogFactory.getLog(KNJH715.class);
    private boolean nonedata = false; //該当データなしフラグ
    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    private Param _param;
    private Map<String, ArrayList<Student>> _classStudentMap;
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
            _classStudentMap = getClassStudent(db2);
            printSvfMain(db2, svf);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();     //DBを閉じる
            outstrm.close(); //ストリームを閉じる
        }
    }//doGetの括り

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    private void setForm(final Vrw32alp svf, final String form, final int data) {
        svf.VrSetForm(form, data);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    /** 帳票出力 **/
    private void printSvfMain(
            final DB2UDB db2,
            final Vrw32alp svf
        ) {
        try {
            setForm(svf, "KNJH715.xml", 1);

            final int MAX_ROW = 45;  //1ページの行数
            String bunri = "";
            String subclassName = "";
            String decline = "";

            //クラス分回す
            for (String hrClass : _classStudentMap.keySet()) {
                ArrayList<Student> stuentList = _classStudentMap.get(hrClass);
                //生徒分回す
                int row = 1;
                int no = 0;
                for (Student student : stuentList) {
                    try {
                        if (MAX_ROW < row) {
                            //改ページ処理
                            svf.VrEndPage();
                            setHeader(svf);
                            row = 1;
                        }
                        //SEQ
                        svf.VrsOutn("SEQ", row, String.valueOf(no + 1));
                        //HR(クラス)
                        svf.VrsOutn("HR_NAMEABBV", row, student._hrName);
                        //NO(出席番号)
                        int attendNo = toInt(student._attendNo, 0);
                        svf.VrsOutn("ATTENDNO", row, String.valueOf(attendNo));
                        //性別
                        svf.VrsOutn("SEX", row, "1".equals(student._sex) ? "〇" : "");
                        //氏名
                        svf.VrsOutn("NAME", row, student._name);

                        if ("1".equals(student._declineFlg)) {
                            //辞退者
                            bunri = "";
                            subclassName = "";
                            decline = "〇";
                        } else {
                            bunri = student._bunri;
                            subclassName = student._subclassName;
                            decline = "";
                        }

                        //文理
                        svf.VrsOutn("BUNR", row, bunri);
                        //選択
                        svf.VrsOutn("SUBCLASS_NAME", row, subclassName);
                        //辞退
                        svf.VrsOutn("DECLINE", row, decline);

                        row++;
                        nonedata = true;
                    } catch (Exception ex) {
                        log.error("setSvfout setStudent error!", ex);
                        continue;
                    }
                    no++;
                }
                if (nonedata) {
                    //印字するデータがある場合のみヘッダーを印字する
                    setHeader(svf);
                }
                //改ページ処理
                svf.VrEndPage();
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

    //ヘッダーの印字
    private void setHeader(final Vrw32alp svf) {
        svf.VrsOut("TITLE", _param._year + "年度　学力テスト名票（HR別）");
        svf.VrsOut("DATE", "出力日：" + _param._date);
    }

    /**生徒選択科目情報**/
    private String getStudentDateSql()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT SRD.HR_CLASS ");
        stb.append("          , SRD.SCHREGNO ");
        stb.append("          , SRH.HR_NAMEABBV ");
        stb.append("          , SRD.ATTENDNO ");
        stb.append("          , SBM.SEX ");
        stb.append("          , SBM.NAME ");
        stb.append("          , NMT.ABBV1 AS BUNRIDIV ");
        stb.append("          , ASD.SUBCLASSABBV ");
        stb.append("          , SAD.DECLINE_FLG ");
        stb.append("       FROM SCHREG_REGD_DAT SRD ");
        stb.append("  LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("         ON SRH.YEAR       = SRD.YEAR ");
        stb.append("        AND SRH.SEMESTER   = SRD.SEMESTER ");
        stb.append("        AND SRH.GRADE      = SRD.GRADE ");
        stb.append("        AND SRH.HR_CLASS   = SRD.HR_CLASS ");
        stb.append("  LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("         ON SBM.SCHREGNO   = SRD.SCHREGNO ");
        stb.append("  LEFT JOIN SCHREG_ACADEMICTEST_DAT SAD ");
        stb.append("         ON SAD.YEAR       = SRD.YEAR ");
        stb.append("        AND SAD.SCHREGNO   = SRD.SCHREGNO ");
        stb.append("  LEFT JOIN ACADEMICTEST_SUBCLASS_DAT ASD ");
        stb.append("         ON ASD.YEAR       = SRD.YEAR ");
        stb.append("        AND ASD.BUNRIDIV   = SAD.BUNRIDIV ");
        stb.append("        AND ASD.CLASSCD    = SAD.CLASSCD ");
        stb.append("        AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ");
        stb.append("  LEFT JOIN NAME_MST NMT ");
        stb.append("         ON NMT.NAMECD1    = 'H319' ");
        stb.append("        AND NMT.NAMECD2    = SAD.BUNRIDIV ");
        stb.append("      WHERE SRD.YEAR       = '" + _param._year + "' ");
        stb.append("        AND SRD.SEMESTER   = '" + _param._semester + "' ");
        stb.append("        AND SRD.GRADE || SRD.HR_CLASS IN ('" + _param._selectClass.replace(",", "','") + "') ");
        stb.append("        AND ASD.ELECTDIV   = '2' ");  //選択科目
        if ("1".equals(_param._jitaiFlg)) {
            //辞退者を取得しない
            stb.append("        AND SAD.DECLINE_FLG <> '1' ");
        }
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

    private Map<String, ArrayList<Student>> getClassStudent(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, ArrayList<Student>> classStudentMap = new HashMap<String, ArrayList<Student>>(){};

        try {
            String sql = getStudentDateSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ArrayList<Student> st = new ArrayList<Student>();
            String oldClass = "";
            while (rs.next()) {
                Student student = new Student();
                student.setStudent(
                          rs.getString("HR_CLASS")
                        , rs.getString("SCHREGNO")
                        , rs.getString("HR_NAMEABBV")
                        , rs.getString("ATTENDNO")
                        , rs.getString("SEX")
                        , rs.getString("NAME")
                        , rs.getString("BUNRIDIV")
                        , rs.getString("SUBCLASSABBV")
                        , rs.getString("DECLINE_FLG")
                        );
                final String hrClass = rs.getString("HR_CLASS");

                if ("".equals(oldClass)) {
                    oldClass = hrClass;
                }else if (oldClass.equals(hrClass) == false) {
                    //クラスが変わったら
                    classStudentMap.put(oldClass, st);
                    oldClass = hrClass;
                    st = new ArrayList<Student>();
                }
                st.add(student);
            }
            if (classStudentMap.containsKey(oldClass) == false) {
                classStudentMap.put(oldClass, st);
            }
        } catch (Exception ex) {
            log.error("getClassStudent error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return classStudentMap;
    }

    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _date;
        private final String _jitaiFlg;
        private final String _selectClass;
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
            _jitaiFlg         = request.getParameter("JITAI");                       //辞退フラグ
            _schoolKind       = request.getParameter("SCHOOLKIND");                  //SCHOOLKIND
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");     //クラス選択時SQL用学年クラス
            _documentRoot     = request.getParameter("DOCUMENTROOT");

            String selectDate = "";

            //カンマ区切りに修正
            for (int i = 0; i < _categorySelected.length; i++ ) {
                if (i == 0) {
                    selectDate = _categorySelected[i];
                } else {
                    selectDate += "," + _categorySelected[i];
                }
            }
            _selectClass = selectDate;  //選択クラス
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJH715' AND NAME = '" + propName + "' "));
        }
    }

    /**生徒の選択科目情報を取得**/
    private class Student {
        private String _hrClass;
        private String _schregno;
        private String _hrName;
        private String _attendNo;
        private String _sex;
        private String _name;
        private String _bunri;
        private String _subclassName;
        private String _declineFlg;
        private ArrayList<HashMap<String, String>> studentData;

        public void setStudent(
                  final String hrClass
                , final String schregno
                , final String hrName
                , final String attendNo
                , final String sex
                , final String name
                , final String bunri
                , final String subclassName
                , final String declineFlg
                ) {
            _hrClass      = hrClass;
            _schregno     = schregno;
            _hrName       = hrName;
            _attendNo     = attendNo;
            _sex          = sex;
            _name         = name;
            _bunri        = bunri;
            _subclassName = subclassName;
            _declineFlg   = declineFlg;
        }
    }
}//クラスの括り
