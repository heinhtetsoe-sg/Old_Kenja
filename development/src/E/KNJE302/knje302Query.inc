<?php

require_once('for_php7.php');

class knje302Query extends Query {

    /**
     * 塾名
     */
    function getPriSchoolName($model) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD='" . $model->preischoolcd . "'";
        return $query;
    }

    /**
     * 教室名
     */
    function getPriSchoolClassName($model) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD='" . $model->preischoolcd . "'";
        $query .= " AND PRISCHOOL_CLASS_CD='" . $model->preischoolClassCd . "'";
        return $query;
    }
    
    /**
     * 塾訪問記録一覧取得
     */
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "       T1.PRISCHOOLCD ";
        $query .= "     , T1.VISIT_DATE ";
        $query .= "     , T1.SEQ ";
        $query .= "     , T1.PRISCHOOL_CLASS_CD ";
        $query .= "     , T3.PRISCHOOL_NAME ";
        $query .= "     , T1.STAFFCD ";
        $query .= "     , T2.STAFFNAME ";
        $query .= "     , T1.PRISCHOOL_STAFF ";
        $query .= "     , T1.COMMENT ";
        $query .= "     , T1.EXAM_STD_INFO ";
        $query .= "     , T1.REMARK ";
        $query .= "     , T1.REGISTERCD ";
        $query .= "     , T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_VISIT_RECORD_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST T2 ";
        $query .= "         ON T1.STAFFCD = T2.STAFFCD ";
        $query .= " LEFT JOIN PRISCHOOL_CLASS_MST T3 ";
        $query .= "      ON T1.PRISCHOOLCD = T3.PRISCHOOLCD ";
        $query .= "     AND T1.PRISCHOOL_CLASS_CD = T3.PRISCHOOL_CLASS_CD ";

        $query .= " WHERE ";
        $query .= "     T1.PRISCHOOLCD='" . $model->preischoolcd . "'";
        if ($model->preischoolClassCd) {
            $query .= " AND T1.PRISCHOOL_CLASS_CD='" . $model->preischoolClassCd . "'";
        }
        $query .= " ORDER BY ";
        $query .= "     VISIT_DATE, SEQ ";

        return $query;
    }

    /**
     * 更新確認用１行取得
     */
    function getOneRow($model) {
        $query  = " SELECT ";
        $query .= "       T1.PRISCHOOLCD ";
        $query .= "     , T1.VISIT_DATE ";
        $query .= "     , T1.SEQ ";
        $query .= "     , T1.PRISCHOOL_CLASS_CD ";
        $query .= "     , T1.STAFFCD ";
        $query .= "     , T1.PRISCHOOL_STAFF ";
        $query .= "     , T1.COMMENT ";
        $query .= "     , T1.EXAM_STD_INFO ";
        $query .= "     , T1.REMARK ";
        $query .= "     , T1.REGISTERCD ";
        $query .= "     , T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_VISIT_RECORD_DAT T1 ";
        
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '".$model->preischoolcd."' ";
        $query .= " AND VISIT_DATE = '". str_replace("/","-", $model->field["VISIT_DATE"]) ."' ";
        $query .= " AND SEQ = '". str_replace("/","-", $model->field["SEQ"]) ."' ";

        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    /*
     * 職員一覧取得
     */
    function getStaffList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFNAME AS LABEL, ";
        $query .= "     T1.STAFFCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST T1 ";
        $query .= "     LEFT JOIN STAFF_RECRUIT_DAT T2 ";
        $query .= "         ON T1.STAFFCD = T2.STAFFCD ";
        $query .= "         AND T1.YEAR = T2.YEAR ";
        $query .= "         AND T2.RECRUIT_DIV = '2' ";
        $query .= "         AND T2.PRISCHOOLCD = '".$model->preischoolcd."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE (T2.STAFFCD, 'XXXXXXXXXX') ";
        $query .= "     , T1.STAFFCD ";

        return $query;
    }

    /**
     * 教室一覧取得
     */
    function getPrischoolClasstList($model)
    {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME AS LABEL, ";
        $query .= "     PRISCHOOL_CLASS_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '".$model->preischoolcd."' ";
        $query .= " ORDER BY ";
        $query .= "     PRISCHOOL_CLASS_CD ";

        return $query;
    }

    /**
     * 最大SEQ取得
     */
    function getMaxSeq(&$model)
    {
        $query  = " SELECT VALUE(MAX(SEQ), '000') MAXSEQ ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_VISIT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '".$model->preischoolcd."' ";
        $query .= " AND VISIT_DATE = '". str_replace("/","-", $model->field["VISIT_DATE"]) ."' ";

        return $query;
    }

    /*
     * INSERT
     */
    function &getInsertQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 最大SEQ取得
        $query = knje302Query::getMaxSeq($model);
        $seq = $db->getOne($query);
        $seq = intval($seq) + 1;

        $data["PRISCHOOLCD"][TEXT] = $model->preischoolcd;
        $data["VISIT_DATE"][DATE] = str_replace("/","-", $model->field["VISIT_DATE"]);
        $data["SEQ"][TEXT] = $seq;
        $data["PRISCHOOL_CLASS_CD"][TEXT] = $model->field["PRISCHOOL_CLASS_CD"];
        $data["STAFFCD"][TEXT] = $model->field["STAFFCD"];
        $data["PRISCHOOL_STAFF"][TEXT] = $model->field["PRISCHOOL_STAFF"];
        $data["COMMENT"][TEXT] = $model->field["COMMENT"];
        $data["EXAM_STD_INFO"][TEXT] = $model->field["EXAM_STD_INFO"];
        $data["REMARK"][TEXT] = $model->field["REMARK"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "PRISCHOOL_VISIT_RECORD_DAT");
        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["PRISCHOOL_CLASS_CD"][TEXT] = $model->field["PRISCHOOL_CLASS_CD"];
        $data["STAFFCD"][TEXT] = $model->field["STAFFCD"];
        $data["PRISCHOOL_STAFF"][TEXT] = $model->field["PRISCHOOL_STAFF"];
        $data["COMMENT"][TEXT] = $model->field["COMMENT"];
        $data["EXAM_STD_INFO"][TEXT] = $model->field["EXAM_STD_INFO"];
        $data["REMARK"][TEXT] = $model->field["REMARK"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "       PRISCHOOLCD    = '".$model->preischoolcd."' ";
        $where .= "   AND VISIT_DATE = '".str_replace("/","-", $model->field["VISIT_DATE"])."' ";
        $where .= "   AND SEQ        = '".$model->field["SEQ"]."' ";

        $query = Query::updateSQL($data, "PRISCHOOL_VISIT_RECORD_DAT", $where);
        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    /**
     * DELETE
     */
    function &getDeleteQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM PRISCHOOL_VISIT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "       PRISCHOOLCD    = '".$model->preischoolcd."' ";
        $query .= "   AND VISIT_DATE = '".str_replace("/","-", $model->field["VISIT_DATE"])."' ";
        $query .= "   AND SEQ        = '".$model->field["SEQ"]."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

}
?>
