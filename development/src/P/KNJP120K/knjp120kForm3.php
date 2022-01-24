<?php

require_once('for_php7.php');

require_once('knjp120kQuery2.inc');
class knjp120kForm3
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp120kindex.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();

        $result = $db->query(knjp120kQuery2::getList($model));
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
        View::toHTML($model, "knjp120kForm3.html", $arg);
    }
}
?>
