<?php

require_once('for_php7.php');

class knja110_3bForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knja110_3bindex.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();

        //起動チェック
        if ($db->getOne(knja110_3bQuery::checktoStart()) == "0") {
            $link = REQUESTROOT."/A/KNJA110B/knja110bindex.php?cmd=edit&schregno=".$model->SCHREGNO;
            $arg["close"] = "closing_window('$link');";
        }

        //項目名（京都用）
        if ($model->kyoto > 0) {
            //項目名
            $arg["label"]["TRANSFERREASON"] = '（国名）';
            $arg["label"]["TRANSFERPLACE"]  = '（学校名）';
            $arg["label"]["TRANSFERADDR"]   = '（学年）';
        }

        //入学日付取得
        $entdate = $db->getOne(knja110_3bQuery::getEntDate($model->SCHREGNO));

        //生徒名表示
        $result = $db->query(knja110_3bQuery::getStudentName($model->SCHREGNO));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $row["SCHREGNO"]."　".$row["NAME"];

        //履歴表示
        $result = $db->query(knja110_3bQuery::getList($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //異動区分がログイン年度で名称マスタ登録有無をチェック
            $transfercd = "";
            $transfercd = $db->getOne(knja110_3bQuery::getTransfercdCheck($row["TRANSFERCD"]));
            //入学日付後の異動開始日にリンクをはる
            if ($row["TRANSFER_SDATE"] >= $entdate && $transfercd != 0) {
                $transfername = "<a href=\"knja110_3bindex.php?cmd=edit&TRANSFERCD=".$row["TRANSFERCD"]."&TRANSFER_SDATE=".$row["TRANSFER_SDATE"]."\" target=\"bottom_frame\">".$row["TRANSFERCD"]."：".$row["TRANSFERNAME"] ."</a>";
            } else {
                $transfername = $row["TRANSFERCD"]."：".$row["TRANSFERNAME"];
            }

            $arg["data"][] = array("TRANSFERNAME"       => $transfername,
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
        View::toHTML($model, "knja110_3bForm1.html", $arg);
    }
}
?>
