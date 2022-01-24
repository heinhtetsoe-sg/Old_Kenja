<?php

require_once('for_php7.php');

class knjl130kForm2
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl130kindex.php", "", "main");

        $db = Query::dbCheckOut();

        $Row = $db->getRow(knjl130kQuery::get_appliname($model), DB_FETCHMODE_ASSOC);

        //氏名,氏名かなにシングルコーテーションが含まれていたらSQL用にエスケープする
        $exist = strstr($Row["NAME"], "'");
        if ($exist != false) {
            $plode = explode("'", $Row["NAME"]);
            for ($i=0; $i<get_count($plode); $i++) {
                if ($i != 0) {
                    $imp = $imp . "''";
                }
                $imp = $imp . $plode[$i];
            }
            $Row["NAME"] = $imp;
        }

        $imp = "";
        $exist = strstr($Row["NAME_KANA"], "'");
        if ($exist != false) {
            $plode = explode("'", $Row["NAME_KANA"]);
            for ($i=0; $i<get_count($plode); $i++) {
                if ($i != 0) {
                    $imp = $imp . "''";
                }
                $imp = $imp . $plode[$i];
            }
            $Row["NAME_KANA"] = $imp;
        }


#        $result = $db->query(knjl130kQuery::get_consultation($model, $Row["NAME"], $Row["NAME_KANA"]));    #2005/09/14 arakaki
        $result = $db->query(knjl130kQuery::get_consultation($model, $Row["NAME"], $Row["NAME_KANA"],$db->getOne(knjl130kQuery::GetJorH())));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $arg["data"]["ORG_MAJORCD".$row["DATADIV"]."_".$row["WISHNO"]] = $row["EXAMCOURSE_MARK"];
            $arg["data"]["ORG_SHDIV".$row["DATADIV"]."_".$row["WISHNO"]] = $row["SHDIV"];
            $arg["data"]["ORG_JUDGEMENT".$row["DATADIV"]."_".$row["WISHNO"]] = $row["JUDGEMENT"];
        }

        Query::dbCheckIn($db);

        //戻るボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"top.main_frame.closeit();\"" ) );

        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl130kForm2.html", $arg);
    }
}
?>