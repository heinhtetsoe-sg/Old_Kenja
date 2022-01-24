<?php

require_once('for_php7.php');

class knjz999Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz999index.php", "", "edit");

        $db = Query::dbCheckOut();

        //初期値の配列を作成
        if (!isset($model->warning)) {
            $query = knjz999Query::getOneRecord($model->stationcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /********************/
        /* テキストボックス */
        /********************/
        //textbox
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["STATIONCD"] = knjCreateTextBox($objForm, $Row["STATIONCD"], "STATIONCD", 7, 7, $extra);
        //textbox
        $extra = "";
        $arg["data"]["STATIONNAME"] = knjCreateTextBox($objForm, $Row["STATIONNAME"], "STATIONNAME", 7, 7, $extra);

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz999index.php?cmd=list';";
        }

        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz999Form2.html", $arg);
    }
}
?>
