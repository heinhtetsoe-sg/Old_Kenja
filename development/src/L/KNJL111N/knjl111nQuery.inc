<?php

require_once('for_php7.php');

class knjl111nQuery extends Query {

    //L003
    function getApplicantDiv($model) {
        $query  = " SELECT ";
        $query .= "      NAMESPARE2, ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '{$model->entexamYear}' ";
        $query .= "      AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //L004
    function getTestDiv($model) {
        $query  = " SELECT ";
        $query .= "      NAMESPARE2, ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '{$model->entexamYear}' ";
        $query .= "      AND NAMECD1 = 'L004' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //L009
    function getSubclassCd($model) {
        $query  = " SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '{$model->entexamYear}' ";
        $query .= "      AND NAMECD1 = 'L009' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //L006
    function getShDiv($model) {
        $query  = " SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '{$model->entexamYear}' ";
        $query .= "      AND NAMECD1 = 'L006' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //コース
    function getCourse($model) {
        $query  = " SELECT ";
        $query .= "      COURSECD || ':' || MAJORCD || ':' || EXAMCOURSECD AS VALUE, ";
        $query .= "      EXAMCOURSE_ABBV AS LABEL ";
        $query .= " FROM ";
        $query .= "      ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "      ENTEXAMYEAR = '{$model->entexamYear}' ";
        $query .= "      AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "      AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //平均点
    function getAvg($model, $shDiv, $courseCd, $subclassCd) {
        $query  = " SELECT ";
        $query .= "      AVERAGE_TOTAL ";
        $query .= " FROM ";
        $query .= "      ENTEXAM_APPLICANT_AVERAGE_DAT ";
        $query .= " WHERE ";
        $query .= "      ENTEXAMYEAR = '{$model->entexamYear}' ";
        $query .= "      AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "      AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "      AND EXAM_TYPE = '1' ";
        $query .= "      AND SHDIV = '{$shDiv}' ";
        $query .= "      AND COURSECD || ':' || MAJORCD || ':' || EXAMCOURSECD = '{$courseCd}' ";
        $query .= "      AND TESTSUBCLASSCD = '{$subclassCd}' ";

        return $query;
    }

    //平均点再計算
    function getRecalc($model, $shDiv, $courseCd, $subclassCd) {
        $query .= " SELECT ";
        $query .= "      ROUND(AVG(DECIMAL(I3.SCORE)),1) AS SETAVG ";
        $query .= " FROM ";
        $query .= "      ENTEXAM_RECEPT_DAT T1 ";
        $query .= "      INNER JOIN ENTEXAM_APPLICANTBASE_DAT I1 ON T1.ENTEXAMYEAR = I1.ENTEXAMYEAR ";
        $query .= "            AND T1.APPLICANTDIV = I1.APPLICANTDIV ";
        $query .= "            AND T1.EXAMNO = I1.EXAMNO ";
        $query .= "            AND I1.SHDIV = '{$shDiv}' ";
        $query .= "            AND T1.ENTEXAMYEAR = I1.ENTEXAMYEAR ";
        $query .= "      INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT I2 ON T1.ENTEXAMYEAR = I2.ENTEXAMYEAR ";
        $query .= "            AND T1.EXAMNO = I2.EXAMNO ";
        $query .= "            AND I2.SEQ = '001' ";
        $query .= "            AND I2.REMARK8 || ':' || I2.REMARK9 || ':' || I2.REMARK10 = '{$courseCd}' ";
        $query .= "      INNER JOIN ENTEXAM_SCORE_DAT I3 ON T1.ENTEXAMYEAR = I3.ENTEXAMYEAR ";
        $query .= "            AND T1.APPLICANTDIV = I3.APPLICANTDIV ";
        $query .= "            AND T1.TESTDIV = I3.TESTDIV ";
        $query .= "            AND T1.EXAM_TYPE = I3.EXAM_TYPE ";
        $query .= "            AND T1.RECEPTNO = I3.RECEPTNO ";
        $query .= "            AND I3.TESTSUBCLASSCD = '{$subclassCd}' ";
        $query .= "            AND I3.ATTEND_FLG = '1' ";
        $query .= " WHERE ";
        $query .= "      T1.ENTEXAMYEAR = '{$model->entexamYear}' ";
        $query .= "      AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "      AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "      AND T1.EXAM_TYPE = '1' ";
        $query .= "      AND (T1.JUDGEDIV IS NULL OR T1.JUDGEDIV IN('1', '2', '3')) ";

        return $query;
    }

    //UPDATE
    function &getDeleteQuery($db, $model) {
        //DELETE
        $query  = "DELETE FROM ENTEXAM_APPLICANT_AVERAGE_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '{$model->entexamYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "   AND EXAM_TYPE = '1' ";
        $db->query($query);
    }

    //UPDATE
    function &getUpdateQuery($db, $model) {

        foreach ($model->setFields as $soeji => $setVal) {
            list($shDiv, $courseCd, $subclassCd) = preg_split("/_/", $soeji);
            list($course, $major, $examCourse) = preg_split("/:/", $courseCd);

            if (strlen($setVal)) {
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->entexamYear;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]        = "1";
                $data["SHDIV"][TEXT]            = $shDiv;
                $data["COURSECD"][TEXT]         = $course;
                $data["MAJORCD"][TEXT]          = $major;
                $data["EXAMCOURSECD"][TEXT]     = $examCourse;
                $data["TESTSUBCLASSCD"][TEXT]   = $subclassCd;
                $data["AVERAGE_TOTAL"][NUMBER]  = $setVal;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANT_AVERAGE_DAT");
                $db->query($query);
            }
        }
        return;
    }

}
?>
