package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ２３３＞  講座別名列
 *
 *	2005/07/04 m-yama 作成日
 *	2005/07/15 o-naka ログ(log)の記述を修正
 *	2006/05/25 o-naka NO001 氏名の前に女性は'*'を表示する(男:空白、女:'*')
 */

public class KNJA233B {

    private static final Log log = LogFactory.getLog(KNJA233B.class);

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;						//Databaseクラスを継承したクラス

        //	ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        final Param param = createParam(request, db2);

        boolean nonedata = false;                               //該当データなしフラグ
        PreparedStatement ps1 = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            OutputStream outstrm = response.getOutputStream();

            //  svf設定
            svf.VrInit();                           //クラスの初期化
            svf.VrSetSpoolFileStream(outstrm);          //PDFファイル名の設定

            //SQL作成
            ps1 = db2.prepareStatement(Pre_Stat1(param));       //生徒preparestatement

            for (int i = 0; i < param._classSelected.length; i++) {
                for (int ib = 0; ib < Integer.parseInt(param._kensuu); ib++) {
                    if (printMain(db2, svf, param, param._classSelected[i], ps1)) {
                        nonedata = true;        //生徒出力のメソッド
                    }
                }

            }
        } catch (Exception e) {
            log.error("exceptioN!", e);
        } finally {
            //  終了処理
            svf.VrQuit();
            DbUtils.closeQuietly(ps1);
            if (null != db2) {
                db2.commit();
                db2.close();                //DBを閉じる
            }
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
        }

    }//doGetの括り

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 62603 $ $Date: 2018-10-02 11:44:28 +0900 (火, 02 10 2018) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    /**SVF-FORM**/
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final Param param, final String classSelected, final PreparedStatement ps1)
    {
        boolean hasdata = false;
        final String firstprintfrm = "2".equals(param._outputlang) ? "KNJA233B_1E.frm" : "KNJA233B_1.frm";
        final String secondprintfrm = "2".equals(param._outputlang) ? "KNJA233B_2E.frm" : "KNJA233B_2.frm";
        if (printStudents(db2, svf, firstprintfrm, classSelected, ps1, param)) {
            hasdata = true;
        }
        if (printStudents(db2, svf, secondprintfrm, classSelected, ps1, param)) {
            hasdata = true;
        }
        return hasdata;
    }//Set_Detail_1()の括り

    private boolean printStudents(final DB2UDB db2, final Vrw32alp svf, final String form, final String classSelected, final PreparedStatement ps1, final Param param) {
        final int maxGyo = 45;
        svf.VrSetForm(form, 1);

        //log.debug(" classSelected = " + classSelected);

        final String[] split = StringUtils.split(classSelected, ",");
//        final String subclasscd = split[0];
//        final String gradeHrclass = split[1];
        final String staffcd = split[2];
        final String appdate = split[3];
        final String chaircd = split[4];
//        final String groupcd = split[5];

        boolean nonedata = false;
        ResultSet rs = null;
        try {
            int pp = 0;
            ps1.setString(++pp, staffcd);
            ps1.setString(++pp, chaircd);	//講座コード
            ps1.setDate(++pp,Date.valueOf(appdate));	//適用開始日付
            rs = ps1.executeQuery();

            int gyo = 1;			//行数カウント用

            while (rs.next()) {
                //	講座名・担任名出力
                if ("2".equals(param._outputlang)) {
                    svf.VrsOut("SUBCLASS_NAME", StringUtils.defaultString(rs.getString("SUBCLASSNAME_ENG")));
                    svf.VrsOut("TEACHER_NAME", StringUtils.defaultString(rs.getString("STAFFNAME_ENG")));
                } else {
                    svf.VrsOut("HR_CLASS", StringUtils.defaultString(rs.getString("CHAIRNAME")) + "　" + rs.getString("STAFFNAME"));
                }
                svf.VrsOutn("CLASSNO", gyo, NumberUtils.isDigits(rs.getString("HR_CLASS")) ? String.valueOf(Integer.parseInt(rs.getString("HR_CLASS"))) : rs.getString("HR_CLASS"));
                svf.VrsOutn("ATTENDNO", gyo, NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO"));

                svf.VrsOutn("SCHREGNO", gyo, StringUtils.defaultString(rs.getString("SCHREGNO")));
                svf.VrsOutn("MARK", gyo, rs.getString("SEX")); //NO001 男:空白、女:'*'

                String strz = StringUtils.defaultString(rs.getString("NAME"));
                int z = strz.indexOf("　"); // 空白文字の位置
                String strx = "";
                String stry = "";
                String field1 = null;
                String field2 = null;
                if (z != -1) {
                    strx = strz.substring(0, z); // 姓
                    stry = strz.substring(z + 1); // 名
                    if (strx.length() == 1) {
                        field1 = "LNAME2"; // 姓１文字
                    } else {
                        field1 = "LNAME1"; // 姓２文字以上
                    }
                    if (stry.length() == 1) {
                        field2 = "FNAME2"; // 名１文字
                    } else {
                        field2 = "FNAME1"; // 名２文字以上
                    }
                }
                if ("2".equals(param._outputlang)) {
                    svf.VrsOutn("NAME", gyo, rs.getString("NAME_ENG"));
                } else {
                    if (z != -1 && strx.length() <= 4 && stry.length() <= 4) {
                        svf.VrsOutn(field1, gyo, strx);
                        svf.VrsOutn(field2, gyo, stry);
                    } else {
                        svf.VrsOutn("NAME", gyo, rs.getString("NAME"));                   //空白がない
                    }
                }

                nonedata = true;
                gyo++;			//行数カウント用

                if (gyo > maxGyo) {
                    svf.VrEndPage();
                    gyo = 1;
                }
            }
            if (gyo != 1) {
                svf.VrEndPage();
            }
        } catch (Exception ex) {
            log.error("Set_Detail_1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return nonedata;
    }

    /**PrepareStatement作成**/
    private String Pre_Stat1(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    CASE WHEN T1.SEX = '2' THEN '*' ELSE '' END AS SEX, ");//NO001 男:空白、女:'*'
        stb.append("    value(T1.NAME,'') NAME, ");
        stb.append("    value(T1.NAME_KANA,'') NAME_KANA, ");
        stb.append("    value(T1.NAME_ENG,'') NAME_ENG, ");
        stb.append("    value(T6.HR_NAMEABBV,'') HR_NAMEABBV, ");
        stb.append("    value(T6.HR_NAME,'') HR_NAME, ");
        stb.append("    value(T2.GRADE,'') GRADE, ");
        stb.append("    value(T2.HR_CLASS,'') HR_CLASS, ");
        stb.append("    value(T2.ATTENDNO,'') ATTENDNO, ");
        stb.append("    T8.CHAIRNAME, ");
        stb.append("    T10.SUBCLASSNAME_ENG, ");
        stb.append("    T9.STAFFNAME_ENG, ");
        stb.append("    T9.STAFFNAME ");
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
        stb.append("    INNER JOIN CHAIR_DAT T8 ON T8.YEAR = T7.YEAR ");
        stb.append("        AND T8.SEMESTER = T7.SEMESTER ");
        stb.append("        AND T8.CHAIRCD = T7.CHAIRCD ");
        stb.append("    LEFT JOIN STAFF_MST T9 ON T9.STAFFCD = ? ");
        stb.append("    LEFT JOIN SUBCLASS_MST T10 ON T10.CLASSCD = T8.CLASSCD AND T10.SCHOOL_KIND = T8.SCHOOL_KIND AND T10.CURRICULUM_CD = T8.CURRICULUM_CD AND T10.SUBCLASSCD = T8.SUBCLASSCD");
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR ");
        stb.append("        AND GDAT.GRADE = T2.GRADE ");
        stb.append("    LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T7.SCHREGNO ");
        stb.append("        AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("    T7.YEAR = '"+param._year+"' AND ");
        stb.append("    T7.SEMESTER = '"+param._semester+"' AND ");
        stb.append("    T7.CHAIRCD =? AND ");
        stb.append("    T7.APPDATE =? ");
        if ("1".equals(param._grdDiv)) {
        	stb.append("    AND VALUE(ENTGRD.GRD_DIV, '') NOT IN ('2', '3') ");
        }
        stb.append("ORDER BY ");
        stb.append("    T2.GRADE,T2.HR_CLASS,T2.ATTENDNO ");

        return stb.toString();

    }//Pre_Stat1()の括り

    private static class Param {
        final String _year;
        final String _semester;
        final String _kensuu;
        final String[] _classSelected;
        final String _outputlang;
        final String _grdDiv;
        public Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");                                //年度
            _semester = request.getParameter("SEMESTER");                            //学期
            _kensuu = request.getParameter("KENSUU");                             //出力件数
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _outputlang = request.getParameter("OUTPUTLANG");  //出力言語(日/英)
            _grdDiv = request.getParameter("GRD_DIV");  //転退学生を出力しない
        }
    }

}//クラスの括り
