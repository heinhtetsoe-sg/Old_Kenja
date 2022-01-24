<?php

require_once('for_php7.php');

class knjg082Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjg082index.php", "", "list");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //グループリスト
        makeGroupList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjg082Form1.html", $arg); 
    }
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//グループリスト
function makeGroupList(&$arg, $db, $model) {
    $g_cnt = 1;
    $result = $db->query(knjg082Query::getGroupList());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //行数取得
        $group_cnt  = $db->getOne(knjg082Query::getGroupCnt($row["GROUP_DIV"]));
        if ($g_cnt == 1) $row["ROWSPAN1"] = $group_cnt;     //グループの行数

        $arg["data"][] = $row;

        if ($g_cnt == $group_cnt) {
            $g_cnt = 1;
        } else {
            $g_cnt++;
        }
    }
    $result->free();

}
?>
