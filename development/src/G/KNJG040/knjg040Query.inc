<?php

require_once('for_php7.php');

class knjg040Query extends Query {

    //リスト内情報取得
    function ReadQuery($model)
    {
        switch($model->sec_competence){
            case DEF_UPDATABLE:        //更新可
            case DEF_REFERABLE:        //参照
                $where = "";
                break;
#            case DEF_REFER_RESTRICT:   //制限付き参照
            case DEF_UPDATE_RESTRICT:  //制限付き更新可
                $where = " AND T1.STAFFCD = '".$model->staffcd."' ";
                break;
            case DEF_NOAUTH:           //権限なし
            default:
                return "SELECT T1.APPLYDAY  FROM PERMREQUEST_DAT T1 WHERE T1.APPLYCD = '' ";
                break;
        }

        $query  = " SELECT ";
        $query .= " T1.APPLYDAY,T1.STAFFCD,T2.STAFFNAME_SHOW,T2.STAFFNAME_KANA,";
        $query .= " CASE WHEN T1.APPLYCD BETWEEN '3' AND '4'  THEN COALESCE(T1.GUIDE,'') || '  ' || COALESCE(T1.BUSINESSTRIP,'')  ELSE COALESCE(T1.VACATIONREASON,'') END AS REASON,";
        $query .= " T1.PERM_CD,T1.SDATE,T1.EDATE,T1.APPLYCD ";
        $query .= " FROM PERMREQUEST_DAT T1 ";
        //年度を条件にしないで申請済みを全て表示する
        //$query .= " LEFT JOIN V_STAFF_MST T2 ON T1.STAFFCD = T2.STAFFCD AND T2.YEAR = '".CTRL_YEAR."'";
        $query .= " LEFT JOIN STAFF_MST T2 ON T1.STAFFCD = T2.STAFFCD";
        $query .= " WHERE T1.APPLYCD = '";
        $query .=  $model->apply_div;
        $query .=  "' ";

        $query .= " AND T1.PERM_CD = '";
        $query .=  $model->perm_div;
        $query .=  "' ";


        $query .= $where;
        $query .= " ORDER BY T1.PERM_CD, T1.APPLYDAY DESC, T2.STAFFNAME_KANA";

        //echo $query;
        return $query;
    }

    function getRow_user($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT T1.STAFFNAME,T2.SECTIONNAME,T3.JOBNAME,W.STAFFCD ";
        //$query .= "   FROM PERMREQUEST_DAT W, V_STAFF_MST T1 ";
        $query .= "   FROM PERMREQUEST_DAT W, STAFF_MST T1 ";
        $query .= "   LEFT JOIN SECTION_MST T2 ON  T1.SECTIONCD = T2.SECTIONCD ";
        $query .= "   LEFT JOIN JOB_MST T3 ON T1.JOBCD = T3.JOBCD ";
        $query .= "WHERE W.STAFFCD = T1.STAFFCD";
        $query .= "  AND T1.STAFFCD    = '". $model->field["STAFFCD"] . "'";
        //$query .= "    AND T1.YEAR       = '".CTRL_YEAR."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

#    function getRow_login_user($model)
#    {
#        $db = Query::dbCheckOut();
#        $query  = " SELECT T1.STAFFNAME,T2.SECTIONNAME,T3.JOBNAME ";
#        $query .= "   FROM V_STAFF_MST T1 ";
#        $query .= "   LEFT JOIN SECTION_MST T2 ON  T1.SECTIONCD = T2.SECTIONCD ";
#        $query .= "   LEFT JOIN JOB_MST T3 ON T1.JOBCD = T3.JOBCD ";
#        $query .= "  WHERE T1.STAFFCD   = '". $model->staffcd . "'";
#        $query .= "    AND T1.YEAR      = '".CTRL_YEAR."'";
#
#        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
#        Query::dbCheckIn($db);
#        return $row;
#    }

    function getRow_data($field,$staffcd)
    {
        if(!isset($field["APPLYDAY"]) || !isset($field["SDATE"]) || !isset($field["EDATE"]))
        {
            return $row;
        }

           $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query  .= " FROM PERMREQUEST_DAT";
        $query  .= " WHERE APPLYDAY = '";
        $query  .= str_replace("/","-",$field["APPLYDAY"]);
        $query  .= "' AND APPLYCD = '";
        $query  .= $field["APPLYCD"];
        $query  .= "' AND STAFFCD = '";
        $query  .= $field["STAFFCD"];
        $query  .= "' AND   SDATE = '";
        $query  .= str_replace("/","-",$field["SDATE"]);
        $query  .= "' AND   EDATE = '";
        $query  .= str_replace("/","-",$field["EDATE"]);
        $query  .= "' ";

        //echo $query;
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;

    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["APPLYDAY"][TEXT]            = str_replace("/","-",$model->field["APPLYDAY"]);
        $data["APPLYCD"][TEXT]             = $model->field["APPLYCD"];
        $data["STAFFCD"][TEXT]             = $model->staffcd;
        $data["SDATE"][TEXT]               = str_replace("/","-",$model->field["SDATE"]);
        $data["EDATE"][TEXT]               = str_replace("/","-",$model->field["EDATE"]);
        $data["HOURS"][TEXT]               = $model->field["HOURS"];
        $data["MINUTES"][TEXT]             = $model->field["MINUTES"];
        $data["VACATION"][TEXT]            = str_replace("/","-",$model->field["VACATION"]);
        $data["VACATIONREASON"][TEXT]      = $model->field["VACATIONREASON"];
        $data["GUIDE"][TEXT]               = $model->field["GUIDE"];
        $data["GUIDE_NUM"][NUMBER]         = $model->field["GUIDE_NUM"];
        $data["BUSINESSTRIP"][TEXT]        = $model->field["BUSINESSTRIP"];
        $data["REMARK"][TEXT]              = $model->field["REMARK"];
        $data["CALL_NAME"][TEXT]           = $model->field["CALL_NAME"];
        $data["CALL_TELNO"][TEXT]          = $model->field["CALL_TELNO"];

        if($model->sec_competence == DEF_UPDATABLE){
                $data["PERM_CD"][TEXT]  = $model->field["PERM_CD"];
            if($model->field["PERM_CD"] == "0"){
                $data["PERM_STAFFCD"][TEXT] = NULL;
            }
            elseif($model->field["PERM_CD"] <> "0"){
                $data["PERM_STAFFCD"][TEXT] = STAFFCD;
            }
        }
        elseif($model->sec_competence == DEF_UPDATE_RESTRICT){
            $data["PERM_CD"][TEXT]      = 0;
            $data["PERM_STAFFCD"][TEXT] = NULL;
        }

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "PERMREQUEST_DAT");
        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * DELETE
     */
    function &getDeleteQuery($model)
    {
     $db = Query::dbCheckOut();

        $query  = " DELETE FROM PERMREQUEST_DAT ";
        $query .= " WHERE APPLYDAY = '".str_replace("/","-",$model->field["APPLYDAY"])."' ";
        $query .= " AND   APPLYCD  = '".$model->field["APPLYCD"]."' ";
        $query .= " AND   STAFFCD  = '".$model->field["STAFFCD"]."' ";
        $query .= " AND   SDATE    = '".str_replace("/","-",$model->field["SDATE"])."' ";
        $query .= " AND   EDATE    = '".str_replace("/","-",$model->field["EDATE"])."' ";

        $db->query($query);
        //echo $query;
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["APPLYDAY"][TEXT]            = str_replace("/","-",$model->field["APPLYDAY"]);
        $data["APPLYCD"][TEXT]             = $model->field["APPLYCD"];
        $data["STAFFCD"][TEXT]             = $model->field["STAFFCD"];
        $data["SDATE"][TEXT]               = str_replace("/","-",$model->field["SDATE"]);
        $data["EDATE"][TEXT]               = str_replace("/","-",$model->field["EDATE"]);
        $data["HOURS"][TEXT]               = $model->field["HOURS"];
        $data["MINUTES"][TEXT]             = $model->field["MINUTES"];
        $data["VACATION"][TEXT]            = str_replace("/","-",$model->field["VACATION"]);
        $data["VACATIONREASON"][TEXT]      = $model->field["VACATIONREASON"];
        $data["GUIDE"][TEXT]               = $model->field["GUIDE"];
        $data["GUIDE_NUM"][NUMBER]         = $model->field["GUIDE_NUM"];
        $data["BUSINESSTRIP"][TEXT]        = $model->field["BUSINESSTRIP"];
        $data["REMARK"][TEXT]              = $model->field["REMARK"];
        $data["CALL_NAME"][TEXT]           = $model->field["CALL_NAME"];
        $data["CALL_TELNO"][TEXT]          = $model->field["CALL_TELNO"];
        $data["PERM_CD"][TEXT]             = $model->field["PERM_CD"];

        if($model->field["PERM_CD"] == "0")
        {
            $data["PERM_STAFFCD"][TEXT]    = NULL;
        }
        elseif($model->field["PERM_CD"] <> "0")
        {
            $data["PERM_STAFFCD"][TEXT]    = STAFFCD;
        }

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $where  = " WHERE APPLYDAY = '".str_replace("/","-",$model->backup_dt["APPLYDAY"])."' ";
        $where .= " AND   APPLYCD  = '".$model->backup_dt["APPLYCD"]."' ";
        $where .= " AND   STAFFCD  = '".$model->backup_dt["STAFFCD"]."' ";
        $where .= " AND   SDATE    = '".str_replace("/","-",$model->backup_dt["SDATE"])."' ";
        $where .= " AND   EDATE    = '".str_replace("/","-",$model->backup_dt["EDATE"])."' ";

        $query = Query::updateSQL($data, "PERMREQUEST_DAT",$where);
        //echo $query;
        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }
}
?>
