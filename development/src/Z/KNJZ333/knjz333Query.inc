<?php

require_once('for_php7.php');

class knjz333Query extends Query {

    //メニュー名
    function getMenuName($model) {
        $query  = " SELECT ";
        $query .= "     SUBNAME ";
        $query .= " FROM ";
        $query .= "     MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     SUBID = 'S".$model->field["MENUID"]."0000' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }
    //処理月
    function getSyoriMonth($model){
        $query .= " SELECT ";
        $query .= "    SYORI_MONTH ";
        $query .= " FROM ";
        $query .= "    MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     SUBID = 'S".$model->field["MENUID"]."0000' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
    
    //非表示
    function getHideFlg($model){
        $query .= " SELECT ";
        $query .= "    HIDE_FLG ";
        $query .= " FROM ";
        $query .= "    MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     SUBID = 'S".$model->field["MENUID"]."0000' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
    
    //MENU_SUB_MSTに登録されてるかカウント
    function getCnt($menuid, $secid){
        $query .= " SELECT ";
        $query .= "    COUNT(SUBID) ";
        $query .= " FROM ";
        $query .= "    MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "    SUBID = 'S".$menuid.$secid."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
    //MENU_SUB_MSTからSUBNAMEを取得
    function getTitle($menuid, $secid){
        $query  = " SELECT ";
        $query .= "    SUBID, ";
        $query .= "    SUBNAME ";
        $query .= " FROM ";
        $query .= "    MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "    SUBID = 'S".$menuid.$secid."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
    
    //サブメニュー
    function getSubMenuId() {
        $query  = " WITH SUBMENU(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('ZZ100', 'ZZ100') ";
        $query .= "     UNION ";
        $query .= "     VALUES('ZZ200', 'ZZ200') ";
        $query .= "     UNION ";
        $query .= "     VALUES('ZZ300', 'ZZ300') ";
        $query .= "     UNION ";
        $query .= "     VALUES('ZZ400', 'ZZ400') ";
        $query .= "     UNION ";
        $query .= "     VALUES('ZZ500', 'ZZ500') ";
        $query .= "     UNION ";
        $query .= "     VALUES('ZZ600', 'ZZ600') ";
        $query .= "     UNION ";
        $query .= "     VALUES('ZZ700', 'ZZ700') ";
        $query .= "     UNION ";
        $query .= "     VALUES('ZZ800', 'ZZ800') ";
        $query .= "     UNION ";
        $query .= "     VALUES('ZZ900', 'ZZ900') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     SUBMENU ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //サブメニュー名
    function getSubMenuName($model) {
        $query  = " SELECT ";
        $query .= "     SUBNAME ";
        $query .= " FROM ";
        $query .= "     MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     SUBID = 'S".$model->field["MENUID"].$model->field["SUB_MENUID"]."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //基礎メニュー取得
    function getMenuCmb($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MENU_MST ";
        $query .= " WHERE ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "     SECURITY_CHK_MNU('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."','".SCHOOLKIND."','".SCHOOLCD."') <> '9' ";
        }else{
            $query .= "     SECURITY_CHK_MNU('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."') <> '9' ";
        }
        $query .= "     AND VALUE(PROGRAMID, 'TITLE') <> 'TITLE' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //基礎メニュー取得
    function getBaseMenuCmb($model) {
        $query  = " WITH MENU_T AS (";
        $query .= knjz333Query::getMenuCmb($model);
        $query .= " ), SUBMENU_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MENU_MST T1, ";
        $query .= "     MENU_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.PARENTMENUID ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     T1.SCHOOLCD = T2.SCHOOLCD ";
            $query .= " AND ";
            $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= " AND ";
            $query .= "     T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.MENUID AS VALUE, ";
        $query .= "     T1.MENUNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MENU_MST T1, ";
        $query .= "     SUBMENU_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.PARENTMENUID ";
        $query .= "     AND T1.PARENTMENUID = 'Root' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     T1.SCHOOLCD = T2.SCHOOLCD ";
            $query .= " AND ";
            $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= " AND ";
            $query .= "     T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.MENUID, ";
        $query .= "     T1.MENUNAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //サブメニュー取得
    function getSubMenuCmb($model) {
        $query  = " WITH MENU_T AS (";
        $query .= knjz333Query::getMenuCmb($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.MENUID AS VALUE, ";
        $query .= "     T1.MENUNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MENU_MST T1, ";
        $query .= "     MENU_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.PARENTMENUID ";
        $query .= "     AND T1.PARENTMENUID = '{$model->field["SELECT_MENUNAME"]}' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     T1.SCHOOLCD = T2.SCHOOLCD ";
            $query .= " AND ";
            $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= " AND ";
            $query .= "     T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.MENUID, ";
        $query .= "     T1.MENUNAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストメニュー取得
    function getListMenuCmb($model) {
        $query  = " WITH MENU_T AS (";
        $query .= knjz333Query::getMenuCmb($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.MENUID AS VALUE, ";
        $query .= "     '【' || T1.PROGRAMID || '】' || T1.MENUNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MENU_T T1 ";
        $query .= " WHERE ";
        $query .= "     T1.PARENTMENUID = '{$model->field["SELECT_SUB_MENUID"]}' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //STAFFメニューID取得
    function getStaffMenuCmb($model) {
        $query  = " SELECT ";
        $query .= "    t1.SUBID AS VALUE, ";
        $query .= "    case ";
        $query .= "        when t2.MENUNAME IS NOT NULL then '【' || t2.PROGRAMID || '】' || t2.MENUNAME ";
        $query .= "        when t1.SUBNAME IS NULL then '【' || t1.MAINID || '】' || ' ' ";
        $query .= "        else '【' || t1.MAINID || '】' || t1.SUBNAME ";
        $query .= "    end AS LABEL ";
        $query .= " FROM ";
        $query .= "    MENU_SUB_MST t1 ";
        $query .= "    left join MENU_MST t2 on t1.MAINID = t2.MENUID ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "    and t1.SCHOOLCD = t2.SCHOOLCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "    t1.PARENTSUBID = 'S".$model->field["MENUID"].$model->field["SUB_MENUID"]."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     t1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     t1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    SUBID ";

        return $query;
    }

    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //基本メニュー検索！
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "    SUBID = 'S".$model->field["MENUID"]."0000' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $first = $db->getOne($query);
        
        if($first > 0){     //0より大きかったらupdate
            $data = array();
            $data["SUBNAME"][TEXT]     = $model->field["MENUNAME"];
            $data["SYORI_MONTH"][NUMBER]     = $model->field["SYORI_MONTH"] != "" ? $model->field["SYORI_MONTH"] : " ";
            $data["HIDE_FLG"][TEXT]     = $model->field["HIDE_FLG"] != "" ? $model->field["HIDE_FLG"] : " ";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            
            $where = " WHERE SUBID = 'S".$model->field["MENUID"]."0000' ";
            if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                $where .= " AND ";
                $where .= "     SCHOOLCD = '".SCHOOLCD."' ";
                $where .= " AND ";
                $where .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            
            $query = Query::updateSQL($data, "MENU_SUB_MST", $where);
            $db->query($query);
        }else{
            //基本メニュー追加
            $data = array();
            $data["SUBID"][TEXT]       = "S".$model->field["MENUID"]."0000";
            $data["SUBSUBID"][TEXT]    = "S".$model->field["MENUID"];
            $data["PARENTSUBID"][TEXT] = "Root";
            $data["SUBNAME"][TEXT]     = $model->field["MENUNAME"];
            $data["MAINID"][TEXT]       = " ";
            $data["SYORI_MONTH"][NUMBER]     = $model->field["SYORI_MONTH"] != "" ? $model->field["SYORI_MONTH"] : " ";
            $data["HIDE_FLG"][TEXT]     = $model->field["HIDE_FLG"] != "" ? $model->field["HIDE_FLG"] : " ";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            
            if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                $data["SCHOOLCD"][TEXT] = SCHOOLCD;
                $data["SCHOOL_KIND"][TEXT] = SCHOOLKIND;
            }
            
            $query = Query::insertSQL($data, "MENU_SUB_MST");
            $db->query($query);
        }

        //サブメニュー検索!!
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "    SUBID = 'S".$model->field["MENUID"].$model->field["SUB_MENUID"]."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $second = $db->getOne($query);
        
        if($second > 0){
            $data = array();
            $data["SUBNAME"][TEXT]     = $model->field["SUB_MENUNAME"];
            
            $where = " WHERE SUBID = 'S".$model->field["MENUID"].$model->field["SUB_MENUID"]."' ";
            if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                $where .= " AND ";
                $where .= "     SCHOOLCD = '".SCHOOLCD."' ";
                $where .= " AND ";
                $where .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            
            $query = Query::updateSQL($data, "MENU_SUB_MST", $where);
            $db->query($query);
        }else{
            //サブメニュー追加
            $data = array();
            $data["SUBID"][TEXT]       = "S".$model->field["MENUID"].$model->field["SUB_MENUID"];
            $data["SUBSUBID"][TEXT]    = "S".$model->field["MENUID"];
            $data["PARENTSUBID"][TEXT] = "S".$model->field["MENUID"]."0000";
            $data["SUBNAME"][TEXT]     = $model->field["SUB_MENUNAME"];
            $data["MAINID"][TEXT]       = " ";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                $data["SCHOOLCD"][TEXT] = SCHOOLCD;
                $data["SCHOOL_KIND"][TEXT] = SCHOOLKIND;
            }

            $query = Query::insertSQL($data, "MENU_SUB_MST");
            $db->query($query);
        }

        $menuArray = $model->selectdata == "" ? array() : explode(",", $model->selectdata);                 //value部分
        $menuTextArray = $model->selectdataLabel == "" ? array() : explode(",", $model->selectdataLabel);   //label部分
        $setMenuIdTop = "S".$model->field["MENUID"].substr($model->field["SUB_MENUID"], 0, 1);              //SA1000のSA1まで

        $setMenuIdRen = 1;
        $updMenuQueryArray = array();
        $updAuthQueryArray = array();

        foreach ($menuArray as $key => $val) {
            $setMenuId = $setMenuIdTop.sprintf("%02d", $setMenuIdRen)."0";
            $checkId = substr($val, 0, 3);
            if($val == "TITLE"){
                //MENU_SUB_MST作成
                $updMenuQueryArray[$setMenuId]["SUBID"][TEXT]       = $setMenuId;
                $updMenuQueryArray[$setMenuId]["SUBSUBID"][TEXT]    = "S".$model->field["MENUID"];
                $updMenuQueryArray[$setMenuId]["PARENTSUBID"][TEXT] = "S".$model->field["MENUID"].$model->field["SUB_MENUID"];
                $updMenuQueryArray[$setMenuId]["SUBNAME"][TEXT]      = $model->field["TITLE_NAME"];
                $updMenuQueryArray[$setMenuId]["MAINID"][TEXT]      = $val;
                $updMenuQueryArray[$setMenuId]["REGISTERCD"][TEXT]   = STAFFCD;
                $updMenuQueryArray[$setMenuId]["UPDATED"][NUMBER]    = "sysdate()";
                if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                    $updMenuQueryArray[$setMenuId]["SCHOOLCD"][TEXT] = SCHOOLCD;
                    $updMenuQueryArray[$setMenuId]["SCHOOL_KIND"][TEXT] = SCHOOLKIND;
                }
            }else if ($checkId == $setMenuIdTop) {
                $query = knjz333Query::getUpdStaffMenuData($model, $val);
                $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                //MENU_SUB_MST作成
                $updMenuQueryArray[$setMenuId]["SUBID"][TEXT]       = $setMenuId;
                $updMenuQueryArray[$setMenuId]["SUBSUBID"][TEXT]    = "S".$model->field["MENUID"];
                $updMenuQueryArray[$setMenuId]["PARENTSUBID"][TEXT] = "S".$model->field["MENUID"].$model->field["SUB_MENUID"];
                $updMenuQueryArray[$setMenuId]["SUBNAME"][TEXT]      = $setRow["SUBNAME"];
                $updMenuQueryArray[$setMenuId]["MAINID"][TEXT]      = $setRow["MAINID"];
                $updMenuQueryArray[$setMenuId]["REGISTERCD"][TEXT]   = STAFFCD;
                $updMenuQueryArray[$setMenuId]["UPDATED"][NUMBER]    = "sysdate()";
                if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                    $updMenuQueryArray[$setMenuId]["SCHOOLCD"][TEXT] = SCHOOLCD;
                    $updMenuQueryArray[$setMenuId]["SCHOOL_KIND"][TEXT] = SCHOOLKIND;
                }
            } else {
                $query = knjz333Query::getUpdMenuData($model, $val);
                $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                //MENU_SUB_MST作成
                $updMenuQueryArray[$setMenuId]["SUBID"][TEXT]       = $setMenuId;
                $updMenuQueryArray[$setMenuId]["SUBSUBID"][TEXT]    = "S".$model->field["MENUID"];
                $updMenuQueryArray[$setMenuId]["PARENTSUBID"][TEXT] = "S".$model->field["MENUID"].$model->field["SUB_MENUID"];
                $updMenuQueryArray[$setMenuId]["MAINID"][TEXT]      = $setRow["MENUID"];
                $updMenuQueryArray[$setMenuId]["REGISTERCD"][TEXT]   = STAFFCD;
                $updMenuQueryArray[$setMenuId]["UPDATED"][NUMBER]    = "sysdate()";
                if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
                    $updMenuQueryArray[$setMenuId]["SCHOOLCD"][TEXT] = SCHOOLCD;
                    $updMenuQueryArray[$setMenuId]["SCHOOL_KIND"][TEXT] = SCHOOLKIND;
                }
            }

            $setMenuIdRen++;
        }

        //MENU_SUB_MST削除
        $query  = " DELETE FROM ";
        $query .= "     MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     PARENTSUBID = 'S".$model->field["MENUID"].$model->field["SUB_MENUID"]."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //MENU_SUB_MST作成
        foreach ($updMenuQueryArray as $key => $val) {
            $query = Query::insertSQL($val, "MENU_SUB_MST");
            $db->query($query);
        }

        /*//権限削除
        $query  = " DELETE FROM ";
        $query .= "     USERAUTH_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND (MENUID = 'ZZ000' OR MENUID = '{$model->field["SUB_MENUID"]}') ";
        $db->query($query);

        //設定メニュー数
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND VALUE(PROGRAMID, 'TITLE') != 'TITLE' ";
        $menuCnt = $db->getOne($query);

        //基本権限作成
        $data = array();
        $data["STAFFCD"][TEXT]      = STAFFCD;
        $data["MENUID"][TEXT]       = "ZZ000";
        $data["USERAUTH"][TEXT]     = $menuCnt > 0 ? "0" : "9";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $query = Query::insertSQL($data, "USERAUTH_DAT");
        $db->query($query);

        //サブ権限作成
        $data = array();
        $data["STAFFCD"][TEXT]      = STAFFCD;
        $data["MENUID"][TEXT]       = $model->field["SUB_MENUID"];
        $data["USERAUTH"][TEXT]     = $setMenuIdRen > 1 ? "0" : "9";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $query = Query::insertSQL($data, "USERAUTH_DAT");
        $db->query($query);

        //権限削除
        $query  = " DELETE FROM ";
        $query .= "     USERAUTH_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND MENUID != '{$model->field["SUB_MENUID"]}' ";
        $query .= "     AND MENUID LIKE '{$setMenuIdTop}%' ";
        $db->query($query);

        //USERAUTH_DAT作成
        foreach ($updAuthQueryArray as $key => $val) {
            $query = Query::insertSQL($val, "USERAUTH_DAT");
            $db->query($query);
        }*/

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //更新用メニュー取得
    function getUpdMenuData($model, $menuId) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MENU_MST ";
        $query .= " WHERE ";
        $query .= "     MENUID = '{$menuId}' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //更新用メニュー取得
    function getUpdStaffMenuData($model, $menuId) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     SUBID = '{$menuId}' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }
    
    //初期化ボタンが使えるかどうか
    function getDefaultMst($model, $menuid)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MENU_SUB_DEFAULT_MST ";
        $query .= " WHERE ";
        $query .= "     SUBID = 'S".$menuid."0000' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
    //初期化時に元のMENU_SUB_MSTを削除する
    function deleteSubmst($model, $menuid, $submenuid)
    {
        $query  = " DELETE FROM ";
        $query .= "     MENU_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     SUBID in ('S".$menuid."0000','S".$menuid.$submenuid."') OR ";
        $query .= "     PARENTSUBID = 'S".$menuid.$submenuid."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
    //初期化INSERT
    function insertDefalt($model, $menuid, $submenuid)
    {
        $query  = " INSERT INTO  ";
        $query .= "     MENU_SUB_MST  ";
        $query .= " SELECT ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "     SCHOOLCD, ";
            $query .= "     SCHOOL_KIND, ";
        }
        $query .= "     SUBID, ";
        $query .= "     SUBSUBID, ";
        $query .= "     PARENTSUBID, ";
        $query .= "     SUBNAME, ";
        $query .= "     MAINID, ";
        $query .= "     SYORI_MONTH, ";
        $query .= "     HIDE_FLG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     CURRENT TIMESTAMP ";
        $query .= " FROM ";
        $query .= "     MENU_SUB_DEFAULT_MST ";
        $query .= " WHERE ";
        $query .= "     SUBID in ('S".$menuid."0000','S".$menuid.$submenuid."') OR ";
        $query .= "     PARENTSUBID = 'S".$menuid.$submenuid."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
}
?>
