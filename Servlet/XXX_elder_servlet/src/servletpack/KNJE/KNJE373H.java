// kanji=漢字
/*
 * 作成日: 2021/02/09
 * 作成者: s-shimoji
 *
 * Copyright(C) 2009-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJE373H {

    private static final Log log = LogFactory.getLog(KNJE373H.class);

    private boolean _hasData;

    private Param _param;

    private static final String SCHOOLDIV_FIN = "1"; // 1:学校
    private static final String SCHOOLDIV_PRI = "2"; // 2:塾
    private static final String[] TEST_KIND_LIST = {
             "-1", // 前年
        "1010101", // １回目素点
        "1010201", // ２回目素点
        "1020101", // ３回目素点
        "2010101", // ４回目素点
        "2020101"  // ５回目素点
    };
    private static final String ENT_DIV_GOUKAKU = "1"; // 1:合格内定進路、決定進路
    private static final String ENT_DIV_KIBOU = "2"; // 2:希望受験進路、合格内定進路
    private static final String SENKOU_KIND_SHINGAKU = "0"; // 0:進学
    private static final String SENKOU_KIND_SYUSYOKU = "1"; // 1:就職

    private static final int LINE_MAX = 40;

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

        String outputDate = KNJ_EditDate.getAutoFormatDate(db2, _param._ctrlDate);
        Map<String, School> schoolMap = getZaisekiseitoZyoukyouHoukokusyo(db2);

        final int maxCnt = 8; //最大印字行
        int pageCnt = 1; //ページ数

        final String maxPage = getMaxPage(schoolMap, maxCnt); //最大ページ数取得

        for (School school : schoolMap.values()) {
            svf.VrSetForm("KNJE373H.frm", 4);
            printTitle(svf, pageCnt, outputDate, school._schoolName, maxPage);
            int stuCnt = 1; //印字生徒数
            boolean printFirstFlg = true;
            boolean pastYearFlg = false;

            for (Student student : school._studentMap.values()) {
                //生徒数が最大行を超えた、または在校/卒業生全出力かつ切り替わり時なら改ページ(※1行目の出力で改ページしないよう、チェック)
                if (stuCnt > maxCnt || ("1".equals(_param._grdDiv) && !printFirstFlg && !pastYearFlg && !student._year.equals(_param._ctrlYear))) {
                    svf.VrEndPage();
                    svf.VrSetForm("KNJE373H.frm", 4);
                    stuCnt = 1;
                    pageCnt++;
                    printTitle(svf, pageCnt, outputDate, school._schoolName, maxPage);
                }

                final String gradeName;
                if (student._year.equals(_param._ctrlYear)) {
                    gradeName = student._gradecd;
                } else {
                    pastYearFlg = true;
                    gradeName = "前年";
                }
                svf.VrsOut("GRADE", gradeName);
                svf.VrsOut("DEPARTMENT_NAME", student._majorName);
                svf.VrsOut("COURSE_NAME", student._courceName);

                final int nameByte = KNJ_EditEdit.getMS932ByteLength(student._name);
                final String nameFieldName = nameByte > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nameFieldName, student._name);

                svf.VrsOut("HR_NAME", student._hrName);

                final int trByte = KNJ_EditEdit.getMS932ByteLength(student._staffName);
                final String trFieldName = trByte > 20 ? "2" : "1";
                svf.VrsOut("TR_NAME" + trFieldName, student._staffName);

                /**
                 * 学業成績の印字
                 */
                int testKindNo = 1;
                for (String testKind : TEST_KIND_LIST) {
                    if (student._rankMap.containsKey(testKind)) {
                        Rank rank = student._rankMap.get(testKind);
                        String avg = rank._avg == null ? "" : new BigDecimal(rank._avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        svf.VrsOutn("AVE1", testKindNo, avg);
                        String hrRank1 = StringUtils.defaultString(rank._rank) + "/" + StringUtils.defaultString(rank._count);
                        svf.VrsOutn("HR_RANK1", testKindNo, hrRank1);
                    }
                    testKindNo++;
                }

                /**
                 * 出欠状況の印字
                 */
                for (Attend attend : student._attendMap.values()) {
                    svf.VrsOutn("ABSENCE1", Integer.parseInt(attend._gradeCd), StringUtils.defaultString(attend._attend, "0"));
                    svf.VrsOutn("LATE1",    Integer.parseInt(attend._gradeCd), StringUtils.defaultString(attend._late,   "0"));
                    svf.VrsOutn("EARLY1",   Integer.parseInt(attend._gradeCd), StringUtils.defaultString(attend._earlsy, "0"));
                }

                /**
                 * 生徒会活動・クラス委員の印字
                 */
                StringBuffer committeeNameBuffer = new StringBuffer();
                for (Committee committee : student._committeeMap.values()) {
                    committeeNameBuffer.append(StringUtils.defaultString(committee._committeeName));
                    committeeNameBuffer.append(" ");
                    committeeNameBuffer.append(StringUtils.defaultString(committee._executiveName));
                    committeeNameBuffer.append("\r\n");
                }
                int committeeNo = 1;
                List<String> committeeNameList = KNJ_EditKinsoku.getTokenList(committeeNameBuffer.toString(), 20, 6);
                for (String committeeName : committeeNameList) {
                    svf.VrsOutn("COMMITTEE_NAME", committeeNo, committeeName);
                    committeeNo++;
                }

                /**
                 * 部活動の印字
                 */
                StringBuffer clubNameBuffer = new StringBuffer();
                for (Club club : student._clubMap.values()) {
                    clubNameBuffer.append(StringUtils.defaultString(club._clubName));
                    clubNameBuffer.append("\r\n");
                }
                int clubNo = 1;
                List<String> clubNameList = KNJ_EditKinsoku.getTokenList(clubNameBuffer.toString(), 20, 6);
                for (String clubName : clubNameList) {
                    svf.VrsOutn("CLUB_NAME", clubNo, clubName);
                    clubNo++;
                }

                /**
                 * 資格・検定の印字
                 */
                int qualifiedNo = 1;
                for (Qualified qualified : student._qualifiedMap.values()) {
                    String qualifiedName = StringUtils.defaultString(qualified._qualifiedName) + " " + StringUtils.defaultString(qualified._rankName);
                    int qualifiedByte = KNJ_EditEdit.getMS932ByteLength(qualifiedName);
                    String qualifiedFieldName = qualifiedByte > 30 ? "2" : "1";
                    svf.VrsOutn("QUALIFY_NAME" + qualifiedFieldName, qualifiedNo, qualifiedName);
                    qualifiedNo++;
                }

                /**
                 * 学生移動の印字
                 */
                svf.VrsOut("TERANSFER", student._grdVal);

                /**
                 * 合格内定進路・決定進路の印字
                 */
                int topNo = 1;
                for (String top : student._topList) {
                    svf.VrsOutn("OFFER", topNo, top);
                    topNo++;
                }
                int bottomNo = 1;
                for (String bottom : student._bottomList) {
                    svf.VrsOutn("DECIDE", bottomNo, bottom);
                    bottomNo++;
                }

                printFirstFlg = false;
                _hasData = true;
                svf.VrEndRecord();
                stuCnt++;
            }
            svf.VrEndPage();
            pageCnt++;
        }
    }

    //最大印字ページ数取得
    private String getMaxPage(final Map<String, School> schoolMap, final int maxCnt) {
        int page = 0;
        for (School school : schoolMap.values() ) {
            int stuCnt = 1;
            boolean printFirstFlg = true;
            boolean pastYearFlg = false;
            page += school._studentMap.size() == 0 ? 1 : 0;
            for (Student student : school._studentMap.values()) {
                //生徒数が最大行を超えた、または在校/卒業生全出力かつ切り替わり時なら改ページ
                if (stuCnt > maxCnt || ("1".equals(_param._grdDiv) && !printFirstFlg && !pastYearFlg && !student._year.equals(_param._ctrlYear))) {
                    stuCnt = 1;
                    page++;
                }
                if (!student._year.equals(_param._ctrlYear)) {
                    pastYearFlg = true;
                }
                printFirstFlg = false;
                stuCnt++;
            }
            page++;
        }
        return String.valueOf(page);
    }

    private void printTitle(final Vrw32alp svf, final int pageCnt, final String outputDate, final String schoolName, final String maxPage) {
        svf.VrsOut("TITLE", "在籍生徒状況報告書");
        svf.VrsOut("FINSCHOOL_NAME", schoolName);
        svf.VrsOut("DATE", outputDate);
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrsOut("PAGE", String.valueOf(pageCnt) + " / " + maxPage );
    }

    private Map<String, School> getZaisekiseitoZyoukyouHoukokusyo(final DB2UDB db2) {
        Map<String, School> schoolMap = getSchregBase(db2);
        Map<String, Map<String, Rank>> schregRankMap = getRankMap(db2);
        Map<String, Map<String, Committee>> schregCommitteeMap = getCommitteeMap(db2);
        Map<String, Map<String, Club>> schregClubMap = getClubMap(db2);
        Map<String, Map<String, Qualified>> schregQualifiedMap = getQualifiedMap(db2);
        Map<String, List<String>> schregTopMap = getGoukakuNaiteiShinroTop(db2);
        Map<String, List<String>> schregBottomMap = getGoukakuNaiteiShinroBottom(db2);

        for (School school : schoolMap.values() ) {
            for (Student student : school._studentMap.values() ) {
                student._rankMap      = schregRankMap.get(student._schregno);
                student._committeeMap = schregCommitteeMap.get(student._schregno);
                student._clubMap      = schregClubMap.get(student._schregno);
                student._qualifiedMap = schregQualifiedMap.get(student._schregno);
                if (schregTopMap.containsKey(student._schregno)) {
                    student._topList      = schregTopMap.get(student._schregno);
                } else {
                    student._topList      = new ArrayList<String>();
                }
                if (schregBottomMap.containsKey(student._schregno)) {
                    student._bottomList   = schregBottomMap.get(student._schregno);
                } else {
                    student._bottomList   = new ArrayList<String>();
                }
            }
        }
        return schoolMap;
    }

    private Map<String, School> getSchregBase(final DB2UDB db2) {
        Map<String, School> schoolMap = new LinkedHashMap<String, School>();
        School school = null;
        Student student = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String schregBaseSql = getSchregBaseSql();
        log.debug(" schreg base sql =" + schregBaseSql);

        try {
            ps = db2.prepareStatement(schregBaseSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String schoolCd = rs.getString("SCHOOLCD");
                final String schoolName = rs.getString("SCHOOL_NAME");
                final String grade = rs.getString("GRADE");
                final String gradeCd = rs.getString("GRADE_CD");
                final String majorCd = rs.getString("MAJORCD");
                final String majorName = rs.getString("MAJORNAME");
                final String courseCode = rs.getString("COURSECODE");
                final String courseAbbv = rs.getString("COURSECODEABBV1");
                final String hrClass= rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String staffName = rs.getString("STAFFNAME");
                final String grdName = rs.getString("GRD_NAME");

                if (schoolMap.containsKey(schoolCd)) {
                    school = schoolMap.get(schoolCd);
                } else {
                    school = new School(schoolCd, schoolName);
                    schoolMap.put(schoolCd, school);
                }

                student = new Student(
                    year,
                    schoolCd,
                    grade,
                    gradeCd,
                    majorCd,
                    majorName,
                    courseCode,
                    courseAbbv,
                    hrClass,
                    hrName,
                    schregno,
                    name,
                    staffName,
                    grdName
                );
                student._attendMap = getAttendMap(db2, student._schregno);
                school._studentMap.put(schregno, student);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return schoolMap;
    }

    private Map<String, Map<String, Rank>> getRankMap(final DB2UDB db2) {
        Map<String, Map<String, Rank>> schregMap = new LinkedHashMap<String, Map<String, Rank>>();
        Map<String, Rank> rankMap = null;
        Rank rank = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String rankLastSql = getLastRankSql();
        log.debug(" rank last sql =" + rankLastSql);

        try {
            ps = db2.prepareStatement(rankLastSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String avg = rs.getString("AVG");
                final String classRank = rs.getString("CLASS_RANK");
                final String count = rs.getString("COUNT");

                if (schregMap.containsKey(schregno)) {
                    rankMap = schregMap.get(schregno);
                } else {
                    rankMap = new LinkedHashMap<String, Rank>();
                    schregMap.put(schregno, rankMap);
                }

                rank = new Rank(schregno, avg, classRank, count);
                rankMap.put("-1", rank);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        final String rankSql = getRankSql();
        log.debug(" rank sql =" + rankSql);

        try {
            ps = db2.prepareStatement(rankSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String testKind = rs.getString("TEST_KINDE");
                final String avg = rs.getString("AVG");
                final String classRank = rs.getString("CLASS_RANK");
                final String count = rs.getString("COUNT");

                if (schregMap.containsKey(schregno)) {
                    rankMap = schregMap.get(schregno);
                } else {
                    rankMap = new LinkedHashMap<String, Rank>();
                    schregMap.put(schregno, rankMap);
                }

                rank = new Rank(schregno, avg, classRank, count);
                rankMap.put(testKind, rank);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return schregMap;
    }

    private Map<String, Attend> getAttendMap(final DB2UDB db2, final String schregno) {
        Attend attendClass = null;
        final Map attendMap = new LinkedHashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String attendSql = getAttendSql(schregno);
        log.debug(" attend sql =" + attendSql);

        try {
            ps = db2.prepareStatement(attendSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String gradeCd = rs.getString("GRADE_CD");
                final String attend = rs.getString("ATTEND");
                final String late = rs.getString("LATE");
                final String early = rs.getString("EARLY");

                attendClass = new Attend(year, gradeCd, attend, late, early);
                attendMap.put(year, attendClass);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return attendMap;
    }

    private Map<String, Map<String, Committee>> getCommitteeMap(final DB2UDB db2) {
        Map<String, Map<String, Committee>> schregMap = new LinkedHashMap<String, Map<String, Committee>>();
        Map<String, Committee> committeeMap = null;
        Committee committee = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String committeeSql = getCommitteeSql();
        log.debug(" committee sql =" + committeeSql);

        try {
            ps = db2.prepareStatement(committeeSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schoolCd = rs.getString("SCHOOLCD");
                final String schregno = rs.getString("SCHREGNO");
                final String committeeCd = rs.getString("COMMITTEECD");
                final String committeeName = rs.getString("COMMITTEENAME");
                final String executiveCd = rs.getString("EXECUTIVECD");
                final String executiveName = rs.getString("EXECUTIVE_NAME");

                if (schregMap.containsKey(schregno)) {
                    committeeMap = schregMap.get(schregno);
                } else {
                    committeeMap = new LinkedHashMap<String, Committee>();
                    schregMap.put(schregno, committeeMap);
                }

                committee = new Committee(schoolCd, schregno, committeeCd, committeeName, executiveCd, executiveName);
                committeeMap.put(committeeCd, committee);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return schregMap;
    }

    private Map<String, Map<String, Club>> getClubMap(final DB2UDB db2) {
        Map<String, Map<String, Club>> schregMap = new LinkedHashMap<String, Map<String, Club>>();
        Map<String, Club> clubMap = null;
        Club club = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String clubSql = getClubSql();
        log.debug(" club sql =" + clubSql);

        try {
            ps = db2.prepareStatement(clubSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schoolCd = rs.getString("SCHOOLCD");
                final String schregno = rs.getString("SCHREGNO");
                final String clubCd = rs.getString("CLUBCD");
                final String clubName = rs.getString("CLUBNAME");

                if (schregMap.containsKey(schregno)) {
                    clubMap = schregMap.get(schregno);
                } else {
                    clubMap = new LinkedHashMap<String, Club>();
                    schregMap.put(schregno, clubMap);
                }

                club = new Club(schoolCd, schregno, clubCd, clubName);
                clubMap.put(clubCd, club);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return schregMap;
    }

    private Map<String, Map<String, Qualified>> getQualifiedMap(final DB2UDB db2) {
        Map<String, Map<String, Qualified>> schregMap = new LinkedHashMap<String, Map<String, Qualified>>();
        Map<String, Qualified> qualifiedMap = null;
        Qualified qualified = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String qualifiedSql = getQualifiedSql();
        log.debug(" qualified sql =" + qualifiedSql);

        try {
            ps = db2.prepareStatement(qualifiedSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schoolCd = rs.getString("SCHOOLCD");
                final String schregno = rs.getString("SCHREGNO");
                final String qualifiedCd = rs.getString("QUALIFIED_CD");
                final String qualifiedName = rs.getString("QUALIFIED_NAME");
                final String rankName = rs.getString("RANK_NAME");

                if (schregMap.containsKey(schregno)) {
                    qualifiedMap = schregMap.get(schregno);
                } else {
                    qualifiedMap = new LinkedHashMap<String, Qualified>();
                    schregMap.put(schregno, qualifiedMap);
                }

                qualified = new Qualified(schoolCd, schregno, qualifiedCd, qualifiedName, rankName);
                qualifiedMap.put(qualifiedCd, qualified);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return schregMap;
    }

    private Map<String, List<String>> getGoukakuNaiteiShinroTop(final DB2UDB db2) {
        Map<String, List<String>> schregMap = new LinkedHashMap<String, List<String>>();
        List<String> goukakuNaiteiShinroTopList = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String goukakuNaiteiShinroTopSql = getGoukakuNaiteiShinroTopSql();
        log.debug(" goukaku naitei shinro top sql =" + goukakuNaiteiShinroTopSql);

        try {
            ps = db2.prepareStatement(goukakuNaiteiShinroTopSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String statName = rs.getString("STAT_NAME");

                if (schregMap.containsKey(schregno)) {
                    goukakuNaiteiShinroTopList = schregMap.get(schregno);
                } else {
                    goukakuNaiteiShinroTopList = new ArrayList<String>();
                    schregMap.put(schregno, goukakuNaiteiShinroTopList);
                }

                goukakuNaiteiShinroTopList.add(statName);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return schregMap;
    }

    private Map<String, List<String>> getGoukakuNaiteiShinroBottom(final DB2UDB db2) {
        Map<String, List<String>> schregMap = new LinkedHashMap<String, List<String>>();
        List<String> goukakuNaiteiShinroBottomList = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String goukakuNaiteiShinroBottomSql = getGoukakuNaiteiShinroBottomSql();
        log.debug(" goukaku naitei shinro bottom sql =" + goukakuNaiteiShinroBottomSql);

        try {
            ps = db2.prepareStatement(goukakuNaiteiShinroBottomSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String statName = rs.getString("STAT_NAME");

                if (schregMap.containsKey(schregno)) {
                    goukakuNaiteiShinroBottomList = schregMap.get(schregno);
                } else {
                    goukakuNaiteiShinroBottomList = new ArrayList<String>();
                    schregMap.put(schregno, goukakuNaiteiShinroBottomList);
                }

                goukakuNaiteiShinroBottomList.add(statName);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return schregMap;
    }

    private String getSchregBaseSql() {
        // 名前の取得
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHOOL AS ( ");
        if (SCHOOLDIV_FIN.equals(_param._schoolDiv)) {
            // 出身学校の場合
            stb.append("     SELECT ");
            stb.append("         FINSCHOOLCD AS SCHOOLCD, ");
            stb.append("         FINSCHOOL_NAME AS SCHOOL_NAME ");
            stb.append("     FROM ");
            stb.append("         FINSCHOOL_MST ");
        } else {
            // 塾の場合
            stb.append("     SELECT ");
            stb.append("         PRISCHOOLCD AS SCHOOLCD, ");
            stb.append("         PRISCHOOL_NAME AS SCHOOL_NAME ");
            stb.append("     FROM ");
            stb.append("         PRISCHOOL_MST ");
        }
        stb.append(" ), ");
        stb.append(" SCHREG AS ( ");
        stb.append(getZaikouseiSotsugyouseiSql());
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCHREG.YEAR, ");
        stb.append("     SCHOOL.SCHOOLCD, ");
        stb.append("     SCHOOL.SCHOOL_NAME, ");
        stb.append("     SCHREG.GRADE, ");
        stb.append("     INT(GDAT.GRADE_CD) AS GRADE_CD, ");
        stb.append("     SCHREG.MAJORCD, ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     SCHREG.COURSECODE, ");
        stb.append("     COURSECODE.COURSECODENAME, ");
        stb.append("     SCHREG.SCHREGNO, ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     COURSECODE.COURSECODEABBV1, ");
        stb.append("     SCHREG.HR_CLASS, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     SCHREG.SCHREGNO, ");
        stb.append("     SCHREG.NAME, ");
        stb.append("     STAFF.STAFFNAME, ");
        stb.append("     A003.NAME1 AS GRD_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG ");
        stb.append("     LEFT JOIN SCHOOL ON ");
        stb.append("               SCHOOL.SCHOOLCD = SCHREG.SCHOOLCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON ");
        stb.append("               GDAT.YEAR  = SCHREG.YEAR ");
        stb.append("           AND GDAT.GRADE = SCHREG.GRADE ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON ");
        stb.append("               MAJOR.MAJORCD  = SCHREG.MAJORCD ");
        stb.append("           AND MAJOR.COURSECD = SCHREG.COURSECD ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSECODE ON ");
        stb.append("               COURSECODE.COURSECODE = SCHREG.COURSECODE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON ");
        stb.append("               HDAT.YEAR     = SCHREG.YEAR ");
        stb.append("           AND HDAT.SEMESTER = SCHREG.SEMESTER ");
        stb.append("           AND HDAT.GRADE    = SCHREG.GRADE ");
        stb.append("           AND HDAT.HR_CLASS = SCHREG.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON ");
        stb.append("               STAFF.STAFFCD = HDAT.TR_CD1 ");
        stb.append("     LEFT JOIN NAME_MST A003 ON ");
        stb.append("               A003.NAMECD1 = 'A003' ");
        stb.append("           AND A003.NAMECD2 = SCHREG.GRD_DIV ");
        stb.append(" ORDER BY ");
        stb.append("     SCHOOL.SCHOOLCD, ");
        stb.append("     SCHREG.YEAR DESC, ");
        stb.append("     GRADE_CD DESC,");
        stb.append("     SCHREG.HR_CLASS, ");
        stb.append("     SCHREG.ATTENDNO ");
        return stb.toString();
    }

    private String getLastRankSql() {
        final StringBuffer stb = new StringBuffer();
        // 学籍成績の取得
        stb.append(" WITH RANK AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SCHREGNO, ");
        stb.append("         SCORE, ");
        stb.append("         CLASS_RANK, ");
        stb.append("         AVG ");
        stb.append("     FROM ");
        stb.append("         RECORD_RANK_SDIV_DAT ");
        stb.append("     WHERE ");
        // TEST_KIND_LIST[0] = -1 ここでは使わず、ヒットもしないので無視すること。
        final String[] TKList = {"9990008"};
        stb.append(SQLUtils.whereIn(true, "SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV", TKList));
        stb.append("     AND CLASSCD       = '99' ");
        stb.append("     AND SCHOOL_KIND   = '" + _param._schoolKind + "' ");
        stb.append("     AND CURRICULUM_CD = '99' ");
        stb.append("     AND SUBCLASSCD    = '999999' ");
        stb.append(" ), ");
        stb.append(" LAST_COUNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         COUNT ");
        stb.append("     FROM ");
        stb.append("         RECORD_AVERAGE_SDIV_DAT ");
        stb.append("     WHERE ");
        stb.append("         CLASSCD       = '99' ");
        stb.append("     AND SCHOOL_KIND   = '" + _param._schoolKind + "' ");
        stb.append("     AND CURRICULUM_CD = '99' ");
        stb.append("     AND SUBCLASSCD    = '999999' ");
        stb.append("     AND AVG_DIV       = '2' "); // 2:クラス
        stb.append("     AND COURSECD      = '0' ");
        stb.append("     AND MAJORCD       = '000' ");
        stb.append("     AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + TKList[0] + "' ");
        stb.append(" ), ");
        stb.append(" SCHREG1 AS ( ");
        stb.append(getZaikouseiSotsugyouseiSql());
        stb.append(" ), ");
        stb.append(" MAX_SEMESTER_T AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         MAX(SEMESTER) AS SEMESTER ");
        stb.append("     FROM ");
        stb.append("         SEMESTER_MST ");
        stb.append("     WHERE ");
        stb.append("         SEMESTER <> '9' ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR ");
        stb.append(" ), ");
        // 在校生、卒業生の２年生の情報を取得する
        stb.append(" LAST_SCH AS ( ");
        stb.append("     SELECT ");
        stb.append("         DAT.YEAR, ");
        stb.append("         DAT.SEMESTER, ");
        stb.append("         DAT.GRADE, ");
        stb.append("         DAT.HR_CLASS, ");
        stb.append("         DAT.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("          SCHREG1 ");
        // 留年した生徒は最新の年度を有効にする
        stb.append("          INNER JOIN (SELECT ");
        stb.append("                          MAX(TN1.YEAR) AS YEAR, ");
        stb.append("                          TN1.SEMESTER, ");
        stb.append("                          TN1.GRADE, ");
        stb.append("                          TN1.SCHREGNO ");
        stb.append("                      FROM ");
        stb.append("                          SCHREG_REGD_DAT TN1 ");
        stb.append("                          LEFT JOIN SCHREG_REGD_GDAT TN2 ");
        stb.append("                            ON TN2.YEAR = TN1.YEAR ");
        stb.append("                           AND TN2.GRADE = TN1.GRADE ");
        stb.append("                      WHERE ");
        stb.append("                          TN1.YEAR  < '" + _param._ctrlYear + "' ");
        stb.append("                          AND TN1.GRADE <= '" + _param._sophomoreGrade + "' ");
        stb.append("                          AND TN2.SCHOOL_KIND    = '" + _param._schoolKind + "' ");
        stb.append("                      GROUP BY ");
        stb.append("                          TN1.SEMESTER, ");
        stb.append("                          TN1.GRADE, ");
        stb.append("                          TN1.SCHREGNO ");
        stb.append("                     ) SCHREG2 ");
        stb.append("                  ON SCHREG2.SCHREGNO = SCHREG1.SCHREGNO ");
        stb.append("          INNER JOIN MAX_SEMESTER_T ");
        stb.append("                  ON MAX_SEMESTER_T.YEAR     = SCHREG2.YEAR ");
        stb.append("                 AND MAX_SEMESTER_T.SEMESTER = SCHREG2.SEMESTER ");
        stb.append("          INNER JOIN SCHREG_REGD_DAT DAT ");
        stb.append("                  ON DAT.YEAR     = SCHREG2.YEAR ");
        stb.append("                 AND DAT.SEMESTER = SCHREG2.SEMESTER ");
        stb.append("                 AND DAT.SCHREGNO = SCHREG2.SCHREGNO ");
        stb.append(" ), ");
        stb.append(" LAST_RANK AS ( ");
        stb.append("     SELECT ");
        stb.append("         LAST_SCH.YEAR, ");
        stb.append("         LAST_DAT.SCHREGNO, ");
        stb.append("         RANK.SCORE, ");
        stb.append("         RANK.CLASS_RANK, ");
        stb.append("         AVG ");
        stb.append("     FROM ");
        stb.append("         LAST_SCH ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT LAST_DAT ");
        stb.append("                 ON LAST_DAT.YEAR     = LAST_SCH.YEAR ");
        stb.append("                AND LAST_DAT.SEMESTER = LAST_SCH.SEMESTER ");
        stb.append("                AND LAST_DAT.GRADE    = LAST_SCH.GRADE ");
        stb.append("                AND LAST_DAT.HR_CLASS = LAST_SCH.HR_CLASS ");
        stb.append("         INNER JOIN RANK ON ");
        stb.append("                    RANK.YEAR     = LAST_DAT.YEAR ");
        stb.append("                AND RANK.SCHREGNO = LAST_DAT.SCHREGNO ");
        stb.append("     ORDER BY ");
        stb.append("         LAST_SCH.YEAR, ");
        stb.append("         LAST_SCH.GRADE, ");
        stb.append("         LAST_SCH.HR_CLASS, ");
        stb.append("         RANK.SCORE DESC ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     LAST_SCH.SCHREGNO, ");
        stb.append("     LAST_RANK.SCORE, ");
        stb.append("     LAST_RANK.CLASS_RANK, ");
        stb.append("     LAST_COUNT.COUNT, ");
        stb.append("     LAST_RANK.AVG ");
        stb.append(" FROM ");
        stb.append("     LAST_SCH ");
        stb.append("     INNER JOIN LAST_COUNT ");
        stb.append("             ON LAST_COUNT.YEAR     = LAST_SCH.YEAR ");
        stb.append("            AND LAST_COUNT.GRADE    = LAST_SCH.GRADE ");
        stb.append("            AND LAST_COUNT.HR_CLASS = LAST_SCH.HR_CLASS ");
        stb.append("     INNER JOIN LAST_RANK ");
        stb.append("             ON LAST_RANK.YEAR      = LAST_SCH.YEAR ");
        stb.append("            AND LAST_RANK.SCHREGNO  = LAST_SCH.SCHREGNO ");
        return stb.toString();
    }

    private String getRankSql() {
        final StringBuffer stb = new StringBuffer();
        // 学籍成績の取得
        stb.append(" WITH RANK AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SCHREGNO, ");
        stb.append("         SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TEST_KINDE, ");
        stb.append("         CLASS_RANK, ");
        stb.append("         AVG ");
        stb.append("     FROM ");
        stb.append("         RECORD_RANK_SDIV_DAT ");
        stb.append("     WHERE ");
        stb.append("         CLASSCD       = '99' ");
        stb.append("     AND SCHOOL_KIND   = '" + _param._schoolKind + "' ");
        stb.append("     AND CURRICULUM_CD = '99' ");
        stb.append("     AND SUBCLASSCD    = '999999' ");
        stb.append(" ), ");
        stb.append(" AVERAGE AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD, ");
        stb.append("         SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TEST_KINDE, ");
        stb.append("         COUNT ");
        stb.append("     FROM ");
        stb.append("         RECORD_AVERAGE_SDIV_DAT ");
        stb.append("     WHERE ");
        stb.append("         CLASSCD       = '99' ");
        stb.append("     AND SCHOOL_KIND   = '" + _param._schoolKind + "' ");
        stb.append("     AND CURRICULUM_CD = '99' ");
        stb.append("     AND SUBCLASSCD    = '999999' ");
        stb.append("     AND AVG_DIV       = '2' "); // 2:クラス
        stb.append("     AND COURSECD      = '0' ");
        stb.append("     AND MAJORCD       = '000' ");
        stb.append(" ), ");
        stb.append(" SCHREG AS ( ");
        stb.append(getZaikouseiSotsugyouseiSql());
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT");
        stb.append("     SCHREG.SCHOOLCD, ");
        stb.append("     SCHREG.SCHREGNO, ");
        stb.append("     RANK.TEST_KINDE, ");
        stb.append("     RANK.AVG, ");
        stb.append("     RANK.CLASS_RANK, ");
        stb.append("     AVERAGE.COUNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG ");
        stb.append("     LEFT JOIN RANK ON ");
        stb.append("               RANK.YEAR     = SCHREG.YEAR ");
        stb.append("           AND RANK.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append("     LEFT JOIN AVERAGE ON ");
        stb.append("               AVERAGE.YEAR       = RANK.YEAR ");
        stb.append("           AND AVERAGE.TEST_KINDE = RANK.TEST_KINDE ");
        stb.append("           AND AVERAGE.GRADE      = SCHREG.GRADE ");
        stb.append("           AND AVERAGE.HR_CLASS   = SCHREG.HR_CLASS ");
        return stb.toString();
    }

    private String getAttendSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        // 欠席状況の取得
        stb.append(" WITH ATTEND_T AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         YEAR, ");
        stb.append("         VALUE(SUM(SICK), 0) + VALUE(SUM(NOTICE), 0) + VALUE(SUM(NONOTICE), 0) AS ATTEND, ");
        stb.append("         VALUE(SUM(LATE), 0)  AS LATE, ");
        stb.append("         VALUE(SUM(EARLY), 0) AS EARLY ");
        stb.append("     FROM ");
        stb.append("         ATTEND_SEMES_DAT ");
        stb.append("     WHERE ");
        stb.append("         SCHREGNO = '" + schregno + "' ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, ");
        stb.append("         SCHREGNO ");
        stb.append("     ORDER BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         YEAR ");
        stb.append(" ), ");
        stb.append(" SCHREG AS ( ");
        stb.append("     SELECT ");
        stb.append("         MAX(REGD.YEAR) AS YEAR, ");
        stb.append("         REGD.GRADE, ");
        stb.append("         GDAT.GRADE_CD ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN ");
        stb.append("         SCHREG_REGD_GDAT GDAT ");
        stb.append("          ON GDAT.YEAR = REGD.YEAR ");
        stb.append("         AND GDAT.GRADE = REGD.GRADE ");
        stb.append("         AND GDAT.SCHOOL_KIND = 'H' ");
        stb.append("     WHERE ");
        stb.append("         SCHREGNO = '" + schregno + "' ");
        stb.append("     GROUP BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         GDAT.GRADE_CD ");
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         GDAT.GRADE_CD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     ATTEND_T.YEAR, ");
        stb.append("     SCHREG.GRADE_CD, ");
        stb.append("     ATTEND_T.ATTEND, ");
        stb.append("     ATTEND_T.LATE, ");
        stb.append("     ATTEND_T.EARLY ");
        stb.append(" FROM ");
        stb.append("     SCHREG ");
        stb.append("     INNER JOIN ATTEND_T ON ");
        stb.append("               ATTEND_T.YEAR = SCHREG.YEAR ");
        stb.append(" ORDER BY ");
        stb.append("     ATTEND_T.YEAR ");
        return stb.toString();
    }

    private String getCommitteeSql() {
        final StringBuffer stb = new StringBuffer();
        // 生徒会活動、クラブ委員の取得
        stb.append(" WITH COMMITTEE AS ( ");
        stb.append("     SELECT ");
        stb.append("         HIST.YEAR, ");
        stb.append("         HIST.SEMESTER, ");
        stb.append("         HIST.SCHREGNO, ");
        stb.append("         HIST.GRADE, ");
        stb.append("         HIST.SEQ, ");
        stb.append("         HIST.COMMITTEE_FLG, ");
        stb.append("         HIST.COMMITTEECD, ");
        stb.append("         MST.COMMITTEENAME, ");
        stb.append("         HIST.EXECUTIVECD, ");
        stb.append("         J002.NAME1 AS EXECUTIVE_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_COMMITTEE_HIST_DAT HIST ");
        stb.append("         LEFT JOIN COMMITTEE_MST MST ON ");
        stb.append("                   MST.SCHOOLCD      = HIST.SCHOOLCD ");
        stb.append("               AND MST.SCHOOL_KIND   = HIST.SCHOOL_KIND ");
        stb.append("               AND MST.COMMITTEE_FLG = HIST.COMMITTEE_FLG ");
        stb.append("               AND MST.COMMITTEECD   = HIST.COMMITTEECD ");
        stb.append("         LEFT JOIN NAME_MST J002 ON ");
        stb.append("                   J002.NAMECD1 = 'J002' ");
        stb.append("               AND J002.NAMECD2 = HIST.EXECUTIVECD ");
        stb.append("     WHERE ");
        stb.append("         HIST.SCHOOLCD    = '" + _param._schoolCd + "'  ");
        stb.append("     AND HIST.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append(" ), ");
        stb.append(" SCHREG AS ( ");
        stb.append(getZaikouseiSotsugyouseiSql());
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCHREG.SCHOOLCD, ");
        stb.append("     SCHREG.SCHREGNO, ");
        stb.append("     COMMITTEE.COMMITTEECD, ");
        stb.append("     COMMITTEE.COMMITTEENAME, ");
        stb.append("     COMMITTEE.EXECUTIVECD, ");
        stb.append("     COMMITTEE.EXECUTIVE_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG ");
        stb.append("     LEFT JOIN COMMITTEE ON ");
        stb.append("               COMMITTEE.YEAR     = SCHREG.YEAR ");
        stb.append("           AND COMMITTEE.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append("           AND COMMITTEE.GRADE    = SCHREG.GRADE ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREG.YEAR, ");
        stb.append("     SCHREG.GRADE, ");
        stb.append("     SCHREG.SCHREGNO, ");
        stb.append("     COMMITTEE.COMMITTEE_FLG, ");
        stb.append("     COMMITTEE.COMMITTEECD ");
        return stb.toString();
    }

    private String getClubSql() {
        // 部活動の取得
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CLUB AS ( ");
        stb.append("     SELECT ");
        stb.append("         '" + _param._ctrlYear + "' AS YEAR, ");
        stb.append("         HIST.SCHREGNO, ");
        stb.append("         HIST.CLUBCD, ");
        stb.append("         CASE WHEN HIST.EDATE IS NULL ");
        stb.append("              THEN CLUB.CLUBNAME ");
        stb.append("              ELSE CLUB.CLUBNAME || '(' || TO_CHAR(HIST.EDATE, 'YY.MM.DD') || '退部)'  ");
        stb.append("         END AS CLUBNAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_CLUB_HIST_DAT HIST ");
        stb.append("         INNER JOIN CLUB_MST CLUB ON ");
        stb.append("                    CLUB.SCHOOLCD    = HIST.SCHOOLCD ");
        stb.append("                AND CLUB.SCHOOL_KIND = HIST.SCHOOL_KIND ");
        stb.append("                AND CLUB.CLUBCD      = HIST.CLUBCD ");
        stb.append("     WHERE ");
        stb.append("         HIST.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND HIST.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND ( ");
        stb.append("              (HIST.EDATE IS NULL AND HIST.SDATE <= DATE('" + _param._thisYearEDate + "')) ");
        stb.append("           OR (HIST.EDATE BETWEEN DATE('" + _param._thisYearSDate + "') AND DATE('" + _param._thisYearEDate + "')) ");
        stb.append("         ) ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         '" + _param._lastYear + "' AS YEAR, ");
        stb.append("         HIST.SCHREGNO, ");
        stb.append("         HIST.CLUBCD, ");
        stb.append("         CASE WHEN HIST.EDATE IS NULL ");
        stb.append("              THEN CLUB.CLUBNAME ");
        stb.append("              ELSE CLUB.CLUBNAME || '(' || TO_CHAR(HIST.EDATE, 'YY.MM.DD') || '退部)'  ");
        stb.append("         END AS CLUBNAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_CLUB_HIST_DAT HIST ");
        stb.append("         INNER JOIN CLUB_MST CLUB ON ");
        stb.append("                    CLUB.SCHOOLCD    = HIST.SCHOOLCD ");
        stb.append("                AND CLUB.SCHOOL_KIND = HIST.SCHOOL_KIND ");
        stb.append("                AND CLUB.CLUBCD      = HIST.CLUBCD ");
        stb.append("     WHERE ");
        stb.append("         HIST.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND HIST.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND ( ");
        stb.append("              (HIST.EDATE IS NULL AND HIST.SDATE <= DATE('" + _param._lastYearEDate + "')) ");
        stb.append("           OR (DATE('" + _param._lastYearEDate + "') BETWEEN HIST.SDATE AND HIST.EDATE) ");
        stb.append("         ) ");
        stb.append(" ), ");
        stb.append(" SCHREG AS ( ");
        stb.append(getZaikouseiSotsugyouseiSql());
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCHREG.SCHOOLCD, ");
        stb.append("     SCHREG.SCHREGNO, ");
        stb.append("     CLUB.CLUBCD, ");
        stb.append("     CLUB.CLUBNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG ");
        stb.append("     LEFT JOIN CLUB ON ");
        stb.append("               CLUB.YEAR     = SCHREG.YEAR ");
        stb.append("           AND CLUB.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     CLUB.CLUBCD ");
        return stb.toString();
    }

    private String getQualifiedSql() {
        final StringBuffer stb = new StringBuffer();
        // 資格・検定の取得
        stb.append(" WITH QTD AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     L1.QUALIFIED_CD, ");
        stb.append("     L1.QUALIFIED_NAME, ");
        stb.append("     L3.NAME1 AS RANK_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_QUALIFIED_HOBBY_DAT T1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ");
        stb.append(" LEFT JOIN ");
        stb.append("     NAME_MST L3 ON  L3.NAMECD2 = T1.RANK ");
        stb.append("                 AND L3.NAMECD1 = 'H312' ");
        if ("1".equals(_param._useQualifiedManagementFlg)) {
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     0 AS SEQ, ");
            stb.append("     L1.QUALIFIED_CD, ");
            stb.append("     L1.QUALIFIED_NAME, ");
            stb.append("     L3.RESULT_NAME AS RANK_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_QUALIFIED_TEST_DAT T1 ");
            stb.append(" LEFT JOIN ");
            stb.append("     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ");
            stb.append(" LEFT JOIN ");
            stb.append("     QUALIFIED_RESULT_MST L3 ON L3.YEAR         = T1.YEAR ");
            stb.append("                            AND L3.QUALIFIED_CD = T1.QUALIFIED_CD ");
            stb.append("                            AND L3.RESULT_CD    = T1.RESULT_CD ");
            stb.append(" WHERE ");
            stb.append("         L3.CERT_FLG = 'T' ");//正式フラグが立っているもの
        }
        stb.append(" ), ");
        stb.append(" SCHREG AS ( ");
        stb.append(getZaikouseiSotsugyouseiSql());
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCHREG.SCHOOLCD, ");
        stb.append("     SCHREG.SCHREGNO, ");
        stb.append("     QTD.QUALIFIED_CD, ");
        stb.append("     QTD.QUALIFIED_NAME, ");
        stb.append("     QTD.RANK_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG ");
        stb.append("     LEFT JOIN QTD ON ");
        stb.append("               QTD.YEAR     = SCHREG.YEAR ");
        stb.append("           AND QTD.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREG.SCHREGNO, ");
        stb.append("     QTD.SEQ ");
        return stb.toString();
    }

    private String getGoukakuNaiteiShinroTopSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG AS ( ");
        stb.append(getZaikouseiSotsugyouseiSql());
        stb.append(" ), MAX_Y_AGCD AS (");  //最終年度の最終SEQを割り出すために、まず最終年度を割り出す
        stb.append(" SELECT ");
        stb.append("   MAX(GRAD.YEAR) AS MXYEAR, ");
        stb.append("   GRAD.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("   SCHREG ");
        stb.append("   LEFT JOIN AFT_GRAD_COURSE_DAT GRAD ");
        stb.append("     ON ( ");
        stb.append("         ((SCHREG.GRAD_YEAR = '" + _param._ctrlYear + "' OR SCHREG.GRAD_YEAR IS NULL) AND GRAD.YEAR <= SCHREG.YEAR) ");  //在校生は年度制限。
        stb.append("         OR SCHREG.GRAD_YEAR < '" + _param._ctrlYear + "' ");  //卒業生は年度指定なし。
        stb.append("        ) ");
        stb.append("    AND GRAD.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append(" GROUP BY ");
        stb.append("   SCHREG.SCHOOLCD, ");
        stb.append("   GRAD.SCHREGNO ");
        stb.append(" ), MAX_SEQ_AGCD AS (");  //上記年度の最終SEQを割り出す。
        stb.append(" SELECT ");
        stb.append("   GRAD.YEAR, ");
        stb.append("   MAX(GRAD.SEQ) AS SEQ, ");
        stb.append("   GRAD.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("   SCHREG ");
        stb.append("   LEFT JOIN AFT_GRAD_COURSE_DAT GRAD ");
        stb.append("     ON GRAD.YEAR     <= SCHREG.YEAR ");
        stb.append("    AND GRAD.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append("   INNER JOIN MAX_Y_AGCD MX_Y");
        stb.append("     ON MX_Y.MXYEAR = GRAD.YEAR ");
        stb.append("    AND MX_Y.SCHREGNO = GRAD.SCHREGNO ");
        stb.append(" GROUP BY ");
        stb.append("   GRAD.YEAR, ");
        stb.append("   GRAD.SCHREGNO ");
        stb.append(" ), MAX_AGCD AS (");  //割り出した最大年度の最大SEQの詳細情報を拾ってくる。
        stb.append(" SELECT ");
        stb.append("   T1.* ");
        stb.append(" FROM ");
        stb.append("   AFT_GRAD_COURSE_DAT T1 ");
        stb.append("   INNER JOIN MAX_SEQ_AGCD T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEQ = T1.SEQ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO");
        stb.append(" ), ");
        stb.append(" STAT AS ( ");
        // 受験報告（進学）
        stb.append("     SELECT ");
        stb.append("         SCHREG.SCHOOLCD, ");
        stb.append("         GRAD.YEAR, ");
        stb.append("         GRAD.SEQ, ");
        stb.append("         GRAD.SCHREGNO, ");
        stb.append("         COLLEGE.SCHOOL_NAME AS STAT_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG ");
        stb.append("         LEFT JOIN MAX_AGCD GRAD ON ");
        stb.append("                   GRAD.YEAR     <= SCHREG.YEAR ");
        stb.append("               AND GRAD.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append("         LEFT JOIN COLLEGE_MST COLLEGE ON  ");
        stb.append("                   COLLEGE.SCHOOL_CD = GRAD.STAT_CD ");
        stb.append("     WHERE ");
        stb.append("         GRAD.SENKOU_KIND = '" + SENKOU_KIND_SHINGAKU + "' "); // 0:進学
        if (ENT_DIV_GOUKAKU.equals(_param._entDiv)) {
            stb.append("     AND GRAD.DECISION = '1' "); // 1:合格
        }
        stb.append("     UNION ALL  ");
        // 受験報告（就職）
        stb.append("     SELECT ");
        stb.append("         SCHREG.SCHOOLCD, ");
        stb.append("         GRAD.YEAR, ");
        stb.append("         GRAD.SEQ, ");
        stb.append("         GRAD.SCHREGNO, ");
        stb.append("         COMPANY.COMPANY_NAME AS STAT_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG ");
        stb.append("         LEFT JOIN MAX_AGCD GRAD ON ");
        stb.append("                   GRAD.YEAR     = SCHREG.GRAD_YEAR ");
        stb.append("               AND GRAD.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append("         LEFT JOIN COMPANY_MST COMPANY ON ");
        stb.append("                   COMPANY.COMPANY_CD = GRAD.STAT_CD ");
        stb.append("     WHERE ");
        stb.append("         GRAD.SENKOU_KIND = '" + SENKOU_KIND_SYUSYOKU + "' "); // 1:就職
        if (ENT_DIV_GOUKAKU.equals(_param._entDiv)) {
            stb.append("     AND GRAD.DECISION = '1' "); // 1:合格
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCHOOLCD, ");
        stb.append("     SCHREGNO, ");
        stb.append("     STAT_NAME ");
        stb.append(" FROM ");
        stb.append("     STAT ");
        stb.append(" ORDER BY ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     SEQ ");
        return stb.toString();
    }


    private String getGoukakuNaiteiShinroBottomSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG AS ( ");
        stb.append(getZaikouseiSotsugyouseiSql());
        stb.append(" ), ");
        stb.append(" STAT AS ( ");
        // 受験報告（進学）
        stb.append("     SELECT ");
        stb.append("         SCHREG.SCHOOLCD, ");
        stb.append("         GRAD.YEAR, ");
        stb.append("         GRAD.SEQ, ");
        stb.append("         GRAD.SCHREGNO, ");
        stb.append("         COLLEGE.SCHOOL_NAME AS STAT_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG ");
        stb.append("         LEFT JOIN AFT_GRAD_COURSE_DAT GRAD ON ");
        stb.append("                   GRAD.YEAR     = SCHREG.GRAD_YEAR ");
        stb.append("               AND GRAD.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append("         LEFT JOIN COLLEGE_MST COLLEGE ON  ");
        stb.append("                   COLLEGE.SCHOOL_CD = GRAD.STAT_CD ");
        stb.append("     WHERE ");
        stb.append("         GRAD.SENKOU_KIND = '" + SENKOU_KIND_SHINGAKU + "' "); // 0:進学
        if (ENT_DIV_GOUKAKU.equals(_param._entDiv)) {
            stb.append("     AND GRAD.PLANSTAT = '1' "); // 1:決定
        } else {
            stb.append("     AND GRAD.DECISION = '1' "); // 1:合格
        }
        stb.append("     UNION ALL  ");
        // 受験報告（就職）
        stb.append("     SELECT ");
        stb.append("         SCHREG.SCHOOLCD, ");
        stb.append("         GRAD.YEAR, ");
        stb.append("         GRAD.SEQ, ");
        stb.append("         GRAD.SCHREGNO, ");
        stb.append("         COMPANY.COMPANY_NAME AS STAT_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG ");
        stb.append("         LEFT JOIN AFT_GRAD_COURSE_DAT GRAD ON ");
        stb.append("                   GRAD.YEAR     = SCHREG.GRAD_YEAR ");
        stb.append("               AND GRAD.SCHREGNO = SCHREG.SCHREGNO ");
        stb.append("         LEFT JOIN COMPANY_MST COMPANY ON ");
        stb.append("                   COMPANY.COMPANY_CD = GRAD.STAT_CD ");
        stb.append("     WHERE ");
        stb.append("         GRAD.SENKOU_KIND = '" + SENKOU_KIND_SYUSYOKU + "' "); // 1:就職
        if (ENT_DIV_GOUKAKU.equals(_param._entDiv)) {
            stb.append("     AND GRAD.PLANSTAT = '1' "); // 1:決定
        } else {
            stb.append("     AND GRAD.DECISION = '1' "); // 1:合格
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCHOOLCD, ");
        stb.append("     SCHREGNO, ");
        stb.append("     STAT_NAME ");
        stb.append(" FROM ");
        stb.append("     STAT ");
        stb.append(" ORDER BY ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     SEQ ");
        return stb.toString();
    }

    private String getZaikouseiSotsugyouseiSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        if (SCHOOLDIV_FIN.equals(_param._schoolDiv)) {
            // 出身学校の場合
            stb.append("     BASE.FINSCHOOLCD AS SCHOOLCD, ");
        } else {
            // 塾の場合
            stb.append("     BASE.PRISCHOOLCD AS SCHOOLCD, ");
        }
        stb.append("     DAT.YEAR, ");
        // 在校生（ログイン年度の在籍者）はログイン年度を、
        // 卒業生（ログイン年度－１の在籍者、かつ、卒業者）はMAXの年度を進路情報の年度として扱う
        stb.append("     CASE WHEN DAT.YEAR = '" + _param._ctrlYear + "' THEN GRAD.YEAR ELSE GRD_GRAD.YEAR END AS GRAD_YEAR, ");
        stb.append("     DAT.SEMESTER, ");
        stb.append("     DAT.GRADE, ");
        stb.append("     DAT.HR_CLASS, ");
        stb.append("     DAT.ATTENDNO, ");
        stb.append("     DAT.MAJORCD, ");
        stb.append("     DAT.COURSECD, ");
        stb.append("     DAT.COURSECODE, ");
        stb.append("     DAT.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.GRD_DIV ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT DAT ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON ");
        stb.append("                GDAT.YEAR        = DAT.YEAR ");
        stb.append("            AND GDAT.GRADE       = DAT.GRADE ");
        stb.append("            AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON ");
        stb.append("                BASE.SCHREGNO = DAT.SCHREGNO ");
        stb.append("     LEFT  JOIN (SELECT ");
        stb.append("                     YEAR, ");
        stb.append("                     SCHREGNO ");
        stb.append("                 FROM ");
        stb.append("                     AFT_GRAD_COURSE_DAT ");
        stb.append("                 WHERE ");
        stb.append("                         SENKOU_KIND IN ('" + SENKOU_KIND_SHINGAKU + "', '" + SENKOU_KIND_SYUSYOKU + "') "); // 0:進学
        if (ENT_DIV_GOUKAKU.equals(_param._entDiv)) {
            stb.append("                     AND ( ");
            stb.append("                                  PLANSTAT = '1' "); // 1:決定
            stb.append("                               OR DECISION = '1' "); // 1:合格
            stb.append("                         ) ");
        }
        stb.append("                 GROUP BY ");
        stb.append("                     YEAR, ");
        stb.append("                     SCHREGNO ");
        stb.append("                ) GRAD ON ");
        stb.append("                GRAD.YEAR     = DAT.YEAR ");
        stb.append("            AND GRAD.SCHREGNO = DAT.SCHREGNO ");
        stb.append("     LEFT  JOIN (SELECT ");
        stb.append("                     MAX(YEAR) AS YEAR, ");
        stb.append("                     SCHREGNO ");
        stb.append("                 FROM ");
        stb.append("                     AFT_GRAD_COURSE_DAT ");
        stb.append("                 WHERE ");
        stb.append("                         SENKOU_KIND IN ('" + SENKOU_KIND_SHINGAKU + "', '" + SENKOU_KIND_SYUSYOKU + "') "); // 0:進学
        if (ENT_DIV_GOUKAKU.equals(_param._entDiv)) {
            stb.append("                     AND ( ");
            stb.append("                                  PLANSTAT = '1' "); // 1:決定
            stb.append("                               OR DECISION = '1' "); // 1:合格
            stb.append("                         ) ");
        }
        stb.append("                 GROUP BY ");
        stb.append("                     SCHREGNO ");
        stb.append("                ) GRD_GRAD ON ");
        stb.append("                GRD_GRAD.SCHREGNO = DAT.SCHREGNO ");
        stb.append(" WHERE ");
        if ("1".equals(_param._grdDiv)) { // 1:全て
            // 在校生
            stb.append("     ( ");
            stb.append("         ( ");
            stb.append("              DAT.YEAR      = '" + _param._ctrlYear + "' ");
            stb.append("          AND DAT.SEMESTER  = '" + _param._ctrlSemester + "' ");
            stb.append("         ) ");
            stb.append("         OR ");
            // 卒業生
            stb.append("         ( ");
            stb.append("              DAT.YEAR     = '" + _param._lastYear + "' ");
            stb.append("          AND DAT.SEMESTER = '" + _param._lastYearSemester + "' ");
            stb.append("          AND DAT.GRADE    = '" + _param._grdGrade + "' ");
            stb.append("          AND BASE.GRD_DIV = '1' "); // 1:卒業
            stb.append("          AND GRD_GRAD.SCHREGNO IS NOT NULL ");
            stb.append("         ) ");
            stb.append("     ) ");
            stb.append("     AND ");
        } else if ("2".equals(_param._grdDiv)) { // 2:在学生
            // 在校生
            stb.append("         DAT.YEAR     = '" + _param._ctrlYear + "' ");
            stb.append("     AND DAT.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND ");
        } else { // 3:卒業生
            // 卒業生
            stb.append("          DAT.YEAR     = '" + _param._lastYear + "' ");
            stb.append("      AND DAT.SEMESTER = '" + _param._lastYearSemester + "' ");
            stb.append("      AND DAT.GRADE    = '" + _param._grdGrade + "' ");
            stb.append("      AND BASE.GRD_DIV = '1' ");
            stb.append("      AND GRD_GRAD.SCHREGNO IS NOT NULL ");
            stb.append("      AND ");
        }
        if (SCHOOLDIV_FIN.equals(_param._schoolDiv)) {
            // 出身学校の場合
            stb.append(SQLUtils.whereIn(true, "BASE.FINSCHOOLCD", _param._categorySelected));
        } else {
            // 塾の場合
            stb.append(SQLUtils.whereIn(true, "BASE.PRISCHOOLCD", _param._categorySelected));
        }
        return stb.toString();
    }

    private class School {
        final String _schoolCd;
        final String _schoolName;
        final Map<String, Student> _studentMap;

        private School (
            final String schoolCd,
            final String schoolName
        ) {
            _schoolCd   = schoolCd;
            _schoolName = schoolName;
            _studentMap = new LinkedHashMap<String, Student>();
        }
    }

    private class Student {
        final String _year;
        final String _schoolCd;
        final String _grade;
        final String _gradecd;
        final String _majorName;
        final String _major;
        final String _courceName;
        final String _courcecode;
        final String _hrClass;
        final String _hrName;
        final String _schregno;
        final String _name;
        final String _staffName;
        final String _grdVal;
        Map<String, Rank> _rankMap; // キーはテスト種別、前年のランクのキーはnull
        Map<String, Attend> _attendMap; // キーはYEAR（各学年次の年）
        Map<String, Committee> _committeeMap; // キーはcommitteeCd（役職区分）
        Map<String, Club> _clubMap; // キーはCLUBCD（クラブコード）
        Map<String, Qualified> _qualifiedMap; // キーはQUALIFIED_CD（資格コード）
        List<String> _topList;
        List<String> _bottomList;

        private Student (
            final String year,
            final String schoolCd,
            final String grade,
            final String gradeCd,
            final String major,
            final String majorName,
            final String courcecode,
            final String courceName,
            final String hrClass,
            final String hrName,
            final String schregno,
            final String name,
            final String staffName,
            final String grdVal
        ) {
            _year         = year;
            _schoolCd     = schoolCd;
            _grade        = grade;
            _gradecd      = gradeCd;
            _major        = major;
            _majorName    = majorName;
            _courcecode   = courcecode;
            _courceName   = courceName;
            _hrClass      = hrClass;
            _hrName       = hrName;
            _schregno     = schregno;
            _name         = name;
            _staffName    = staffName;
            _grdVal       = grdVal;
        }
    }

    private class Rank {
        final String _schregno;
        final String _avg;
        final String _rank;
        final String _count;

        private Rank (final String schregno, final String avg, final String rank, final String count) {
            _schregno = schregno;
            _avg      = avg;
            _rank     = rank;
            _count    = count;
        }
    }


    private class Attend {
        final String _year;
        final String _gradeCd;
        final String _attend;
        final String _late;
        final String _earlsy;

        private Attend (
            final String year,
            final String gradeCd,
            final String attend,
            final String late,
            final String earlsy) {
            _year = year;
            _gradeCd = gradeCd;
            _attend   = attend;
            _late     = late;
            _earlsy   = earlsy;
        }
    }


    private class Committee {
        final String _schoolCd;
        final String _schregno;
        final String _committeeCd;
        final String _committeeName;
        final String _executiveCd;
        final String _executiveName;

        private Committee (
            final String schoolCd,
            final String schregno,
            final String committeeCd,
            final String committeeName,
            final String executiveCd,
            final String executiveName) {
            _schoolCd      = schoolCd;
            _schregno      = schregno;
            _committeeCd   = committeeCd;
            _committeeName = committeeName;
            _executiveCd   = executiveCd;
            _executiveName = executiveName;
        }
    }

    private class Club {
        final String _schoolCd;
        final String _schregno;
        final String _clubcd;
        final String _clubName;

        private Club (
            final String schoolCd,
            final String schregno,
            final String clubCd,
            final String clubName) {
            _schoolCd = schoolCd;
            _schregno = schregno;
            _clubcd   = clubCd;
            _clubName = clubName;
        }
    }


    private class Qualified {
        final String _schoolCd;
        final String _schregno;
        final String _qualifiedCd;
        final String _qualifiedName;
        final String _rankName;

        private Qualified (
            final String schoolCd,
            final String schregno,
            final String qualifiedCd,
            final String qualifiedName,
            final String rankName) {
            _schoolCd      = schoolCd;
            _schregno      = schregno;
            _qualifiedCd   = qualifiedCd;
            _qualifiedName = qualifiedName;
            _rankName      = rankName;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolDiv;
        private final String _grdDiv;
        private final String _testKind;
        private final String[] _categorySelected;
        private final String _entDiv;
        private final String _schoolKind;
        private final String _schoolCd;
        private final String _useQualifiedManagementFlg;
        private final String _lastYear;
        private final String _lastYearSemester;
        private final String _towYearAgo;
        private final String _towYearAgoSemester;
        private final String _schoolName;
        private final String _grdGrade;
        private final String _sophomoreGrade;
        private final String _thisYearSDate;
        private final String _thisYearEDate;
        private final String _lastYearSDate;
        private final String _lastYearEDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear                  = request.getParameter("CTRL_YEAR");
            _ctrlSemester              = request.getParameter("CTRL_SEMESTER");
            _ctrlDate                  = request.getParameter("CTRL_DATE");
            _schoolDiv                 = request.getParameter("SCHOOLDIV");
            _grdDiv                    = request.getParameter("GRD_DIV");
            _testKind                  = request.getParameter("TESTKIND");
            _categorySelected          = request.getParameterValues("CATEGORY_SELECTED");
            _entDiv                    = request.getParameter("ENT_DIV");
            _schoolKind                = request.getParameter("SCHOOLKIND");
            _schoolCd                  = request.getParameter("SCHOOLCD");
            _useQualifiedManagementFlg = request.getParameter("useQualifiedManagementFlg");
            _lastYear                  = String.valueOf((Integer.parseInt(_ctrlYear) - 1));
            _lastYearSemester          = getLastYearSemester(db2, _lastYear);
            _schoolName                = getSchoolName(db2);
            _towYearAgo                = String.valueOf((Integer.parseInt(_lastYear) - 1));
            _towYearAgoSemester        = getLastYearSemester(db2, _towYearAgo);
            _grdGrade                  = getGrdGrade(db2);
            _sophomoreGrade            = getSophomoreGrade(db2, _grdGrade);
            String[] seme             = getSemeAllSDateAndEDate(db2, _ctrlYear);
            _thisYearSDate             = seme[0];
            _thisYearEDate             = seme[1];
            seme                       = getSemeAllSDateAndEDate(db2, _lastYear);
            _lastYearSDate             = seme[0];
            _lastYearEDate             = seme[1];
        }

        private String getLastYearSemester(final DB2UDB db2, final String paramYear) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            String sql = " SELECT MAX(SEMESTER) AS SEMESTER FROM SEMESTER_MST WHERE YEAR='" + paramYear + "' AND SEMESTER <> '9' ";
            log.debug(" max semester sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTER");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
                throw e;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getSchoolName(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR='" + _ctrlYear + "' AND SCHOOLCD = '" + _schoolCd + "' AND SCHOOL_KIND = '" + _schoolKind + "'";
            log.debug(" school name sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
                throw e;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getGrdGrade(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            String sql = " SELECT NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "'";
            log.debug(" grd grade sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("NAMESPARE2");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getSophomoreGrade(final DB2UDB db2, final String grdGrade) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            String sql = " SELECT MAX(GRADE) AS GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND SCHOOL_KIND = '" + _schoolKind + "' AND GRADE < '" + grdGrade + "' ";
            log.debug(" sophomore grade sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("GRADE");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String[] getSemeAllSDateAndEDate(final DB2UDB db2, final String paramYear) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String[] rtn = new String[2];

            String sql = " SELECT SDATE, EDATE FROM SEMESTER_MST WHERE YEAR='" + paramYear + "' AND SEMESTER = '9' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn[0] = rs.getString("SDATE");
                    rtn[1] = rs.getString("EDATE");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
                throw e;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
}
