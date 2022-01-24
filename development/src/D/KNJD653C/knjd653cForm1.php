<?php

require_once('for_php7.php');

class knjd653cForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd653cForm1", "POST", "knjd653cindex.php", "", "knjd653cForm1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjd653cQuery::getSemester();
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
        $query = knjd653cQuery::getSchoolname();
        $schoolName = $db->getOne($query);

        //学年コンボ
        $query = knjd653cQuery::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "onChange=\"return btn_submit('knjd653c');\"", 1);

        //テスト種別コンボ
        $query = knjd653cQuery::getTestItem($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "STYLE=\"WIDTH:140\" ", 1, $schoolName, $model->field["SEMESTER"]);

        //出力対象ラジオボタン 1:クラス別 2:コース別 3:学年別
        $model->field["SELECT_DIV"] = $model->field["SELECT_DIV"] ? $model->field["SELECT_DIV"] : '1';
        $opt = array(1, 2, 3);
        $submit = " onclick=\"return btn_submit('knjd653c');\"";
        $extra = array("id=\"SELECT_DIV1\"".$submit, "id=\"SELECT_DIV2\"".$submit, "id=\"SELECT_DIV3\"".$submit);
        $radioArray = knjCreateRadio($objForm, "SELECT_DIV", $model->field["SELECT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //クラス一覧リストToリスト
        if ($model->field["SELECT_DIV"] == "1" || $model->field["SELECT_DIV"] == "2") {
            $arg["showList"] = '1';
            makeListToList($objForm, $arg, $db, $model);
        }

        //出力順ラジオボタン 1:３教科 2:５教科 3:全教科
        $model->field["SORT"] = $model->field["SORT"] ? $model->field["SORT"] : '3';
        $opt = array(1, 2, 3);
        $disSort = $model->field["SELECT_DIV"] == "1" ? " disabled" : "";
        $extra = array("id=\"SORT1\"".$disSort, "id=\"SORT2\"".$disSort, "id=\"SORT3\"".$disSort);
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //単位保留チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT4");

        //空行チェックボックス
        //makeCheckBox($objForm, $arg, $model, "OUTPUT5");

        //コース毎に改頁チェックボックス
        //makeCheckBox($objForm, $arg, $model, "OUTPUT_COURSE");

        //総合順位出力チェックボックス 1:学級 2:学年 3:コース
        $check_rank = array(1, 2, 3);
        foreach($check_rank as $key){
            $extra  = (($model->cmd == "" && $key != "2") || $model->field["OUTPUT_RANK".$key] == "1") ? "checked" : "";
            $extra .= " id=\"OUTPUT_RANK{$key}\"";
            $arg["data"]["OUTPUT_RANK".$key] = knjCreateCheckBox($objForm, "OUTPUT_RANK".$key, "1", $extra, "");
        }

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $model->field["OUTPUT_KIJUN"] = $model->field["OUTPUT_KIJUN"] ? $model->field["OUTPUT_KIJUN"] : '1';
        $opt_kijun = array(1, 2);
        $extra = array("id=\"OUTPUT_KIJUN1\"", "id=\"OUTPUT_KIJUN2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $model->field["OUTPUT_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //最大科目数ラジオボタン 1:15科目 2:20科目
        $model->field["OUTPUT_KAMOKUSU"] = $model->field["OUTPUT_KAMOKUSU"] ? $model->field["OUTPUT_KAMOKUSU"] : '1';
        $opt_kijun = array(1, 2);
        $extra = array("id=\"OUTPUT_KAMOKUSU1\"", "id=\"OUTPUT_KAMOKUSU2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KAMOKUSU", $model->field["OUTPUT_KAMOKUSU"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd653cForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $schoolName = "", $semester = "")
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

    if ($name == "TESTKINDCD" && $semester == "9" && $schoolName == 'kumamoto') {
        $opt[] = array("label" => "9909　学年評定", "value" => "9909");
    }

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    if ($model->field["SELECT_DIV"] == "1") {
        $query = knjd653cQuery::getHrClass($model);
        $arg["data"]["LIST_NAME"] = "クラス";
    } else if ($model->field["SELECT_DIV"] == "2") {
        $query = knjd653cQuery::getCourse($model);
        $arg["data"]["LIST_NAME"] = "コース";
    }
    $result = $db->query($query);
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

//チェックボックスを作成する
function makeCheckBox(&$objForm, &$arg, $model, $name)
{
    $extra  = ($model->field[$name] == "1") ? "checked" : "";
    $extra .= " id=\"$name\"";
    $value = isset($model->field[$name]) ? $model->field[$name] : 1;
    $arg["data"][$name] = knjCreateCheckBox($objForm, $name, $value, $extra, "");
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
    $objForm->ae(createHiddenAe("PRGID", "KNJD653C"));
    $objForm->ae(createHiddenAe("CTRL_DATE", CTRL_DATE));
    $objForm->ae(createHiddenAe("CTRL_SEMESTER", CTRL_SEMESTER));
    $objForm->ae(createHiddenAe("useCurriculumcd", $model->Properties["useCurriculumcd"]));
    $objForm->ae(createHiddenAe("cmd"));
    //年度
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
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
