<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjb0050Form1.php 56585 2017-10-22 12:47:53Z maeshiro $
class knjb0050Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb0050Form1", "POST", "knjb0050index.php", "", "knjb0050Form1");

		//$arg["STAFFCD"] = STAFFCD;
		$arg["dbname"]  = DB_DATABASE;
		/* ----------↓---------2005.02.23 add by nakamoto----------↓--------- */
		$arg["year"] = $model->year;
		$arg["semester"] = $model->semester;
		$arg["staffcd"] = $model->staffcd;
		/* ----------↑---------2005.02.23 add by nakamoto----------↑--------- */
        $arg["chaircd"] = $model->chaircd;           // 講座コード
        $arg["groupcd"] = $model->groupcd;           // 群コード

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
        View::toHTML($model, "knjb0050Form1.html", $arg); 
    }

}
?>
