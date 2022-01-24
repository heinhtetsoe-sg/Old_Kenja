<?php

require_once('for_php7.php');

class knjg080Form2 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("knjg080Form2", "POST", "knjg080index.php", "", "knjg080Form2");

        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //区分
        $arg["DIV"] = $db->getOne(knjg080Query::get_name_setup_div($model->div));

        //ALLチェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "id=\"CHECKALL\" onClick=\"return check_all(this);\"", "");

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $cnt = get_count($db->getcol(knjg080Query::getStudentList($model)));
        if($model->div && $cnt)
        {
            $result = $db->query(knjg080Query::getStudentList($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($firstflg) {
                    $setval = $row;
                    $firstflg = false;
                } else {
                    $checked = ($setval["NAME_OUTPUT_FLG"] == "1") ? "checked" : "";
                    $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $setval["SCHREGNO"], $checked, "1");
                    $arg["data"][] = $setval;
                    $setval = $row;
                }
            }
            $checked = ($setval["NAME_OUTPUT_FLG"] == "1") ? "checked" : "";
            $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED",  $setval["SCHREGNO"], $checked, "1");

            $arg["data"][] = $setval;
        }

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('form2_update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('form2_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
        //終了
        $extra = "onclick=\"return btn_submit('back');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DIV", $model->div);
        knjCreateHidden($objForm, "selectdata", $model->selectdata);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg080Form2.html", $arg); 
    }
}
?>
