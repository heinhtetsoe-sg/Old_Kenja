<?php

require_once('for_php7.php');

class knjm390wForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm390windex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //スクーリング種別
        $query = knjm390wQuery::selectName("M001");
        $extra = "onChange=\"btn_submit('chg_course');\"";
        makeCmb($objForm, $arg, $db, $query, "COURSE", $model->field["COURSE"], $extra, 1);

        //日付データ
        if ($model->Date == "") $model->Date = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATE"] = View::popUpCalendar($objForm  ,"DATE" ,str_replace("-","/",$model->Date),"reload=true");

        //チェック用hidden
        knjCreateHidden($objForm, "YEAR", $model->Year);
        knjCreateHidden($objForm, "DEFOULTDATE", $model->Date);
        knjCreateHidden($objForm, "DEFOULTSEME", $model->semester);
        knjCreateHidden($objForm, "GAKKISU", $model->control["学期数"]);
        knjCreateHidden($objForm, "SEME1S", $model->control["学期開始日付"]["1"]);
        knjCreateHidden($objForm, "SEME1E", $model->control["学期終了日付"]["1"]);
        knjCreateHidden($objForm, "SEME2S", $model->control["学期開始日付"]["2"]);
        knjCreateHidden($objForm, "SEME2E", $model->control["学期終了日付"]["2"]);
        if ($model->control["学期数"] == 3) {
            knjCreateHidden($objForm, "SEME3S", $model->control["学期開始日付"]["3"]);
            knjCreateHidden($objForm, "SEME3E", $model->control["学期終了日付"]["3"]);
        }

        //講座コンボ
        $opt_chair = array();
        $result = $db->query(knjm390wQuery::getAuth($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        }
        if (!$model->field["CHAIR"]) {
            $model->field["CHAIR"]        = $opt_chair[0]["value"];
        }
        if (!$opt_chair[0]) {
            $arg["Closing"] = " closing_window('MSG300');";
        }

        $objForm->ae( array("type"    => "select",
                            "name"    => "CHAIR",
                            "size"    => "1",
                            "value"   => $model->field["CHAIR"],
                            "extrahtml" => "onChange=\"btn_submit('');\" ",
                            "options" => $opt_chair));

        $arg["sel"]["CHAIR"] = $objForm->ge("CHAIR");

        $result->free();

        //校時
        $disabled = ($model->field["COURSE"] == 2 || $model->field["COURSE"] == 4 || $model->field["COURSE"] == 6) ? "disabled" : "";
        $query = knjm390wQuery::selectName("B001");
        $extra = "$disabled onChange=\"btn_submit('');\" ";
        makeCmb($objForm, $arg, $db, $query, "PERIOD", $model->field["PERIOD"], $extra, 1);

        //担当者
        $disabled = ($model->field["COURSE"] == 2 || $model->field["COURSE"] == 4 || $model->field["COURSE"] == 6) ? "disabled" : "";
        $opt_staf = array();
        $result = $db->query(knjm390wQuery::selectStaff($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_staf[] = array("label" => $row["STAFFNAME"],
                                "value" => $row["STAFFCD"]);
        }

        if (!$model->field["STAFF"] && $model->User == 0) {
            $model->field["STAFF"] = $opt_staf[0]["value"];
        } else if (!$model->field["STAFF"] && $model->User == 1) {
            $model->field["STAFF"] = STAFFCD;
        }
        $objForm->ae( array("type"    => "select",
                            "name"    => "STAFF",
                            "size"    => "1",
                            "value"   => $model->field["STAFF"],
                            "extrahtml" => "$disabled onChange=\"btn_submit('change');\" ",
                            "options" => $opt_staf));

        $arg["sel"]["STAFF"] = $objForm->ge("STAFF");

        $result->free();

        //単位時間
        if ($model->cmd == 'chg_course') $model->field["CREDIT_TIME"] = "";
        $opt = array();
        $max_time = $db->getOne(knjm390wQuery::getCreditTime($model));
        if ($max_time && $model->field["COURSE"] != "1") {
            for ($i = 1; $i <= $max_time; $i++) {
                $opt[] = array("label" => $i.'時間', "value" => $i);
            }
        }
        $extra = ($model->field["COURSE"] == "1") ? "disabled style=\"width:70px;background-color:darkgray;\"" : "";
        $arg["sel"]["CREDIT_TIME"] = knjCreateCombo($objForm, "CREDIT_TIME", $model->field["CREDIT_TIME"], $opt, $extra, 1);

        //備考
        $extra = "style=\"height:35px;\"";
        $arg["sel"]["REMARK"] = KnjCreateTextArea($objForm, "REMARK", 2, 20, "soft", $extra, $model->field["REMARK"]);

        //学籍番号
        if ($model->cmd == 'addread' && !$model->field["sonotaConfFlg"]) $model->field["SCHREGNO"] = '';
        $extra = "style=\"ime-mode: disabled;\" onblur=\"this.value=toInteger(this.value)\"onkeydown=\"checkkey()\";";
        $arg["sel"]["SCHREGNO"] = knjCreateTextBox($objForm, $model->field["SCHREGNO"], "SCHREGNO", 8, 8, $extra);

        // その他の更新確認ダイアログ
        if ($model->field["sonotaConfFlg"]) {
            $model->field["sonotaConfFlg"] = '';
            $arg["confirmSonota"] = " confirmSonota('add');";
        }

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/
        //抽出データ出力
        $schcnt = 0;
        //開講しない科目、講座情報チェック
        $model->Attendchaircd = "";
        $model->not_chairdat = $db->getOne(knjm390wQuery::getAttendChairDataCheck($model));
        //開講しない科目、講座情報取得
        if ($model->not_chairdat > 0) {
            $query = knjm390wQuery::getAttendChairData($model, "");
            $result = $db->query($query);
            $i = 0;
            while ($rowcheck = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($i == 0) {
                    $model->Attendchaircd  = $rowcheck["CHAIRCD"];
                } else {
                    $model->Attendchaircd .= ','.$rowcheck["CHAIRCD"];
                }
                $i++;
            }
            //開講する科目講座をセット
            $model->Attendchaircd .= ','.$model->field["CHAIR"];
        }
        $result = $db->query(knjm390wQuery::getSch($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //チェックボックス
            $extra = ($model->field["DELCHK"]["$schcnt"] == "on") ? "checked" : "";
            $Row["DELCHK"] = knjCreateCheckBox($objForm, "DELCHK".$schcnt, "on", $extra, "");
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

            //単位時間
            $model->setdata["C_TIME"][$schcnt] = ($row["CREDIT_TIME"]) ? sprintf("%d", $row["CREDIT_TIME"]).'時間' : "";

            $Row["C_TIME"] = $model->setdata["C_TIME"][$schcnt];
            $Row["CREDITID"] = "CREDIT".$schcnt;

            //登録日付
            $model->setdata["T_TIME"][$schcnt] = $row["RECEIPT_TIME"];

            $Row["T_TIME"] = $model->setdata["T_TIME"][$schcnt];
            $Row["TIMEID"] = "TIME".$schcnt;

            //hidden
            knjCreateHidden($objForm, "T_TIME".$schcnt, $model->setdata["T_TIME"][$schcnt]);

            //備考
            $model->setdata["REMARK2"][$schcnt] = $row["REMARK"];

            //リンク設定
            $subdata = "loadwindow('knjm390windex.php?cmd=subform1&REMARKSUB={$row["REMARK"]}&SCHREGNOSUB={$row["SCHREGNO"]}&SCHREGNOSUBNAME={$row["NAME_SHOW"]}&STAFFSUB={$model->field["STAFF"]}&PERIODSUB={$model->field["PERIOD"]}&DATESUB={$model->Date}&CHAIRSUB={$model->field["CHAIR"]}&COURSESUB={$model->field["COURSE"]}&RECEIPTTIMESUB={$row["RECEIPT_TIME"]}',500,200,350,250)";

            $row["REMARK"] = View::alink("#", htmlspecialchars($row["REMARK"]),"onclick=\"$subdata\"");

            $Row["REMARK2"] = $row["REMARK"];
            $Row["REMAID"] = "REMA".$schcnt;

            $arg["data2"][] = $Row;

            $schcnt++;
        }
        $model->schcntall = $schcnt;
        $result->free();

        $arg["TOTALCNT"] = $model->schcntall."件";

        //ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "登 録", $extra);

        //$extra = "onclick=\"closeWin();\"";
        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";	//2013/01/15 キーイベントタイムアウト処理復活
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        $extra = "onclick=\"return btn_submit('chdel');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "指定行削除", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "sonotaNotChk", $model->field["sonotaNotChk"]);
        knjCreateHidden($objForm, "sonotaConfFlg", $model->field["sonotaConfFlg"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm390wForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["sel"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
