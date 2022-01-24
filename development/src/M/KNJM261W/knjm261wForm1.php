<?php

require_once('for_php7.php');


class knjm261wForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm261windex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjm261wQuery::useCombinedDat();
        $model->useCombinedDat = $db->getOne($query);

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

        //登録後クリア
        if ($model->cmd == 'addread') {
            $model->field["REPNO"] = '';
            $model->field["SCHREGNO"] = '';
        }

        if ($model->field["INPUT_RADIO"] == "1") {
            //レポート番号
            $extra = "onkeydown=\"keyfocs1()\";";
            $arg["sel"]["REPNO"] = knjCreateTextBox($objForm, $model->field["REPNO"], "REPNO", 10, 10, $extra);

            //学籍番号
            $extra = "onblur=\"this.value=toInteger(this.value)\"; onkeydown=\"checkkey()\";";
            $arg["sel"]["SCHREGNO"] = knjCreateTextBox($objForm, $model->field["SCHREGNO"], "SCHREGNO", 8, 8, $extra);

            $arg["INPUT1"] = "1";
        } else {
            //学籍番号
            $extra = "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"checkkey()\";";
            $arg["sel"]["SCHREGNO"] = knjCreateTextBox($objForm, $model->field["SCHREGNO"], "SCHREGNO", 8, 8, $extra);

            //科目
            $query = knjm261wQuery::getSubClass($model);
            $extra = "onChange=\"btn_submit('main')\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

            //回数
            $query = knjm261wQuery::getStandardSeq($model);
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

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/
        //抽出データ出力
        $schcnt = 0;
        $result = $db->query(knjm261wQuery::getSch($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //チェックボックス
            if($model->field["DELCHK"]["$schcnt"] == "on") {
                $check_del = "checked";
            } else {
                $check_del = "";
            }
            $extra = $check_del;
            $Row["DELCHK"] = knjCreateCheckBox($objForm, "DELCHK".$schcnt, "on", $extra);
            $Row["DELID"] = "DEL".$schcnt;

            //レポート番号
            $model->setdata["REPORTNO"][$schcnt] = $row["SCHREGNO"].substr(CTRL_YEAR,3,1).$row["CURRICULUM_CD"].$row["SUBCLASSCD"].sprintf("%02d",$row["STANDARD_SEQ"]);

            if ($model->useCombinedDat == "1") {
                $query = knjm261wQuery::getCombSubclassSaki($model, $row);
                $model->combSubclass = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (is_array($model->combSubclass)) {
                    $Row["REPORTNO"] = substr(CTRL_YEAR,3,1).$model->combSubclass["COMBINED_CURRICULUM_CD"].$model->combSubclass["COMBINED_SUBCLASSCD"].sprintf("%02d",$row["STANDARD_SEQ"]);
                    $row["SUBCLASSNAME"] = $model->combSubclass["SUBCLASSNAME"];
                } else {
                    $Row["REPORTNO"] = substr(CTRL_YEAR,3,1).$row["CURRICULUM_CD"].$row["SUBCLASSCD"].sprintf("%02d",$row["STANDARD_SEQ"]);
                }
            } else {
                $Row["REPORTNO"] = substr(CTRL_YEAR,3,1).$row["CURRICULUM_CD"].$row["SUBCLASSCD"].sprintf("%02d",$row["STANDARD_SEQ"]);
            }
            $Row["SCHREGNO"] = $row["SCHREGNO"];
            $Row["REPID"] = "REP".$schcnt;

            knjCreateHidden($objForm, "REPORTNO".$schcnt, $model->setdata["REPORTNO"][$schcnt].$row["REPRESENT_SEQ"]);

            //学籍番号
            $model->setdata["SCHREGNO2"][$schcnt] = $row["SCHREGNO"];

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
            $model->setdata["T_TIME"][$schcnt] = $row["RECEIPT_TIME"];

            $Row["T_TIME"] = $model->setdata["T_TIME"][$schcnt];
            $Row["TIMEID"] = "TIME".$schcnt;

            $arg["data2"][] = $Row;

            $schcnt++;
        }
        $model->schcntall = $schcnt;
        $result->free();
        Query::dbCheckIn($db);
        $arg["TOTALCNT"] = $model->schcntall."件";

        //ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "登　録", $extra);

        //$extra = "onclick=\"closeWin();\"";
        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";	//2013/01/10 キーイベントタイムアウト処理復活
        $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "終　了", $extra);

        $extra = "onclick=\"return btn_submit('chdel');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "指定行削除", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm261wForm1.html", $arg); 
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
