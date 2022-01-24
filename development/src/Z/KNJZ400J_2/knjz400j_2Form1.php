<?php

require_once('for_php7.php');

class knjz400j_2Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz400j_2index.php", "", "edit");

        $db = Query::dbCheckOut();
        //教科取得
        $query = "SELECT * FROM class_mst ";
        $result = $db->query($query);
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[$row["CLASSCD"]] = $row["CLASSNAME"];
        }
        $result->free();
        //一覧取得
        $query = "select * from jviewname_mst order by viewcd";
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。 
             array_walk($row, "htmlspecialchars_array");
            $row["CLASSNAME"] = $opt[substr($row["VIEWCD"], 0 ,2)];
             $arg["data"][] = $row; 
        }
        $result->free();
        Query::dbCheckIn($db);
        $arg["year"] = $model->year_code;
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz400j_2Form1.html", $arg); 
    }
}
?>
