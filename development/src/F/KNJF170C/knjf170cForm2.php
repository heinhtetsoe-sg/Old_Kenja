<?php

require_once('for_php7.php');

class knjf170cForm2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf170cindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("changeFlg")) {
            //データ取得
            $date = ($model->date) ? str_replace("/", "-", $model->date) : CTRL_DATE;
            $query = knjf170cQuery::getRow($model, $date);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //欠席データ取得
            if ($model->Properties["useNurseoffAttend"] == "1") {
                $attend = array();
                $query = knjf170cQuery::getNurseoffAttendDat($model, $date);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $attend[$row["DI_CD"]][$row["GRADE"]] = $row["CNT"];
                }
                $result->free();
            }
        } else {
            $Row =& $model->field;
            if ($model->Properties["useNurseoffAttend"] == "1") {
                $attend =& $model->att_field;
            }
        }

        //デフォルト値
        $model->year = ($model->year) ? $model->year : CTRL_YEAR;

        //初期化
        if (VARS::get("changeFlg")) {
            $model->date = "";
        }
        //日付作成
        $model->date = ($model->date) ? $model->date : CTRL_DATE;
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", str_replace("-", "/", $model->date));

        if ($model->z010 == "miyagiken") {
            $arg["not_miyagiken"] = "";
            $extra = "";
            $arg["data"]["WEATHER_TEXT"] = knjCreateTextBox($objForm, $Row["WEATHER_TEXT"], "WEATHER_TEXT", 11, 5, $extra);
        } else {
            $arg["not_miyagiken"] = "1";
            //天気コンボ作成
            $query = knjf170cQuery::getNameMst($model);
            $extra = "onChange=\"textDisabled()\"";
            makeCmb($objForm, $arg, $db, $query, "WEATHER", $Row["WEATHER"], $extra, 1);

            //天気テキストエリア作成
            $extra = "";
            if ($Row["WEATHER"] != "5" && ($model->cmd == "edit" || $model->cmd == "clear")) {
                $extra = "disabled";
            }
            $arg["data"]["WEATHER_TEXT"] = knjCreateTextBox($objForm, $Row["WEATHER_TEXT"], "WEATHER_TEXT", 11, 5, $extra);
        }
        //色コンボ作成
        $query = knjf170cQuery::getConboNameMst('F152');
        $extra = "onChange=\"textDisabled()\"";
        makeCmb($objForm, $arg, $db, $query, "COLOR", $Row["COLOR"], $extra, 1);

        //濁りコンボ作成
        $query = knjf170cQuery::getConboNameMst('F153');
        $extra = "onChange=\"textDisabled()\"";
        makeCmb($objForm, $arg, $db, $query, "TURBIDITY", $Row["TURBIDITY"], $extra, 1);

        //臭いコンボ作成
        $query = knjf170cQuery::getConboNameMst('F154');
        $extra = "onChange=\"textDisabled()\"";
        makeCmb($objForm, $arg, $db, $query, "SMELL", $Row["SMELL"], $extra, 1);

        //味コンボ作成
        $query = knjf170cQuery::getConboNameMst('F155');
        $extra = "onChange=\"textDisabled()\"";
        makeCmb($objForm, $arg, $db, $query, "TASTE", $Row["TASTE"], $extra, 1);

        //AEDコンボ作成
        $query = knjf170cQuery::getConboNameMst('F156');
        $extra = "onChange=\"textDisabled()\"";
        makeCmb($objForm, $arg, $db, $query, "AED", $Row["AED"], $extra, 1);

        //気温テキストボックス
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["TEMPERATURE"] = knjCreateTextBox($objForm, $Row["TEMPERATURE"], "TEMPERATURE", 5, 5, $extra);

        //湿度テキストボックス
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toInteger_Humidity(this)\"";
        $arg["data"]["HUMIDITY"] = knjCreateTextBox($objForm, $Row["HUMIDITY"], "HUMIDITY", 5, 3, $extra);

        //検査時間(時)テキストボックス
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toInteger_CheckTime(this, 'hour')\"";
        $arg["data"]["CHECK_HOUR"] = knjCreateTextBox($objForm, $Row["CHECK_HOUR"], "CHECK_HOUR", 5, 2, $extra);

        //検査時間(分)テキストボックス
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toInteger_CheckTime(this, 'minute')\"";
        $arg["data"]["CHECK_MINUTE"] = knjCreateTextBox($objForm, $Row["CHECK_MINUTE"], "CHECK_MINUTE", 5, 2, $extra);

        //残留塩素テキストボックス
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toFloat_ResidualChlorine(this)\"";
        $arg["data"]["RESIDUAL_CHLORINE"] = knjCreateTextBox($objForm, $Row["RESIDUAL_CHLORINE"], "RESIDUAL_CHLORINE", 5, 4, $extra);

        //特記事項テキストボックス
        $extra = "STYLE=\"text-align: right\"";
        $arg["data"]["WATER_REMARK"] = knjCreateTextBox($objForm, $Row["WATER_REMARK"], "WATER_REMARK", 7, $model->nurseoff_tokkiziko_moji, $extra);

        //欠席状況表示
        if ($model->Properties["useNurseoffAttend"] == "1") {
            $arg["useNurseoffAttend"] = 1;

            //項目行
            $grade_cnt = 3;
            $grade_list = $sep = "";
            $header  = "<td width=\"110\" align=\"center\" colspan=\"2\"></td>";
            foreach ($model->grade as $gkey => $gval) {
                $header .= "<td width=\"80\" align=\"center\">".$gval."</td>";

                $grade_cnt++;

                $grade_list .= $sep.$gkey;
                $sep = ",";
            }
            $header .= "<td width=\"80\" align=\"center\">計</td>";

            $arg["header"]["GRADE"] = $header;
            $arg["header"]["COLSPAN"] = $grade_cnt;
            $arg["header"]["WIDTH"] = $grade_cnt * 80;
            knjCreateHidden($objForm, "GRADE_LIST", $grade_list);

            //欠席状況
            foreach ($model->di_cd as $dkey => $dval) {
                $setData = "";
                //出欠項目
                if ($dkey == '4') {
                    $setData .= "<td width=\"30\" class=\"no_search\" align=\"center\" rowspan=\"3\">欠<br>席</td>";
                }
                $colspan = (in_array($dkey, array('2','3'))) ? 2 : 1;
                $setData .= "<td width=\"80\" class=\"no_search\" align=\"center\" colspan=\"{$colspan}\">".$dval."</td>";

                $total = 0;
                foreach ($model->grade as $gkey => $gval) {
                    //欠席状況テキスト
                    $extra = "STYLE=\"text-align:right\" onblur=\"calc(this, '$dkey');\"";
                    $textbox = knjCreateTextBox($objForm, $attend[$dkey][$gkey], "CNT_".$dkey."_".$gkey, 3, 3, $extra);
                    $setData .= "<td width=\"80\" align=\"center\">".$textbox."</td>";

                    if ($attend[$dkey][$gkey] > 0) {
                        $total += (int)$attend[$dkey][$gkey];
                    }
                }
                //合計
                $id = 'total_'.$dkey;
                $setData .= "<td width=\"80\" align=\"center\" id='$id'>".$total."</td>";

                $arg["attend"][] = $setData;
            }
        }

        //行事テキストボックス
        $extra = "";
        $arg["data"]["EVENT"] = knjCreateTextArea($objForm, "EVENT", $model->nurseoff_gyouzi_gyou, ((int)$model->nurseoff_gyouzi_moji * 2 + 1), "soft", $extra, $Row["EVENT"]);

        //日誌テキストボックス
        $extra = "";
        $arg["data"]["DIARY"] = knjCreateTextArea($objForm, "DIARY", $model->nurseoff_diary_gyou, ((int)$model->nurseoff_diary_moji * 2 + 1), "soft", $extra, $Row["DIARY"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && !isset($model->warning)) {
            if ($model->cmd == "clear") {
                $arg["reload"]  = "";
            } else {
                $arg["reload"]  = "parent.left_frame.location.href='knjf170cindex.php?cmd=list';";
            }
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf170cForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    if (AUTHORITY < DEF_UPDATE_RESTRICT) {
        //追加ボタン
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", "disabled");
        //修正ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
        //削除ボタン
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "disabled");
    } else {
        //追加ボタン
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", " onclick=\"return btn_submit('add');\"");
        //修正ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");
        //削除ボタン
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");
    }
    //取消ボタン
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", $model->year);
}
