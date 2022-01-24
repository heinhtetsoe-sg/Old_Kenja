<?php

require_once('for_php7.php');

class knjz030kQuery extends Query {

#    //選択科目マスタが登録済みか
#    function cnt_Electclass($year)
#    {
#        $query  = " SELECT ";
#        $query .= "     COUNT(*) ";
#        $query .= " FROM ";
#        $query .= "     electclass_mst w1, ";
#        $query .= "     v_electclass_mst w2 ";
#        $query .= " WHERE ";
#        $query .= "     w2.year = '".$year."' AND ";
#        $query .= "     w1.groupcd = w2.groupcd ";
#        return $query;
#    }

    //入試年度一覧取得
    function selectYearQuery()
    {
        $query  = "SELECT DISTINCT entexamyear ";
        $query .= "  FROM entexam_wishdiv_mst ";
        $query .= " ORDER BY entexamyear DESC";
        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(ENTEXAMYEAR) ";
        $query .= " FROM entexam_wishdiv_mst ";
        $query .= " WHERE ENTEXAMYEAR >= '" .$model->examyear. "'";
        return $query;
    }

    //左画面 一覧表示
    function Listdata($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.testdiv, ";
        $query .= "     T1.desirediv, ";
        $query .= "     T1.wishno, ";
        $query .= "     T1.coursecd, ";
        $query .= "     T1.majorcd, ";
        $query .= "     T1.examcoursecd, ";
        $query .= "     '('|| coalesce(T2.examcourse_mark,' ') || ') ' || T2.examcourse_name AS examcourse_name ";
        $query .= " FROM ";
        $query .= "     entexam_wishdiv_mst T1 ";
        $query .= "     LEFT OUTER JOIN entexam_course_mst T2 ";
        $query .= "                  ON T2.entexamyear  = T1.entexamyear ";
        $query .= "                 AND T2.coursecd     = T1.coursecd ";
        $query .= "                 AND T2.majorcd      = T1.majorcd ";
        $query .= "                 AND T2.examcoursecd = T1.examcoursecd ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear = '".$model->examyear."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.testdiv, ";
        $query .= "     T1.desirediv, ";
        $query .= "     T1.wishno,";
        $query .= "     T1.coursecd, ";
        $query .= "     T1.majorcd, ";
        $query .= "     T1.examcoursecd ";
        return $query;
    }

    //試験区分
    function getTestdiv($year)
    {
        return "SELECT namecd2,name1 FROM v_name_mst WHERE year = '".$year."' AND namecd1 = 'L003'";
    }

    //右画面のリスト2リスト設定
    function getListCourse($model,$list)
    {
        //選択コース一覧　リスト用
        if($list == 'Llist'){
            $query  = " SELECT ";
            $query .= "     T1.wishno, ";
            $query .= "     T1.coursecd, ";
            $query .= "     T1.majorcd, ";
            $query .= "     T1.examcoursecd, ";
            $query .= "     '('|| coalesce(T2.examcourse_mark,' ') || ')' || T2.examcourse_name AS examcourse_name ";
            $query .= " FROM ";
            $query .= "     entexam_wishdiv_mst T1 ";
            $query .= "     LEFT OUTER JOIN entexam_course_mst T2 ";
            $query .= "                  ON T2.entexamyear  = T1.entexamyear ";
            $query .= "                 AND T2.coursecd     = T1.coursecd ";
            $query .= "                 AND T2.majorcd      = T1.majorcd ";
            $query .= "                 AND T2.examcoursecd = T1.examcoursecd ";
            $query .= " WHERE ";
            $query .= "     T1.entexamyear = '".$model->examyear."' AND ";
            $query .= "     T1.testdiv     = '".$model->testdiv."' AND ";
            $query .= "     T1.desirediv   = '".$model->desirediv."' ";
            $query .= " ORDER BY ";
            $query .= "     T1.wishno,";
            $query .= "     T1.coursecd, ";
            $query .= "     T1.majorcd, ";
            $query .= "     T1.examcoursecd ";

        //コース一覧　リスト用
        }else{
            $query  = " SELECT ";
            $query .= "     T1.coursecd, ";
            $query .= "     T1.majorcd, ";
            $query .= "     T1.examcoursecd, ";
            $query .= "     '('|| coalesce(T1.examcourse_mark,' ') || ')' || T1.examcourse_name AS examcourse_name ";
            $query .= " FROM ";
            $query .= "     entexam_course_mst T1 ";
            $query .= "WHERE ";
            $query .= "     T1.entexamyear = '".$model->examyear."' AND ";
            $query .= "     (T1.coursecd, T1.majorcd, T1.examcoursecd) NOT IN ";
            $query .= "     (SELECT T2.coursecd, T2.majorcd, T2.examcoursecd ";
            $query .= "        FROM entexam_wishdiv_mst T2";
            $query .= "       WHERE T2.entexamyear = '".$model->examyear."' ";
            $query .= "         AND T2.testdiv     = '".$model->testdiv."' ";
            $query .= "         AND T2.desirediv   = '".$model->desirediv."' ";
            $query .= "     )";
            $query .= " ORDER BY ";
            $query .= "     T1.coursecd, ";
            $query .= "     T1.majorcd, ";
            $query .= "     T1.examcoursecd ";
        }
        return $query;
    }

    //右画面 選択コース一覧
#    function getChCourse($model)
#    {
#        $query  = " SELECT ";
#        $query .= "     T1.wishno, ";
#        $query .= "     T1.coursecd, ";
#        $query .= "     T1.majorcd, ";
#        $query .= "     T1.examcoursecd, ";
#        $query .= "     T2.examcourse_name, ";
#        $query .= "     T2.examcourse_mark ";
#        $query .= " FROM ";
#        $query .= "     entexam_wishdiv_mst T1 ";
#        $query .= " LEFT OUTER JOIN entexam_course_mst T2 ";
#        $query .= "              ON T2.coursecd     = T1.coursecd ";
#        $query .= "             AND T2.majorcd      = T1.majorcd ";
#        $query .= "             AND T2.examcoursecd = T1.examcoursecd ";
#        $query .= " WHERE ";
#        $query .= "     T1.entexamyear = '".$model->examyear."' AND ";
#        $query .= "     T1.testdiv     = '".$model->testdiv."' AND ";
#        $query .= "     T1.desirediv   = '".$model->desirediv."' ";
#        $query .= " GROUP BY ";
#        $query .= "     T1.wishno, ";
#        $query .= "     T1.coursecd, ";
#        $query .= "     T1.majorcd, ";
#        $query .= "     T1.examcoursecd, ";
#        $query .= "     T2.examcourse_name, ";
#        $query .= "     T2.examcourse_mark ";
#        $query .= " ORDER BY ";
#        $query .= "     T1.wishno ";
#        return $query;
#    }

    //右画面 コース一覧
#    function getCourse($model)
#    {
#        $query  = " SELECT ";
#        $query .= "     coursecd, ";
#        $query .= "     majorcd, ";
#        $query .= "     examcoursecd, ";
#        $query .= "     examcourse_name, ";
#        $query .= "     examcourse_mark ";
#        $query .= " FROM ";
#        $query .= "     entexam_course_mst ";
#        $query .= " WHERE ";
#        $query .= "     entexamyear = '".$model->examyear."' AND ";
#        $query .= "     (coursecd,majorcd,examcoursecd) NOT IN(SELECT coursecd,majorcd,examcoursecd ";
#        $query .= "                                              FROM entexam_wishdiv_mst ";
#        $query .= "                                             WHERE entexamyear = '".$model->examyear."' ";
#        $query .= "                                               AND testdiv     = '".$model->testdiv."' ";
#        $query .= "                                               AND desirediv   = '".$model->desirediv."') ";
#        $query .= " GROUP BY ";
#        $query .= "     coursecd, ";
#        $query .= "     majorcd, ";
#        $query .= "     examcoursecd, ";
#        $query .= "     examcourse_name , ";
#        $query .= "     examcourse_mark ";
#        return $query;
#    }

    //コピー前チェック
    function cntWishdiv($examyear)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT COUNT(*) FROM entexam_wishdiv_mst WHERE entexamyear = '".$examyear."' ";

        $cntdiv = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntdiv;
    }

    //今年度のデータが存在するかチェック
    function CheckData($examyear)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM entexam_wishdiv_mst WHERE entexamyear = '".$examyear."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    //次年度データ作成
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $flg = false;

        $query  = "";
        $query .= "  SELECT";
        $query .= "      *";
        $query .= "  FROM";
        $query .= "      entexam_wishdiv_mst T1";
        $query .= "  WHERE";
        $query .= "      ENTEXAMYEAR = '".$model->examyear."' AND";
        $query .= "      (COURSECD,MAJORCD) IN ";
        $query .= "      (SELECT COURSECD,MAJORCD";
        $query .= "         FROM V_MAJOR_MST";
        $query .= "        WHERE YEAR = '".($model->examyear+1)."'";
        $query .= "      )  AND";
        $query .= "      NOT EXISTS (SELECT 'X'";
        $query .= "                    FROM entexam_wishdiv_mst W1";
        $query .= "                   WHERE W1.ENTEXAMYEAR  = '".($model->examyear+1)."' AND";
        $query .= "                         W1.desirediv    = T1.desirediv AND";
        $query .= "                         W1.wishno       = T1.wishno";
        $query .= "                 )";
        $db->query($query);

        //次年度作成できるデータが存在すれば追加
        if (is_array($db->getRow($query, DB_FETCHMODE_ASSOC))) {
            $flg = true;

            $db->autoCommit(false);

            $query  = "";
            $query .= " INSERT INTO entexam_wishdiv_mst";
            $query .= " (";
            $query .= "     ENTEXAMYEAR,";
            $query .= "     TESTDIV,";
            $query .= "     DESIREDIV,";
            $query .= "     WISHNO,";
            $query .= "     COURSECD,";
            $query .= "     MAJORCD,";
            $query .= "     EXAMCOURSECD,";
            $query .= "     REGISTERCD,UPDATED";
            $query .= " )";
            $query .= "  SELECT";
            $query .= "     '".($model->examyear + 1)."',";
            $query .= "     TESTDIV, ";
            $query .= "     DESIREDIV,";
            $query .= "     WISHNO,";
            $query .= "     COURSECD,";
            $query .= "     MAJORCD,";
            $query .= "     EXAMCOURSECD,";
            $query .= "     '".STAFFCD."', sysdate()";
            $query .= "  FROM";
            $query .= "      entexam_wishdiv_mst T1";
            $query .= "  WHERE";
            $query .= "      ENTEXAMYEAR = '".$model->examyear."' AND";
            $query .= "      (COURSECD,MAJORCD) IN ";
            $query .= "      (SELECT COURSECD,MAJORCD";
            $query .= "         FROM V_MAJOR_MST";
            $query .= "        WHERE YEAR = '".($model->examyear + 1)."'";
            $query .= "      )  AND";
            $query .= "      NOT EXISTS (SELECT 'X'";
            $query .= "                    FROM entexam_wishdiv_mst W1";
            $query .= "                   WHERE W1.ENTEXAMYEAR  = '".($model->examyear+1)."' AND";
            $query .= "                         W1.desirediv    = T1.desirediv AND";
            $query .= "                         W1.wishno       = T1.wishno";
            $query .= "                 )";

            $db->query($query);
            $db->commit();
        }
        Query::dbCheckIn($db);
       return $flg;
    }

    //使用中かのチェック
    function getCntWishdiv($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     entexam_applicantbase_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$model->examyear."' AND ";
        $query .= "     testdiv     = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     desirediv   = '".$model->field["DESIREDIV"]."' ";
        return $query;
    }

    //使用中かのチェック
    function getChCntCourse($model)
    {
        $selectdata = str_replace(",", "','", $model->selectdata);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     entexam_applicantbase_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$model->examyear."' AND ";
        $query .= "     testdiv     = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     desirediv   = '".$model->field["DESIREDIV"]."' AND ";
        $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode) IN ";
        $query .= "     (SELECT coursecd,majorcd,examcoursecd ";
        $query .= "        FROM entexam_wishdiv_mst ";
        $query .= "       WHERE entexamyear = '".$model->examyear."' ";
        $query .= "         AND testdiv     = '".$model->field["TESTDIV"]."' ";
        $query .= "         AND desirediv   = '".$model->field["DESIREDIV"]."' ";
        $query .= "         AND coursecd || majorcd || examcoursecd NOT IN ('".$selectdata."') ";
        $query .= "     )";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM entexam_wishdiv_mst ";
        $query .= "       WHERE entexamyear = '".$model->examyear."' ";
        $query .= "         AND testdiv     = '".$model->field["TESTDIV"]."' ";
        $query .= "         AND desirediv   = '".$model->field["DESIREDIV"]."' ";

        $db->query($query);

        if($model->selectdata){
            $selectarr = array();
            $selectarr = explode(",",$model->selectdata);

            for($arr=0; $arr<get_count($selectarr); $arr++)
            {
                $data["ENTEXAMYEAR"][TEXT]  = $model->examyear;
                $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
                $data["DESIREDIV"][TEXT]    = $model->field["DESIREDIV"];
                $data["WISHNO"][TEXT]       = $arr == 3 ? $arr + 6 : $arr + 1;
                $data["COURSECD"][TEXT]     = substr("$selectarr[$arr]",0,1);
                $data["MAJORCD"][TEXT]      = substr("$selectarr[$arr]",1,3);
                $data["EXAMCOURSECD"][TEXT] = substr("$selectarr[$arr]",4);
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "SYSDATE()";

                $query = Query::insertSQL($data, entexam_wishdiv_mst);
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
