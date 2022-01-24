<?php

require_once('for_php7.php');

class knjl034gForm2 {

    function main(&$model) {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl034gindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset") && $model->year && $model->applicantdiv && $model->item_cd && $model->exemption_cd) {
            $query = knjl034gQuery::getRow($model->year, $model->applicantdiv, $model->item_cd, $model->exemption_cd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /****************/
        /*  コンボ作成  */
        /****************/
        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('change');\"";
        $query = knjl034gQuery::getApplicantdiv($model->year);
        makeCombo($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, $model, "blank");

        //免除コードコンボ
        $extra = "";
        $query = knjl034gQuery::getExemptionCd($model->year);
        makeCombo($objForm, $arg, $db, $query, $Row["EXEMPTION_CD"], "EXEMPTION_CD", $extra, 1, $model, "blank");

        //費目コードコンボ
        $extra = "";
        $query = knjl034gQuery::getItemCd($model->year, $Row["APPLICANTDIV"]);
        makeCombo($objForm, $arg, $db, $query, $Row["ITEM_CD"], "ITEM_CD", $extra, 1, $model, "blank");

        /******************/
        /*  テキスト作成  */
        /******************/
        //免除額テキストボックス
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toMoney(this.value)\"";
        $Row["EXEMPTION_MONEY"] = strlen($Row["EXEMPTION_MONEY"]) ? number_format($Row["EXEMPTION_MONEY"]) : "";
        $arg["data"]["EXEMPTION_MONEY"] = knjCreateTextBox($objForm, $Row["EXEMPTION_MONEY"], "EXEMPTION_MONEY", 7, 7, $extra);

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
            $arg["reload"]  = "parent.left_frame.location.href='knjl034gindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl034gForm2.html", $arg);
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
        if ($name == "ITEM_CD") {
            $item_money = strlen($row["ITEM_MONEY"]) ? "(" . number_format($row["ITEM_MONEY"]) . ")" : "";
            $row["LABEL"] = $row["LABEL"] . $item_money;
        }

        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"])    $value_flg = true;

        if (!isset($model->warning) && $model->cmd != "change" && $row["DEFAULT"] && $default_flg) {
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
