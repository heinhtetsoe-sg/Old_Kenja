<?php

require_once('for_php7.php');
class knjz020kQuery extends Query {

    //年度一覧取得
    function selectYearQuery()
    {
        $query  = " SELECT entexamyear FROM entexam_perfect_mst ";
        $query .= "  GROUP BY entexamyear ";
        $query .= "  ORDER BY entexamyear DESC";
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

    //コース
    function getCourse($examyear)
    {
        $query  = " SELECT ";
        $query .= "     coursecd, ";
        $query .= "     majorcd, ";
        $query .= "     examcoursecd, ";
        $query .= "     examcourse_name ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$examyear."' ";
        return $query;
    }

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.testdiv, ";
        $query .= "     T2.name1 AS testname, ";
        $query .= "     T1.testsubclasscd, ";
        $query .= "     T3.name1 AS subclassname, ";
        $query .= "     T1.a_perfect, ";
        $query .= "     T1.a_total_flg, ";  //2005.08.13---alp
        $query .= "     T1.b_perfect, ";    //2005.08.13---alp
        $query .= "     T1.b_total_flg, ";  //2005.08.13---alp
        $query .= "     T1.autocalc, ";
        $query .= "     T1.inc_magnification, ";
        $query .= "     T1.adoptiondiv ";
        $query .= " FROM ";
        $query .= "     entexam_perfect_mst T1 ";
        $query .= " LEFT OUTER JOIN v_name_mst T2 ";
        $query .= "              ON T2.year    = T1.entexamyear ";
        $query .= "             AND T2.namecd1 = 'L003' ";      //固定
        $query .= "             AND T2.namecd2 = T1.testdiv ";
        $query .= " LEFT OUTER JOIN v_name_mst T3 ";
        $query .= "              ON T3.year = T1.entexamyear ";
        $query .= "             AND T3.namecd1 = 'L009' ";      //固定
        $query .= "             AND T3.namecd2 = T1.testsubclasscd ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear  = '".$model->year."' AND ";
        $query .= "     T1.coursecd     = '".SUBSTR("$model->course",0,1)."' AND ";
        $query .= "     T1.majorcd      = '".SUBSTR("$model->course",1,3)."' AND ";
        $query .= "     T1.examcoursecd = '".SUBSTR("$model->course",4)."' ";
        return $query;
    }

    //１レコード取得
    function getRow($model,$flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     testdiv, ";
        $query .= "     testsubclasscd, ";
        $query .= "     a_perfect, ";
        $query .= "     a_total_flg, "; //2005.08.13---alp
        $query .= "     b_perfect, ";   //2005.08.13---alp
        $query .= "     b_total_flg, "; //2005.08.13---alp
        $query .= "     autocalc, ";
        $query .= "     inc_magnification, ";
        $query .= "     adoptiondiv ";
        $query .= " FROM ";
        $query .= "     entexam_perfect_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear    = '".$model->year."' AND ";

        if($flg == 1){
            $query .= "     testdiv        = '".$model->testdiv."' AND ";
            $query .= "     testsubclasscd = '".$model->testsubclasscd."' AND ";
        }elseif($flg == 2){
            $query .= "     testdiv        = '".$model->field["TESTDIV"]."' AND ";
            $query .= "     testsubclasscd = '".$model->field["TESTSUBCLASSCD"]."' AND ";
        }

        $query .= "     coursecd       = '".substr($model->field["COURSE"],0,1)."' AND ";
        $query .= "     majorcd        = '".substr($model->field["COURSE"],1,3)."' AND ";
        $query .= "     examcoursecd   = '".substr($model->field["COURSE"],4)."' ";

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //更新・削除時のチェック
    function ChScoreDat($model, $score)
    {
        $db = Query::dbCheckOut();

        //更新時は最高得点を取得
        if($model->cmd == "update"){
            //$query  = " SELECT MAX(T1.a_score) ";
            if ($score == "a") $query  = " SELECT MAX(T1.a_score) ";//2005.08.13---alp
            if ($score == "b") $query  = " SELECT MAX(T1.b_score) ";//2005.08.13---alp

        //削除時はデータ存在を確認
        }elseif($model->cmd == "delete"){
            $query  = " SELECT COUNT(*) ";
        }

        $query .= " FROM ";
        $query .= "     entexam_score_dat T1, ";
        $query .= "     entexam_applicantbase_dat T2, ";
        $query .= "     entexam_wishdiv_mst T3 ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear    = '".$model->year."' AND ";
        $query .= "     T1.testdiv        = '".$model->testdiv."' AND ";
        $query .= "     T1.testsubclasscd = '".$model->testsubclasscd."' AND ";
        $query .= "     T1.examno         = T2.examno AND ";
        $query .= "     T2.entexamyear    = T1.entexamyear AND ";
        $query .= "     T2.testdiv        = T1.testdiv AND ";
        $query .= "     T2.desirediv      = T3.desirediv AND ";
        $query .= "     T3.entexamyear    = T1.entexamyear AND ";
        $query .= "     T3.testdiv        = T1.testdiv AND ";
        $query .= "     T3.coursecd       = '".substr($model->field["COURSE"],0,1)."' AND ";
        $query .= "     T3.majorcd        = '".substr($model->field["COURSE"],1,3)."' AND ";
        $query .= "     T3.examcoursecd   = '".substr($model->field["COURSE"],4)."' ";

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return $row;
    }

    //名称マスタ取得
    function getName($year,$cd)
    {
        $query  = " SELECT namecd2,name1 FROM v_name_mst WHERE year = '".$year."' AND namecd1 = '".$cd."' ";
        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $data["ENTEXAMYEAR"][TEXT]          = $model->year;
        $data["TESTDIV"][TEXT]              = $model->field["TESTDIV"];
        $data["COURSECD"][TEXT]             = substr($model->field["COURSE"],0,1);
        $data["MAJORCD"][TEXT]              = substr($model->field["COURSE"],1,3);
        $data["EXAMCOURSECD"][TEXT]         = substr($model->field["COURSE"],4);
        $data["TESTSUBCLASSCD"][TEXT]       = $model->field["TESTSUBCLASSCD"];
        $data["A_PERFECT"][NUMBER]          = $model->field["A_PERFECT"];
        $data["A_TOTAL_FLG"][TEXT]          = ($model->field["A_TOTAL_FLG"] == "1") ? 1 : 0 ;//2005.08.13---alp
        $data["B_PERFECT"][NUMBER]          = $model->field["B_PERFECT"];//2005.08.13---alp
        $data["B_TOTAL_FLG"][TEXT]          = ($model->field["B_TOTAL_FLG"] == "1") ? 1 : 0 ;//2005.08.13---alp
            if($model->field["AUTOCALC"] == "1"){
                $data["AUTOCALC"][TEXT]             = 1;
                $data["INC_MAGNIFICATION"][NUMBER]  = $model->field["INC_MAGNIFICATION"];
            }else{
                $data["AUTOCALC"][TEXT]             = 0;
                $data["INC_MAGNIFICATION"][NUMBER]  = "1.0";
            }

        $data["ADOPTIONDIV"][TEXT]          = '0';
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PERFECT_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["A_PERFECT"][NUMBER]  = $model->field["A_PERFECT"];
        $data["A_TOTAL_FLG"][TEXT]  = ($model->field["A_TOTAL_FLG"] == "1") ? 1 : 0 ;//2005.08.13---alp
        $data["B_PERFECT"][NUMBER]  = $model->field["B_PERFECT"];//2005.08.13---alp
        $data["B_TOTAL_FLG"][TEXT]  = ($model->field["B_TOTAL_FLG"] == "1") ? 1 : 0 ;//2005.08.13---alp
            if($model->field["AUTOCALC"] == "1"){
                $data["AUTOCALC"][TEXT]             = 1;
                $data["INC_MAGNIFICATION"][NUMBER]  = $model->field["INC_MAGNIFICATION"];
            }else{
                $data["AUTOCALC"][TEXT]             = 0;
                $data["INC_MAGNIFICATION"][NUMBER]  = "1.0";
            }

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE entexamyear       = '".$model->year."'";
        $where .= "   AND testdiv           = '".$model->field["TESTDIV"]."'";
        $where .= "   AND testsubclasscd    = '".$model->field["TESTSUBCLASSCD"]."'";
        $where .= "   AND coursecd          = '".substr($model->field["COURSE"],0,1)."'";
        $where .= "   AND majorcd           = '".substr($model->field["COURSE"],1,3)."'";
        $where .= "   AND examcoursecd      = '".substr($model->field["COURSE"],4)."'";

        $query = Query::updateSQL($data, "ENTEXAM_PERFECT_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM entexam_perfect_mst ";
        $query .= " WHERE entexamyear       = '".$model->year."'";
        $query .= "   AND testdiv           = '".$model->field["TESTDIV"]."'";
        $query .= "   AND testsubclasscd    = '".$model->field["TESTSUBCLASSCD"]."'";
        $query .= "   AND coursecd          = '".substr("$model->course",0,1)."'";
        $query .= "   AND majorcd           = '".substr("$model->course",1,3)."'";
        $query .= "   AND examcoursecd      = '".substr("$model->course",4)."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);

        return $result;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM entexam_perfect_mst WHERE entexamyear = '".$year."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
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
        $query .= "      entexam_perfect_mst T1,";
        $query .= "      v_name_mst T2,";
        $query .= "      v_name_mst T3";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear    = '".$model->year."' AND";
        $query .= "      T1.testdiv        = T2.namecd2 AND";
        $query .= "      T2.namecd1        = 'L004' AND";
        $query .= "      T2.year           = '".$model->NextYear."' AND";
        $query .= "      T1.testsubclasscd = T3.NAMECD2 AND";
        $query .= "      T3.namecd1        = 'L009' AND";
        $query .= "      T3.year           = '".$model->NextYear."' AND";
        $query .= "     (COURSECD,MAJORCD,EXAMCOURSECD) IN (SELECT coursecd,majorcd,examcoursecd ";
        $query .= "                                           FROM entexam_course_mst ";
        $query .= "                                          WHERE ENTEXAMYEAR = '".$model->NextYear."' ";
        $query .= "                                         ) AND ";
        $query .= "      NOT EXISTS (SELECT 'X'";
        $query .= "                    FROM entexam_perfect_mst W1";
        $query .= "                   WHERE W1.entexamyear    = '".$model->NextYear."' ";
        $query .= "                     AND W1.coursecd       = T1.coursecd ";
        $query .= "                     AND W1.majorcd        = T1.majorcd ";
        $query .= "                     AND W1.examcoursecd   = T1.examcoursecd ";
        $query .= "                     AND W1.testdiv        = T1.testdiv ";
        $query .= "                     AND W1.testsubclasscd = T1.testsubclasscd ";
        $query .= "                 )";

        //次年度作成できるデータが存在すれば追加
        if (is_array($db->getRow($query, DB_FETCHMODE_ASSOC))) {
            $flg = true;

            $db->autoCommit(false);
            $query  = "";
            $query .= " INSERT INTO entexam_perfect_mst";
            $query .= "     ( entexamyear,";
            $query .= "       testdiv,";
            $query .= "       coursecd,";
            $query .= "       majorcd,";
            $query .= "       examcoursecd,";
            $query .= "       testsubclasscd,";
            $query .= "       a_perfect,";
            $query .= "       a_total_flg,";
            $query .= "       b_perfect,";
            $query .= "       b_total_flg,";
            $query .= "       autocalc,";
            $query .= "       inc_magnification,";
            $query .= "       adoptiondiv,";
            $query .= "       registercd,";
            $query .= "       updated )";
            $query .= "     SELECT '$model->NextYear',";
            $query .= "             testdiv,";            
            $query .= "             coursecd,";
            $query .= "             majorcd,";
            $query .= "             examcoursecd,";
            $query .= "             testsubclasscd,";
            $query .= "             a_perfect,";
            $query .= "             a_total_flg,";
            $query .= "             b_perfect,";
            $query .= "             b_total_flg,";
            $query .= "             autocalc,";
            $query .= "             inc_magnification,";
            $query .= "             adoptiondiv,";
            $query .= "             '".STAFFCD."', ";
            $query .= "             sysdate() ";
            $query .= "       FROM entexam_perfect_mst T1 ";
            $query .= "      WHERE ENTEXAMYEAR = '".$model->year."' ";
            $query .= "        AND (COURSECD,MAJORCD,EXAMCOURSECD) IN (SELECT coursecd,majorcd,examcoursecd";
            $query .= "                                                  FROM entexam_course_mst";
            $query .= "                                                 WHERE entexamyear = '".$model->NextYear."') ";
            $query .= "        AND NOT EXISTS (SELECT 'X'";
            $query .= "                          FROM entexam_perfect_mst W1";
            $query .= "                         WHERE W1.entexamyear    = '".$model->NextYear."' ";
            $query .= "                           AND W1.coursecd       = T1.coursecd ";
            $query .= "                           AND W1.majorcd        = T1.majorcd ";
            $query .= "                           AND W1.examcoursecd   = T1.examcoursecd ";
            $query .= "                           AND W1.testdiv        = T1.testdiv ";
            $query .= "                           AND W1.testsubclasscd = T1.testsubclasscd ) ";

            $db->query($query);
            $db->commit();
        }
        Query::dbCheckIn($db);
        return $flg;
    }

//------------------------------↓ adoptiondiv ↓------------------------------
    //アラカルト科目一覧
    function getAdoSubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.testsubclasscd,T2.name1 ";
        $query .= " FROM ";
        $query .= "     entexam_perfect_mst T1 ";
        $query .= " LEFT OUTER JOIN v_name_mst T2 ";
        $query .= "              ON T2.year    = '".$model->year."'";
        $query .= "             AND T2.namecd1 = 'L009' ";
        $query .= "             AND T2.namecd2 = T1.testsubclasscd ";
        $query .= " WHERE T1.entexamyear   = '".$model->year."'";
        $query .= "   AND T1.testdiv       = '".$model->adotestdiv."'";
        $query .= "   AND T1.coursecd      = '".substr("$model->course",0,1)."'";
        $query .= "   AND T1.majorcd       = '".substr("$model->course",1,3)."'";
        $query .= "   AND T1.examcoursecd  = '".substr("$model->course",4)."'";
        $query .= "   AND T1.adoptiondiv  = '1' ";
        return $query;
    }

    //科目一覧
    function getSubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.testsubclasscd, ";
        $query .= "     T2.name1 ";
        $query .= " FROM ";
        $query .= "     entexam_perfect_mst T1 ";
        $query .= " LEFT OUTER JOIN v_name_mst T2 ";
        $query .= "              ON T2.year    = '".$model->year."' ";
        $query .= "             AND T2.namecd1 = 'L009' ";
        $query .= "             AND T2.namecd2 = T1.testsubclasscd ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear  = '".$model->year."' AND ";
        $query .= "     T1.testdiv      = '".$model->adotestdiv."' AND ";
        $query .= "     T1.coursecd     = '".substr("$model->course",0,1)."' AND ";
        $query .= "     T1.majorcd      = '".substr("$model->course",1,3)."' AND ";
        $query .= "     T1.examcoursecd = '".substr("$model->course",4)."' AND ";
        $query .= "     T1.testsubclasscd NOT IN (SELECT testsubclasscd ";
        $query .= "                                 FROM entexam_perfect_mst ";
        $query .= "                                WHERE entexamyear  = '".$model->year."'";
        $query .= "                                  AND testdiv      = '".$model->adotestdiv."'";
        $query .= "                                  AND coursecd     = '".substr("$model->course",0,1)."'";
        $query .= "                                  AND majorcd      = '".substr("$model->course",1,3)."'";
        $query .= "                                  AND examcoursecd = '".substr("$model->course",4)."'";
        $query .= "                                  AND adoptiondiv  = '1' ) ";
        return $query;
    }

    //UPDATE
    function getAdoUpdateQuery($model)
    {
        $selectdata = str_replace(",","','",$model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ADOPTIONDIV='0'にする
        $data["ADOPTIONDIV"][TEXT]  = 0;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE entexamyear     = '".$model->year."'";
        $where .= "   AND testdiv         = '".$model->adotestdiv."'";
        $where .= "   AND coursecd        = '".substr("$model->course",0,1)."'";
        $where .= "   AND majorcd         = '".substr("$model->course",1,3)."'";
        $where .= "   AND examcoursecd    = '".substr("$model->course",4)."'";

        $query = Query::updateSQL($data, "entexam_perfect_mst", $where);
        $db->query($query);

        //選択された科目を ADOPTIONDIV='1'にする
        if($selectdata != ""){
            $data["ADOPTIONDIV"][TEXT]  = 1;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE entexamyear     = '".$model->year."'";
            $where .= "   AND testdiv         = '".$model->adotestdiv."'";
            $where .= "   AND coursecd        = '".substr("$model->course",0,1)."'";
            $where .= "   AND majorcd         = '".substr("$model->course",1,3)."'";
            $where .= "   AND examcoursecd    = '".substr("$model->course",4)."'";
            $where .= "   AND testsubclasscd IN ('$selectdata')";

            $query = Query::updateSQL($data, "entexam_perfect_mst", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
//------------------------------↑ adoptiondiv ↑------------------------------
}
?>
