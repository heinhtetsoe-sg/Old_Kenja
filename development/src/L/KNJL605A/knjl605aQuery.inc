<?php

require_once('for_php7.php');
class knjl605aQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd) {
        $query = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     NAMECD1 = '{$namecd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ENTEXAM_SPECIAL_SCHOLARSHIP_MST ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //指定年度の入試コースが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model) {
        $query  = " SELECT MIN(ENTEXAMYEAR) ";
        $query .= " FROM ENTEXAM_COURSE_MST ";
        $query .= " WHERE ENTEXAMYEAR >= '{$model->year}' ";

        return $query;
    }

    //データ取得
    function selectQuery($examYear) {

        $query = "";

        $query .= " SELECT ";
        $query .= "   T1.ENTEXAMYEAR ";
        $query .= "   , T1.APPLICANTDIV ";
        $query .= "   , L003.NAME1 APPLICANTDIV_NAME ";
        $query .= "   , T1.TESTDIV ";
        $query .= "   , CASE WHEN T1.APPLICANTDIV = '1' THEN L024.NAME1 ELSE L004.NAME1 END TESTDIV_NAME ";
        $query .= "   , T1.SP_SCHOLAR_CD ";
        $query .= "   , T1.SP_SCHOLAR_NAME ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_SPECIAL_SCHOLARSHIP_MST T1 ";
        $query .= " LEFT JOIN V_NAME_MST L003 ";
        $query .= "    ON T1.ENTEXAMYEAR = L003.YEAR ";
        $query .= "   AND T1.APPLICANTDIV = L003.NAMECD2 ";
        $query .= "   AND L003.NAMECD1 = 'L003' ";
        $query .= " LEFT JOIN V_NAME_MST L004 ";
        $query .= "    ON T1.ENTEXAMYEAR = L004.YEAR ";
        $query .= "   AND T1.TESTDIV = L004.NAMECD2 ";
        $query .= "   AND L004.NAMECD1 = 'L004' ";
        $query .= " LEFT JOIN V_NAME_MST L024 ";
        $query .= "    ON T1.ENTEXAMYEAR = L024.YEAR ";
        $query .= "   AND T1.TESTDIV = L024.NAMECD2 ";
        $query .= "   AND L024.NAMECD1 = 'L024' ";

        $query .= " WHERE T1.ENTEXAMYEAR = '{$examYear}' ";

        $query .= " ORDER BY ";
        $query .= "   T1.ENTEXAMYEAR ";
        $query .= "   , T1.APPLICANTDIV ";
        $query .= "   , T1.TESTDIV ";
        $query .= "   , T1.SP_SCHOLAR_CD ";
        
        return $query;
    }

    function selectCourseQuery($examYear, $appDiv, $testDiv, $scholarCd) {

        $query = "";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR ";
        $query .= "   , T1.APPLICANTDIV ";
        $query .= "   , T1.TESTDIV ";
        $query .= "   , T1.SP_SCHOLAR_CD ";
        $query .= "   , T1.COURSECD ";
        $query .= "   , T1.MAJORCD ";
        $query .= "   , T1.EXAMCOURSECD ";
        $query .= "   , COURSE.EXAMCOURSE_NAME ";
        $query .= "   , T1.COURSECD || T1.MAJORCD || '-' || T1.EXAMCOURSECD VALUE ";
        $query .= "   , T1.EXAMCOURSECD || ':' || COURSE.EXAMCOURSE_NAME LABEL ";

        $query .= " FROM ";
        $query .= "   ENTEXAM_SPECIAL_SCHOLARSHIP_DAT T1 ";
        $query .= " INNER JOIN ENTEXAM_COURSE_MST COURSE ";
        $query .= "    ON T1.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ";
        $query .= "   AND T1.APPLICANTDIV = COURSE.APPLICANTDIV ";
        $query .= "   AND T1.TESTDIV = COURSE.TESTDIV ";
        $query .= "   AND T1.COURSECD = COURSE.COURSECD ";
        $query .= "   AND T1.MAJORCD = COURSE.MAJORCD ";
        $query .= "   AND T1.EXAMCOURSECD = COURSE.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$examYear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$appDiv}' ";
        $query .= "   AND T1.TESTDIV = '{$testDiv}' ";
        $query .= "   AND T1.SP_SCHOLAR_CD = '{$scholarCd}' ";
        $query .= " ORDER BY ";
        $query .= "   T1.ENTEXAMYEAR ";
        $query .= "   , T1.APPLICANTDIV ";
        $query .= "   , T1.TESTDIV ";
        $query .= "   , T1.SP_SCHOLAR_CD ";
        $query .= "   , COURSE.COURSECD ";

        return $query;
    }

    function selectEntexamCourseQuery($examYear, $appDiv, $testDiv, $scholarCd) {

        $query = "";
        $query .= " SELECT ";
        $query .= "   T1.COURSECD || T1.MAJORCD || '-' || T1.EXAMCOURSECD VALUE ";
        $query .= "   , T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME LABEL ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR = '{$examYear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$appDiv}' ";
        $query .= "   AND T1.TESTDIV = '{$testDiv}' ";

        $query .= "   AND (T1.COURSECD, T1.MAJORCD, T1.EXAMCOURSECD) NOT IN ( ";

        $query .= "     SELECT ";
        $query .= "       COURSECD, MAJORCD, EXAMCOURSECD ";
        $query .= "     FROM ";
        $query .= "       ENTEXAM_SPECIAL_SCHOLARSHIP_DAT ";
        $query .= "     WHERE ";
        $query .= "           ENTEXAMYEAR = '{$examYear}' ";
        $query .= "       AND APPLICANTDIV = '{$appDiv}' ";
        $query .= "       AND TESTDIV = '{$testDiv}' ";
        $query .= "       AND SP_SCHOLAR_CD = '{$scholarCd}' ";
        $query .= "   ) ";

        $query .= " ORDER BY ";
        $query .= "   T1.COURSECD ";
        $query .= "   , T1.MAJORCD ";
        $query .= "   , T1.EXAMCOURSECD ";

        return $query;
    }

    //１レコード取得
    function getRow($examYear, $applicantDiv, $testDiv, $scholarCd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPECIAL_SCHOLARSHIP_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$examYear}' ";
        $query .= " AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " AND TESTDIV      = '{$testDiv}' ";
        $query .= " AND SP_SCHOLAR_CD     = '{$scholarCd}' ";

        return $query;
    }


    //INSERT(特別奨学生マスタ)
    function &getInsertSpScholarshipMstQuery($fields) {

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["SP_SCHOLAR_CD"][TEXT]    = $fields["SP_SCHOLAR_CD"];
        $data["SP_SCHOLAR_NAME"][TEXT]  = $fields["SP_SCHOLAR_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_SPECIAL_SCHOLARSHIP_MST");

        return $query;
    }
    //INSERT(特別奨学生コースデータ)
    function &getInsertSpScholarshipDatQuery($fields, $course) {
        // コースCDへ分割
        list($temp, $examCourseCd) = explode('-', $course);
        $courseCd = substr($temp, 0, 1);
        $majorCd = substr($temp, 1);

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["SP_SCHOLAR_CD"][TEXT]    = $fields["SP_SCHOLAR_CD"];
        $data["COURSECD"][TEXT]         = $courseCd;
        $data["MAJORCD"][TEXT]          = $majorCd;
        $data["EXAMCOURSECD"][TEXT]     = $examCourseCd;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_SPECIAL_SCHOLARSHIP_DAT");

        return $query;
    }

    //UPDATE(特別奨学生マスタ)
    function &getUpdateSpScholarshipMstQuery($fields) {

        $data["SP_SCHOLAR_NAME"][TEXT]  = $fields["SP_SCHOLAR_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        
        $where  = " WHERE ENTEXAMYEAR   = '{$fields["ENTEXAMYEAR"]}'";
        $where .= "   AND APPLICANTDIV  = '{$fields["APPLICANTDIV"]}'";
        $where .= "   AND TESTDIV       = '{$fields["TESTDIV"]}'";
        $where .= "   AND SP_SCHOLAR_CD = '{$fields["SP_SCHOLAR_CD"]}'";

        $query = Query::updateSQL($data, "ENTEXAM_SPECIAL_SCHOLARSHIP_MST", $where);

        return $query;
    }

    //DELETE(特別奨学生マスタ)
    function &getDeleteSpScholarshipMstQuery($fields) {

        $query  = " DELETE FROM ENTEXAM_SPECIAL_SCHOLARSHIP_MST ";
        $query .= "       WHERE ENTEXAMYEAR   = '{$fields["ENTEXAMYEAR"] }'";
        $query .= "         AND APPLICANTDIV  = '{$fields["APPLICANTDIV"]}'";
        $query .= "         AND TESTDIV       = '{$fields["TESTDIV"]}'";
        $query .= "         AND SP_SCHOLAR_CD = '{$fields["SP_SCHOLAR_CD"]}'";

        return $query;
    }
    //DELETE(特別奨学生コースデータ)
    function &getDeleteSpScholarshipDatQuery($fields) {

        $query  = " DELETE FROM ENTEXAM_SPECIAL_SCHOLARSHIP_DAT ";
        $query .= "       WHERE ENTEXAMYEAR   = '{$fields["ENTEXAMYEAR"] }'";
        $query .= "         AND APPLICANTDIV  = '{$fields["APPLICANTDIV"]}'";
        $query .= "         AND TESTDIV       = '{$fields["TESTDIV"]}'";
        $query .= "         AND SP_SCHOLAR_CD = '{$fields["SP_SCHOLAR_CD"]}'";

        return $query;
    }

    //ある年度にデータが存在するのかチェック
    function getCheckSpScholarshipMstSql($year) {
        $query  = " SELECT COUNT(*) ";
        $query .= " FROM ENTEXAM_SPECIAL_SCHOLARSHIP_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //次年度の対象コースチェック
    function getCheckCourseSql($year, $nextYear) {

        $query  = "";
        $query .= " WITH SCHOLARSHIP AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.ENTEXAMYEAR ";
        $query .= "     , T1.APPLICANTDIV ";
        $query .= "     , T1.TESTDIV ";
        $query .= "     , T2.COURSECD ";
        $query .= "     , T2.MAJORCD ";
        $query .= "     , T2.EXAMCOURSECD ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_SPECIAL_SCHOLARSHIP_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_SPECIAL_SCHOLARSHIP_DAT T2 ";
        $query .= "       ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "       AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "       AND T1.TESTDIV = T2.TESTDIV ";
        $query .= "       AND T1.SP_SCHOLAR_CD = T2.SP_SCHOLAR_CD ";
        $query .= "   WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$year}' ";
        $query .= " ) ";
        $query .= " , COURSE AS ( ";
        $query .= "   SELECT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= "     , APPLICANTDIV ";
        $query .= "     , TESTDIV ";
        $query .= "     , COURSECD ";
        $query .= "     , MAJORCD ";
        $query .= "     , EXAMCOURSECD ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= "   WHERE ";
        $query .= "     ENTEXAMYEAR = '{$nextYear}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   SCHOLARSHIP ";
        $query .= "   LEFT JOIN COURSE ";
        $query .= "     ON SCHOLARSHIP.APPLICANTDIV = COURSE.APPLICANTDIV ";
        $query .= "     AND SCHOLARSHIP.TESTDIV = COURSE.TESTDIV ";
        $query .= "     AND SCHOLARSHIP.COURSECD = COURSE.COURSECD ";
        $query .= "     AND SCHOLARSHIP.MAJORCD = COURSE.MAJORCD ";
        $query .= "     AND SCHOLARSHIP.EXAMCOURSECD = COURSE.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "   COURSE.ENTEXAMYEAR IS NULL ";

        return $query;
    }

    //次年度作成(特別奨学生マスタ)
    function getCopyQuery($thisYear, $nextYear) {

        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_SPECIAL_SCHOLARSHIP_MST ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' ";
        $query .= "   , APPLICANTDIV ";
        $query .= "   , TESTDIV ";
        $query .= "   , SP_SCHOLAR_CD ";
        $query .= "   , SP_SCHOLAR_NAME ";

        $query .= "   , '". STAFFCD ."' ";
        $query .= "   , sysdate() ";
        $query .= " FROM ENTEXAM_SPECIAL_SCHOLARSHIP_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }

    //次年度作成(特別奨学生コースデータ)
    function getCopySubQuery($thisYear, $nextYear) {

        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_SPECIAL_SCHOLARSHIP_DAT ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' ";
        $query .= "   , APPLICANTDIV ";
        $query .= "   , TESTDIV ";
        $query .= "   , SP_SCHOLAR_CD ";
        $query .= "   , COURSECD ";
        $query .= "   , MAJORCD ";
        $query .= "   , EXAMCOURSECD ";

        $query .= "   , '". STAFFCD ."' ";
        $query .= "   , sysdate() ";
        $query .= " FROM ENTEXAM_SPECIAL_SCHOLARSHIP_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }

}
?>
