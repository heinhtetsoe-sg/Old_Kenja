<?php

require_once('for_php7.php');


class knjd617iForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd617iForm1", "POST", "knjd617iindex.php", "", "knjd617iForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd617iQuery::getSemester($model, 0);
        $extra = "onchange=\"return btn_submit('knjd617i'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //学年コンボ作成
        $query = knjd617iQuery::getSchoolName();
        $schoolName = $db->getOne($query);
        $query = knjd617iQuery::getGrade($seme, $model);
        $extra = "onchange=\"return btn_submit('knjd617i'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //テストコンボ作成
        $query = knjd617iQuery::getTest($model, $model->field["GRADE"], 1);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        $add = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            $add = true;
            if ($model->field["TESTKINDCD"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        if ($add == false) {
            $query = knjd617iQuery::getTest($model, $model->field["GRADE"], 2);
            $opt = array();
            $value_flg = false;
            $result = $db->query($query);
            $add = false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);

                $add = true;
                if ($model->field["TESTKINDCD"] == $row["VALUE"]) $value_flg = true;
            }
            $result->free();
        }
        $model->field["TESTKINDCD"] = ($model->field["TESTKINDCD"] && $value_flg) ? $model->field["TESTKINDCD"] : $opt[0]["value"];
        $arg["data"]["TESTKINDCD"] = knjCreateCombo($objForm, "TESTKINDCD", $model->field["TESTKINDCD"], $opt, $extra, $size);

        //出欠集計開始日付作成
        if ($model->field["SDATE"] == "") {
            $model->field["SDATE"] = str_replace("-", "/", $db->getOne(knjd617iQuery::getSdate($model->field["GRADE"])));
        }
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);

        //出欠集計日付作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //クラスリストToリスト作成
        makeClassList($objForm, $arg, $db, $model, $seme);
/***
        //帳票パターン
        $selectForm = array();
        if ($model->Properties["knjd617iSelectForm"] == "") {
            $selectForm = array(1, 2, 3, 4, 5);
        } else {
            $selectForm = explode(",", $model->Properties["knjd617iSelectForm"]);
        }
        $defaultOutputPattern = "";
        foreach ($selectForm as $form) {
            $arg["selectForm".$form] = "1";
            if ($defaultOutputPattern == '') {
                $defaultOutputPattern = $form;
            }
        }
        $optForm = array(1, 2, 3, 4, 5);
        $model->field["OUTPUT_PATERN"] = ($model->field["OUTPUT_PATERN"] == "") ? $defaultOutputPattern : $model->field["OUTPUT_PATERN"];
        $extra = array("id=\"OUTPUT_PATERN1\" onclick=\"return chkPattern();\" ", "id=\"OUTPUT_PATERN2\" onclick=\"return chkPattern();\" ", "id=\"OUTPUT_PATERN3\" onclick=\"return chkPattern();\" ", "id=\"OUTPUT_PATERN4\" onclick=\"return chkPattern();\" ", "id=\"OUTPUT_PATERN5\" onclick=\"return chkPattern();\" ");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_PATERN", $model->field["OUTPUT_PATERN"], $extra, $optForm, get_count($optForm));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        // 実施科目ラジオ作成
        $query = knjd617iQuery::getSemester($model, 1);
        $result = $db->query($query);
        $ss = array();
        $opt_div = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ss[] = $row;
            $opt_div[] = $row["VALUE"];
        }
        $opt_div[] = "9";
        $result->free();
        $extra = "";
        $value = $model->field["TAKESEMES"] ? $model->field["TAKESEMES"] : "9";
        foreach ($ss as $row) {
            $s = array();
            $s["SEME"] = ($row["SEMESTERDIV"] == $row["VALUE"] ? ($row["LABEL"]."・通年") : $row["LABEL"])."科目のみ";
            $s["ID"] = "TAKESEMES".$row["VALUE"];
            $objForm->ae( array("type"      => "radio",
                                "name"      => "TAKESEMES",
                                "value"     => $value,
                                "extrahtml" => " id=TAKESEMES".$row["VALUE"]."",
                                "multiple"  => get_count($opt_div)));

            $s["TAKESEMES"] = $objForm->ge("TAKESEMES", $row["VALUE"]);
            $arg["seme"][] = $s;
        }
        $s = array();
        $s["SEME"] = "全て";
        $s["ID"] = "TAKESEMES9";
        $objForm->ae( array("type"      => "radio",
                            "name"      => "TAKESEMES",
                            "value"     => $value,
                            "extrahtml" => " id=TAKESEMES9"."",
                            "multiple"  => get_count($opt_div)));

        $s["TAKESEMES"] = $objForm->ge("TAKESEMES", "9");
        $arg["seme"][] = $s;

        //総合順位出力ラジオボタン 1.学級 2:学年 3:コース 4:学科
        $opt_rank = array(1, 2, 3, 4);
        $model->field["OUTPUT_RANK"] = ($model->field["OUTPUT_RANK"] == "") ? ($model->Properties["knjd617iDefaultOutputRank"] ? $model->Properties["knjd617iDefaultOutputRank"] : "2") : $model->field["OUTPUT_RANK"];
        $extra = array("id=\"OUTPUT_RANK1\"", "id=\"OUTPUT_RANK2\"", "id=\"OUTPUT_RANK3\"", "id=\"OUTPUT_RANK4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_RANK", $model->field["OUTPUT_RANK"], $extra, $opt_rank, get_count($opt_rank));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
***/
        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $model->field["OUTPUT_KIJUN"] = $model->field["OUTPUT_KIJUN"] ? $model->field["OUTPUT_KIJUN"] : ($model->Properties["knjd617iDefaultOutputKijun"] ? $model->Properties["knjd617iDefaultOutputKijun"] : '2');
        $opt_kijun = array(1, 2);
        $extra = array("id=\"OUTPUT_KIJUN1\"", "id=\"OUTPUT_KIJUN2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $model->field["OUTPUT_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力順ラジオボタンを作成
        $opt = array(1, 2);
        if (!$model->field["OUTPUT_ORDER"]) $model->field["OUTPUT_ORDER"] = 1;
        $extra = array("id=\"OUTPUT_ORDER1\" ", "id=\"OUTPUT_ORDER2\" ");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_ORDER", $model->field["OUTPUT_ORDER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
/***
        //単位保留チェックボックス
        $extra = ($model->field["OUTPUT4"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"OUTPUT4\"";
        $arg["data"]["OUTPUT4"] = knjCreateCheckBox($objForm, "OUTPUT4", "1", $extra, "");
        
        //総合的な学習の時間チェックボックス
        $extra = ($model->field["OUTPUT5"] == "1") ? "checked" : "";
        $extra .= " id=\"OUTPUT5\"";
        $arg["data"]["OUTPUT5"] = knjCreateCheckBox($objForm, "OUTPUT5", "1", $extra, "");
        
        //欠点に追指導を含むチェックボックス
        $extra = ($model->field["OUTPUT6"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"OUTPUT6\"";
        $arg["data"]["OUTPUT6"] = knjCreateCheckBox($objForm, "OUTPUT6", "1", $extra, "");
        
        //欠点者数に欠査者を含めないチェックボックス
        $extra = ($model->field["OUTPUT7"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"OUTPUT7\"";
        $arg["data"]["OUTPUT7"] = knjCreateCheckBox($objForm, "OUTPUT7", "1", $extra, "");
        
        //備考欄出力（出欠備考を出力）チェックボックス
        $extra = ($model->field["OUTPUT_BIKO"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"OUTPUT_BIKO\"";
        $arg["data"]["OUTPUT_BIKO"] = knjCreateCheckBox($objForm, "OUTPUT_BIKO", "1", $extra, "");

        //備考欄出力選択 全て/学期から/年間まとめ
        $model->field["BIKO_KIND"] = $model->field["BIKO_KIND"] ? $model->field["BIKO_KIND"] : '1';
        $opt = array(1, 2, 3);
        $extra = array("id=\"BIKO_KIND1\"", "id=\"BIKO_KIND2\"", "id=\"BIKO_KIND3\"");
        $radioArray = knjCreateRadio($objForm, "BIKO_KIND", $model->field["BIKO_KIND"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //欠課時数を0表記する
        $extra  = ($model->field["PRINT_KEKKA0"] == "1") ? "checked='checked' " : "";
        $extra .= " id='PRINT_KEKKA0'";
        $arg["data"]["PRINT_KEKKA0"] = knjCreateCheckBox($objForm, "PRINT_KEKKA0", "1", $extra);

        //欠課時数を分数表記する
        $extra = "";
        if ($model->field["OUTPUT_PATERN"] == "1") {
            $extra .= $model->field["OUTPUT_STUDENT_JISU"] == "1" ? "checked" : "";
        } else {
            $extra .= " disabled ";
        }
        $extra .= " id=\"OUTPUT_STUDENT_JISU\" onclick =\"chkPattern();\"";
        $arg["data"]["OUTPUT_STUDENT_JISU"] = knjCreateCheckBox($objForm, "OUTPUT_STUDENT_JISU", "1", $extra, "");

        //全クラスの成績表を出力する
        $extra  = ($model->field["PRINT_REPORT"] == "1") ? "checked='checked' " : "";
        $extra .= " id='PRINT_REPORT'";
        $arg["data"]["PRINT_REPORT"] = knjCreateCheckBox($objForm, "PRINT_REPORT", "1", $extra);

        //空行を詰めて印字
        $extra  = ($model->field["NOT_EMPTY_LINE"] == "1") ? "checked='checked' " : "";
        $extra .= " id='NOT_EMPTY_LINE'";
        $arg["data"]["NOT_EMPTY_LINE"] = knjCreateCheckBox($objForm, "NOT_EMPTY_LINE", "1", $extra);
***/
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $seme);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd617iForm1.html", $arg); 
    }
}

function makeClassList(&$objForm, &$arg, $db, $model, $seme) {

    //クラス一覧リストを作成する
    $query = knjd617iQuery::getHrClass($seme, $model->field["GRADE"]);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"height:150px;width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt1, $extra, 20);

    //対象クラスリストを作成する
    $extra = "multiple style=\"height:150px;width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'print');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //CSV出力
    //$extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
    //$arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model, $seme) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJD617I");
    knjCreateHidden($objForm, "cmd");

    //日付範囲チェック用
    if($model->field["SEMESTER"] == '9'){
        $sseme = $model->control["学期開始日付"][9];
        $eseme = $model->control["学期終了日付"][9];
        $semeflg = CTRL_SEMESTER;
    } else {
        $sseme = $model->control["学期開始日付"][$seme];
        $eseme = $model->control["学期終了日付"][$seme];
        $semeflg = $model->field["SEMESTER"];
    }
    knjCreateHidden($objForm, "SEME_SDATE", $sseme);
    knjCreateHidden($objForm, "SEME_EDATE", $eseme);
    knjCreateHidden($objForm, "SEME_FLG", $semeflg);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
    knjCreateHidden($objForm, "knjd617iPrintNullRemark", $model->Properties["knjd617iPrintNullRemark"]);
    knjCreateHidden($objForm, "knjd617iPrintPerfect", $model->Properties["knjd617iPrintPerfect"]);

    //帳票パターン
    knjCreateHidden($objForm, "OUTPUT_PATERN", 1);
    //順位の基準点ラジオボタン 1:総合点 2:平均点
    //knjCreateHidden($objForm, "OUTPUT_KIJUN", 2);
}

?>
