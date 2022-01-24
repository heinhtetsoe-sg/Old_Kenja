<?php

require_once('for_php7.php');

class knja110_3Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knja110_3index.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();

        //起動チェック
        if ($db->getOne(knja110_3Query::checktoStart()) == "0") {
            $link = REQUESTROOT."/A/KNJA110/knja110index.php?cmd=edit&schregno=".$model->SCHREGNO;
            $arg["close"] = "closing_window('$link');";
        }

        $result = $db->query(knja110_3Query::getStudentName($model->SCHREGNO));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $row["SCHREGNO"]."　".$row["NAME"];

        $result = $db->query(knja110_3Query::getList($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {

            $arg["data"][] = array("TRANSFERNAME"       => $row["TRANSFERNAME"],
                                   "TRANSFERCD"         => $row["TRANSFERCD"],
                                   "TRANSFER_SDATE"     => str_replace("-","/",$row["TRANSFER_SDATE"]),
                                   "TRANSFER_EDATE"     => str_replace("-","/",$row["TRANSFER_EDATE"]),
                                   "TRANSFERREASON"     => $row["TRANSFERREASON"],
                                   "TRANSFERPLACE"      => $row["TRANSFERPLACE"],
                                   "TRANSFERADDR"       => $row["TRANSFERADDR"],
                                   "ABROAD_CLASSDAYS"   => $row["ABROAD_CLASSDAYS"],
                                   "ABROAD_CREDITS"     => $row["ABROAD_CREDITS"]);
        }
        $result->free();
        Query::dbCheckIn($db);   

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja110_3Form1.html", $arg);
    }
}
?>
