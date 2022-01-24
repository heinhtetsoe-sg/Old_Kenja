<?php

require_once('for_php7.php');

class knjg041Query extends Query {

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
        $query .= "     APPLYDAY, ";
        $query .= "     APPLYCD, ";
        $query .= "     PERM_CD, ";
        $query .= "     CASE WHEN APPLYCD BETWEEN '3' AND '4' THEN COALESCE(GUIDE,'') || '  ' || COALESCE(BUSINESSTRIP,'') ";
        $query .= "          ELSE COALESCE(VACATIONREASON,'') END AS REASON,";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     PERMREQUEST_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= " ORDER BY ";
        $query .= "     APPLYDAY DESC, ";
        $query .= "     APPLYCD, ";
        $query .= "     PERM_CD ";

        return $query;
    }

    function getRow_data($field, $staffcd) {
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
        $query .= "     STAFFCD     = '".STAFFCD."' AND ";
        $query .= "     SDATE       = '".str_replace("/","-",$field["SDATE"])."' AND ";
        $query .= "     EDATE       = '".str_replace("/","-",$field["EDATE"])."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data["APPLYDAY"][TEXT]             = str_replace("/","-",$model->field["APPLYDAY"]);
        $data["APPLYCD"][TEXT]              = $model->field["APPLYCD"];
        $data["STAFFCD"][TEXT]              = STAFFCD;
        $data["SDATE"][TEXT]                = str_replace("/","-",$model->field["SDATE"]);
        $data["EDATE"][TEXT]                = str_replace("/","-",$model->field["EDATE"]);
        $data["HOURS"][TEXT]                = $model->field["HOURS"];
        $data["MINUTES"][TEXT]              = $model->field["MINUTES"];
        $data["VACATION"][TEXT]             = str_replace("/","-",$model->field["VACATION"]);
        $data["VACATIONREASON"][TEXT]       = $model->field["VACATIONREASON"];
        $data["GUIDE"][TEXT]                = $model->field["GUIDE"];
        $data["GUIDE_NUM"][NUMBER]          = $model->field["GUIDE_NUM"];
        $data["BUSINESSTRIP"][TEXT]         = $model->field["BUSINESSTRIP"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["CALL_NAME"][TEXT]            = $model->field["CALL_NAME"];
        $data["CALL_TELNO"][TEXT]           = $model->field["CALL_TELNO"];

        $data["PERM_CD"][TEXT]              = 0;    //申請中
        $data["PERM_STAFFCD"][TEXT]         = NULL;

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "PERMREQUEST_DAT");
        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * DELETE
     */
    function &getDeleteQuery($model) {
     $db = Query::dbCheckOut();

        $query  = " DELETE FROM PERMREQUEST_DAT ";
        $query .= " WHERE APPLYDAY = '".str_replace("/","-",$model->field["APPLYDAY"])."' ";
        $query .= " AND   APPLYCD  = '".$model->field["APPLYCD"]."' ";
        $query .= " AND   STAFFCD  = '".STAFFCD."' ";
        $query .= " AND   SDATE    = '".str_replace("/","-",$model->field["SDATE"])."' ";
        $query .= " AND   EDATE    = '".str_replace("/","-",$model->field["EDATE"])."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data["APPLYDAY"][TEXT]             = str_replace("/","-",$model->field["APPLYDAY"]);
        $data["APPLYCD"][TEXT]              = $model->field["APPLYCD"];
        $data["STAFFCD"][TEXT]              = STAFFCD;
        $data["SDATE"][TEXT]                = str_replace("/","-",$model->field["SDATE"]);
        $data["EDATE"][TEXT]                = str_replace("/","-",$model->field["EDATE"]);
        $data["HOURS"][TEXT]                = $model->field["HOURS"];
        $data["MINUTES"][TEXT]              = $model->field["MINUTES"];
        $data["VACATION"][TEXT]             = str_replace("/","-",$model->field["VACATION"]);
        $data["VACATIONREASON"][TEXT]       = $model->field["VACATIONREASON"];
        $data["GUIDE"][TEXT]                = $model->field["GUIDE"];
        $data["GUIDE_NUM"][NUMBER]          = $model->field["GUIDE_NUM"];
        $data["BUSINESSTRIP"][TEXT]         = $model->field["BUSINESSTRIP"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["CALL_NAME"][TEXT]            = $model->field["CALL_NAME"];
        $data["CALL_TELNO"][TEXT]           = $model->field["CALL_TELNO"];
        $data["PERM_CD"][TEXT]              = $model->field["PERM_CD"];
        $data["PERM_STAFFCD"][TEXT]         = NULL;

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE APPLYDAY = '".str_replace("/","-",$model->backup_dt["APPLYDAY"])."' ";
        $where .= " AND   APPLYCD  = '".$model->backup_dt["APPLYCD"]."' ";
        $where .= " AND   STAFFCD  = '".STAFFCD."' ";
        $where .= " AND   SDATE    = '".str_replace("/","-",$model->backup_dt["SDATE"])."' ";
        $where .= " AND   EDATE    = '".str_replace("/","-",$model->backup_dt["EDATE"])."' ";

        $query = Query::updateSQL($data, "PERMREQUEST_DAT",$where);
        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }
}
?>
