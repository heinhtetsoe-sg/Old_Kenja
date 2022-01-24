<?php

require_once('for_php7.php');

class knjz418Form2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz418index.php", "", "edit");
        $db  = Query::dbCheckOut();
        if (!isset($model->warning)) {
            $query = knjz418Query::getJobtype_M_S_Mst($model->jobtype_lcd, $model->jobtype_mcd, $model->jobtype_scd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //中分類
        $opt = array();
        $query = knjz418Query::getJobtypeMcd($model);
        $value = $model->jobtype_lcd ."_". $model->jobtype_mcd;
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["JOBTYPE_L_MCD"] = knjCreateCombo($objForm, "JOBTYPE_L_MCD", $value, $opt, $extra, 1);


        /********************/
        /* テキストボックス */
        /********************/
        //小分類コード
        $extra = "style=\"text-align:right\" onblur=\"check_cd(this)\"";
        $arg["data"]["JOBTYPE_SCD"] = knjCreateTextBox($objForm, $Row["JOBTYPE_SCD"], "JOBTYPE_SCD", 3, 3, $extra);
        //小分類名称
        $extra = "style=\"width:100%\"";
        $arg["data"]["JOBTYPE_SNAME"] = knjCreateTextBox($objForm, $Row["JOBTYPE_SNAME"], "JOBTYPE_SNAME", 80, 150, $extra);
        //小分類名称かな
        $extra = "style=\"width:100%\"";
        $arg["data"]["JOBTYPE_SNAME_KANA"] = knjCreateTextBox($objForm, $Row["JOBTYPE_SNAME_KANA"], "JOBTYPE_SNAME_KANA", 80, 300, $extra);

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
            $arg["reload"]  = "window.open('knjz418index.php?cmd=list&ed=1','left_frame');";
        }
        View::toHTML($model, "knjz418Form2.html", $arg);
    }
}
?>
