<?php

require_once('for_php7.php');

class knjl531aQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1";
        } else {
            $query .= "     NAMECD2 AS VALUE,";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //志望区分の取得
    function getHopeCourse($year) {
        $query  = " SELECT ";
        $query .= "   T1.HOPE_COURSECODE AS VALUE, ";
        $query .= "   T1.HOPE_COURSECODE || ':' || T1.HOPE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST T1 ";
        $query .= "   INNER JOIN ENTEXAM_HOPE_COURSE_YDAT T2 ";
        $query .= "           ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= "          AND T2.ENTEXAMYEAR     = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分の取得(CSVタイトル用)
    function getHopeCourseName($year, $desirediv) {
        $query  = " SELECT ";
        $query .= "   T1.HOPE_NAME ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST T1 ";
        $query .= "   INNER JOIN ENTEXAM_HOPE_COURSE_YDAT T2 ";
        $query .= "           ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= "          AND T2.ENTEXAMYEAR     = '".$year."' ";
        $query .= " WHERE ";
        $query .= "     T1.HOPE_COURSECODE = '".$desirediv."' ";

        return $query;
    }

    //一覧表示
    function getCsvQuery($model) {
        $query  = " WITH HOPE_COURSE AS( ";
        $query .= " SELECT ";
        $query .= "   T1.HOPE_COURSECODE, ";
        $query .= "   T1.HOPE_NAME ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST T1 ";
        $query .= "   INNER JOIN ENTEXAM_HOPE_COURSE_YDAT T2 ";
        $query .= "           ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= "          AND T2.ENTEXAMYEAR     = '".$model->ObjYear."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.FS_CD, ";
        $query .= "     F1.FINSCHOOL_NAME AS FINSCHOOL_NAME, ";
        $query .= "     D1.REMARK8 AS GOKAKU, ";
        $query .= "     B1.TESTDIV, ";
        $query .= "     L004.NAME1 AS TESTDIV_NAME, ";
        $query .= "     B1.DESIREDIV, ";
        $query .= "     HOPE.HOPE_NAME AS DESIREDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D1 ";
        $query .= "             ON D1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "            AND D1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "            AND D1.EXAMNO       = B1.EXAMNO ";
        $query .= "            AND D1.SEQ          = '004' ";
        $query .= "            AND D1.REMARK8      = '1' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D2 ";
        $query .= "             ON D2.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "            AND D2.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "            AND D2.EXAMNO       = B1.EXAMNO ";
        $query .= "            AND D2.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL_MST S1 ";
        $query .= "             ON S1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "            AND S1.ENTEXAM_SCHOOLCD = B1.FS_CD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ";
        $query .= "            ON F1.FINSCHOOLCD  = S1.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST L004 ";
        $query .= "            ON L004.NAMECD2 = B1.TESTDIV ";
        $query .= "           AND L004.NAMECD1 = 'L004' ";
        $query .= "     LEFT JOIN HOPE_COURSE HOPE ";
        $query .= "            ON HOPE.HOPE_COURSECODE = D2.REMARK10 ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND B1.TESTDIV      = '".$model->testdiv."' ";
        if (strlen($model->desirediv)) {
            $query .= "     AND D2.REMARK10 = '".$model->desirediv."' ";
        }
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= "     B1.NAME, ";
        }
        $query .= "     B1.EXAMNO ";

        return $query;
    }
}
?>