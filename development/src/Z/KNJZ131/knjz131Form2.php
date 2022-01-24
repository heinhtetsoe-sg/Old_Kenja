<?php

require_once('for_php7.php');

class knjz131Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz131index.php", "", "edit");

        $model->year = $model->year ? $model->year : CTRL_YEAR;

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz131Query::getRightDataRow($model, $model->diCd);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        $extraInt = " style=\"text-align:right\" ";

        //コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["DI_CD"] = knjCreateTextBox($objForm, $Row["DI_CD"], "DI_CD", 2, 2, $extraInt.$extra);

        //名称
        $extra = "";
        $arg["data"]["DI_NAME1"] = knjCreateTextBox($objForm, $Row["DI_NAME1"], "DI_NAME1", 60, 40, $extra);

        //名称
        $extra = "";
        $arg["data"]["DI_NAME2"] = knjCreateTextBox($objForm, $Row["DI_NAME2"], "DI_NAME2", 60, 40, $extra);

        //処理読替コンボ
        $query = knjz131Query::getYomikae($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["ATSUB_REPL_DI_CD"], "ATSUB_REPL_DI_CD", $extra, 1, "BLANK");

        //出欠記号
        $extra = "";
        $arg["data"]["DI_MARK"] = knjCreateTextBox($objForm, $Row["DI_MARK"], "DI_MARK", 2, 2, $extra);

        //カウント倍数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MULTIPLY"] = knjCreateTextBox($objForm, $Row["MULTIPLY"], "MULTIPLY", 1, 1, $extraInt.$extra);

        //管理者以外表示可
        $extra = " id=\"RESTRICT_FLG\" ";
        $checked = $Row["RESTRICT_FLG"] == "1" ? " checked " : "";
        $arg["data"]["RESTRICT_FLG"] = knjCreateCheckBox($objForm, "RESTRICT_FLG", "1", $extra.$checked);

        //全校時の出欠コードコンボ
        $query = knjz131Query::getDi($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["ONEDAY_DI_CD"], "ONEDAY_DI_CD", $extra, 1, "BLANK");

        //出欠入力表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ORDER"] = knjCreateTextBox($objForm, $Row["ORDER"], "ORDER", 2, 2, $extraInt.$extra);

        //出欠届け表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PETITION_ORDER"] = knjCreateTextBox($objForm, $Row["PETITION_ORDER"], "PETITION_ORDER", 2, 2, $extraInt.$extra);

        //読替先処理コード
        $query = knjz131Query::getDi($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["REP_DI_CD"], "REP_DI_CD", $extra, 1, "BLANK");

        //追加
        $extra = "onclick=\"return btn_submit('add')\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete')\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリア
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz131index.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz131Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }

    if (!$value_flg) {
        $value = $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
