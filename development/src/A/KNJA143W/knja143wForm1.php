<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knja143wForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja143wForm1", "POST", "knja143windex.php", "", "knja143wForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //(内部データとして)学期マスタを取得
        $query = knja143wQuery::getSemeMst(CTRL_YEAR, CTRL_SEMESTER);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);

        //年度
        $objForm->ae(createHiddenAe("YEAR", $Row_Mst["YEAR"]));
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];

        //1:生徒, 2:教職員
        $opt = array(1, 2);
        $model->field["TAISHOUSHA"] = ($model->field["TAISHOUSHA"] == "") ? "1" : $model->field["TAISHOUSHA"];
        if ($model->field["TAISHOUSHA"] == "2")  {
            $extra .= "disabled";
        }
        $disabledStaff = $model->Properties["knja143wStaffOutputDisabled"] == "1" ? " disabled " : "";
        $extra = array("id=\"TAISHOUSHA1\" onclick=\"return btn_submit('output');\"", "id=\"TAISHOUSHA2\" onclick=\"return btn_submit('output');\".$disabledStaff ");
        $radioArray = knjCreateRadio($objForm, "TAISHOUSHA", $model->field["TAISHOUSHA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //1:クラス選択、2:個人選択
        $opt = array(1, 2);
        if ($model->field["TAISHOUSHA"] == "2")  {
            $disflg = " disabled";
        } else {
            $disflg = "";
        }
        $model->field["SEL_CLASSTYPE"] = ($model->field["SEL_CLASSTYPE"] == "") ? "1" : $model->field["SEL_CLASSTYPE"];
        $extra = array("id=\"SEL_CLASSTYPE1\" onclick=\"return btn_submit('output');\".$disflg", "id=\"SEL_CLASSTYPE2\" onclick=\"return btn_submit('output');\".$disflg ");
        $radioArray = knjCreateRadio($objForm, "SEL_CLASSTYPE", $model->field["SEL_CLASSTYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学期コンボ
        $extra = "onChange=\"return btn_submit('knja143w');\".$disflg";
        $query = knja143wQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        if ($model->field["TAISHOUSHA"] == "1") {
            if ($model->field["SEL_CLASSTYPE"] == "1") {
                $arg["data"]["SELECTTITLE"] = "クラス";
            } else {
                $arg["data"]["SELECTTITLE"] = "生徒";
            }
        } else {
            $arg["data"]["SELECTTITLE"] = "教職員";
            $arg["showIssueDate"] = "1";
        }
        //発行日
        if( !isset($model->field["TERM_SDATE"]) ) 
            $model->field["TERM_SDATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["TERM_SDATE"]=View::popUpCalendar($objForm,"TERM_SDATE",$model->field["TERM_SDATE"]);

        //有効期限
        if ($model->Properties["knja143wExpireDate"] == '1' || $model->Properties["knja143wExpireDate"] == '2' || $model->Properties["useFormNameA143W"] == 'A143W_7' && $model->field["TAISHOUSHA"] == '2') {
            // 表示しない
        } else {
//            $arg["showExpireDate"] = "1";
            $setYear = CTRL_YEAR + 1;
            $setDate = $setYear.'-03-31';
            if( !isset($model->field["TERM_EDATE"])) { 
                $model->field["TERM_EDATE"] = str_replace("-","/",$setDate);
            }
            $arg["data"]["TERM_EDATE"]=View::popUpCalendar($objForm,"TERM_EDATE",$model->field["TERM_EDATE"]);
        }

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["showSchKind"] = "1";
            $extra = "onChange=\"return btn_submit('knja143w');\"";
            $query = knja143wQuery::getSchoolKind($model);
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        } else {
            $model->field["SCHKIND"] = SCHOOLKIND;
        }

        //学年コンボ
        $arg["showGrade"] = "1";
        if ($model->field["TAISHOUSHA"] == "1") {
            $extra = "onChange=\"return btn_submit('knja143w');\"";
            $query = knja143wQuery::getGrade($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        } else {
            $extra = "onChange=\"return btn_submit('knja143w');\"disabled";
            $query = knja143wQuery::getGrade($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        }

        //個別選択の場合のみ、学級選択コンボを表示
        if ($model->field["TAISHOUSHA"] == "1" && $model->field["SEL_CLASSTYPE"] == "2") {
            //コンボ選択は年組
            $arg["showGradeHrClass"] = "1";
            $query = knja143wQuery::getAuth($model, CTRL_YEAR, $model->field["SEMESTER"], $model->field["GRADE"], 0);
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

            $extra = "onchange=\"return btn_submit('knja143w');\"";
            if ($model->field["TAISHOUSHA"] == "2")  {
                $extra .= "disabled";
            }
            $arg["data"]["GRADE_HR_CLASS"] = createCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);
        }

        //生徒一覧リスト
        $opt_right = array();
        $opt_left  = array();

        $selectStudent = array();
        $selectStudentLabel = array();

        if ($model->field["TAISHOUSHA"] == "1")  {
            if ($model->field["SEL_CLASSTYPE"] == "1") {
                //選択はクラス
                $query = knja143wQuery::getAuth($model, CTRL_YEAR, $model->field["SEMESTER"], $model->field["GRADE"], 1);
            } else {
                //選択は生徒
                $query = knja143wQuery::getSchno($model, CTRL_YEAR, $model->field["SEMESTER"]);
            }
        } else {
            //選択は教師
            $query = knja143wQuery::getStaff(CTRL_YEAR);
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
        View::toHTML($model, "knja143wForm1.html", $arg); 
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
    $objForm->ae(createHiddenAe("PRGID", "KNJA143W"));
    $objForm->ae(createHiddenAe("DOCUMENTROOT", DOCUMENTROOT));
    $objForm->ae(createHiddenAe("cmd"));
    knjCreateHidden($objForm, "usePrgSchoolKind" , $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "useSchoolKindField" , $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "selectSchoolKind" , $model->selectSchoolKind);
    knjCreateHidden($objForm, "SCHOOLCD" , SCHOOLCD);

    if ($model->Properties["use_prg_schoolkind"] != "1") {
        knjCreateHidden($objForm, "SCHKIND" , SCHOOLKIND);
    }

    if ($model->field["TAISHOUSHA"] == "1")  {
        if ($model->field["SEL_CLASSTYPE"] == "1") {
            $grade = $model->field["GRADE"];
        } else {
            $grade = substr($model->field["GRADE_HR_CLASS"], 0, 2);
        }
        $gradYear = CTRL_YEAR + 3 - (((int)$grade + 2) % 3);
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
