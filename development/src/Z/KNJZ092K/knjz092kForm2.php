<?php

require_once('for_php7.php');

class knjz092kForm2 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz092kindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $modeFlg = $model->cmd == "changePre" ? "2" : "1";
            $query = knjz092kQuery::getRow($model, $modeFlg);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }
        if (!is_array($Row)) {
            $Row =& $model->field;
        }

        /******************/
        /* コンボボックス */
        /******************/
        //都道府県コードコンボ
        $model->year = $model->year ? $model->year : CTRL_YEAR;
        $result    = $db->query(knjz092kQuery::getName($model->year));
        $opt       = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $extra = "onChange=\"return btn_submit('changePre')\"";
        $arg["data"]["PREFECTURESCD"] = knjCreateCombo($objForm, "PREFECTURESCD", $Row["PREFECTURESCD"], $opt, $extra, 1);

        //学年コンボ
        $query = knjz092kQuery::getGrade($model->year);
        $extra = "onChange=\"return btn_submit('changePre')\"";
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra, 1, "");

        //ランクコンボ
        $rankNameCd = "G218";
        if ("2016" > $model->year) {
            $rankNameCd = "G213";
        } else if ("2016" == $model->year && $Row["GRADE"] > "01") {
            $rankNameCd = "G213";
        } else if ("2017" == $model->year && $Row["GRADE"] == "03") {
            $rankNameCd = "G213";
        }
        $query = knjz092kQuery::getRank($model, $rankNameCd);
        $extra = "onChange=\"return btn_submit('changePre')\"";
        makeCmb($objForm, $arg, $db, $query, $Row["RANK_DIV"], "RANK_DIV", $extra, 1, "BLANK");

        /********************/
        /* テキストボックス */
        /********************/
        /**** 1 ****/
        //上限額1
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value); changeTotal();\"";
        $arg["data"]["REDUCTIONMONEY_1"] = knjCreateTextBox($objForm, $Row["REDUCTIONMONEY_1"], "REDUCTIONMONEY_1", 8, 8, $extra);

        /**** 2 ****/
        //上限額2
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value); changeTotal();\"";
        $arg["data"]["REDUCTIONMONEY_2"] = knjCreateTextBox($objForm, $Row["REDUCTIONMONEY_2"], "REDUCTIONMONEY_2", 8, 8, $extra);

        //合計支援額
        $arg["data"]["MAX_MONEY"] = (strlen($Row["MAX_MONEY"])) ? number_format($Row["MAX_MONEY"]): "";

        /**** 1 ****/
        //下限額1
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value); changeTotal();\"";
        $arg["data"]["MIN_MONEY_1"] = knjCreateTextBox($objForm, $Row["MIN_MONEY_1"], "MIN_MONEY_1", 8, 8, $extra);

        /**** 2 ****/
        //下限額2
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value); changeTotal();\"";
        $arg["data"]["MIN_MONEY_2"] = knjCreateTextBox($objForm, $Row["MIN_MONEY_2"], "MIN_MONEY_2", 8, 8, $extra);

        //保護者負担金1
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value); changeTotal();\"";
        $arg["data"]["PARENTS_MONEY_1"] = knjCreateTextBox($objForm, $Row["PARENTS_MONEY_1"], "PARENTS_MONEY_1", 8, 8, $extra);

        //保護者負担金2
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value); changeTotal();\"";
        $arg["data"]["PARENTS_MONEY_2"] = knjCreateTextBox($objForm, $Row["PARENTS_MONEY_2"], "PARENTS_MONEY_2", 8, 8, $extra);

        //合計下限額
        $arg["data"]["MIN_MONEY"] = (strlen($Row["MIN_MONEY"])) ? number_format($Row["MIN_MONEY"]): "";

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

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz092kindex.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz092kForm2.html", $arg);
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
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
