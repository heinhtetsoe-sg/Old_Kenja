<?php

require_once('for_php7.php');

class knjz330form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]  = $objForm->get_start("MENU", "POST", "knjz330index.php", "", "MENU");
        $db = Query::dbCheckOut();

        //教科、科目、クラス取得
        $result = $db->query(knjz330Query::selectQuery($model, $model->root));
        $opt = $arg["data"] = array();
        $menuid = "";
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $path[$row["MENUID"]] = $row["MENUID"];
            if (strtoupper($row["PARENTMENUID"]) == "ROOT")
            {
                $opt[$row["MENUID"]] = array("label"  => $row["MENUNAME"],
                                             "value"  => $row["MENUID"]);
    
            } else {
                if (isset($path[$row["PARENTMENUID"]])){
                    $path[$row["MENUID"]] = $path[$row["PARENTMENUID"]] .",".$row["MENUID"];      //ルートのメニューID
                }
                $arg["tree"][] = View::alink("knjz330index.php", htmlspecialchars($row["MENUNAME"]),"target=\"right_frame\"",
                                    array("cmd"     => "main",
                                          "MENUID"  => $row["MENUID"],
                                          "PATH"    => $path[$row["MENUID"]],
                                          "MN"      => $row["MENUNAME"]));
                if (!$menuid){
                    $menuid = $row["MENUID"];
                    $mname = urlencode($row["MENUNAME"]);
                    $paths = $path[$row["MENUID"]];
                }
            }
        }
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "ROOT",
                            "size"       => "1",
                            "extrahtml"  => "onChange=\"return initialize();\"",
                            "value"      => $model->root,
                            "options"    => $opt));

        $arg["root"] = $objForm->ge("ROOT");


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => "tree"
                            ) );
        $arg["finish"]  = $objForm->get_finish();

        $arg["target"] = "right_frame";

        if($model->cmd =="tree" && $model->ini != "1"){
            $arg["reload"] ="window.open('knjz330index.php?cmd=main&MENUID=$menuid&MN=$mname&PATH=$paths', 'right_frame');";
        }
        unset($model->ini);
        $arg["REQUESTROOT"] = REQUESTROOT;
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz330Form1.html", $arg);
    }
}       
?>
    
