/*
 * $Id: 78809efae79c53411924c03a100423ca49984e40 $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJC156 {

    private static final Log log = LogFactory.getLog(KNJC156.class);

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
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

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
            svf.VrQuit();

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }

    }
    
    private static String add(final String num1, final String num2) {
    	if (!NumberUtils.isNumber(num1)) return num2;
    	if (!NumberUtils.isNumber(num2)) return num1;
    	return String.valueOf((int) Double.parseDouble(num1) + Double.parseDouble(num2));
    }

    private static String intString(final String num1) {
    	if (!NumberUtils.isNumber(num1)) {
    		return num1;
    	}
    	return String.valueOf((int) Double.parseDouble(num1));
    }

	private Collection formatDateList(Collection executeDateSet) {
		final List rtn = new ArrayList();
		for (final Iterator it = executeDateSet.iterator(); it.hasNext();) {
			final String executeDate = (String) it.next();
			rtn.add(KNJ_EditDate.h_format_JP_MD(executeDate));
		}
		return rtn;
	}
	
	private List getPageList(final List list, final int maxLine) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
        	final Object o = it.next();
            if (null == current || current.size() >= maxLine) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

	private void printMain(final DB2UDB db2, final Vrw32alp svf) {
		final int maxCol = 15;
        final List printList = SchregData.getSchList(db2, _param);
        for (final Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final SchregData schData = (SchregData) iterator.next();

            schData.load(db2, _param);

            final List pageExecuteDateList = getPageList(schData._executeDateList, maxCol);
            final int maxPage = Math.max(1, pageExecuteDateList.size());
            
			for (int pi = 0; pi < maxPage; pi++) {
            	final List executeDateList = pi >= pageExecuteDateList.size() ? Collections.EMPTY_LIST : (List) pageExecuteDateList.get(pi);
            	
                svf.VrSetForm("KNJC156.frm", 4);

                svf.VrsOut("TITLE", "授業予定表");      // タイトル
                svf.VrsOut("HR_NAME", schData._hrNameAbbv); // クラス
                svf.VrsOut("SCHREGNO", schData._schregno);  // 学籍番号
                svf.VrsOut("NAME", schData._name);          // 氏名
                
                for (final Iterator subit = schData._subclassList.iterator(); subit.hasNext();) {
                	final Subclass subClassData = (Subclass) subit.next();
                	
    				final int rowNo = Integer.parseInt((String) schData._subclasscdRowMap.get(subClassData._subclasscd));

                    svf.VrsOutn("SUBCLASS_NAME" + (KNJ_EditEdit.getMS932ByteLength(subClassData._subclassName) > 18 ? "2": "1"), rowNo, subClassData._subclassName); // 科目
                    svf.VrsOutn("MUST_TIME", rowNo, subClassData._schSeqAll);                       // 必要時間数
                }

                int colNo  = 0;
                for (final Iterator dit = executeDateList.iterator(); dit.hasNext();) {
                	final Map dateMap = (Map) dit.next();

                	final String executeDate = (String) dateMap.get("DATE");
                    final String dateMonth = KNJ_EditDate.h_format_S(executeDate, "M");
                    final String dateDay   = KNJ_EditDate.h_format_S(executeDate, "d");
                    svf.VrsOut("DATE2_1", dateMonth + "月"); // 日付（月）
                    svf.VrsOut("DATE2_2", dateDay   + "日"); // 日付（日）
                    
                    final Map subclassMap = (Map) dateMap.get("SUBCLASSMAP");
                    //log.debug(" subclassMap = " + subclassMap + " (" + executeDate + ")");

                    for (final Iterator subit = schData._subclassList.iterator(); subit.hasNext();) {
                    	final Subclass subclass = (Subclass) subit.next();
                    	
        				final int rowNo = Integer.parseInt((String) schData._subclasscdRowMap.get(subclass._subclasscd));
        				
                        final Map subclassperiodMap = (Map) subclassMap.get(subclass._subclasscd);
                        if (null != subclassperiodMap) {
                        	StringBuffer periodListName = new StringBuffer();
                        	final Map periodMap = (Map) subclassperiodMap.get("PERIODMAP");
                        	for (final Iterator it = periodMap.keySet().iterator(); it.hasNext();) {
                        		final String periodcd = (String) it.next();
                        		String periodname = StringUtils.defaultString((String) periodMap.get(periodcd));
                        		for (int i = 0; i < periodname.length(); i++) {
                        			final char ch = periodname.charAt(i);
                        			if (!('0' <= ch && ch <= '9' || '０' <= ch && ch <= '９')) {
                        				if (i == 0) {
                        					periodname = "";
                        				} else {
                        					periodname = periodname.substring(0, i);
                        				}
                        				break;
                        			}
                        		}
                        		if (StringUtils.isBlank(periodname)) {
                        			log.info(" periodname null " + periodname + ", " + periodMap.get(periodcd));
                        			continue;
                        		}
                        		if (periodListName.length() > 0) {
                        			periodListName.append("･");
                        		}
                        		periodListName.append(periodname);
                        	}
                        	svf.VrsOutn("PERIOD_NAME", rowNo, periodListName.toString());
                        }
                    }

                    svf.VrEndRecord();
                    colNo += 1;
                }

                for (int i = colNo; i < maxCol; i++) {
                    svf.VrEndRecord();
                }
                _hasData = true;
            }
        }
    }

    private static class SchregData {
        final String _schregno;
        final String _schoolKind;
        final String _name;
        final String _nameKana;
        final String _hrNameAbbv;
        final String _staffName;
        final String _attendno;
        Map _subclasscdRowMap;
        List _subclassList;
        List _executeDateList;
        public SchregData(
                final String schregno,
                final String schoolKind,
                final String name,
                final String nameKana,
                final String hrNameAbbv,
                final String staffName,
                final String attendno
        ) {
            _schregno     = schregno;
            _schoolKind   = schoolKind;
            _name         = name;
            _nameKana     = nameKana;
            _hrNameAbbv   = hrNameAbbv;
            _staffName    = staffName;
            _attendno     = attendno;
        }
        
        private void load(final DB2UDB db2, final Param param) {
    		int rowNo = 1;
    		_subclasscdRowMap = new TreeMap();
    		_subclassList = Subclass.getSubclassList(db2, _schregno, param);
    		for (Iterator it2 = _subclassList.iterator(); it2.hasNext();) {
    		    Subclass subClassData = (Subclass) it2.next();

    		    _subclasscdRowMap.put(subClassData._subclasscd, String.valueOf(rowNo));
    		    rowNo++;
    		}
    		
    		_executeDateList = getExecuteDateList(db2, _schregno, param);

    	}
        
        /**
         * 生徒ごとの授業実施日付
         * @param db2
         * @param schregNo
         * @return
         */
        private static List getExecuteDateList(final DB2UDB db2, final String schregNo, final Param param) {
            final List dateMapList = new ArrayList();
            final Map dateKeyMap = new HashMap();
            final String sql = getExecuteDateSql(schregNo, param);
            log.debug(" sql =" + sql);

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
            	
                final String date = KnjDbUtils.getString(row, "EXECUTEDATE");
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (null == subclasscd) {
                	continue;
                }

                if (!dateKeyMap.containsKey(date)) {
                	final Map dateMap = new HashMap();
                	dateMap.put("DATE", date);
                	dateMap.put("SUBCLASSMAP", new TreeMap());
                	dateKeyMap.put(date, dateMap);
                	dateMapList.add(dateMap);
                }

            	final Map dateMap = (Map) dateKeyMap.get(date);
            	final Map subclassMap = (Map) dateMap.get("SUBCLASSMAP");
            	if (!subclassMap.containsKey(subclasscd)) {
            		final Map subclassPeriodMap = new TreeMap();
            		subclassPeriodMap.put("PERIODMAP", new TreeMap());
            		subclassMap.put(subclasscd, subclassPeriodMap);
            	}

            	final Map subclassPeriodMap = (Map) subclassMap.get(subclasscd);
            	final Map periodMap = (Map) subclassPeriodMap.get("PERIODMAP");
            	periodMap.put(KnjDbUtils.getString(row, "PERIODCD"), KnjDbUtils.getString(row, "PERIOD_ABBV"));
            }
            return dateMapList;
        }

        private static String getExecuteDateSql(final String schregNo, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHEDULE AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCCHR.YEAR ");
            stb.append("   , SCCHR.SEMESTER ");
            stb.append("   , SCCHR.EXECUTEDATE ");
            stb.append("   , SCCHR.PERIODCD ");
            stb.append("   , CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , B001.NAME1 AS PERIOD_ABBV ");
            stb.append("   , STD.APPENDDATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REG_D ");
            stb.append("     INNER JOIN CHAIR_STD_DAT STD ON REG_D.YEAR     = STD.YEAR ");
            stb.append("                                  AND REG_D.SEMESTER = STD.SEMESTER ");
            stb.append("                                  AND REG_D.SCHREGNO = STD.SCHREGNO ");
            stb.append("     INNER JOIN SCH_CHR_DAT SCCHR ON SCCHR.EXECUTEDATE BETWEEN STD.APPDATE AND STD.APPENDDATE ");
            stb.append("                                 AND STD.CHAIRCD = SCCHR.CHAIRCD ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON STD.YEAR     = CHAIR.YEAR ");
            stb.append("                               AND STD.SEMESTER = CHAIR.SEMESTER ");
            stb.append("                               AND STD.CHAIRCD  = CHAIR.CHAIRCD ");
            stb.append("     LEFT JOIN NAME_MST B001 ON B001.NAMECD1 = 'B001' ");
            stb.append("                            AND B001.NAMECD2 = SCCHR.PERIODCD ");
            stb.append(" WHERE ");
            stb.append("         REG_D.YEAR     = '"+ param._ctrlYear +"' ");
            stb.append("     AND REG_D.SCHREGNO = '"+ schregNo +"' ");
            stb.append("     AND CHAIR.CLASSCD <= '90' ");
            stb.append(" GROUP BY ");
            stb.append("     SCCHR.YEAR ");
            stb.append("   , SCCHR.SEMESTER ");
            stb.append("   , SCCHR.EXECUTEDATE, SCCHR.PERIODCD, CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD, B001.NAME1 ");
            stb.append("   , STD.APPENDDATE ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.EXECUTEDATE ");
            stb.append("   , T1.PERIODCD ");
            stb.append("   , T1.SUBCLASSCD ");
            stb.append("   , T1.PERIOD_ABBV ");
            stb.append(" FROM ");
            stb.append("     SCHEDULE T1 ");
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("     INNER JOIN SEMESTER_MST SEME ON T1.YEAR       = SEME.YEAR ");
                stb.append("                                 AND T1.SEMESTER   = SEME.SEMESTER ");
                stb.append("                                 AND T1.APPENDDATE = SEME.EDATE ");
            }
            stb.append(" GROUP BY ");
            stb.append("     T1.EXECUTEDATE ");
            stb.append("   , T1.PERIODCD ");
            stb.append("   , T1.SUBCLASSCD ");
            stb.append("   , T1.PERIOD_ABBV ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXECUTEDATE ");
            stb.append("   , T1.PERIODCD ");
            stb.append("   , T1.SUBCLASSCD ");
            stb.append("   , T1.PERIOD_ABBV ");

            return stb.toString();
        }
        
        private static List getSchList(final DB2UDB db2, final Param param) {
            final List retList = new ArrayList();
            final String sql = getSchregSql(param);
            log.debug(" sql =" + sql);

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final String schregno   = KnjDbUtils.getString(row, "SCHREGNO");
                final String name       = KnjDbUtils.getString(row, "NAME");
                final String nameKana   = KnjDbUtils.getString(row, "NAME_KANA");
                final String hrNameAbbv = KnjDbUtils.getString(row, "HR_NAMEABBV");
                final String staffName  = KnjDbUtils.getString(row, "STAFFNAME");
                final String schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
                final String attendno = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) : KnjDbUtils.getString(row, "ATTENDNO");

                final SchregData schData = new SchregData(schregno, schoolKind, name, nameKana, hrNameAbbv, staffName, attendno);
                retList.add(schData);
            }
            return retList;
        }

        private static String getSchregSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     HDAT.HR_NAMEABBV, ");
            stb.append("     STFF.STAFFNAME, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR  = GDAT.YEAR ");
            stb.append("                                    AND REGD.GRADE = GDAT.GRADE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR     = HDAT.YEAR ");
            stb.append("                                    AND REGD.SEMESTER = HDAT.SEMESTER ");
            stb.append("                                    AND REGD.GRADE    = HDAT.GRADE ");
            stb.append("                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST STFF ON HDAT.TR_CD1 = STFF.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("         REGD.YEAR     = '" + param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append("     AND REGD.SCHREGNO IN " + param._schNoSelectedIn + " ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO ");

            return stb.toString();
        }
    }

    private static class Subclass {
        final String _subclasscd;
        final String _subclassName;
        String _schSeqAll;
        public Subclass(
                final String subclassCd,
                final String subclassName,
                final String schSeqAll
        ) {
            _subclasscd   = subclassCd;
            _subclassName = subclassName;
            _schSeqAll    = schSeqAll;
        }
        

        /**
         * 生徒ごとの科目（講座）リスト
         * @param db2
         * @param schregno
         * @return
         */
        private static List getSubclassList(final DB2UDB db2, final String schregno, final Param param) {
            final List subclassList = new ArrayList();
            final Map subclassDataMap = new HashMap();
            
            final String sql = getSubClassSql(schregno, param);
            log.debug(" sql =" + sql);

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final String subclasscd   = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String subclassName = KnjDbUtils.getString(row, "SUBCLASSNAME");
                final String schSeqAll    = KnjDbUtils.getString(row, "SCH_SEQ_ALL");

                final String key = subclasscd;
				if (subclassDataMap.containsKey(key)) {
                    final Subclass added = (Subclass) subclassDataMap.get(key);
                    if (null == added._schSeqAll || NumberUtils.isDigits(added._schSeqAll) && NumberUtils.isDigits(schSeqAll) && Integer.parseInt(schSeqAll) > Integer.parseInt(added._schSeqAll)) {
                    	added._schSeqAll = schSeqAll;
                    }
                } else {
                	final Subclass subclass = new Subclass(subclasscd, subclassName, schSeqAll);
                	subclassDataMap.put(key, subclass);
                	subclassList.add(subclass);
                }
            }

            return subclassList;
        }

        private static String getSubClassSql(final String schregno, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STD_D.SCHREGNO, ");
            stb.append("     SUB_M.CLASSCD || '-' || SUB_M.SCHOOL_KIND || '-' || SUB_M.CURRICULUM_CD || '-' || SUB_M.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SUB_M.SUBCLASSNAME, ");
            stb.append("     MAX(CORES.SCH_SEQ_ALL) AS SCH_SEQ_ALL ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT STD_D ");
            stb.append("     LEFT JOIN CHAIR_DAT CHAIR ON STD_D.YEAR     = CHAIR.YEAR ");
            stb.append("                              AND STD_D.SEMESTER = CHAIR.SEMESTER ");
            stb.append("                              AND STD_D.CHAIRCD  = CHAIR.CHAIRCD ");
            stb.append("     INNER JOIN V_SUBCLASS_MST SUB_M ON CHAIR.YEAR          = SUB_M.YEAR ");
            stb.append("                                   AND CHAIR.CLASSCD       = SUB_M.CLASSCD ");
            stb.append("                                   AND CHAIR.SCHOOL_KIND   = SUB_M.SCHOOL_KIND ");
            stb.append("                                   AND CHAIR.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
            stb.append("                                   AND CHAIR.SUBCLASSCD    = SUB_M.SUBCLASSCD ");
            stb.append("     LEFT JOIN CHAIR_CORRES_DAT CORES ON CHAIR.YEAR          = CORES.YEAR ");
            stb.append("                                     AND CHAIR.CHAIRCD       = CORES.CHAIRCD ");
            stb.append("                                     AND CHAIR.CLASSCD       = CORES.CLASSCD ");
            stb.append("                                     AND CHAIR.SCHOOL_KIND   = CORES.SCHOOL_KIND ");
            stb.append("                                     AND CHAIR.CURRICULUM_CD = CORES.CURRICULUM_CD ");
            stb.append("                                     AND CHAIR.SUBCLASSCD    = CORES.SUBCLASSCD ");
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("     INNER JOIN SEMESTER_MST SEME ON STD_D.YEAR       = SEME.YEAR ");
                stb.append("                                 AND STD_D.SEMESTER   = SEME.SEMESTER ");
                stb.append("                                 AND STD_D.APPENDDATE = SEME.EDATE ");
            }
            stb.append(" WHERE ");
            stb.append("         STD_D.YEAR     = '"+ param._ctrlYear +"' ");
            stb.append("     AND STD_D.SCHREGNO = '"+ schregno +"' ");
            stb.append("     AND CHAIR.CLASSCD <= '90' ");
            stb.append("     AND EXISTS (SELECT * ");
            stb.append("                 FROM SCH_CHR_DAT ");
            stb.append("                 WHERE ");
            stb.append("                      EXECUTEDATE BETWEEN STD_D.APPDATE AND STD_D.APPENDDATE ");
            stb.append("                  AND CHAIRCD = CHAIR.CHAIRCD ");
            stb.append("                )");
            stb.append(" GROUP BY ");
            stb.append("     STD_D.SCHREGNO, ");
            stb.append("     SUB_M.CLASSCD || '-' || SUB_M.SCHOOL_KIND || '-' || SUB_M.CURRICULUM_CD || '-' || SUB_M.SUBCLASSCD, ");
            stb.append("     SUB_M.SUBCLASSNAME ");
            stb.append(" ORDER BY ");
            stb.append("     STD_D.SCHREGNO, ");
            stb.append("     SUB_M.CLASSCD || '-' || SUB_M.SCHOOL_KIND || '-' || SUB_M.CURRICULUM_CD || '-' || SUB_M.SUBCLASSCD ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private static Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 69266 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolCd;
        private final String _schNoSelectedIn;
        private final String _printSubclassLastChairStd;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _ctrlYear           = request.getParameter("CTRL_YEAR");
            _ctrlSemester       = request.getParameter("CTRL_SEMESTER");
            _ctrlDate           = request.getParameter("CTRL_DATE");
            _schoolCd           = request.getParameter("SCHOOLCD");

            final String[] schNoSelected = request.getParameterValues("CATEGORY_SELECTED");
            _schNoSelectedIn = getSchregNoIn(schNoSelected);

            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");
        }

        private String getSchregNoIn(final String[] schNoSelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < schNoSelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + schNoSelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }
    }
}

// eof
