<?php

require_once('for_php7.php');

class knjz407Query extends Query {

    //左フレームのリスト
    function getList() {
        return knjz407Query::getBaseRemarkMst("", true);
    }

    //BASE_REMARK_MST,BASE_REMARK_DETAIL_MSTからレコード取得
    function getBaseRemarkMst($no, $noSetWhere = false) {
        $query  = " WITH BASE_CNT_MST AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.CODE, ";
        $query .= "   COUNT(CODE) AS CNT ";
        $query .= " FROM ";
        $query .= "   BASE_REMARK_DETAIL_MST T1 ";
        $query .= " GROUP BY ";
        $query .= "   T1.CODE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "  T1.CODE, ";
        $query .= "  T1.NAME AS NAME1, ";
        $query .= "  T2.SEQ, ";
        $query .= "  T2.NAME AS NAME2, ";
        $query .= "  T3.CNT AS RECCNT, ";
        $query .= "  T2.QUESTION_CONTENTS, ";
        $query .= "  T2.ANSWER_PATTERN, ";
        $query .= "  T2.ANSWER_SELECT_COUNT ";
        $query .= " FROM ";
        $query .= "   BASE_REMARK_MST T1 ";
        $query .= "   LEFT JOIN BASE_REMARK_DETAIL_MST T2 ";
        $query .= "     ON T2.CODE = T1.CODE ";
        $query .= "   LEFT JOIN BASE_CNT_MST T3 ";
        $query .= "     ON T3.CODE = T1.CODE ";
        if (!$noSetWhere) {
            $query .= " WHERE ";
            $query .= "   T1.CODE = '".$no."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   T1.CODE, ";
        $query .= "   T2.SEQ ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["CODE"][TEXT]         = $model->field["CODE"];
        $data["NAME"][TEXT]         = $model->field["NAME1"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "BASE_REMARK_MST");
        $db->query($query);

        knjz407Query::insertDetailQuery($db, $model);
        Query::dbCheckIn($db);
    }

    function insertDetailQuery($db, $model) {
        foreach ($model->detailRemark as $dremark) {
            $data = array();
            $data["CODE"][TEXT]               = $model->field["CODE"];
            $data["SEQ"][TEXT]                = $dremark["SEQ"];
            $data["NAME"][TEXT]               = $dremark["NAME2"];
            $data["QUESTION_CONTENTS"][TEXT]  = $dremark["QUESTION_CONTENTS"];
            $data["ANSWER_PATTERN"][TEXT]      = $dremark["ANSWER_PATTERN"];
            $data["ANSWER_SELECT_COUNT"][TEXT] = $dremark["ANSWER_SELECT_COUNT"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $query = Query::insertSQL($data, "BASE_REMARK_DETAIL_MST");
            $db->query($query);
        }
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["NAME"][TEXT]        = $model->field["NAME1"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     CODE = '".$model->field["CODE"]."' ";

        $query = Query::updateSQL($data, "BASE_REMARK_MST", $where);
        $db->query($query);

        $query = knjz407Query::getBaseRemarkMst($model->field["CODE"]);
        $oneRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($oneRow["RECCNT"] > 0) {
            knjz407Query::deleteDetailQuery($db, $model);
        }
        knjz407Query::insertDetailQuery($db, $model);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     BASE_REMARK_MST ";
        $query .= " WHERE ";
        $query .= "     CODE = '".$model->field["CODE"]."' ";

        $db->query($query);

        knjz407Query::deleteDetailQuery($db, $model);
        Query::dbCheckIn($db);
    }

    function deleteDetailQuery($db, $model) {
        $query  = " DELETE FROM ";
        $query .= "     BASE_REMARK_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     CODE = '".$model->field["CODE"]."' ";
        $db->query($query);
    }
}
?>
