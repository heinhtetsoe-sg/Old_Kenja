<?php

require_once('for_php7.php');

class knjz517Form1 {

    function main(&$model) {

        $arg["jscript"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz517index.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["authcheck"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //圏域コンボ
        $kyoto = $db->getOne(knjz517Query::getZ010());
        $arg["AREA_NAME"] = ($kyoto == 'kyoto') ? "圏域" : "地区";
        $opt = array();
        $value_flg = false;
        $query = knjz517Query::getNameMst();
        $result = $db->query($query);
        $opt[] = array('label' => "", 'value' => "NULL");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->areacd == $row["VALUE"]) $value_flg = true;
        }
        $opt[] = array('label' => "-- 全て --", 'value' => "ALL");
        $model->areacd = ($model->areacd && $value_flg) ? $model->areacd : (($model->areacd == "NULL" || !$model->areacd) ? $opt[0]["value"] : $model->areacd);
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["AREACD"] = knjCreateCombo($objForm, "LEFT_AREACD", $model->areacd, $opt, $extra, 1);

        //リスト作成
        makeList($arg, $db, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        if (VARS::get("shori") != "update") {
            $arg["jscript"] = "window.open('knjz517index.php?cmd=edit','right_frame')";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz517Form1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $query = knjz517Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        $arg["data"][] = $row;
    }
    $result->free();
}
?>
