<?php

require_once('for_php7.php');

class knjb020Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjb020index.php", "", "list");
        $db = Query::dbCheckOut();
        
        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE) $arg["jscript"] = "OnAuthError();";

        //教科コンボ作成
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        
        $result = $db->query(knjb020Query::GetClasscd());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["CLASSCD"]."  ".$row["CLASSNAME"],
                           "value" => $row["CLASSCD"]);
        }
        
        $objForm->ae( array("type"        => "select",
                            "name"        => "CLASSCD",
                            "size"        => "1",
                            "extrahtml"   => "OnChange=\"btn_submit('list');\"",
                            "value"       => $model->classcd,
                            "options"     => $opt));
                            
        //ヘッダー作成
        $arg["HEADERS"] = array( "YEAR"      => CTRL_YEAR."年度",
                                 "SEMES"     => CTRL_SEMESTERNAME,
                                 "CLASSCD"   => $objForm->ge("CLASSCD"));
        
        //リストデータ作成
        $result   = $db->query(knjb020Query::GetListValue($model->classcd));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            $row["CHAIR"] = View::alink("knjb020index.php", 
                                         $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                                         "target=right_frame", 
                                          array( "cmd"      => "sel", 
                                                 "CHAIRCD"  => $row["CHAIRCD"]));

            $arg["data"][]  = $row;
        }

        $result->free();
        Query::dbCheckIn($db);

        //hidden作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

       if($model->cmd == "list"){
           $arg["reload"] = "window.open('knjb020index.php?cmd=sel&init=1', 'right_frame');";
       }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb020Form1.html", $arg); 
    }
}
?>
