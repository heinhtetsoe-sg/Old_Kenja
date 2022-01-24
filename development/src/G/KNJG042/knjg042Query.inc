<?php

require_once('for_php7.php');

class knjg042Query extends Query {

    //職員情報取得
    function getStaffInfo($model) {

        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T2.SECTIONNAME, ";
        $query .= "     T3.JOBNAME ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1 ";
        $query .= "     LEFT JOIN SECTION_MST T2 ON  T1.SECTIONCD = T2.SECTIONCD ";
        $query .= "     LEFT JOIN JOB_MST T3 ON T1.JOBCD = T3.JOBCD ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD  = '".STAFFCD."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $namecd2="") {

        $query  = " SELECT ";
        $query .= "     ABBV1, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト取得
    function getList($model) {

        $query  = " SELECT ";
        $query .= "     T1.APPLYDAY, ";
        $query .= "     T1.APPLYCD, ";
        $query .= "     T1.PERM_CD, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T2.STAFFNAME, ";
        $query .= "     CASE WHEN T1.APPLYCD BETWEEN '3' AND '4' THEN COALESCE(T1.GUIDE,'') || '  ' || COALESCE(T1.BUSINESSTRIP,'') ";
        $query .= "          ELSE COALESCE(T1.VACATIONREASON,'') END AS REASON,";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE ";
        $query .= " FROM ";
        $query .= "     PERMREQUEST_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST T2 ON T1.STAFFCD = T2.STAFFCD ";
        $query .= " WHERE ";
        if ($model->apply_div != "") {
            $query .= "     T1.APPLYCD = '".$model->apply_div."' AND ";
        }
        $query .= "     T1.PERM_CD = '".$model->perm_div."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.APPLYDAY DESC, ";
        $query .= "     T1.APPLYCD, ";
        $query .= "     T1.PERM_CD, ";
        $query .= "     T1.STAFFCD ";

        return $query;
    }

    function getRow_data($field) {
        if (!isset($field["APPLYDAY"]) || !isset($field["SDATE"]) || !isset($field["EDATE"])) {
            return $row;
        }

        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PERMREQUEST_DAT ";
        $query .= " WHERE ";
        $query .= "     APPLYDAY    = '".str_replace("/","-",$field["APPLYDAY"])."' AND ";
        $query .= "     APPLYCD     = '".$field["APPLYCD"]."' AND ";
        $query .= "     STAFFCD     = '".$field["STAFFCD"]."' AND ";
        $query .= "     SDATE       = '".str_replace("/","-",$field["SDATE"])."' AND ";
        $query .= "     EDATE       = '".str_replace("/","-",$field["EDATE"])."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $perm_cd = substr($model->cmd,6,1);

        $data["PERM_CD"][TEXT]          = $perm_cd;
        if ($perm_cd == "0") {
            $data["PERM_STAFFCD"][TEXT]     = NULL;
        } else {
            $data["PERM_STAFFCD"][TEXT]     = STAFFCD;
        }

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE APPLYDAY = '".str_replace("/","-",$model->backup_dt["APPLYDAY"])."' ";
        $where .= " AND   APPLYCD  = '".$model->backup_dt["APPLYCD"]."' ";
        $where .= " AND   STAFFCD  = '".$model->backup_dt["STAFFCD"]."' ";
        $where .= " AND   SDATE    = '".str_replace("/","-",$model->backup_dt["SDATE"])."' ";
        $where .= " AND   EDATE    = '".str_replace("/","-",$model->backup_dt["EDATE"])."' ";

        $query = Query::updateSQL($data, "PERMREQUEST_DAT",$where);
        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }
}
?>
