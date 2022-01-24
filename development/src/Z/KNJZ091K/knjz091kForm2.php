<?php

require_once('for_php7.php');

class knjz091kForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz091kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)) {
            $Row = knjz091kQuery::getRow($model, 1);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //学年コンボ
        $result    = $db->query(knjz091kQuery::getGrade($model->year));
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
        //基準額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["REDUCTIONMONEY"] = knjCreateTextBox($objForm, $Row["REDUCTIONMONEY"], "REDUCTIONMONEY", 8, 8, $extra);
        //軽減額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["REDUCTION_ADD_MONEY"] = knjCreateTextBox($objForm, $Row["REDUCTION_ADD_MONEY"], "REDUCTION_ADD_MONEY", 8, 8, $extra);
        //課税総所得下限額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["INCOME_LOW"] = knjCreateTextBox($objForm, $Row["INCOME_LOW"], "INCOME_LOW", 8, 8, $extra);
        //課税総所得上限額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["INCOME_HIGH"] = knjCreateTextBox($objForm, $Row["INCOME_HIGH"], "INCOME_HIGH", 8, 8, $extra);

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
            $arg["reload"]  = "window.open('knjz091kindex.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz091kForm2.html", $arg);
    }
}
?>
