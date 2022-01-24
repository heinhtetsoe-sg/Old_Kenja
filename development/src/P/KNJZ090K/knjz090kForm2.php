<?php

require_once('for_php7.php');

class knjz090kForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz090kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)) {
            if ($model->cmd == 'prefecturescd') {
                $Row =& $model->field;
            } else {
                $Row = knjz090kQuery::getRow($model,1);
            }
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        /******************/
        /* コンボボックス */
        /******************/
        //都道府県コードコンボ
        $model->year = $model->year ? $model->year : CTRL_YEAR;
        $result    = $db->query(knjz090kQuery::getName($model->year));
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        if($Row["PREFECTURESCD"] == "") {
            $Row["PREFECTURESCD"] = $opt[0]["value"];
        }
        $result->free();
        $extra = "onChange=\"btn_submit('prefecturescd')\"";
        $arg["data"]["PREFECTURESCD"] = knjCreateCombo($objForm, "PREFECTURESCD", $Row["PREFECTURESCD"], $opt, $extra, 1);

        //学年コンボ
        $result    = $db->query(knjz090kQuery::getGrade($model->year));
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["GRADE"],
                           "value" => $row["GRADE"]);
        }
        $result->free();
        $extra = "onChange=\"btn_submit('prefecturescd')\"";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $Row["GRADE"], $opt, $extra, 1);

        //ランクコンボ
        $rankNameCd = "G218";
        if ("2016" > $model->year) {
            $rankNameCd = "G213";
        } else if ("2016" == $model->year && $Row["GRADE"] > "01") {
            $rankNameCd = "G213";
        } else if ("2017" == $model->year && $Row["GRADE"] == "03") {
            $rankNameCd = "G213";
        }

        $opt = array();
        $query = knjz090kQuery::getNameMst($model->year, $rankNameCd, "");
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];

        //ランクコンボ1有効設定
        $query = knjz090kQuery::getNameMst($model->year, 'G202', $Row["PREFECTURESCD"]);
        $lankStatus = $db->getOne($query);
        if($lankStatus === '1') {
            $extra = "";
        } else {
            $extra = " disabled ";
        }
        $arg["data"]["INCOME_RANK1"] = knjCreateCombo($objForm, "INCOME_RANK1", $Row["INCOME_RANK1"], $opt, $extra, 1);

        //ランクコンボ2有効設定
        $query = knjz090kQuery::getNameMst($model->year, 'G202', $Row["PREFECTURESCD"]);
        $lankStatus = $db->getOne($query);
        if($lankStatus === '1') {
            $extra = "";
        } else {
            $extra = " disabled ";
        }
        $arg["data"]["INCOME_RANK2"] = knjCreateCombo($objForm, "INCOME_RANK2", $Row["INCOME_RANK2"], $opt, $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/
        /**** 1 ****/
        //軽減額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["REDUCTIONMONEY_1"] = knjCreateTextBox($objForm, $Row["REDUCTIONMONEY_1"], "REDUCTIONMONEY_1", 8, 8, $extra);
        //課税総所得下限額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["INCOME_LOW1"] = knjCreateTextBox($objForm, $Row["INCOME_LOW1"], "INCOME_LOW1", 8, 8, $extra);
        //課税総所得上限額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["INCOME_HIGH1"] = knjCreateTextBox($objForm, $Row["INCOME_HIGH1"], "INCOME_HIGH1", 8, 8, $extra);
        //兄弟姉妹
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["INCOME_SIBLINGS1"] = knjCreateTextBox($objForm, $Row["INCOME_SIBLINGS1"], "INCOME_SIBLINGS1", 2, 2, $extra);

        /**** 2 ****/
        //軽減額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["REDUCTIONMONEY_2"] = knjCreateTextBox($objForm, $Row["REDUCTIONMONEY_2"], "REDUCTIONMONEY_2", 8, 8, $extra);
        //課税総所得下限額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["INCOME_LOW2"] = knjCreateTextBox($objForm, $Row["INCOME_LOW2"], "INCOME_LOW2", 8, 8, $extra);
        //課税総所得上限額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["INCOME_HIGH2"] = knjCreateTextBox($objForm, $Row["INCOME_HIGH2"], "INCOME_HIGH2", 8, 8, $extra);
        //兄弟姉妹
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["INCOME_SIBLINGS2"] = knjCreateTextBox($objForm, $Row["INCOME_SIBLINGS2"], "INCOME_SIBLINGS2", 2, 2, $extra);
        //備考
        $extra = "";
        $arg["data"]["REDUCTIONREMARK"] = knjCreateTextBox($objForm, $Row["REDUCTIONREMARK"], "REDUCTIONREMARK", 50, 50, $extra);

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
            $arg["reload"]  = "window.open('knjz090kindex.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz090kForm2.html", $arg);
    }
}
?>
