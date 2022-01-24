<?php

require_once('for_php7.php');

class knjz330Form3
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz330index.php", "", "main");
        $db           = Query::dbCheckOut();

        //サブシステムコンボ
        $result = $db->query(knjz330Query::selectQueryAuth($model));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (strtoupper($row["PARENTMENUID"]) == "ROOT"){
                $opt[] = array("label"  => $row["MENUNAME"], "value" => $row["SUBMENUID"]);
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

    //    $authnames = array("0" => "更新可",
    //                       "1" => "制限付更新可",
    //                       "2" => "参照可",
    //                       "3" => "制限付更新可",
    //                       "9" => "権限なし");
        $authnames = array("0" => "更新可",
                           "1" => "更新可制限付",
                           "2" => "参照可",
                           "3" => "参照可制限付",
                           "9" => "権限なし");

        $result    = $db->query(knjz330Query::GetProgramAuthlist($model, $model->subsystem));

        $i = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {            
            $row["authname"] = $authnames[$row["GROUPAUTH"]];
            $arg["data"][] = $row; 
            $i++;
        }

        Query::dbCheckIn($db);

        $arg["DIV_HEIGHT"] = ($i > 20) ? "450" : "*";

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "BTN_END",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["BTN_END"] = $objForm->ge("BTN_END");

            //$link1="/~gaku/gakuseki/src/LZ/LZZ020/index.php";

        //戻るボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"window.open('".REQUESTROOT."/Z/KNJZ330/knjz330index.php?ini=1', '_self');\""));

            $arg["back"] = $objForm->ge("back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz330Form3.html", $arg); 
    }
}
?>
