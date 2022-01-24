<?php

require_once('for_php7.php');

class knjf121aQuery extends Query {
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

    //名称マスタから性別の略称を取得
    function getSelectName($namecd1) {
        $query = " SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = '".$namecd1."' ORDER BY 1";

        return $query;
    }

    //学年名称を取得
    function getGradeName($grade) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model, $type) {

        $sdate = str_replace("/","-",$model->field["SDATE"]);
        $edate = str_replace("/","-",$model->field["EDATE"]);

        $query  = " WITH GRADE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.GRADE_NAME1 AS GRADE_NAME, ";
        $query .= "         T2.NAMECD2 AS SEX, ";
        $query .= "         T2.ABBV1 AS ABBV ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT T1, ";
        $query .= "         NAME_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T2.NAMECD1 = 'Z002' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND  = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ), REGD_INFO AS ( ";
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

        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.SEX, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 4            THEN 1 ELSE 0 END) AS NUM4, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 5            THEN 1 ELSE 0 END) AS NUM5, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 6            THEN 1 ELSE 0 END) AS NUM6, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 7            THEN 1 ELSE 0 END) AS NUM7, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 8            THEN 1 ELSE 0 END) AS NUM8, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 9            THEN 1 ELSE 0 END) AS NUM9, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 10           THEN 1 ELSE 0 END) AS NUM10, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 11           THEN 1 ELSE 0 END) AS NUM11, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 12           THEN 1 ELSE 0 END) AS NUM12, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 1            THEN 1 ELSE 0 END) AS NUM1, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 2            THEN 1 ELSE 0 END) AS NUM2, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 3            THEN 1 ELSE 0 END) AS NUM3, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) IS NOT NULL    THEN 1 ELSE 0 END) AS NUM ";
        $query .= "     FROM ";
        $query .= "         GRADE T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCH_INFO L1 ON L1.GRADE = T1.GRADE AND L1.SEX = T1.SEX ";
        $query .= "     LEFT JOIN ";
        $query .= "         NURSEOFF_VISITREC_DAT L2 ON L2.SCHREGNO = L1.SCHREGNO AND ";
        if($type != '9') {
            $query .= "         L2.TYPE = '".$type."' AND ";
        }
        $query .= "         L2.VISIT_DATE BETWEEN '".$sdate."' AND '".$edate."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.SEX ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         '9' AS SEX, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 4            THEN 1 ELSE 0 END) AS NUM4, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 5            THEN 1 ELSE 0 END) AS NUM5, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 6            THEN 1 ELSE 0 END) AS NUM6, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 7            THEN 1 ELSE 0 END) AS NUM7, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 8            THEN 1 ELSE 0 END) AS NUM8, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 9            THEN 1 ELSE 0 END) AS NUM9, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 10           THEN 1 ELSE 0 END) AS NUM10, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 11           THEN 1 ELSE 0 END) AS NUM11, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 12           THEN 1 ELSE 0 END) AS NUM12, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 1            THEN 1 ELSE 0 END) AS NUM1, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 2            THEN 1 ELSE 0 END) AS NUM2, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) = 3            THEN 1 ELSE 0 END) AS NUM3, ";
        $query .= "         SUM(CASE WHEN MONTH(L2.VISIT_DATE) IS NOT NULL    THEN 1 ELSE 0 END) AS NUM ";
        $query .= "     FROM ";
        $query .= "         GRADE T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCH_INFO L1 ON L1.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN ";
        $query .= "         NURSEOFF_VISITREC_DAT L2 ON L2.SCHREGNO = L1.SCHREGNO AND ";
        if($type != '9') {
            $query .= "         L2.TYPE = '".$type."' AND ";
        }
        $query .= "         L2.VISIT_DATE BETWEEN '".$sdate."' AND '".$edate."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.SEX ";
        $query .= "     ORDER BY ";
        $query .= "         GRADE, ";
        $query .= "         SEX ";

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
