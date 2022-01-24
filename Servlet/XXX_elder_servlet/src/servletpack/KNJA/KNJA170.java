package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Grade_Hrclass;
import servletpack.KNJZ.detail.KNJ_Semester;

/**
 * 学校教育システム 賢者 [学籍管理] ＜ＫＮＪＡ１７０＞ 生徒基本データ(生徒名簿)
 * 2005/01/28 nakamoto 作成（東京都）
 * 2005/12/18 m-yama NO001 SCHREG_BASE_DAT、SCHREG_ADDRESS_DAT修正に伴う修正
 */

public class KNJA170 extends HttpServlet {
    /*pkg*/static final Log log = LogFactory.getLog(KNJA170.class);

    private boolean nonedata; // 該当データなしフラグ

    private String pyear, psemester, pgrade, phr_class, pschregno, date1, date2, tel_flg, selectForm;
    private String ymd;
    private String hrName;
    private String staffname;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        Vrw32alp svf = new Vrw32alp();
        // svf設定
        svf.VrInit(); // クラスの初期化
        svf.VrSetSpoolFileStream(outstrm); // PDFファイル名の設定

        // ＤＢ接続
        DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJA170]DB2 open error!", ex);
            return;
        }

        try {
            setParameter(request, db2);
            
            nonedata = false;
            printMain(db2, svf);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            db2.close(); // DBを閉じる
            
            /* 該当データ無し */
            if (nonedata == false) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            
            svf.VrPrint();
            svf.VrQuit();
            
            outstrm.close(); // ストリームを閉じる
        }
    }

    private void setParameter(final HttpServletRequest request, final DB2UDB db2) {
        // パラメータの取得
        try {
            pyear = request.getParameter("YEAR"); // 年度
            psemester = request.getParameter("OUTPUT"); // 学期
            tel_flg = request.getParameter("TEL"); // 電話番号出力フラグ
            selectForm = request.getParameter("FORM"); //フォームパターン

            // '学年＋組'パラメータを分解
            String strx = request.getParameter("GRADE_HR_CLASS"); // 学年＋組
            KNJ_Grade_Hrclass gradehrclass = new KNJ_Grade_Hrclass(); // クラスのインスタンス作成
            KNJ_Grade_Hrclass.ReturnVal returnval1 = gradehrclass.Grade_Hrclass(strx);
            pgrade = returnval1.val1; // 学年
            phr_class = returnval1.val2; // 組

            // 学籍番号
            String c_select[] = request.getParameterValues("category_name"); // 学籍番号
            pschregno = "(";
            for (int ia = 0; ia < c_select.length; ia++) {
                if (c_select[ia] == null)
                    break;
                if (ia > 0)
                    pschregno = pschregno + ",";
                pschregno = pschregno + "'" + c_select[ia] + "'";
            }
            pschregno = pschregno + ")";

            // 作成日(現在処理日)の取得
            try {
                KNJ_Control control = new KNJ_Control(); // クラスのインスタンス作成
                KNJ_Control.ReturnVal returnval = control.Control(db2);
                ymd = KNJ_EditDate.h_format_JP(returnval.val3);
            } catch (Exception e) {
                log.error("[KNJA170]ctrl_date get error!", e);
            }

            // 学期期間の取得(住所取得用)
            try {
                KNJ_Semester semester = new KNJ_Semester(); // クラスのインスタンス作成
                KNJ_Semester.ReturnVal returnval = semester.Semester(db2, pyear, psemester);
                date1 = returnval.val2; // 学期開始日
                date2 = returnval.val3; // 学期終了日
            } catch (Exception e) {
                log.error("[KNJA170]Semester sdate get error!", e);
            }

            // 組名称及び担任名の取得
            try {
                KNJ_Grade_Hrclass hrclass_staff = new KNJ_Grade_Hrclass(); // クラスのインスタンス作成
                KNJ_Grade_Hrclass.ReturnVal returnval = hrclass_staff.Hrclass_Staff(db2, pyear, psemester, pgrade, phr_class);
                hrName = returnval.val1;
                staffname = returnval.val3;
            } catch (Exception e) {
                log.error("[KNJA170]hrclass_staff error!", e);
            }

        } catch (Exception ex) {
            log.error("[KNJA170]parameter error!", ex);
        }
        
        log.debug("[KNJA170]pyear=" + pyear);
        log.debug("[KNJA170]psemester=" + psemester);
        log.debug("[KNJA170]pgrade=" + pgrade);
        log.debug("[KNJA170]phr_class=" + phr_class);
        log.debug("[KNJA170]pschregno=" + pschregno);

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        String formName;
        if (null == selectForm) {
            formName = "KNJA170.frm";
        } else if ("1".equals(selectForm)) {
            formName = "KNJA170_1.frm";
        } else if ("2".equals(selectForm)) {
            formName = "KNJA170_2.frm";
        } else {
            return;
        }
        log.info(" formName = " + formName);
        svf.VrSetForm(formName, 4);

        svf.VrsOut("YMD", ymd); // 現在処理日
        svf.VrsOut("nendo", KenjaProperties.gengou(Integer.parseInt(pyear)) + "年度");

        svf.VrsOut("HR_NAME", hrName); // 組名称
        svf.VrsOut("tannin_mei", staffname); // 担任名

        // ＳＱＬ作成
        final String sql = sql();

        log.debug("[KNJA170]sql=" + sql);

        // 照会結果の取得およびＳＶＦへ出力
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                // svfフォームのフィールドへ出力
                if (null == selectForm) {
                    svf.VrlOut("gakkyuubango", rs.getInt("ATTENDNO"));
                    svf.VrsOut("gakusekibango", rs.getString("SCHREGNO"));
                    svf.VrsOut("birthday1", KNJ_EditDate.h_format_JP_N(rs.getString("BIRTHDAY")));
                    svf.VrsOut("birthday2", KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")));
                    svf.VrsOut("ZIPCD", rs.getString("ZIPCD1"));
                    svf.VrsOut("hogosha_ZIPCD", rs.getString("GUARD_ZIPCD"));

                    svf.VrsOut("seito_kana", rs.getString("SEITO_KANA")); // ふりがな
                    svf.VrsOut("NAME", rs.getString("SEITO_KANJI")); // 生徒名
                    svf.VrsOut("seito_jyusho", rs.getString("ADDRESS1")); // 現住所
                    svf.VrsOut("seito_jyusho2", rs.getString("ADDRESS2")); // 現住所
                    svf.VrsOut("hogosha_kanji", rs.getString("GUARD_NAME")); // 保護者名

                    // 電話番号出力フラグ
                    if (tel_flg != null) {
                        svf.VrsOut("PHONE1", rs.getString("TELNO1"));
                        svf.VrsOut("PHONE2", rs.getString("TELNO2"));
                    }

                    svf.VrsOut("syussinko", rs.getString("J_NAME"));
                    
                } else if ("1".equals(selectForm)) {
                    svf.VrlOut("gakkyuubango", rs.getInt("ATTENDNO"));
                    svf.VrsOut("gakusekibango", rs.getString("SCHREGNO"));
                    svf.VrsOut("seito_kana", rs.getString("SEITO_KANA"));
                    svf.VrsOut("NAME", rs.getString("SEITO_KANJI"));
                    svf.VrsOut("birthday1", KNJ_EditDate.h_format_JP_N(rs.getString("BIRTHDAY")));
                    svf.VrsOut("birthday2", KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")));
                    svf.VrsOut("seito_jyusho", rs.getString("ADDRESS1"));
                    svf.VrsOut("seito_jyusho2", rs.getString("ADDRESS2"));
                    svf.VrsOut("hogosha_kanji", rs.getString("GUARD_NAME")); // 保護者名
                    svf.VrsOut("yuubinbango", rs.getString("GUARD_ZIPCD")); // 郵便番号

                    svf.VrsOut("hogosha_jyusho", rs.getString("GUARD_ADDR1")); // 保護者現住所
                    svf.VrsOut("hogosha_jyusho2", rs.getString("GUARD_ADDR2")); // 保護者現住所
                    svf.VrsOut("hogosha_shoku", rs.getString("WORK_NAME")); // 職業

                    // 電話番号出力フラグ
                    if (tel_flg != null) {
                        svf.VrsOut("denwabango", rs.getString("GUARD_TEL"));
                    }
                    svf.VrsOut("syussinko", rs.getString("J_NAME"));

                } else if ("2".equals(selectForm)) {

                    svf.VrlOut("gakkyuubango", rs.getInt("ATTENDNO"));
                    svf.VrsOut("gakusekibango", rs.getString("SCHREGNO"));
                    svf.VrsOut("ZIPCD", rs.getString("ZIPCD1"));
                    svf.VrsOut("hogosha_ZIPCD", rs.getString("GUARD_ZIPCD"));

                    // 電話番号出力フラグ
                    if (tel_flg != null) {
                        svf.VrsOut("PHONE1", rs.getString("TELNO1"));
                        svf.VrsOut("PHONE2", rs.getString("GUARD_TEL"));
                    }

                    svf.VrsOut("seito_kana" + (ms932ByteLength(rs.getString("SEITO_KANA")) > 24 ? "2" : ""), rs.getString("SEITO_KANA"));

                    svf.VrsOut("NAME" + (ms932ByteLength(rs.getString("SEITO_KANJI")) > 20 ? "2" : ""), rs.getString("SEITO_KANJI"));

                    String setAddrField1 = "";
                    String setAddrField2 = "2";
                    if (ms932ByteLength(rs.getString("ADDRESS1")) > 40 || ms932ByteLength(rs.getString("ADDRESS2")) > 40) {
                        setAddrField1 = "3";
                        setAddrField2 = "4";
                    }
                    svf.VrsOut("seito_jyusho" + setAddrField1, rs.getString("ADDRESS1"));
                    svf.VrsOut("seito_jyusho" + setAddrField2, rs.getString("ADDRESS2"));

                    svf.VrsOut("hogosha_kana" + (ms932ByteLength(rs.getString("GUARD_KANA")) > 24 ? "2" : ""), rs.getString("GUARD_KANA"));

                    svf.VrsOut("hogosha_kanji" + (ms932ByteLength(rs.getString("GUARD_NAME")) > 20 ? "2" : ""), rs.getString("GUARD_NAME"));

                    setAddrField1 = "";
                    setAddrField2 = "2";
                    if (ms932ByteLength(rs.getString("GUARD_ADDR1")) > 40 || ms932ByteLength(rs.getString("GUARD_ADDR2")) > 40) {
                        setAddrField1 = "3";
                        setAddrField2 = "4";
                    }
                    svf.VrsOut("hogosha_jyusho" + setAddrField1, rs.getString("GUARD_ADDR1"));
                    svf.VrsOut("hogosha_jyusho" + setAddrField2, rs.getString("GUARD_ADDR2"));
                }
                svf.VrEndRecord();
                nonedata = true; // 該当データなしフラグ
            }
        } catch (Exception e) {
            log.error("[KNJA170]DB2 query error!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     DB1.YEAR, ");
        stb.append("     DB1.GRADE, ");
        stb.append("     DB1.HR_CLASS, ");
        stb.append("     DB1.SEMESTER, ");
        stb.append("     DB1.SCHREGNO, ");
        stb.append("     DB1.ATTENDNO, ");
        stb.append("     DB2.NAME AS SEITO_KANJI, ");
        stb.append("     VALUE(DB2.NAME_KANA,'') AS SEITO_KANA, ");
        stb.append("     VALUE(CHAR(DB2.BIRTHDAY),'') AS BIRTHDAY, ");
        stb.append("     VALUE(DB3.ZIPCD,'') AS ZIPCD1, ");
        stb.append("     VALUE(DB3.ADDR1,'') AS ADDRESS1, ");
        stb.append("     value(DB3.ADDR2,'') AS ADDRESS2, ");
        stb.append("     VALUE(DB3.TELNO,'') AS TELNO1, ");
        stb.append("     VALUE(DB4.GUARD_NAME,'') AS GUARD_NAME, ");
        stb.append("     VALUE(DB4.GUARD_KANA,'') AS GUARD_KANA, ");
        stb.append("     VALUE(DB4.GUARD_ZIPCD,'') AS GUARD_ZIPCD, ");
        stb.append("     VALUE(DB4.GUARD_ADDR1,'') AS GUARD_ADDR1, ");
        stb.append("     VALUE(DB4.GUARD_ADDR2,'') AS GUARD_ADDR2, ");
        stb.append("     VALUE(DB4.GUARD_TELNO,'') AS GUARD_TEL, ");
        stb.append("     VALUE(DB8.NAME1,'') AS WORK_NAME, ");
        stb.append("     VALUE(DB2.EMERGENCYTELNO,'') AS TELNO2, ");
        stb.append("     VALUE(DB6.FINSCHOOL_NAME,'') AS J_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT               DB1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST     DB2 ON DB1.SCHREGNO = DB2.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT  DB7 ON DB1.YEAR = DB7.YEAR ");
        stb.append("           AND DB1.SEMESTER = DB7.SEMESTER ");
        stb.append("           AND DB1.GRADE = DB7.GRADE ");
        stb.append("           AND DB1.HR_CLASS = DB7.HR_CLASS ");
        stb.append("     LEFT  JOIN GUARDIAN_DAT      DB4 ON DB2.SCHREGNO = DB4.SCHREGNO ");
        stb.append("     LEFT  JOIN FINSCHOOL_MST     DB6 ON DB2.FINSCHOOLCD = DB6.FINSCHOOLCD ");
        stb.append("     LEFT JOIN (SELECT ");
        stb.append("                    SCHREGNO, ");
        stb.append("                    ZIPCD, ");
        stb.append("                    TELNO, ");
        stb.append("                    ADDR1, ");
        stb.append("                    ADDR2 ");
        stb.append("                FROM ");
        stb.append("                    SCHREG_ADDRESS_DAT W1 ");
        stb.append("                WHERE ");
        stb.append("                    (W1.SCHREGNO,W1.ISSUEDATE) IN ( ");
        stb.append("                    SELECT SCHREGNO,MAX(ISSUEDATE) ");
        stb.append("                    FROM   SCHREG_ADDRESS_DAT W2 ");
        stb.append("                                    WHERE  W2.ISSUEDATE <= '" + date2 + "' ");
        stb.append("                    AND (W2.EXPIREDATE IS NULL ");
        stb.append("                    OR W2.EXPIREDATE >= '" + date1 + "') ");
        stb.append("                    AND W2.SCHREGNO IN " + pschregno + " ");
        stb.append("                                    GROUP BY SCHREGNO ) ");
        stb.append("                ) DB3 ON DB3.SCHREGNO = DB1.SCHREGNO ");
        stb.append("     LEFT  JOIN NAME_MST DB8 ON DB8.NAMECD1 = 'H202' ");
        stb.append("     AND DB8.NAMECD2 = DB4.GUARD_JOBCD ");
        stb.append(" WHERE ");
        stb.append("     DB1.YEAR     = '" + pyear + "' ");
        stb.append("     AND DB1.SEMESTER = '" + psemester + "' ");
        stb.append("     AND DB1.GRADE    = '" + pgrade + "' ");
        stb.append("     AND DB1.HR_CLASS = '" + phr_class + "' ");
        stb.append("     AND DB1.SCHREGNO IN " + pschregno + " ");
        stb.append(" ORDER BY ");
        stb.append("     DB1.ATTENDNO ");
        return stb.toString();
    }

    private int ms932ByteLength(String name) {
        int count = 0;
        if (name != null) {
            try {
                count = name.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return count;
    }
}
