<?php

require_once('for_php7.php');
class knjg020aQuery extends Query {

    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    function getSchoolMst($model) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->setSchoolKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    function getPageData($model) {

        $query .= " WITH ";
        $query .= " CERTIF_DATA AS( ";
        $query .= "     SELECT  T1.CERTIF_INDEX ";
        $query .= "     ,T2.REMARK1 ";
        $query .= "     ,T1.CERTIF_NO ";
        $query .= "     ,T1.CERTIF_KINDCD ";
        $query .= "     ,T1.ISSUEDATE ";
        $query .= "     ,T1.SCHREGNO ";
        $query .= "     ,T2.REMARK14 AS HAKKOUZUMI ";
        $query .= "     ,VALUE(T1.GRADUATE_FLG, '0') AS GRADUATE_FLG ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT T1 ";
        $query .= "     LEFT JOIN CERTIF_DETAIL_EACHTYPE_DAT T2 ";
        $query .= "          ON T2.YEAR = T1.YEAR ";
        $query .= "          AND T2.CERTIF_INDEX = T1.CERTIF_INDEX ";
        $query .= " WHERE   T1.ISSUECD = '1' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND (T2.REMARK1 IS NOT NULL ";
        $query .= "          OR T1.CERTIF_NO IS NOT NULL) ";
        if ($model->field["STD_DIV"] == "2") {
            $query .= "     AND T1.GRADUATE_FLG = '0' ";
        } else if ($model->field["STD_DIV"] == "1") {
            $query .= "     AND T1.GRADUATE_FLG = '1' ";
        }
        $query .= " ) ";

        $query .= "     ,SCHREG_DATA AS( ";
        $query .= "         SELECT ";
        $query .= "             '0' AS GRAD_KUBUN, SCHREGNO, NAME, REAL_NAME, GRD_DATE ";
        $query .= "         FROM ";
        $query .= "             SCHREG_BASE_MST W1 ";
        $query .= "         WHERE ";
        $query .= "             EXISTS(SELECT 'X' FROM CERTIF_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO) ";
        if ($model->field["STD_DIV"] != "2") {
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         '1' AS GRAD_KUBUN, SCHREGNO, NAME, REAL_NAME, GRD_DATE ";
            $query .= "     FROM ";
            $query .= "         GRD_BASE_MST W1 ";
            $query .= "     WHERE ";
            $query .= "         EXISTS(SELECT 'X' FROM CERTIF_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO) ";
        }
        $query .= " ), REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.HR_NAME, ";
        $query .= "     L1.HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= "   AND GDAT.SCHOOL_KIND = '".$model->setSchoolKind."' ";
        } else if ("1" == $model->Properties["useSchool_KindField"] && SCHOOLKIND != '') {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= "   AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.GRADE = L1.GRADE ";
        $query .= "          AND T1.HR_CLASS = L1.HR_CLASS, ";
        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(SEMESTER) AS MAX_SEME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_DATA) ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO ";
        $query .= "     ) T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.MAX_SEME ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ), GREGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.HR_NAME, ";
        $query .= "     L1.HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     GRD_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= "   AND GDAT.SCHOOL_KIND = '".$model->setSchoolKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != '') {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= "   AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN GRD_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.GRADE = L1.GRADE ";
        $query .= "          AND T1.HR_CLASS = L1.HR_CLASS, ";
        $query .= "     (SELECT ";
        $query .= "         W1.YEAR, ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         MAX(W1.SEMESTER) AS MAX_SEME ";
        $query .= "     FROM ";
        $query .= "         GRD_REGD_DAT W1, ";
        $query .= "        (SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             MAX(YEAR) AS MAX_YEAR ";
        $query .= "         FROM ";
        $query .= "             GRD_REGD_DAT ";
        $query .= "         WHERE ";
        $query .= "             SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_DATA) ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO ";
        $query .= "        ) W2 ";
        $query .= "     WHERE ";
        $query .= "         W1.SCHREGNO = W2.SCHREGNO ";
        $query .= "         AND W1.SCHREGNO = W2.MAX_YEAR ";
        $query .= "     GROUP BY ";
        $query .= "         W1.YEAR, ";
        $query .= "         W1.SCHREGNO ";
        $query .= "     ) T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.MAX_SEME ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= " ,DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.CERTIF_INDEX ";
        $query .= "         ,W1.REMARK1 ";
        $query .= "         ,W1.CERTIF_NO ";
        $query .= "         ,W1.CERTIF_KINDCD ";
        $query .= "         ,W1.ISSUEDATE ";
        $query .= "         ,W1.SCHREGNO ";
        $query .= "         ,W1.HAKKOUZUMI ";
        $query .= "         ,W1.GRADUATE_FLG ";
        $query .= "     FROM ";
        $query .= "         CERTIF_DATA W1 ";
        $query .= "     WHERE ";
        $query .= "         EXISTS(SELECT 'X' FROM SCHREG_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.REMARK1 ";
        $query .= "     ,T1.CERTIF_NO ";
        $query .= "     ,T1.CERTIF_INDEX ";
        $query .= "     ,T1.ISSUEDATE ";
        $query .= "     ,T3.KINDNAME ";
        $query .= "     ,CASE WHEN T2.NAME IS NULL THEN T2_2.NAME ELSE T2.NAME END AS NAME ";
        $query .= "     ,CASE WHEN T2.REAL_NAME IS NULL THEN T2_2.REAL_NAME ELSE T2.REAL_NAME END AS REAL_NAME ";
        $query .= "     ,T4.SCHREGNO AS PRINT_REAL_NAME ";
        $query .= "     ,T4.NAME_OUTPUT_FLG ";
        $query .= "     ,T1.SCHREGNO ";
        $query .= "     ,T1.HAKKOUZUMI ";
        $query .= " FROM ";
        $query .= "     DATA T1 ";
        $query .= "     LEFT JOIN SCHREG_DATA T2 ON T2.GRAD_KUBUN = T1.GRADUATE_FLG ";
        $query .= "           AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_DATA T2_2 ON T2_2.GRAD_KUBUN = '0' ";
        $query .= "           AND T2_2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT  JOIN CERTIF_KIND_MST T3 ON T3.CERTIF_KINDCD = T1.CERTIF_KINDCD ";
        $query .= "     LEFT  JOIN SCHREG_NAME_SETUP_DAT T4 ON T4.DIV = '07' AND T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT  JOIN CERTIF_ISSUE_PRINT_ORDER_DAT ORDER ON ORDER.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T1.CERTIF_INDEX = ORDER.CERTIF_INDEX ";
        $query .= "     LEFT JOIN REGD_T ON REGD_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GREGD_T ON GREGD_T.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE ";
            $query .= "     REGD_T.SCHREGNO IS NOT NULL ";
            $query .= "     OR GREGD_T.SCHREGNO IS NOT NULL ";
        } else if ("1" == $model->Properties["useSchool_KindField"] && SCHOOLKIND != '') {
            $query .= " WHERE ";
            $query .= "     REGD_T.SCHREGNO IS NOT NULL ";
            $query .= "     OR GREGD_T.SCHREGNO IS NOT NULL ";
        }
        $query .= " ORDER BY VALUE(ORDER.PRINT_NO, 999999), T1.ISSUEDATE, CAST(T1.CERTIF_NO AS INT) ";

        return $query;
    }

    function getDetailDat($model, $certifData)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CERTIF_INDEX = '{$certifData["CERTIF_INDEX"]}' ";

        return $query;
    }

    function updatePrintZumi($model, $certifData, $detailCnt)
    {
        $data = array();
        if ($detailCnt == 0) {
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["CERTIF_INDEX"][TEXT]     = $certifData["CERTIF_INDEX"];
            $data["SCHREGNO"][TEXT]         = $certifData["SCHREGNO"];
            $data["TYPE"][TEXT]             = "1";
        }
        $data["REMARK14"][TEXT]             = "1";
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($detailCnt == 0) {
            $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT");
        } else {
            $where = " WHERE YEAR = '".CTRL_YEAR."' AND CERTIF_INDEX = '{$certifData["CERTIF_INDEX"]}'";
            $query = Query::updateSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT", $where);
        }

        return $query;
    }
}
?>
