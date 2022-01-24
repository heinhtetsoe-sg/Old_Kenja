<?php

require_once('for_php7.php');

class knjp040kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp040kindex.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();
        
#        //起動チェック
#        if ($db->getOne(knjp040kQuery::checktoStart()) == "0") {
#            $link = REQUESTROOT."/A/KNJA110/knja110index.php?cmd=edit&schregno=".$model->schregno;
#            $arg["close"] = "closing_window('$link');";
#        }
        
        $row = $db->getRow(knjp040kQuery::getStudentName($model->schregno),DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $row["SCHREGNO"]."　".$row["NAME_SHOW"];

        //先頭は本人
        $relation = View::alink("knjp040kindex.php", "00:本人","target=bottom_frame",
                                                 array("relation" => $row["RELATIONSHIP"],
                                                       "name"     => $row["NAME_KANA"],
                                                       "cmd"      => "edit"));

        $arg["data"][0] = array("RELATIONSHIP" => $relation,
                                "NAME_SHOW"    => $row["NAME_SHOW"],
                                "NAME_KANA"    => $row["NAME_KANA"]);

        $result = $db->query(knjp040kQuery::getList($model->schregno));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            $row["RELATIONSHIP"] = View::alink("knjp040kindex.php", $row["RELATIONSHIP"].":".$row["RELA_NAME"],"target=bottom_frame",
                                                     array("relation" => $row["RELATIONSHIP"],
                                                           "name"     => $row["NAME_KANA"],
                                                           "cmd"      => "edit"));
            $arg["data"][] = array("RELATIONSHIP" => $row["RELATIONSHIP"],
                                   "NAME_SHOW"    => $row["NAME_SHOW"],
                                   "NAME_KANA"    => $row["NAME_KANA"]);
        }
        $result->free();
        Query::dbCheckIn($db);   

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
    
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp040kForm1.html", $arg);
    }
}
?>
