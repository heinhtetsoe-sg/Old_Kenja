<?php

require_once('for_php7.php');

class knjl033nForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl033nindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2") && $model->year && $model->applicantdiv && $model->judge_kind != ""){
            $query = knjl033nQuery::getRow($model->year, $model->applicantdiv, $model->judge_kind);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /**************/
        /**コンボ作成**/
        /**************/
        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjl033nQuery::getNameMst($model, "L003", "APPLICANTDIV");
        makeCombo($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "blank");

        //奨学金種別
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjl033nQuery::getNameMst($model, "L025", "JUDGE_KIND");
        makeCombo($objForm, $arg, $db, $query, $Row["JUDGE_KIND"], "JUDGE_KIND", $extra, 1, "blank");

        /****************/
        /**テキスト作成**/
        /****************/
        //extra
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";

        //入学金（金額）テキストボックス
        $arg["data"]["ENT_MONEY"] = knjCreateTextBox($objForm, $Row["ENT_MONEY"], "ENT_MONEY", 6, 6, $extra);
        //入学金（文言）テキストボックス
        $arg["data"]["ENT_MONEY_NAME"] = knjCreateTextBox($objForm, $Row["ENT_MONEY_NAME"], "ENT_MONEY_NAME", 40, 40, "");
        //施設拡充費（金額）テキストボックス
        $arg["data"]["FAC_MONEY"] = knjCreateTextBox($objForm, $Row["FAC_MONEY"], "FAC_MONEY", 6, 6, $extra);
        //施設拡充費（文言）テキストボックス
        $arg["data"]["FAC_MONEY_NAME"] = knjCreateTextBox($objForm, $Row["FAC_MONEY_NAME"], "FAC_MONEY_NAME", 40, 40, "");
        //行事費4月分（金額）テキストボックス
        $arg["data"]["REMARK1_MONEY"] = knjCreateTextBox($objForm, $Row["REMARK1_MONEY"], "REMARK1_MONEY", 6, 6, $extra);
        //行事費4月分（文言）テキストボックス
        $arg["data"]["REMARK1_MONEY_NAME"] = knjCreateTextBox($objForm, $Row["REMARK1_MONEY_NAME"], "REMARK1_MONEY_NAME", 40, 40, "");
        //諸検査料（金額）テキストボックス
        $arg["data"]["REMARK2_MONEY"] = knjCreateTextBox($objForm, $Row["REMARK2_MONEY"], "REMARK2_MONEY", 6, 6, $extra);
        //諸検査料（文言）テキストボックス
        $arg["data"]["REMARK2_MONEY_NAME"] = knjCreateTextBox($objForm, $Row["REMARK2_MONEY_NAME"], "REMARK2_MONEY_NAME", 40, 40, "");
        //クラブ後援会費（金額）テキストボックス
        $arg["data"]["REMARK3_MONEY"] = knjCreateTextBox($objForm, $Row["REMARK3_MONEY"], "REMARK3_MONEY", 6, 6, $extra);
        //クラブ後援会費（文言）テキストボックス
        $arg["data"]["REMARK3_MONEY_NAME"] = knjCreateTextBox($objForm, $Row["REMARK3_MONEY_NAME"], "REMARK3_MONEY_NAME", 40, 40, "");

        /**************/
        /**ボタン作成**/
        /**************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //修正ボタン
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

        /**************/
        /**hidden作成**/
        /**************/
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl033nindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl033nForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = false) {
    $opt = array();
    $value_flg = false;
    if ($blank == "blank") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if($name == "JUDGE_KIND") {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
