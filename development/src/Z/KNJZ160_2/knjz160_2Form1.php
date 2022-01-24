<?php

require_once('for_php7.php');

class knjz160_2Form1
{
    function main(&$model)
    {
        $arg["jscript"] = "";
        $arg["data"] = array();
        
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz160_2index.php", "", "edit");

        $db     = Query::dbCheckOut();
        $result = $db->query(knjz160_2Query::getList());
    
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][] = $row; 
        }
        $result->free();
        Query::dbCheckIn($db);
    
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz160_2Form1.html", $arg); 
    }
}
?>
