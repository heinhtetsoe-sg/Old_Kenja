<?php

require_once('for_php7.php');

class knjz151_2Form1
{
    function main(&$model)
    {
    //権限チェック
    if (AUTHORITY != DEF_UPDATABLE){
        $arg["jscript"] = "OnAuthError();";
    }

    $objForm = new form;
    
    //フォーム作成
    $arg["start"]   = $objForm->get_start("list", "POST", "knjz151_2index.php", "", "edit");

    $db     = Query::dbCheckOut();
    $result = $db->query(knjz151_2Query::getList($model));
    
    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        //レコードを連想配列のまま配列$arg[data]に追加していく。 
        array_walk($row, "htmlspecialchars_array");
        if ($row["ISSUECOMPANYCD"] == $model->issuecd) {
            $row["ISSUECOMPANYNAME"] = ($row["ISSUECOMPANYNAME"]) ? $row["ISSUECOMPANYNAME"] : "　";
            $row["ISSUECOMPANYNAME"] = "<a name=\"target\">{$row["ISSUECOMPANYNAME"]}</a><script>location.href='#target';</script>";
        }
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
    View::toHTML($model, "knjz151_2Form1.html", $arg);
    }
} 
?>
