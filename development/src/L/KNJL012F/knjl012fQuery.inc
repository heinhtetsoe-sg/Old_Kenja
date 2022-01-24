<?php

require_once('for_php7.php');

class knjl012fQuery extends Query {

    //出身学校コードを取得
    function getNameMstA023() {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'A023' ";
        $query .= "     AND NAME1 = 'J' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //入試制度取得
    function getApctDiv($namecd, $year, $applicantdiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 = '".$applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    function getTestDiv($namecd, $year, $testdiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 = '".$testdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試回数(2:高校のみ)
    function getTestdiv0($namecd, $year, $testdiv) {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.NAMECD1 = '{$namecd}' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getGradeHrclass() {
        $query  = " SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.GRADE || T1.HR_CLASS) AS LINE_NO, ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = T1.YEAR AND G1.GRADE = T1.GRADE  ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND G1.SCHOOL_KIND = 'J' ";
        $query .= "     AND G1.GRADE_CD = '03' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getSchregRegdDat($model) {
        $query  = " WITH T_REGD AS ( ";
        $query .= " SELECT ";
        $query .= "     '{$model->examno1Keta}' || '{$model->examno2Keta}' || substr(T1.ATTENDNO,2) AS EXAMNO, ";
        $query .= "     T1.ATTENDNO || '番　' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HRCLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.GRD_DIV IS NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     EXAMNO AS VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     T_REGD ";
        $query .= " WHERE ";
        $query .= "     EXAMNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             EXAMNO ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "             APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "             TESTDIV         = '".$model->field["TESTDIV"]."' ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志願者取得
    function getEntexamApplicantbaseDat($model) {
        $query  = " WITH T_REGD AS ( ";
        $query .= " SELECT ";
        $query .= "     '{$model->examno1Keta}' || '{$model->examno2Keta}' || substr(T1.ATTENDNO,2) AS EXAMNO, ";
        $query .= "     T1.ATTENDNO || '番　' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HRCLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.GRD_DIV IS NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO AS VALUE, ";
        $query .= "     T2.LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1, ";
        $query .= "     T_REGD T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     T1.EXAMNO       = T2.EXAMNO ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfo($model) {
        $query  = " WITH T_REGD AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '{$model->examno1Keta}' || '{$model->examno2Keta}' || substr(T1.ATTENDNO,2) AS EXAMNO, ";
        $query .= "     T1.ATTENDNO || '番　' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HRCLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.GRD_DIV IS NULL ";
        $query .= " ) ";
        $query .= " , MAX_ADDR AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) ";
        $query .= " , T_ADDR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ZIPCD, ";
        $query .= "         T1.ADDR1, ";
        $query .= "         T1.ADDR2, ";
        $query .= "         T1.TELNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT T1, ";
        $query .= "         MAX_ADDR T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         AND T1.ISSUEDATE = T2.ISSUEDATE ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     TRANSLATE_HK_K(T2.NAME_KANA) AS NAME_KANA, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDR1, ";
        $query .= "     L1.ADDR2, ";
        $query .= "     L1.TELNO, ";
        $query .= "     L2.GUARD_NAME, ";
        $query .= "     TRANSLATE_HK_K(L2.GUARD_KANA) AS GUARD_KANA, ";
        $query .= "     L2.GUARD_ZIPCD, ";
        $query .= "     L2.GUARD_ADDR1, ";
        $query .= "     L2.GUARD_ADDR2, ";
        $query .= "     L2.GUARD_TELNO, ";
        $query .= "     L2.RELATIONSHIP ";
        $query .= " FROM ";
        $query .= "     T_REGD T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN T_ADDR L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.EXAMNO IN ('".str_replace(",","','",$model->selectdata)."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //元号取得
    function getEracd($birthday, $FIELD) {
        $birthday= str_replace("-","/",$birthday);

        $query  = " SELECT ";
        $query .=       $FIELD ;
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L007' AND ";
        $query .= "     '".$birthday."' BETWEEN NAMESPARE2 AND NAMESPARE3 ";

        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT ---DELETE
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     TESTDIV = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     EXAMNO IN ( SELECT ";
        $query .= "                     '{$model->examno1Keta}' || '{$model->examno2Keta}' || substr(T1.ATTENDNO,2) AS EXAMNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT T1, ";
        $query .= "                     SCHREG_BASE_MST T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "                     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HRCLASS"]."' AND ";
        $query .= "                     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "                     T2.GRD_DIV IS NULL ) ";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---DELETE
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     EXAMNO IN ( SELECT ";
        $query .= "                     '{$model->examno1Keta}' || '{$model->examno2Keta}' || substr(T1.ATTENDNO,2) AS EXAMNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT T1, ";
        $query .= "                     SCHREG_BASE_MST T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "                     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HRCLASS"]."' AND ";
        $query .= "                     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "                     T2.GRD_DIV IS NULL ) ";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---DELETE
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     EXAMNO IN ( SELECT ";
        $query .= "                     '{$model->examno1Keta}' || '{$model->examno2Keta}' || substr(T1.ATTENDNO,2) AS EXAMNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT T1, ";
        $query .= "                     SCHREG_BASE_MST T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "                     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HRCLASS"]."' AND ";
        $query .= "                     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "                     T2.GRD_DIV IS NULL ) ";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DAT ---INSERT
        $result = $db->query(knjl012fQuery::getStudentInfo($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $eracd = $db->getOne(knjl012fQuery::getEracd($row["BIRTHDAY"], "NAMECD2"));
            $year = substr($row["BIRTHDAY"],0,4) - $db->getOne(knjl012fQuery::getEracd($row["BIRTHDAY"], "NAMESPARE1")) + 1;

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
            $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
            $data["TESTDIV0"][TEXT]         = $model->field["TESTDIV0"];//入試回数
            $data["SHDIV"][TEXT]            = '1';
            $data["DESIREDIV"][TEXT]        = '1';
            $data["RECEPTDATE"][TEXT]       = str_replace("/", "-", $model->field["RECEPTDATE"]);
            $data["NAME"][TEXT]             = $row["NAME"];
            $data["NAME_KANA"][TEXT]        = $row["NAME_KANA"];
            $data["SEX"][TEXT]              = $row["SEX"];
            $data["ERACD"][TEXT]            = ($row["BIRTHDAY"]) ? $eracd : '';
            $data["BIRTH_Y"][TEXT]          = ($row["BIRTHDAY"]) ? $year : '';
            $data["BIRTH_M"][TEXT]          = ($row["BIRTHDAY"]) ? substr($row["BIRTHDAY"],5,2) : '';
            $data["BIRTH_D"][TEXT]          = ($row["BIRTHDAY"]) ? substr($row["BIRTHDAY"],8) : '';
            $data["BIRTHDAY"][TEXT]         = $row["BIRTHDAY"];
            $data["FS_CD"][TEXT]            = $model->fsCd;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT ---INSERT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $row["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $row["ADDR1"];
            $data["ADDRESS2"][TEXT]         = $row["ADDR2"];
            $data["TELNO"][TEXT]            = $row["TELNO"];
            //保護者
            $data["GNAME"][TEXT]            = $row["GUARD_NAME"];
            $data["GKANA"][TEXT]            = $row["GUARD_KANA"];
            $data["GZIPCD"][TEXT]           = $row["GUARD_ZIPCD"];
            $data["GADDRESS1"][TEXT]        = $row["GUARD_ADDR1"];
            $data["GADDRESS2"][TEXT]        = $row["GUARD_ADDR2"];
            $data["GTELNO"][TEXT]           = $row["GUARD_TELNO"];
            $data["RELATIONSHIP"][TEXT]     = $row["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
