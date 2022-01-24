<?php

require_once('for_php7.php');

class knjz900Query extends Query {

    //プロパティー一覧取得
    function getList() {
        $query  = " WITH MENU_P AS ( ";
        $query .= "     SELECT ";
        $query .= "         MENUID, ";
        $query .= "         '　>> ' || MENUNAME AS MENUNAME, ";
        $query .= "         PROGRAMID AS PRG_ID, ";
        $query .= "         '' AS NAME, ";
        $query .= "         '' AS VALUE, ";
        $query .= "         '' AS REMARK ";
        $query .= "     FROM ";
        $query .= "         MENU_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID IS NULL AND ";
        $query .= "         MENUID IN (SELECT ";
        $query .= "                         T1.PARENTMENUID ";
        $query .= "                     FROM ";
        $query .= "                         MENU_MST T1, ";
        $query .= "                         PRGINFO_PROPERTIES T2 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.PROGRAMID = T2.PROGRAMID ";
        $query .= "                     ) ";
        $query .= " ), MENU_G AS ( ";
        $query .= "     SELECT ";
        $query .= "         MENUID, ";
        $query .= "         MENUNAME, ";
        $query .= "         PROGRAMID AS PRG_ID, ";
        $query .= "         '' AS NAME, ";
        $query .= "         '' AS VALUE, ";
        $query .= "         '' AS REMARK ";
        $query .= "     FROM ";
        $query .= "         MENU_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID IS NULL AND ";
        $query .= "         MENUID IN (SELECT ";
        $query .= "                         T1.PARENTMENUID ";
        $query .= "                     FROM ";
        $query .= "                         MENU_MST T1, ";
        $query .= "                         MENU_P T2 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.MENUID = T2.MENUID ";
        $query .= "                     ) ";

        $query .= " ), PROPERTY_PRG AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         PROGRAMID ";
        $query .= "     FROM ";
        $query .= "         PRGINFO_PROPERTIES ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID NOT IN (SELECT DISTINCT ";
        $query .= "                                 PROGRAMID ";
        $query .= "                             FROM ";
        $query .= "                                 MENU_MST ";
        $query .= "                             WHERE ";
        $query .= "                                 PROGRAMID IS NOT NULL ";
        $query .= "                             ) ";
        $query .= " ), ASSIGN_NO AS ( ";
        $query .= "     SELECT ";
        $query .= "         RTRIM('000' || CHAR(ROW_NUMBER() OVER (ORDER BY PROGRAMID))) AS CT, ";
        $query .= "         PROGRAMID ";
        $query .= "     FROM ";
        $query .= "         PROPERTY_PRG ";
        $query .= " ), MENU_PROPERTY AS ( ";
        $query .= "     SELECT ";
        $query .= "         'ZZ' || SUBSTR(T1.CT, LENGTH(T1.CT)-3) AS MENUID, ";
        $query .= "         T1.PROGRAMID AS MENUNAME, ";
        $query .= "         T1.PROGRAMID AS PRG_ID, ";
        $query .= "         T2.NAME, ";
        $query .= "         T2.VALUE, ";
        $query .= "         T2.REMARK ";
        $query .= "     FROM ";
        $query .= "         ASSIGN_NO T1  ";
        $query .= "     LEFT JOIN PRGINFO_PROPERTIES T2 ON T1.PROGRAMID = T2.PROGRAMID ";
        $query .= " ), MENU_PROPERTY_G AS (  ";
        $query .= "     SELECT  ";
        $query .= "         'ZZ0000' AS MENUID,  ";
        $query .= "         'その他' AS MENUNAME,  ";
        $query .= "         '' AS PRG_ID,  ";
        $query .= "         '' AS NAME,  ";
        $query .= "         '' AS VALUE,  ";
        $query .= "         '' AS REMARK  ";
        $query .= "     FROM  ";
        $query .= "         PROPERTY_PRG  ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.MENUID, ";
        $query .= "     T1.MENUNAME, ";
        $query .= "     T1.PROGRAMID AS PRG_ID, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.VALUE, ";
        $query .= "     T2.REMARK ";
        $query .= " FROM ";
        $query .= "     MENU_MST T1, ";
        $query .= "     PRGINFO_PROPERTIES T2 ";
        $query .= " WHERE ";
        $query .= "     T1.PROGRAMID = T2.PROGRAMID ";
        if(!$ikkatsu){
            $query .= " UNION   ";
            $query .= " SELECT * FROM MENU_P ";
            $query .= " UNION ";
            $query .= " SELECT * FROM MENU_G ";
            $query .= " UNION ";
            $query .= " SELECT * FROM MENU_PROPERTY ";
            $query .= " UNION ";
            $query .= " SELECT * FROM MENU_PROPERTY_G ";
            $query .= " ORDER BY MENUID, PRG_ID, NAME ";
        }

        return $query;
    }

    //プロパティー数取得
    function getPrgNameCnt($programid) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     PRGINFO_PROPERTIES ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".$programid."' ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $flg="") {
        $query  = " SELECT ";
        $query .= "     T2.MENUID, ";
        $query .= "     T1.PROGRAMID, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     PRGINFO_PROPERTIES T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MENU_MST T2 ON T2.PROGRAMID = T1.PROGRAMID ";
        $query .= " WHERE ";
        $query .= "     T1.PROGRAMID   = '".$model->prg_id."' AND ";
        $query .= "     T1.NAME        = '".$model->name ."' ";
        if($flg == ""){
            $query .= "     AND T2.MENUID  = '".$model->menuid ."' ";
        }

        return $query;
    }

    //メニュー取得
    function getMenuMst() {
        $query  = " SELECT ";
        $query .= "     MENUID, ";
        $query .= "     MENUNAME, ";
        $query .= "     PROGRAMID AS PRG_ID ";
        $query .= " FROM ";
        $query .= "     MENU_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID IS NOT NULL AND ";
        $query .= "     PROGRAMID <> 'TITLE' AND ";
        $query .= "     MENUID IN ( SELECT ";
        $query .= "                     T2.MENUID ";
        $query .= "                 FROM ";
        $query .= "                     USERGROUP_DAT T1, ";
        $query .= "                     GROUPAUTH_DAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     T1.GROUPCD = T2.GROUPCD AND ";
        $query .= "                     T1.STAFFCD = '".STAFFCD."' AND ";
        $query .= "                     INT(T2.GROUPAUTH) < 4 ";
        $query .= "                 UNION    ";
        $query .= "                 SELECT ";
        $query .= "                     MENUID ";
        $query .= "                 FROM ";
        $query .= "                     USERAUTH_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     STAFFCD = '".STAFFCD."' AND ";
        $query .= "                     INT(USERAUTH) < 4 ";
        $query .= "                 ORDER BY MENUID ";
        $query .= "                ) ";
        $query .= " ORDER BY ";
        $query .= "     MENUID ";

        return $query;
    }

    //プログラムＩＤ取得
    function getProgramID($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     PROGRAMID ";
        $query .= " FROM ";
        $query .= "     MENU_MST ";
        $query .= " WHERE ";
        $query .= "     MENUID = '".$model->field["MENUID"]."' ";

        $prg_id = $db->getOne($query);
        Query::dbCheckIn($db);
        return $prg_id;
    }

    //重複チェック
    function chkDuplicationData($model) {
        $prg_id = ($model->field["INPUT"] == "1") ? knjz900Query::getProgramID($model): $model->field["PROGRAMID"];
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PRGINFO_PROPERTIES ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID   = '".$prg_id."' AND ";
        $query .= "     NAME        = '".$model->field["NAME"]."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($model) {

        $prg_id = ($model->field["INPUT"] == "1") ? knjz900Query::getProgramID($model) : $model->field["PROGRAMID"];
        if($model->cmd == "update"){
            knjz900Query::getDeleteQuery($model, $prg_id);   //update時
        }

        $db = Query::dbCheckOut();

        $data["PROGRAMID"][TEXT]    = $prg_id;
        $data["NAME"][TEXT]         = $model->field["NAME"];
        $data["VALUE"][TEXT]        = $model->field["VALUE"];
        $data["REMARK"][TEXT]       = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "PRGINFO_PROPERTIES");
        $db->query($query);

        Query::dbCheckIn($db);
        return $prg_id;
    }

    //DELETE
    function &getDeleteQuery($model, $prg_id) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM PRGINFO_PROPERTIES ";
        $query .= " WHERE PROGRAMID     = '".$prg_id."' AND ";
        $query .= "            NAME     = '".$model->field["NAME"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    /**************/
    /*  一括処理  */
    /**************/

    //プロパティー一覧取得
    function getList2() {
        $query  = " WITH MENU AS( ";
        $query .= "     SELECT ";
        $query .= "         PROGRAMID, ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_MST ";
        $query .= "     GROUP BY ";
        $query .= "         PROGRAMID ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.PROGRAMID AS PRG_ID, ";
        $query .= "     CASE VALUE(S2.MENUNAME,'') WHEN '' THEN T1.PROGRAMID ELSE S2.MENUNAME END MENUNAME, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     PRGINFO_PROPERTIES T1 ";
        $query .= "     LEFT JOIN MENU S1 ON T1.PROGRAMID = S1.PROGRAMID ";
        $query .= "     LEFT JOIN MENU_MST S2 ON S1.MENUID = S2.MENUID ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.PROGRAMID ";

        return $query;
    }

    //プロパティー数取得
    function getPrgNameCnt2($name, $value) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     PRGINFO_PROPERTIES ";
        $query .= " WHERE ";
        $query .= "     NAME = '".$name."' AND ";
        $query .= "     VALUE(VALUE,'') = '".$value."' ";

        return $query;
    }

    //１レコード取得
    function getRow2($model) {

        $query  = " SELECT ";
        $query .= "     NAME, ";
        $query .= "     VALUE, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     PRGINFO_PROPERTIES ";
        $query .= " WHERE ";
        $query .= "     NAME            = '".$model->name ."' AND ";
        $query .= "     VALUE(VALUE,'') = '".$model->value ."' ";

        return $query;
    }

    //対象プログラム取得
    function getProgramList($model, $side="") {
        $query  = " WITH MENU AS( ";
        $query .= "     SELECT ";
        $query .= "         PROGRAMID, ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID IS NOT NULL ";
        $query .= "     GROUP BY ";
        $query .= "         PROGRAMID ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.PROGRAMID AS VALUE, ";
        $query .= "     T1.PROGRAMID || ' ' || T2.MENUNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MENU T1 ";
        $query .= "     LEFT JOIN MENU_MST T2 ON T1.MENUID = T2.MENUID ";
        $query .= " WHERE ";
        if($side == "left"){
            $query .= "     T1.PROGRAMID IN (   SELECT ";
            $query .= "                             PROGRAMID ";
            $query .= "                         FROM ";
            $query .= "                             PRGINFO_PROPERTIES ";
            $query .= "                         WHERE ";
            $query .= "                             NAME = '".$model->name."' AND ";
            $query .= "                             VALUE(VALUE,'') = '".$model->value."') ";
            $query .= " UNION ";
            $query .= " SELECT DISTINCT ";
            $query .= "     PROGRAMID AS VALUE, ";
            $query .= "     PROGRAMID AS LABEL ";
            $query .= " FROM ";
            $query .= "     PRGINFO_PROPERTIES ";
            $query .= " WHERE ";
            $query .= "     PROGRAMID IN (  SELECT ";
            $query .= "                         PROGRAMID ";
            $query .= "                     FROM ";
            $query .= "                         PRGINFO_PROPERTIES ";
            $query .= "                     WHERE ";
            $query .= "                             NAME = '".$model->name."' AND ";
            $query .= "                             VALUE(VALUE,'') = '".$model->value."') AND ";
            $query .= "     PROGRAMID NOT IN (  SELECT ";
            $query .= "                             VALUE(PROGRAMID,'') AS PROGRAMID ";
            $query .= "                         FROM ";
            $query .= "                             MENU_MST) ";
        } else {
            $query .= "     T1.PROGRAMID NOT IN(SELECT ";
            $query .= "                             PROGRAMID ";
            $query .= "                         FROM ";
            $query .= "                             PRGINFO_PROPERTIES ";
            $query .= "                         WHERE ";
            $query .= "                             NAME = '".$model->name."' AND ";
            $query .= "                             VALUE(VALUE,'') = '".$model->value."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新
    function &getUpdateSubQuery1($model) {

        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM PRGINFO_PROPERTIES ";
        $query .= " WHERE NAME            = '".$model->field["NAME"]."' AND ";
        $query .= "       VALUE(VALUE,'') = '".$model->field["VALUE"]."' ";
        $db->query($query);

        $query  = " DELETE FROM PRGINFO_PROPERTIES ";
        $query .= " WHERE NAME  = '".$model->field["NAME"]."' AND ";
        $query .= "       PROGRAMID IN ('".implode("','", explode(',', $model->selectdata))."') ";
        $db->query($query);

        //更新
        if($model->selectdata != ""){
            $prg_id = explode(",", $model->selectdata);
            for ($i = 0; $i < get_count($prg_id); $i++) {

                $data["PROGRAMID"][TEXT]    = $prg_id[$i];
                $data["NAME"][TEXT]         = $model->field["NAME"];
                $data["VALUE"][TEXT]        = $model->field["VALUE"];
                $data["REMARK"][TEXT]       = $model->field["REMARK"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "PRGINFO_PROPERTIES");
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
