<?php

require_once('for_php7.php');

class knjf130aQuery extends Query {
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

    //学年名称取得
    function getGradeName() {
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model) {

        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';

        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO, ";
        $query .= "         S1.GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT S1, ";
        $query .= "         (SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             YEAR, ";
        $query .= "             MAX(SEMESTER) AS SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO, ";
        $query .= "             YEAR ) S2 ";
        $query .= "     WHERE ";
        $query .= "         S1.YEAR     = S2.YEAR AND ";
        $query .= "         S1.SEMESTER = S2.SEMESTER AND ";
        $query .= "         S1.SCHREGNO = S2.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND S1.GRADE IN (SELECT ";
            $query .= "                         E1.GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT E1 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR         = E1.YEAR AND ";
            $query .= "                         E1.SCHOOL_KIND  = '".$model->field["SCHKIND"]."' ";
            $query .= "                     ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND S1.GRADE IN (SELECT ";
            $query .= "                         E1.GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT E1 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR         = E1.YEAR AND ";
            $query .= "                         E1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                     ) ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     L4.GRADE, ";
        $query .= "     L3.NAMECD2, ";
        $query .= "     SUM(CASE L2.SEX WHEN '1' THEN 1 ELSE 0 END) AS DANSI, ";
        $query .= "     SUM(CASE L2.SEX WHEN '2' THEN 1 ELSE 0 END) AS JOSI ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD1 = 'F700' ";
        $query .= "                 AND L3.NAMECD2 = T1.VISIT_PERIODCD ";
        $query .= " INNER JOIN ";
        $query .= "     SCH_INFO L4 ON  L4.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "      T1.VISIT_DATE BETWEEN '".$sdate."' AND '".$edate."' ";
        $query .= " GROUP BY ";
        $query .= "     L4.GRADE, ";
        $query .= "     L3.NAMECD2, ";
        $query .= "     L3.NAMESPARE1 ";
        $query .= " ORDER BY ";
        $query .= "     L4.GRADE, ";
        $query .= "     L3.NAMESPARE1 ";

        return $query;
    }
}
?>
