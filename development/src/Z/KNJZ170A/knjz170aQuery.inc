<?php

require_once('for_php7.php');

class knjz170aQuery extends Query {

    //年度
    function getYear($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        
        return $query;
    }

    //全体の一覧の取得（左画面）
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ELECTCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->selectYear."' ";
        $query .= " ORDER BY ";
        $query .= "     GROUPCD ";
        
        return $query;
    }

    //１レコード取得
    function getRow($cd, $model)
    {
        if ($cd != "")
        {
            $db    = Query::dbCheckOut();
            $query = "SELECT * FROM electclass_dat WHERE YEAR =  '".$model->selectYear."' AND GROUPCD = '" .sprintf("%04d",$cd)."'";
            $row   = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            return $row;
        }
        return;
    }
    
    //ログイン年度データカウント
    function cntThisOrder($model) {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ELECTCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->selectYear."' ";
        
        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);
        
        return $data_check;
    }
    
    //前年度データカウント
    function cntLastOrder($model) {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ELECTCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->selectYear - 1)."' ";
        
        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);
        
        return $data_check;
    }
    
    //前年度からコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ELECTCLASS_DAT ";
        $query .= "           ( YEAR, ";
        $query .= "             GROUPCD, ";
        $query .= "             GROUPNAME, ";
        $query .= "             GROUPABBV, ";
        $query .= "             REMARK, ";
        $query .= "             SHOWORDER, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->selectYear."', ";
        $query .= "         GROUPCD, ";
        $query .= "         GROUPNAME, ";
        $query .= "         GROUPABBV, ";
        $query .= "         REMARK, ";
        $query .= "         SHOWORDER, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         ELECTCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".($model->selectYear - 1)."' ";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        
        return true;
    }

    //--- INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        
        $data["YEAR"][TEXT]          = $model->selectYear;
        $data["GROUPCD"][TEXT]       = sprintf("%04d",$fields["GROUPCD"]);
        $data["GROUPNAME"][TEXT]     = $fields["GROUPNAME"];
        $data["GROUPABBV"][TEXT]     = $fields["GROUPABBV"];
        $data["REMARK"][TEXT]        = $fields["REMARK"];
        $data["SHOWORDER"][NUMBER]   = $fields["SHOWORDER"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;        
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $query = Query::insertSQL($data, "electclass_dat");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($model, $fields)
    {        
        $db = Query::dbCheckOut();
        
        $data["GROUPNAME"][TEXT]     = $fields["GROUPNAME"];
        $data["GROUPABBV"][TEXT]     = $fields["GROUPABBV"];
        $data["REMARK"][TEXT]        = $fields["REMARK"];
        $data["SHOWORDER"][NUMBER]   = $fields["SHOWORDER"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $where  = " WHERE YEAR    = '".$model->selectYear."' ";
        $where .= " AND   GROUPCD = '" .sprintf("%04d",$fields["GROUPCD"])."'";
        $query  = Query::updateSQL($data, "electclass_dat", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM electclass_dat "
                ." WHERE YEAR =  '".$model->selectYear."' AND GROUPCD  = '" .sprintf("%04d",$fields["GROUPCD"])."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
