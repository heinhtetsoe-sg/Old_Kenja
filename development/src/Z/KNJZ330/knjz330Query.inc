<?php

require_once('for_php7.php');

class knjz330Query extends Query {

    //MENUデータ取得
    function selectQuery($model, $menuid="")
    {
        $query  = "select ";
        $query .= "MENUID,";
        $query .= "SUBMENUID,";
        $query .= "PARENTMENUID,";
        $query .= "MENUNAME,";
        $query .= "PROGRAMID,";
        $query .= "PROCESSCD,";
        $query .= "INVALID_FLG ";
        $query .= "from MENU_MST ";
        if ($menuid != "" || ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "")) {
            $query .= "where ";
        }
        if ($menuid != ""){
            $query .= "PARENTMENUID in('Root','" .$menuid ."') ";
        }
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            if ($menuid != "") $query .= " AND ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "order by MENUID,SUBMENUID";
        return $query;
    }

    //設定グループと参照グループ取得
    function GetGroup($model)
    {
        $db = Query::dbCheckOut();
        $opt    = array();
        $query = "";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $result = $db->query("select distinct groupcd, groupname from usergroup_mst ".$query." order by groupcd");
    
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["GROUPCD"]."　".$row["GROUPNAME"], 
                           "value" => $row["GROUPCD"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        return $opt;
    }

    //AuthlistのMENUデータ取得
    function selectQueryAuth($model, $menuid="")
    {
        $query  = "select ";
        $query .= "MENUID,";
        $query .= "SUBMENUID,";
        $query .= "PARENTMENUID,";
        $query .= "MENUNAME,";
        $query .= "PROGRAMID,";
        $query .= "PROCESSCD,";
        $query .= "INVALID_FLG ";
        $query .= "from MENU_MST ";
        if ($menuid != "" || ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "")) {
            $query .= "where ";
        }
        if ($menuid != ""){
            $query .= "PARENTMENUID in('Root', '" .$menuid ."') ";
        }
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            if ($menuid != "") $query .= " AND ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "order by MENUID,SUBMENUID";

        return $query;
    }

    //各プログラムの権限の取得
    function GetProgramAuth($model, $menuid, $setgroup="")
    {
        $query  = "select a.menuid, a.groupauth from groupauth_dat a";
        $query .= " where a.menuid in (select m.menuid from menu_mst m where m.parentmenuid = '".$menuid."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND a.SCHOOLCD      = m.SCHOOLCD ";
            $query .= " AND a.SCHOOL_KIND   = m.SCHOOL_KIND) ";
            $query .= " AND a.SCHOOLCD      = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND a.SCHOOL_KIND   = '".SCHOOLKIND."' ";
        } else {
            $query .= " ) ";
        }
        if ($setgroup != ""){
            $query .= " and a.groupcd = '".$setgroup."'";
        }
        return $query;
    }
    
    //Authlistの各プログラムの権限の取得
    function GetProgramAuthlist($model, $submenuid)
    {
        $query  = "SELECT A.GROUPCD, ";
        $query .= "       B.GROUPNAME, ";
        $query .= "       A.MENUID, ";
        $query .= "       M.PROGRAMID, ";
        $query .= "       M.MENUNAME, ";
        $query .= "       A.GROUPAUTH ";
        $query .= " FROM MENU_MST M, ";    
        $query .= "      GROUPAUTH_DAT A ";
        $query .= "      LEFT OUTER JOIN USERGROUP_MST B ON A.GROUPCD = B.GROUPCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND A.SCHOOLCD      = B.SCHOOLCD ";
            $query .= "             AND A.SCHOOL_KIND   = B.SCHOOL_KIND ";
            $query .= " WHERE A.MENUID IN (";
            $query .= "                     SELECT MENUID ";
            $query .= "                       FROM MENU_MST ";
            $query .= "                      WHERE SUBMENUID = '".$submenuid."'";
            $query .= "                        AND not (PARENTMENUID like '_0000' or PARENTMENUID = 'Root') ";
            $query .= "                        AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "                        AND SCHOOL_KIND = '".SCHOOLKIND."') ";
            $query .= "   AND M.SCHOOLCD    = A.SCHOOLCD ";
            $query .= "   AND M.SCHOOL_KIND = A.SCHOOL_KIND ";
            $query .= "   AND M.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "   AND M.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " WHERE A.MENUID IN (";
            $query .= "                     SELECT MENUID ";
            $query .= "                       FROM MENU_MST ";
            $query .= "                      WHERE SUBMENUID = '".$submenuid."'";
            $query .= "                        AND not (PARENTMENUID like '_0000' or PARENTMENUID = 'Root'))";
        } 
        $query .= "   AND M.MENUID = A.MENUID ";
        $query .= " order by GROUPCD ";
        return $query;
    }

    //権限の更新
    function getUpdateQuery($model)
    {
        $i         = 0;
        $sp        = ",";
        $auth_flg  = 0;
        $menuidarr = array();
        $Fld = $school_kind = "";
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->authradio as $key => $val){
            $menuidarr[] = $key;
        }
        
        //各プログラムの権限更新
        $menuid = implode($menuidarr, "','");
        $query  = "DELETE FROM groupauth_dat where groupcd = '".$model->setgroup."' and menuid in ('".$menuid."')";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";

            //追加用
            $Fld = "SCHOOLCD, SCHOOL_KIND,";
            $school_kind = ",'".sprintf("%012d", SCHOOLCD)."','".SCHOOLKIND."'";
        }
        $db->query($query);
       
        foreach ($model->authradio as $key => $val)
        {
            if ($val == "7")  //値が"未設定"の場合はINSERTしない
                continue;

            $query  = "INSERT INTO groupauth_dat(GROUPCD, ".$Fld." MENUID, GROUPAUTH, REGISTERCD, UPDATED) ";
            $query .= "VALUES";            
            $query .= "('" .$model->setgroup."'";    //グループコード
            $query .= $school_kind;                 //学校コード、校種
            $query .= ",'" .$key."'";               //メニューID
            $query .= ",'" .$val."'";               //グループ権限
            $query .= ",'" .STAFFCD."'";            //登録者コード
            $query .= ",sysdate())";                //更新日付
            $db->query($query);
            if ($val != "9" && $val != "7")
                ++$auth_flg;
        }
          
        //その親たちの権限更新
        if ($auth_flg > 0) { //権限あり
            $query  = "DELETE FROM groupauth_dat where groupcd = '".$model->setgroup."'";
            $query .= " and menuid in ('".str_replace(",", "','",$model->path)."')";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $db->query($query);

            $sp = ",";
            $menuid = explode(",", $model->path);
            $query  = "INSERT INTO groupauth_dat(GROUPCD, ".$Fld." MENUID, GROUPAUTH, REGISTERCD, UPDATED) ";
            $query .= "VALUES";
            for ($i = 0; $i < get_count($menuid); $i++) {
                if ($i == get_count($menuid) - 1)
                    $sp = "";
                $query .= "('" .$model->setgroup."'";
                $query .= $school_kind;
                $query.= ",'" .$menuid[$i]."'";
                $query .= ",'0'";
                $query .= ",'" .STAFFCD."'";                                                     
                $query.= ",sysdate())" .$sp;                                                     
            }
            $db->query($query);

        } elseif ($auth_flg == 0) { //権限なしまたは未設定
            
            $menuid = explode(",", $model->path);
    
            for ($i = get_count($menuid) - 1; $i >= 0; $i--)
            {
                $auth_flg = 0;
                $result   = $db->query(knjz330Query::GetProgramAuth($model, $menuid[$i], $model->setgroup));    //子供たちの権限チェック
                
                if ($result->numRows() == 0) { //子供がすべて未設定の場合 -- 親を削除する 
                    $query  = "DELETE FROM groupauth_dat where groupcd = '".$model->setgroup."' and menuid = '".$menuid[$i]."'";
                    if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                        $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                    }
                    $db->query($query);
                } else { 
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        if ($row["GROUPAUTH"] != "9")
                            ++$auth_flg;
                    }
                    if ($auth_flg == 0)  //その他の子フォルダに権限なし -- 親フォルダの権限を消す
                    {
                        $query  = "DELETE FROM groupauth_dat where groupcd = '".$model->setgroup."' and menuid = '".$menuid[$i]."'";
                        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                        }
                        $db->query($query);
                    
                        $query  = "INSERT INTO groupauth_dat(GROUPCD, ".$Fld." MENUID, GROUPAUTH, REGISTERCD, UPDATED) ";
                        $query .= "VALUES";
                        $query .= "('" .$model->setgroup."'";
                        $query .= $school_kind;
                        $query .= ",'" .$menuid[$i]."'";
                        $query .= ",'9'";
                        $query .= ",'" .STAFFCD."'";
                        $query .= ",sysdate())";
                        $db->query($query);
                    }
                }                
            }                                                 
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
