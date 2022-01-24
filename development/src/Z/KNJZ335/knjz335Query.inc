<?php

require_once('for_php7.php');

class knjz335Query extends Query {

    //職員
    function getStaffMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     STAFFNAME, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST T1 ";
        $query .= "     LEFT JOIN EDBOARD_STAFF_CHANGE_SCHOOL_DAT L1 ON T1.STAFFCD = L1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     STAFFNAME ";
        $query .= " ORDER BY ";
        $query .= "     CNT DESC, ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //掛持ちの学校
    function getChangeSchool($model, $staffCd)
    {
        $query  = " SELECT ";
        $query .= "     T1.EDBOARD_SCHOOLCD, ";
        $query .= "     L1.EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_STAFF_CHANGE_SCHOOL_DAT T1 ";
        $query .= "     LEFT JOIN EDBOARD_SCHOOL_MST L1 ON T1.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$staffCd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EDBOARD_SCHOOLCD ";

        return $query;
    }

    //職員名
    function getStaffName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$model->leftStaffCd."' ";

        return $query;
    }

    //学校一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EDBOARD_SCHOOLCD, ";
        $query .= "     T1.EDBOARD_SCHOOLNAME, ";
        $query .= "     L1.STAFFCD ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN EDBOARD_STAFF_CHANGE_SCHOOL_DAT L1 ON L1.STAFFCD = '".$model->leftStaffCd."' ";
        $query .= "          AND T1.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.EDBOARD_SCHOOLCD ";
        return $query;
    }

    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     EDBOARD_STAFF_CHANGE_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->leftStaffCd."' ";
        $db->query($query);

        $data = array();
        $selectdata = explode(",", $model->selectdata);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $data["STAFFCD"][TEXT]          = $model->leftStaffCd;
            $data["EDBOARD_SCHOOLCD"][TEXT] = $selectdata[$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            $query = Query::insertSQL($data, "EDBOARD_STAFF_CHANGE_SCHOOL_DAT");

            $db->query($query);
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
}
?>
