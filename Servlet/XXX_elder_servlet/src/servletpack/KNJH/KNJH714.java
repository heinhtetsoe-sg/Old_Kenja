// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/05/07
 * 作成者: Nutec
 *
 */
package servletpack.KNJH;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
 *                  ＜ＫＮＪＨ７１４ ＞  学力テスト分割科目名票
 */
public class KNJH714 {

    private static final Log log = LogFactory.getLog(KNJH714.class);
    private boolean nonedata = false; //該当データなしフラグ
    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    private Param _param;
    private ArrayList<FacStudent> _facStudent;
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
            _facStudent = getFacsStudent(db2);
            printSvfMainStaff(db2, svf, _facStudent);

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

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

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
            final ArrayList<FacStudent> facStudentData
        ) {
            try {
                setForm(svf, _param, "KNJH714.xml", 4);

                int seq = 1;
                String type = "1";
                String fac = "";        //教室情報保管用
                String bunri = "";      //文理区分情報保管用
                String classCd = "";    //講座情報保管用
                String subclass = "";  //科目情報保管用
                for (FacStudent facStudentDatum : facStudentData) {
                    try {
                        if ("".equals(fac)) {
                            setHeader(svf, facStudentDatum);
                        }else if (  (fac.equals(facStudentDatum._fac) == false)
                            || isChangeSubclass(facStudentDatum, bunri, classCd, subclass)) {
                            //教室または科目が変わった場合、改ページ
                            //合計人数
                            svf.VrsOut("TOTAL", "計" + String.valueOf(seq - 1) + "名");
                            //改ページ処理
                            svf.VrEndRecord();
                            setForm(svf, _param, "KNJH714.xml", 4);
                            setHeader(svf, facStudentDatum);
                            seq = 1;
                        }
                        //文理区分情報を保管
                        bunri = facStudentDatum._bunri;
                        //講座情報を保管
                        classCd = facStudentDatum._classCd;
                        //科目情報を保管
                        subclass = facStudentDatum._subClass;
                        //教室情報を保管
                        fac = facStudentDatum._fac;

                        if (seq % 45 == 0) {
                            type = "2";
                        } else {
                            type = "1";
                        }
                        if (seq % 45 == 1) {
                            //改ページ処理
                            svf.VrEndRecord();
                            setForm(svf, _param, "KNJH714.xml", 4);
                            setHeader(svf, facStudentDatum);
                        }
                        //SEQ
                        svf.VrsOut("SEQ" + type, String.valueOf(seq));
                        //HR
                        svf.VrsOut("HR_NAMEABBV" + type, facStudentDatum._hrNameAbbv);
                        //No.
                        int attendNo = toInt(facStudentDatum._attendNo, 0);
                        svf.VrsOut("ATTENDNO" + type, String.valueOf(attendNo));
                        //性別
                        svf.VrsOut("SEX" + type, "1".equals(facStudentDatum._sex) ? "〇" : "");
                        //氏名
                        svf.VrsOut("NAME" + type, facStudentDatum._name);

                        seq++;
                        svf.VrEndRecord();
                        nonedata = true;
                    } catch (Exception ex) {
                        log.error("setSvfout setStudent error!", ex);
                        continue;
                    }
                }

                if (nonedata) {
                    svf.VrsOut("TOTAL", "計" + String.valueOf(seq - 1) + "名");
                    svf.VrEndRecord();
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
    private void setHeader(final Vrw32alp svf, FacStudent facStudentData) {
        //出力日付
        svf.VrsOut("DATE", "出力日：" + _param._date);
        //タイトル
        svf.VrsOut("TITLE", _param._year + "年度　" + facStudentData._testName);
        //実施日
        final String[] executeDate = facStudentData._date.split("-", 0);
        final String date = toInt(executeDate[1], 0) + "月" + executeDate[2] + "日";
        svf.VrsOut("EXEC_DATE", date + "　第" + facStudentData._zigen);
        //科目・施設名
        svf.VrsOut("SUBCLASS_NAME", facStudentData._subClassName + "　" + facStudentData._facilityName);
    }

    //科目変更判定
    private boolean isChangeSubclass(FacStudent facStudentData, String bunri, String classCd, String subclass) {
        if (   (("".equals(bunri)    == false) && (bunri.equals(facStudentData._bunri)       == false))
            || (("".equals(classCd)  == false) && (classCd.equals(facStudentData._classCd)   == false))
            || (("".equals(subclass) == false) && (subclass.equals(facStudentData._subClass) == false))) {
                return true;
            }
        return false;
    }

    private ArrayList<FacStudent> getFacsStudent(final DB2UDB db2) {
        ArrayList<FacStudent> facStudentData = new ArrayList<FacStudent>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getFacStudentDateSql(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            //生徒の選択科目情報を格納
            while (rs.next()) {
                FacStudent fs = new FacStudent(
                          rs.getString("TESTNAME")
                        , rs.getString("EXAM_DATE")
                        , rs.getString("ZIGEN")
                        , rs.getString("BUNRIDIV")
                        , rs.getString("CLASSCD")
                        , rs.getString("SUBCLASSCD")
                        , rs.getString("SUBCLASSNAME")
                        , rs.getString("FACCD")
                        , rs.getString("FACILITYNAME")
                        , rs.getString("HR_NAMEABBV")
                        , rs.getString("ATTENDNO")
                        , rs.getString("SEX")
                        , rs.getString("NAME")
                        );
                facStudentData.add(fs);
            }
        } catch (Exception e) {
            log.error("getStudentDateSql exception!", e);
        }
        return facStudentData;
    }

    /**教室ごとの生徒情報を取得**/
    private static class FacStudent {
        private String _testName;
        private String _date;
        private String _zigen;
        private String _bunri;
        private String _classCd;
        private String _subClass;
        private String _subClassName;
        private String _fac;
        private String _facilityName;
        private String _hrNameAbbv;
        private String _attendNo;
        private String _sex;
        private String _name;

        public FacStudent(
                  final String testName
                , final String date
                , final String zigen
                , final String bunri
                , final String classCd
                , final String subClass
                , final String subClassName
                , final String fac
                , final String facilityName
                , final String hrNameAbbv
                , final String attendNo
                , final String sex
                , final String name
                ) {
            _testName     = testName;
            _date         = date;
            _zigen        = zigen;
            _bunri        = bunri;
            _classCd      = classCd;
            _subClass     = subClass;
            _subClassName = subClassName;
            _fac          = fac;
            _facilityName = facilityName;
            _hrNameAbbv   = hrNameAbbv;
            _attendNo     = attendNo;
            _sex          = sex;
            _name         = name;
        }
    }

    /**生徒選択科目情報**/
    private static String getFacStudentDateSql(final Param _param)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SRD AS ( ");
        stb.append("     SELECT YEAR ");
        stb.append("          , SCHREGNO ");
        stb.append("          , SEMESTER ");
        stb.append("          , HR_CLASS ");
        stb.append("          , ATTENDNO ");
        stb.append("       FROM SCHREG_REGD_DAT SRD ");
        stb.append("      WHERE YEAR     = '" + _param._year + "' ");
        stb.append("        AND SEMESTER = '" + _param._semester + "' ");
        stb.append("        AND GRADE    = '03' ");
        stb.append("   GROUP BY YEAR ");
        stb.append("           , SCHREGNO ");
        stb.append("           , SEMESTER ");
        stb.append("           , HR_CLASS ");
        stb.append("           , ATTENDNO ");
        stb.append(" )   SELECT NM.NAME1 AS TESTNAME ");
        stb.append("          , AM.EXAM_DATE ");
        stb.append("          , NM1.NAME1 AS ZIGEN ");
        stb.append("          , AFD.BUNRIDIV ");
        stb.append("          , AFD.CLASSCD ");
        stb.append("          , AFD.SUBCLASSCD ");
        stb.append("          , SUB.SUBCLASSNAME ");
        stb.append("          , AFD.FACCD ");
        stb.append("          , FAC.FACILITYNAME ");
        stb.append("          , SRD.HR_CLASS ");
        stb.append("          , SRH.HR_NAMEABBV ");
        stb.append("          , SRD.ATTENDNO ");
        stb.append("          , SBM.SEX ");
        stb.append("          , SBM.NAME ");
        stb.append("       FROM ACADEMICTEST_FAC_DAT AFD ");
        stb.append("  LEFT JOIN SRD ");
        stb.append("         ON SRD.SCHREGNO = AFD.SCHREGNO ");
        stb.append("  LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("         ON SRH.YEAR     = SRD.YEAR ");
        stb.append("        AND SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("        AND SRH.GRADE    = '03' ");
        stb.append("        AND SRH.HR_CLASS = SRD.HR_CLASS ");
        stb.append("  LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("         ON SBM.SCHREGNO = SRD.SCHREGNO ");
        stb.append("  LEFT JOIN ACADEMICTEST_MST AM ");
        stb.append("         ON AM.YEAR    = SRD.YEAR ");
        stb.append("        AND AM.TESTDIV = '" + _param._testId.substring(0, 1) + "' ");
        stb.append("        AND AM.TESTID  = AFD.TESTID ");
        stb.append("  LEFT JOIN FACILITY_MST FAC ");
        stb.append("         ON FAC.FACCD = AFD.FACCD ");
        stb.append("  LEFT JOIN ACADEMICTEST_SUBCLASS_DAT SUB ");
        stb.append("         ON SUB.YEAR       = SRD.YEAR ");
        stb.append("        AND SUB.BUNRIDIV   = AFD.BUNRIDIV ");
        stb.append("        AND SUB.CLASSCD    = AFD.CLASSCD ");
        stb.append("        AND SUB.SUBCLASSCD = AFD.SUBCLASSCD ");
        stb.append("  LEFT JOIN NAME_MST NM ");
        stb.append("         ON NAMECD1 = 'H320' ");
        stb.append("        AND NAMECD2 = '1' ");
        stb.append("  LEFT JOIN NAME_MST NM1 ");
        stb.append("         ON NM1.NAMECD1 = 'H321' ");
        stb.append("        AND NM1.NAMECD2 = AFD.PERIODID ");
        stb.append("      WHERE AFD.TESTID   = '" + _param._testId + "' ");
        stb.append("        AND AFD.PERIODID = '" + _param._periodId + "' ");
        stb.append("        AND AFD.FACCD   IN (" + _param._facCd + ") ");
        stb.append("   ORDER BY AFD.FACCD ");
        stb.append("          , AFD.SUBCLASSCD ");
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
        private final String _testId;
        private final String _periodId;
        private final String _facCd;
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
            _testId           = request.getParameter("TESTNAME");                    //学力テスト区分
            _periodId         = request.getParameter("PERIODID");                    //時限
            _schoolKind       = request.getParameter("SCHOOLKIND");                  //SCHOOLKIND
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");     //クラス選択時SQL用学年クラス
            _documentRoot     = request.getParameter("DOCUMENTROOT");
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            String fac = "";
            for (int i = 0; i < _categorySelected.length; i++) {
                if (i == 0) {
                    fac = _categorySelected[i];
                } else {
                    fac += ", " + _categorySelected[i];
                }
            }
            _facCd = fac;
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJH714' AND NAME = '" + propName + "' "));
        }

    }
}//クラスの括り
