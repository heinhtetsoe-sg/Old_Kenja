<?php

require_once('for_php7.php');


class knjs570Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjs570Form1", "POST", "knjs570index.php", "", "knjs570Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボボックス
        $query = knjs570Query::selectYearQuery($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "");

        //学年コンボ
        $query = knjs570Query::getGrade($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "");

        //リストTOリスト
        makeListToList($arg, $objForm, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs570Form1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function makeListToList(&$arg, &$objForm, $db, $model) {

    $leftOpt = array();
    $rightOpt = array();
    $selectOpt = array();

    //選択済み科目
    $result = $db->query(knjs570Query::selectUnitSubclassQuery($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $selectOpt[] = $row["VALUE"];

    }
    $result->free();

    if ($model->field["YEAR"] && $model->field["GRADE"]) {
        $result = $db->query(knjs570Query::selectSubclassQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["VALUE"], $selectOpt)) {
                $leftOpt[] = array("label" => $row["LABEL"],
                                   "value" => $row["VALUE"]);
            } else {
                $rightOpt[] = array("label" => $row["LABEL"],
                                    "value" => $row["VALUE"]);
            }
        }
        $result->free();
    }

    //出力対象クラスリスト
    $extra = "multiple style=\"width:220px\" width:\"220px\" ondblclick=\"move('left','SUBCLASS_SELECTED','SUBCLASS_NAME',1)\"";
    $arg["data"]["SUBCLASS_NAME"] = knjCreateCombo($objForm, "SUBCLASS_NAME", "", $rightOpt, $extra, 20);

    //選択済み科目
    $extra = "multiple style=\"width:220px\" width:\"220px\" ondblclick=\"move('right','SUBCLASS_SELECTED','SUBCLASS_NAME',1)\"";
    $arg["data"]["SUBCLASS_SELECTED"] = knjCreateCombo($objForm, "SUBCLASS_SELECTED", "", $leftOpt, $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('sel_del_all','SUBCLASS_SELECTED','SUBCLASS_NAME',1);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('sel_add_all','SUBCLASS_SELECTED','SUBCLASS_NAME',1);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('right','SUBCLASS_SELECTED','SUBCLASS_NAME',1);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('left','SUBCLASS_SELECTED','SUBCLASS_NAME',1);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //更新ボタンを作成する
    $extra = "onClick=\"return btn_submit('update')\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    
    //前年度からコピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
    
}

?>
