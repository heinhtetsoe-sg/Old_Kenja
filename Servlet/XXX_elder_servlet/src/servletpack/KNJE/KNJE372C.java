package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;


/**
 * 一覧
 */
public class KNJE372C {

    private static final Log log = LogFactory.getLog(KNJE372C.class);
    private boolean _hasData;
    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

        } catch (final Exception e) {
            log.error("Exception:", e);
            return;
        }

        _param = createParam(db2, request);

        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();

            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            printMain(db2, svf);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private static List getPageList(final List studentlist, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = studentlist.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        svf.VrSetForm("KNJE372C.frm", 1);

        //生徒単位で回す
        final int maxLine = 17;
        final List studentList = getStudenList(db2);

        for (final Iterator it = getPageList(studentList, maxLine).iterator(); it.hasNext();) {
            final List pageStudents = (List) it.next();

            //タイトル
            svf.VrsOut("TITLE", "志望学科登録チェックリスト");
            svf.VrsOut("HR_NAME", _param._hrName);
            svf.VrsOut("DATE", _param._ctrlDate.replace("-","/"));

            int schRowCnt = 1;
            for (int linei = 0; linei < pageStudents.size(); linei++) {
	            final Student student = (Student) pageStudents.get(linei);

	            //出席番号
	            svf.VrsOutn("NO", schRowCnt, student._attendno);

	            //氏名
	            final int nameByte	= getMS932ByteLength(student._name);
	            final String fieldNo = nameByte <= 20 ? "1" : nameByte <= 30 ? "2" : "3";
	            svf.VrsOutn("NAME"+fieldNo, schRowCnt, student._name);

	            //校友会活動CD
	            svf.VrsOutn("ACT_CD" , schRowCnt, student._actCd);
	            //校友会活動名
	            final String actName = (_param._actNameMap.containsKey(student._actCd)) ? _param._actNameMap.get(student._actCd) : "";
	            svf.VrsOutn("ACT_NAME1" , schRowCnt, actName);
	            //校友会活動内容
	            String[] contentList = KNJ_EditEdit.get_token(student._actContent, 50, 4);
	            if (contentList != null) {
	            	for (int act_i = 0; act_i < contentList.length; act_i++) {
	            		final String rowContent = contentList[act_i];
			            svf.VrsOutn("ACT" + (act_i + 1) , schRowCnt, rowContent);
	            	}
	            }
	            //辞退
	            final String declineMark = ("1".equals(student._decFlg)) ? "〇" : "";
	            svf.VrsOutn("REFUSE" , schRowCnt, declineMark);

	            //志望順位単位のループ
	            int colCnt = 0;
	            for (int hopeColCnt = 1; hopeColCnt <= _param._limitCnt; hopeColCnt++) {
	            	final String hopeOrder = String.format("%02d", hopeColCnt);
	            	colCnt++;
	            	final int hopeRow = (hopeColCnt <= 21) ? 1 : 2; 	//21列目までは1段目に表示
	            	if ((colCnt > 21)) {
	            	    colCnt = 1;
	            	}

	            	//ヘッダ行 志望順位
		            svf.VrsOutn("HOPE_HEAD_NO" + hopeRow, colCnt, hopeOrder);

		            //志望学科CD
	            	if (!student._hopeMap.containsKey(hopeOrder)) continue;
	            	final String depCd = student._hopeMap.get(hopeOrder);
		            svf.VrsOutn("HOPE_NO" + schRowCnt + "_" + hopeRow, colCnt, depCd);

		            //志望学科名
		            final String depName = (_param._depNameMap.containsKey(depCd)) ? _param._depNameMap.get(depCd) : "";
		            final int depNameByte = getMS932ByteLength(depName);
		            final String depNameSizeField = (depNameByte <= 7) ? "1" : "2";
		            svf.VrsOutn("DEP_NAME" + schRowCnt + "_" + hopeRow + "_" + depNameSizeField, colCnt, depName);
	            }

	            schRowCnt++;
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }

    private List getStudenList(final DB2UDB db2)  throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List studentList = new ArrayList();
        try {
            final String sql = getStudentSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            Map schMap = new TreeMap();

            while (rs.next()) {
                final String schregno 		= rs.getString("SCHREGNO");
				final String attendno   	= rs.getString("ATTENDNO");
                final String proAvg1 		= StringUtils.defaultIfEmpty(rs.getString("PROFICIENCY1_AVG"), "");
				final String proAvg2   		= StringUtils.defaultIfEmpty(rs.getString("PROFICIENCY2_AVG"), "");
				final String name   		= StringUtils.defaultIfEmpty(rs.getString("NAME"), "");
				final String actCd   		= StringUtils.defaultIfEmpty(rs.getString("ACTIVITY_CD"), "");
				final String actContent   	= StringUtils.defaultIfEmpty(rs.getString("ACTIVITY_CONTENT"), "");
				final String decFlg   		= StringUtils.defaultIfEmpty(rs.getString("DECLINE_FLG"), "");

				final String hopeOrder 		= rs.getString("HOPE_ORDER");
				final String depCd			= StringUtils.defaultIfEmpty(rs.getString("DEPARTMENT_CD"), "");

				if (!schMap.containsKey(schregno)) {
					final Student student = new Student(schregno, attendno, proAvg1, proAvg2, name, actCd, actContent, decFlg);
					schMap.put(schregno, student);

					//生徒をリストに追加
					studentList.add(student);
				}

				//志望順-志望学科をマップに登録
				final Student student = (Student)schMap.get(schregno);
				if (hopeOrder != null) {
					student._hopeMap.put(hopeOrder, depCd);
				}
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return studentList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT ");
        stb.append("      T1.SCHREGNO, ");
        stb.append("      T1.ATTENDNO, ");
        stb.append("      T2.PROFICIENCY1_AVG, ");
        stb.append("      T2.PROFICIENCY2_AVG, ");
        stb.append("      T3.NAME, ");
        stb.append("      T4.ACTIVITY_CD, ");
        stb.append("      T4.ACTIVITY_CONTENT, ");
        stb.append("      T4.DECLINE_FLG, ");
        stb.append("      T5.HOPE_ORDER, ");
        stb.append("      T5.DEPARTMENT_CD ");
        stb.append("    FROM ");
        stb.append("      SCHREG_REGD_DAT T1 ");
        stb.append("      LEFT JOIN AFT_SCHREG_CONVERT_SCORE_DAT T2 ");
        stb.append("        ON T2.YEAR = T1.YEAR ");
        stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST T3 ");
        stb.append("        ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("      LEFT JOIN AFT_SCHREG_RECOMMENDATION_INFO_DAT T4 ");
        stb.append("        ON T4.YEAR = T1.YEAR ");
        stb.append("        AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("      LEFT JOIN AFT_SCHREG_HOPE_DEPARTMENT T5 ");
        stb.append("        ON T5.YEAR = T1.YEAR ");
        stb.append("        AND T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("    WHERE ");
        stb.append("      T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("      AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("      AND T1.GRADE || '-' || T1.HR_CLASS = '" + _param._grade + "-" + _param._hrClass +  "' ");
        stb.append("    ORDER BY ");
        stb.append("      T1.GRADE, ");
        stb.append("      T1.HR_CLASS, ");
        stb.append("      T1.ATTENDNO, ");
        stb.append("      T5.HOPE_ORDER ");

        return stb.toString();
    }


    private class Student {
        final String _schregno;
        final String _attendno;
        final String _proAvg1;
        final String _proAvg2;
        final String _name;
        final String _actCd;
        final String _actContent;
        final String _decFlg;
        Map<String, String> _hopeMap;
        Student(
                final String schregno,
                final String attendno,
                final String proAvg1,
                final String proAvg2,
                final String name,
                final String actCd,
                final String actContent,
                final String decFlg
        ) {
        	_schregno   =  schregno;
        	_attendno   =  attendno;
        	_proAvg1    =  proAvg1;
        	_proAvg2    =  proAvg2;
        	_name       =  name;
        	_actCd      =  actCd;
        	_actContent =  actContent;
        	_decFlg     =  decFlg;
        	_hopeMap    = new TreeMap();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 77224 $ $Date: 2020-10-02 17:17:14 +0900 (金, 02 10 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {
    	final String _ctrlDate;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _grade;
        final String _hrClass;
        final String _schoolKind;
        final String _gradeCd;
        final String _hrName;
        final int _limitCnt;
        final Map<String, String> _depNameMap;
        final Map<String, String> _actNameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) {
        	_ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _schoolKind = "H"; //高校固定
            _gradeCd = "03"; //3年固定
            _hrName = getHrName(db2, _grade);
            final String limitCntStr = request.getParameter("limitCnt");
            _limitCnt = (limitCntStr != null) ? Integer.parseInt(limitCntStr) : 0;
            _depNameMap = getDepNameMap(db2);
            _actNameMap = getActNameMap(db2);
        }
        private String getHrName(final DB2UDB db2, final String grade) {
            String hrName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT HR_NAME FROM SCHREG_REGD_HDAT WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _ctrlSemester + "' AND GRADE = '" + _grade + "' AND HR_CLASS = '" + _hrClass + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                	hrName = rs.getString("HR_NAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return hrName;
        }

        private Map getDepNameMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map rtnMap = new TreeMap();
            try {
            	final StringBuffer stb = new StringBuffer();
            	stb.append("    WITH REC_LIMIT AS (  ");
            	stb.append("    SELECT  ");
            	stb.append("      DEPARTMENT_S,  ");
            	stb.append("      DEPARTMENT_H,  ");
            	stb.append("      DEPARTMENT_ABBV2  ");
            	stb.append("    FROM  ");
            	stb.append("      AFT_RECOMMENDATION_LIMIT_MST  ");
            	stb.append("    WHERE  ");
            	stb.append("      YEAR = '" + _ctrlYear + "'  ");
            	stb.append("    ), DEPARTMENT_HS AS ( ");
            	stb.append("    SELECT DEPARTMENT_S AS DEPARTMENTCD, '専)' || DEPARTMENT_ABBV2 AS DEP_ABBV FROM REC_LIMIT  ");
            	stb.append("    UNION  ");
            	stb.append("    SELECT DEPARTMENT_H AS DEPARTMENTCD, '併)' || DEPARTMENT_ABBV2 AS DEP_ABBV FROM REC_LIMIT  ");
            	stb.append("    ) ");
            	stb.append("    SELECT  ");
            	stb.append("      DEPARTMENTCD,  ");
            	stb.append("      DEP_ABBV  ");
            	stb.append("    FROM  ");
            	stb.append("      DEPARTMENT_HS  ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                	rtnMap.put(rs.getString("DEPARTMENTCD"), rs.getString("DEP_ABBV"));
                }
            } catch (SQLException ex) {
                log.debug(" exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        private Map getActNameMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map rtnMap = new TreeMap();
            try {
            	final StringBuffer stb = new StringBuffer();
            	stb.append("    SELECT  ");
            	stb.append("      NAMECD2,  ");
            	stb.append("      NAME1  ");
            	stb.append("    FROM  ");
            	stb.append("      V_NAME_MST  ");
            	stb.append("    WHERE  ");
            	stb.append("      YEAR 		= '" + _ctrlYear + "' AND ");
            	stb.append("      NAMECD1 	= 'E071'  ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                	rtnMap.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (SQLException ex) {
                log.debug(" exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }
    }

}// クラスの括り
