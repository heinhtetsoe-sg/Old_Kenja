<?php

require_once('for_php7.php');

class knjf120aQuery extends Query {
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

    //名称マスタから性別を取得
    function getSelectName($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS NAMECD, ";
        $query .= "     NAME2 AS NAME ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z002' ";
        $query .= " ORDER BY NAMECD ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($type, $model) {

        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';

        $query  = " WITH ";
        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " SCH_INFO AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.SEX, ";
            $query .= "         T2.SCHOOL_KIND ";
            $query .= "     FROM ";
            $query .= "         GRD_BASE_MST T1, ";
            $query .= "         SCHREG_REGD_GDAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.GRD_GRADE    = T2.GRADE AND ";
            $query .= "         T2.YEAR         = '".CTRL_YEAR."' ";
            $query .= "     UNION ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         L1.SEX, ";
            $query .= "         T2.SCHOOL_KIND ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO, ";
            $query .= "         SCHREG_REGD_GDAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = T2.YEAR AND ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "         T1.GRADE    = T2.GRADE AND ";
            $query .= "         T1.SCHREGNO NOT IN (SELECT SCHREGNO FROM GRD_BASE_MST) ";
            $query .= " ), ";
        }
        $query .= " VISITREC_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.SEX ";
        $query .= "     FROM ";
        $query .= "         NURSEOFF_VISITREC_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "         INNER JOIN SCH_INFO T2 ON T1.SCHREGNO = T2.SCHREGNO AND T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         INNER JOIN SCH_INFO T2 ON T1.SCHREGNO = T2.SCHREGNO AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= "         LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.VISIT_DATE BETWEEN '".$sdate."' AND '".$edate."' ";
        $query .= "         AND T1.TYPE = '".$type."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || '".$type."' AS KUBUN, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 4    THEN 1 ELSE 0 END) AS NINZUU4, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 5    THEN 1 ELSE 0 END) AS NINZUU5, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 6    THEN 1 ELSE 0 END) AS NINZUU6, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 7    THEN 1 ELSE 0 END) AS NINZUU7, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 8    THEN 1 ELSE 0 END) AS NINZUU8, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 9    THEN 1 ELSE 0 END) AS NINZUU9, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 10   THEN 1 ELSE 0 END) AS NINZUU10, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 11   THEN 1 ELSE 0 END) AS NINZUU11, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 12   THEN 1 ELSE 0 END) AS NINZUU12, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 1    THEN 1 ELSE 0 END) AS NINZUU1, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 2    THEN 1 ELSE 0 END) AS NINZUU2, ";
        $query .= "     SUM(CASE MONTH(L2.VISIT_DATE) WHEN 3    THEN 1 ELSE 0 END) AS NINZUU3, ";
        $query .= "     SUM(CASE WHEN L2.VISIT_DATE IS NOT NULL THEN 1 ELSE 0 END) AS TOTAL ";
        $query .= " FROM ";
        $query .= "     (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'Z002') T1 ";
        $query .= "     LEFT JOIN VISITREC_DAT L2 ON L2.SEX = T1.NAMECD2 ";
        $query .= " GROUP BY ";
        $query .= "     T1.NAMECD2 ";
        $query .= " ORDER BY ";
        $query .= "     KUBUN ";

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
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
?>
