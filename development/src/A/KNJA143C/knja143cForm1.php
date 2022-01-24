<?php

require_once('for_php7.php');

// kanji=漢字

class knja143cForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja143cForm1", "POST", "knja143cindex.php", "", "knja143cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //印刷ラジオボタン 1:表 2:裏
        $opt_data = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onClick=\"return btn_submit('output')\"", "id=\"OUTPUT2\" onClick=\"return btn_submit('output')\"");
        $radioArray = createRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["OUTPUT"] == 1) {
            $arg["omote"] = $model->field["OUTPUT"];
        }
        if ($model->field["OUTPUT"] == 2) {
            $arg["ura"] = $model->field["OUTPUT"];
        }

        //部数テキストボックス
        $model->field["BUSUU"] = ($model->field["BUSUU"] == "") ? 1 : $model->field["BUSUU"];
        $extra = " onBlur=\"return toInteger(this.value);\"";
        $arg["data"]["BUSUU"] = knjCreateTextBox($objForm, $model->field["BUSUU"], "BUSUU", 4, 3, $extra);

        //1:個人,2:クラス表示指定
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knja143c')\"", "id=\"DISP2\" onClick=\"return btn_submit('knja143c')\"");
        $radioArray = createRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["DISP"] == 1) {
            $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
            $arg["data"]["TITLE_RIGHT"] = "生徒一覧";
        }
        if ($model->field["DISP"] == 2) {
            $arg["data"]["TITLE_LEFT"]  = "出力対象クラス";
            $arg["data"]["TITLE_RIGHT"] = "クラス一覧";
        }

        //年度・学期
        $ctrl_year      = CTRL_YEAR;
        $ctrl_semester  = CTRL_SEMESTER;

        $query = knja143cQuery::getSemeMst($ctrl_year, $ctrl_semester);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //有効期限
        if (!isset($model->field["TERM_SDATE"])) {
            $model->field["TERM_SDATE"] = str_replace("-","/",CTRL_DATE);
        }
        if (!isset($model->field["TERM_EDATE"])) {
            $nendoPlass = CTRL_YEAR + 1;
            $model->field["TERM_EDATE"] = $nendoPlass ."/03/31";
        }
        $arg["data"]["TERM_SDATE"] = View::popUpCalendar($objForm, "TERM_SDATE", $model->field["TERM_SDATE"]);
        $arg["data"]["TERM_EDATE"] = View::popUpCalendar($objForm, "TERM_EDATE", $model->field["TERM_EDATE"]);

        //クラス一覧リスト
        $row1 = array();
        $class_flg = false;
        $query = knja143cQuery::getAuth($ctrl_year, $ctrl_semester);
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
            $query = knja143cQuery::getSchno2($model,$ctrl_year,$ctrl_semester);
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

        makeHidden($objForm, $Row_Mst, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja143cForm1.html", $arg); 
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
function makeHidden(&$objForm, $Row_Mst, $model)
{
    createHidden($objForm, "YEAR", $Row_Mst["YEAR"]);
    createHidden($objForm, "SEMESTER", $Row_Mst["SEMESTER"]);
    createHidden($objForm, "DBNAME", DB_DATABASE);
    createHidden($objForm, "PRGID", "KNJA143C");
    createHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    createHidden($objForm, "cmd");
    createHidden($objForm, "selectleft");
    createHidden($objForm, "selectleftval");
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
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
