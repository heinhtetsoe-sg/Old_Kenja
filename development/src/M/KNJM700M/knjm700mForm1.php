<?php

require_once('for_php7.php');

class knjm700mForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm700mindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //チェック用hidden
        knjCreateHidden($objForm, "DEFOULTDATE", $model->field["ATTENDDATE"]);

        //教科コンボ
        $query = knjm700mQuery::getClassMst($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["CLASSCD"], "CLASSCD", $extra, 1, "BLANK");

        //科目コンボ
        $query = knjm700mQuery::getSubClassMst($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //講座コンボ
        $query = knjm700mQuery::getChairDat($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        $blank = $model->field["CLASSCD"] == "94" ? "" : "BLANK";
        makeCmb($objForm, $arg, $db, $query, $model->field["CHAIRCD"], "CHAIRCD", $extra, 1, $blank);

        //校時コンボ
        if (substr($model->field["CLASSCD"], 0, 2) == '93' && $model->cmd == 'change') $model->field["PERIODF"] = '7';
        if (substr($model->field["CLASSCD"], 0, 2) == '94' && $model->cmd == 'change') $model->field["PERIODF"] = '1';
        $query = knjm700mQuery::getNameMst("B001");
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PERIODF"], "PERIODF", $extra, 1, "BLANK");

        //単位時間コンボ
        if (substr($model->field["CLASSCD"], 0, 2) == '93' && $model->cmd == 'change') $model->field["CREDIT_TIME"] = '2';
        if (substr($model->field["CLASSCD"], 0, 2) == '94' && $model->cmd == 'change') $model->field["CREDIT_TIME"] = '2';
        $query = knjm700mQuery::getNameMst("M010");
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["CREDIT_TIME"], "CREDIT_TIME", $extra, 1, "BLANK");

        //日付データ
        $model->field["ATTENDDATE"] = $model->field["ATTENDDATE"] ? $model->field["ATTENDDATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["sel"]["ATTENDDATE"] = View::popUpCalendar($objForm  ,"ATTENDDATE" ,str_replace("-", "/", $model->field["ATTENDDATE"]));

        //備考
        $extra = "onkeydown=\"keyfocs1()\";";
        $arg["sel"]["REMARK"] = knjCreateTextBox($objForm, $model->field["REMARK"], "REMARK", 60, 30, $extra);

        //学籍番号
        if ($model->cmd == 'addread') {
            $model->field["SCHREGNO"] = '';
        }
        $extra = "style=\"ime-mode: disabled;\" onblur=\"this.value=toInteger(this.value)\"onkeydown=\"checkkey()\";";
        $arg["sel"]["SCHREGNO"] = knjCreateTextBox($objForm, $model->field["SCHREGNO"], "SCHREGNO", 8, 8, $extra);

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/
        $query = knjm700mQuery::getSemesterMst($model);
        $setSem = $db->getOne($query);
        $setSem = $setSem ? $setSem : CTRL_SEMESTER;

        //抽出データ出力
        $schcnt = 0;
        $query = knjm700mQuery::getSch($model, $setSem);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $setRow = $row;

            //チェックボックス
            if ($model->field["DELCHK"][$schcnt] == "on") {
                $check_del = "checked";
            } else {
                $check_del = "";
            }
            $extra = $check_del;
            $setRow["DELCHK"] = knjCreateCheckBox($objForm, "DELCHK".$schcnt, "on", $extra);

            //削除時に使用するテーブルのキー
            $delKey = $row["YEAR"].":".$row["SEMESTER"].":".$row["SCHREGNO"].":".$row["SUBCLASSCD"].":".$row["ATTENDDATE"].":".$row["PERIODF"];
            knjCreateHidden($objForm, "DEL_KEY".$schcnt, $delKey);

            $arg["data2"][] = $setRow;

            $schcnt++;
        }
        $model->schcntall = $schcnt;
        $result->free();
        $arg["TOTALCNT"] = $model->schcntall."件";

        //ボタン
        $extra = "onClick=\"return btn_submit('add');\"";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "登　録", $extra);

        //$extra = "onClick=\"closeWin();\"";
        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";	//2013/01/10 キーイベントタイムアウト処理復活
        $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "終　了", $extra);

        $extra = "onClick=\"return btn_submit('chdel');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "指定行削除", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjm700mForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
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
    $value = (strlen($value) && $value_flg) ? $value : $opt[0]["value"];
    $arg["sel"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
