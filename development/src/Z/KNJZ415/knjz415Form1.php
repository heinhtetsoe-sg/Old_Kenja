<?php

require_once('for_php7.php');

class knjz415Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm      = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz415index.php", "", "list");

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
        View::toHTML($model, "knjz415Form1.html", $arg); 
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
    $query = knjz415Query::GetLGroup();
    $result = $db->query($query);
    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //レコードを連想配列のまま配列に追加していく。
        array_walk($row, "htmlspecialchars_array");
        if ($key !== $row["L_GROUPCD"]) {
            $cnt = get_count($db->getCol(knjz415Query::GetLGroup($row["L_GROUPCD"])));
            $row["ROWSPAN"] = ($cnt) > 0 ? $cnt : 1;
        }
        $key = $row["L_GROUPCD"];

        $row["SEP"] = ($row["INDUSTRY_LCD"]) ? ":" : "";

        if ($row["L_GROUPCD"] == $model->industry_lcd) {
            $row["L_GROUPNAME"] = ($row["L_GROUPNAME"]) ? $row["L_GROUPNAME"] : "　";
            $row["L_GROUPNAME"] = "<a name=\"target\">{$row["L_GROUPNAME"]}</a><script>location.href='#target';</script>";
        }

        $arg["data"][] = $row;
    }
    $result->free();
}
?>
