<?php

require_once('for_php7.php');


class knjz403Form1{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjz403Form1", "POST", "knjz403index.php", "", "knjz403Form1");
        //DB接続
        $db     = Query::dbCheckOut();
        
        //処理年度
        $arg["YEAR"] = CTRL_YEAR;
        
        //前年度からコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["copy"] = knjCreateBtn($objForm, "copy", "前年度からコピー", $extra);

        //データ表示
        $result = $db->query(knjz403Query::selectQuery($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
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
        View::toHTML($model, "knjz403Form1.html", $arg); 

    }
}
?>
