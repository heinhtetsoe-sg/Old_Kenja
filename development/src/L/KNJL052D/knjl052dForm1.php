<?php

require_once('for_php7.php');

class knjl052dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボボックス
        $query = knjl052dQuery::selectYearQuery();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1, "");

        //科目コンボ
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl052dQuery::getNameMst($model, "L009");
        makeCmb($objForm, $arg, $db, $query, "SUBCLASS_CD", $model->field["SUBCLASS_CD"], $extra, 1, "");

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //更新ALLチェック
        $chk = ($model->judgediv == "on") ? " checked" : "";
        $extra = "onclick=\"chkDataALL(this);\"";
        $arg["TOP"]["QUEST_FLAG_ALL"] = knjCreateCheckBox($objForm, "QUEST_FLAG_ALL", "on", $extra.$chk);

        //データ取得
        $arrQesData = array();
        $result = $db->query(knjl052dQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $arrQesData[$row["QUESTION_ORDER"]] = $row;
        }

        //データセット
        $setLargeQ = $setQ = 1;
        for ($i = 1; $i <= 100; $i++) {
            if (isset($model->warning)) {
                $arrQesData[$i]["QUEST_FLAG"] = $model->field["QUEST_FLAG-".$i];
                $arrQesData[$i]["PATTERN_CD"] = $model->field["PATTERN_CD-".$i];
                $arrQesData[$i]["ANSWER1"]    = $model->field["ANSWER1-".$i];
                $arrQesData[$i]["POINT1"]     = $model->field["POINT1-".$i];
                $arrQesData[$i]["ANSWER2"]    = $model->field["ANSWER2-".$i];
                $arrQesData[$i]["POINT2"]     = $model->field["POINT2-".$i];
                $arrQesData[$i]["ANSWER3"]    = $model->field["ANSWER3-".$i];
                $arrQesData[$i]["POINT3"]     = $model->field["POINT3-".$i];
            }

            //設問フラグcheckbox
            $checked = ($arrQesData[$i]["QUEST_FLAG"] == "1") ? " checked": "";
            $extra = "id=\"QUEST_FLAG-{$i}\" onclick=\"chkUnDisabled(this);\"".$checked;
            $setParts["QUEST_FLAG"] = knjCreateCheckBox($objForm, "QUEST_FLAG-".$i, "1", $extra);

            //大設問番号-設問番号
            if ($setQ > 15) {
                $setQ = 1;
                $setLargeQ++;
            }
            if ($i > 60) {
                if ($setQ > 10) {
                    $setQ = 1;
                    $setLargeQ++;
                }
            }
            $setParts["LARGE_QUESTION"] = $setLargeQ;
            $setParts["QUESTION"] = $setQ;

            //順位
            $setParts["QUESTION_ORDER"] = $i;

            //テキストボックス使用制限
            $textDisabled = ($arrQesData[$i]["QUEST_FLAG"] == "1") ? "": " disabled";

            //パターンコード
            $extra = "style=\"text-align:right\" onblur=\"checkVal(this, '1');\" onkeydown=\"keyChangeEntToTab(this)\"".$textDisabled;
            $setParts["PATTERN_CD"] = knjCreateTextBox($objForm, $arrQesData[$i]["PATTERN_CD"], "PATTERN_CD-".$i, 3, 1, $extra);

            //正解１番号
            $extra = "style=\"text-align:right\" onblur=\"checkVal(this, '2');\" onkeydown=\"keyChangeEntToTab(this)\"".$textDisabled;
            $setParts["ANSWER1"] = knjCreateTextBox($objForm, $arrQesData[$i]["ANSWER1"], "ANSWER1-".$i, 3, 2, $extra);
            //正解１配点
            $extra = "style=\"text-align:right\" onblur=\"checkVal(this, '3');\" onkeydown=\"keyChangeEntToTab(this)\"".$textDisabled;
            $setParts["POINT1"] = knjCreateTextBox($objForm, $arrQesData[$i]["POINT1"], "POINT1-".$i, 3, 3, $extra);

            //正解２番号
            $extra = "style=\"text-align:right\" onblur=\"checkVal(this, '2');\" onkeydown=\"keyChangeEntToTab(this)\"".$textDisabled;
            $setParts["ANSWER2"] = knjCreateTextBox($objForm, $arrQesData[$i]["ANSWER2"], "ANSWER2-".$i, 3, 2, $extra);
            //正解２配点
            $extra = "style=\"text-align:right\" onblur=\"checkVal(this, '3');\" onkeydown=\"keyChangeEntToTab(this)\"".$textDisabled;
            $setParts["POINT2"] = knjCreateTextBox($objForm, $arrQesData[$i]["POINT2"], "POINT2-".$i, 3, 3, $extra);

            //正解３番号
            $extra = "style=\"text-align:right\" onblur=\"checkVal(this, '2');\" onkeydown=\"keyChangeEntToTab(this)\"".$textDisabled;
            $setParts["ANSWER3"] = knjCreateTextBox($objForm, $arrQesData[$i]["ANSWER3"], "ANSWER3-".$i, 3, 2, $extra);
            //正解３配点
            $extra = "style=\"text-align:right\" onblur=\"checkVal(this, '3');\" onkeydown=\"keyChangeEntToTab(this)\"".$textDisabled;
            $setParts["POINT3"] = knjCreateTextBox($objForm, $arrQesData[$i]["POINT3"], "POINT3-".$i, 3, 3, $extra);

            $setQ++;

            $arg["data"][] = $setParts;
        }

        //ボタン作成
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl052dindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl052dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "TEST_DATE") {
            $row["LABEL"] = str_replace('-', '/', $row["LABEL"]);
        }
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    if ($name == "YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    }
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
