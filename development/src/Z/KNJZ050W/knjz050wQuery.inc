<?php

require_once('for_php7.php');

class knjz050wQuery extends Query {

    //権限チェック（入試管理者）
    function getAdminFlg() {
        $query  = " SELECT ";
        $query .= "     FIELD1 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "     AND STAFF_SEQ = '009' ";

        $db = Query::dbCheckOut();
        $rtnFlg = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnFlg;
    }
    
    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " WITH T_COMMON(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('".(CTRL_YEAR+1)."', '".(CTRL_YEAR+1)."') ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR AS VALUE, ";
        $query .= "     ENTEXAMYEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPORT_MST ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //左画面リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     SPORT_CD, ";
        $query .= "     SPORT_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPORT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     SPORT_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($db, $year, $sportCd) {
        $query  = " SELECT ";
        $query .= "     SPORT_CD, ";
        $query .= "     SPORT_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPORT_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '".$year."' ";
        $query .= "     AND SPORT_CD    = '".$sportCd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //ある年度にデータが存在するのかチェック
    function getCheckCourseMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPORT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //COPY
    function getCopyQuery($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_SPORT_MST ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     SPORT_CD, ";
        $query .= "     SPORT_NAME, ";
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_SPORT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT] = $fields["ENTEXAMYEAR"];
        $data["SPORT_CD"][TEXT]    = $fields["SPORT_CD"];
        $data["SPORT_NAME"][TEXT]  = $fields["SPORT_NAME"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_SPORT_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT] = $fields["ENTEXAMYEAR"];
        $data["SPORT_CD"][TEXT]    = $fields["SPORT_CD"];
        $data["SPORT_NAME"][TEXT]  = $fields["SPORT_NAME"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR = '".$fields["ENTEXAMYEAR"]."' ";
        $where .= "     AND SPORT_CD    = '".$fields["SPORT_CD"]."' ";


        $query = Query::updateSQL($data, "ENTEXAM_SPORT_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_SPORT_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '".$fields["ENTEXAMYEAR"]."' ";
        $query .= "     AND SPORT_CD    = '".$fields["SPORT_CD"]."' ";

      $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
