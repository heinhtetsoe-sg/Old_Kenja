<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjc010Form1.php 56585 2017-10-22 12:47:53Z maeshiro $
class knjc010Form1
{
	function main(&$model)
	{

$objForm = new form;
// フォーム作成
$arg["start"]   = $objForm->get_start("knjc010", "POST", "knjc010index.php", "", "knjc010");

$arg["STAFFCD"] = STAFFCD;
$arg["disp_staff"] = $model->staffcd;
if( isset($model->syoribi) ){
	$arg["syoribi"] = str_replace("/", "-", $model->syoribi);
}
else{
	$arg["syoribi"] = isset($syoribi)?str_replace("/", "-", $syoribi):str_replace("/", "-", $model->control["学籍処理日"]);
}
$arg["kouji"]   = $model->kouji;
$arg["chaircd"]   = $model->chaircd;
$arg["grade"]   = $model->grade;
$arg["hrclass"]   = $model->hrclass;
$arg["debug"]   = isset($debug)? $debug:"true";
$arg["dbname"]  = DB_DATABASE;

$arg["ctrl_m_ctrl_year"]        = CTRL_YEAR;
$arg["ctrl_m_ctrl_semester"]    = CTRL_SEMESTER;
$arg["ctrl_m_ctrl_date"]        = CTRL_DATE;
$arg["ctrl_m_attend_ctrl_date"] = ATTEND_CTRL_DATE;
//
$objForm->ae( array("type"      => "hidden",
                   	"name"      => "cmd"
                   	) );

$arg["finish"]  = $objForm->get_finish();
//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjc010Form1.html", $arg); 

	}
}
?>
