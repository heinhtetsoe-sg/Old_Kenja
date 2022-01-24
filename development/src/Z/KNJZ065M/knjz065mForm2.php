<?php

require_once('for_php7.php');

class knjz065mForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz065mindex.php", "", "edit");

        //各フィールド取得
        if (isset($model->year) && isset($model->semester) && isset($model->gakubu_school_kind) && isset($model->ghr_cd) && isset($model->grade) && isset($model->hr_class) && isset($model->condition) && isset($model->groupcd) && isset($model->unitcd) && !isset($model->warning)) {
            if ($model->cmd === 'edit2') {
                $Row = knjz065mQuery::getUnitGroupYmstRow($model);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //校種
        $query =  knjz065mQuery::getSchkind($model, $model->gakubu_school_kind);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["SCHOOL_KIND_NAME"] = $row["LABEL"];

        //状態区分コンボ作成
        $query = knjz065mQuery::getCondition();
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "CONDITION", $Row["CONDITION"], $extra, 1, $model);

        //科目グループ名コンボ作成
        $query = knjz065mQuery::getGroupNameCombo($model, $Row["CONDITION"]);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "GROUPCD", $Row["GROUPCD"], $extra, 1, $model);

        //科目コンボ作成
        $query = knjz065mQuery::getSubclassCombo($model, $Row["CONDITION"], $Row["GROUPCD"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SET_SUBCLASSCD", $Row["SET_SUBCLASSCD"], $extra, 1, $model);

        //単元コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["UNITCD"] = knjCreateTextBox($objForm, $Row["UNITCD"], "UNITCD", 2, 2, $extra);

        //単元名
        $extra = "";
        $arg["UNITNAME"] = knjCreateTextBox($objForm, $Row["UNITNAME"], "UNITNAME", 40, 20, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm);

        if (VARS::post("cmd") == "insert" || VARS::post("cmd") == "update" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knjz065mindex.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz065mForm2.html", $arg);
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
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return doSubmit('insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return doSubmit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
