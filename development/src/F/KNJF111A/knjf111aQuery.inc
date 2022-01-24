<?php

require_once('for_php7.php');

class knjf111aQuery extends Query {
    //年度コンボ
    function getYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }
    //校種取得
    function getSchkind($model, $year) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
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

    //名称マスタから来室理由を取得
    function getSelectName($namecd1) {
        $query = " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = '".$namecd1."' ORDER BY 1";

        return $query;
    }

    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    function getGdat($model) {
        $query  = " WITH GRADES AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         GRADE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     L1.SCHOOL_KIND, ";
        $query .= "     L1.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     GRADES T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_REGD_GDAT L1 ON  L1.YEAR         = T1.YEAR ";
            $query .= "                         AND L1.GRADE        = T1.GRADE ";
            $query .= "                         AND L1.SCHOOL_KIND  = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_REGD_GDAT L1 ON  L1.YEAR         = T1.YEAR ";
            $query .= "                         AND L1.GRADE        = T1.GRADE ";
            $query .= "                         AND L1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else {
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_GDAT L1 ON  L1.YEAR     = T1.YEAR ";
            $query .= "                         AND L1.GRADE    = T1.GRADE ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     SCHOOL_KIND ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model, $type, $namecd1, $gdat_array) {

        $sdate = str_replace("/","-",$model->field["SDATE"]);
        $edate = str_replace("/","-",$model->field["EDATE"]);

        /* 対象の生徒情報の取得 */
        $query  = " WITH REGD_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         MAX(SEMESTER) AS SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= " ), SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T2.SEX ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO, ";
        $query .= "         REGD_INFO T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     '' AS KUBUN, ";
        $query .= "     T1.NAMECD2, ";
        foreach ($gdat_array as $school_kind => $grade_array) {
            $kind = strtoupper($school_kind);
            foreach($grade_array as $grade) {
                $query .= "     SUM(CASE WHEN T3.GRADE = '{$grade}' AND T3.SEX = '1'  THEN 1 ELSE 0 END) AS NUM_{$grade}M, ";
                $query .= "     SUM(CASE WHEN T3.GRADE = '{$grade}' AND T3.SEX = '2'  THEN 1 ELSE 0 END) AS NUM_{$grade}F, ";
                $query .= "     SUM(CASE WHEN T3.GRADE = '{$grade}'                   THEN 1 ELSE 0 END) AS NUM_{$grade}T, ";
            }
            $grades = implode("','", $grade_array);
            $query .= "     SUM(CASE WHEN T3.GRADE IN ('{$grades}') AND T3.SEX = '1'       THEN 1 ELSE 0 END) AS NUM_{$kind}M, ";
            $query .= "     SUM(CASE WHEN T3.GRADE IN ('{$grades}') AND T3.SEX = '2'       THEN 1 ELSE 0 END) AS NUM_{$kind}F, ";
            $query .= "     SUM(CASE WHEN T3.GRADE IN ('{$grades}') AND T3.SEX IS NOT NULL THEN 1 ELSE 0 END) AS NUM_{$kind}T, ";
        }

        $query .= "     '' AS REMARK ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NURSEOFF_VISITREC_DAT T2 ON (T2.VISIT_REASON1 = T1.NAMECD2 ";
        $query .= "                              OR  T2.VISIT_REASON2 = T1.NAMECD2 ";
        $query .= "                              OR  T2.VISIT_REASON3 = T1.NAMECD2) ";
        $query .= "                              AND T2.TYPE = '".$type."' ";
        $query .= "                              AND T2.VISIT_DATE BETWEEN '".$sdate."' AND '".$edate."' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCH_INFO T3 ON T2.SCHREGNO = T3.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = '".$namecd1."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.NAMECD2 ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";

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
