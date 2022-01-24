<?php

require_once('for_php7.php');
class knjd614bForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd614bForm1", "POST", "knjd614bindex.php", "", "knjd614bForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度を作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボを作成する
        $query = knjd614bQuery::getSchoolKind();
        $extra = "onChange=\"return btn_submit('knjd614b');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1);

        //学期コンボボックスを作成する
        $extra = "onChange=\"return btn_submit('knjd614b');\"";
        $query = knjd614bQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //学年コンボボックスを作成する
        $extra = "onChange=\"return btn_submit('knjd614b');\"";
        $query = knjd614bQuery::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //テスト種別コンボボックスを作成する
        $extra = "onChange=\"return btn_submit('knjd614b');\"";
        $query = knjd614bQuery::getTestcd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTCD"], "TESTCD", $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["ATTEND_DATE"] == "") $model->field["ATTEND_DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd614b_2', 'on')\" onkeydown=\"if (window.event.keyCode == 13) {tmp_list('knjd614b_2', 'on');}\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["ATTEND_DATE"], "ATTEND_DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd614b_2', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=ATTEND_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['ATTEND_DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["ATTEND_DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //出力対象一覧リストを作成する
        //クラス一覧
        makeListToListHrClass($objForm, $arg, $db, $model);
        //科目一覧
        makeListToListSubclass($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd614bForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て",
                       "value" => "ALL");
    }

    if ($query) {
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        if ($name == "SEMESTER") {
            $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
        } else {
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        }
        $result->free();

    }
    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//出力対象一覧リストを作成する(クラス一覧)
function makeListToListHrClass(&$objForm, &$arg, $db, $model)
{
    $selectdata = ($model->selectdata != "" && $model->cmd == "knjd614b_2") ? explode(",", $model->selectdata) : array();

    //対象者リスト
    $opt_right = array();
    $opt_left = array();
    $query = knjd614bQuery::getHrClass($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $selectdata)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        } else {
            $opt_left[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    makeListToList($objForm, $arg, $opt_right, $opt_left, "CATEGORY_SELECTED", "CATEGORY_NAME", "hrClassButton", "200");
}

//出力対象一覧リストを作成する(クラス一覧)
function makeListToListSubclass(&$objForm, &$arg, $db, $model)
{
    $selectSubclass = ($model->selectSubclass != "" && $model->cmd == "knjd614b_2") ? explode(",", $model->selectSubclass) : array();

    //対象者リスト
    $opt_right = array();
    $opt_left = array();
    $query = knjd614bQuery::getSubclassCd($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $mk = $row["FLG"] == "1" ? "●" : "　";
        $label = $mk.$row["SUBCLASSCD"].":".$row["SUBCLASSNAME"];
        $value = $row["SUBCLASSCD"].":0";
        if (!in_array($value, $selectSubclass)) {
            $opt_right[] = array('label' => $label,
                'value' => $value);
        } else {
            $opt_left[] = array('label' => $label,
                'value' => $value);
        }
    }
    $result->free();

    makeListToList($objForm, $arg, $opt_right, $opt_left, "SUBCLASS_SELECTED", "SUBCLASS_NAME", "subclassButton", "280");
}

//リストTOリスト作成 共通部分
function makeListToList(&$objForm, &$arg, $optRight, $optLeft, $categorySelected, $categoryName, $button_name, $height) {
    //一覧リスト（右）
    $extra = "multiple style=\"width:500px; height:".$height."px\" ondblclick=\"move1('left', '".$categoryName."', '".$categorySelected."')\"";
    $arg["data"][$categoryName] = knjCreateCombo($objForm, $categoryName, "", $optRight, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:500px; height:".$height."px\" ondblclick=\"move1('right', '".$categoryName."', '".$categorySelected."')\"";
    $arg["data"][$categorySelected] = knjCreateCombo($objForm, $categorySelected, "", $optLeft, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', '".$categoryName."', '".$categorySelected."');\"";
    $arg[$button_name]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '".$categoryName."', '".$categorySelected."');\"";
    $arg[$button_name]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', '".$categoryName."', '".$categorySelected."');\"";
    $arg[$button_name]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '".$categoryName."', '".$categorySelected."');\"";
    $arg[$button_name]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //終了ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    //テスト名称
    $result = $db->query(knjd614bQuery::getTestcd($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $objForm->ae(createHiddenAe("TESTNAMES".$row["VALUE"], $row["LABEL"]));
    }
    $result->free();

    $objForm->ae(createHiddenAe("PRGID", "KNJD614B"));
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
    $objForm->ae(createHiddenAe("CTRL_SEMESTER", CTRL_SEMESTER));
    $objForm->ae(createHiddenAe("DATE", CTRL_DATE));
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("TESTNAME"));
    knjCreateHidden($objForm, "useCurriculumcd",   $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataText");
    knjCreateHidden($objForm, "selectSubclass");
    knjCreateHidden($objForm, "selectSubclassText");
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
