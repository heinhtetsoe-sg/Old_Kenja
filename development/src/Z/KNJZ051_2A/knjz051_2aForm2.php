<?php

require_once('for_php7.php');

class knjz051_2aForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz051_2aindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz051_2aQuery::getRow($model->coursecd, $model->majorcd);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //課程コンボ
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjz051_2aQuery::getCourse());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label"  => $row["LABEL"],
                           "value"  => $row["VALUE"]);

            if ($Row["COURSECD"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["COURSECD"] = ($Row["COURSECD"] && $value_flg) ? $Row["COURSECD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["COURSECD"] = knjCreateCombo($objForm, "COURSECD", $Row["COURSECD"], $opt, $extra, 1);

        //学科コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MAJORCD"] = knjCreateTextBox($objForm, $Row["MAJORCD"], "MAJORCD", 3, 3, $extra);

        //学科名称
        $extra = "";
        $arg["data"]["MAJORNAME"] = knjCreateTextBox($objForm, $Row["MAJORNAME"], "MAJORNAME", 40, 60, $extra);

        //表示用名称
        $extra = "";
        $arg["data"]["MAJORNAME2"] = knjCreateTextBox($objForm, $Row["MAJORNAME2"], "MAJORNAME2", 40, 60, $extra);

        //学科略称
        $extra = "";
        $arg["data"]["MAJORABBV"] = knjCreateTextBox($objForm, $Row["MAJORABBV"], "MAJORABBV", 4, 6, $extra);

        //学科英字
        $majorengLength = $model->majorengLength <= 20 ? 20 : 45;
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["MAJORENG"] = knjCreateTextBox($objForm, $Row["MAJORENG"], "MAJORENG", $majorengLength, $majorengLength, $extra);

        //学科銀行コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MAJORBANKCD"] = knjCreateTextBox($objForm, $Row["MAJORBANKCD"], "MAJORBANKCD", 2, 2, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ051A/knjz051aindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"] = "parent.left_frame.location.href='knjz051_2aindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz051_2aForm2.html", $arg); 
    }
}
?>
