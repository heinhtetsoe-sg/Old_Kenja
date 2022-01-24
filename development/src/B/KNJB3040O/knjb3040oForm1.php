<?php
// kanji=漢字
// $Id: knjb3040oForm1.php,v 1.2 2012/05/21 10:51:29 maesiro Exp $
class knjb3040oForm1
{
    function main(&$model){

$objForm = new form;
// フォーム作成
$arg["start"]   = $objForm->get_start("knjb3040oForm1", "POST", "knjb3040oindex.php", "", "knjb3040oForm1");

$arg["STAFFCD"] = STAFFCD;
$arg["dbname"]  = DB_DATABASE;
$arg["dbhost"]  = DB_HOST;
//ポート指定を除いたDBホスト名を取出し、アプレットの取出し元とする。
$P = strpos($arg["dbhost"], ':');
$applethost = ($P>0)?substr($arg["dbhost"],0,$P):$arg["dbhost"];
$arg["APPHOST"] = '"http://'.$applethost.APPLET_ROOT.'_OLD/KNJB3040"';

$arg["ctrl_m_ctrl_year"]             = CTRL_YEAR;
$arg["ctrl_m_ctrl_semester"]         = CTRL_SEMESTER;
$arg["ctrl_m_ctrl_date"]             = CTRL_DATE;
$arg["ctrl_m_attend_ctrl_date"]      = ATTEND_CTRL_DATE;
$arg["PROFICIENCY_COUNTFLG_DATADIV"] = $model->PROFICIENCY_COUNTFLG_DATADIV;
$arg["useCurriculumcd"]              = $model->useCurriculumcd;
//
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


$arg["finish"]  = $objForm->get_finish();
//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjb3040oForm1.html", $arg); 
    }

}
?>
