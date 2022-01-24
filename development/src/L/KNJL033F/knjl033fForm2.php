<?php

require_once('for_php7.php');

class knjl033fForm2 {

    function main(&$model) {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl033findex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2") && $model->year && $model->applicantdiv && $model->item_cd) {
            $query = knjl033fQuery::getRow($model->year, $model->applicantdiv, $model->item_cd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /****************/
        /*  コンボ作成  */
        /****************/
        //入試制度コンボ
        $extra = "";
        $query = knjl033fQuery::getApplicantdiv($model->year);
        makeCombo($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, $model, "blank");

        /******************/
        /*  テキスト作成  */
        /******************/
        //費目コードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ITEM_CD"] = knjCreateTextBox($objForm, $Row["ITEM_CD"], "ITEM_CD", 2, 2, $extra);

        //費目名テキストボックス
        $arg["data"]["ITEM_NAME"] = knjCreateTextBox($objForm, $Row["ITEM_NAME"], "ITEM_NAME", 40, 40, "");

        //費目金額テキストボックス
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toMoney(this.value)\"";
        $Row["ITEM_MONEY"] = strlen($Row["ITEM_MONEY"]) ? number_format($Row["ITEM_MONEY"]) : "";
        $arg["data"]["ITEM_MONEY"] = knjCreateTextBox($objForm, $Row["ITEM_MONEY"], "ITEM_MONEY", 7, 7, $extra);

        /******************/
        /*  チェック作成  */
        /******************/
        //入学金対象チェックボックス
        $extra = ($Row["REMARK1"] == "1") ? "checked" : "";
        $arg["data"]["REMARK1"] = knjCreateCheckBox($objForm, "REMARK1", "1", $extra);
        //諸費対象チェックボックス
        $extra = ($Row["REMARK3"] == "1") ? "checked" : "";
        $arg["data"]["REMARK3"] = knjCreateCheckBox($objForm, "REMARK3", "1", $extra);
        //受験料チェックボックス
        $extra = ($Row["REMARK4"] == "1") ? "checked" : "";
        $arg["data"]["REMARK4"] = knjCreateCheckBox($objForm, "REMARK4", "1", $extra);
        //入学支度金貸付金チェックボックス
        $extra = ($Row["REMARK5"] == "1") ? "checked" : "";
        $arg["data"]["REMARK5"] = knjCreateCheckBox($objForm, "REMARK5", "1", $extra);

        /****************/
        /*  ボタン作成  */
        /****************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '終 了', $extra);

        /****************/
        /*  hidden作成  */
        /****************/
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (!isset($model->warning) && $model->cmd != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl033findex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl033fForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $model, $blank="") {
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
        $i++;
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"])    $value_flg = true;

        if (!isset($model->warning) && $row["DEFAULT"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
