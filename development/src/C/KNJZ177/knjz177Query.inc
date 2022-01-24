<?php

require_once('for_php7.php');

class knjz177Query extends Query {
    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メインデータ作成
    function selectSemesAll($year) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }
    /* 対象月データの取得 */
    function selectMonthQuery($month, $year, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->school_kind."05";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     namecd2, ";
        $query .= "     name1, ";
        $query .= "     namespare1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year  = '{$year}' ";
        $query .= "     AND namecd1 = '{$setNameCd}' ";
        $query .= "     AND namecd2 IN (SELECT ";
        $query .= "                         CONTROL_CODE ";
        $query .= "                     FROM ";
        $query .= "                         ADMIN_CONTROL_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR='{$year}' AND CONTROL_FLG ='2' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     AND SCHOOL_KIND = '".$model->school_kind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "                    ) ";
        $query .= "     AND namecd2 = '".sprintf('%02d',$month)."' ";
        $query .= " ORDER BY ";
        $query .= "     namespare1 ";

        return $query;
    }
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
    function getAppointedDay($year, $school_kind , $month, $semester) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SCHOOL_KIND = '{$school_kind}' AND ";
        $query .= "     INT(MONTH) = {$month} AND ";
        $query .= "     SEMESTER = '{$semester}' ";

        return $query;
    }

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

    function getUpdateQuery($model, $table) {
        $data = array();
        $data["APPOINTED_DAY"][DATE]       = $model->SIMEBI;

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where =  " WHERE "
                 ."     YEAR = '{$model->YEAR}' AND "
                 ."     INT(MONTH) = {$model->MONTH} AND "
                 ."     SEMESTER = '{$model->SEMESTER}' AND "
                 ."     APPOINTED_DAY <> '{$model->SIMEBI}' ";
       if ($model->Properties["use_prg_schoolkind"] == "1") {
           $where .= "     AND SCHREGNO IN ( "
                    ."           SELECT "
                    ."             RD.SCHREGNO "
                    ."           FROM "
                    ."             SCHREG_REGD_DAT RD "
                    ."             INNER JOIN SCHREG_REGD_GDAT GD "
                    ."                ON GD.YEAR = RD.YEAR "
                    ."               AND GD.GRADE = RD.GRADE "
                    ."           WHERE "
                    ."               RD.YEAR = '{$model->YEAR}' AND "
                    ."               RD.SEMESTER = '{$model->SEMESTER}' AND "
                    ."               GD.SCHOOL_KIND = '{$model->school_kind}'"
                    ."           ) ";
       }

        $query = Query::updateSQL($data, $table, $where);
        return $query;
    }

    function getInsertQuery($model) {
        $month = $model->MONTH;
        if (strlen($month) == 1) {
            $month = '0' . $month;
        }
        $data["YEAR"][TEXT]           = $model->YEAR;
        $data["SCHOOL_KIND"][TEXT]    = $model->school_kind;
        $data["MONTH"][TEXT]          = $month;
        $data["SEMESTER"][TEXT]       = $model->SEMESTER;
        $data["APPOINTED_DAY"][TEXT]  = $model->SIMEBI;
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "SYSDATE()";
        $query = Query::insertSQL($data, "appointed_day_mst");

        return $query;
    }

    function getDeleteQuery($model) {
        $month = $model->MONTH;
        if (strlen($month) == 1) {
            $month = '0' . $month;
        }
        $query  = " DELETE FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->YEAR}' AND ";
        $query .= "     SCHOOL_KIND = '{$model->school_kind}' AND ";
        $query .= "     MONTH       = '{$month}' AND ";
        $query .= "     SEMESTER    = '{$model->SEMESTER}' ";

        return $query;
    }
}
?>
