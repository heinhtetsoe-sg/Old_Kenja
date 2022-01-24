<?php

require_once('for_php7.php');

class knjd062aForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd062aForm1", "POST", "knjd062aindex.php", "", "knjd062aForm1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjd062aQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('gakki');\"", 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }

        //学年コンボ
        $query = knjd062aQuery::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "onChange=\"return btn_submit('knjd062a');\"", 1);

        //テスト種別コンボ
        if ($model->field["SEMESTER"] == "9" && $model->testTable == "TESTITEM_MST_COUNTFLG") {
            $opt = array();
            $opt[] = array('label' => '0000  評価成績',	'value' => '0');
            $arg["data"]["TESTKINDCD"] = createCombo($objForm, "TESTKINDCD", $model->field["TESTKINDCD"], $opt, "", 1);
        } else {
            $query = knjd062aQuery::getTestItem($model);
            makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "", 1, $model);
        }

        //出欠集計範囲ラジオボタン 1:累計 2:学期
        $opt_attend = array(1, 2);
        $model->field["ATTEND"] = ($model->field["ATTEND"] == "") ? "1" : $model->field["ATTEND"];
        $click = " onclick=\"return btn_submit('knjd062a');\"";
        $extra = array("id=\"ATTEND1\"".$click, "id=\"ATTEND2\"".$click);
        $radioArray = knjCreateRadio($objForm, "ATTEND", $model->field["ATTEND"], $extra, $opt_attend, get_count($opt_attend));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計日付（開始日付）
        if($model->field["ATTEND"]){
            $model->field["SDATE"] = ($model->field["ATTEND"] == '1') ? $model->control["学期開始日付"][9] : $model->control["学期開始日付"][$model->field["SEMESTER"]];
        } else {
            $model->field["SDATE"] = $model->control["学期開始日付"][9];
        }
        $arg["el"]["SDATE"] = $model->field["SDATE"];

        //出欠集計日付（終了日付）
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $value);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //単位保留チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT4");

        //空行チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT5");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd062aForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "")
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "") ? CTRL_SEMESTER : $value;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    if ($name == "TESTKINDCD" && $model->testTable == "TESTITEM_MST_COUNTFLG") {
        $opt[] = array('label' => '0000  評価成績',	'value' => '0');
    }

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjd062aQuery::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = createCombo($objForm, "CLASS_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = createCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);
}

//チェックボックスを作成する
function makeCheckBox(&$objForm, &$arg, $model, $name)
{
    $check = ($model->field[$name] == "1") ? "checked" : "";
    $extra = " id=\"$name\"";
    $value = isset($model->field[$name]) ? $model->field[$name] : 1;

    $arg["data"][$name] = createCheckBox($objForm, $name, $value, $check.$extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //閉じるボタン
    $arg["button"]["btn_end"]   = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID", "KNJD062A"));
    $objForm->ae(createHiddenAe("cmd"));
    //学期
    $objForm->ae(createHiddenAe("SEME_DATE", $seme));
    //学期開始日付
    $objForm->ae(createHiddenAe("SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]));
    //学期終了日付
    $objForm->ae(createHiddenAe("SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]));
    //学期終了日付
    $objForm->ae(createHiddenAe("SEME_FLG", $semeflg));
    //年度
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
    //テスト種別使用テーブル
    $objForm->ae(createHiddenAe("COUNTFLG", $model->testTable));
    //出欠集計範囲（開始日付）
    $objForm->ae(createHiddenAe("SDATE", $model->field["SDATE"]));
    //教育課程コード使用フラグ
    $objForm->ae(createHiddenAe("useCurriculumcd", $model->Properties["useCurriculumcd"]));
    $objForm->ae(createHiddenAe("useClassDetailDat", $model->Properties["useClassDetailDat"]));
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"       => "checkbox",
                        "name"       => $name,
                        "extrahtml"  => $extra,    
                        "value"      => $value));

    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
