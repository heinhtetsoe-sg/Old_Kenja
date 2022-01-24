<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knja143hForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja143hForm1", "POST", "knja143hindex.php", "", "knja143hForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //学期マスタ
        $query = knja143hQuery::getSemeMst(CTRL_YEAR, CTRL_SEMESTER);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);

        //年度
        $objForm->ae(createHiddenAe("YEAR", $Row_Mst["YEAR"]));
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];

        //学期
        $objForm->ae(createHiddenAe("GAKKI", $Row_Mst["SEMESTER"]));
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //1:生徒, 2:教職員
        $opt = array(1, 2);
        $model->field["TAISHOUSHA"] = ($model->field["TAISHOUSHA"] == "") ? "1" : $model->field["TAISHOUSHA"];
        if ($model->field["TAISHOUSHA"] == "2")  {
            $extra .= "disabled";
        }
        $disabledStaff = $model->Properties["knja143hStaffOutputDisabled"] == "1" || $model->Properties["useFormNameA143H"] == 'A143H_10' ? " disabled " : "";
        $extra = array("id=\"TAISHOUSHA1\" onclick=\"return btn_submit('output');\"", "id=\"TAISHOUSHA2\" onclick=\"return btn_submit('output');\".$disabledStaff ");
        $radioArray = knjCreateRadio($objForm, "TAISHOUSHA", $model->field["TAISHOUSHA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //出力区分(1:A4用紙, 2:ラベル)
        if ($model->schoolName == "fukuiken"){
            $arg["isFukui"] = "1";
            $opt = array(1, 2);
            $model->outDiv = ($model->outDiv == "") ? "1" : $model->outDiv;
            if ($model->field["TAISHOUSHA"] == "2")  {
                $extra = array("id=\"OUTDIV1\" onclick=\"return btn_submit('');\"disabled", "id=\"OUTDIV2\" onclick=\"return btn_submit('');\"disabled");
            } else{
                $extra = array("id=\"OUTDIV1\" onclick=\"return btn_submit('');\"", "id=\"OUTDIV2\" onclick=\"return btn_submit('');\"");
            }
            $radioArray = knjCreateRadio($objForm, "OUTDIV", $model->outDiv, $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }
        
        //履修登録者出力
        if ($model->field["RISHUU"] === '1') {
            $extra = "checked='checked' onclick=\"return btn_submit('knja143h');\" ";
        } else {
            $extra = "onclick=\"return btn_submit('knja143h');\"";
        }
        if ($model->field["TAISHOUSHA"] == "2")  {
            $extra .= "disabled";
        }
        $arg["data"]["RISHUU"] = knjCreateCheckBox($objForm, "RISHUU", "1", $extra);

        if ($model->Properties["knja143hNoRishuTourokusha"] != "1") {
            $arg["showRishuTourokusha"] = "1";
        }
        if ($model->field["RISHUU"] === '1') {
            //履修履歴コンボ
            $arg["rirekiCodeSet"] = "1";
            $query = knja143hQuery::getRirekiCode($Row_Mst["YEAR"]);
            $extra = "onChange=\"return btn_submit('knja143h');\"";
            makeCmb($objForm, $arg, $db, $query, "RIREKI_CODE", $model->field["RIREKI_CODE"], $extra, 1);
        }

        //発行日
        if( !isset($model->field["TERM_SDATE"]) ) 
            $model->field["TERM_SDATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["TERM_SDATE"]=View::popUpCalendar($objForm,"TERM_SDATE",$model->field["TERM_SDATE"]);

        //有効期限
        if ($model->Properties["knja143hExpireDate"] == '1' || $model->Properties["knja143hExpireDate"] == '2' || $model->Properties["useFormNameA143H"] == 'A143H_7' && $model->field["TAISHOUSHA"] == '2') {
            // 表示しない
        } else {
            $arg["showExpireDate"] = "1";
            $setYear = CTRL_YEAR + 1;
            $setDate = $setYear.'-03-31';
            if( !isset($model->field["TERM_EDATE"])) { 
                $model->field["TERM_EDATE"] = str_replace("-","/",$setDate);
            }
            $arg["data"]["TERM_EDATE"]=View::popUpCalendar($objForm,"TERM_EDATE",$model->field["TERM_EDATE"]);
        }

        if ($model->Properties["KNJA143HshowStampCheckbox"] == "1") {
            $arg["showStampCheckbox"] = "1";
            // 印影出力
            $extra = $model->field["PRINT_STAMP"] == "1" ? "checked" : "";
            $extra .= " id=\"PRINT_STAMP\" onclick =\"kubun();\"";
            $arg["data"]["PRINT_STAMP"] = knjCreateCheckBox($objForm, "PRINT_STAMP", "1", $extra, "");
        }

        //クラス
        $query = knja143hQuery::getAuth($model, CTRL_YEAR, CTRL_SEMESTER);
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

        $extra = "onchange=\"return btn_submit('knja143h');\"";
        if ($model->field["TAISHOUSHA"] == "2")  {
            $extra .= "disabled";
        }
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
        if ($model->field["TAISHOUSHA"] == "1")  {
            $query = knja143hQuery::getSchno($model, CTRL_YEAR, CTRL_SEMESTER);
        } else {
            $query = knja143hQuery::getStaff(CTRL_YEAR);
        }
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (!in_array($row["VALUE"], $selectStudent)) {
                $opt_right[] = array('label' => $row["NAME"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = createCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = createCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

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

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja143hForm1.html", $arg); 
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
function makeHidden(&$objForm, $model, $db)
{
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID", "KNJA143H"));
    $objForm->ae(createHiddenAe("DOCUMENTROOT", DOCUMENTROOT));
    $objForm->ae(createHiddenAe("cmd"));
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useFormNameA143H" , $model->Properties["useFormNameA143H"]);
    
    if ($model->Properties["knja143hExpireDate"] == '1') {
        knjCreateHidden($objForm, "TERM_EDATE" , (CTRL_YEAR + 1) ."-03-31");
    } else if ($model->Properties["knja143hExpireDate"] == '2') {
        $grade = substr($model->field["GRADE_HR_CLASS"], 0, 2);
        $query = knja143hQuery::getMaxGrade($model, CTRL_YEAR, $grade);
        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        if ($row["MAX_GRADE_CD"] != '' && $row["GRADE_CD"] != '') {
            $gradYear = ($row["MAX_GRADE_CD"] - $row["GRADE_CD"]) + CTRL_YEAR + 1;
        } else {
            $gradYear = ($row["MAX_GRADE"] - $row["GRADE"]) + CTRL_YEAR + 1;
        }
        knjCreateHidden($objForm, "TERM_EDATE" , $gradYear ."/03/31");
    }
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
