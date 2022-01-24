<?php

require_once('for_php7.php');

class knjp190kQuery extends Query {

    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($schregno)
    {
        $query  ="SELECT schregno,name_show FROM schreg_base_mst ";
        $query .=" WHERE schregno = '".$schregno."'";
        return $query;
    }

    //生徒交付データよりデータを取得 NO002
    function getlist($schregno)
    {
        $query  = " SELECT ";
        $query .= "     year, ";
        $query .= "     schregno, ";
        $query .= "     grantcd, ";
        $query .= "     meisyou_get(grantcd,'G212',1) grantname, ";
        $query .= "     grantsdate, ";
        $query .= "     grantedate, ";
        $query .= "     grant_money, ";
        $query .= "     remark ";
        $query .= " FROM ";
        $query .= "     schreg_grant_dat ";
        $query .= " WHERE ";
#        $query .= "     year     = '".CTRL_YEAR."' AND ";
        $query .= "     schregno = '".$schregno."' ";
        $query .= " ORDER BY grantcd ";
        return $query;
    }

    //名称取得 NO002
    function getName()
    {
        $query  = " SELECT namecd2,name1 ";
        $query .= "   FROM v_name_mst ";
        $query .= "  WHERE year    = '".CTRL_YEAR."' ";
        $query .= "    AND namecd1 = 'G212' ";
        $query .= "  ORDER BY namecd2 ";
        return $query;
    }

    //１レコード取得
    function getRow($grantcd,$schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     year AS grantyear,schregno,grantcd,grantsdate,grantedate,grant_money,remark,updated ";
        $query .= " FROM ";
        $query .= "     schreg_grant_dat ";
        $query .= " WHERE ";
#        $query .= "     year     = '".CTRL_YEAR."' AND ";
        $query .= "     schregno = '".$schregno."' AND ";
        $query .= "     grantcd  = '".$grantcd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //入学日付、卒業日付を取得
    function getEgYear($schregno)
    {
        $db = Query::dbCheckOut();

       $query  = " SELECT COALESCE(ent_date, DATE('0001-01-01'))             AS ent_date, ";    //入学日付
       $query .= "        Fiscalyear(COALESCE(ent_date, DATE('0001-01-01'))) AS eyear, ";       //入学日付(年度)
       $query .= "        COALESCE(grd_date, DATE('9999-12-31'))             AS grd_date, ";    //除籍(卒業)日付
       $query .= "        Fiscalyear(COALESCE(grd_date, DATE('9999-12-31'))) AS gyear ";        //除籍(卒業)日付(年度)
       $query .= "   FROM schreg_base_mst ";
       $query .= "  WHERE schregno = '".$schregno."' ";

        $date = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $date;
    }

    // INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

#        $data["YEAR"][TEXT]          = CTRL_YEAR;
        $data["YEAR"][TEXT]          = $model->field["GRANTYEAR"];
        $data["SCHREGNO"][TEXT]      = $model->schregno;
        $data["GRANTCD"][TEXT]       = $model->field["GRANTCD"];
        $data["GRANTSDATE"][TEXT]    = strtr($model->field["GRANTSDATE"],"/","-");
        $data["GRANTEDATE"][TEXT]    = strtr($model->field["GRANTEDATE"],"/","-");
        $data["GRANT_MONEY"][NUMBER] = is_null($model->field["GRANT_MONEY"])? 0 : $model->field["GRANT_MONEY"];
        $data["REMARK"][TEXT]        = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $query = Query::insertSQL($data, "schreg_grant_dat");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
            
        $data["YEAR"][TEXT]          = $model->field["GRANTYEAR"];
        $data["GRANTSDATE"][TEXT]    = strtr($model->field["GRANTSDATE"],"/","-");
        $data["GRANTEDATE"][TEXT]    = strtr($model->field["GRANTEDATE"],"/","-");
        $data["GRANT_MONEY"][NUMBER] = is_null($model->field["GRANT_MONEY"])? 0 : $model->field["GRANT_MONEY"];
        $data["REMARK"][TEXT]        = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $where  = " WHERE schregno   = '".$model->schregno."'";
        $where .= "   AND grantcd    = '".$model->field["GRANTCD"]."'";
/*
        $where  = " WHERE year       = '".CTRL_YEAR."'";
        $where .= "   AND schregno   = '".$model->schregno."'";
        $where .= "   AND grantcd    = '".$model->field["GRANTCD"]."'";
*/
        $query = Query::updateSQL($data, "schreg_grant_dat", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return ;
    }

    // DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM schreg_grant_dat ";
        $query .= " WHERE ";
#        $query .= "     year = '".CTRL_YEAR."' AND ";
        $query .= "     schregno = '".$model->schregno."' AND ";
        $query .= "     grantcd = '".$model->field["GRANTCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
