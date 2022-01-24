<?php

require_once('for_php7.php');

class knjz040mForm2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz040mindex.php", "", "edit");
        $db  = Query::dbCheckOut();
        if (!isset($model->warning)) {
            $query = knjz040mQuery::get_natpubpri_area_div_area($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

/***********************************************************************************************/
/***********************************************************************************************/
/*******                       *****************************************************************/
/******* ENTEXAM_NATPUBPRI_MST *****************************************************************/
/*******                       *****************************************************************/
/***********************************************************************************************/
/***********************************************************************************************/
        //国公私立コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["NATPUBPRI_CD"] = knjCreateTextBox($objForm, $Row["NATPUBPRI_CD"], "NATPUBPRI_CD", 1, 1, $extra);

        //国公私立名称
        $extra = "";
        $arg["data"]["NATPUBPRI_NAME"] = knjCreateTextBox($objForm, $Row["NATPUBPRI_NAME"], "NATPUBPRI_NAME", 6, 3, $extra);

        //国公私立略称
        $extra = "";
        $arg["data"]["NATPUBPRI_ABBV"] = knjCreateTextBox($objForm, $Row["NATPUBPRI_ABBV"], "NATPUBPRI_ABBV", 6, 3, $extra);

/**********************************************************************************************/
/**********************************************************************************************/
/*******                      *****************************************************************/
/******* ENTEXAM_AREA_DIV_MST *****************************************************************/
/*******                      *****************************************************************/
/**********************************************************************************************/
/**********************************************************************************************/
        //所在地区分コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["AREA_DIV_CD"] = knjCreateTextBox($objForm, $Row["AREA_DIV_CD"], "AREA_DIV_CD", 2, 2, $extra);

        //所在地区分名称
        $extra = "";
        $arg["data"]["AREA_DIV_NAME"] = knjCreateTextBox($objForm, $Row["AREA_DIV_NAME"], "AREA_DIV_NAME", 20, 10, $extra);

        //所在地区分略称
        $extra = "";
        $arg["data"]["AREA_DIV_ABBV"] = knjCreateTextBox($objForm, $Row["AREA_DIV_ABBV"], "AREA_DIV_ABBV", 20, 10, $extra);

/******************************************************************************************/
/******************************************************************************************/
/*******                  *****************************************************************/
/******* ENTEXAM_AREA_MST *****************************************************************/
/*******                  *****************************************************************/
/******************************************************************************************/
/******************************************************************************************/
        //所在地コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["AREA_CD"] = knjCreateTextBox($objForm, $Row["AREA_CD"], "AREA_CD", 2, 2, $extra);

        //所在地名称
        $extra = "";
        $arg["data"]["AREA_NAME"] = knjCreateTextBox($objForm, $Row["AREA_NAME"], "AREA_NAME", 20, 10, $extra);

        //所在地略称
        $extra = "";
        $arg["data"]["AREA_ABBV"] = knjCreateTextBox($objForm, $Row["AREA_ABBV"], "AREA_ABBV", 20, 10, $extra);

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
            $arg["reload"]  = "window.open('knjz040mindex.php?cmd=list&ed=1','left_frame');";
        }
        View::toHTML($model, "knjz040mForm2.html", $arg);
    }
}
?>
