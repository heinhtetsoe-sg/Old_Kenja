<?php

require_once('for_php7.php');
class knjz177_gradeQuery extends Query {
    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->school_kind) {  //親画面から渡ってきた校種を使う
                $query .= "     AND NAME1 = '".$model->school_kind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年コンボ
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".$model->school_kind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //対象の学期表示
    function getTaishouGrade($model) {
    
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE, ";
        $query .= "     L1.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_GRADE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                  AND L1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND L1.SCHOOL_KIND = '".$model->school_kind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE ";

        return $query;
    }

    //対象データ一覧を取得する
    function getRecordList($model, $getGrade, $semester, $month)
    {
        if (strlen($month) == 1) {
            $month = '0' . $month;
        }
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " AND MONTH = '".$month."' ";
        $query .= " AND SEMESTER = '".$semester."' ";
        $query .= " AND GRADE = '".$getGrade."' ";

        return $query;
    }

    //対象の学期表示
    function getSemesterHyouji($model, $setSemester) {
    
        $query  = " SELECT ";
        $query .= "     SEMESTER AS SEMESTER1, ";
        $query .= "     SEMESTERNAME AS SEMESTERNAME1, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 THEN MONTH(SDATE) + 12 ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 THEN MONTH(EDATE) + 12 ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "  AND SEMESTER = '".$setSemester."' ";

        return $query;
    }
    
    //対象データ
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

    //月の表示文字するをDBから取得
    function getMonthName($year, $month, $model) {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->school_kind."05";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query .= " SELECT DISTINCT  ";
        $query .= "     NAME1 ";
        $query .= " FROM  ";
        $query .= "     V_NAME_MST  ";
        $query .= " WHERE  ";
        $query .= "     YEAR  = '{$year}'  ";
        $query .= "     AND NAMECD1 = '{$setNameCd}'  ";
        $query .= "     AND NAMECD2 = '{$month}' ";

        return $query;
    }

    //背景に色をつけるかどうか、
    function getListColor($year, $month, $model) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     AND SCHOOL_KIND = '".$model->school_kind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND CONTROL_CODE = '{$month}' ";
        $query .= "     AND CONTROL_FLG = '2' ";
        return $query;
    }

    //締め日の取得
    function getAppointedDay($model, $year, $school_kind, $month, $semester) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND = '{$school_kind}' AND ";
        }
        $query .= "     INT(MONTH) = {$month} AND ";
        $query .= "     SEMESTER = '{$semester}' ";

        return $query;
    }
    
    //締め日の取得(学年)
    function getAppointedGradeDay($year, $month, $semester, $setGrade) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     INT(MONTH) = {$month} AND ";
        $query .= "     SEMESTER = '{$semester}' ";
        $query .= " AND GRADE = '{$setGrade}' ";

        return $query;
    }
    
    //更新チェック
    function checker($year, $semester) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     MONTH(SDATE) AS SMONTH, ";
        $query .= "     DAY(SDATE) AS SDAY, ";
        $query .= "     MONTH(EDATE) AS EMONTH, ";
        $query .= "     DAY(EDATE) AS EDAY     ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER = '{$semester}' ";

        return $query;
    }
    
    //更新処理
    function getInsertQuery($model, $semester, $month, $appointedDay) {
        if (strlen($month) == 1) {
            $month = '0' . $month;
        }
        $data = array();
        $data["YEAR"][TEXT]           = $model->year;
        $data["MONTH"][TEXT]          = $month;
        $data["SEMESTER"][TEXT]       = $semester;
        $data["APPOINTED_DAY"][TEXT]  = $appointedDay;
        $data["GRADE"][TEXT]          = $model->grade;
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "SYSDATE()";
        $query = Query::insertSQL($data, "APPOINTED_DAY_GRADE_MST");

        return $query;
    }
    
    //削除処理（更新前）
    function getDeleteQuery($model) {
        $query  = " DELETE FROM ";
        $query .= "     APPOINTED_DAY_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     GRADE = '{$model->grade}' ";

        return $query;
    }
}
?>
