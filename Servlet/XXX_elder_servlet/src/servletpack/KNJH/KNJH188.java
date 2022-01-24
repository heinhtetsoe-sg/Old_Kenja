// kanji=漢字
/*
 * $Id: 0f8d38de433acb3900144b07d038d5a451dcad33 $
 *
 */
package servletpack.KNJH;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

/*
 *  学校教育システム 賢者 [事務管理] 預かり保育名簿
 */

public class KNJH188 {

    private static final Log log = LogFactory.getLog(KNJH188.class);

    private String PRGID_KNJH189 = "KNJH189";

    private Param _param;
    private boolean _hasdata = false;
    
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        
        log.debug(" $Id: 0f8d38de433acb3900144b07d038d5a451dcad33 $ ");
        KNJServletUtils.debugParam(request, log);

        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
            
            // ＤＢ接続
            DB2UDB db2 = null;
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
                db2.open();
            } catch (Exception ex) {
                log.error("db2 open error!", ex);
                return;
            }
            _param = new Param(request, db2);
            
            // 印刷処理
            printMain(db2, svf);
            
        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            // 終了処理
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = PRGID_KNJH189.equals(_param._prgid) ? "KNJH189.frm" : "KNJH188.frm";
        final String title = _param._dateSlash + "の預かり保育" + (PRGID_KNJH189.equals(_param._prgid) ? "" : "（幼稚園用）");
        final int maxLine = 25;
        final Map fareMap = new TreeMap();
        
        final List childCareCourseList = getCourseList(ChildCare.getChildCareList(db2, _param));
        
        for (int crsi = 0; crsi < childCareCourseList.size(); crsi++) {
            final List childCareInCourseList = (List) childCareCourseList.get(crsi);
            
            final List pageList = getPageList(childCareInCourseList, maxLine);
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List childCareList = (List) pageList.get(pi);
                
                svf.VrSetForm(form, 1);
                
                svf.VrsOut("TITLE", title); // タイトル
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 作成日
                
                for (int i = 0; i < childCareList.size(); i++) {
                    final ChildCare childCare = (ChildCare) childCareList.get(i);
                    final int line = i + 1;
                    svf.VrsOut("COURSE", childCare._coursecodename); // 在籍コース
                    svf.VrsOutn("SCHREG_NO", line, childCare._schregno); // 学籍番号
                    svf.VrsOutn("GRADE", line, NumberUtils.isDigits(childCare._grade) ? String.valueOf(Integer.parseInt(childCare._grade)) : null); // 学年
                    svf.VrsOutn("HR_NAME" + (getMS932ByteLength(childCare._hrName) > 10 ? "2" : "1"), line, childCare._hrName); // クラス名
                    
                    svf.VrsOutn("NAME" + (getMS932ByteLength(childCare._nameKana) > 20 ? "2" : "1"), line, childCare._nameKana); // なまえ
                    svf.VrsOutn("NAME" + (getMS932ByteLength(childCare._name) > 20 ? "4" : "3"), line, childCare._name); // なまえ
                    
                    svf.VrsOutn("BUS_NAME", line, childCare._busName); // バス名称

                    if (PRGID_KNJH189.equals(_param._prgid)) {

                        svf.VrsOutn("ADDR", line, getShikuTyouson(childCare._addr1, childCare._prefCity)); // 市区町村
                        //svf.VrsOutn("PICKUP_TIME", line, null); // お迎え時間
                        if (NumberUtils.isDigits(childCare._fare)) {
                            svf.VrsOutn("REMARK", line, childCare._fare + "円"); // 備考
                            getMappedList(fareMap, Integer.valueOf(childCare._fare)).add(childCare._schregno);
                        }

                    } else {
                        final String[] remarkToken = KNJ_EditEdit.get_token(childCare._careRemark1.toString(), 50, 4);
                        if (null != remarkToken) {
                            for (int j = 0; j < remarkToken.length; j++) {
                                svf.VrsOutn("HEALTH" + String.valueOf(j + 1), line, remarkToken[j]); // 保健特記
                            }
                        }
                    }
                    if (null != childCare._pickUp) {
                        final String[] tokens = KNJ_EditEdit.get_token(childCare._pickUp, 14, 4);
                        if (null != tokens) {
                            for (int j = 0; j < tokens.length; j++) {
                                svf.VrsOutn("PICKUP_TIME" + String.valueOf(j + 1), line, tokens[j]); // お迎え
                            }
                        }
                    }
                    if (null != childCare._remark) {
                        final String[] tokens = KNJ_EditEdit.get_token(childCare._remark, 14, 4);
                        if (null != tokens) {
                            for (int j = 0; j < tokens.length; j++) {
                                svf.VrsOutn("REMARK" + String.valueOf(j + 1), line, tokens[j]); // 備考
                            }
                        }
                    }
                }
                _hasdata = true;
                
                if (PRGID_KNJH189.equals(_param._prgid) && pi == pageList.size() - 1) {
                    svf.VrsOut("MONEY", getFooterFareText(fareMap)); // 金額
                }
                
                svf.VrEndPage();
            }
        }
    }

    private List getCourseList(final List childCareListAll) {
        final List rtn = new ArrayList();
        List current = null;
        ChildCare last = null;
        for (final Iterator it = childCareListAll.iterator(); it.hasNext();) {
            final ChildCare cc = (ChildCare) it.next();
            if (null == last || !StringUtils.defaultString(last._coursecode).equals(cc._coursecode)) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(cc);
            last = cc;
        }
        return rtn;
    }

    private static String getFooterFareText(final Map fareMap) {
        final StringBuffer stb = new StringBuffer();
        String comma = "";
        int totalFair = 0;
        int totalCount = 0;
        for (final Iterator it = fareMap.keySet().iterator(); it.hasNext();) {
            final Integer fair = (Integer) it.next();
            final List list = (List) fareMap.get(fair);
            
            final int count = list.size();
            stb.append(comma).append(fair + "円(" + count + "人)");
            comma = "、";
            
            totalFair += fair.intValue() * count;
            totalCount += count;
        }
        
        stb.append(comma).append("合計 " + totalFair + "円(" + totalCount + "人)");
        return stb.toString();
    }
    
    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static String getShikuTyouson(final String addr1, final String prefCity) {
        if (null != addr1 && null != prefCity && 0 == addr1.indexOf(prefCity)) {
            return prefCity;
        }
        return addr1;
    }

    private static List getPageList(final List list, final int cnt) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= cnt) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
            }
        }
        return rtn;
    }
    
    private static class ChildCare {
        final String _coursecode;
        final String _careDate;
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _hrName;
        final String _name;
        final String _nameKana;
        final String _coursecodename;
        final String _busName;
        final StringBuffer _careRemark1 = new StringBuffer();
        final String _fare;
        final String _addr1;
        final String _prefCity;
        final String _pickUp;
        final String _remark;

        ChildCare(
            final String coursecode,
            final String careDate,
            final String schregno,
            final String grade,
            final String hrClass,
            final String attendno,
            final String hrName,
            final String name,
            final String nameKana,
            final String coursecodename,
            final String busName,
            final String fare,
            final String addr1,
            final String prefCity,
            final String pickUp,
            final String remark
        ) {
            _coursecode = coursecode;
            _careDate = careDate;
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _hrName = hrName;
            _name = name;
            _nameKana = nameKana;
            _coursecodename = coursecodename;
            _busName = busName;
            _fare = fare;
            _addr1 = addr1;
            _prefCity = prefCity;
            _pickUp = pickUp;
            _remark = remark;
        }

        public static List getChildCareList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map m = new HashMap();
                final String sql = sql(param);
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (!m.containsKey(schregno)) {
                        final String coursecode = rs.getString("COURSECODE");
                        final String careDate = rs.getString("CARE_DATE");
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String attendno = rs.getString("ATTENDNO");
                        final String hrName = rs.getString("HR_NAME");
                        final String name = rs.getString("NAME");
                        final String nameKana = rs.getString("NAME_KANA");
                        final String coursecodename = rs.getString("COURSECODENAME");
                        final String busName = rs.getString("BUS_NAME");
                        final String fare = rs.getString("FARE");
                        final String addr1 = rs.getString("ADDR1");
                        final String prefCity = rs.getString("PREF_CITY");
                        final String pickUp = rs.getString("PICK_UP");
                        final String remark = rs.getString("REMARK");
                        final ChildCare childcare = new ChildCare(coursecode, careDate, schregno, grade, hrClass, attendno, hrName, name, nameKana, coursecodename, busName, fare, addr1, prefCity, pickUp, remark);
                        list.add(childcare);
                        m.put(schregno, childcare);
                    }
                    if (null != rs.getString("CARE_REMARK1")) {
                        final ChildCare childcare = (ChildCare) m.get(schregno);
                        if (childcare._careRemark1.length() != 0) {
                            childcare._careRemark1.append("\n");
                        }
                        childcare._careRemark1.append(rs.getString("CARE_REMARK1"));
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CARE_DATE, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.PICK_UP, ");
            stb.append("     T1.REMARK, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     T3.COURSECODENAME, ");
            stb.append("     ENVR.HOWTOCOMMUTECD, ");
            stb.append("     ENVR.FLG_2, ");
            stb.append("     CASE WHEN ENVR.HOWTOCOMMUTECD = '1' AND ENVR.FLG_2 = '3' THEN BUSYM.BUS_NAME END AS BUS_NAME, ");
            stb.append("     MEDC.CARE_REMARK1, ");
            stb.append("     T5.FARE, ");
            stb.append("     T_ADDR.ADDR1, ");
            stb.append("     TZIP.PREF || TZIP.CITY AS PREF_CITY ");
            stb.append(" FROM CHILDCARE_DAT T1 ");
            stb.append(" INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER <> '9' ");
            stb.append("     AND T1.CARE_DATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND REGD.YEAR = T1.YEAR ");
            stb.append("     AND REGD.SEMESTER = T2.SEMESTER ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN COURSECODE_MST T3 ON T3.COURSECODE = REGD.COURSECODE ");
            stb.append(" LEFT JOIN SCHREG_ENVIR_DAT ENVR ON ENVR.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN CHILDCARE_BUS_YMST BUSYM ON BUSYM.YEAR = T1.YEAR ");
            stb.append("     AND BUSYM.COURSE_CD = ENVR.ROSEN_2 ");
            stb.append(" LEFT JOIN MEDEXAM_CARE_DAT MEDC ON MEDC.YEAR = T1.YEAR ");
            stb.append("     AND MEDC.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND (MEDC.CARE_DIV = '01' AND MEDC.CARE_KIND = '02' AND MEDC.CARE_ITEM = '04' AND MEDC.CARE_SEQ = '00' ");
            stb.append("       OR MEDC.CARE_DIV = '02' AND MEDC.CARE_KIND = '02' AND MEDC.CARE_ITEM = '04' AND MEDC.CARE_SEQ = '00' ");
            stb.append("       OR MEDC.CARE_DIV = '03' AND MEDC.CARE_KIND = '02' AND MEDC.CARE_ITEM = '03' AND MEDC.CARE_SEQ = '00' ");
            stb.append("       OR MEDC.CARE_DIV = '04' AND MEDC.CARE_KIND = '02' AND MEDC.CARE_ITEM = '05' AND MEDC.CARE_SEQ = '00' ");
            stb.append("       OR MEDC.CARE_DIV = '06' AND MEDC.CARE_KIND = '02' AND MEDC.CARE_ITEM = '02' AND MEDC.CARE_SEQ = '00' ");
            stb.append("         ) ");
            stb.append(" LEFT JOIN CHILDCARE_FARE_MST T5 ON T5.FARE_CD = T1.FARE_CD ");
            stb.append(" LEFT JOIN SCHREG_ADDRESS_DAT T_ADDR ON T_ADDR.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T1.CARE_DATE BETWEEN T_ADDR.ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') ");
            stb.append(" LEFT JOIN ZIPCD_MST TZIP ON TZIP.NEW_ZIPCD = T_ADDR.ZIPCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.CARE_DATE = '" + param._date + "' ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     MEDC.CARE_DIV ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _dateSlash;
        final String _loginDate;
        final String _prgid;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester  = request.getParameter("SEMESTER");
            _dateSlash = request.getParameter("DATE");
            _date = _dateSlash.replace('/', '-');
            _loginDate = null == request.getParameter("LOGIN_DATE") ? null : request.getParameter("LOGIN_DATE").replace('/', '-');
        }
    }
}
