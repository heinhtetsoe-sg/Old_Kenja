<?php

require_once('for_php7.php');


class knjb033Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb033Form1", "POST", "knjb033index.php", "", "knjb033Form1");

		$arg["STAFFCD"] = STAFFCD;
		$arg["year"] = $model->control["年度"];
		$arg["semester"] = $model->control["学期"];
		$arg["dbname"]  = DB_DATABASE;

        $arg["chaircd"] = $model->chaircd;           // 講座コード
        $arg["groupcd"] = $model->groupcd;           // 群コード
        $arg["groupname"] = $model->groupname;       // 群名称

		$objForm->ae( array("type"      => "hidden",
        		            "name"      => "cmd"
                		    ) );

//if($model->auth != DEF_UPDATABLE){
//    $arg["Security"] = " close_window(); " ;
//}

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb033Form1.html", $arg); 
    }

}
?>
