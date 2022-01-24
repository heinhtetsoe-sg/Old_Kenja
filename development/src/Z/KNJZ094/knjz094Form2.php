<?php

require_once('for_php7.php');

class knjz094Form2 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz094index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = $db->getRow(knjz094Query::getRow($model->districtcd), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //所在地コード
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["DISTRICTCD"] = knjCreateTextBox($objForm, $Row["DISTRICTCD"], "DISTRICTCD", 5, 5, $extra);

        //所在地名称
        $extra = "";
        $arg["data"]["DISTRICT_NAME"] = knjCreateTextBox($objForm, $Row["DISTRICT_NAME"], "DISTRICT_NAME", 51, 25, $extra);

        //所在地略称
        $extra = "";
        $arg["data"]["DISTRICT_NAME_ABBV"] = knjCreateTextBox($objForm, $Row["DISTRICT_NAME_ABBV"], "DISTRICT_NAME_ABBV", 51, 25, $extra);

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

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz094index.php?cmd=list';";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz094Form2.html", $arg); 
    }
}
?>
