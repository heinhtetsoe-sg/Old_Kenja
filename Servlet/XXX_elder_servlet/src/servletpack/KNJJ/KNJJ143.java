/*
 * $Id: 1a6709d714b1fc14fae1b417ce318b4663adf5ac $
 *
 * 作成日: 2018/02/22
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJJ143 {

    private static final Log log = LogFactory.getLog(KNJJ143.class);

    private boolean _hasData;

    private Param _param;

	private char _nowSchoolKind;

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

            log.debug(" **** VrsInit ****");
            svf.VrInit();
            log.debug(" **** VrSetSpoolFileStream ****");
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
                log.debug(" **** VrSetForm ****");
                svf.VrSetForm("MES001.frm", 0);
                log.debug(" **** VrOut ****");
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            log.debug(" **** VrQuit ****");
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
    	log.debug("_schoolKindInState:" + _param._schoolKindInState);
    	if (StringUtils.contains(_param._schoolKindInState, "J")) {
    		_nowSchoolKind = 'J';
    		printMainSub(db2, svf);
    	}
        log.debug(" **** VrEndPage ****");
        svf.VrEndPage();
    	if (StringUtils.contains(_param._schoolKindInState, "H")) {
    		_nowSchoolKind = 'H';
        	log.debug("_nowSchoolKind:" + _nowSchoolKind);
    		printMainSub(db2, svf);
    	}
        log.debug(" **** VrEndPage ****");
        svf.VrEndPage();
    }

    private void printMainSub(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJJ143.frm", 4);

        final List printList1 = getList1(db2);
        final List printList2 = getList2(db2);
        ArrayList chkTtlflg;
        final int maxchgCol = 10;
        int idx = 0;

        final int maxLine = 31;
        int printLine = 1;
        String beforeGradeStr = "";
        String beforeClassStr = "";
        boolean firstgradeflg = true;

        setTitle(svf);//ヘッダ

        //帳票の並びと出力順序の関係で、奇数項目のみ出力
        setCouncilInfo(svf, printList1, 1);
        //偶数項目のみ出力
        setCouncilInfo(svf, printList1, 0);

        chkTtlflg = setCommitteeTitle(svf, printList2);
        List outttlchk = getCommitteeChkList(chkTtlflg);
        for (Iterator iterator = printList2.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printLine > maxLine) {
            	break;
            }
            if (!"".equals(beforeGradeStr) && !beforeGradeStr.equals(printData._grade)) {
                log.debug("ENDRECORD1: / beforeGradeStr:" + beforeGradeStr + "/printData._grade:" + printData._grade);
                log.debug(" **** VrEndRecord ****");
            	svf.VrEndRecord();
            	outttlchk = getCommitteeChkList(chkTtlflg);
            	firstgradeflg = true;
            } else if (!"".equals(beforeClassStr) && !beforeClassStr.equals(printData._hrNameAbbv)) {
                log.debug("ENDRECORD2: / beforeClassStr:" + beforeClassStr + "/printData._hrNameAbbv:" + printData._hrNameAbbv);
                log.debug(" **** VrEndRecord ****");
            	svf.VrEndRecord();
            	outttlchk = getCommitteeChkList(chkTtlflg);
            	firstgradeflg = false;
            } else {
                if (cntOutEndCol(outttlchk) >= maxchgCol) {
            	    continue;
                }
            }

            log.debug("printData._committeeFlg:" + printData._committeeFlg);
            final int outcol = chkTtlflg.indexOf(printData._committeeFlg) + 1;
            if (outcol > 0) {
                log.debug("outcol:" + outcol + "/" + "chkTtlFlg:" + chkTtlflg.size() + "/" + "outttlchk:" + outttlchk.size());
                final committeeOutChk chkObj = (committeeOutChk)outttlchk.get(outcol - 1);
                if (firstgradeflg) {
                    //クラス名称(クラスが変わったことで行が変わる)
            	    if (!beforeClassStr.equals(printData._hrNameAbbv)) {
                        //クラス名称
                        log.debug(" **** VrsOut ****");
                        svf.VrsOut("HR_NAME_ABBV1", printData._hrNameAbbv);
            	    }

                    //2レコードまで出力。
                    if (chkObj._out_cnt <= 2) {
                        //どの委員なのか(出力列)を判定
                        //記号+氏名
                        final String outnamestr = (printData._positionMark == null ? "": printData._positionMark) + printData._name;
                        final int outnamelentype = 14 >= KNJ_EditEdit.getMS932ByteLength(outnamestr) ? 1: 2;
            	        final String namefieldstr = "NAME1_" + (outcol) + "_" + chkObj._out_cnt + "_" + outnamelentype;
                        log.debug("namefieldstrA:" + namefieldstr);
                        log.debug(" **** VrsOut ****");
                        svf.VrsOut(namefieldstr, outnamestr);

            	        //氏名かな
                        final String outkanastr = printData._nameKana;
                        final int outkanalentype = 16 >= KNJ_EditEdit.getMS932ByteLength(outkanastr) ? 1: 2;
            	        final String kanafieldstr = "KANA1_" + (outcol) + "_" + chkObj._out_cnt + "_" + outkanalentype;
                        log.debug("kanafieldstrA:" + kanafieldstr);
                        log.debug(" **** VrsOut ****");
                        svf.VrsOut(kanafieldstr, outkanastr);

                        //出力数をカウント
                        chkObj.incCnt();
                        outttlchk.set(outcol -1, chkObj);
                        log.debug("cntA:" + ((committeeOutChk)outttlchk.get(outcol - 1))._out_cnt);
                    }
                } else {
                    //クラス名称(クラスが変わったことで行が変わる)
            	    if (!beforeClassStr.equals(printData._hrNameAbbv)) {
                        log.debug(" **** VrsOut ****");
                        svf.VrsOut("HR_NAME_ABBV2", printData._hrNameAbbv);
            	    }
                    //2レコードまで出力。
                    if (chkObj._out_cnt <= 2) {
                        //どの委員なのか(出力列)を判定
                        final String outnamestr = (printData._positionMark == null ? "": printData._positionMark) + printData._name;
                        final int outnamelentype = 14 >= KNJ_EditEdit.getMS932ByteLength(outnamestr) ? 1: 2;
                	    final String namefieldstr = "NAME2_" + (outcol) + "_" + chkObj._out_cnt + "_" + outnamelentype;
                        log.debug("namefieldstrB:" + namefieldstr);
                        log.debug(" **** VrsOut ****");
                        svf.VrsOut(namefieldstr, outnamestr);

                	    //氏名かな
                        final String outkanastr = printData._nameKana;
                        final int outkanalentype = 16 >= KNJ_EditEdit.getMS932ByteLength(outkanastr) ? 1: 2;
                	    final String kanafieldstr = "KANA2_" + (outcol) + "_" + chkObj._out_cnt + "_" + outkanalentype;
                        log.debug("kanafieldstrB:" + kanafieldstr);
                        log.debug(" **** VrsOut ****");
                        svf.VrsOut(kanafieldstr, outkanastr);

                        //出力数をカウント
                	    ((committeeOutChk)outttlchk.get(outcol - 1)).incCnt();
                        log.debug("cntB:" + ((committeeOutChk)outttlchk.get(outcol - 1))._out_cnt);
                    }
                }
            }
            beforeClassStr = printData._hrNameAbbv;
            beforeGradeStr = printData._grade;

            printLine++;
            _hasData = true;
        }
        if (_hasData) {
            log.debug(" **** VrEndRecord ****");
            svf.VrEndRecord();
        }
    }

    private int cntOutEndCol(final List outttlchk) {
        int retval = 0;
        for (Iterator iterator = outttlchk.iterator(); iterator.hasNext();) {
            committeeOutChk chkData = (committeeOutChk) iterator.next();
        	if (chkData._out_cnt > 2) {
        		retval++;
        	}
        }
        return retval;
    }

    private void setTitle(final Vrw32alp svf) {
        String setYear = KNJ_EditDate.h_format_JP_N(_param._loginDate);
        //年を抜く
        setYear = setYear.substring(0, setYear.length()-1);
        log.debug(" **** VrsOut(Title) ****");
        String setkindstr = "";
        for (int ii = 0;ii < _param._schoolKindNm.size();ii++) {
        	SchKindCls namewk = (SchKindCls)_param._schoolKindNm.get(ii);
        	if (namewk._code.equals(String.valueOf(_nowSchoolKind))) {
        		setkindstr = namewk._label;
        	}
        }

        svf.VrsOut("TITLE", _param._loginYear + "(" + setYear + ")年度　" + _param._semesterNm + "　" + setkindstr + "自治会クラス委員表");
    }

    private void setCouncilInfo(final Vrw32alp svf, final List printList, final int outRemind) {
        final int maxchgRow = 2;
        final int maxCommitteeCnt = 10;
        int rowcnt = 1;
        int colcnt = 1;
        int idx = 0;
        for (Iterator iterator1 = printList.iterator(); iterator1.hasNext();) {
            final PrintData printData = (PrintData) iterator1.next();
            if (idx >= maxCommitteeCnt) {
            	continue;
            } else {
            	if (rowcnt > maxchgRow) {
            		colcnt++;
            		rowcnt = 1;
            	}
            	//idxは0ベースのため、1ずらして判定
            	if (((idx+1) % 2) == outRemind) {
            		setCouncilInfoSub(svf, rowcnt, colcnt, printData);
                }
        		rowcnt++;
            }
            idx++;
        }
    }

    private void setCouncilInfoSub(final Vrw32alp svf, final int rowcnt, final int colcnt, final PrintData printData) {
    		//役職
    	    final String fieldStr1 = "EXECTIVE" + colcnt;
            log.debug(" **** VrsOutn ****");
    	    svf.VrsOutn(fieldStr1, rowcnt, printData._positionNm);
    	    //氏名(クラス略称)
    	    final String nameStr = printData._name + "(" + printData._hrNameAbbv + ")";
    	    if (20 >= KNJ_EditEdit.getMS932ByteLength(nameStr)) {
        	    final String fieldStr2 = "EXECTIVE_NAME" + colcnt + "_1";
                log.debug(" **** VrsOutn ****");
        	    svf.VrsOutn(fieldStr2, rowcnt, nameStr);
            	log.debug("fieldStr21:" + fieldStr2);
    	    } else {
        	    final String fieldStr2 = "EXECTIVE_NAME" + colcnt + "_2";
                log.debug(" **** VrsOutn ****");
        	    svf.VrsOutn(fieldStr2, rowcnt, nameStr);
            	log.debug("fieldStr22:" + fieldStr2);
    	    }
    	    //氏名かな
    	    if (24 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
        	    final String fieldStr2 = "EXECTIVE_KANA" + colcnt + "_1";
                log.debug(" **** VrsOutn ****");
        	    svf.VrsOutn(fieldStr2, rowcnt, printData._name);
            	log.debug("fieldStr23:" + fieldStr2);
    	    } else {
        	    final String fieldStr2 = "EXECTIVE_KANA" + colcnt + "_2";
                log.debug(" **** VrsOutn ****");
        	    svf.VrsOutn(fieldStr2, rowcnt, printData._name);
            	log.debug("fieldStr24:" + fieldStr2);
    	    }
    }

    private ArrayList setCommitteeTitle(final Vrw32alp svf, final List printList) {
    	ArrayList chkttlflg = new ArrayList();
    	int maxttlcnt = 10;
    	int idx = 1;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (idx > maxttlcnt) {
            	break;
            }
            if (chkttlflg.size() < 1 || !chkttlflg.contains(printData._committeeFlg)) {
            	//未出力データなので、設定
            	final String fieldStr = "COMMITTEE_NAME" + idx;
                log.debug(" **** VrsOut ****");
            	svf.VrsOut(fieldStr, printData._committeeNm);
            	log.debug("printData._schregNo:" +printData._schregNo + "/ printData._committeeNm:" + printData._committeeNm);
            	//列に対する値としてコードを記憶
            	chkttlflg.add(printData._committeeFlg);
            	idx++;
            }
        }
        return chkttlflg;
    }

    private List getCommitteeChkList(final List chkTtlCd) {
    	ArrayList outttlcdchk = new ArrayList();
    	for (Iterator iterator = chkTtlCd.iterator(); iterator.hasNext();) {
    		String colcd = (String) iterator.next();
    		committeeOutChk addval = new committeeOutChk(colcd, 1);
    		outttlcdchk.add(addval);
    	}
    	return outttlcdchk;
    }

    private class committeeOutChk {
    	private String _col_cd;
    	private int _out_cnt;
    	committeeOutChk(final String colcd, final int outcnt) {
    		_col_cd = colcd;
    		_out_cnt = outcnt;
    	}
    	public void incCnt() {
    		_out_cnt++;
    	}
    }

    private List getList1(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql1();
            log.debug(" sql1 =" + sql);
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

                final PrintData printData = new PrintData(name, nameKana, hrnameabbv, positionnm, positionmark, committeenm, schregno, grade, committeeflg, committeecd, executivecd);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private List getList2(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql2();
            log.debug(" sql2 =" + sql);
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

                final PrintData printData = new PrintData(name, nameKana, hrnameabbv, positionnm, positionmark, committeenm, schregno, grade, committeeflg, committeecd, executivecd);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql1() {
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
        stb.append(" SELECT ");
        stb.append("     SBM.NAME AS NAME, ");
        stb.append("     SBM.NAME_KANA AS NAME_KANA, ");
        stb.append("     SRH.HR_NAMEABBV AS HR_NAMEABBV, ");
        stb.append("     J002_NM.NAME1 AS POSITION_NM, ");
        stb.append("     J002_NM.ABBV3 AS POSITION_MARK, ");
        stb.append("     J003_NM.NAME1 AS COMMITTEE_NM, ");
        stb.append("     SC1.SCHREGNO AS SCHREGNO, ");
        stb.append("     SC1.GRADE AS GRADE, ");
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
        stb.append(" LEFT JOIN NAME_MST J003_NM ON J003_NM.NAMECD1 = 'J003' AND J003_NM.NAMECD2 = SC2.COMMITTEE_FLG AND J003_NM.NAMESPARE1 = '1' ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT SRH ON  SRH.YEAR = SC1.YEAR AND SRH.SEMESTER = SC1.SEMESTER AND SRH.GRADE = SC1.GRADE AND SRH.HR_CLASS = SC1.HR_CLASS ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!"".equals(_param._schoolKindInState)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ");
                stb.append("   AND GDAT.SCHOOL_KIND IN (" + _param._schoolKindInState + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._schoolKind)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     SC1.YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND SC1.SEMESTER = '" + _param._loginSamester + "' ");
        stb.append("     AND SC2.COMMITTEE_FLG = COMMITTEE_TBL.COMMITTEE_FLG ");
        stb.append("     AND SC2.COMMITTEECD = COMMITTEE_TBL.COMMITTEECD ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!"".equals(_param._schoolKindInState)) {
                stb.append("     AND GDAT.SCHOOL_KIND = '" + _nowSchoolKind + "' ");
                stb.append(" ORDER BY ");
                stb.append("	GDAT.SCHOOL_KIND DESC, SC2.COMMITTEECD, CASE WHEN SC2.EXECUTIVECD IS NULL THEN 0 ELSE 1 END DESC, EXECUTIVECD DESC  ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._schoolKind)) {
            stb.append("     AND GDAT.SCHOOL_KIND = '" + _nowSchoolKind + "' ");
            stb.append(" ORDER BY ");
            stb.append("	GDAT.SCHOOL_KIND DESC, SC2.COMMITTEECD, CASE WHEN SC2.EXECUTIVECD IS NULL THEN 0 ELSE 1 END DESC, EXECUTIVECD DESC  ");
        } else {
            stb.append(" ORDER BY ");
            stb.append("	SC2.COMMITTEECD, CASE WHEN SC2.EXECUTIVECD IS NULL THEN 0 ELSE 1 END DESC, EXECUTIVECD DESC  ");
        }

        return stb.toString();
    }

    private String getSql2() {
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
        stb.append(" SELECT ");
        stb.append("     SBM.NAME AS NAME, ");
        stb.append("     SBM.NAME_KANA AS NAME_KANA, ");
        stb.append("     SRH.HR_NAMEABBV AS HR_NAMEABBV, ");
        stb.append("     J002_NM.NAME1 AS POSITION_NM, ");
        stb.append("     J002_NM.ABBV3 AS POSITION_MARK, ");
        stb.append("     J003_NM.NAME1 AS COMMITTEE_NM, ");
        stb.append("     SC1.SCHREGNO AS SCHREGNO, ");
        stb.append("     SC1.GRADE AS GRADE, ");
        stb.append("     SC2.COMMITTEE_FLG AS COMMITTEE_FLG, ");
        stb.append("     SC2.COMMITTEECD AS COMMITTEECD, ");
        stb.append("     SC2.EXECUTIVECD AS EXECUTIVECD ");
        stb.append(" FROM ");
        stb.append(" COMMITTEE_TBL, ");
        stb.append(" SCHREG_REGD_DAT SC1 ");
        stb.append(" LEFT JOIN SCHREG_COMMITTEE_HIST_DAT SC2 ON SC2.YEAR = SC1.YEAR AND SC2.SCHREGNO = SC1.SCHREGNO AND SC2.SEMESTER = SC1.SEMESTER AND SC2.GRADE = SC1.GRADE ");
        stb.append(" INNER JOIN SCHREG_BASE_MST SC3 ON SC3.SCHREGNO = SC1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_BASE_MST SBM ON SBM.SCHREGNO = SC1.SCHREGNO ");
        stb.append(" LEFT JOIN NAME_MST J002_NM ON J002_NM.NAMECD1 = 'J002' AND J002_NM.NAMECD2 = SC2.EXECUTIVECD ");
        stb.append(" LEFT JOIN NAME_MST J003_NM ON J003_NM.NAMECD1 = 'J003' AND J003_NM.NAMECD2 = SC2.COMMITTEE_FLG ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT SRH ON  SRH.YEAR = SC1.YEAR AND SRH.SEMESTER = SC1.SEMESTER AND SRH.GRADE = SC1.GRADE AND SRH.HR_CLASS = SC1.HR_CLASS ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!"".equals(_param._schoolKindInState)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ");
                stb.append("   AND GDAT.SCHOOL_KIND IN (" + _param._schoolKindInState + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._schoolKind)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     SC1.YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND SC1.SEMESTER = '" + _param._loginSamester + "' ");
        stb.append("     AND SC2.COMMITTEE_FLG <> COMMITTEE_TBL.COMMITTEE_FLG ");
        stb.append("     AND SC2.COMMITTEECD IS NOT NULL");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!"".equals(_param._schoolKindInState)) {
                stb.append("     AND GDAT.SCHOOL_KIND = '" + _nowSchoolKind + "' ");
                stb.append(" ORDER BY ");
                stb.append("     GDAT.SCHOOL_KIND DESC, SC1.GRADE DESC, SC1.HR_CLASS, SC2.COMMITTEE_FLG, CASE WHEN SC2.EXECUTIVECD IS NULL THEN 0 ELSE 1 END DESC, SC2.EXECUTIVECD DESC ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._schoolKind)) {
            stb.append("     AND GDAT.SCHOOL_KIND = '" + _nowSchoolKind + "' ");
            stb.append(" ORDER BY ");
            stb.append("     GDAT.SCHOOL_KIND DESC, SC1.GRADE DESC, SC1.HR_CLASS, SC2.COMMITTEE_FLG, CASE WHEN SC2.EXECUTIVECD IS NULL THEN 0 ELSE 1 END DESC, SC2.EXECUTIVECD DESC ");
        } else {
            stb.append(" ORDER BY ");
            stb.append("     SC1.GRADE DESC, SC1.HR_CLASS, SC2.COMMITTEECD, CASE WHEN SC2.EXECUTIVECD IS NULL THEN 0 ELSE 1 END DESC, SC2.EXECUTIVECD DESC ");
        }

        return stb.toString();
    }

    private class PrintData {
        final String _name;
        final String _nameKana;
        final String _hrNameAbbv;
        final String _positionNm;
        final String _positionMark;
        final String _committeeNm;
        final String _schregNo;
        final String _grade;
        final String _committeeFlg;
        final String _committeeCd;
        final String _executiveCd;

        public PrintData(
                final String name,
                final String nameKana,
                final String hrnameabbv,
                final String positionnm,
                final String positionmark,
                final String committeenm,
                final String schregno,
                final String grade,
                final String committeeflg,
                final String committeecd,
                final String executivecd
        ) {
            _name = name;
            _nameKana = nameKana;
            _hrNameAbbv = hrnameabbv;
            _positionNm = positionnm;
            _positionMark = positionmark;
            _committeeNm = committeenm;
            _schregNo = schregno;
            _grade = grade;
            _committeeFlg = committeeflg;
            _committeeCd = committeecd;
            _executiveCd = executivecd;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 58725 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** 校種格納クラス*/
    private class SchKindCls {
        final String _code;
        final String _label;
        SchKindCls(final String code, final String label) {
            _code = code;
            _label = label;
        }
    }
    /** パラメータクラス */
    private class Param {
        final String _loginYear;
        final String _loginSamester;
        final String _loginDate;
        final String _committeeFlg;
        final String _useSchool_KindField;
        final String _schoolKind;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;
        final String _schoolKindInState;
        final String _semesterNm;
        final ArrayList _schoolKindNm;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginSamester = request.getParameter("LOGIN_SEMESTER");
            _semesterNm    = getNameMst(db2, "NAME1", "J004", _loginSamester);
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _committeeFlg  = request.getParameter("COMMITTEE_FLG");
            _schoolKind    = request.getParameter("SCHOOLKIND");
            _use_prg_schoolkind    = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind    = request.getParameter("selectSchoolKind");
            _schoolKindInState    = getSchoolKindInState();
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            if ("1".equals(_use_prg_schoolkind)) {
                if (!"".equals(_schoolKindInState)) {
                    _schoolKindNm = getNameListMst(db2, "ABBV1", "A023", _selectSchoolKind);
                } else {
                	_schoolKindNm = new ArrayList();
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_schoolKind)) {
            	_schoolKindNm = new ArrayList();
            	_schoolKindNm.add(_schoolKind);
            } else {
            	_schoolKindNm = new ArrayList();
            }
        }

        private ArrayList getNameListMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            ArrayList rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            String[] cutstr = StringUtils.split(namecd2, ":");
            String tmpstr = "";
        	for (int ii = 0;ii < cutstr.length; ii++) {
        		tmpstr += "'" + cutstr[ii] + "'";
        		if (ii + 1 < cutstr.length) {
        			tmpstr += ", ";
        		}
        	}
            try {
            	log.debug("sql0 = SELECT NAME1," + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAME1 IN (" + tmpstr + ") ");
                    ps = db2.prepareStatement(" SELECT NAME1," + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAME1 IN (" + tmpstr + ") ");
                    rs = ps.executeQuery();
                    while (rs.next()) {
                    	tmpstr = rs.getString(field);
                    	SchKindCls addwk = new SchKindCls(rs.getString("NAME1"), rs.getString(field));
                    	rtn.add(addwk);
                    }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        private String getSchoolKindInState() {
            String retStr = "";
            if (!"1".equals(_use_prg_schoolkind)) {
            	log.debug("ROUTE1");
                return retStr;
            }
            String cutwk = _selectSchoolKind;
            if (null == cutwk || "".equals(cutwk)) {
            	cutwk = "P:J:H";
            }
            final String[] strSplit = StringUtils.split(cutwk, ":");
            String sep = "";
            for (int i = 0; i < strSplit.length; i++) {
                retStr += sep + "'" + strSplit[i] + "'";
                sep = ",";
            }
        	log.debug("ROUTE3");
            return retStr;
        }
    }
}

// eof
