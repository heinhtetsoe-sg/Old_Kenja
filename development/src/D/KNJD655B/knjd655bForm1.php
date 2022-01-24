<?php

require_once('for_php7.php');

class knjd655bForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd655bForm1", "POST", "knjd655bindex.php", "", "knjd655bForm1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjd655bQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('gakki');\"", 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }
        //学校名取得
        $query = knjd655bQuery::getSchoolname();
        $schoolName = $db->getOne($query);

        //学年コンボ
        $query = knjd655bQuery::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "onChange=\"return btn_submit('knjd655b');\"", 1);

        //テスト種別コンボ
        $query = knjd655bQuery::getTestItem($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], "STYLE=\"WIDTH:140\" ", 1, $schoolName, $model->field["SEMESTER"]);

        //出欠集計開始日付
        $sDate = $model->control["学期開始日付"]["9"];
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $eDate = ($model->field["EDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["EDATE"];
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $eDate);

        //終了日付チェック用
        $seme_edate = $model->control["学期終了日付"][$model->field["SEMESTER"]];
        knjCreateHidden($objForm, "SEME_EDATE", $seme_edate);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //総合順位出力ラジオボタン 1:学年 2:コース
        $opt_rank = array(1, 2);
        $model->field["RANK_DIV"] = ($model->field["RANK_DIV"] == "") ? "1" : $model->field["RANK_DIV"];
        $extra = array("id=\"RANK_DIV1\"", "id=\"RANK_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "RANK_DIV", $model->field["RANK_DIV"], $extra, $opt_rank, get_count($opt_rank));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $model->field["RANK_KIJUN"] = $model->field["RANK_KIJUN"] ? $model->field["RANK_KIJUN"] : '2';
        $opt_kijun = array(1, 2);
        $extra = array("id=\"RANK_KIJUN1\"", "id=\"RANK_KIJUN2\"");
        $radioArray = knjCreateRadio($objForm, "RANK_KIJUN", $model->field["RANK_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //成績上位者
        $value = ($model->field["RANK_NO"]) ? $model->field["RANK_NO"] : "6";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RANK_NO"] = knjCreateTextBox($objForm, $value, "RANK_NO", 4, 1, $extra);

        //成績欠点
        $query = knjd655bQuery::getJHFlg($model->field["GRADE"]);
        $first_val = ("J" == $db->getOne($query)) ? "60" : "40";
        $value = ($model->field["BAD_SCORE"]) ? $model->field["BAD_SCORE"] : $first_val;
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["BAD_SCORE"] = knjCreateTextBox($objForm, $value, "BAD_SCORE", 4, 2, $extra);

        //成績欠点科目
        $value = ($model->field["BAD_SUBCNT"]) ? $model->field["BAD_SUBCNT"] : "15";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["BAD_SUBCNT"] = knjCreateTextBox($objForm, $value, "BAD_SUBCNT", 4, 2, $extra);

        //偏差値出力チェックボックス
        $extra = ($model->field["KANSAN"] == "1") ? "checked" : "";
        $extra .= " id=\"KANSAN\"";
        $arg["data"]["KANSAN"] = knjCreateCheckBox($objForm, "KANSAN", "1", $extra, "");

        if ($schoolName == 'CHIBEN') {
            $arg["KANSAN_FLG"] = '1';
        } else {
            unset($arg["KANSAN_FLG"]);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd655bForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $schoolName = "", $semester = "")
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "GRADE") {
            $opt[] = array('label' => (int)$row["VALUE"] ."学年",
                           'value' => $row["VALUE"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "") ? CTRL_SEMESTER : $value;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjd655bQuery::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = createCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = createCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

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
    $objForm->ae(createHiddenAe("PRGID", "KNJD655B"));
    $objForm->ae(createHiddenAe("LOGIN_DATE", CTRL_DATE));
    $objForm->ae(createHiddenAe("CTRL_SEMESTER", CTRL_SEMESTER));
    $objForm->ae(createHiddenAe("cmd"));
    //年度
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
    $objForm->ae(createHiddenAe("useCurriculumcd", $model->Properties["useCurriculumcd"]));
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
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
