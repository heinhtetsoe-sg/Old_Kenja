<?php

require_once('for_php7.php');

class knjz320Query extends Query {

    //出身学校年度一覧取得
    function selectSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

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
            $query .= "PARENTMENUID in ('Root', '" .$menuid ."') ";
        }
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            if ($menuid != "") $query .= " AND ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "order by MENUID,SUBMENUID";

        return $query;
    }

    //設定職員と参照職員取得
    function GetGroup($model, $year)
    {
        if ($model->Properties["useSchoolWare"] == "1") {
            $query .= " SELECT ";
            $query .= "     STF_WORK.STAFFCD, ";
            $query .= "     STF.STAFFNAME ";
            $query .= " FROM ";
            $query .= "     STAFF_WORK_HIST_DAT STF_WORK ";
            $query .= "     INNER JOIN STAFF_MST STF ON STF_WORK.STAFFCD = STF.STAFFCD ";
            $query .= " WHERE ";
            $query .= "     STF_WORK.FROM_SCHOOLCD = '{$model->schoolCd}' ";
            $query .= "     AND STF_WORK.USE_KNJ = '1' ";
            $query .= " GROUP BY ";
            $query .= "     STF_WORK.STAFFCD, ";
            $query .= "     STF.STAFFNAME ";
            $query .= " ORDER BY ";
            $query .= "     STF_WORK.STAFFCD ";
        } else {
            $query .= " SELECT ";
            $query .= "     STAFFCD, ";
            $query .= "     STAFFNAME ";
            $query .= " FROM ";
            $query .= "     V_STAFF_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$year}' ";
            $query .= " ORDER BY ";
            $query .= "     STAFFNAME_KANA ";
        }
        return $query;
    }

    //AuthlistのMENUデータ取得
    function selectQueryAuth($model)
    {
        $query  = "select ";
        $query .= "MENUID,";        //メニューID
        $query .= "SUBMENUID,";     //サブシステムID
        $query .= "PARENTMENUID,";  //親メニューID
        $query .= "MENUNAME ";      //メニュー名称
        $query .= "from MENU_MST ";
        $query .= "where PARENTMENUID = 'Root' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "order by MENUID,SUBMENUID ";

        return $query;
    }

    //各プログラムの権限の取得
    function GetProgramAuth($model, $menuid, $setstaff="")
    {
        $query  = "select a.menuid, a.userauth from userauth_dat a";
        $query .= " where a.menuid in (select m.menuid from menu_mst m where m.parentmenuid = '".$menuid."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND a.SCHOOLCD      = m.SCHOOLCD ";
            $query .= " AND a.SCHOOL_KIND   = m.SCHOOL_KIND) ";
            $query .= " AND a.SCHOOLCD      = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND a.SCHOOL_KIND   = '".SCHOOLKIND."' ";
        } else {
            $query .= " ) ";
        }
        if ($setstaff != ""){
            $query .= " and a.staffcd = '".$setstaff."'";
        }
        return $query;
    }

    //Authlistの各プログラムの権限の取得
    function GetProgramAuthlist($submenuid,$model)
    {
        $order = array("desc", "asc");

        $query  = "SELECT A.STAFFCD, ";
        $query .= "       B.STAFFNAME_SHOW AS STAFF_NAME, ";
        $query .= "       B.STAFFNAME_KANA AS STAFF_KANA, ";
        $query .= "       A.MENUID, ";
        $query .= "       M.PROGRAMID, ";
        $query .= "       M.MENUNAME, ";
        $query .= "       A.USERAUTH ";
        $query .= " FROM MENU_MST M,";
        $query .= "      V_STAFF_MST VS, ";
        $query .= "      USERAUTH_DAT A ";
        $query .= "      LEFT OUTER JOIN STAFF_MST B ON A.STAFFCD = B.STAFFCD ";
        $query .= " WHERE A.MENUID IN (";
        $query .= "                     SELECT MENUID ";
        $query .= "                       FROM MENU_MST MM ";
        $query .= "                      WHERE SUBMENUID = '".$submenuid."'";
        $query .= "                        AND not (PARENTMENUID like '_0000' or PARENTMENUID = 'Root')";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                        AND MM.SCHOOLCD      = A.SCHOOLCD ";
            $query .= "                        AND MM.SCHOOL_KIND   = A.SCHOOL_KIND) ";
            $query .= "   AND M.SCHOOLCD    = A.SCHOOLCD ";
            $query .= "   AND M.SCHOOL_KIND = A.SCHOOL_KIND ";
            $query .= "   AND M.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "   AND M.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= "                 )";
        }
        $query .= "   AND M.MENUID = A.MENUID ";
        $query .= "   AND VS.STAFFCD = A.STAFFCD ";
        $query .= "   AND VS.YEAR = '".CTRL_YEAR."'";

       switch ($model->s_id)
       {
       case "1";
       $query .= " ORDER BY  A.STAFFCD ".$order[$model->sort[$model->s_id]].", A.MENUID asc "; break;
       case "2":
       $query .= " ORDER BY  STAFF_KANA ".$order[$model->sort[$model->s_id]].", A.MENUID asc, A.STAFFCD asc "; break;
       }
        return $query;
    }

    //権限の更新
    function getUpdateQuery($model)
    {
        $i         = 0;
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
        $query  = "DELETE FROM userauth_dat where staffcd = '".$model->setstaff."' and menuid in ('".$menuid."')";
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

            $query  = "INSERT INTO userauth_dat(STAFFCD, ".$Fld." MENUID, USERAUTH, REGISTERCD, UPDATED) ";
            $query .= "VALUES";
            $query .= "('" .$model->setstaff."'";   //グループコード
            $query .= $school_kind;                 //学校コード、校種
            $query .= ",'" .$key."'";               //メニューID
            $query .= ",'" .$val."'";               //グループ権限
            $query .= ",'" .STAFFCD."'";
            $query .= ",sysdate())";                //更新日付
            $db->query($query);
            if ($val != "9" && $val != "7") //権限がある場合をチェック
                ++$auth_flg;
        }

        //その親たちの権限更新
        if ($auth_flg > 0) { //権限あり
            $query  = "DELETE FROM userauth_dat where staffcd = '".$model->setstaff."'";
            $query .= " and menuid in ('".str_replace(",", "','",$model->path)."')";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $db->query($query);

            $sp = ",";
            $menuid = explode(",", $model->path);
            $query  = "INSERT INTO userauth_dat(STAFFCD, ".$Fld." MENUID, USERAUTH, REGISTERCD, UPDATED) ";
            $query .= "VALUES";
            for ($i = 0; $i < get_count($menuid); $i++) {
                if ($i == get_count($menuid) - 1)
                    $sp = "";
                $query .= "('" .$model->setstaff."'";
                $query .= $school_kind;
                $query.= ",'" .$menuid[$i]."'";
                $query.= ",'0'";
                $query .= ",'" .STAFFCD."'";
                $query.= ",sysdate())" .$sp;
            }
            $db->query($query);

        } elseif ($auth_flg == 0) { //権限なしまたは未設定
            
            $menuid = explode(",", $model->path);
            
            for ($i = get_count($menuid) - 1; $i >= 0; $i--)
            {
                $auth_flg = 0;
                $result   = $db->query(knjz320Query::GetProgramAuth($model, $menuid[$i], $model->setstaff));    //子供たちの権限チェック
                
                if ($result->numRows() == 0) { //子供がすべて未設定の場合 -- 親を削除する 
                    $query  = "DELETE FROM userauth_dat where staffcd = '".$model->setstaff."' and menuid = '".$menuid[$i]."'";
                    if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                        $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                    }
                    $db->query($query);
                } else {
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        if ($row["USERAUTH"] != "9")
                            ++$auth_flg;
                    }
                    if ($auth_flg == 0) //子供がすべて権限なし -- 親を削除する。
                    { 
                        $query  = "DELETE FROM userauth_dat where staffcd = '".$model->setstaff."' and menuid = '".$menuid[$i]."'";
                        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                        }
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