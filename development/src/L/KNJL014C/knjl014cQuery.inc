<?php

require_once('for_php7.php');

class knjl014cQuery extends Query {

    //学校コードを取得：五条・和歌山を判断するため
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //入試制度取得
    function getApctDiv($namecd, $year, $applicantdiv)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL, ";
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
    function getTestDiv($namecd, $year, $testdiv)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
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

    //年組取得
    function getGradeHrclass()
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE = '16' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getSchregRegdDat($model)
    {
        $query  = " SELECT ";
        $query .= "     '{$model->examno1Keta}' || substr(T1.HR_CLASS,3) || substr(T1.ATTENDNO,2) AS VALUE, ";
        $query .= "     T1.ATTENDNO || '番 ' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HRCLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.GRD_DIV IS NULL AND ";
        $query .= "     '{$model->examno1Keta}' || substr(T1.HR_CLASS,3) || substr(T1.ATTENDNO,2) NOT IN ( ";
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
    function getEntexamApplicantbaseDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO AS VALUE, ";
        $query .= "     T2.ATTENDNO || '番 ' || T3.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2, ";
        $query .= "     SCHREG_BASE_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HRCLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "     T3.GRD_DIV IS NULL AND ";
        $query .= "     T1.EXAMNO = '{$model->examno1Keta}' || substr(T2.HR_CLASS,3) || substr(T2.ATTENDNO,2) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfo($model)
    {
        $query  = " WITH MAX_ADDR AS ( ";
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
        $query .= "     '{$model->examno1Keta}' || substr(T1.HR_CLASS,3) || substr(T1.ATTENDNO,2) AS EXAMNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDR1, ";
        $query .= "     L1.ADDR2, ";
        $query .= "     L1.TELNO, ";
        $query .= "     L2.GUARD_NAME, ";
        $query .= "     L2.GUARD_KANA, ";
        $query .= "     L2.GUARD_ZIPCD, ";
        $query .= "     L2.GUARD_ADDR1, ";
        $query .= "     L2.GUARD_ADDR2, ";
        $query .= "     L2.GUARD_TELNO, ";
        $query .= "     L2.RELATIONSHIP ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= "     LEFT JOIN T_ADDR L1 ON L1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT L2 ON L2.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = '16' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.GRD_DIV IS NULL AND ";
        $query .= "     '{$model->examno1Keta}' || substr(T1.HR_CLASS,3) || substr(T1.ATTENDNO,2) IN ('".str_replace(",","','",$model->selectdata)."') ";
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";

        return $query;
    }

    //元号取得
    function getEracd($birthday, $FIELD)
    {
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
    function &getUpdateQuery($model)
    {
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
        $query .= "                     '{$model->examno1Keta}' || substr(T1.HR_CLASS,3) || substr(T1.ATTENDNO,2) AS EXAMNO ";
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
        $query .= "                     '{$model->examno1Keta}' || substr(T1.HR_CLASS,3) || substr(T1.ATTENDNO,2) AS EXAMNO ";
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
        $result = $db->query(knjl014cQuery::getStudentInfo($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $eracd = $db->getOne(knjl014cQuery::getEracd($row["BIRTHDAY"], "NAMECD2"));
            $year = substr($row["BIRTHDAY"],0,4) - $db->getOne(knjl014cQuery::getEracd($row["BIRTHDAY"], "NAMESPARE1")) + 1;

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
            $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
            $data["SHDIV"][TEXT]            = '1';
            $data["DESIREDIV"][TEXT]        = '1';
            $data["NAME"][TEXT]             = $row["NAME"];
            $data["NAME_KANA"][TEXT]        = $row["NAME_KANA"];
            $data["SEX"][TEXT]              = $row["SEX"];
            $data["ERACD"][TEXT]            = ($row["BIRTHDAY"]) ? $eracd : '';
            $data["BIRTH_Y"][TEXT]          = ($row["BIRTHDAY"]) ? $year : '';
            $data["BIRTH_M"][TEXT]          = ($row["BIRTHDAY"]) ? substr($row["BIRTHDAY"],5,2) : '';
            $data["BIRTH_D"][TEXT]          = ($row["BIRTHDAY"]) ? substr($row["BIRTHDAY"],8) : '';
            $data["BIRTHDAY"][TEXT]         = $row["BIRTHDAY"];
            $data["FS_CD"][TEXT]            = ($model->isCollege) ? '1392105' : '1192102';
            $data["FS_NAME"][TEXT]          = ($model->isCollege) ? '智辯学園奈良カレッジ小学部' : '智辯学園和歌山小学校';
            $data["FS_AREA_CD"][TEXT]       = ($model->isCollege) ? '36' : '11';
            $data["FS_NATPUBPRIDIV"][TEXT]  = '3';
            $data["SUC_COURSECD"][TEXT]     = ($model->isCollege) ? '1' : '5';
            $data["SUC_MAJORCD"][TEXT]      = ($model->isCollege) ? '001' : '100';
            $data["SUC_COURSECODE"][TEXT]   = ($model->isCollege) ? '0000' : '1110';
            $data["JUDGEMENT"][TEXT]        = '1';
            $data["PROCEDUREDIV"][TEXT]     = '1';
            $data["ENTDIV"][TEXT]           = '1';
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
