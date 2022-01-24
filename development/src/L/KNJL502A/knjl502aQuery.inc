<?php

require_once('for_php7.php');

class knjl502aQuery extends Query {
    //リストデータ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     CONF.TESTDIV ";
        $query .= "     , CONF.HOPE_COURSECODE ";
        $query .= "     , CONF.CLASS_SCORE ";
        $query .= "     , CONF.SCORE5 ";
        $query .= "     , CONF.SCORE9 ";
        $query .= "     , CONF.HEALTH_PE_DISREGARD ";
        $query .= "     , NAME.NAME1";
        $query .= "     , HOPE.HOPE_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONFRPT_BASE_MST CONF ";
        $query .= "     INNER JOIN V_NAME_MST NAME ";
        $query .= "         ON NAME.YEAR = CONF.ENTEXAMYEAR ";
        $query .= "         AND NAME.NAMECD2 = CONF.TESTDIV ";
        $query .= "         AND NAME.NAMECD1 = 'L004' ";
        $query .= "     INNER JOIN ENTEXAM_HOPE_COURSE_MST HOPE ";
        $query .= "         ON HOPE.HOPE_COURSECODE = CONF.HOPE_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     TESTDIV ";
        $query .= "     , HOPE_COURSECODE ";

        return $query;
    }

    //入試区分コード取得
    function getNameMst($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= "     , NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'L004' ";

        return $query;
    }

    //志望区分取得
    function getHopeCoursecode($model) {
        $query  = " SELECT ";
        $query .= "     YDAT.HOPE_COURSECODE || ' : ' || MST.HOPE_NAME AS LABEL, ";
        $query .= "     YDAT.HOPE_COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HOPE_COURSE_YDAT YDAT ";
        $query .= "     INNER JOIN ENTEXAM_HOPE_COURSE_MST MST ON YDAT.HOPE_COURSECODE = MST.HOPE_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     YDAT.ENTEXAMYEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     TESTDIV ";
        $query .= "     , HOPE_COURSECODE ";
        $query .= "     , CLASS_SCORE ";
        $query .= "     , SCORE5 ";
        $query .= "     , SCORE9 ";
        $query .= "     , HEALTH_PE_DISREGARD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONFRPT_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        if ($model->field["TESTDIV"] && $model->cmd != "reset") {
            $query .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
            $query .= "     AND HOPE_COURSECODE = '".$model->field["HOPE_COURSECODE"]."' ";
        } else {
            $query .= "     AND TESTDIV = '".$model->testdiv."' ";
            $query .= "     AND HOPE_COURSECODE = '".$model->hopeCoursecode."' ";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

/***** 前年度コピー処理 *****/
    //年度のデータが存在するかチェック
    function CheckData($model) {
        $year  = $model->year;     //今年度チェック用
        $Lyear = $model->year - 1; //前年度チェック用

        $db = Query::dbCheckOut();

            $query  = " SELECT DISTINCT ";
            $query .= "     CASE WHEN ENTEXAMYEAR = '".$year."'  THEN 'X' ";
            $query .= "          WHEN ENTEXAMYEAR = '".$Lyear."' THEN 'Y' END  AS CNT";
            $query .= " FROM ";
            $query .= "     ENTEXAM_CONFRPT_BASE_MST ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     IN ('".$year."', '".$Lyear."') ";

        $result = $db->query($query);
        $opt_year = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = $row["CNT"];
        }

        Query::dbCheckIn($db);
        return $opt_year;
    }

    //前年度のデータをコピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ENTEXAM_CONFRPT_BASE_MST ";
        $query .= "         (  ";
        $query .= "          ENTEXAMYEAR ";
        $query .= "          , TESTDIV ";
        $query .= "          , HOPE_COURSECODE ";
        $query .= "          , CLASS_SCORE ";
        $query .= "          , SCORE5 ";
        $query .= "          , SCORE9 ";
        $query .= "          , HEALTH_PE_DISREGARD ";
        $query .= "          , REGISTERCD ";
        $query .= "          , UPDATED ";
        $query .= "         ) ";
        $query .= "     ( ";
        $query .= "      SELECT ";
        $query .= "          '".$model->year."' ";
        $query .= "          , TESTDIV ";
        $query .= "          , HOPE_COURSECODE ";
        $query .= "          , CLASS_SCORE ";
        $query .= "          , SCORE5 ";
        $query .= "          , SCORE9 ";
        $query .= "          , HEALTH_PE_DISREGARD ";
        $query .= "          , '".STAFFCD."' ";
        $query .= "          , SYSDATE() ";
        $query .= "      FROM ";
        $query .= "          ENTEXAM_CONFRPT_BASE_MST ";
        $query .= "      WHERE ";
        $query .= "          ENTEXAMYEAR = '".($model->year - 1)."' ";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["ENTEXAMYEAR"][TEXT]         = $model->year;
        $data["TESTDIV"][TEXT]             = $model->field["TESTDIV"];
        $data["HOPE_COURSECODE"][TEXT]     = $model->field["HOPE_COURSECODE"];
        $data["CLASS_SCORE"][TEXT]         = $model->field["CLASS_SCORE"];
        $data["SCORE5"][NUMBER]            = $model->field["SCORE5"];
        $data["SCORE9"][NUMBER]            = $model->field["SCORE9"];
        $data["HEALTH_PE_DISREGARD"][TEXT] = $model->field["HEALTH_PE_DISREGARD"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_CONFRPT_BASE_MST");
        $db->query($query);

        $db->commit();

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields, $reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["ENTEXAMYEAR"][TEXT]         = $model->year;
        $data["TESTDIV"][TEXT]             = $model->field["TESTDIV"];
        $data["HOPE_COURSECODE"][TEXT]     = $model->field["HOPE_COURSECODE"];
        $data["CLASS_SCORE"][TEXT]         = $model->field["CLASS_SCORE"];
        $data["SCORE5"][NUMBER]            = $model->field["SCORE5"];
        $data["SCORE9"][NUMBER]            = $model->field["SCORE9"];
        $data["HEALTH_PE_DISREGARD"][TEXT] = $model->field["HEALTH_PE_DISREGARD"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR     = '".$model->year."' ";
        $where .= "   AND TESTDIV         = '".$model->field["TESTDIV"]."' ";
        $where .= "   AND HOPE_COURSECODE = '".$model->field["HOPE_COURSECODE"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_CONFRPT_BASE_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ENTEXAM_CONFRPT_BASE_MST ";
        $query .= " WHERE ENTEXAMYEAR     = '".$model->year."' ";
        $query .= "   AND TESTDIV         = '".$model->field["TESTDIV"]."' ";
        $query .= "   AND HOPE_COURSECODE = '".$model->field["HOPE_COURSECODE"]."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

}
?>
