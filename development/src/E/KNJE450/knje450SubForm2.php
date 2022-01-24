<?php

require_once('for_php7.php');

class knje450SubForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knje450index.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //カレンダー呼び出し
        $my = new mycalendar();

        //生徒情報
        $hr_name = $db->getOne(knje450Query::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //作成年月日コンボ
        $opt = array();
        $opt[] = array('label' => "(( 新規 ))", 'value' => "");
        $value_flg = false;
        $query = knje450Query::getWritingDateList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => str_replace("-", "/", $row["WRITING_DATE"]),
                           'value' => $row["WRITING_DATE"]);
            if ($model->writing_date == $row["WRITING_DATE"]) $value_flg = true;
        }
        $result->free();

        if (!($model->writing_date && $value_flg)) {
            $model->writing_date = $opt[0]["value"];
            if (!isset($model->warning)) unset($model->field);
        }
        $extra = "onChange=\"return btn_submit('subform2');\"";
        $arg["data"]["WRITING_DATE"] = knjCreateCombo($objForm, "WRITING_DATE", $model->writing_date, $opt, $extra, 1);

        //作成日
        $arg["data"]["WRT_DATE"] = View::popUpCalendar($objForm, "WRT_DATE", $model->field["WRT_DATE"]);

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning) && $model->writing_date != "") {
            $Row = $db->getRow(knje450Query::getSubQuery($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /**************/
        /*  医療連携  */
        /**************/
        //障害名
        $arg["data"]["HANDICAP"] = knjCreateTextBox($objForm, $Row["HANDICAP"], "HANDICAP", 100, 100, "");

        //障害名マスタ参照ボタン
        $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE450/knje450index.php?cmd=reference1&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 500)\"";
        $arg["button"]["btn_ref"] = knjCreateBtn($objForm, "btn_ref", "障害名マスタ参照", $extra);

        //診療時期
        $arg["data"]["DIAGNOSIS_DATE"] = str_replace("\n", "", $my->MyMonthWin2($objForm, "DIAGNOSIS_DATE", $Row["DIAGNOSIS_DATE"]));

        //機関コンボ
        $query = knje450Query::getCenter("MEDICAL_CENTER_MST");
        makeCmb($objForm, $arg, $db, $query, "INSTITUTES_CD", $Row["INSTITUTES_CD"], "", 1, "blank");

        //主治医
        $arg["data"]["ATTENDING_DOCTOR"] = knjCreateTextBox($objForm, $Row["ATTENDING_DOCTOR"], "ATTENDING_DOCTOR", 20, 20, "");

        //備考
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 50, 50, "");

        //服薬ラジオボタン(1:有 2:無)
        $opt = array(1, 2);
        if ($Row["MEDICINE_FLG"] == "") $Row["MEDICINE_FLG"] = "1";
        $extra = array("id=\"MEDICINE_FLG1\"", "id=\"MEDICINE_FLG2\"");
        $radioArray = knjCreateRadio($objForm, "MEDICINE_FLG", $Row["MEDICINE_FLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //薬剤名
        $arg["data"]["MEDICINE_NAME"] = knjCreateTextBox($objForm, $Row["MEDICINE_NAME"], "MEDICINE_NAME", 50, 50, "");

        /******************/
        /*  諸検査の結果  */
        /******************/
        for ($cd=1 ; $cd <= 2; $cd++) {
            //実施年月
            $arg["data"]["EXAMINATION_DATE_".$cd] = str_replace("\n", "", $my->MyMonthWin2($objForm, "EXAMINATION_DATE_".$cd, $Row["EXAMINATION_DATE_".$cd]));

            //機関コンボ
            $query = knje450Query::getCenter("CHECK_CENTER_MST");
            makeCmb($objForm, $arg, $db, $query, "EXAM_INST_CD_".$cd, $Row["EXAM_INST_CD_".$cd], "", 1, "blank");

            //検査者
            $arg["data"]["TESTER_NAME_".$cd] = knjCreateTextBox($objForm, $Row["TESTER_NAME_".$cd], "TESTER_NAME_".$cd, 20, 20, "");

            //諸検査
            $max = ($cd == "1") ? 3 : 7;
            for ($i=1 ; $i <= $max; $i++) {
                $name = "REMARK".$i."_".$cd;
                $arg["data"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 3, 3, "");
            }

            //解釈・特記事項等
            $name = "OTHER_TEXT_".$cd;
            $height = 4 * 13.5 + (4 - 1) * 3 + 5;
            $arg["data"][$name] = KnjCreateTextArea($objForm, $name, 4, (26 * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row[$name]);
            $arg["data"][$name."_COMMENT"] = "(全角26文字X4行まで)";
        }

        /************/
        /*  教育歴  */
        /************/
        $setVal = array();
        for ($i=1 ; $i <= 2; $i++) {
            $sk = ($i == "1") ? "P" : "J";

            //教育歴（開始年月）
            $setVal["S_YM"] = str_replace("\n", "", $my->MyMonthWin2($objForm, $sk."_S_YM", $Row[$sk."_S_YM"]));

            //教育歴（終了年月）
            $setVal["E_YM"] = str_replace("\n", "", $my->MyMonthWin2($objForm, $sk."_E_YM", $Row[$sk."_E_YM"]));

            //通級チェックボックス
            $extra  = ($Row[$sk."_PASSING_GRADE_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"{$sk}_PASSING_GRADE_FLG\"";
            $setVal["PASSING_GRADE_FLG"] = knjCreateCheckBox($objForm, $sk."_PASSING_GRADE_FLG", "1", $extra, "");

            //特別支援学級チェックボックス
            $extra  = ($Row[$sk."_SUPPORT_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"{$sk}_SUPPORT_FLG\"";
            $setVal["SUPPORT_FLG"] = knjCreateCheckBox($objForm, $sk."_SUPPORT_FLG", "1", $extra, "");

            //その他チェックボックス
            $extra  = ($Row[$sk."_ETC_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"{$sk}_ETC_FLG\" onclick=\"OptionUse(this);\"";
            $setVal["ETC_FLG"] = knjCreateCheckBox($objForm, $sk."_ETC_FLG", "1", $extra, "");

            //その他備考
            $extra  = ($Row[$sk."_ETC_FLG"] == 1) ? "" : " disabled";
            $extra .= " onblur=\"lenCheck(this, 12);\"";
            $setVal["ETC"] = knjCreateTextBox($objForm, $Row[$sk."_ETC"], $sk."_ETC", 12, 12, $extra);

            //時期（開始年月）
            $setVal["DATE_S_YM"] = str_replace("\n", "", $my->MyMonthWin2($objForm, $sk."_DATE_S_YM", $Row[$sk."_DATE_S_YM"]));

            //時期（終了年月）
            $setVal["DATE_E_YM"] = str_replace("\n", "", $my->MyMonthWin2($objForm, $sk."_DATE_E_YM", $Row[$sk."_DATE_E_YM"]));

            $arg["data"][$sk] = str_replace("\n", "", ($setVal["S_YM"]." ～ ".$setVal["E_YM"]." ［".$setVal["PASSING_GRADE_FLG"]."<LABEL for=\"{$sk}_PASSING_GRADE_FLG\">通級</LABEL>　".$setVal["SUPPORT_FLG"]."<LABEL for=\"{$sk}_SUPPORT_FLG\">特別支援学級</LABEL>　".$setVal["ETC_FLG"]."<LABEL for=\"{$sk}_ETC_FLG\">その他</LABEL>　".$setVal["ETC"]."　時期：".$setVal["DATE_S_YM"]." ～ ".$setVal["DATE_E_YM"]));
        }

        //中学校からの引き継ぎ
        $height = 4 * 13.5 + (4 - 1) * 3 + 5;
        $arg["data"]["EDUCATION_TEXT"] = KnjCreateTextArea($objForm, "EDUCATION_TEXT", 4, (50 * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row["EDUCATION_TEXT"]);
        $arg["data"]["EDUCATION_TEXT_COMMENT"] = "(全角50文字X4行まで)";

        /************/
        /*  相談歴  */
        /************/
        for ($cd=1 ; $cd <= 3; $cd++) {
            //機関コンボ
            $query = knje450Query::getCenter("WELFARE_ADVICE_CENTER_MST");
            makeCmb($objForm, $arg, $db, $query, "CONS_INST_CD_".$cd, $Row["CONS_INST_CD_".$cd], "", 1, "blank");

            //時期
            $arg["data"]["CONSULT_DATE_".$cd] = str_replace("\n", "", $my->MyMonthWin2($objForm, "CONSULT_DATE_".$cd, $Row["CONSULT_DATE_".$cd]));

            //相談内容
            $height = 5 * 13.5 + (5 - 1) * 3 + 5;
            $arg["data"]["CONSULT_TEXT_".$cd] = KnjCreateTextArea($objForm, "CONSULT_TEXT_".$cd, 5, (25 * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row["CONSULT_TEXT_".$cd]);
            $arg["data"]["CONSULT_TEXT_".$cd."_COMMENT"] = "(全角25文字X5行まで)";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje450SubForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタンを作成する
    $extra = (strlen($model->writing_date) > 0) ? "onclick=\"return btn_submit('subform2_update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタンを作成する
    $extra = (strlen($model->writing_date) > 0) ? "onclick=\"return btn_submit('subform2_clear');\"" : "disabled";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('edit');\"");

    //追加ボタン
    $extra = "onclick=\"return btn_submit('subform2_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);

    //アセスメントボタン
    $extra = "style=\"height:30px;background:#00FFFF;color:#000080;font:bold\" onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "アセスメント", $extra);

    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "THIS_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "NEXT_YEAR", CTRL_YEAR+1);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJE451");
    knjCreateHidden($objForm, "STAFFCD", STAFFCD);
    knjCreateHidden($objForm, "SCHREG_SELECTED");
}
?>
