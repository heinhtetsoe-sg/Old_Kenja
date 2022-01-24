<?php

require_once('for_php7.php');

class knjl052dQuery extends Query {
    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " WITH T_ALL (LABEL, VALUE) AS ( ";
        $query .= "      VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= "      ) ";
        $query .= " SELECT ";
        $query .= "      LABEL, ";
        $query .= "      VALUE ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR AS LABEL, ";
        $query .= "     ENTEXAMYEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUESTION_POINT_YMST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($model, $namecd1) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストデータ取得
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     QUESTION_ORDER, ";
        $query .= "     QUEST_FLAG, ";
        $query .= "     PATTERN_CD, ";
        $query .= "     ANSWER1, ";
        $query .= "     POINT1, ";
        $query .= "     ANSWER2, ";
        $query .= "     POINT2, ";
        $query .= "     ANSWER3, ";
        $query .= "     POINT3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUESTION_POINT_YMST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND SUBCLASS_CD = '".$model->field["SUBCLASS_CD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     INT(QUESTION_ORDER) ";

        return $query;
    }

/***** 次年度作成処理 *****/
    //ある年度にデータが存在するのかチェック
    function getCheckEntexamQuesPointYMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUESTION_POINT_YMST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //COPY(ENTEXAM_QUESTION_POINT_YMST)
    function getCopyQuery($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_QUESTION_POINT_YMST ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     SUBCLASS_CD, ";
        $query .= "     LARGE_QUESTION, ";
        $query .= "     QUESTION, ";
        $query .= "     QUESTION_ORDER, ";
        $query .= "     PATTERN_CD, ";
        $query .= "     ANSWER1, ";
        $query .= "     POINT1, ";
        $query .= "     ANSWER2, ";
        $query .= "     POINT2, ";
        $query .= "     ANSWER3, ";
        $query .= "     POINT3, ";
        $query .= "     QUEST_FLAG, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     SUBCLASS_CD, ";
        $query .= "     LARGE_QUESTION, ";
        $query .= "     QUESTION, ";
        $query .= "     QUESTION_ORDER, ";
        $query .= "     PATTERN_CD, ";
        $query .= "     ANSWER1, ";
        $query .= "     POINT1, ";
        $query .= "     ANSWER2, ";
        $query .= "     POINT2, ";
        $query .= "     ANSWER3, ";
        $query .= "     POINT3, ";
        $query .= "     QUEST_FLAG, ";
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_QUESTION_POINT_YMST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }

/******** 更新処理  ********/
    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ENTEXAM_QUESTION_POINT_YMST ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND SUBCLASS_CD = '".$model->field["SUBCLASS_CD"]."' ";
        $db->query($query);

        $setLargeQ = $setQ = 1;
        for ($i = 1; $i <= 100; $i++) {
            //大設問番号-設問番号
            if ($setQ > 15) {
                $setQ = 1;
                $setLargeQ++;
            }
            if ($i > 60) {
                if ($setQ > 10) {
                    $setQ = 1;
                    $setLargeQ++;
                }
            }
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["SUBCLASS_CD"][TEXT]      = $model->field["SUBCLASS_CD"];
            $data["LARGE_QUESTION"][TEXT]   = $setLargeQ;
            $data["QUESTION"][TEXT]         = $setQ;
            $data["QUESTION_ORDER"][TEXT]   = $i;
            $data["PATTERN_CD"][TEXT]       = $model->field["PATTERN_CD-".$i];
            $data["ANSWER1"][TEXT]          = $model->field["ANSWER1-".$i];
            $data["POINT1"][TEXT]           = $model->field["POINT1-".$i];
            $data["ANSWER2"][TEXT]          = $model->field["ANSWER2-".$i];
            $data["POINT2"][TEXT]           = $model->field["POINT2-".$i];
            $data["ANSWER3"][TEXT]          = $model->field["ANSWER3-".$i];
            $data["POINT3"][TEXT]           = $model->field["POINT3-".$i];
            $data["QUEST_FLAG"][TEXT]       = ($model->field["QUEST_FLAG-".$i] != "") ? "1": "0";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_QUESTION_POINT_YMST");
            $db->query($query);

            $setQ++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>