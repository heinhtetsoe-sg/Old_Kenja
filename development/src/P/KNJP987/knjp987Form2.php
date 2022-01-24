<?php

require_once('for_php7.php');

class knjp987Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp987index.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $Row = knjp987Query::getRow($model, $model->taxCd, $model->dateFrom);
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //コード：名称コンボ
        $opt = array();
        $value_flg = false;
        $query = knjp987Query::getNameMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["TAX_CD"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["TAX_CD"] = ($Row["TAX_CD"] && $value_flg) ? $Row["TAX_CD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["TAX_CD"] = knjCreateCombo($objForm, "TAX_CD", $Row["TAX_CD"], $opt, $extra, 1);

        //開始日
        $dateFrom = str_replace("-", "/", $Row["DATE_FROM"]);
        $arg["data"]["DATE_FROM"] = View::popUpCalendarAlp($objForm, "DATE_FROM", $dateFrom, "", "");

        //終了日
        $dateTo = str_replace("-", "/", $Row["DATE_TO"]);
        $arg["data"]["DATE_TO"] = View::popUpCalendarAlp($objForm, "DATE_TO", $dateTo, "", "");

        //値
        $extra = "style=\"text-align:right\" onblur=\"this.value=toNumber(this.value)\"";
        $arg["data"]["TAX_VALUE"] = knjCreateTextBox($objForm, $Row["TAX_VALUE"], "TAX_VALUE", 5, 5, $extra);

        //内容
        $extra = "";
        $arg["data"]["TAX_SUMMARY"] = knjCreateTextBox($objForm, $Row["TAX_SUMMARY"], "TAX_SUMMARY", 21, 30, $extra);

        //ボタン作成
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリア
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjp987index.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp987Form2.html", $arg);
    }
}
?>
