<?php

require_once('for_php7.php');

class knjz041Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz041index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz041Query::getRow($model->coursecd);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //課程コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COURSECD"] = knjCreateTextBox($objForm, $Row["COURSECD"], "COURSECD", 1, 1, $extra);

        //課程名称
        $extra = "";
        $arg["data"]["COURSENAME"] = knjCreateTextBox($objForm, $Row["COURSENAME"], "COURSENAME", 10, 12, $extra);

        //課程略称
        $extra = "";
        $arg["data"]["COURSEABBV"] = knjCreateTextBox($objForm, $Row["COURSEABBV"], "COURSEABBV", 6, 6, $extra);

        //課程英字
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["COURSEENG"] = knjCreateTextBox($objForm, $Row["COURSEENG"], "COURSEENG", 10, 10, $extra);

        //校時名称
        $result = $db->query(knjz041Query::getName());
        $opt = array();        
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label"  => $row["NAMECD2"]."  ".$row["NAME1"],
                           "value"  => $row["NAMECD2"]);
        }
        //開始校時
        $extra = "";
        $arg["data"]["S_PERIODCD"] = knjCreateCombo($objForm, "S_PERIODCD", $Row["S_PERIODCD"], $opt, $extra, 1);

        //終了校時
        $extra = "";
        $arg["data"]["E_PERIODCD"] = knjCreateCombo($objForm, "E_PERIODCD", $Row["E_PERIODCD"], $opt, $extra, 1);

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
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz041index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz041Form2.html", $arg); 
    }
}
?>
