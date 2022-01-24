package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ２９０＞  レポート担当者別提出リスト
 *
 *  2005/04/19 m-yama 作成日
 *  @version $Id: 1cf2478fed5bb67629d1a7fc3bf64fe68b9986bf $
 */
public class KNJM290 {

    private static final Log log = LogFactory.getLog(KNJM290.class);
    private int _len;            //列数カウント用
    private int _ccnt;
    private String[] _staffnm;
    private String[] _staffcd;
    private String _z010;
    private boolean _isSagaken;

    /**
     * KNJM.classから最初に呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    //  CSOFF: ExecutableStatementCount|MethodLength
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        final String[] param  = new String[8];

        KNJServletUtils.debugParam(request, log);

        //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;

        boolean nonedata = false;
        svf.VrInit(); //クラスの初期化
        try {
            //  パラメータの取得
            param[0] = request.getParameter("YEAR"); //年度
            param[1] = request.getParameter("STAFF"); //学期
            param[2] = request.getParameter("DATEF"); //日付FROM
            param[3] = request.getParameter("DATET"); //日付TO
            param[7] = request.getParameter("useCurriculumcd");                     //教育課程
            log.debug("class" + param[0]);
            log.debug("date" + param[1]);

            //  print設定
            response.setContentType("application/pdf");

            //  svf設定
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            //  ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            _z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            _isSagaken = "sagaken".equals(_z010);

            nonedata = false; //該当データなしフラグ
            setHead(db2, svf, param); //見出し出力のメソッド
            for (int ia = 0; ia < param.length; ia++) {
                log.debug("[KNJM290]param[" + ia + "] = " + param[ia]);
            }

            //SQL作成
            ps1 = db2.prepareStatement(preStat1(param)); //設定データpreparestatement
            ps2 = db2.prepareStatement(preStat2(param)); //講座担当コードpreparestatement
            ps3 = db2.prepareStatement(preStat3(param)); //講座担当コードpreparestatement

            //固定項目GET
            if (param[1].equals("0")) {
                allclassdata(db2, svf, param, ps3);
                log.debug("alltantou");
            } else {
                classdata(db2, svf, param, ps2);
                log.debug("tantou");
            }

            for (int ia = 0; ia < _staffnm.length; ia++) {
                log.debug("staff" + _staffcd[ia]);
                log.debug("data" + _staffnm[ia]);
                if (setDetail1(db2, svf, param, _staffnm[ia], _staffcd[ia], ps1)) {
                    nonedata = true;
                }
                if (nonedata) {
                    //最終データフィールド出力
                    svf.VrEndPage();
                }
            }
        } catch (Exception e) {
        	log.error("exception!", e);
        } finally {
        	//  該当データ無し
        	if (!nonedata) {
        		svf.VrSetForm("MES001.frm", 0);
        		svf.VrsOut("note", "note");
        		svf.VrEndPage();
        	}
            if (null != svf) {
                svf.VrQuit();
            }
            DbUtils.closeQuietly(ps1);
            DbUtils.closeQuietly(ps2);
            DbUtils.closeQuietly(ps3);
            if (null != db2) {
                db2.commit();
                db2.close();                //DBを閉じる
            }
        }
    }
    //  CSON: ExecutableStatementCount|MethodLength

    /** SVF-FORM **/
    private void setHead(final DB2UDB db2, final Vrw32alp svf, final String[] param) {

        final KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJM290.frm", 1);
    //  作成日(現在処理日)の取得
        returnval = getinfo.Control(db2);
        param[6] = KNJ_EditDate.h_format_thi(returnval.val3, 0);

    }

    /** SVF-FORM **/
    private void allclassdata(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final PreparedStatement ps3
    ) throws Exception {

        int allcnt = 0;
        final ResultSet rs3 = ps3.executeQuery();
        try {
            log.debug("Allclass");
            while (rs3.next()) {
                allcnt++;
            }
        } finally {
            DbUtils.closeQuietly(rs3);
        }

        _staffnm = new String[allcnt];
        _staffcd = new String[allcnt];

        final ResultSet rs4 = ps3.executeQuery();
        try {
            while (rs4.next()) {
                _staffnm[_ccnt] = rs4.getString("STAFFNAME");
                _staffcd[_ccnt] = rs4.getString("STAFFCD");
                _ccnt++;
            }
        } finally {
            DbUtils.closeQuietly(rs4);
        }

    }

    /** SVF-FORM **/
    private void classdata(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final PreparedStatement ps2
    ) throws Exception {

        int allcnt = 0;
        final ResultSet rs1 = ps2.executeQuery();
        try {
            log.debug("classdata");
            while (rs1.next()) {
                allcnt++;
            }
        } finally {
            DbUtils.closeQuietly(rs1);
        }

        _staffnm = new String[allcnt];
        _staffcd = new String[allcnt];

        final ResultSet rs2 = ps2.executeQuery();
        try {
            while (rs2.next()) {
                _staffnm[_ccnt] = rs2.getString("STAFFNAME");
                _staffcd[_ccnt] = rs2.getString("STAFFCD");
                _ccnt++;
            }
        } finally {
            DbUtils.closeQuietly(rs2);
        }

    }


    /**SVF-FORM**/
    //  CSOFF: ExecutableStatementCount|MethodLength
    private boolean setDetail1(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String[] param,
            final String staffnm,
            final String staffcd,
            final PreparedStatement ps1
    ) throws Exception {
        log.debug("main");
        boolean nonedata = false;
        String seqflg  ;            //回数設定
        int kensuu = 0;              //件数カウンタ
        ps1.setString(1, staffcd);   //講座コード
        log.debug("start");
        final ResultSet rs = ps1.executeQuery();
        log.debug("end");
        try {
            String subno    ;           //処理科目
            String bfrsubno ;           //処理科目
            int gyo   = 1;          //行数カウント用
            bfrsubno = "*";
            while (rs.next()) {
                subno = rs.getString("SUBCLASSCD");
                log.debug("ima " + String.valueOf(subno) + "kako " + String.valueOf(bfrsubno));
                if (!subno.equalsIgnoreCase(bfrsubno) && gyo > 1) {
                    bfrsubno = rs.getString("SUBCLASSCD");
                    gyo = 1;
                    svf.VrsOut("kensuu"       , "計　　" + String.valueOf(kensuu) + "件");
                    kensuu = 0;
                    svf.VrEndPage();                  //SVFフィールド出力
                }
                bfrsubno = rs.getString("SUBCLASSCD");
                if (gyo > 50) {
                    gyo = 1;
                    svf.VrEndPage();                  //SVFフィールド出力
                }

                //ヘッダ出力
                svf.VrsOut("NENDO1"       , String.valueOf(param[0]));
                svf.VrsOut("ATTESTOR"     , String.valueOf(staffnm));
                svf.VrsOut("SDATE"        , String.valueOf(param[2]));
                svf.VrsOut("EDATE"        , String.valueOf(param[3]));
                svf.VrsOut("DATE"         , String.valueOf(param[6]));
                //科目コード・学籍・生徒・科目名・再提出・回数・受付月日・返信日付・評価
                svf.VrsOutn("SCHREGNO"        , gyo, rs.getString("SCHREGNO"));
                svf.VrsOutn("SCHREGNAME"      , gyo, rs.getString("NAME"));
                svf.VrsOutn("SUBCLASSNAME"        , gyo, rs.getString("SUBCLASSABBV"));
                svf.VrsOutn("RECEIPT_DATE"        , gyo, KNJ_EditDate.h_format_JP_MD(rs.getString("RECEIPT_DATE")));
                svf.VrsOutn("GRAD_DATE"       , gyo, KNJ_EditDate.h_format_JP_MD(rs.getString("GRAD_DATE")));
                svf.VrsOutn("GRAD_VALUE"      , gyo, rs.getString("NAME1"));

                if (rs.getString("REPSEQFLG").equals("A")) {
                    if (!rs.getString("REPRESENT_SEQ").equals("0")) {
                        seqflg = rs.getString("REPRESENT_SEQ");
                        seqflg = seqflg + String.valueOf("回");
                    } else {
                        seqflg = String.valueOf("");
                    }
                } else {
                    seqflg = String.valueOf("");
                }
                svf.VrsOutn("SAI"     , gyo, String.valueOf(seqflg));

                if (rs.getString("STANSEQFLG").equals("A")) {
                    seqflg = String.valueOf("第");
                    seqflg = seqflg + rs.getString("STANDARD_SEQ");
                    seqflg = seqflg + String.valueOf("回");
                } else {
                    seqflg = String.valueOf("");
                }
                svf.VrsOutn("SEQ"     , gyo, String.valueOf(seqflg));

                nonedata = true;
                gyo++;          //行数カウント用
                kensuu++;        //件数カウンタ
            }
            if (nonedata) {
                svf.VrsOut("kensuu"       , "計　　" + String.valueOf(kensuu) + "件");
            }

        } finally {
            DbUtils.closeQuietly(rs);
        }
        return nonedata;

    }
    //  CSON: ExecutableStatementCount|MethodLength

    /**PrepareStatement作成**/
    private String preStat1(final String[] param) {

        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT");
        if ("1".equals(param[7])) {
            stb.append("     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     t1.SUBCLASSCD, ");
        }
        stb.append("    t1.SCHREGNO,");
        stb.append("    t2.NAME,");
        stb.append("    t3.SUBCLASSABBV,");
        stb.append("    t1.REPRESENT_SEQ,");
        stb.append("    CASE WHEN REPRESENT_SEQ IS NULL THEN 'N' ELSE 'A' END AS REPSEQFLG,");
        stb.append("    t1.STANDARD_SEQ,");
        stb.append("    CASE WHEN STANDARD_SEQ IS NULL THEN 'N' ELSE 'A' END AS STANSEQFLG,");
        stb.append("    t1.RECEIPT_DATE,");
        stb.append("    t1.GRAD_DATE,");
        stb.append("    t4.NAME1");
        stb.append(" FROM");
        stb.append("    REP_PRESENT_DAT t1 LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO");
        stb.append("    LEFT JOIN SUBCLASS_MST t3 ON t1.SUBCLASSCD = t3.SUBCLASSCD");
        if ("1".equals(param[7])) {
            stb.append("       AND t1.CLASSCD = t3.CLASSCD ");
            stb.append("       AND t1.SCHOOL_KIND = t3.SCHOOL_KIND ");
            stb.append("       AND t1.CURRICULUM_CD = t3.CURRICULUM_CD ");
        }
        stb.append("    LEFT JOIN V_NAME_MST t4 ON t1.GRAD_VALUE = t4.NAMECD2 AND  t4.YEAR = t1.YEAR AND t4.NAMECD1 = 'M003'");
        stb.append(" WHERE");
        stb.append("    t1.YEAR = '").append(param[0]).append("' ");
        if (_isSagaken) {
            stb.append("    AND (t1.GRAD_DATE is null or t1.GRAD_DATE between '").append(param[2].replace('/', '-')).append("' and '").append(param[3].replace('/', '-')).append("') ");
        } else {
            stb.append("    AND t1.GRAD_DATE between '").append(param[2].replace('/', '-')).append("' and '").append(param[3].replace('/', '-')).append("' ");
        }
        stb.append("    AND t1.STAFFCD = ?");
        stb.append(" ORDER BY");
        if ("1".equals(param[7])) {
            stb.append("       t1.CLASSCD, ");
            stb.append("       t1.SCHOOL_KIND, ");
            stb.append("       t1.CURRICULUM_CD, ");
        }
        stb.append("    t1.SUBCLASSCD,");
        stb.append("    t1.SCHREGNO,");
        stb.append("    t1.STANDARD_SEQ");

        log.debug(stb);
        return stb.toString();

    }


    /**担当者指定時抽出**/
    private String preStat2(final String[] param) {

        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT");
        stb.append("    t1.STAFFCD,");
        stb.append("    t2.STAFFNAME");
        stb.append(" FROM");
        stb.append("    REP_PRESENT_DAT t1 LEFT JOIN STAFF_MST t2 ON t1.STAFFCD = t2.STAFFCD");
        stb.append(" WHERE");
        stb.append("    t1.YEAR = '").append(param[0]).append("' AND");
        stb.append("    t1.STAFFCD = '").append(param[1]).append("'");
        stb.append(" GROUP BY");
        stb.append("    t1.STAFFCD,");
        stb.append("    t2.STAFFNAME");

        return stb.toString();

    }

    /**全担当者抽出**/
    private String preStat3(final String[] param) {

        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT");
        stb.append("    t1.STAFFCD,");
        stb.append("    t2.STAFFNAME");
        stb.append(" FROM");
        stb.append("    REP_PRESENT_DAT t1 LEFT JOIN STAFF_MST t2 ON t1.STAFFCD = t2.STAFFCD");
        stb.append(" WHERE");
        stb.append("    t1.YEAR = '").append(param[0]).append("' AND");
        stb.append("    t1.GRAD_DATE between '").append(param[2].replace('/', '-')).append("' and '").append(param[3].replace('/', '-')).append("' AND");
        stb.append("    t1.STAFFCD IS NOT NULL AND t1.STAFFCD <> ''");
        stb.append(" GROUP BY");
        stb.append("    t1.STAFFCD,");
        stb.append("    t2.STAFFNAME");

        log.debug(stb);
        return stb.toString();

    }
}
