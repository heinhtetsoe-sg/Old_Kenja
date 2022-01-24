/*
 * $Id: 2c2d1a1255da2c9e24a6a7bc81c5da7572ea2502 $
 *
 * 作成日: 2015/06/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 アンケート設問
 */
public class KNJZ422 {

    private static final Log log = LogFactory.getLog(KNJZ422.class);

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

    private static int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return ret;
    }

    private static List getTokenList(final String source0, final int bytePerLine) {
        if (source0 == null || source0.length() == 0) {
            return Collections.EMPTY_LIST;
        }
        final String source = StringUtils.replace(StringUtils.replace(source0, "\r\n", "\n"), "\r", "\n");
        final List tokenList = new ArrayList();        //分割後の文字列の配列
        int startIndex = 0;                         //文字列の分割開始位置
        int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
        for (int idx = 0; idx < source.length(); idx += 1) {
            //改行マークチェック
            if (source.charAt(idx) == '\n') {
                tokenList.add(source.substring(startIndex, idx));
                byteLengthInLine = 0;
                startIndex = idx + 1;
            } else {
                final int sbytelen = getMS932ByteCount(source.substring(idx, idx + 1));
                byteLengthInLine += sbytelen;
                if (byteLengthInLine > bytePerLine) {
                    tokenList.add(source.substring(startIndex, idx));
                    byteLengthInLine = sbytelen;
                    startIndex = idx;
                }
            }
        }
        if (byteLengthInLine > 0) {
            tokenList.add(source.substring(startIndex));
        }
        return tokenList;
    }

    private static String repeat(final String s, final int count) {
        return StringUtils.repeat(s, count);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        String nengo = "";
        try {
            nengo = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(_param._ctrlDate))[0];
        } catch (Exception e) {
            log.error("exception! date = " + _param._ctrlDate, e);
        }
        final String date = nengo + "   年   月   日";
        final List dataList = QuestionFormatDat.load(db2, _param);
        int line = QuestionFormatDat.maxLine;
        for (int i = 0; i < dataList.size(); i++) {
            final QuestionFormatDat qfd = (QuestionFormatDat) dataList.get(i);
            final List lineList = QuestionFormatDat.toLineList(qfd);
            if (line + lineList.size() > QuestionFormatDat.maxLine) {
                if (_hasData) {
                    svf.VrEndPage();
                }
                svf.VrSetForm("KNJZ422.frm", 1);
                svf.VrsOut("DATE", date);
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　" + _param._questionnairename);
                line = 1;
            }
            for (final Iterator it = lineList.iterator(); it.hasNext();) {
                final String text = (String) it.next();
                svf.VrsOutn("SENTENCE", line, text);
                line += 1;
                _hasData = true;
            }
        }
        if (!_hasData) {
            svf.VrSetForm("KNJZ422.frm", 1);
            svf.VrsOut("DATE", date);
            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　" + _param._questionnairename);
            _hasData = true;
        }
        svf.VrEndPage();
    }

    private static class QuestionFormatDat {
        static final int keta = 110;
        static final int maxLine = 70;
        static final int numKeta = 3;
        
        final String _questionnairecd;
        final int _questionitemNo;
        final String _questionContents;
        final String _answerPattern;
        final int _answerSelectCount;
        
        QuestionFormatDat(
            final String questionnairecd,
            final int questionitemNo,
            final String questionContents,
            final String answerPattern,
            final int answerSelectCount
        ) {
            _questionnairecd = questionnairecd;
            _questionitemNo = questionitemNo;
            _questionContents = questionContents;
            _answerPattern = answerPattern;
            _answerSelectCount = answerSelectCount;
        }
        
        public static List toLineList(final QuestionFormatDat qfd) {
            final String num = String.valueOf(qfd._questionitemNo);

            final List questionLineList = addHead("設問" + num + repeat(" ", numKeta - num.length()) + " ", getTokenList(qfd._questionContents, 60));

            final List list = new ArrayList();
            list.addAll(questionLineList);
            list.addAll(getAnswerLineList(questionLineList, qfd));
            list.add("");
            return list;
        }

        private static List addHead(final String head, final List lineList) {
            lineList.set(0, head + lineList.get(0));
            final String spc = repeat(" ", getMS932ByteCount(head));
            for (int i = 1; i < lineList.size(); i++) {
                lineList.set(i, spc + lineList.get(i));
            }
            return lineList;
        }

        public static List getAnswerLineList(final List questionLineList, final QuestionFormatDat qfd) {
            final LinkedList answerLineList = new LinkedList();
            final String head = "回答" + repeat(" ", numKeta) + " ";
            if ("1".equals(qfd._answerPattern)) {
                answerLineList.add(getSelectLine("○", qfd._answerSelectCount));
                answerLineList.add("※あてはまるものに１つ黒丸にしてください。");
            } else if ("2".equals(qfd._answerPattern)) {
                answerLineList.add(getSelectLine("□", qfd._answerSelectCount));
                answerLineList.add("※あてはまるものに レ をいれてください。（複数選択可）");
            } else {
                int boxHight = 1;
                String comment = "";
                if ("3".equals(qfd._answerPattern)) {
                    boxHight = 16;
                    comment = "※128文字以内で記述してください。";
                } else if ("4".equals(qfd._answerPattern)) {
                    boxHight = 33;
                    comment = "※256文字以内で記述してください。";
                } else if ("5".equals(qfd._answerPattern)) {
                    boxHight = 68;
                    comment = "※512文字以内で記述してください。";
                }
                answerLineList.add(comment);

                boxHight = Math.min(boxHight, maxLine - questionLineList.size() - 1);
                int boxWidth = keta - getMS932ByteCount(head);
                boxWidth = boxWidth - (boxWidth % 2 == 1 ? 1 : 0);
                answerLineList.addAll(getBoxLine(boxWidth, boxHight));
            }
            return addHead(head, answerLineList);
        }

        public static List getBoxLine(final int width, int height) {
            final int c = width - 4;
            final String upper = "\u250C" + repeat("\u2500", c / 2) + "\u2510";  // "ボックスの左上角"、"ボックスの横線"、"ボックスの右上角"
            final String inter = "\u2502" + repeat("　",     c / 2) + "\u2502";  // "ボックスの縦線"、  "スペース"、      "ボックスの縦線"
            final String lower = "\u2514" + repeat("\u2500", c / 2) + "\u2518";  // "ボックスの左下角"、"ボックスの横線"、"ボックスの右下角"

            final LinkedList boxLineList = new LinkedList();
            boxLineList.add(upper);
            for (int i = 0; i < height - 2; i++) {
                boxLineList.add(inter);
            }
            boxLineList.add(lower);
            return boxLineList;
        }

        private static String getSelectLine(final String mark, final int count) {
            final StringBuffer stb = new StringBuffer();
            String spc = "";
            for (int i = 1; i <= count; i++) {
                stb.append(spc + String.valueOf(i) + " " + mark);
                spc = "   ";
            }
            return stb.toString();
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String questionnairecd = rs.getString("QUESTIONNAIRECD");
                    final int questionitemNo = rs.getInt("QUESTIONITEM_NO");
                    final String questionContents = rs.getString("QUESTION_CONTENTS");
                    final String answerPattern = rs.getString("ANSWER_PATTERN");
                    final int answerSelectCount = rs.getInt("ANSWER_SELECT_COUNT");
                    final QuestionFormatDat questionformatdat = new QuestionFormatDat(questionnairecd, questionitemNo, questionContents, answerPattern, answerSelectCount);
                    list.add(questionformatdat);
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
            stb.append("     T1.QUESTIONNAIRECD, ");
            stb.append("     T1.QUESTIONITEM_NO, ");
            stb.append("     T1.QUESTION_CONTENTS, ");
            stb.append("     T1.ANSWER_PATTERN, ");
            stb.append("     T1.ANSWER_SELECT_COUNT ");
            stb.append(" FROM QUESTION_FORMAT_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.QUESTIONNAIRECD = '" + param._questionnairecd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.QUESTIONITEM_NO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _ctrlDate;
        final String _questionnairecd;
        String _questionnairename;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _questionnairecd = request.getParameter("QUESTIONNAIRECD");
            setQuestionnairename(db2);
        }
        
        public void setQuestionnairename(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT QUESTIONNAIRENAME FROM QUESTIONNAIRE_MST WHERE QUESTIONNAiRECD = '" + _questionnairecd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _questionnairename = rs.getString("QUESTIONNAIRENAME");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

// eof

