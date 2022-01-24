<?php

require_once('for_php7.php');


class knja240Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knja240Form1", "POST", "knja240index.php", "", "knja240Form1");


//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

$arg["data"]["YEAR"] = $model->control["年度"];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => $model->control["年度"],
                    ) );

//処理日を作成する/////////////////////////////////////////////////////////////////////////////////
$arg["data"]["DATE"]=View::popUpCalendar($objForm,"DATE",isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);


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

$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJA240"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "GRADE_HVAL",
                    "value"      => $model->control["学年数"]
                    ) );


//学期期間日付取得////////////////////////////////////////////////////////////////////////////
$semester = $model->control['学期開始日付'][1] ."," .$model->control['学期終了日付'][1];
$semester = $semester ."," .$model->control['学期開始日付'][2] ."," .$model->control['学期終了日付'][2];
$semester = $semester ."," .$model->control['学期開始日付'][3] ."," .$model->control['学期終了日付'][3];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEMESTER"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEME_DATE",
                    "value"     => $semester
                    ) );
//////////////////////////////////////////////////////////////////////////////////////////////


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "useSchregRegdHdat",
                    "value"      => $model->Properties["useSchregRegdHdat"]
                    ) );



//フォーム終わり/////////////////////////////////////////////////////////////////////////////////////////////////////
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 ///////////////////////////////////////////////////////////
View::toHTML($model, "knja240Form1.html", $arg); 
}
}
?>
