<?php

require_once('for_php7.php');

class knjp709Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp709index.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)) {
            $Row = knjp709Query::getRow($model, 1);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            //校種コンボ
            $query = knjp709Query::getSchkind($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1, "");
        }

        if ($model->Properties["unUseIncomeRank"] == "1") {
            $arg["unUseIncomeRank"] = "1";
        } else {
            $arg["useIncomeRank"] = "1";
        }

        //授業料区分コンボ
        $query = knjp709Query::getSchooldiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SCHOOLDIV", $Row["SCHOOLDIV"], $extra, 1, "");

        //学年コンボ
        $result    = $db->query(knjp709Query::getGrade($model, $model->year));
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["GRADE"],
                           "value" => $row["GRADE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $Row["GRADE"], $opt, $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/
        for ($i = 1; $i <= 2; $i++) {
            //基準額
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["REDUCTIONMONEY".$i] = knjCreateTextBox($objForm, $Row["REDUCTIONMONEY".$i], "REDUCTIONMONEY".$i, 8, 8, $extra);
            //加算額
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["REDUCTION_ADD_MONEY".$i] = knjCreateTextBox($objForm, $Row["REDUCTION_ADD_MONEY".$i], "REDUCTION_ADD_MONEY".$i, 8, 8, $extra);
            //課税総所得下限額
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["INCOME_LOW".$i] = knjCreateTextBox($objForm, $Row["INCOME_LOW".$i], "INCOME_LOW".$i, 8, 8, $extra);
            //課税総所得上限額
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["INCOME_HIGH".$i] = knjCreateTextBox($objForm, $Row["INCOME_HIGH".$i], "INCOME_HIGH".$i, 8, 8, $extra);
            //ランク
            $query = knjp709Query::getNameMst(CTRL_YEAR, "P001", "");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "INCOME_RANK".$i, $Row["INCOME_RANK".$i], $extra, 1, "BLANK");
        }
        //備考
        $extra = "";
        $arg["data"]["REDUCTIONREMARK"] = knjCreateTextBox($objForm, $Row["REDUCTIONREMARK"], "REDUCTIONREMARK", 51, 50, $extra);

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjp709index.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp709Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
