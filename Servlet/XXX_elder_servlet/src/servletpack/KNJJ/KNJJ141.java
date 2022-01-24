/*
 * $Id: 81024d753defdcfe5b2d14a4e43562d301c3b65c $
 *
 * 作成日: 2016/08/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;


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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJJ141 {

    private static final Log log = LogFactory.getLog(KNJJ141.class);

    private boolean _hasData;

    private static String GRADEALL = "ALL";
    private static String TOTALCLUBCD = "ZZZZ";

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
        for (Iterator iterator21 = _param._clubMstList.iterator(); iterator21.hasNext();) {
            svf.VrSetForm("KNJJ141.frm", 4);
            svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　" + ("2".equals(_param._patternType) ? "学年別・男女別集計表" : "部活動人数一覧表"));
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._date));
            svf.VrsOut("CLUB_TITLE", "部活動名");

            List pageList = (List) iterator21.next();
            for (Iterator<Map<String, RegdHdat>> iterator = _param._regdList.iterator(); iterator.hasNext();) {
                Map<String, RegdHdat> pageMap = iterator.next();

                for (Iterator<String> iterator2 = pageMap.keySet().iterator(); iterator2.hasNext();) {
                    final String regdKey = iterator2.next();
                    RegdHdat regdHdat = pageMap.get(regdKey);
                    final String hrField = "999".equals(regdHdat._hrClass) ? "2" : "1";
                    final String cntField = "999".equals(regdHdat._hrClass) ? "TOTAL_" : "";
                    if ("2".equals(_param._patternType) && !"999".equals(regdHdat._hrClass)) {
                    	continue;
                    }
                    boolean contflg = false;
                    int setFieldCnt = 1;
                    int totalManCnt = 0;
                    int totalWomanCnt = 0;
                    int totalSumCnt = 0;
                    for (Iterator iterator211 = pageList.iterator(); iterator211.hasNext();) {
                        ClubMst committee = (ClubMst) iterator211.next();
                        final String setCommitteeTitleField = getMS932ByteLength(committee._name) > 10 ? "2" : "1";
                        final PrintData printDataMan = (PrintData) _param._clubData.get(regdHdat._grade + regdHdat._hrClass + "1" + committee._schoolcd + committee._schoolKind + committee._cd);
                        final PrintData printDataWoMan = (PrintData) _param._clubData.get(regdHdat._grade + regdHdat._hrClass + "2" + committee._schoolcd + committee._schoolKind + committee._cd);
                        final PrintData printDataTotal = (PrintData) _param._clubData.get(regdHdat._grade + regdHdat._hrClass + "3" + committee._schoolcd + committee._schoolKind + committee._cd);
                        if (!committee._schoolKind.equals(regdHdat._schoolkind)) {
                            contflg = true;   //クラブについては、pageList作成時に校種毎にページ分けしているので、処理ページに対してクラスの校種が合わない(出力しない)場合は、全列出力しない。
                            continue;
                        }
                        svf.VrsOutn("CLUB_NAME" + setCommitteeTitleField, setFieldCnt, committee._name);
                        if (null != printDataMan) {
                            svf.VrsOutn(cntField + "MALE", setFieldCnt, printDataMan._cnt);
                            totalManCnt += Integer.parseInt(printDataMan._cnt);
                        } else if (TOTALCLUBCD.equals(committee._cd)) {
                            svf.VrsOutn(cntField + "MALE", setFieldCnt, String.valueOf(totalManCnt));
                        } else {
                            svf.VrsOutn(cntField + "MALE", setFieldCnt, "0");
                        }
                        if (null != printDataWoMan) {
                            svf.VrsOutn(cntField + "FEMALE", setFieldCnt, printDataWoMan._cnt);
                            totalWomanCnt += Integer.parseInt(printDataWoMan._cnt);
                        } else if (TOTALCLUBCD.equals(committee._cd)) {
                            svf.VrsOutn(cntField + "FEMALE", setFieldCnt, String.valueOf(totalWomanCnt));
                        } else {
                            svf.VrsOutn(cntField + "FEMALE", setFieldCnt, "0");
                        }
                        if ("999".equals(regdHdat._hrClass)) {
                            if (printDataTotal != null) {
                                svf.VrsOutn("TOTAL", setFieldCnt, printDataTotal._cnt);
                                totalSumCnt += Integer.parseInt(printDataTotal._cnt);
                            } else if (TOTALCLUBCD.equals(committee._cd)) {
                                svf.VrsOutn("TOTAL", setFieldCnt, String.valueOf(totalSumCnt));
                            } else {
                                svf.VrsOutn("TOTAL", setFieldCnt, "0");
                            }
                        }
                        setFieldCnt++;
                    }
                    if (!contflg) {
                        //校種が一致した場合のみ出力。
                        svf.VrsOut("HR_NAME" + hrField, regdHdat._hrName);
                    }
                    svf.VrEndRecord();
                    _hasData = true;

                }
            }
        }
    }

    private static int getMS932ByteLength(final String s) {
        return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private static <T> List<List<T>> getPageList(final List<T> list, final int maxLine) {
        final List<List<T>> rtn = new ArrayList<List<T>>();
        List<T> current = null;
        for (final T t : list) {
            if (null == current || current.size() >= maxLine) {
                current = new ArrayList<T>();
                rtn.add(current);
            }
            current.add(t);
        }
        return rtn;
    }

    private static <K, T, U> Map<T, U> getMappedMap(final Map<K, Map<T, U>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<T, U>());
        }
        return map.get(key1);
    }

    private static <T, K> List<T> getMappedList(final Map<K, List<T>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<T>());
        }
        return map.get(key1);
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76200 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _sdate;
        private final String _year;
        private final String _semester;
        private final String _date;
        private final List _clubMstList;
        private final List<Map<String, RegdHdat>> _regdList;
        private final Map _clubData;
        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOLKIND;
        private String use_prg_schoolkind;
        private String selectSchoolKind;
        private String selectSchoolKindSql;
        private String useClubMultiSchoolKind;
        private String _patternType;
        private String _grade;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _sdate = request.getParameter("BASE_DATE").replace('/', '-');
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("CTRL_DATE");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _patternType = request.getParameter("PRTPATTERN");
            _grade = request.getParameter("GRADE");
            use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            selectSchoolKind = request.getParameter("selectSchoolKind");
            useClubMultiSchoolKind = request.getParameter("useClubMultiSchoolKind");
            if (!StringUtils.isBlank(selectSchoolKind)) {
                final StringBuffer sql = new StringBuffer("('");
                final String[] split = StringUtils.split(selectSchoolKind, ":");
                for (int i = 0; i < split.length; i++) {
                    sql.append(split[i]);
                    if (i < split.length - 1) {
                        sql.append("','");
                    }
                }
                selectSchoolKindSql = sql.append("')").toString();
            }
            _clubMstList = getClubMstList(db2);
            _regdList = getRegdList(db2);
            _clubData = getPrintData(db2);
        }

        private List getClubMstList(final DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            final String committeeSql = getClubSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(committeeSql);
                rs = ps.executeQuery();
                final int maxCnt = 50;
                int cnt = 1;
                List pageList = new ArrayList();
                String schcdbk = "";
                String schkindcdbk = "";
                while (rs.next()) {
                    String schoolcd = "";
                    String schoolKind = "";
                    if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                        schoolcd = rs.getString("SCHOOLCD");
                        schoolKind = rs.getString("SCHOOL_KIND");
                    }
                    final String cd = rs.getString("CLUBCD");
                    final String name = rs.getString("CLUBNAME");

                    final ClubMst committee = new ClubMst(schoolcd, schoolKind, cd, name);
                    if (cnt > maxCnt) {
                        retList.add(pageList);
                        pageList = new ArrayList();
                        cnt = 1;
                        if (!"".equals(schkindcdbk) && !schkindcdbk.equals(schoolKind)) {
                        	setTotalClubCd(pageList, schcdbk, schkindcdbk);
                        	cnt++;
                        }
                    } else {
                        if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                            if (!"1".equals(useClubMultiSchoolKind) && !"".equals(schkindcdbk) && !schkindcdbk.equals(schoolKind)) {
                            	setTotalClubCd(pageList, schcdbk, schkindcdbk);
                            	cnt++;
                                retList.add(pageList);
                                pageList = new ArrayList();
                                cnt = 1;
                            }
                        }
                    }
                    if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                        schkindcdbk = schoolKind;
                    }
                    schcdbk = schoolcd;
                    pageList.add(committee);

                    cnt++;
                }
                if (cnt > 1) {
                    retList.add(pageList);
                }
                if (retList.size() > 0) {
                	if (cnt > maxCnt) {
                        pageList = new ArrayList();
                        retList.add(pageList);
                        cnt = 1;
                	}
                	setTotalClubCd(pageList, schcdbk, schkindcdbk);
                	cnt++;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

        private void setTotalClubCd(final List pageList, final String schCdBk, final String schKindCdBk) {
            final String cdWk = TOTALCLUBCD;
            final String nameWk = "計";
            final ClubMst committeeWk = new ClubMst(schCdBk, schKindCdBk, cdWk, nameWk);
            pageList.add(committeeWk);
        }

        private String getClubSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     T1.SCHOOLCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
            }
            stb.append("     T1.CLUBCD, ");
            stb.append("     T2.CLUBNAME ");
            stb.append(" FROM ");
            stb.append("     CLUB_YDAT T1, ");
            stb.append("     CLUB_MST T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.CLUBCD   = T2.CLUBCD ");
            if ("1".equals(useClubMultiSchoolKind)) {
                stb.append("   AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND T1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
                stb.append("   AND T2.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND T2.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            } else if ("1".equals(use_prg_schoolkind)) {
                stb.append("   AND T2.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append("   AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                    stb.append("   AND T1.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                stb.append("   AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND T1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
                stb.append("   AND T2.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND T2.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append(" ORDER BY ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     T1.SCHOOLCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
            }
            stb.append("     T1.CLUBCD ");
            return stb.toString();
        }

        private List<Map<String, RegdHdat>> getRegdList(final DB2UDB db2) throws SQLException {
            final List<Map<String, RegdHdat>> retList = new ArrayList();
            final String committeeSql = getRegdHdat();
        	final List<String> schoolKindList = new ArrayList<String>();
        	final Map<String, Map<String, List<RegdHdat>>> schoolKindGradeHdatListMap = new HashMap<String, Map<String, List<RegdHdat>>>();

        	final List<Map<String, String>> rowList = KnjDbUtils.query(db2, committeeSql);
        	for (final Map<String, String> row : rowList) {
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String hrName = KnjDbUtils.getString(row, "HR_NAME");
                final String schoolkind;
                if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                    schoolkind = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_KIND"));
                } else {
                    schoolkind = "";
                }
                final RegdHdat hdat = new RegdHdat(grade, hrClass, hrName, schoolkind);
                if (!schoolKindList.contains(schoolkind)) {
                	schoolKindList.add(schoolkind);
                }
                getMappedList(getMappedMap(schoolKindGradeHdatListMap, schoolkind), grade).add(hdat);
        	}
            final int maxCnt = 50;
        	int cnt = 0;
        	Map<String, RegdHdat> pageMap = null;
        	for (int schoolkindi = 0; schoolkindi < schoolKindList.size(); schoolkindi++) {
        		final String schoolkind = schoolKindList.get(schoolkindi);
        		final String skis = String.valueOf(schoolkindi);
        		final Map<String, List<RegdHdat>> gradeHdatListMap = schoolKindGradeHdatListMap.get(schoolkind);
        		for (final String grade : gradeHdatListMap.keySet()) {

        			if (!"2".equals(_patternType)) {
            			for (final RegdHdat hdat : gradeHdatListMap.get(grade)) {
            				final int h = 2;
            				if (null == pageMap || cnt + h > maxCnt) {
            					pageMap = new TreeMap<String, RegdHdat>();
            					retList.add(pageMap);
            					cnt = 0;
        				    }
        				    pageMap.put(skis + hdat._grade + hdat._hrClass, hdat);
        				    cnt += h;
        			    }
        			}

        			final int h = 3;
    				if (null == pageMap || cnt + h > maxCnt) {
    					pageMap = new TreeMap<String, RegdHdat>();
    					retList.add(pageMap);
    					cnt = 0;
    				}
                    final RegdHdat hdatAll = new RegdHdat(grade, "999", grade + "学年合計", schoolkind);
                    pageMap.put(skis + grade + "999", hdatAll);
                    cnt += h;
        		}

        		final int h = 3;
				if (null == pageMap || cnt + h > maxCnt) {
					pageMap = new TreeMap<String, RegdHdat>();
					retList.add(pageMap);
					cnt = 0;
				}
				if (GRADEALL.equals(_grade)) {
                    final RegdHdat totakAll = new RegdHdat("99", "999", "全学年合計", schoolkind);
                    pageMap.put(skis + "99" + "999", totakAll);
                    cnt += h;
				}
        	}
            return retList;
        }

        private String getRegdHdat() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGDH.* ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     ,GDAT.SCHOOL_KIND ");
            }
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT REGDH ");
            if ("1".equals(useClubMultiSchoolKind)) {
                //全生徒が対象
                stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGDH.YEAR AND GDAT.GRADE = REGDH.GRADE ");
            } else if ("1".equals(use_prg_schoolkind)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGDH.YEAR AND GDAT.GRADE = REGDH.GRADE ");
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append("   AND GDAT.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField)) {
                stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGDH.YEAR AND GDAT.GRADE = REGDH.GRADE ");
                if (!StringUtils.isBlank(_SCHOOLKIND)) {
                    stb.append("       AND GDAT.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
                }
            }
            stb.append(" WHERE ");
            stb.append("     REGDH.YEAR = '" + _year + "' ");
            stb.append("     AND REGDH.SEMESTER = '" + _semester + "' ");
            if (!GRADEALL.equals(_grade)) {
            	stb.append(" AND REGDH.GRADE = '" + _grade + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     REGDH.GRADE, ");
            stb.append("     REGDH.HR_CLASS ");
            return stb.toString();
        }

        private Map getPrintData(final DB2UDB db2) throws SQLException {
            final Map retMap = new HashMap();
            final String clubDataSql = getClubDataSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(clubDataSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String sex = rs.getString("SEX");
                    final String cCd = rs.getString("CLUBCD");
                    final String cnt = rs.getString("CNT");
                    String schoolcd = "";
                    String schoolKind = "";
                    if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                        schoolcd = rs.getString("SCHOOLCD");
                        schoolKind = rs.getString("SCHOOL_KIND");
                    }

                    final PrintData printData = new PrintData(grade, hrClass, hrName, sex, cCd, cnt);
                    retMap.put(grade + hrClass + sex + schoolcd + schoolKind + cCd, printData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retMap;
        }

        private String getClubDataSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_H AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGDH.*, ");
            stb.append("     REGD_G.SCHOOL_KIND, ");
            stb.append("     N1.NAMECD2 AS SEX, ");
            stb.append("     N1.NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT REGDH ");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT REGD_G ");
            stb.append("   ON REGD_G.YEAR = REGDH.YEAR ");
            stb.append("  AND REGD_G.GRADE = REGDH.GRADE ");

            if ("1".equals(useClubMultiSchoolKind)) {
                //全生徒が対象
            } else if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append(" INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGDH.YEAR AND REGDG.GRADE = REGDH.GRADE ");
                    stb.append("   AND REGDG.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGDH.YEAR AND REGDG.GRADE = REGDH.GRADE ");
                stb.append("       AND REGDG.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' ");
            stb.append(" WHERE ");
            stb.append("     REGDH.YEAR = '" + _year + "' ");
            stb.append("     AND REGDH.SEMESTER = '" + _semester + "' ");
            if (!GRADEALL.equals(_grade)) {
            	stb.append(" AND REGDH.GRADE = '" + _grade + "' ");
            }
            stb.append(" ), REGD_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.*, ");
            stb.append("     BASE.SEX ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     , ");
            stb.append("     (SELECT ");
            stb.append("          T2.SCHREGNO, ");
            stb.append("          MAX(T2.SEMESTER) AS SEMESTER ");
            stb.append("      FROM ");
            stb.append("          SCHREG_REGD_DAT T2 ");
            stb.append("      WHERE ");
            stb.append("          T2.YEAR = '" + _year + "' ");
            stb.append("      GROUP BY ");
            stb.append("          T2.SCHREGNO ");
            stb.append("     ) MAXSEME ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _year + "' ");
            stb.append("     AND REGD.SCHREGNO = MAXSEME.SCHREGNO ");
            stb.append("     AND REGD.SEMESTER = MAXSEME.SEMESTER ");
            if (!GRADEALL.equals(_grade)) {
            	stb.append(" AND REGD.GRADE = '" + _grade + "' ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     REGD_H.GRADE, ");
            stb.append("     REGD_H.HR_CLASS, ");
            stb.append("     REGD_H.HR_NAME, ");
            stb.append("     REGD_H.SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     CLUB.SCHOOLCD, ");
                stb.append("     REGD_H.SCHOOL_KIND, ");
            }
            stb.append("     CLUB.CLUBCD, ");
            stb.append("     SUM(CASE WHEN CLUB.CLUBCD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ");
            stb.append(" FROM ");
            stb.append("     REGD_H ");
            stb.append("     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ");
            stb.append("          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ");
            stb.append("          AND REGD_H.SEX = REGD_T.SEX ");
            stb.append("     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON '" + _sdate + "' BETWEEN CLUB.SDATE AND VALUE(CLUB.EDATE, '9999-12-31') ");
            stb.append("          AND REGD_T.SCHREGNO = CLUB.SCHREGNO ");
            if ("1".equals(useClubMultiSchoolKind)) {
                stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND CLUB.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            } else if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                    stb.append("   AND CLUB.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND CLUB.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     REGD_H.GRADE, ");
            stb.append("     REGD_H.HR_CLASS, ");
            stb.append("     REGD_H.HR_NAME, ");
            stb.append("     REGD_H.SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     CLUB.SCHOOLCD, ");
                stb.append("     REGD_H.SCHOOL_KIND, ");
            }
            stb.append("     CLUB.CLUBCD ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     REGD_H.GRADE, ");
            stb.append("     '999' AS HR_CLASS, ");
            stb.append("     '学年' AS HR_NAME, ");
            stb.append("     REGD_H.SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     CLUB.SCHOOLCD, ");
                stb.append("     REGD_H.SCHOOL_KIND, ");
            }
            stb.append("     CLUB.CLUBCD, ");
            stb.append("     SUM(CASE WHEN CLUB.CLUBCD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ");
            stb.append(" FROM ");
            stb.append("     REGD_H ");
            stb.append("     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ");
            stb.append("          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ");
            stb.append("          AND REGD_H.SEX = REGD_T.SEX ");
            stb.append("     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON '" + _sdate + "' BETWEEN CLUB.SDATE AND VALUE(CLUB.EDATE, '9999-12-31') ");
            stb.append("          AND REGD_T.SCHREGNO = CLUB.SCHREGNO ");
            if ("1".equals(useClubMultiSchoolKind)) {
                stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND CLUB.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            } else if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                    stb.append("   AND CLUB.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND CLUB.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     REGD_H.GRADE, ");
            stb.append("     REGD_H.SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     CLUB.SCHOOLCD, ");
                stb.append("     REGD_H.SCHOOL_KIND, ");
            }
            stb.append("     CLUB.CLUBCD ");

            stb.append(" UNION ");  //学年別合計
            stb.append(" SELECT ");
            stb.append("     REGD_H.GRADE, ");
            stb.append("     '999' AS HR_CLASS, ");
            stb.append("     '学年' AS HR_NAME, ");
            stb.append("     '3' AS SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     CLUB.SCHOOLCD, ");
                stb.append("     REGD_H.SCHOOL_KIND, ");
            }
            stb.append("     CLUB.CLUBCD, ");
            stb.append("     SUM(CASE WHEN CLUB.CLUBCD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ");
            stb.append(" FROM ");
            stb.append("     REGD_H ");
            stb.append("     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ");
            stb.append("          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ");
            stb.append("          AND REGD_H.SEX = REGD_T.SEX ");
            stb.append("     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON '" + _sdate + "' BETWEEN CLUB.SDATE AND VALUE(CLUB.EDATE, '9999-12-31') ");
            stb.append("          AND REGD_T.SCHREGNO = CLUB.SCHREGNO ");
            if ("1".equals(useClubMultiSchoolKind)) {
                stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND CLUB.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            } else if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                    stb.append("   AND CLUB.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND CLUB.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     REGD_H.GRADE, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     CLUB.SCHOOLCD, ");
                stb.append("     REGD_H.SCHOOL_KIND, ");
            }
            stb.append("     CLUB.CLUBCD ");
            stb.append(" UNION ");  //(校種別)全学年男女別合計
            stb.append(" SELECT ");
            stb.append("     '99' AS GRADE, ");
            stb.append("     '999' AS HR_CLASS, ");
            stb.append("     '全学年' AS HR_NAME, ");
            stb.append("     REGD_H.SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     CLUB.SCHOOLCD, ");
                stb.append("     REGD_H.SCHOOL_KIND, ");
            }
            stb.append("     CLUB.CLUBCD, ");
            stb.append("     SUM(CASE WHEN CLUB.CLUBCD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ");
            stb.append(" FROM ");
            stb.append("     REGD_H ");
            stb.append("     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ");
            stb.append("          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ");
            stb.append("          AND REGD_H.SEX = REGD_T.SEX ");
            stb.append("     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON '" + _sdate + "' BETWEEN CLUB.SDATE AND VALUE(CLUB.EDATE, '9999-12-31') ");
            stb.append("          AND REGD_T.SCHREGNO = CLUB.SCHREGNO ");
            if ("1".equals(useClubMultiSchoolKind)) {
                stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND CLUB.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            } else if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                    stb.append("   AND CLUB.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND CLUB.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     REGD_H.SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     CLUB.SCHOOLCD, ");
                stb.append("     REGD_H.SCHOOL_KIND, ");
            }
            stb.append("     CLUB.CLUBCD ");
            stb.append(" UNION ");  //(校種別)全学年合計
            stb.append(" SELECT ");
            stb.append("     '99' AS GRADE, ");
            stb.append("     '999' AS HR_CLASS, ");
            stb.append("     '全学年' AS HR_NAME, ");
            stb.append("     '3' AS SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     CLUB.SCHOOLCD, ");
                stb.append("     REGD_H.SCHOOL_KIND, ");
            }
            stb.append("     CLUB.CLUBCD, ");
            stb.append("     SUM(CASE WHEN CLUB.CLUBCD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ");
            stb.append(" FROM ");
            stb.append("     REGD_H ");
            stb.append("     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ");
            stb.append("          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ");
            stb.append("          AND REGD_H.SEX = REGD_T.SEX ");
            stb.append("     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON '" + _sdate + "' BETWEEN CLUB.SDATE AND VALUE(CLUB.EDATE, '9999-12-31') ");
            stb.append("          AND REGD_T.SCHREGNO = CLUB.SCHREGNO ");
            if ("1".equals(useClubMultiSchoolKind)) {
                stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND CLUB.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            } else if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                    stb.append("   AND CLUB.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                stb.append("   AND CLUB.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND CLUB.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append(" GROUP BY ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     CLUB.SCHOOLCD, ");
                stb.append("     REGD_H.SCHOOL_KIND, ");
            }
            stb.append("     CLUB.CLUBCD ");

            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     SCHOOLCD, ");
                stb.append("     SCHOOL_KIND, ");
            }
            stb.append("     CLUBCD ");

            return stb.toString();
        }

    }

    /** 部活動 */
    private class ClubMst {
        private final String _schoolcd;
        private final String _schoolKind;
        private final String _cd;
        private final String _name;

        public ClubMst(
                final String schoolcd,
                final String schoolKind,
                final String cd,
                final String name
                ) {
            _schoolcd = schoolcd;
            _schoolKind = schoolKind;
            _cd = cd;
            _name = name;
        }
    }

    /** REGDH */
    private class RegdHdat {
    	private final String _schoolkind;
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final Map _manMap;
        private final Map _womanMap;

        public RegdHdat(
                final String grade,
                final String hrClass,
                final String hrName,
                final String schoolkind
                ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _manMap = new HashMap();
            _womanMap = new HashMap();
            _schoolkind = schoolkind;
        }
    }

    /** 印字データ */
    private class PrintData {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _sex;
        private final String _cCd;
        private final String _cnt;

        public PrintData(
                final String grade,
                final String hrClass,
                final String hrName,
                final String sex,
                final String cCd,
                final String cnt
                ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _sex = sex;
            _cCd = cCd;
            _cnt = cnt;
        }
    }
}

// eof
