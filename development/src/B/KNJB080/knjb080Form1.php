<?php

require_once('for_php7.php');


class knjb080Form1
{
    function main(&$model){
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb080Form1", "POST", "knjb080index.php", "", "knjb080Form1");

        //時間割選択コンボボックスを作成
        $row2 = knjb080Query::getBscHdQuery();
        $objForm->ae( array("type"       => "select",
                            "name"       => "TITLE",
                            "size"       => "1",
                            "value"      => $model->field["TITLE"],
                            "options"    => isset($row2)?$row2:array(),
                            "extrahtml"  => ""));

        $arg["data"]["TITLE"] = $objForm->ge("TITLE");

        //施設選択コンボボックスを作成
        $row1 = $query = knjb080Query::getFacility();
        $objForm->ae( array("type"       => "select",
                            "name"       => "FACCD_NAME1",
                            "size"       => "1",
                            "value"      => $model->field["FACCD_NAME1"],
                            "extrahtml"  => "",
                            "options"    => isset($row1)?$row1:array()));

        $objForm->ae( array("type"       => "select",
                            "name"       => "FACCD_NAME2",
                            "size"       => "1",
                            "value"      => isset($model->field["FACCD_NAME2"])?$model->field["FACCD_NAME2"]:$row1[get_count($row1)-1]["value"],
                            "extrahtml"  => "",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["FACCD_NAME1"] = $objForm->ge("FACCD_NAME1");
        $arg["data"]["FACCD_NAME2"] = $objForm->ge("FACCD_NAME2");


        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJB080"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "BSCSEQ"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER"
                            ) );
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb080Form1.html", $arg); 
    }

}
?>
