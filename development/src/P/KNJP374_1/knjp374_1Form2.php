<?php

require_once('for_php7.php');

class knjp374_1Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp374_1index.php", "", "edit");

        $db = Query::dbCheckOut();

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            //校種コンボ
            $model->schoolKind = $model->sendSchoolKind;
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knjp374_1Query::getOneRecord($model, $model->transferDiv);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //振込区分コード
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["TRANSFER_DIV"] = knjCreateTextBox($objForm, $Row["TRANSFER_DIV"], "TRANSFER_DIV", 2, 2, $extra);

        //振込区分名
        $extra = "";
        $arg["data"]["TRANSFER_NAME"] = knjCreateTextBox($objForm, $Row["TRANSFER_NAME"], "TRANSFER_NAME", 62, 90, $extra);

        Query::dbCheckIn($db);

        /**********/
        /* ボタン */
        /**********/
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

        //戻るボタン
        $link = REQUESTROOT."/P/KNJP374/knjp374index.php?cmd=edit&SEND_PRGID=KNJP374_1&SEND_AUTH=".$model->auth."&SEND_GRADE=".$model->sendGrade."&SEND_HR_CLASS=".$model->sendHrClass."&SEND_SCHOOL_KIND=".$model->sendSchoolKind;
        $extra = "onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjp374_1index.php?cmd=list','left_frame');";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp374_1Form2.html", $arg);
    }
}
?>
