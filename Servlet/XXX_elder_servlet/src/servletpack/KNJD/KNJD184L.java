/*
 * $Id$
 *
 * 作成日: 2019/03/25
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD184L {

    private static final Log log = LogFactory.getLog(KNJD184L.class);

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

            printMain(db2, svf, response);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
        	if (null != _param) {
        		DbUtils.closeQuietly(_param._psSubclassView);
        	}
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final HttpServletResponse response) {
        final Map printMap = GradeHrClass.getPrintList(db2, _param);

        for (Iterator itArea = printMap.keySet().iterator(); itArea.hasNext();) {
            final String cd = (String) itArea.next();
            final GradeHrClass gradeHrClass = (GradeHrClass) printMap.get(cd);

            for (final Iterator itStudent = gradeHrClass._studentList.iterator(); itStudent.hasNext();) {

                final Student student = (Student) itStudent.next();

                if ("1".equals(_param._printHyoshi)) {
                	printForm1(db2, svf, student);
                }

                if ("1".equals(_param._printSeiseki)) {
                	printForm2(db2, svf, student);

                	printForm3(db2, svf, student);
                }

                if ("1".equals(_param._printShuryo)) {
                	printForm4(db2, svf, student);
                }

                _hasData = true;
            }
        }
    }


    private void printForm1(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD184L_1.frm", 1);

        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度"); //年度
        svf.VrsOut("SCHOOL_NAME", (String) _param._certifSchoolMap.get("SCHOOL_NAME")); //学校名
        //final String hr_name = "".equals(student._gradeName) && "".equals(student._hrName) ? "第" + student._gradeName + "学年　" + student._hrName + "組":"";
        //svf.VrsOut("HR_NAME", hr_name); //年組

        if (null != _param._logoFile) {
            svf.VrsOut("SCHOOL_LOGO", _param._logoFile.getAbsolutePath()); //年組
        }

        svf.VrsOut("HR_NAME", student._hrName + student._shussekiBangou); //年組
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名
        final String pNameField = KNJ_EditEdit.getMS932ByteLength((String)_param._certifSchoolMap.get("PRINCIPAL_NAME")) > 30 ? "2" : "1";
        svf.VrsOut("PRINCIPAL_NAME" + pNameField, (String) _param._certifSchoolMap.get("PRINCIPAL_NAME")); //校長名
        final String trNameField = KNJ_EditEdit.getMS932ByteLength(student._staffName) > 30 ? "2" : "1";
        svf.VrsOut("TR_NAME" + trNameField, student._staffName); //担任名

        svf.VrEndPage();
    }

    private void VrsOutnArray(final Vrw32alp svf, final String field, final List data) {
    	for (int i = 0; i < data.size(); i++) {
    		svf.VrsOutn(field, i + 1, (String) data.get(i));
    	}
    }

    private void printForm2(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD184L_2.frm", 4);

        svf.VrsOut("HR_NAME", student._hrName + student._shussekiBangou); //年組
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        //評定
        setHyoutei(svf, student);

        //宗教・道徳
        VrsOutnArray(svf, "MORAL1", KNJ_EditKinsoku.getTokenList(student._moral1, 15 * 2));
        if("2".equals(_param._semester)) {
            //2学期
            VrsOutnArray(svf, "MORAL2", KNJ_EditKinsoku.getTokenList(student._moral2, 15 * 2));
        }else if("3".equals(_param._semester)) {
            //2学期
            VrsOutnArray(svf, "MORAL2", KNJ_EditKinsoku.getTokenList(student._moral2, 15 * 2));
            //3学期
            VrsOutnArray(svf, "MORAL3", KNJ_EditKinsoku.getTokenList(student._moral3, 15 * 2));
        }

        //出欠席の様子
        setAttend(db2, svf, student);

        //所見
        VrsOutnArray(svf, "REMARK1", KNJ_EditKinsoku.getTokenList(student._remark1_1, 30 * 2));
        if (Integer.parseInt(_param._semester) >= 3) {
        	VrsOutnArray(svf, "REMARK2", KNJ_EditKinsoku.getTokenList(student._remark2, 30 * 2));
        }

        //学校生活の様子
        setView(db2, svf, student);

        svf.VrEndPage();
    }

    private void setHyoutei(final Vrw32alp svf, final Student student) {
        int idx = 1;
        for (Iterator itArea = student.getPrintSubclassList().iterator(); itArea.hasNext();) {
            final SubClass subClass = (SubClass) itArea.next();

            if (_param.isPrintSubclass(subClass._mst, student._gradeCd)) {
                //1学期
                svf.VrsOut("CLASS_NAME" + String.valueOf(idx), insertSpace(subClass._subclassName)); //教科名
                svf.VrsOutn("VAL" + String.valueOf(idx), 1, subClass._score1); //評定

                if(!"1".equals(_param._semester)) {
                    //2学期
                    svf.VrsOutn("VAL" + String.valueOf(idx), 2, subClass._score2); //評定
                }
                if("3".equals(_param._semester)) {
                    //3学期
                    svf.VrsOutn("VAL" + String.valueOf(idx), 3, subClass._score3); //評定
                }

                idx++;
            }
        }

    }

    private String insertSpace(final String s) {
    	final StringBuffer stb = new StringBuffer();
    	if (null != s) {
    		String space = "";
    		for (int i = 0; i < s.length(); i++) {
    			stb.append(space).append(s.charAt(i));
    			space = " ";
    		}
    	}
		return stb.toString();
	}

	private void setView(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        String sql = viewSql(student, student._grade);
        List list = KnjDbUtils.query(db2, sql);
        if (list.isEmpty()) {
            sql = viewSql(student, "00");
            list = KnjDbUtils.query(db2, sql);
        }

        for (final Iterator it = list.iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();

            final String lName = StringUtils.defaultString(KnjDbUtils.getString(row, "L_NAME"));
            final String mName = StringUtils.defaultString(KnjDbUtils.getString(row, "M_NAME"));
            final String view1;
            final String view2;
            final String view3;
            if ("1".equals(_param._prmBehaviorSd)) {
                view1 = StringUtils.defaultString(KnjDbUtils.getString(row, "NMVIEW1"));
                view2 = StringUtils.defaultString(KnjDbUtils.getString(row, "NMVIEW2"));
                view3 = StringUtils.defaultString(KnjDbUtils.getString(row, "NMVIEW3"));
            } else {
                view1 = "".equals(StringUtils.defaultString(KnjDbUtils.getString(row, "VIEW1"))) ? "" : "○";
                view2 = "".equals(StringUtils.defaultString(KnjDbUtils.getString(row, "VIEW2"))) ? "" : "○";
                view3 = "".equals(StringUtils.defaultString(KnjDbUtils.getString(row, "VIEW3"))) ? "" : "○";
            }

            svf.VrsOut("VIEW_CLASS", lName); //学校生活教科
            svf.VrsOut("VIEW_NAME", mName); //学校生活めあて
            svf.VrsOut("VIEW1", view1); //学校生活評価(1学期)
            final int iseme = Integer.parseInt(_param._semester);
            if (iseme >= 2) {
            	svf.VrsOut("VIEW2", view2); //学校生活評価(2学期)
            }
            if (iseme >= 3) {
            	svf.VrsOut("VIEW3", view3); //学校生活評価(3学期)
            }

            svf.VrEndRecord();
        }
        if (list.size() == 0) {
            svf.VrEndRecord();
        }
    }

    private void setAttend(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        String totalLesson = "0";
        String totalSuspend = "0";
        String totalnotice = "0";
        String totalnonotice = "0";
        String totalLate = "0";
        String totalEarly = "0";
        String totalNoruma = "0";
        String totalShusseki = "0";
        int eDateMonth = Integer.parseInt(_param._eDateMonth);

        eDateMonth = eDateMonth >= 4 ? eDateMonth - 3 : eDateMonth + 9;

        for(int i = 1; i <= 8; i++) {
            for(int j = 1; j <= 12; j++) {
                if( eDateMonth >= j) {
                    svf.VrsOutn("ATTEND" + String.valueOf(j) , i , "0");
                }
            }
            svf.VrsOutn("ATTEND_TOTAL" , i , "0");
        }


        try {
            final String sql = attendSql(student);
            //log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String month = StringUtils.defaultString(rs.getString("MONTH"),"0");
                final String lesson = StringUtils.defaultString(rs.getString("LESSON"),"0"); //授業日数
                final String suspend = StringUtils.defaultString(rs.getString("SUSPEND"),"0"); //出席停止・忌引き日数
                final String nonotice = StringUtils.defaultString(rs.getString("NONOTICE"),"0"); //病欠
                final String notice = StringUtils.defaultString(rs.getString("NOTICE"),"0"); //事故欠
                final String late = StringUtils.defaultString(rs.getString("LATE"),"0"); //遅刻
                final String early = StringUtils.defaultString(rs.getString("EARLY"),"0"); //早退
                final String noruma = String.valueOf(Integer.parseInt(lesson) - Integer.parseInt(suspend)); //出席しなければならない日数
                final String sick = StringUtils.defaultString(rs.getString("SICK"),"0"); //計算ワーク
                final String shusseki = String.valueOf(Integer.parseInt(noruma) - Integer.parseInt(sick) - Integer.parseInt(notice) - Integer.parseInt(nonotice)); //出席日数

                final String attendField = Integer.parseInt(month) < 4 ? String.valueOf(Integer.parseInt(month) + 9) : String.valueOf(Integer.parseInt(month) - 3);
                svf.VrsOutn("ATTEND" + attendField , 1 , lesson); //授業日数
                svf.VrsOutn("ATTEND" + attendField , 2 , suspend); //出席停止・忌引き日数
                svf.VrsOutn("ATTEND" + attendField , 3 , noruma); //出席しなければならない日数
                svf.VrsOutn("ATTEND" + attendField , 4 , nonotice);  //病欠
                svf.VrsOutn("ATTEND" + attendField , 5 , notice);  //事故欠
                svf.VrsOutn("ATTEND" + attendField , 6 , shusseki); //出席日数
                svf.VrsOutn("ATTEND" + attendField , 7 , late); //遅刻
                svf.VrsOutn("ATTEND" + attendField , 8 , early); //早退

                //各月
                totalLesson = String.valueOf(Integer.parseInt(totalLesson) + Integer.parseInt(lesson));
                totalSuspend = String.valueOf(Integer.parseInt(totalSuspend) + Integer.parseInt(suspend));
                totalnotice = String.valueOf(Integer.parseInt(totalnotice) + Integer.parseInt(notice));
                totalnonotice = String.valueOf(Integer.parseInt(totalnonotice) + Integer.parseInt(nonotice));
                totalLate = String.valueOf(Integer.parseInt(totalLate) + Integer.parseInt(late));
                totalEarly = String.valueOf(Integer.parseInt(totalEarly) + Integer.parseInt(early));
                totalNoruma = String.valueOf(Integer.parseInt(totalNoruma) + Integer.parseInt(noruma));
                totalShusseki = String.valueOf(Integer.parseInt(totalShusseki) + Integer.parseInt(shusseki));
            }

            //計
            svf.VrsOutn("ATTEND_TOTAL" , 1 , totalLesson); //授業日数
            svf.VrsOutn("ATTEND_TOTAL" , 2 , totalSuspend); //出席停止・忌引き日数
            svf.VrsOutn("ATTEND_TOTAL" , 3 , totalNoruma); //出席しなければならない日数
            svf.VrsOutn("ATTEND_TOTAL" , 4 , totalnonotice); //病欠
            svf.VrsOutn("ATTEND_TOTAL" , 5 , totalnotice); //事故欠
            svf.VrsOutn("ATTEND_TOTAL" , 6 , totalShusseki); //出席日数
            svf.VrsOutn("ATTEND_TOTAL" , 7 , totalLate); //遅刻
            svf.VrsOutn("ATTEND_TOTAL" , 8 , totalEarly); //早退

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void printForm3(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD184L_3.frm", 4);

        svf.VrsOut("HR_NAME", student._hrName + student._shussekiBangou); //年組

        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        int grp = 1;
        int lineIdx = 0;
        final int recordStartY = 632;
        final int recordHeight = 722 - recordStartY;
        final int VIEW_CLASSfieldY = 650;
        boolean endrecord = false;
        final List printSubclassList = student.getPrintSubclassList();
        for (Iterator itArea = printSubclassList.iterator(); itArea.hasNext();) {
            final SubClass subClass = (SubClass) itArea.next();

            try {
            	boolean isModifyY = false;
            	if (null == _param._psSubclassView) {
            		final String sql = subClassViewSql();
            		//log.info(" subclass view sql =" + sql);
            		_param._psSubclassView = db2.prepareStatement(sql);
            	}
            	final Object[] arg = {student._grade, subClass._subclassCd, student._grade, student._schregno, student._schregno, student._schregno};
            	//log.info(" subclass view arg = " + ArrayUtils.toString(arg));
                final List rowList = KnjDbUtils.query(db2, _param._psSubclassView, arg);
                final List subclassNameCharList = new ArrayList();
                String chkSubClsName = "90".equals(subClass._subclassCd.substring(0, 2)) ? "総合" : StringUtils.defaultString(subClass._subclassName);
                if (chkSubClsName.length() < rowList.size()) {
                	final int diff = rowList.size() - chkSubClsName.length();
                	chkSubClsName = StringUtils.repeat(" ", diff / 2) + chkSubClsName;
                	if (diff % 2 == 1) {
                		isModifyY = true;
                	}
                }

                for (int i = 0; i < chkSubClsName.length(); i++) {
                	final char ch = chkSubClsName.charAt(i);
                	subclassNameCharList.add(String.valueOf(ch));
                }
                final int maxLine = Math.max(rowList.size(), subclassNameCharList.size());

                for (int i = 0; i < maxLine; i++) {
                    if (i < subclassNameCharList.size()) {
                        svf.VrsOut("VIEW_CLASS", (String) subclassNameCharList.get(i));
                        if (isModifyY) {
                            svf.VrAttribute("VIEW_CLASS", "Y=" + String.valueOf(recordStartY + recordHeight * lineIdx + recordHeight / 2 + (VIEW_CLASSfieldY - recordStartY)));
                        }
                    }

                	if (i < rowList.size()) {
                		final Map row = (Map) rowList.get(i);
                		final String viewName = StringUtils.defaultString(KnjDbUtils.getString(row, "VIEWNAME"));
                		final String view1 = KnjDbUtils.getString(row, "SCHVIEW1");
                		final String view2 = KnjDbUtils.getString(row, "SCHVIEW2");
                		final String view3 = KnjDbUtils.getString(row, "SCHVIEW3");

                        final int viewnlen = KNJ_EditEdit.getMS932ByteLength(viewName);
                        final String vnfield = viewnlen > 66 ? "2" : "1";
                        svf.VrsOut("VIEW_NAME" + vnfield, viewName); //学校生活めあて
                        //1学期
                        svf.VrsOut("VIEW1", view1); //学校生活評価
                        if(!"1".equals(_param._semester)) {
                            //2学期
                            svf.VrsOut("VIEW2", view2); //学校生活評価
                        }
                        if("3".equals(_param._semester)) {
                            //3学期
                            svf.VrsOut("VIEW3", view3); //学校生活評価
                        }
                	}

                    svf.VrsOut("GRPCD", String.valueOf(grp)); //グループ
                    svf.VrEndRecord();
                    endrecord = true;
                    lineIdx += 1;
                }
                grp++;
            } catch (Exception ex) {
                log.error("Exception:", ex);
            }
        }
        if (!endrecord) {
        	svf.VrEndRecord();
        }

        svf.VrEndPage();
    }

    private void printForm4(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD184L_4.frm", 1);

        if (NumberUtils.isDigits(student._gradeCd)) {
            svf.VrsOut("GRADE", Param.toZenkaku(Integer.valueOf(student._gradeCd).toString())); //学年
        } else {
            svf.VrsOut("GRADE", StringUtils.defaultString(student._gradeCd)); //学年
        }

        svf.VrsOut("CERTIF_DATE", KNJ_EditDate.h_format_JP(db2, _param._shuryoDate)); //日付

        svf.VrsOut("JOB_NAME", (String) _param._certifSchoolMap.get("JOB_NAME")); //職名

        final String pNameField = KNJ_EditEdit.getMS932ByteLength((String)_param._certifSchoolMap.get("PRINCIPAL_NAME")) > 30 ? "2" : "1";
        svf.VrsOut("PRINCIPAL_NAME" + pNameField, (String) _param._certifSchoolMap.get("PRINCIPAL_NAME")); //校長名

        svf.VrEndPage();
    }

    private static class GradeHrClass {
        private final String _grade;
        private final String _gradeName;
        private final String _hrClass;
        private final String _hrName;
        private final List _studentList;

        public GradeHrClass(
                final String grade,
                final String gradeName,
                final String hrClass,
                final String hrName
        ) {
            _grade = grade;
            _gradeName = gradeName;
            _hrClass = hrClass;
            _hrName = hrName;
            _studentList = new ArrayList();
        }

        private static Map getPrintList(final DB2UDB db2, final Param param) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List studentList = new ArrayList();
            try {
                final String sql = studentSql(param);
                //log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                	final String gradeHrClassCd = rs.getString("GRADE_HR_CLASS");
                    final String grade = rs.getString("GRADE");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String gradeName = StringUtils.defaultString(rs.getString("GRADE_NAME3"));
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                    final String attendno = rs.getString("ATTENDNO");
                    final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                    final String name = StringUtils.defaultString(rs.getString("NAME"));
                    final String staffName = StringUtils.defaultString(rs.getString("STAFFNAME"));


                    final String moral1 = StringUtils.defaultString(rs.getString("MORAL1"));
                    final String moral2 = StringUtils.defaultString(rs.getString("MORAL2"));
                    final String moral3 = StringUtils.defaultString(rs.getString("MORAL3"));

                    final String remark1_1 = StringUtils.defaultString(rs.getString("REMARK1_1"));
                    final String remark1_2 = StringUtils.defaultString(rs.getString("REMARK1_2"));
                    final String remark2 = StringUtils.defaultString(rs.getString("REMARK2"));


                    GradeHrClass gradeHrClass = null;
                    if (retMap.containsKey(gradeHrClassCd)) {
                    	gradeHrClass = (GradeHrClass) retMap.get(gradeHrClassCd);
                    } else {
                    	gradeHrClass = new GradeHrClass(grade, gradeName, hrClass, hrName);
                    }
                    final Student student = new Student(gradeHrClassCd,grade,gradeCd,gradeName,hrClass,hrName,attendno,schregno,name,staffName,moral1,moral2,moral3,remark1_1,remark1_2,remark2);
                    gradeHrClass._studentList.add(student);
                    studentList.add(student);
                    retMap.put(gradeHrClassCd, gradeHrClass);
                }

            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
            	final Student student = (Student) it.next();
                SubClass.setSubClassMap(db2, student, param);
            }
            return retMap;
        }

        private static String studentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     REGD.GRADE || REGD.HR_CLASS AS GRADE_HR_CLASS, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     GDAT.GRADE_NAME3, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     STAFF.STAFFNAME, ");
            //宗教・道徳
            stb.append("     HRD1.REMARK1 AS MORAL1, ");
            stb.append("     HRD2.REMARK1 AS MORAL2, ");
            stb.append("     HRD9.REMARK1 AS MORAL3, ");
            //所見
            stb.append("     HRD1.COMMUNICATION AS REMARK1_1, ");
            stb.append("     HRD2.COMMUNICATION AS REMARK1_2, ");
            stb.append("     HRD9.COMMUNICATION AS REMARK2 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ");
            stb.append("        ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ");
            stb.append("       ON REGD.YEAR     = REGDH.YEAR ");
            stb.append("      AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("      AND REGD.GRADE    = REGDH.GRADE ");
            stb.append("      AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("       ON REGD.YEAR  = GDAT.YEAR ");
            stb.append("      AND REGD.GRADE = GDAT.GRADE ");
            stb.append("     LEFT JOIN STAFF_MST STAFF ");
            stb.append("       ON STAFF.STAFFCD = REGDH.TR_CD1 ");
            //1学期 宗教・道徳 , 所見
            stb.append("     LEFT JOIN HREPORTREMARK_DAT HRD1 ");
            stb.append("       ON HRD1.YEAR     = REGD.YEAR ");
            stb.append("      AND HRD1.SCHREGNO = REGD.SCHREGNO ");
            stb.append("      AND HRD1.SEMESTER = '1' ");
            //2学期 宗教・道徳 , 所見
            stb.append("     LEFT JOIN HREPORTREMARK_DAT HRD2 ");
            stb.append("       ON HRD2.YEAR     = REGD.YEAR ");
            stb.append("      AND HRD2.SCHREGNO = REGD.SCHREGNO ");
            stb.append("      AND HRD2.SEMESTER = '2' ");
            //3学期 宗教・道徳 , 所見
            stb.append("     LEFT JOIN HREPORTREMARK_DAT HRD9 ");
            stb.append("       ON HRD9.YEAR     = REGD.YEAR ");
            stb.append("      AND HRD9.SCHREGNO = REGD.SCHREGNO ");
            stb.append("      AND HRD9.SEMESTER = '3' ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._ctrlYear + "' ");
            if ("9".equals(param._semester)) {
            	stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
            	stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            }
            if ("1".equals(param._disp)) {
                stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + whereIn(true, param._categorySelected) + " ");
            } else {
                stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrclass + "' ");
                stb.append("         AND REGD.SCHREGNO IN " + whereIn(true, param._categorySelected));
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");
            return stb.toString();
        }

        /**
         * 文字列の配列を、SQL文where節のin句で使える文字列に変換する。
         * 例:<br/>
         * <pre>
         * whereIn(*, null)                         = null
         * whereIn(*, [])                           = null
         * whereIn(false, [null])                   = "(null)"
         * whereIn(true, [null])                    = null
         * whereIn(*, ["can't"])                    = "('can''t')"
         * whereIn(*, ["abc", "don't"])             = "('abc', 'don''t')"
         * whereIn(false, ["abc", null, "xyz"])     = "('abc', null, 'xyz')"
         * whereIn(true, ["abc", null, "xyz"])      = "('abc', 'xyz')"
         * </pre>
         * @param skipNull nullをスキップするか否か。<code>true</code>ならスキップする
         * @param array 文字列の配列
         * @return 変換後の文字列
         */
        public static String whereIn(final boolean skipNull, final String[] array) {
            if (null == array || 0 == array.length) {
                return null;
            }

            final StringBuffer sb = new StringBuffer();
            int n = 0;
            for (int i = 0; i < array.length; i++) {
                if (null == array[i] && skipNull) {
                    continue;
                }

                if (0 == n) { sb.append("("); }
                if (0 != n) { sb.append(", "); }

                if (null == array[i]) {
                    sb.append(String.valueOf(array[i])); // "null"
                } else {
                    sb.append('\'');
                    sb.append(StringEscapeUtils.escapeSql(array[i]));
                    sb.append('\'');
                }
                //--
                n++;
            }

            if (0 == n) {
                return null;
            }

            sb.append(")");
            return sb.toString();
        }
    }

    private static class Student {
        final String _gradeHrClass;
        final String _grade;
        final String _gradeCd;
        final String _gradeName;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _staffName;

        final String _moral1;
        final String _moral2;
        final String _moral3;

        final String _remark1_1;
        final String _remark1_2;
        final String _remark2;
        private String _shussekiBangou = "";
        final Map _subclassMap;

        public Student(
                final String gradeHrClass,
                final String grade,
                final String gradeCd,
                final String gradeName,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String schregno,
                final String name,
                final String staffName,
                final String moral1,
                final String moral2,
                final String moral3,
                final String remark1_1,
                final String remark1_2,
                final String remark2
        ) {
            _gradeHrClass = gradeHrClass;
            _grade = grade;
            _gradeCd = gradeCd;
            _gradeName = gradeName;
            _hrClass = hrClass;
            _hrName = StringUtils.defaultString(hrName);
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _staffName = staffName;
            _moral1 = moral1;
            _moral2 = moral2;
            _moral3 = moral3;
            _remark1_1 = remark1_1;
            _remark1_2 = remark1_2;
            _remark2 = remark2;
            _subclassMap = new TreeMap();
            if (NumberUtils.isDigits(_attendno)) {
            	_shussekiBangou = String.valueOf(Integer.parseInt(_attendno)) + "番";
            } else {
            	_shussekiBangou = StringUtils.defaultString(_attendno);
            }

        }

		public List getPrintSubclassList() {
			final List list = new ArrayList();
	        for (Iterator itArea = _subclassMap.keySet().iterator(); itArea.hasNext();) {
	            final String cd = (String) itArea.next();
	            final SubClass subClass = (SubClass) _subclassMap.get(cd);
	            list.add(subClass);
	        }
	        Collections.sort(list);
			return list;
		}
    }

    private static class SubClass implements Comparable {
        private final SubclassMst _mst;
        private final String _subclassCd;
        private final String _subclassName;
        private final String _score1;
        private final String _score2;
        private final String _score3;

        public SubClass(
        		final SubclassMst mst,
                final String subclassCd,
                final String subclassName,
                final String score1,
                final String score2,
                final String score3
        ) {
        	_mst = mst;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
        }

        private static void setSubClassMap(final DB2UDB db2, Student student, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = subClassSql(student, param);
                //log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String subclassCd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassName = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String score1 = StringUtils.defaultString(rs.getString("SCORE1"));
                    final String score2 = StringUtils.defaultString(rs.getString("SCORE2"));
                    final String score3 = StringUtils.defaultString(rs.getString("SCORE3"));

                    final String subclassKey = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");

                    final SubClass subClass = new SubClass((SubclassMst) param._subclassMst.get(subclassKey), subclassCd,subclassName,score1,score2,score3);
                    student._subclassMap.put(subclassCd, subClass);
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public int compareTo(final Object o) {
        	final SubClass osub = (SubClass) o;
        	if (null == osub._mst) {
        		return -1;
        	} else if (null == _mst) {
        		return 1;
        	} else {
        		int cmp = _mst.compareTo(osub._mst);
        		if (0 != cmp) {
        			return cmp;
        		}
        	}
        	return _subclassCd.compareTo(_subclassCd);
        }

        private static String subClassSql(final Student student, final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH SUBCLASS AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T2.SHOWORDER3, ");
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     T2.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append(" JVIEWNAME_GRADE_YDAT T1, ");
            stb.append(" V_SUBCLASS_MST T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.YEAR          = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.CLASSCD       = T2.CLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ");
            stb.append("     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("     AND T1.SUBCLASSCD    = T2.SUBCLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND   = 'P' ");
            stb.append("     AND T1.GRADE         = '" + student._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD ");
            stb.append(" ) ");

            //メイン取得
            stb.append(" SELECT ");
            stb.append("     SUB.CLASSCD, ");
            stb.append("     SUB.SCHOOL_KIND, ");
            stb.append("     SUB.CURRICULUM_CD, ");
            stb.append("     SUB.SUBCLASSCD, ");
            stb.append("     SUB.SUBCLASSNAME, ");
            stb.append("     CASE WHEN SCORE1.SCORE = 1 ");
            stb.append("          THEN 'C' ");
            stb.append("          WHEN SCORE1.SCORE = 2 ");
            stb.append("          THEN 'B' ");
            stb.append("          WHEN SCORE1.SCORE = 3 ");
            stb.append("          THEN 'A' ");
            stb.append("          ELSE '' ");
            stb.append("     END AS SCORE1, ");
            stb.append("     CASE WHEN SCORE2.SCORE = 1 ");
            stb.append("          THEN 'C' ");
            stb.append("          WHEN SCORE2.SCORE = 2 ");
            stb.append("          THEN 'B' ");
            stb.append("          WHEN SCORE2.SCORE = 3 ");
            stb.append("          THEN 'A' ");
            stb.append("          ELSE '' ");
            stb.append("     END AS SCORE2, ");
            stb.append("     CASE WHEN SCORE3.SCORE = 1 ");
            stb.append("          THEN 'C' ");
            stb.append("          WHEN SCORE3.SCORE = 2 ");
            stb.append("          THEN 'B' ");
            stb.append("          WHEN SCORE3.SCORE = 3 ");
            stb.append("          THEN 'A' ");
            stb.append("          ELSE '' ");
            stb.append("     END AS SCORE3 ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS SUB ");
            stb.append("     INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = SUB.CLASSCD ");
            stb.append("         AND CLM.SCHOOL_KIND = SUB.SCHOOL_KIND  ");
            //1学期 評定
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE1 ");
            stb.append("       ON SCORE1.YEAR        = SUB.YEAR ");
            stb.append("      AND SCORE1.SEMESTER    = '1' ");
            stb.append("      AND SCORE1.TESTKINDCD  = '99' ");
            stb.append("      AND SCORE1.TESTITEMCD  = '00' ");
            stb.append("      AND SCORE1.SCORE_DIV   = '08' ");
            stb.append("      AND SCORE1.SCHOOL_KIND = SUB.SCHOOL_KIND ");
            stb.append("      AND SCORE1.SCHREGNO    = '" + student._schregno + "' ");
            stb.append("      AND SCORE1.CURRICULUM_CD = SUB.CURRICULUM_CD ");
            stb.append("      AND SCORE1.SUBCLASSCD  = SUB.SUBCLASSCD ");
            //2学期 評定
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE2 ");
            stb.append("       ON SCORE2.YEAR        = SUB.YEAR ");
            stb.append("      AND SCORE2.SEMESTER    = '2' ");
            stb.append("      AND SCORE2.TESTKINDCD  = '99' ");
            stb.append("      AND SCORE2.TESTITEMCD  = '00' ");
            stb.append("      AND SCORE2.SCORE_DIV   = '08' ");
            stb.append("      AND SCORE2.SCHOOL_KIND = SUB.SCHOOL_KIND ");
            stb.append("      AND SCORE2.SCHREGNO    = '" + student._schregno + "' ");
            stb.append("      AND SCORE2.CURRICULUM_CD = SUB.CURRICULUM_CD ");
            stb.append("      AND SCORE2.SUBCLASSCD  = SUB.SUBCLASSCD ");
            //3学期 評定
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE3 ");
            stb.append("       ON SCORE3.YEAR        = SUB.YEAR ");
            stb.append("      AND SCORE3.SEMESTER    = '3' ");
            stb.append("      AND SCORE3.TESTKINDCD  = '99' ");
            stb.append("      AND SCORE3.TESTITEMCD  = '00' ");
            stb.append("      AND SCORE3.SCORE_DIV   = '08' ");
            stb.append("      AND SCORE3.SCHOOL_KIND = SUB.SCHOOL_KIND ");
            stb.append("      AND SCORE3.SCHREGNO    = '" + student._schregno + "' ");
            stb.append("      AND SCORE3.CURRICULUM_CD = SUB.CURRICULUM_CD ");
            stb.append("      AND SCORE3.SUBCLASSCD  = SUB.SUBCLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(CLM.SHOWORDER3, -1), ");
            stb.append("     CLM.CLASSCD, ");
            stb.append("     VALUE(SUB.SHOWORDER3, -1), ");
            stb.append("     SUB.CLASSCD, ");
            stb.append("     SUB.SCHOOL_KIND, ");
            stb.append("     SUB.CURRICULUM_CD, ");
            stb.append("     SUB.SUBCLASSCD ");
            return stb.toString();
        }
    }

    private String viewSql(final Student student, final String lMstGrade) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.L_CD, ");
        stb.append("     T1.L_NAME, ");
        stb.append("     TM.M_NAME, ");
        if ("1".equals(_param._prmBehaviorSd)) {
            stb.append("     LL1.NAMESPARE1 AS NMVIEW1, ");
            stb.append("     LL2.NAMESPARE1 AS NMVIEW2, ");
            stb.append("     LL3.NAMESPARE1 AS NMVIEW3, ");
        }
        stb.append("     L1.M_CD AS VIEW1, ");
        stb.append("     L2.M_CD AS VIEW2, ");
        stb.append("     L3.M_CD AS VIEW3 ");
        stb.append(" FROM ");
        stb.append("     HREPORT_BEHAVIOR_L_MST T1 ");
        stb.append("     LEFT JOIN HREPORT_BEHAVIOR_M_MST TM ");
        stb.append("       ON TM.YEAR = T1.YEAR ");
        stb.append("      AND TM.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("      AND TM.GRADE = T1.GRADE ");
        stb.append("      AND TM.L_CD = T1.L_CD ");
        stb.append("     LEFT JOIN HREPORT_BEHAVIOR_LM_DAT L1 ");
        stb.append("       ON L1.YEAR = T1.YEAR ");
        stb.append("      AND L1.L_CD = T1.L_CD ");
        stb.append("      AND L1.M_CD = TM.M_CD ");
        stb.append("      AND L1.SCHREGNO = '" + student._schregno + "' ");
        stb.append("      AND L1.SEMESTER = '1' ");
        stb.append("     LEFT JOIN HREPORT_BEHAVIOR_LM_DAT L2 ");
        stb.append("       ON L2.YEAR = T1.YEAR ");
        stb.append("      AND L2.L_CD = T1.L_CD ");
        stb.append("      AND L2.M_CD = TM.M_CD ");
        stb.append("      AND L2.SCHREGNO = '" + student._schregno + "' ");
        stb.append("      AND L2.SEMESTER = '2' ");
        stb.append("     LEFT JOIN HREPORT_BEHAVIOR_LM_DAT L3 ");
        stb.append("       ON L3.YEAR = T1.YEAR ");
        stb.append("      AND L3.L_CD = T1.L_CD ");
        stb.append("      AND L3.M_CD = TM.M_CD ");
        stb.append("      AND L3.SCHREGNO = '" + student._schregno + "' ");
        stb.append("      AND L3.SEMESTER = '3' ");
        if ("1".equals(_param._prmBehaviorSd)) {
            stb.append("     LEFT JOIN NAME_MST LL1 ");
            stb.append("       ON LL1.NAMECD1 = 'D036' ");
            stb.append("      AND INTEGER(LL1.NAME1) = INTEGER(L1.RECORD) ");
            stb.append("     LEFT JOIN NAME_MST LL2 ");
            stb.append("       ON LL2.NAMECD1 = 'D036' ");
            stb.append("      AND INTEGER(LL2.NAME1) = INTEGER(L2.RECORD) ");
            stb.append("     LEFT JOIN NAME_MST LL3 ");
            stb.append("       ON LL3.NAMECD1 = 'D036' ");
            stb.append("      AND INTEGER(LL3.NAME1) = INTEGER(L3.RECORD) ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR            = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SCHOOL_KIND = 'P' ");
        stb.append("     AND T1.GRADE       = '" + lMstGrade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.L_CD ");
        stb.append("   , TM.M_CD ");

        return stb.toString();
    }

    private String attendSql(final Student student) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     SEMESTER, ");
        stb.append("     MONTH, ");
        stb.append("     LESSON, ");
        stb.append("     VALUE(SUSPEND, 0) + VALUE(KOUDOME, 0) + VALUE(VIRUS, 0) + VALUE(MOURNING, 0) AS SUSPEND, ");
        stb.append("     SICK, ");
        stb.append("     NOTICE, ");
        stb.append("     NONOTICE, ");
        stb.append("     LATE, ");
        stb.append("     EARLY ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SEMES_DAT");
        stb.append(" WHERE ");
        stb.append("     YEAR         = '" + _param._ctrlYear + "' ");
        if("1".equals(_param._semester)) {
            stb.append("     AND SEMESTER = '1' ");
        }else if("2".equals(_param._semester)) {
        	stb.append("     AND SEMESTER IN ('1','2') ");
        }
        stb.append("     AND SCHREGNO = '" + student._schregno + "' ");

        return stb.toString();
    }

    private String subClassViewSql() {
        final StringBuffer stb = new StringBuffer();
        final String q = "?";

        stb.append(" WITH SUBCLASS AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T2.SUBCLASSCD, ");
        stb.append("     T2.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append(" JVIEWNAME_GRADE_YDAT T1, ");
        stb.append(" SUBCLASS_MST T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR          = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.CLASSCD       = T2.CLASSCD ");
        stb.append("     AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ");
        stb.append("     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("     AND T1.SUBCLASSCD    = T2.SUBCLASSCD ");
        stb.append("     AND T1.SCHOOL_KIND   = 'P' ");
        stb.append("     AND T1.GRADE         = " + q + " ");
        stb.append("     AND T1.SUBCLASSCD    = " + q + " ");
        stb.append(" ORDER BY ");
        stb.append("     SUBCLASSCD ");
        stb.append(" ) ");

        stb.append(" SELECT DISTINCT ");
        stb.append("     SUB.SUBCLASSNAME, ");
        stb.append("     JGM.VIEWCD, ");
        stb.append("     JGM.VIEWNAME, ");
        stb.append("     D" + _param._schoolKind + "29_1.NAMESPARE1 AS SCHVIEW1, ");
        stb.append("     D" + _param._schoolKind + "29_2.NAMESPARE1 AS SCHVIEW2, ");
        stb.append("     D" + _param._schoolKind + "29_3.NAMESPARE1 AS SCHVIEW3  ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS SUB ");
        //名称
        stb.append("     LEFT JOIN JVIEWNAME_GRADE_YDAT YDAT ");
        stb.append("       ON YDAT.YEAR        = SUB.YEAR ");
        stb.append("      AND YDAT.SUBCLASSCD  = SUB.SUBCLASSCD ");
        stb.append("      AND YDAT.SCHOOL_KIND = SUB.SCHOOL_KIND ");
        stb.append("      AND YDAT.GRADE       = " + q + " ");
        stb.append("     LEFT JOIN JVIEWNAME_GRADE_MST JGM ");
        stb.append("       ON JGM.GRADE         = YDAT.GRADE ");
        stb.append("      AND JGM.CLASSCD       = YDAT.CLASSCD ");
        stb.append("      AND JGM.SCHOOL_KIND   = YDAT.SCHOOL_KIND ");
        stb.append("      AND JGM.CURRICULUM_CD = YDAT.CURRICULUM_CD ");
        stb.append("      AND JGM.SUBCLASSCD    = YDAT.SUBCLASSCD ");
        stb.append("      AND JGM.VIEWCD        = YDAT.VIEWCD ");
        //1学期
        stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT SCORE1 ");
        stb.append("       ON SCORE1.YEAR        = SUB.YEAR ");
        stb.append("      AND SCORE1.SEMESTER    = '1' ");
        stb.append("      AND SCORE1.SCHOOL_KIND = SUB.SCHOOL_KIND ");
        stb.append("      AND SCORE1.SCHREGNO    = " + q + " ");
        stb.append("      AND SCORE1.SUBCLASSCD  = SUB.SUBCLASSCD ");
        stb.append("      AND SCORE1.VIEWCD      = JGM.VIEWCD ");
        stb.append("     LEFT JOIN NAME_MST D" + _param._schoolKind + "29_1 ");
        stb.append("       ON D" + _param._schoolKind + "29_1.ABBV1 = SCORE1.STATUS ");
        stb.append("      AND D" + _param._schoolKind + "29_1.NAMECD1 = 'D" + _param._schoolKind + "29' ");
        //2学期
        stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT SCORE2 ");
        stb.append("      ON SCORE2.YEAR        = SUB.YEAR ");
        stb.append("     AND SCORE2.SEMESTER    = '2' ");
        stb.append("     AND SCORE2.SCHOOL_KIND = SUB.SCHOOL_KIND ");
        stb.append("     AND SCORE2.SCHREGNO    = " + q + " ");
        stb.append("     AND SCORE2.SUBCLASSCD  = SUB.SUBCLASSCD ");
        stb.append("     AND SCORE2.VIEWCD      = JGM.VIEWCD ");
        stb.append("     LEFT JOIN NAME_MST D" + _param._schoolKind + "29_2 ");
        stb.append("       ON D" + _param._schoolKind + "29_2.ABBV1 = SCORE2.STATUS ");
        stb.append("      AND D" + _param._schoolKind + "29_2.NAMECD1 = 'D" + _param._schoolKind + "29' ");
        //3学期
        stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT SCORE3 ");
        stb.append("       ON SCORE3.YEAR        = SUB.YEAR ");
        stb.append("      AND SCORE3.SEMESTER    = '3' ");
        stb.append("      AND SCORE3.SCHOOL_KIND = SUB.SCHOOL_KIND ");
        stb.append("      AND SCORE3.SCHREGNO    = " + q + " ");
        stb.append("      AND SCORE3.SUBCLASSCD  = SUB.SUBCLASSCD ");
        stb.append("      AND SCORE3.VIEWCD      = JGM.VIEWCD ");
        stb.append("     LEFT JOIN NAME_MST D" + _param._schoolKind + "29_3 ");
        stb.append("       ON D" + _param._schoolKind + "29_3.ABBV1 = SCORE3.STATUS ");
        stb.append("      AND D" + _param._schoolKind + "29_3.NAMECD1 = 'D" + _param._schoolKind + "29' ");
        stb.append(" ORDER BY ");
        stb.append("     JGM.VIEWCD ");

        return stb.toString();
    }

    private static class SubclassMst implements Comparable {
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        public SubclassMst(final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final int classShoworder3,
                final int subclassShoworder3,
                final boolean isSaki, final boolean isMoto) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _classShoworder3 = new Integer(classShoworder3);
            _subclassShoworder3 = new Integer(subclassShoworder3);
            _isSaki = isSaki;
            _isMoto = isMoto;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "SubclassMst(" + _subclasscd + ":" + _subclassname + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 76863 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _semester;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _grade;
        private final String _gradeHrclass;
        private final String _disp;
        private final String[] _categorySelected;
        private final String _loginDate;
        final String _shuryoDate;
        final String _printHyoshi;
        final String _printSeiseki;
        final String _printShuryo;
        private final String _cmd;
        private final String _eDateMonth;
        private final Map _certifSchoolMap;
        private final String _prmBehaviorSd;
        private final String _schoolKind;
        private Map _subclassMst = null;
        final String _documentroot;
        final String _imagePath;
        final String _extension;
        final File _logoFile;
        PreparedStatement _psSubclassView;
        final Map _d104;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _disp = request.getParameter("DISP");
            if ("1".equals(_disp)) {
                _gradeHrclass = null;
                _grade = request.getParameter("GRADE");
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            } else {
                _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
                _grade = _gradeHrclass.substring(0, 2);
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
                for (int i = 0; i < _categorySelected.length; i++) {
                	_categorySelected[i] = StringUtils.split(_categorySelected[i], "-")[0];
                }
            }
            _loginDate = request.getParameter("CTRL_DATE");
            _printHyoshi = request.getParameter("PRINT_HYOSHI");
            _printSeiseki = request.getParameter("PRINT_SEISEKI");
            _printShuryo = request.getParameter("PRINT_SHURYO");
            _shuryoDate = request.getParameter("SHURYO_DATE");
            _cmd = request.getParameter("cmd");
            _eDateMonth = getSemesterMst(db2, "MONTH(EDATE)", "MONTH");
            _certifSchoolMap = getCertifScholl(db2);
            _prmBehaviorSd = request.getParameter("knjdBehaviorsd_UseText_P");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _schoolKind = getSchoolKind(db2);
            setSubclassMst(db2);

            final Map controlMst = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST "));
            _imagePath = StringUtils.defaultString(KnjDbUtils.getString(controlMst, "IMAGEPATH"));
            _extension = "jpg";

            final File file = new File(_documentroot + "/" + _imagePath + "/" + "SCHOOLLOGO_P." + _extension);
            if (file.exists()) {
                _logoFile = file;
            } else {
            	log.warn(" logoFile " + file.getAbsolutePath() + " not exists.");
                _logoFile = null;
            }
            _d104 = getD104(db2);
        }

        private Map getD104(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT ");
                stb.append("    NAME1, ");
                stb.append("    ABBV1 AS GRADE01, ");
                stb.append("    ABBV2 AS GRADE02, ");
                stb.append("    ABBV3 AS GRADE03, ");
                stb.append("    NAMESPARE1 AS GRADE04, ");
                stb.append("    NAMESPARE2 AS GRADE05, ");
                stb.append("    NAMESPARE3 AS GRADE06 ");
                stb.append("FROM ");
                stb.append("    NAME_MST ");
                stb.append("WHERE ");
                stb.append("    NAMECD1 = 'D104' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map gradeMap = new HashMap();
                    gradeMap.put("01", StringUtils.defaultString(rs.getString("GRADE01")));
                    gradeMap.put("02", StringUtils.defaultString(rs.getString("GRADE02")));
                    gradeMap.put("03", StringUtils.defaultString(rs.getString("GRADE03")));
                    gradeMap.put("04", StringUtils.defaultString(rs.getString("GRADE04")));
                    gradeMap.put("05", StringUtils.defaultString(rs.getString("GRADE05")));
                    gradeMap.put("06", StringUtils.defaultString(rs.getString("GRADE06")));
                    retMap.put(rs.getString("NAME1"), gradeMap);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private boolean isPrintSubclass(final SubclassMst subclassMst, final String grade) {
            if (_d104.containsKey(subclassMst._subclasscd)) {
                final Map printFlgMap = (Map) _d104.get(subclassMst._subclasscd);
                final String printFlg = (String) printFlgMap.get(grade);
                return !"1".equals(printFlg);
            } else {
                return true;
            }
        }

        private static String toZenkaku(final String s) {
            if (StringUtils.isBlank(s)) {
                return "";
            }
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < s.length(); i++) {
            	final char n = s.charAt(0);
            	stb.append(String.valueOf((char) (0xFF10 + n - '0')));
            }
            return stb.toString();
        }

        private String getSchoolKind(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT school_kind FROM schreg_regd_gdat WHERE year='" + _ctrlYear + "' AND grade='" + _grade + "'"));
        }
        private String getSemesterMst(final DB2UDB db2, final String field, final String field2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " AS " + field2 + "  FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field2);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private Map getCertifScholl(final DB2UDB db2) {
            final Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '117' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put("SCHOOL_NAME", StringUtils.defaultString(rs.getString("SCHOOL_NAME")));
                    rtnMap.put("JOB_NAME", rs.getString("JOB_NAME"));
                    rtnMap.put("PRINCIPAL_NAME", StringUtils.defaultString(rs.getString("PRINCIPAL_NAME")));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnMap;
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMst = new HashMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";
                sql += " UNION ";
                sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " VALUE(T2.SHOWORDER3, 999999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999999) AS SUBCLASS_SHOWORDER3, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final int classShoworder3 = rs.getInt("CLASS_SHOWORDER3");
                    final int subclassShoworder3 = rs.getInt("SUBCLASS_SHOWORDER3");
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), classShoworder3, subclassShoworder3, isSaki, isMoto);
                    _subclassMst.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }
}
// eof

