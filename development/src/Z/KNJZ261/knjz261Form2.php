<?php

require_once('for_php7.php');

class knjz261Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz261index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz261Query::getRow($model->dutysharecd);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //校務分掌部コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["DUTYSHARECD"] = knjCreateTextBox($objForm, $Row["DUTYSHARECD"], "DUTYSHARECD", 5, 4, $extra);

        //校務分掌部名称
        $extra = "";
        $arg["data"]["SHARENAME"] = knjCreateTextBox($objForm, $Row["SHARENAME"], "SHARENAME", 16, 24, $extra);

        //学校基本調査名称
        $extra = "";
        $arg["data"]["BASE_SHARENAME"] = knjCreateTextBox($objForm, $Row["BASE_SHARENAME"], "BASE_SHARENAME", 16, 24, $extra);

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
            $arg["reload"] = "parent.left_frame.location.href='knjz261index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz261Form2.html", $arg); 
    }
}
?>
