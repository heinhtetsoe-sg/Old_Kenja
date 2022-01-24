<?php

require_once('for_php7.php');

class knjh020_2Query extends Query {


    //
    function getGrdCls()
    {
        $query  = "SELECT DISTINCT ";
        $query .= "       GRADE || ',' || HR_CLASS AS GC, GRADE, HR_CLASS, ";
        $query .= "       HR_NAME AS GC_J ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR = '".$this->year."' ";
        $query .= " ORDER BY GC";

        return $query;
    }

    //NAME_MSTから情報を取得
    function getNameMst_data($cd)
    {
        $query  = "SELECT * FROM V_NAME_MST ";
        $query .= "WHERE NAMECD1 = '".$cd."' AND ";
        $query .= "      YEAR = '".CTRL_YEAR."'";

        return $query;
    }

    //本人のデータ取得
    function getRow_himself($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT NAME_SHOW,BIRTHDAY FROM  SCHREG_BASE_MST ";
        $query .= " WHERE SCHREGNO ='";
        $query .= $model->schregno;
        $query .= "' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        
        return $row;
    }
    
    //連番の最大値を取得
    function getRela_Maxno($model){
        $db = Query::dbCheckOut();

        $query  = " SELECT MAX(INT(RELANO)) AS MAX FROM SCHREG_RELA_DAT ";
        $query .= " WHERE SCHREGNO ='";
        $query .= $model->schregno;
        $query .= "' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        
        return $row["MAX"];
    }

    //親族のデータ一覧を取得
    function getRow_relative_list($model)
    {
        $query  = " SELECT * FROM  SCHREG_RELA_DAT ";
        $query .= " WHERE SCHREGNO ='".$model->schregno."' ORDER BY RELANO";

        return $query;
    }

    //親族のデータを取得
    function getRow_relative($model){
        $db = Query::dbCheckOut();

        $query  = " SELECT * FROM  SCHREG_RELA_DAT ";
        $query .= " WHERE SCHREGNO ='".$model->schregno."'";
        $query .= "   AND RELANO ='".$model->relano."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        
        return $row;
    }

    //親族情報INSERT
    function &getInsertQuery($model,$relano)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["RELANO"][TEXT]           = $relano;
        $data["RELANAME"][TEXT]         = $model->field["RELANAME"];
        $data["RELAKANA"][TEXT]         = $model->field["RELAKANA"];
        $data["RELASEX"][TEXT]          = $model->field["RELASEX"];
        $data["RELABIRTHDAY"][TEXT]     = str_replace("/","-",$model->field["RELABIRTHDAY"]);
        $data["RELATIONSHIP"][TEXT]     = $model->field["RELATIONSHIP"];
        $data["RELA_SCHREGNO"][TEXT]    = $model->field["RELA_SCHREGNO"];
        $data["OCCUPATION"][TEXT]       = $model->field["OCCUPATION"];
        $data["REGIDENTIALCD"][TEXT]    = $model->field["REGIDENTIALCD"];
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        
        $query = Query::insertSQL($data, "SCHREG_RELA_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);   
        $model->relano = $relano;

        return true;
    }

    //親族情報UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["RELANAME"][TEXT]         = $model->field["RELANAME"];
        $data["RELAKANA"][TEXT]         = $model->field["RELAKANA"];
        $data["RELASEX"][TEXT]          = $model->field["RELASEX"];
        $data["RELABIRTHDAY"][TEXT]     = str_replace("/","-",$model->field["RELABIRTHDAY"]);
        $data["RELATIONSHIP"][TEXT]     = $model->field["RELATIONSHIP"];
        $data["RELA_SCHREGNO"][TEXT]    = $model->field["RELA_SCHREGNO"];
        $data["OCCUPATION"][TEXT]       = $model->field["OCCUPATION"];
        $data["REGIDENTIALCD"][TEXT]    = $model->field["REGIDENTIALCD"];
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        
        $where  = " WHERE SCHREGNO = '".$model->schregno."' ";
        $where .= " AND RELANO = '".$model->relano."' ";
        
        $query = Query::updateSQL($data, "SCHREG_RELA_DAT", $where);
        $db->query($query);

        $db->commit(); //コミットする。
        Query::dbCheckIn($db);   

        return true;
    }

    //delete
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SCHREG_RELA_DAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."' ";
        $query .= " AND RELANO = '".$model->relano."' ";
        
        $db->query($query);
        $db->commit(); //コミットする。
        Query::dbCheckIn($db);   

        return true;
    }
    //親族データを挿入
    function add_Schreg_Rela_Dat($model){
        
        $db = Query::dbCheckOut();

        $query = "SELECT RELANO FROM SCHREG_RELA_DAT WHERE SCHREGNO = '".$model->schregno. "' ORDER BY RELANO DESC";
        $max = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        $db->autoCommit(false);
        $query  = "SELECT * FROM SCHREG_RELA_DAT WHERE SCHREGNO = '".$model->stucd."' ";
        $result = $db->query($query);

        $ret = false;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $max["RELANO"]++;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["RELANO"][TEXT]           = $max["RELANO"];
            $data["RELANAME"][TEXT]         = $row["RELANAME"];
            $data["RELAKANA"][TEXT]         = $row["RELAKANA"];
            $data["RELASEX"][TEXT]          = $row["RELASEX"];
            $data["RELABIRTHDAY"][TEXT]     = $row["RELABIRTHDAY"];
            $data["RELATIONSHIP"][TEXT]     = $row["RELATIONSHIP"];
            $data["RELA_SCHREGNO"][TEXT]    = $model->stucd;
            $data["OCCUPATION"][TEXT]       = $row["OCCUPATION"];
            $data["REGIDENTIALCD"][TEXT]    = $row["REGIDENTIALCD"];
            $data["REMARK"][TEXT]           = $row["REMARK"];
            $data["REGISTERCD"][TEXT]       = $row["REGISTERCD"];
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_RELA_DAT");
            $db->query($query);
            $ret = true;
        }
        $db->commit();
        Query::dbCheckIn($db);   

        return $ret;
    }
}
?>
