/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2020/02/20
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD172K {

    private static final Log log = LogFactory.getLog(KNJD172K.class);

    private static final String SEMEALL = "9";

    private static final String DETAIL_DIV_06 = "06";
    private static final String DETAIL_DIV_07 = "07";

    private static final String DETAIL_CODE_01 = "01";
    private static final String DETAIL_CODE_02 = "02";

    private static final String SUBCLASSCD_HYOGEN = "900100";
    private static final String SUBCLASSCD_TANKYU = "900200";

    private static final String SUBCLASS_VIEW_CNT_MAX= "MAX";
    private static final String SUBCLASS_VIEW_CNT_CENTER= "CENTER";

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
        final List studentList = getList(db2);
        Attendance.load(db2, _param, studentList);
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            //通知票
            printSvfMain(db2, svf, student);
            svf.VrEndPage();

            _hasData = true;
        }
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD172K.frm";
        svf.VrSetForm(form , 4);

        //明細部以外を印字
        printTitle(svf, student);

        int grpcd = 1;
        String defSubclass = "";
        String defSubclassName = "";
        int defMaxCnt = 0;
        String val2_1 = "";
        String val2_2 = "";
        String val2_3 = "";
        int line = 1;
        //■観点別評価
        for (Iterator it = student._scoreList.iterator(); it.hasNext();) {
            final ScoreData scoreData = (ScoreData) it.next();
            if(!"".equals(defSubclass) && !defSubclass.equals(scoreData._subclasscd)) {
                //観点サイズを超えた科目名称、5段階評価 を印字
                setOverName(svf, grpcd, defSubclassName, line, val2_1, val2_2, val2_3, defMaxCnt);
                grpcd++;
                line = 1;
            }

            svf.VrsOut("GRPCD1", String.valueOf(grpcd));
            svf.VrsOut("GRPCD2", String.valueOf(grpcd));

            int maxCnt = getSubclassViewCnt(student,scoreData._subclasscd, SUBCLASS_VIEW_CNT_MAX);
            if(maxCnt < 5) maxCnt = 5;
            if(maxCnt >= scoreData._subclassname.length()) {
                svf.VrsOut("CLASS_NAME1_1", safeSubstring(scoreData._subclassname, line, 1)); //教科名
            } else {
                svf.VrsOut("CLASS_NAME1_3", safeSubstring(scoreData._subclassname, line, 1)); //教科名
                svf.VrsOut("CLASS_NAME1_2", safeSubstring(scoreData._subclassname, maxCnt+line, 1)); //教科名
            }

            svf.VrsOut("VIEW1", scoreData._viewname); //評価の観点
            final String viewField = KNJ_EditEdit.getMS932ByteLength(scoreData._grade_viewname) > 54 ? "_2" : "_1";
            svf.VrsOut("VIEW2" + viewField, scoreData._grade_viewname); //観点
            svf.VrsOut("VAL1_1", scoreData._s1_statusname); //1学期
            svf.VrsOut("VAL1_2", scoreData._s2_statusname); //2学期
            svf.VrsOut("VAL1_3", scoreData._s3_statusname); //3学期

            final int viewCnt = getSubclassViewCnt(student,scoreData._subclasscd, SUBCLASS_VIEW_CNT_CENTER);
            if(line == viewCnt) svf.VrsOut("VAL9", scoreData._grade_assess); //学年評定

            svf.VrEndRecord();

            defSubclass = scoreData._subclasscd;
            defSubclassName = scoreData._subclassname;
            defMaxCnt = maxCnt;
            val2_1 = scoreData._sem1_assess;
            val2_2 = scoreData._sem2_assess;
            val2_3 = scoreData._sem3_assess;
            line++;
        }

        //観点サイズを超えた科目名称、5段階評価 を印字
        setOverName(svf, grpcd, defSubclassName, line, val2_1, val2_2, val2_3, defMaxCnt);
    }

    private void printTitle(final Vrw32alp svf, final Student student) {
        //明細部以外を印字

        //ヘッダ
        svf.VrsOut("HR_NAME1", student._hrname + " " + student._attendno); //年組番
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 28 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        //出欠の記録
        printAttend(svf, student);

        //総合的な学習の時間、道徳
        printDetail(svf, student);

        //活動の記録
        printCommitClub(svf, student);

        //身体の記録
        svf.VrsOut("HIGHT",      student._height);    //身長
        svf.VrsOut("WIGHT",      student._weight);    //体重
//        svf.VrsOut("SITHEIGHT",  student._sitheight); //座高
        svf.VrsOut("EYE_R_1", printSvfRegdOutVision(student._r_barevision)); //視力（右）
        svf.VrsOut("EYE_R_2", printSvfRegdOutVision(student._r_vision));     //視力（右・矯正）
        svf.VrsOut("EYE_L_1", printSvfRegdOutVision(student._l_barevision)); //視力（左）
        svf.VrsOut("EYE_L_2", printSvfRegdOutVision(student._l_vision));     //視力（左・矯正）

        //フッター
        svf.VrsOut("DATE", _param._date2); //日付
        svf.VrsOut("SCHOOL_NAME", _param._schoolname1); //学校名
        svf.VrsOut("HR_NAME2", student._hrname); //年組番
        svf.VrsOut("TR_NAME", student._staffname); //担任名
    }

    //総合的な学習の時間、道徳 を印字
    private void printDetail(final Vrw32alp svf, final Student student) {
        //総合的な学習の時間（DIV = 06:総合表現 07:総合探究）
        for (Iterator it = student._remarkDetailMap.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final HreportremarkDetail remarkDetail = (HreportremarkDetail) student._remarkDetailMap.get(key);

            if(DETAIL_CODE_01.equals(remarkDetail._code)) {
                //観点
                continue;
            } else {
                //学習活動・評価
                final String field1 = (DETAIL_DIV_06.equals(remarkDetail._div)) ? "SP_ACT1" : (DETAIL_DIV_07.equals(remarkDetail._div)) ? "SP_ACT2" : "";
                final String field2 = (DETAIL_CODE_02.equals(remarkDetail._code)) ? "_2" : "";
                final int size = (DETAIL_CODE_01.equals(remarkDetail._code)) ? 16 : (DETAIL_CODE_02.equals(remarkDetail._code)) ? 60 : 0;
                final String field = field1 + field2;
                final LinkedList<String> lines = new LinkedList<String>();
                for (final String remark : Arrays.asList(remarkDetail._remark1, remarkDetail._remark2, remarkDetail._remark3)) {
                    if (!StringUtils.isBlank(remark)) {
                        final String[] arr = KNJ_EditEdit.get_token(remark, size, 2);
                        if (null != arr) {
                            for (final String s : arr) {
                                if (!StringUtils.isEmpty(s)) {
                                    lines.add(s);
                                }
                            }
                        }
                    }
                }
                final int kugyoCount = (6 - lines.size()) / 2; // 上下に空行を追加しセンタリングする
                for (int i = 0; i < kugyoCount; i++) {
                    lines.addFirst("");
                    lines.add("");
                }
                for (int i = 0; i < lines.size(); i++) {
                    svf.VrsOutn(field, i + 1, lines.get(i));
                }
            }
        }

        //総合表現 観点
        final int spActSize = 16;
        int line = 1;
        String field = "SP_ACT1_1";
        for (Iterator it = _param._jviewnameListHyogen.iterator(); it.hasNext();) {
            final String jviewname = (String) it.next();
            if(KNJ_EditEdit.getMS932ByteLength(jviewname) > spActSize ) {
                final String[] value = KNJ_EditEdit.get_token(jviewname, spActSize, 2);
                svf.VrsOutn(field, line++, value[0]);
                svf.VrsOutn(field, line++, value[1]);
            } else {
                svf.VrsOutn(field, line++, jviewname);
                svf.VrsOutn(field, line++, "　");
            }
        }

        //総合探究 観点
        line = 1;
        field = "SP_ACT2_1";
        for (Iterator it = _param._jviewnameListTankyu.iterator(); it.hasNext();) {
            final String jviewname = (String) it.next();
            if(KNJ_EditEdit.getMS932ByteLength(jviewname) > spActSize ) {
                final String[] value = KNJ_EditEdit.get_token(jviewname, spActSize, 2);
                svf.VrsOutn(field, line++, value[0]);
                svf.VrsOutn(field, line++, value[1]);
            } else {
                svf.VrsOutn(field, line++, jviewname);
                svf.VrsOutn(field, line++, "　");
            }
        }

        //道徳 1学期
        if(KNJ_EditEdit.getMS932ByteLength(student._moral1) > 60 ) {
            final String[] moral = KNJ_EditEdit.get_token(student._moral1, 60, 2);
            svf.VrsOutn("MORAL1", 1, moral[0]);
            svf.VrsOutn("MORAL1", 2, moral[1]);
        } else {
            svf.VrsOutn("MORAL1", 1, student._moral1);
        }

        //道徳 2学期
        if(_param._semes2Flg) {
            if(KNJ_EditEdit.getMS932ByteLength(student._moral2) > 60 ) {
                final String[] moral = KNJ_EditEdit.get_token(student._moral2, 60, 2);
                svf.VrsOutn("MORAL2", 1, moral[0]);
                svf.VrsOutn("MORAL2", 2, moral[1]);
            } else {
                svf.VrsOutn("MORAL2", 1, student._moral2);
            }
        }

        //道徳 3学期
        if(_param._semes3Flg) {
            if(KNJ_EditEdit.getMS932ByteLength(student._moral3) > 60 ) {
                final String[] moral = KNJ_EditEdit.get_token(student._moral3, 60, 2);
                svf.VrsOutn("MORAL3", 1, moral[0]);
                svf.VrsOutn("MORAL3", 2, moral[1]);
            } else {
                svf.VrsOutn("MORAL3", 1, student._moral3);
            }
        }
    }

    //活動の記録 を印字
    private void printCommitClub(final Vrw32alp svf, final Student student) {
        for (Iterator it = student._commitClubList.iterator(); it.hasNext();) {
            final CommitClub commitClub = (CommitClub) it.next();
            final String field = ("1".equals(commitClub._flg)) ? "1" : ("2".equals(commitClub._flg)) ? "2" : "3";
            svf.VrsOut("ACT" + field, commitClub._name);
            svf.VrEndRecord();
        }
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    /**
    * SVF-OUT 健康診断印刷 視力出力
    */
   private String printSvfRegdOutVision(final String vision) {

       String str = null;
       if (vision != null)
           if (3 < vision.length())
               if (vision.substring(vision.length() - 1, vision.length()).equals("0"))
                   str = vision.substring(0, vision.length() - 1);
               else
                   str = vision;
           else
               str = vision;
       else str = "";

       return str;
   }


   //渡された科目の観点の数を返却（MAX:最大、CENTER:中央）
   private int getSubclassViewCnt(final Student student, final String subClassCd, final String kbn) {
       int rtn = 0;
        for (Iterator it = student._scoreList.iterator(); it.hasNext();) {
            final ScoreData scoreData = (ScoreData) it.next();
            if(subClassCd.equals(scoreData._subclasscd)) rtn++;
        }
        rtn += 1; //5段階評価の行数を加算
        if(SUBCLASS_VIEW_CNT_CENTER.equals(kbn)) {
            //中央値
            rtn = rtn/2 + rtn%2;
        }
        return rtn;
   }

   /**
    * 引数の文字列の指定部分のみを返却
    * @param str 元文字列
    * @param starti 開始位置
    * @param mojisu 取得文字数
    * @return 指定部分の文字列
    */
   private static String safeSubstring(final String str, final int starti, final int mojisu) {
       int start = starti-1;
       if (str.length() <= start) {
           return "";
       }
       if (start + mojisu > str.length()) {
           return str.substring(start);
       }
       return str.substring(start, start + mojisu);
   }


   //観点サイズを超えた科目名称、5段階評価 を印字
   private void setOverName(final Vrw32alp svf, final int grpcd, String subclassname, int line, final String val2_1,final String val2_2, final String val2_3, final int maxCnt) {
       String name = safeSubstring(subclassname, line, 1);
       if(subclassname.length() > maxCnt-1 ) {
           //5段階評価を除いた最大行分繰り返し
           while(maxCnt-1 > line) {
               if("".equals(name)) break;
               svf.VrsOut("GRPCD1", String.valueOf(grpcd));
               svf.VrsOut("GRPCD2", String.valueOf(grpcd));
               if(maxCnt >= subclassname.length()) {
                   svf.VrsOut("CLASS_NAME1_1", safeSubstring(subclassname, line, 1)); //教科名
               } else {
                   svf.VrsOut("CLASS_NAME1_3", safeSubstring(subclassname, line, 1)); //教科名
                   svf.VrsOut("CLASS_NAME1_2", safeSubstring(subclassname, maxCnt+line, 1)); //教科名
               }
               svf.VrEndRecord();
               line++;
               name = safeSubstring(subclassname, line, 1);
           }
       }

       svf.VrsOut("GRPCD3", String.valueOf(grpcd));
       if(maxCnt >= subclassname.length()) {
           svf.VrsOut("CLASS_NAME1", name); //教科名
       } else {
           svf.VrsOut("CLASS_NAME2_1", name); //教科名
           svf.VrsOut("CLASS_NAME2_2", safeSubstring(subclassname, maxCnt+line, 1)); //教科名
       }
       //5段階評価
       svf.VrsOut("VAL2_1", val2_1); //1学期
       svf.VrsOut("VAL2_2", val2_2); //2学期
       svf.VrsOut("VAL2_3", val2_3); //3学期
       svf.VrsOut("DUMMY", "DUMMY"); //表示用
       svf.VrEndRecord();
   }

    // 出欠記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        int lesson = 0;
        int mourning = 0;
        int suspend = 0;
        int mLesson = 0;
        int sick = 0;
        int present = 0;
        int late = 0;
        int early = 0;
        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final int line = getSemeLine(semester);
            final Attendance att = (Attendance) student._attendMap.get(Integer.parseInt(semester));
            if (null != att) {
                if(line == 2 && !_param._semes2Flg) continue;
                if(line == 3 && !_param._semes3Flg) continue;
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson));      // 授業日数
                svf.VrsOutn("MOURNING", line, String.valueOf(att._mourning));  // 忌引出停日数
                svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend));    // 出席停止
                svf.VrsOutn("MUST", line, String.valueOf(att._mLesson));       // 要出席日数
                svf.VrsOutn("NOTICE", line, String.valueOf(att._sick));        // 欠席日数
                svf.VrsOutn("PRESENT", line, String.valueOf(att._present));    // 出席日数
                svf.VrsOutn("LATE", line, String.valueOf(att._late));          // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(att._early));        // 早退
                lesson   += att._lesson;
                mourning += att._mourning;
                suspend  += att._suspend;
                mLesson  += att._mLesson;
                sick     += att._sick;
                present  += att._present;
                late     += att._late;
                early    += att._early;
            }
        }
        svf.VrsOutn("LESSON", 4, String.valueOf(lesson));      // 授業日数
        svf.VrsOutn("MOURNING", 4, String.valueOf(mourning));  // 忌引出停日数
        svf.VrsOutn("SUSPEND", 4, String.valueOf(suspend));    // 出席停止
        svf.VrsOutn("MUST", 4, String.valueOf(mLesson));       // 要出席日数
        svf.VrsOutn("NOTICE", 4, String.valueOf(sick));        // 欠席日数
        svf.VrsOutn("PRESENT", 4, String.valueOf(present));    // 出席日数
        svf.VrsOutn("LATE", 4, String.valueOf(late));          // 遅刻
        svf.VrsOutn("EARLY", 4, String.valueOf(early));        // 早退
    }

    private int getSemeLine(final String semester) {
        final int line;
        if (SEMEALL.equals(semester)) {
            line = 3;
        } else {
            line = Integer.parseInt(semester);
        }
        return line;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._gradename = rs.getString("GRADE_NAME2");
                student._hrname = rs.getString("HR_NAME");
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._course = rs.getString("COURSE");
                student._majorname = rs.getString("MAJORNAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student._guard_zipcd = StringUtils.defaultString(rs.getString("GUARD_ZIPCD"));
                student._guard_addr1 = StringUtils.defaultString(rs.getString("GUARD_ADDR1"));
                student._guard_addr2 = StringUtils.defaultString(rs.getString("GUARD_ADDR2"));
                student._guard_name = StringUtils.defaultString(rs.getString("GUARD_NAME"));
                student._communication = StringUtils.defaultString(rs.getString("COMMUNICATION"));
                student._height = StringUtils.defaultString(rs.getString("HEIGHT"));
                student._weight = StringUtils.defaultString(rs.getString("WEIGHT"));
                student._sitheight = StringUtils.defaultString(rs.getString("SITHEIGHT"));
                student._r_barevision = StringUtils.defaultString(rs.getString("R_BAREVISION"));
                student._l_barevision = StringUtils.defaultString(rs.getString("L_BAREVISION"));
                student._r_vision = StringUtils.defaultString(rs.getString("R_VISION"));
                student._l_vision = StringUtils.defaultString(rs.getString("L_VISION"));
                student._moral1 = StringUtils.defaultString(rs.getString("MORAL1"));
                student._moral2 = StringUtils.defaultString(rs.getString("MORAL2"));
                student._moral3 = StringUtils.defaultString(rs.getString("MORAL3"));

                student._scoreList = student.setScoreList(db2);
                student._commitClubList = student.setCommitClubList(db2);
                student.setHreportremarkDetail(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '"+ _param._loginYear +"' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND T1.SEMESTER = '"+ _param._loginSemester +"' ");
        } else {
            stb.append("     AND T1.SEMESTER = '"+ _param._semester +"' ");
        }
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        if ("1".equals(_param._output)) {
            stb.append("    AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("    AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }

        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '"+ _param._date +"' THEN T2.EDATE ELSE '"+ _param._date +"' END) ");
        stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '"+ _param._date +"' THEN T2.EDATE ELSE '"+ _param._date +"' END)) ) ");
//        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//        stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + parameter._date +"' THEN T2.EDATE ELSE '" + parameter._date +"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        //                      異動者チェック：休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '"+ _param._date +"' THEN T2.EDATE ELSE '"+ _param._date +"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append("    ) ");

        //メイン表
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGDG.GRADE_NAME2 ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,STF2.STAFFNAME AS STAFFNAME2 ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGDG.GRADE ");
        stb.append("            ,REGDG.GRADE_CD ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,MAJOR.MAJORNAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("            ,GADDR.GUARD_ZIPCD ");
        stb.append("            ,GADDR.GUARD_ADDR1 ");
        stb.append("            ,GADDR.GUARD_ADDR2 ");
        stb.append("            ,GUARDIAN.GUARD_NAME ");
        stb.append("            ,R1.COMMUNICATION ");
        stb.append("            ,W3.HEIGHT ");
        stb.append("            ,W3.WEIGHT ");
        stb.append("            ,W3.SITHEIGHT ");
        stb.append("            ,W3.R_BAREVISION ");
        stb.append("            ,W3.L_BAREVISION ");
        stb.append("            ,W3.R_VISION ");
        stb.append("            ,W3.L_VISION ");
        stb.append("            ,W5_1.REMARK1 AS MORAL1 ");
        stb.append("            ,W5_2.REMARK1 AS MORAL2 ");
        stb.append("            ,W5_3.REMARK1 AS MORAL3 ");
        stb.append("     FROM    SCHNO_A REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = REGD.COURSECD ");
        stb.append("                  AND MAJOR.MAJORCD = REGD.MAJORCD ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARDIAN ");
        stb.append("            ON GUARDIAN.SCHREGNO = REGD.SCHREGNO  ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT GROUP BY SCHREGNO) L_GADDR ");
        stb.append("            ON L_GADDR.SCHREGNO = GUARDIAN.SCHREGNO  ");
        stb.append("     LEFT JOIN GUARDIAN_ADDRESS_DAT GADDR ON GADDR.SCHREGNO = L_GADDR.SCHREGNO AND GADDR.ISSUEDATE = L_GADDR.ISSUEDATE ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT R1 ");
        stb.append("            ON R1.YEAR     = REGD.YEAR ");
        stb.append("           AND R1.SEMESTER = REGD.SEMESTER ");
        stb.append("           AND R1.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN MEDEXAM_DET_DAT W3 ON W3.YEAR = '"+ _param._loginYear +"' AND W3.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT W5_1 ON W5_1.YEAR = '"+ _param._loginYear +"' AND W5_1.SEMESTER = '1' AND W5_1.SCHREGNO = REGD.SCHREGNO AND W5_1.DIV = '08' AND W5_1.CODE = '01' "); //1学期
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT W5_2 ON W5_2.YEAR = '"+ _param._loginYear +"' AND W5_2.SEMESTER = '2' AND W5_2.SCHREGNO = REGD.SCHREGNO AND W5_2.DIV = '08' AND W5_2.CODE = '01' "); //2学期
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT W5_3 ON W5_3.YEAR = '"+ _param._loginYear +"' AND W5_3.SEMESTER = '3' AND W5_3.SCHREGNO = REGD.SCHREGNO AND W5_3.DIV = '08' AND W5_3.CODE = '01' "); //3学期
        stb.append("     WHERE   REGD.YEAR = '"+ _param._loginYear +"' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '"+ _param._loginSemester +"' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '"+ _param._semester +"' ");
        }
        if ("1".equals(_param._output)) {
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(0, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
        String _schregno;
        String _name;
        String _schoolKind;
        String _gradename;
        String _hrname;
        String _staffname;
        String _staffname2;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _hrClassName1;
        String _entyear;
        String _guard_zipcd;
        String _guard_addr1;
        String _guard_addr2;
        String _guard_name;
        String _communication;
        String _height;
        String _weight;
        String _sitheight;
        String _r_barevision;
        String _l_barevision;
        String _r_vision;
        String _l_vision;
        String _moral1;
        String _moral2;
        String _moral3;
        List _scoreList;
        List _commitClubList;
        final Map _remarkDetailMap = new TreeMap();
        final Map _attendMap = new TreeMap();

        private List setScoreList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final String sql = perspectiveSql();
            log.debug(" perspectiveSql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String viewcd = StringUtils.defaultString(rs.getString("VIEWCD"));
                    final String viewname = StringUtils.defaultString(rs.getString("VIEWNAME"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String grade_viewcd = StringUtils.defaultString(rs.getString("GRADE_VIEWCD"));
                    final String grade_viewname = StringUtils.defaultString(rs.getString("GRADE_VIEWNAME"));
                    final String s1_status = StringUtils.defaultString(rs.getString("S1_STATUS"));
                    final String s1_statusname = StringUtils.defaultString(rs.getString("S1_STATUSNAME"));
                    final String s2_status = StringUtils.defaultString(rs.getString("S2_STATUS"));
                    final String s2_statusname = StringUtils.defaultString(rs.getString("S2_STATUSNAME"));
                    final String s3_status = StringUtils.defaultString(rs.getString("S3_STATUS"));
                    final String s3_statusname = StringUtils.defaultString(rs.getString("S3_STATUSNAME"));
                    final String sem1_assess = StringUtils.defaultString(rs.getString("SEM1_ASSESS"));
                    final String sem2_assess = StringUtils.defaultString(rs.getString("SEM2_ASSESS"));
                    final String sem3_assess = StringUtils.defaultString(rs.getString("SEM3_ASSESS"));
                    final String grade_assess = StringUtils.defaultString(rs.getString("GRADE_ASSESS"));

                    final ScoreData scoreData = new ScoreData(viewcd, viewname, subclasscd, subclassname,
                                                                 grade_viewcd, grade_viewname, s1_status,s1_statusname,
                                                                 s2_status, s2_statusname, s3_status, s3_statusname,
                                                                 sem1_assess, sem2_assess, sem3_assess, grade_assess);

                    retList.add(scoreData);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return retList;
        }

        private String perspectiveSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   REGD.SCHREGNO, ");
            stb.append("   JS1.VIEWCD, ");
            stb.append("   JS1.VIEWNAME, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("   JS1.CLASSCD || JS1.SCHOOL_KIND || JS1.CURRICULUM_CD || JS1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("   JS1.SUBCLASSCD, ");
            }
            stb.append("   SUB.SUBCLASSNAME, ");
            stb.append("   JG1.STUDYREC_VIEWCD AS GRADE_VIEWCD, ");
            stb.append("   JG1.VIEWNAME AS GRADE_VIEWNAME, ");
            stb.append("   JR1.STATUS  AS S1_STATUS, ");
            stb.append("   JR1NM.NAME1 AS S1_STATUSNAME, ");
            stb.append("   JR2.STATUS  AS S2_STATUS, ");
            stb.append("   JR2NM.NAME1 AS S2_STATUSNAME, ");
            stb.append("   JR3.STATUS  AS S3_STATUS, ");
            stb.append("   JR3NM.NAME1 AS S3_STATUSNAME, ");
            stb.append("   KIN.SEM1_ASSESS, ");
            stb.append("   KIN.SEM2_ASSESS, ");
            stb.append("   KIN.SEM3_ASSESS, ");
            stb.append("   KIN.GRADE_ASSESS ");
            stb.append(" FROM  ");
            stb.append("   JVIEWNAME_SUB_MST JS1 ");
            stb.append("   INNER JOIN SUBCLASS_MST SUB ");
            stb.append("           ON SUB.SUBCLASSCD    = JS1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         AND SUB.CLASSCD       = JS1.CLASSCD ");
                stb.append("         AND SUB.SCHOOL_KIND   = JS1.SCHOOL_KIND ");
                stb.append("         AND SUB.CURRICULUM_CD = JS1.CURRICULUM_CD ");
            }
            stb.append("   INNER JOIN JVIEWNAME_GRADE_MST JG1 ");
            stb.append("           ON JG1.GRADE = '"+ _grade +"' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          AND JG1.STUDYREC_CLASSCD       = JS1.CLASSCD ");
                stb.append("          AND JG1.STUDYREC_SCHOOL_KIND   = JS1.SCHOOL_KIND ");
                stb.append("          AND JG1.STUDYREC_CURRICULUM_CD = JS1.CURRICULUM_CD ");
            }
            stb.append("          AND JG1.STUDYREC_SUBCLASSCD    = JS1.SUBCLASSCD ");
            stb.append("          AND JG1.STUDYREC_VIEWCD        = JS1.VIEWCD ");
            stb.append("   INNER JOIN SCHREG_REGD_DAT REGD ");
            stb.append("           ON REGD.YEAR     = '"+ _param._loginYear +"'  ");
            stb.append("          AND REGD.SEMESTER = '"+ _param._semester +"' ");
            stb.append("          AND REGD.GRADE    = JG1.GRADE ");
            stb.append("          AND REGD.HR_CLASS = '"+ _hrClass +"' ");
            stb.append("          AND REGD.SCHREGNO = '"+ _schregno +"' ");
            stb.append("   LEFT JOIN JVIEWSTAT_RECORD_DAT JR1 ");
            stb.append("          ON JR1.YEAR          = REGD.YEAR ");
            stb.append("         AND JR1.SEMESTER      = '1' ");
            stb.append("         AND JR1.SCHREGNO      = REGD.SCHREGNO ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         AND JR1.CLASSCD       = JG1.CLASSCD ");
                stb.append("         AND JR1.SCHOOL_KIND   = JG1.SCHOOL_KIND ");
                stb.append("         AND JR1.CURRICULUM_CD = JG1.CURRICULUM_CD ");
            }
            stb.append("         AND JR1.SUBCLASSCD    = JG1.SUBCLASSCD ");
            stb.append("         AND JR1.VIEWCD        = JG1.VIEWCD ");
            stb.append("   LEFT JOIN JVIEWSTAT_RECORD_DAT JR2 ");
            stb.append("          ON JR2.YEAR          = REGD.YEAR ");
            stb.append("         AND JR2.SEMESTER      = '2' ");
            stb.append("         AND JR2.SCHREGNO      = REGD.SCHREGNO ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         AND JR2.CLASSCD       = JG1.CLASSCD ");
                stb.append("         AND JR2.SCHOOL_KIND   = JG1.SCHOOL_KIND ");
                stb.append("         AND JR2.CURRICULUM_CD = JG1.CURRICULUM_CD ");
            }
            stb.append("         AND JR2.SUBCLASSCD    = JG1.SUBCLASSCD ");
            stb.append("         AND JR2.VIEWCD        = JG1.VIEWCD ");
            stb.append("   LEFT JOIN JVIEWSTAT_RECORD_DAT JR3 ");
            stb.append("          ON JR3.YEAR          = REGD.YEAR ");
            stb.append("         AND JR3.SEMESTER      = '3' ");
            stb.append("         AND JR3.SCHREGNO      = REGD.SCHREGNO ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         AND JR3.CLASSCD       = JG1.CLASSCD ");
                stb.append("         AND JR3.SCHOOL_KIND   = JG1.SCHOOL_KIND ");
                stb.append("         AND JR3.CURRICULUM_CD = JG1.CURRICULUM_CD ");
            }
            stb.append("         AND JR3.SUBCLASSCD    = JG1.SUBCLASSCD ");
            stb.append("         AND JR3.VIEWCD        = JG1.VIEWCD ");
            stb.append("   LEFT JOIN NAME_MST JR1NM ON JR1NM.NAMECD1 = 'D029' AND JR1NM.ABBV1 = JR1.STATUS ");
            stb.append("   LEFT JOIN NAME_MST JR2NM ON JR2NM.NAMECD1 = 'D029' AND JR2NM.ABBV1 = JR2.STATUS ");
            stb.append("   LEFT JOIN NAME_MST JR3NM ON JR3NM.NAMECD1 = 'D029' AND JR3NM.ABBV1 = JR3.STATUS ");
            stb.append("   LEFT JOIN KIN_RECORD_DAT KIN ");
            stb.append("          ON KIN.YEAR          = REGD.YEAR ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         AND KIN.CLASSCD       = JS1.CLASSCD ");
                stb.append("         AND KIN.SCHOOL_KIND   = JS1.SCHOOL_KIND ");
                stb.append("         AND KIN.CURRICULUM_CD = JS1.CURRICULUM_CD ");
            }
            stb.append("         AND KIN.SUBCLASSCD    = JS1.SUBCLASSCD ");
            stb.append("         AND KIN.SCHREGNO      = REGD.SCHREGNO ");
            stb.append(" ORDER BY  ");
            stb.append("   REGD.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("   JS1.CLASSCD, ");
                stb.append("   JS1.SCHOOL_KIND, ");
                stb.append("   JS1.CURRICULUM_CD, ");
            }
            stb.append("   JS1.SUBCLASSCD, ");
            stb.append("   JS1.VIEWCD, ");
            stb.append("   JG1.VIEWCD ");

            return stb.toString();
        }

        private void setHreportremarkDetail(final DB2UDB db2) {
            final String sql = hreportremarkDetailDatSql();
            log.debug(" hreportremarkDetailDatSql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String div = StringUtils.defaultString(rs.getString("DIV"));
                    final String code = StringUtils.defaultString(rs.getString("CODE"));
                    final String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
                    final String remark2 = StringUtils.defaultString(rs.getString("REMARK2"));
                    final String remark3 = StringUtils.defaultString(rs.getString("REMARK3"));

                    final String key = div + code;
                    if (!_remarkDetailMap.containsKey(key)) {
                        HreportremarkDetail hreportremarkDetail = new HreportremarkDetail(div, code, remark1, remark2, remark3);
                        _remarkDetailMap.put(key, hreportremarkDetail);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }


        private String hreportremarkDetailDatSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("   DT.SCHREGNO, ");
            stb.append("   DT.DIV, ");
            stb.append("   DT.CODE, ");
            stb.append("   DT.REMARK1, ");
            stb.append("   DT.REMARK2, ");
            stb.append("   DT.REMARK3 ");
            stb.append(" FROM  ");
            stb.append("   HREPORTREMARK_DETAIL_DAT DT ");
            stb.append(" WHERE  ");
            stb.append("       DT.YEAR     = '"+ _param._loginYear +"'  ");
            stb.append("   AND DT.SEMESTER = '"+ _param._semester +"' ");
            stb.append("   AND DT.SCHREGNO = '"+ _schregno +"' ");
            stb.append("   AND DT.DIV  IN ('"+DETAIL_DIV_06+"', '"+DETAIL_DIV_07+"') ");
            stb.append("   AND DT.CODE IN ('"+DETAIL_CODE_01+"', '"+DETAIL_CODE_02+"') ");
            stb.append(" ORDER BY  ");
            stb.append("   SCHREGNO, ");
            stb.append("   DIV, ");
            stb.append("   CODE ");

            return stb.toString();
        }

        private List setCommitClubList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final String sql = prestatementCommitAndClub();
            log.debug(" prestatementCommitAndClubSql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String flg = StringUtils.defaultString(rs.getString("FLG"));
                    final String name = StringUtils.defaultString(rs.getString("NAME"));
                    final CommitClub commitClub = new CommitClub(flg,name);
                    retList.add(commitClub);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return retList;
        }

        /**
         *  委員会、クラブ活動
         *  (クラブは異動指定日が属するデータを出力)
         */
        String prestatementCommitAndClub()
        {
            final StringBuffer stb = new StringBuffer();

            try {
                stb.append("WITH COMMITTEE_DAT AS(");
                stb.append("  SELECT  SCHREGNO, COMMITTEE_FLG, MAX(SEQ) AS SEQ ");
                stb.append("  FROM    SCHREG_COMMITTEE_HIST_DAT ");
                stb.append("  WHERE   YEAR    = '"+ _param._loginYear +"' AND ");
                stb.append("          SCHREGNO = '"+ _schregno+"' ");
                stb.append("  GROUP BY SCHREGNO, COMMITTEE_FLG) ");
                stb.append(",CLUB_DAT AS(");
                stb.append("  SELECT  SCHREGNO,CLUBCD ");
                stb.append("  FROM    SCHREG_CLUB_HIST_DAT S1 ");
                stb.append("  WHERE   SCHREGNO  = '"+_schregno+"' ");
                stb.append("          AND SDATE = (SELECT MAX(SDATE) AS SDATE ");
                stb.append("                         FROM SCHREG_CLUB_HIST_DAT ");
                stb.append("                        WHERE SCHREGNO = '"+ _schregno +"' AND ");
                stb.append("                              SDATE <= '"+ _param._date +"' ");
                stb.append("                        GROUP BY SCHREGNO) ");
                stb.append("          AND '"+ _param._date +"' <= value(edate, '"+ _param._date +"') ");
                stb.append(") ");
                //メイン
                stb.append(" SELECT ");
                stb.append("   T3.COMMITTEE_FLG AS FLG, ");
                stb.append("   T4.COMMITTEENAME AS NAME ");
                stb.append(" FROM");
                stb.append("   SCHREG_COMMITTEE_HIST_DAT T3 ");
                stb.append("   INNER JOIN COMMITTEE_MST T4 ON T4.COMMITTEECD = T3.COMMITTEECD ");
                stb.append("                              AND T4.COMMITTEE_FLG = T3.COMMITTEE_FLG ");
                stb.append(" WHERE T3.YEAR =  '"+ _param._loginYear +"' AND ");
                stb.append("       EXISTS(SELECT 'X' FROM COMMITTEE_DAT T1 WHERE T1.SEQ=T3.SEQ AND T1.SCHREGNO = T3.SCHREGNO AND ");
                stb.append("       T1.COMMITTEE_FLG = T3.COMMITTEE_FLG) ");
                stb.append("UNION ");
                stb.append(" SELECT ");
                stb.append("   '3'      AS FLG, ");
                stb.append("   CLUBNAME AS NAME ");
                stb.append(" FROM ");
                stb.append("   CLUB_DAT T1 ");
                stb.append("   INNER JOIN CLUB_MST T2 ON T1.CLUBCD = T2.CLUBCD ");
            } catch (Exception ex) {
                log.error("error! ", ex);
            }
            return stb.toString();
        }
    }

    private static class ScoreData {
        final String _viewcd;
        final String _viewname;
        final String _subclasscd;
        final String _subclassname;
        final String _grade_viewcd;
        final String _grade_viewname;
        final String _s1_status;
        final String _s1_statusname;
        final String _s2_status;
        final String _s2_statusname;
        final String _s3_status;
        final String _s3_statusname;
        final String _sem1_assess;
        final String _sem2_assess;
        final String _sem3_assess;
        final String _grade_assess;


        private ScoreData(
                final String viewcd,
                final String viewname,
                final String subclasscd,
                final String subclassname,
                final String grade_viewcd,
                final String grade_viewname,
                final String s1_status,
                final String s1_statusname,
                final String s2_status,
                final String s2_statusname,
                final String s3_status,
                final String s3_statusname,
                final String sem1_assess,
                final String sem2_assess,
                final String sem3_assess,
                final String grade_assess
        ) {
            _viewcd = viewcd;
            _viewname = viewname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _grade_viewcd = grade_viewcd;
            _grade_viewname = grade_viewname;
            _s1_status = s1_status;
            _s1_statusname = s1_statusname;
            _s2_status = s2_status;
            _s2_statusname = s2_statusname;
            _s3_status = s3_status;
            _s3_statusname = s3_statusname;
            _sem1_assess = sem1_assess;
            _sem2_assess = sem2_assess;
            _sem3_assess = sem3_assess;
            _grade_assess = grade_assess;
        }

    }

    private static class HreportremarkDetail {
        final String _div;
        final String _code;
        final String _remark1;
        final String _remark2;
        final String _remark3;

        private HreportremarkDetail(
                final String div,
                final String code,
                final String remark1,
                final String remark2,
                final String remark3
        ) {
            _div = div;
            _code = code;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
        }
    }


    private static class CommitClub {
        final String _flg;
        final String _name;


        private CommitClub(
                final String flg,
                final String name
        ) {
            _flg = flg;
            _name = name;
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int present,
                final int late,
                final int early,
                final int abroad
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList
        ) {
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._loginYear,
                        SEMEALL,
                        null,
                        param._date,
                        param._attendParamMap
                );
                psAtSeme = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    psAtSeme.setString(1, student._schregno);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }
                        final int semes = rsAtSeme.getInt("SEMESTER");
                        if (semes > Integer.parseInt(param._semester)) {
                            continue;
                        }
                        final Attendance attendance = new Attendance(
                                rsAtSeme.getInt("LESSON"),
                                rsAtSeme.getInt("MLESSON"),
                                rsAtSeme.getInt("SUSPEND") + rsAtSeme.getInt("VIRUS") + rsAtSeme.getInt("KOUDOME"),
                                rsAtSeme.getInt("MOURNING"),
                                rsAtSeme.getInt("SICK"),
                                rsAtSeme.getInt("PRESENT"),
                                rsAtSeme.getInt("LATE"),
                                rsAtSeme.getInt("EARLY"),
                                rsAtSeme.getInt("TRANSFER_DATE")
                        );
                        student._attendMap.put(semes, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Semester)) {
                return 0;
            }
            Semester s = (Semester) o;
            return _semester.compareTo(s._semester);
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75541 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _loginYear;
        final String _semester;
        final String _loginSemester;
        final String _date;
        final String _date2;
        final String _output;
        final String _hrclass;
        final String _grade;
        final String _useCurriculumcd;
        final String[] _categorySelected;

        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        final String _schoolKind;
        final String _schoolname1;
        final String _nendo;

        private String _certifSchoolSchoolName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private Map _semesterMap;

        private final List _jviewnameListHyogen;
        private final List _jviewnameListTankyu;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("YEAR");               //ログイン年度
            _semester = request.getParameter("GAKKI");               //指定学期
            _loginSemester = request.getParameter("LOGIN_SEMESTER"); //ログイン学期
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));    //異動基準日
            _date2 = KNJ_EditDate.h_format_JP(db2, request.getParameter("DATE2")); //処理日
            _output = request.getParameter("OUTPUT");                     //1:個人指定  2:クラス指定
            _hrclass = request.getParameter("GRADE_HR_CLASS");              //学年・組
            if ("1".equals(_output)) {
                _grade = _hrclass.substring(0, 2);
            } else {
                _grade = request.getParameter("GRADE_HR_CLASS");
            }
            _categorySelected = request.getParameterValues("category_selected");
            _useCurriculumcd = request.getParameter("useCurriculumcd"); //教育課程

            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            _schoolKind = getSchoolKind(db2);

            _schoolname1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _loginYear + "' "));
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG");

            _semesterMap = loadSemester(db2);

            _jviewnameListHyogen = getJviewnameList(db2, SUBCLASSCD_HYOGEN);
            _jviewnameListTankyu = getJviewnameList(db2, SUBCLASSCD_TANKYU);
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='"+ _loginYear +"'"
                        + "   AND GRADE='"+ _grade +"'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '"+ _loginYear +"' AND GRADE = '"+ _grade +"' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        //対象科目の観点を取得
        private List getJviewnameList (final DB2UDB db2, final String subclasscd) {
            final List rtnList = new ArrayList();

            final String classcd = subclasscd.substring(0,2);
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS( ");
            stb.append("   SELECT ");
            stb.append("     GRADE, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     MAX(CURRICULUM_CD) AS CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD ");
            stb.append("   FROM JVIEWNAME_GRADE_MST ");
            stb.append("   WHERE GRADE       = '"+ _grade +"' ");
            stb.append("     AND CLASSCD     = '"+ classcd +"' ");
            stb.append("     AND SCHOOL_KIND = '"+ _schoolKind +"' ");
            stb.append("     AND SUBCLASSCD  = '"+ subclasscd +"' ");
            stb.append("   GROUP BY GRADE, CLASSCD, SCHOOL_KIND, SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T2.* ");
            stb.append(" FROM ");
            stb.append("   MAIN T1 ");
            stb.append("   INNER JOIN JVIEWNAME_GRADE_MST T2 ");
            stb.append("           ON T2.GRADE         = T1.GRADE ");
            stb.append("          AND T2.CLASSCD       = T1.CLASSCD ");
            stb.append("          AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("   T2.VIEWCD ");
            final String sql =  stb.toString();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                log.debug(sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnList.add(StringUtils.defaultString(rs.getString("VIEWNAME")));
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnList;
        }

    }

}

// eof
