<?php

require_once('for_php7.php');

class knjz020cQuery extends Query {

    //受験コースマスタの存在チェック
    function entexam_course($year)
    {
        $query  = " SELECT * ";
        $query .= "   FROM entexam_course_mst ";
        $query .= "  WHERE entexamyear = '{$year}'";

        return $query;
    }

    //名称マスタの存在チェック
    function get_name_mst($year, $mname)
    {
        $query  = " SELECT * ";
        $query .= "   FROM v_name_mst ";
        $query .= "  WHERE year = '{$year}' AND ";
        $query .= "        namecd1 = '{$mname}'";

        return $query;
    }

    //年度一覧取得
    function selectYearQuery()
    {
        $query  = " SELECT entexamyear ";
        $query .= " FROM entexam_perfect_mst ";
        $query .= " GROUP BY entexamyear ";
        $query .= " ORDER BY entexamyear DESC";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(ENTEXAMYEAR) ";
        $query .= " FROM entexam_perfect_mst ";
        $query .= " WHERE ENTEXAMYEAR >= '{$model->year}'";
        return $query;
    }

    //名称マスタよりデータを取得
    function getName($year, $namecd1)
    {

        $query  = "SELECT ";
        $query .= "    NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "    NAMECD2 AS VALUE ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR    = '{$year}' AND ";
        $query .= "    NAMECD1 = '{$namecd1}' ";
        $query .= "ORDER BY ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2 ";

        return $query;
    }

    //コースコンボ設定
    function get_Course($year)
    {
        $query .= "SELECT ";
        $query .= "    T1.APPLICANTDIV || '-' || T1.TESTDIV || '-' || T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD || ':' || RTRIM(T3.NAME1) ||'　　'||RTRIM(T2.COURSENAME) || RTRIM(T2.MAJORNAME) ||' '|| RTRIM(T1.EXAMCOURSE_NAME) AS LABEL, ";
        $query .= "    T1.ENTEXAMYEAR || '-' || T1.APPLICANTDIV || '-' || T1.TESTDIV || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "    T1.COURSECD, ";
        $query .= "    T1.MAJORCD, ";
        $query .= "    T1.EXAMCOURSECD, ";
        $query .= "    RTRIM(T3.NAME1) ||'　　'||RTRIM(T2.COURSENAME) || RTRIM(T2.MAJORNAME) ||' '|| RTRIM(T1.EXAMCOURSE_NAME) AS COURSENAME ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_COURSE_MST T1 ";
        $query .= "LEFT OUTER JOIN ";
        $query .= "    V_NAME_MST T3 ";
        $query .= "ON ";
        $query .= "    T1.ENTEXAMYEAR = T3.YEAR AND ";
        $query .= "    T1.APPLICANTDIV = T3.NAMECD2 AND ";
        $query .= "    T3.NAMECD1 = 'L003' ";
        $query .= "LEFT OUTER JOIN ";
        $query .= "    V_COURSE_MAJOR_MST T2 ";
        $query .= "ON ";
        $query .= "    T1.ENTEXAMYEAR = T2.YEAR AND ";
        $query .= "    T1.COURSECD = T2.COURSECD AND ";
        $query .= "    T1.MAJORCD = T2.MAJORCD ";
        $query .= "WHERE ";
        $query .= "    T1.ENTEXAMYEAR = '{$year}' ";
        $query .= "ORDER BY ";
        $query .= "    T1.APPLICANTDIV, ";
        $query .= "    T1.TESTDIV, ";
        $query .= "    T1.COURSECD, ";
        $query .= "    T1.MAJORCD, ";
        $query .= "    T1.EXAMCOURSECD ";

        return $query;
    }

    //データ取得
    function selectQuery($model)
    {
        $query .= "SELECT ";
        $query .= "    T1.*, ";
        $query .= "    T3.NAME1 AS SCHOOL, ";
        $query .= "    T4.ABBV1 AS TESTNAME, ";
        $query .= "    T5.NAME1 AS SUBCLASSNAME, ";
        $query .= "    T6.COURSENAME, ";
        $query .= "    T6.MAJORNAME, ";
        $query .= "    T2.EXAMCOURSE_NAME ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_PERFECT_MST T1  ";
        $query .= "LEFT OUTER JOIN ENTEXAM_COURSE_MST T2  ";
        $query .= "     ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR  ";
        $query .= "    AND T1.APPLICANTDIV = T2.APPLICANTDIV  ";
        $query .= "    AND T1.TESTDIV = T2.TESTDIV  ";
        $query .= "    AND T1.COURSECD = T2.COURSECD  ";
        $query .= "    AND T1.EXAMCOURSECD = T2.EXAMCOURSECD  ";
        $query .= "    AND T1.MAJORCD = T2.MAJORCD  ";
        $query .= "LEFT OUTER JOIN V_NAME_MST T3  ";
        $query .= "     ON T1.ENTEXAMYEAR = T3.YEAR  ";
        $query .= "    AND T1.APPLICANTDIV = T3.NAMECD2  ";
        $query .= "    AND T3.NAMECD1 = 'L003'  ";
        $query .= "LEFT OUTER JOIN V_NAME_MST T4  ";
        $query .= "     ON T1.ENTEXAMYEAR = T4.YEAR  ";
        $query .= "    AND T1.TESTDIV = T4.NAMECD2  ";
        $query .= "    AND T4.NAMECD1 = 'L004'  ";
        $query .= "LEFT OUTER JOIN V_NAME_MST T5  ";
        $query .= "     ON T1.ENTEXAMYEAR = T5.YEAR  ";
        $query .= "    AND INT(T1.TESTSUBCLASSCD) = INT(T5.NAMECD2)  ";
        $query .= "    AND T5.NAMECD1 = 'L009'  ";
        $query .= "LEFT OUTER JOIN V_COURSE_MAJOR_MST T6  ";
        $query .= "     ON T1.ENTEXAMYEAR = T6.YEAR  ";
        $query .= "    AND T1.COURSECD = T6.COURSECD  ";
        $query .= "    AND T1.MAJORCD = T6.MAJORCD ";
        $query .= "WHERE ";
        $query .= "    T1.ENTEXAMYEAR || T1.APPLICANTDIV || T1.TESTDIV || T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD = '" .str_replace("-", "", $model->totalcd). "' ";
        $query .= "ORDER BY ";
        $query .= "    T1.APPLICANTDIV, ";
        $query .= "    T1.TESTDIV, ";
        $query .= "    T1.TESTSUBCLASSCD, ";
        $query .= "    T1.COURSECD, ";
        $query .= "    T1.MAJORCD, ";
        $query .= "    T1.EXAMCOURSECD ";

        return $query;
    }

    //課程学科
    function selectTotalcd($year)
    {
        $query  = "SELECT ";
        $query .= "    COURSECD || MAJORCD || ':' || COURSENAME || MAJORNAME AS LABEL, ";
        $query .= "    COURSECD || MAJORCD AS VALUE ";
        $query .= "FROM ";
        $query .= "    V_COURSE_MAJOR_MST  ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' ";
        $query .= "ORDER BY ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD ";

        return $query;
    }

    //最初に開く時の1レコード取得
    function getDefault($year) {
        $query  = "SELECT ";
        $query .= "    APPLICANTDIV, ";
        $query .= "    APPLICANTDIV, ";
        $query .= "    TESTDIV, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    EXAMCOURSECD ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_COURSE_MST  ";
        $query .= "WHERE ";
        $query .= "    ENTEXAMYEAR = '{$year}' ";
        $query .= "ORDER BY ";
        $query .= "    APPLICANTDIV, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    EXAMCOURSECD ";
        $query .= "FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //１レコード取得
    function getRow($db,$model)
    {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     entexam_perfect_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '{$model->field["ENTEXAMYEAR"]}' AND";
        $query .= "         testdiv  = '{$model->field["TESTDIV"]}' AND";
        $query .= "  testsubclasscd  = '{$model->field["TESTSUBCLASSCD"]}' AND";
        $query .= "    applicantdiv  = '{$model->field["APPLICANTDIV"]}' AND";
        $query .= "        coursecd  = '{$model->field["COURSECD"]}' AND ";
        $query .= "         majorcd  = '{$model->field["MAJORCD"]}' AND ";
        $query .= "    examcoursecd  = '{$model->field["EXAMCOURSECD"]}'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //ENTEXAM_SCORE_DATのMAX(score)
    function ChScoreDat($model)
    {
        $db = Query::dbCheckOut();

        //更新時は最高得点を取得
        if($model->cmd == "update"){
            $query  = " SELECT MAX(T1.score) ";

        //削除時はデータ存在を確認
        }elseif($model->cmd == "delete"){
            $query  = " SELECT COUNT(*) ";
        }

        $query .= " FROM ";
        $query .= "     entexam_score_dat T1, ";
        $query .= "     entexam_recept_dat T2, ";
        $query .= "     entexam_applicantbase_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear    = '{$model->field["ENTEXAMYEAR"]}' AND ";
        $query .= "     T1.applicantdiv   = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     T1.testdiv        = '{$model->field["TESTDIV"]}' AND ";
        $query .= "     T1.testsubclasscd = '{$model->field["TESTSUBCLASSCD"]}' AND ";
        $query .= "     T2.entexamyear    = T1.entexamyear AND ";
        $query .= "     T2.applicantdiv   = T1.applicantdiv AND ";
        $query .= "     T2.testdiv        = T1.testdiv AND ";
        $query .= "     T2.receptno       = T1.receptno AND ";
        $query .= "     T3.entexamyear    = T2.entexamyear AND ";
        $query .= "     T3.applicantdiv   = T2.applicantdiv AND ";
        $query .= "     T3.testdiv        = T2.testdiv AND ";
        $query .= "     T3.examno         = T2.examno ";

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return $row;
    }

    //重複チェック
    function CheckData($model)
    {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     entexam_perfect_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '{$model->field["ENTEXAMYEAR"]}' AND";
        $query .= "         testdiv  = '{$model->field["TESTDIV"]}' AND";
        $query .= "  testsubclasscd  = '{$model->field["TESTSUBCLASSCD"]}' AND";
        $query .= "    applicantdiv  = '{$model->field["APPLICANTDIV"]}' AND";
        $query .= "        coursecd  = '{$model->field["COURSECD"]}' AND ";
        $query .= "         majorcd  = '{$model->field["MAJORCD"]}' AND ";
        $query .= "    examcoursecd  = '{$model->field["EXAMCOURSECD"]}'";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $model->field["ENTEXAMYEAR"];
        $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["COURSECD"][TEXT]         = $model->field["COURSECD"];
        $data["MAJORCD"][TEXT]          = $model->field["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $model->field["EXAMCOURSECD"];
        $data["TESTSUBCLASSCD"][TEXT]   = $model->field["TESTSUBCLASSCD"];
        $data["PERFECT"][NUMBER]        = $model->field["PERFECT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PERFECT_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM entexam_perfect_mst ";
        $query .= "       WHERE entexamyear     = '{$model->field["ENTEXAMYEAR"]}'";
        $query .= "         AND testdiv         = '{$model->field["TESTDIV"]}'";
        $query .= "         AND applicantdiv    = '{$model->field["APPLICANTDIV"]}'";
        $query .= "         AND coursecd        = '{$model->field["COURSECD"]}'";
        $query .= "         AND majorcd         = '{$model->field["MAJORCD"]}'";
        $query .= "         AND examcoursecd    = '{$model->field["EXAMCOURSECD"]}'";
        $query .= "         AND testsubclasscd  = '{$model->field["TESTSUBCLASSCD"]}'";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    function getCheckPerfectMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    function getCheckNextYearCourseMstSql($year) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //次年度のデータを作成
    function &getCopyQuery($thisYear, $nextYear)
    {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     L1.APPLICANTDIV, ";
        $query .= "     L1.TESTDIV, ";
        $query .= "     L1.COURSECD, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     L1.EXAMCOURSECD, ";
        $query .= "     L1.TESTSUBCLASSCD, ";
        $query .= "     L1.PERFECT, ";
        $query .= "     '" . STAFFCD . "', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " INNER JOIN ENTEXAM_PERFECT_MST L1 ON L1.ENTEXAMYEAR  = '{$thisYear}' ";
        $query .= "                                 AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                 AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "                                 AND L1.COURSECD     = T1.COURSECD ";
        $query .= "                                 AND L1.MAJORCD      = T1.MAJORCD ";
        $query .= "                                 AND L1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE T1.ENTEXAMYEAR = '{$nextYear}' ";

        return $query;
     }
}
?>
