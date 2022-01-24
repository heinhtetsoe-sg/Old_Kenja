<?php

require_once('for_php7.php');
class knjz331aQuery extends Query {

    //メニュー名
    function getMenuName($model) {
        $query  = " SELECT ";
        $query .= "     MENUNAME ";
        $query .= " FROM ";
        $query .= "     MENU_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND MENUID = 'ZZ000' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
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
        $query .= "     MENUNAME ";
        $query .= " FROM ";
        $query .= "     MENU_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND MENUID = '{$model->field["SUB_MENUID"]}' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
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
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SECURITY_CHK_MNU('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."','".SCHOOLKIND."','".SCHOOLCD."') <> '9' ";
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= "     SECURITY_CHK_MNU('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."') <> '9' ";
        }
        $query .= " AND VALUE(PROGRAMID, 'TITLE') <> 'TITLE' ";

        return $query;
    }

    //基礎メニュー取得
    function getBaseMenuCmb($model) {
        $query  = " WITH MENU_T AS (";
        $query .= knjz331aQuery::getMenuCmb($model);
        $query .= " ), SUBMENU_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MENU_MST T1, ";
        $query .= "     MENU_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.PARENTMENUID ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
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
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
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
        $query .= knjz331aQuery::getMenuCmb($model);
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
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
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
        $query .= knjz331aQuery::getMenuCmb($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.MENUID AS VALUE, ";
        $query .= "     '【' || T1.PROGRAMID || '】' || T1.MENUNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MENU_T T1 ";
        $query .= " WHERE ";
        $query .= "     T1.PARENTMENUID = '{$model->field["SELECT_SUB_MENUID"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //STAFFメニューID取得
    function getStaffMenuCmb($model) {
        $query .= " SELECT ";
        $query .= "     T1.MENUID AS VALUE, ";
        $query .= "     '【' || T1.PROGRAMID || '】' || T1.MENUNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MENU_STAFF_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".STAFFCD."' ";
        $query .= "     AND T1.PARENTMENUID = '{$model->field["SUB_MENUID"]}' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //基本メニュー削除
        $query  = " DELETE FROM ";
        $query .= "     MENU_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND MENUID = 'ZZ000' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //基本メニュー追加
        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        }
        $data["STAFFCD"][TEXT]      = STAFFCD;
        $data["MENUID"][TEXT]       = "ZZ000";
        $data["SUBMENUID"][TEXT]    = "Z";
        $data["PARENTMENUID"][TEXT] = "Root";
        $data["MENUNAME"][DATE]     = $model->field["MENUNAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $query = Query::insertSQL($data, "MENU_STAFF_MST");
        $db->query($query);


        //サブメニュー削除
        $query  = " DELETE FROM ";
        $query .= "     MENU_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND MENUID = '{$model->field["SUB_MENUID"]}' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //サブメニュー追加
        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        }
        $data["STAFFCD"][TEXT]      = STAFFCD;
        $data["MENUID"][TEXT]       = $model->field["SUB_MENUID"];
        $data["SUBMENUID"][TEXT]    = "Z";
        $data["PARENTMENUID"][TEXT] = "ZZ000";
        $data["MENUNAME"][DATE]     = $model->field["SUB_MENUNAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $query = Query::insertSQL($data, "MENU_STAFF_MST");
        $db->query($query);

        $menuArray = $model->selectdata == "" ? array() : explode(",", $model->selectdata);
        $menuTextArray = $model->selectdataLabel == "" ? array() : explode(",", $model->selectdataLabel);
        $setMenuIdTop = substr($model->field["SUB_MENUID"], 0, 3);

        $setMenuIdRen = 1;
        $updMenuQueryArray = array();
        $updAuthQueryArray = array();

        foreach ($menuArray as $key => $val) {
            $setMenuId = $setMenuIdTop.sprintf("%02d", $setMenuIdRen);
            $checkId = substr($val, 0, 3);
            if ($val == "TITLE") {
                $setRow["MENUNAME"]     = str_replace("【TITLE】", "", $menuTextArray[$key]);
                $setRow["PROGRAMID"]    = "TITLE";
                $setRow["PROGRAMPATH"]  = "TITLE";
                $setRow["PROCESSCD"]    = "1";
                $setRow["INVALID_FLG"]  = "0";

                $setRow["USERAUTH"]     = "0";
            } else {
                if ($checkId == $setMenuIdTop) {
                    $query = knjz331aQuery::getUpdStaffMenuData($model, $val);
                } else {
                    $query = knjz331aQuery::getUpdMenuData($model, $val);
                }
                $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }

            //MENU_STAFF_MST作成
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $updMenuQueryArray[$setMenuId]["SCHOOLCD"][TEXT]     = SCHOOLCD;
                $updMenuQueryArray[$setMenuId]["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
            }
            $updMenuQueryArray[$setMenuId]["STAFFCD"][TEXT]      = STAFFCD;
            $updMenuQueryArray[$setMenuId]["MENUID"][TEXT]       = $setMenuId;
            $updMenuQueryArray[$setMenuId]["SUBMENUID"][TEXT]    = "Z";
            $updMenuQueryArray[$setMenuId]["PARENTMENUID"][TEXT] = $model->field["SUB_MENUID"];
            $updMenuQueryArray[$setMenuId]["MENUNAME"][TEXT]     = $setRow["MENUNAME"];
            $updMenuQueryArray[$setMenuId]["PROGRAMID"][TEXT]    = $setRow["PROGRAMID"];
            $updMenuQueryArray[$setMenuId]["PROGRAMPATH"][TEXT]  = $setRow["PROGRAMPATH"];
            $updMenuQueryArray[$setMenuId]["PROCESSCD"][TEXT]    = $setRow["PROCESSCD"];
            $updMenuQueryArray[$setMenuId]["INVALID_FLG"][TEXT]  = $setRow["INVALID_FLG"];
            $updMenuQueryArray[$setMenuId]["PROGRAMMEMO"][TEXT]  = $setRow["PROGRAMMEMO"];
            $updMenuQueryArray[$setMenuId]["SHOWORDER"][TEXT]    = $setRow["SHOWORDER"];
            $updMenuQueryArray[$setMenuId]["SSL"][TEXT]          = $setRow["SSL"];
            $updMenuQueryArray[$setMenuId]["REGISTERCD"][TEXT]   = STAFFCD;
            $updMenuQueryArray[$setMenuId]["UPDATED"][NUMBER]    = "sysdate()";

            //権限作成
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $updAuthQueryArray[$setMenuId]["SCHOOLCD"][TEXT]     = SCHOOLCD;
                $updAuthQueryArray[$setMenuId]["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
            }
            $updAuthQueryArray[$setMenuId]["STAFFCD"][TEXT]      = STAFFCD;
            $updAuthQueryArray[$setMenuId]["MENUID"][TEXT]       = $setMenuId;
            $updAuthQueryArray[$setMenuId]["USERAUTH"][TEXT]     = $setRow["USERAUTH"];
            $updAuthQueryArray[$setMenuId]["REGISTERCD"][TEXT]   = STAFFCD;
            $updAuthQueryArray[$setMenuId]["UPDATED"][NUMBER]    = "sysdate()";

            $setMenuIdRen++;
        }

        //MENU_STAFF_MST削除
        $query  = " DELETE FROM ";
        $query .= "     MENU_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND PARENTMENUID = '{$model->field["SUB_MENUID"]}' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //MENU_STAFF_MST作成
        foreach ($updMenuQueryArray as $key => $val) {
            $query = Query::insertSQL($val, "MENU_STAFF_MST");
            $db->query($query);
        }

        //権限削除
        $query  = " DELETE FROM ";
        $query .= "     USERAUTH_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND (MENUID = 'ZZ000' OR MENUID = '{$model->field["SUB_MENUID"]}') ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //設定メニュー数
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND VALUE(PROGRAMID, 'TITLE') != 'TITLE' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $menuCnt = $db->getOne($query);

        //基本権限作成
        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        }
        $data["STAFFCD"][TEXT]      = STAFFCD;
        $data["MENUID"][TEXT]       = "ZZ000";
        $data["USERAUTH"][TEXT]     = $menuCnt > 0 ? "0" : "9";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $query = Query::insertSQL($data, "USERAUTH_DAT");
        $db->query($query);

        //サブ権限作成
        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        }
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
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //USERAUTH_DAT作成
        foreach ($updAuthQueryArray as $key => $val) {
            $query = Query::insertSQL($val, "USERAUTH_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //更新用メニュー取得
    function getUpdMenuData($model, $menuId) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SECURITY_CHK_MNU2('" .STAFFCD ."',T1.MENUID,'" .CTRL_YEAR ."','".SCHOOLKIND."','".SCHOOLCD."') AS USERAUTH ";
        } else {
            $query .= "     SECURITY_CHK_MNU2('" .STAFFCD ."',T1.MENUID,'" .CTRL_YEAR ."') AS USERAUTH ";
        }
        $query .= " FROM ";
        $query .= "     MENU_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = '{$menuId}' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //更新用メニュー取得
    function getUpdStaffMenuData($model, $menuId) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SECURITY_CHK_MNU2('" .STAFFCD ."',T1.MENUID,'" .CTRL_YEAR ."','".SCHOOLKIND."','".SCHOOLCD."') AS USERAUTH ";
        } else {
            $query .= "     SECURITY_CHK_MNU2('" .STAFFCD ."',T1.MENUID,'" .CTRL_YEAR ."') AS USERAUTH ";
        }
        $query .= " FROM ";
        $query .= "     MENU_STAFF_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".STAFFCD."' ";
        $query .= "     AND T1.MENUID = '{$menuId}' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }

        return $query;
    }
}
?>
