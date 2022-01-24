<?php

require_once('for_php7.php');

class knjl140sQuery extends Query {
    //最終学期を取得
    function getSemesterLast() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";

        $db = Query::dbCheckOut();
        $rtnVal = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnVal;
    }
    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //合格コースコンボ
    function getGoukaku($model) {
        $query  = " SELECT distinct ";
        $query .= "     T1.SUC_COURSECD   ||  ";
        $query .= "     T1.SUC_MAJORCD    || ";
        $query .= "     ':'               || ";
        $query .= "     L3.COURSENAME     || ";
        $query .= "     case when L5.MAJORSCD = '0' then L4.MAJORLNAME else L5.MAJORSNAME end AS LABEL, ";
        $query .= "     T1.SUC_COURSECD   || ':' || ";
        $query .= "     T1.SUC_MAJORCD    || ':' || ";
        $query .= "     T1.SUC_COURSECODE || ':' || ";
        $query .= "     T1.TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                 AND L1.NAMECD1 = 'L013' ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L3 ON L3.COURSECD = T1.SUC_COURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_MAJORL_MST L4 ON  L4.MAJORLCD = substr(T1.SUC_MAJORCD,1,2) ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_MAJORS_MST L5 ON  L5.MAJORLCD = substr(T1.SUC_MAJORCD,1,2) ";
        $query .= "                           AND L5.MAJORSCD = substr(T1.SUC_MAJORCD,3,1) ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= " AND T1.PROCEDUREDIV = '1' ";
        $query .= " AND T1.ENTDIV = '1' ";
        $query .= " AND L1.NAMESPARE1 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入学者のCSV
    function selectCvsStudentQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO AS SCHREGNO, ";
        $query .= "     '' AS ENTDIV, ";
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        $query .= "     '0' AS INOUTCD, ";
        $query .= "     N5.MAIN_COURSECD AS COURSECD, ";
        $query .= "     N5.MAIN_MAJORCD AS MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE AS COURSECODE, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     CAST(REPLACE(CAST(T1.BIRTHDAY AS CHAR(10)), '-', '/') AS CHAR(10)) AS BIRTHDAY, ";
        $query .= "     T1.SEX, "; //コードで出力する
        $query .= "     T1.FS_CD, ";
        $query .= "     CASE WHEN VALUE(T1.FS_GRDYEAR, '') != '' ";
        $query .= "          THEN FS_GRDYEAR || '/3/31' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS FINSCHOOLGRADDATE, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDRESS1, ";
        $query .= "     L1.ADDRESS2, ";
        $query .= "     L1.TELNO, ";
        $query .= "     '' AS FAXNO, ";
        $query .= "     '' AS EMAIL, ";
        $query .= "     L1.EMERGENCYCALL, ";
        $query .= "     L1.EMERGENCYTELNO, ";
        $query .= "     '' AS SCALASHIPDIV, ";
        $query .= "     '' AS KATEI_ENT_YEAR, ";
        $query .= "     '' AS TIKU_CD, ";
        $query .= "     '' AS EXAMNO, ";
        $query .= "     '' AS MUSHOU_KAISUU, ";
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L1.EXAMNO = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT "; //合格
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_MAJORS_MST N5 ON  N5.MAJORLCD = substr(T1.SUC_MAJORCD,1,2) ";
        $query .= "                           AND N5.MAJORSCD = substr(T1.SUC_MAJORCD,3,1) ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";
        $query .= "   AND T1.SUC_COURSECD = '{$model->suc_coursecd}' ";
        $query .= "   AND T1.SUC_MAJORCD  = '{$model->suc_majorcd}' ";

        if ($model->suc_coursecode != 'DUMMY') {
            $query .= "   AND T1.SUC_COURSECODE = '{$model->suc_coursecode}' ";
        }

        $query .= " ORDER BY ";
        $query .= $model->selected_data;

        return $query;
    }

    //保護者のCSV
    function selectCvsGuardQuery($model) {
        $query  = " SELECT ";
        $query .= "     '' AS GRADE, ";
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        $query .= "     '' AS NAME, ";
        $query .= "     T1.EXAMNO AS SCHREGNO, ";
        $query .= "     '01' AS RELATIONSHIP, ";
        $query .= "     L1.GNAME, ";
        $query .= "     L1.GKANA, ";
        $query .= "     '' AS SEITO_SEX, ";
        $query .= "     '' AS BIRTHDAY, ";
        $query .= "     L1.GZIPCD, ";
        $query .= "     L1.GADDRESS1, ";
        $query .= "     L1.GADDRESS2, ";
        $query .= "     L1.GTELNO, ";
        $query .= "     L1.GFAXNO, ";
        $query .= "     '' AS EMAIL, ";
        $query .= "     '' AS GUARD_JOBCD, ";
        $query .= "     '' AS GUARD_WORK_NAME, ";
        $query .= "     '' AS GUARD_WORK_TELNO, ";
        $query .= "     '01' AS GUARANTOR_RELATIONSHIP, ";
        $query .= "     L1.GNAME AS GUARANTOR_NAME, ";
        $query .= "     L1.GKANA AS GUARANTOR_KANA, ";
        $query .= "     '' AS GUARANTOR_SEX, ";
        $query .= "     L1.GZIPCD AS GUARANTOR_ZIPCD, ";
        $query .= "     L1.GADDRESS1 AS GUARANTOR_ADDR1, ";
        $query .= "     L1.GADDRESS2 AS GUARANTOR_ADDR2, ";
        $query .= "     L1.GTELNO AS GUARANTOR_TELNO, ";
        $query .= "     '' AS GUARANTOR_JOBCD, ";
        $query .= "     '' AS PUBLIC_OFFICE, ";
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L1.EXAMNO = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT "; //合格
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";
        $query .= "   AND T1.SUC_COURSECD = '{$model->suc_coursecd}' ";
        $query .= "   AND T1.SUC_MAJORCD  = '{$model->suc_majorcd}' ";

        if ($model->suc_coursecode != 'DUMMY') {
            $query .= "   AND T1.SUC_COURSECODE = '{$model->suc_coursecode}' ";
        }

        $query .= " ORDER BY ";
        $query .= $model->selected_data;

        return $query;
    }
}
?>
