<?php

require_once('for_php7.php');

class knjz020oQuery extends Query {

    //受験コースマスタの存在チェック
    function entexam_course($year)
    {
        $query  = " SELECT * ";
        $query .= "   FROM entexam_course_mst ";
        $query .= "  WHERE entexamyear = '".$year."'";
        
        return $query; 
    }

    //入試区分マスタの存在チェック
    function getTestdivMst($year)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     VALUE ";

        return $query; 
    }

    //名称マスタの存在チェック
    function get_name_mst($year, $mname)
    {
        $query  = " SELECT * ";
        $query .= "   FROM v_name_mst ";
        $query .= "  WHERE year = '".$year."' AND ";
        $query .= "        namecd1 = '".$mname."'";
        
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
        $query .= " WHERE ENTEXAMYEAR >= '" .$model->year. "'";
        return $query;
    }

    //名称マスタよりデータを取得
    function GetName($year, $namecd1)
    {
        $query = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year ."' AND ";
        $query .= "     NAMECD1 IN ('".implode($namecd1, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //コースコンボ設定
    function get_Course($year)
    {
        $query  = " SELECT ";
        $query .= "     T1.coursecd, T1.majorcd, T1.examcoursecd, ";
        $query .= "     rtrim(T2.coursename) || rtrim(T2.majorname) ||' '|| rtrim(T1.examcourse_name) as coursename ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_course_major_mst T2 ";
        $query .= " ON ";
        $query .= "     T1.entexamyear = T2.year AND ";
        $query .= "     T1.coursecd = T2.coursecd AND ";
        $query .= "     T1.majorcd = T2.majorcd ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear = '".$year."'";
        $query .= " ORDER BY ";
        $query .= "     T1.coursecd, T1.majorcd, T1.examcoursecd ";
        
        return $query;
    }

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.name  as testname, ";
        $query .= "     T3.name1 as subclassname ";
        $query .= " FROM ";
        $query .= "     entexam_perfect_mst T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_TESTDIV_MST T2 ";
        $query .= " ON ";
        $query .= "     T1.testdiv = T2.testdiv AND ";
        $query .= "     T1.entexamyear = T2.entexamyear ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     name_mst T3 ";
        $query .= " ON ";
        $query .= "     T1.testsubclasscd = T3.namecd2 AND ";
        $query .= "     T3.namecd1 = 'L009' ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear = '".$model->year."' AND ";
        $query .= "     T1.coursecd || T1.majorcd || T1.examcoursecd = '".$model->examcoursecd."'";
        $query .= " ORDER BY ";
        $query .= "     T2.SHOWORDER, ";
        $query .= "     T1.testdiv, ";
        $query .= "     T1.testsubclasscd, ";
        $query .= "     T1.perfect DESC ";

        return $query;
    }

    //課程学科
    function selectTotalcd($year)
    {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.MAJORNAME, ";
        $query .= "     T2.COURSENAME ";
        $query .= " FROM ";
        $query .= "     V_MAJOR_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_COURSE_MST T2 ";
        $query .= " ON  T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.COURSECD = T2.COURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year. "'";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD ";

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
        $query .= "     entexamyear  = '".$model->year."' AND";
        $query .= "         testdiv  = '".$model->testdiv."' AND";
        $query .= "  testsubclasscd  = '".$model->testsubclasscd."' AND";
        $query .= "        coursecd  = '".$model->coursecd."' AND ";
        $query .= "         majorcd  = '".$model->majorcd."' AND ";
        $query .= "    examcoursecd  = '".$model->coursecode."'";

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
        $query .= "     entexam_applicantbase_dat T3, ";
        $query .= "     entexam_wishdiv_mst T4 ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear    = '".$model->year."' AND ";
        $query .= "     T1.testdiv        = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     T1.testsubclasscd = '".$model->field["TESTSUBCLASSCD"]."' AND ";
        $query .= "     T2.entexamyear    = T1.entexamyear AND ";
        $query .= "     T2.testdiv        = T1.testdiv AND ";
        $query .= "     T2.receptno       = T1.receptno AND ";
        $query .= "     T3.entexamyear    = T2.entexamyear AND ";
        $query .= "     T3.testdiv        = T2.testdiv AND ";
        $query .= "     T3.examno         = T2.examno AND ";
        $query .= "     T4.entexamyear    = T3.entexamyear AND ";
        $query .= "     T4.desirediv      = T3.desirediv AND ";
        $query .= "     T4.wishno         = '1' AND ";
        $query .= "     T4.coursecd       = '".$model->coursecd."' AND ";
        $query .= "     T4.majorcd        = '".$model->majorcd."' AND ";
        $query .= "     T4.examcoursecd   = '".$model->coursecode."' ";

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return $row;
    }

    //重複チェック
    function CheckData($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     entexam_perfect_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->year."' AND";
        $query .= "         testdiv  = '".$model->field["TESTDIV"]."' AND";
        $query .= "  testsubclasscd  = '".$model->field["TESTSUBCLASSCD"]."' AND";
        $query .= "        coursecd  = '".substr($model->examcoursecd, 0, 1)."' AND ";
        $query .= "         majorcd  = '".substr($model->examcoursecd, 1, 3)."' AND ";
        $query .= "    examcoursecd  = '".substr($model->examcoursecd, 4, 4)."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $model->field["ENTEXAMYEAR"];
        $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
        $data["COURSECD"][TEXT]         = substr($model->examcoursecd, 0, 1);
        $data["MAJORCD"][TEXT]          = substr($model->examcoursecd, 1, 3);
        $data["EXAMCOURSECD"][TEXT]     = substr($model->examcoursecd, 4, 4);
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
        $query .= "       WHERE entexamyear     = '".$model->field["ENTEXAMYEAR"]."'";
        $query .= "         AND testdiv         = '".$model->field["TESTDIV"]."'";
        $query .= "         AND coursecd        = '".substr($model->examcoursecd, 0, 1)."'";
        $query .= "         AND majorcd         = '".substr($model->examcoursecd, 1, 3)."'";
        $query .= "         AND examcoursecd    = '".substr($model->examcoursecd, 4, 4)."'";
        $query .= "         AND testsubclasscd  = '".$model->field["TESTSUBCLASSCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return;
    }

    //次年度のデータを作成
    function &getCopyYearQuery($model)
    {
        $flg = false;
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "  SELECT";
        $query .= "      *";
        $query .= "  FROM";
        $query .= "      ENTEXAM_PERFECT_MST T1,";
        $query .= "      ENTEXAM_TESTDIV_MST T2,";
        $query .= "      V_NAME_MST T3";
        $query .= "  WHERE";
        $query .= "      T1.ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "      T1.TESTDIV = T2.TESTDIV AND";
        $query .= "      T2.ENTEXAMYEAR = '".$model->NextYear."' AND";
        $query .= "      T1.TESTSUBCLASSCD = T3.NAMECD2 AND";
        $query .= "      T3.NAMECD1 = 'L009' AND";
        $query .= "      T3.YEAR = '".$model->NextYear."' AND";        
        $query .= "      (COURSECD,MAJORCD,EXAMCOURSECD) IN ";
        $query .= "      (SELECT COURSECD,MAJORCD,EXAMCOURSECD";
        $query .= "         FROM ENTEXAM_COURSE_MST";
        $query .= "        WHERE ENTEXAMYEAR = '".$model->NextYear."'";
        $query .= "      )  AND";
        $query .= "      NOT EXISTS (SELECT 'X'";
        $query .= "                    FROM ENTEXAM_PERFECT_MST W1";
        $query .= "                   WHERE W1.ENTEXAMYEAR    = '".$model->NextYear."' AND";
        $query .= "                         W1.COURSECD       = T1.COURSECD AND";
        $query .= "                         W1.MAJORCD        = T1.MAJORCD AND";
        $query .= "                         W1.EXAMCOURSECD   = T1.EXAMCOURSECD AND";
        $query .= "                         W1.TESTDIV        = T1.TESTDIV AND";
        $query .= "                         W1.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ";
        $query .= "                 )";

        //次年度作成できるデータが存在すれば追加
        if (is_array($db->getRow($query, DB_FETCHMODE_ASSOC))) {
            $flg = true;

            $db->autoCommit(false);
            $query  = "";
            $query .= " INSERT INTO ENTEXAM_PERFECT_MST";
            $query .= " (";
            $query .= "     ENTEXAMYEAR,";
            $query .= "     TESTDIV,";
            $query .= "     COURSECD,";
            $query .= "     MAJORCD,";
            $query .= "     EXAMCOURSECD,";
            $query .= "     TESTSUBCLASSCD,";
            $query .= "     PERFECT,";
            $query .= "     REGISTERCD,UPDATED";
            $query .= " )";
            $query .= "  SELECT";
            $query .= "      '$model->NextYear',";
            $query .= "      TESTDIV,";            
            $query .= "      COURSECD,";
            $query .= "      MAJORCD,";
            $query .= "      EXAMCOURSECD,";
            $query .= "      TESTSUBCLASSCD,";
            $query .= "      PERFECT,";
            $query .= "      '".STAFFCD."', sysdate()";
            $query .= "  FROM";
            $query .= "      ENTEXAM_PERFECT_MST T1";
            $query .= "  WHERE";
            $query .= "      ENTEXAMYEAR = '".$model->year."' AND";
            $query .= "      (COURSECD,MAJORCD,EXAMCOURSECD) IN ";
            $query .= "      (SELECT COURSECD,MAJORCD,EXAMCOURSECD";
            $query .= "         FROM ENTEXAM_COURSE_MST";
            $query .= "        WHERE ENTEXAMYEAR = '".$model->NextYear."'";
            $query .= "      )  AND";
            $query .= "      NOT EXISTS (SELECT 'X'";
            $query .= "                    FROM ENTEXAM_PERFECT_MST W1";
            $query .= "                   WHERE W1.ENTEXAMYEAR    = '".$model->NextYear."' AND";
            $query .= "                         W1.COURSECD       = T1.COURSECD AND";
            $query .= "                         W1.MAJORCD        = T1.MAJORCD AND";
            $query .= "                         W1.EXAMCOURSECD   = T1.EXAMCOURSECD AND";
            $query .= "                         W1.TESTDIV        = T1.TESTDIV AND";
            $query .= "                         W1.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ";
            $query .= "                 )";

            $db->query($query);
            $db->commit();
        }

        Query::dbCheckIn($db);

        return $flg;
     }

}
?>
