<?php

require_once('for_php7.php');

class knjf170aForm2 {

    function main(&$model) {

        //権限チェック（宮城さん仕様）
        if (AUTHORITY == DEF_REFERABLE) {
           $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf170aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //デフォルト値
        if (!$model->year) $model->year = CTRL_YEAR;
        if (!$model->campus_div) {
            $model->campus_div = $db->getOne(knjf170aQuery::getCampusDiv($model, $model->year));
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            //データ取得
            $date = ($model->date) ? str_replace("/", "-", $model->date) : CTRL_DATE;
            $query = knjf170aQuery::getRow($model, $date);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //欠席データ取得
            if ($model->Properties["useNurseoffAttend"] == "1") {
                $attend = array();
                $query = knjf170aQuery::getNurseoffAttendCampusDat($model, $date);
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

        //日付作成
        $model->date = ($model->date) ? $model->date : CTRL_DATE;
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", str_replace("-","/",$model->date));

        //天気コンボ
        $query = knjf170aQuery::getNameMst($model);
        $extra = "onChange=\"textDisabled()\"";
        makeCmb($objForm, $arg, $db, $query, "WEATHER", $Row["WEATHER"], $extra, 1);

        //天気テキストエリア
        if ($Row["WEATHER"] != "5" && $model->cmd == "edit") {
            $extra = "disabled";
        }
        $arg["data"]["WEATHER_TEXT"] = knjCreateTextBox($objForm, $Row["WEATHER_TEXT"], "WEATHER_TEXT", 11, 5, $extra);

        //気温テキストボックス
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["TEMPERATURE"] = knjCreateTextBox($objForm, $Row["TEMPERATURE"], "TEMPERATURE", 5, 5, $extra);

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
                if ($dkey == '4') $setData .= "<td width=\"30\" class=\"no_search\" align=\"center\" rowspan=\"3\">欠<br>席</td>";
                $colspan = (in_array($dkey, array('2','3'))) ? 2 : 1;
                $setData .= "<td width=\"80\" class=\"no_search\" align=\"center\" colspan=\"{$colspan}\">".$dval."</td>";

                $total = 0;
                foreach ($model->grade as $gkey => $gval) {
                    //欠席状況テキスト
                    $extra = "STYLE=\"text-align:right\" onblur=\"calc(this, '$dkey');\"";
                    $textbox = knjCreateTextBox($objForm, $attend[$dkey][$gkey], "CNT_".$dkey."_".$gkey, 3, 3, $extra);
                    $setData .= "<td width=\"80\" align=\"center\">".$textbox."</td>";

                    if ($attend[$dkey][$gkey] > 0) $total += $attend[$dkey][$gkey];
                }
                //合計
                $id = 'total_'.$dkey;
                $setData .= "<td width=\"80\" align=\"center\" id='$id'>".$total."</td>";

                $arg["attend"][] = $setData;
            }
        }

        //行事テキストボックス
        $extra = "";
        $arg["data"]["EVENT"] = knjCreateTextArea($objForm, "EVENT", 6, 25, "soft", $extra, $Row["EVENT"]);

        //日誌テキストボックス
        $extra = "";
        $arg["data"]["DIARY"] = knjCreateTextArea($objForm, "DIARY", $model->nurseoff_diary_gyou, ($model->nurseoff_diary_moji * 2 + 1), "soft", $extra, $Row["DIARY"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && !isset($model->warning)) {
            $arg["reload"] = "parent.left_frame.location.href='knjf170aindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf170aForm2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
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
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "CAMPUS_DIV", $model->campus_div);
}
?>
