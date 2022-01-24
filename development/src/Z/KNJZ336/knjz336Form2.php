<?php

require_once('for_php7.php');

class knjz336Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz336index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = $db->getRow(knjz336Query::getRow($model->systemId), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //システムＩＤ
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["SYSTEMID"] = knjCreateTextBox($objForm, $Row["SYSTEMID"], "SYSTEMID", 8, 8, $extra);

        //システム名称
        $extra = "";
        $arg["data"]["SYSTEM_NAME"] = knjCreateTextBox($objForm, $Row["SYSTEM_NAME"], "SYSTEM_NAME", 60, 40, $extra);

        //システム名略称
        $extra = "";
        $arg["data"]["SYSTEM_NAME_ABBV"] = knjCreateTextBox($objForm, $Row["SYSTEM_NAME_ABBV"], "SYSTEM_NAME_ABBV", 60, 40, $extra);

        //表示順
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["SHOWORDER"] = knjCreateTextBox($objForm, $Row["SHOWORDER"], "SHOWORDER", 2, 2, $extra);

        /********/
        /*ボタン*/
        /********/
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
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz336index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz336Form2.html", $arg); 
    }
}
?>
