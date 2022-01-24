<?php

require_once('for_php7.php');

class knjz020pForm2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz020pindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //左画面の入試区分と同じ値を取得
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $model->testdiv = ($model->cmd != "" || $model->applicantdiv == $model->field["APP_HOLD"]) ? $model->testdiv : "";
        $query = knjz020pQuery::getNameMst($model, $namecd1);
        makeCombo2($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset") && $model->year && $model->applicantdiv && $model->testdiv && $model->totalcd && $model->testsubclasscd) {
            $query = knjz020pQuery::getRow($model->year, $model->applicantdiv, $model->testdiv, $model->totalcd, $model->testsubclasscd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /**************/
        /**コンボ作成**/
        /**************/
        //試験コースコンボ
        $query = knjz020pQuery::getEntExamCourse($model);
        makeCombo($objForm, $arg, $db, $query, $Row["TOTALCD"], "TOTALCD", "", 1, "blank");

        //試験科目コンボ
        $query = knjz020pQuery::getTestSubclass($model);
        makeCombo($objForm, $arg, $db, $query, $Row["TESTSUBCLASSCD"], "TESTSUBCLASSCD", "", 1, "blank");

        /****************/
        /**テキスト作成**/
        /****************/
        //満点テキストボックス
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PERFECT"] = knjCreateTextBox($objForm, $Row["PERFECT"], "PERFECT", 3, 3, $extra);

        /**************/
        /**ボタン作成**/
        /**************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '終 了', $extra);

        /**************/
        /**hidden作成**/
        /**************/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz020pindex.php?cmd=list2"
                            . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."&testdiv=".$model->testdiv."';";

        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz020pForm2.html", $arg);
    }
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = false) {
    $opt = array();
    $value_flg = false;
    if ($blank == "blank") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//左画面の入試区分と同じ値を取得
function makeCombo2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

//    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
