/*
 * $Id: 6745a00667ec7037c6d5f6d44eac3168bf8a15f9 $
 *
 * 作成日: 2016/01/08
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJH182 {

    private static final Log log = LogFactory.getLog(KNJH182.class);

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

            if ("csv".equals(_param._cmd)) {
                outputCsv(request, response, db2);
            } else {
                printMain(db2, svf);
            }
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
    	//usr_prg_schoolkind、useSchool_KindField共に立っていない場合でも最低1回既存処理では回る処理となっているので、とりあえず_SCHOOLKINDを設定して1回回すようにしておく。
    	final List fixedList = new ArrayList();
    	fixedList.add(_param._SCHOOLKIND);

    	final List schKList = "1".equals(_param._use_prg_schoolkind) ? Arrays.asList(StringUtils.split(_param._selectSchoolKind, ':')) : fixedList;
    	for (Iterator ite = schKList.iterator();ite.hasNext();) {
    		final String getSchKStr = (String)ite.next();
        	_param._gdatList = _param.getGdatList(db2, getSchKStr);
        	if (_param._gdatList == null || (_param._gdatList != null && _param._gdatList.size() == 0)) {
        		continue;
        	}
            printMainSub(db2, svf);
    	}
	}

    private void printMainSub(final DB2UDB db2, final Vrw32alp svf) {
        final List list = getList(db2);
        for (int line = 0; line < list.size(); line++) {

            final Map injiMap = (Map) list.get(line);
            final RegdGdat regdGdat = (RegdGdat) injiMap.get("REGDG");
            final List injiList = (List) injiMap.get("INJI_DATA");
            if (injiList.size() == 0) {
            	continue;
            }
            List dispGradeList = new ArrayList();
            final int lpLastIdx = regdGdat._gradeMap.size() <= 3 ? 0 : regdGdat._gradeMap.size() <= 7 ? 1 : 2;  //その行までに出力する学年の数で、ループ回数(=0ベースの出力ページ数)を特定。最終行は3年分。それ以外は4年分。MAX11年分。
            for (int divCnt = 0; divCnt < lpLastIdx+1;divCnt++) {
                if (injiList.size() == 0) {
                    continue;
                }
                svf.VrSetForm("KNJH182.frm", 4);
                svf.VrsOut("TITLE", getTitle(db2));
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._date));
                int gradeWkCnt = 1;
                int lpWkCnt = 1;
                for (Iterator itGdat = regdGdat._gradeMap.keySet().iterator(); itGdat.hasNext();) {
                    final String grade = (String) itGdat.next();
                    final GradeData gradeData = (GradeData) regdGdat._gradeMap.get(grade);
                    if ((4 * divCnt) < lpWkCnt && lpWkCnt <= 4 * (divCnt + 1) && lpWkCnt < 12) {  //学年11までは対応。
                        svf.VrsOut("GRADE" + gradeWkCnt, gradeData._gradeName);
                        dispGradeList.add(String.valueOf(gradeWkCnt));
                        gradeWkCnt++;
                    }
                    lpWkCnt++;
                }
                if (divCnt == lpLastIdx) {
                	svf.VrsOut("GRADE4", "全学年");  //最後にだけ出力
                }
                svf.VrEndRecord();

                svf.VrsOut("PREF_HEADER1", "都・県");
                svf.VrsOut("PREF_HEADER1", "区・市・郡");
                svf.VrEndRecord();

                for (Iterator itInji = injiList.iterator(); itInji.hasNext();) {
                    final InjiData injiData = (InjiData) itInji.next();
                    String setField = "1";
                    if ("".equals(injiData._pref)) {
                        setField = "2";
                    } else {
                        svf.VrsOut("PREF" + setField, injiData._pref);
                    }
                    if (divCnt == 0) {
                        svf.VrsOut("CITY" + setField, injiData._city);
                        svf.VrsOut("MALE1_" + setField, injiData._mCnt1);
                        svf.VrsOut("FEMALE1_" + setField, injiData._wCnt1);
                        svf.VrsOut("TOTAL1_" + setField, injiData._cnt1All);
                        svf.VrsOut("MALE2_" + setField, injiData._mCnt2);
                        svf.VrsOut("FEMALE2_" + setField, injiData._wCnt2);
                        svf.VrsOut("TOTAL2_" + setField, injiData._cnt2All);
                        svf.VrsOut("MALE3_" + setField, injiData._mCnt3);
                        svf.VrsOut("FEMALE3_" + setField, injiData._wCnt3);
                        svf.VrsOut("TOTAL3_" + setField, injiData._cnt3All);
                    } else if (divCnt == 1) {
                        svf.VrsOut("CITY" + setField, injiData._city);
                        if (dispGradeList.contains("1")) {
                            svf.VrsOut("MALE1_" + setField, injiData._mCnt5);
                            svf.VrsOut("FEMALE1_" + setField, injiData._wCnt5);
                            svf.VrsOut("TOTAL1_" + setField, injiData._cnt5All);
                        }
                        if (dispGradeList.contains("2")) {
                            svf.VrsOut("MALE2_" + setField, injiData._mCnt6);
                            svf.VrsOut("FEMALE2_" + setField, injiData._wCnt6);
                            svf.VrsOut("TOTAL2_" + setField, injiData._cnt6All);
                        }
                        if (dispGradeList.contains("3")) {
                            svf.VrsOut("MALE3_" + setField, injiData._mCnt7);
                            svf.VrsOut("FEMALE3_" + setField, injiData._wCnt7);
                            svf.VrsOut("TOTAL3_" + setField, injiData._cnt7All);
                        }
                    } else {
                        svf.VrsOut("CITY" + setField, injiData._city);
                        if (dispGradeList.contains("1")) {
                            svf.VrsOut("MALE1_" + setField, injiData._mCnt9);
                            svf.VrsOut("FEMALE1_" + setField, injiData._wCnt9);
                            svf.VrsOut("TOTAL1_" + setField, injiData._cnt9All);
                        }
                        if (dispGradeList.contains("2")) {
                            svf.VrsOut("MALE2_" + setField, injiData._mCnt10);
                            svf.VrsOut("FEMALE2_" + setField, injiData._wCnt10);
                            svf.VrsOut("TOTAL2_" + setField, injiData._cnt10All);
                        }
                        if (dispGradeList.contains("3")) {
                            svf.VrsOut("MALE3_" + setField, injiData._mCnt11);
                            svf.VrsOut("FEMALE3_" + setField, injiData._wCnt11);
                            svf.VrsOut("TOTAL3_" + setField, injiData._cnt11All);
                        }
                    }

                    if (divCnt == lpLastIdx) {
                        svf.VrsOut("MALE4_" + setField, injiData._mCntALL);
                        svf.VrsOut("FEMALE4_" + setField, injiData._wCntALL);
                        svf.VrsOut("TOTAL4_" + setField, injiData._cntAll);
                    } else if (divCnt == 0) {
                        if (dispGradeList.contains("4")) {
                            svf.VrsOut("MALE4_" + setField, injiData._mCnt4);
                            svf.VrsOut("FEMALE4_" + setField, injiData._wCnt4);
                            svf.VrsOut("TOTAL4_" + setField, injiData._cnt4All);
                        }
                    } else if (divCnt == 1) {
                        if (dispGradeList.contains("4")) {
                            svf.VrsOut("MALE4_" + setField, injiData._mCnt8);
                            svf.VrsOut("FEMALE4_" + setField, injiData._wCnt8);
                            svf.VrsOut("TOTAL4_" + setField, injiData._cnt8All);
                        }
                    }

                    svf.VrEndRecord();
                }

                svf.VrsOut("BLANK2", "1");
                svf.VrEndRecord();
                svf.VrsOut("BLANK", "1");
                svf.VrEndRecord();

                ////////以下は、合計の処理

                if (injiList.size() == 0) {
                    continue;
                }
                int gradeCnt = 1;
                lpWkCnt = 1;
                dispGradeList = new ArrayList();
                for (Iterator itGdat = regdGdat._gradeMap.keySet().iterator(); itGdat.hasNext();) {
                    final String grade = (String) itGdat.next();
                    final GradeData gradeData = (GradeData) regdGdat._gradeMap.get(grade);
                    if ((4 * divCnt) < lpWkCnt && lpWkCnt <= 4 * (divCnt + 1)) {
                        svf.VrsOut("GRADE" + gradeCnt, gradeData._gradeName);
                        dispGradeList.add(String.valueOf(gradeCnt));
                        gradeCnt++;
                    }
                    lpWkCnt++;
                }

                if (divCnt == lpLastIdx) {
                    svf.VrsOut("GRADE4", "全学年");
                }
                svf.VrEndRecord();

                svf.VrsOut("PREF_HEADER2", "都・県");
                svf.VrEndRecord();

                final List totalList = (List) injiMap.get("TOTAL_DATA");
                int mKeiCnt1      = 0;
                int wKeiCnt1      = 0;
                int cntKei1All    = 0;
                int mKeiCnt2      = 0;
                int wKeiCnt2      = 0;
                int cntKei2All    = 0;
                int mKeiCnt3      = 0;
                int wKeiCnt3      = 0;
                int cntKei3All    = 0;
                int mKeiCnt4      = 0;
                int wKeiCnt4      = 0;
                int cntKei4All    = 0;
                int mKeiCnt5      = 0;
                int wKeiCnt5      = 0;
                int cntKei5All    = 0;
                int mKeiCnt6      = 0;
                int wKeiCnt6      = 0;
                int cntKei6All    = 0;
                int mKeiCnt7      = 0;
                int wKeiCnt7      = 0;
                int cntKei7All    = 0;
                int mKeiCnt8      = 0;
                int wKeiCnt8      = 0;
                int cntKei8All    = 0;
                int mKeiCnt9      = 0;
                int wKeiCnt9      = 0;
                int cntKei9All    = 0;
                int mKeiCnt10     = 0;
                int wKeiCnt10     = 0;
                int cntKei10All   = 0;
                int mKeiCnt11     = 0;
                int wKeiCnt11     = 0;
                int cntKei11All   = 0;
                int mKeiCntALL    = 0;
                int wKeiCntALL    = 0;
                int cntKeiAll     = 0;
                for (Iterator itTotal = totalList.iterator(); itTotal.hasNext();) {
                    final InjiData injiData = (InjiData) itTotal.next();
                    if (divCnt == 0) {
                        svf.VrsOut("PREF2", injiData._pref);
                        svf.VrsOut("PREF_MALE1", injiData._mCnt1);
                        svf.VrsOut("PREF_FEMALE1", injiData._wCnt1);
                        svf.VrsOut("PREF_TOTAL1", injiData._cnt1All);
                        svf.VrsOut("PREF_MALE2", injiData._mCnt2);
                        svf.VrsOut("PREF_FEMALE2", injiData._wCnt2);
                        svf.VrsOut("PREF_TOTAL2", injiData._cnt2All);
                        svf.VrsOut("PREF_MALE3", injiData._mCnt3);
                        svf.VrsOut("PREF_FEMALE3", injiData._wCnt3);
                        svf.VrsOut("PREF_TOTAL3", injiData._cnt3All);
                        mKeiCnt1      += Integer.parseInt(injiData._mCnt1);
                        wKeiCnt1      += Integer.parseInt(injiData._wCnt1);
                        cntKei1All    += Integer.parseInt(injiData._cnt1All);
                        mKeiCnt2      += Integer.parseInt(injiData._mCnt2);
                        wKeiCnt2      += Integer.parseInt(injiData._wCnt2);
                        cntKei2All    += Integer.parseInt(injiData._cnt2All);
                        mKeiCnt3      += Integer.parseInt(injiData._mCnt3);
                        wKeiCnt3      += Integer.parseInt(injiData._wCnt3);
                        cntKei3All    += Integer.parseInt(injiData._cnt3All);
                    } else if (divCnt == 1) {
                        svf.VrsOut("PREF2", injiData._pref);
                        if (dispGradeList.contains("1")) {
                            svf.VrsOut("PREF_MALE1", injiData._mCnt5);
                            svf.VrsOut("PREF_FEMALE1", injiData._wCnt5);
                            svf.VrsOut("PREF_TOTAL1", injiData._cnt5All);
                            mKeiCnt5      += Integer.parseInt(injiData._mCnt5);
                            wKeiCnt5      += Integer.parseInt(injiData._wCnt5);
                            cntKei5All    += Integer.parseInt(injiData._cnt5All);
                        }
                        if (dispGradeList.contains("2")) {
                            svf.VrsOut("PREF_MALE2", injiData._mCnt6);
                            svf.VrsOut("PREF_FEMALE2", injiData._wCnt6);
                            svf.VrsOut("PREF_TOTAL2", injiData._cnt6All);
                            mKeiCnt6      += Integer.parseInt(injiData._mCnt6);
                            wKeiCnt6      += Integer.parseInt(injiData._wCnt6);
                            cntKei6All    += Integer.parseInt(injiData._cnt6All);
                        }
                        if (dispGradeList.contains("3")) {
                            svf.VrsOut("PREF_MALE3", injiData._mCnt7);
                            svf.VrsOut("PREF_FEMALE3", injiData._wCnt7);
                            svf.VrsOut("PREF_TOTAL3", injiData._cnt7All);
                            mKeiCnt7      += Integer.parseInt(injiData._mCnt7);
                            wKeiCnt7      += Integer.parseInt(injiData._wCnt7);
                            cntKei7All    += Integer.parseInt(injiData._cnt7All);
                        }
                    } else {
                        svf.VrsOut("PREF2", injiData._pref);
                        if (dispGradeList.contains("1")) {
                            svf.VrsOut("PREF_MALE1", injiData._mCnt9);
                            svf.VrsOut("PREF_FEMALE1", injiData._wCnt9);
                            svf.VrsOut("PREF_TOTAL1", injiData._cnt9All);
                            mKeiCnt9      += Integer.parseInt(injiData._mCnt9);
                            wKeiCnt9      += Integer.parseInt(injiData._wCnt9);
                            cntKei9All    += Integer.parseInt(injiData._cnt9All);
                        }
                        if (dispGradeList.contains("2")) {
                            svf.VrsOut("PREF_MALE2", injiData._mCnt10);
                            svf.VrsOut("PREF_FEMALE2", injiData._wCnt10);
                            svf.VrsOut("PREF_TOTAL2", injiData._cnt10All);
                            mKeiCnt10      += Integer.parseInt(injiData._mCnt10);
                            wKeiCnt10      += Integer.parseInt(injiData._wCnt10);
                            cntKei10All    += Integer.parseInt(injiData._cnt10All);
                        }
                        if (dispGradeList.contains("3")) {
                            svf.VrsOut("PREF_MALE3", injiData._mCnt11);
                            svf.VrsOut("PREF_FEMALE3", injiData._wCnt11);
                            svf.VrsOut("PREF_TOTAL3", injiData._cnt11All);
                            mKeiCnt11      += Integer.parseInt(injiData._mCnt11);
                            wKeiCnt11      += Integer.parseInt(injiData._wCnt11);
                            cntKei11All    += Integer.parseInt(injiData._cnt11All);
                        }
                    }
                    if (divCnt == lpLastIdx) {
                        svf.VrsOut("PREF_MALE4", injiData._mCntALL);
                        svf.VrsOut("PREF_FEMALE4", injiData._wCntALL);
                        svf.VrsOut("PREF_TOTAL4", injiData._cntAll);
                        mKeiCntALL    += Integer.parseInt(injiData._mCntALL);
                        wKeiCntALL    += Integer.parseInt(injiData._wCntALL);
                        cntKeiAll     += Integer.parseInt(injiData._cntAll);
                    } else if (divCnt == 0) {
                        if (dispGradeList.contains("4")) {
                            svf.VrsOut("PREF_MALE4", injiData._mCnt4);
                            svf.VrsOut("PREF_FEMALE4", injiData._wCnt4);
                            svf.VrsOut("PREF_TOTAL4", injiData._cnt4All);
                            mKeiCnt4    += Integer.parseInt(injiData._mCnt4);
                            wKeiCnt4    += Integer.parseInt(injiData._wCnt4);
                            cntKei4All  += Integer.parseInt(injiData._cnt4All);
                        }
                    } else if (divCnt == 1) {
                        if (dispGradeList.contains("4")) {
                            svf.VrsOut("PREF_MALE4", injiData._mCnt8);
                            svf.VrsOut("PREF_FEMALE4", injiData._wCnt8);
                            svf.VrsOut("PREF_TOTAL4", injiData._cnt8All);
                            mKeiCnt8    += Integer.parseInt(injiData._mCnt8);
                            wKeiCnt8    += Integer.parseInt(injiData._wCnt8);
                            cntKei8All  += Integer.parseInt(injiData._cnt8All);
                        }
                    }

                    svf.VrEndRecord();
                }
                if (divCnt == 0) {
                    svf.VrsOut("TOTAL_MALE1", String.valueOf(mKeiCnt1));
                    svf.VrsOut("TOTAL_FEMALE1", String.valueOf(wKeiCnt1));
                    svf.VrsOut("TOTAL_TOTAL1", String.valueOf(cntKei1All));
                    svf.VrsOut("TOTAL_MALE2", String.valueOf(mKeiCnt2));
                    svf.VrsOut("TOTAL_FEMALE2", String.valueOf(wKeiCnt2));
                    svf.VrsOut("TOTAL_TOTAL2", String.valueOf(cntKei2All));
                    svf.VrsOut("TOTAL_MALE3", String.valueOf(mKeiCnt3));
                    svf.VrsOut("TOTAL_FEMALE3", String.valueOf(wKeiCnt3));
                    svf.VrsOut("TOTAL_TOTAL3", String.valueOf(cntKei3All));
                } else if (divCnt == 1) {
                    if (dispGradeList.contains("1")) {
                        svf.VrsOut("TOTAL_MALE1", String.valueOf(mKeiCnt5));
                        svf.VrsOut("TOTAL_FEMALE1", String.valueOf(wKeiCnt5));
                        svf.VrsOut("TOTAL_TOTAL1", String.valueOf(cntKei5All));
                    }
                    if (dispGradeList.contains("2")) {
                        svf.VrsOut("TOTAL_MALE2", String.valueOf(mKeiCnt6));
                        svf.VrsOut("TOTAL_FEMALE2", String.valueOf(wKeiCnt6));
                        svf.VrsOut("TOTAL_TOTAL2", String.valueOf(cntKei6All));
                    }
                    if (dispGradeList.contains("3")) {
                        svf.VrsOut("TOTAL_MALE3", String.valueOf(mKeiCnt7));
                        svf.VrsOut("TOTAL_FEMALE3", String.valueOf(wKeiCnt7));
                        svf.VrsOut("TOTAL_TOTAL3", String.valueOf(cntKei7All));
                    }
                } else {
                    if (dispGradeList.contains("1")) {
                        svf.VrsOut("TOTAL_MALE1", String.valueOf(mKeiCnt9));
                        svf.VrsOut("TOTAL_FEMALE1", String.valueOf(wKeiCnt9));
                        svf.VrsOut("TOTAL_TOTAL1", String.valueOf(cntKei9All));
                    }
                    if (dispGradeList.contains("2")) {
                        svf.VrsOut("TOTAL_MALE2", String.valueOf(mKeiCnt10));
                        svf.VrsOut("TOTAL_FEMALE2", String.valueOf(wKeiCnt10));
                        svf.VrsOut("TOTAL_TOTAL2", String.valueOf(cntKei10All));
                    }
                    if (dispGradeList.contains("3")) {
                        svf.VrsOut("TOTAL_MALE3", String.valueOf(mKeiCnt11));
                        svf.VrsOut("TOTAL_FEMALE3", String.valueOf(wKeiCnt11));
                        svf.VrsOut("TOTAL_TOTAL3", String.valueOf(cntKei11All));
                    }
                }
                if (divCnt == lpLastIdx) {
                    svf.VrsOut("TOTAL_MALE4", String.valueOf(mKeiCntALL));
                    svf.VrsOut("TOTAL_FEMALE4", String.valueOf(wKeiCntALL));
                    svf.VrsOut("TOTAL_TOTAL4", String.valueOf(cntKeiAll));
                } else if (divCnt == 0) {
                    svf.VrsOut("TOTAL_MALE4", String.valueOf(mKeiCnt4));
                    svf.VrsOut("TOTAL_FEMALE4", String.valueOf(wKeiCnt4));
                    svf.VrsOut("TOTAL_TOTAL4", String.valueOf(cntKei4All));
                } else if (divCnt == 1) {
                    svf.VrsOut("TOTAL_MALE4", String.valueOf(mKeiCnt8));
                    svf.VrsOut("TOTAL_FEMALE4", String.valueOf(wKeiCnt8));
                    svf.VrsOut("TOTAL_TOTAL4", String.valueOf(cntKei8All));
                }
                svf.VrEndRecord();

                svf.VrEndPage();
            }

            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        for (Iterator iterator = _param._gdatList.iterator(); iterator.hasNext();) {
            final RegdGdat regdGdat = (RegdGdat) iterator.next();
            if (regdGdat == null) {
            	continue;
            }
            final Map injiMap = new HashMap();
            injiMap.put("REGDG", regdGdat);
            final List injiList = new ArrayList();
            final List totalInjiList = new ArrayList();

            String grade1 = "";
            String grade2 = "";
            String grade3 = "";
            String grade4 = "";
            String grade5 = "";
            String grade6 = "";
            String grade7 = "";
            String grade8 = "";
            String grade9 = "";
            String grade10 = "";
            String grade11 = "";
            for (Iterator itGrade = regdGdat._gradeMap.keySet().iterator(); itGrade.hasNext();) {
                final String gradeDataKey = (String) itGrade.next();
                final GradeData gradeData = (GradeData) regdGdat._gradeMap.get(gradeDataKey);
                if ("".equals(grade1)) {
                    grade1 = gradeData._grade;
                } else if ("".equals(grade2)) {
                    grade2 = gradeData._grade;
                } else if ("".equals(grade3)) {
                    grade3 = gradeData._grade;
                } else if ("".equals(grade4)) {
                    grade4 = gradeData._grade;
                } else if ("".equals(grade5)) {
                    grade5 = gradeData._grade;
                } else if ("".equals(grade6)) {
                    grade6 = gradeData._grade;
                } else if ("".equals(grade7)) {
                    grade7 = gradeData._grade;
                } else if ("".equals(grade8)) {
                    grade8 = gradeData._grade;
                } else if ("".equals(grade9)) {
                    grade9 = gradeData._grade;
                } else if ("".equals(grade10)) {
                    grade10 = gradeData._grade;
                } else if ("".equals(grade11)) {
                    grade11 = gradeData._grade;
                }
            }

            try {
                final String sql = sql(regdGdat._schoolKind, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8, grade9, grade10, grade11);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                int mKeiCnt1      = 0;
                int wKeiCnt1      = 0;
                int cntKei1All    = 0;
                int mKeiCnt2      = 0;
                int wKeiCnt2      = 0;
                int cntKei2All    = 0;
                int mKeiCnt3      = 0;
                int wKeiCnt3      = 0;
                int cntKei3All    = 0;
                int mKeiCnt4      = 0;
                int wKeiCnt4      = 0;
                int cntKei4All    = 0;
                int mKeiCnt5      = 0;
                int wKeiCnt5      = 0;
                int cntKei5All    = 0;
                int mKeiCnt6      = 0;
                int wKeiCnt6      = 0;
                int cntKei6All    = 0;
                int mKeiCnt7      = 0;
                int wKeiCnt7      = 0;
                int cntKei7All    = 0;
                int mKeiCnt8      = 0;
                int wKeiCnt8      = 0;
                int cntKei8All    = 0;
                int mKeiCnt9      = 0;
                int wKeiCnt9      = 0;
                int cntKei9All    = 0;
                int mKeiCnt10     = 0;
                int wKeiCnt10     = 0;
                int cntKei10All   = 0;
                int mKeiCnt11     = 0;
                int wKeiCnt11     = 0;
                int cntKei11All   = 0;
                int mKeiCntALL    = 0;
                int wKeiCntALL    = 0;
                int cntKeiAll     = 0;
                String befPref = "";
                while (rs.next()) {
                    final String pref       = rs.getString("PREF");
                    final String city       = rs.getString("CITYNAME");
                    final String mCnt1      = rs.getString("M_CNT1");
                    final String wCnt1      = rs.getString("W_CNT1");
                    final String cnt1All    = rs.getString("CNT1_ALL");
                    final String mCnt2      = rs.getString("M_CNT2");
                    final String wCnt2      = rs.getString("W_CNT2");
                    final String cnt2All    = rs.getString("CNT2_ALL");
                    final String mCnt3      = rs.getString("M_CNT3");
                    final String wCnt3      = rs.getString("W_CNT3");
                    final String cnt3All    = rs.getString("CNT3_ALL");
                    final String mCnt4      = ("".equals(StringUtils.defaultString(grade4))) ? null : rs.getString("M_CNT4");
                    final String wCnt4      = ("".equals(StringUtils.defaultString(grade4))) ? null : rs.getString("W_CNT4");
                    final String cnt4All    = ("".equals(StringUtils.defaultString(grade4))) ? null : rs.getString("CNT4_ALL");
                    final String mCnt5      = ("".equals(StringUtils.defaultString(grade5))) ? null : rs.getString("M_CNT5");
                    final String wCnt5      = ("".equals(StringUtils.defaultString(grade5))) ? null : rs.getString("W_CNT5");
                    final String cnt5All    = ("".equals(StringUtils.defaultString(grade5))) ? null : rs.getString("CNT5_ALL");
                    final String mCnt6      = ("".equals(StringUtils.defaultString(grade6))) ? null : rs.getString("M_CNT6");
                    final String wCnt6      = ("".equals(StringUtils.defaultString(grade6))) ? null : rs.getString("W_CNT6");
                    final String cnt6All    = ("".equals(StringUtils.defaultString(grade6))) ? null : rs.getString("CNT6_ALL");
                    final String mCnt7      = ("".equals(StringUtils.defaultString(grade7))) ? null : rs.getString("M_CNT7");
                    final String wCnt7      = ("".equals(StringUtils.defaultString(grade7))) ? null : rs.getString("W_CNT7");
                    final String cnt7All    = ("".equals(StringUtils.defaultString(grade7))) ? null : rs.getString("CNT7_ALL");
                    final String mCnt8      = ("".equals(StringUtils.defaultString(grade8))) ? null : rs.getString("M_CNT8");
                    final String wCnt8      = ("".equals(StringUtils.defaultString(grade8))) ? null : rs.getString("W_CNT8");
                    final String cnt8All    = ("".equals(StringUtils.defaultString(grade8))) ? null : rs.getString("CNT8_ALL");
                    final String mCnt9      = ("".equals(StringUtils.defaultString(grade9))) ? null : rs.getString("M_CNT9");
                    final String wCnt9      = ("".equals(StringUtils.defaultString(grade9))) ? null : rs.getString("W_CNT9");
                    final String cnt9All    = ("".equals(StringUtils.defaultString(grade9))) ? null : rs.getString("CNT9_ALL");
                    final String mCnt10     = ("".equals(StringUtils.defaultString(grade10))) ? null : rs.getString("M_CNT10");
                    final String wCnt10     = ("".equals(StringUtils.defaultString(grade10))) ? null : rs.getString("W_CNT10");
                    final String cnt10All   = ("".equals(StringUtils.defaultString(grade10))) ? null : rs.getString("CNT10_ALL");
                    final String mCnt11     = ("".equals(StringUtils.defaultString(grade11))) ? null : rs.getString("M_CNT11");
                    final String wCnt11     = ("".equals(StringUtils.defaultString(grade11))) ? null : rs.getString("W_CNT11");
                    final String cnt11All   = ("".equals(StringUtils.defaultString(grade11))) ? null : rs.getString("CNT11_ALL");

                    final String mCntALL    = rs.getString("M_CNT_ALL");
                    final String wCntALL    = rs.getString("W_CNT_ALL");
                    final String cntAll     = rs.getString("CNT");

                    if ("".equals(befPref) || befPref.equals(pref)) {
                        mKeiCnt1      += Integer.parseInt(mCnt1);
                        wKeiCnt1      += Integer.parseInt(wCnt1);
                        cntKei1All    += Integer.parseInt(cnt1All);
                        mKeiCnt2      += Integer.parseInt(mCnt2);
                        wKeiCnt2      += Integer.parseInt(wCnt2);
                        cntKei2All    += Integer.parseInt(cnt2All);
                        mKeiCnt3      += Integer.parseInt(mCnt3);
                        wKeiCnt3      += Integer.parseInt(wCnt3);
                        cntKei3All    += Integer.parseInt(cnt3All);
                        if (!"".equals(StringUtils.defaultString(grade4))) {
                            mKeiCnt4      += Integer.parseInt(StringUtils.defaultString(mCnt4, ""));
                            wKeiCnt4      += Integer.parseInt(StringUtils.defaultString(wCnt4, ""));
                            cntKei4All    += Integer.parseInt(StringUtils.defaultString(cnt4All, ""));
                        }
                        if (!"".equals(StringUtils.defaultString(grade5))) {
                            mKeiCnt5      += Integer.parseInt(StringUtils.defaultString(mCnt5, ""));
                            wKeiCnt5      += Integer.parseInt(StringUtils.defaultString(wCnt5, ""));
                            cntKei5All    += Integer.parseInt(StringUtils.defaultString(cnt5All, ""));
                        }
                        if (!"".equals(StringUtils.defaultString(grade6))) {
                            mKeiCnt6      += Integer.parseInt(StringUtils.defaultString(mCnt6, ""));
                            wKeiCnt6      += Integer.parseInt(StringUtils.defaultString(wCnt6, ""));
                            cntKei6All    += Integer.parseInt(StringUtils.defaultString(cnt6All, ""));
                        }

                        if (!"".equals(StringUtils.defaultString(grade7))) {
                            mKeiCnt7      += Integer.parseInt(StringUtils.defaultString(mCnt7, ""));
                            wKeiCnt7      += Integer.parseInt(StringUtils.defaultString(wCnt7, ""));
                            cntKei7All    += Integer.parseInt(StringUtils.defaultString(cnt7All, ""));
                        }
                        if (!"".equals(StringUtils.defaultString(grade8))) {
                            mKeiCnt8      += Integer.parseInt(StringUtils.defaultString(mCnt8, ""));
                            wKeiCnt8      += Integer.parseInt(StringUtils.defaultString(wCnt8, ""));
                            cntKei8All    += Integer.parseInt(StringUtils.defaultString(cnt8All, ""));
                        }
                        if (!"".equals(StringUtils.defaultString(grade9))) {
                            mKeiCnt9      += Integer.parseInt(StringUtils.defaultString(mCnt9, ""));
                            wKeiCnt9      += Integer.parseInt(StringUtils.defaultString(wCnt9, ""));
                            cntKei9All    += Integer.parseInt(StringUtils.defaultString(cnt9All, ""));
                        }

                        if (!"".equals(StringUtils.defaultString(grade10))) {
                            mKeiCnt10     += Integer.parseInt(StringUtils.defaultString(mCnt10, ""));
                            wKeiCnt10     += Integer.parseInt(StringUtils.defaultString(wCnt10, ""));
                            cntKei10All   += Integer.parseInt(StringUtils.defaultString(cnt10All, ""));
                        }
                        if (!"".equals(StringUtils.defaultString(grade11))) {
                            mKeiCnt11     += Integer.parseInt(StringUtils.defaultString(mCnt11, ""));
                            wKeiCnt11     += Integer.parseInt(StringUtils.defaultString(wCnt11, ""));
                            cntKei11All   += Integer.parseInt(StringUtils.defaultString(cnt11All, ""));
                        }

                        mKeiCntALL    += Integer.parseInt(mCntALL);
                        wKeiCntALL    += Integer.parseInt(wCntALL);
                        cntKeiAll     += Integer.parseInt(cntAll);
                    } else {
                        final InjiData injiData = new InjiData(befPref, "", String.valueOf(mKeiCnt1), String.valueOf(wKeiCnt1), String.valueOf(cntKei1All), String.valueOf(mKeiCnt2), String.valueOf(wKeiCnt2), String.valueOf(cntKei2All), String.valueOf(mKeiCnt3), String.valueOf(wKeiCnt3), String.valueOf(cntKei3All), String.valueOf(mKeiCnt4), String.valueOf(wKeiCnt4), String.valueOf(cntKei4All), String.valueOf(mKeiCnt5), String.valueOf(wKeiCnt5), String.valueOf(cntKei5All), String.valueOf(mKeiCnt6), String.valueOf(wKeiCnt6), String.valueOf(cntKei6All)
                                                                , String.valueOf(mKeiCnt7), String.valueOf(wKeiCnt7), String.valueOf(cntKei7All), String.valueOf(mKeiCnt8), String.valueOf(wKeiCnt8), String.valueOf(cntKei8All), String.valueOf(mKeiCnt9), String.valueOf(wKeiCnt9), String.valueOf(cntKei9All), String.valueOf(mKeiCnt10), String.valueOf(wKeiCnt10), String.valueOf(cntKei10All), String.valueOf(mKeiCnt11), String.valueOf(wKeiCnt11), String.valueOf(cntKei11All)
                        		                                , String.valueOf(mKeiCntALL), String.valueOf(wKeiCntALL), String.valueOf(cntKeiAll));
                        totalInjiList.add(injiData);
                        mKeiCnt1      = Integer.parseInt(mCnt1);
                        wKeiCnt1      = Integer.parseInt(wCnt1);
                        cntKei1All    = Integer.parseInt(cnt1All);
                        mKeiCnt2      = Integer.parseInt(mCnt2);
                        wKeiCnt2      = Integer.parseInt(wCnt2);
                        cntKei2All    = Integer.parseInt(cnt2All);
                        mKeiCnt3      = Integer.parseInt(mCnt3);
                        wKeiCnt3      = Integer.parseInt(wCnt3);
                        cntKei3All    = Integer.parseInt(cnt3All);
                        if ("".equals(StringUtils.defaultString(grade4))) {
                            mKeiCnt4      += Integer.parseInt(StringUtils.defaultString(mCnt4, "0"));
                            wKeiCnt4      += Integer.parseInt(StringUtils.defaultString(wCnt4, "0"));
                            cntKei4All    += Integer.parseInt(StringUtils.defaultString(cnt4All, "0"));
                        }
                        if ("".equals(StringUtils.defaultString(grade5))) {
                            mKeiCnt5      += Integer.parseInt(StringUtils.defaultString(mCnt5, "0"));
                            wKeiCnt5      += Integer.parseInt(StringUtils.defaultString(wCnt5, "0"));
                            cntKei5All    += Integer.parseInt(StringUtils.defaultString(cnt5All, "0"));
                        }
                        if ("".equals(StringUtils.defaultString(grade6))) {
                            mKeiCnt6      += Integer.parseInt(StringUtils.defaultString(mCnt6, "0"));
                            wKeiCnt6      += Integer.parseInt(StringUtils.defaultString(wCnt6, "0"));
                            cntKei6All    += Integer.parseInt(StringUtils.defaultString(cnt6All, "0"));
                        }
                        if ("".equals(StringUtils.defaultString(grade7))) {
                            mKeiCnt7      += Integer.parseInt(StringUtils.defaultString(mCnt7, "0"));
                            wKeiCnt7      += Integer.parseInt(StringUtils.defaultString(wCnt7, "0"));
                            cntKei7All    += Integer.parseInt(StringUtils.defaultString(cnt7All, "0"));
                        }
                        if ("".equals(StringUtils.defaultString(grade8))) {
                            mKeiCnt8      += Integer.parseInt(StringUtils.defaultString(mCnt8, "0"));
                            wKeiCnt8      += Integer.parseInt(StringUtils.defaultString(wCnt8, "0"));
                            cntKei8All    += Integer.parseInt(StringUtils.defaultString(cnt8All, "0"));
                        }
                        if ("".equals(StringUtils.defaultString(grade9))) {
                            mKeiCnt9      += Integer.parseInt(StringUtils.defaultString(mCnt9, "0"));
                            wKeiCnt9      += Integer.parseInt(StringUtils.defaultString(wCnt9, "0"));
                            cntKei9All    += Integer.parseInt(StringUtils.defaultString(cnt9All, "0"));
                        }
                        if ("".equals(StringUtils.defaultString(grade10))) {
                            mKeiCnt10     += Integer.parseInt(StringUtils.defaultString(mCnt10, "0"));
                            wKeiCnt10     += Integer.parseInt(StringUtils.defaultString(wCnt10, "0"));
                            cntKei10All    += Integer.parseInt(StringUtils.defaultString(cnt10All, "0"));
                        }
                        if ("".equals(StringUtils.defaultString(grade11))) {
                            mKeiCnt11      += Integer.parseInt(StringUtils.defaultString(mCnt11, "0"));
                            wKeiCnt11      += Integer.parseInt(StringUtils.defaultString(wCnt11, "0"));
                            cntKei11All    += Integer.parseInt(StringUtils.defaultString(cnt11All, "0"));
                        }

                        mKeiCntALL    = Integer.parseInt(mCntALL);
                        wKeiCntALL    = Integer.parseInt(wCntALL);
                        cntKeiAll     = Integer.parseInt(cntAll);
                    }
                    String setPref = pref;
                    if (!"".equals(befPref) && befPref.equals(pref)) {
                        setPref = "";
                    }

                    final InjiData injiData = new InjiData(setPref, city, mCnt1, wCnt1, cnt1All, mCnt2, wCnt2, cnt2All, mCnt3, wCnt3, cnt3All, mCnt4, wCnt4, cnt4All, mCnt5, wCnt5, cnt5All, mCnt6, wCnt6, cnt6All
                    		                                , mCnt7, wCnt7, cnt7All, mCnt8, wCnt8, cnt8All, mCnt9, wCnt9, cnt9All, mCnt10, wCnt10, cnt10All, mCnt11, wCnt11, cnt11All, mCntALL, wCntALL, cntAll);

                    injiList.add(injiData);
                    befPref = pref;
                }
                final InjiData injiData = new InjiData(befPref, "", String.valueOf(mKeiCnt1), String.valueOf(wKeiCnt1), String.valueOf(cntKei1All), String.valueOf(mKeiCnt2), String.valueOf(wKeiCnt2), String.valueOf(cntKei2All), String.valueOf(mKeiCnt3), String.valueOf(wKeiCnt3), String.valueOf(cntKei3All), String.valueOf(mKeiCnt4), String.valueOf(wKeiCnt4), String.valueOf(cntKei4All), String.valueOf(mKeiCnt5), String.valueOf(wKeiCnt5), String.valueOf(cntKei5All), String.valueOf(mKeiCnt6), String.valueOf(wKeiCnt6), String.valueOf(cntKei6All)
                		                                , String.valueOf(mKeiCnt7), String.valueOf(wKeiCnt7), String.valueOf(cntKei7All), String.valueOf(mKeiCnt8), String.valueOf(wKeiCnt8), String.valueOf(cntKei8All), String.valueOf(mKeiCnt9), String.valueOf(wKeiCnt9), String.valueOf(cntKei9All), String.valueOf(mKeiCnt10), String.valueOf(wKeiCnt10), String.valueOf(cntKei10All), String.valueOf(mKeiCnt11), String.valueOf(wKeiCnt11), String.valueOf(cntKei11All)
                		                                , String.valueOf(mKeiCntALL), String.valueOf(wKeiCntALL), String.valueOf(cntKeiAll));
                totalInjiList.add(injiData);

                injiMap.put("INJI_DATA", injiList);
                injiMap.put("TOTAL_DATA", totalInjiList);
                retList.add(injiMap);
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return retList;
    }


    private String sql(final String schoolKind, final String grade1, final String grade2, final String grade3, final String grade4, final String grade5, final String grade6, final String grade7, final String grade8, final String grade9, final String grade10, final String grade11) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     CASE WHEN PRE.PREF_CD = '" + _param._schoolPref + "' ");
        stb.append("          THEN '999' ");
        stb.append("          ELSE PRE.PREF_CD ");
        stb.append("     END AS ORDER_PREF, ");
        stb.append("     ZIP.PREF, ");
        stb.append("     CAST(CASE WHEN POSSTR(REPLACE(ADDR.ADDR1, ZIP.PREF, ''), '郡') > 0 ");
        stb.append("               THEN SUBSTR(REPLACE(ADDR.ADDR1, ZIP.PREF, ''), 1, POSSTR(REPLACE(ADDR.ADDR1, ZIP.PREF, ''), '郡') - 1) || '郡' ");
        stb.append("               ELSE CASE WHEN POSSTR(REPLACE(ADDR.ADDR1, ZIP.PREF, ''), '区') > 0 ");
        stb.append("                         THEN SUBSTR(REPLACE(ADDR.ADDR1, ZIP.PREF, ''), 1, POSSTR(REPLACE(ADDR.ADDR1, ZIP.PREF, ''), '区') - 1) || '区' ");
        stb.append("                         ELSE CASE WHEN POSSTR(REPLACE(ADDR.ADDR1, ZIP.PREF, ''), '市') > 0 ");
        stb.append("                                   THEN SUBSTR(REPLACE(ADDR.ADDR1, ZIP.PREF, ''), 1, POSSTR(REPLACE(ADDR.ADDR1, ZIP.PREF, ''), '市') - 1) || '市' ");
        stb.append("                                   ELSE '' ");
        stb.append("                              END ");
        stb.append("                    END ");
        stb.append("          END AS VARCHAR(4000) ");
        stb.append("     ) AS CITYNAME, ");
        stb.append("     ADDR.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     BASE.SEX ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("           AND REGD.GRADE = GDAT.GRADE ");
        stb.append("           AND GDAT.SCHOOL_KIND = '" + schoolKind + "' ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO, ");
        stb.append("     SCHREG_ADDRESS_DAT ADDR ");
        stb.append("     LEFT JOIN ZIPCD_MST ZIP ON ADDR.ZIPCD = ZIP.NEW_ZIPCD ");
        stb.append("     LEFT JOIN PREF_MST PRE ON PRE.PREF_NAME = ZIP.PREF ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._year + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND ADDR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     AND '" + _param._date + "' BETWEEN ADDR.ISSUEDATE AND ADDR.EXPIREDATE ");
        stb.append("     AND ZIP.PREF IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     ORDER_PREF, ");
        stb.append("     PREF, ");
        stb.append("     CITYNAME, ");
        stb.append("     SUM(CASE WHEN GRADE = '" + grade1 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT1, ");
        stb.append("     SUM(CASE WHEN GRADE = '" + grade1 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT1, ");
        stb.append("     SUM(CASE WHEN GRADE = '" + grade1 + "' THEN 1 ELSE 0 END) AS CNT1_ALL, ");
        stb.append("     SUM(CASE WHEN GRADE = '" + grade2 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT2, ");
        stb.append("     SUM(CASE WHEN GRADE = '" + grade2 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT2, ");
        stb.append("     SUM(CASE WHEN GRADE = '" + grade2 + "' THEN 1 ELSE 0 END) AS CNT2_ALL, ");
        stb.append("     SUM(CASE WHEN GRADE = '" + grade3 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT3, ");
        stb.append("     SUM(CASE WHEN GRADE = '" + grade3 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT3, ");
        stb.append("     SUM(CASE WHEN GRADE = '" + grade3 + "' THEN 1 ELSE 0 END) AS CNT3_ALL, ");
        if (!"".equals(grade4)) {
            stb.append("     SUM(CASE WHEN GRADE = '" + grade4 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT4, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade4 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT4, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade4 + "' THEN 1 ELSE 0 END) AS CNT4_ALL, ");
        }
        if (!"".equals(grade5)) {
            stb.append("     SUM(CASE WHEN GRADE = '" + grade5 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT5, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade5 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT5, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade5 + "' THEN 1 ELSE 0 END) AS CNT5_ALL, ");
        }
        if (!"".equals(grade6)) {
            stb.append("     SUM(CASE WHEN GRADE = '" + grade6 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT6, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade6 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT6, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade6 + "' THEN 1 ELSE 0 END) AS CNT6_ALL, ");
        }
        if (!"".equals(grade7)) {
            stb.append("     SUM(CASE WHEN GRADE = '" + grade7 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT7, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade7 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT7, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade7 + "' THEN 1 ELSE 0 END) AS CNT7_ALL, ");
        }
        if (!"".equals(grade8)) {
            stb.append("     SUM(CASE WHEN GRADE = '" + grade8 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT8, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade8 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT8, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade8 + "' THEN 1 ELSE 0 END) AS CNT8_ALL, ");
        }
        if (!"".equals(grade9)) {
            stb.append("     SUM(CASE WHEN GRADE = '" + grade9 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT9, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade9 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT9, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade9 + "' THEN 1 ELSE 0 END) AS CNT9_ALL, ");
        }
        if (!"".equals(grade10)) {
            stb.append("     SUM(CASE WHEN GRADE = '" + grade10 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT10, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade10 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT10, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade10 + "' THEN 1 ELSE 0 END) AS CNT10_ALL, ");
        }
        if (!"".equals(grade11)) {
            stb.append("     SUM(CASE WHEN GRADE = '" + grade11 + "' AND SEX = '1' THEN 1 ELSE 0 END) AS M_CNT11, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade11 + "' AND SEX = '2' THEN 1 ELSE 0 END) AS W_CNT11, ");
            stb.append("     SUM(CASE WHEN GRADE = '" + grade11 + "' THEN 1 ELSE 0 END) AS CNT11_ALL, ");
        }
        stb.append("     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS M_CNT_ALL, ");
        stb.append("     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS W_CNT_ALL, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     ORDER_PREF, ");
        stb.append("     PREF, ");
        stb.append("     CITYNAME ");
        stb.append(" ORDER BY ");
        stb.append("     ORDER_PREF DESC, ");
        stb.append("     CITYNAME DESC ");

        return stb.toString();
    }

    private void outputCsv(final HttpServletRequest request, final HttpServletResponse response, final DB2UDB db2) {
        final String filename = getTitle(db2) + ".csv";
        final Map csvParam = new HashMap();
        csvParam.put("HttpServletRequest", request);

    	final List fixedList = new ArrayList();
    	fixedList.add(_param._SCHOOLKIND);

    	boolean putFstTitleFlg = true;
    	final List schKList = "1".equals(_param._use_prg_schoolkind) ? Arrays.asList(StringUtils.split(_param._selectSchoolKind, ':')) : fixedList;
        final List lines = new ArrayList();
    	for (Iterator ite = schKList.iterator();ite.hasNext();) {
    		final String getSchKStr = (String)ite.next();
        	_param._gdatList = _param.getGdatList(db2, getSchKStr);
        	if (_param._gdatList == null || (_param._gdatList != null && _param._gdatList.size() == 0)) {
        		continue;
        	}
        	getCsvOutputLine(db2, putFstTitleFlg, lines);
        	putFstTitleFlg = false;
    	}
        CsvUtils.outputLines(log, response, filename, lines, csvParam);
    }

    private List getCsvOutputLine(final DB2UDB db2, final boolean putFstTitleFlg, final List lines) {
        final List list = getList(db2);

        for (int line = 0; line < list.size(); line++) {
            final Map injiMap = (Map) list.get(line);
            final RegdGdat regdGdat = (RegdGdat) injiMap.get("REGDG");
            final List injiList = (List) injiMap.get("INJI_DATA");
            if (injiList.size() == 0) {
                continue;
            }

            lines.add(Arrays.asList(new String[] {""}));
            lines.add(Arrays.asList(new String[] {""}));

            if (putFstTitleFlg) {
                lines.add(Arrays.asList(new String[] {"", "", "", getTitle(db2)}));
                lines.add(Arrays.asList(new String[] {"", "", "", "", "","", "", "","", "", "", KNJ_EditDate.h_format_JP(db2, _param._date)}));
            }

            final List dataTitle = new ArrayList();
            dataTitle.add("通学地域");
            dataTitle.add("");
            List dispGradeList = new ArrayList();
            int gradeWkCnt = 1;
            for (Iterator itGdat = regdGdat._gradeMap.keySet().iterator(); itGdat.hasNext();) {
                final String grade = (String) itGdat.next();
                final GradeData gradeData = (GradeData) regdGdat._gradeMap.get(grade);
                dataTitle.add(gradeData._gradeName);
                dataTitle.add("");
                dataTitle.add("");
                dispGradeList.add(String.valueOf(gradeWkCnt));
                gradeWkCnt++;
            }
            dataTitle.add("全学年");
            lines.add(dataTitle);

            final List dataTitle2 = new ArrayList();
            dataTitle2.add("都・県");
            dataTitle2.add("区・市・郡");
            for (int tCnt = 0;tCnt < dispGradeList.size();tCnt++) {
                dataTitle2.add("男");
                dataTitle2.add("女");
                dataTitle2.add("小計");
            }
            dataTitle2.add("男");
            dataTitle2.add("女");
            dataTitle2.add("合計");
            lines.add(dataTitle2);

            for (Iterator itInji = injiList.iterator(); itInji.hasNext();) {
                final List injiLine = new ArrayList();

                final InjiData injiData = (InjiData) itInji.next();
                injiLine.add(injiData._pref);
                injiLine.add(injiData._city);
                if (dispGradeList.contains("1")) {
                    injiLine.add(injiData._mCnt1);
                    injiLine.add(injiData._wCnt1);
                    injiLine.add(injiData._cnt1All);
                }
                if (dispGradeList.contains("2")) {
                    injiLine.add(injiData._mCnt2);
                    injiLine.add(injiData._wCnt2);
                    injiLine.add(injiData._cnt2All);
                }
                if (dispGradeList.contains("3")) {
                    injiLine.add(injiData._mCnt3);
                    injiLine.add(injiData._wCnt3);
                    injiLine.add(injiData._cnt3All);
                }
                if (dispGradeList.contains("4")) {
                    injiLine.add(injiData._mCnt4);
                    injiLine.add(injiData._wCnt4);
                    injiLine.add(injiData._cnt4All);
                }
                if (dispGradeList.contains("5")) {
                    injiLine.add(injiData._mCnt5);
                    injiLine.add(injiData._wCnt5);
                    injiLine.add(injiData._cnt5All);
                }
                if (dispGradeList.contains("6")) {
                    injiLine.add(injiData._mCnt6);
                    injiLine.add(injiData._wCnt6);
                    injiLine.add(injiData._cnt6All);
                }
                if (dispGradeList.contains("7")) {
                    injiLine.add(injiData._mCnt7);
                    injiLine.add(injiData._wCnt7);
                    injiLine.add(injiData._cnt7All);
                }
                if (dispGradeList.contains("8")) {
                    injiLine.add(injiData._mCnt8);
                    injiLine.add(injiData._wCnt8);
                    injiLine.add(injiData._cnt8All);
                }
                if (dispGradeList.contains("9")) {
                    injiLine.add(injiData._mCnt9);
                    injiLine.add(injiData._wCnt9);
                    injiLine.add(injiData._cnt9All);
                }
                if (dispGradeList.contains("10")) {
                    injiLine.add(injiData._mCnt10);
                    injiLine.add(injiData._wCnt10);
                    injiLine.add(injiData._cnt10All);
                }
                if (dispGradeList.contains("11")) {
                    injiLine.add(injiData._mCnt11);
                    injiLine.add(injiData._wCnt11);
                    injiLine.add(injiData._cnt11All);
                }

                injiLine.add(injiData._mCntALL);
                injiLine.add(injiData._wCntALL);
                injiLine.add(injiData._cntAll);
                lines.add(injiLine);
            }

            lines.add(Arrays.asList(new String[] {""}));
            lines.add(Arrays.asList(new String[] {""}));

            final List dataTitle3 = new ArrayList();
            dataTitle3.add("通学地域");
            dataTitle3.add("");
            for (Iterator itGdat = regdGdat._gradeMap.keySet().iterator(); itGdat.hasNext();) {
                final String grade = (String) itGdat.next();
                final GradeData gradeData = (GradeData) regdGdat._gradeMap.get(grade);
                dataTitle3.add(gradeData._gradeName);
                dataTitle3.add("");
                dataTitle3.add("");
            }
            dataTitle3.add("全学年");
            lines.add(dataTitle3);

            final List dataTitle4 = new ArrayList();
            dataTitle4.add("都・県");
            dataTitle4.add("");
            for (int tCnt = 0;tCnt < dispGradeList.size();tCnt++) {
                dataTitle4.add("男");
                dataTitle4.add("女");
                dataTitle4.add("小計");
            }
            dataTitle4.add("男");
            dataTitle4.add("女");
            dataTitle4.add("合計");
            lines.add(dataTitle4);

            final List totalList = (List) injiMap.get("TOTAL_DATA");
            int mKeiCnt1      = 0;
            int wKeiCnt1      = 0;
            int cntKei1All    = 0;
            int mKeiCnt2      = 0;
            int wKeiCnt2      = 0;
            int cntKei2All    = 0;
            int mKeiCnt3      = 0;
            int wKeiCnt3      = 0;
            int cntKei3All    = 0;
            int mKeiCnt4      = 0;
            int wKeiCnt4      = 0;
            int cntKei4All    = 0;
            int mKeiCnt5      = 0;
            int wKeiCnt5      = 0;
            int cntKei5All    = 0;
            int mKeiCnt6      = 0;
            int wKeiCnt6      = 0;
            int cntKei6All    = 0;
            int mKeiCnt7      = 0;
            int wKeiCnt7      = 0;
            int cntKei7All    = 0;
            int mKeiCnt8      = 0;
            int wKeiCnt8      = 0;
            int cntKei8All    = 0;
            int mKeiCnt9      = 0;
            int wKeiCnt9      = 0;
            int cntKei9All    = 0;
            int mKeiCnt10     = 0;
            int wKeiCnt10     = 0;
            int cntKei10All   = 0;
            int mKeiCnt11     = 0;
            int wKeiCnt11     = 0;
            int cntKei11All   = 0;
            int mKeiCntALL    = 0;
            int wKeiCntALL    = 0;
            int cntKeiAll     = 0;
            for (Iterator itTotal = totalList.iterator(); itTotal.hasNext();) {
                final InjiData injiData = (InjiData) itTotal.next();
                final List injiLine = new ArrayList();
                injiLine.add(injiData._pref);
                injiLine.add(injiData._city);

                if (dispGradeList.contains("1")) {
                    injiLine.add(injiData._mCnt1);
                    injiLine.add(injiData._wCnt1);
                    injiLine.add(injiData._cnt1All);
                }
                if (dispGradeList.contains("2")) {
                    injiLine.add(injiData._mCnt2);
                    injiLine.add(injiData._wCnt2);
                    injiLine.add(injiData._cnt2All);
                }
                if (dispGradeList.contains("3")) {
                    injiLine.add(injiData._mCnt3);
                    injiLine.add(injiData._wCnt3);
                    injiLine.add(injiData._cnt3All);
                }
                if (dispGradeList.contains("4")) {
                    injiLine.add(injiData._mCnt4);
                    injiLine.add(injiData._wCnt4);
                    injiLine.add(injiData._cnt4All);
                }
                if (dispGradeList.contains("5")) {
                    injiLine.add(injiData._mCnt5);
                    injiLine.add(injiData._wCnt5);
                    injiLine.add(injiData._cnt5All);
                }
                if (dispGradeList.contains("6")) {
                    injiLine.add(injiData._mCnt6);
                    injiLine.add(injiData._wCnt6);
                    injiLine.add(injiData._cnt6All);
                }
                if (dispGradeList.contains("7")) {
                    injiLine.add(injiData._mCnt7);
                    injiLine.add(injiData._wCnt7);
                    injiLine.add(injiData._cnt7All);
                }
                if (dispGradeList.contains("8")) {
                    injiLine.add(injiData._mCnt8);
                    injiLine.add(injiData._wCnt8);
                    injiLine.add(injiData._cnt8All);
                }
                if (dispGradeList.contains("9")) {
                    injiLine.add(injiData._mCnt9);
                    injiLine.add(injiData._wCnt9);
                    injiLine.add(injiData._cnt9All);
                }
                if (dispGradeList.contains("10")) {
                    injiLine.add(injiData._mCnt10);
                    injiLine.add(injiData._wCnt10);
                    injiLine.add(injiData._cnt10All);
                }
                if (dispGradeList.contains("11")) {
                    injiLine.add(injiData._mCnt11);
                    injiLine.add(injiData._wCnt11);
                    injiLine.add(injiData._cnt11All);
                }

                injiLine.add(injiData._mCntALL);
                injiLine.add(injiData._wCntALL);
                injiLine.add(injiData._cntAll);
                lines.add(injiLine);

                mKeiCnt1      += Integer.parseInt(injiData._mCnt1);
                wKeiCnt1      += Integer.parseInt(injiData._wCnt1);
                cntKei1All    += Integer.parseInt(injiData._cnt1All);
                mKeiCnt2      += Integer.parseInt(injiData._mCnt2);
                wKeiCnt2      += Integer.parseInt(injiData._wCnt2);
                cntKei2All    += Integer.parseInt(injiData._cnt2All);
                mKeiCnt3      += Integer.parseInt(injiData._mCnt3);
                wKeiCnt3      += Integer.parseInt(injiData._wCnt3);
                cntKei3All    += Integer.parseInt(injiData._cnt3All);
                mKeiCnt4      += Integer.parseInt(injiData._mCnt4);
                wKeiCnt4      += Integer.parseInt(injiData._wCnt4);
                cntKei4All    += Integer.parseInt(injiData._cnt4All);
                mKeiCnt5      += Integer.parseInt(injiData._mCnt5);
                wKeiCnt5      += Integer.parseInt(injiData._wCnt5);
                cntKei5All    += Integer.parseInt(injiData._cnt5All);
                mKeiCnt6      += Integer.parseInt(injiData._mCnt6);
                wKeiCnt6      += Integer.parseInt(injiData._wCnt6);
                cntKei6All    += Integer.parseInt(injiData._cnt6All);
                mKeiCnt7      += Integer.parseInt(injiData._mCnt7);
                wKeiCnt7      += Integer.parseInt(injiData._wCnt7);
                cntKei7All    += Integer.parseInt(injiData._cnt7All);
                mKeiCnt8      += Integer.parseInt(injiData._mCnt8);
                wKeiCnt8      += Integer.parseInt(injiData._wCnt8);
                cntKei8All    += Integer.parseInt(injiData._cnt8All);
                mKeiCnt9      += Integer.parseInt(injiData._mCnt9);
                wKeiCnt9      += Integer.parseInt(injiData._wCnt9);
                cntKei9All    += Integer.parseInt(injiData._cnt9All);
                mKeiCnt10     += Integer.parseInt(injiData._mCnt10);
                wKeiCnt10     += Integer.parseInt(injiData._wCnt10);
                cntKei10All   += Integer.parseInt(injiData._cnt10All);
                mKeiCnt11     += Integer.parseInt(injiData._mCnt11);
                wKeiCnt11     += Integer.parseInt(injiData._wCnt11);
                cntKei11All   += Integer.parseInt(injiData._cnt11All);
                mKeiCntALL    += Integer.parseInt(injiData._mCntALL);
                wKeiCntALL    += Integer.parseInt(injiData._wCntALL);
                cntKeiAll     += Integer.parseInt(injiData._cntAll);
            }
            final List injiLine = new ArrayList();
            injiLine.add("総計");
            injiLine.add("");
            if (dispGradeList.contains("1")) {
                injiLine.add(String.valueOf(mKeiCnt1));
                injiLine.add(String.valueOf(wKeiCnt1));
                injiLine.add(String.valueOf(cntKei1All));
            }
            if (dispGradeList.contains("2")) {
                injiLine.add(String.valueOf(mKeiCnt2));
                injiLine.add(String.valueOf(wKeiCnt2));
                injiLine.add(String.valueOf(cntKei2All));
            }
            if (dispGradeList.contains("3")) {
                injiLine.add(String.valueOf(mKeiCnt3));
                injiLine.add(String.valueOf(wKeiCnt3));
                injiLine.add(String.valueOf(cntKei3All));
            }
            if (dispGradeList.contains("4")) {
                injiLine.add(String.valueOf(mKeiCnt4));
                injiLine.add(String.valueOf(wKeiCnt4));
                injiLine.add(String.valueOf(cntKei4All));
            }
            if (dispGradeList.contains("5")) {
                injiLine.add(String.valueOf(mKeiCnt5));
                injiLine.add(String.valueOf(wKeiCnt5));
                injiLine.add(String.valueOf(cntKei5All));
            }
            if (dispGradeList.contains("6")) {
                injiLine.add(String.valueOf(mKeiCnt6));
                injiLine.add(String.valueOf(wKeiCnt6));
                injiLine.add(String.valueOf(cntKei6All));
            }
            if (dispGradeList.contains("7")) {
                injiLine.add(String.valueOf(mKeiCnt7));
                injiLine.add(String.valueOf(wKeiCnt7));
                injiLine.add(String.valueOf(cntKei7All));
            }
            if (dispGradeList.contains("8")) {
                injiLine.add(String.valueOf(mKeiCnt8));
                injiLine.add(String.valueOf(wKeiCnt8));
                injiLine.add(String.valueOf(cntKei8All));
            }
            if (dispGradeList.contains("9")) {
                injiLine.add(String.valueOf(mKeiCnt9));
                injiLine.add(String.valueOf(wKeiCnt9));
                injiLine.add(String.valueOf(cntKei9All));
            }
            if (dispGradeList.contains("10")) {
                injiLine.add(String.valueOf(mKeiCnt10));
                injiLine.add(String.valueOf(wKeiCnt10));
                injiLine.add(String.valueOf(cntKei10All));
            }
            if (dispGradeList.contains("11")) {
                injiLine.add(String.valueOf(mKeiCnt11));
                injiLine.add(String.valueOf(wKeiCnt11));
                injiLine.add(String.valueOf(cntKei11All));
            }

            injiLine.add(String.valueOf(mKeiCntALL));
            injiLine.add(String.valueOf(wKeiCntALL));
            injiLine.add(String.valueOf(cntKeiAll));
            lines.add(injiLine);

        }

        return lines;
    }

    private String getTitle(final DB2UDB db2) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        final String title = "　生徒通学地域一覧";
        return nendo + title;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77269 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _date;
        private final String _cmd;
        private final String _schoolPref;
        private List _gdatList;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("CTRL_DATE");
            _cmd = request.getParameter("cmd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _schoolPref = getSchoolPref(db2);
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            final String ssKStr = request.getParameter("selectSchoolKind");
            _selectSchoolKind = "".equals(ssKStr) ? getNM_A023(db2) : ssKStr;
        }

        private String getNM_A023(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            String sep = "";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   T1.NAME1 ");
                stb.append(" FROM ");
                stb.append("   V_NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.NAMECD1 = 'A023' ");
                stb.append(" ORDER BY T1.NAME1 DESC ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retstr += sep + rs.getString("NAME1");
                    sep = ":";
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }
        private String getSchoolPref(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     PRE.PREF_CD ");
                stb.append(" FROM ");
                stb.append("     SCHOOL_MST T1 ");
                stb.append("     LEFT JOIN ZIPCD_MST ZIP ON T1.SCHOOLZIPCD = ZIP.NEW_ZIPCD ");
                stb.append("     LEFT JOIN PREF_MST PRE ON PRE.PREF_NAME = ZIP.PREF ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retstr = rs.getString("PREF_CD");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        public List getGdatList(final DB2UDB db2, final String selSchKind) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                String sql = " SELECT * FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' ";
                if ("1".equals(_use_prg_schoolkind) ) {
                	if (!StringUtils.isBlank(_selectSchoolKind)) {
                        sql += " AND SCHOOL_KIND = '" + selSchKind + "' ";
                    }
                } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                    sql += "   AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
                }
                sql += " ORDER BY SCHOOL_KIND DESC, GRADE ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                String befSchoolKind = "";
                RegdGdat regdGdat = null;
                while (rs.next()) {
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    if (!"".equals(befSchoolKind) && !befSchoolKind.equals(schoolKind)) {
                        continue;
                    }
                    if (null == regdGdat) {
                        regdGdat = new RegdGdat(schoolKind);
                    }
                    if (!"".equals(befSchoolKind) && !befSchoolKind.equals(schoolKind)) {
                        retList.add(regdGdat);
                        regdGdat = new RegdGdat(schoolKind);
                    }
                    final String grade = rs.getString("GRADE");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String gradeName = rs.getString("GRADE_NAME1");
                    final GradeData gradeData = new GradeData(grade, gradeCd, gradeName);
                    regdGdat._gradeMap.put(grade, gradeData);

                    befSchoolKind = schoolKind;
                }
                if (regdGdat != null) {
                    retList.add(regdGdat);
                }
        	} catch (Exception e) {
        		log.error("getGdatList exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

    }

    private class RegdGdat {
        private final String _schoolKind;
        private final Map _gradeMap;
        public RegdGdat(final String schoolKind) {
            _schoolKind = schoolKind;
            _gradeMap = new TreeMap();
        }
    }

    private class GradeData {
        private final String _grade;
        private final String _gradeCd;
        private final String _gradeName;
        public GradeData(
                final String grade,
                final String gradeCd,
                final String gradeName
        ) {
            _grade = grade;
            _gradeCd = gradeCd;
            _gradeName = gradeName;
        }
    }

    private class InjiData {
        private final String _pref;
        private final String _city;
        private final String _mCnt1;
        private final String _wCnt1;
        private final String _cnt1All;
        private final String _mCnt2;
        private final String _wCnt2;
        private final String _cnt2All;
        private final String _mCnt3;
        private final String _wCnt3;
        private final String _cnt3All;
        private final String _mCnt4;
        private final String _wCnt4;
        private final String _cnt4All;
        private final String _mCnt5;
        private final String _wCnt5;
        private final String _cnt5All;
        private final String _mCnt6;
        private final String _wCnt6;
        private final String _cnt6All;
        private final String _mCnt7;
        private final String _wCnt7;
        private final String _cnt7All;
        private final String _mCnt8;
        private final String _wCnt8;
        private final String _cnt8All;
        private final String _mCnt9;
        private final String _wCnt9;
        private final String _cnt9All;
        private final String _mCnt10;
        private final String _wCnt10;
        private final String _cnt10All;
        private final String _mCnt11;
        private final String _wCnt11;
        private final String _cnt11All;
        private final String _mCntALL;
        private final String _wCntALL;
        private final String _cntAll;
        public InjiData(
                final String pref,
                final String city,
                final String mCnt1,
                final String wCnt1,
                final String cnt1All,
                final String mCnt2,
                final String wCnt2,
                final String cnt2All,
                final String mCnt3,
                final String wCnt3,
                final String cnt3All,
                final String mCnt4,
                final String wCnt4,
                final String cnt4All,
                final String mCnt5,
                final String wCnt5,
                final String cnt5All,
                final String mCnt6,
                final String wCnt6,
                final String cnt6All,
                final String mCnt7,
                final String wCnt7,
                final String cnt7All,
                final String mCnt8,
                final String wCnt8,
                final String cnt8All,
                final String mCnt9,
                final String wCnt9,
                final String cnt9All,
                final String mCnt10,
                final String wCnt10,
                final String cnt10All,
                final String mCnt11,
                final String wCnt11,
                final String cnt11All,
                final String mCntALL,
                final String wCntALL,
                final String cntAll
        ) {
            _pref       = pref;
            _city       = city;
            _mCnt1      = mCnt1;
            _wCnt1      = wCnt1;
            _cnt1All    = cnt1All;
            _mCnt2      = mCnt2;
            _wCnt2      = wCnt2;
            _cnt2All    = cnt2All;
            _mCnt3      = mCnt3;
            _wCnt3      = wCnt3;
            _cnt3All    = cnt3All;
            _mCntALL    = mCntALL;
            _wCntALL    = wCntALL;
            _cntAll     = cntAll;

            _mCnt4      = mCnt4;
            _wCnt4      = wCnt4;
            _cnt4All    = cnt4All;
            _mCnt5      = mCnt5;
            _wCnt5      = wCnt5;
            _cnt5All    = cnt5All;
            _mCnt6      = mCnt6;
            _wCnt6      = wCnt6;
            _cnt6All    = cnt6All;
            _mCnt7      = mCnt7;
            _wCnt7      = wCnt7;
            _cnt7All    = cnt7All;
            _mCnt8      = mCnt8;
            _wCnt8      = wCnt8;
            _cnt8All    = cnt8All;
            _mCnt9      = mCnt9;
            _wCnt9      = wCnt9;
            _cnt9All    = cnt9All;
            _mCnt10     = mCnt10;
            _wCnt10     = wCnt10;
            _cnt10All   = cnt10All;
            _mCnt11     = mCnt11;
            _wCnt11     = wCnt11;
            _cnt11All   = cnt11All;
        }
    }
}

// eof

