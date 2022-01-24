<?php

require_once('for_php7.php');

class knjg080Form4 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("knjg080Form4", "POST", "knjg080index.php", "", "knjg080Form4");

        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //区分
        $arg["DIV"] = $db->getOne(knjg080Query::get_name_setup_div($model->div));

        //ALLチェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "id=\"CHECKALL\" onClick=\"return check_all_guard(this);\"", "");

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $cnt = get_count($db->getcol(knjg080Query::getGuardianList($model)));
        if($model->div && $cnt)
        {
            $query = knjg080Query::getGuardianList($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($firstflg) {
                    $setval = $row;
                    $firstflg = false;
                } else {
                    $checked = ($setval["GUARD_NAME_OUTPUT_FLG"] == "1") ? "checked" : "";
                    $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $setval["SCHREGNO"], $checked, "1");
                    $arg["data"][] = $setval;
                    $setval = $row;
                }
            }
            $checked = ($setval["GUARD_NAME_OUTPUT_FLG"] == "1") ? "checked" : "";
            $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED",  $setval["SCHREGNO"], $checked, "1");

            $arg["data"][] = $setval;
        }

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('form4_update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('form4_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
        //終了
        $extra = "onclick=\"return btn_submit('back_change2');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DIV", $model->div);
        knjCreateHidden($objForm, "selectguardiandata", $model->selectguardiandata);
        
        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg080Form4.html", $arg); 
    }
}
?>
