<?php

require_once('for_php7.php');

class knjh332Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh332index.php", "", "edit");

       //デフォルト値
    	if($model->groupcd ==""){
        	$row = knjh332Query::getFirst_GroupKey();
        	$model->groupcd = sprintf("%08d",$row["GROUPCD"]);
    	}

        $db     = Query::dbCheckOut();

        //権限設定グループコンボ設定
        $opt_grp  = array();
        $result    = $db->query(knjh332Query::getGroup());   
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_grp[] = array("label" => $row["GROUPNAME"], 
                           		"value" => sprintf("%08d",$row["GROUPCD"]));

        }
        $result->free();

        $objForm->ae( array("type"      => "select",
                            "name"		=> "GROUPCD",
                            "size"		=> "1",
                           	"value"		=> $model->groupcd,
                           	"extrahtml" => "onchange=\"btn_submit('group');\"",
                            "options"	=> $opt_grp ));

        $arg["GROUPCD"] = $objForm->ge("GROUPCD");

		//リスト
        $query  = knjh332Query::getList($model->groupcd);
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             //レコードを連想配列のまま配列$arg[data]に追加していく。 
             array_walk($row, "htmlspecialchars_array");
             $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "group"){
            $arg["reload"] = "window.open('knjh332index.php?cmd=edit','right_frame');";
		}

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh332Form1.html", $arg); 
    }
}
?>
