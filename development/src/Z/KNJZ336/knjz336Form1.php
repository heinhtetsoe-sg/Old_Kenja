<?php

require_once('for_php7.php');

class knjz336Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz336index.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz336Form1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $result = $db->query(knjz336Query::getList($model));

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
         array_walk($row, "htmlspecialchars_array");
         $arg["data"][] = $row;
    }

    $result->free();
}
//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
?>
