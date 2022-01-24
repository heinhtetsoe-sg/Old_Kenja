<?php

require_once('for_php7.php');

class knjz512Form2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz512index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->centercd) {
            $query = knjz512Query::getMedicalCenterMst($model->centercd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CENTERCD"] = knjCreateTextBox($objForm, $Row["CENTERCD"], "CENTERCD", 5, 5, $extra);

        //名称
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 60, 60, "");

        //略称
        $arg["data"]["ABBV"] = knjCreateTextBox($objForm, $Row["ABBV"], "ABBV", 20, 20, "");

        //郵便番号
        $arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $Row["ZIPCD"], "ADDR1");

        //住所１
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $Row["ADDR1"], "ADDR1", 100, 100, "");

        //住所２
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $Row["ADDR2"], "ADDR2", 100, 100, "");

        //電話番号
        $extra = "onblur=\"this.value=toTelNo(this, '電話')\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 16, 14, $extra);

        //ＦＡＸ番号
        $extra = "onblur=\"this.value=toTelNo(this, 'ＦＡＸ')\"";
        $arg["data"]["FAXNO"] = knjCreateTextBox($objForm, $Row["FAXNO"], "FAXNO", 16, 14, $extra);

        //代表者・院長
        $arg["data"]["MEDICAL_DIRECTOR_NAME"] = knjCreateTextBox($objForm, $Row["MEDICAL_DIRECTOR_NAME"], "MEDICAL_DIRECTOR_NAME", 60, 60, "");

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz512index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz512Form2.html", $arg);
    }
}
?>
