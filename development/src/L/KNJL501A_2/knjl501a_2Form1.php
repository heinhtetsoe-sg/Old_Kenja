<?php

require_once('for_php7.php');

class knjl501a_2Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl501a_2index.php", "", "edit");

        //権限チェック
        authCheck($arg, $model);

        //DB接続
        $db = Query::dbCheckOut();

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl501a_2Form1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $bifKey = "";
    $query = knjl501a_2Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        $arg["data"][] = $row;
    }
    $result->free();
}

//権限チェック
function authCheck(&$arg, $model) {
    if ($model->auth != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

?>
