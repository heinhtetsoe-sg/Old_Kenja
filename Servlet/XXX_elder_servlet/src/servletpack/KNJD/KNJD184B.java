/*
 * $Id: 98e817fba6d4c7748735303ef8115bca54dd51ce $
 *
 * 作成日: 2018/06/14
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

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
import java.util.TreeMap;
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD184B {

    private static final Log log = LogFactory.getLog(KNJD184B.class);

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
        final String frmName = "J".equals(_param._schoolKind) ? "KNJD184B_J.frm" : "KNJD184B_H.frm";
        log.info(" form = " + frmName);

        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度";

        final List studentList = getStudentList(db2);

        for (int line = 0; line < studentList.size(); line++) {
            final Student student = (Student) studentList.get(line);

            //成績が無くても、出欠などの出力で出力される必要があるので、チェックはしない。
//            if (student._classCdOrder.size() == 0) {
//                continue;
//            }
            svf.VrSetForm(frmName, 4);

            final String attendno = NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno);

            //学校名
            if ("H".equals(_param._schoolKind)) {
                //ヘッダに出力
                svf.VrsOut("SCHOOL_NAME1", (_param._schoolInfo != null ? _param._schoolInfo._schoolname : ""));
            }
            //フッタに出力
            svf.VrsOut("SCHOOL_NAME2", (_param._schoolInfo != null ? _param._schoolInfo._schoolname : ""));

            //タイトル&専攻名称
            if ("H".equals(_param._schoolKind)) {
                svf.VrsOut("TITLE", nendo + "　　" + "通 知 表");
                svf.VrsOut("MAJOR_NAME", student._majorname);
            } else {
                svf.VrsOut("TITLE", nendo + "　" + "通 知 表");
            }

            //学校長
            String princname = _param._schoolInfo != null ? _param._schoolInfo._principalname : "";
            final String princField = getMS932ByteLength(princname) > 28 ? "2" : "1";
            svf.VrsOut("PRINCIPAL_NAME" + princField, princname);

            //担任
            String teachername = student._staffname != null ? student._staffname : "";
            final String teacherField = getMS932ByteLength(teachername) > 28 ? "2" : "1";
            svf.VrsOut("TEACHER_NAME" + teacherField, teachername);

            //押印
            final String imgFIleName = getImage(student._stampno);
            if(imgFIleName != null) {
                svf.VrsOut("STAMP",imgFIleName);
            }

            //年組番号
            svf.VrsOut("HR_NAME", student._hrName + " " + attendno + "番");

            //生徒情報
            final String nameField = getMS932ByteLength(student._name) > 32 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);

            //その他
            //svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));

            final HREPORTREMARKDAT hreportwk = (HREPORTREMARKDAT) student._hreportremarkMap.get(_param._semester);
            if (hreportwk != null && hreportwk._communication != null) {
                int colsize = 0;
                if ("J".equals(_param._schoolKind)) {
                    colsize = 40;
                } else {
                    colsize = 32;
                }
                final List tokenList = KNJ_EditKinsoku.getTokenList(hreportwk._communication, colsize);
                for (int i = 0; i < tokenList.size(); i++) {
                    svf.VrsOutn("OPINION" , (i + 1), (String) tokenList.get(i));
                }
            }
            if ("J".equals(_param._schoolKind) && hreportwk != null && hreportwk._specialactremark != null) {
                final List spacttokenList = KNJ_EditKinsoku.getTokenList(hreportwk._specialactremark, 40);
                for (int i = 0; i < spacttokenList.size(); i++) {
                    svf.VrsOutn("SPECIAL_ACT" , (i + 1), (String) spacttokenList.get(i));
                }
            }

            //出欠欄
            printAttendData(svf, student);

            //科目欄
            printSubclassRecord(svf, student);

            _hasData = true;
        }
    }

    //出欠欄
    private void printAttendData(final Vrw32alp svf, final Student student) {
        int col = 1;

        for (Iterator itSemesterMst = _param._semesterMstMap.keySet().iterator(); itSemesterMst.hasNext();) {
            final String semester = (String) itSemesterMst.next();
            final SemesterMst semesterMst = (SemesterMst) _param._semesterMstMap.get(semester);
            svf.VrsOutn("SEMESTER2", col, semesterMst._semesterName);

            if (!"9".equals(semester) && Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                col++;
                continue;
            }
            if ("9".equals(semester) && (_param._semesterMstMap.size() - 1) > Integer.parseInt(_param._semester)) {
                col++;
                continue;
            }

            final Map attendMap = (Map) student._attendMap.get( ("9".equals(semester) ? "99" : semester) );
            if (null != attendMap) {
                svf.VrsOutn("LESSON", col, (String) attendMap.get("JYUGYOU"));
                svf.VrsOutn("SUSPEND", col, (String) attendMap.get("SHUTTEI"));
                svf.VrsOutn("PRESENT", col, (String) attendMap.get("SHUSSEKISUBEKI"));
                svf.VrsOutn("ABSENCE", col, (String) attendMap.get("KESSEKI"));
                svf.VrsOutn("ATTEND", col, (String) attendMap.get("SHUSSEKI"));
                svf.VrsOutn("LATE", col, (String) attendMap.get("TIKOKU"));
                svf.VrsOutn("EARLY", col, (String) attendMap.get("SOUTAI"));
            } else {
                svf.VrsOutn("LESSON", col, "");
                svf.VrsOutn("SUSPEND", col, "");
                svf.VrsOutn("PRESENT", col, "");
                svf.VrsOutn("ABSENCE", col, "");
                svf.VrsOutn("ATTEND", col, "");
                svf.VrsOutn("LATE", col, "");
                svf.VrsOutn("EARLY", col, "");
            }
            col++;
        }
    }

//    private List getClassAbbvArray(final Map subclassMap, final String s) {
//    	final List tokenList = new ArrayList();
//    	if (subclassMap.size() == 1) {
//    		tokenList.add(s);
//    		return tokenList;
//    	}
//		if (null != s) {
//			final String[] arr;
//			boolean reverse = false;
//			if (subclassMap.size() >= s.length()) {
//				arr = KNJ_EditEdit.get_token(s, 2, 99);
//			} else {
//				arr = KNJ_EditEdit.get_token(new StringBuffer(s).reverse().toString(), 4, 99);
//				reverse = true;
//			}
//			if (null != arr) {
//				int last = arr.length - 1;
//				for (int i = arr.length - 1; i >= 0; i--) {
//					if (!StringUtils.isBlank(arr[i])) {
//						break;
//					}
//					last = i - 1;
//				}
//				for (int i = 0; i <= last; i++) {
//					tokenList.add(arr[i]);
//				}
//			}
//			if (reverse) {
//				final List reversedTokenList = new ArrayList(tokenList);
//				tokenList.clear();
//				for (final ListIterator lit = reversedTokenList.listIterator(reversedTokenList.size()); lit.hasPrevious();) {
//					final String reversed = (String) lit.previous();
//					tokenList.add(new StringBuffer(StringUtils.defaultString(reversed)).reverse().toString());
//				}
//			}
//		}
//    	return tokenList;
//    }

    //科目欄
    private void printSubclassRecord(final Vrw32alp svf, final Student student) {
        //タイトルを設定
        if ("J".equals(_param._schoolKind)) {
            svf.VrsOut("SEMESTER1_1", "1");
            svf.VrsOut("SEMESTER1_2", "2");
            svf.VrsOut("SEMESTER1_9", "学年");
        } else {
            SemesterMst semesterMst = (SemesterMst) _param._semesterMstMap.get("1");
            svf.VrsOut("SEMESTER1_1", semesterMst._semesterName);
            semesterMst = (SemesterMst) _param._semesterMstMap.get("2");
            svf.VrsOut("SEMESTER1_2", semesterMst._semesterName);
            semesterMst = (SemesterMst) _param._semesterMstMap.get("9");
            svf.VrsOut("SEMESTER1_3", semesterMst._semesterName);

            svf.VrsOut("DIV_NAME1", _param._divName);
            svf.VrsOut("DIV_NAME2", _param._divName);
            svf.VrsOut("DIV_NAME9", _param._divName);
            svf.VrsOut("NOTICE", _param._notice);
        }
        if ("H".equals(_param._schoolKind)) {
            printTotalScore(svf, student);
            printHSubclassRecordDetail(svf, student);
        } else {
            printJSubclassRecordDetail(svf, student);

        }
    }

    private void printTotalScore(final Vrw32alp svf, final Student student) {
        if (student._totalscore != null) {
            if (student._totalscore._score1 != null) {
                svf.VrsOut("TOTAL1", student._totalscore._score1);
            }
            if (student._totalscore._score2 != null) {
                svf.VrsOut("TOTAL2", student._totalscore._score2);
            }
            if (student._totalscore._score9 != null) {
                svf.VrsOut("TOTAL3", student._totalscore._score9);
            }
            if (student._totalscore._avg1 != null) {
                svf.VrsOut("AVERAGE1", rounddown(student._totalscore._avg1, 1)); //小数1桁で切り捨て(※合計が100点超えるため、5桁表示を意識)
            }
            if (student._totalscore._avg2 != null) {
                svf.VrsOut("AVERAGE2", rounddown(student._totalscore._avg2, 1)); //小数1桁で切り捨て(※合計が100点超えるため、5桁表示を意識)
            }
            if (student._totalscore._avg9 != null) {
                svf.VrsOut("AVERAGE3", rounddown(student._totalscore._avg9, 1)); //小数1桁で切り捨て(※合計が100点超えるため、5桁表示を意識)
            }
            if (student._totalscore._rank1 != null) {
                svf.VrsOut("RANK1", student._totalscore._rank1);
            }
            if (student._totalscore._rank2 != null) {
                svf.VrsOut("RANK2", student._totalscore._rank2);
            }
            if (student._totalscore._rank9 != null) {
                svf.VrsOut("RANK3", student._totalscore._rank9);
            }
        }
    }

    private String rounddown(final String val, final int keta) {
        if (val == null || !StringUtils.isNumeric(val)) {
            return val;
        }
        BigDecimal bd1 = new BigDecimal(val);
        return bd1.setScale(keta - 1, BigDecimal.ROUND_DOWN).toString();
    }

    private void printJSubclassRecordDetail(final Vrw32alp svf, final Student student) {
        final int maxLine = 47;
        int outlinecnt = 0;

        final Map classcdViewInfoListMap = new HashMap();
        final List subclasscdList = new ArrayList();
        for (Iterator itview = student._viewMap.keySet().iterator(); itview.hasNext();) {
            final String viewkey = (String)itview.next();
            if (outlinecnt > maxLine) {
                continue;
            }
            final ViewInfo viewobj = (ViewInfo)student._viewMap.get(viewkey);
            if (null != viewobj && null != viewobj._classcd) {
                if (!subclasscdList.contains(viewobj.getSumKeyCdStr())) {
                    subclasscdList.add(viewobj.getSumKeyCdStr());
                }
                if (!classcdViewInfoListMap.containsKey(viewobj.getSumKeyCdStr())) {
                    classcdViewInfoListMap.put(viewobj.getSumKeyCdStr(), new ArrayList());
                }
                final List viewInfoList = (List) classcdViewInfoListMap.get(viewobj.getSumKeyCdStr());
                viewInfoList.add(viewobj);
            }
        }
        printView:
        for (int i = 0; i < subclasscdList.size(); i++) {
            final String subclasscd = (String) subclasscdList.get(i);
            final List viewInfoList = (List) classcdViewInfoListMap.get(subclasscd);

            String subclassname = "";
            Score score = null;
            final Subclass subclass = (Subclass) student._subclassMap.get(subclasscd);
            if (subclass != null) {
                subclassname = subclass._subclassName;
                if (null != subclass._subclassName) {
                    //log.info(" subclass " + subclass._classCd + "-" + subclass._schoolKind + "-" + subclass._curriculumCd + "-" + subclass._subclassCd + " = " + subclass._subclassName);
                    score = (Score) subclass._scoreMap.get(subclasscd);
                }
            }

            for (int j = 0, len = Math.max(subclassname.length(), viewInfoList.size()); j < len; j++) {

                if (j < subclassname.length()) {
                    svf.VrsOut("CLASS_NAME", String.valueOf(subclassname.charAt(j)));
                }

                if (j < viewInfoList.size()) {
                    final ViewInfo viewobj = (ViewInfo) viewInfoList.get(j);
                    int vnamelen = KNJ_EditEdit.getMS932ByteLength(viewobj._viewname);
                    final String viewfield = vnamelen > 36 ? "VIEWNAME2" : "VIEWNAME1";
                    svf.VrsOut(viewfield, viewobj._viewname);
                    svf.VrsOut("VIEW9_2", viewobj._status);
                }

                if (j == len / 2) {
                    if (null != score) {
                        svf.VrsOut("VIEW1", score._score1);
                        svf.VrsOut("VIEW2", score._score2);
                        svf.VrsOut("VIEW9_1", score._score9);
                        if (len % 2 == 0) {
                            final int recordStartY = 1266;
                            final int recordHeight = 60;
                            final String y = String.valueOf(recordStartY + outlinecnt * recordHeight - recordHeight / 2); // センタリング
                            svf.VrAttribute("VIEW1", "Y=" + y);
                            svf.VrAttribute("VIEW2", "Y=" + y);
                            svf.VrAttribute("VIEW9_1", "Y=" + y);
                        }
                    }
                }

                svf.VrsOut("GRP1", String.valueOf(i)); //結合用の処理
                svf.VrsOut("GRP2", String.valueOf(i));
                svf.VrsOut("GRP3", String.valueOf(i));
                svf.VrsOut("GRP4", String.valueOf(i));
                svf.VrEndRecord();

                outlinecnt++;
                if (outlinecnt > maxLine) {
                    break printView;
                }
            }
        }
        //最終行の出力が無い場合は、空行の人の出力のために、空でも１回打っておく。
        if (outlinecnt < maxLine) {
            svf.VrEndRecord();
        }
    }

    private void printHSubclassRecordDetail(final Vrw32alp svf, final Student student) {
        final int maxLine = 18;
        int linecnt = 0;

        for (Iterator itClassMst = student._classCdOrder.iterator(); itClassMst.hasNext();) {
            final String classMstKey = (String) itClassMst.next();
            final ClassMst classMst = (ClassMst) student._classMstMap.get(classMstKey);

            int subClassCnt = 0;
            for (Iterator itSubclass = classMst._subclassKeyOrder.iterator(); itSubclass.hasNext();) {
                final String subclassKey = (String) itSubclass.next();
                final Subclass subclass = (Subclass) student._subclassMap.get(subclassKey);

                if (null == subclass._subclassName) {
                    continue;
                }
                //log.info(" subclass " + subclass._classCd + "-" + subclass._schoolKind + "-" + subclass._curriculumCd + "-" + subclass._subclassCd + " = " + subclass._subclassName);
                String rowid = "";
                if ("92".equals(classMst._classCd)) {
                    rowid = "3";
                } else if (Integer.parseInt(classMst._classCd) >= 90) {
                    rowid = "2";
                } else {
                    rowid = "1";
                }
                if (!"".equals(classMst._className)) {
                    if (!"92".equals(classMst._classCd) && Integer.parseInt(classMst._classCd) < 90) {
                        svf.VrsOut("CLASS_NAME", (String) classMst._className);
                    }
                    svf.VrsOut("SUBCLASS_NAME"+rowid, subclass._subclassName);
                }

                final Score score = (Score) subclass._scoreMap.get(subclassKey);
                if (null != score) {
                    boolean useOffdays = "1".equals(_param._schoolMstSemOffDays);
                    if ("3".equals(rowid)) {
                        if (isDigits(score._sick1) || isDigits(score._notice1) || isDigits(score._nonotice1) || useOffdays && isDigits(score._offdays1)) {
                            final int calcwk1 = useOffdays ? convval(score._sick1) + convval(score._notice1) + convval(score._nonotice1) + convval(score._offdays1) : convval(score._sick1) + convval(score._notice1) + convval(score._nonotice1);
                            svf.VrsOut("KEKKA"+rowid+"_1", String.valueOf(calcwk1));
                        }
                        if (isDigits(score._sick2) || isDigits(score._notice2) || isDigits(score._nonotice2) || useOffdays && isDigits(score._offdays2)) {
                            final int calcwk2 = useOffdays ? convval(score._sick2) + convval(score._notice2) + convval(score._nonotice2) + convval(score._offdays2) : convval(score._sick2) + convval(score._notice2) + convval(score._nonotice2);
                            svf.VrsOut("KEKKA"+rowid+"_2", String.valueOf(calcwk2));
                        }
                        if (isDigits(score._sick9) || isDigits(score._notice9) || isDigits(score._nonotice9) || useOffdays && isDigits(score._offdays9)) {
                            final int calcwk9 = useOffdays ? convval(score._sick9) + convval(score._notice9) + convval(score._nonotice9) + convval(score._offdays9) : convval(score._sick9) + convval(score._notice9) + convval(score._nonotice9);
                            svf.VrsOut("KEKKA"+rowid+"_9", String.valueOf(calcwk9));
                        }
                    } else {
                        svf.VrsOut("CREDIT"+rowid, subclass._credits);
                        svf.VrsOut("DIV"+rowid+"_1", score._score1);
                        if (isDigits(score._sick1) || isDigits(score._notice1) || isDigits(score._nonotice1) || useOffdays && isDigits(score._offdays1)) {
                            final int calcwk1 = useOffdays ? convval(score._sick1) + convval(score._notice1) + convval(score._nonotice1) + convval(score._offdays1) : convval(score._sick1) + convval(score._notice1) + convval(score._nonotice1);
                            svf.VrsOut("KEKKA"+rowid+"_1", String.valueOf(calcwk1));
                        }
                        svf.VrsOut("DIV"+rowid+"_2", score._score2);
                        if (isDigits(score._sick2) || isDigits(score._notice2) || isDigits(score._nonotice2) || useOffdays && isDigits(score._offdays2)) {
                            final int calcwk2 = useOffdays ? convval(score._sick2) + convval(score._notice2) + convval(score._nonotice2) + convval(score._offdays2) : convval(score._sick2) + convval(score._notice2) + convval(score._nonotice2);
                            svf.VrsOut("KEKKA"+rowid+"_2", String.valueOf(calcwk2));
                        }
                        svf.VrsOut("DIV"+rowid+"_9", score._score9);
                        if (isDigits(score._sick9) || isDigits(score._notice9) || isDigits(score._nonotice9) || useOffdays && isDigits(score._offdays9)) {
                            final int calcwk9 = useOffdays ? convval(score._sick9) + convval(score._notice9) + convval(score._nonotice9) + convval(score._offdays9) : convval(score._sick9) + convval(score._notice9) + convval(score._nonotice9);
                            svf.VrsOut("KEKKA"+rowid+"_9", String.valueOf(calcwk9));
                        }
                        if (_param._maxSemesterId.equals(_param._semester)) {
                            svf.VrsOut("GET_CREDIT"+rowid, score._getcredit);
                        }
                    }
                    subClassCnt++;
                    svf.VrEndRecord();
                }
                if (score == null) {
                    subClassCnt++;
                    svf.VrEndRecord();
                }
            }
            linecnt += subClassCnt;
        }
        for (;linecnt < maxLine;linecnt++) {
            svf.VrsOut("BLANK", "0");
            svf.VrEndRecord();
        }

        //空行の人の出力のために、空でも１回打っておく。
        svf.VrEndRecord();
    }

    private static boolean isDigits(final String s) {
        return NumberUtils.isDigits(s);
    }

    private int convval(final String str) {
        return isDigits(str) ? Integer.parseInt(str) : 0;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private List getStudentList(final DB2UDB db2) {
        final List<Student> retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
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
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String hrAbbv = rs.getString("HR_NAMEABBV");
                final String majorname = rs.getString("MAJORNAME");
                final String trCd1 = rs.getString("TR_CD1");
                final String staffname = rs.getString("STAFFNAME");
                final Student student = new Student(schregno, grade, hrClass, attendno, coursecd, majorcd, coursecode, name, hrName, hrAbbv, majorname, trCd1, staffname);
                //評定・欠課情報1(学期別)
                student.setAttendData(db2);
                //評定
                if ("J".equals(_param._schoolKind)) {
                    student.setViewName(db2, student._schregno, student._grade);
                }
                student.setScoreData(db2);
                //評定(合計)
                student.setTotalScoreData(db2);
                //特別活動・所見・備考
                student.setHreportremark(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        // 印影設定
        final Map<String, List<Student>> staffStudentListMap = new HashMap<String, List<Student>>();
        for (final Student student : retList) {
            if (null == student._trCd1) {
                continue;
            }
            if (!staffStudentListMap.containsKey(student._trCd1)) {
                staffStudentListMap.put(student._trCd1, new ArrayList<Student>());
            }
            staffStudentListMap.get(student._trCd1).add(student);
        }
        try {
            for (final Map.Entry<String, List<Student>> e : staffStudentListMap.entrySet()) {
                final String staffcd = e.getKey();
                final List<Student> studentList = e.getValue();

                ps = db2.prepareStatement(getStampSql());

                String stampno = null;
                for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {staffcd})) {

                    stampno = KnjDbUtils.getString(row, "STAMP_NO");
                    if (null == getImage(stampno)) {
                        log.info(" ignore staff " + staffcd + " stampno " + stampno);
                    } else {
                        break;
                    }
                }
                log.info(" staff " + staffcd + ", stampno = " +stampno);

                for (final Student student : studentList) {
                    student._stampno = stampno;
                }
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     REGD.COURSECODE, ");
        stb.append("     BASE.NAME, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     REGH.HR_NAMEABBV, ");
        stb.append("     MM.MAJORNAME, ");
        stb.append("     REGH.TR_CD1, ");
        stb.append("     SM.STAFFNAME ");
//      stb.append("  ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 "); //集計日より転退学日が小さいなら除外対象として1
//      stb.append("       WHEN W4.SCHREGNO IS NOT NULL THEN 1 "); //集計日より転編入日が大きい日付なら除外対象として1
//      stb.append("       WHEN W5.SCHREGNO IS NOT NULL THEN 1 "); //集計日が留学期間中なら除外対象として1
//      stb.append("       ELSE 0 END AS LEAVE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");

        //               転学・退学者で、異動日が出欠集計日より小さい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                  AND W3.GRD_DIV IN('2','3') ");
        stb.append("                  AND W3.GRD_DATE < '" + _param._summaryDate + "' ");
        //               転入・編入者で、異動日が出欠集計日より大きい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                  AND W4.ENT_DIV IN('4','5') ");
        stb.append("                  AND W4.ENT_DATE > '" + _param._summaryDate + "' ");
        //               留学・休学者で、出欠集計日が留学・休学期間内にある場合
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
        stb.append("                  AND '" + _param._summaryDate + "'  BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");

        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST SM ");
        stb.append("       ON REGH.TR_CD1 = SM.STAFFCD ");
        stb.append("     LEFT JOIN MAJOR_MST MM ");
        stb.append("       ON MM.COURSECD = REGD.COURSECD ");
        stb.append("       AND MM.MAJORCD = REGD.MAJORCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN (" + _param._sqlInstate + ") ");
        } else {
            stb.append("     AND REGD.SCHREGNO IN (" + _param._sqlInstate + ") ");
        }
        stb.append("  AND W3.SCHREGNO IS NULL AND W4.SCHREGNO IS NULL AND W5.SCHREGNO IS NULL"); //転学・退学者、転入・編入者、留学・休学者は除外
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    private String getStampSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     STAFFCD, ");
        stb.append("     STAMP_NO ");
        stb.append(" FROM ");
        stb.append("     ATTEST_INKAN_DAT AID ");
        stb.append(" WHERE ");
        stb.append("     STAFFCD = ? ");
        stb.append("     AND FISCALYEAR(VALUE(START_DATE, DATE)) <= '" + _param._ctrlYear + "' ");
        stb.append("     AND STOP_DATE IS NULL ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE(START_DATE, DATE) DESC ");
        return stb.toString();
    }

    /** 生徒クラス */
    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _name;
        final String _hrName;
        final String _hrAbbv;
        final String _majorname;
        final String _trCd1;
        final String _staffname;
        final List _classCdOrder = new ArrayList();
        TotalInfo _totalscore;
        Map _attendMap = new HashMap();
        Map _classMstMap = new TreeMap();
        Map _subclassMap = new HashMap();
        Map _hreportremarkMap = new TreeMap();
        Map _viewMap = new LinkedMap();
        String _stampno;
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String name,
                final String hrName,
                final String hrAbbv,
                final String majorname,
                final String trCd1,
                final String staffname
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _name = name;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _majorname = majorname;
            _trCd1 = trCd1;
            _staffname = staffname;
            _totalscore = null;
        }

        public void setAttendData(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getAttendSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int totalJyugyou = 0;
                int totalShuttei = 0;
                int totalShussekiSubeki = 0;
                int totalKesseki = 0;
                int totalShusseki = 0;
                int totalTochuKekka = 0;
                int totalLate = 0;
                int totalEarly = 0;

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final int lesson = rs.getInt("LESSON");
                    final int offDays = rs.getInt("OFFDAYS");
                    final int suspend = rs.getInt("SUSPEND");
                    final int mourning = rs.getInt("MOURNING");
                    final int abroad = rs.getInt("ABROAD");
                    final int sick = rs.getInt("SICK");
                    final int notice = rs.getInt("NOTICE");
                    final int nonotice = rs.getInt("NONOTICE");
                    final int late = rs.getInt("LATE");
                    final int early = rs.getInt("EARLY");
                    final int tochuKekka = rs.getInt("TOCHU_KEKKA");

                    if (!"9".equals(semester) && Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                        continue;
                    }
                    final Map setMap = new HashMap();
                    final int setJyugyou = "1".equals(_param._schoolMstSemOffDays) ? lesson - abroad : lesson - offDays - abroad;
                    setMap.put("JYUGYOU", String.valueOf(setJyugyou));
                    final int setShuttei = suspend + mourning;
                    setMap.put("SHUTTEI", String.valueOf(setShuttei));
                    final int setShussekiSubeki = setJyugyou - setShuttei;
                    setMap.put("SHUSSEKISUBEKI", String.valueOf(setShussekiSubeki));
                    final int setKesseki = "1".equals(_param._schoolMstSemOffDays) ? sick + notice + nonotice + offDays : sick + notice + nonotice;
                    setMap.put("KESSEKI", String.valueOf(setKesseki));
                    final int setShusseki = setJyugyou - setShuttei - setKesseki;
                    setMap.put("SHUSSEKI", String.valueOf(setShusseki));
                    setMap.put("TOCHUKEKKA", String.valueOf(tochuKekka));
                    setMap.put("TIKOKU", String.valueOf(late));
                    setMap.put("SOUTAI", String.valueOf(early));

                    totalJyugyou += setJyugyou;
                    totalShuttei += setShuttei;
                    totalShussekiSubeki += setShussekiSubeki;
                    totalKesseki += setKesseki;
                    totalShusseki += setShusseki;
                    totalTochuKekka += tochuKekka;
                    totalLate += late;
                    totalEarly += early;

                    _attendMap.put(semester, setMap);
                }
                if (_attendMap.size() > 0) {
                    final Map setMap = new HashMap();
                    setMap.put("JYUGYOU", String.valueOf(totalJyugyou));
                    setMap.put("SHUTTEI", String.valueOf(totalShuttei));
                    setMap.put("SHUSSEKISUBEKI", String.valueOf(totalShussekiSubeki));
                    setMap.put("KESSEKI", String.valueOf(totalKesseki));
                    setMap.put("SHUSSEKI", String.valueOf(totalShusseki));
                    setMap.put("TOCHUKEKKA", String.valueOf(totalTochuKekka));
                    setMap.put("TIKOKU", String.valueOf(totalLate));
                    setMap.put("SOUTAI", String.valueOf(totalEarly));
                    _attendMap.put("99", setMap);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getAttendSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     SUM(VALUE(LESSON, 0)) AS LESSON, ");
            stb.append("     SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ");
            stb.append("     SUM(VALUE(ABSENT, 0)) AS ABSENT, ");
            stb.append("     SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ");
            stb.append("     SUM(VALUE(MOURNING, 0)) AS MOURNING, ");
            stb.append("     SUM(VALUE(ABROAD, 0)) AS ABROAD, ");
            stb.append("     SUM(VALUE(SICK, 0)) AS SICK, ");
            stb.append("     SUM(VALUE(NOTICE, 0)) AS NOTICE, ");
            stb.append("     SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ");
            stb.append("     SUM(VALUE(LATE, 0)) AS LATE, ");
            stb.append("     SUM(VALUE(EARLY, 0)) AS EARLY, ");
            stb.append("     SUM(VALUE(TOCHU_KEKKA, 0)) AS TOCHU_KEKKA     ");
            stb.append(" FROM ");
            stb.append("     V_ATTEND_SEMES_DAT ");
            stb.append(" WHERE ");
            stb.append("     COPYCD = '0' ");
            stb.append("     AND YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregno + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SEMESTER ");

            return stb.toString();
        }

        public void setScoreData(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getScoreSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String credits = rs.getString("CREDITS");
                    final String score1 = rs.getString("SCORE_1");
                    final String score2 = rs.getString("SCORE_2");
                    final String score9 = rs.getString("SCORE_9");
                    final String sick1 = rs.getString("SICK_1");
                    final String sick2 = rs.getString("SICK_2");
                    final String sick9 = rs.getString("SICK_9");
                    final String notice1 = rs.getString("NOTICE_1");
                    final String notice2 = rs.getString("NOTICE_2");
                    final String notice9 = rs.getString("NOTICE_9");
                    final String nonotice1 = rs.getString("NONOTICE_1");
                    final String nonotice2 = rs.getString("NONOTICE_2");
                    final String nonotice9 = rs.getString("NONOTICE_9");
                    final String offdays1 = rs.getString("OFFDAYS_1");
                    final String offdays2 = rs.getString("OFFDAYS_2");
                    final String offdays9 = rs.getString("OFFDAYS_9");
                    final String getcredit = rs.getString("GET_CREDIT");

                    final String subclassKey = classCd + curriculumCd + subclassCd;
                    if ("H".equals(_param._schoolKind) && _param._replaceCombinedAttendSubclassList.contains(classCd + schoolKind + curriculumCd + subclassCd)) {
                    //if (_param._isNoPrintMoto && _param._replaceCombinedAttendSubclassList.contains(subclassKey)) {
                        continue;
                    }

                    ClassMst classMst = null;
                    if (_classMstMap.containsKey(classCd)) {
                        classMst = (ClassMst) _classMstMap.get(classCd);
                    } else {
                        classMst = new ClassMst(classCd, className);
                        _classMstMap.put(classCd, classMst);
                        _classCdOrder.add(classCd);
                    }

                    Subclass subclass = null;
                    if (_subclassMap.containsKey(subclassKey)) {
                        subclass = (Subclass) _subclassMap.get(subclassKey);
                    } else {
                        subclass = new Subclass(classCd, curriculumCd, subclassCd, subclassName, credits);
                        _subclassMap.put(subclassKey, subclass);
                        classMst._subclassKeyOrder.add(subclassKey);
                    }
                    subclass.setScoreMap(subclassKey, score1, score2, score9, sick1, sick2, sick9,
                                                      notice1, notice2, notice9, nonotice1, nonotice2, nonotice9,
                                                      offdays1, offdays2, offdays9, getcredit);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getScoreSql() {
            final StringBuffer stb = new StringBuffer();
            //1.講座リストを作成(SUBCLSDAT)
            //2.期別講座別欠席情報を作成(ATTEND_SUBCLSDAT)
            //3.上記と評定情報を紐づける。
            //※指定期以下を出力するよう、結合条件でSEMESTERを利用して制限をするようにしているので注意。
            stb.append(" WITH SUBCLSDAT AS ( ");
            stb.append(" SELECT ");
            stb.append("  T1.YEAR, ");
            stb.append("  T2.GRADE, ");
            stb.append("  T2.SCHREGNO, ");
            stb.append("  T3.CLASSCD, ");
            stb.append("  T3.SCHOOL_KIND, ");
            stb.append("  T3.SUBCLASSCD, ");
            stb.append("  T3.CURRICULUM_CD, ");
            stb.append("  VALUE(T5.CLASSORDERNAME2, T5.CLASSNAME) AS CLASSNAME, ");
            stb.append("  VALUE(T4.SUBCLASSORDERNAME2, T4.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("  CHAIR_STD_DAT T1 ");
            stb.append("  LEFT JOIN SCHREG_REGD_DAT T2 ");
            stb.append("    ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("  INNER JOIN CHAIR_DAT T3 ");
            stb.append("    ON T3.YEAR = T1.YEAR ");
            stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("  LEFT JOIN SUBCLASS_MST T4 ");
            stb.append("    ON T4.CLASSCD = T3.CLASSCD ");
            stb.append("    AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("    AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("    AND T4.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("  LEFT JOIN CLASS_MST T5 ");
            stb.append("    ON T5.CLASSCD = T3.CLASSCD ");
            stb.append("    AND T5.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("  T2.SCHREGNO IS NOT NULL ");
            stb.append("  AND T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("  AND T2.SEMESTER <= '" + _param._semester + "' ");
            stb.append("  AND T2.SCHREGNO = '" + _schregno + "' ");
            stb.append("  AND T1.APPDATE < '" + _param._summaryDate + "' ");
            stb.append(" GROUP BY ");
            stb.append("  T1.YEAR, ");
            stb.append("  T2.GRADE, ");
            stb.append("  T2.SCHREGNO, ");
            stb.append("  T3.CLASSCD, ");
            stb.append("  T3.SCHOOL_KIND, ");
            stb.append("  T3.SUBCLASSCD, ");
            stb.append("  T3.CURRICULUM_CD, ");
            stb.append("  VALUE(T5.CLASSORDERNAME2, T5.CLASSNAME), ");
            stb.append("  VALUE(T4.SUBCLASSORDERNAME2, T4.SUBCLASSNAME) ");
            stb.append(" ORDER BY ");
            stb.append("  T1.YEAR, ");
            stb.append("  T2.SCHREGNO ");
            stb.append(" ), ATTEND_SUBCLSDAT0 AS ( ");
            stb.append(" SELECT ");
            stb.append("  YEAR, ");
            stb.append("  VALUE(SEMESTER, '9') AS SEMESTER, ");
            stb.append("  SCHREGNO, ");
            stb.append("  CLASSCD, ");
            stb.append("  SCHOOL_KIND, ");
            stb.append("  CURRICULUM_CD, ");
            stb.append("  SUBCLASSCD, ");
            stb.append("  SUM(LESSON) AS LESSON, ");
            stb.append("  SUM(OFFDAYS) AS OFFDAYS, ");
            stb.append("  SUM(ABSENT) AS ABSENT, ");
            stb.append("  SUM(MOURNING) AS MOURNING, ");
            stb.append("  SUM(ABROAD) AS ABROAD, ");
            stb.append("  SUM(SICK) AS SICK, ");
            stb.append("  SUM(NOTICE) AS NOTICE, ");
            stb.append("  SUM(NONOTICE) AS NONOTICE, ");
            stb.append("  SUM(NURSEOFF) AS NURSEOFF, ");
            stb.append("  SUM(LATE) AS LATE, ");
            stb.append("  SUM(EARLY) AS EARLY, ");
            stb.append("  SUM(VIRUS) AS VIRUS, ");
            stb.append("  SUM(KOUDOME) AS KOUDOME ");
            stb.append(" FROM ");
            stb.append("  ATTEND_SUBCLASS_DAT ");
            stb.append(" WHERE ");
            stb.append("  YEAR = '" + _param._ctrlYear + "' ");
            stb.append("  AND CASE WHEN MONTH < '04' THEN '" + (Integer.parseInt(_param._ctrlYear) + 1) + "' ELSE YEAR END || '-' || MONTH || '-' || APPOINTED_DAY <= '" + _param._summaryDate + "' ");
            stb.append("  AND SEMESTER <= '" + _param._semester + "'");
            stb.append("  AND COPYCD = '0' ");
            stb.append(" GROUP BY ");
            stb.append(" GROUPING SETS ( ");
            stb.append("  (YEAR, ");
            stb.append("  SEMESTER, ");
            stb.append("  SCHREGNO, ");
            stb.append("  CLASSCD, ");
            stb.append("  SCHOOL_KIND, ");
            stb.append("  CURRICULUM_CD, ");
            stb.append("  SUBCLASSCD), ");
            stb.append("  (YEAR, ");
            stb.append("  SCHREGNO, ");
            stb.append("  CLASSCD, ");
            stb.append("  SCHOOL_KIND, ");
            stb.append("  CURRICULUM_CD, ");
            stb.append("  SUBCLASSCD) ");
            stb.append("  ) ");

            stb.append(" ), ATTEND_SUBCLSDAT AS ( ");
            stb.append(" SELECT ");
            stb.append("  YEAR, ");
            stb.append("  SEMESTER, ");
            stb.append("  SCHREGNO, ");
            stb.append("  CLASSCD, ");
            stb.append("  SCHOOL_KIND, ");
            stb.append("  CURRICULUM_CD, ");
            stb.append("  SUBCLASSCD, ");
            stb.append("  LESSON, ");
            stb.append("  OFFDAYS, ");
            stb.append("  ABSENT, ");
            stb.append("  MOURNING, ");
            stb.append("  ABROAD, ");
            stb.append("  SICK, ");
            stb.append("  NOTICE, ");
            stb.append("  NONOTICE, ");
            stb.append("  NURSEOFF, ");
            stb.append("  LATE, ");
            stb.append("  EARLY, ");
            stb.append("  VIRUS, ");
            stb.append("  KOUDOME ");
            stb.append(" FROM ");
            stb.append("  ATTEND_SUBCLSDAT0 ");
            stb.append(" WHERE ");
            stb.append("  (CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD) ");
            stb.append("    NOT IN (SELECT COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD ");
            stb.append("            FROM SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append("            WHERE YEAR = '" + _param._ctrlYear + "' ");
            stb.append("           ) ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("  T1.YEAR, ");
            stb.append("  T1.SEMESTER, ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  COMBINED_CLASSCD AS CLASSCD, ");
            stb.append("  COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append("  COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
            stb.append("  COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("  SUM(LESSON) AS LESSON, ");
            stb.append("  SUM(OFFDAYS) AS OFFDAYS, ");
            stb.append("  SUM(ABSENT) AS ABSENT, ");
            stb.append("  SUM(MOURNING) AS MOURNING, ");
            stb.append("  SUM(ABROAD) AS ABROAD, ");
            stb.append("  SUM(SICK) AS SICK, ");
            stb.append("  SUM(NOTICE) AS NOTICE, ");
            stb.append("  SUM(NONOTICE) AS NONOTICE, ");
            stb.append("  SUM(NURSEOFF) AS NURSEOFF, ");
            stb.append("  SUM(LATE) AS LATE, ");
            stb.append("  SUM(EARLY) AS EARLY, ");
            stb.append("  SUM(VIRUS) AS VIRUS, ");
            stb.append("  SUM(KOUDOME) AS KOUDOME ");
            stb.append(" FROM ");
            stb.append("  ATTEND_SUBCLSDAT0 T1 ");
            stb.append("  INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ON T2.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("      AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("      AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("      AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("      AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" GROUP BY ");
            stb.append("  T1.YEAR, ");
            stb.append("  T1.SEMESTER, ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  COMBINED_CLASSCD, ");
            stb.append("  COMBINED_SCHOOL_KIND, ");
            stb.append("  COMBINED_CURRICULUM_CD, ");
            stb.append("  COMBINED_SUBCLASSCD ");
            stb.append(" ), COMBINED_CREDIT AS ( ");
            stb.append(" SELECT ");
            stb.append("   T3.YEAR, ");
            stb.append("   T3.COURSECD, ");
            stb.append("   T3.MAJORCD, ");
            stb.append("   T3.GRADE, ");
            stb.append("   T3.COURSECODE, ");
            stb.append("   T4.COMBINED_CLASSCD       AS CLASSCD, ");
            stb.append("   T4.COMBINED_SCHOOL_KIND   AS SCHOOL_KIND, ");
            stb.append("   T4.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
            stb.append("   T4.COMBINED_SUBCLASSCD    AS SUBCLASSCD, ");
            stb.append("   T4.CALCULATE_CREDIT_FLG, ");
            stb.append("   SUM(T3.CREDITS) AS CREDITS ");
            stb.append(" FROM ");
            stb.append("   SUBCLSDAT T1 ");
            stb.append("   INNER JOIN SCHREG_REGD_DAT T2 ");
            stb.append("           ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("          AND T2.YEAR     = T1.YEAR ");
            stb.append("          AND T2.SEMESTER = '" + _param._semester + "' ");
            stb.append("   INNER JOIN CREDIT_MST T3 ");
            stb.append("           ON T3.YEAR          = T1.YEAR ");
            stb.append("          AND T3.COURSECD      = T2.COURSECD ");
            stb.append("          AND T3.MAJORCD       = T2.MAJORCD ");
            stb.append("          AND T3.GRADE         = T2.GRADE ");
            stb.append("          AND T3.COURSECODE    = T2.COURSECODE ");
            stb.append("          AND T3.CLASSCD       = T1.CLASSCD ");
            stb.append("          AND T3.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
            stb.append("          AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T4 ");
            stb.append("           ON T4.YEAR                 = T3.YEAR ");
            stb.append("          AND T4.ATTEND_CLASSCD       = T3.CLASSCD ");
            stb.append("          AND T4.ATTEND_SCHOOL_KIND   = T3.SCHOOL_KIND ");
            stb.append("          AND T4.ATTEND_CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("          AND T4.ATTEND_SUBCLASSCD    = T3.SUBCLASSCD ");
            stb.append(" GROUP BY ");
            stb.append("   T3.YEAR, ");
            stb.append("   T3.COURSECD, ");
            stb.append("   T3.MAJORCD, ");
            stb.append("   T3.GRADE, ");
            stb.append("   T3.COURSECODE, ");
            stb.append("   T4.COMBINED_CLASSCD, ");
            stb.append("   T4.COMBINED_SCHOOL_KIND, ");
            stb.append("   T4.COMBINED_CURRICULUM_CD, ");
            stb.append("   T4.COMBINED_SUBCLASSCD, ");
            stb.append("   T4.CALCULATE_CREDIT_FLG ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("  T1.YEAR, ");
/*            stb.append("  T1.SEMESTER, "); */
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.CURRICULUM_CD, ");
            stb.append("  T1.SUBCLASSCD, ");
            stb.append("  T1.CLASSNAME, ");
            stb.append("  T1.SUBCLASSNAME, ");
            stb.append("  CASE WHEN T6.CALCULATE_CREDIT_FLG = '2' THEN T6.CREDITS ELSE T5.CREDITS END AS CREDITS, ");
            stb.append("  T2_1.SCORE AS SCORE_1, ");
            stb.append("  T4_1.SICK AS SICK_1, ");
            stb.append("  T4_1.NOTICE AS NOTICE_1, ");
            stb.append("  T4_1.NONOTICE AS NONOTICE_1, ");
            stb.append("  T4_1.OFFDAYS AS OFFDAYS_1, ");
            stb.append("  T2_2.SCORE AS SCORE_2, ");
            stb.append("  T4_2.SICK AS SICK_2, ");
            stb.append("  T4_2.NOTICE AS NOTICE_2, ");
            stb.append("  T4_2.NONOTICE AS NONOTICE_2, ");
            stb.append("  T4_2.OFFDAYS AS OFFDAYS_2, ");
            stb.append("  T2_9.SCORE AS SCORE_9, ");
            stb.append("  T4_9.SICK AS SICK_9, ");
            stb.append("  T4_9.NOTICE AS NOTICE_9, ");
            stb.append("  T4_9.NONOTICE NONOTICE_9, ");
            stb.append("  T4_9.OFFDAYS AS OFFDAYS_9, ");
            stb.append("  T3_9.GET_CREDIT ");
            stb.append(" FROM ");
            stb.append("  SUBCLSDAT T1 ");
            stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T2_1 ");
            stb.append("    ON T2_1.YEAR = T1.YEAR ");
            stb.append("    AND T2_1.SEMESTER = '1' AND T2_1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("    AND T2_1.TESTKINDCD = '99' ");
            stb.append("    AND T2_1.TESTITEMCD = '00' ");
            stb.append("    AND T2_1.SCORE_DIV = '" + _param._scoreDiv + "' ");
            stb.append("    AND T2_1.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T2_1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("    AND T2_1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T2_1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND T2_1.SCHREGNO = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T2_2 ");
            stb.append("    ON T2_2.YEAR = T1.YEAR ");
            stb.append("    AND T2_2.SEMESTER = '2' AND T2_2.SEMESTER <= '" + _param._semester + "' ");
            stb.append("    AND T2_2.TESTKINDCD = '99' ");
            stb.append("    AND T2_2.TESTITEMCD = '00' ");
            stb.append("    AND T2_2.SCORE_DIV = '" + _param._scoreDiv + "' ");
            stb.append("    AND T2_2.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T2_2.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("    AND T2_2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T2_2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND T2_2.SCHREGNO = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T2_9 ");
            stb.append("    ON T2_9.YEAR = T1.YEAR ");
            stb.append("    AND T2_9.SEMESTER = '9' AND '" + _param._maxSemesterId + "' = '" + _param._semester + "' ");
            stb.append("    AND T2_9.TESTKINDCD = '99' ");
            stb.append("    AND T2_9.TESTITEMCD = '00' ");
            stb.append("    AND T2_9.SCORE_DIV = '" + _param._scoreDiv + "' ");
            stb.append("    AND T2_9.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T2_9.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("    AND T2_9.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T2_9.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND T2_9.SCHREGNO = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_SCORE_DAT T3_9 ");
            stb.append("    ON T3_9.YEAR = T1.YEAR ");
            stb.append("    AND T3_9.SEMESTER = '9' ");
            stb.append("    AND T3_9.TESTKINDCD = '99' ");
            stb.append("    AND T3_9.TESTITEMCD = '00' ");
            stb.append("    AND T3_9.SCORE_DIV = '09' ");
            stb.append("    AND T3_9.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T3_9.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("    AND T3_9.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T3_9.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND T3_9.SCHREGNO = T1.SCHREGNO ");
            stb.append("  LEFT JOIN ATTEND_SUBCLSDAT T4_1 ");
            stb.append("    ON T4_1.YEAR = T1.YEAR ");
            stb.append("    AND T4_1.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T4_1.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T4_1.SEMESTER = '1' ");
            stb.append("    AND T4_1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("    AND T4_1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T4_1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN ATTEND_SUBCLSDAT T4_2 ");
            stb.append("    ON T4_2.YEAR = T1.YEAR ");
            stb.append("    AND T4_2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T4_2.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T4_2.SEMESTER = '2' ");
            stb.append("    AND T4_2.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("    AND T4_2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T4_2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN ATTEND_SUBCLSDAT T4_9 ");
            stb.append("    ON T4_9.YEAR = T1.YEAR ");
            stb.append("    AND T4_9.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T4_9.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T4_9.SEMESTER = '9' ");
            stb.append("    AND T4_9.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("    AND T4_9.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T4_9.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN SCHREG_REGD_DAT COURSE_REGD ");
            stb.append("    ON COURSE_REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND COURSE_REGD.YEAR = T1.YEAR ");
            stb.append("    AND COURSE_REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("  LEFT JOIN CREDIT_MST T5 ");
            stb.append("    ON T5.YEAR = COURSE_REGD.YEAR ");
            stb.append("    AND T5.COURSECD = COURSE_REGD.COURSECD ");
            stb.append("    AND T5.MAJORCD = COURSE_REGD.MAJORCD ");
            stb.append("    AND T5.GRADE = COURSE_REGD.GRADE ");
            stb.append("    AND T5.COURSECODE = COURSE_REGD.COURSECODE ");
            stb.append("    AND T5.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T5.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("    AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN COMBINED_CREDIT T6 ");
            stb.append("     ON T6.YEAR          = COURSE_REGD.YEAR ");
            stb.append("    AND T6.COURSECD      = COURSE_REGD.COURSECD ");
            stb.append("    AND T6.MAJORCD       = COURSE_REGD.MAJORCD ");
            stb.append("    AND T6.GRADE         = COURSE_REGD.GRADE ");
            stb.append("    AND T6.COURSECODE    = COURSE_REGD.COURSECODE ");
            stb.append("    AND T6.CLASSCD       = T1.CLASSCD ");
            stb.append("    AND T6.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
            stb.append("    AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND T6.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("   AND T1.SCHREGNO = '"+ _schregno+ "' ");
            stb.append(" ORDER BY ");
            stb.append("   CLASSCD, CURRICULUM_CD, SUBCLASSCD ");

            return stb.toString();
        }

        public void setTotalScoreData(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getTotalScoreSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String score1 = rs.getString("SCORE1");
                    final String score2 = rs.getString("SCORE2");
                    final String score9 = rs.getString("SCORE9");
                    final String avg1 = roundHalfUp(rs.getBigDecimal("AVG1"));
                    final String avg2 = roundHalfUp(rs.getBigDecimal("AVG2"));
                    final String avg9 = roundHalfUp(rs.getBigDecimal("AVG9"));
                    final String rank1 = rs.getString("RANK1");
                    final String rank2 = rs.getString("RANK2");
                    final String rank9 = rs.getString("RANK9");
                    _totalscore = new TotalInfo(score1, score2, score9, avg1, avg2, avg9, rank1, rank2, rank9);
                }

               db2.commit();
            } finally {
               DbUtils.closeQuietly(null, ps, rs);
            }
        }
        private String roundHalfUp(final BigDecimal bd) {
            return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private String getTotalScoreSql() {
            final StringBuffer stb = new StringBuffer();
            String semesterwk;
            if (Integer.parseInt(_param._semester) >= 3) {
                semesterwk = "9";
            } else {
                semesterwk = _param._semester;
            }
            stb.append(" select ");
            stb.append("  T1.YEAR, ");
            stb.append("  T2_1.SCORE AS SCORE1, ");
            stb.append("  T2_1.AVG AS AVG1, ");
            stb.append("  T2_1.CLASS_AVG_RANK AS RANK1, ");
            stb.append("  T2_2.SCORE AS SCORE2, ");
            stb.append("  T2_2.AVG AS AVG2, ");
            stb.append("  T2_2.CLASS_AVG_RANK AS RANK2, ");
            stb.append("  T2_9.SCORE AS SCORE9, ");
            stb.append("  T2_9.AVG AS AVG9, ");
            stb.append("  T2_9.CLASS_AVG_RANK AS RANK9 ");
            stb.append(" from ");
            stb.append(" schreg_regd_dat T1 ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T2_1 ");
            stb.append("   ON T2_1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("   AND T2_1.YEAR = T1.YEAR ");
            stb.append("   AND T2_1.SEMESTER = '1' AND T2_1.SEMESTER <= '" + semesterwk + "'");
            stb.append("   AND T2_1.TESTKINDCD = '99' ");
            stb.append("   AND T2_1.TESTITEMCD = '00' ");
            stb.append("   AND T2_1.SCORE_DIV = '" + _param._scoreDiv + "' ");
            stb.append("   AND T2_1.CLASSCD = '99' ");
            stb.append("   AND T2_1.CURRICULUM_CD = '99' ");
            stb.append("   AND T2_1.SUBCLASSCD = '999999' ");
            stb.append("   AND T2_1.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T2_2 ");
            stb.append("   ON T2_2.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("   AND T2_2.YEAR = T1.YEAR ");
            stb.append("   AND T2_2.SEMESTER = '2' AND T2_2.SEMESTER <= '" + semesterwk + "'");
            stb.append("   AND T2_2.TESTKINDCD = '99' ");
            stb.append("   AND T2_2.TESTITEMCD = '00' ");
            stb.append("   AND T2_2.SCORE_DIV = '" + _param._scoreDiv + "' ");
            stb.append("   AND T2_2.CLASSCD = '99' ");
            stb.append("   AND T2_2.CURRICULUM_CD = '99' ");
            stb.append("   AND T2_2.SUBCLASSCD = '999999' ");
            stb.append("   AND T2_2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T2_9 ");
            stb.append("   ON T2_9.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("   AND T2_9.YEAR = T1.YEAR ");
            stb.append("   AND T2_9.SEMESTER = '9' AND T2_9.SEMESTER <= '" + semesterwk + "'");
            stb.append("   AND T2_9.TESTKINDCD = '99' ");
            stb.append("   AND T2_9.TESTITEMCD = '00' ");
            stb.append("   AND T2_9.SCORE_DIV = '" + _param._scoreDiv + "' ");
            stb.append("   AND T2_9.CLASSCD = '99' ");
            stb.append("   AND T2_9.CURRICULUM_CD = '99' ");
            stb.append("   AND T2_9.SUBCLASSCD = '999999' ");
            stb.append("   AND T2_9.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("  AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("  AND T1.SCHREGNO = '" + _schregno + "' ");

            return stb.toString();
        }

        public void setHreportremark(DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = " SELECT SEMESTER, SPECIALACTREMARK, COMMUNICATION FROM HREPORTREMARK_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND SCHREGNO = '" + _schregno + "' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String communication = rs.getString("COMMUNICATION");
                    final String specialactremark = rs.getString("SPECIALACTREMARK");
                    HREPORTREMARKDAT addwk = new HREPORTREMARKDAT(semester, communication, specialactremark);
                    _hreportremarkMap.put(rs.getString("SEMESTER"), addwk);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void setViewName(final DB2UDB db2, final String schregno, final String grade) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getViewNameSql(schregno, grade);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String curriculumcd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    final String status = rs.getString("NAME1");
                    final String clsname = rs.getString("CLASSNAME");
                    ViewInfo addwk = new ViewInfo(classcd, curriculumcd, subclasscd, viewcd, viewname, status, clsname);
                    _viewMap.put(viewcd, addwk);
                }
                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getViewNameSql(final String schregno, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  T1.VIEWCD, ");
            stb.append("  T1.VIEWNAME, ");
            stb.append("  M1.NAME1, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.CURRICULUM_CD, ");
            stb.append("  T1.SUBCLASSCD, ");
            stb.append("  CM1.CLASSNAME ");
            stb.append(" FROM ");
            stb.append("  JVIEWNAME_GRADE_MST T1 ");
            stb.append("  LEFT JOIN JVIEWSTAT_RECORD_DAT T2 ");
            stb.append("   ON T2.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("   AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("   AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("   AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("   AND T2.VIEWCD = T1.VIEWCD  ");
            stb.append("   AND T2.SCHREGNO = '" + schregno + "' ");
            stb.append("   AND T2.SEMESTER = '9'  ");
            stb.append("  LEFT JOIN NAME_MST M1 ");
            stb.append("   ON M1.NAMECD1 = 'D028' ");
            stb.append("   AND M1.ABBV1 = T2.STATUS ");
            stb.append("  LEFT JOIN CLASS_MST CM1 ");
            stb.append("   ON CM1.CLASSCD = T1.CLASSCD ");
            stb.append("   AND CM1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("  T1.GRADE = '" + grade + "' ");
            stb.append("  AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append(" GROUP BY ");
            stb.append("  T1.VIEWCD, ");
            stb.append("  T1.VIEWNAME, ");
            stb.append("  M1.NAME1, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.CURRICULUM_CD, ");
            stb.append("  T1.SUBCLASSCD, ");
            stb.append("  CM1.CLASSNAME ");
            stb.append(" ORDER BY ");
            stb.append("   CLASSCD, CURRICULUM_CD, SUBCLASSCD, VIEWCD ");
            return stb.toString();
        }
    }

    private class TotalInfo {
        final String _score1;
        final String _score2;
        final String _score9;
        final String _avg1;
        final String _avg2;
        final String _avg9;
        final String _rank1;
        final String _rank2;
        final String _rank9;
        TotalInfo(final String score1, final String score2, final String score9, final String avg1, final String avg2, final String avg9, final String rank1, final String rank2, final String rank9) {
            _score1 = score1;
            _score2 = score2;
            _score9 = score9;
            _avg1 = avg1;
            _avg2 = avg2;
            _avg9 = avg9;
            _rank1 = rank1;
            _rank2 = rank2;
            _rank9 = rank9;
        }
    }
    private class HREPORTREMARKDAT {
        final String _semester;
        final String _communication;
        final String _specialactremark;

        HREPORTREMARKDAT (final String semester, final String communication, final String specialactremark) {
            _semester = semester;
            _communication = communication;
            _specialactremark = specialactremark;
        }
    }

    private class ClassMst {
        final String _classCd;
        final String _className;
        final List _subclassKeyOrder = new ArrayList();
        public ClassMst(
                final String classCd,
                final String className
                ) throws SQLException {
            _classCd = classCd;
            _className = className;
        }

    }

    private class Subclass {
        final String _classCd;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _credits;
        Map _scoreMap = new HashMap();
        public Subclass(
                final String classCd,
                final String curriculumCd,
                final String subclassCd,
                final String subclassName,
                final String credits
                ) throws SQLException {
            _classCd = classCd;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _credits = credits;
        }

        public void setScoreMap(
                final String clskeycd, //classcd+curriculum_cd+subclasscd
                final String score1,
                final String score2,
                final String score9,
                final String sick1,
                final String sick2,
                final String sick9,
                final String notice1,
                final String notice2,
                final String notice9,
                final String nonotice1,
                final String nonotice2,
                final String nonotice9,
                final String offdays1,
                final String offdays2,
                final String offdays9,
                final String getcredit
        ) {
            final Score setScore = new Score(clskeycd, score1, score2, score9, sick1, sick2, sick9, notice1, notice2, notice9,
                                                        nonotice1, nonotice2, nonotice9, offdays1, offdays2, offdays9, getcredit);
            _scoreMap.put(clskeycd, setScore);
        }
    }

    private class ViewInfo {
        final String _viewcd;
        final String _viewname;
        final String _status;
        final String _classcd;
        final String _curriculumcd;
        final String _subclasscd;
        final String _classname;
        ViewInfo(final String classcd, final String curriculumcd, final String subclasscd, final String viewcd, final String viewname, final String status, final String classname) {
            _classcd = classcd;
            _curriculumcd = curriculumcd;
            _subclasscd = subclasscd;
            _viewcd = viewcd;
            _viewname = viewname;
            _status = status;
            _classname = classname;
        }
        String getSumKeyCdStr() {
            return _classcd + _curriculumcd + _subclasscd;
        }
    }

    private class Score {
        final String _clskeycd;
        final String _score1;
        final String _score2;
        final String _score9;
        final String _sick1;
        final String _sick2;
        final String _sick9;
        final String _notice1;
        final String _notice2;
        final String _notice9;
        final String _nonotice1;
        final String _nonotice2;
        final String _nonotice9;
        final String _offdays1;
        final String _offdays2;
        final String _offdays9;
        final String _getcredit;
        public Score(
                final String clskeycd,
                final String score1,
                final String score2,
                final String score9,
                final String sick1,
                final String sick2,
                final String sick9,
                final String notice1,
                final String notice2,
                final String notice9,
                final String nonotice1,
                final String nonotice2,
                final String nonotice9,
                final String offdays1,
                final String offdays2,
                final String offdays9,
                final String getcredit
        ) {
            _clskeycd = clskeycd;
            _score1 = score1;
            _score2 = score2;
            _score9 = score9;
            _sick1 = sick1;
            _sick2 = sick2;
            _sick9 = sick9;
            _notice1 = notice1;
            _notice2 = notice2;
            _notice9 = notice9;
            _nonotice1 = nonotice1;
            _nonotice2 = nonotice2;
            _nonotice9 = nonotice9;
            _offdays1 = offdays1;
            _offdays2 = offdays2;
            _offdays9 = offdays9;
            _getcredit = getcredit;
        }
    }

//    private static class TestItemMst {
//        final String _semester;
//        final String _testCd;
//        final String _testName;
//        int _cnt = 0;
//        int _totalScore = 0;
//        String _assessLow = "";
//        String _assessHigh = "";
//        public TestItemMst(
//                final String semester,
//                final String testCd,
//                final String testName
//        ) throws SQLException {
//            _semester = semester;
//            _testCd = testCd;
//            _testName = testName;
//        }
//
//    }

    private static class SemesterMst {
        final String _semester;
        final String _semesterName;
        public SemesterMst(
                final String semester,
                final String semesterName
        ) throws SQLException {
            _semester = semester;
            _semesterName = semesterName;
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 68805 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class SCHOOLINFO {
        final String _schoolname;
        final String _principalname;
        public SCHOOLINFO(final String schoolname, final String principalname) {
            _schoolname = schoolname;
            _principalname = principalname;
        }
    }

    /** パラメータクラス */
    private static class Param {
        final String _semester;
        final String _disp;
        final String _grade;
        final String _gradeHrClass;
        final String[] _categorySelected;
        final String _sqlInstate;
        final String _summaryDate;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _usePrgSchoolkind;
        final String _selectschoolkind;
        final String _useschoolKindfield;
        final String _paraSchoolkind;
        final String _schoolcd;
        final String _printLogStaffcd;
        final String _semesterName;
        final Map _semesterMstMap;
//        final List _testItemMstList;
        final String _schoolMstSemOffDays;
        final String _schoolKind;
        final SCHOOLINFO _schoolInfo;
        final boolean _hasSchoolMstSchoolKind;
//        boolean _isNoPrintMoto;
        final List _replaceCombinedAttendSubclassList = new ArrayList();
        final String _maxSemesterId;

        final String _docBase;
        final String _folder;
        String _imgFIleName;

        final String _z010Name1;
        final String _scoreDiv;
        final String _divName;
        final String _notice;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _disp = request.getParameter("DISP");
            _grade = request.getParameter("GRADE");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _summaryDate = request.getParameter("SUMMARY_DATE").replace('/', '-');
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectschoolkind = request.getParameter("selectSchoolKind");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _paraSchoolkind = request.getParameter("SCHOOLKIND");
            _schoolcd = request.getParameter("SCHOOLCD");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _semesterMstMap = getSemesterMstMap(db2);
            _maxSemesterId = getMaxSemester();
            _semesterName = ((SemesterMst) _semesterMstMap.get(_semester))._semesterName;
//            _testItemMstList = getTestItemMstList(db2);
            String setInstate = "";
            String sep = "";
            for (int i = 0; i < _categorySelected.length; i++) {
                final String selectVal = _categorySelected[i];
                final String[] setVal = StringUtils.split(selectVal, "-");
                setInstate += sep + "'" + setVal[0] + "'";
                sep = ",";
            }
            _sqlInstate = setInstate;
            String grade = _grade;
            if ("2".equals(_disp)) {
                grade = _gradeHrClass.substring(0, 2);
            }
            _schoolKind = getSchoolKind(db2, grade);
            _schoolMstSemOffDays = getSemOffDays(db2);
            _schoolInfo = getSchoolName(db2);
//            loadNameMstD016(db2);
            setReplaceCombined(db2);
            _hasSchoolMstSchoolKind = setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");

            _docBase = request.getParameter("DOCUMENTROOT");

            _z010Name1 = getNameMstZ010(db2);
            _scoreDiv = "kkotou".equals(_z010Name1) && "H".equals(_schoolKind) ? "09" : "08";
            _divName = "kkotou".equals(_z010Name1) && "H".equals(_schoolKind) ? "評定" : "評価";
            _notice = "kkotou".equals(_z010Name1) && "H".equals(_schoolKind) ? "" : "※注意　評価は10段階評価で、学年評定1,2は単位修得が認められません。";

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            _folder = returnval.val4;            //格納フォルダ
        }

        private String getNameMstZ010(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("NAME1");
                }
                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getMaxSemester() {
            String retstr = "1";
            for (Iterator ite = _semesterMstMap.keySet().iterator();ite.hasNext();) {
                String kstr = (String)ite.next();
                if (Integer.parseInt(retstr) < Integer.parseInt(kstr)) {
                    if (!"9".equals(kstr)) {
                        retstr = kstr;
                    }
                }
            }
            return retstr;
        }
        private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT 1 FROM ");
            if (StringUtils.isBlank(colname)) {
                stb.append("SYSCAT.TABLES");
            } else {
                stb.append("SYSCAT.COLUMNS");
            }
            stb.append(" WHERE TABNAME = '" + tabname + "' ");
            if (!StringUtils.isBlank(colname)) {
                stb.append(" AND COLNAME = '" + colname + "' ");
            }
            boolean hasTableColumn = KnjDbUtils.query(db2, stb.toString()).size() > 0;
            log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
            return hasTableColumn;
        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemester();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSemester() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");
            return stb.toString();
        }

        private Map getSemesterMstMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemesterMstSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semesterName = rs.getString("SEMESTERNAME");
                    final SemesterMst semesterMst = new SemesterMst(semester, semesterName);
                    retMap.put(semester, semesterMst);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private String getSemesterMstSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
//            stb.append("     AND SEMESTER < '9' ");
            return stb.toString();
        }

        private String getSemOffDays(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemOffDays();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SEM_OFFDAYS");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSemOffDays() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VALUE(SEM_OFFDAYS, '0') AS SEM_OFFDAYS ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            if (_hasSchoolMstSchoolKind) {
                stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
            }
            return stb.toString();
        }

//        private List getTestItemMstList(final DB2UDB db2) throws SQLException {
//            final List retList = new ArrayList();
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            try {
//                final String sql = getTestItemMst();
//                log.debug(" sql =" + sql);
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//
//                while (rs.next()) {
//                    final String semester = rs.getString("SEMESTER");
//                    final String testCd = rs.getString("TESTCD");
//                    final String testName = rs.getString("TESTITEMNAME");
//
//                    final TestItemMst testItemMst = new TestItemMst(semester, testCd, testName);
//                    retList.add(testItemMst);
//                }
//
//                db2.commit();
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//            return retList;
//        }

//        private String getTestItemMst() {
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     TESTITEM.SEMESTER, ");
//            stb.append("     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV AS TESTCD, ");
//            stb.append("     TESTITEM.TESTITEMNAME ");
//            stb.append(" FROM ");
//            stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV TESTITEM ");
//            stb.append(" WHERE ");
//            stb.append("     TESTITEM.YEAR = '" + _ctrlYear + "' ");
//            stb.append("     AND TESTITEM.SCORE_DIV = '01' ");
//            stb.append(" ORDER BY ");
//            stb.append("     TESTITEM.SEMESTER, ");
//            stb.append("     TESTCD ");
//            return stb.toString();
//        }

        private String getSchoolKind(final DB2UDB db2, final String grade) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSchoolKind(grade);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SCHOOL_KIND");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSchoolKind(final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GDAT.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT GDAT ");
            stb.append(" WHERE ");
            stb.append("     GDAT.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND GDAT.GRADE = '" + grade + "' ");
            return stb.toString();
        }

        private SCHOOLINFO getSchoolName(final DB2UDB db2) throws SQLException {
            SCHOOLINFO retclswk = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSchoolNameSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String schname = rs.getString("SCHOOL_NAME");
                    final String princname = rs.getString("PRINCIPAL_NAME");
                    retclswk = new SCHOOLINFO(schname, princname);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retclswk;
        }

        private String getSchoolNameSql() {
            final StringBuffer stb = new StringBuffer();
            final String certifKind = "J".equals(_schoolKind) ? "103" : "104";
            stb.append(" SELECT ");
            stb.append("     SCHOOL_NAME, ");
            stb.append("     PRINCIPAL_NAME ");
            stb.append(" FROM ");
            stb.append("     CERTIF_SCHOOL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND CERTIF_KINDCD = '" + certifKind + "' ");

            return stb.toString();
        }


//        private void loadNameMstD016(final DB2UDB db2) {
//            _isNoPrintMoto = false;
//            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'D" + _schoolKind + "16' AND NAMECD2 = '01' ";
//            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
//            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
//            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
//        }

        private void setReplaceCombined(final DB2UDB db2) {
            final String sql = "SELECT ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";
            final List rowList = KnjDbUtils.query(db2, sql);
            _replaceCombinedAttendSubclassList.addAll(KnjDbUtils.getColumnDataList(rowList, "ATTEND_SUBCLASSCD"));
        }

    }

    public String getImage(String fileName) {
        if (null == fileName) {
            return null;
        }

        String extension = "bmp";
        String folder2 = "stamp";

        String image_pass = _param._docBase + "/" + _param._folder + "/" + folder2 + "/";   //イメージパス
        String imgFIleName = image_pass + fileName + "." + extension;
        log.debug(">>>ドキュメントフルパス=" + imgFIleName);

        File f1 = new File(imgFIleName);

        if (!f1.exists()) {
            log.debug(">>>イメージファイルがありません。：" + imgFIleName);
            return null;
        }

        return imgFIleName;
    }
}

// eof

