<?php

require_once('for_php7.php');

# kanji=漢字
# $Id: AttendAccumulate.php,v 1.7 2017/07/19 06:47:13 yamauchi Exp $

//共通関数
class AttendAccumulate{

    /**
     * ATTEND_SEMES_DATの情報を返す。
     *  -- Key = 学期＋月
     *  -- Val = Map：SDAY(開始日付)
     *              ：EDAY(終了日付)
     * @param db db
     * @param z010 近大か否かを判断する為の文字列
     * @param year 年度
     * @return ATTEND_SEMES_DATの各学期＋月毎の開始日付、終了日付を<code>Map</code>で返す。
     */
    function getAttendSemesMap(
            $db,
            $z010,
            $year
    ) {
        $rtnMap = array();
        $semesMap = array();

        $query = "SELECT SEMESTER, SDATE FROM SEMESTER_MST WHERE YEAR = '" . $year . "' AND SEMESTER < '9'";
        $result = $db->query($query);
        while ($rsSemes = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semesMap[$rsSemes["SEMESTER"]] = $rsSemes["SDATE"];
        }

        $query = AttendAccumulate::getAttendSemesAllSql($year);

        $result = $db->query($query);
        $defSday = "01";
        $nextYear = (int)$year + 1;

        $bef_seme = "";
        $startDay = "";
        while ($rsAttend = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $endDay = AttendAccumulate::getEndDay($rsAttend["ENDDAY"], $z010);
            if ($bef_seme != $rsAttend["SEMESTER"]) {
                $startDay = $semesMap[$rsAttend["SEMESTER"]];
            } else {
                $month = $rsAttend["MONTH"];

                $setYear = $rsAttend["MONTH"] <= 3 ? $nextYear : $year;

                $startDay = $setYear . "-" . $month . "-" . $defSday;
            }
            $dataMap = array();
            $dataMap["SDAY"] = $startDay;
            $dataMap["EDAY"] = $endDay;
            $dataMap["SM"]   = $rsAttend["SM"];
            $rtnMap[$rsAttend["SM_KEY"]] = $dataMap;

            $bef_seme = $rsAttend["SEMESTER"];
        }

        return $rtnMap;
    }

    function getEndDay($endDay, $z010) {
        if ("KINDAI" == $z010) {

            // 集計日が１の場合、翌月の１日と判断する
            $endDayArray = preg_split("/-/", $endDay);
            if ($endDayArray[2] == 1) {
                $dateAdd = mktime(0, 0, 0, (int)$endDayArray[1] + 1, $endDayArray[2], $endDayArray[0]);
                $endDayAddArray = getdate($dateAdd);
                $endDay = $endDayAddArray["year"]."-".$endDayAddArray["mon"]."-".$endDayAddArray["mday"];
            }
            return $endDay;
        } else {
            return $endDay;
        }
    }

    function getAttendSemesAllSql($year) {

        $query  = " WITH T_ATTEND AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER || MONTH AS SM, ";
        $query .= "         SEMESTER, ";
        $query .= "         MONTH, ";
        $query .= "         MAX((CASE WHEN MONTH BETWEEN '01' AND '03' ";
        $query .= "                   THEN RTRIM(CHAR(INT(YEAR) + 1)) ";
        $query .= "                   ELSE YEAR ";
        $query .= "              END ) || '-' || MONTH || '-' || APPOINTED_DAY) AS ENDDAY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '" . $year . "' ";
        $query .= "     GROUP BY ";
        $query .= "         SEMESTER, ";
        $query .= "         MONTH ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.SM, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.MONTH, ";
        $query .= "     T1.ENDDAY, ";
        $query .= "     (CASE WHEN INT(T1.MONTH) < 4 ";
        $query .= "           THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1)) ";
        $query .= "           ELSE T1.SEMESTER ";
        $query .= "      END ) || T1.MONTH AS SM_KEY ";
        $query .= " FROM ";
        $query .= "     T_ATTEND T1 ";
        $query .= " ORDER BY ";
        $query .= "     SM_KEY ";

        return $query;
    }

    function getPeiodValue(
            $db,
            $definecode,
            $year,
            $sSemester,
            $eSemester
    ) {
    //  校時名称
        $periodnum;
        $query .= " SELECT ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1, ";
        if ($definecode != "" && $definecode["usefromtoperiod"]) {
            $query .= "    CASE WHEN NAMECD2 BETWEEN S_PERIODCD AND E_PERIODCD THEN 1 ELSE 0 END AS ONPERIOD ";
        } else {
            $query .= "    1 AS ONPERIOD ";
        }
        if ($definecode != "" && $definecode["usefromtoperiod"]) {
            $query .= " FROM ";
            $query .= "    NAME_MST W1, ";
            $query .= "    COURSE_MST W2 ";
            $query .= " WHERE ";
            $query .= "    NAMECD1 = 'B001' ";
            $query .= "    AND COURSECD IN(SELECT ";
            $query .= "                        MIN(COURSECD) ";
            $query .= "                    FROM ";
            $query .= "                        SCHREG_REGD_DAT W3 ";
            $query .= "                    WHERE ";
            $query .= "                        W3.YEAR = '" . $year . "' ";
            $query .= "                        AND W3.SEMESTER BETWEEN '" . $sSemester . "' AND '" . $eSemester . "' ";
            $query .= "                    ) ";
        } else {
            $query .= " FROM ";
            $query .= "    NAME_MST W1 ";
            $query .= " WHERE ";
            $query .= "    NAMECD1 = 'B001' ";
        }
        $query .= "    AND EXISTS(SELECT ";
        $query .= "                   'X' ";
        $query .= "               FROM ";
        $query .= "                   NAME_YDAT W2 ";
        $query .= "               WHERE ";
        $query .= "                   W2.YEAR = '" . $year . "' ";
        $query .= "                   AND W2.NAMECD1 = 'B001' ";
        $query .= "                   AND W2.NAMECD2 = W1.NAMECD2) ";
        $query .= " ORDER BY ";
        $query .= "    NAMECD2 ";

        $result = $db->query($query);

        $periodlist = array();
        $sep = "";
        $i = 0;
        $stb2 = "";
        while ($rs = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($i >= 16) {
                break;
            }
            if ($rs["ONPERIOD"] == 1) {
                $periodlist[] = $rs["NAME1"];
            }
            if ($rs["ONPERIOD"] == 1) {
                if ($stb2 == "") {
                    $stb2 .= "(";
                }
                $stb2 .= $sep."'". $rs["NAMECD2"] ."'";
                $sep = ",";
            }
            $i++;
        }
        $result->free();

        $periodnum = get_count($periodlist) <= 9 ? 9: 16;

        if ($stb2 != "") {
            $stb2 .= ")";
        } else if ($periodnum == 9) {
            $stb2 = "('1','2','3','4','5','6','7','8','9')";
        } else {
            $stb2 = "('1','2','3','4','5','6','7','8','9','A','B','C','D','E','F''G')";
        }
        return $stb2;
    }

    /**
     * 出欠データの端数取得
     * @param attendSemAllMap   getAttendSemesMap()で取得したMap
     * @param sDate             指定範囲の開始日付
     * @param eDate             指定範囲の終了日付
     * @return 出欠データ端数取得の<code>Map</code>を返す
     * @throws ParseException
     */
    function getHasuuMap(
            $attendSemAllMap,
            $sDate,
            $eDate
    ) {
        $rtnMap = array();

        $befSemMonth = "";
        $befDayFrom = "";
        $befDayTo = "";
        $aftSemMonth = "";
        $aftDayFrom = "";
        $aftDayTo = "";
        $semeStrDay = "";
        $semesFlg = false;

        $attendSemesInState = "";
        $attendSemesInState .= "('";
        $sep = "";
        foreach ($attendSemAllMap as $key => $val) {
            $semeAllMap = $val;
            $sday = $semeAllMap["SDAY"];
            $eday = $semeAllMap["EDAY"];
            $sm = $semeAllMap["SM"];

            /** 指定開始日付以上且つ、指定終了日付以下 */
            if ($sday >= $sDate && 
                $eday <= $eDate
            ) {
                $attendSemesInState .= $sep.$sm;
                $sep = "','";

                $semeStrDay = $semeStrDay == "" ? $sday : $semeStrDay;
                $aftSemMonth = $key;
                $edateArray = preg_split("/-/", $eday);
                $dateAdd = mktime(0, 0, 0, $edateArray[1], (int)$edateArray[2] + 1, $edateArray[0]);
                $edayAddArray = getdate($dateAdd);
                $edayAdd = $edayAddArray["year"]."-".$edayAddArray["mon"]."-".$edayAddArray["mday"];
                $aftDayFrom = $edayAdd;
                $aftDayTo = $semeAllMap["EDAY"];

                $semesFlg = true;
            }

            if (!$semesFlg || $befSemMonth == "") {
                $befSemMonth = $key;
                $befDayFrom = $sday;
                $befDayTo = $eday;
            }
        }
        if ($semesFlg) {
            $befSday = $attendSemAllMap["befSemMonth"]["SDAY"];
            if ($sDate == $semeStrDay) {
                $befDayFrom = "";
                $befDayTo = "";
            } else {
                $befDayFrom = $sDate;
            }
            $aftEday = $attendSemAllMap["aftSemMonth"]["EDAY"];
            if ($eDate == $aftEday) {
                $aftDayFrom = "";
                $aftDayTo = "";
            } else if ($eDate == $befSday) {
                $aftDayFrom = "";
                $aftDayTo = "";
            } else {
                $aftDayTo = $eDate;
            }
        } else {
            $befDayFrom = $sDate;
            $befDayTo = $eDate;
            $aftDayFrom = "";
            $aftDayTo = "";
        }
        $attendSemesInState .= "')";
        // TODO: 戻り値は Map ではなく、独自のclass を設けた方がよくね?
        $rtnMap["semesFlg"] = $semesFlg;
        $rtnMap["attendSemesInState"] = $attendSemesInState;
        $rtnMap["befDayFrom"] = $befDayFrom;
        $rtnMap["befDayTo"] = $befDayTo;
        $rtnMap["aftDayFrom"] = $aftDayFrom;
        $rtnMap["aftDayTo"] = $aftDayTo;
        return $rtnMap;
    }

    /**
     * SCHOOL_MSTの情報を返す。
     *  -- Key = フィールド名
     *  -- Val = 値
     * @param db db
     * @param year 年度
     * @param schoolcd 学校コード
     * @param school_kind 校種
     * @return SCHOOL_MSTのフィールドと値を返す。
     */
    function getSchoolMstMap(
            $db,
            $year,
            $schoolcd="",
            $school_kind=""
    ) {
        $rtnMap = array();

        $query  = " SELECT * FROM V_SCHOOL_MST WHERE YEAR = '" . $year . "' ";
        if (strlen($schoolcd))      $query .= " AND SCHOOLCD    = '".$schoolcd."' ";
        if (strlen($school_kind))   $query .= " AND SCHOOL_KIND = '".$school_kind."' ";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $rtnMap[$key] = $val;
            }
        }

        return $rtnMap;
    }

    /**
     * 科目別出欠データSQLを返す
     * -- 学期またがり可
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * 実行結果の SEMESTER が "9" は学期の総合計
     * @param semesFlg      true:ATTEND_SUBCLASS_DAT使用
     * @param definecode    KNJDefineCode
     * @param defineSchoolCode    KNJDefineSchool
     * @param year          年度
     * @param sSemester     対象学期範囲From
     * @param eSemester     対象学期範囲To
     * @param semesInState  ATTEND_SUBCLASS_DATの対象(学期＋月)
     * @param periodInState 対象校時
     * @param befDayFrom    開始日付の端数用From
     * @param befDayTo      開始日付の端数用To
     * @param aftDayFrom    終了日付の端数用From
     * @param aftDayTo      終了日付の端数用To
     * @param grade         学年：指定しない場合は、""
     * @param hrClass       クラス：指定しない場合は、""
     * @param schregno      学籍番号：指定しない場合は、""
     * @return 出欠データSQL<code>String</code>を返す
     */
    function getAttendSubclassSql (
            $semesFlg,
            $definecode,
            $defineSchoolCode,
            $knjSchoolMst,
            $year,
            $sSemester,
            $eSemester,
            $semesInState,
            $periodInState,
            $befDayFrom,
            $befDayTo,
            $aftDayFrom,
            $aftDayTo,
            $grade,
            $hrClass,
            $schregno
    ) {
        //対象生徒
        $query  = " WITH SCHNO AS( ";
        $query .= " SELECT ";
        $query .= "    W1.SCHREGNO, ";
        $query .= "    W1.GRADE, ";
        $query .= "    W1.SEMESTER, ";
        $query .= "    W1.HR_CLASS, ";
        $query .= "    W1.COURSECD, ";
        $query .= "    W1.MAJORCD, ";
        $query .= "    W1.COURSECODE ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT W1 ";
        $query .= " WHERE ";
        $query .= "    W1.YEAR = '". $year ."' ";
        $query .= "    AND W1.SEMESTER BETWEEN '". $sSemester ."' AND '". $eSemester ."' ";
        if ($schregno != "") {
            $query .= "    AND W1.SCHREGNO = '". $schregno ."' ";
        }
        if ($grade != "") {
            $query .= "    AND W1.GRADE = '". $grade ."' ";
        }
        if ($hrClass != "") {
            $query .= "    AND W1.HR_CLASS = '". $hrClass ."' ";
        }

        //端数計算有無の判定
        if ($befDayFrom != "" || $aftDayFrom != "") {
            //対象生徒の時間割データ
            $query .= " ), SCHEDULE_SCHREG_R AS( ";
            $query .= " SELECT ";
            $query .= "    T2.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.CHAIRCD, ";
            $query .= "    T1.PERIODCD ";
            $query .= " FROM ";
            $query .= "    SCH_CHR_DAT T1, ";
            $query .= "    CHAIR_STD_DAT T2 ";
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '". $year ."' ";
            $query .= "    AND T1.SEMESTER BETWEEN '". $sSemester ."' AND '". $eSemester ."' ";
            $query .= "    AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
            if ($befDayFrom != "" && $aftDayFrom != "") {
                $query .= "    AND (T1.EXECUTEDATE BETWEEN '". $befDayFrom ."' AND '". $befDayTo ."' ";
                $query .= "         OR T1.EXECUTEDATE BETWEEN '". $aftDayFrom ."' AND '". $aftDayTo ."') ";
            } else if ($befDayFrom != "") {
                $query .= "    AND T1.EXECUTEDATE BETWEEN '". $befDayFrom ."' AND '". $befDayTo ."' ";
            } else if ($aftDayFrom != "") {
                $query .= "    AND T1.EXECUTEDATE BETWEEN '". $aftDayFrom ."' AND '". $aftDayTo ."' ";
            }
            $query .= "    AND T1.YEAR = T2.YEAR ";
            $query .= "    AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "    AND T1.CHAIRCD = T2.CHAIRCD ";
            if ($definecode != "" && $definecode["usefromtoperiod"]) {
                $query .= "    AND T1.PERIODCD IN ". $periodInState ." ";
            }
            $query .= "    AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO GROUP BY SCHREGNO) ";
            $query .= "    AND NOT EXISTS(SELECT ";
            $query .= "                       'X' ";
            $query .= "                   FROM ";
            $query .= "                       SCHREG_BASE_MST T3 ";
            $query .= "                   WHERE ";
            $query .= "                       T3.SCHREGNO = T2.SCHREGNO ";
            $query .= "                       AND ((ENT_DIV IN('4', '5') AND EXECUTEDATE < ENT_DATE) ";
            $query .= "                             OR (GRD_DIV IN('2', '3') AND EXECUTEDATE > GRD_DATE)) ";
            $query .= "                  ) ";
            $query .= " GROUP BY ";
            $query .= "    T2.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.CHAIRCD, ";
            $query .= "    T1.PERIODCD ";

            $query .= " ), SCHEDULE_SCHREG AS( ";
            $query .= " SELECT ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.PERIODCD ";
            $query .= " FROM ";
            $query .= "    SCHEDULE_SCHREG_R T1 ";
            $query .= " WHERE ";
            $query .= "    NOT EXISTS(SELECT ";
            $query .= "                       'X' ";
            $query .= "                   FROM ";
            $query .= "                       SCHREG_TRANSFER_DAT T3 ";
            $query .= "                   WHERE ";
            $query .= "                       T3.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND TRANSFERCD IN('1','2') ";
            $query .= "                       AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
            $query .= "                  ) ";
            $query .= " GROUP BY ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.PERIODCD ";
        }
        
        //端数計算有無の判定
        if ($befDayFrom != "" || $aftDayFrom != "") {
            //対象生徒の出欠データ
            $query .= " ), T_ATTEND_DAT AS ( ";
            $query .= " SELECT ";
            $query .= "    T0.CHAIRCD, ";
            $query .= "    T2.SUBCLASSCD, ";
            $query .= "    T0.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T0.ATTENDDATE, ";
            $query .= "    T0.PERIODCD, ";
            $query .= "    T0.DI_CD ";
            $query .= " FROM ";
            $query .= "    ATTEND_DAT T0, ";
            $query .= "    SCHEDULE_SCHREG T1, ";
            $query .= "    CHAIR_DAT T2 ";
            $query .= " WHERE ";
            $query .= "    T0.YEAR = '". $year ."' ";
            if ($befDayFrom != "" && $aftDayFrom != "") {
                $query .= "    AND (T0.ATTENDDATE BETWEEN '". $befDayFrom ."' AND '". $befDayTo ."' ";
                $query .= "         OR T0.ATTENDDATE BETWEEN '". $aftDayFrom ."' AND '". $aftDayTo ."') ";
            } else if ($befDayFrom != "") {
                $query .= "    AND T0.ATTENDDATE BETWEEN '". $befDayFrom ."' AND '". $befDayTo ."' ";
            } else if ($aftDayFrom != "") {
                $query .= "    AND T0.ATTENDDATE BETWEEN '". $aftDayFrom ."' AND '". $aftDayTo ."' ";
            }
            $query .= "    AND T0.SCHREGNO = T1.SCHREGNO ";
            $query .= "    AND T0.ATTENDDATE = T1.EXECUTEDATE ";
            $query .= "    AND T0.PERIODCD = T1.PERIODCD ";
            $query .= "    AND T2.YEAR = T0.YEAR ";
            $query .= "    AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "    AND T2.CHAIRCD = T0.CHAIRCD ";
            // 休学中の授業日数
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     T1.CHAIRCD, ";
            $query .= "     T3.SUBCLASSCD, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     VALUE(T1.SEMESTER, '9') AS SEMESTER, ";
            $query .= "     T1.EXECUTEDATE AS ATTENDDATE, ";
            $query .= "     T1.PERIODCD, ";
            $query .= "     '00' AS DI_CD ";
            $query .= " FROM ";
            $query .= "     SCHEDULE_SCHREG_R T1, ";
            $query .= "     SCHREG_TRANSFER_DAT T2, ";
            $query .= "     CHAIR_DAT T3 ";
            $query .= " WHERE ";
            $query .= "     EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHNO E1 ";
            $query .= "             WHERE ";
            $query .= "                 E1.SCHREGNO = T2.SCHREGNO ";
            $query .= "             ) ";
            $query .= "     AND T2.TRANSFERCD IN('2') ";
            $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "     AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ";
            $query .= "     AND T3.YEAR = '". $year ."' ";
            $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
            $query .= "     AND T3.CHAIRCD = T1.CHAIRCD  ";
            $query .= " GROUP BY ";
            $query .= "     GROUPING SETS ((T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD), (T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD)) ";
        }
        
        if ($befDayFrom != "" || $aftDayFrom != "") {
            //テスト項目マスタの集計フラグの表
            $query .= " ), TEST_COUNTFLG AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.EXECUTEDATE, ";
            $query .= "         T1.PERIODCD, ";
            $query .= "         T1.CHAIRCD, ";
            $query .= "         '2' AS DATADIV ";
            $query .= "     FROM ";
            $query .= "         SCH_CHR_TEST T1, ";
            $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '". $year ."' ";
            if ($befDayFrom != "" && $aftDayFrom != "") {
                $query .= "     AND (T1.EXECUTEDATE BETWEEN '". $befDayFrom ."' AND '". $befDayTo ."' ";
                $query .= "       OR T1.EXECUTEDATE BETWEEN '". $aftDayFrom ."' AND '". $aftDayTo ."') ";
            } else if ($befDayFrom != "") {
                $query .= "     AND T1.EXECUTEDATE BETWEEN '". $befDayFrom ."' AND '". $befDayTo ."' ";
            } else if ($aftDayFrom != "") {
                $query .= "     AND T1.EXECUTEDATE BETWEEN '". $aftDayFrom ."' AND '". $aftDayTo ."' ";
            }
            $query .= "         AND T2.YEAR       = T1.YEAR ";
            $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
            $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
            $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
            $query .= "     GROUP BY ";
            $query .= "         T1.EXECUTEDATE, ";
            $query .= "         T1.PERIODCD, ";
            $query .= "         T1.CHAIRCD ";
            //時間割のDATADIVの表
            $query .= " ), T_DATADIV AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.EXECUTEDATE, ";
            $query .= "         T1.PERIODCD, ";
            $query .= "         T1.CHAIRCD, ";
            $query .= "         T1.DATADIV ";
            $query .= "     FROM ";
            $query .= "         SCH_CHR_DAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '". $year ."' ";
            if ($befDayFrom != "" && $aftDayFrom != "") {
                $query .= "     AND (T1.EXECUTEDATE BETWEEN '". $befDayFrom ."' AND '". $befDayTo ."' ";
                $query .= "       OR T1.EXECUTEDATE BETWEEN '". $aftDayFrom ."' AND '". $aftDayTo ."') ";
            } else if ($befDayFrom != "") {
                $query .= "     AND T1.EXECUTEDATE BETWEEN '". $befDayFrom ."' AND '". $befDayTo ."' ";
            } else if ($aftDayFrom != "") {
                $query .= "     AND T1.EXECUTEDATE BETWEEN '". $aftDayFrom ."' AND '". $aftDayTo ."' ";
            }
            $query .= "     GROUP BY ";
            $query .= "         T1.EXECUTEDATE, ";
            $query .= "         T1.PERIODCD, ";
            $query .= "         T1.CHAIRCD, ";
            $query .= "         T1.DATADIV ";
        }

        //生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
        $query .= "), SCH_ATTEND_SUM AS( ";
        //端数計算有無の判定
        if ($befDayFrom != "" || $aftDayFrom != "") {
            $query .= "    SELECT ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        T1.SUBCLASSCD, ";
            $query .= "        T1.SEMESTER, ";
            $query .= "        SUM(CASE WHEN DI_CD IN( ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "    '00', ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "    '2', '9', ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "    '19', '20', ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "    '3', '10', ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "    '1', '8', ";
            }
            $query .= "        '4','5','6','14','11','12','13') THEN 1 ELSE 0 END) ";
            $query .= "        AS ABSENT1, ";
            $query .= "        SUM(CASE WHEN DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(L1.ABBV2, '1')) ELSE 0 END)AS LATE_EARLY ";
            $query .= "    FROM ";
            $query .= "        T_ATTEND_DAT T1 ";
            $query .= "        INNER JOIN T_DATADIV DAT ";
            $query .= "                           ON DAT.EXECUTEDATE = T1.ATTENDDATE ";
            $query .= "                          AND DAT.PERIODCD    = T1.PERIODCD ";
            $query .= "                          AND DAT.CHAIRCD     = T1.CHAIRCD ";
            $query .= "        LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C001' AND L1.NAMECD2 = T1.DI_CD ";
            $query .= "        , SCHNO T0 ";
            $query .= "    WHERE ";
            $query .= "        T1.SCHREGNO = T0.SCHREGNO ";
            $query .= "        AND T1.SEMESTER = T0.SEMESTER ";
            if ($definecode["useschchrcountflg"]) {
                $query .= "        AND NOT EXISTS(SELECT ";
                $query .= "                           'X' ";
                $query .= "                       FROM ";
                $query .= "                           SCH_CHR_COUNTFLG T4 ";
                $query .= "                       WHERE ";
                $query .= "                           T4.EXECUTEDATE = T1.ATTENDDATE ";
                $query .= "                           AND T4.PERIODCD = T1.PERIODCD ";
                $query .= "                           AND T4.CHAIRCD = T1.CHAIRCD ";
                $query .= "                           AND T4.GRADE = T0.GRADE ";
                $query .= "                           AND T4.HR_CLASS = T0.HR_CLASS ";
                $query .= "                           AND DAT.DATADIV IN ('0','1') "; //テスト(DATADIV=2)以外
                $query .= "                           AND T4.COUNTFLG = '0') ";
                $query .= "        AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
                $query .= "                        WHERE TEST.EXECUTEDATE = T1.ATTENDDATE ";
                $query .= "                          AND TEST.PERIODCD    = T1.PERIODCD ";
                $query .= "                          AND TEST.CHAIRCD     = T1.CHAIRCD ";
                $query .= "                          AND TEST.DATADIV     = DAT.DATADIV) "; //テスト(DATADIV=2)
            }
            $query .= "    GROUP BY ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        T1.SUBCLASSCD, ";
            $query .= "        T1.SEMESTER ";
            $query .= "    UNION ALL ";
        }
        $query .= "    SELECT ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        T1.SUBCLASSCD, ";
        $query .= "        SEMESTER, ";
        $query .= "        SUM(VALUE(T1.SICK,0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) + VALUE(T1.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "            + VALUE(T1.OFFDAYS, 0) ";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .= "            + VALUE(T1.SUSPEND, 0) ";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .= "            + VALUE(T1.VIRUS, 0) ";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .= "            + VALUE(T1.MOURNING, 0) ";
        }
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .= "            + VALUE(T1.ABSENT, 0) ";
        }

        $query .= "        ) AS ABSENT1, ";
        $query .= "        SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ";
        $query .= "    FROM ";
        $query .= "        ATTEND_SUBCLASS_DAT T1 ";
        $query .= "    WHERE ";
        $query .= "        YEAR = '". $year ."' ";
        $query .= "        AND SEMESTER BETWEEN '". $sSemester ."' AND '". $eSemester ."' ";
        $query .= "        AND T1.SEMESTER || T1.MONTH  IN ". $semesInState ." ";
        $query .= "        AND EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       SCHNO T2 ";
        $query .= "                   WHERE ";
        $query .= "                       T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                   GROUP BY ";
        $query .= "                       SCHREGNO) ";
        $query .= "    GROUP BY ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        T1.SEMESTER, ";
        $query .= "        T1.SUBCLASSCD ";
        
        //ペナルティー欠課を加味した生徒欠課集計の表（出欠データと集計テーブルを合算）
        $query .= "), ATTEND_B AS( ";
        if ($defineSchoolCode["ABSENT_COV"] == 1) {
            //学期でペナルティ欠課を算出する場合
            $query .= "    SELECT ";
            $query .= "        SCHREGNO, ";
            $query .= "        SUBCLASSCD, ";
            $query .= "        VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1, ";
            $query .= "        VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2, ";
            $query .= "        VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3, ";
            $query .= "        VALUE(SUM(ABSENT),0) AS ABSENT_SEM9 ";
            $query .= "    FROM (SELECT ";
            $query .= "              SCHREGNO, ";
            $query .= "              SUBCLASSCD, ";
            $query .= "              SEMESTER, ";
            $query .= "              VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / ". $defineSchoolCode["ABSENT_COV_LATE"] ." AS ABSENT ";
            $query .= "          FROM ";
            $query .= "              SCH_ATTEND_SUM T1 ";
            $query .= "          GROUP BY ";
            $query .= "              SCHREGNO, ";
            $query .= "              SUBCLASSCD, ";
            $query .= "              SEMESTER ";
            $query .= "          ) T1 ";
            $query .= "    GROUP BY ";
            $query .= "        SCHREGNO, ";
            $query .= "        SUBCLASSCD ";
        } else if ($defineSchoolCode["ABSENT_COV"] == 2) {
            //通年でペナルティ欠課を算出する場合 
            //学期の欠課時数は学期別で換算したペナルティ欠課を加算、学年の欠課時数は年間で換算する
            $query .= "    SELECT ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        T1.SUBCLASSCD, ";
            $query .= "        T1.ABSENT_SEM9, ";
            $query .= "        T2.ABSENT_SEM1, ";
            $query .= "        T2.ABSENT_SEM2, ";
            $query .= "        T2.ABSENT_SEM3 ";
            $query .= "    FROM ";
            $query .= "        (SELECT ";
            $query .= "             SCHREGNO, ";
            $query .= "             SUBCLASSCD, ";
            $query .= "             VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / ". $defineSchoolCode["ABSENT_COV_LATE"] ." AS ABSENT_SEM9 ";
            $query .= "         FROM ";
            $query .= "             SCH_ATTEND_SUM T1 ";
            $query .= "         GROUP BY ";
            $query .= "             SCHREGNO, ";
            $query .= "             SUBCLASSCD ";
            $query .= "        )T1, ";
            $query .= "        (SELECT ";
            $query .= "             SCHREGNO, ";
            $query .= "             SUBCLASSCD, ";
            $query .= "             VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1, ";
            $query .= "             VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2, ";
            $query .= "             VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3 ";
            $query .= "         FROM ";
            $query .= "             (SELECT ";
            $query .= "                  SCHREGNO, ";
            $query .= "                  SUBCLASSCD, ";
            $query .= "                  SEMESTER, ";
            $query .= "                  VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / ". $defineSchoolCode["ABSENT_COV_LATE"] ." AS ABSENT ";
            $query .= "              FROM ";
            $query .= "                  SCH_ATTEND_SUM T1 ";
            $query .= "              GROUP BY ";
            $query .= "                  SCHREGNO, ";
            $query .= "                  SUBCLASSCD, ";
            $query .= "                  SEMESTER ";
            $query .= "              ) T1 ";
            $query .= "         GROUP BY ";
            $query .= "             SCHREGNO, ";
            $query .= "             SUBCLASSCD ";
            $query .= "        ) T2 ";
            $query .= "    WHERE ";
            $query .= "        T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "        AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        } else {
            //ペナルティ欠課なしの場合
            $query .= "    SELECT ";
            $query .= "        SCHREGNO, ";
            $query .= "        SUBCLASSCD, ";
            $query .= "        VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM1, ";
            $query .= "        VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM2, ";
            $query .= "        VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM3, ";
            $query .= "        VALUE(SUM(ABSENT1),0) AS ABSENT_SEM9 ";
            $query .= "    FROM ";
            $query .= "        SCH_ATTEND_SUM T1 ";
            $query .= "    GROUP BY ";
            $query .= "        SCHREGNO, ";
            $query .= "        SUBCLASSCD ";
        }
        //単位マスタの欠課時数上限値
        $query .= " )   ";
        $query .= " ,T_CREDIT AS ( ";
        $query .= " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.ABSENCE_HIGH ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST T1, ";
        $query .= "     (SELECT SCHREGNO,GRADE,COURSECD,MAJORCD,COURSECODE FROM SCHNO ";
        $query .= "     GROUP BY SCHREGNO,GRADE,COURSECD,MAJORCD,COURSECODE) T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='". $year ."' AND ";
        $query .= "     T1.COURSECD=T2.COURSECD AND ";
        $query .= "     T1.MAJORCD=T2.MAJORCD AND ";
        $query .= "     T1.GRADE=T2.GRADE AND ";
        $query .= "     T1.COURSECODE=T2.COURSECODE ";
        $query .= " )   ";
        //合併先科目の欠課時数
        $query .= " , ATTEND_COMBINED AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     SUM(CASE WHEN T2.CALCULATE_CREDIT_FLG='2' AND T3.ABSENCE_HIGH < T1.ABSENT_SEM1 THEN 0 ELSE T1.ABSENT_SEM1 END) AS ABSENT_SEM1, ";
        $query .= "     SUM(CASE WHEN T2.CALCULATE_CREDIT_FLG='2' AND T3.ABSENCE_HIGH < T1.ABSENT_SEM2 THEN 0 ELSE T1.ABSENT_SEM2 END) AS ABSENT_SEM2, ";
        $query .= "     SUM(CASE WHEN T2.CALCULATE_CREDIT_FLG='2' AND T3.ABSENCE_HIGH < T1.ABSENT_SEM3 THEN 0 ELSE T1.ABSENT_SEM3 END) AS ABSENT_SEM3, ";
        $query .= "     SUM(CASE WHEN T2.CALCULATE_CREDIT_FLG='2' AND T3.ABSENCE_HIGH < T1.ABSENT_SEM9 THEN 0 ELSE T1.ABSENT_SEM9 END) AS ABSENT_SEM9 ";
        $query .= " FROM ";
        $query .= "     ATTEND_B T1 ";
        $query .= "     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ";
        $query .= "              ON T2.REPLACECD='1' ";
        $query .= "             AND T2.YEAR='". $year ."' ";
        $query .= "             AND T2.ATTEND_SUBCLASSCD=T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN T_CREDIT T3 ";
        $query .= "              ON T3.SCHREGNO=T1.SCHREGNO ";
        $query .= "             AND T3.SUBCLASSCD=T1.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.COMBINED_SUBCLASSCD ";
        $query .= " )   ";

        //メイン表
        $query .= " SELECT ";
        $query .= "    SCHREGNO, ";
        $query .= "    SUBCLASSCD, ";
        $query .= "    '1' AS SEMESTER, ";
        $query .= "    ABSENT_SEM1 AS ABSENT_SEM ";
        $query .= " FROM ";
        $query .= "    ATTEND_B TT0 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "    SCHREGNO, ";
        $query .= "    SUBCLASSCD, ";
        $query .= "    '2' AS SEMESTER, ";
        $query .= "    ABSENT_SEM2 AS ABSENT_SEM ";
        $query .= " FROM ";
        $query .= "    ATTEND_B TT0 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "    SCHREGNO, ";
        $query .= "    SUBCLASSCD, ";
        $query .= "    '3' AS SEMESTER, ";
        $query .= "    ABSENT_SEM3 AS ABSENT_SEM ";
        $query .= " FROM ";
        $query .= "    ATTEND_B TT0 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "    SCHREGNO, ";
        $query .= "    SUBCLASSCD, ";
        $query .= "    '9' AS SEMESTER, ";
        $query .= "    ABSENT_SEM9 AS ABSENT_SEM ";
        $query .= " FROM ";
        $query .= "    ATTEND_B ";
        $query .= " UNION   ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     '1' AS SEMESTER, ";
        $query .= "     ABSENT_SEM1 AS ABSENT_SEM ";
        $query .= " FROM ";
        $query .= "     ATTEND_COMBINED TT0 ";
        $query .= " UNION   ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     '2' AS SEMESTER, ";
        $query .= "     ABSENT_SEM2 AS ABSENT_SEM ";
        $query .= " FROM ";
        $query .= "     ATTEND_COMBINED TT0 ";
        $query .= " UNION   ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     '3' AS SEMESTER, ";
        $query .= "     ABSENT_SEM3 AS ABSENT_SEM ";
        $query .= " FROM ";
        $query .= "     ATTEND_COMBINED TT0 ";
        $query .= " UNION   ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     ABSENT_SEM9 AS ABSENT_SEM ";
        $query .= " FROM ";
        $query .= "     ATTEND_COMBINED ";
        $query .= " ORDER BY ";
        $query .= "    SCHREGNO, ";
        $query .= "    SEMESTER ";

        return $query;
    }

    /**
     * 出欠データSQLを返す
     * -- 学期またがり可
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * -- 実行結果の SEMESTER が "9" は学期の総合計
     * -- 学年、クラス、学籍番号に"?"を指定したときはPreparedStatementで代入する
     * @param $semesFlg      true:ATTEND_SEMES_DAT使用
     * @param $defineSchool  KNJDefineSchool
     * @param $year          年度
     * @param $sSemester     対象学期範囲From
     * @param $eSemester     対象学期範囲To
     * @param $semesInState  ATTEND_SEMES_DATの対象(学期＋月)
     * @param $periodInState 対象校時
     * @param $befDayFrom    開始日付の端数用From
     * @param $befDayTo      開始日付の端数用To
     * @param $aftDayFrom    終了日付の端数用From
     * @param $aftDayTo      終了日付の端数用To
     * @param $grade         学年：指定しない場合は、Null
     * @param $hrClass       クラス：指定しない場合は、Null
     * @param $schregno      学籍番号：指定しない場合は、Null
     * @param $groupByDiv    グループ化区分：HR_CLASS(クラス単位)、GRADE(学年単位)、SCHREGNO(学籍単位)、SEMESTER(生徒学期単位)
     * @param $useCurriculumcd 1=教育課程コードを使用する
     * @return 出欠データSQL<code>String</code>を返す
     */
    function getAttendSemesSql(
            $semesFlg,
            $definecode,
            $knjSchoolMst,
            $year,
            $sSemester,
            $eSemester,
            $semesInState,
            $periodInState,
            $befDayFrom,
            $befDayTo,
            $aftDayFrom,
            $aftDayTo,
            $grade,
            $hrClass,
            $schregno,
            $groupByDiv,
            $useCurriculumcd
    ) {
        $query = "";
        //対象生徒
        $query .= "WITH SCHNO0 AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "    W1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT W1 ";
        $query .= " WHERE ";
        $query .= "    W1.YEAR = '".$year."' ";
        $query .= "    AND W1.SEMESTER BETWEEN '".$sSemester."' AND '".$eSemester."' ";
        if ($schregno != '') {
            $query .= "    AND W1.SCHREGNO = '".$schregno."' ";
        }
        if ($grade != '') {
            $query .= "    AND W1.GRADE = '".$grade."' ";
        }
        if ($hrClass != '') {
            $query .= "    AND W1.HR_CLASS = '".$hrClass."' ";
        }
        $query .= " ), SCHNO AS( ";
        $query .= " SELECT ";
        $query .= "    W1.SCHREGNO, ";
        $query .= "    W1.GRADE, ";
        $query .= "    W1.SEMESTER, ";
        $query .= "    W1.HR_CLASS ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT W1, SCHNO0 W2 ";
        $query .= " WHERE ";
        $query .= "    W1.YEAR = '".$year."' ";
        $query .= "    AND W1.SEMESTER BETWEEN '".$sSemester."' AND '".$eSemester."' ";
        $query .= "    AND W1.SCHREGNO = W2.SCHREGNO ";

        if ($befDayFrom != '' || $aftDayFrom != '') {
            //対象生徒の時間割データ
            $query .= " ), SCHEDULE_SCHREG_R AS( ";
            $query .= " SELECT ";
            $query .= "    T2.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.PERIODCD, ";
            $query .= "    T1.DATADIV ";
            $query .= " FROM ";
            $query .= "    SCH_CHR_DAT T1, ";
            $query .= "    CHAIR_STD_DAT T2 ";
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '".$year."' ";
            $query .= "    AND T1.SEMESTER BETWEEN '".$sSemester."' AND '".$eSemester."' ";
            $query .= "    AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
            if ($befDayFrom != '' && $aftDayFrom != '') {
                $query .= "    AND (T1.EXECUTEDATE BETWEEN '".$befDayFrom."' AND '".$befDayTo."' ";
                $query .= "         OR T1.EXECUTEDATE BETWEEN '".$aftDayFrom."' AND '".$aftDayTo."') ";
            } else if ($befDayFrom != '') {
                $query .= "    AND T1.EXECUTEDATE BETWEEN '".$befDayFrom."' AND '".$befDayTo."' ";
            } else if ($aftDayFrom != '') {
                $query .= "    AND T1.EXECUTEDATE BETWEEN '".$aftDayFrom."' AND '".$aftDayTo."' ";
            }
            $query .= "    AND T1.YEAR = T2.YEAR ";
            $query .= "    AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "    AND T1.CHAIRCD = T2.CHAIRCD ";
            if ($definecode != '' && $definecode["usefromtoperiod"])
                $query .= "    AND T1.PERIODCD IN ".$periodInState." ";
            $query .= "    AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO GROUP BY SCHREGNO) ";
            $query .= "    AND NOT EXISTS(SELECT ";
            $query .= "                       'X' ";
            $query .= "                   FROM ";
            $query .= "                       SCHREG_BASE_MST T3 ";
            $query .= "                   WHERE ";
            $query .= "                       T3.SCHREGNO = T2.SCHREGNO ";
            $query .= "                       AND ((ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE) ";
            $query .= "                             OR (GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE)) ";
            $query .= "                  ) ";
            $query .= " GROUP BY ";
            $query .= "    T2.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.PERIODCD, ";
            $query .= "    T1.DATADIV ";

            $query .= " ), SCHEDULE_SCHREG AS( ";
            $query .= " SELECT ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.PERIODCD, ";
            $query .= "    T1.DATADIV ";
            $query .= " FROM ";
            $query .= "    SCHEDULE_SCHREG_R T1 ";
            $query .= " WHERE ";
            $query .= "    NOT EXISTS(SELECT ";
            $query .= "                       'X' ";
            $query .= "                   FROM ";
            $query .= "                       SCHREG_TRANSFER_DAT T3 ";
            $query .= "                   WHERE ";
            $query .= "                       T3.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND TRANSFERCD IN('1','2') ";
            $query .= "                       AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
            $query .= "                  ) ";
            $query .= " GROUP BY ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.PERIODCD, ";
            $query .= "    T1.DATADIV ";
            
            //対象生徒の出欠データ
            $query .= " ), T_ATTEND_DAT AS( ";
            $query .= " SELECT ";
            $query .= "    T0.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T0.ATTENDDATE, ";
            $query .= "    T0.PERIODCD, ";
            $query .= "    T0.DI_CD, ";
            $query .= "    T1.DATADIV ";
            $query .= " FROM ";
            $query .= "    ATTEND_DAT T0, ";
            $query .= "    SCHEDULE_SCHREG T1 ";
            $query .= " WHERE ";
            $query .= "    T0.YEAR = '".$year."' ";
            if ($befDayFrom != '' && $aftDayFrom != '') {
                $query .= "    AND (T0.ATTENDDATE BETWEEN '".$befDayFrom."' AND '".$befDayTo."' ";
                $query .= "         OR T0.ATTENDDATE BETWEEN '".$aftDayFrom."' AND '".$aftDayTo."') ";
            } else if ($befDayFrom != '') {
                $query .= "    AND T0.ATTENDDATE BETWEEN '".$befDayFrom."' AND '".$befDayTo."' ";
            } else if ($aftDayFrom != '') {
                $query .= "    AND T0.ATTENDDATE BETWEEN '".$aftDayFrom."' AND '".$aftDayTo."' ";
            }
            $query .= "    AND T0.SCHREGNO = T1.SCHREGNO ";
            $query .= "    AND T0.ATTENDDATE = T1.EXECUTEDATE ";
            $query .= "    AND T0.PERIODCD = T1.PERIODCD ";
            
            //対象生徒の科目コードごとの出欠データ
            $query .= " ), T_ATTEND_DAT_SUBCLASS AS( ";
            $query .= " SELECT ";
            $query .= "    T0.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T0.ATTENDDATE, ";
            $query .= "    T0.PERIODCD, ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T2.CLASSCD, ";
                $query .= "    T2.CURRICULUM_CD, ";
                $query .= "    T2.SCHOOL_KIND, ";
            }
            $query .= "    T2.SUBCLASSCD, ";
            $query .= "    T0.DI_CD ";
            $query .= " FROM ";
            $query .= "    ATTEND_DAT T0, ";
            $query .= "    SCHEDULE_SCHREG T1, ";
            $query .= "    CHAIR_DAT T2 ";
            $query .= " WHERE ";
            $query .= "    T0.YEAR = '".$year."' ";
            if ($befDayFrom != '' && $aftDayFrom != '') {
                $query .= "    AND (T0.ATTENDDATE BETWEEN '".$befDayFrom."' AND '".$befDayTo."' ";
                $query .= "         OR T0.ATTENDDATE BETWEEN '".$aftDayFrom."' AND '".$aftDayTo."') ";
            } else if ($befDayFrom != '') {
                $query .= "    AND T0.ATTENDDATE BETWEEN '".$befDayFrom."' AND '".$befDayTo."' ";
            } else if ($aftDayFrom != '') {
                $query .= "    AND T0.ATTENDDATE BETWEEN '".$aftDayFrom."' AND '".$aftDayTo."' ";
            }
            $query .= "    AND T0.SCHREGNO = T1.SCHREGNO ";
            $query .= "    AND T0.ATTENDDATE = T1.EXECUTEDATE ";
            $query .= "    AND T0.PERIODCD = T1.PERIODCD ";
            $query .= "    AND T2.YEAR = T0.YEAR ";
            $query .= "    AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "    AND T2.CHAIRCD = T0.CHAIRCD ";
            
            //対象生徒の出欠データ（忌引・出停した日）
            $query .= " ), T_ATTEND_DAT_B AS( ";
            $query .= " SELECT ";
            $query .= "    T0.SCHREGNO, ";
            $query .= "    T0.SEMESTER, ";
            $query .= "    T0.ATTENDDATE, ";
            $query .= "    MIN(T0.PERIODCD) AS FIRST_PERIOD, ";
            $query .= "    COUNT(DISTINCT T0.PERIODCD) AS PERIOD_CNT ";
            $query .= " FROM ";
            $query .= "    T_ATTEND_DAT T0 ";
            $query .= " WHERE ";
            $query .= "    DI_CD IN('2','3','9','10') ";
            $query .= " GROUP BY ";
            $query .= "    T0.SCHREGNO, ";
            $query .= "    T0.SEMESTER, ";
            $query .= "    T0.ATTENDDATE ";
            
            //対象生徒の日単位の最小校時・最大校時・校時数
            $query .= " ), T_PERIOD_CNT AS( ";
            $query .= " SELECT ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    MIN(T1.PERIODCD) AS FIRST_PERIOD, ";
            $query .= "    MAX(T1.PERIODCD) AS LAST_PERIOD, ";
            $query .= "    COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ";
            $query .= " FROM ";
            $query .= "    SCHEDULE_SCHREG T1 ";
            $query .= " GROUP BY ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE ";

            if ("1" == $knjSchoolMst["SYUKESSEKI_HANTEI_HOU"]) {
                //対象生徒の日単位のデータ（忌引・出停した日）
                $query .= " ), T_PERIOD_SUSPEND_MOURNING AS( ";
                $query .= " SELECT ";
                $query .= "    T0.SCHREGNO, ";
                $query .= "    T0.EXECUTEDATE ";
                $query .= " FROM ";
                $query .= "    T_PERIOD_CNT T0, ";
                $query .= "    T_ATTEND_DAT_B T1 ";
                $query .= " WHERE ";
                $query .= "        T0.SCHREGNO = T1.SCHREGNO ";
                $query .= "    AND T0.EXECUTEDATE = T1.ATTENDDATE ";
                $query .= "    AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ";
                $query .= "    AND T0.PERIOD_CNT = T1.PERIOD_CNT ";
            }
            
            $query .= " ), T_KESSEKI AS ( ";
            $query .= "    SELECT ";
            $query .= "        W0.SCHREGNO, ";
            $query .= "        VALUE(W1.SEMESTER, '9') AS SEMESTER, ";
            $query .= "        W0.DI_CD, ";
            $query .= "        W0.ATTENDDATE ";
            $query .= "    FROM ";
            $query .= "        ATTEND_DAT W0, ";
            $query .= "        (SELECT ";
            $query .= "             T0.SCHREGNO, ";
            $query .= "             T0.SEMESTER, ";
            $query .= "             T0.EXECUTEDATE, ";
            if ("1" == $knjSchoolMst["SYUKESSEKI_HANTEI_HOU"]) {
                $query .= "         T2.FIRST_PERIOD ";
            } else {
                $query .= "         T0.FIRST_PERIOD ";
            }
            $query .= "         FROM ";
            $query .= "             T_PERIOD_CNT T0, ";
            $query .= "             ( ";
            $query .= "              SELECT ";
            $query .= "                  W1.SCHREGNO, W1.ATTENDDATE, ";
            $query .= "                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ";
            $query .= "                  COUNT(W1.PERIODCD) AS PERIOD_CNT ";
            $query .= "              FROM ";
            $query .= "                  T_ATTEND_DAT W1 ";
            $query .= "              WHERE ";
            $query .= "                  W1.DI_CD IN ('1', '4','5','6','11','12','13' ";
            if ("1" == $knjSchoolMst["SYUKESSEKI_HANTEI_HOU"]) {
                $query .= "                          ,'2','9','3','10' ";
            }
            $query .= "                              ) ";
            $query .= "              GROUP BY ";
            $query .= "                  W1.SCHREGNO, ";
            $query .= "                  W1.ATTENDDATE ";
            $query .= "             ) T1 ";
            if ("1" == $knjSchoolMst["SYUKESSEKI_HANTEI_HOU"]) {
                $query .= "         INNER JOIN ( ";
                $query .= "              SELECT ";
                $query .= "                  W1.SCHREGNO, W1.ATTENDDATE, ";
                $query .= "                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ";
                $query .= "                  COUNT(W1.PERIODCD) AS PERIOD_CNT ";
                $query .= "              FROM ";
                $query .= "                  T_ATTEND_DAT W1 ";
                $query .= "              WHERE ";
                $query .= "                  W1.DI_CD IN ('4','5','6','11','12','13') ";
                $query .= "              GROUP BY ";
                $query .= "                  W1.SCHREGNO, ";
                $query .= "                  W1.ATTENDDATE ";
                $query .= "             ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE ";
            }
            $query .= "         WHERE ";
            $query .= "             T0.SCHREGNO = T1.SCHREGNO ";
            $query .= "             AND T0.EXECUTEDATE = T1.ATTENDDATE ";
            $query .= "             AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ";
            $query .= "             AND T0.PERIOD_CNT = T1.PERIOD_CNT ";
            $query .= "        ) W1 ";
            $query .= "    WHERE ";
            $query .= "        W0.SCHREGNO = W1.SCHREGNO ";
            $query .= "        AND W0.ATTENDDATE = W1.EXECUTEDATE ";
            $query .= "        AND W0.PERIODCD = W1.FIRST_PERIOD ";
            if ("1" == $knjSchoolMst["SYUKESSEKI_HANTEI_HOU"]) {
                $query .= "    AND (W1.SCHREGNO, W1.EXECUTEDATE) NOT IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ";
            }
            
            //対象生徒の出欠データ
            $query .= " ), T_ATTEND_DAT_JISU AS( ";
            $query .= " SELECT ";
            $query .= "    T0.SCHREGNO, ";
            $query .= "    T0.SEMESTER, ";
            $query .= "    SUM(CASE WHEN ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T0.CLASSCD = '92' ";
            } else {
                $query .= "    SUBSTR(T0.SUBCLASSCD, 1, 2) = '92' ";
            }
            $query .= "       AND T0.DI_CD IN ('4','5','6','11','12','13') THEN 1 ELSE 0 END) AS REIHAI_KEKKA, ";
            $query .= "    SUM(CASE WHEN ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T0.CLASSCD <> '92' ";
            } else {
                $query .= "    SUBSTR(T0.SUBCLASSCD, 1, 2) <> '92' ";
            }
            $query .= "       AND T0.DI_CD IN ('4','5','6','11','12','13') THEN 1 ELSE 0 END) AS KEKKA_JISU, ";
            $query .= "    SUM(CASE WHEN ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T0.CLASSCD = '92' ";
            } else {
                $query .= "    SUBSTR(T0.SUBCLASSCD, 1, 2) = '92' ";
            }
            $query .= "       AND T0.DI_CD IN ('15','23','24') THEN SMALLINT(VALUE(L1.ABBV2, '1')) ELSE 0 END ";
            $query .= "       ) AS REIHAI_TIKOKU, ";
            $query .= "    SUM(CASE WHEN ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T0.CLASSCD <> '92' ";
            } else {
                $query .= "    SUBSTR(T0.SUBCLASSCD, 1, 2) <> '92' ";
            }
            $query .= "       AND T0.DI_CD IN ('15','23','24') THEN SMALLINT(VALUE(L1.ABBV2, '1')) ELSE 0 END ";
            $query .= "       ) AS JYUGYOU_TIKOKU, ";
            $query .= "    SUM(CASE WHEN ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T0.CLASSCD <> '92' ";
            } else {
                $query .= "    SUBSTR(T0.SUBCLASSCD, 1, 2) <> '92' ";
            }
            $query .= "       AND T0.DI_CD IN ('16') THEN SMALLINT(VALUE(L1.ABBV2, '1')) ELSE 0 END ";
            $query .= "       ) AS JYUGYOU_SOUTAI ";
            $query .= " FROM ";
            $query .= "    T_ATTEND_DAT_SUBCLASS T0 ";
            $query .= "    LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C001' AND L1.NAMECD2 = T0.DI_CD ";
            $query .= " WHERE ";
            $query .= "    NOT EXISTS (SELECT 'X' FROM T_KESSEKI L1 WHERE L1.SCHREGNO = T0.SCHREGNO ";
            $query .= "                 AND L1.SEMESTER = T0.SEMESTER ";
            $query .= "                 AND L1.ATTENDDATE = T0.ATTENDDATE ";
            $query .= "               ) ";
            $query .= " GROUP BY ";
            $query .= "    T0.SCHREGNO, ";
            $query .= "    T0.SEMESTER ";
        }
        $query .=      ") ";
        
        //メイン表
        $query .= " SELECT ";
        if ($groupByDiv == "HR_CLASS") {
            $query .= "    TT0.GRADE, ";
            $query .= "    TT0.HR_CLASS, ";
        } else if ($groupByDiv == "GRADE") {
            $query .= "    TT0.GRADE, ";
        } else if ($groupByDiv == "SCHREGNO") {
            $query .= "    TT0.SCHREGNO, ";
        } else if ($groupByDiv == "SEMESTER") {
            $query .= "    TT0.SCHREGNO, ";
            $query .= "    VALUE(TT0.SEMESTER, '9') AS SEMESTER, ";
        }
        if ($befDayFrom == '' && $aftDayFrom == '') {
            $query .= "    SUM(VALUE(TT7.LESSON,0)) AS LESSON, ";
            $query .= "    SUM(VALUE(TT7.SUSPEND,0)) AS SUSPEND, ";
            $query .= "    SUM(VALUE(TT7.MOURNING,0)) AS MOURNING, ";
            $query .= "    SUM(VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0)) AS MLESSON, ";
            $query .= "    SUM(VALUE(TT7.SICK,0)) AS SICK, ";
            $query .= "    SUM(VALUE(TT7.SICK_ONLY,0)) AS SICK_ONLY, ";
            $query .= "    SUM(VALUE(TT7.NOTICE_ONLY,0)) AS NOTICE_ONLY, ";
            $query .= "    SUM(VALUE(TT7.NONOTICE_ONLY,0)) AS NONOTICE_ONLY, ";
            $query .= "    SUM(VALUE(TT7.REIHAI_KEKKA,0)) AS REIHAI_KEKKA, ";
            $query .= "    SUM(VALUE(TT7.KEKKA_JISU,0)) AS KEKKA_JISU, ";
            $query .= "    SUM(VALUE(TT7.REIHAI_TIKOKU,0)) AS REIHAI_TIKOKU, ";
            $query .= "    SUM(VALUE(TT7.JYUGYOU_TIKOKU,0)) AS JYUGYOU_TIKOKU, ";
            $query .= "    SUM(VALUE(TT7.JYUGYOU_SOUTAI,0)) AS JYUGYOU_SOUTAI, ";
            $query .= "    SUM(VALUE(TT7.ABSENT,0)) AS ABSENT, ";
            $query .= "    SUM(VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ";
            $query .= "         - VALUE(TT7.SICK,0)) AS PRESENT, ";
            $query .= "    SUM(VALUE(TT7.LATE,0)) AS LATE, ";
            $query .= "    SUM(VALUE(TT7.EARLY,0)) AS EARLY, ";
            $query .= "    SUM(VALUE(TT7.ABROAD,0)) AS TRANSFER_DATE, ";
            $query .= "    SUM(VALUE(TT7.OFFDAYS,0)) AS OFFDAYS ";
        } else if ($semesFlg) {
            $query .= "    SUM(VALUE(TT12.LESSON,0) ";
            if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "         + VALUE(TT14.TRANSFER_DATE, 0) ";
            }
            $query .= "         + VALUE(TT7.LESSON,0)) AS LESSON, ";
            $query .= "    SUM(VALUE(TT3.SUSPEND,0) + VALUE(TT7.SUSPEND,0)) AS SUSPEND, ";
            $query .= "    SUM(VALUE(TT4.MOURNING,0) + VALUE(TT7.MOURNING,0)) AS MOURNING, ";
            $query .= "    SUM(VALUE(TT12.LESSON,0) ";
            if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "         + VALUE(TT14.TRANSFER_DATE, 0) ";
            }
            $query .= "         - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ";
            $query .= "         + VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0)) AS MLESSON, ";
            $query .= "    SUM(VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) ";
            if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "         + VALUE(TT14.TRANSFER_DATE, 0) ";
            }
            $query .= "         + VALUE(TT7.SICK,0)) AS SICK, ";
            $query .= "    SUM(VALUE(TT5.SICK,0) + VALUE(TT7.SICK_ONLY,0)) AS SICK_ONLY, ";
            $query .= "    SUM(VALUE(TT5.NOTICE,0) + VALUE(TT7.NOTICE_ONLY,0)) AS NOTICE_ONLY, ";
            $query .= "    SUM(VALUE(TT5.NONOTICE,0) + VALUE(TT7.NONOTICE_ONLY,0)) AS NONOTICE_ONLY, ";
            $query .= "    SUM(VALUE(TT15.REIHAI_KEKKA,0) + VALUE(TT7.REIHAI_KEKKA,0)) AS REIHAI_KEKKA, ";
            $query .= "    SUM(VALUE(TT15.KEKKA_JISU,0) + VALUE(TT7.KEKKA_JISU,0)) AS KEKKA_JISU, ";
            $query .= "    SUM(VALUE(TT15.REIHAI_TIKOKU,0) + VALUE(TT7.REIHAI_TIKOKU,0)) AS REIHAI_TIKOKU, ";
            $query .= "    SUM(VALUE(TT15.JYUGYOU_TIKOKU,0) + VALUE(TT7.JYUGYOU_TIKOKU,0)) AS JYUGYOU_TIKOKU, ";
            $query .= "    SUM(VALUE(TT15.JYUGYOU_SOUTAI,0) + VALUE(TT7.JYUGYOU_SOUTAI,0)) AS JYUGYOU_SOUTAI, ";
            $query .= "    SUM(VALUE(TT5.ABSENT,0) + VALUE(TT7.ABSENT,0)) AS ABSENT, ";
            $query .= "    SUM(VALUE(TT12.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ";
            $query .= "         + VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ";
            $query .= "         - VALUE(TT5.SICK,0) - VALUE(TT5.NOTICE,0) - VALUE(TT5.NONOTICE,0) ";
            $query .= "         - VALUE(TT7.SICK,0)) AS PRESENT, ";
            $query .= "    SUM(VALUE(TT6.LATE,0) + VALUE(TT10.LATE,0) + VALUE(TT7.LATE,0)) AS LATE, ";
            $query .= "    SUM(VALUE(TT6.EARLY,0) + VALUE(TT11.EARLY,0) + VALUE(TT7.EARLY,0)) AS EARLY, ";
            $query .= "    SUM(VALUE(TT13.TRANSFER_DATE,0) + VALUE(TT7.ABROAD,0)) AS TRANSFER_DATE, ";
            $query .= "    SUM(VALUE(TT14.TRANSFER_DATE,0) + VALUE(TT7.OFFDAYS,0)) AS OFFDAYS ";
        } else {
            $query .= "    SUM(VALUE(TT12.LESSON,0) ";
            if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "         + VALUE(TT14.TRANSFER_DATE, 0) ";
            }
            $query .= "        ) AS LESSON, ";
            $query .= "    SUM(VALUE(TT3.SUSPEND,0)) AS SUSPEND, ";
            $query .= "    SUM(VALUE(TT4.MOURNING,0)) AS MOURNING, ";
            $query .= "    SUM(VALUE(TT12.LESSON,0) ";
            if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "         + VALUE(TT14.TRANSFER_DATE, 0) ";
            }
            $query .= "         - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0)) AS MLESSON, ";
            $query .= "    SUM(VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) ";
            if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "         + VALUE(TT14.TRANSFER_DATE, 0) ";
            }
            $query .= "        ) AS SICK, ";
            $query .= "    SUM(VALUE(TT5.SICK,0)) AS SICK_ONLY, ";
            $query .= "    SUM(VALUE(TT5.NOTICE,0)) AS NOTICE_ONLY, ";
            $query .= "    SUM(VALUE(TT5.NONOTICE,0)) AS NONOTICE_ONLY, ";
            $query .= "    SUM(VALUE(TT15.REIHAI_KEKKA,0)) AS REIHAI_KEKKA, ";
            $query .= "    SUM(VALUE(TT15.KEKKA_JISU,0)) AS KEKKA_JISU, ";
            $query .= "    SUM(VALUE(TT15.REIHAI_TIKOKU,0)) AS REIHAI_TIKOKU, ";
            $query .= "    SUM(VALUE(TT15.JYUGYOU_TIKOKU,0)) AS JYUGYOU_TIKOKU, ";
            $query .= "    SUM(VALUE(TT15.JYUGYOU_SOUTAI,0)) AS JYUGYOU_SOUTAI, ";
            $query .= "    SUM(VALUE(TT5.ABSENT,0)) AS ABSENT, ";
            $query .= "    SUM(VALUE(TT12.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ";
            $query .= "         - VALUE(TT5.SICK,0) - VALUE(TT5.NOTICE,0) - VALUE(TT5.NONOTICE,0)) AS PRESENT, ";
            $query .= "    SUM(VALUE(TT6.LATE,0) + VALUE(TT10.LATE,0)) AS LATE, ";
            $query .= "    SUM(VALUE(TT6.EARLY,0) + VALUE(TT11.EARLY,0)) AS EARLY, ";
            $query .= "    SUM(VALUE(TT13.TRANSFER_DATE,0)) AS TRANSFER_DATE, ";
            $query .= "    SUM(VALUE(TT14.TRANSFER_DATE,0)) AS OFFDAYS ";
        }
        $query .= " FROM ";
        $query .= "    SCHNO TT0 ";
        if ($befDayFrom != '' || $aftDayFrom != '') {
            //個人別出停日数
            $query .= " LEFT OUTER JOIN( ";
            $query .= "    SELECT ";
            $query .= "        W1.SCHREGNO, ";
            $query .= "        VALUE(W1.SEMESTER, '9') AS SEMESTER, ";
            $query .= "        COUNT(DISTINCT W1.ATTENDDATE) AS SUSPEND ";
            $query .= "    FROM ";
            $query .= "        T_ATTEND_DAT W1 ";
            $query .= "    WHERE ";
            $query .= "        W1.DI_CD IN ('2','9') ";
            if ("1" == $knjSchoolMst["SYUKESSEKI_HANTEI_HOU"]) {
                $query .= "    AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ";
            }
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ";
            $query .= "    ) TT3 ON TT0.SCHREGNO = TT3.SCHREGNO ";
            $query .= "          AND TT0.SEMESTER = TT3.SEMESTER ";
            //個人別忌引日数
            $query .= " LEFT OUTER JOIN( ";
            $query .= "    SELECT ";
            $query .= "        W1.SCHREGNO, ";
            $query .= "        VALUE(W1.SEMESTER, '9') AS SEMESTER, ";
            $query .= "        COUNT(DISTINCT W1.ATTENDDATE) AS MOURNING ";
            $query .= "    FROM ";
            $query .= "        T_ATTEND_DAT W1 ";
            $query .= "    WHERE ";
            $query .= "        W1.DI_CD IN ('3','10') ";
            if ("1" == $knjSchoolMst["SYUKESSEKI_HANTEI_HOU"]) {
                $query .= "    AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ";
            }
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ";
            $query .= "    ) TT4 ON TT0.SCHREGNO = TT4.SCHREGNO ";
            $query .= "          AND TT0.SEMESTER = TT4.SEMESTER ";
            //個人別欠席日数
            $query .= " LEFT OUTER JOIN( ";
            $query .= "    SELECT ";
            $query .= "        W0.SCHREGNO, ";
            $query .= "        VALUE(W0.SEMESTER, '9') AS SEMESTER, ";
            $query .= "        SUM(CASE W0.DI_CD WHEN '1' THEN 1 ELSE 0 END) AS ABSENT, ";
            $query .= "        SUM(CASE W0.DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ";
            $query .= "        SUM(CASE W0.DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ";
            $query .= "        SUM(CASE W0.DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ";
            $query .= "    FROM ";
            $query .= "        T_KESSEKI W0 ";
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((W0.SCHREGNO, W0.SEMESTER), (W0.SCHREGNO)) ";
            $query .= "    )TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ";
            $query .= "         AND TT0.SEMESTER = TT5.SEMESTER ";
            //個人別遅刻・早退回数
            $query .= " LEFT OUTER JOIN( ";
            $query .= "    SELECT ";
            $query .= "        T0.SCHREGNO, ";
            $query .= "        VALUE(T0.SEMESTER, '9') AS SEMESTER, ";
            $query .= "        COUNT(T2.ATTENDDATE) AS LATE, ";
            $query .= "        COUNT(T3.ATTENDDATE) AS EARLY ";
            $query .= "    FROM ";
            $query .= "        T_PERIOD_CNT T0 ";
            $query .= "        INNER JOIN( ";
            $query .= "            SELECT ";
            $query .= "                W1.SCHREGNO, ";
            $query .= "                W1.ATTENDDATE, ";
            $query .= "                COUNT(W1.PERIODCD) AS PERIOD_CNT ";
            $query .= "            FROM ";
            $query .= "                T_ATTEND_DAT W1 ";
            $query .= "            WHERE ";
            $query .= "                W1.DI_CD NOT IN ('0','14','15','16','23','24') ";
            if (!"1" == $knjSchoolMst["SYUKESSEKI_HANTEI_HOU"]) {
                $query .= "                AND NOT EXISTS ";
                $query .= "                        (SELECT ";
                $query .= "                             'X' ";
                $query .= "                         FROM ";
                $query .= "                             T_ATTEND_DAT_B W2 ";
                $query .= "                         WHERE ";
                $query .= "                             W2.SCHREGNO = W1.SCHREGNO ";
                $query .= "                             AND W2.ATTENDDATE = W1.ATTENDDATE ";
                $query .= "                        ) ";
            }
            $query .= "            GROUP BY ";
            $query .= "                W1.SCHREGNO, ";
            $query .= "                W1.ATTENDDATE ";
            $query .= "        )T1 ON T0.SCHREGNO = T1.SCHREGNO ";
            $query .= "            AND T0.EXECUTEDATE = T1.ATTENDDATE ";
            $query .= "            AND T0.PERIOD_CNT != T1.PERIOD_CNT ";
            $query .= "        LEFT OUTER JOIN( ";
            $query .= "            SELECT ";
            $query .= "                SCHREGNO, ";
            $query .= "                ATTENDDATE, ";
            $query .= "                PERIODCD ";
            $query .= "            FROM ";
            $query .= "                T_ATTEND_DAT ";
            $query .= "            WHERE ";
            $query .= "                DI_CD IN ('4','5','6','11','12','13') ";
            $query .= "        )T2 ON T0.SCHREGNO = T2.SCHREGNO ";
            $query .= "            AND T0.EXECUTEDATE  = T2.ATTENDDATE ";
            $query .= "            AND T0.FIRST_PERIOD = T2.PERIODCD ";
            $query .= "        LEFT OUTER JOIN(";
            $query .= "            SELECT ";
            $query .= "                SCHREGNO, ";
            $query .= "                ATTENDDATE, ";
            $query .= "                PERIODCD ";
            $query .= "            FROM ";
            $query .= "                T_ATTEND_DAT ";
            $query .= "            WHERE ";
            $query .= "                DI_CD IN ('4','5','6') ";
            $query .= "        )T3 ON T0.SCHREGNO= T3.SCHREGNO ";
            $query .= "            AND T0.EXECUTEDATE = T3.ATTENDDATE ";
            $query .= "            AND T0.LAST_PERIOD = T3.PERIODCD ";
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((T0.SCHREGNO, T0.SEMESTER), (T0.SCHREGNO)) ";
            $query .= " )TT6 ON TT0.SCHREGNO = TT6.SCHREGNO ";
            $query .= "      AND TT0.SEMESTER = TT6.SEMESTER ";

            //個人別遅刻回数
            $query .= " LEFT OUTER JOIN( ";
            $query .= "    SELECT ";
            $query .= "        T0.SCHREGNO, ";
            $query .= "        VALUE(T0.SEMESTER, '9') AS SEMESTER, ";
            $query .= "        COUNT(T2.ATTENDDATE) AS LATE ";
            $query .= "    FROM ";
            $query .= "        T_PERIOD_CNT T0 ";
            $query .= "        INNER JOIN( ";
            $query .= "            SELECT ";
            $query .= "                SCHREGNO, ";
            $query .= "                ATTENDDATE, ";
            $query .= "                PERIODCD ";
            $query .= "            FROM ";
            $query .= "                T_ATTEND_DAT W1 ";
            $query .= "            WHERE ";
            $query .= "                DI_CD IN ('15','23','24') ";
            if (!"1" == $knjSchoolMst["SYUKESSEKI_HANTEI_HOU"]) {
                $query .= "                AND NOT EXISTS ";
                $query .= "                        (SELECT ";
                $query .= "                            'X' ";
                $query .= "                         FROM ";
                $query .= "                            T_ATTEND_DAT_B W2 ";
                $query .= "                         WHERE ";
                $query .= "                            W2.SCHREGNO = W1.SCHREGNO ";
                $query .= "                            AND W2.ATTENDDATE = W1.ATTENDDATE) ";
            }
            $query .= "        )T2 ON T0.SCHREGNO = T2.SCHREGNO ";
            $query .= "            AND T0.EXECUTEDATE = T2.ATTENDDATE ";
            $query .= "            AND T0.FIRST_PERIOD = T2.PERIODCD ";
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((T0.SCHREGNO, T0.SEMESTER), (T0.SCHREGNO)) ";
            $query .= " )TT10 ON TT0.SCHREGNO = TT10.SCHREGNO ";
            $query .= "       AND TT0.SEMESTER = TT10.SEMESTER ";

            //個人別早退回数
            $query .= " LEFT OUTER JOIN( ";
            $query .= "    SELECT ";
            $query .= "        T0.SCHREGNO, ";
            $query .= "        VALUE(T0.SEMESTER, '9') AS SEMESTER, ";
            $query .= "        COUNT(T2.ATTENDDATE) AS EARLY ";
            $query .= "    FROM ";
            $query .= "        T_PERIOD_CNT T0 ";
            $query .= "        INNER JOIN( ";
            $query .= "            SELECT ";
            $query .= "                SCHREGNO, ";
            $query .= "                ATTENDDATE, ";
            $query .= "                PERIODCD ";
            $query .= "            FROM ";
            $query .= "                T_ATTEND_DAT W1 ";
            $query .= "            WHERE ";
            $query .= "                DI_CD IN ('16') ";
            if (!"1" == $knjSchoolMst["SYUKESSEKI_HANTEI_HOU"]) {
                $query .= "                AND NOT EXISTS ";
                $query .= "                        (SELECT ";
                $query .= "                            'X' ";
                $query .= "                         FROM ";
                $query .= "                            T_ATTEND_DAT_B W2 ";
                $query .= "                         WHERE ";
                $query .= "                            W2.SCHREGNO = W1.SCHREGNO ";
                $query .= "                            AND W2.ATTENDDATE = W1.ATTENDDATE) ";
            }
            $query .= "        )T2 ON T0.SCHREGNO = T2.SCHREGNO ";
            $query .= "            AND T0.EXECUTEDATE = T2.ATTENDDATE ";
            $query .= "            AND T0.LAST_PERIOD = T2.PERIODCD ";
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((T0.SCHREGNO, T0.SEMESTER), (T0.SCHREGNO)) ";
            $query .= " )TT11 ON TT0.SCHREGNO = TT11.SCHREGNO ";
            $query .= "       AND TT0.SEMESTER = TT11.SEMESTER ";

            //授業時数
            $query .= " LEFT OUTER JOIN( ";
            $query .= "    SELECT ";
            $query .= "        T0.SCHREGNO, ";
            $query .= "        VALUE(T0.SEMESTER, '9') AS SEMESTER, ";
            $query .= "        SUM(CASE WHEN VALUE(T0.PERIOD_CNT, 0) > 0 ";
            $query .= "                 THEN 1 ";
            $query .= "                 ELSE 0 ";
            $query .= "            END ";
            $query .= "        ) AS LESSON ";
            $query .= "    FROM ";
            $query .= "        T_PERIOD_CNT T0 ";
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((T0.SCHREGNO, T0.SEMESTER), (T0.SCHREGNO)) ";
            $query .= " )TT12 ON TT0.SCHREGNO = TT12.SCHREGNO ";
            $query .= "       AND TT0.SEMESTER = TT12.SEMESTER ";
            
            // 留学中の授業日数
            $query .= " LEFT OUTER JOIN( ";
            $query .= "    SELECT ";
            $query .= "       T1.SCHREGNO,";
            $query .= "       VALUE(T1.SEMESTER, '9') AS SEMESTER, ";
            $query .= "       COUNT(DISTINCT T1.EXECUTEDATE) AS TRANSFER_DATE ";
            $query .= "    FROM ";
            $query .= "       SCHEDULE_SCHREG_R T1, ";
            $query .= "       SCHREG_TRANSFER_DAT T2 ";
            $query .= "    WHERE ";
            $query .= "       EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T2.SCHREGNO AND T3.SCHREGNO = T1.SCHREGNO) ";
            $query .= "       AND T2.TRANSFERCD IN('1') ";
            $query .= "       AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ";
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((T1.SCHREGNO, T1.SEMESTER), (T1.SCHREGNO)) ";
            $query .= "  )TT13 ON TT0.SCHREGNO = TT13.SCHREGNO ";
            $query .= "       AND TT0.SEMESTER = TT13.SEMESTER ";

            // 休学中の授業日数
            $query .= " LEFT OUTER JOIN( ";
            $query .= "    SELECT ";
            $query .= "       T1.SCHREGNO,";
            $query .= "       VALUE(T1.SEMESTER, '9') AS SEMESTER, ";
            $query .= "       COUNT(DISTINCT T1.EXECUTEDATE) AS TRANSFER_DATE ";
            $query .= "    FROM ";
            $query .= "       SCHEDULE_SCHREG_R T1, ";
            $query .= "       SCHREG_TRANSFER_DAT T2 ";
            $query .= "    WHERE ";
            $query .= "       EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T2.SCHREGNO AND T3.SCHREGNO = T1.SCHREGNO) ";
            $query .= "       AND T2.TRANSFERCD IN('2') ";
            $query .= "       AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ";
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((T1.SCHREGNO, T1.SEMESTER), (T1.SCHREGNO)) ";
            $query .= "  )TT14 ON TT0.SCHREGNO = TT14.SCHREGNO ";
            $query .= "       AND TT0.SEMESTER = TT14.SEMESTER ";
            
            //個人別授業欠課時数
            $query .= " LEFT OUTER JOIN( ";
            $query .= "    SELECT ";
            $query .= "        W0.SCHREGNO, ";
            $query .= "        VALUE(W0.SEMESTER, '9') AS SEMESTER, ";
            $query .= "        SUM(VALUE(W0.REIHAI_KEKKA, 0)) AS REIHAI_KEKKA, ";
            $query .= "        SUM(VALUE(W0.KEKKA_JISU, 0)) AS KEKKA_JISU, ";
            $query .= "        SUM(VALUE(W0.REIHAI_TIKOKU, 0)) AS REIHAI_TIKOKU, ";
            $query .= "        SUM(VALUE(W0.JYUGYOU_TIKOKU, 0)) AS JYUGYOU_TIKOKU, ";
            $query .= "        SUM(VALUE(W0.JYUGYOU_SOUTAI, 0)) AS JYUGYOU_SOUTAI ";
            $query .= "    FROM ";
            $query .= "        T_ATTEND_DAT_JISU W0 ";
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((W0.SCHREGNO, W0.SEMESTER), (W0.SCHREGNO)) ";
            $query .= "    )TT15 ON TT0.SCHREGNO = TT15.SCHREGNO ";
            $query .= "         AND TT0.SEMESTER = TT15.SEMESTER ";
        }
        
        if ($semesFlg) {
            //月別集計データから集計した表
            $query .= " LEFT JOIN( ";
            $query .= "    SELECT ";
            $query .= "        SCHREGNO, ";
            $query .= "        VALUE(SEMESTER, '9') AS SEMESTER, ";
            $query .= "        SUM( VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ";
            if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "           + VALUE(OFFDAYS, 0) ";
            }
            $query .= "        ) AS LESSON, ";
            $query .= "        SUM(MOURNING) AS MOURNING, ";
            $query .= "        SUM(SUSPEND) AS SUSPEND, ";
            $query .= "        SUM(ABSENT) AS ABSENT, ";
            $query .= "        SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ";
            if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "           + VALUE(OFFDAYS, 0) ";
            }
            $query .= "        ) AS SICK, ";
            $query .= "        SUM(VALUE(SICK,0)) AS SICK_ONLY, ";
            $query .= "        SUM(VALUE(NOTICE,0)) AS NOTICE_ONLY, ";
            $query .= "        SUM(VALUE(NONOTICE,0)) AS NONOTICE_ONLY, ";
            $query .= "        SUM(VALUE(REIHAI_KEKKA,0)) AS REIHAI_KEKKA, ";
            $query .= "        SUM(VALUE(KEKKA_JISU,0)) AS KEKKA_JISU, ";
            $query .= "        SUM(VALUE(REIHAI_TIKOKU,0)) AS REIHAI_TIKOKU, ";
            $query .= "        SUM(VALUE(JYUGYOU_TIKOKU,0)) AS JYUGYOU_TIKOKU, ";
            $query .= "        SUM(VALUE(JYUGYOU_SOUTAI,0)) AS JYUGYOU_SOUTAI, ";
            $query .= "        SUM(LATE) AS LATE, ";
            $query .= "        SUM(EARLY) AS EARLY, ";
            $query .= "        SUM(ABROAD) AS ABROAD, ";
            $query .= "        SUM(OFFDAYS) AS OFFDAYS ";
            $query .= "    FROM ";
            $query .= "        V_ATTEND_SEMES_DAT W1 ";
            $query .= "    WHERE ";
            $query .= "        W1.YEAR = '".$year."' ";
            $query .= "        AND SEMESTER BETWEEN '".$sSemester."' AND '".$eSemester."' ";
            $query .= "        AND W1.SEMESTER || W1.MONTH IN ".$semesInState." ";
            $query .= "        AND EXISTS ";
            $query .= "            (SELECT ";
            $query .= "                'X' ";
            $query .= "             FROM ";
            $query .= "                SCHNO W2 ";
            $query .= "             WHERE ";
            $query .= "                W1.SCHREGNO = W2.SCHREGNO)";
            $query .= "    GROUP BY ";
            $query .= "        GROUPING SETS ((SCHREGNO, SEMESTER), (SCHREGNO)) ";
            $query .= ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ";
            $query .= "     AND TT0.SEMESTER = TT7.SEMESTER ";
        }
        $query .= " GROUP BY ";
        if ($groupByDiv == "HR_CLASS") {
            $query .= "    TT0.GRADE, ";
            $query .= "    TT0.HR_CLASS ";
        } else if ($groupByDiv == "GRADE") {
            $query .= "    TT0.GRADE ";
        } else if ($groupByDiv == "SCHREGNO") {
            $query .= "    TT0.SCHREGNO ";
        } else if ($groupByDiv == "SEMESTER") {
            $query .= "    GROUPING SETS ( ";
            $query .= "        (TT0.SCHREGNO, TT0.SEMESTER), ";
            $query .= "        (TT0.SCHREGNO)) ";
        }
        
        $query .= " ORDER BY ";
        if ($groupByDiv == "HR_CLASS") {
            $query .= "    TT0.GRADE, ";
            $query .= "    TT0.HR_CLASS ";
        } else if ($groupByDiv == "GRADE") {
            $query .= "    TT0.GRADE ";
        } else if ($groupByDiv == "SCHREGNO") {
            $query .= "    TT0.SCHREGNO ";
        } else if ($groupByDiv == "SEMESTER") {
            $query .= "    TT0.SCHREGNO, ";
            $query .= "    TT0.SEMESTER ";
        }

        return $query;
    }

}
?>
