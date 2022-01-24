<?php

require_once('for_php7.php');

class knjf131aQuery extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //学校名称2取得
    function getSchoolName2($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOLNAME2 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else {
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //名称マスタから来室校時名を取得
    function getSelectName($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SCHOOL_KIND IN ('H','J') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND  = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     INT(GRADE) ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model, $grade, $type) {
        $sdate = str_replace("/", "-", $model->field["SDATE"]);
        $edate = str_replace("/", "-", $model->field["EDATE"]);
        $ctrl_year = CTRL_YEAR;

        $query  = " WITH REGD_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         MAX(SEMESTER) AS SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= " ), SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE     ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         REGD_INFO T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ), SUB_MAIN AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SEX, ";
        $query .= "     SUM(CASE WHEN DAYOFWEEK(T1.VISIT_DATE) = 2            THEN 1 ELSE 0 END) AS NINZUU1, ";
        $query .= "     SUM(CASE WHEN DAYOFWEEK(T1.VISIT_DATE) = 3            THEN 1 ELSE 0 END) AS NINZUU2, ";
        $query .= "     SUM(CASE WHEN DAYOFWEEK(T1.VISIT_DATE) = 4            THEN 1 ELSE 0 END) AS NINZUU3, ";
        $query .= "     SUM(CASE WHEN DAYOFWEEK(T1.VISIT_DATE) = 5            THEN 1 ELSE 0 END) AS NINZUU4, ";
        $query .= "     SUM(CASE WHEN DAYOFWEEK(T1.VISIT_DATE) = 6            THEN 1 ELSE 0 END) AS NINZUU5, ";
        $query .= "     SUM(CASE WHEN DAYOFWEEK(T1.VISIT_DATE) = 7            THEN 1 ELSE 0 END) AS NINZUU6, ";
        $query .= "     SUM(CASE WHEN DAYOFWEEK(T1.VISIT_DATE) = 1            THEN 1 ELSE 0 END) AS NINZUU7, ";
        $query .= "     SUM(CASE WHEN DAYOFWEEK(T1.VISIT_DATE) IS NOT NULL    THEN 1 ELSE 0 END) AS NINZUU ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCH_INFO L3 ON L3.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.VISIT_DATE BETWEEN '{$sdate}' AND '{$edate}' ";
        if($type != '9') {
            $query .= "     AND T1.TYPE = '{$type}' ";
        }
        $query .= "     AND L3.GRADE = '{$grade}' ";
        $query .= " GROUP BY ";
        $query .= "     L1.SEX ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     ABBV1, ";
        $query .= "     VALUE(NINZUU1, 0) AS NINZUU1, ";
        $query .= "     VALUE(NINZUU2, 0) AS NINZUU2, ";
        $query .= "     VALUE(NINZUU3, 0) AS NINZUU3, ";
        $query .= "     VALUE(NINZUU4, 0) AS NINZUU4, ";
        $query .= "     VALUE(NINZUU5, 0) AS NINZUU5, ";
        $query .= "     VALUE(NINZUU6, 0) AS NINZUU6, ";
        $query .= "     VALUE(NINZUU7, 0) AS NINZUU7, ";
        $query .= "     VALUE(NINZUU,  0) AS NINZUU ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SUB_MAIN L1 ON L1.SEX = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'Z002' ";
        $query .= "     AND NAMECD2 IN ('1', '2') ";
        $query .= " ORDER BY ";
        $query .= "     T1.ABBV1 DESC ";

        return $query;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
?>
