<?php

require_once('for_php7.php');

class knjz417Form2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz417index.php", "", "edit");
        $db  = Query::dbCheckOut();
        if (!isset($model->warning)) {
            $query = knjz417Query::getJobtype_L_M_Mst($model->jobtype_lcd, $model->jobtype_mcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /********************/
        /* テキストボックス */
        /********************/
        //職業(大)コード
        $extra = "onblur=\"check_cd(this)\"";
        $arg["data"]["JOBTYPE_LCD"] = knjCreateTextBox($objForm, $Row["JOBTYPE_LCD"], "JOBTYPE_LCD", 2, 2, $extra);
        //職業(大)名称
        $extra = "style=\"width:100%\"";
        $arg["data"]["JOBTYPE_LNAME"] = knjCreateTextBox($objForm, $Row["JOBTYPE_LNAME"], "JOBTYPE_LNAME", 80, 150, $extra);
        //職業(大)名称かな
        $extra = "style=\"width:100%\"";
        $arg["data"]["JOBTYPE_LNAME_KANA"] = knjCreateTextBox($objForm, $Row["JOBTYPE_LNAME_KANA"], "JOBTYPE_LNAME_KANA", 80, 300, $extra);
        //職業(中)コード
        $extra = "onblur=\"check_cd(this)\"";
        $arg["data"]["JOBTYPE_MCD"] = knjCreateTextBox($objForm, $Row["JOBTYPE_MCD"], "JOBTYPE_MCD", 2, 2, $extra);
        //職業(中)名称
        $extra = "style=\"width:100%\"";
        $arg["data"]["JOBTYPE_MNAME"] = knjCreateTextBox($objForm, $Row["JOBTYPE_MNAME"], "JOBTYPE_MNAME", 80, 150, $extra);
        //職業(中)名称かな
        $extra = "style=\"width:100%\"";
        $arg["data"]["JOBTYPE_MNAME_KANA"] = knjCreateTextBox($objForm, $Row["JOBTYPE_MNAME_KANA"], "JOBTYPE_MNAME_KANA", 80, 300, $extra);

        /**********/
        /* ボタン */
        /**********/
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

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz417index.php?cmd=list&ed=1','left_frame');";
        }
        View::toHTML($model, "knjz417Form2.html", $arg);
    }
}
?>
