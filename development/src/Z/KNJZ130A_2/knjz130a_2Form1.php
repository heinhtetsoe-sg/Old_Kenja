<?php

require_once('for_php7.php');

class knjz130a_2Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz130a_2index.php", "", "edit");

        $db = Query::dbCheckOut();

        $opt = array();
        $query = knjz130a_2Query::getCombo();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。 
             $opt[] = array("label" => $row["NAMECD"]."　".htmlspecialchars($row["CDMEMO"]),
                            "value" => $row["NAMECD"]);
            
            if (!isset($model->namecd1)) $model->namecd1 = $row["NAMECD"];
        
        }

        //コンボボックスを作成
        $objForm->ae( array("type"       => "select",
                            "name"       => "NAMECD1",
                            "size"       => "1",
                            "value"      => $model->namecd1,
                            "extrahtml"   => "onchange=\"return btn_submit('list');\"",
                            "options"    => $opt));

        $arg["top"]["NAMECD1"] = $objForm->ge("NAMECD1");

        $query = knjz130a_2Query::getList($model);
        $result = $db->query($query);
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
        View::toHTML($model, "knjz130a_2Form1.html", $arg); 
    }
}        
?>
