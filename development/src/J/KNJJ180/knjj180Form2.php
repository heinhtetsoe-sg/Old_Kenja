<?php

require_once('for_php7.php');

class knjj180Form2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjj180index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->branchcd) && !isset($model->warning)) {
            $Row = knjj180Query::getRow($model->branchcd);
        } else {
            $Row =& $model->field;
        }

        //支部コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["BRANCHCD"] = knjCreateTextBox($objForm, $Row["BRANCHCD"], "BRANCHCD", 3, 2, $extra);

        //支部名称
        $extra = "";
        $arg["data"]["BRANCHNAME"] = knjCreateTextBox($objForm, $Row["BRANCHNAME"], "BRANCHNAME", 50, 75, $extra);

        //支部略称
        $extra = "";
        $arg["data"]["ABBV"] = knjCreateTextBox($objForm, $Row["ABBV"], "ABBV", 50, 75, $extra);

        /**********/
        /* ボタン */
        /**********/
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
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjj180index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj180Form2.html", $arg); 
    }
}
?>
