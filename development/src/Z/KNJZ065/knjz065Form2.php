<?php

require_once('for_php7.php');

class knjz065Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz065index.php", "", "edit");

        //各フィールド取得
        if (isset($model->year) && isset($model->semester) && isset($model->gakubu_school_kind) && isset($model->hr_class) && isset($model->hr_class2) && isset($model->condition) && isset($model->groupcd) && !isset($model->warning)) {
            if ($model->cmd === 'edit2') {
                $Row = knjz065Query::getUnitGroupYmst($model);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //状態表示
        if ($model->condition) {
            $arg["CONDITION"] = $db->getOne(knjz065Query::getCondition($model->condition));
        }
        //グループコード
        if ($model->condition != "" && $model->groupcd != "") {
            $arg["GROUPCD"] = $db->getOne(knjz065Query::getGroupcd($model));
        }
        //科目表示
        $arg["SET_SUBCLASSCD"] = $db->getOne(knjz065Query::getSubclassMst($model));
        

        //単元コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["UNITCD"] = knjCreateTextBox($objForm, $Row["UNITCD"], "UNITCD", 2, 2, $extra);
        
        //名称
        $extra = "";
        $arg["UNITNAME"] = knjCreateTextBox($objForm, $Row["UNITNAME"], "UNITNAME", 60, 90, $extra);
        
        //略称
        $extra = "";
        $arg["UNITABBV"] = knjCreateTextBox($objForm, $Row["UNITABBV"], "UNITABBV", 45, 60, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knjz065index.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz065Form2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    $opt[] = array('label' => "",'value' => "");
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('edit2');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
