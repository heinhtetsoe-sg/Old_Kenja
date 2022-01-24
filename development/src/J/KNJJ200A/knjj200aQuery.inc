<?php

require_once('for_php7.php');

class knjj200aQuery extends Query {

    //生徒情報取得
    function getSchInfo($model){
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     NAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //年組名取得
    function getHrName($model) {
        $query  = " WITH SEMESTER AS( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         MAX(SEMESTER) AS SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SCHREGNO = '".$model->schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.GRADE || T2.HR_CLASS AS GRADE_HR_CLASS, ";
        $query .= "     T3.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER T1, ";
        $query .= "     SCHREG_REGD_DAT T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.GRADE = T3.GRADE AND ";
        $query .= "     T2.HR_CLASS = T3.HR_CLASS ";

        return $query;
    }

    //区分取得
    function getNameMst($namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";

        return $query;
    }

    //役職名取得
    function getExecutivecd($div) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTIVECD AS VALUE, ";
        $query .= "     T1.EXECUTIVECD || ' ' || T1.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_COMMITTEE_MST T1, ";
        $query .= "     GUARDIAN_COMMITTEE_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.DIV = '".$div."' ";
        $query .= "     AND T2.DIV = T1.DIV ";
        $query .= "     AND T2.EXECUTIVECD = T1.EXECUTIVECD ";
        $query .= " ORDER BY ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.EXECUTIVECD ";

        return $query;
    }

    //保護者会役員履歴データ取得
    function getGrdCommitHistDat($model)
    {
        $query  = " WITH SEMESTER AS( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         MAX(SEMESTER) AS SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$model->schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= " ), GRADE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T2.GRADE || T2.HR_CLASS AS GRADE_HR_CLASS, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SEMESTER T1, ";
        $query .= "         SCHREG_REGD_DAT T2, ";
        $query .= "         SCHREG_REGD_HDAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T1.YEAR = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.GRADE = T3.GRADE AND ";
        $query .= "         T2.HR_CLASS = T3.HR_CLASS ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.DIV, ";
        $query .= "     N1.NAME1 AS DIV_NAME, ";
        $query .= "     T2.EXECUTIVECD, ";
        $query .= "     T4.NAME AS EXECUTIVENAME, ";
        $query .= "     T2.GUARD_NAME_DIV, ";
        $query .= "     G1.GUARD_NAME AS GUARD_NAME1, ";
        $query .= "     G2.GUARD_NAME AS GUARD_NAME2, ";
        $query .= "     T2.REMARK ";
        $query .= " FROM ";
        $query .= "     GRADE T1, ";
        $query .= "     GUARDIAN_COMMITTEE_DAT T2 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR = T2.YEAR ";
        $query .= "             AND N1.NAMECD1 = 'J006' ";
        $query .= "             AND N1.NAMECD2 = T2.DIV ";
        $query .= "     LEFT JOIN GUARDIAN_COMMITTEE_YDAT T3 ";
        $query .= "              ON T3.YEAR = T2.YEAR ";
        $query .= "             AND T3.DIV = T2.DIV ";
        $query .= "             AND T3.EXECUTIVECD = T2.EXECUTIVECD ";
        $query .= "     LEFT JOIN GUARDIAN_COMMITTEE_MST T4 ";
        $query .= "              ON T4.DIV = T3.DIV ";
        $query .= "             AND T4.EXECUTIVECD = T3.EXECUTIVECD ";
        $query .= "     LEFT JOIN GUARDIAN_DAT G1 ON G1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN2_DAT G2 ON G2.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T2.DIV, ";
        $query .= "     T2.EXECUTIVECD ";

        return $query;
    }

    //１レコード取得（右上リストで選択された１行を取得）
    function getRow($schregno, $div, $executivecd)
    {
        $query  = " SELECT * FROM GUARDIAN_COMMITTEE_DAT ";
        $query .= " WHERE YEAR          = '".CTRL_YEAR."' ";
        $query .= "   AND SCHREGNO      = '{$schregno}' ";
        $query .= "   AND DIV           = '{$div}' ";
        $query .= "   AND EXECUTIVECD   = '{$executivecd}' ";

        return $query;
    }

    //保護者氏名を取得
    function getGuardName($model, $val) {
        $table = ($val == "2") ? "GUARDIAN2_DAT" : "GUARDIAN_DAT";

        $query  = " SELECT GUARD_NAME FROM {$table} ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //INSERT
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $cnt = get_count($db->getOne(knjj200aQuery::getRow($model->schregno, $model->field["DIV"], $model->field["EXECUTIVECD"])));
        Query::dbCheckIn($db);
        if ($cnt > 0) {
            knjj200aQuery::getDeleteQuery($model);
        }

        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["DIV"][TEXT]              = $model->field["DIV"];
        $data["EXECUTIVECD"][TEXT]      = $model->field["EXECUTIVECD"];
        $data["GUARD_NAME_DIV"][TEXT]   = $model->field["GUARD_NAME_DIV"];
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "GUARDIAN_COMMITTEE_DAT");
        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM GUARDIAN_COMMITTEE_DAT ";
        $query .= " WHERE YEAR          = '".CTRL_YEAR."' ";
        $query .= "   AND SCHREGNO      = '".$model->schregno."' ";
        $query .= "   AND DIV           = '".$model->field["DIV"]."' ";
        $query .= "   AND EXECUTIVECD   = '".$model->field["EXECUTIVECD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
