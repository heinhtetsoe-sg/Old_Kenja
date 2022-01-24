<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knja143fForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja143fForm1", "POST", "knja143findex.php", "", "knja143fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //学期マスタ
        $query = knja143fQuery::getSemeMst(CTRL_YEAR, CTRL_SEMESTER);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);

        //年度
        $objForm->ae(createHiddenAe("YEAR", $Row_Mst["YEAR"]));
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];

        //学期
        $objForm->ae(createHiddenAe("GAKKI", $Row_Mst["SEMESTER"]));
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //履修登録者出力
        if ($model->field["RISHUU"] === '1') {
            $extra = "checked='checked' onclick=\"return btn_submit('knja143f');\" ";
        } else {
            $extra = "onclick=\"return btn_submit('knja143f');\"";
        }
        $arg["data"]["RISHUU"] = knjCreateCheckBox($objForm, "RISHUU", "1", $extra);

        if ($model->field["RISHUU"] === '1') {
            //履修履歴コンボ
            $arg["rirekiCodeSet"] = "1";
            $query = knja143fQuery::getRirekiCode($Row_Mst["YEAR"]);
            $extra = "onChange=\"return btn_submit('knja143f');\"";
            makeCmb($objForm, $arg, $db, $query, "RIREKI_CODE", $model->field["RIREKI_CODE"], $extra, 1);
        }

        //発行日
        if( !isset($model->field["TERM_SDATE"]) ) 
            $model->field["TERM_SDATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["TERM_SDATE"]=View::popUpCalendar($objForm,"TERM_SDATE",$model->field["TERM_SDATE"]);

        //クラス
        $query = knja143fQuery::getAuth($model, CTRL_YEAR, CTRL_SEMESTER);
        $class_flg = false;
        $row1 = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"]) || !$class_flg) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $extra = "onchange=\"return btn_submit('knja143f');\"";
        $arg["data"]["GRADE_HR_CLASS"] = createCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //生徒一覧リスト
        $opt_right = array();
        $opt_left  = array();

        $selectStudent = ($model->selectStudent != "") ? explode(",",$model->selectStudent) : array();
        $selectStudentLabel = ($model->selectStudentLabel != "") ? explode(",",$model->selectStudentLabel) : array();

        for ($i = 0; $i < get_count($selectStudent); $i++) {
            $opt_left[] = array('label' => $selectStudentLabel[$i],
                                'value' => $selectStudent[$i]);
        }

        $query = knja143fQuery::getSchno($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (!in_array($row["SCHREGNO"], $selectStudent)) {
                $opt_right[] = array('label' => $row["NAME"],
                                     'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();

        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left', '" . $model->Properties["PRINT_ORDER_TYPE"] . "')\"";
        $arg["data"]["CATEGORY_NAME"] = createCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right', '" . $model->Properties["PRINT_ORDER_TYPE"] . "')\"";
        $arg["data"]["CATEGORY_SELECTED"] = createCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', '" . $model->Properties["PRINT_ORDER_TYPE"] . "');\"";
        $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '" . $model->Properties["PRINT_ORDER_TYPE"] . "');\"";
        $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', '" . $model->Properties["PRINT_ORDER_TYPE"] . "');\"";
        $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '" . $model->Properties["PRINT_ORDER_TYPE"] . "');\"";
        $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);

        //学籍番号のみ出力
        $extra  = $model->field["ONLY_OUTSCHREG"] ? " checked " : "";
        $extra .= " id=\"ONLY_OUTSCHREG\"";
        $arg["data"]["ONLY_OUTSCHREG"] = knjCreateCheckBox($objForm, "ONLY_OUTSCHREG", "1", $extra);

        $objForm->ae(createHiddenAe("selectStudent"));
        $objForm->ae(createHiddenAe("selectStudentLabel"));

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja143fForm1.html", $arg); 
    }
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
    $objForm->ae(createHiddenAe("PRGID", "KNJA143F"));
    $objForm->ae(createHiddenAe("DOCUMENTROOT", DOCUMENTROOT));
    $objForm->ae(createHiddenAe("cmd"));
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "PRINT_ORDER_TYPE" , $model->Properties["PRINT_ORDER_TYPE"]);
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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('label' => "全　て",
                       'value' => "");
    }
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
    }
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

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "YEAR_SEMESTER") ? CTRL_YEAR.":".CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
