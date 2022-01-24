<?php

require_once('for_php7.php');


class knjm270mForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm270mindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["valWindowHeight"]  = $model->windowHeight - 380;
        $resizeFlg = $model->cmd == "cmdStart" ? true : false;
        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //入力方法
        $opt = array(1, 2);
        $model->field["INPUT_RADIO"] = ($model->field["INPUT_RADIO"] == "") ? "1" : $model->field["INPUT_RADIO"];
        $submit = "onClick=\"btn_submit('main')\"";
        $extra = array("id=\"INPUT_RADIO1\" {$submit}", "id=\"INPUT_RADIO2\" {$submit}");
        $radioArray = knjCreateRadio($objForm, "INPUT_RADIO", $model->field["INPUT_RADIO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["sel"][$key] = $val;

        //日付データ
        if ($model->Date == "") $model->Date = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATE"] = View::popUpCalendar($objForm  ,"DATE" ,str_replace("-","/",$model->Date),"reload=true");

        //チェック用hidden
        knjCreateHidden($objForm, "DEFOULTDATE", $model->Date);

        //添削者
        $opt_staf = array();
        $result = $db->query(knjm270mQuery::selectStaff($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_staf[] = array("label" => $row["STAFFNAME"],
                                "value" => $row["STAFFCD"]);
        }

        $result->free();

        if (!$model->field["STAFF"] && $model->User == 0) {
            $model->field["STAFF"] = $opt_staf[0]["value"];
        } else if(!$model->field["STAFF"] && $model->User == 1) {
            $model->field["STAFF"] = STAFFCD;
        }

        if (!$opt_staf[0]) {
            $arg["Closing"] = " closing_window('MSG300');";
        }
        $extra = "$disabled onChange=\"btn_submit('');\" ";
        $arg["sel"]["STAFF"] = knjCreateCombo($objForm, "STAFF", $model->field["STAFF"], $opt_staf, $extra, 1);

        //登録後クリア
        if ($model->cmd == 'addread'){
            $model->field["REPNO"] = '';
            $model->field["SCHREGNO"] = '';
            $model->field["HYOUKA"] = '';
        }

        if ($model->field["INPUT_RADIO"] == "1") {
            //レポート番号
            $extra = "onkeydown=\"keyfocs2(this)\";";
            $arg["sel"]["REPNO"] = knjCreateTextBox($objForm, $model->field["REPNO"], "REPNO", 18, 18, $extra);
            $arg["INPUT1"] = "1";
        } else {
            //学籍番号
            $extra = "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"keyfocs2(this)\";";
            $arg["sel"]["SCHREGNO"] = knjCreateTextBox($objForm, $model->field["SCHREGNO"], "SCHREGNO", 8, 8, $extra);

            //科目
            $query = knjm270mQuery::getSubClass($model);
            $extra = "onChange=\"btn_submit('main')\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

            //回数
            $query = knjm270mQuery::getStandardSeq($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $seqAll = $row["REP_SEQ_ALL"];
                $startSeq = $row["REP_START_SEQ"];
            }
            $result->free();
            $seqArray = array();
            $seqArray[] = array("label" => "", "value" => "");
            for ($i = $startSeq; $i < $startSeq + $seqAll; $i++) {
                $seqArray[] = array('label' => $i."回目",
                                    'value' => $i);
                if ($model->field["STANDARD_SEQ"] == $i) $value_flg = true;
            }
            $model->field["STANDARD_SEQ"] = ($model->field["STANDARD_SEQ"] && $value_flg) ? $model->field["STANDARD_SEQ"] : $seqArray[0]["value"];
            $extra = "onChange=\"btn_submit('main')\"";
            $arg["sel"]["STANDARD_SEQ"] = knjCreateCombo($objForm, "STANDARD_SEQ", $model->field["STANDARD_SEQ"], $seqArray, $extra, 1);

            $arg["INPUT2"] = "1";
        }

        //評価
        $extra = "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"checkkey()\";";
        $arg["sel"]["HYOUKA"] = knjCreateTextBox($objForm, $model->field["HYOUKA"], "HYOUKA", 8, 8, $extra);

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/

        //ソート表示文字作成
        $order[1] = "▲";
        $order[2] = "▼";
        $model->getSort = $model->getSort ? $model->getSort : "SRT_TIME";

        //リストヘッダーソート作成
        $model->sort["SRT_REPORT"] = $model->sort["SRT_REPORT"] ? $model->sort["SRT_REPORT"] : 1;
        $setOrder = $model->getSort == "SRT_REPORT" ? $order[$model->sort["SRT_REPORT"]] : "";
        $REPORT_SORT = "<a href=\"knjm270mindex.php?cmd=sort&sort=SRT_REPORT\" target=\"_self\" STYLE=\"color:white\">レポートNo.{$setOrder}</a>";
        $arg["REPORT_SORT"] = $REPORT_SORT;

        //リストヘッダーソート作成
        $model->sort["SRT_NAME"] = $model->sort["SRT_NAME"] ? $model->sort["SRT_NAME"] : 1;
        $setOrder = $model->getSort == "SRT_NAME" ? $order[$model->sort["SRT_NAME"]] : "";
        $NAME_SORT = "<a href=\"knjm270mindex.php?cmd=sort&sort=SRT_NAME\" target=\"_self\" STYLE=\"color:white\">氏名{$setOrder}</a>";
        $arg["NAME_SORT"] = $NAME_SORT;

        //リストヘッダーソート作成
        $model->sort["SRT_SUBCLASS"] = $model->sort["SRT_SUBCLASS"] ? $model->sort["SRT_SUBCLASS"] : 1;
        $setOrder = $model->getSort == "SRT_SUBCLASS" ? $order[$model->sort["SRT_SUBCLASS"]] : "";
        $SUBCLASS_SORT = "<a href=\"knjm270mindex.php?cmd=sort&sort=SRT_SUBCLASS\" target=\"_self\" STYLE=\"color:white\">科目名{$setOrder}</a>";
        $arg["SUBCLASS_SORT"] = $SUBCLASS_SORT;

        //リストヘッダーソート作成
        $model->sort["SRT_TIME"] = $model->sort["SRT_TIME"] ? $model->sort["SRT_TIME"] : 2;
        $setOrder = $model->getSort == "SRT_TIME" ? $order[$model->sort["SRT_TIME"]] : "";
        $TIME_SORT = "<a href=\"knjm270mindex.php?cmd=sort&sort=SRT_TIME\" target=\"_self\" STYLE=\"color:white\">登録時刻{$setOrder}</a>";
        $arg["TIME_SORT"] = $TIME_SORT;

        //抽出データ出力
        $schcnt = 0;
        $result = $db->query(knjm270mQuery::getSch($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //チェックボックス
            if ($model->field["DELCHK"]["$schcnt"] == "on") {
                $check_del = "checked";
            } else {
                $check_del = "";
            }
            $extra = $check_del;
            $Row["DELCHK"] = knjCreateCheckBox($objForm, "DELCHK".$schcnt, "on", $extra);
            $Row["DELID"] = "DEL".$schcnt;

            //レポート番号
            $model->setdata["REPORTNO"][$schcnt] = $row["SCHREGNO"].substr(CTRL_YEAR,3,1).$row["CURRICULUM_CD"].$row["SUBCLASSCD"].sprintf("%02d",$row["STANDARD_SEQ"]);

            $Row["REPORTNO"] = $model->setdata["REPORTNO"][$schcnt];
            $Row["REPID"] = "REP".$schcnt;

            knjCreateHidden($objForm, "REPORTNO".$schcnt, $model->setdata["REPORTNO"][$schcnt]);

            //学籍番号
            $model->setdata["SCHREGNO2"][$schcnt] = $row["SCHREGNO"];

            $Row["SCHREGNO2"] = $model->setdata["SCHREGNO2"][$schcnt];
            $Row["SCHID"] = "SCH".$schcnt;

            knjCreateHidden($objForm, "SCHREGNO2".$schcnt, $model->setdata["SCHREGNO2"][$schcnt]);

            //提出受付
            $model->setdata["RECEIPT_DATE"][$schcnt] = $row["RECEIPT_DATE"];
            //氏名（漢字）
            $model->setdata["NAME"][$schcnt] = $row["NAME"];
            
            $Row["NAME"] = $model->setdata["NAME"][$schcnt];
            $Row["NAMEID"] = "NAME".$schcnt;

            //科目名
            $model->setdata["SUBCLASSNAME"][$schcnt] = $row["SUBCLASSNAME"];

            $Row["SUBCLASSNAME"] = $model->setdata["SUBCLASSNAME"][$schcnt];
            $Row["SCLID"] = "SCL".$schcnt;

            //回数
            $model->setdata["STANDARD_SEQ"][$schcnt] = $row["STANDARD_SEQ"];

            $Row["STANDARD_SEQ"] = "第".$model->setdata["STANDARD_SEQ"][$schcnt]."回";
            $Row["STQID"] = "RSQ".$schcnt;

            //評価
            $model->setdata["GRAD_VALUE"][$schcnt] = $row["GRAD_VALUE"];

            //リンク設定
            $linkSubclass = $row["CLASSCD"].$row["SCHOOL_KIND"].$row["CURRICULUM_CD"].$row["SUBCLASSCD"];
            $subdata = "loadwindow('knjm270mindex.php?cmd=subform1&GRAD_VALUESUB={$row["GRAD_VALUE"]}&SCHREGNOSUB={$row["SCHREGNO"]}&SUBCLASSSUB={$linkSubclass}&STSEQSUB={$row["STANDARD_SEQ"]}&RSEQSUB={$row["REPRESENT_SEQ"]}&DATESUB={$model->Date}&CHAIRSUB={$row["CHAIRCD"]}&RECDAYSUB={$row["RECEIPT_DATE"]}',500,200,350,300)";

            $row["GRAD_VALUESUB"] = View::alink("#", htmlspecialchars($row["NAME1"]),"onclick=\"$subdata\"");

            $Row["GRAD_VALUESUB"] = $row["GRAD_VALUESUB"];
            $Row["GRDVID"] = "GRDV".$schcnt;

            //再提出数
            $model->setdata["REPRESENT_SEQ"][$schcnt] = $row["REPRESENT_SEQ"];

            if ($row["REPRESENT_SEQ"] != 0) {
                $Row["REPRESENT_SEQ"] = "再".$model->setdata["REPRESENT_SEQ"][$schcnt];
                $Row["RSQID"] = "RSQ".$schcnt;
            } else {
                $Row["REPRESENT_SEQ"] = "";
                $Row["RSQID"] = "RSQ".$schcnt;
            }
            //登録日付
            $model->setdata["T_TIME"][$schcnt] = $row["GRAD_TIME"];

            $Row["T_TIME"] = $model->setdata["T_TIME"][$schcnt];
            $Row["TIMEID"] = "TIME".$schcnt;

            $arg["data2"][] = $Row;

            $schcnt++;
        }
        $model->schcntall = $schcnt;
        $result->free();

        $arg["TOTALCNT"] = $model->schcntall."件";

        //ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "登　録", $extra);

        //$extra = "onclick=\"closeWin();\"";
        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";	//2012/01/10 キーイベントタイムアウト処理復活
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終　了", $extra);

        $extra = "onclick=\"return btn_submit('chdel');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "評価返送取消", $extra);
        
        //HIDDEN
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm270mForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["sel"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
