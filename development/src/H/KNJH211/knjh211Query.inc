<?php

require_once('for_php7.php');

class knjh211Query extends Query
{

    //学籍基礎マスタより学籍番号と名前を取得
    public function getSchregno_name($schregno)
    {
        $query  ="SELECT schregno,name_show as name FROM schreg_base_mst";
        $query .=" WHERE schregno = '".$schregno."'";

        return $query;
    }

    //寮履歴データよりデータを取得
    public function getAward($model, $year, $schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= " T3.DOMI_CD, ";
        $query .= " T1.DOMI_CD || '　' || T1.DOMI_NAME AS DOMI_NAME, ";
        $query .= " T3.DOMI_ENTDAY, ";
        $query .= " T3.DOMI_OUTDAY ";
        $query .= " FROM ";
        $query .= " ( ";
        $query .= "    SELECT * ";
        $query .= "    FROM  SCHREG_DOMITORY_HIST_DAT ";
        $query .= "    WHERE SCHREGNO   = '".$schregno."' ";
        $query .= " ) T3 ";
        $query .= " INNER JOIN ( ";
        $query .= "    SELECT DOMI_CD ";
        $query .= "    FROM  DOMITORY_YDAT ";
        $query .= "    WHERE YEAR    = '".$year."'";
        $query .= " ) T2 ON T2.DOMI_CD  = T3.DOMI_CD ";
        $query .= " INNER JOIN DOMITORY_MST T1 ON T1.DOMI_CD = T3.DOMI_CD ";

        return $query;
    }
    
    //１レコード取得
    public function getRow($model, $cd1, $cd2)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM SCHREG_DOMITORY_HIST_DAT ";
        $query .= "    WHERE SCHREGNO   = '".$cd1."' ";
        $query .= "    AND DOMI_CD      = '".$cd2."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //１レコード取得DOMI_ENTDAY含む
    public function getRowSdate($model, $cd1, $cd2, $cd3)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM SCHREG_DOMITORY_HIST_DAT ";
        $query .= "    WHERE SCHREGNO   = '".$cd1."' ";
        $query .= "    AND DOMI_CD      = '".$cd2."' ";
        $query .= "    AND DOMI_ENTDAY  = '" .str_replace("/", "-", $cd3) ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //DOMITORY_MSTから寮名を取得
    public function getDomitory_Data($model, $cd1)
    {

        $query  = " SELECT DISTINCT T1.DOMI_CD,T1.DOMI_CD || '　' || T1.DOMI_NAME AS DOMI_NAME";
        $query .= " FROM DOMITORY_MST T1,DOMITORY_YDAT T2";
        $query .= " WHERE   T2.YEAR = '" .$cd1 ."'";
        $query .= "     AND T2.DOMI_CD  = T1.DOMI_CD";
        $query .= " ORDER BY T1.DOMI_CD ";

        return $query;
    }

    public function getDomitoryHistory_key_chk($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query .= " FROM SCHREG_DOMITORY_HIST_DAT ";
        $query .= "    WHERE SCHREGNO    = '".$model->field["SCHREGNO"]."' ";
        $query .= "      AND DOMI_ENTDAY = '".str_replace("/", "-", $model->field["DOMI_ENTDAY"])."' ";

        $getrec = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $getrec;
    }

    //チェック用レコード取得 NO001
    public function getDomitoryHistory_Dat_chk($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query .= " FROM SCHREG_DOMITORY_HIST_DAT ";
        $query .= "    WHERE SCHREGNO    = '".$model->field["SCHREGNO"]."' ";
        if ($model->field["DOMI_OUTDAY"] != "") {
            //内側から始まる
            $query .= "      AND ((DOMI_ENTDAY    <= '".str_replace("/", "-", $model->field["DOMI_ENTDAY"])."' AND ( DOMI_OUTDAY IS NULL OR DOMI_OUTDAY >= '".str_replace("/", "-", $model->field["DOMI_ENTDAY"])."')) ";
            //内側で止まる
            $query .= "           OR (DOMI_ENTDAY <= '".str_replace("/", "-", $model->field["DOMI_OUTDAY"])."' AND ( DOMI_OUTDAY IS NULL OR DOMI_OUTDAY >= '".str_replace("/", "-", $model->field["DOMI_OUTDAY"])."')) ";
            //範囲を包含
            $query .= "           OR (DOMI_ENTDAY >= '".str_replace("/", "-", $model->field["DOMI_ENTDAY"])."' AND ( DOMI_OUTDAY IS NULL OR DOMI_OUTDAY <= '".str_replace("/", "-", $model->field["DOMI_OUTDAY"])."'))) ";
        } else {
            $query .= "      AND (DOMI_OUTDAY IS NULL OR DOMI_OUTDAY > '".str_replace("/", "-", $model->field["DOMI_ENTDAY"])."' ) ";
        }
        $getrec = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $getrec;
    }

    //INSERT
    public function &getInsertQuery($model, $fields, $schregno)
    {
        $db = Query::dbCheckOut();
        
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["DOMI_CD"][TEXT]      = $fields["DOMI_CD"];
        $data["DOMI_ENTDAY"][TEXT]  = str_replace("/", "-", $fields["DOMI_ENTDAY"]);
        $data["DOMI_OUTDAY"][TEXT]  = str_replace("/", "-", $fields["DOMI_OUTDAY"]);
        $data["REMARK"][TEXT]       = "";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_DOMITORY_HIST_DAT");
        
        $db->query($query);
        Query::dbCheckIn($db);
        
        return;
    }

    //UPDATE
    public function &getUpdateQuery($model, $fields, $schregno)
    {
        $db = Query::dbCheckOut();

        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["DOMI_CD"][TEXT]       = $fields["DOMI_CD"];
        $data["DOMI_ENTDAY"][TEXT]        = str_replace("/", "-", $fields["DOMI_ENTDAY"]);
        $data["DOMI_OUTDAY"][TEXT]        = str_replace("/", "-", $fields["DOMI_OUTDAY"]);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE SCHREGNO    = '" .$schregno ."' ";
        $where .= "   AND DOMI_CD     = '" .$fields["DOMI_CD"] ."' ";
        $where .= "   AND DOMI_ENTDAY = '" .str_replace("/", "-", $fields["DOMI_ENTDAY"]) ."' ";

        $query = Query::updateSQL($data, "SCHREG_DOMITORY_HIST_DAT", $where);
        
        $db->query($query);
        Query::dbCheckIn($db);
        
        return ;
    }

    //DELETE
    public function &getDeleteQuery($model, $fields, $schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM SCHREG_DOMITORY_HIST_DAT ";
        $query .= " WHERE SCHREGNO    = '" .$schregno ."' ";
        $query .= "   AND DOMI_CD     = '" .$fields["DOMI_CD"] ."' ";
        $query .= "   AND DOMI_ENTDAY = '" .str_replace("/", "-", $fields["DOMI_ENTDAY"]) ."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
