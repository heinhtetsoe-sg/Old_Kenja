<?php
class knjj512Form2
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjj512index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "edit") {
            $model->field["AGE"] = $model->age;
        }

        if (!isset($model->warning) && $model->field["AGE"]) {
            $query = knjj512Query::getSportsTotalValueBaseMst($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //年齢コンボ
        $opt = array();
        $opt[] = array('label' => "12歳", 'value' => "12");
        $opt[] = array('label' => "13歳", 'value' => "13");
        $opt[] = array('label' => "14歳", 'value' => "14");
        $opt[] = array('label' => "15歳", 'value' => "15");
        $opt[] = array('label' => "16歳", 'value' => "16");
        $opt[] = array('label' => "17歳", 'value' => "17");
        $opt[] = array('label' => "18歳", 'value' => "18");
        $opt[] = array('label' => "19歳", 'value' => "19");
        $extra = "onchange=\"return btn_submit('chenge');\"";
        $Row["AGE"] = ($Row["AGE"]) ? $Row["AGE"] : ($model->field["AGE"]) ? $model->field["AGE"] : $opt[0]["value"];
        $arg["data"]["AGE"] = knjCreateCombo($objForm, "AGE", $Row["AGE"], $opt, $extra, $size);

        //A～E
        $extra = "onblur=\"CodeCheck(this);\"";
        $arg["data"]["TOTAL_SCORE_HIGH_A"] = knjCreateTextBox($objForm, $Row["TOTAL_SCORE_HIGH_A"], "TOTAL_SCORE_HIGH_A", 3, 3, $extra);
        $arg["data"]["TOTAL_SCORE_LOW_B"] = knjCreateTextBox($objForm, $Row["TOTAL_SCORE_LOW_B"], "TOTAL_SCORE_LOW_B", 3, 3, $extra);
        $arg["data"]["TOTAL_SCORE_HIGH_B"] = knjCreateTextBox($objForm, $Row["TOTAL_SCORE_HIGH_B"], "TOTAL_SCORE_HIGH_B", 3, 3, $extra);
        $arg["data"]["TOTAL_SCORE_LOW_C"] = knjCreateTextBox($objForm, $Row["TOTAL_SCORE_LOW_C"], "TOTAL_SCORE_LOW_C", 3, 3, $extra);
        $arg["data"]["TOTAL_SCORE_HIGH_C"] = knjCreateTextBox($objForm, $Row["TOTAL_SCORE_HIGH_C"], "TOTAL_SCORE_HIGH_C", 3, 3, $extra);
        $arg["data"]["TOTAL_SCORE_LOW_D"] = knjCreateTextBox($objForm, $Row["TOTAL_SCORE_LOW_D"], "TOTAL_SCORE_LOW_D", 3, 3, $extra);
        $arg["data"]["TOTAL_SCORE_HIGH_D"] = knjCreateTextBox($objForm, $Row["TOTAL_SCORE_HIGH_D"], "TOTAL_SCORE_HIGH_D", 3, 3, $extra);
        $arg["data"]["TOTAL_SCORE_LOW_E"] = knjCreateTextBox($objForm, $Row["TOTAL_SCORE_LOW_E"], "TOTAL_SCORE_LOW_E", 3, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjj512index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj512Form2.html", $arg);
    }
}


//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row[VALUE]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "TOTAL_SCORE_LOW_A", "999");  //A 下限 '999'固定
    knjCreateHidden($objForm, "TOTAL_SCORE_HIGH_E", "0"); //E 上限 '0'固定
}
