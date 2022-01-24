<?php

require_once('for_php7.php');

class knjz462Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz462index.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjz462Query::getSchkind($model);
            $extra = "onchange=\"return Sending();\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schkind, $extra, 1);
        }

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz462Form1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $bifKey = "";
    $query = knjz462Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        if ($bifKey !== $row["ITEMCD"]) {
            $cnt = $db->getOne(knjz462Query::getItemCnt($model, $row["ITEMCD"]));
            $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
        }
        $bifKey = $row["ITEMCD"];

        //更新後この行が画面の先頭に来るようにする
        if ($row["ITEMCD"] == $model->itemcd) {
            $row["ITEMNAME"] = ($row["ITEMNAME"]) ? $row["ITEMNAME"] : "　";
            $row["ITEMNAME"] = "<a name=\"target\">{$row["ITEMNAME"]}</a><script>location.href='#target';</script>";
        }

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
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
