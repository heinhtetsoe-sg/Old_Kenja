<?php

require_once('for_php7.php');

class knjl603aForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl603aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset") && $model->entexamyear && $model->applicantdiv && $model->testdiv && $model->examhallcd) {
            $Row = $db->getRow(knjl603aQuery::getEntexamHallYdat($model, $model->examhallcd), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /******************/
        /*  テキスト作成  */
        /******************/
        //会場コード
        $extra = "onblur=\"this.value=toAlphaNumber(this.value)\"";
        $arg["data"]["EXAMHALLCD"] = knjCreateTextBox($objForm, $Row["EXAMHALLCD"], "EXAMHALLCD", 2, 3, $extra);

        //収容人数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CAPA_CNT"] = knjCreateTextBox($objForm, $Row["CAPA_CNT"], "CAPA_CNT", 2, 3, $extra);

        /****************/
        /*  ボタン作成  */
        /****************/
        //追加1（会場）
        $extra = "onclick=\"return btn_submit('add1');\"";
        $arg["button"]["btn_add1"] = knjCreateBtn($objForm, 'btn_add1', '追 加', $extra);
        //更新1（会場）
        $extra = "onclick=\"return btn_submit('update1');\"";
        $arg["button"]["btn_update1"] = knjCreateBtn($objForm, 'btn_update1', '更 新', $extra);
        //削除1（会場）
        $extra = "onclick=\"return btn_submit('delete1');\"";
        $arg["button"]["btn_del1"] = knjCreateBtn($objForm, 'btn_del1', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        /****************/
        /*  hidden作成  */
        /****************/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl603aindex.php?cmd=list2"
                            . "&ENTEXAMYEAR=".$model->entexamyear."&APPLICANTDIV=".$model->applicantdiv."&TESTDIV=".$model->testdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl603aForm2.html", $arg);
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

        if ($value == $row["VALUE"]) $value_flg = true;

        if (!isset($model->warning) && ($model->cmd != "change") && $row["DEFAULT"] && $default_flg) {
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
