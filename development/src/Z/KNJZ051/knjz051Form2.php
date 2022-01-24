<?php

require_once('for_php7.php');

class knjz051Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz051index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz051Query::getRow($model->coursecd, $model->majorcd);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //課程コンボ
        $result = $db->query(knjz051Query::getCourse());
        $opt = array();        
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label"  => $row["LABEL"],
                           "value"  => $row["VALUE"]);
        }
        $extra = "";
        $arg["data"]["COURSECD"] = knjCreateCombo($objForm, "COURSECD", $Row["COURSECD"], $opt, $extra, 1);

        //学科コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MAJORCD"] = knjCreateTextBox($objForm, $Row["MAJORCD"], "MAJORCD", 3, 3, $extra);

        //学科名称
        $extra = "";
        $arg["data"]["MAJORNAME"] = knjCreateTextBox($objForm, $Row["MAJORNAME"], "MAJORNAME", 40, 60, $extra);

        //学科略称
        $extra = "";
        $arg["data"]["MAJORABBV"] = knjCreateTextBox($objForm, $Row["MAJORABBV"], "MAJORABBV", 4, 6, $extra);

        //学科英字
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["MAJORENG"] = knjCreateTextBox($objForm, $Row["MAJORENG"], "MAJORENG", 20, 20, $extra);

        //学科銀行コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MAJORBANKCD"] = knjCreateTextBox($objForm, $Row["MAJORBANKCD"], "MAJORBANKCD", 2, 2, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"] = "parent.left_frame.location.href='knjz051index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz051Form2.html", $arg); 
    }
}
?>
