<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knja144Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja144Form1", "POST", "knja144index.php", "", "knja144Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $objForm->ae(createHiddenAe("GAKKI", CTRL_SEMESTER));
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //タイトル---NO001
        $query = knja144Query::getNameMst(CTRL_YEAR);
        makeCmb($objForm, $arg, $db, $query, $model->field["TITLE"], "TITLE", 1, "");

        //クラス
        $query = knja144Query::getAuth(CTRL_YEAR, CTRL_SEMESTER);
        $class_flg = false;
        $row1 = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"]) || !$class_flg) 
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
        					"extrahtml"  => "onchange=\"return btn_submit('output');\"",
                            "options"    => $row1));
        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //学期マスタ取得（9学期）
        $grade = substr($model->field["GRADE_HR_CLASS"], 0, 2);
        $query = knja144Query::getSemeMst(CTRL_YEAR, 9, $grade);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);

        //有効期限
        if( $model->cmd=="output" || !isset($model->field["TERM_SDATE"]) ) 
            $model->field["TERM_SDATE"] = str_replace("-", "/", $Row_Mst["SDATE"]);
        if( $model->cmd=="output" || !isset($model->field["TERM_EDATE"]) ) 
            $model->field["TERM_EDATE"] = str_replace("-", "/", $Row_Mst["EDATE"]);
        $arg["data"]["TERM_SDATE"]=View::popUpCalendar($objForm,"TERM_SDATE",$model->field["TERM_SDATE"]);//開始
        $arg["data"]["TERM_EDATE"]=View::popUpCalendar($objForm,"TERM_EDATE",$model->field["TERM_EDATE"]);//終了

        //生徒一覧リストTOリスト
        $optst_right = array();
        $optst_left  = array();

        $selectStudent = ($model->selectStudent != "") ? explode(",",$model->selectStudent) : array();
        $selectStudentLabel = ($model->selectStudentLabel != "") ? explode(",",$model->selectStudentLabel) : array();

        for ($i = 0; $i < get_count($selectStudent); $i++) {
            $optst_left[] = array('label' => $selectStudentLabel[$i],
                                  'value' => $selectStudent[$i]);
        }

        $query = knja144Query::getSchno($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["VALUE"], $selectStudent)) {
                $optst_right[] = array('label' => $row["LABEL"],
                                       'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = createCombo($objForm, "category_name", "", $optst_right, $extra, 15);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = createCombo($objForm, "category_selected", "", $optst_left, $extra, 15);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);

        $objForm->ae(createHiddenAe("selectStudent"));
        $objForm->ae(createHiddenAe("selectStudentLabel"));
//NO002--->

        //項目一覧リストTOリスト
        $opt_out = $opt_left = $opt_right = array();
        $opt_out[] = array('label' => "学科名称",   'value' => "1");
        $opt_out[] = array('label' => "クラス名称＋出席番号", 'value' => "2");

        $selectdata = ($model->selectdata != "") ? explode(",",$model->selectdata) : array();
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt_left[]  = array("label" => $opt_out[$selectdata[$i]-1]["label"],
                                 "value" => $opt_out[$selectdata[$i]-1]["value"]);
        }
        for ($i = 0; $i < get_count($opt_out); $i++) {
            if (in_array($opt_out[$i]["value"],$selectdata)) continue;
            $opt_right[] = array("label" => $opt_out[$i]["label"],
                                 "value" => $opt_out[$i]["value"]);
        }

        //出力項目選択一覧リスト
        $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1_out('left')\"";
        $arg["data"]["category_name_out"] = createCombo($objForm, "category_name_out", "", $opt_right, $extra, 6);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1_out('right')\"";
        $arg["data"]["category_selected_out"] = createCombo($objForm, "category_selected_out", "", $opt_left, $extra, 6);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves_out('right');\"";
        $arg["button"]["btn_rights_out"] = createBtn($objForm, "btn_rights_out", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves_out('left');\"";
        $arg["button"]["btn_lefts_out"] = createBtn($objForm, "btn_lefts_out", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1_out('right');\"";
        $arg["button"]["btn_right1_out"] = createBtn($objForm, "btn_right1_out", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1_out('left');\"";
        $arg["button"]["btn_left1_out"] = createBtn($objForm, "btn_left1_out", "＜", $extra);

        //出席番号出力「する」「しない」チェックボックス
        $check1  = ($model->field["CHECK1"] == "on") ? "checked" : "";
        $check1 .= " id=\"CHECK1\"";
        $arg["data"]["CHECK1"] = createCheckBox($objForm, "CHECK1", "on", $check1, "");

        $objForm->ae(createHiddenAe("selectdata"));

//NO002<---

        //「年齢は出力しない」チェックボックス---NO003
        $check2  = ($model->field["CHECK2"] == "on") ? "checked" : "";
        $check2 .= " id=\"CHECK2\"";
        $arg["data"]["CHECK2"] = createCheckBox($objForm, "CHECK2", "on", $check2, "");

        //ボタンを作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja144Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $value = ($value) ? $value : $opt[0]["value"];

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID", "KNJA144"));
    $objForm->ae(createHiddenAe("DOCUMENTROOT", DOCUMENTROOT));
    $objForm->ae(createHiddenAe("cmd"));
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
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
