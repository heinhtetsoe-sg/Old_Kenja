<?php

require_once('for_php7.php');

class knjm210_3Form4
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjm210_3index.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();

//        $model->schregno = "20031935";
        $result = $db->query(knjm210_3Query::getStudentName($model->schregno));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $arg["NAME"] = "　　　学籍番号：".$row["SCHREGNO"]."　　　氏名：".$row["NAME"];

        $result = $db->query(knjm210_3Query::getList($model->schregno));
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
        View::toHTML($model, "knjm210_3Form4.html", $arg);
    }
}
?>
