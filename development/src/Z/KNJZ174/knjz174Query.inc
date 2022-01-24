<?php

require_once('for_php7.php');

class knjz174Query extends Query {

    //メインデータ作成
    function selectSemesAll($seme = "")
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        if (!$seme) {
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        } else {
            $query .= "     SEMESTERNAME, ";
            $query .= "     MONTH(SDATE) AS S_MONTH, ";
            $query .= "     DAY(SDATE) AS S_DAY, ";
            $query .= "     MONTH(EDATE) AS E_MONTH, ";
            $query .= "     DAY(EDATE) AS E_DAY ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (!$seme) {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$seme."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    /* 対象月データの取得 */
    function selectMonthQuery($month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $setNameCd = "Z".$model->sendSchoolKind."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, NAME1, NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$setNameCd}' ";
        $query .= "     AND NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //コピー元
    function selectCopyQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '-' || T1.MONTH || '-' || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T1.MONTH || '月(' || L1.SEMESTERNAME || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_LESSON_DAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR IN ('".(CTRL_YEAR - 1)."', '".CTRL_YEAR."') ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".$model->sendSchoolKind."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ取得
    function selectData($model)
    {
        list($month, $seme) = preg_split("/-/", $model->field["MONTHCD"]);
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     L1.LESSON ";
        $query .= " FROM ";
        if ($model->field["HR_CLASS_DIV"] == "2") {
            $query .= "     SCHREG_REGD_FI_HDAT T1 ";
        } else {
            $query .= "     SCHREG_REGD_HDAT T1 ";
        }
        $query .= "     LEFT JOIN ATTEND_SEMES_LESSON_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND L1.MONTH = '{$month}' ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.GRADE = L1.GRADE ";
        $query .= "          AND T1.HR_CLASS = L1.HR_CLASS ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON T1.YEAR = G1.YEAR ";
            $query .= "          AND T1.GRADE       = G1.GRADE ";
            $query .= "          AND G1.SCHOOL_KIND = '".$model->sendSchoolKind."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '{$seme}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期マスタより対象日付を取得
    function getLessonDay($month, $seme) {
        $year = ($month * 1) < 4 ? CTRL_YEAR + 1 : CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     CASE WHEN MONTH(SDATE) = ".($month * 1)." ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS SDAY_FLG, ";
        $query .= "     CASE WHEN MONTH(EDATE) = ".($month * 1)." ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS EDAY_FLG, ";
        $query .= "     DAY(SDATE) AS SDAY, ";
        $query .= "     DAY(EDATE) AS EDAY, ";
        $query .= "     DAY(last_day(DATE('".$year."-".$month."-01'))) AS DAY_MAX ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$seme}' ";
        return $query;
    }

    //EVENT_DATより学年ごとの休日数を取得（対象学年の全てのクラスが休日の時をカウントする）
    function getHoliCnt($gradeHr, $semester, $fDay, $eDay, $hr_class_div) {
        $query  = " WITH HOLIDAY_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     COUNT(*) AS GRADE_CNT ";
        $query .= " FROM ";
        $query .= "     EVENT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '{$gradeHr}' ";
        $query .= "     AND T1.EXECUTEDATE BETWEEN '{$fDay}' AND '{$eDay}' ";
        $query .= "     AND T1.HOLIDAY_FLG = '1' ";
        $query .= "     AND T1.HR_CLASS_DIV = '{$hr_class_div}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.EXECUTEDATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS HOLI_CNT ";
        $query .= " FROM ";
        $query .= "     HOLIDAY_DATA T1 ";

        return $query;
    }

    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($month, $seme) = preg_split("/-/", $model->field["MONTHCD"]);

        $query  =  " DELETE FROM ATTEND_SEMES_LESSON_DAT ";
        $query .=  " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MONTH = '{$month}' ";
        $query .= "     AND SEMESTER = '{$seme}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND GRADE IN ( SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->sendSchoolKind."' ";
            $query .= "             ) ";
        }

        $db->query($query);

        foreach ($model->gradeHr as $key => $val) {
            if ($model->field["LESSON".$val]) {
                list($grade, $hrClass) = preg_split("/-/", $val);
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["MONTH"][TEXT]        = $month;
                $data["SEMESTER"][TEXT]     = $seme;
                $data["GRADE"][TEXT]        = $grade;
                $data["HR_CLASS"][TEXT]     = $hrClass;
                $data["LESSON"][NUMBER]     = $model->field["LESSON".$val];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";

                $query = Query::insertSQL($data, "ATTEND_SEMES_LESSON_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
    
    function getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($month, $seme) = preg_split("/-/", $model->field["MONTHCD"]);

        $query  =  " DELETE FROM ATTEND_SEMES_LESSON_DAT ";
        $query .=  " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MONTH = '{$month}' ";
        $query .= "     AND SEMESTER = '{$seme}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND GRADE IN ( SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->sendSchoolKind."' ";
            $query .= "             ) ";
        }

        $db->query($query);

        list($insYear, $insMonth, $insSeme) = preg_split("/-/", $model->field["COPY_KEY"]);

        $query  = " INSERT INTO ";
        $query .= "     ATTEND_SEMES_LESSON_DAT ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     '{$month}', ";
        $query .= "     '{$seme}', ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     LESSON, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_LESSON_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$insYear}' ";
        $query .= "     AND MONTH = '{$insMonth}' ";
        $query .= "     AND SEMESTER = '{$insSeme}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND GRADE IN ( SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".$insYear."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->sendSchoolKind."' ";
            $query .= "             ) ";
        }

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
