<?php
class knjlz05hForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjlz05hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset") && $model->kinddiv) {
            $Row = $db->getRow(knjlz05hQuery::getRow($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //入試区分コード
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["KINDDIV"] = knjCreateTextBox($objForm, $Row["KINDDIV"], "KINDDIV", 2, 2, $extra);
        //入試区分名称
        $extra = "";
        $arg["data"]["KINDDIV_NAME"] = knjCreateTextBox($objForm, $Row["KINDDIV_NAME"], "KINDDIV_NAME", 42, 40, $extra);
        //入試区分略称
        $extra = "";
        $arg["data"]["KINDDIV_ABBV"] = knjCreateTextBox($objForm, $Row["KINDDIV_ABBV"], "KINDDIV_ABBV", 22, 20, $extra);

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
        knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjlz05hindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjlz05hForm2.html", $arg); 
    }
}
?>
