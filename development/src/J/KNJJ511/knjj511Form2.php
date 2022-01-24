<?php
class knjj511Form2
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjj511index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "edit") {
            $model->field["ITEMCD"] = $model->itemcd;
            $model->field["SEX"] = $model->sex;
        }

        //種別コンボ
        $model->field["ITEMCD"] = ($model->field["ITEMCD"] == "") ? "" : $model->field["ITEMCD"];
        $extra = "onchange=\"return btn_submit('chenge');\" ";
        $query = knjj511Query::getSportsItemMst($model);
        makeCmb($objForm, $arg, $db, $query, "ITEMCD", $model->field["ITEMCD"], $extra, 1);

        //性別コンボ
        $model->field["SEX"] = ($model->field["SEX"] == "") ? "" : $model->field["SEX"];
        $extra = "onchange=\"return btn_submit('chenge');\" ";
        $query = knjj511Query::getNameMst($model->year, "Z002");
        makeCmb($objForm, $arg, $db, $query, "SEX", $model->field["SEX"], $extra, 1);

        if (!isset($model->warning) && $model->field["ITEMCD"] && $model->field["SEX"]) {
            $query = knjj511Query::getSportsItemScoreBaseMst($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //単位
        if ($Row["UNIT"] != "") {
            $arg["data"]["UNIT"] = "単位：".$Row["UNIT"];
        }

        //10～1
        // $extra = "onblur=\"CodeCheck(this);\"";
        $extra = "onblur=\"calc(this);\"";
        foreach ($model->totalLevel as $lebel => $val) {
            $low   = "RECORD_LOW_".$val;
            $high  = "RECORD_HIGH_".$val;
            if ($val != "10") {
                $arg["data"][$low] = knjCreateTextBox($objForm, $Row[$low], $low, 8, 8, $extra);
            }
            if ($val != "1") {
                $arg["data"][$high] = knjCreateTextBox($objForm, $Row[$high], $high, 8, 8, $extra);
            }
        }

        //基準
        $arg["data"]["CRITERIA1"]  = "以上";
        $arg["data"]["CRITERIA2"]  = "以下";
        if ($Row["ITEMCD"] == "006" || $Row["ITEMCD"] == "008") {
            $arg["data"]["CRITERIA1"]  = "以下";
            $arg["data"]["CRITERIA2"]  = "以上";
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjj511index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj511Form2.html", $arg);
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
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "RECORD_LOW_10", "999"); //10 下限 '999'固定
    knjCreateHidden($objForm, "RECORD_HIGH_1", "0"); //1 上限 '0'固定
}
