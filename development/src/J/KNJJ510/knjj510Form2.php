<?php

require_once('for_php7.php');

class knjj510Form2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjj510index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->itemcd) {
            $query = knjj510Query::getSportsItemMst($model->itemcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //種目コード
        $extra = "onblur=\"CodeCheck(this);\"";
        $arg["data"]["ITEMCD"] = knjCreateTextBox($objForm, $Row["ITEMCD"], "ITEMCD", 3, 3, $extra);

        //種目名称
        $arg["data"]["ITEMNAME"] = knjCreateTextBox($objForm, $Row["ITEMNAME"], "ITEMNAME", 20, 20, "");

        //種目略称
        $arg["data"]["ITEMABBV"] = knjCreateTextBox($objForm, $Row["ITEMABBV"], "ITEMABBV", 20, 20, "");

        //単位
        $arg["data"]["UNIT"] = knjCreateTextBox($objForm, $Row["UNIT"], "UNIT", 6, 6, "");

        //通知表への印刷順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHOWORDER"] = knjCreateTextBox($objForm, $Row["SHOWORDER"], "SHOWORDER", 2, 2, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjj510index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj510Form2.html", $arg);
    }
}
?>
