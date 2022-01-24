<?php

require_once('for_php7.php');

class knjz320Form3
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz320index.php", "", "main");
        $db           = Query::dbCheckOut();

        //サブシステムコンボ
        $result = $db->query(knjz320Query::selectQueryAuth($model));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (strtoupper($row["PARENTMENUID"]) == "ROOT"){
                $opt[] = array("label"  => $row["MENUNAME"], "value"  => $row["SUBMENUID"]);
            }
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "SUBSYSTEM_COMBO",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('listauth');\"",
                            "value"      => $model->subsystem,
                            "options"    => $opt));

        $arg["TOP"]["SUBSYSTEM_COMBO"] = $objForm->ge("SUBSYSTEM_COMBO");
    
        if ($model->subsystem == "") {
            $model->subsystem = $opt[0]["value"];
        }

        $authnames = array("0" => "更新可",
                           "1" => "制限付更新可",
                           "2" => "参照可",
                           "3" => "制限付更新可",
                           "9" => "権限なし");

            //ソート
            $mark = array("(▼)","(▲)");

            switch ($model->s_id) {
                    case "1":
                            $mark1 = $mark[$model->sort[$model->s_id]];break;
                    case "2":
                            $mark2 = $mark[$model->sort[$model->s_id]];break;
            }

            $arg["S_SORT"] = View::alink("knjz320index.php", "<font color=\"#ffffff\">職員コード".$mark1."</font>", "target=_self tabindex=\"-1\"", 
                            array("cmd"         => "listauth",
                                  "sort1"       => ($model->sort["1"] == "1")?"0":"1",
                                  "s_id"        => "1" ) );

            $arg["K_SORT"] = View::alink("knjz320index.php", "<font color=\"#ffffff\">氏名かな".$mark2."</font>", "target=_self tabindex=\"-1\"", 
                            array("cmd"         => "listauth",
                                  "sort2"       => ($model->sort["2"] == "1")?"0":"1",
                                  "s_id"        => "2" ) );

        $result    = $db->query(knjz320Query::GetProgramAuthlist($model->subsystem,$model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $h = $h+1;
            $row["authname"] = $authnames[$row["USERAUTH"]];
            $arg["data"][] = $row; 

        }

            $arg["h"]=($h>20)? "450" : "*";;

        Query::dbCheckIn($db);

            //戻るボタン作成
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_end",
                                "value"       => "戻 る",
                                "extrahtml"   => "tabindex=\"$ii+3\" onclick=\"window.open('".REQUESTROOT."/Z/KNJZ320/knjz320index.php?ini_back=1', '_self');\"" ) );

            $arg["btn_end"]  = $objForm->ge("btn_end");

            //hiddenを作成する
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz320Form3.html", $arg); 
    }
}
?>
