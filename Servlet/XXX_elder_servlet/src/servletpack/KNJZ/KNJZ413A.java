/*
 * $Id: 4496d8c64345196f92a93a2baa50984969f3f7e8 $
 *
 * 闖ｴ諛茨ｿｽ蜈亥ｾ�: 2013/01/09
 * 闖ｴ諛茨ｿｽ蜊��ｿｽ�ｿｽ: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * KNJZ413A 大学マスタのPDF出力
 */
public class KNJZ413A {

    private static final Log log = LogFactory.getLog(KNJZ413A.class);

    private boolean _hasData;

    private Param _param;

    /**
     * @param request
     * @param response
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

    private List getCollegeInfosList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
//            log.fatal(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schoolGroup     = rs.getString("SCHOOL_GROUP");
                final String schoolName      = rs.getString("SCHOOL_NAME");
                final String facultyName     = rs.getString("FACULTYNAME");
                final String departmentName  = rs.getString("DEPARTMENTNAME");
                final String schoolCd        = rs.getString("SCHOOL_CD");
                final String FacultyCd       = rs.getString("FACULTYCD");
                final String departmentCd    = rs.getString("DEPARTMENTCD");

                final CollegeInfo collegeInfo = new CollegeInfo(schoolGroup, schoolName, facultyName, departmentName, schoolCd, FacultyCd, departmentCd);
                list.add(collegeInfo);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }
    
    private static int getMS932Length(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }
        return 0;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJZ413A.frm", 1);
        final List list = getCollegeInfosList(db2);
        //log.fatal(" size = " + list.size());
        int line = 0;
        
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._date));
        for (int i = 0; i < list.size(); i++) {
            
        	if (line >= 60) {
                svf.VrEndPage();
                svf.VrsOut("DATE", _param._date);
                line = 0;
            }
            line += 1;
            
            final CollegeInfo collegeInfo = (CollegeInfo) list.get(i);
            
			svf.VrsOutn("SCHOOL_GROUP", line, collegeInfo.getSchoolGroupAbbv());
			
            final String sNameField = KNJ_EditEdit.getMS932ByteLength(collegeInfo._schoolName) > 30 ? "2" : "1";
			svf.VrsOutn("SCHOOL_NAME" + sNameField, line, collegeInfo._schoolName);
			final String fNameField = KNJ_EditEdit.getMS932ByteLength(collegeInfo._facultyName) > 30 ? "2" : "1";
			svf.VrsOutn("FACULTYNAME" + fNameField, line, collegeInfo._facultyName);

			svf.VrsOutn("DEPARTMENTNAME1", line, collegeInfo._departmentName);
			svf.VrsOutn("SCHOOL_CD", line, collegeInfo._schoolCd);
			svf.VrsOutn("FACULTYCD", line, collegeInfo._FacultyCd);
			svf.VrsOutn("DEPARTMENTCD", line, collegeInfo._departmentCd);
            
            _hasData = true;
        }

	    if (_hasData) {
            svf.VrEndPage();
        }

    }
    
    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT ");
        stb.append("      T1.SCHOOL_GROUP, ");
        stb.append("      T1.SCHOOL_NAME, ");
        stb.append("      T2.FACULTYNAME, ");
        stb.append("      T3.DEPARTMENTNAME, ");
        stb.append("      T1.SCHOOL_CD, ");
        stb.append("      T2.FACULTYCD, ");
        stb.append("      T3.DEPARTMENTCD ");
        stb.append("    FROM ");
        stb.append("      COLLEGE_MST T1 ");
        stb.append("      LEFT JOIN COLLEGE_FACULTY_MST T2 ");
        stb.append("        ON T1.SCHOOL_CD = T2.SCHOOL_CD ");
        stb.append("      LEFT JOIN COLLEGE_DEPARTMENT_MST T3 ");
        stb.append("        ON T2.SCHOOL_CD = T3.SCHOOL_CD ");
        stb.append("        AND T2.FACULTYCD = T3.FACULTYCD ");
        if (!"ALL".equals(_param._schoolGroup)) {
        	stb.append("    WHERE ");
        	stb.append("      T1.SCHOOL_GROUP = '" + _param._schoolGroup + "' ");
        }
        stb.append("    ORDER BY ");
        stb.append("      T1.SCHOOL_GROUP, ");
        stb.append("      T1.SCHOOL_CD, ");
        stb.append("      T2.FACULTYCD, ");
        stb.append("      T3.DEPARTMENTCD ");
        
        return stb.toString();
    }

    private static class CollegeInfo {
    	final String _schoolGroup;
        final String _schoolName;
        final String _facultyName;
        final String _departmentName;
        final String _schoolCd;
        final String _FacultyCd;
        final String _departmentCd;
        CollegeInfo(
        		final String schoolGroup,
                final String schoolName,
                final String facultyName,
                final String departmentName,
                final String schoolCd,
                final String FacultyCd,
                final String departmentCd
        ) {
        	_schoolGroup    = schoolGroup;
            _schoolName 	= schoolName;
            _facultyName 	= facultyName;
            _departmentName = departmentName;
            _schoolCd 		= schoolCd;
            _FacultyCd 		= FacultyCd;
            _departmentCd 	= departmentCd;
          }
       
       private String getSchoolGroupAbbv() {
    	   int schoolGroupNum = Integer.parseInt(StringUtils.defaultIfEmpty(this._schoolGroup, "0")); //文字列を数値に変換する際に0埋めが取れることに注意
    	   switch (schoolGroupNum) {
    	   	case 1:
    		   return "国";
    	   	case 2:
    		   return "公";
    	   	case 3:
    		   return "私";
    	   	case 4:
    		   return "公短";
    	   	case 5:
    		   return "私短";
    	   	case 6:
    		   return "専門";
    	   	case 7:
    		   return "看護";
    	   	case 8:
    		   return "大学校";
    	   	case 9:
    		   return "職業";
    	   	case 99:
    		   return "その他";
    	   	default:
    			 return "";    		   
    	   }
       }
        
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72923 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    private class Param {
        final String _date;
        final String _schoolGroup;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {

            _schoolGroup = request.getParameter("SCHOOL_GROUP");
            _date		 = request.getParameter("CTRL_DATE");
            
        }
    }
}

// eof

