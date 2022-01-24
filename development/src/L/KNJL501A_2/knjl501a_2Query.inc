<?php

require_once('for_php7.php');

class knjl501a_2Query extends Query {
    //左フレームのリスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "   T1.HOPE_COURSECODE, ";  // 志望区分コード
        $query .= "   T1.HOPE_NAME,       ";  // 志望区分名称
        $query .= "   T1.COURSECODE,      ";  // 学籍コースコード
        $query .= "   T3.COURSECODENAME,  ";  // 学籍コース名称
        $query .= "   T1.NOTICE_NAME,     ";  // 通知用名称
        $query .= "   T1.PASS_NAME,       ";  // コース合格名称
        $query .= "   T1.NOT_PASS_NAME    ";  // コース不合格名称
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST T1 ";
        $query .= "   LEFT JOIN COURSECODE_MST T3 ";
        $query .= "     ON T3.COURSECODE = T1.COURSECODE ";
        $query .= " ORDER BY ";
        $query .= "  T1.HOPE_COURSECODE ";

        return $query;
    }

    function getCourseCodeList($model) {
        $query  = " SELECT ";
        $query .= "   T1.COURSECODE || ':' || T1.COURSECODENAME AS LABEL, ";  // 学籍コースコード
        $query .= "   T1.COURSECODE AS VALUE  ";  // 学籍コース名称
        $query .= " FROM ";
        $query .= "   COURSECODE_MST T1 ";
        $query .= "   INNER JOIN COURSECODE_YDAT T2 ";
        $query .= "     ON T2.YEAR = {$model->examyear}";
        $query .= "    AND T2.COURSECODE = T1.COURSECODE ";
        return $query;
    }

    //ENTEXAM_HOPE_COURSE_MST から１レコード取得
    function getHopeCourseDat($model) {
        $query  = " SELECT ";
        $query .= "   T1.HOPE_COURSECODE, ";  // 志望区分コード
        $query .= "   T1.HOPE_NAME,       ";  // 志望区分名称
        $query .= "   T1.COURSECODE,      ";  // 学籍コースコード
        $query .= "   T1.NOTICE_NAME,     ";  // 通知用名称
        $query .= "   T1.PASS_NAME,       ";  // コース合格名称
        $query .= "   T1.NOT_PASS_NAME    ";  // コース不合格名称
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "   T1.HOPE_COURSECODE = '{$model->field["HOPE_COURSECODE"]}' ";

        return $query;
    }

    function getUseYearHopeCourseYDat($model) {
        $query  = " SELECT ";
        $query .= "  COUNT(ENTEXAMYEAR) AS CNT";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "   HOPE_COURSECODE = '{$model->field["HOPE_COURSECODE"]}' ";
        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    //ENTEXAM_HOPE_COURSE_MST を更新
    function &getInsertMstQuery($model) {
        $db = Query::dbCheckOut();

        $data["HOPE_COURSECODE"][TEXT] = $model->field["HOPE_COURSECODE"];
        $data["HOPE_NAME"][TEXT]       = $model->field["HOPE_NAME"];
        $data["COURSECODE"][TEXT]      = $model->field["COURSECODE"];
        $data["NOTICE_NAME"][TEXT]     = $model->field["NOTICE_NAME"];
        $data["PASS_NAME"][TEXT]       = $model->field["PASS_NAME"];
        $data["NOT_PASS_NAME"][TEXT]   = $model->field["NOT_PASS_NAME"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][NUMBER]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_HOPE_COURSE_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    //ENTEXAM_HOPE_COURSE_MST を更新
    function &getUpdateMstQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["HOPE_NAME"][TEXT]   = $model->field["HOPE_NAME"];
        $data["COURSECODE"][TEXT]    = $model->field["COURSECODE"];
        $data["NOTICE_NAME"][TEXT]   = $model->field["NOTICE_NAME"];
        $data["PASS_NAME"][TEXT]     = $model->field["PASS_NAME"];
        $data["NOT_PASS_NAME"][TEXT] = $model->field["NOT_PASS_NAME"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        $where  = " WHERE ";
        $where .= "     HOPE_COURSECODE = '{$model->field["HOPE_COURSECODE"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_HOPE_COURSE_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_HOPE_COURSE_DAT から削除
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_HOPE_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     HOPE_COURSECODE = '{$model->field["HOPE_COURSECODE"]}' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
