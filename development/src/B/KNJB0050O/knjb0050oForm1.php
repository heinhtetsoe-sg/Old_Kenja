<?php
// kanji=漢字
// $Id: knjb0050oForm1.php,v 1.4 2009/05/26 08:17:47 takara Exp $
class knjb0050oForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb0050oForm1", "POST", "knjb0050oindex.php", "", "knjb0050oForm1");

		//$arg["STAFFCD"] = STAFFCD;
		$arg["dbhost"]  = DB_HOST;	//2010.01.26
		//ポート指定を除いたDBホスト名を取出し、アプレットの取出し元とする。
		$P = strpos($arg["dbhost"], ':');
		$applethost = ($P>0)?substr($arg["dbhost"],0,$P):$arg["dbhost"];
		$arg["APPHOST"] = '"http://'.$applethost.APPLET_ROOT.'_OLD/KNJB0050"';
		//echo '['.$arg["APPHOST"].']<hr>';
		$arg["dbname"]  = DB_DATABASE;
		/* ----------↓---------2005.02.23 add by nakamoto----------↓--------- */
		$arg["year"] = $model->year;
		$arg["semester"] = $model->semester;
		$arg["staffcd"] = $model->staffcd;
		/* ----------↑---------2005.02.23 add by nakamoto----------↑--------- */
        $arg["chaircd"] = $model->chaircd;           // 講座コード
        $arg["groupcd"] = $model->groupcd;           // 群コード
        $arg["useCurriculumcd"] = $model->Properties["useCurriculumcd"] == "1" ? "true" : "false";

		$arg["ctrl_m_ctrl_year"]        = $model->year;
		$arg["ctrl_m_ctrl_semester"]    = $model->semester;
        $arg["ctrl_m_ctrl_date"]        = CTRL_DATE;
        $arg["ctrl_m_attend_ctrl_date"] = ATTEND_CTRL_DATE;

		$objForm->ae( array("type"      => "hidden",
        		            "name"      => "cmd"
                		    ) );

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb0050oForm1.html", $arg); 
    }

}
?>
