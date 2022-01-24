<?php

require_once('for_php7.php');

class knjz292Query extends Query {

    //教育委員会用学校コード取得
    function getEdboardSchoolcd() {
        $query  = " SELECT ";
        $query .= "     KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //設定している学校コード取得
    function getSchoolcd() {
        $query  = " SELECT ";
        $query .= "     SCHOOL_REMARK1 AS FINSCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_SEQ = '008' ";

        return $query;
    }

    //リスト取得
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T1.STAFFNAME_SHOW, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST T1";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD";

        return $query;
    }

    //１レコード取得(県側職員異動テーブル)
    function getRow($model, $staffcd) {
        
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.IDOU_DIV, ";
        $query .= "     T1.IDOU_DATE, ";
        $query .= "     T1.ASSIGNMENT_DATE, ";
        //$query .= "     T1.FROM_FINSCHOOLCD, ";
        $query .= "     T1.TO_FINSCHOOLCD || '-' || T1.TO_EDBOARD_SCHOOLCD AS TO_FINSCHOOLCD, ";
        $query .= "     L1.EDBOARD_SCHOOLNAME AS TO_EDBOARD_SCHOOLNAME, ";
        //$query .= "     T1.FROM_EDBOARD_SCHOOLCD, ";
        //$query .= "     T1.TO_EDBOARD_SCHOOLCD, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     EDBOARD_STAFF_IDOU_DAT T1";
        $query .= "     LEFT JOIN EDBOARD_SCHOOL_MST L1 ON L1.EDBOARD_SCHOOLCD = T1.TO_EDBOARD_SCHOOLCD";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."'";
        $query .= " AND T1.STAFFCD = '".$staffcd."'";

        return $query;
    }

    //県側管理の学校コード
    function getCheckSchoolcd($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->edboard_schoolcd."' ";
        $query .= " AND REMARK = '".$model->schoolcd."' ";

        return $query;
    }

    //職員名取得
    function getStaffName($staffcd) {
        $query  = " SELECT ";
        $query .= "     STAFFNAME ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$staffcd."' ";

        return $query;
    }

    //異動区分取得
    function getIdoudiv() {
        $query  = " SELECT ";
        $query .= "     '2' AS VALUE, ";
        $query .= "     '2' || '  ' || '転出' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '3' AS VALUE, ";
        $query .= "     '3' || '  ' || '退職' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //県側管理の学校コード
    function getToFinschoolcd() {
        $query  = " SELECT ";
        $query .= "     REMARK || '-' || EDBOARD_SCHOOLCD AS VALUE, ";
        $query .= "     REMARK || ':' || EDBOARD_SCHOOLNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     REMARK IS NOT NULL ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '9999999-9999' AS VALUE, ";
        $query .= "     'その他' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //異動職員存在チェック
    function getEdboardStaffIdouCnt($staffcd) {
        $query  = " SELECT "; 
        $query .= "     COUNT(*) AS CNT "; 
        $query .= " FROM "; 
        $query .= "     EDBOARD_STAFF_IDOU_DAT "; 
        $query .= " WHERE "; 
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND STAFFCD = '".$staffcd."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $getCnt = $db2->getOne(knjz292Query::getEdboardStaffIdouCnt($model->staffcd));
        list($finschoolcd, $edboard_schoolcd) = explode("-", $fields["TO_FINSCHOOLCD"]);
        
        $data = array();
        $data["IDOU_DIV"][TEXT]             = $fields["IDOU_DIV"];
        $data["IDOU_DATE"][TEXT]            = str_replace("/","-",$fields["IDOU_DATE"]);
        $data["ASSIGNMENT_DATE"][TEXT]      = str_replace("/","-",$fields["ASSIGNMENT_DATE"]);
        $data["FROM_FINSCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["TO_FINSCHOOLCD"][TEXT]       = $finschoolcd;
        $data["FROM_EDBOARD_SCHOOLCD"][TEXT]= $model->edboard_schoolcd;
        $data["TO_EDBOARD_SCHOOLCD"][TEXT]  = $edboard_schoolcd;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCnt == 0) {
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["STAFFCD"][TEXT]              = $model->staffcd;
            $query = Query::insertSQL($data, "EDBOARD_STAFF_IDOU_DAT");
        } else {
            $where  = " WHERE YEAR            = '".CTRL_YEAR."' ";
            $where .= "  AND  STAFFCD         = '".$model->staffcd."' ";
            $query = Query::updateSQL($data, "EDBOARD_STAFF_IDOU_DAT", $where);
        }
        $db2->query($query);
        
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }

    //DELETE
    function &getDeleteQuery($model, $staffcd) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $db2->autoCommit(false);

        $query  = " DELETE FROM EDBOARD_STAFF_IDOU_DAT ";
        $query .= " WHERE YEAR    = '".CTRL_YEAR."' ";
        $query .= " AND   STAFFCD = '".$staffcd."' ";
        $db2->query($query);

        $db2->commit();  // トランザクションをコミットする。

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return true;
    }

}
?>
