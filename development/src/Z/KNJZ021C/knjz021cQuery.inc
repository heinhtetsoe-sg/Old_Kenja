<?php

require_once('for_php7.php');

class knjz021cQuery extends Query {

    //年度一覧取得
    function selectYearQuery()
    {
        $query  = " SELECT entexamyear ";
        $query .= " FROM ENTEXAM_PERFECT_PRE_MST ";
        $query .= " GROUP BY entexamyear ";
        $query .= " ORDER BY entexamyear DESC";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(ENTEXAMYEAR) ";
        $query .= " FROM ENTEXAM_PERFECT_PRE_MST ";
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

    //データ取得
    function selectQuery($model)
    {
        $query .= "SELECT ";
        $query .= "    T1.*, ";
        $query .= "    T3.NAME1 AS SCHOOL, ";
        $query .= "    T4.NAME1 AS TYPE, ";
        $query .= "    T5.NAME1 AS SUBCLASSNAME ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_PERFECT_PRE_MST T1  ";
        $query .= "LEFT OUTER JOIN V_NAME_MST T3  ";
        $query .= "     ON T1.ENTEXAMYEAR = T3.YEAR  ";
        $query .= "    AND T1.APPLICANTDIV = T3.NAMECD2  ";
        $query .= "    AND T3.NAMECD1 = 'L103'  ";
        $query .= "LEFT OUTER JOIN V_NAME_MST T4  ";
        $query .= "     ON T1.ENTEXAMYEAR = T4.YEAR  ";
        $query .= "    AND T1.PRE_EXAM_TYPE = T4.NAMECD2  ";
        $query .= "    AND T4.NAMECD1 = 'L105'  ";
        $query .= "LEFT OUTER JOIN V_NAME_MST T5  ";
        $query .= "     ON T1.ENTEXAMYEAR = T5.YEAR  ";
        $query .= "    AND INT(T1.TESTSUBCLASSCD) = INT(T5.NAMECD2)  ";
        $query .= "    AND T5.NAMECD1 = 'L109'  ";
        $query .= "WHERE ";
        $query .= "    ENTEXAMYEAR = '{$model->year}' ";
        $query .= "ORDER BY ";
        $query .= "    T1.APPLICANTDIV, ";
        $query .= "    T1.PRE_EXAM_TYPE, ";
        $query .= "    T1.TESTSUBCLASSCD ";

        return $query;
    }

    //最初に開く時の1レコード取得
    function getDefault($year) {
        $query  = "SELECT ";
        $query .= "    ENTEXAMYEAR, ";
        $query .= "    APPLICANTDIV ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_PERFECT_PRE_MST  ";
        $query .= "WHERE ";
        $query .= "    ENTEXAMYEAR = '{$year}' ";
        $query .= "FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //１レコード取得
    function getRow($db,$model)
    {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_PRE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->field["ENTEXAMYEAR"]}' AND";
        $query .= "    APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND";
        $query .= "   PRE_EXAM_TYPE  = '{$model->field["PRE_EXAM_TYPE"]}' AND ";
        $query .= "  TESTSUBCLASSCD  = '{$model->field["TESTSUBCLASSCD"]}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //ENTEXAM_SCORE_PRE_DATのMAX(score)
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
        
        if ($model->field["PRE_EXAM_TYPE"] == '1') {
            $query .= " FROM ";
            $query .= "     entexam_score_pre_dat T1, ";
            $query .= "     entexam_recept_pre_dat T2, ";
            $query .= "     entexam_applicantbase_pre_dat T3 ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR    = '{$model->field["ENTEXAMYEAR"]}' AND ";
            $query .= "     T1.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' AND ";
            $query .= "     T1.TESTSUBCLASSCD = '{$model->field["TESTSUBCLASSCD"]}' AND ";
            $query .= "     T2.ENTEXAMYEAR    = T1.ENTEXAMYEAR AND ";
            $query .= "     T2.APPLICANTDIV   = T1.APPLICANTDIV AND ";
            $query .= "     T2.PRE_RECEPTNO   = T1.PRE_RECEPTNO AND ";
            $query .= "     T3.ENTEXAMYEAR    = T2.ENTEXAMYEAR AND ";
            $query .= "     T3.APPLICANTDIV   = T2.APPLICANTDIV AND ";
            $query .= "     T3.PRE_RECEPTNO   = T2.PRE_RECEPTNO AND";
            $query .= "     T3.PRE_EXAM_TYPE  = '1' ";
            
        } else if ($model->field["PRE_EXAM_TYPE"] == '2') {
            $query .= " FROM ";
            $query .= "     entexam_score_pre_dat T1, ";
            $query .= "     entexam_recept_pre_dat T2, ";
            $query .= "     entexam_applicantbase_pre_dat T3 ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR    = '{$model->field["ENTEXAMYEAR"]}' AND ";
            $query .= "     T1.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' AND ";
            $query .= "     T1.TESTSUBCLASSCD = '{$model->field["TESTSUBCLASSCD"]}' AND ";
            $query .= "     T2.ENTEXAMYEAR    = T1.ENTEXAMYEAR AND ";
            $query .= "     T2.APPLICANTDIV   = T1.APPLICANTDIV AND ";
            $query .= "     T2.PRE_RECEPTNO   = T1.PRE_RECEPTNO AND ";
            $query .= "     T3.ENTEXAMYEAR    = T2.ENTEXAMYEAR AND ";
            $query .= "     T3.APPLICANTDIV   = T2.APPLICANTDIV AND ";
            $query .= "     T3.PRE_RECEPTNO   = T2.PRE_RECEPTNO AND";
            $query .= "     T3.PRE_EXAM_TYPE  = '2' ";
        }
        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return $row;
    }


    //重複チェック
    function CheckData($model, $entexamyear, $applicantdiv)
    {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_PRE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$entexamyear}' AND";
        $query .= "    APPLICANTDIV  = '{$applicantdiv}' AND";
        $query .= "    PRE_EXAM_TYPE = '{$model->field["PRE_EXAM_TYPE"]}' AND";
        $query .= "  TESTSUBCLASSCD  = '{$model->field["TESTSUBCLASSCD"]}'";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $entexamyear, $applicantdiv)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $entexamyear;
        $data["APPLICANTDIV"][TEXT]     = $applicantdiv;
        $data["PRE_EXAM_TYPE"][TEXT]    = $model->field["PRE_EXAM_TYPE"];
        $data["TESTSUBCLASSCD"][TEXT]   = $model->field["TESTSUBCLASSCD"];
        $data["PERFECT"][NUMBER]        = $model->field["PERFECT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PERFECT_PRE_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $entexamyear, $applicantdiv)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_PERFECT_PRE_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '{$entexamyear}'";
        $query .= "         AND APPLICANTDIV    = '{$applicantdiv}'";
        $query .= "         AND PRE_EXAM_TYPE    = '{$model->field["PRE_EXAM_TYPE"]}'";
        $query .= "         AND TESTSUBCLASSCD  = '{$model->field["TESTSUBCLASSCD"]}'";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    function getCheckPerfectMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_PRE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //次年度のデータを作成
    function &getCopyQuery($thisYear, $nextYear)
    {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_PERFECT_PRE_MST ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     PRE_EXAM_TYPE, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     PERFECT, ";
        $query .= "     '" . STAFFCD . "', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_PRE_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
     }
}
?>
