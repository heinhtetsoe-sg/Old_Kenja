<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjb030SubForm4
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform4", "POST", "knjb030index.php", "", "subform4");

        $arg["NAME_SHOW"] = substr($model->term,0,4)."年度";

        $db = Query::dbCheckOut();

        //SQL文発行
        //教科書一覧取得
        $query = knjb030Query::selectQuerySubForm4($model->term);
        $result = $db->query($query);
        $i = 0;
		$param = VARS::get("param");
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            array_walk($row, "htmlspecialchars_array");

			if(strstr($param,$row["TEXTBOOKCD"])){
				$check = "checked";
			} else {
				$check = "";
			}
			//選択（チェック）
			$objForm->ae(array("type"		=> "checkbox",
							   "name"	  	=> "CHECK",
							   "value"	  	=> $row["TEXTBOOKCD"].",".$row["TEXTBOOKABBV"],
							   "extrahtml"	=> $check,
							   "multiple"	=> "1" ));

			$row["CHECK"] = $objForm->ge("CHECK");

            $row["backcolor"] = (strstr($param,$row["TEXTBOOKCD"])) ? "#ccffcc" : "#ffffff";  //#ccffff
//            $row["backcolor"] = "#ffffff";
            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);


        //選択ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_sentaku",
                            "value"     => "選 択",
                            "extrahtml" => "onclick=\"return btn_submit()\"" ));

        $arg["btn_sentaku"] = $objForm->ge("btn_sentaku");

        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"return top.main_frame.right_frame.closeit()\"" ));

        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
                                                
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb030SubForm4.html", $arg);
    }
}
?>