// kanji=漢字
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 65d1667b969bee33e24522b5187207b57fc375b4 $
 */
public class KNJL394I {

    private static final Log log = LogFactory.getLog("KNJL394I.class");

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

            _hasData = printMain(db2, svf);
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

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final Map applicantMap = getApplicantMap(db2); //志願者Map

        if(applicantMap.isEmpty()) {
            return false;
        }

        log.debug(applicantMap.size());
        final int maxLine = 50; //最大印字行
        final int maxPage; //最大ページ
        final int amari = applicantMap.size() % maxLine;

        if(amari > 0) {
            maxPage = (applicantMap.size() / maxLine) + 1;
        } else {
            maxPage = (applicantMap.size() / maxLine);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String outputDate = sdf.format(new Date());

        int page = 0; // ページ
        int line = 1; //印字行

        for (Iterator ite = applicantMap.keySet().iterator(); ite.hasNext();) {
            final String Key = (String)ite.next();
            final Applicant applicant = (Applicant)applicantMap.get(Key);

            if (line > maxLine || page == 0) {
                if(line > maxLine) svf.VrEndPage();
                page++;
                line = 1;
                svf.VrSetForm("KNJL394I.frm", 1);
                svf.VrsOut("PAGE", "(" + String.valueOf(page) + "／" + maxPage + ")"); //ページ
                final String div = "1".equals(_param._outputDiv) ? "(男女共)" : "2".equals(_param._outputDiv) ? "(男子のみ)" : "(女子のみ)";
                final String sort = "1".equals(_param._sortDiv) ? "(受験番号順)" : "(序列順)";
                svf.VrsOut("TITLE", _param._year + "年度　" + _param._testAbbv + "　成績一覧表  " + sort + " " + div ); //タイトル
                svf.VrsOut("DATE", outputDate);
                svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名

                //教科名
                int cnt = 1;
                for(Iterator classite = _param._subclassMap.values().iterator(); classite.hasNext();) {
                    final String className = (String)classite.next();
                    svf.VrsOut("CLASS_NAME" + cnt, className);
                    cnt++;
                }

                //資料評価
                svf.VrsOut("DIV_NAME1", "資１");
                svf.VrsOut("DIV_NAME2", "資２");
                svf.VrsOut("DIV_NAME3", "資３");
                svf.VrsOut("DIV_NAME4", "資４");
                svf.VrsOut("DIV_NAME5", "資５");
                svf.VrsOut("DIV_NAME6", "資６");
                svf.VrsOut("DIV_NAME7", "資７");
            }

            svf.VrsOutn("EXAM_NO1", line, applicant._examno); //受験番号
            svf.VrsOutn("RANK", line, applicant._totalRank); //序列
            svf.VrsOutn("PASS_RANK", line, applicant._goukakuRank); //合格者序列
            final String fieldName = getFieldName(applicant._name);
            svf.VrsOutn("NAME" + fieldName, line, applicant._name); //氏名
            svf.VrsOutn("SEX", line, "1".equals(applicant._sex) ? "男" : "女"); //性別
            svf.VrsOutn("PASS_SCORE", line, applicant._totalScore); //合計点

            //各科目得点
            int cnt = 1;
            for(Iterator classite = _param._subclassMap.keySet().iterator(); classite.hasNext();) {
                final String classCd = (String)classite.next();
                if(applicant._scoreMap.containsKey(classCd)){
                    final Score score = (Score)applicant._scoreMap.get(classCd);
                    if(!"1".equals(score._attendFlg)) {
                        svf.VrsOutn("SCORE" + cnt, line, score._score);
                    } else {
                        svf.VrsOutn("SCORE" + cnt, line, "欠席");
                    }
                }
                cnt++;
            }

            svf.VrsOutn("DIV1", line, applicant._remark1); //資料1
            svf.VrsOutn("DIV2", line, applicant._remark2); //資料2
            svf.VrsOutn("DIV3", line, applicant._remark3); //資料3
            svf.VrsOutn("DIV4", line, applicant._remark4); //資料4
            svf.VrsOutn("DIV5", line, applicant._remark5); //資料5
            svf.VrsOutn("DIV6", line, applicant._remark6); //資料6
            svf.VrsOutn("DIV7", line, applicant._remark7); //資料7
            final String fieldSchool = getFieldName(applicant._schoolName);
            svf.VrsOutn("FINSCHOOL_NAME" + fieldSchool, line, applicant._schoolName); //学校名称
            svf.VrsOutn("EXAM_NO2", line, applicant._examno2); //受験番号2
            if(applicant._judgement != null) {
                svf.VrsOutn("JUDGE2", line, applicant._judgement); //合否
            }

            line++;
        }
        svf.VrEndPage();

    return true;
    }

    private String getFieldName(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 30 ? "1" : "2";
    }

    // 志願者取得
    private Map getApplicantMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   RPT.ENTEXAMYEAR, ");
            stb.append("   RPT.APPLICANTDIV, ");
            stb.append("   RPT.TESTDIV, ");
            stb.append("   RPT.RECEPTNO, ");
            if ("1".equals(_param._outputDiv)) {
                stb.append("   RPT.TOTAL_RANK1, ");
            } else {
                stb.append("   RPT.SEX_RANK1 AS TOTAL_RANK1, ");
            }
            stb.append("   DTL.REMARK1 AS GOUKAKURANK, ");
            stb.append("   BASE.NAME, ");
            stb.append("   BASE.SEX, ");
            stb.append("   RPT.TOTAL1, ");
            stb.append("   T1.REMARK1 AS REMARK1, ");
            stb.append("   T2.REMARK1 AS REMARK2, ");
            stb.append("   T3.REMARK1 AS REMARK3, ");
            stb.append("   T4.REMARK1 AS REMARK4, ");
            stb.append("   T5.REMARK1 AS REMARK5, ");
            stb.append("   T6.REMARK1 AS REMARK6, ");
            stb.append("   T7.REMARK1 AS REMARK7, ");
            stb.append("   S1.FINSCHOOL_NAME, ");
            stb.append("   BASE.RECOM_EXAMNO, ");
            stb.append("   SET013.ABBV1 AS JUDGEMENT ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_RECEPT_DAT RPT ");
            stb.append(" LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT DTL ON DTL.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND DTL.APPLICANTDIV = RPT.APPLICANTDIV AND DTL.TESTDIV = RPT.TESTDIV AND DTL.EXAM_TYPE = RPT.EXAM_TYPE AND DTL.RECEPTNO = RPT.RECEPTNO AND DTL.SEQ = '015' ");
            stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RPT.APPLICANTDIV AND BASE.TESTDIV = RPT.TESTDIV AND BASE.EXAMNO = RPT.EXAMNO ");
            stb.append(" LEFT JOIN ENTEXAM_DOCUMENT_VIEW_DAT T1 ON T1.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND T1.APPLICANTDIV = RPT.APPLICANTDIV AND T1.TESTDIV = RPT.TESTDIV AND T1.EXAMNO = RPT.EXAMNO AND T1.SEQ = '001'   ");
            stb.append(" LEFT JOIN ENTEXAM_DOCUMENT_VIEW_DAT T2 ON T2.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND T2.APPLICANTDIV = RPT.APPLICANTDIV AND T2.TESTDIV = RPT.TESTDIV AND T2.EXAMNO = RPT.EXAMNO AND T2.SEQ = '002' ");
            stb.append(" LEFT JOIN ENTEXAM_DOCUMENT_VIEW_DAT T3 ON T3.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND T3.APPLICANTDIV = RPT.APPLICANTDIV AND T3.TESTDIV = RPT.TESTDIV AND T3.EXAMNO = RPT.EXAMNO AND T3.SEQ = '003' ");
            stb.append(" LEFT JOIN ENTEXAM_DOCUMENT_VIEW_DAT T4 ON T4.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND T4.APPLICANTDIV = RPT.APPLICANTDIV AND T4.TESTDIV = RPT.TESTDIV AND T4.EXAMNO = RPT.EXAMNO AND T4.SEQ = '004' ");
            stb.append(" LEFT JOIN ENTEXAM_DOCUMENT_VIEW_DAT T5 ON T5.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND T5.APPLICANTDIV = RPT.APPLICANTDIV AND T5.TESTDIV = RPT.TESTDIV AND T5.EXAMNO = RPT.EXAMNO AND T5.SEQ = '005' ");
            stb.append(" LEFT JOIN ENTEXAM_DOCUMENT_VIEW_DAT T6 ON T6.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND T6.APPLICANTDIV = RPT.APPLICANTDIV AND T6.TESTDIV = RPT.TESTDIV AND T6.EXAMNO = RPT.EXAMNO AND T6.SEQ = '006' ");
            stb.append(" LEFT JOIN ENTEXAM_DOCUMENT_VIEW_DAT T7 ON T7.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND T7.APPLICANTDIV = RPT.APPLICANTDIV AND T7.TESTDIV = RPT.TESTDIV AND T7.EXAMNO = RPT.EXAMNO AND T7.SEQ = '007' ");
            stb.append(" LEFT JOIN FINSCHOOL_MST S1 ON S1.FINSCHOOLCD = BASE.FS_CD ");
            stb.append(" LEFT JOIN ENTEXAM_SETTING_MST SET013 ");
            stb.append("      ON SET013.ENTEXAMYEAR  = RPT.ENTEXAMYEAR ");
            stb.append("     AND SET013.APPLICANTDIV = RPT.APPLICANTDIV ");
            stb.append("     AND SET013.SETTING_CD   = 'L013' ");
            stb.append("     AND SET013.SEQ          = RPT.JUDGEDIV ");
            stb.append(" WHERE ");
            stb.append("   RPT.ENTEXAMYEAR = '" + _param._year + "' AND ");
            stb.append("   RPT.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   RPT.TESTDIV = '" + _param._testDiv + "' AND ");
            stb.append("   VALUE(BASE.JUDGEMENT, '') <> '5' AND "); //5:未受験  B日程志願者のうちA日程または帰国生入試で既に合格している場合は除外
            stb.append("   RPT.EXAM_TYPE = '1' ");
            if("2".equals(_param._outputDiv)) {
                stb.append("     AND BASE.SEX = '1' ");
            } else if("3".equals(_param._outputDiv)) {
                stb.append("     AND BASE.SEX = '2' ");
            }
            if("1".equals(_param._sortDiv)) {
                stb.append(" ORDER BY RPT.RECEPTNO");
            } else {
                stb.append(" ORDER BY VALUE(RPT.TOTAL1, -1) DESC, RPT.RECEPTNO");
            }


            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String applicantdiv = rs.getString("APPLICANTDIV");
                final String testdiv = rs.getString("TESTDIV");
                final String receptno = rs.getString("RECEPTNO");
                final String totalRank = rs.getString("TOTAL_RANK1");
                final String goukakuRank = rs.getString("GOUKAKURANK");
                final String totalScore = rs.getString("TOTAL1");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");
                final String remark3 = rs.getString("REMARK3");
                final String remark4 = rs.getString("REMARK4");
                final String remark5 = rs.getString("REMARK5");
                final String remark6 = rs.getString("REMARK6");
                final String remark7 = rs.getString("REMARK7");
                final String schoolName = rs.getString("FINSCHOOL_NAME");
                final String examno2 = rs.getString("RECOM_EXAMNO");
                final String judgement = rs.getString("JUDGEMENT");

                final Applicant applicant = new Applicant(entexamyear, applicantdiv, testdiv, receptno, name, sex, totalRank, goukakuRank, totalScore,
                        remark1, remark2, remark3, remark4, remark5, remark6, remark7, schoolName, examno2, judgement);
                applicant.setEntexamScore(db2);

                if(!retMap.containsKey(receptno)) {
                    retMap.put(receptno, applicant);
                }
            }
        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    private class Score {
        final String _subclassCd;
        final String _attendFlg;
        final String _score;
        public Score(final String subclassCd, final String attendFlg, final String score) {
            _subclassCd = subclassCd;
            _attendFlg = attendFlg;
            _score = score;
        }
    }

    private class Applicant {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examno;
        final String _name;
        final String _sex;
        final String _totalRank;
        final String _goukakuRank;
        final String _totalScore;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        final String _remark5;
        final String _remark6;
        final String _remark7;
        final String _schoolName;
        final String _examno2;
        final String _judgement;
        final Map _scoreMap;

        public Applicant(
                final String entexamyear, final String applicantdiv, final String testdiv,final String examno,
                final String name, final String sex, final String totalRank, final String goukakuRank, final String totalScore,
                final String remark1, final String remark2, final String remark3,
                final String remark4, final String remark5, final String remark6, final String remark7,
                final String schoolName, final String examno2, final String judgement) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _examno = examno;
            _name = name;
            _sex = sex;
            _totalRank = totalRank;
            _goukakuRank = goukakuRank;
            _totalScore = totalScore;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark6 = remark6;
            _remark7 = remark7;
            _scoreMap = new LinkedMap();
            _schoolName = schoolName;
            _examno2 = examno2;
            _judgement = judgement;
        }

        public void setEntexamScore(final DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.TESTSUBCLASSCD, ");
            stb.append("   T1.ATTEND_FLG, ");
            stb.append("   T1.SCORE ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_SCORE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.ENTEXAMYEAR = '" + _entexamyear + "' AND ");
            stb.append("   T1.APPLICANTDIV = '" + _applicantdiv + "' AND ");
            stb.append("   T1.TESTDIV = '" + _testdiv + "' AND ");
            stb.append("   T1.EXAM_TYPE = '1' AND ");
            stb.append("   T1.RECEPTNO = '" + _examno + "' ");

            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                final Score score = new Score(
                        KnjDbUtils.getString(row, "TESTSUBCLASSCD"),
                        KnjDbUtils.getString(row, "ATTEND_FLG"),
                        KnjDbUtils.getString(row, "SCORE")
                    );
                if(!_scoreMap.containsKey(score._subclassCd)) {
                    _scoreMap.put(score._subclassCd, score);
                }
            }
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76037 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _sortDiv;  //並び区分　1受験番号順: 2:成績順
        final String _outputDiv;  //抽出区分　1:全員 2:男子のみ 3:女子のみ
        final String _testAbbv;
        final String _date;
        final String _schoolKind;
        final String _schoolName;
        final Map _subclassMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _sortDiv = request.getParameter("SORT_DIV");
            _schoolKind = request.getParameter("SCHOOLKIND");
              _date = request.getParameter("CTRL_DATE");
              _testAbbv = getTestDivAbbv(db2);
              _schoolName = getSchoolName(db2);
              _subclassMap = getSubclassName(db2);

        }
        private String getTestDivAbbv(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _year + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }

        private String getSchoolName(final DB2UDB db2) {
            final String kindcd = "1".equals(_applicantDiv) ? "105" : "106";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + kindcd + "' "));

        }

        private Map getSubclassName(final DB2UDB db2) {
            final Map retMap = new LinkedMap();

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.TESTSUBCLASSCD, ");
            stb.append("     T2.NAME1 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT T1 ");
            stb.append(" LEFT JOIN ");
            stb.append("     ENTEXAM_SETTING_MST T2 ON T2.ENTEXAMYEAR = '" + _year + "' AND T2.APPLICANTDIV = '" + _applicantDiv + "' AND T2.SETTING_CD = 'L009' AND T2.SEQ = T1.TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _year + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + _testDiv + "' ");
            stb.append("     AND T1.EXAM_TYPE= '1' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.TESTSUBCLASSCD ");

            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                final String cd = KnjDbUtils.getString(row, "TESTSUBCLASSCD");
                final String name = KnjDbUtils.getString(row, "NAME1");
                if(!retMap.containsKey(cd)) {
                    retMap.put(cd, name);
                }
            }
            return retMap;
        }
    }

    public static String h_format_Seireki_MD(final String date) {
        if (null == date) {
            return date;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }
}

// eof
