<?php

require_once('for_php7.php');

class knjg080Form3 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("knjg080Form3", "POST", "knjg080index.php", "", "knjg080Form3");

        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //区分
        $arg["DIV"] = $db->getOne(knjg080Query::get_name_setup_div($model->div));

        //ALLチェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "id=\"CHECKALL\" onClick=\"return check_all_staff(this);\"", "");

        //データを取得
        $setval = array();
        $query = knjg080Query::getStaffList($model);
        $cnt = get_count($db->getcol($query));
        if ($model->div && $cnt) {
            list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
            $query = knjg080Query::getStaffList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $ume = "" ;
                for ($umecnt = 1; $umecnt <= strlen($row["STAFFCD"]) - (int)$simo; $umecnt++) {
                    $ume .= $fuseji;
                }
                if ($fuseji) {
                    $row["STAFFCD"] = $ume.substr($row["STAFFCD"], (strlen($row["STAFFCD"]) - (int)$simo), (int)$simo);
                }
                $checked = ($row["NAME_OUTPUT_FLG"] == "1") ? "checked" : "";
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["STAFFCD"], $checked, "1");
                $arg["data"][] = $row;
            }
        }

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('form3_update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('form3_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
        //終了
        $extra = "onclick=\"return btn_submit('back_change');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DIV", $model->div);
        knjCreateHidden($objForm, "selectstaffdata", $model->selectstaffdata);

        knjCreateHidden($objForm, "STAFF_YEAR", $model->year);
        //knjCreateHidden($objForm, "CHECKED", $model->checked);
        
        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg080Form3.html", $arg); 
    }
}
?>
