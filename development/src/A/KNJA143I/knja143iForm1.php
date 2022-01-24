<?php

require_once('for_php7.php');

// kanji=漢字

class knja143iForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja143iForm1", "POST", "knja143iindex.php", "", "knja143iForm1");

        //DB接続
        $db = Query::dbCheckOut();

        if($model->Properties["useFormNameA143I"] == ""){
            //出力区分を表示
            $arg["data"]["OUT"] = 1; 
            //1:新入生,2:在籍表示指定
            $opt = array(1, 2);
            $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
            $extra = array("id=\"OUTPUT1\" onClick=\"return btn_submit('knja143i')\"", "id=\"OUTPUT2\" onClick=\"return btn_submit('knja143i')\"");
            $radioArray = createRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }else{
            //出力区分 = 2:在籍
            $model->field["OUTPUT"] = "2";
        }

        //1:個人,2:クラス表示指定
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knja143i')\"", "id=\"DISP2\" onClick=\"return btn_submit('knja143i')\"");
        $radioArray = createRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if($model->Properties["useFormNameA143I"] == ""){
            //出力順を表示
            $arg["data"]["SORT"] = 1; 
            //出力順(1:年組番,2:学籍番号（受験番号）)
            $opt = array(1, 2);
            $model->field["SORT_DIV"] = ($model->field["SORT_DIV"] == "") ? "1" : $model->field["SORT_DIV"];
            $extra = array();
            foreach($opt as $key => $val) {
                array_push($extra, " id=\"SORT_DIV{$val}\"");
            }
            $radioArray = knjCreateRadio($objForm, "SORT_DIV", $model->field["SORT_DIV"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }else{
            //出力順 = 1:年組番
            $model->field["SORT_DIV"] = "1";
        }

        if ($model->field["DISP"] == 1) {
            $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
            $arg["data"]["TITLE_RIGHT"] = "生徒一覧";
        }
        if ($model->field["DISP"] == 2) {
            $arg["data"]["TITLE_LEFT"]  = "出力対象クラス";
            $arg["data"]["TITLE_RIGHT"] = "クラス一覧";
        }

        //年度コンボ
        $extra = "onChange=\"return btn_submit('change_year');\"";
        $query = knja143iQuery::getYear(CTRL_YEAR);
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //学期コンボ
        $extra = "onChange=\"return btn_submit('knja143i');\"";
        $query = knja143iQuery::getSemester($model->field["YEAR"]);
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //有効期限
        if (!strlen($model->field["TERM_SDATE"])) {
            $model->field["TERM_SDATE"] = str_replace("-","/", $model->field["YEAR"]."-04-01");
        }
        if (!strlen($model->field["TERM_EDATE"])) {
            $nendoPlass = $model->field["YEAR"] + 1;
            $model->field["TERM_EDATE"] = $nendoPlass ."/03/31";
        }
        $arg["data"]["TERM_SDATE"] = View::popUpCalendar($objForm, "TERM_SDATE", $model->field["TERM_SDATE"]);
        $arg["data"]["TERM_EDATE"] = View::popUpCalendar($objForm, "TERM_EDATE", $model->field["TERM_EDATE"]);

        //クラス一覧リスト
        $row1 = array();
        $class_flg = false;
        $query = knja143iQuery::getAuth($model, $model->field["YEAR"], $model->field["SEMESTER"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }

        //1:個人表示指定用
        $opt_left = array();
        if ($model->field["DISP"] == 1) {
            if ($model->field["GRADE_HR_CLASS"]=="" || !$class_flg) {
                $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            }

            $objForm->ae( array("type"       => "select",
                                "name"       => "GRADE_HR_CLASS",
                                "size"       => "1",
                                "value"      => $model->field["GRADE_HR_CLASS"],
                                "extrahtml"  => "onChange=\"return btn_submit('change_class');\"",
                                "options"    => $row1));

            $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

            $row1 = array();
            //生徒単位
            $selectleft = ($model->selectleft != "") ? explode(",", $model->selectleft) : array();
            $selectleftval = ($model->selectleftval != "") ? explode(",", $model->selectleftval) : array();
            $query = knja143iQuery::getSchno2($model,$model->field["YEAR"],$model->field["SEMESTER"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"], 
                                                             "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

                if ($model->cmd == 'change_class' ) {
                    if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
                        $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
                                        'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                    }
                } else {
                    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            }
            //左リストで選択されたものを再セット
            if($model->cmd == 'change_class' ) {
                for ($i = 0; $i < get_count($selectleft); $i++) {
                    $opt_left[] = array("label" => $selectleftval[$i],
                                        "value" => $selectleft[$i]);
                }
            }
        }

        $result->free();

        $chdt = $model->field["DISP"];


        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left',$chdt)\"";
        $arg["data"]["CATEGORY_NAME"] = createCombo($objForm, "category_name", "", $row1, $extra, 20);
        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right',$chdt)\"";
        $arg["data"]["CATEGORY_SELECTED"] = createCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $chdt);\"";
        $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left',$chdt);\"";
        $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right',$chdt);\"";
        $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left',$chdt);\"";
        $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);

        makeBtn($objForm, $arg);

        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja143iForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    createHidden($objForm, "DBNAME", DB_DATABASE);
    createHidden($objForm, "PRGID", "KNJA143I");
    createHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    createHidden($objForm, "cmd");
    createHidden($objForm, "selectleft");
    createHidden($objForm, "selectleftval");
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useFormNameA143I" , $model->Properties["useFormNameA143I"]);
    if($model->Properties["useFormNameA143I"] != ""){
        knjCreateHidden($objForm, "OUTPUT" , "2");
        knjCreateHidden($objForm, "SORT_DIV" , "1");
    }
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

//ラジオ作成
function createRadio(&$objForm, $name, $value, $extra, $multi, $count)
{
    $ret = array();
    
    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) $ext = $extra[$i-1];
        else $ext = $extra;
        
        $objForm->ae( array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));
        $ret[$name.$i] = $objForm->ge($name, $i);
    }
    
    return $ret;
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra, $type = "button")
{
    $objForm->ae( array("type"      => $type,
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHidden(&$objForm, $name, $value = "")
{
    $objForm->ae( array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value));

    return $objForm->ge($name);
}

?>
