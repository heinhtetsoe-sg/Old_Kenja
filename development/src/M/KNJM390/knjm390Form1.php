<?php

require_once('for_php7.php');

/********************************************************************/
/* スクーリング出席登録                             山城 2005/03/18 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm390Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm390index.php", "", "main");

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //スクーリング種別
        $opt_corse = array();
        $db = Query::dbCheckOut();
        $result = $db->query(knjm390Query::selectName("M001"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_corse[] = array("label" => $row["NAME1"],
                                 "value" => $row["NAMECD2"]);
        }

        if (!$model->field["COURSE"]) $model->field["COURSE"] = $opt_corse[0]["value"];
        $extra = "onChange=\"btn_submit('');\"";
        $arg["sel"]["COURSE"] = knjCreateCombo($objForm, "COURSE", $model->field["COURSE"], $opt_corse, $extra, 1);

        $result->free();
        Query::dbCheckIn($db);

        //日付データ
        if ($model->Date == "") $model->Date = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATE"] = View::popUpCalendar($objForm  ,"DATE" ,str_replace("-","/",$model->Date),"reload=true");
        //チェック用hidden
        //hidden
        knjCreateHidden($objForm, "YEAR", $model->Year);
        knjCreateHidden($objForm, "DEFOULTDATE", $model->Date);
        knjCreateHidden($objForm, "DEFOULTSEME", $model->semester);
        knjCreateHidden($objForm, "GAKKISU", $model->control["学期数"]);
        knjCreateHidden($objForm, "SEME1S", $model->control["学期開始日付"]["1"]);
        knjCreateHidden($objForm, "SEME1E", $model->control["学期終了日付"]["1"]);
        knjCreateHidden($objForm, "SEME2S", $model->control["学期開始日付"]["2"]);
        knjCreateHidden($objForm, "SEME2E", $model->control["学期終了日付"]["2"]);

        //チェック用hidden
        if ($model->control["学期数"] == 3) {
            knjCreateHidden($objForm, "SEME3S", $model->control["学期開始日付"]["3"]);
            knjCreateHidden($objForm, "SEME3E", $model->control["学期終了日付"]["3"]);
        }

        //講座コンボ
        $opt_chair = array();
        $db = Query::dbCheckOut();
        $result = $db->query(knjm390Query::getAuth($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("label" => $row["CHAIRNAME"],
                                 "value" => $row["CHAIRCD"]);
        }
        if (!$model->field["CHAIR"]) {
            $model->field["CHAIR"]        = $opt_chair[0]["value"];
        }
        if (!$opt_chair[0]) {
            $arg["Closing"] = " closing_window('MSG300');";
        }
        $extra = "onChange=\"btn_submit('');\" ";
        $arg["sel"]["CHAIR"] = knjCreateCombo($objForm, "CHAIR", $model->field["CHAIR"], $opt_chair, $extra, 1);

        $result->free();
        Query::dbCheckIn($db);

        if ($model->field["COURSE"] == 1) {
            $disabled = "";
        } else {
            $disabled = "disabled";
        }
        //校時
        $opt_peri = array();
        $db = Query::dbCheckOut();
        $result = $db->query(knjm390Query::selectName("B001"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_peri[] = array("label" => $row["NAME1"],
                                "value" => $row["NAMECD2"]);
        }

        if (!$model->field["PERIOD"]) $model->field["PERIOD"] = $opt_peri[0]["value"];
        $extra = "$disabled onChange=\"btn_submit('');\" ";
        $arg["sel"]["PERIOD"] = knjCreateCombo($objForm, "PERIOD", $model->field["PERIOD"], $opt_peri, $extra, 1);

        $result->free();
        Query::dbCheckIn($db);

        //担当者
        $opt_staf = array();
        $db = Query::dbCheckOut();
        $result = $db->query(knjm390Query::selectStaff($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_staf[] = array("label" => $row["STAFFNAME"],
                                "value" => $row["STAFFCD"]);
        }

        if (!$model->field["STAFF"] && $model->User == 0) {
            $model->field["STAFF"] = $opt_staf[0]["value"];
        } else if (!$model->field["STAFF"] && $model->User == 1) {
            $model->field["STAFF"] = STAFFCD;
        }
        $extra = "$disabled onChange=\"btn_submit('change');\" ";
        $arg["sel"]["STAFF"] = knjCreateCombo($objForm, "STAFF", $model->field["STAFF"], $opt_staf, $extra, 1);

        $result->free();
        Query::dbCheckIn($db);

        //回数
        $opt_seq = array();
        $namecnt = 0;
        $db = Query::dbCheckOut();
        $result = $db->query(knjm390Query::selectSeq($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_seq[] = array("label" => $row["SCHOOLING_SEQ"],
                               "value" => $row["SCHOOLING_SEQ"]);
            $namecnt++;
        }

        if ($namecnt > 1) {
            $arg["sel"]["fukusu"] = "回数が複数あります。";
        }
        $model->field["SCHOOLING_SEQ"] = ($model->cmd == 'change') ? $model->field["SCHOOLING_SEQ"] : $opt_seq[0]["value"];
        if ($namecnt == 0) $model->field["SCHOOLING_SEQ"] = $opt_seq[0]["value"];
        $extra = "$disabled onchange = \"return btn_submit('change')\"";
        $arg["sel"]["SCHOOL_SEQ"] = knjCreateCombo($objForm, "SCHOOLING_SEQ", $model->field["SCHOOLING_SEQ"], $opt_seq, $extra, 1);

        $result->free();
        Query::dbCheckIn($db);

        //出席校
        if ($model->Properties["usePartnerSchool"] == "1") {
            $opt_partner = array();
            $db = Query::dbCheckOut();
            $result = $db->query(knjm390Query::getPartnerSchool($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_partner[] = array("label" => $row["PARTNER_SCHOOL_NAME"],
                                       "value" => $row["PARTNER_SCHOOLCD"]);
            }

            if (!$model->field["PARTNER_SCHOOL"]) $model->field["PARTNER_SCHOOL"] = $opt_partner[0]["value"];
            $extra = "$disabled onChange=\"btn_submit('change');\" ";
            $arg["sel"]["PARTNER_SCHOOL"] = knjCreateCombo($objForm, "PARTNER_SCHOOL", $model->field["PARTNER_SCHOOL"], $opt_partner, $extra, 1);

            $result->free();
            Query::dbCheckIn($db);
        }

        //備考
        $extra = "style=\"height:35px;\"";
        $arg["sel"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", "2", "20", "", $extra, $model->field["REMARK"]);

        //学籍番号
        if ($model->cmd == 'addread') {
            $model->field["SCHREGNO"] = '';
        }
        $extra = "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"checkkey()\";";
        $arg["sel"]["SCHREGNO"] = knjCreateTextBox($objForm, $model->field["SCHREGNO"], "SCHREGNO", 8, 8, $extra);

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/
        //抽出データ出力
        $schcnt = 0;
        $db = Query::dbCheckOut();
        $result = $db->query(knjm390Query::getSch($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //チェックボックス
            if ($model->field["DELCHK"]["$schcnt"] == "on") {
                $check_del = "checked";
            } else {
                $check_del = "";
            }

            $objForm->ae( array("type"      => "checkbox",
                                "name"      => "DELCHK".$schcnt,
                                "value"     => "on",
                                "extrahtml" => $check_del ) );

            $Row["DELCHK"]   = $objForm->ge("DELCHK".$schcnt);
            $Row["DELID"] = "DEL".$schcnt;
            //学籍番号
            $model->setdata["SCHREGNO2"][$schcnt] = $row["SCHREGNO"];

            $Row["SCHREGNO2"] = $model->setdata["SCHREGNO2"][$schcnt];
            $Row["SCHID"] = "SCH".$schcnt;

            //hidden
            knjCreateHidden($objForm, "SCHREGNO2".$schcnt, $model->setdata["SCHREGNO2"][$schcnt]);

            //氏名（漢字）
            $model->setdata["NAME"][$schcnt] = $row["NAME_SHOW"];

            $Row["NAME"] = $model->setdata["NAME"][$schcnt];
            $Row["NAMEID"] = "NAME".$schcnt;

            //登録日付
            $model->setdata["T_TIME"][$schcnt] = $row["RECEIPT_TIME"];

            $Row["T_TIME"] = $model->setdata["T_TIME"][$schcnt];
            $Row["TIMEID"] = "TIME".$schcnt;

            //備考
            $model->setdata["REMARK2"][$schcnt] = $row["REMARK"];

            //リンク設定
            $subdata = "loadwindow('knjm390index.php?cmd=subform1&REMARKSUB={$row["REMARK"]}&SCHREGNOSUB={$row["SCHREGNO"]}&SCHREGNOSUBNAME={$row["NAME_SHOW"]}&SEQSUB={$model->field["SCHOOLING_SEQ"]}&STAFFSUB={$model->field["STAFF"]}&PERIODSUB={$model->field["PERIOD"]}&DATESUB={$model->Date}&CHAIRSUB={$model->field["CHAIR"]}&COURSESUB={$model->field["COURSE"]}',500,200,350,250)";

            $row["REMARK"] = View::alink("#", htmlspecialchars($row["REMARK"]),"onclick=\"$subdata\"");

            $Row["REMARK2"] = $row["REMARK"];
            $Row["REMAID"] = "REMA".$schcnt;

            $arg["data2"][] = $Row;

            $schcnt++;
        }
        $model->schcntall = $schcnt;
        $result->free();
        Query::dbCheckIn($db);
        $arg["TOTALCNT"] = $model->schcntall."件";

        //ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "登　録", $extra);

        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";	//2013/01/15 キーイベントタイムアウト処理復活
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_cancel", "終　了", $extra);

        $extra = "onclick=\"return btn_submit('chdel');\"";
        $arg["button"]["BTN_DEL"] = knjCreateBtn($objForm, "btn_del", "指定行削除", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm390Form1.html", $arg); 
    }
}
?>
