<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjb3040Form1.php 56585 2017-10-22 12:47:53Z maeshiro $
class knjb3040Form1
{
    function main(&$model){

$objForm = new form;
// フォーム作成
$arg["start"]   = $objForm->get_start("knjb3040Form1", "POST", "knjb3040index.php", "", "knjb3040Form1");

$arg["STAFFCD"] = STAFFCD;
$arg["dbname"]  = DB_DATABASE;

$arg["ctrl_m_ctrl_year"]             = CTRL_YEAR;
$arg["ctrl_m_ctrl_semester"]         = CTRL_SEMESTER;
$arg["ctrl_m_ctrl_date"]             = CTRL_DATE;
$arg["ctrl_m_attend_ctrl_date"]      = ATTEND_CTRL_DATE;
$arg["PROFICIENCY_COUNTFLG_DATADIV"] = $model->PROFICIENCY_COUNTFLG_DATADIV;
$arg["isShowStaffcd"] = $model->Properties["notShowStaffcd"] == "1" ? "false" : "true";
$arg["showMaskStaffCd"] = $model->Properties["showMaskStaffCd"];
$arg["SCHOOLCD"] = SCHOOLCD;
$arg["SCHOOLKIND"] = SCHOOLKIND;
//
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


$arg["finish"]  = $objForm->get_finish();
//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjb3040Form1.html", $arg); 
    }

}
?>
