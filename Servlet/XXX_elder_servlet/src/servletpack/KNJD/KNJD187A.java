/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2018/07/19
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD187A {

    private static final Log log = LogFactory.getLog(KNJD187A.class);

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 7教科科目コード。 */
    private static final String ALL7 = "777777";
    /** 9教科科目コード。 */
    private static final String ALL9 = "999999";

    private static final String SEMEALL = "9";

    private static final int MAXCOL = 16;
    private static final int SCORE_RANGE = 5;
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final String formName = "H".equals(_param._schoolKind) ? "KNJD187A_2.frm" : "KNJD187A.frm";
        svf.VrSetForm(formName, 1);
        final Map studentMap = getGHMapInfo(db2);
        for (Iterator ite = studentMap.keySet().iterator(); ite.hasNext();) {
            final String getKey = (String)ite.next();
            final GradeHrCls hrClSObj = (GradeHrCls)studentMap.get(getKey);
            for (Iterator its = hrClSObj._schregMap.keySet().iterator();its.hasNext();) {
                final String getSubKey = (String)its.next();
                final Student student = (Student)hrClSObj._schregMap.get(getSubKey);

                setExceptSpread(db2, svf, hrClSObj, student);
                //表1 成績(レコードフォーマットへ変わる事前提で処理を配置)
                final String scKey = "99-" + hrClSObj._schoolKind + "-99-999999";
                int putCol = 1;
                int pageCnt = 1;
                for (Iterator itr = student._subclsInfoMap.keySet().iterator();itr.hasNext();) {
                    final String key1 = (String)itr.next();  //classcd - schkind
                    final Map subMap = (Map)student._subclsInfoMap.get(key1);
                    for (Iterator itd = subMap.keySet().iterator();itd.hasNext();) {
                        final String key2 = (String)itd.next();  //subclassのFULLセットのキー
                        final SubclsInfo si = (SubclsInfo)subMap.get(key2);
                        if (putCol > MAXCOL) {
                            svf.VrEndPage();
                            setExceptSpread(db2, svf, hrClSObj, student);
                            putCol = 1;
                            pageCnt++;
                        }
                        //教科名
                        svf.VrsOutn("CLASS_NAME1", putCol, si._subclassAbbv);
                        final String sKey1 = _param._semester + _param._testcd + "08" + "-" + key2;
                        if (student._subclassRankMap.containsKey(sKey1)) {
                            SubclassRank prtwk1 = (SubclassRank)student._subclassRankMap.get(sKey1);
                            svf.VrsOutn("SCORE1_1", putCol, String.valueOf(prtwk1._score));  //成績

                            //学年平均
                            final String gAvg = (!"".equals(StringUtils.defaultString(prtwk1._gAvg, ""))) ? prtwk1._gAvg : "0";
                            svf.VrsOutn("GRADE_AVE", putCol, gAvg);

                            //ヒストグラム
                            if (pageCnt == 1 && putCol <= 9) {
                                //科目別コース別のヒストグラムのため、科目コード(混合)＋教育課程(混合)でキーを指定
                                final String key3 = key2 + "-" + student._courseCd + "-" + student._majorCd + "-" + student._courseCode;
                                if (_param._subclassBunpuMap.containsKey(key3)) {
                                    final SubclassBunpu bunpu = (SubclassBunpu) _param._subclassBunpuMap.get(key3);
                                    printRange(svf, bunpu._score9, 1, putCol);
                                    printRange(svf, bunpu._score8, 2, putCol);
                                    printRange(svf, bunpu._score7, 3, putCol);
                                    printRange(svf, bunpu._score6, 4, putCol);
                                    printRange(svf, bunpu._score5, 5, putCol);
                                    printRange(svf, bunpu._score4, 6, putCol);
                                    printRange(svf, bunpu._score3, 7, putCol);
                                    printRange(svf, bunpu._score2, 8, putCol);
                                    printRange(svf, bunpu._score1, 9, putCol);
                                    printRange(svf, bunpu._score0, 10, putCol);
                                    printRange(svf, bunpu.getTotal(), 11, putCol);
                                }
                            }
                        }
                        final String sKey2 = _param._semester + _param._testcd + "09" + "-" + key2;
                        if (student._subclassRankMap.containsKey(sKey2)) {
                            SubclassRank prtwk2 = (SubclassRank)student._subclassRankMap.get(sKey2);
                            svf.VrsOutn("SCORE1_2", putCol, String.valueOf(prtwk2._score));  //評定
                        }
                        putCol++;
                    }
                }

                //上記では総計が出力されない(CHAIR_STD_DAT由来の科目コードには"99-校種-99-999999"は無い)ので、ここで出力
                //平均
                ////成績
                final String sKey1 = _param._semester + _param._testcd + "08" + "-" + scKey;
                if (student._subclassRankMap.containsKey(sKey1)) {
                    SubclassRank prtwk1 = (SubclassRank)student._subclassRankMap.get(sKey1);
                    if (prtwk1 != null) {
                        svf.VrsOut("AVE1", String.valueOf(prtwk1._avg));  //成績
                    }
                }
                ////5段階
                final String sKey2 = _param._semester + _param._testcd + "09" + "-" + scKey;
                if (student._subclassRankMap.containsKey(sKey2)) {
                    SubclassRank prtwk2 = (SubclassRank)student._subclassRankMap.get(sKey2);
                    if (prtwk2 != null) {
                        svf.VrsOut("AVE2", String.valueOf(prtwk2._avg));  //評定
                    }
                }

                svf.VrEndPage();
                _hasData = true;
            }

        }
    }
    private void setExceptSpread(final DB2UDB db2, final Vrw32alp svf, final GradeHrCls hrClSObj, final Student student) {
        //タイトル
        final String nendo = _param._year + "年度";
        svf.VrsOut("NENDO", nendo);
        svf.VrsOut("SCHOOL_NAME1", (String)_param._certifInfo.get("SCHOOL_NAME"));
        svf.VrsOut("GRADE1",  Integer.parseInt(hrClSObj._gradeCd) + "年 " + _param._semesterName);

        //年組番
        svf.VrsOut("HR_NAME", student._hrName + " " + (student._attendNo.length() < 2 ? " " : "") +Integer.parseInt(student._attendNo) + "番");
        //氏名
        final int nLen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nField = nLen > 30 ? "3" : nLen > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nField, student._name);

        //担任名
        final int trLen = KNJ_EditEdit.getMS932ByteLength(hrClSObj._staffName);
        final String trField = trLen > 30 ? "3" : trLen > 20 ? "2" : "1";
        svf.VrsOut("TR_NAME"+trField, hrClSObj._staffName);

        //学年末のみ出力
        if (SEMEALL.equals(_param._semester)) {
            svf.VrsOut("GRADE2", hrClSObj._gradeName2);
            //修了年月日
            final String[] dCutWk = StringUtils.split(_param._printDate, '-');
            final String putDStr;
            if (dCutWk.length > 2) {
                final String padTuki = (dCutWk[1].length() > 1 ? "" : "0");
                final String padHi = (dCutWk[2].length() > 1 ? "" : "0");
                putDStr = dCutWk[0] + "年" + padTuki + dCutWk[1] + "月" + padHi + dCutWk[2] + "日";
            } else {
                putDStr = "";
            }
            svf.VrsOut("DATE", putDStr);
            //学校名
            svf.VrsOut("SCHOOL_NAME2", (String)_param._certifInfo.get("SCHOOL_NAME") + "長");
            //役職名
            //svf.VrsOut("", (String)_param._certifInfo.get("JOB_NAME"));???
            //校長名
            final int pLen = KNJ_EditEdit.getMS932ByteLength((String)_param._certifInfo.get("PRINCIPAL_NAME"));
            final String pField = pLen > 30 ? "3" : pLen > 20 ? "2" : "1";
            svf.VrsOut("PRINCIPAL_NAME" + pField, (String)_param._certifInfo.get("PRINCIPAL_NAME"));
            //道徳
            if (!"H".equals(_param._schoolKind)) {
                if (!"".equals(StringUtils.defaultString(student._moral, ""))) {
                    final List<String> mwk = KNJ_EditKinsoku.getTokenList(student._moral, 60, 6);
                    for (int cnt = 0;cnt < mwk.size();cnt++) {
                        svf.VrsOutn("MORAL", cnt + 1, mwk.get(cnt));
                    }
                }
            }
        }
        int tsPutMax = "H".equals(_param._schoolKind) ? 1 : 2;
        int tsPutCnt = 0;
        for (Iterator itts = student._totalStudyLst.iterator();itts.hasNext();) {
            final TotalStudyInfo prtWk = (TotalStudyInfo)itts.next();
            if (tsPutCnt < tsPutMax) {
                //学習内容
                if (!"".equals(prtWk._totalStudy_subclsName)) {
                    svf.VrsOut("TOTAL_ACT" + (tsPutCnt * 2 + 1), prtWk._totalStudy_subclsName);
                }
                //評価
                if (!"".equals(prtWk._totalStudy_remark)) {
                    final List<String> sarwk = KNJ_EditKinsoku.getTokenList(prtWk._totalStudy_remark, 60, 2);
                    for (int i = 0; i < sarwk.size(); i++) {
                        svf.VrsOutn("TOTAL_ACT" + (tsPutCnt * 2 + 2), i + 1, sarwk.get(i));
                    }
                }
            }
            tsPutCnt++;
        }

        //出欠情報
        AttendSemesDat att = (AttendSemesDat)student._attendSemesDatMap.get(_param._semester);
        ////授業日数
        svf.VrsOut("LESSON", String.valueOf(att._lesson));
        ////忌引等の日数
        svf.VrsOut("MOURNING", String.valueOf(att._suspend + att._mourning + att._virus + att._koudome));
        ////欠席日数
        svf.VrsOut("NOTICE", String.valueOf(att._sick));
        ////遅刻
        svf.VrsOut("LATE", String.valueOf(att._late));
        ////早退
        svf.VrsOut("EARLY", String.valueOf(att._early));
        //特別活動の記録
        ////クラブ
           if (!"".equals(StringUtils.defaultString(student._someClubName, ""))) {
               List<String> putwk = KNJ_EditKinsoku.getTokenList(student._someClubName, 28, 2);
               for (int cnt = 0;cnt < putwk.size();cnt++) {
                   if (!"".equals(StringUtils.defaultString(putwk.get(cnt), ""))) {
                       svf.VrsOutn("CLUB_NAME", cnt + 1, putwk.get(cnt));
                   }
               }
           }
        ////委員会
        String committeeStr = StringUtils.defaultString(student._committee);
        if (!"".equals(committeeStr)) {
                List<String> cmtewk = KNJ_EditKinsoku.getTokenList(committeeStr, 28, 2);
                for (int cnt = 0;cnt < cmtewk.size();cnt++) {
                svf.VrsOutn("COMMITTEE_NAME", cnt+1, cmtewk.get(cnt));
                }
        }

    }

    private void printRange(final Vrw32alp svf, final int score, final int setField, final int colCnt) {
        svf.VrsOutn("DIST" + setField, colCnt, String.valueOf(score));
    }

    private Map getGHMapInfo(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        GradeHrCls ghCls = null;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("  REGD.SCHREGNO, ");
            stb.append("  REGD.GRADE, ");
            stb.append("  GDAT.SCHOOL_KIND, ");
            stb.append("  GDAT.GRADE_CD, ");
            stb.append("  GDAT.GRADE_NAME1, ");
            stb.append("  GDAT.GRADE_NAME2, ");
            stb.append("  REGD.HR_CLASS, ");
            stb.append("  HDAT.HR_NAME, ");
            stb.append("  HDAT.HR_NAMEABBV, ");
            stb.append("  SM.STAFFNAME, ");
            stb.append("  REGD.ATTENDNO, ");
            stb.append("  BASE.NAME, ");
            stb.append("  REGD.COURSECD, ");
            stb.append("  REGD.MAJORCD, ");
            stb.append("  REGD.COURSECODE, ");
            stb.append("  MAJR.MAJORNAME, ");
            stb.append("  CCODE.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT REGD ");
            stb.append("    INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("          AND GDAT.GRADE = REGD.GRADE ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("          AND HDAT.GRADE = REGD.GRADE ");
            stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("    LEFT JOIN MAJOR_MST MAJR ON REGD.COURSECD = MAJR.COURSECD ");
            stb.append("         AND REGD.MAJORCD = MAJR.MAJORCD ");
            stb.append("    LEFT JOIN COURSECODE_MST CCODE ON REGD.COURSECODE = CCODE.COURSECODE ");
            stb.append("    LEFT JOIN SEMESTER_MST SEM_MST ON SEM_MST.SEMESTER = REGD.SEMESTER ");
            stb.append("    LEFT JOIN STAFF_MST SM ON SM.STAFFCD = HDAT.TR_CD1 ");
            stb.append(" WHERE ");
            stb.append("  REGD.YEAR = '" + _param._year + "' ");
            stb.append("  AND REGD.SEMESTER = '" + (SEMEALL.equals(_param._semester) ? _param._maxSemester : _param._semester) + "' ");
            if ("1".equals(_param._categoryIsClass)) {
                stb.append("  AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
            } else {
                stb.append("  AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
            }
            stb.append("  AND ( ");
            stb.append("       BASE.GRD_DATE IS NULL OR (");
            stb.append("         BASE.GRD_DATE IS NOT NULL AND ( ");
            stb.append("           (BASE.GRD_DIV IN('2','3') AND BASE.GRD_DATE > (CASE WHEN SEM_MST.EDATE < '" + _param._ctrlDate + "' THEN SEM_MST.EDATE ELSE '" + _param._ctrlDate + "' END) ");
            stb.append("           OR (BASE.ENT_DIV IN('4','5') AND BASE.ENT_DATE <= CASE WHEN SEM_MST.SDATE < '" + _param._ctrlDate + "' THEN SEM_MST.SDATE ELSE '" + _param._ctrlDate + "' END)) ");
            stb.append("         )");
            stb.append("       )");
            stb.append("  )");
            stb.append(" ORDER BY ");
            stb.append("    REGD.GRADE, ");
            stb.append("    REGD.HR_CLASS, ");
            stb.append("    REGD.ATTENDNO  ");

            //log.debug(" regd sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String gradeCd = rs.getString("GRADE_CD");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String gradeName2 = rs.getString("GRADE_NAME2");
                final String hrclass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String hrAbbv = rs.getString("HR_NAMEABBV");
                final String staffName = rs.getString("STAFFNAME");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");
                final String majorName = rs.getString("MAJORNAME");
                final String courseCodeName = rs.getString("COURSECODENAME");

                final Student student = new Student(
                        schregno,
                        grade,
                        hrclass,
                        hrName,
                        hrAbbv,
                        attendno,
                        name,
                        coursecd,
                        majorcd,
                        coursecode,
                        majorName,
                        courseCodeName
                );
                student.setSubclassInfo(db2);
                student.setSubclassRank(db2);
                student.setTotalStudyAct(db2);
                student.setMoral(db2);
                AttendSemesDat.setAttendSemesDatList(db2, _param, student);
                student.setClubCommittee(db2,schoolKind);
                final String rmKey = grade + "-" + hrclass;
                if (retMap.containsKey(rmKey)) {
                    ghCls = (GradeHrCls)retMap.get(rmKey);
                } else {
                    ghCls = new GradeHrCls(grade, schoolKind, gradeCd, gradeName, gradeName2, hrclass, hrName, hrAbbv, staffName, new LinkedMap());
                    retMap.put(rmKey, ghCls);
                }
                if (!ghCls._schregMap.containsKey(schregno)) {
                    ghCls._schregMap.put(schregno, student);
                }
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private class GradeHrCls {
        final String _grade;
        final String _schoolKind;
        final String _gradeCd;
        final String _gradeName;
        final String _gradeName2;
        final String _hrClass;
        final String _hrName;
        final String _hrAbbv;
        final String _staffName;
        final Map _schregMap;
        GradeHrCls(
                final String grade,
                final String schoolKind,
                final String gradeCd,
                final String gradeName,
                final String gradeName2,
                final String hrClass,
                final String hrName,
                final String hrAbbv,
                final String staffName,
                final Map schregMap
                ) {
            _grade = grade;
            _schoolKind = schoolKind;
            _gradeCd = gradeCd;
            _gradeName = gradeName;
            _gradeName2 = gradeName2;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _staffName = staffName;
            _schregMap = schregMap;
        }
    }
    private class TotalStudyInfo {
        final String _totalStudy_subclsName;
        final String _totalStudy_remark;
        TotalStudyInfo(final String totalStudy_subclsName, final String totalStudy_remark) {
            _totalStudy_subclsName = totalStudy_subclsName;
            _totalStudy_remark = totalStudy_remark;

        }
    }
    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrAbbv;
        final String _attendNo;
        final String _name;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _majorName;
        final String _courseCodeName;
        final Map _subclassRankMap;
        final Map _subclsInfoMap;
        final Map _attendSemesDatMap;
//        String _totalStudyTime;
//        String _specialActRemark;
        String _moral;
        private String _committee;
        private String _someClubName;
        private List _totalStudyLst;

        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrAbbv,
                final String attendNo,
                final String name,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String majorName,
                final String courseCodeName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _attendNo = attendNo;
            _name = name;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
            _subclassRankMap = new LinkedMap();
            _subclsInfoMap = new LinkedMap();
            _attendSemesDatMap = new LinkedMap();
//            _totalStudyTime = "";
//            _specialActRemark = "";
            _moral = "";
            _committee = "";
            _someClubName = "";
            _totalStudyLst = new ArrayList();
        }

        public void setSubclassInfo(final DB2UDB db2) throws SQLException {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._maxSemester : _param._semester;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String pskey = "setSubclassInfo";
            if (!_param._psBuffer.containsKey(pskey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("   T1.SCHREGNO ");
                stb.append("   ,T3.CLASSCD ");
                stb.append("   ,T3.SCHOOL_KIND ");
                stb.append("   ,T3.CURRICULUM_CD ");
                stb.append("   ,T3.SUBCLASSCD ");
                stb.append("   ,L1.CLASSNAME ");
                stb.append("   ,L1.CLASSABBV ");
                stb.append("   ,L2.SUBCLASSNAME ");
                stb.append("   ,L2.SUBCLASSABBV ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append("   LEFT JOIN CHAIR_STD_DAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("   LEFT JOIN CHAIR_DAT T3 ");
                stb.append("     ON T3.YEAR = T2.YEAR ");
                stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
                stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
                stb.append("   LEFT JOIN CLASS_MST L1 ");
                stb.append("     ON L1.CLASSCD = T3.CLASSCD ");
                stb.append("    AND L1.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("   LEFT JOIN SUBCLASS_MST L2 ");
                stb.append("     ON L2.CLASSCD = T3.CLASSCD ");
                stb.append("    AND L2.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("    AND L2.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("    AND L2.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _param._year + "' ");
                stb.append("   AND T1.SEMESTER = '" + useSemester + "' ");
                stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append("   AND T3.CLASSCD IS NOT NULL ");
                stb.append("   AND T3.CLASSCD < '90' ");
                stb.append(" ORDER BY ");
                stb.append("   T1.SCHREGNO ");
                stb.append("   ,T3.CLASSCD ");
                stb.append("   ,T3.SCHOOL_KIND ");
                stb.append("   ,T3.CURRICULUM_CD ");
                stb.append("   ,T3.SUBCLASSCD ");
                _param._psBuffer.put(pskey, stb.toString());
            }
            final String sql = (String)_param._psBuffer.get(pskey);
            Map subMap = null;
            try {
                ps = db2.prepareStatement(sql);
                ps.setString(1, _schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String school_Kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String classabbv = rs.getString("CLASSABBV");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");
                    SubclsInfo addwk = new SubclsInfo(classcd, school_Kind, curriculum_Cd, subclasscd, classname, classabbv, subclassname, subclassabbv);
                    final String mKey = classcd + "-" + school_Kind;
                    if (_subclsInfoMap.containsKey(mKey)) {
                        subMap = (Map)_subclsInfoMap.get(mKey);
                    } else {
                        subMap = new LinkedMap();
                        _subclsInfoMap.put(mKey, subMap);
                    }
                    final String subKey = classcd + "-" + school_Kind + "-" + curriculum_Cd  + "-" + subclasscd;
                    subMap.put(subKey, addwk);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void setSubclassRank(final DB2UDB db2) throws SQLException {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._maxSemester : _param._semester;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String pskey = "setSubclassRank";

            try {
                if (!_param._psBuffer.containsKey(pskey)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ");
                    stb.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV AS TESTCD, ");
                    stb.append("     REC_RANK.CLASSCD, ");
                    stb.append("     REC_RANK.SCHOOL_KIND, ");
                    stb.append("     REC_RANK.CURRICULUM_CD, ");
                    stb.append("     REC_RANK.SUBCLASSCD, ");
                    stb.append("     SUBM.SUBCLASSNAME, ");
                    stb.append("     REC_RANK.SCORE, ");
                    stb.append("     DECIMAL(ROUND(REC_RANK.AVG*10,0)/10,5,1) AS AVG, ");
                    stb.append("     REC_AVG.COUNT AS GCNT, ");
                    stb.append("     DECIMAL(ROUND(REC_AVG.AVG*10,0)/10,5,1) AS GAVG ");
                    stb.append(" FROM ");
                    stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
                    stb.append("     INNER JOIN SUBCLASS_MST SUBM ON REC_RANK.CLASSCD = SUBM.CLASSCD ");
                    stb.append("           AND REC_RANK.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
                    stb.append("           AND REC_RANK.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
                    stb.append("           AND REC_RANK.SUBCLASSCD = SUBM.SUBCLASSCD ");
                    stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ");
                    stb.append("           ON REGD.YEAR = REC_RANK.YEAR ");
                    stb.append("           AND REGD.SEMESTER = '" + useSemester + "' ");
                    stb.append("           AND REGD.SCHREGNO = REC_RANK.SCHREGNO ");
                    stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT REC_AVG ON REC_RANK.YEAR = REC_AVG.YEAR ");
                    stb.append("          AND REC_RANK.SEMESTER = REC_AVG.SEMESTER ");
                    stb.append("          AND REC_RANK.TESTKINDCD = REC_AVG.TESTKINDCD ");
                    stb.append("          AND REC_RANK.TESTITEMCD = REC_AVG.TESTITEMCD ");
                    stb.append("          AND REC_RANK.SCORE_DIV = REC_AVG.SCORE_DIV ");
                    stb.append("          AND REC_RANK.CLASSCD = REC_AVG.CLASSCD ");
                    stb.append("          AND REC_RANK.SCHOOL_KIND = REC_AVG.SCHOOL_KIND ");
                    stb.append("          AND REC_RANK.CURRICULUM_CD = REC_AVG.CURRICULUM_CD ");
                    stb.append("          AND REC_RANK.SUBCLASSCD = REC_AVG.SUBCLASSCD ");
                    stb.append("          AND REC_AVG.AVG_DIV = '3' ");  //固定 3:コース
                    stb.append("          AND REC_AVG.GRADE = '" + _param._grade + "' ");
                    stb.append("          AND REC_AVG.HR_CLASS = '000' ");
                    stb.append("          AND REC_AVG.COURSECD = REGD.COURSECD ");
                    stb.append("          AND REC_AVG.MAJORCD = REGD.MAJORCD ");
                    stb.append("          AND REC_AVG.COURSECODE = REGD.COURSECODE ");
                    stb.append(" WHERE ");
                    stb.append("     REC_RANK.YEAR = '" + _param._year + "' ");
                    stb.append("     AND REC_RANK.SEMESTER = '" + _param._semester + "' ");
                    stb.append("     AND REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD = '" + _param._testcd + "' ");  //SCORE_DIVの指定は出力時に指定。
                    stb.append("     AND REC_RANK.SCHREGNO = ? ");
                    stb.append(" UNION ");
                    stb.append(" SELECT ");
                    stb.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV AS TESTCD, ");
                    stb.append("     REC_RANK.CLASSCD, ");
                    stb.append("     REC_RANK.SCHOOL_KIND, ");
                    stb.append("     REC_RANK.CURRICULUM_CD, ");
                    stb.append("     REC_RANK.SUBCLASSCD, ");
                    stb.append("     '全科目' AS SUBCLASSNAME, ");
                    stb.append("     REC_RANK.SCORE, ");
                    stb.append("     DECIMAL(ROUND(REC_RANK.AVG*10,0)/10,5,1) AS AVG, ");
                    stb.append("     0 AS GCNT, ");
                    stb.append("     0 AS GAVG ");
                    stb.append(" FROM ");
                    stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
                    stb.append(" WHERE ");
                    stb.append("     REC_RANK.YEAR = '" + _param._year + "' ");
//                    stb.append("     AND REC_RANK.SCORE_DIV = '01' ");  //SCORE_DIVの指定は出力時に指定。
                    stb.append("     AND REC_RANK.SCHREGNO = ? ");
                    stb.append("     AND REC_RANK.SUBCLASSCD = '" + ALL9 + "' ");
                    stb.append(" ORDER BY ");
                    stb.append("     TESTCD, ");
                    stb.append("     CLASSCD, ");
                    stb.append("     SCHOOL_KIND, ");
                    stb.append("     CURRICULUM_CD, ");
                    stb.append("     SUBCLASSCD ");
                    _param._psBuffer.put(pskey, stb.toString());
                }
                final String sql = (String)_param._psBuffer.get(pskey);
                ps = db2.prepareStatement(sql);

                ps.setString(1, _schregno);
                ps.setString(2, _schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String testcd = rs.getString("TESTCD");
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculumCd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final int score = rs.getInt("SCORE");
                    final String avg = StringUtils.defaultString(rs.getString("AVG"));
                    final String gCnt = StringUtils.defaultString(rs.getString("GCNT"));
                    final String gAvg = StringUtils.defaultString(rs.getString("GAVG"));

                    final SubclassRank subclassRank = new SubclassRank(
                            testcd,
                            classcd,
                            schoolKind,
                            curriculumCd,
                            subclasscd,
                            subclassname,
                            score,
                            avg,
                            gCnt,
                            gAvg
                    );
                    final String mKey = testcd + "-" + classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
                    _subclassRankMap.put(mKey, subclassRank);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setMoral(final DB2UDB db2) throws SQLException {
            if (!SEMEALL.equals(_param._semester)) {
                _moral = "";
                return;
            }

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REMARK2 ");          //学年末のみ入力
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND SEMESTER = '" + SEMEALL + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            //log.warn("sql : "+ stb.toString());
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _moral = StringUtils.defaultString(rs.getString("REMARK2"), "");
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        private void setTotalStudyAct(final DB2UDB db2) throws SQLException {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._maxSemester : _param._semester;
            final StringBuffer stb = new StringBuffer();
            final String fixedTestCd = "990009";
            final String fixedClsCd = "90";
            stb.append(" WITH FIXED_PHRASE AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("      T1.YEAR ");
            stb.append("      ,T1.SEMESTER ");
            stb.append("      ,T1.CLASSCD ");
            stb.append("      ,T1.SCHOOL_KIND ");
            stb.append("      ,T1.CURRICULUM_CD ");
            stb.append("      ,T1.SUBCLASSCD ");
            stb.append("      ,T1.SCHREGNO ");
            stb.append("      ,T1.SCORE AS REMARK1 ");
            stb.append("      ,T3.REMARK ");
            stb.append(" FROM ");
            stb.append("      SCHREG_REGD_DAT T0 ");
            stb.append("      LEFT JOIN RECORD_SCORE_DAT T1 ");
            stb.append("        ON T1.YEAR = T0.YEAR ");
            stb.append("       AND T1.SEMESTER = T0.SEMESTER ");
            stb.append("       AND T1.SCHREGNO = T0.SCHREGNO ");
            stb.append("       AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + fixedTestCd + "' ");  //固定 ");
            stb.append("       AND T1.CLASSCD = '" + fixedClsCd + "' ");  //固定 ");
            stb.append("      LEFT JOIN HTRAINREMARK_TEMP_SEMES_COURSE_DAT T2 ");
            stb.append("        ON T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = '" + SEMEALL + "' ");  //固定 ");
            stb.append("       AND T2.GRADE = '" + _param._grade + "' ");  //学年 ");
            stb.append("       AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = T0.COURSECD || T0.MAJORCD || T0.COURSECODE ");
            stb.append("       AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
            stb.append("            = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append("      LEFT JOIN HTRAINREMARK_TEMP_SEMES_DAT T3 ");
            stb.append("        ON T3.YEAR = T2.YEAR ");
            stb.append("       AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("       AND T3.GRADE = T2.GRADE ");
            stb.append("       AND T3.DATA_DIV = T2.DATA_DIV ");
            stb.append("       AND T3.PATTERN_CD = T1.SCORE ");
            stb.append(" WHERE ");
            stb.append("     T0.YEAR = '" + _param._year + "' ");  //年度 ");
            stb.append("     AND T0.SEMESTER = '" + useSemester + "' ");  //"9"ならlastSemester ");
            stb.append("     AND T0.SCHREGNO = '" + _schregno + "' ");  //学籍NO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("    T3.SUBCLASSNAME ");
            stb.append("   ,T3.SUBCLASSABBV ");
            stb.append("   ,T2.REMARK1 ");
            stb.append("   ,T2.REMARK ");
            stb.append(" FROM ");
            stb.append("   RECORD_SCORE_DAT T1 ");
            stb.append("   LEFT JOIN FIXED_PHRASE T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN SUBCLASS_MST T3 ");
            stb.append("     ON T3.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._year + "' ");
            stb.append("   AND T1.SEMESTER = '" + useSemester + "' ");  //"9"ならlastSemester
            stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + fixedTestCd + "' ");  //固定
            stb.append("   AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("   AND T1.CLASSCD = '" + fixedClsCd + "' ");  //固定 ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            //log.warn("sql : "+ stb.toString());
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String totalStudy_subclsName = StringUtils.defaultString(rs.getString("SUBCLASSNAME"), "");
                    final String totalStudy_remark = StringUtils.defaultString(rs.getString("REMARK"), "");
                    TotalStudyInfo addwk = new TotalStudyInfo(totalStudy_subclsName, totalStudy_remark);
                    _totalStudyLst.add(addwk);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        private void setClubCommittee(final DB2UDB db2, final String schoolKind) throws SQLException {
            //委員会情報取得(指定学期まで)
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._maxSemester : _param._semester;
            final String sqlCommittee = " SELECT T1.SEMESTER, T2.COMMITTEE_FLG, T2.COMMITTEENAME FROM SCHREG_COMMITTEE_HIST_DAT T1 "
                    + " LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG AND T2.COMMITTEECD = T1.COMMITTEECD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND "
                    + " WHERE T1.YEAR = '" + _param._year + "' AND T1.SCHREGNO = ? AND T1.SEMESTER IN ('" + useSemester + "', '" + SEMEALL + "') AND T1.SCHOOL_KIND = '" + schoolKind + "' ORDER BY T1.SEMESTER, T2.COMMITTEE_FLG DESC, T2.COMMITTEECD ";
            //委員会情報取得(指定学期のみ)
            PreparedStatement ps1 = null;
            try {
                ps1 = db2.prepareStatement(sqlCommittee);
                String delim = "";
                String concatStr = "";
                for (final Iterator ite = KnjDbUtils.query(db2, ps1, new Object[] {_schregno}).iterator();ite.hasNext();) {
                    final Map row = (Map) ite.next();
                    final String committeeName = KnjDbUtils.getString(row, "COMMITTEENAME");
                    concatStr += delim + committeeName;
                    delim = "・";
                }
                _committee = concatStr;
                DbUtils.closeQuietly(ps1);
                db2.commit();

            } catch (Exception e){
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps1);
                db2.commit();
            }

            //部活情報取得(指定学期に在籍した部活のみ)
            Semester semesInf = (Semester)_param._semesterMap.get(useSemester);
            final String sqlClub = " SELECT T1.CLUBCD, T3.CLUBNAME, MAX(T1.SDATE) AS SDATE FROM SCHREG_CLUB_HIST_DAT T1 "
                    + " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.YEAR = '" + _param._year + "' AND T2.GRADE = '" + _param._grade + "' "
                    + " LEFT JOIN CLUB_MST T3 ON T3.SCHOOLCD = T1.SCHOOLCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CLUBCD = T1.CLUBCD "
                    + " WHERE T1.SCHREGNO = ? "
                    + " AND ( "
                    + "  (T1.EDATE IS NULL AND T1.SDATE <= '" + semesInf._dateRange._edate + "') or (T1.SDATE <= '" + semesInf._dateRange._sdate + "' AND '" + semesInf._dateRange._sdate + "' <= T1.EDATE)"
                    + "  or (T1.SDATE <= '" + semesInf._dateRange._edate + "' AND '" + semesInf._dateRange._edate + "' <= T1.EDATE) or ('" + semesInf._dateRange._sdate + "' <= T1.SDATE AND T1.EDATE <= '" + semesInf._dateRange._edate + "') "
                    + " ) GROUP BY T1.CLUBCD, T3.CLUBNAME ORDER BY T1.CLUBCD ";
            //部活情報取得(指定学期に在籍した部活のみ)
            PreparedStatement ps2 = null;
            try {
                ps2 = db2.prepareStatement(sqlClub);
                String concatStr = "";
                String delim = "";
                concatStr = "";
                for (final Iterator ite = KnjDbUtils.query(db2, ps2, new Object[] {_schregno}).iterator();ite.hasNext();) {
                    final Map row = (Map) ite.next();
                    final String clubName = KnjDbUtils.getString(row, "CLUBNAME");
                    if (!"".equals(StringUtils.defaultString(clubName, ""))) {
                        concatStr += delim + clubName;
                        delim = "・";
                    }
                }
                _someClubName = concatStr;
            } catch (Exception e){
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps2);
                db2.commit();
            }
        }
    }
    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _transferDate;
        int _offdays;
        int _kekkaJisu;
        int _virus;
        int _koudome;

        public AttendSemesDat(
                final String semester
        ) {
            _semester = semester;
        }

        public void add(
                final AttendSemesDat o
        ) {
            _lesson += o._lesson;
            _suspend += o._suspend;
            _mourning += o._mourning;
            _mlesson += o._mlesson;
            _sick += o._sick;
            _absent += o._absent;
            _present += o._present;
            _late += o._late;
            _early += o._early;
            _transferDate += o._transferDate;
            _offdays += o._offdays;
            _kekkaJisu += o._kekkaJisu;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final Student student) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String psKey = "attendPs";
            try {
                final String sql;
                if (!param._psBuffer.containsKey(psKey)) {
                    param._attendParamMap.put("schregno", "?");
                    sql = AttendAccumulate.getAttendSemesSql(
                            param._year,
                            param._semester,
                            null,
                            param._attSemesDate,
                            param._attendParamMap
                    );
                    param._psBuffer.put(psKey, sql);
                } else {
                    sql = (String)param._psBuffer.get(psKey);
                }
                ps = db2.prepareStatement(sql);

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    final int lesson = rs.getInt("LESSON");
                    final int suspend = rs.getInt("SUSPEND");
                    final int mourning = rs.getInt("MOURNING");
                    final int mlesson = rs.getInt("MLESSON");
                    final int sick = rs.getInt("SICK");
                    final int absent = rs.getInt("ABSENT");
                    final int present = rs.getInt("PRESENT");
                    final int late = rs.getInt("LATE");
                    final int early = rs.getInt("EARLY");
                    final int transferDate = rs.getInt("TRANSFER_DATE");
                    final int offdays = rs.getInt("OFFDAYS");
                    final int kekkaJisu = rs.getInt("KEKKA_JISU");
                    final int virus = rs.getInt("VIRUS");
                    final int koudome = rs.getInt("KOUDOME");

                    final AttendSemesDat attendSemesDat = new AttendSemesDat(semester);
                    attendSemesDat._lesson = lesson;
                    attendSemesDat._suspend = suspend;
                    attendSemesDat._mourning = mourning;
                    attendSemesDat._mlesson = mlesson;
                    attendSemesDat._sick = sick;
                    attendSemesDat._absent = absent;
                    attendSemesDat._present = present;
                    attendSemesDat._late = late;
                    attendSemesDat._early = early;
                    attendSemesDat._transferDate = transferDate;
                    attendSemesDat._offdays = offdays;
                    attendSemesDat._kekkaJisu = kekkaJisu;
                    attendSemesDat._virus = virus;
                    attendSemesDat._koudome = koudome;

                    student._attendSemesDatMap.put(semester, attendSemesDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private class SubclassRank {
        final String _testcd;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final int _score;
        final String _avg;
        final String _gCnt;
        final String _gAvg;

        public SubclassRank(
                final String testcd,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final int score,
                final String avg,
                final String gCnt,
                final String gAvg
        ) {
            _testcd = testcd;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score = score;
            _avg = avg;
            _gCnt = gCnt;
            _gAvg = gAvg;
        }

        public String getKey() {
            return _classcd + _schoolKind + _curriculumCd + _subclasscd;
        }
    }

    private class SubclassBunpu {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final int _score0;
        final int _score1;
        final int _score2;
        final int _score3;
        final int _score4;
        final int _score5;
        final int _score6;
        final int _score7;
        final int _score8;
        final int _score9;

        public SubclassBunpu(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final int score0,
                final int score1,
                final int score2,
                final int score3,
                final int score4,
                final int score5,
                final int score6,
                final int score7,
                final int score8,
                final int score9
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score0 = score0;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score4 = score4;
            _score5 = score5;
            _score6 = score6;
            _score7 = score7;
            _score8 = score8;
            _score9 = score9;
        }
        private int getTotal() {
            return _score0 + _score1 + _score2 + _score3 + _score4 + _score5 + _score6 + _score7 + _score8 + _score9;
        }
    }

    private class SubclsInfo {
        final String _classCd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclassCd;
        final String _className;
        final String _classAbbv;
        final String _subclassName;
        final String _subclassAbbv;
        public SubclsInfo (final String classCd, final String school_Kind, final String curriculum_Cd, final String subclassCd, final String className, final String classAbbv, final String subclassName, final String subclassAbbv)
        {
            _classCd = classCd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclassCd = subclassCd;
            _className = className;
            _classAbbv = classAbbv;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
        }
    }

    private class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _categoryIsClass;
        final String _semester;
        final String _semesterName;
        final String _testcd;
        final String _schoolKind;
        final String _grade;
        final String _hrClass;
        final String[] _categorySelected;
        final String _year;
        final String _ctrlSeme;
        final String _attSemesDate;
        final String _printDate;
        final String _maxSemester;
        final String _ctrlDate;

        final Map _attendParamMap;
        //final String _schoolKind;
        //final String _schoolCd;
        //final String _usePrgSchoolKind;
        //final String _selSchoolKind;
        //final String _useSchoolKindField;
        //final String _usecurriculumcd;

        final Map _subclassBunpuMap;
//        final List _d017Name1List;
        private Map _semesterMap;

        final Map _certifInfo;
        final Map _psBuffer;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categoryIsClass = request.getParameter("DISP");
            _semester = request.getParameter("SEMESTER");
            _testcd = "9900";  //学期末のみが対象
            _hrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_categoryIsClass)) {
                _grade = request.getParameter("GRADE");
            } else {
                _grade = StringUtils.substring(_hrClass, 0, 2);
            }
            String[] csCutWk = request.getParameterValues("CATEGORY_SELECTED");
            if (!"1".equals(_categoryIsClass)) {
                for (int cnt=0;cnt < csCutWk.length;cnt++) {
                    final String[] schregno_split = StringUtils.split(csCutWk[cnt], '-');
                    csCutWk[cnt] = schregno_split[0];
                }
            }
            _categorySelected = csCutWk;
            _year = request.getParameter("CTRL_YEAR");
            _ctrlSeme = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _maxSemester = request.getParameter("MAX_SEMESTER");
            _attSemesDate = StringUtils.replace(request.getParameter("ATTSEMES_DATE"), "/", "-");
            _printDate = StringUtils.replace(request.getParameter("PRINT_DATE"), "/", "-");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _psBuffer = new HashMap();

            _schoolKind = getSchoolKind(db2);
            //_usePrgSchoolKind = request.getParameter("use_prg_schoolkind");
            //_selSchoolKind = request.getParameter("selectSchoolKind");
            //_useSchoolKindField = request.getParameter("useSchool_KindField");
            //_usecurriculumcd = request.getParameter("useCurriculumcd");
            //_schoolKind = request.getParameter("SCHOOLKIND");
            //_schoolCd = request.getParameter("SCHOOLCD");

            _semesterName = getSemesterName(db2);
            _subclassBunpuMap = getSubclassBunpu(db2);
//            _d017Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year +"' AND NAMECD1 = 'D017' "), "NAME1");
            _certifInfo = getCertifInfo(db2);
            _semesterMap = loadSemester(db2, _year, _grade);
        }

        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + year + "'"
                        + "   AND GRADE='" + grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("loadSemester exception!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }
        private String getSemesterName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
        }
        private String getSchoolKind(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
        }
        private Map getCertifInfo(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String getCKCode = "H".equals(_schoolKind) ? "104" : "103";
            try {
                ps = db2.prepareStatement(" SELECT * from CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + getCKCode + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {  //1レコードのはずなので、先頭データだけ取得
                    final String kStr1 = "SCHOOL_NAME";
                    retMap.put(kStr1, rs.getString(kStr1));
                    final String kStr2 = "JOB_NAME";
                    retMap.put(kStr2, rs.getString(kStr2));
                    final String kStr3 = "PRINCIPAL_NAME";
                    retMap.put(kStr3, rs.getString(kStr3));
                }
            } catch (SQLException ex) {
                log.debug("getCertifInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getSubclassBunpu(final DB2UDB db2) {
            final String useSemester = SEMEALL.equals(_semester) ? _maxSemester : _semester;
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     REC_RANK.CLASSCD, ");
                stb.append("     REC_RANK.SCHOOL_KIND, ");
                stb.append("     REC_RANK.CURRICULUM_CD, ");
                stb.append("     REC_RANK.SUBCLASSCD, ");
                stb.append("     REGD.COURSECD, ");
                stb.append("     REGD.MAJORCD, ");
                stb.append("     REGD.COURSECODE, ");
                stb.append("     MAX(SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 0 AND 9 THEN 1 ELSE 0 END) AS SCORE0, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 10 AND 19 THEN 1 ELSE 0 END) AS SCORE1, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 20 AND 29 THEN 1 ELSE 0 END) AS SCORE2, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 30 AND 39 THEN 1 ELSE 0 END) AS SCORE3, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 40 AND 49 THEN 1 ELSE 0 END) AS SCORE4, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 50 AND 59 THEN 1 ELSE 0 END) AS SCORE5, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 60 AND 69 THEN 1 ELSE 0 END) AS SCORE6, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 70 AND 79 THEN 1 ELSE 0 END) AS SCORE7, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 80 AND 89 THEN 1 ELSE 0 END) AS SCORE8, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 90 AND 100 THEN 1 ELSE 0 END) AS SCORE9 ");
                stb.append(" FROM ");
                stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ");
                stb.append("       ON REGD.YEAR = REC_RANK.YEAR ");
                stb.append("      AND REGD.SEMESTER = '" + useSemester + "' ");
                stb.append("      AND REGD.SCHREGNO = REC_RANK.SCHREGNO ");
                stb.append("     INNER JOIN SUBCLASS_MST SUBM ON REC_RANK.CLASSCD = SUBM.CLASSCD ");
                stb.append("           AND REC_RANK.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
                stb.append("           AND REC_RANK.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
                stb.append("           AND REC_RANK.SUBCLASSCD = SUBM.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("     REC_RANK.YEAR = '" + _year + "' ");
                stb.append("     AND REC_RANK.SEMESTER = '" + _semester + "' ");
                stb.append("     AND REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV = '" + _testcd + "08" + "' ");
                stb.append("     AND EXISTS ( ");
                stb.append("         SELECT ");
                stb.append("             'x' ");
                stb.append("         FROM ");
                stb.append("             SCHREG_REGD_DAT REGD ");
                stb.append("         WHERE ");
                stb.append("             REC_RANK.YEAR = REGD.YEAR ");
                stb.append("             AND REGD.SEMESTER = '" + useSemester + "' ");
                stb.append("             AND REGD.GRADE = '" + _grade + "' ");
                stb.append("             AND REC_RANK.SCHREGNO = REGD.SCHREGNO ");
                stb.append("     ) ");
                stb.append(" GROUP BY ");
                stb.append("     REC_RANK.CLASSCD, ");
                stb.append("     REC_RANK.SCHOOL_KIND, ");
                stb.append("     REC_RANK.CURRICULUM_CD, ");
                stb.append("     REC_RANK.SUBCLASSCD, ");
                stb.append("     REGD.COURSECD, ");
                stb.append("     REGD.MAJORCD, ");
                stb.append("     REGD.COURSECODE ");


                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculumCd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String coursecd = StringUtils.defaultString(rs.getString("COURSECD"));
                    final String majorcd = StringUtils.defaultString(rs.getString("MAJORCD"));
                    final String coursecode = StringUtils.defaultString(rs.getString("COURSECODE"));
                    final int score0 = rs.getInt("SCORE0");
                    final int score1 = rs.getInt("SCORE1");
                    final int score2 = rs.getInt("SCORE2");
                    final int score3 = rs.getInt("SCORE3");
                    final int score4 = rs.getInt("SCORE4");
                    final int score5 = rs.getInt("SCORE5");
                    final int score6 = rs.getInt("SCORE6");
                    final int score7 = rs.getInt("SCORE7");
                    final int score8 = rs.getInt("SCORE8");
                    final int score9 = rs.getInt("SCORE9");

                    final SubclassBunpu subclassBunpu = new SubclassBunpu(classcd, schoolKind, curriculumCd, subclasscd, subclassname, score0, score1, score2, score3, score4, score5, score6, score7, score8, score9);
                    final String setSubclsCrsCd = classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd + "-" + coursecd + "-" + majorcd + "-" + coursecode;
                    retMap.put(setSubclsCrsCd, subclassBunpu);
                }
            } catch (SQLException ex) {
                log.debug("getSubclassBunpu exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

    }
}

// eof
