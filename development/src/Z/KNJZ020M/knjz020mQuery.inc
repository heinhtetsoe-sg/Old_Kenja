<?php

require_once('for_php7.php');

class knjz020mQuery extends Query {
    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L009' ";
        return $query;
    }

    //科目の満点を取ってくる
    function getPerfect($row, $model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     TESTSUBCLASSCD = '{$row["NAMECD2"]}' ";

        return $query;
    }


    //科目の答案用紙ごとの満点を取ってくる
    function getDetailPerfect($row, $model, $i) {
        $query .= " SELECT ";
        $query .= "     PERFECT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     TESTSUBCLASSCD = '{$row["NAMECD2"]}' AND ";
        $query .= "     TESTPAPERCD = '{$i}' ";

        return $query;
    }

    //コースマスタから1レコード取得(更新用)
    function getEntexamCourseMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    //ENTEXAM_PERFECT_MST を更新
    function &getInsertMstQuery($entexamCourseMst, $subclass_cd, $perfect, $rate, $db) {

        $data["ENTEXAMYEAR"][TEXT]    = $entexamCourseMst["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]   = $entexamCourseMst["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]        = $entexamCourseMst["TESTDIV"];
        $data["COURSECD"][TEXT]       = $entexamCourseMst["COURSECD"];
        $data["MAJORCD"][TEXT]        = $entexamCourseMst["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]   = $entexamCourseMst["EXAMCOURSECD"];
        $data["TESTSUBCLASSCD"][TEXT] = $subclass_cd;
        $data["PERFECT"][NUMBER]      = $perfect;
        $data["RATE"][NUMBER]         = $rate;
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PERFECT_MST");
        $db->query($query);
        return;
    }

    //ENTEXAM_PERFECT_DETAIL_MST を更新
    function &getInsertDetailQuery($entexamCourseMst, $subclass_cd, $testpapercd, $detail_perfect, $db) {

        $data["ENTEXAMYEAR"][TEXT]    = $entexamCourseMst["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]   = $entexamCourseMst["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]        = $entexamCourseMst["TESTDIV"];
        $data["COURSECD"][TEXT]       = $entexamCourseMst["COURSECD"];
        $data["MAJORCD"][TEXT]        = $entexamCourseMst["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]   = $entexamCourseMst["EXAMCOURSECD"];
        $data["TESTSUBCLASSCD"][TEXT] = $subclass_cd;
        $data["TESTPAPERCD"][TEXT]    = $testpapercd;
        $data["PERFECT"][NUMBER]      = $detail_perfect;
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PERFECT_DETAIL_MST");
        $db->query($query);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model, $db) {
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_PERFECT_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     APPLICANTDIV = '1' AND ";
        $query .= "     TESTDIV = '1' ";

        $db->query($query);


        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     APPLICANTDIV = '1' AND ";
        $query .= "     TESTDIV = '1' ";

        $db->query($query);
    }
}
?>
