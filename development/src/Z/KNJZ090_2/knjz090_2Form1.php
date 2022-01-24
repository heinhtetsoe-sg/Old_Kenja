<?php

require_once('for_php7.php');

class knjz090_2Form1
{
    function main(&$model)
    {
    //権限チェック
    if (AUTHORITY != DEF_UPDATABLE){
        $arg["jscript"] = "OnAuthError();";
    }

    $objForm = new form;
    
    //フォーム作成
    $arg["start"]   = $objForm->get_start("list", "POST", "knjz090_2index.php", "", "edit");

    $db     = Query::dbCheckOut();
//    $query  = "select * from FINSCHOOL_MST order by FINSCHOOLCD";
//    $result = $db->query($query);
    $result = $db->query(knjz090_2Query::selectQuery($model));
    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
         //レコードを連想配列のまま配列$arg[data]に追加していく。 
         array_walk($row, "htmlspecialchars_array");
         $arg["data"][] = $row; 
    }
    $result->free();
    Query::dbCheckIn($db);

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjz090_2Form1.html", $arg);
    }
} 
?>
