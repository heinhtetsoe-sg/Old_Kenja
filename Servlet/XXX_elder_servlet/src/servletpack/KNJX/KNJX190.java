// kanji=漢字
/*
 * $Id: 495c034bd7c2a7116788f914c94147ef8daa80ca $
 *
 * 作成日: 2011/03/03 11:12:49 - JST
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 495c034bd7c2a7116788f914c94147ef8daa80ca $
 */
public class KNJX190 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX190.class");

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
        retList.add("学年");
        retList.add("クラス");
        retList.add("出席番号");
        retList.add("氏名");
        retList.add("※年度");
        retList.add("※学籍番号");
        retList.add("※年次");
        retList.add("出欠記録備考");
        retList.add("特別活動記録");
        retList.add("指導上参考");
        retList.add("学習／行動の特技・特徴等");
        retList.add("部活動・資格取得等");
        retList.add("その他");
        if ("1".equals(_param._tyousasyoSougouHyoukaNentani)) {
            retList.add("活動内容");
            retList.add("評価");
        }
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME",
                "YEAR",
                "SCHREGNO",
                "ANNUAL",
                "ATTENDREC_REMARK",
                "SPECIALACTREC",
                "TRAIN_REF",
                "TRAIN_REF1",
                "TRAIN_REF2",
                "TRAIN_REF3",};

        final String[] cols2 = {"GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME",
                "YEAR",
                "SCHREGNO",
                "ANNUAL",
                "ATTENDREC_REMARK",
                "SPECIALACTREC",
                "TRAIN_REF",
                "TRAIN_REF1",
                "TRAIN_REF2",
                "TRAIN_REF3",
                "TOTALSTUDYACT",
                "TOTALSTUDYVAL",};
        return "1".equals(_param._tyousasyoSougouHyoukaNentani) ? cols2 : cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();

        final String table_regd = ("grd".equals(_param._mode)) ? "GRD_REGD_DAT" : "SCHREG_REGD_DAT";
        final String table_base = ("grd".equals(_param._mode)) ? "GRD_BASE_MST" : "SCHREG_BASE_MST";
        final String table_ent  = ("grd".equals(_param._mode)) ? "GRD_HEXAM_ENTREMARK_DAT" : "HEXAM_ENTREMARK_DAT";

        stb.append(" WITH GRADE AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T2.SCHREGNO, ");
        stb.append("         T2.YEAR ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_GDAT T1, ");
        stb.append("         " + table_regd + " T2 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = T2.YEAR AND ");
        stb.append("         T1.GRADE = T2.GRADE AND ");
        stb.append("         T1.SCHOOL_KIND = 'H' ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T3.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.ANNUAL, ");
        stb.append("     T3.ATTENDREC_REMARK, ");
        stb.append("     T3.SPECIALACTREC, ");
        stb.append("     T3.TRAIN_REF, ");
        stb.append("     T3.TRAIN_REF1, ");
        stb.append("     T3.TRAIN_REF2, ");
        stb.append("     T3.TRAIN_REF3 ");
        if ("1".equals(_param._tyousasyoSougouHyoukaNentani)) {
            stb.append("    ,T3.TOTALSTUDYACT ");
            stb.append("    ,T3.TOTALSTUDYVAL ");
        }
        stb.append(" FROM ");
        stb.append("     " + table_regd + " T1 ");
        stb.append("     LEFT JOIN " + table_base + " T2 ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN GRADE T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN " + table_ent + "  T3 ON  T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("                                AND T3.YEAR     = T4.YEAR ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' ");
        stb.append("     AND T3.SCHREGNO = T1.SCHREGNO ");
        if (_param._gradeHrClass != null && !"".equals(_param._gradeHrClass)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' ");
        }
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
        private final String _gradeHrClass;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _mode;
        private final String _tyousasyoSougouHyoukaNentani;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _mode = request.getParameter("XLS_MODE");
            _tyousasyoSougouHyoukaNentani = request.getParameter("tyousasyoSougouHyoukaNentani");
        }

    }
}

// eof
