// kanji=漢字
/*
 * $Id: 87b765974d658062dc13b3a49ec4d9b28ad6883f $
 *
 * 作成日: 2010/06/28 13:39:41 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJE372A {

    private static final Log log = LogFactory.getLog("KNJE372A.class");

    private boolean _hasData;

    Param _param;

    private static final String FORM_FILE = "KNJE372A.frm";
    private static final int MAX_LEN_MAIN_FACULTY = 13;
    private static final int MAX_LEN_KOKURITU_COLLEGE = 12;
    private static final int MAX_LEN_SHIRITU = 11;
    private static final int MAX_LEN_SENMON = 6;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            log.debug("年組：" + _param._classSelectedIn);
            log.debug("系列大学：" + _param._mainCollegeCode);
            log.debug("系列学部：" + _param._mainFacultyIn);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final List hrclassList = createHrClass(db2);
        for (final Iterator it = hrclassList.iterator(); it.hasNext();) {
            final HrClass hrclass = (HrClass) it.next();

            svf.VrSetForm(FORM_FILE, 1);
            printHeader(svf);
            printHrClass(svf, hrclass);
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printHeader(final Vrw32alp svf) {
        svf.VrsOut("NENDO"          , _param._nendo );
        svf.VrsOut("TITLE"          , "進路志望調査集計表" );
        svf.VrsOut("LIMITDATE"      , _param.getReturnDate() );
        svf.VrsOut("DATE"           , _param.getTargetDate() );
//        svf.VrsOut("MAIN_COLLEGE"   , _param.getMainCollegeName() );
    }

    private void printHrClass(final Vrw32alp svf, final HrClass hrclass) {
        svf.VrsOut("HR_NAME"    , hrclass.getHrName() );
        svf.VrsOut("TEACHER"    , hrclass.getStaffName() );
        //在籍数
        svf.VrsOut("ENROLL_NUM"     , hrclass._totalZaiseki );
        svf.VrsOut("ENROLL_MALE"    , hrclass._totalZaisekiBoy );
        svf.VrsOut("ENROLL_FEMALE"  , hrclass._totalZaisekiGirl );
        //近大
        printMainFaculty(svf, hrclass);
        //国公立大学
        printKokuRitu(svf, hrclass);
        //私立大学
        printShiRitu(svf, hrclass);
        //他大学(18:未定,19:合計,20:大学合計)
        printHoka(svf, hrclass);
        //各種学校
        printSenmon(svf, hrclass);
        //就職
        printJob(svf, hrclass);
        //(27:未定)
        printMitei(svf, hrclass);
        //(28:休学,29:留学)
        printRyugaku(svf, hrclass);
        //(1:合計)
        printTotalALL(svf, hrclass);
    }

    private void printMainFaculty(final Vrw32alp svf, final HrClass hrclass) {
        /*** 項目 ***/
        //近大(1-13:学部)
        for (final Iterator it = _param._mainFacultyList.iterator(); it.hasNext();) {
            final MainFaculty mainFaculty = (MainFaculty) it.next();
            svf.VrsOutn("FACULTY",  mainFaculty._gyo,  mainFaculty._facultynameShow1);
        }
        //近大(14:未定,15:合計)
        svf.VrsOutn("FACULTY",      14,  "未定");
        svf.VrsOutn("FACULTY",      15,  "合計");
        /*** 集計(上表) ***/
        //近大(1-13:学部,14:未定)
        for (final Iterator it = hrclass._cntKindaiList.iterator(); it.hasNext();) {
            final CntKindai cntKindai = (CntKindai) it.next();
            svf.VrsOutn("MALE1",    cntKindai._gyo,  cntKindai.getCntFacultyBoy());
            svf.VrsOutn("FEMALE1",  cntKindai._gyo,  cntKindai.getCntFacultyGirl());
            svf.VrsOutn("TOTAL1",   cntKindai._gyo,  cntKindai.getCntFaculty());
        }
        //近大(15:合計)
        svf.VrsOutn("MALE1",    15,  getPrintCnt(hrclass._totalKindaiBoy));
        svf.VrsOutn("FEMALE1",  15,  getPrintCnt(hrclass._totalKindaiGirl));
        svf.VrsOutn("TOTAL1",   15,  getPrintCnt(hrclass._totalKindai));
    }

    private void printKokuRitu(final Vrw32alp svf, final HrClass hrclass) {
        /*** 項目(下表) ***/
        //国公立大学(1-12:大学)
        for (final Iterator it = _param._kokuRituCollegeList.iterator(); it.hasNext();) {
            final KokuRituCollege kokuRituCollege = (KokuRituCollege) it.next();
            log.debug(kokuRituCollege);
            svf.VrsOutn("COLLEGE_NAME1",  kokuRituCollege._gyo,  kokuRituCollege._schoolNameShow1);
        }
        /*** 集計(下表) ***/
        //国公立大学(1-12:大学,13:その他)
        for (final Iterator it = hrclass._cntKokuRituList.iterator(); it.hasNext();) {
            final CntKokuRitu cntKokuRitu = (CntKokuRitu) it.next();
            svf.VrsOutn("COLLEGE_MALE1",    cntKokuRitu._gyo,  cntKokuRitu.getCntBoy());
            svf.VrsOutn("COLLEGE_FEMALE1",  cntKokuRitu._gyo,  cntKokuRitu.getCntGirl());
            svf.VrsOutn("COLLEGE_TOTAL1",   cntKokuRitu._gyo,  cntKokuRitu.getCnt());
        }
        //国公立大学(14:合計)
        svf.VrsOutn("COLLEGE_MALE1",    14,  getPrintCnt(hrclass._totalKokuRituBoy));
        svf.VrsOutn("COLLEGE_FEMALE1",  14,  getPrintCnt(hrclass._totalKokuRituGirl));
        svf.VrsOutn("COLLEGE_TOTAL1",   14,  getPrintCnt(hrclass._totalKokuRitu));
        /*** 項目(上表) ***/
        //他大学(16:国公立)
        svf.VrsOutn("FACULTY",      16,  "国公立");
        /*** 集計(上表) ***/
        //他大学(16:国公立)
        svf.VrsOutn("MALE1",    16,  getPrintCnt(hrclass._totalKokuRituBoy));
        svf.VrsOutn("FEMALE1",  16,  getPrintCnt(hrclass._totalKokuRituGirl));
        svf.VrsOutn("TOTAL1",   16,  getPrintCnt(hrclass._totalKokuRitu));
    }

    private void printShiRitu(final Vrw32alp svf, final HrClass hrclass) {
        /*** 項目(下表) ***/
        //私立大学(1-11:大学)
        for (final Iterator it = _param._shiRituList.iterator(); it.hasNext();) {
            final ShiRitu shiRitu = (ShiRitu) it.next();
            log.debug(shiRitu);
            svf.VrsOutn("COLLEGE_NAME2",  shiRitu._gyo,  shiRitu._schoolNameShow1);
        }
        /*** 集計(下表) ***/
        //私立大学(1-11:大学,12:その他)
        for (final Iterator it = hrclass._cntShiRituList.iterator(); it.hasNext();) {
            final CntShiRitu cntShiRitu = (CntShiRitu) it.next();
            svf.VrsOutn("COLLEGE_MALE2",    cntShiRitu._gyo,  cntShiRitu.getCntBoy());
            svf.VrsOutn("COLLEGE_FEMALE2",  cntShiRitu._gyo,  cntShiRitu.getCntGirl());
            svf.VrsOutn("COLLEGE_TOTAL2",   cntShiRitu._gyo,  cntShiRitu.getCnt());
        }
        //私立大学(13:合計)
        svf.VrsOutn("COLLEGE_MALE2",    13,  getPrintCnt(hrclass._totalShiRituBoy));
        svf.VrsOutn("COLLEGE_FEMALE2",  13,  getPrintCnt(hrclass._totalShiRituGirl));
        svf.VrsOutn("COLLEGE_TOTAL2",   13,  getPrintCnt(hrclass._totalShiRitu));
        /*** 項目(上表) ***/
        //他大学(17:私立)
        svf.VrsOutn("FACULTY",      17,  "私立");
        /*** 集計(上表) ***/
        //他大学(17:私立)
        svf.VrsOutn("MALE1",    17,  getPrintCnt(hrclass._totalShiRituBoy));
        svf.VrsOutn("FEMALE1",  17,  getPrintCnt(hrclass._totalShiRituGirl));
        svf.VrsOutn("TOTAL1",   17,  getPrintCnt(hrclass._totalShiRitu));
    }

    private void printHoka(final Vrw32alp svf, final HrClass hrclass) {
        /*** 項目(上表) ***/
        //他大学(18:未定,19:合計)
        svf.VrsOutn("FACULTY",      18,  "未定");
        svf.VrsOutn("FACULTY",      19,  "合計");
        /*** 集計(上表) ***/
        //他大学(18:未定,19:合計,20:大学合計)
        svf.VrsOutn("MALE1",    18,  getPrintCnt(hrclass._totalHokaBoy));
        svf.VrsOutn("FEMALE1",  18,  getPrintCnt(hrclass._totalHokaGirl));
        svf.VrsOutn("TOTAL1",   18,  getPrintCnt(hrclass._totalHoka));
        svf.VrsOutn("MALE1",    19,  getPrintCnt(hrclass._totalHokaBoy + hrclass._totalShiRituBoy + hrclass._totalKokuRituBoy));
        svf.VrsOutn("FEMALE1",  19,  getPrintCnt(hrclass._totalHokaGirl + hrclass._totalShiRituGirl + hrclass._totalKokuRituGirl));
        svf.VrsOutn("TOTAL1",   19,  getPrintCnt(hrclass._totalHoka + hrclass._totalShiRitu + hrclass._totalKokuRitu));
        svf.VrsOutn("MALE1",    20,  getPrintCnt(hrclass._totalHokaBoy + hrclass._totalShiRituBoy + hrclass._totalKokuRituBoy + hrclass._totalKindaiBoy));
        svf.VrsOutn("FEMALE1",  20,  getPrintCnt(hrclass._totalHokaGirl + hrclass._totalShiRituGirl + hrclass._totalKokuRituGirl + hrclass._totalKindaiGirl));
        svf.VrsOutn("TOTAL1",   20,  getPrintCnt(hrclass._totalHoka + hrclass._totalShiRitu + hrclass._totalKokuRitu + hrclass._totalKindai));
    }

    private void printSenmon(final Vrw32alp svf, final HrClass hrclass) {
        /*** 項目(下表) ***/
        //私立大学(1-11:大学)
        for (final Iterator it = _param._senmonList.iterator(); it.hasNext();) {
            final Senmon senmon = (Senmon) it.next();
            log.debug(senmon);
            svf.VrsOutn("COLLEGE_NAME3",  senmon._gyo,  senmon._bunyaName);
        }
        /*** 集計(下表) ***/
        //各種学校(1-6:大学,7:その他)
        for (final Iterator it = hrclass._cntSenmonList.iterator(); it.hasNext();) {
            final CntSenmon cntSenmon = (CntSenmon) it.next();
            svf.VrsOutn("COLLEGE_MALE3",    cntSenmon._gyo,  cntSenmon.getCntBoy());
            svf.VrsOutn("COLLEGE_FEMALE3",  cntSenmon._gyo,  cntSenmon.getCntGirl());
            svf.VrsOutn("COLLEGE_TOTAL3",   cntSenmon._gyo,  cntSenmon.getCnt());
        }
        //各種学校(8:合計)
        svf.VrsOutn("COLLEGE_MALE3",    8,  getPrintCnt(hrclass._totalSenmonBoy));
        svf.VrsOutn("COLLEGE_FEMALE3",  8,  getPrintCnt(hrclass._totalSenmonGirl));
        svf.VrsOutn("COLLEGE_TOTAL3",   8,  getPrintCnt(hrclass._totalSenmon));
        /*** 集計(上表) ***/
        //(21:各種・専門)
        svf.VrsOutn("MALE1",    21,  getPrintCnt(hrclass._totalSenmonBoy));
        svf.VrsOutn("FEMALE1",  21,  getPrintCnt(hrclass._totalSenmonGirl));
        svf.VrsOutn("TOTAL1",   21,  getPrintCnt(hrclass._totalSenmon));
    }

    private void printJob(final Vrw32alp svf, final HrClass hrclass) {
        /*** 項目(上表) ***/
        //就職(1:学校紹介,2:縁故,3:家業,4:未定,5:合計)
        svf.VrsOutn("JOB_REASON",   1,  "学校紹介");
        svf.VrsOutn("JOB_REASON",   2,  "縁故");
        svf.VrsOutn("JOB_REASON",   3,  "家業");
        svf.VrsOutn("JOB_REASON",   4,  "未定");
        svf.VrsOutn("JOB_REASON",   5,  "合計");
        /*** 集計(上表) ***/
        //就職(22:学校紹介,23:縁故,24:家業,25:未定,26:合計)
        svf.VrsOutn("MALE1",    22,  getPrintCnt(hrclass._totalJob1Boy));
        svf.VrsOutn("FEMALE1",  22,  getPrintCnt(hrclass._totalJob1Girl));
        svf.VrsOutn("TOTAL1",   22,  getPrintCnt(hrclass._totalJob1));
        svf.VrsOutn("MALE1",    23,  getPrintCnt(hrclass._totalJob2Boy));
        svf.VrsOutn("FEMALE1",  23,  getPrintCnt(hrclass._totalJob2Girl));
        svf.VrsOutn("TOTAL1",   23,  getPrintCnt(hrclass._totalJob2));
        svf.VrsOutn("MALE1",    24,  getPrintCnt(hrclass._totalJob3Boy));
        svf.VrsOutn("FEMALE1",  24,  getPrintCnt(hrclass._totalJob3Girl));
        svf.VrsOutn("TOTAL1",   24,  getPrintCnt(hrclass._totalJob3));
        svf.VrsOutn("MALE1",    25,  getPrintCnt(hrclass._totalJob4Boy));
        svf.VrsOutn("FEMALE1",  25,  getPrintCnt(hrclass._totalJob4Girl));
        svf.VrsOutn("TOTAL1",   25,  getPrintCnt(hrclass._totalJob4));
        svf.VrsOutn("MALE1",    26,  getPrintCnt(hrclass._totalJobBoy));
        svf.VrsOutn("FEMALE1",  26,  getPrintCnt(hrclass._totalJobGirl));
        svf.VrsOutn("TOTAL1",   26,  getPrintCnt(hrclass._totalJob));
    }

    private void printMitei(final Vrw32alp svf, final HrClass hrclass) {
        /*** 集計(上表) ***/
        //(27:未定)
        svf.VrsOutn("MALE1",    27,  getPrintCnt(hrclass._totalMiteiBoy + hrclass._totalMitei2Boy));
        svf.VrsOutn("FEMALE1",  27,  getPrintCnt(hrclass._totalMiteiGirl + hrclass._totalMitei2Girl));
        svf.VrsOutn("TOTAL1",   27,  getPrintCnt(hrclass._totalMitei + hrclass._totalMitei2));
    }

    private void printRyugaku(final Vrw32alp svf, final HrClass hrclass) {
        /*** 集計(上表) ***/
        //(28:休学,29:留学)
        svf.VrsOutn("MALE1",    28,  getPrintCnt(hrclass._totalKyugakuBoy));
        svf.VrsOutn("FEMALE1",  28,  getPrintCnt(hrclass._totalKyugakuGirl));
        svf.VrsOutn("TOTAL1",   28,  getPrintCnt(hrclass._totalKyugaku));
        svf.VrsOutn("MALE1",    29,  getPrintCnt(hrclass._totalRyugakuBoy));
        svf.VrsOutn("FEMALE1",  29,  getPrintCnt(hrclass._totalRyugakuGirl));
        svf.VrsOutn("TOTAL1",   29,  getPrintCnt(hrclass._totalRyugaku));
    }

    private void printTotalALL(final Vrw32alp svf, final HrClass hrclass) {
        /*** 集計(上表) ***/
        //(1:合計)
        svf.VrsOut("MALE2",    getPrintCnt(hrclass._totalKyugakuBoy + hrclass._totalRyugakuBoy + hrclass._totalSenmonBoy + hrclass._totalJobBoy + hrclass._totalMiteiBoy + hrclass._totalMitei2Boy + hrclass._totalHokaBoy + hrclass._totalShiRituBoy + hrclass._totalKokuRituBoy + hrclass._totalKindaiBoy));
        svf.VrsOut("FEMALE2",  getPrintCnt(hrclass._totalKyugakuGirl + hrclass._totalRyugakuGirl + hrclass._totalSenmonGirl + hrclass._totalJobGirl + hrclass._totalMiteiGirl + hrclass._totalMitei2Girl + hrclass._totalHokaGirl + hrclass._totalShiRituGirl + hrclass._totalKokuRituGirl + hrclass._totalKindaiGirl));
        svf.VrsOut("TOTAL2",   getPrintCnt(hrclass._totalKyugaku + hrclass._totalRyugaku + hrclass._totalSenmon + hrclass._totalJob + hrclass._totalMitei + hrclass._totalMitei2 + hrclass._totalHoka + hrclass._totalShiRitu + hrclass._totalKokuRitu + hrclass._totalKindai));
    }

    private String getPrintCnt(final int cnt) {
        return (cnt == 0) ? "" : String.valueOf(cnt);
    }

    private List createHrClass(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sqlHrClass();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String staffName = rs.getString("STAFFNAME");
                
                final HrClass hrclass = new HrClass(grade, hrClass, hrName, staffName);
                hrclass.load(db2);
                rtn.add(hrclass);
            }
        } catch (final Exception ex) {
            log.error("クラスのロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlHrClass() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     L1.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("                                  AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("                                  AND T3.GRADE = T1.GRADE ");
        stb.append("                                  AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.TR_CD1 ");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + _param._classSelectedIn + " ");
        return stb.toString();
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _staffName;

        private String _totalZaisekiBoy;
        private String _totalZaisekiGirl;
        private String _totalZaiseki;

        private List _cntKindaiList;
        private int _totalKindaiBoy;
        private int _totalKindaiGirl;
        private int _totalKindai;

        private List _cntKokuRituList;
        private int _totalKokuRituBoy;
        private int _totalKokuRituGirl;
        private int _totalKokuRitu;

        private List _cntShiRituList;
        private int _totalShiRituBoy;
        private int _totalShiRituGirl;
        private int _totalShiRitu;

        private int _totalHokaBoy;
        private int _totalHokaGirl;
        private int _totalHoka;

        private List _cntSenmonList;
        private int _totalSenmonBoy;
        private int _totalSenmonGirl;
        private int _totalSenmon;

        private int _totalJob1Boy;
        private int _totalJob1Girl;
        private int _totalJob1;
        private int _totalJob2Boy;
        private int _totalJob2Girl;
        private int _totalJob2;
        private int _totalJob3Boy;
        private int _totalJob3Girl;
        private int _totalJob3;
        private int _totalJob4Boy;
        private int _totalJob4Girl;
        private int _totalJob4;
        private int _totalJobBoy;
        private int _totalJobGirl;
        private int _totalJob;

        private int _totalMiteiBoy;
        private int _totalMiteiGirl;
        private int _totalMitei;

        private int _totalKyugakuBoy;
        private int _totalKyugakuGirl;
        private int _totalKyugaku;
        private int _totalRyugakuBoy;
        private int _totalRyugakuGirl;
        private int _totalRyugaku;
        private int _totalMitei2Boy;
        private int _totalMitei2Girl;
        private int _totalMitei2;

        public HrClass(
                final String grade, 
                final String hrClass,
                final String hrName,
                final String staffName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staffName = staffName;
        }

        private String getHrName() {
            return (null == _hrName) ? "" : _hrName;
        }

        private String getStaffName() {
            return (null == _staffName) ? "" : _staffName;
        }

        private void load(final DB2UDB db2) throws SQLException {
            setCntZaiseki(db2);
            _cntKindaiList = createCntKindai(db2);
            setTotalKindai();
            _cntKokuRituList = createCntKokuRitu(db2);
            setTotalKokuRitu();
            _cntShiRituList = createCntShiRitu(db2);
            setTotalShiRitu();
            setCntHoka(db2);
            _cntSenmonList = createCntSenmon(db2);
            setTotalSenmon();
            setCntJob(db2);
            setCntMitei(db2);
            setCntRyugaku(db2);
        }

        private void setCntZaiseki(final DB2UDB db2) throws SQLException {
            final String sql = sqlCntZaiseki();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _totalZaiseki = rs.getString("CNT_CLASS");
                    _totalZaisekiBoy = rs.getString("CNT_CLASS_BOY");
                    _totalZaisekiGirl = rs.getString("CNT_CLASS_GIRL");
                }
            } catch (final Exception ex) {
                log.error("在籍数のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlCntZaiseki() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.SEX ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.GRADE = '" + _grade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     ) ");
            stb.append(" , T_CNT_CLASS AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_CLASS ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT_CLASS_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_CLASS_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT_CLASS_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_CLASS_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     L1.GRADE, ");
            stb.append("     L1.HR_CLASS, ");
            stb.append("     L1.CNT_CLASS, ");
            stb.append("     L2.CNT_CLASS_BOY, ");
            stb.append("     L3.CNT_CLASS_GIRL ");
            stb.append(" FROM ");
            stb.append("     T_CNT_CLASS L1 ");
            stb.append("     LEFT JOIN T_CNT_CLASS_BOY L2 ON L2.GRADE = L1.GRADE AND L2.HR_CLASS = L1.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT_CLASS_GIRL L3 ON L3.GRADE = L1.GRADE AND L3.HR_CLASS = L1.HR_CLASS ");
            stb.append(" ORDER BY ");
            stb.append("     L1.GRADE, ");
            stb.append("     L1.HR_CLASS ");
            return stb.toString();
        }

        private List createCntKindai(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlCntKindai();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String facultycd = rs.getString("FACULTYCD");
                    final String cntFaculty = rs.getString("CNT_FACULTY");
                    final String cntFacultyBoy = rs.getString("CNT_FACULTY_BOY");
                    final String cntFacultyGirl = rs.getString("CNT_FACULTY_GIRL");

                    int gyo = 0;
                    //近大(1-13:学部)
                    for (final Iterator it = _param._mainFacultyList.iterator(); it.hasNext();) {
                        final MainFaculty mainFaculty = (MainFaculty) it.next();
                        if (mainFaculty._facultycd.equals(facultycd)) {
                            gyo = mainFaculty._gyo;
                        }
                    }
                    //近大(14:未定)
                    if (gyo == 0) {
                        gyo = 14;
                    }

                    final CntKindai cntKindai = new CntKindai(facultycd, cntFaculty, cntFacultyBoy, cntFacultyGirl, gyo);
                    rtn.add(cntKindai);
                }
            } catch (final Exception ex) {
                log.error("近大のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlCntKindai() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.SEX ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.GRADE = '" + _grade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     ) ");
            stb.append(" , T_COURSE_HOPE_DAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.ENTRYDATE, ");
            stb.append("         L1.SEQ, ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP" + _param._outDiv + " AS SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD" + _param._outDiv + " AS SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD" + _param._outDiv + " AS FACULTYCD ");
            stb.append("     FROM ");
            stb.append("         COURSE_HOPE_DAT L1  ");
            stb.append("     WHERE ");
            stb.append("             L1.YEAR = '" + _param._year + "' ");
            stb.append("         AND L1.QUESTIONNAIRECD = '" + _param._questionnairecd + "' ");
            stb.append("     ) ");
            stb.append(" , MAX_ENTRYDATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1  ");
            stb.append("     GROUP BY ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , MAX_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.SEQ) AS SEQ, ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         MAX_ENTRYDATE T1  ");
            stb.append("         LEFT JOIN T_COURSE_HOPE_DAT L1 ON T1.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                                   AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     GROUP BY ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_SCHREG_COURSE_HOPE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.SEX, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD, ");
            stb.append("         N1.NAMESPARE1 AS TAISHOU_FLG, ");
            stb.append("         N1.NAMESPARE2 AS RITU_FLG ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1 ");
            stb.append("         INNER JOIN T_SCHREG T1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("         INNER JOIN MAX_SEQ T2 ON T2.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                              AND T2.SEQ = L1.SEQ ");
            stb.append("                              AND T2.SCHREGNO = L1.SCHREGNO ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E012' ");
            stb.append("                              AND N1.NAMECD2 = L1.SCHOOL_GROUP ");
            stb.append("     WHERE ");
            stb.append("             L1.COURSE_KIND = '1' ");
            stb.append("         AND L1.SCHOOL_CD = '" + _param._mainCollegeCode + "' ");
            stb.append("         AND N1.NAMESPARE1 = '1' ");
            stb.append("     ) ");
            stb.append(" , T_CNT_FACULTY AS ( ");
            stb.append("     SELECT ");
            stb.append("         FACULTYCD, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_FACULTY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     GROUP BY ");
            stb.append("         FACULTYCD ");
            stb.append("     ) ");
            stb.append(" , T_CNT_FACULTY_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         FACULTYCD, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_FACULTY_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         FACULTYCD ");
            stb.append("     ) ");
            stb.append(" , T_CNT_FACULTY_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         FACULTYCD, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_FACULTY_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("     GROUP BY ");
            stb.append("         FACULTYCD ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     L1.FACULTYCD, ");
            stb.append("     L1.CNT_FACULTY, ");
            stb.append("     L2.CNT_FACULTY_BOY, ");
            stb.append("     L3.CNT_FACULTY_GIRL ");
            stb.append(" FROM ");
            stb.append("     T_CNT_FACULTY L1 ");
            stb.append("     LEFT JOIN T_CNT_FACULTY_BOY L2 ON L2.FACULTYCD = L1.FACULTYCD ");
            stb.append("     LEFT JOIN T_CNT_FACULTY_GIRL L3 ON L3.FACULTYCD = L1.FACULTYCD ");
            stb.append(" ORDER BY ");
            stb.append("     L1.FACULTYCD ");
            return stb.toString();
        }

        private void setTotalKindai() {
            _totalKindaiBoy = 0;
            _totalKindaiGirl = 0;
            _totalKindai = 0;
            //近大(1-13:学部,14:未定)
            for (final Iterator it = _cntKindaiList.iterator(); it.hasNext();) {
                final CntKindai cntKindai = (CntKindai) it.next();
                _totalKindaiBoy += cntKindai.getCntFacultyBoyInt();
                _totalKindaiGirl += cntKindai.getCntFacultyGirlInt();
                _totalKindai += cntKindai.getCntFacultyInt();
            }
        }

        private List createCntKokuRitu(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlCntKokuRitu();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolCd = rs.getString("SCHOOL_CD");
                    final String cnt = rs.getString("CNT");
                    final String cntBoy = rs.getString("CNT_BOY");
                    final String cntGirl = rs.getString("CNT_GIRL");

                    int gyo = 0;
                    //国公立大学(1-12:大学)
                    for (final Iterator it = _param._kokuRituCollegeList.iterator(); it.hasNext();) {
                        final KokuRituCollege kokuRituCollege = (KokuRituCollege) it.next();
                        if (kokuRituCollege._schoolCd.equals(schoolCd)) {
                            gyo = kokuRituCollege._gyo;
                        }
                    }
                    //国公立大学(13:その他)
                    if (gyo == 0) {
                        gyo = 13;
                    }

                    final CntKokuRitu cntKokuRitu = new CntKokuRitu(schoolCd, cnt, cntBoy, cntGirl, gyo);
                    rtn.add(cntKokuRitu);
                }
            } catch (final Exception ex) {
                log.error("国公立大学のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private List createCntShiRitu(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlCntShiRitu();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolCd = rs.getString("SCHOOL_CD");
                    final String cnt = rs.getString("CNT");
                    final String cntBoy = rs.getString("CNT_BOY");
                    final String cntGirl = rs.getString("CNT_GIRL");

                    int gyo = 0;
                    //私立大学(1-11:大学)
                    for (final Iterator it = _param._shiRituList.iterator(); it.hasNext();) {
                        final ShiRitu shiRitu = (ShiRitu) it.next();
                        if (shiRitu._schoolCd.equals(schoolCd)) {
                            gyo = shiRitu._gyo;
                        }
                    }
                    //私立大学(12:その他)
                    if (gyo == 0) {
                        gyo = 12;
                    }

                    final CntShiRitu cntShiRitu = new CntShiRitu(schoolCd, cnt, cntBoy, cntGirl, gyo);
                    rtn.add(cntShiRitu);
                }
            } catch (final Exception ex) {
                log.error("私立大学のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private List createCntSenmon(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlCntSenmon();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String bunya = rs.getString("BUNYA");
                    final String cnt = rs.getString("CNT");
                    final String cntBoy = rs.getString("CNT_BOY");
                    final String cntGirl = rs.getString("CNT_GIRL");

                    int gyo = 0;
                    //各種学校(1-6:大学)
                    for (final Iterator it = _param._senmonList.iterator(); it.hasNext();) {
                        final Senmon senmon = (Senmon) it.next();
                        if (senmon._bunya.equals(bunya)) {
                            gyo = senmon._gyo;
                        }
                    }
                    //各種学校(7:その他)
                    if (gyo == 0) {
                        gyo = 7;
                    }

                    final CntSenmon cntSenmon = new CntSenmon(bunya, cnt, cntBoy, cntGirl, gyo);
                    rtn.add(cntSenmon);
                }
            } catch (final Exception ex) {
                log.error("専門のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlCntKokuRitu() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.SEX ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.GRADE = '" + _grade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     ) ");
            stb.append(" , T_COURSE_HOPE_DAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.ENTRYDATE, ");
            stb.append("         L1.SEQ, ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP" + _param._outDiv + " AS SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD" + _param._outDiv + " AS SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD" + _param._outDiv + " AS FACULTYCD ");
            stb.append("     FROM ");
            stb.append("         COURSE_HOPE_DAT L1  ");
            stb.append("     WHERE ");
            stb.append("             L1.YEAR = '" + _param._year + "' ");
            stb.append("         AND L1.QUESTIONNAIRECD = '" + _param._questionnairecd + "' ");
            stb.append("     ) ");
            stb.append(" , MAX_ENTRYDATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1  ");
            stb.append("     GROUP BY ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , MAX_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.SEQ) AS SEQ, ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         MAX_ENTRYDATE T1  ");
            stb.append("         LEFT JOIN T_COURSE_HOPE_DAT L1 ON T1.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                                   AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     GROUP BY ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_SCHREG_COURSE_HOPE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.SEX, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD, ");
            stb.append("         N1.NAMESPARE1 AS TAISHOU_FLG, ");
            stb.append("         N1.NAMESPARE2 AS RITU_FLG ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1 ");
            stb.append("         INNER JOIN T_SCHREG T1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("         INNER JOIN MAX_SEQ T2 ON T2.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                              AND T2.SEQ = L1.SEQ ");
            stb.append("                              AND T2.SCHREGNO = L1.SCHREGNO ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E012' ");
            stb.append("                              AND N1.NAMECD2 = L1.SCHOOL_GROUP ");
            stb.append("     WHERE ");
            stb.append("             L1.COURSE_KIND = '1' "); //進学
            stb.append("         AND L1.SCHOOL_CD <> '" + _param._mainCollegeCode + "' "); //系列大学以外
            stb.append("         AND N1.NAMESPARE1 = '1' "); //大学
            stb.append("         AND N1.NAMESPARE2 = '1' "); //国公立
            stb.append("     ) ");
            stb.append(" , T_CNT AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHOOL_CD, ");
            stb.append("         COUNT(SCHREGNO) AS CNT ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     GROUP BY ");
            stb.append("         SCHOOL_CD ");
            stb.append("     ) ");
            stb.append(" , T_CNT_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHOOL_CD, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         SCHOOL_CD ");
            stb.append("     ) ");
            stb.append(" , T_CNT_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHOOL_CD, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("     GROUP BY ");
            stb.append("         SCHOOL_CD ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     L1.SCHOOL_CD, ");
            stb.append("     L1.CNT, ");
            stb.append("     L2.CNT_BOY, ");
            stb.append("     L3.CNT_GIRL ");
            stb.append(" FROM ");
            stb.append("     T_CNT L1 ");
            stb.append("     LEFT JOIN T_CNT_BOY L2 ON L2.SCHOOL_CD = L1.SCHOOL_CD ");
            stb.append("     LEFT JOIN T_CNT_GIRL L3 ON L3.SCHOOL_CD = L1.SCHOOL_CD ");
            stb.append(" ORDER BY ");
            stb.append("     L1.SCHOOL_CD ");
            return stb.toString();
        }

        private String sqlCntShiRitu() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.SEX ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.GRADE = '" + _grade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     ) ");
            stb.append(" , T_COURSE_HOPE_DAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.ENTRYDATE, ");
            stb.append("         L1.SEQ, ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP" + _param._outDiv + " AS SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD" + _param._outDiv + " AS SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD" + _param._outDiv + " AS FACULTYCD ");
            stb.append("     FROM ");
            stb.append("         COURSE_HOPE_DAT L1  ");
            stb.append("     WHERE ");
            stb.append("             L1.YEAR = '" + _param._year + "' ");
            stb.append("         AND L1.QUESTIONNAIRECD = '" + _param._questionnairecd + "' ");
            stb.append("     ) ");
            stb.append(" , MAX_ENTRYDATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1  ");
            stb.append("     GROUP BY ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , MAX_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.SEQ) AS SEQ, ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         MAX_ENTRYDATE T1  ");
            stb.append("         LEFT JOIN T_COURSE_HOPE_DAT L1 ON T1.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                                   AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     GROUP BY ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_SCHREG_COURSE_HOPE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.SEX, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD, ");
            stb.append("         N1.NAMESPARE1 AS TAISHOU_FLG, ");
            stb.append("         N1.NAMESPARE2 AS RITU_FLG ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1 ");
            stb.append("         INNER JOIN T_SCHREG T1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("         INNER JOIN MAX_SEQ T2 ON T2.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                              AND T2.SEQ = L1.SEQ ");
            stb.append("                              AND T2.SCHREGNO = L1.SCHREGNO ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E012' ");
            stb.append("                              AND N1.NAMECD2 = L1.SCHOOL_GROUP ");
            stb.append("     WHERE ");
            stb.append("             L1.COURSE_KIND = '1' "); //進学
            stb.append("         AND L1.SCHOOL_CD <> '" + _param._mainCollegeCode + "' "); //系列大学以外
            stb.append("         AND N1.NAMESPARE1 = '1' "); //大学
            stb.append("         AND N1.NAMESPARE2 = '2' "); //私立
            stb.append("     ) ");
            stb.append(" , T_CNT AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHOOL_CD, ");
            stb.append("         COUNT(SCHREGNO) AS CNT ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     GROUP BY ");
            stb.append("         SCHOOL_CD ");
            stb.append("     ) ");
            stb.append(" , T_CNT_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHOOL_CD, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         SCHOOL_CD ");
            stb.append("     ) ");
            stb.append(" , T_CNT_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHOOL_CD, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("     GROUP BY ");
            stb.append("         SCHOOL_CD ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     L1.SCHOOL_CD, ");
            stb.append("     L1.CNT, ");
            stb.append("     L2.CNT_BOY, ");
            stb.append("     L3.CNT_GIRL ");
            stb.append(" FROM ");
            stb.append("     T_CNT L1 ");
            stb.append("     LEFT JOIN T_CNT_BOY L2 ON L2.SCHOOL_CD = L1.SCHOOL_CD ");
            stb.append("     LEFT JOIN T_CNT_GIRL L3 ON L3.SCHOOL_CD = L1.SCHOOL_CD ");
            stb.append(" ORDER BY ");
            stb.append("     L1.SCHOOL_CD ");
            return stb.toString();
        }

        private String sqlCntHoka() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.SEX ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.GRADE = '" + _grade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     ) ");
            stb.append(" , T_COURSE_HOPE_DAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.ENTRYDATE, ");
            stb.append("         L1.SEQ, ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP" + _param._outDiv + " AS SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD" + _param._outDiv + " AS SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD" + _param._outDiv + " AS FACULTYCD ");
            stb.append("     FROM ");
            stb.append("         COURSE_HOPE_DAT L1  ");
            stb.append("     WHERE ");
            stb.append("             L1.YEAR = '" + _param._year + "' ");
            stb.append("         AND L1.QUESTIONNAIRECD = '" + _param._questionnairecd + "' ");
            stb.append("     ) ");
            stb.append(" , MAX_ENTRYDATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1  ");
            stb.append("     GROUP BY ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , MAX_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.SEQ) AS SEQ, ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         MAX_ENTRYDATE T1  ");
            stb.append("         LEFT JOIN T_COURSE_HOPE_DAT L1 ON T1.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                                   AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     GROUP BY ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_SCHREG_COURSE_HOPE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.SEX, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD, ");
            stb.append("         N1.NAMESPARE1 AS TAISHOU_FLG, ");
            stb.append("         N1.NAMESPARE2 AS RITU_FLG ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1 ");
            stb.append("         INNER JOIN T_SCHREG T1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("         INNER JOIN MAX_SEQ T2 ON T2.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                              AND T2.SEQ = L1.SEQ ");
            stb.append("                              AND T2.SCHREGNO = L1.SCHREGNO ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E012' ");
            stb.append("                              AND N1.NAMECD2 = L1.SCHOOL_GROUP ");
            stb.append("     WHERE ");
            stb.append("             L1.COURSE_KIND = '1' "); //進学
            stb.append("         AND L1.SCHOOL_CD <> '" + _param._mainCollegeCode + "' "); //系列大学以外
            stb.append("         AND N1.NAMESPARE1 = '1' "); //大学
            stb.append("         AND VALUE(N1.NAMESPARE2,'0') NOT IN ('1','2') "); //国公立・私立以外
            stb.append("     ) ");
            stb.append(" , T_CNT AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     L1.GRADE, ");
            stb.append("     L1.HR_CLASS, ");
            stb.append("     L1.CNT, ");
            stb.append("     L2.CNT_BOY, ");
            stb.append("     L3.CNT_GIRL ");
            stb.append(" FROM ");
            stb.append("     T_CNT L1 ");
            stb.append("     LEFT JOIN T_CNT_BOY L2 ON L2.GRADE = L1.GRADE AND L2.HR_CLASS = L1.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT_GIRL L3 ON L3.GRADE = L1.GRADE AND L3.HR_CLASS = L1.HR_CLASS ");
            stb.append(" ORDER BY ");
            stb.append("     L1.GRADE, ");
            stb.append("     L1.HR_CLASS ");
            return stb.toString();
        }

        private String sqlCntSenmon() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.SEX ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.GRADE = '" + _grade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     ) ");
            stb.append(" , T_COURSE_HOPE_DAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.ENTRYDATE, ");
            stb.append("         L1.SEQ, ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP" + _param._outDiv + " AS SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD" + _param._outDiv + " AS SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD" + _param._outDiv + " AS FACULTYCD ");
            stb.append("     FROM ");
            stb.append("         COURSE_HOPE_DAT L1  ");
            stb.append("     WHERE ");
            stb.append("             L1.YEAR = '" + _param._year + "' ");
            stb.append("         AND L1.QUESTIONNAIRECD = '" + _param._questionnairecd + "' ");
            stb.append("     ) ");
            stb.append(" , MAX_ENTRYDATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1  ");
            stb.append("     GROUP BY ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , MAX_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.SEQ) AS SEQ, ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         MAX_ENTRYDATE T1  ");
            stb.append("         LEFT JOIN T_COURSE_HOPE_DAT L1 ON T1.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                                   AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     GROUP BY ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_SCHREG_COURSE_HOPE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.SEX, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD, ");
            stb.append("         N1.NAMESPARE1 AS TAISHOU_FLG, ");
            stb.append("         N1.NAMESPARE2 AS RITU_FLG, ");
            stb.append("         VALUE(L2.BUNYA,'99') AS BUNYA ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1 ");
            stb.append("         INNER JOIN T_SCHREG T1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("         INNER JOIN MAX_SEQ T2 ON T2.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                              AND T2.SEQ = L1.SEQ ");
            stb.append("                              AND T2.SCHREGNO = L1.SCHREGNO ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E012' ");
            stb.append("                              AND N1.NAMECD2 = L1.SCHOOL_GROUP ");
            stb.append("         LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'E017' ");
            stb.append("                              AND N2.NAME1 = L1.SCHOOL_CD ");
            stb.append("         LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.SCHOOL_CD ");
            stb.append("     WHERE ");
            stb.append("             L1.COURSE_KIND = '1' "); //進学
            stb.append("         AND N1.NAMESPARE1 = '2' "); //各種学校・専門
            stb.append("         AND VALUE(N2.NAME1,'0') = '0' "); //登録された大学コードは対象外
            stb.append("     ) ");
            stb.append(" , T_CNT AS ( ");
            stb.append("     SELECT ");
            stb.append("         BUNYA, ");
            stb.append("         COUNT(SCHREGNO) AS CNT ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     GROUP BY ");
            stb.append("         BUNYA ");
            stb.append("     ) ");
            stb.append(" , T_CNT_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         BUNYA, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         BUNYA ");
            stb.append("     ) ");
            stb.append(" , T_CNT_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         BUNYA, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("     GROUP BY ");
            stb.append("         BUNYA ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     L1.BUNYA, ");
            stb.append("     L1.CNT, ");
            stb.append("     L2.CNT_BOY, ");
            stb.append("     L3.CNT_GIRL ");
            stb.append(" FROM ");
            stb.append("     T_CNT L1 ");
            stb.append("     LEFT JOIN T_CNT_BOY L2 ON L2.BUNYA = L1.BUNYA ");
            stb.append("     LEFT JOIN T_CNT_GIRL L3 ON L3.BUNYA = L1.BUNYA ");
            stb.append(" ORDER BY ");
            stb.append("     L1.BUNYA ");
            return stb.toString();
        }

        private String sqlCntJob() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.SEX ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.GRADE = '" + _grade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     ) ");
            stb.append(" , T_COURSE_HOPE_DAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.ENTRYDATE, ");
            stb.append("         L1.SEQ, ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP" + _param._outDiv + " AS SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD" + _param._outDiv + " AS SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD" + _param._outDiv + " AS FACULTYCD, ");
            stb.append("         L1.INTRODUCTION_DIV" + _param._outDiv + " AS INTRODUCTION_DIV ");
            stb.append("     FROM ");
            stb.append("         COURSE_HOPE_DAT L1  ");
            stb.append("     WHERE ");
            stb.append("             L1.YEAR = '" + _param._year + "' ");
            stb.append("         AND L1.QUESTIONNAIRECD = '" + _param._questionnairecd + "' ");
            stb.append("     ) ");
            stb.append(" , MAX_ENTRYDATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1  ");
            stb.append("     GROUP BY ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , MAX_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.SEQ) AS SEQ, ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         MAX_ENTRYDATE T1  ");
            stb.append("         LEFT JOIN T_COURSE_HOPE_DAT L1 ON T1.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                                   AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     GROUP BY ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_SCHREG_COURSE_HOPE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.SEX, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.INTRODUCTION_DIV ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1 ");
            stb.append("         INNER JOIN T_SCHREG T1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("         INNER JOIN MAX_SEQ T2 ON T2.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                              AND T2.SEQ = L1.SEQ ");
            stb.append("                              AND T2.SCHREGNO = L1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         L1.COURSE_KIND IN ('2','3') "); //就職・家事手伝い
            stb.append("     ) ");
            stb.append(" , T_CNT AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT1 AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT1 ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         COURSE_KIND = '2' "); //就職
            stb.append("         AND INTRODUCTION_DIV = '1' "); //学校紹介
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT1_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT1_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("         AND COURSE_KIND = '2' "); //就職
            stb.append("         AND INTRODUCTION_DIV = '1' "); //学校紹介
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT1_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT1_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("         AND COURSE_KIND = '2' "); //就職
            stb.append("         AND INTRODUCTION_DIV = '1' "); //学校紹介
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT2 AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT2 ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         COURSE_KIND = '2' "); //就職
            stb.append("         AND INTRODUCTION_DIV = '2' "); //縁故
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT2_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT2_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("         AND COURSE_KIND = '2' "); //就職
            stb.append("         AND INTRODUCTION_DIV = '2' "); //縁故
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT2_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT2_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("         AND COURSE_KIND = '2' "); //就職
            stb.append("         AND INTRODUCTION_DIV = '2' "); //縁故
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT3 AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT3 ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         COURSE_KIND = '3' "); //家事手伝い
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT3_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT3_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("         AND COURSE_KIND = '3' "); //家事手伝い
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT3_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT3_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("         AND COURSE_KIND = '3' "); //家事手伝い
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT4 AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT4 ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         COURSE_KIND = '2' "); //就職
            stb.append("         AND VALUE(INTRODUCTION_DIV,'0') NOT IN ('1','2') "); //未定
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT4_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT4_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("         AND COURSE_KIND = '2' "); //就職
            stb.append("         AND VALUE(INTRODUCTION_DIV,'0') NOT IN ('1','2') "); //未定
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT4_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT4_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("         AND COURSE_KIND = '2' "); //就職
            stb.append("         AND VALUE(INTRODUCTION_DIV,'0') NOT IN ('1','2') "); //未定
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     L0.GRADE, ");
            stb.append("     L0.HR_CLASS, ");
            stb.append("     VALUE(L1.CNT1,0) AS CNT1, ");
            stb.append("     VALUE(L2.CNT1_BOY,0) AS CNT1_BOY, ");
            stb.append("     VALUE(L3.CNT1_GIRL,0) AS CNT1_GIRL, ");
            stb.append("     VALUE(M1.CNT2,0) AS CNT2, ");
            stb.append("     VALUE(M2.CNT2_BOY,0) AS CNT2_BOY, ");
            stb.append("     VALUE(M3.CNT2_GIRL,0) AS CNT2_GIRL, ");
            stb.append("     VALUE(N1.CNT3,0) AS CNT3, ");
            stb.append("     VALUE(N2.CNT3_BOY,0) AS CNT3_BOY, ");
            stb.append("     VALUE(N3.CNT3_GIRL,0) AS CNT3_GIRL, ");
            stb.append("     VALUE(O1.CNT4,0) AS CNT4, ");
            stb.append("     VALUE(O2.CNT4_BOY,0) AS CNT4_BOY, ");
            stb.append("     VALUE(O3.CNT4_GIRL,0) AS CNT4_GIRL ");
            stb.append(" FROM ");
            stb.append("     T_CNT L0 ");
            stb.append("     LEFT JOIN T_CNT1 L1 ON L1.GRADE = L0.GRADE AND L1.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT1_BOY L2 ON L2.GRADE = L0.GRADE AND L2.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT1_GIRL L3 ON L3.GRADE = L0.GRADE AND L3.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT2 M1 ON M1.GRADE = L0.GRADE AND M1.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT2_BOY M2 ON M2.GRADE = L0.GRADE AND M2.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT2_GIRL M3 ON M3.GRADE = L0.GRADE AND M3.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT3 N1 ON N1.GRADE = L0.GRADE AND N1.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT3_BOY N2 ON N2.GRADE = L0.GRADE AND N2.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT3_GIRL N3 ON N3.GRADE = L0.GRADE AND N3.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT4 O1 ON O1.GRADE = L0.GRADE AND O1.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT4_BOY O2 ON O2.GRADE = L0.GRADE AND O2.HR_CLASS = L0.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT4_GIRL O3 ON O3.GRADE = L0.GRADE AND O3.HR_CLASS = L0.HR_CLASS ");
            stb.append(" ORDER BY ");
            stb.append("     L0.GRADE, ");
            stb.append("     L0.HR_CLASS ");
            return stb.toString();
        }

        private String sqlCntMitei() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.SEX ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.GRADE = '" + _grade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     ) ");
            stb.append(" , T_COURSE_HOPE_DAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.ENTRYDATE, ");
            stb.append("         L1.SEQ, ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP" + _param._outDiv + " AS SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD" + _param._outDiv + " AS SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD" + _param._outDiv + " AS FACULTYCD ");
            stb.append("     FROM ");
            stb.append("         COURSE_HOPE_DAT L1  ");
            stb.append("     WHERE ");
            stb.append("             L1.YEAR = '" + _param._year + "' ");
            stb.append("         AND L1.QUESTIONNAIRECD = '" + _param._questionnairecd + "' ");
            stb.append("     ) ");
            stb.append(" , MAX_ENTRYDATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1  ");
            stb.append("     GROUP BY ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , MAX_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.SEQ) AS SEQ, ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         MAX_ENTRYDATE T1  ");
            stb.append("         LEFT JOIN T_COURSE_HOPE_DAT L1 ON T1.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                                   AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     GROUP BY ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_SCHREG_COURSE_HOPE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.SEX, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD, ");
            stb.append("         N1.NAMESPARE1 AS TAISHOU_FLG, ");
            stb.append("         N1.NAMESPARE2 AS RITU_FLG ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1 ");
            stb.append("         INNER JOIN T_SCHREG T1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("         INNER JOIN MAX_SEQ T2 ON T2.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                              AND T2.SEQ = L1.SEQ ");
            stb.append("                              AND T2.SCHREGNO = L1.SCHREGNO ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E012' ");
            stb.append("                              AND N1.NAMECD2 = L1.SCHOOL_GROUP ");
            stb.append("         LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'E017' ");
            stb.append("                              AND N2.NAME1 = L1.SCHOOL_CD ");
            stb.append("     WHERE ");
            stb.append("             L1.COURSE_KIND = '4' "); //未定
            stb.append("         OR (L1.COURSE_KIND = '1' AND N1.NAMESPARE1 = '3') "); //進学で、予備校・その他は対象
            stb.append("         OR (L1.COURSE_KIND = '1' AND N2.NAME1 IS NOT NULL) "); //進学で、登録された大学コードは対象
            stb.append("     ) ");
            stb.append(" , T_CNT AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_BOY ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) AS CNT_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG_COURSE_HOPE ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     L1.GRADE, ");
            stb.append("     L1.HR_CLASS, ");
            stb.append("     L1.CNT, ");
            stb.append("     L2.CNT_BOY, ");
            stb.append("     L3.CNT_GIRL ");
            stb.append(" FROM ");
            stb.append("     T_CNT L1 ");
            stb.append("     LEFT JOIN T_CNT_BOY L2 ON L2.GRADE = L1.GRADE AND L2.HR_CLASS = L1.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT_GIRL L3 ON L3.GRADE = L1.GRADE AND L3.HR_CLASS = L1.HR_CLASS ");
            stb.append(" ORDER BY ");
            stb.append("     L1.GRADE, ");
            stb.append("     L1.HR_CLASS ");
            return stb.toString();
        }

        private String sqlCntRyugaku() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T2.SEX ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.GRADE = '" + _grade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     ) ");
            stb.append(" , T_COURSE_HOPE_DAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.ENTRYDATE, ");
            stb.append("         L1.SEQ, ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP" + _param._outDiv + " AS SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD" + _param._outDiv + " AS SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD" + _param._outDiv + " AS FACULTYCD ");
            stb.append("     FROM ");
            stb.append("         COURSE_HOPE_DAT L1  ");
            stb.append("     WHERE ");
            stb.append("             L1.YEAR = '" + _param._year + "' ");
            stb.append("         AND L1.QUESTIONNAIRECD = '" + _param._questionnairecd + "' ");
            stb.append("     ) ");
            stb.append(" , MAX_ENTRYDATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.ENTRYDATE) AS ENTRYDATE, ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1  ");
            stb.append("     GROUP BY ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , MAX_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(L1.SEQ) AS SEQ, ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         MAX_ENTRYDATE T1  ");
            stb.append("         LEFT JOIN T_COURSE_HOPE_DAT L1 ON T1.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                                   AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     GROUP BY ");
            stb.append("         T1.ENTRYDATE, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_SCHREG_COURSE_HOPE AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         L1.COURSE_KIND, ");
            stb.append("         L1.SCHOOL_GROUP, ");
            stb.append("         L1.SCHOOL_CD, ");
            stb.append("         L1.FACULTYCD, ");
            stb.append("         N1.NAMESPARE1 AS TAISHOU_FLG, ");
            stb.append("         N1.NAMESPARE2 AS RITU_FLG ");
            stb.append("     FROM ");
            stb.append("         T_COURSE_HOPE_DAT L1 ");
            stb.append("         INNER JOIN MAX_SEQ T2 ON T2.ENTRYDATE = L1.ENTRYDATE ");
            stb.append("                              AND T2.SEQ = L1.SEQ ");
            stb.append("                              AND T2.SCHREGNO = L1.SCHREGNO ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E012' ");
            stb.append("                              AND N1.NAMECD2 = L1.SCHOOL_GROUP ");
            stb.append("     ) ");
            stb.append(" , T_TRANSFER AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.SEX, ");
            stb.append("         T2.TRANSFERCD AS TRANSFERCD1, ");
            stb.append("         T3.TRANSFERCD AS TRANSFERCD2 ");
            stb.append("     FROM ");
            stb.append("         T_SCHREG T1 ");
            stb.append("         LEFT JOIN SCHREG_TRANSFER_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T2.TRANSFERCD = '1' "); //留学
            stb.append("             AND DATE('" + _param._targetDate + "') BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
            stb.append("         LEFT JOIN SCHREG_TRANSFER_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T3.TRANSFERCD = '2' "); //休学
            stb.append("             AND DATE('" + _param._targetDate + "') BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append("     WHERE ");
            stb.append("         NOT EXISTS (SELECT 'X' FROM T_SCHREG_COURSE_HOPE L1 WHERE L1.SCHREGNO = T1.SCHREGNO) ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.SEX, ");
            stb.append("         T2.TRANSFERCD, ");
            stb.append("         T3.TRANSFERCD ");
            stb.append("     ) ");
            stb.append(" , T_CNT AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) - COUNT(TRANSFERCD1) - COUNT(TRANSFERCD2) AS CNT_MITEI, ");
            stb.append("         COUNT(TRANSFERCD2) AS CNT_KYUGAKU, ");
            stb.append("         COUNT(TRANSFERCD1) AS CNT_RYUGAKU ");
            stb.append("     FROM ");
            stb.append("         T_TRANSFER ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT_BOY AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) - COUNT(TRANSFERCD1) - COUNT(TRANSFERCD2) AS CNT_MITEI_BOY, ");
            stb.append("         COUNT(TRANSFERCD2) AS CNT_KYUGAKU_BOY, ");
            stb.append("         COUNT(TRANSFERCD1) AS CNT_RYUGAKU_BOY ");
            stb.append("     FROM ");
            stb.append("         T_TRANSFER ");
            stb.append("     WHERE ");
            stb.append("         SEX = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");
            stb.append(" , T_CNT_GIRL AS ( ");
            stb.append("     SELECT ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         COUNT(SCHREGNO) - COUNT(TRANSFERCD1) - COUNT(TRANSFERCD2) AS CNT_MITEI_GIRL, ");
            stb.append("         COUNT(TRANSFERCD2) AS CNT_KYUGAKU_GIRL, ");
            stb.append("         COUNT(TRANSFERCD1) AS CNT_RYUGAKU_GIRL ");
            stb.append("     FROM ");
            stb.append("         T_TRANSFER ");
            stb.append("     WHERE ");
            stb.append("         SEX = '2' ");
            stb.append("     GROUP BY ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     L1.GRADE, ");
            stb.append("     L1.HR_CLASS, ");
            stb.append("     VALUE(L1.CNT_KYUGAKU,0) AS CNT_KYUGAKU, ");
            stb.append("     VALUE(L1.CNT_RYUGAKU,0) AS CNT_RYUGAKU, ");
            stb.append("     VALUE(L2.CNT_KYUGAKU_BOY,0) AS CNT_KYUGAKU_BOY, ");
            stb.append("     VALUE(L2.CNT_RYUGAKU_BOY,0) AS CNT_RYUGAKU_BOY, ");
            stb.append("     VALUE(L3.CNT_KYUGAKU_GIRL,0) AS CNT_KYUGAKU_GIRL, ");
            stb.append("     VALUE(L3.CNT_RYUGAKU_GIRL,0) AS CNT_RYUGAKU_GIRL, ");
            stb.append("     VALUE(L1.CNT_MITEI,0) AS CNT_MITEI, ");
            stb.append("     VALUE(L2.CNT_MITEI_BOY,0) AS CNT_MITEI_BOY, ");
            stb.append("     VALUE(L3.CNT_MITEI_GIRL,0) AS CNT_MITEI_GIRL ");
            stb.append(" FROM ");
            stb.append("     T_CNT L1 ");
            stb.append("     LEFT JOIN T_CNT_BOY L2 ON L2.GRADE = L1.GRADE AND L2.HR_CLASS = L1.HR_CLASS ");
            stb.append("     LEFT JOIN T_CNT_GIRL L3 ON L3.GRADE = L1.GRADE AND L3.HR_CLASS = L1.HR_CLASS ");
            stb.append(" ORDER BY ");
            stb.append("     L1.GRADE, ");
            stb.append("     L1.HR_CLASS ");
            return stb.toString();
        }

        private void setTotalKokuRitu() {
            _totalKokuRituBoy = 0;
            _totalKokuRituGirl = 0;
            _totalKokuRitu = 0;
            //国公立大学(1-12:大学,13:その他)
            for (final Iterator it = _cntKokuRituList.iterator(); it.hasNext();) {
                final CntKokuRitu cntKokuRitu = (CntKokuRitu) it.next();
                _totalKokuRituBoy += cntKokuRitu.getCntBoyInt();
                _totalKokuRituGirl += cntKokuRitu.getCntGirlInt();
                _totalKokuRitu += cntKokuRitu.getCntInt();
            }
        }

        private void setTotalShiRitu() {
            _totalShiRituBoy = 0;
            _totalShiRituGirl = 0;
            _totalShiRitu = 0;
            //私立大学(1-11:大学,12:その他)
            for (final Iterator it = _cntShiRituList.iterator(); it.hasNext();) {
                final CntShiRitu cntShiRitu = (CntShiRitu) it.next();
                _totalShiRituBoy += cntShiRitu.getCntBoyInt();
                _totalShiRituGirl += cntShiRitu.getCntGirlInt();
                _totalShiRitu += cntShiRitu.getCntInt();
            }
        }

        private void setCntHoka(final DB2UDB db2) throws SQLException {
            _totalHokaBoy = 0;
            _totalHokaGirl = 0;
            _totalHoka = 0;
            final String sql = sqlCntHoka();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _totalHoka = rs.getInt("CNT");
                    _totalHokaBoy = rs.getInt("CNT_BOY");
                    _totalHokaGirl = rs.getInt("CNT_GIRL");
                }
            } catch (final Exception ex) {
                log.error("他大学(未定)のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setTotalSenmon() {
            _totalSenmonBoy = 0;
            _totalSenmonGirl = 0;
            _totalSenmon = 0;
            //私立大学(1-11:大学,12:その他)
            for (final Iterator it = _cntSenmonList.iterator(); it.hasNext();) {
                final CntSenmon cntSenmon = (CntSenmon) it.next();
                _totalSenmonBoy += cntSenmon.getCntBoyInt();
                _totalSenmonGirl += cntSenmon.getCntGirlInt();
                _totalSenmon += cntSenmon.getCntInt();
            }
        }

        private void setCntJob(final DB2UDB db2) throws SQLException {
            _totalJob1Boy = 0;
            _totalJob1Girl = 0;
            _totalJob1 = 0;
            _totalJob2Boy = 0;
            _totalJob2Girl = 0;
            _totalJob2 = 0;
            _totalJob3Boy = 0;
            _totalJob3Girl = 0;
            _totalJob3 = 0;
            _totalJob4Boy = 0;
            _totalJob4Girl = 0;
            _totalJob4 = 0;
            _totalJobBoy = 0;
            _totalJobGirl = 0;
            _totalJob = 0;
            final String sql = sqlCntJob();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _totalJob1 = rs.getInt("CNT1");
                    _totalJob1Boy = rs.getInt("CNT1_BOY");
                    _totalJob1Girl = rs.getInt("CNT1_GIRL");
                    _totalJob2 = rs.getInt("CNT2");
                    _totalJob2Boy = rs.getInt("CNT2_BOY");
                    _totalJob2Girl = rs.getInt("CNT2_GIRL");
                    _totalJob3 = rs.getInt("CNT3");
                    _totalJob3Boy = rs.getInt("CNT3_BOY");
                    _totalJob3Girl = rs.getInt("CNT3_GIRL");
                    _totalJob4 = rs.getInt("CNT4");
                    _totalJob4Boy = rs.getInt("CNT4_BOY");
                    _totalJob4Girl = rs.getInt("CNT4_GIRL");
                    _totalJob = _totalJob1 + _totalJob2 + _totalJob3 + _totalJob4;
                    _totalJobBoy = _totalJob1Boy + _totalJob2Boy + _totalJob3Boy + _totalJob4Boy;
                    _totalJobGirl = _totalJob1Girl + _totalJob2Girl + _totalJob3Girl + _totalJob4Girl;
                }
            } catch (final Exception ex) {
                log.error("就職のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setCntMitei(final DB2UDB db2) throws SQLException {
            _totalMiteiBoy = 0;
            _totalMiteiGirl = 0;
            _totalMitei = 0;
            final String sql = sqlCntMitei();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _totalMitei = rs.getInt("CNT");
                    _totalMiteiBoy = rs.getInt("CNT_BOY");
                    _totalMiteiGirl = rs.getInt("CNT_GIRL");
                }
            } catch (final Exception ex) {
                log.error("未定のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setCntRyugaku(final DB2UDB db2) throws SQLException {
            _totalKyugakuBoy = 0;
            _totalKyugakuGirl = 0;
            _totalKyugaku = 0;
            _totalRyugakuBoy = 0;
            _totalRyugakuGirl = 0;
            _totalRyugaku = 0;
            _totalMitei2Boy = 0;
            _totalMitei2Girl = 0;
            _totalMitei2 = 0;
            final String sql = sqlCntRyugaku();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _totalKyugaku = rs.getInt("CNT_KYUGAKU");
                    _totalKyugakuBoy = rs.getInt("CNT_KYUGAKU_BOY");
                    _totalKyugakuGirl = rs.getInt("CNT_KYUGAKU_GIRL");
                    _totalRyugaku = rs.getInt("CNT_RYUGAKU");
                    _totalRyugakuBoy = rs.getInt("CNT_RYUGAKU_BOY");
                    _totalRyugakuGirl = rs.getInt("CNT_RYUGAKU_GIRL");
                    _totalMitei2 = rs.getInt("CNT_MITEI");
                    _totalMitei2Boy = rs.getInt("CNT_MITEI_BOY");
                    _totalMitei2Girl = rs.getInt("CNT_MITEI_GIRL");
                }
            } catch (final Exception ex) {
                log.error("留学のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public String toString() {
            return _grade + _hrClass + ":" + _hrName;
        }
    }

    private class CntKindai {
        private final String _facultycd;
        private final String _cntFaculty;
        private final String _cntFacultyBoy;
        private final String _cntFacultyGirl;
        private final int _gyo;

        public CntKindai(
                final String facultycd, 
                final String cntFaculty,
                final String cntFacultyBoy,
                final String cntFacultyGirl,
                final int gyo
        ) {
            _facultycd = facultycd;
            _cntFaculty = cntFaculty;
            _cntFacultyBoy = cntFacultyBoy;
            _cntFacultyGirl = cntFacultyGirl;
            _gyo = gyo;
        }

        private String getCntFaculty() {
            return (null == _cntFaculty) ? "" : _cntFaculty;
        }

        private int getCntFacultyInt() {
            return (null == _cntFaculty) ? 0 : Integer.parseInt(_cntFaculty);
        }

        private String getCntFacultyBoy() {
            return (null == _cntFacultyBoy) ? "" : _cntFacultyBoy;
        }

        private int getCntFacultyBoyInt() {
            return (null == _cntFacultyBoy) ? 0 : Integer.parseInt(_cntFacultyBoy);
        }

        private String getCntFacultyGirl() {
            return (null == _cntFacultyGirl) ? "" : _cntFacultyGirl;
        }

        private int getCntFacultyGirlInt() {
            return (null == _cntFacultyGirl) ? 0 : Integer.parseInt(_cntFacultyGirl);
        }
    }

    private class CntKokuRitu {
        private final String _schoolCd;
        private final String _cnt;
        private final String _cntBoy;
        private final String _cntGirl;
        private final int _gyo;

        public CntKokuRitu(
                final String schoolCd, 
                final String cnt,
                final String cntBoy,
                final String cntGirl,
                final int gyo
        ) {
            _schoolCd = schoolCd;
            _cnt = cnt;
            _cntBoy = cntBoy;
            _cntGirl = cntGirl;
            _gyo = gyo;
        }

        private String getCnt() {
            return (null == _cnt) ? "" : _cnt;
        }
        private int getCntInt() {
            return (null == _cnt) ? 0 : Integer.parseInt(_cnt);
        }

        private String getCntBoy() {
            return (null == _cntBoy) ? "" : _cntBoy;
        }
        private int getCntBoyInt() {
            return (null == _cntBoy) ? 0 : Integer.parseInt(_cntBoy);
        }

        private String getCntGirl() {
            return (null == _cntGirl) ? "" : _cntGirl;
        }
        private int getCntGirlInt() {
            return (null == _cntGirl) ? 0 : Integer.parseInt(_cntGirl);
        }
    }

    private class CntShiRitu {
        private final String _schoolCd;
        private final String _cnt;
        private final String _cntBoy;
        private final String _cntGirl;
        private final int _gyo;

        public CntShiRitu(
                final String schoolCd, 
                final String cnt,
                final String cntBoy,
                final String cntGirl,
                final int gyo
        ) {
            _schoolCd = schoolCd;
            _cnt = cnt;
            _cntBoy = cntBoy;
            _cntGirl = cntGirl;
            _gyo = gyo;
        }

        private String getCnt() {
            return (null == _cnt) ? "" : _cnt;
        }
        private int getCntInt() {
            return (null == _cnt) ? 0 : Integer.parseInt(_cnt);
        }

        private String getCntBoy() {
            return (null == _cntBoy) ? "" : _cntBoy;
        }
        private int getCntBoyInt() {
            return (null == _cntBoy) ? 0 : Integer.parseInt(_cntBoy);
        }

        private String getCntGirl() {
            return (null == _cntGirl) ? "" : _cntGirl;
        }
        private int getCntGirlInt() {
            return (null == _cntGirl) ? 0 : Integer.parseInt(_cntGirl);
        }
    }

    private class CntSenmon {
        private final String _bunya;
        private final String _cnt;
        private final String _cntBoy;
        private final String _cntGirl;
        private final int _gyo;

        public CntSenmon(
                final String bunya, 
                final String cnt,
                final String cntBoy,
                final String cntGirl,
                final int gyo
        ) {
            _bunya = bunya;
            _cnt = cnt;
            _cntBoy = cntBoy;
            _cntGirl = cntGirl;
            _gyo = gyo;
        }

        private String getCnt() {
            return (null == _cnt) ? "" : _cnt;
        }
        private int getCntInt() {
            return (null == _cnt) ? 0 : Integer.parseInt(_cnt);
        }

        private String getCntBoy() {
            return (null == _cntBoy) ? "" : _cntBoy;
        }
        private int getCntBoyInt() {
            return (null == _cntBoy) ? 0 : Integer.parseInt(_cntBoy);
        }

        private String getCntGirl() {
            return (null == _cntGirl) ? "" : _cntGirl;
        }
        private int getCntGirlInt() {
            return (null == _cntGirl) ? 0 : Integer.parseInt(_cntGirl);
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class MainFaculty {
        private final String _facultycd;
        private final String _facultynameShow1;
        private final int _gyo;
        
        public MainFaculty(
                final String facultycd, 
                final String facultynameShow1,
                final int gyo
        ) {
            _facultycd = facultycd;
            _facultynameShow1 = facultynameShow1;
            _gyo = gyo;
        }
        
        public String toString() {
            return _facultycd + ":" + _facultynameShow1;
        }
    }

    private class KokuRituCollege {
        private final String _schoolCd;
        private final String _schoolNameShow1;
        private final int _gyo;
        
        public KokuRituCollege(
                final String schoolCd, 
                final String schoolNameShow1,
                final int gyo
        ) {
            _schoolCd = schoolCd;
            _schoolNameShow1 = schoolNameShow1;
            _gyo = gyo;
        }
        
        public String toString() {
            return _schoolCd + ":" + _schoolNameShow1;
        }
    }

    private class ShiRitu {
        private final String _schoolCd;
        private final String _schoolNameShow1;
        private final int _gyo;
        
        public ShiRitu(
                final String schoolCd, 
                final String schoolNameShow1,
                final int gyo
        ) {
            _schoolCd = schoolCd;
            _schoolNameShow1 = schoolNameShow1;
            _gyo = gyo;
        }
        
        public String toString() {
            return _schoolCd + ":" + _schoolNameShow1;
        }
    }

    private class Senmon {
        private final String _bunya;
        private final String _bunyaName;
        private final int _gyo;
        
        public Senmon(
                final String bunya, 
                final String bunyaName,
                final int gyo
        ) {
            _bunya = bunya;
            _bunyaName = bunyaName;
            _gyo = gyo;
        }
        
        public String toString() {
            return _bunya + ":" + _bunyaName;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _targetDate; //対象年月日
        private final String _returnDate; //提出期限
        private final String _outDiv; //希望 1:第一希望 2:第二希望
        private final String _questionnairecd; //調査名(アンケートコード)
        private final String[] _classSelected;
        private final String _classSelectedIn;
//        private final boolean _isPrintGoukei;
        private final String _nendo;

        private boolean _isPrintMark; //複数クラス選択マーク「*」

        //系列大学
        private String _mainCollegeCode;
        private String _mainCollegeName;
        //系列学部(E025)
        private final List _mainFacultyList;
        private final String _mainFacultyIn;
        //国公立大学(E023)
        private final List _kokuRituCollegeList;
        //私立大学(E024)
        private final List _shiRituList;
        //専門(E009:分野)
        private final List _senmonList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _targetDate = request.getParameter("DATE").replace('/', '-');
            _returnDate = request.getParameter("DATE2");
            _outDiv = request.getParameter("OUT_DIV");
            _questionnairecd = request.getParameter("CHOUSA");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _classSelectedIn = getClassSelectedIn();
//            String outDiv = request.getParameter("OUT_DIV");
//            _isPrintGoukei = "1".equals(outDiv);
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year));
            _nendo = gengou + "年度";
            setMainCollege(db2);
            _mainFacultyList = createMainFaculty(db2);
            _mainFacultyIn = getMainFacultyIn();
            _kokuRituCollegeList = createKokuRituCollege(db2);
            _shiRituList = createShiRitu(db2);
            _senmonList = createSenmon(db2);
        }

        private String getClassSelectedIn() {
            StringBuffer stb = new StringBuffer();
            _isPrintMark = false;
            stb.append("(");
            for (int i = 0; i < _classSelected.length; i++) {
                if (0 < i) {
                    _isPrintMark = true;
                    stb.append(",");
                }
                stb.append("'" + _classSelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }

        private String getTargetDate() {
            if (null == _targetDate || "".equals(_targetDate)) return "";
            return KNJ_EditDate.h_format_JP(_targetDate);
        }

        private String getReturnDate() {
            if (null == _returnDate || "".equals(_returnDate)) return "";
            return KNJ_EditDate.h_format_JP_MD(_returnDate) + "（" + KNJ_EditDate.h_format_W(_returnDate) + "）";
        }

        private String getMainCollegeName() {
            return (null == _mainCollegeName) ? "" : _mainCollegeName;
        }

        private void setMainCollege(final DB2UDB db2) throws SQLException {
            _mainCollegeCode = null;
            _mainCollegeName = null;
            final String sql = sqlMainCollege();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _mainCollegeCode = rs.getString("ABBV3");
                    _mainCollegeName = rs.getString("SCHOOL_NAME");
                }
            } catch (final Exception ex) {
                log.error("系列大学のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlMainCollege() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L1.ABBV3, ");
            stb.append("     L2.SCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     NAME_MST L1 ");
            stb.append("     LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.ABBV3 ");
            stb.append(" WHERE ");
            stb.append("     L1.NAMECD1 = 'Z010' AND ");
            stb.append("     L1.NAMECD2 = '00' ");
            return stb.toString();
        }

        private List createMainFaculty(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlMainFaculty();
            log.debug(sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int cnt = 0;
                while (rs.next()) {
                    cnt++;
                    if (MAX_LEN_MAIN_FACULTY < cnt) continue;
                    final String facultycd = rs.getString("FACULTYCD");
                    final String facultynameShow1 = rs.getString("FACULTYNAME_SHOW1");

                    final MainFaculty mainFaculty = new MainFaculty(facultycd, facultynameShow1, cnt);
                    rtn.add(mainFaculty);
                }
            } catch (final Exception ex) {
                log.error("系列学部のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlMainFaculty() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L1.NAME1 AS FACULTYCD, ");
            stb.append("     L2.FACULTYNAME_SHOW1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST L1 ");
            stb.append("     LEFT JOIN COLLEGE_FACULTY_MST L2 ");
            stb.append("          ON  L2.SCHOOL_CD = '" + _mainCollegeCode + "' ");
            stb.append("          AND L2.FACULTYCD = L1.NAME1 ");
            stb.append(" WHERE ");
            stb.append("     L1.NAMECD1 = 'E025' ");
            stb.append("     AND L1.NAME1 IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     L1.NAMECD2 ");
            return stb.toString();
        }

        private String getMainFacultyIn() {
            int i = 0;
            StringBuffer stb = new StringBuffer();
            stb.append("('");
            for (final Iterator it = _mainFacultyList.iterator(); it.hasNext();) {
                final MainFaculty mainFaculty = (MainFaculty) it.next();

                if (0 < i) {
                    stb.append("','");
                }
                stb.append(mainFaculty._facultycd);
                i++;
            }
            stb.append("')");
            return stb.toString();
        }

        private List createKokuRituCollege(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlKokuRituCollege();
            log.debug(sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int cnt = 0;
                while (rs.next()) {
                    cnt++;
                    if (MAX_LEN_KOKURITU_COLLEGE < cnt) continue;
                    final String schoolCd = rs.getString("SCHOOL_CD");
                    final String schoolNameShow1 = rs.getString("SCHOOL_NAME_SHOW1");

                    final KokuRituCollege kokuRituCollege = new KokuRituCollege(schoolCd, schoolNameShow1, cnt);
                    rtn.add(kokuRituCollege);
                }
            } catch (final Exception ex) {
                log.error("国公立のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlKokuRituCollege() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L1.NAME1 AS SCHOOL_CD, ");
            stb.append("     L2.SCHOOL_NAME_SHOW1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST L1 ");
            stb.append("     LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.NAME1 ");
            stb.append(" WHERE ");
            stb.append("     L1.NAMECD1 = 'E023' ");
            stb.append("     AND L1.NAME1 IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     L1.NAMECD2 ");
            return stb.toString();
        }

        private List createShiRitu(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlShiRitu();
            log.debug(sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int cnt = 0;
                while (rs.next()) {
                    cnt++;
                    if (MAX_LEN_SHIRITU < cnt) continue;
                    final String schoolCd = rs.getString("SCHOOL_CD");
                    final String schoolNameShow1 = rs.getString("SCHOOL_NAME_SHOW1");

                    final ShiRitu shiRitu = new ShiRitu(schoolCd, schoolNameShow1, cnt);
                    rtn.add(shiRitu);
                }
            } catch (final Exception ex) {
                log.error("私立のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlShiRitu() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L1.NAME1 AS SCHOOL_CD, ");
            stb.append("     L2.SCHOOL_NAME_SHOW1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST L1 ");
            stb.append("     LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.NAME1 ");
            stb.append(" WHERE ");
            stb.append("     L1.NAMECD1 = 'E024' ");
            stb.append("     AND L1.NAME1 IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     L1.NAMECD2 ");
            return stb.toString();
        }

        private List createSenmon(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlSenmon();
            log.debug(sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int cnt = 0;
                while (rs.next()) {
                    cnt++;
                    if (MAX_LEN_SENMON < cnt) continue;
                    final String bunya = rs.getString("BUNYA");
                    final String bunyaName = rs.getString("NAME1");

                    final Senmon senmon = new Senmon(bunya, bunyaName, cnt);
                    rtn.add(senmon);
                }
            } catch (final Exception ex) {
                log.error("各種学校のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlSenmon() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L1.NAMECD2 AS BUNYA, ");
            stb.append("     L1.NAME1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST L1 ");
            stb.append(" WHERE ");
            stb.append("     L1.NAMECD1 = 'E009' ");
            stb.append("     AND L1.NAMESPARE1 IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     L1.NAMESPARE1 ");
            return stb.toString();
        }

    }
}

// eof
