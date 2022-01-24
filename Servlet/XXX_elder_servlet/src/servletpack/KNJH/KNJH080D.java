/*
 * $Id: f68bf7073b89d72ed35044b6ea08fd604d179ec8 $
 *
 * 作成日: 2017/11/27
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJH080D {

    private static final Log log = LogFactory.getLog(KNJH080D.class);

    private static final String SCHOOLCD_HONKOU = "0";
    private static final String SCHOOLCD_ZENSEKI = "1";
    private static final String SOUGOU_CLASS = "90";

    private boolean _hasData;

    private Param _param;

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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List printList = getStudentList(db2);
        final Map studentMeisaiMap = getMeisaiMap(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final StudentData studentData = (StudentData) iterator.next();

            if (_param._printpage1) {
                print1Page(svf, studentMeisaiMap, studentData);
                _hasData = true;
            }
            if (_param._printpage2) {
                print2Page(svf, studentMeisaiMap, studentData);
                _hasData = true;
            }
            if (_param._printpage3) {
                print3Page(svf, studentMeisaiMap, studentData);
                _hasData = true;
            }
        }
    }

    private StudentMeisaiData printHeadInfo(final Vrw32alp svf, final int paneno, final Map studentMeisaiMap, final StudentData studentData) {
    	//学籍番号
        svf.VrsOut("SCHREGNO", studentData._schregno);
        svf.VrsOut("MAJORNAME", studentData._majorname);
        int outputstrid = KNJ_EditEdit.getMS932ByteLength(studentData._kana) > 26 ? 2 : 1;
        svf.VrsOut("NAME1_" + outputstrid, studentData._kana);
        outputstrid = KNJ_EditEdit.getMS932ByteLength(studentData._name) > 26 ? 2 : 1;
        svf.VrsOut("NAME2_" + outputstrid, studentData._name);
        outputstrid = KNJ_EditEdit.getMS932ByteLength(studentData._eng) > 26 ? 2 : 1;
        svf.VrsOut("NAME3_" + outputstrid, studentData._eng);
        svf.VrsOut("SEX", studentData._sex);

        //生徒明細
        final StudentMeisaiData meisaiData = (StudentMeisaiData) studentMeisaiMap.get(studentData._schregno);

        //年組
        int ghCnt = 1;
        for (Iterator itGradeHr = meisaiData._gradeHrList.iterator(); itGradeHr.hasNext();) {
            final GradeHr gradeHr = (GradeHr) itGradeHr.next();
            svf.VrsOut("HR_ATTNO" + ghCnt, gradeHr._hrName + "　" + gradeHr._attendno + "番");
            final String courseField = KNJ_EditEdit.getMS932ByteLength(gradeHr._coursecodeName) > 20 ? "_2" : "";
            svf.VrsOut("COURSECODENAME" + ghCnt + courseField, gradeHr._coursecodeName);
            ghCnt++;
        }
        return meisaiData;
    }

    private void print1Page(final Vrw32alp svf, final Map studentMeisaiMap, final StudentData studentData) {
        svf.VrSetForm("KNJH080D_1.frm", 4);
        StudentMeisaiData meisaiData = printHeadInfo(svf, 1, studentMeisaiMap, studentData);

        String strdir1 = "P" + studentData._schregno + "." + _param._extention;
        String strdir2 = _param._documentroot + "/" + _param._folder + "/" + strdir1;
        File f1 = new File(strdir2);   //写真データ存在チェック用

        //写真
        if (f1.exists()) {
            svf.VrsOut("BITMAP", strdir2);
        } else{
            svf.VrsOut("BITMAP", "");
        }

        //特待
        svf.VrsOut("TESTDIV1", studentData._testdivname);
        svf.VrsOut("TESTDIV2", studentData._scholarshipName);
        //実技判定
        svf.VrsOut("JITSUGI", studentData._practicaljudge);
        //総得点
        svf.VrsOut("TOTAL_SCORE", studentData._totalpoint);

        //備考
        svf.VrsOut("REMARK1", studentData._remark1);
        svf.VrsOut("REMARK2", studentData._remark2);
        svf.VrsOut("REMARK3", studentData._remark3);

        //平均点
        int ghCnt = 1;
        for (Iterator itGradeHr = meisaiData._gradeHrList.iterator(); itGradeHr.hasNext();) {
            final GradeHr gradeHr = (GradeHr) itGradeHr.next();
            if (!"".equals(gradeHr._avg)) {
                final BigDecimal setVal = new BigDecimal(gradeHr._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
                svf.VrsOut("AVERAGE" + ghCnt, setVal.toString());
            }
            ghCnt++;
        }

        // 校内実力テスト平均点
        for (Iterator ite_eff = meisaiData._efficiecyList.iterator(); ite_eff.hasNext();) {
            final efficiency effobj = (efficiency) ite_eff.next();

            if ("01".equals(effobj._gradecd)) {
                svf.VrsOut("MOCK_NUM1", "(" + effobj._cnt + "回)");
                svf.VrsOut("MOCK_AVERAGE1", effobj._avg);
            }
            if ("02".equals(effobj._gradecd)) {
                svf.VrsOut("MOCK_NUM2", "(" + effobj._cnt + "回)");
                svf.VrsOut("MOCK_AVERAGE2", effobj._avg);
            }
        }

        // 単位未履修科目
        for (Iterator ite_ufc = meisaiData._unfinishcreditList.iterator(); ite_ufc.hasNext();) {
            final UnFinishCredit ufcobj = (UnFinishCredit) ite_ufc.next();
            svf.VrsOut("GRADE", ufcobj._grade);
            int outputstrid = KNJ_EditEdit.getMS932ByteLength(ufcobj._subclassname) > 20 ? 2 : 1;
            svf.VrsOut("SUBCLASS_NAME1_" + outputstrid, ufcobj._subclassname);
            outputstrid = KNJ_EditEdit.getMS932ByteLength(ufcobj._classnameeng) > 20 ? 2 : 1;
            svf.VrsOut("SUBCLASS_NAME2_" + outputstrid, ufcobj._classnameeng);
            svf.VrsOut("CREDIT", ufcobj._credit);
            svf.VrEndRecord();
        }
        if (meisaiData._unfinishcreditList.size() == 0) {
            svf.VrsOut("BLANK", "1");
            svf.VrEndRecord();
        }

        //処分記録
        String sep = "";
        int syobunCnt = 1;
        for (Iterator itGradeHr = meisaiData._syobunList.iterator(); itGradeHr.hasNext();) {
            final SyouBatsu syobun = (SyouBatsu) itGradeHr.next();
            svf.VrsOut("PUNISHMENT" + syobunCnt, syobun._detailSdate + "　" + syobun._detailName + "　" + syobun._content);
            syobunCnt++;
        }

        //PTA
        int gradeCnt = 1;
        for (Iterator itIinkaiGHR = meisaiData._gradeHrList.iterator(); itIinkaiGHR.hasNext();) {
            final GradeHr gradeHr = (GradeHr) itIinkaiGHR.next();

            String setIinkai = "";
            sep = "";
            boolean yearExists = false;
            for (Iterator itPta = meisaiData._ptaList.iterator(); itPta.hasNext();) {
                final Committee committee = (Committee) itPta.next();
                if (committee._year.equals(gradeHr._year)) {
                    setIinkai += sep + committee._year + committee._committeeName + "(" + committee._semester + ")" + committee._executiveName;
                    sep = "、";
                    yearExists = true;
                }
            }
            svf.VrsOut("PTA" + gradeCnt, setIinkai);
            if (yearExists) {
                gradeCnt++;
            }
        }
        svf.VrEndPage();
    }

    private void print2Page(final Vrw32alp svf, final Map studentMeisaiMap, final StudentData studentData) {
        svf.VrSetForm("KNJH080D_2.frm", 4);
        final StudentMeisaiData meisaiData = printHeadInfo(svf, 2, studentMeisaiMap, studentData);

        svf.VrsOut("BLANK", "1");
        svf.VrEndRecord();
        //希望進路
        int totalCnt = 1;
        totalCnt = kartePrint(svf, meisaiData._kibouList, "希望進路/Post Graduation Goals", "1", totalCnt);
        svf.VrsOut("BLANK", "1");
        svf.VrEndRecord();

        //特記事項
        totalCnt = kartePrint(svf, meisaiData._tokkiList, "特記事項", "2", totalCnt);
        svf.VrsOut("BLANK", "1");
        svf.VrEndRecord();

        //指導・面談の記録
        totalCnt = kartePrint(svf, meisaiData._mendanList, "指導・面談の記録/Record of Interviews", "3", totalCnt);
        svf.VrsOut("BLANK", "1");
        svf.VrEndRecord();

        //家庭環境
        totalCnt = kartePrint(svf, meisaiData._familyenvList, "家庭環境/Particular Incidents", "4", totalCnt);
        svf.VrsOut("BLANK", "1");
        svf.VrEndRecord();

        //アルバイト
        totalCnt = ptjobPrint(svf, meisaiData._ptjobList, "アルバイト/Part Time Job", "5", totalCnt);
        svf.VrEndPage();
    }

    private void print3Page(final Vrw32alp svf, final Map studentMeisaiMap, final StudentData studentData) {

    	svf.VrSetForm("KNJH080D_3.frm", 1);
        StudentMeisaiData meisaiData = printHeadInfo(svf, 3, studentMeisaiMap, studentData);

        //総合的な学習の時間(学習活動)
        String tststr1 = "";
        String tststr2 = "";
        String tststr3 = "";
        int tstidx1 = 1;
        int tstidx2 = 1;
        int tstidx3 = 1;
        String delimstr1 = "";
        String delimstr2 = "";
        String delimstr3 = "";
        for (Iterator it_tst = meisaiData._totalstudyactList.iterator(); it_tst.hasNext();) {
            final TotalStudyAct tstobj = (TotalStudyAct) it_tst.next();
            if (tstidx1 <= 3 && "01".equals(tstobj._gradecd)) {
            	tststr1 += delimstr1 + tstobj._totalstudyact;
            	delimstr1 = "、";
            }
            if (tstidx2 <= 3 && "02".equals(tstobj._gradecd)) {
            	tststr2 += delimstr2 + tstobj._totalstudyact;
            	delimstr2 = "、";
            }
            if (tstidx3 <= 3 && "03".equals(tstobj._gradecd)) {
            	tststr3 += delimstr3 + tstobj._totalstudyact;
            	delimstr3 = "、";
            }
        }
        final String[] tstcutstr1 = KNJ_EditEdit.get_token(tststr1, 94, 3);
        final String[] tstcutstr2 = KNJ_EditEdit.get_token(tststr2, 94, 3);
        final String[] tstcutstr3 = KNJ_EditEdit.get_token(tststr3, 94, 3);
        for (int tstidx = 0;tstidx < 3;tstidx++) {
        	if (tstcutstr1 != null && tstidx < tstcutstr1.length) {
                svf.VrsOut("TOTAL_ACT1_"+tstidx, tstcutstr1[tstidx]);
        	}
        	if (tstcutstr2 != null && tstidx < tstcutstr2.length) {
                svf.VrsOut("TOTAL_ACT2_"+tstidx, tstcutstr2[tstidx]);
        	}
        	if (tstcutstr3 != null && tstidx < tstcutstr3.length) {
                svf.VrsOut("TOTAL_ACT3_"+tstidx, tstcutstr3[tstidx]);
        	}
        }

        //部活動
        String setClub = "";
        String beoreClub = "";
        String sep = "";
        for (Iterator itGradeHr = meisaiData._clubList.iterator(); itGradeHr.hasNext();) {
            final Club club = (Club) itGradeHr.next();
            if (!"".equals(club._clubName) && !beoreClub.equals(club._clubName)) {
                setClub += sep + club._clubName + (!"".equals(club._executiveName) ? "(" + club._executiveName + ")" : "");
                sep = "、";
                beoreClub = club._clubName;
            }
        }
        final String[] clubVal = KNJ_EditEdit.get_token(setClub, 94, 3);
        if (clubVal != null) {
            for (int i = 0; i < clubVal.length; i++) {
                svf.VrsOut("CLUB" + String.valueOf(i + 1), clubVal[i]);
            }
        }

        //大会成績
        String setTaikai = "";
        sep = "";
        for (Iterator itGradeHr = meisaiData._taikaiList.iterator(); itGradeHr.hasNext();) {
            final Taikai taikai = (Taikai) itGradeHr.next();
            setTaikai += sep + taikai._detailDate + "　" + taikai._clubName + "　" + taikai._meetName + "　" + taikai._recordName;
            sep = "、";
        }
        final String[] taikaiVal = KNJ_EditEdit.get_token(setTaikai, 94, 2);
        if (taikaiVal != null) {
            for (int i = 0; i < taikaiVal.length; i++) {
                svf.VrsOut("RESULTS" + String.valueOf(i + 1), taikaiVal[i]);
            }
        }

        //賞
        String setSyou = "";
        sep = "";
        for (Iterator itGradeHr = meisaiData._syouList.iterator(); itGradeHr.hasNext();) {
            final SyouBatsu syou = (SyouBatsu) itGradeHr.next();
            setSyou += sep + syou._detailSdate + "　" + syou._detailName + "　" + syou._content;
            sep = "、";
        }
        final String[] syouVal = KNJ_EditEdit.get_token(setSyou, 94, 3);
        if (syouVal != null) {
            for (int i = 0; i < syouVal.length; i++) {
                svf.VrsOut("PRIZE" + String.valueOf(i + 1), syouVal[i]);
            }
        }

        //ボランティア
        String setVol = "";
        sep = "";
        for (Iterator it_vol = meisaiData._volunteerList.iterator(); it_vol.hasNext();) {
            final SyouBatsu volobj = (SyouBatsu) it_vol.next();
            setVol += sep + volobj._detailSdate + "　" + volobj._detailName + "　" + volobj._content;
            sep = "、";
        }
        final String[] volVal = KNJ_EditEdit.get_token(setVol, 94, 2);
        if (volVal != null) {
            for (int i = 0; i < volVal.length; i++) {
                svf.VrsOut("VOLUNTEER" + String.valueOf(i + 1), volVal[i]);
            }
        }

        //留学
        String setTrn = "";
        sep = "";
        for (Iterator it_trn = meisaiData._transferList.iterator(); it_trn.hasNext();) {
            final Transfer trnobj = (Transfer) it_trn.next();
            setTrn += sep + trnobj._transfersdate + "～" + trnobj._transferedate + "　" + trnobj._transferaddr + "　" + trnobj._transferplace;
            sep = "、";
        }
        final String[] trnVal = KNJ_EditEdit.get_token(setTrn, 94, 2);
        if (trnVal != null) {
            for (int i = 0; i < trnVal.length; i++) {
                svf.VrsOut("ABROAD" + String.valueOf(i + 1), trnVal[i]);
            }
        }

        //海外研修
        String setOst = "";
        sep = "";
        for (Iterator it_ost = meisaiData._ostrainingList.iterator(); it_ost.hasNext();) {
            final SyouBatsu ostobj = (SyouBatsu) it_ost.next();
            setOst += sep + ostobj._detailSdate + "　" + ostobj._detailName + "　" + ostobj._content;
            sep = "、";
        }
        final String[] ostVal = KNJ_EditEdit.get_token(setOst, 94, 2);
        if (ostVal != null) {
            for (int i = 0; i < ostVal.length; i++) {
                svf.VrsOut("ABROAD" + String.valueOf(i + 1), ostVal[i]);
            }
        }

        //生徒会
        String cclstr1 = "";
        String cclstr2 = "";
        String cclstr3 = "";
        delimstr1 = "";
        delimstr2 = "";
        delimstr3 = "";
        for (Iterator itcouncil = meisaiData._councilList.iterator(); itcouncil.hasNext();) {
            final Council cclobj = (Council) itcouncil.next();
            if (!"5".equals(cclobj._committeeflg)) continue;
            if ("01".equals(cclobj._grade)) {
             	cclstr1 += delimstr1 + cclobj._positionnm;
              	delimstr1 = "、";
            }
            if ("02".equals(cclobj._grade)) {
               	cclstr2 += delimstr2 + cclobj._positionnm;
               	delimstr2 = "、";
            }
            if ("03".equals(cclobj._grade)) {
               	cclstr3 += delimstr3 + cclobj._positionnm;
               	delimstr3 = "、";
            }
        }
        svf.VrsOut("COMMITTEE1", cclstr1);
        svf.VrsOut("COMMITTEE2", cclstr2);
        svf.VrsOut("COMMITTEE3", cclstr3);

        //学級
        int gradeCnt = 1;
        for (Iterator itIinkaiGHR = meisaiData._gradeHrList.iterator(); itIinkaiGHR.hasNext();) {
            final GradeHr gradeHr = (GradeHr) itIinkaiGHR.next();

            //学級
            String setClassIinkai = "";
            sep = "";
            for (Iterator itIinkai = meisaiData._classIinkaiList.iterator(); itIinkai.hasNext();) {
                final Committee committee = (Committee) itIinkai.next();
                if (committee._year.equals(gradeHr._year)) {
                    setClassIinkai += sep + committee._committeeName + (!"".equals(committee._executiveName) ? "(" + committee._executiveName + ")" : "");
                    sep = "、";
                }
            }

            svf.VrsOut("CR_JOB" + gradeCnt, setClassIinkai);
            gradeCnt++;
        }
        //資格
        String setqlc = "";
        sep = "";
        for (Iterator it_qlc = meisaiData._qualificationList.iterator(); it_qlc.hasNext();) {
            final Qualification qlcobj = (Qualification) it_qlc.next();
            setqlc += sep + qlcobj._regddate + "　" + qlcobj._qualifiedname + "　" + qlcobj._result;
            sep = "、";
        }
        final String[] qlcVal = KNJ_EditEdit.get_token(setqlc, 94, 3);
        if (qlcVal != null) {
            for (int i = 0; i < qlcVal.length; i++) {
                svf.VrsOut("LISENCE" + String.valueOf(i + 1), qlcVal[i]);
            }
        }

        svf.VrEndPage();

    }

    private int ptjobPrint(final Vrw32alp svf, final List karteList, final String title, final String titleDiv, final int kibouCnt) {
        svf.VrsOut("TITLE_SHIRO", titleDiv);
        svf.VrsOut("DATE_SHIRO", String.valueOf(kibouCnt+200));
        svf.VrsOut("TITLE", title);
        int retCnt = kibouCnt;
        for (Iterator itGradeHr = karteList.iterator(); itGradeHr.hasNext();) {
            final SyouBatsu ptjobobj = (SyouBatsu) itGradeHr.next();
            svf.VrsOut("REMARK_SHIRO", String.valueOf(retCnt+400));
            svf.VrsOut("DATE_SHIRO", String.valueOf(retCnt+200));
            svf.VrsOut("DATE", ptjobobj._detailSdate);

            final String[] contentVal = KNJ_EditEdit.get_token(ptjobobj._content, 80, 5);
            if (contentVal != null) {
                for (int i = 0; i < contentVal.length; i++) {
                    if (null == contentVal[i]) {
                        continue;
                    }
                    svf.VrsOut("REMARK", contentVal[i]);
                    svf.VrsOut("DATE_SHIRO", String.valueOf(retCnt+200));
                    svf.VrsOut("REMARK_SHIRO", String.valueOf(retCnt+400));
                    //svf.VrsOut("DATE", String.valueOf(retCnt));
                    //svf.VrsOut("TITLE_SHIRO", titleDiv);
                    svf.VrEndRecord();
                }
            } else {
                svf.VrsOut("REMARK_SHIRO", String.valueOf(retCnt+400));
                svf.VrsOut("DATE_SHIRO", String.valueOf(retCnt+200));
                svf.VrsOut("TITLE_SHIRO", titleDiv);
                svf.VrEndRecord();
            }

            retCnt++;
        }
        if (retCnt == kibouCnt) {
            svf.VrsOut("REMARK_SHIRO", String.valueOf(retCnt+400));
            svf.VrsOut("DATE_SHIRO", String.valueOf(retCnt+200));
            svf.VrsOut("TITLE_SHIRO", titleDiv);
            svf.VrEndRecord();
            retCnt++;
        }
        return retCnt;
    }

    private int kartePrint(final Vrw32alp svf, final List karteList, final String title, final String titleDiv, final int kibouCnt) {
        svf.VrsOut("TITLE_SHIRO", titleDiv);
        svf.VrsOut("DATE_SHIRO", String.valueOf(kibouCnt+200));
        svf.VrsOut("TITLE", title);
        int retCnt = kibouCnt;
        for (Iterator itGradeHr = karteList.iterator(); itGradeHr.hasNext();) {
            final Karte karte = (Karte) itGradeHr.next();
            svf.VrsOut("REMARK_SHIRO", String.valueOf(retCnt+400));
            svf.VrsOut("DATE_SHIRO", String.valueOf(retCnt+200));
            svf.VrsOut("DATE", karte._trainDate);

            final String[] contentVal = KNJ_EditEdit.get_token(karte._content, 80, 5);
            if (contentVal != null) {
                for (int i = 0; i < contentVal.length; i++) {
                    if (null == contentVal[i]) {
                        continue;
                    }
                    svf.VrsOut("REMARK", contentVal[i]);
                    svf.VrsOut("DATE_SHIRO", String.valueOf(retCnt+200));
                    svf.VrsOut("REMARK_SHIRO", String.valueOf(retCnt+400));
                    //svf.VrsOut("DATE", String.valueOf(retCnt));
                    //svf.VrsOut("TITLE_SHIRO", titleDiv);
                    svf.VrEndRecord();
                }
            } else {
                svf.VrsOut("REMARK_SHIRO", String.valueOf(retCnt+400));
                svf.VrsOut("DATE_SHIRO", String.valueOf(retCnt+200));
                svf.VrsOut("TITLE_SHIRO", titleDiv);
                svf.VrEndRecord();
            }

            retCnt++;
        }
        if (retCnt == kibouCnt) {
            svf.VrsOut("REMARK_SHIRO", String.valueOf(retCnt+400));
            svf.VrsOut("DATE_SHIRO", String.valueOf(retCnt+200));
            svf.VrsOut("TITLE_SHIRO", titleDiv);
            svf.VrEndRecord();
            retCnt++;
        }
        return retCnt;
    }

    private List getStudentList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PreparedStatement psScholar = null;
        ResultSet rsScholar = null;
        PreparedStatement pspj = null;
        ResultSet rspj = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String kana = rs.getString("NAME_KANA");
                final String eng = rs.getString("NAME_ENG");
                final String sex = rs.getString("SEX");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String majorname = rs.getString("MAJORNAME");
                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");
                final String remark3 = rs.getString("REMARK3");
                final String entYear = rs.getString("ENT_YEAR");
                final String testdivname = StringUtils.defaultString(rs.getString("TESTDIV_NAME"));
                final String totalpoint = rs.getString("TOTALPOINT");

                final String scholarSql = getScholarSql(schregno);
                log.debug(" getScholarSql =" + scholarSql);
                psScholar = db2.prepareStatement(scholarSql);
                rsScholar = psScholar.executeQuery();
                String scholarshipName = "";
                while (rsScholar.next()) {
                    scholarshipName = rsScholar.getString("SCHOLARSHIP_NAME");
                }

                final String pjsql = getPracticalJudgesql(schregno);
                log.debug(" getPracticalJudgesql =" + pjsql);
                pspj = db2.prepareStatement(pjsql);
                rspj = pspj.executeQuery();
                String practicaljudge = "";
                while (rspj.next()) {
                	practicaljudge = StringUtils.defaultString(rspj.getString("REMARK3"));
                }

                final StudentData studentData = new StudentData(schregno, grade, hrClass, attendno, name, kana, eng, sex, scholarshipName, coursecd, majorcd, majorname, remark1, remark2, remark3, entYear, testdivname, totalpoint, practicaljudge);
                retList.add(studentData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            DbUtils.closeQuietly(null, psScholar, rsScholar);
            DbUtils.closeQuietly(null, pspj, rspj);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_SCHOLARSHIP AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SCHOLARSHIP, ");
        stb.append("     meisyou_get(SCHOLARSHIP,'A044',1) SCHOLARSHIP_NAME, ");
        stb.append("     FROM_DATE, ");
        stb.append("     TO_DATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_SCHOLARSHIP_HIST_DAT ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO IN (" + _param._inState + ") ");
        stb.append("     AND '" + _param._ctrlDate + "' BETWEEN FROM_DATE AND VALUE(TO_DATE, '9999-12-31') ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREGNO, ");
        stb.append("     FROM_DATE DESC ");
        stb.append(" FETCH FIRST 1 ROWS ONLY ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.NAME_ENG, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     SCHOLARSHIP.SCHOLARSHIP_NAME, ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     BASE.REMARK1, ");
        stb.append("     BASE.REMARK2, ");
        stb.append("     BASE.REMARK3, ");
        stb.append("     NM045.NAME1 AS TESTDIV_NAME, ");
        stb.append("     ERD.TOTAL4 AS TOTALPOINT, ");
        stb.append("     FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON REGD.COURSECD = MAJOR.COURSECD ");
        stb.append("          AND REGD.MAJORCD = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN MAX_SCHOLARSHIP SCHOLARSHIP ON REGD.SCHREGNO = SCHOLARSHIP.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST SBDM ON REGD.SCHREGNO = SBDM.SCHREGNO AND SBDM.BASE_SEQ = '003' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT EABD ON EABD.ENTEXAMYEAR = CHAR(YEAR(BASE.ENT_DATE)) ");
        stb.append("          AND EABD.EXAMNO = SBDM.BASE_REMARK1  ");
        stb.append("     LEFT JOIN NAME_MST NM045 ON NM045.NAMECD1 = 'L045' AND NM045.NAMECD2 = EABD.TESTDIV0 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT ERD ON ERD.ENTEXAMYEAR = EABD.ENTEXAMYEAR ");
        stb.append("          AND ERD.APPLICANTDIV = EABD.APPLICANTDIV ");
        stb.append("          AND ERD.TESTDIV = EABD.TESTDIV ");
        stb.append("          AND ERD.RECEPTNO = EABD.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND REGD.SCHREGNO IN (" + _param._inState + ") ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    private String getScholarSql(final String schregNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SCHOLARSHIP, ");
        stb.append("     meisyou_get(SCHOLARSHIP,'A044',1) SCHOLARSHIP_NAME, ");
        stb.append("     FROM_DATE, ");
        stb.append("     TO_DATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_SCHOLARSHIP_HIST_DAT ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO = '" + schregNo + "' ");
        stb.append("     AND '" + _param._ctrlDate + "' BETWEEN FROM_DATE AND VALUE(TO_DATE, '9999-12-31') ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREGNO, ");
        stb.append("     FROM_DATE DESC ");
        stb.append(" FETCH FIRST 1 ROWS ONLY ");
        return stb.toString();
    }

    private Map getMeisaiMap(final DB2UDB db2) {
        final Map retmap = new HashMap();

        setGradeHr(db2, retmap);
        setTaikai(db2, retmap);
        setSyou(db2, retmap);
        setBatsu(db2, retmap);
        setClub(db2, retmap);
        setIinkai(db2, retmap);
        setClassIinkai(db2, retmap);
        setPta(db2, retmap);
        setKibou(db2, retmap);
        setTokki(db2, retmap);
        setMendan(db2, retmap);
        //実力テスト
        setefficiency(db2, retmap);
        //ボランティア
        setVolunteer(db2, retmap);
        //アルバイト
        setPtJob(db2, retmap);
        //海外研修
        setOsTraining(db2, retmap);
        //未履修科目
        setUnFinishCredit(db2, retmap);
        //総合的な学習
        setTotalStudyAct(db2, retmap);
        //留学
        setTransfer(db2, retmap);
        //生徒会
        setCouncil(db2, retmap);
        //資格
        setQualification(db2, retmap);
        //家庭環境
        setfamilyenv(db2, retmap);

        return retmap;
    }

    private void setGradeHr(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getGradeHrSql();
            log.debug(" getGradeHrSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String semester = rs.getString("SEMESTER");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");
                final String coursecodeName = rs.getString("COURSECODENAME");
                final String avg = StringUtils.defaultString(rs.getString("AVG"));
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final GradeHr gradeHr = new GradeHr(schregno, year, semester, grade, hrClass, hrName, attendno, coursecd, majorcd, coursecode, coursecodeName, avg);
                meisaiData._gradeHrList.add(gradeHr);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getGradeHrSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH GRADE_YEAR AS ( ");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     MAX(REGD.YEAR) AS YEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("           AND REGD.GRADE = GDAT.GRADE ");
        stb.append("           AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append(" WHERE ");
        stb.append("     REGD.SCHREGNO IN (" + _param._inState + ") ");
        stb.append("     AND REGD.YEAR <= '" + _param._ctrlYear + "' ");
        stb.append(" GROUP BY ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE ");
        stb.append(" ), MAX_REGD AS ( ");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.YEAR, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     MAX(REGD.SEMESTER) AS SEMESTER ");
        stb.append(" FROM ");
        stb.append("     GRADE_YEAR GY, ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append(" WHERE ");
        stb.append("     GY.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     AND GY.GRADE = REGD.GRADE ");
        stb.append("     AND GY.YEAR = REGD.YEAR ");
        stb.append(" GROUP BY ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.YEAR, ");
        stb.append("     REGD.GRADE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.YEAR, ");
        stb.append("     REGD.SEMESTER, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     REGD.COURSECODE, ");
        stb.append("     COURSECODE.COURSECODENAME, ");
        stb.append("     REC.AVG ");
        stb.append(" FROM ");
        stb.append("     MAX_REGD, ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSECODE ON REGD.COURSECODE = COURSECODE.COURSECODE ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT REC ON REGD.YEAR = REC.YEAR ");
        stb.append("          AND REC.SEMESTER = '9' ");
        stb.append("          AND REC.TESTKINDCD = '99' ");
        stb.append("          AND REC.TESTITEMCD = '00' ");
        stb.append("          AND REC.SCORE_DIV = '08' ");
        stb.append("          AND REC.CLASSCD = '99' ");
        stb.append("          AND REC.SUBCLASSCD = '999999' ");
        stb.append("          AND REGD.SCHREGNO = REC.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     MAX_REGD.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     AND MAX_REGD.YEAR = REGD.YEAR ");
        stb.append("     AND MAX_REGD.SEMESTER = REGD.SEMESTER ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.YEAR ");

        return stb.toString();
    }

    private void setTaikai(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getTaikaiSql();
            log.debug(" getTaikaiSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String detailDate = rs.getString("DETAIL_DATE");
                final String clubCd = rs.getString("CLUBCD");
                final String clubName = rs.getString("CLUBNAME");
                final String meetName = rs.getString("MEET_NAME");
                final String recordCd = rs.getString("RECORDCD");
                final String recordName = rs.getString("RECORDNAME");
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final Taikai taikai = new Taikai(schregno, detailDate, clubCd, clubName, meetName, recordCd, recordName);
                meisaiData._taikaiList.add(taikai);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getTaikaiSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SCH_CLUB.SCHREGNO, ");
        stb.append("     SCH_CLUB.DETAIL_DATE, ");
        stb.append("     SCH_CLUB.CLUBCD, ");
        stb.append("     CLUB_M.CLUBNAME, ");
        stb.append("     SCH_CLUB.MEET_NAME, ");
        stb.append("     SCH_CLUB.RECORDCD, ");
        stb.append("     CLUB_REC.RECORDNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_CLUB_HDETAIL_DAT SCH_CLUB ");
        stb.append("     LEFT JOIN CLUB_MST CLUB_M ON SCH_CLUB.CLUBCD = CLUB_M.CLUBCD ");
        stb.append("          AND SCH_CLUB.SCHOOLCD = CLUB_M.SCHOOLCD ");
        stb.append("          AND SCH_CLUB.SCHOOL_KIND = CLUB_M.SCHOOL_KIND ");
        stb.append("     LEFT JOIN CLUB_RECORD_MST CLUB_REC ON SCH_CLUB.RECORDCD = CLUB_REC.RECORDCD ");
        stb.append("          AND SCH_CLUB.SCHOOLCD = CLUB_REC.SCHOOLCD ");
        stb.append("          AND SCH_CLUB.SCHOOL_KIND = CLUB_REC.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     SCH_CLUB.SCHREGNO IN (" + _param._inState + ") ");
        stb.append("     AND FISCALYEAR(SCH_CLUB.DETAIL_DATE) <= '" + _param._ctrlYear + "' ");
        stb.append("     AND SCH_CLUB.DIV = '1' ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     SCH_CLUB.SCHREGNO, ");
        stb.append("     SCH_CLUB.DETAIL_DATE, ");
        stb.append("     SCH_CLUB.CLUBCD, ");
        stb.append("     CLUB_M.CLUBNAME, ");
        stb.append("     GRP_CLUB.MEET_NAME, ");
        stb.append("     GRP_CLUB.RECORDCD, ");
        stb.append("     CLUB_REC.RECORDNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_CLUB_HDETAIL_DAT SCH_CLUB ");
        stb.append("     INNER JOIN GROUP_CLUB_HDETAIL_DAT GRP_CLUB ON SCH_CLUB.CLUBCD = GRP_CLUB.CLUBCD ");
        stb.append("           AND SCH_CLUB.SCHOOLCD = GRP_CLUB.SCHOOLCD ");
        stb.append("           AND SCH_CLUB.SCHOOL_KIND = GRP_CLUB.SCHOOL_KIND ");
        stb.append("           AND SCH_CLUB.DETAIL_DATE = GRP_CLUB.DETAIL_DATE ");
        stb.append("           AND SCH_CLUB.GROUPCD = GRP_CLUB.GROUPCD ");
        stb.append("     LEFT JOIN CLUB_MST CLUB_M ON GRP_CLUB.CLUBCD = CLUB_M.CLUBCD ");
        stb.append("          AND GRP_CLUB.SCHOOLCD = CLUB_M.SCHOOLCD ");
        stb.append("          AND GRP_CLUB.SCHOOL_KIND = CLUB_M.SCHOOL_KIND ");
        stb.append("     LEFT JOIN CLUB_RECORD_MST CLUB_REC ON GRP_CLUB.RECORDCD = CLUB_REC.RECORDCD ");
        stb.append("          AND GRP_CLUB.SCHOOLCD = CLUB_REC.SCHOOLCD ");
        stb.append("          AND GRP_CLUB.SCHOOL_KIND = CLUB_REC.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     SCH_CLUB.SCHREGNO IN (" + _param._inState + ") ");
        stb.append("     AND FISCALYEAR(SCH_CLUB.DETAIL_DATE) <= '" + _param._ctrlYear + "' ");
        stb.append("     AND SCH_CLUB.DIV = '2' ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREGNO, ");
        stb.append("     DETAIL_DATE, ");
        stb.append("     CLUBCD ");
        return stb.toString();
    }

    private void setSyou(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getKatudouSql("01");
            log.debug(" getKatudouSql_01 =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String detailSdate = rs.getString("DETAIL_SDATE");
                final String detailCd = rs.getString("DETAILCD");
                final String content = rs.getString("CONTENT");
                final String remark = rs.getString("REMARK");
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final SyouBatsu syou = new SyouBatsu(schregno, year, detailSdate, detailCd, content, remark);
                meisaiData._syouList.add(syou);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setBatsu(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSyouBatsuSql("H304", "2");
            log.debug(" getSyouBatsuSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String detailSdate = rs.getString("DETAIL_SDATE");
                final String detailCd = rs.getString("DETAILCD");
                final String detailName = rs.getString("DETAILNAME");
                final String content = rs.getString("CONTENT");
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final SyouBatsu batsu = new SyouBatsu(schregno, year, detailSdate, detailCd, detailName, content);
                meisaiData._syobunList.add(batsu);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setVolunteer(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getKatudouSql("04");
            log.debug(" getKatudouSql_04 =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String detailSdate = rs.getString("DETAIL_SDATE");
                final String detailCd = rs.getString("DETAILCD");
                final String content = rs.getString("CONTENT");
                final String remark = rs.getString("REMARK");
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final SyouBatsu syou = new SyouBatsu(schregno, year, detailSdate, detailCd, content, remark);
                meisaiData._volunteerList.add(syou);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setPtJob(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getKatudouSql("02");
            log.debug(" getKatudouSql_02 =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String detailSdate = rs.getString("DETAIL_SDATE");
                final String detailCd = rs.getString("DETAILCD");
                final String content = rs.getString("CONTENT");
                final String remark = rs.getString("REMARK");
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final SyouBatsu syou = new SyouBatsu(schregno, year, detailSdate, detailCd, content, remark);
                meisaiData._ptjobList.add(syou);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setOsTraining(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getKatudouSql("03");
            log.debug(" getKatudouSql_03 =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String detailSdate = rs.getString("DETAIL_SDATE");
                final String detailCd = rs.getString("DETAILCD");
                final String content = rs.getString("CONTENT");
                final String remark = rs.getString("REMARK");
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final SyouBatsu syou = new SyouBatsu(schregno, year, detailSdate, detailCd, content, remark);
                meisaiData._ostrainingList.add(syou);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getSyouBatsuSql(final String namecd1, final String detailDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.DETAIL_SDATE, ");
        stb.append("     T1.DETAILCD, ");
        stb.append("     L1.NAME1 AS DETAILNAME, ");
        stb.append("     T1.CONTENT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_DETAILHIST_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = '" + namecd1 + "' ");
        stb.append("          AND T1.DETAILCD = L1.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     YEAR <= '" + _param._ctrlYear + "' ");
        stb.append("     AND SCHREGNO IN (" + _param._inState + ") ");
        stb.append("     AND T1.DETAIL_DIV = '" + detailDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.DETAIL_SDATE ");
        return stb.toString();
    }

    private String getKatudouSql(final String detailCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.DETAIL_SDATE, ");
        stb.append("     T1.DETAILCD, ");
        stb.append("     T1.CONTENT, ");
        stb.append("     T1.REMARK ");
        stb.append(" FROM ");
        stb.append("     SCHREG_DETAILHIST_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     YEAR <= '" + _param._ctrlYear + "' ");
        stb.append("     AND SCHREGNO IN (" + _param._inState + ") ");
        stb.append("     AND T1.DETAIL_DIV = '4' ");
        stb.append("     AND T1.DETAILCD = '" + detailCd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.DETAIL_SDATE ");
        return stb.toString();
    }

    private void setClub(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getClubSql();
            log.debug(" getClubSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String clubCd = rs.getString("CLUBCD");
                final String clubName = StringUtils.defaultString(rs.getString("CLUBNAME"));
                final String sdate = rs.getString("SDATE");
                final String executiveCd = StringUtils.defaultString(rs.getString("EXECUTIVECD"));
                final String executiveName = StringUtils.defaultString(rs.getString("EXECUTIVENAME"));
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final Club batsu = new Club(schregno, clubCd, clubName, sdate, executiveCd, executiveName);
                meisaiData._clubList.add(batsu);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getClubSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLUBCD, ");
        stb.append("     CLUB.CLUBNAME, ");
        stb.append("     T1.SDATE, ");
        stb.append("     T1.EXECUTIVECD, ");
        stb.append("     J001.NAME1 AS EXECUTIVENAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_CLUB_HIST_DAT T1 ");
        stb.append("     LEFT JOIN CLUB_MST CLUB ON T1.CLUBCD = CLUB.CLUBCD ");
        stb.append("          AND T1.SCHOOLCD = CLUB.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = CLUB.SCHOOL_KIND ");
        stb.append("     LEFT JOIN NAME_MST J001 ON J001.NAMECD1 = 'J001' ");
        stb.append("          AND T1.EXECUTIVECD = J001.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO IN (" + _param._inState + ") ");
        stb.append("     AND FISCALYEAR(T1.SDATE) <= '" + _param._ctrlYear + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.SDATE ");
        return stb.toString();
    }

    private void setIinkai(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getCommitteeSql("1");
            log.debug(" getIinkaiSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String committeeCd = rs.getString("COMMITTEECD");
                final String committeeName = StringUtils.defaultString(rs.getString("COMMITTEENAME"));
                final String executiveCd = StringUtils.defaultString(rs.getString("EXECUTIVECD"));
                final String executiveName = StringUtils.defaultString(rs.getString("EXECUTIVENAME"));
                final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final Committee committee = new Committee(schregno, year, committeeCd, committeeName, executiveCd, executiveName, semester);
                meisaiData._iinkaiList.add(committee);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setClassIinkai(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getCommitteeSql("2");
            log.debug(" getClassIinkaiSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String committeeCd = rs.getString("COMMITTEECD");
                final String committeeName = StringUtils.defaultString(rs.getString("COMMITTEENAME"));
                final String executiveCd = StringUtils.defaultString(rs.getString("EXECUTIVECD"));
                final String executiveName = StringUtils.defaultString(rs.getString("EXECUTIVENAME"));
                final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final Committee committee = new Committee(schregno, year, committeeCd, committeeName, executiveCd, executiveName, semester);
                meisaiData._classIinkaiList.add(committee);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setPta(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getCommitteeSql("3");
            log.debug(" getPtaSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String committeeCd = rs.getString("COMMITTEECD");
                final String committeeName = StringUtils.defaultString(rs.getString("COMMITTEENAME"));
                final String executiveCd = StringUtils.defaultString(rs.getString("EXECUTIVECD"));
                final String executiveName = StringUtils.defaultString(rs.getString("EXECUTIVENAME"));
                final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final Committee committee = new Committee(schregno, year, committeeCd, committeeName, executiveCd, executiveName, semester);
                meisaiData._ptaList.add(committee);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getCommitteeSql(final String div) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.COMMITTEECD, ");
        stb.append("     COMMITTEE.COMMITTEENAME, ");
        stb.append("     T1.EXECUTIVECD, ");
        stb.append("     J002.NAME1 AS EXECUTIVENAME, ");
        stb.append("     J004.NAME1 AS SEMESTER ");
        stb.append(" FROM ");
        stb.append("     SCHREG_COMMITTEE_HIST_DAT T1 ");
        stb.append("     LEFT JOIN COMMITTEE_MST COMMITTEE ON T1.COMMITTEECD = COMMITTEE.COMMITTEECD ");
        stb.append("          AND T1.SCHOOLCD = COMMITTEE.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = COMMITTEE.SCHOOL_KIND ");
        stb.append("          AND T1.COMMITTEE_FLG = COMMITTEE.COMMITTEE_FLG ");
        stb.append("     LEFT JOIN NAME_MST J002 ON J002.NAMECD1 = 'J002' ");
        stb.append("          AND T1.EXECUTIVECD = J002.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST J004 ON J004.NAMECD1 = 'J004' ");
        stb.append("          AND T1.SEMESTER = J004.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST J003 ON J003.NAMECD1 = 'J003' ");
        stb.append("          AND J004.NAMESPARE1 = '" + div +  "' ");
        stb.append("          AND J004.NAMECD2 = COMMITTEE.COMMITTEE_FLG ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR <= '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SCHREGNO IN (" + _param._inState + ") ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEQ ");
        return stb.toString();
    }

    private void setKibou(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getKarteSql("1");
            log.debug(" Kibou =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String trainDate = rs.getString("TRAINDATE");
                final String howtotrainCd = StringUtils.defaultString(rs.getString("HOWTOTRAINCD"));
                final String howtotrainName = StringUtils.defaultString(rs.getString("HOWTOTRAINNAME"));
                final String content = StringUtils.defaultString(rs.getString("CONTENT"));
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final Karte karte = new Karte(schregno, year, trainDate, howtotrainCd, howtotrainName, content);
                meisaiData._kibouList.add(karte);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setTokki(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getKarteSql("2");
            log.debug(" Tokki =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String trainDate = rs.getString("TRAINDATE");
                final String howtotrainCd = StringUtils.defaultString(rs.getString("HOWTOTRAINCD"));
                final String howtotrainName = StringUtils.defaultString(rs.getString("HOWTOTRAINNAME"));
                final String content = StringUtils.defaultString(rs.getString("CONTENT"));
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final Karte karte = new Karte(schregno, year, trainDate, howtotrainCd, howtotrainName, content);
                meisaiData._tokkiList.add(karte);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setMendan(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getKarteSql("3");
            log.debug(" Mendan =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String trainDate = rs.getString("TRAINDATE");
                final String howtotrainCd = StringUtils.defaultString(rs.getString("HOWTOTRAINCD"));
                final String howtotrainName = StringUtils.defaultString(rs.getString("HOWTOTRAINNAME"));
                final String content = StringUtils.defaultString(rs.getString("CONTENT"));
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final Karte karte = new Karte(schregno, year, trainDate, howtotrainCd, howtotrainName, content);
                meisaiData._mendanList.add(karte);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setfamilyenv(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getKarteSql("4");
            log.debug(" familyenv =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String trainDate = rs.getString("TRAINDATE");
                final String howtotrainCd = StringUtils.defaultString(rs.getString("HOWTOTRAINCD"));
                final String howtotrainName = StringUtils.defaultString(rs.getString("HOWTOTRAINNAME"));
                final String content = StringUtils.defaultString(rs.getString("CONTENT"));
                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final Karte karte = new Karte(schregno, year, trainDate, howtotrainCd, howtotrainName, content);
                meisaiData._familyenvList.add(karte);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getKarteSql(final String div) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.TRAINDATE, ");
        stb.append("     T1.HOWTOTRAINCD, ");
        stb.append("     H302.NAME1 AS HOWTOTRAINNAME, ");
        stb.append("     T1.CONTENT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TRAINHIST_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST H302 ON H302.NAMECD1 = 'H302' ");
        stb.append("          AND T1.HOWTOTRAINCD = H302.NAMECD2 ");
        stb.append("          AND H302.NAMESPARE1 = '" + div + "' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR <= '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SCHREGNO IN (" + _param._inState + ") ");
        stb.append("     AND T1.HOWTOTRAINCD = '" + "0" + div + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.TRAINDATE ");
        return stb.toString();
    }

    private void setefficiency(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getefficiencysql();
            log.debug(" Efficiency =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String gradecd = StringUtils.defaultString(rs.getString("GRADECD"));
                final String proficiencydivname = StringUtils.defaultString(rs.getString("PROFICIENCYDIVNAME"));
                final String avg = StringUtils.defaultString(rs.getString("AVG"));
                final String cnt = StringUtils.defaultString(rs.getString("CNT"));

                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final efficiency eff = new efficiency(schregno, year, gradecd, proficiencydivname, avg, cnt);
                meisaiData._efficiecyList.add(eff);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getGradeYearsql(final boolean getmaxyear) {
        final StringBuffer stb = new StringBuffer();
        stb.append("   ( SELECT ");
        stb.append("    SRD.SCHREGNO, ");
        stb.append("    SRG.GRADE_CD AS GRADECD, ");
        if (getmaxyear) {
            stb.append("    MAX(SRD.YEAR) AS YEAR ");
        } else {
            stb.append("    SRD.YEAR AS YEAR ");
        }
        stb.append("   FROM ");
        stb.append("    SCHREG_REGD_DAT SRD ");
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT SRG ON SRG.YEAR = SRD.YEAR ");
        stb.append("      AND SRG.GRADE = SRD.GRADE ");
        stb.append("   WHERE ");
        stb.append("    SRD.SCHREGNO IN (" + _param._inState + ") ");
        stb.append("    AND SRG.GRADE_CD = '01' ");
        stb.append("   GROUP BY ");
        stb.append("    SRD.SCHREGNO, ");
        stb.append("    SRD.YEAR, ");
        stb.append("    SRG.GRADE_CD ");
        stb.append("   ORDER BY");
        stb.append("    SRD.YEAR ");
        stb.append("   ) UNION ( ");
        stb.append("   SELECT ");
        stb.append("    SRD.SCHREGNO, ");
        stb.append("    SRG.GRADE_CD AS GRADECD, ");
        if (getmaxyear) {
            stb.append("    MAX(SRD.YEAR) AS YEAR ");
        } else {
            stb.append("    SRD.YEAR AS YEAR ");
        }
        stb.append("   FROM ");
        stb.append("    SCHREG_REGD_DAT SRD ");
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT SRG ON SRG.YEAR = SRD.YEAR ");
        stb.append("      AND SRG.GRADE = SRD.GRADE ");
        stb.append("   WHERE ");
        stb.append("    SRD.SCHREGNO IN (" + _param._inState + ") ");
        stb.append("    AND SRG.GRADE_CD = '02' ");
        stb.append("   GROUP BY ");
        stb.append("    SRD.SCHREGNO, ");
        stb.append("    SRD.YEAR, ");
        stb.append("    SRG.GRADE_CD ");
        stb.append("   ORDER BY");
        stb.append("    SRD.YEAR ");
        stb.append("   ) ");
        return stb.toString();
    }

    private String getefficiencysql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH GETMAXSEM AS ( ");
        stb.append("   SELECT ");
        stb.append("    T1.YEAR, ");
        stb.append("    T1.PROFICIENCYDIV, ");
        stb.append("    T1.SCHREGNO ");
        stb.append("   FROM ");
        stb.append("    PROFICIENCY_RANK_DAT T1 ");
        stb.append("   WHERE ");
        stb.append("    T1.YEAR <= '" + _param._ctrlYear + "' AND T1.YEAR >= CHAR(INTEGER('" + _param._ctrlYear + "')-2) ");
        stb.append("    AND T1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' ");
        stb.append("    AND T1.PROFICIENCY_SUBCLASS_CD = '" + _param._proficiencySubclassCd + "' ");
        stb.append("    AND T1.RANK_DATA_DIV = '01' ");
        stb.append("    AND T1.RANK_DIV = '01' ");
        stb.append("   GROUP BY ");
        stb.append("    T1.YEAR, ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.PROFICIENCYDIV ");
        stb.append(" ), GETGRADEYEAR AS ( ");
        stb.append(getGradeYearsql(true));
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("  GMS.YEAR, ");
        stb.append("  GMS.SCHREGNO, ");
        stb.append("  GGY.GRADECD, ");
        stb.append("  T1.PROFICIENCYDIV, ");
        stb.append("  NM.NAME1 AS PROFICIENCYDIVNAME, ");
        stb.append("  AVG(T1.SCORE) AS AVG, ");
        stb.append("  COUNT(T1.PROFICIENCYCD) AS CNT ");
        stb.append(" FROM ");
        stb.append("  PROFICIENCY_RANK_DAT T1 ");
        stb.append("  INNER JOIN GETMAXSEM GMS ");
        stb.append("    ON  T1.YEAR = GMS.YEAR ");
        stb.append("    AND T1.SCHREGNO = GMS.SCHREGNO ");
        stb.append("    AND T1.PROFICIENCYDIV = GMS.PROFICIENCYDIV ");
        stb.append("  LEFT JOIN NAME_MST NM ");
        stb.append("    ON NM.NAMECD1 = 'H508' ");
        stb.append("    AND T1.PROFICIENCYDIV = NM.NAMECD2 ");
        stb.append("  LEFT JOIN GETGRADEYEAR GGY ON GGY.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("    GGY.YEAR = GMS.YEAR");
        stb.append("    AND T1.SCHREGNO IN (" + _param._inState + ") ");
        stb.append("    AND T1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' ");
        stb.append("    AND T1.PROFICIENCY_SUBCLASS_CD = '" + _param._proficiencySubclassCd + "' ");
        stb.append("    AND T1.RANK_DATA_DIV = '01' ");
        stb.append("    AND T1.RANK_DIV = '01' ");
        stb.append(" GROUP BY ");
        stb.append("  GMS.YEAR, ");
        stb.append("  GMS.SCHREGNO, ");
        stb.append("  GGY.GRADECD, ");
        stb.append("  T1.PROFICIENCYDIV, ");
        stb.append("  NM.NAME1 ");

        return stb.toString();
    }

    private void setUnFinishCredit(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getunfinishcreditsql();
            log.debug(" UnFinishCredit =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String classnameeng = rs.getString("SUBCLASSNAME_ENG");
                final String unuly = StringUtils.defaultString(rs.getString("UNFINISHCREDIT_UNTIL_LAST_YEAR"));

                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final UnFinishCredit ufc = new UnFinishCredit(schregno, grade, subclassname, classnameeng, unuly);
                meisaiData._unfinishcreditList.add(ufc);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getschinfosql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     L1.HR_NAMEABBV || '-' || T1A.ATTENDNO AS HOMEROOMATEND ");
        stb.append("     , T1A.SCHREGNO ");
        stb.append("     , L2.NAME ");
        stb.append("     , L2.GRD_DATE ");
        stb.append("     , T1A.GRADE ");
        stb.append("     , T1A.COURSECD ");
        stb.append("     , T1A.MAJORCD ");
        stb.append("     , T1A.COURSECODE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1A ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1A.YEAR ");
        stb.append("          AND L1.SEMESTER = T1A.SEMESTER ");
        stb.append("          AND L1.GRADE || L1.HR_CLASS = T1A.GRADE || T1A.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1A.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1A.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1A.SEMESTER = '" + _param._ctrlSemester + "' ");

        return stb.toString();
    }

    private String getunfinishcreditsql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCH_INFO AS ( ");
        stb.append(getschinfosql());
        stb.append(" ), ");
        stb.append(" SCH_INF_LAST_YEAR AS ( ");
        stb.append(getSchInfoLastYear());
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     RTRIM(CHAR(INTEGER(L1.GRADE))) || '年' AS GRADE, ");
        stb.append("     L2.SUBCLASSNAME, ");
        stb.append("     L2.SUBCLASSNAME_ENG, ");
        stb.append("     SUM(VALUE(L1.CREDITS, 0)) AS UNFINISHCREDIT_UNTIL_LAST_YEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append("     LEFT JOIN SCH_INF_LAST_YEAR T2 ON T1.YEAR || T1.SCHREGNO = T2.YEAR || T2.SCHREGNO ");
        stb.append("     LEFT JOIN CREDIT_MST L1 ON L1.YEAR = T1.YEAR ");
        stb.append("          AND L1.COURSECD = T2.COURSECD ");
        stb.append("          AND L1.MAJORCD = T2.MAJORCD ");
        stb.append("          AND L1.GRADE = T2.GRADE ");
        stb.append("          AND L1.COURSECODE = T2.COURSECODE ");
        stb.append("          AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD = '" + SCHOOLCD_HONKOU + "' ");
        stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind +  "'");
        stb.append("     AND T1.YEAR < '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.YEAR || T1.SCHREGNO = T2.YEAR || T2.SCHREGNO ");
        stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
        stb.append("     AND VALUE(T1.COMP_CREDIT, 0) = 0 ");
        stb.append("     AND T1.SUBCLASSCD IN (SELECT ");
        stb.append("                               SUBCLASSCD ");
        stb.append("                           FROM ");
        stb.append("                               SUBCLASS_MST ");
        stb.append("                           WHERE ");
        stb.append("                               ELECTDIV <> '1' ");
        stb.append("                          ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append("     , T1.SUBCLASSCD ");
        stb.append("     , L1.GRADE ");
        stb.append("     , L2.SUBCLASSNAME ");
        stb.append("     , L2.SUBCLASSNAME_ENG ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SUBCLASSCD ");

        return stb.toString();
    }

    private String getSchInfoLastYear() {

        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T1.GRADE, ");
        if (_param.isGakuensei()) {
            stb.append("     MAX(T1.YEAR) AS YEAR ");
        } else {
            stb.append("     T1.YEAR ");
        }
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT SRG ON SRG.YEAR = T1.YEAR ");
        stb.append("       AND SRG.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR < '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ");
        if (_param.isGakuensei()) {
            stb.append("     AND SRG.GRADE_CD IN ('01', '02') ");
            stb.append("     AND EXISTS ( ");
            stb.append("            SELECT ");
            stb.append("                'x' ");
            stb.append("            FROM ");
            stb.append("                (SELECT ");
            stb.append("                        T1A.SCHREGNO, ");
            stb.append("                    T1A.GRADE, ");
            stb.append("                    MAX(T1A.YEAR || T1A.SEMESTER) AS YEAR_SEM ");
            stb.append("                FROM ");
            stb.append("                    SCHREG_REGD_DAT T1A");
            stb.append("                    LEFT JOIN SCHREG_REGD_GDAT SRGA ON SRGA.YEAR = T1A.YEAR ");
            stb.append("                      AND SRGA.GRADE = T1A.GRADE ");
            stb.append("                WHERE ");
            stb.append("                    T1A.YEAR < '" + _param._ctrlYear + "' ");
            stb.append("                    AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ");
            stb.append("                        AND SRGA.GRADE IN ('01', '02') ");
            stb.append("                GROUP BY ");
            stb.append("                    T1A.SCHREGNO, ");
            stb.append("                    T1A.GRADE) E1 ");
            stb.append("            WHERE ");
            stb.append("                T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("                AND T1.GRADE = E1.GRADE ");
            stb.append("                AND T1.YEAR || T1.SEMESTER = E1.YEAR_SEM ");
            stb.append("        ) ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        if (_param.isTannisei()) {
            stb.append("     T1.YEAR, ");
        }
        stb.append("     T1.GRADE ");

        return stb.toString();
    }

    private void setTotalStudyAct(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getTotalActsql();
            log.debug(" setTotalStudyAct =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String gradecd = rs.getString("GRADECD");
                final String totalstudyact = rs.getString("TOTALSTUDYACT");

                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }

                final TotalStudyAct tsa = new TotalStudyAct(schregno, year, gradecd, totalstudyact);
                meisaiData._totalstudyactList.add(tsa);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getTotalActsql() {
        final StringBuffer stb = new StringBuffer();
    	stb.append(" WITH GETGRADEYEAR AS ( ");
        stb.append(getGradeYearsql(false));
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     GGY.GRADECD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.TOTALSTUDYACT ");
        stb.append(" FROM ");
        stb.append("     HTRAINREMARK_DAT T1 ");
        stb.append("     LEFT JOIN GETGRADEYEAR GGY ON GGY.YEAR = T1.YEAR ");
        stb.append("     AND GGY.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR <= '2006'      AND ");
        stb.append("     T1.SCHREGNO IN (" + _param._inState + ")       ");
        stb.append(" ORDER BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO ");
        return stb.toString();
    }

    private void setTransfer(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getTransfersql();
            log.debug(" getTransfersql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String transfersdate = rs.getString("TRANSFER_SDATE");
                final String transferedate = rs.getString("TRANSFER_EDATE");
                final String transferaddr = rs.getString("TRANSFERADDR");
                final String transferplace = rs.getString("TRANSFERPLACE");

                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }
                final Transfer tcls = new Transfer(schregno, transfersdate, transferedate, transferaddr, transferplace);
                meisaiData._transferList.add(tcls);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getTransfersql() {
        final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("  SCHREGNO, ");
    	stb.append("  TRANSFERCD, ");
    	stb.append("  TRANSFER_SDATE, ");
    	stb.append("  TRANSFER_EDATE, ");
    	stb.append("  TRANSFERADDR, ");
    	stb.append("  TRANSFERPLACE  ");
    	stb.append(" FROM ");
    	stb.append("  SCHREG_TRANSFER_DAT ");
    	stb.append(" WHERE ");
    	stb.append("  SCHREGNO IN(" + _param._inState + ") ");
    	stb.append("  AND TRANSFERCD = '2' ");
    	stb.append("  AND TRANSFER_EDATE < DATE('" + _param._ctrlDate + "') ");
        return stb.toString();
    }

    private void setCouncil(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getCouncilsql();
            log.debug(" getCouncilsql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String hrnameabbv = rs.getString("HR_NAMEABBV");
                final String positionnm = rs.getString("POSITION_NM");
                final String positionmark = rs.getString("POSITION_MARK");
                final String committeenm = rs.getString("COMMITTEE_NM");
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String committeeflg = rs.getString("COMMITTEE_FLG");
                final String committeecd = rs.getString("COMMITTEECD");
                final String executivecd = rs.getString("EXECUTIVECD");

                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }
                final Council ccls = new Council(name, nameKana, hrnameabbv, positionnm, positionmark, committeenm, schregno, grade, committeeflg, committeecd, executivecd);
                meisaiData._councilList.add(ccls);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getCouncilsql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH COMMITTEE_TBL as ( ");
        stb.append("     SELECT ");
        stb.append("         NAMESPARE1 AS COMMITTEE_FLG, ");
        stb.append("         NAMESPARE2 AS COMMITTEECD ");
        stb.append("     FROM ");
        stb.append("         NAME_MST ");
        stb.append("     WHERE ");
        stb.append("         NAMECD1 = 'J009' ");
        stb.append("     ) ");
        stb.append(" , GETGRADEYEAR AS (");
        stb.append("   SELECT ");
        stb.append("    SRD.SCHREGNO, ");
        stb.append("    SRG.GRADE_CD AS GRADECD, ");
        stb.append("    SRD.YEAR AS YEAR ");
        stb.append("   FROM ");
        stb.append("    SCHREG_REGD_DAT SRD ");
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT SRG ON SRG.YEAR = SRD.YEAR ");
        stb.append("      AND SRG.GRADE = SRD.GRADE ");
        stb.append("   WHERE ");
        stb.append("    SRD.SCHREGNO IN (" + _param._inState + ") ");
        stb.append("   GROUP BY ");
        stb.append("    SRD.SCHREGNO, ");
        stb.append("    SRD.YEAR, ");
        stb.append("    SRG.GRADE_CD ");
        stb.append("   ORDER BY");
        stb.append("    SRD.YEAR ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     SBM.NAME AS NAME, ");
        stb.append("     SBM.NAME_KANA AS NAME_KANA, ");
        stb.append("     SRH.HR_NAMEABBV AS HR_NAMEABBV, ");
        stb.append("     J002_NM.NAME1 AS POSITION_NM, ");
        stb.append("     J002_NM.ABBV3 AS POSITION_MARK, ");
        stb.append("     J003_NM.NAME1 AS COMMITTEE_NM, ");
        stb.append("     SC1.SCHREGNO AS SCHREGNO, ");
        stb.append("     GGY.GRADECD AS GRADE, ");
        stb.append("     SC2.COMMITTEE_FLG AS COMMITTEE_FLG, ");
        stb.append("     SC2.COMMITTEECD AS COMMITTEECD, ");
        stb.append("     SC2.EXECUTIVECD AS EXECUTIVECD ");
        stb.append(" FROM ");
        stb.append(" COMMITTEE_TBL, ");
        stb.append(" SCHREG_REGD_DAT SC1 ");
        stb.append(" INNER JOIN SCHREG_BASE_MST SC3 ON SC3.SCHREGNO = SC1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_COMMITTEE_HIST_DAT SC2 ON SC2.YEAR = SC1.YEAR AND SC2.SCHREGNO = SC1.SCHREGNO AND SC2.SEMESTER = SC1.SEMESTER AND SC2.GRADE = SC1.GRADE ");
        stb.append(" LEFT JOIN SCHREG_BASE_MST SBM ON SBM.SCHREGNO = SC1.SCHREGNO ");
        stb.append(" LEFT JOIN NAME_MST J002_NM ON J002_NM.NAMECD1 = 'J002' AND J002_NM.NAMECD2 = SC2.EXECUTIVECD ");
        stb.append(" LEFT JOIN NAME_MST J003_NM ON J003_NM.NAMECD1 = 'J003' AND J003_NM.NAMECD2 = SC2.COMMITTEE_FLG ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT SRH ON  SRH.YEAR = SC1.YEAR AND SRH.SEMESTER = SC1.SEMESTER AND SRH.GRADE = SC1.GRADE AND SRH.HR_CLASS = SC1.HR_CLASS ");
        stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ");
        stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append(" LEFT JOIN GETGRADEYEAR GGY ON GGY.SCHREGNO = SC1.SCHREGNO AND GGY.YEAR = SC1.YEAR ");
        stb.append(" WHERE ");
        stb.append("    SC1.SCHREGNO IN (" + _param._inState + ") ");
        stb.append("     AND SC1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND SC2.COMMITTEE_FLG = COMMITTEE_TBL.COMMITTEE_FLG ");
        stb.append("     AND SC2.COMMITTEECD = COMMITTEE_TBL.COMMITTEECD ");
        stb.append("     AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append(" ORDER BY ");
        stb.append("	GDAT.SCHOOL_KIND DESC, SC2.COMMITTEECD, CASE WHEN SC2.EXECUTIVECD IS NULL THEN 0 ELSE 1 END DESC, EXECUTIVECD DESC  ");

        return stb.toString();
    }

    private void setQualification(final DB2UDB db2, final Map retmap) {
        StudentMeisaiData meisaiData;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getQualificationsql();
            log.debug(" getQualificationsql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String year = rs.getString("YEAR");
                final String regddate = rs.getString("REGDDATE");
                final String qualifiedname = rs.getString("QUALIFIED_NAME");
                final String result = StringUtils.defaultString(rs.getString("RESULT"));

                if (!retmap.containsKey(schregno)) {
                    meisaiData = new StudentMeisaiData(schregno);
                    retmap.put(schregno, meisaiData);
                } else {
                    meisaiData = (StudentMeisaiData) retmap.get(schregno);
                }
                final Qualification qcls = new Qualification(year, schregno, regddate, qualifiedname, result);
                meisaiData._qualificationList.add(qcls);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getQualificationsql() {
        final StringBuffer stb = new StringBuffer();
    	stb.append(" WITH NM312 AS ( ");
    	stb.append("     SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'H312' ");
    	stb.append(" ), QDATA_MERGE AS ( ");
    	stb.append(" SELECT  ");
    	stb.append("   SQTD.YEAR, ");
    	stb.append("   SQTD.SCHREGNO, ");
    	stb.append("   SQTD.QUALIFIED_CD, ");
    	stb.append("   SQTD.TEST_DATE AS REGDDATE, ");
    	stb.append("   SQTD.TEST_CD AS RANK, ");
    	stb.append("   QRM.RESULT_NAME AS RESULT ");
    	stb.append(" FROM ");
    	stb.append("  SCHREG_QUALIFIED_TEST_DAT SQTD ");
    	stb.append("  LEFT JOIN QUALIFIED_RESULT_MST QRM ");
    	stb.append("    ON SQTD.YEAR = QRM.YEAR ");
    	stb.append("    AND SQTD.QUALIFIED_CD = QRM.QUALIFIED_CD ");
    	stb.append("    AND SQTD.TEST_CD = QRM.RESULT_CD ");
    	stb.append("  ");
    	stb.append(" UNION ");
    	stb.append("  ");
    	stb.append(" SELECT  ");
    	stb.append("   SQHD.YEAR, ");
    	stb.append("   SQHD.SCHREGNO, ");
    	stb.append("   SQHD.QUALIFIED_CD, ");
    	stb.append("   SQHD.REGDDATE, ");
    	stb.append("   SQHD.RANK, ");
    	stb.append("   QRM.RESULT_NAME AS RESULT ");
    	stb.append(" FROM ");
    	stb.append("  SCHREG_QUALIFIED_HOBBY_DAT SQHD ");
    	stb.append("  LEFT JOIN QUALIFIED_RESULT_MST QRM ");
    	stb.append("    ON SQHD.YEAR = QRM.YEAR ");
    	stb.append("    AND SQHD.QUALIFIED_CD = QRM.QUALIFIED_CD ");
    	stb.append("    AND SQHD.RANK = QRM.RESULT_CD ");
    	stb.append(" ) ");
    	stb.append(" SELECT  ");
    	stb.append("   QDATA_MERGE.YEAR, ");
    	stb.append("   QDATA_MERGE.SCHREGNO, ");
    	stb.append("   QDATA_MERGE.QUALIFIED_CD, ");
    	stb.append("   QDATA_MERGE.REGDDATE, ");
    	stb.append("   QM.QUALIFIED_NAME, ");
    	stb.append("   QDATA_MERGE.RANK, ");
    	stb.append("   QDATA_MERGE.RESULT ");
    	stb.append(" FROM ");
    	stb.append("   QDATA_MERGE ");
    	stb.append("   LEFT JOIN NM312 ON NM312.NAMECD2 = RANK ");
    	stb.append("   LEFT JOIN QUALIFIED_MST QM ON QM.QUALIFIED_CD = QDATA_MERGE.QUALIFIED_CD ");
    	stb.append(" WHERE ");
    	stb.append("  QDATA_MERGE.SCHREGNO IN (" + _param._inState + ") ");
    	stb.append(" ORDER BY ");
    	stb.append("  QDATA_MERGE.SCHREGNO, ");
    	stb.append("  QDATA_MERGE.YEAR, ");
    	stb.append("  QDATA_MERGE.REGDDATE ");

        return stb.toString();
    }

    private String getPracticalJudgesql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("  BASE.SCHREGNO, ");
        stb.append("  EABDD.ENTEXAMYEAR, ");
        stb.append("  EABDD.EXAMNO, ");
        stb.append("  CASE ");
        stb.append("   WHEN EABDD.REMARK3 = '1' THEN '－' ");
        stb.append("   WHEN EABDD.REMARK3 = '2' THEN '可' ");
        stb.append("   WHEN EABDD.REMARK3 = '3' THEN '不' ");
        stb.append("   ELSE '' END AS REMARK3 ");
        stb.append(" FROM ");
        stb.append("  SCHREG_BASE_MST BASE");
        stb.append("  LEFT JOIN SCHREG_BASE_DETAIL_MST SBDM ON BASE.SCHREGNO = SBDM.SCHREGNO AND SBDM.BASE_SEQ = '003' ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DAT EABD ON EABD.ENTEXAMYEAR = CHAR(YEAR(BASE.ENT_DATE)) ");
        stb.append("          AND EABD.EXAMNO = SBDM.BASE_REMARK1 ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT EABDD ON EABDD.ENTEXAMYEAR = EABD.ENTEXAMYEAR");
        stb.append("      AND EABDD.EXAMNO = EABD.EXAMNO ");
        stb.append("      AND EABDD.APPLICANTDIV = EABD.APPLICANTDIV");
        stb.append(" WHERE ");
        stb.append("  BASE.SCHREGNO IN ('" + schregno + "') ");
        return stb.toString();
    }

    private class StudentData {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _name;
        final String _kana;
        final String _eng;
        final String _sex;
        final String _scholarshipName;
        final String _coursecd;
        final String _majorcd;
        final String _majorname;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _entYear;
        final String _testdivname;
        final String _totalpoint;
        final String _practicaljudge;
        public StudentData(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String name,
                final String kana,
                final String eng,
                final String sex,
                final String scholarshipName,
                final String coursecd,
                final String majorcd,
                final String majorname,
                final String remark1,
                final String remark2,
                final String remark3,
                final String entYear,
                final String testdivname,
                final String totalpoint,
                final String practicaljudge
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _name = name;
            _kana = kana;
            _eng = eng;
            _sex = sex;
            _scholarshipName = scholarshipName;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _majorname = majorname;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _entYear = entYear;
            _testdivname = testdivname;
            _totalpoint = totalpoint;
            _practicaljudge = practicaljudge;
        }

    }

    private class StudentMeisaiData {
        final String _schregno;
        final List _gradeHrList;
        final List _taikaiList;
        final List _syouList;
        final List _clubList;
        final List _iinkaiList;
        final List _classIinkaiList;
        final List _syobunList;
        final List _ptaList;
        final List _kibouList;
        final List _tokkiList;
        final List _mendanList;
        final List _volunteerList;       //ボランティア
        final List _ptjobList;           //アルバイト
        final List _ostrainingList;      //海外研修
        final List _efficiecyList;       //実力テスト
        final List _unfinishcreditList;  //未履修科目
        final List _totalstudyactList;   //総合的な学習
        final List _transferList;        //留学
        final List _councilList;         //生徒会
        final List _qualificationList;   //資格
        final List _familyenvList;       //家庭環境
        public StudentMeisaiData(
                final String schregno
        ) {
            _schregno = schregno;
            _gradeHrList = new ArrayList();
            _taikaiList = new ArrayList();
            _syouList = new ArrayList();
            _clubList = new ArrayList();
            _iinkaiList = new ArrayList();
            _classIinkaiList = new ArrayList();
            _syobunList = new ArrayList();
            _ptaList = new ArrayList();
            _kibouList = new ArrayList();
            _tokkiList = new ArrayList();
            _mendanList = new ArrayList();
            _volunteerList = new ArrayList();
            _ptjobList = new ArrayList();
            _ostrainingList = new ArrayList();
            _efficiecyList = new ArrayList();
            _unfinishcreditList = new ArrayList();
            _totalstudyactList = new ArrayList();
            _transferList = new ArrayList();
            _councilList = new ArrayList();
            _qualificationList = new ArrayList();
            _familyenvList = new ArrayList();
        }

    }

    private class GradeHr {
        final String _schregno;
        final String _year;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _coursecodeName;
        final String _avg;
        public GradeHr(
                final String schregno,
                final String year,
                final String semester,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String coursecodeName,
                final String avg
        ) {
            _schregno = schregno;
            _year = year;
            _semester = semester;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _hrName = hrName;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _coursecodeName = coursecodeName;
            _avg = avg;
        }

    }

    private class Taikai {
        final String _schregno;
        final String _detailDate;
        final String _clubCd;
        final String _clubName;
        final String _meetName;
        final String _recordCd;
        final String _recordName;
        public Taikai(
                final String schregno,
                final String detailDate,
                final String clubCd,
                final String clubName,
                final String meetName,
                final String recordCd,
                final String recordName
        ) {
            _schregno = schregno;
            _detailDate = StringUtils.replace(detailDate, "-", "/");;
            _clubCd = clubCd;
            _clubName = clubName;
            _meetName = meetName;
            _recordCd = recordCd;
            _recordName = recordName;
        }

    }

    private class SyouBatsu {
        final String _schregno;
        final String _year;
        final String _detailSdate;
        final String _detailCd;
        final String _detailName;
        final String _content;
        public SyouBatsu(
                final String schregno,
                final String year,
                final String detailSdate,
                final String detailCd,
                final String detailName,
                final String content
        ) {
            _schregno = schregno;
            _year = year;
            _detailSdate = StringUtils.replace(detailSdate, "-", "/");;
            _detailCd = detailCd;
            _detailName = detailName;
            _content = content;
        }

    }

    private class Club {
        final String _schregno;
        final String _clubCd;
        final String _clubName;
        final String _sdate;
        final String _executiveCd;
        final String _executiveName;
        public Club(
                final String schregno,
                final String clubCd,
                final String clubName,
                final String sdate,
                final String executiveCd,
                final String executiveName
        ) {
            _schregno = schregno;
            _clubCd = clubCd;
            _clubName = clubName;
            _sdate = sdate;
            _executiveCd = executiveCd;
            _executiveName = executiveName;
        }

    }

    private class Committee {
        final String _schregno;
        final String _year;
        final String _committeeCd;
        final String _committeeName;
        final String _executiveCd;
        final String _executiveName;
        final String _semester;
        public Committee(
                final String schregno,
                final String year,
                final String committeeCd,
                final String committeeName,
                final String executiveCd,
                final String executiveName,
                final String semester
        ) {
            _schregno = schregno;
            _year = year;
            _committeeCd = committeeCd;
            _committeeName = committeeName;
            _executiveCd = executiveCd;
            _executiveName = executiveName;
            _semester = semester;
        }
    }

    private class Karte {
        final String _schregno;
        final String _year;
        final String _trainDate;
        final String _howtotrainCd;
        final String _howtotrainName;
        final String _content;
        public Karte(
                final String schregno,
                final String year,
                final String trainDate,
                final String howtotrainCd,
                final String howtotrainName,
                final String content
        ) {
            _schregno = schregno;
            _year = year;
            _trainDate = StringUtils.replace(trainDate, "-", "/");
            _howtotrainCd = howtotrainCd;
            _howtotrainName = howtotrainName;
            _content = content;
        }
    }


    //実力テスト
    private class efficiency {
    	private String _schregno;
    	private String _year;
    	private String _gradecd;
    	private String _proficiencydiv_name;
    	private String _avg;
    	private String _cnt;
    	efficiency(final String schregno, final String year, final String gradecd, final String proficiencydiv_name, final String avg, final String cnt) {
    		_schregno = schregno;
    		_year = year;
    		_proficiencydiv_name = proficiencydiv_name;
    		_avg = avg;
    		_cnt = cnt;
    		_gradecd = gradecd;
    	}
    }

    //未履修科目単位
    private class UnFinishCredit {
    	private String _schregno;
    	private String _grade;
    	private String _subclassname;
    	private String _classnameeng;
    	private String _credit;

    	UnFinishCredit(final String schregno, final String grade, final String subclassname, final String classnameeng, final String credit) {
    		_schregno = schregno;
    		_grade = grade;
    		_subclassname = subclassname;
    		_classnameeng = classnameeng;
    		_credit = (credit != null && !"".equals(credit)) ? credit : "0";
    	}
    }

    //総合的な学習
    private class TotalStudyAct {
    	private String _schregno;
    	private String _year;
    	private String _gradecd;
    	private String _totalstudyact;

    	TotalStudyAct(final String schregno, final String year, final String gradecd, final String totalstudyact) {
    		_schregno = schregno;
    		_year = year;
    		_gradecd = gradecd;
    		_totalstudyact = totalstudyact;
    	}
    }

    //留学
    private class Transfer {
    	private String _schregno;
    	private String _transfersdate;
    	private String _transferedate;
    	private String _transferaddr;
    	private String _transferplace;
    	Transfer (final String schregno, final String transfersdate, final String transferedate, final String transferaddr, final String transferplace) {
    		_schregno = schregno;
    		_transfersdate = transfersdate;
    		_transferedate = transferedate;
    		_transferaddr = transferaddr;
    		_transferplace = transferplace;
    	}
    }

    //生徒会
    private class Council {
    	private String _name;
    	private String _kana;
    	private String _hr_nameabbv;
    	private String _positionnm;
    	private String _positionmark;
    	private String _committeenm;
    	private String _schregno;
    	private String _grade;
    	private String _committeeflg;
    	private String _committeecd;
    	private String _executivecd;
    	Council (final String name, final String kana, final String hr_nameabbv,
    			final String positionnm, final String positionmark, final String committeenm,
    			final String schregno, final String grade, final String committeeflg,
    			final String committeecd, final String executivecd) {
            _name = name;
            _kana = kana;
            _hr_nameabbv = hr_nameabbv;
            _positionnm = positionnm;
            _positionmark = positionmark;
            _committeenm = committeenm;
            _schregno = schregno;
            _grade = grade;
            _committeeflg = committeeflg;
            _committeecd = committeecd;
            _executivecd = executivecd;
    	}
    }

    //資格
    private class Qualification {
    	private String _year;
    	private String _schregno;
    	private String _regddate;
    	private String _qualifiedname;
    	private String _result;
    	Qualification (final String year, final String schregno, final String regddate, final String qualifiedname, final String result) {
    		_year = year;
    		_schregno = schregno;
    		_regddate = regddate;
    		_qualifiedname = qualifiedname;
    		_result = result;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 66730 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _gradeHrClass;
        private final String _schoolKind;
        private final String[] _categorySelected;
        private final String _inState;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _documentroot;
        private final String _folder;
        private final String _extention;
        private final String _proficiencydiv;
        private final String _proficiencySubclassCd;
        private final boolean _printpage1;
        private final boolean _printpage2;
        private final boolean _printpage3;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _inState = getInState();
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _documentroot = request.getParameter("DOCUMENTROOT");
            KNJ_Control imagepath_extension = new KNJ_Control();            //取得クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
            _folder = returnval.val4;                                           //写真データ格納フォルダ
            _extention = returnval.val5;                                          //写真データの拡張子
            _schoolKind = getSchoolKind(db2);
            _proficiencydiv = request.getParameter("PROFICIENCY_TYPE");
            _proficiencySubclassCd = request.getParameter("PROFICIENCY_SUBJECT");
            _printpage1 = "1".equals(request.getParameter("PRINTPAGE1")) ? true : false;
            _printpage2 = "1".equals(request.getParameter("PRINTPAGE2")) ? true : false;
            _printpage3 = "1".equals(request.getParameter("PRINTPAGE3")) ? true : false;
        }

        boolean isGakuensei() {
        	return true;
        }
        boolean isTannisei() {
        	return false;
        }
        private String getInState() {
            String retStr = "";
            String sep = "";
            for (int selectCnt = 0; selectCnt < _categorySelected.length; selectCnt++) {
                final String gradeHr = _categorySelected[selectCnt];
                retStr += sep + "'" + gradeHr + "'";
                sep = ",";
            }
            return retStr;
        }

        private String getSchoolKind(final DB2UDB db2) {
            String retSchoolKind = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String grade = StringUtils.substring(_gradeHrClass, 0, 2);
                final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolKind;
        }

    }
}

// eof
