<?php

require_once('for_php7.php');

class knjl035bQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '{$model->year}' ";

        return $query;
    }

    //左画面 一覧表示
    function Listdata($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.WISHNO, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD, ";
        $query .= "     '('|| COALESCE(T2.EXAMCOURSE_MARK,' ') || ') ' || T2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1 ";
        $query .= "     LEFT OUTER JOIN ENTEXAM_COURSE_MST T2 ";
        $query .= "                  ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                 AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                 AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "                 AND T2.COURSECD     = T1.COURSECD ";
        $query .= "                 AND T2.MAJORCD      = T1.MAJORCD ";
        $query .= "                 AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.WISHNO,";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($year, $namecd) {
        $query .= " SELECT ";
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

    //１レコード取得
    function getRow($year, $applicantdiv, $testdiv, $desirediv)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$year}' AND";
        $query .= "     APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     TESTDIV      = '{$testdiv}' AND ";
        $query .= "     DESIREDIV    = '{$desirediv}' ";

        return $query;
    }

    //右画面のリスト2リスト設定
    function getListCourse($model, $list, $applicantdiv, $testdiv, $desirediv)
    {
        //選択コース一覧　リスト用
        if($list == 'Llist'){
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.WISHNO, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.EXAMCOURSECD, ";
            $query .= "     '('|| VALUE(T2.EXAMCOURSE_MARK,' ') || ')' || T2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_WISHDIV_MST T1 ";
            $query .= "     LEFT JOIN ENTEXAM_COURSE_MST T2 ";
            $query .= "                  ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                 AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                 AND T2.TESTDIV      = T1.TESTDIV ";
            $query .= "                 AND T2.COURSECD     = T1.COURSECD ";
            $query .= "                 AND T2.MAJORCD      = T1.MAJORCD ";
            $query .= "                 AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
            $query .= "     T1.APPLICANTDIV = '".$applicantdiv."' AND ";
            $query .= "     T1.TESTDIV      = '".$testdiv."' AND ";
            $query .= "     T1.DESIREDIV    = '".$desirediv."' ";
            $query .= " ORDER BY ";
            $query .= "     T1.WISHNO,";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.EXAMCOURSECD ";

        //コース一覧　リスト用
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.EXAMCOURSECD, ";
            $query .= "     '('|| VALUE(T1.EXAMCOURSE_MARK,' ') || ')' || T1.EXAMCOURSE_NAME AS EXAMCOURSE_NAME ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_COURSE_MST T1 ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
            $query .= "     T1.APPLICANTDIV = '".$applicantdiv."' AND ";
            $query .= "     T1.TESTDIV      = '".$testdiv."' AND ";
            $query .= "     (T1.COURSECD, T1.MAJORCD, T1.EXAMCOURSECD) NOT IN ";
            $query .= "     (SELECT T2.COURSECD, T2.MAJORCD, T2.EXAMCOURSECD ";
            $query .= "        FROM ENTEXAM_WISHDIV_MST T2";
            $query .= "       WHERE T2.ENTEXAMYEAR  = '".$model->year."' ";
            $query .= "         AND T2.APPLICANTDIV = '".$applicantdiv."' ";
            $query .= "         AND T2.TESTDIV      = '".$testdiv."' ";
            $query .= "         AND T2.DESIREDIV    = '".$desirediv."' ";
            $query .= "     )";
            $query .= " ORDER BY ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.EXAMCOURSECD ";

        }
        return $query;
    }

    //コピー前チェック
    function cntWishdiv($year)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT COUNT(*) FROM entexam_wishdiv_mst WHERE entexamyear = '".$year."' ";

        $cntdiv = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntdiv;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query = " SELECT 'X' FROM ENTEXAM_WISHDIV_MST WHERE ENTEXAMYEAR = '".$year."'";
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
        $query .= "      ENTEXAM_WISHDIV_MST T1";
        $query .= "  WHERE";
        $query .= "      ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "      (COURSECD, MAJORCD) IN ";
        $query .= "      (SELECT COURSECD, MAJORCD";
        $query .= "         FROM V_MAJOR_MST";
        $query .= "        WHERE YEAR = '".($model->year+1)."'";
        $query .= "      )  AND ";
        $query .= "      NOT EXISTS (SELECT 'X'";
        $query .= "                    FROM ENTEXAM_WISHDIV_MST W1";
        $query .= "                   WHERE W1.ENTEXAMYEAR  = '".($model->year+1)."' AND";
        $query .= "                         W1.APPLICANTDIV = T1.APPLICANTDIV AND";
        $query .= "                         W1.TESTDIV      = T1.TESTDIV AND";
        $query .= "                         W1.DESIREDIV    = T1.DESIREDIV AND";
        $query .= "                         W1.WISHNO       = T1.WISHNO";
        $query .= "                 )";
        $db->query($query);

        //次年度作成できるデータが存在すれば追加
        if (is_array($db->getRow($query, DB_FETCHMODE_ASSOC))) {
            $flg = true;

            $db->autoCommit(false);

            $query  = "";
            $query .= " INSERT INTO ENTEXAM_WISHDIV_MST";
            $query .= " (";
            $query .= "     ENTEXAMYEAR,";
            $query .= "     APPLICANTDIV,";
            $query .= "     TESTDIV,";
            $query .= "     DESIREDIV,";
            $query .= "     WISHNO,";
            $query .= "     COURSECD,";
            $query .= "     MAJORCD,";
            $query .= "     EXAMCOURSECD,";
            $query .= "     REGISTERCD, UPDATED";
            $query .= " )";
            $query .= "  SELECT";
            $query .= "     '".($model->year + 1)."',";
            $query .= "     APPLICANTDIV, ";
            $query .= "     TESTDIV, ";
            $query .= "     DESIREDIV,";
            $query .= "     WISHNO,";
            $query .= "     COURSECD,";
            $query .= "     MAJORCD,";
            $query .= "     EXAMCOURSECD,";
            $query .= "     '".STAFFCD."', sysdate()";
            $query .= "  FROM";
            $query .= "      ENTEXAM_WISHDIV_MST T1";
            $query .= "  WHERE";
            $query .= "      ENTEXAMYEAR = '".$model->year."' AND";
            $query .= "      (COURSECD,MAJORCD) IN ";
            $query .= "      (SELECT COURSECD,MAJORCD";
            $query .= "         FROM V_MAJOR_MST";
            $query .= "        WHERE YEAR = '".($model->year + 1)."'";
            $query .= "      )  AND";
            $query .= "      NOT EXISTS (SELECT 'X'";
            $query .= "                    FROM ENTEXAM_WISHDIV_MST W1";
            $query .= "                   WHERE W1.ENTEXAMYEAR  = '".($model->year+1)."' AND";
            $query .= "                         W1.APPLICANTDIV = T1.APPLICANTDIV AND";
            $query .= "                         W1.TESTDIV      = T1.TESTDIV AND";
            $query .= "                         W1.DESIREDIV    = T1.DESIREDIV AND";
            $query .= "                         W1.WISHNO       = T1.WISHNO";
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
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     TESTDIV         = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     DESIREDIV       = '".$model->field["DESIREDIV"]."' ";
        return $query;
    }

    //使用中かのチェック
    function getChCntCourse($model)
    {
        $selectdata = str_replace(",", "','", $model->selectdata);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     TESTDIV         = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     DESIREDIV       = '".$model->field["DESIREDIV"]."' AND ";
        $query .= "     (SUC_COURSECD, SUC_MAJORCD, SUC_COURSECODE) IN ";
        $query .= "     (SELECT COURSECD, MAJORCD, EXAMCOURSECD ";
        $query .= "        FROM ENTEXAM_WISHDIV_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$model->year."' ";
        $query .= "         AND APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "         AND TESTDIV         = '".$model->field["TESTDIV"]."' ";
        $query .= "         AND DESIREDIV       = '".$model->field["DESIREDIV"]."' ";
        $query .= "         AND COURSECD || MAJORCD || EXAMCOURSECD NOT IN ('".$selectdata."') ";
        $query .= "     )";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ENTEXAM_WISHDIV_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$model->year."' ";
        $query .= "         AND APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "         AND TESTDIV         = '".$model->field["TESTDIV"]."' ";
        $query .= "         AND DESIREDIV       = '".$model->field["DESIREDIV"]."' ";

        $db->query($query);

        if($model->selectdata){
            $selectarr = array();
            $selectarr = explode(",",$model->selectdata);

            for($arr=0; $arr<get_count($selectarr); $arr++)
            {
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
                $data["DESIREDIV"][TEXT]    = $model->field["DESIREDIV"];
                $data["WISHNO"][TEXT]       = $arr + 1;
                $data["COURSECD"][TEXT]     = substr("$selectarr[$arr]",0,1);
                $data["MAJORCD"][TEXT]      = substr("$selectarr[$arr]",1,3);
                $data["EXAMCOURSECD"][TEXT] = substr("$selectarr[$arr]",4);
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "SYSDATE()";

                $query = Query::insertSQL($data, ENTEXAM_WISHDIV_MST);
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
