<?php

require_once('for_php7.php');

class knjz023Query extends Query {

    //学年コンボ
    function getGrade($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科コンボ
    function getCourseMajor($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSENAME || T1.MAJORNAME AS LABEL, ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //学年ごとの授業数をカウント
    function getAttendSemesLessonDat($model) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_LESSON_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND GRADE    = '{$model->field["GRADE"]}' ";

        return $query;
    }
        
    //学期リスト
    function getList($year) {
        $query  = " SELECT  ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER,  ";
        $query .= "     SEMESTERNAME,  ";
        $query .= "     YEAR(SDATE) AS SYEAR, ";
        $query .= "     YEAR(EDATE) AS EYEAR, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "         THEN MONTH(SDATE) + 12 ";
        $query .= "         ELSE MONTH(SDATE) ";
        $query .= "     END AS SMONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "         THEN MONTH(EDATE) + 12 ";
        $query .= "         ELSE MONTH(EDATE) ";
        $query .= "     END AS EMONTH, ";
        $query .= "     DAY(SDATE) AS SDAY, ";
        $query .= "     DAY(EDATE) AS EDAY ";
        $query .= " FROM  ";
        $query .= "     SEMESTER_MST  ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '{$year}'  ";
        $query .= "     AND SEMESTER <> '9'  ";
        $query .= " ORDER BY  ";
        $query .= "     SEMESTER  ";

        return $query;
    }

    //学期マスタより対象日付を取得
    function getLessonDay($month, $seme) {
        $year = (($month * 1) < 4) ? CTRL_YEAR + 1 : CTRL_YEAR;

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
    function getHoliCnt($model, $grade, $semester, $fDay, $eDay) {
        $query  = " WITH REGD_HDAT_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     COUNT(*) AS GRADE_CNT ";
        $query .= " FROM ";
        if ($model->field["HR_CLASS_DIV"] == "2") {
            $query .= "     SCHREG_REGD_FI_HDAT ";
        } else {
            $query .= "     SCHREG_REGD_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '{$semester}' ";
        $query .= " AND GRADE = '{$grade}' ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
        $query .= " ) ";
        $query .= " ,HOLIDAY_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     COUNT(*) AS GRADE_CNT ";
        $query .= " FROM ";
        $query .= "     EVENT_DAT T1 ";
        if ($model->field["HR_CLASS_DIV"] == "2") {
            $query .= "     LEFT JOIN SCHREG_REGD_FI_HDAT L1 ON L1.GRADE = T1.GRADE ";
            $query .= "          AND L1.HR_CLASS = T1.HR_CLASS ";
        } else {
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.GRADE = T1.GRADE ";
            $query .= "          AND L1.HR_CLASS = T1.HR_CLASS ";
        }
        $query .= " WHERE ";
        $query .= "     L1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND L1.SEMESTER = '{$semester}' ";
        $query .= " AND L1.GRADE = '{$grade}' ";
        $query .= " AND T1.EXECUTEDATE BETWEEN '{$fDay}' AND '{$eDay}' ";
        $query .= " AND T1.HOLIDAY_FLG = '1' ";
        $query .= " AND T1.HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.EXECUTEDATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS HOLI_CNT ";
        $query .= " FROM ";
        $query .= "     HOLIDAY_DATA T1 ";
        $query .= "     INNER JOIN REGD_HDAT_DATA L1 ON L1.GRADE = T1.GRADE ";
        $query .= "                                 AND L1.GRADE_CNT = T1.GRADE_CNT ";

        return $query;
    }

    //月の表示文字するをDBから取得
    function getMonthName($year, $month) {
        $query .= " SELECT DISTINCT  ";
        $query .= "     NAME1 ";
        $query .= " FROM  ";
        $query .= "     V_NAME_MST  ";
        $query .= " WHERE  ";
        $query .= "     YEAR  = '{$year}'  ";
        $query .= "     AND NAMECD1 = 'Z005'  ";
        $query .= "     AND NAMECD2 = '{$month}' ";

        return $query;
    }

    //授業日数の取得
    function getLesson($year, $grade, $coursecd, $month, $semester) {
        $query  = " SELECT ";
        $query .= "     LESSON ";
        $query .= " FROM ";
        $query .= "     ATTEND_LESSON_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$year}' ";
        $query .= " AND GRADE    = '{$grade}' ";
        $query .= " AND COURSECD || MAJORCD = '{$coursecd}' ";
        $query .= " AND SEMESTER = '{$semester}' ";
        $query .= " AND MONTH    = '{$month}' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($year, $grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     GRADE   = '".$grade."' ";

        return $query;
    }

    //削除処理
    function getDeleteQuery($model) {
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_LESSON_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " AND COURSECD || MAJORCD = '{$model->field["COURSE"]}' ";

        return $query;
    }

    //更新処理
    function getInsertQuery($model, $semester, $month, $lesson) {
        $coursecd = substr($model->field["COURSE"], 0, 1);
        $majorcd  = substr($model->field["COURSE"], 1, 3);

        $data = array();
        $data["YEAR"][TEXT]           = CTRL_YEAR;
        $data["GRADE"][TEXT]          = $model->field["GRADE"];
        $data["COURSECD"][TEXT]       = $coursecd;
        $data["MAJORCD"][TEXT]        = $majorcd;
        $data["SEMESTER"][TEXT]       = $semester;
        $data["MONTH"][TEXT]          = $month;
        $data["LESSON"][NUMBER]       = $lesson;
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "SYSDATE()";

        $query = Query::insertSQL($data, "ATTEND_LESSON_MST");
        return $query;
    }
}
?>