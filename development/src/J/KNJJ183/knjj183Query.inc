<?php

require_once('for_php7.php');

class knjj183Query extends Query {
    //校種取得
    function getSchoolkind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";

        return $query;
    }

    //生徒情報の取得
    function getSchData($schregno) {
        $query  = " WITH SCHADDTABLE AS ( ";
        $query .= "                     SELECT ";
        $query .= "                         S1.SCHREGNO, ";
        $query .= "                         S1.ZIPCD, ";
        $query .= "                         N1.NAME1, ";
        $query .= "                         S1.ADDR1, ";
        $query .= "                         S1.ADDR2, ";
        $query .= "                         S1.TELNO, ";
        $query .= "                         S1.TELNO2 ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_ADDRESS_DAT S1 ";
        $query .= "                         LEFT JOIN V_NAME_MST N1 ON NAMECD1 ='A020'  ";
        $query .= "                                                AND NAMECD2 = S1.AREACD  ";
        $query .= "                                                AND YEAR    = '".CTRL_YEAR."', ";
        $query .= "                         (SELECT ";
        $query .= "                             SCHREGNO,MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_ADDRESS_DAT ";
        $query .= "                          WHERE ";
        $query .= "                              SCHREGNO = '".$schregno."' ";
        $query .= "                          GROUP BY ";
        $query .= "                              SCHREGNO ) S2 ";
        $query .= "                     WHERE ";
        $query .= "                             S1.SCHREGNO  = '".$schregno."' ";
        $query .= "                         AND S1.ISSUEDATE = S2.ISSUEDATE ";
        $query .= "                     ) ";
        $query .= " SELECT ";
        $query .= "     N3.ABBV1, ";
        $query .= "     T6.GRADE_NAME1 AS GRADE, ";
        $query .= "     T4.HR_CLASS_NAME1 AS HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.SCHREGNO,  ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     N1.NAME1 AS SEX, ";
        $query .= "     F1.FINSCHOOL_NAME AS JFIN_NAME, ";
        $query .= "     F2.FINSCHOOL_NAME AS HFIN_NAME, ";
        $query .= "     T3.ZIPCD, ";
        $query .= "     T3.ADDR1, ";
        $query .= "     T3.ADDR2, ";
        $query .= "     T3.TELNO, ";
        $query .= "     T3.TELNO2, ";
        $query .= "     T1.BIRTHDAY ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " LEFT JOIN SCHADDTABLE T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T4 ON T2.YEAR = T4.YEAR ";
        $query .= "                              AND T2.SEMESTER             = T4.SEMESTER ";
        $query .= "                              AND T2.GRADE || T2.HR_CLASS = T4.GRADE || T4.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_ENVIR_DAT T5 ON T1.SCHREGNO = T5.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T6 ON T2.YEAR  = T6.YEAR ";
        $query .= "                              AND T2.GRADE = T6.GRADE ";
        $query .= " LEFT JOIN V_NAME_MST N1 ON T2.YEAR    = N1.YEAR ";
        $query .= "                        AND N1.NAMECD1 = 'Z002' ";
        $query .= "                        AND T1.SEX     = N1.NAMECD2 ";
        $query .= " LEFT JOIN V_NAME_MST N3 ON T2.YEAR        = N3.YEAR ";
        $query .= "                        AND N3.NAMECD1     = 'A023' ";
        $query .= "                        AND T6.SCHOOL_KIND = N3.NAME1 ";
        $query .= " LEFT JOIN SCHREG_ENT_GRD_HIST_DAT H1 ON T1.SCHREGNO    = H1.SCHREGNO ";
        $query .= "                                     AND H1.SCHOOL_KIND = 'J' ";
        $query .= " LEFT JOIN FINSCHOOL_MST F1       ON H1.FINSCHOOLCD = F1.FINSCHOOLCD ";
        $query .= " LEFT JOIN SCHREG_ENT_GRD_HIST_DAT H2 ON T1.SCHREGNO    = H2.SCHREGNO ";
        $query .= "                                     AND H2.SCHOOL_KIND = 'H' ";
        $query .= " LEFT JOIN FINSCHOOL_MST F2       ON H2.FINSCHOOLCD = F2.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //保護者情報の取得(BRANCH_MST)
    function getBranchMst($schregno, $schoolkind, $year) {
        $year = ($year == "lastyear") ? CTRL_YEAR -1 : CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GUARD_NAME, ";
        $query .= "     GUARD_KANA, ";
        $query .= "     GUARD_ZIPCD, ";
        $query .= "     GUARD_ADDR1, ";
        $query .= "     GUARD_ADDR2, ";
        $query .= "     GUARD_TELNO, ";
        $query .= "     GUARD_TELNO2, ";
        $query .= "     BRANCHCD, ";
        $query .= "     BRANCH_POSITION, ";
        $query .= "     SEND_NAME, ";
        $query .= "     RESIDENTCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_BRANCH_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '000000000000' ";
        $query .= "     AND SCHOOL_KIND = '".$schoolkind."' ";
        $query .= "     AND YEAR        = '".$year."' ";
        $query .= "     AND SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //保護者情報の取得(コピー用)
    function getGuardianAddr($schregno) {
        $query  = " SELECT ";
        $query .= "     GUARD_NAME, ";
        $query .= "     GUARD_KANA, ";
        $query .= "     GUARD_ZIPCD, ";
        $query .= "     GUARD_ADDR1, ";
        $query .= "     GUARD_ADDR2, ";
        $query .= "     GUARD_TELNO, ";
        $query .= "     GUARD_TELNO2 ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //支部取得
    function getBranchName() {
        $query  = " SELECT ";
        $query .= "     BRANCHCD AS VALUE, ";
        $query .= "     BRANCHCD || '：' || BRANCHNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     BRANCH_MST ";

        return $query;
    }

    //名称取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]         = "000000000000";
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolkind;
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["BRANCHCD"][TEXT]         = $model->field["BRANCHCD"];
        $data["BRANCH_POSITION"][TEXT]  = $model->field["BRANCH_POSITION"];
        $data["GUARD_NAME"][TEXT]       = $model->field["GUARD_NAME"];
        $data["GUARD_KANA"][TEXT]       = $model->field["GUARD_KANA"];
        $data["GUARD_ZIPCD"][TEXT]      = $model->field["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $model->field["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $model->field["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]     = $model->field["GUARD_TELNO2"];
        $data["SEND_NAME"][TEXT]        = $model->field["SEND_NAME"];
        $data["RESIDENTCD"][TEXT]       = $model->field["RESIDENTCD"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data,"SCHREG_BRANCH_DAT");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateModel($model) {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["BRANCHCD"][TEXT]         = $model->field["BRANCHCD"];
        $data["BRANCH_POSITION"][TEXT]  = $model->field["BRANCH_POSITION"];
        $data["GUARD_NAME"][TEXT]       = $model->field["GUARD_NAME"];
        $data["GUARD_KANA"][TEXT]       = $model->field["GUARD_KANA"];
        $data["GUARD_ZIPCD"][TEXT]      = $model->field["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $model->field["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $model->field["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]     = $model->field["GUARD_TELNO2"];
        $data["SEND_NAME"][TEXT]        = $model->field["SEND_NAME"];
        $data["RESIDENTCD"][TEXT]       = $model->field["RESIDENTCD"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "         SCHOOLCD    = '000000000000' ";
        $where .= "     AND SCHOOL_KIND = '".$model->schoolkind."' ";
        $where .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $where .= "     AND SCHREGNO    = '".$model->schregno."' ";

        $query = Query::updateSQL($data,"SCHREG_BRANCH_DAT",$where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM SCHREG_BRANCH_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '000000000000' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
?>
