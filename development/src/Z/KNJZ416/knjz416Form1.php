<?php

require_once('for_php7.php');

class knjz416Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm      = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz416index.php", "", "list");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //大分類グループリスト
        makeList($arg, $db, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz416Form1.html", $arg); 
    }
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//大分類グループリスト
function makeList(&$arg, $db, $model) {
    $key = "";
    $query = knjz416Query::getMGroup();
    $result = $db->query($query);
    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //レコードを連想配列のまま配列に追加していく。
        array_walk($row, "htmlspecialchars_array");
        if ($key !== $row["M_GROUPCD"]) {
            $cnt = get_count($db->getCol(knjz416Query::getMGroup($row["M_GROUPCD"])));
            $row["ROWSPAN"] = ($cnt) > 0 ? $cnt : 1;
        }
        $key = $row["M_GROUPCD"];

        $row["MSEP"] = ($row["INDUSTRY_MCD"]) ? ":" : "";
        $row["LSEP"] = ($row["INDUSTRY_LCD"]) ? ":" : "";

        if ($row["M_GROUPCD"] == $model->industry_lcd) {
            $row["M_GROUPNAME"] = ($row["M_GROUPNAME"]) ? $row["M_GROUPNAME"] : "　";
            $row["M_GROUPNAME"] = "<a name=\"target\">{$row["M_GROUPNAME"]}</a><script>location.href='#target';</script>";
        }

        $arg["data"][] = $row;
    }
    $result->free();
}
?>
