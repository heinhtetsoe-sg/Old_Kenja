<?php

require_once('for_php7.php');

class knjh530_2Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh530_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knjh530_2Query::getProficiency($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["PROFICIENCYDIV"] = $Row["PROFICIENCYDIV"];
            $Row["PROFICIENCYCD"] = $Row["PROFICIENCYCD"];
        } else {
            $Row =& $model->field;
        }

        //実力テスト種別
        $query = knjh530_2Query::getProficiencyDiv();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYDIV"], "PROFICIENCYDIV", $extra, 1, "BLANK");

        //実力テストコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PROFICIENCYCD"] = knjCreateTextBox($objForm, $Row["PROFICIENCYCD"], "PROFICIENCYCD", 4, 4, $extra);

        //実力テスト名称
        $arg["data"]["PROFICIENCYNAME1"] = knjCreateTextBox($objForm, $Row["PROFICIENCYNAME1"], "PROFICIENCYNAME1", 40, 40, "");

        //実力テスト略称
        $arg["data"]["PROFICIENCYNAME2"] = knjCreateTextBox($objForm, $Row["PROFICIENCYNAME2"], "PROFICIENCYNAME2", 40, 40, "");

        //実力テスト名称３
        $arg["data"]["PROFICIENCYNAME3"] = knjCreateTextBox($objForm, $Row["PROFICIENCYNAME3"], "PROFICIENCYNAME3", 40, 40, "");

        //集計フラグ
        $extra = $Row["COUNTFLG"] == "1" || !isset($model->proficiencyDiv) ? "checked" : "";
        $arg["data"]["COUNTFLG"] = knjCreateCheckBox($objForm, "COUNTFLG", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjh530_2index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh530_2Form2.html", $arg); 
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $link  = REQUESTROOT."/H/KNJH530/knjh530index.php?cmd=back";
    $link .= "&year_code={$model->parentParam["year_code"]}";
    $link .= "&semester_code={$model->parentParam["semester_code"]}";
    $link .= "&grade_code={$model->parentParam["grade_code"]}";
    $link .= "&proficiencydiv_code={$model->parentParam["proficiencydiv_code"]}";
    $link .= "&year_add_code={$model->parentParam["year_add_code"]}";
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row)
{
    knjCreateHidden($objForm, "cmd");

}

?>
