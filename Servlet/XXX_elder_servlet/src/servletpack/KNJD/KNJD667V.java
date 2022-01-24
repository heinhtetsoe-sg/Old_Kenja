/*
 * $Id: 94cd449e3ba62c8de57e4e1d77b9935566f84f50 $
 *
 * 作成日: 2018/07/25
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD667V {

    private static final Log log = LogFactory.getLog(KNJD667V.class);

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
        String formname = "1".equals(_param._category_isclass) ? "KNJD667V_1.frm" : "KNJD667V_2.frm";

        getDataList(db2);

        for (Iterator iterator = _param._outputinf._outputmap.keySet().iterator(); iterator.hasNext();) {
            String kstr = (String)iterator.next();
            OutputAllInfo ctlobj = (OutputAllInfo)_param._outputinf._outputmap.get(kstr);

            svf.VrSetForm(formname, 4);
            setTitle(svf, ctlobj);

            int putcnt = 0;
            int pageputmax = "1".equals(_param._category_isclass) ? 14 : 7;
            for (Iterator ita = ctlobj._datamap.keySet().iterator();ita.hasNext();) {
                String astr = (String)ita.next();
                OutputDataInfo ctlsubobj = (OutputDataInfo)ctlobj._datamap.get(astr);
                if (putcnt >= pageputmax) {
                	//改ページ
                	//svf.VrEndPage();
                    svf.VrSetForm(formname, 4);
                    setTitle(svf, ctlobj);
                	putcnt = 0;
                }

                setSubTitle(svf, ctlsubobj, putcnt, astr);

            	int putmax = "1".equals(_param._category_isclass) ? 12 : 35;
                final List data = ctlsubobj.getDataList(putmax);
				for (int ii = 0;ii < data.size();ii++) {
                	DataDetail putwk = (DataDetail) data.get(ii);
                	String rankfield = "RANK"+(putcnt+1); //RANKxx
                	//putwk._rank;
                	svf.VrsOut(rankfield, putwk._rank);
                    int namelen = KNJ_EditEdit.getMS932ByteLength(putwk._name); //NAMExx_yy
                	String namefield = "NAME" + (putcnt+1) + "_" + (namelen > 30 ? "3" :(namelen > 20 ? "2" : "1"));
                	//putwk._name;
                	svf.VrsOut(namefield, putwk._name);
                	//SCORExx
                	String scorefield = "SCORE"+(putcnt+1);
                	//putwk._score;
                	svf.VrsOut(scorefield, putwk._score);
                	svf.VrEndRecord();
                }
                putcnt++;
            }
            _hasData = true;
        }
    }

    private void setTitle(final Vrw32alp svf, final OutputAllInfo ctlobj) {
        String setttlstr = _param._outputinf._gradename + "・" + _param._outputinf._semestername + "・" + _param._outputinf._testname;
        if ("1".equals(_param._category_isclass)) {
        	setttlstr += "　" + ctlobj._hrname;
        } else {
        	setttlstr += "　" + ctlobj._coursecodename;
        }
    	//TITLE
    	svf.VrsOut("TITLE", setttlstr);
    	//COLOR
    	svf.VrAttribute("TITLE", "PAINT=("+_param._gradecolor + ",0,2)");
    }

    private void setSubTitle(final Vrw32alp svf, final OutputDataInfo ctlsubobj, final int putcnt, final String keystr) {
        //SUBCLASS_NAME
    	final String subclsnamefield = "SUBCLASS_NAME" + (putcnt+1);
    	String perfstr = StringUtils.defaultString(ctlsubobj._perfect, "");
    	String subclsname = "555555".equals(keystr) ? "5教科合計" : ctlsubobj._subclassname;
    	String setsubstr = StringUtils.defaultString(subclsname, keystr) + ("".equals(perfstr) ? "" : perfstr + "点");
    	svf.VrsOut(subclsnamefield, setsubstr);
    	//COLOR
    	svf.VrAttribute(subclsnamefield, "PAINT=("+_param._gradecolor + ",0,2)");
    	svf.VrEndRecord();
    	//BLANK
    	svf.VrsOut("BLANK"+(putcnt+1), "AAA");
    	svf.VrEndRecord();
    	//HEADER
    	svf.VrsOut("HEADER"+(putcnt+1), "AAA");
    	svf.VrEndRecord();

    }

    private void getDataList(final DB2UDB db2) {
    	String sql = "";
    	//クラス指定なら、クラス指定のsqlを利用する。
    	if ("1".equals(_param._category_isclass)) {
    		sql = sqlgetClassRankList();
    	} else {
    		sql = sqlgetCourseRankList();
    	}
        PreparedStatement pssub = null;
        ResultSet rssub = null;

        try {
            log.debug(" sql =" + sql);
            pssub = db2.prepareStatement(sql);
            rssub = pssub.executeQuery();
            while (rssub.next()) {
                final String coursecode = rssub.getString("COURSECODE");
                final String coursecodename = rssub.getString("COURSECODENAME");
                //final String gradename  = rssub.getString("GRADENAME");
                final String hrclass = rssub.getString("HR_CLASS");
                final String hrname = rssub.getString("HR_NAME");
                final String subclasscd = rssub.getString("SUBCLASSCD");
                final String subclassname = rssub.getString("SUBCLASSNAME");
                //final String semestername = rssub.getString("SEMESTERNAME");
                final String perfect = rssub.getString("PERFECT");
                final String rank = rssub.getString("RANK");
                final String name = rssub.getString("NAME");
                final String score = rssub.getString("SCORE");
                _param._outputinf.setData(hrclass, hrname, coursecode, coursecodename, subclasscd, subclassname, perfect, rank, name, score);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, pssub, rssub);
            db2.commit();
        }

    }

    /** (クラス別)各種情報取得SQL */
    private String sqlgetClassRankList() {
        final StringBuffer stb = new StringBuffer();

        //PERFECT_RECORD_SDIV_DATと結合するために必要となる情報がSCHREG_REGD_DATに保持しているので、
        //先にRECORD_RANK_SDIV_DATとSCHREG_REGD_DATを結合する。
        stb.append(" WITH SCHREGRELATION AS (");
        stb.append(" SELECT ");
        stb.append("   RRSD.*, ");
        stb.append("   SRD.GRADE, ");
        stb.append("   SRD.HR_CLASS, ");
        stb.append("   SRD.COURSECD, ");
        stb.append("   SRD.MAJORCD, ");
        stb.append("   SRD.COURSECODE ");
        stb.append(" FROM");
        stb.append("   RECORD_RANK_SDIV_DAT RRSD ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("     ON  SRD.SCHREGNO = RRSD.SCHREGNO ");
        stb.append("     AND SRD.YEAR = RRSD.YEAR ");
        stb.append("     AND SRD.SEMESTER = RRSD.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("   RRSD.YEAR = '" + _param._year + "' ");
        stb.append("   AND RRSD.SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND SRD.GRADE = '" + _param._grade + "' ");
        stb.append(" ) ");
        //上記を元に、必要な情報と結合する。
        stb.append(" SELECT ");
        stb.append("   SCHREL.COURSECODE,");
        stb.append("   CM.COURSECODENAME,");
        stb.append("   HDAT.HR_CLASS, ");
        stb.append("   HDAT.HR_NAME, ");
        stb.append("   SCHREL.SUBCLASSCD, ");
        stb.append("   SM.SUBCLASSNAME, ");
        stb.append("   PRD.PERFECT, ");
        stb.append("   SBM.NAME, ");
        stb.append("   SCHREL.SCORE AS SCORE, ");
        stb.append("   SCHREL.CLASS_RANK AS RANK ");
        stb.append(" FROM ");
        stb.append("   SCHREGRELATION SCHREL ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("     ON  SRD.SCHREGNO = SCHREL.SCHREGNO ");
        stb.append("     AND SRD.YEAR = SCHREL.YEAR ");
        stb.append("     AND SRD.SEMESTER = SCHREL.SEMESTER ");
        stb.append("   LEFT JOIN PERFECT_RECORD_SDIV_DAT PRD ");
        stb.append("     ON  PRD.YEAR = SCHREL.YEAR ");
        stb.append("     AND PRD.SEMESTER = SCHREL.SEMESTER ");
        stb.append("     AND PRD.TESTKINDCD = SCHREL.TESTKINDCD ");
        stb.append("     AND PRD.TESTITEMCD = SCHREL.TESTITEMCD ");
        stb.append("     AND PRD.CLASSCD = SCHREL.CLASSCD ");
        stb.append("     AND PRD.SCHOOL_KIND = SCHREL.SCHOOL_KIND ");
        stb.append("     AND PRD.CURRICULUM_CD = SCHREL.CURRICULUM_CD ");
        stb.append("     AND PRD.SUBCLASSCD = SCHREL.SUBCLASSCD ");
        stb.append("     AND PRD.DIV = '1' ");
        stb.append("     AND PRD.GRADE = SCHREL.GRADE ");
        stb.append("     AND PRD.COURSECODE = SCHREL.COURSECODE ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("     ON SBM.SCHREGNO = SCHREL.SCHREGNO ");
        stb.append("   LEFT JOIN SUBCLASS_MST SM ");
        stb.append("     ON SM.CLASSCD = SCHREL.CLASSCD ");
        stb.append("     AND SM.SCHOOL_KIND = SCHREL.SCHOOL_KIND ");
        stb.append("     AND SM.CURRICULUM_CD = SCHREL.CURRICULUM_CD ");
        stb.append("     AND SM.SUBCLASSCD = SCHREL.SUBCLASSCD ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT HDAT ");
        stb.append("     ON  HDAT.YEAR = SCHREL.YEAR ");
        stb.append("     AND HDAT.SEMESTER = SCHREL.SEMESTER ");
        stb.append("     AND HDAT.GRADE = SCHREL.GRADE ");
        stb.append("     AND HDAT.HR_CLASS = SCHREL.HR_CLASS ");
        stb.append("   LEFT JOIN COURSECODE_MST CM ");
        stb.append("     ON CM.COURSECODE = SCHREL.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("   SCHREL.YEAR = '" + _param._year + "' ");
        stb.append("   AND SCHREL.GRADE = '" + _param._grade + "' ");
        stb.append("   AND SCHREL.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("   AND SCHREL.SEMESTER || SCHREL.TESTKINDCD || SCHREL.TESTITEMCD || SCHREL.SCORE_DIV = '" + _param._testkind_cd + "' ");
        stb.append("   AND SCHREL.HR_CLASS IN " + _param._selectInState + " ");
        //5教科合計以外の合計は除外する。
        stb.append("   AND SCHREL.SUBCLASSCD NOT IN ('333333', '777777', '999999', '99999B') ");
        //合併先を除外する
        stb.append("   AND (SCHREL.CLASSCD, SCHREL.SCHOOL_KIND, SCHREL.CURRICULUM_CD, SCHREL.SUBCLASSCD) NOT IN (SELECT ");
        stb.append("        COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD ");
        stb.append("     FROM ");
        stb.append("        SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("     WHERE ");
        stb.append("        YEAR = '" + _param._year + "' ");
        stb.append("   ) ");
        stb.append(" ORDER BY ");
        stb.append("   HDAT.HR_CLASS, ");
        stb.append("   CASE WHEN SM.SUBCLASSCD = '555555' THEN 1 ELSE 0 END DESC, ");
        stb.append("   SM.SUBCLASSCD ASC, ");
        stb.append("   SCHREL.CLASS_RANK ");


        return stb.toString();
    }

    /** (コース別)各種情報取得SQL */
    private String sqlgetCourseRankList() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCHREGRELATION AS (");
        stb.append(" SELECT ");
        stb.append("   RRSD.*, ");
        stb.append("   SRD.GRADE, ");
        stb.append("   SRD.HR_CLASS, ");
        stb.append("   SRD.COURSECD, ");
        stb.append("   SRD.MAJORCD, ");
        stb.append("   SRD.COURSECODE ");
        stb.append(" FROM");
        stb.append("   RECORD_RANK_SDIV_DAT RRSD ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("     ON  SRD.SCHREGNO = RRSD.SCHREGNO ");
        stb.append("     AND SRD.YEAR = RRSD.YEAR ");
        stb.append("     AND SRD.SEMESTER = RRSD.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("   RRSD.YEAR = '" + _param._year + "' ");
        stb.append("   AND RRSD.SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND SRD.GRADE = '" + _param._grade + "' ");
        stb.append(" ) ");
        //上記を元に、必要な情報と結合する。
        stb.append(" SELECT ");
        stb.append("   SCHREL.COURSECODE,");
        stb.append("   CM.COURSECODENAME,");
        stb.append("   HDAT.HR_CLASS, ");
        stb.append("   HDAT.HR_NAME, ");
        stb.append("   SCHREL.SUBCLASSCD, ");
        stb.append("   SM.SUBCLASSNAME, ");
        stb.append("   PRD.PERFECT, ");
        stb.append("   SBM.NAME, ");
        stb.append("   SCHREL.SCORE AS SCORE, ");
        stb.append("   SCHREL.COURSE_RANK AS RANK ");
        stb.append(" FROM ");
        stb.append("   SCHREGRELATION SCHREL ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("     ON  SRD.SCHREGNO = SCHREL.SCHREGNO ");
        stb.append("     AND SRD.YEAR = SCHREL.YEAR ");
        stb.append("     AND SRD.SEMESTER = SCHREL.SEMESTER ");
        stb.append("   LEFT JOIN PERFECT_RECORD_SDIV_DAT PRD ");
        stb.append("     ON  PRD.YEAR = SCHREL.YEAR ");
        stb.append("     AND PRD.SEMESTER = SCHREL.SEMESTER ");
        stb.append("     AND PRD.TESTKINDCD = SCHREL.TESTKINDCD ");
        stb.append("     AND PRD.TESTITEMCD = SCHREL.TESTITEMCD ");
        stb.append("     AND PRD.CLASSCD = SCHREL.CLASSCD ");
        stb.append("     AND PRD.SCHOOL_KIND = SCHREL.SCHOOL_KIND ");
        stb.append("     AND PRD.CURRICULUM_CD = SCHREL.CURRICULUM_CD ");
        stb.append("     AND PRD.SUBCLASSCD = SCHREL.SUBCLASSCD ");
        stb.append("     AND PRD.DIV = '1' ");
        stb.append("     AND PRD.GRADE = SCHREL.GRADE ");
        stb.append("     AND PRD.COURSECODE = SCHREL.COURSECODE ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("     ON SBM.SCHREGNO = SCHREL.SCHREGNO ");
        stb.append("   LEFT JOIN SUBCLASS_MST SM ");
        stb.append("     ON SM.CLASSCD = SCHREL.CLASSCD ");
        stb.append("     AND SM.SCHOOL_KIND = SCHREL.SCHOOL_KIND ");
        stb.append("     AND SM.CURRICULUM_CD = SCHREL.CURRICULUM_CD ");
        stb.append("     AND SM.SUBCLASSCD = SCHREL.SUBCLASSCD ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT HDAT ");
        stb.append("     ON  HDAT.YEAR = SCHREL.YEAR ");
        stb.append("     AND HDAT.SEMESTER = SCHREL.SEMESTER ");
        stb.append("     AND HDAT.GRADE = SCHREL.GRADE ");
        stb.append("     AND HDAT.HR_CLASS = SCHREL.HR_CLASS ");
        stb.append("   LEFT JOIN COURSECODE_MST CM ");
        stb.append("     ON CM.COURSECODE = SCHREL.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("   SCHREL.YEAR = '" + _param._year + "' ");
        stb.append("   AND SCHREL.GRADE = '" + _param._grade + "' ");
        stb.append("   AND SCHREL.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("   AND SCHREL.SEMESTER || SCHREL.TESTKINDCD || SCHREL.TESTITEMCD || SCHREL.SCORE_DIV = '" + _param._testkind_cd + "' ");
        stb.append("   AND SCHREL.COURSECODE IN " + _param._selectInState + " ");
        //5教科合計以外の合計は除外する。
        stb.append("   AND SCHREL.SUBCLASSCD NOT IN ('333333', '777777', '999999', '99999B') ");
        //合併先を除外する
        stb.append("   AND (SCHREL.CLASSCD, SCHREL.SCHOOL_KIND, SCHREL.CURRICULUM_CD, SCHREL.SUBCLASSCD) NOT IN (SELECT ");
        stb.append("        COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD ");
        stb.append("     FROM ");
        stb.append("        SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("     WHERE ");
        stb.append("        YEAR = '" + _param._year + "' ");
        stb.append("   ) ");
        stb.append(" ORDER BY ");
        stb.append("   SCHREL.COURSECODE, ");
        stb.append("   CASE WHEN SM.SUBCLASSCD = '555555' THEN 1 ELSE 0 END DESC, ");
        stb.append("   SM.SUBCLASSCD ASC, ");
        stb.append("   SCHREL.COURSE_RANK ");

        return stb.toString();
    }

    private class OutputInfo {
    	//ヘッダ情報
    	private final String _gradename;
    	private final String _semestername;
    	private final String _testname;
    	//データ情報
    	final Map _outputmap;  //HR_CLASS or COURSECODE毎にOutputAllInfoを格納。

    	OutputInfo (final String gradename, final String semestername, final String testname) {
    		_gradename = gradename;
    		_semestername = semestername;
    		_testname = testname;
    		_outputmap = new LinkedMap();
    	}
    	private void setData(final String hrclass, final String hrname, final String coursecode, final String coursecodename, final String subclasscd, final String subclassname, final String perfect, final String rank, final String name, final String score) {
    		String keystr = "";
    		if ("1".equals(_param._category_isclass)) {
    			keystr = hrclass;//HR_CLASS
    		} else {
    			keystr = coursecode;//COURSECODE
    		}
    		OutputAllInfo ctlobj;
    		if (null == _outputmap.get(keystr)) {
    			ctlobj = new OutputAllInfo(keystr);//
    			_outputmap.put(keystr, ctlobj); //HR_CLASS or COURSECODEを指定
    		} else {
    			ctlobj = (OutputAllInfo)_outputmap.get(keystr);
    		}
			//詳細データを突っ込む
			ctlobj.setData(hrname, coursecodename, subclasscd, subclassname, perfect, rank, name, score);
    	}
    }

    private class OutputAllInfo {
    	//表タイトル情報
    	private final String _keystr; //HR_CLASS or COURSECODE
    	private String _hrname;
    	private String _coursecodename;
    	//リスト情報
    	private final Map _datamap;   //出力データ分だけDataDetailを格納。
    	OutputAllInfo(final String keystr) {
    		_keystr = keystr;
    		_datamap = new LinkedMap();
    		_hrname = "";
    		_coursecodename = "";
    	}
    	private void setData(final String hrname, final String coursecodename, final String subclasscd, final String subclassname, final String perfect, final String rank, final String name, final String score) {
    		String subkeystr = subclasscd; //subclasscd
    		if ("1".equals(_param._category_isclass)) {
    			_hrname = hrname;
    		} else {
    			_coursecodename = coursecodename;
    		}
    		OutputDataInfo ctlobj;
    		if (null == _datamap.get(subkeystr)) {
    			ctlobj = new OutputDataInfo(subclassname, perfect);
    			//詳細データを突っ込む
    			_datamap.put(subkeystr, ctlobj);
    		} else {
    			ctlobj = (OutputDataInfo)_datamap.get(subkeystr);
    			//詳細データを突っ込む
    		}
    		ctlobj.setData(rank, name, score);
    	}
    }

    private class OutputDataInfo {
    	//表タイトル情報
    	private final String _subclassname;
    	private final String _perfect;
    	//リスト情報
    	private final List _datalist;   //出力データ分だけDataDetailを格納。
    	OutputDataInfo(final String subclassname, final String perfect) {
    		_subclassname = subclassname;
    		_perfect = perfect;
    		_datalist = new ArrayList();
    	}
    	private void setData(final String rank, final String name, final String score) {
    		DataDetail addwk = new DataDetail(rank, name, score);
    		_datalist.add(addwk);
    	}
    	private List getDataList(final int max) {
    		final List rtn = new ArrayList();
    		if (_datalist.size() <= max) {
    			rtn.addAll(_datalist);
    		} else {
    			// max内の最後の生徒と同一順位のすべての生徒がmaxに収まらない場合、maxの同一順位の生徒を対象から除く
    			rtn.addAll(_datalist.subList(0, max));
    			DataDetail printLast = (DataDetail) rtn.get(rtn.size() - 1);
    			boolean removeFlg = false;
    			List sameRankList = new ArrayList();
    			if (NumberUtils.isDigits(printLast._rank)) {
    				for (final Iterator it = _datalist.iterator(); it.hasNext();) {
    					DataDetail d = (DataDetail) it.next();
    					if (NumberUtils.isDigits(printLast._rank) && NumberUtils.isDigits(d._rank) && Integer.parseInt(d._rank) == Integer.parseInt(printLast._rank) || !NumberUtils.isDigits(printLast._rank) && !NumberUtils.isDigits(d._rank)) {
    						sameRankList.add(d);
    						if (!rtn.contains(d)) {
    							removeFlg = true;
    						}
    					}
    				}
    			}
    			if (removeFlg) {
    				rtn.removeAll(sameRankList);
    			}
    		}
    		return rtn;
    	}
    }

    private class DataDetail {
    	private final String _rank;
    	private final String _name;
    	private final String _score;
    	DataDetail(final String rank, final String name, final String score) {
    		_rank = rank;
    		_name = name;
    		_score = score;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67694 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _category_isclass;
        private final String _testkind_cd;
        private final String _schoolkind;
        private final String _grade;
        private final String _gradecolor;
        private final String _useCurriculumcd;
        private final String _date;
        private final String[] _categorySelected;
        private final String _selectInState;
        private final OutputInfo _outputinf;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year               = request.getParameter("YEAR");
            _semester           = request.getParameter("SEMESTER");
            _category_isclass   = request.getParameter("CATEGORY_IS_CLASS");
            _testkind_cd        = request.getParameter("TESTKIND_CD");
            _schoolkind         = request.getParameter("SCHOOLKIND");
            _grade              = request.getParameter("GRADE");
            _gradecolor         = request.getParameter("GRADECOLOR");
            _useCurriculumcd    = request.getParameter("useCurriculumcd");
            _date               = request.getParameter("CTRL_DATE");

            _categorySelected   = request.getParameterValues("CATEGORY_SELECTED");
            _selectInState = getSelectedInState();

            String dbg = getGradeName(db2);
            dbg = getSemesterName(db2);
            dbg = getTestName(db2);
            _outputinf = new OutputInfo(getGradeName(db2), getSemesterName(db2), getTestName(db2));
        }

        private String getGradeName(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
        }
        private String getSemesterName(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
        }
        private String getTestName(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testkind_cd + "' "));
        }

        private String getSelectedInState() {
        	String retstr = "";
        	String sep = "";
        	for (int ii = 0;ii < _categorySelected.length;ii++) {
        		retstr += sep + "'" + _categorySelected[ii] + "'";
        		sep = ",";
        	}
        	retstr = "(" + retstr + ")";
        	return retstr;
        }

    }
}

// eof
