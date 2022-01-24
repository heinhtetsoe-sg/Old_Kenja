<?php

require_once('for_php7.php');

class knjh070Query extends Query {

    //名称取得
    function getName()
    {
        $query  = "SELECT namecd1,namecd2,name1 FROM v_name_mst ";
        $query .= " WHERE year = '".CTRL_YEAR."'";
        $query .= " AND namecd1 IN ('H301','H302') ";   //,'H013'←資格取得や特技の項目
        $query .= "ORDER BY namecd1,namecd2";
        return $query;
    }

    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($schregno)
    {
        $query  ="SELECT schregno,name_show FROM schreg_base_mst ";
        $query .=" WHERE schregno = '".$schregno."'";
        return $query;
    }

    //学籍賞罰データよりデータを取得
    function selectQuery($model)
    {
        $query = " SELECT ";
        $query .= "   T1.TRAINDATE, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.PATIENTCD, ";
        $query .= "   T1.PATIENTCD || ':' || MEISYOU_GET(T1.PATIENTCD,'H301',1) AS PATIENT, ";
        $query .= "   T1.STAFFCD, ";
        $query .= "   T1.STAFFCD || ':' || T2.STAFFNAME_SHOW AS STAFFNAME_SHOW, ";
        $query .= "   T1.HOWTOTRAINCD, ";
        $query .= "   T1.HOWTOTRAINCD || ':' || MEISYOU_GET(T1.HOWTOTRAINCD,'H302',1) AS HOWTOTRAIN, ";
        $query .= "   T1.CONTENT ";
        $query .= " FROM ";
        $query .= "   SCHREG_TRAINHIST_DAT T1 LEFT OUTER JOIN V_STAFF_MST T2  ";
        $query .= "   ON T1.YEAR = T2.YEAR AND T1.STAFFCD = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR ."' AND ";
        if ($model->cmd == 'edit' || $model->cmd == 'add'){
            $query .= "   T1.SCHREGNO = '". $model->schregno ."' AND ";
            $query .= "   T1.TRAINDATE = '". str_replace("/","-",$model->traindate) ."' ";
        }else{
            $query .= "   T1.SCHREGNO = '". $model->schregno ."' ";
            $query .= " ORDER BY ";
            $query .= "   T1.TRAINDATE ";
        }

        return $query;
    }
    //対応者を取得
    function selectStaffQuery($model)
    {
        $query = " SELECT ";
        $query .= "   STAFFCD, ";
        $query .= "   STAFFCD || '　' || STAFFNAME_SHOW AS STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "   V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR ."' ";

        return $query;
    }
    // INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TRAINDATE"][TEXT]    = str_replace("/","-",$model->field["TRAINDATE"]);
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["PATIENTCD"][TEXT]    = $model->field["PATIENTCD"];
        $data["STAFFCD"][TEXT]      = $model->field["STAFFCD"];
        $data["HOWTOTRAINCD"][TEXT] = $model->field["HOWTOTRAINCD"];
        $data["CONTENT"][TEXT]      = $model->field["CONTENT"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;

        $query = Query::insertSQL($data, "SCHREG_TRAINHIST_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        
        $data["PATIENTCD"][TEXT]    = $model->field["PATIENTCD"];
        $data["STAFFCD"][TEXT]      = $model->field["STAFFCD"];
        $data["HOWTOTRAINCD"][TEXT] = $model->field["HOWTOTRAINCD"];
        $data["CONTENT"][TEXT]      = $model->field["CONTENT"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR      = '" .CTRL_YEAR. "'";
        $where .= " AND TRAINDATE   = '" .str_replace("/","-",$model->traindate)."'";
        $where .= " AND SCHREGNO    = '" .$model->schregno. "'";

        $query = Query::updateSQL($data, "SCHREG_TRAINHIST_DAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return ;
    }

    // DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query = " DELETE FROM ";
        $query .= "   SCHREG_TRAINHIST_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '" .CTRL_YEAR. "' AND ";
        $query .= "   TRAINDATE = '" .str_replace("/","-",$model->traindate)."' AND ";
        $query .= "   SCHREGNO = '" .$model->schregno. "' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
