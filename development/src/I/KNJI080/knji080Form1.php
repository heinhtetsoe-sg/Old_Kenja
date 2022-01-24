<?php

require_once('for_php7.php');


class knji080Form1
{
    function main(&$model){

        //権限チェック          
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

//オブジェクト作成
$objForm = new form;

//フォーム作成//////////////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knji080Form1", "POST", "knji080index.php", "", "knji080Form1");

$db = Query::dbCheckOut();


//卒業年度・卒業期
$opt_year = array();
$query = knji080Query::GetYear();
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_year[]= array('label' => $row["GRADUATE_YEAR"]."年度卒(第".$row["PRESENT_EST"]."期卒)",
	                    'value' => $row["GRADUATE_YEAR"]);

	if($model->field["YEAR"]=="") $model->field["YEAR"] = $row["GRADUATE_YEAR"];
}

$objForm->ae( array("type"       => "select",
                    "name"       => "YEAR",
                    "size"       => "1",
                    "extrahtml"  => "onChange=\"return btn_submit('knji080');\"",
                    "value"      => $model->field["YEAR"],
                    "options"    => $opt_year));

$arg["data"]["YEAR"] = $objForm->ge("YEAR");


//クラス一覧
$opt_class = array();
$query = knji080Query::GetGradeHrClass($model->field["YEAR"],$model->control["学校区分"]);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_class[]= array('label' => $row["HR_NAME"],
                    	'value' => $row["VALUE"]);
}

$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
					"extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('left')\"",
                    "size"       => "15",
                    "options"    => $opt_class));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('right')\"",
                    "size"       => "15",
                    "options"    => array()));

$arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


//対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_rights",
                    "value"       => ">>",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

$arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


//対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_lefts",
                    "value"       => "<<",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

$arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


//対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right1",
                    "value"       => "＞",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


//対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left1",
                    "value"       => "＜",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


//印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");


//hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );

//プログラムＩＤ
$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJI080"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knji080Form1.html", $arg); 
}
}
?>
