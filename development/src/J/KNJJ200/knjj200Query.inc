<?php

require_once('for_php7.php');

class knjj200Query extends Query {

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

    //名称取得
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
        $query .= "     T2.EXECUTIVECD, ";
        $query .= "     T3.NAME1 AS EXECUTIVENAME, ";
        $query .= "     T2.REMARK ";
        $query .= " FROM ";
        $query .= "     GRADE T1, ";
        $query .= "     GUARDIAN_COMMITTEE_HIST_DAT T2 ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T2.YEAR = T3.YEAR AND ";
        $query .= "                                T3.NAMECD1 = 'J005' AND ";
        $query .= "                                T2.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY T1.YEAR ";

        return $query;
    }

    //１レコード取得（右上リストで選択された１行を取得）
    function getRow($model)
    {
        $query  = " SELECT * FROM GUARDIAN_COMMITTEE_HIST_DAT ";
        $query .= " WHERE YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "       SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //INSERT
    function &getUpdateQuery($model, $cmd)
    {
        if($cmd == "update"){
            knjj200Query::getDeleteQuery($model);
        }
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["EXECUTIVECD"][TEXT]      = $model->field["EXECUTIVECD"];
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "GUARDIAN_COMMITTEE_HIST_DAT");
        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM GUARDIAN_COMMITTEE_HIST_DAT ";
        $query .= " WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "   AND SCHREGNO = '".$model->schregno."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
