<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjz150_2Form1.php 56580 2017-10-22 12:35:29Z maeshiro $
class knjz150_2Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjz150_2Form1", "POST", "knjz150_2index.php", "", "knjz150_2Form2");

        $db     = Query::dbCheckOut();
        $query  = "SELECT * FROM TEXTBOOK_MST ORDER BY TEXTBOOKCD";
        $result = $db->query($query);
    
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
        View::toHTML($model, "knjz150_2Form1.html", $arg); 
    }
}
?>
