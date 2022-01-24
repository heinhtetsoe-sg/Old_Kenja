<?php

require_once('for_php7.php');
class knjd415Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd415index.php", "", "edit");

        //各フィールド取得
        if (isset($model->year) && isset($model->condition) && isset($model->groupcd) && isset($model->groupname) && !isset($model->warning)) {
            if ($model->cmd === 'edit2') {
                $Row = knjd415Query::getCompGroupYMst($model);
            } else {
                $Row =& $model->field;
                if($model->cmd === 'delete_after'){
                    $Row["CLASSGROUP_CD"] = '';
                    $Row["CLASSGROUP_NAME"] = '';
                }
            }
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //状態区分コンボ作成
        $query = knjd415Query::getCondition();
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "CONDITION", $Row["CONDITION"], $extra, 1, $model);

        //グループコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["CLASSGROUP_CD"] = knjCreateTextBox($objForm, $Row["CLASSGROUP_CD"], "CLASSGROUP_CD", 2, 2, $extra);
        $arg["CLASSGROUP_CD_COMMENT"] = "(半角数字{$model->cd_moji}桁)";
        
        //名称
        $extra = "";
        $arg["CLASSGROUP_NAME"] = knjCreateTextBox($objForm, $Row["CLASSGROUP_NAME"], "CLASSGROUP_NAME", 20, 10, $extra);
        $arg["CLASSGROUP_NAME_COMMENT"] = "(全角{$model->name_moji}文字まで)";

        //リストToリスト作成
        if ($Row["CLASSGROUP_CD"] == $model->groupcd && $Row["CONDITION"] == $model->condition) {
            makeListToList($objForm, $arg, $db, $model, $Row["CONDITION"], $Row["CLASSGROUP_CD"], "");
        } else {
            makeListToList($objForm, $arg, $db, $model, $Row["CONDITION"], "", "");
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        if (VARS::post("cmd") == "add" || VARS::post("cmd") == "update" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knjd415index.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd415Form2.html", $arg);
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
    $selectdata2 = '';

    //対象科目一覧取得
    $query = knjd415Query::getCompGroupDat($model, $setcondition, $setgroupcd, $setName);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        $selected[] = $row["VALUE"];
        if($selectdata2 == ''){
            $selectdata2 .= $row["VALUE"];
        } else {
            $selectdata2 .= ',';
            $selectdata2 .= $row["VALUE"];
        }
    }
    knjCreateHidden($objForm, "selectdata2", $selectdata2);
    $result->free();

    //対象科目一覧作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', 'SELECTED', 'NAME', 1);\"";
    $arg["data"]["SELECTED"] = knjCreateCombo($objForm, "SELECTED", "", $opt_left, $extra, 20);

    //科目一覧取得
    $query = knjd415Query::getGradeKindCompSubclassMst($model, $setcondition, $setName, $query);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selected)) continue;
        $opt_right[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
    }
    $result->free();

    //科目一覧作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left','SELECTED','NAME',1);\"";
    $arg["data"]["NAME"] = knjCreateCombo($objForm, "NAME", "", $opt_right, $extra, 20);

    //選択ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_add_all','SELECTED','NAME',1);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //選択ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('left','SELECTED','NAME',1);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //取消ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('right','SELECTED','NAME',1);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //取消ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_del_all','SELECTED','NAME',1);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //追加ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);    
    //更新ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('update');\"";
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
    knjCreateHidden($objForm, "selectdata");
}
?>
