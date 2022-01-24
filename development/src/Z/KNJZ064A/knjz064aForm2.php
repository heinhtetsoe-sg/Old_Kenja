<?php

require_once('for_php7.php');

class knjz064aForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz064aindex.php", "", "edit");

        //各フィールド取得
        if (isset($model->year) && isset($model->semester) && isset($model->gakubu_school_kind) &&  isset($model->hr_class) &&  isset($model->hr_class2) && isset($model->condition) && isset($model->groupcd) && !isset($model->warning)) {
            if ($model->cmd === 'edit2') {
                $Row = knjz064aQuery::getCompGroupYMst($model);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //状態区分コンボ作成
        $query = knjz064aQuery::getCondition();
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "CONDITION", $Row["CONDITION"], $extra, 1, $model);

        //グループコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["GROUPCD"] = knjCreateTextBox($objForm, $Row["GROUPCD"], "GROUPCD", 4, 4, $extra);
        
        //名称
        $extra = "";
        $arg["GROUPNAME"] = knjCreateTextBox($objForm, $Row["GROUPNAME"], "GROUPNAME", 45, 60, $extra);
        
        //略称
        $extra = "";
        $arg["GROUPABBV"] = knjCreateTextBox($objForm, $Row["GROUPABBV"], "GROUPABBV", 45, 60, $extra);

        //指導計画帳票パターンコンボ作成(プロパティが立っていたら、非表示)
        if ($model->Properties["unuseEduPlan_Group_GuidancePattern"] == "1") {
            $arg["USE_GUIDANCEPATTERN"] = 0;
        } else {
            $arg["USE_GUIDANCEPATTERN"] = 1;
            $query = knjz064aQuery::getGuidancePattern($model, $Row);
            makeCmb($objForm, $arg, $db, $query, "GUIDANCE_PATTERN", $Row["GUIDANCE_PATTERN"], "", 1, $model);
        }

        //リストToリスト作成
        if ($Row["GROUPCD"] == $model->groupcd && $Row["CONDITION"] == $model->condition) {
            makeListToList($objForm, $arg, $db, $model, $Row["CONDITION"], $Row["GROUPCD"], "CATEGORY");
            makeListToList($objForm, $arg, $db, $model, $Row["CONDITION"], $Row["GROUPCD"], "NERAI");
        } else {
            makeListToList($objForm, $arg, $db, $model, $Row["CONDITION"], "", "CATEGORY");
            makeListToList($objForm, $arg, $db, $model, $Row["CONDITION"], "", "NERAI");
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knjz064aindex.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz064aForm2.html", $arg);
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

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $setcondition, $setgroupcd, $setName) {
    $opt_right = $opt_left = array();
    $selected = array();

    //対象科目一覧取得
    $query = knjz064aQuery::getCompGroupDat($model, $setcondition, $setgroupcd, $setName);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        $selected[] = $row["VALUE"];
    }
    $result->free();

    //対象科目一覧作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', '{$setName}_SELECTED', '{$setName}_NAME', 1);\"";
    $arg["data"]["{$setName}_SELECTED"] = knjCreateCombo($objForm, "{$setName}_SELECTED", "", $opt_left, $extra, 20);

    //科目一覧取得
    $query = knjz064aQuery::getGradeKindCompSubclassMst($model, $setcondition, $setName, $query);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selected)) continue;
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $result->free();

    //科目一覧作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left','{$setName}_SELECTED','{$setName}_NAME',1);\"";
    $arg["data"]["{$setName}_NAME"] = knjCreateCombo($objForm, "{$setName}_NAME", "", $opt_right, $extra, 20);

    //選択ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_add_all','{$setName}_SELECTED','{$setName}_NAME',1);\"";
    $arg["button"]["{$setName}btn_lefts"] = knjCreateBtn($objForm, "{$setName}btn_lefts", "<<", $extra);
    //選択ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('left','{$setName}_SELECTED','{$setName}_NAME',1);\"";
    $arg["button"]["{$setName}btn_left1"] = knjCreateBtn($objForm, "{$setName}btn_left1", "＜", $extra);
    //取消ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('right','{$setName}_SELECTED','{$setName}_NAME',1);\"";
    $arg["button"]["{$setName}btn_right1"] = knjCreateBtn($objForm, "{$setName}btn_right1", "＞", $extra);
    //取消ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_del_all','{$setName}_SELECTED','{$setName}_NAME',1);\"";
    $arg["button"]["{$setName}btn_rights"] = knjCreateBtn($objForm, "{$setName}btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //追加ボタン1
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return doSubmit('1');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //追加ボタン2
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return doSubmit('2');\"";
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra);
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
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdata2");
}
?>
