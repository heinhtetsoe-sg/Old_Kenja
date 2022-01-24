// kanji=漢字
/*
 * $Id: 9961bed3d5b3384135a29a48972bbbb942635ea7 $
 *
 * 作成日: 2011/04/01 17:54:47 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJX;

import java.util.ArrayList;
import java.util.List;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 9961bed3d5b3384135a29a48972bbbb942635ea7 $
 */
public class KNJX_C033K extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX_C033K.class");

    private boolean _hasData;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        _param = createParam(_db2, request);

        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        //ヘッダデータ取得
        _headList = getHeadData();

        //出力データ取得
        _dataList = getXlsDataList();

        outPutXls(response, _param._header);
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        if ("1".equals(_param._useCurriculumcd)) {
            retList.add("※教科コード");
            retList.add("※学校種別");
            retList.add("※教育課程コード");
        }
        retList.add("※科目コード");
        retList.add("科目名");
        retList.add("※講座コード");
        retList.add("講座名");
        retList.add("※年度");
        retList.add("※対象月");
        retList.add("※学期");
        retList.add("※学籍番号");
        retList.add("年組");
        retList.add("出席番号");
        retList.add("氏名");
        retList.add("締め日");
        retList.add("※授業時数");
        retList.add("※欠課種別");
        retList.add("欠課数");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols;
        if ("1".equals(_param._useCurriculumcd)) {
            cols = new String[]{
                    "CLASSCD",
                    "SCHOOL_KIND",
                    "CURRICULUM_CD",
                    "SUBCLASSCD",
                    "SUBCLASSNAME",
                    "CHAIRCD",
                    "CHAIRNAME",
                    "YEAR",
                    "MONTH",
                    "SEMESTER",
                    "SCHREGNO",
                    "HR_NAME",
                    "ATTENDNO",
                    "NAME",
                    "APPOINTED_DAY",
                    "LESSON",
                    "SICK_DIV",
                    "SICK_DATA",};
        } else {
            cols = new String[]{
                    "SUBCLASSCD",
                    "SUBCLASSNAME",
                    "CHAIRCD",
                    "CHAIRNAME",
                    "YEAR",
                    "MONTH",
                    "SEMESTER",
                    "SCHREGNO",
                    "HR_NAME",
                    "ATTENDNO",
                    "NAME",
                    "APPOINTED_DAY",
                    "LESSON",
                    "SICK_DIV",
                    "SICK_DATA",};
        }
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHINFO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEMESTER, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         S2.HR_NAME, ");
        stb.append("         S1.NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST S1 ON T1.SCHREGNO = S1.SCHREGNO  ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT S2 ON T1.YEAR = S2.YEAR ");
        stb.append("                                  AND T1.SEMESTER = S2.SEMESTER ");
        stb.append("                                  AND T1.GRADE = S2.GRADE ");
        stb.append("                                  AND T1.HR_CLASS = S2.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.SCHOOL_KIND, ");
            stb.append("     T3.CURRICULUM_CD, ");
        }
        stb.append("     T3.SUBCLASSCD, ");
        stb.append("     S1.SUBCLASSNAME, ");
        stb.append("     T2.CHAIRCD, ");
        stb.append("     T3.CHAIRNAME, ");
        stb.append("     T1.YEAR, ");
        stb.append("     '" + _param._attendMonth + "' AS MONTH, ");
        stb.append("     '" + _param._attendSem + "' AS SEMESTER, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     S4.APPOINTED_DAY, ");
        stb.append("     S3.LESSON, ");
        stb.append("     '" + _param._sick + "' AS SICK_DIV, ");
        stb.append("     S3." + _param.getSickField() + " AS SICK_DATA ");
        stb.append(" FROM ");
        stb.append("     SCHINFO T1 ");
        stb.append("     INNER JOIN CHAIR_STD_DAT T2 ");
        stb.append("          ON T2.YEAR     = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN CHAIR_DAT T3 ");
        stb.append("          ON T3.YEAR     = T1.YEAR ");
        stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T3.CHAIRCD  = T2.CHAIRCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST S1 ON T3.SUBCLASSCD = S1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T3.CLASSCD  = S1.CLASSCD ");
            stb.append("         AND T3.SCHOOL_KIND  = S1.SCHOOL_KIND ");
            stb.append("         AND T3.CURRICULUM_CD  = S1.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN ATTEND_SUBCLASS_DAT S3 ");
        stb.append("          ON S3.YEAR       = T1.YEAR ");
        stb.append("         AND S3.MONTH      = '" + _param._attendMonth + "' ");
        stb.append("         AND S3.SEMESTER   = '" + _param._attendSem + "' ");
        stb.append("         AND S3.SCHREGNO   = T1.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND S3.CLASSCD  = T3.CLASSCD ");
            stb.append("         AND S3.SCHOOL_KIND  = T3.SCHOOL_KIND ");
            stb.append("         AND S3.CURRICULUM_CD  = T3.CURRICULUM_CD ");
        }
        stb.append("         AND S3.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("     LEFT JOIN APPOINTED_DAY_MST S4 ");
        stb.append("          ON S4.YEAR      = T1.YEAR ");
        stb.append("         AND S4.MONTH     = '" + _param._attendMonth + "' ");
        stb.append("         AND S4.SEMESTER  = '" + _param._attendSem + "' ");
        stb.append(" WHERE ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
        }
        stb.append("     T3.SUBCLASSCD = '" + _param._subclassCd + "' AND ");
        stb.append("     T3.CHAIRCD = '" + _param._chairCd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _yearSem;
        private final String _subclassCd;
        private final String _chairCd;
        private final String _monthSem;
        private final String _attendMonth;
        private final String _attendSem;
        private final String _sick;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _subclassCd = request.getParameter("SUBCLASSCD");
            _chairCd = request.getParameter("CHAIRCD");
            _monthSem = request.getParameter("MONTH");
            final String[] monthSem = StringUtils.split(_monthSem, "-");
            _attendMonth = monthSem[0];
            _attendSem = monthSem[1];
            _sick = request.getParameter("SICK");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

        private String getSickField() {
            if ("1".equals(_sick))  return "ABSENT";
            if ("2".equals(_sick))  return "SUSPEND";
            if ("3".equals(_sick))  return "MOURNING";
            if ("4".equals(_sick))  return "SICK";
            if ("5".equals(_sick))  return "NOTICE";
            if ("6".equals(_sick))  return "NONOTICE";
            if ("15".equals(_sick)) return "LATE";
            if ("16".equals(_sick)) return "EARLY";
            if ("19".equals(_sick)) return "VIRUS";
            if ("25".equals(_sick)) return "KOUDOME";
            return "SICK";
        }

    }
}

// eof
