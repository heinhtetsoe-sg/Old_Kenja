<?php

require_once('for_php7.php');

class knjh303Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh303Form1", "POST", "knjh303index.php", "", "knjh303Form1");

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $query = knjh303Query::getSchregno_name($model->schregno);
        $Row                  = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"]     = $Row["NAME_SHOW"];
        
        //学籍賞罰データよりデータを取得
        if($model->schregno)
        {        
            $result = $db->query(knjh303Query::selectQuery($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                 $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);


		//終了ボタンを作成する
		$objForm->ae( array("type"		=> "button",
                    		"name"		=> "btn_end",
                    		"value"		=> "戻る",
                    		"extrahtml"	=> "onclick=\"closeWin();\"" ) );

		$arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh303Form1.html", $arg);
    }
}
?>
