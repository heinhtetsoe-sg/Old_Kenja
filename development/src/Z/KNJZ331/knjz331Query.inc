<?php

require_once('for_php7.php');

class knjz331Query extends Query {

    //メニュー名
    function getMenuName() {
        $query  = " SELECT ";
        $query .= "     MENUNAME ";
        $query .= " FROM ";
        $query .= "     MENU_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND MENUID = 'ZZ000' ";

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

        return $query;
    }

    //メニュー数
    function getMenuCnt($model) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND PARENTMENUID = '{$model->field["SUB_MENUID"]}' ";

        return $query;
    }

    //メニューID取得
    function getMenuId($model) {
        $query .= " SELECT ";
        $query .= "     MENUID ";
        $query .= " FROM ";
        $query .= "     MENU_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND PARENTMENUID = '{$model->field["SUB_MENUID"]}' ";

        return $query;
    }

    //メニュー取得
    function getMenuCmb($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     PROGRAMID || ':' || MAX(MENUID) AS VALUE, ";
        $query .= "     PROGRAMID || '：' || MAX(MENUNAME) AS LABEL ";
        $query .= " FROM ";
        $query .= "     MENU_MST ";
        $query .= " WHERE ";
        $query .= "     SECURITY_CHK_MNU('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."') <> '9' ";
        $query .= "     AND VALUE(PROGRAMID, 'TITLE') <> 'TITLE' ";
        $query .= " GROUP BY ";
        $query .= "     PROGRAMID ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($db, $model) {
        $db->autoCommit(false);

        //対象講座
        $selectChairData = array();
        $selectChair = explode(",", $model->selectChair);

        $query = knjz331Query::getSemesterDate($model);
        $setDate = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //収入生徒割り振りデータをセット(Delete Insert)
        $query  = " DELETE FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SEMESTER  = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregNo}' ";
        $db->query($query);

        //生徒データをInsert
        $setZumiChair = array();
        if ($model->selectChair) {
            foreach ($selectChair as $setChairCd) {

                if ($setZumiChair["$setChairCd"]) {
                    continue;
                }
                $data = array();
                $data["YEAR"][TEXT]         = $model->exeYear;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["CHAIRCD"][TEXT]      = $setChairCd;
                $data["SCHREGNO"][TEXT]     = $model->schregNo;
                $data["APPDATE"][DATE]      = $setDate["SDATE"];
                $data["APPENDDATE"][DATE]   = $setDate["EDATE"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";
                $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                $db->query($query);
                $setZumiChair[$setChairCd] = $setChairCd;
            }
        }

        $db->commit();
        return;
    }
}
?>
