<?php

require_once('for_php7.php');

// kanji=漢字

class knja146Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja146Form1", "POST", "knja146index.php", "", "knja146Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //出力データラジオボタン 1:カード(新入生) 2:カード(在籍)
        $opt_data = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "2" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onClick=\"return btn_submit('output')\"", "id=\"OUTPUT2\" onClick=\"return btn_submit('output')\"");
        $radioArray = createRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //1:個人,2:クラス表示指定
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knja146')\"", "id=\"DISP2\" onClick=\"return btn_submit('knja146')\"");
        $radioArray = createRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["DISP"] == 1) {
            $arg["schno"] = $model->field["DISP"];
        }
        if ($model->field["DISP"] == 2) {
            $arg["clsno"] = $model->field["DISP"];
        }

        //年度・学期
        if ($model->field["OUTPUT"] == 1) {
            if (CTRL_SEMESTER < $model->control["学期数"]) {
                $ctrl_year      = CTRL_YEAR;
                $ctrl_semester  = CTRL_SEMESTER + 1;
            //最終学期
            } else {
                $ctrl_year      = CTRL_YEAR + 1;
                $ctrl_semester  = 1;
            }
        } else {
            $ctrl_year      = CTRL_YEAR;
            $ctrl_semester  = CTRL_SEMESTER;
        }

        $query = knja146Query::getSemeMst($ctrl_year, $ctrl_semester);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //有効期限
        if ($model->field["OUTPUT"] == 1) {
            //月末日を取得
            if ($Row_Mst["SDATE"] != "") {
                $query = knja146Query::getLastDay($Row_Mst["SDATE"]);
                $term_edate = $db->getOne($query);
            }
            if ($model->cmd=="output" || !isset($model->field["TERM_SDATE"])) {
                $model->field["TERM_SDATE"] = str_replace("-","/",$Row_Mst["SDATE"]);
            }
            if ($model->cmd=="output" || !isset($model->field["TERM_EDATE"])) {
                $model->field["TERM_EDATE"] = str_replace("-","/",$term_edate);
            }
        }
        if ($model->field["OUTPUT"] == 2) {
            if ($model->cmd=="output" || !isset($model->field["TERM_SDATE"])) {
                $model->field["TERM_SDATE"] = $model->control["学期開始日付"][9];
            }
            if ($model->cmd=="output" || !isset($model->field["TERM_EDATE"])) {
                $model->field["TERM_EDATE"] = $model->control["学期終了日付"][9];
            }
        }
        $arg["data"]["TERM_SDATE"] = View::popUpCalendar($objForm, "TERM_SDATE", $model->field["TERM_SDATE"]);
        $arg["data"]["TERM_EDATE"] = View::popUpCalendar($objForm, "TERM_EDATE", $model->field["TERM_EDATE"]);

        //クラス一覧リスト
        $row1 = array();
        $class_flg = false;
        $query = knja146Query::getAuth($ctrl_year, $ctrl_semester);
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
            if ($model->field["OUTPUT"] == 1) {
                $query = knja146Query::getSchno1($model,$ctrl_year,$ctrl_semester);
            } else {
                $query = knja146Query::getSchno2($model,$ctrl_year,$ctrl_semester);
            }
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

        makeHidden($objForm, $Row_Mst);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja146Form1.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
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
function makeHidden(&$objForm, $Row_Mst)
{
    createHidden($objForm, "YEAR", $Row_Mst["YEAR"]);
    createHidden($objForm, "SEMESTER", $Row_Mst["SEMESTER"]);
    createHidden($objForm, "DBNAME", DB_DATABASE);
    createHidden($objForm, "PRGID", "KNJA146");
    createHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    createHidden($objForm, "cmd");
    createHidden($objForm, "selectleft");
    createHidden($objForm, "selectleftval");
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
