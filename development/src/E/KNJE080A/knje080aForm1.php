<?php

require_once('for_php7.php');

class knje080aForm1
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje080aForm1", "POST", "knje080aindex.php", "", "knje080aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //卒業生選択ラジオボタンを作成
        $opt = array(1, 2);
        if ($model->field["REGDDIV"] == "") {
            $model->field["REGDDIV"] = "1";
        }
        $onclick = "onclick =\" return btn_submit('knje080a');\"";
        $extra = array("id=\"REGDDIV1\" ".$onclick , "id=\"REGDDIV2\" ".$onclick);
        $radioArray = knjCreateRadio($objForm, "REGDDIV", $model->field["REGDDIV"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        if ($model->field["REGDDIV"] == "2") {
            $arg["show_grd_year"] = "1";
            //卒業生年度学期択コンボボックス
            $query = knje080aQuery::getAuthGrdYear();
            $extra = "onchange=\"return btn_submit('knje080a'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "GRD_YEAR_SEMESTER", $model->field["GRD_YEAR_SEMESTER"], $extra, 1, $model);

            list ($grdYear, $grdSemester) = explode("-", $model->field["GRD_YEAR_SEMESTER"]);
            //クラス選択コンボボックス
            $query = knje080aQuery::getAuth($model, $grdYear, $grdSemester);
            $extra = "onchange=\"return btn_submit('knje080a'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, $model);
        } else {
            $arg["show_regd_year"] = "1";
            //クラス選択コンボボックス
            $query = knje080aQuery::getAuth($model, CTRL_YEAR, CTRL_SEMESTER);
            $extra = "onchange=\"return btn_submit('knje080a'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, $model);
        }
        $rows = knje080aQuery::getCertifMaxnumber($db, "151", $model);
        foreach ($rows as $key => $val) {
            knjCreateHidden($objForm, $key, $val);
        }

        //年組のMAX文字数取得
        $max_len = 0;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
            $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $max_len);

        // 発行番号は印刷しない
        $extra = $model->field["NOT_PRINT_CERTIFNO"] == "1" ? "checked" : "";
        $extra .= " id=\"NOT_PRINT_CERTIFNO\" onclick =\"check1(this);\"";
        $arg["data"]["NOT_PRINT_CERTIFNO"] = knjCreateCheckBox($objForm, "NOT_PRINT_CERTIFNO", "1", $extra, "");

        // 印刷する発行番号を指定する
        $extra = $model->field["USE_CERTIFNO_START"] == "1" ? "checked" : "";
        $extra .= " id=\"USE_CERTIFNO_START\" onclick =\"check1(this);\"";
        $arg["data"]["USE_CERTIFNO_START"] = knjCreateCheckBox($objForm, "USE_CERTIFNO_START", "1", $extra, "");

        // 開始発行番号
        $extra  = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align: right;\" ";
        $extra .= " id=\"CERTIFNO_START\" ";
        $arg["data"]["CERTIFNO_START"] = knjCreateTextBox($objForm, $model->field["CERTIFNO_START"], "CERTIFNO_START", 8, 8, $extra);

        // 発行年月日
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        // 出欠集計範囲
        //出欠集計日付作成
        $seme1sdate = str_replace('-', '/', $db->getOne(knje080aQuery::getSemesterField("1", "SDATE")));
        knjCreateHidden($objForm, "ATTEND_START_DATE_MIN", $seme1sdate);
        if ($model->field["ATTEND_START_DATE"] == "") {
            // 1学期の開始日
            $model->field["ATTEND_START_DATE"] = $seme1sdate;
        }
        $arg["data"]["ATTEND_START_DATE"] = View::popUpCalendar($objForm, "ATTEND_START_DATE", $model->field["ATTEND_START_DATE"]);
        knjCreateHidden($objForm, "ATTEND_END_DATE_MAX", str_replace('-', '/', $db->getOne(knje080aQuery::getSemesterField("9", "EDATE"))));
        $model->field["ATTEND_END_DATE"] = $model->field["ATTEND_END_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["ATTEND_END_DATE"];
        $arg["data"]["ATTEND_END_DATE"] = View::popUpCalendar($objForm, "ATTEND_END_DATE", $model->field["ATTEND_END_DATE"]);

        // 履修中は印刷しない
        $extra = $model->field["NOT_PRINT_RISHUTYU"] == "1" ? "checked" : "";
        $extra .= " id=\"NOT_PRINT_RISHUTYU\" onclick =\"check1(this);\"";
        $arg["data"]["NOT_PRINT_RISHUTYU"] = knjCreateCheckBox($objForm, "NOT_PRINT_RISHUTYU", "1", $extra, "");
        // 履修中は「履」を印刷する。
        $extra = $model->field["PRINT_RI"] == "1" ? "checked" : "";
        $extra .= " id=\"PRINT_RI\" onclick =\"check1(this);\"";
        $arg["data"]["PRINT_RI"] = knjCreateCheckBox($objForm, "PRINT_RI", "1", $extra, "");

        // 備考欄へ出力する内容
        $extra = ""; // "style=\"height:210px;\"";
        $extra .= " id=\"BIKO\" ";
        $moji = 30;
        $gyo = 4;
        $arg["data"]["BIKO"] = knjCreateTextArea($objForm, "BIKO", $gyo, $moji * 2, "soft", $extra, $model->field["BIKO"]);
        $arg["data"]["BIKO_SIZE"] = "(全角{$moji}文字{$gyo}行まで)";
        knjCreateHidden($objForm, "BIKO_KETA", (int)$moji * 2);
        knjCreateHidden($objForm, "BIKO_GYO", $gyo);
        KnjCreateHidden($objForm, "BIKO_STAT", "statusareaBIKO");

        // 在学生を卒業と出力する。
        $extra = $model->field["PRINT_SOTSU"] == "1" ? "checked" : "";
        $extra .= " id=\"PRINT_SOTSU\" onclick=\"check1(this);\" ";
        $arg["data"]["PRINT_SOTSU"] = knjCreateCheckBox($objForm, "PRINT_SOTSU", "1", $extra, "");
        // 卒業日
        //$model->field["PRINT_GRD_DATE"] = $model->field["PRINT_GRD_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["PRINT_GRD_DATE"];
        $arg["data"]["PRINT_GRD_DATE"] = View::popUpCalendar($objForm, "PRINT_GRD_DATE", $model->field["PRINT_GRD_DATE"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knje080aForm1.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $max_len)
{

    $opt1 = $opt2 = array();
    //生徒一覧リストを作成する
    $query = knje080aQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //クラス名称調整
        $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
        $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
        $len = $zenkaku * 2 + $hankaku;
        $hr_name = $row["HR_NAME"];
        for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) {
            $hr_name .= "&nbsp;";
        }
        if (!in_array($row["SCHREGNO"], $model->select_data["selectdata"])) {
            $opt1[]= array('label' => $row["SCHREGNO"].' '.$hr_name.' '.$row["ATTENDNO"].'番 '.$row["NAME_SHOW"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧リスト
    $extra = "multiple style=\"width:280px; height: 320px; \" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

    //出力対象者リストを作成する
    if ($model->select_data["selectdata"][0]) {
        $query = knje080aQuery::getList($model, $model->select_data["selectdata"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //クラス名称調整
            $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
            $len = $zenkaku * 2 + $hankaku;
            $hr_name = $row["HR_NAME"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) {
                $hr_name .= "&nbsp;";
            }

            $opt2[]= array('label' => $row["SCHREGNO"].' '.$hr_name.' '.$row["ATTENDNO"].'番 '.$row["NAME_SHOW"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
    }

    //出力対象者一覧リスト
    $extra = "multiple style=\"width:280px; height: 320px; \" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", isset($opt2)?$opt2:array(), $extra, 20);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
{
    $opt = array();
    $value_flg = false;
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
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE080A");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "seisekishoumeishoTaniPrintRyugaku", $model->Properties["seisekishoumeishoTaniPrintRyugaku"]);
    knjCreateHidden($objForm, "seisekishoumeishoNotPrintAnotherStudyrec", $model->Properties["seisekishoumeishoNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "certifPrintRealName", $model->Properties["certifPrintRealName"]);
    knjCreateHidden($objForm, "seisekishoumeishoPrintCoursecodename", $model->Properties["seisekishoumeishoPrintCoursecodename"]);
    knjCreateHidden($objForm, "seisekishoumeishoCreditOnlyClasscd", $model->Properties["seisekishoumeishoCreditOnlyClasscd"]);
}
