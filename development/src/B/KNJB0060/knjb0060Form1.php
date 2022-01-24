<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjb0060Form1.php,v 1.4 2014/04/14 06:23:23 maesiro Exp $
class knjb0060Form1
{
    function main(&$model){

$objForm = new form;
// フォーム作成
$arg["start"]   = $objForm->get_start("knjb0060Form1", "POST", "knjb0060index.php", "", "knjb0060Form1");

$arg["staffcd"]  = STAFFCD      ;
$arg["year"]     = CTRL_YEAR    ;
$arg["semester"] = CTRL_SEMESTER;
$arg["dbname"]   = DB_DATABASE  ;
$arg["useCurriculumcd"] = $model->Properties["useCurriculumcd"] == "1" ? "true" : "false";
$arg["isShowStaffcd"] = $model->Properties["notShowStaffcd"] == "1" ? "false" : "true";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $arg["SCHOOLCD"] = sprintf("%012d", SCHOOLCD);
                $arg["SCHOOLKIND"] = SCHOOLKIND;
        }

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
View::toHTML($model, "knjb0060Form1.html", $arg); 
    }

}
?>
