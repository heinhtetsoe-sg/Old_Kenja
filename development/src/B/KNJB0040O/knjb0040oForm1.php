<?php
// kanji=漢字
// $Id: knjb0040oForm1.php,v 1.4 2005/10/27 05:03:24 takaesu Exp $
class knjb0040oForm1
{
    function main(&$model){

$objForm = new form;
// フォーム作成
$arg["start"]   = $objForm->get_start("knjb0040oForm1", "POST", "knjb0040oindex.php", "", "knjb0040oForm1");

$arg["STAFFCD"] = STAFFCD;
$arg["dbname"]  = DB_DATABASE;
$arg["dbhost"]  = DB_HOST;	//2010.01.26
//ポート指定を除いたDBホスト名を取出し、アプレットの取出し元とする。
$P = strpos($arg["dbhost"], ':');
$applethost = ($P>0)?substr($arg["dbhost"],0,$P):$arg["dbhost"];
$arg["APPHOST"] = '"http://'.$applethost.APPLET_ROOT.'_OLD/KNJB0040"';
//echo '['.$arg["APPHOST"].']<hr>';
$arg["useCurriculumcd"] = $model->Properties["useCurriculumcd"] == "1" ? "true" : "false";

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
View::toHTML($model, "knjb0040oForm1.html", $arg); 
    }

}
?>
