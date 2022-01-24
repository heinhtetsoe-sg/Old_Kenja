<?php

require_once('for_php7.php');


class knjd429bForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd429bForm1", "POST", "knjd429bindex.php", "", "knjd429bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "changeHukusiki") {
            $model->field["SCHOOL_KIND"] = "";
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd429bQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd429b');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //コンボ切替ラジオボタン 1:法定クラス 2:実クラス
        $opt = array(1, 2);
        $extra = array("id=\"HUKUSIKI_RADIO1\" onClick=\"btn_submit('changeHukusiki');\"", "id=\"HUKUSIKI_RADIO2\" onClick=\"btn_submit('changeHukusiki');\"");
        $model->field["HUKUSIKI_RADIO"] = $model->field["HUKUSIKI_RADIO"] ? $model->field["HUKUSIKI_RADIO"] : '1';
        $radioArray = knjCreateRadio($objForm, "HUKUSIKI_RADIO", $model->field["HUKUSIKI_RADIO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //法廷クラス・実クラスによってコンボを作成
        if ($model->field["HUKUSIKI_RADIO"] == "1") {
            //HUKUSIKI_RADIO = 1:法廷クラス
            //校種コンボ
            $query = knjd429bQuery::getSchoolKind($model);
            $extra = "onChange=\"return btn_submit('changeSchoolKind');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
            
            //クラスコンボ作成
            $arg["data"]["GHR_CD_LABEL"] = "年組";
            $query = knjd429bQuery::getHrClass(CTRL_YEAR, $seme, $model);
            $extra = "onchange=\"return btn_submit('knjd429b');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        } else {
            //HUKUSIKI_RADIO = 2:実クラス
            //クラスコンボ作成
            $arg["data"]["GHR_CD_LABEL"] = "実クラス";
            $query = knjd429bQuery::getGhr($model);
            $extra = "onchange=\"return btn_submit('changeGhr');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

            //校種の設定
            if ($model->field["GRADE_HR_CLASS"] != '') {
                $query = knjd429bQuery::getGhr($model, $model->field["GRADE_HR_CLASS"]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row["SCHOOL_KIND"] && $row["SCHOOL_KIND_FLG"] != "1") {
                    $model->setWarning("実クラス内に校種が複数存在します。");
                } else {
                    $model->field["SCHOOL_KIND"] = $row["SCHOOL_KIND"];
                    knjCreateHidden($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"]);
                }
            }
        }

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd429b', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd429b', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //帳票パターンラジオボタン（名称マスタ「A035」より取得）
        if (in_array($model->cmd, array("", "changeHukusiki", "changeSchoolKind", "changeGhr", "")) && $model->field["SCHOOL_KIND"]) {
            $query = knjd429bQuery::getHreportConditionDat(CTRL_YEAR, $model, "102");
            $model->field["PRINT_PATTERN"] = $db->getOne($query);
        }
        $query = knjd429bQuery::getPatternUseMainly();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = $row["NAMECD2"];
            $arg["data"]["PRINT_PATTERN".$row["NAMECD2"]."_LABEL"] = $row["NAME1"];
        }
        if(!$model->field["PRINT_PATTERN"]) {
            $model->field["PRINT_PATTERN"] = "101";
        }
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PRINT_PATTERN{$val}\"");
        }
        $radioArray = paternRadio($objForm, "PRINT_PATTERN", $model->field["PRINT_PATTERN"], $extra, $opt, get_count($opt), $opt);
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力ページチェックボックス 1:表紙・裏表紙 2:学習の記録 3:出欠の記録
        $name = array("PRINT_SIDE1", "PRINT_SIDE2", "PRINT_SIDE3");
        foreach ($name as $key => $val) {
            $extra  = ($model->field[$val] == "1" || $model->cmd == "") ? "checked" : "";
            $extra .= " id=\"$val\"";
            if ($val == "PRINT_SIDE1") $extra .= " onchange=\"return resetOutput1(this);\" ";
            $arg["data"][$val] = knjCreateCheckBox($objForm, $val, "1", $extra, "");
        }

        //修了証書チェックボックス
        $disabled = ($model->field["SCHOOL_KIND"] != "H") ? " disabled " : "";
        if ($model->cmd == "") {
            $checked = " checked ";
            $model->field["OUTPUT1"] = "1";
        } else if ($model->field["PRINT_SIDE1"] == "") {
            $checked = "";
            $model->field["OUTPUT1"] = "";
        } else {
            $checked = ($model->field["OUTPUT1"]) ? " checked " : "";
        }
        $extra = " id=\"OUTPUT1\" ".$checked.$disabled;
        $extra .= " onchange=\"return chkPrintSide1(this);\" ";
        $arg["data"]["OUTPUT1"] = knjCreateCheckBox($objForm, "OUTPUT1", "1", $extra, "");

        if ($model->field["SCHOOL_KIND"] != "H") {
            knjCreateHidden($objForm, "OUTPUT1", $model->field["OUTPUT1"]);
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $seme);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd429bForm1.html", $arg);
    }
}

function makeListToList(&$objForm, &$arg, $db, &$model, $seme) {

    //表示切替
    $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
    $arg["data"]["TITLE_RIGHT"] = "生徒一覧";

    //対象外の生徒取得
    $query = knjd429bQuery::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //初期化
    $opt_right = $opt_left = array();

    //左リストで選択されたものを再セット
    $selectdata = ($model->selectdata != "") ? explode(",", $model->selectdata) : array();
    $selectdataText = ($model->selectdataText != "") ? explode(",", $model->selectdataText) : array();
    for ($i = 0; $i < get_count($selectdata); $i++) {
        $opt_left[] = array("label" => $selectdataText[$i],
                            "value" => $selectdata[$i]);
    }
    //対象者リストを作成する
    if ($model->field["HUKUSIKI_RADIO"] == "1") {
        //法廷クラス
        $query = knjd429bQuery::getStudent1($model, $seme);
        $result = $db->query($query);
        $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
            if (!in_array($row["SCHREGNO"], $selectdata)) {
                $opt_right[] = array('label' => $row["SCHREGNO_SHOW"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();
    } else {
        //実クラス
        $query = knjd429bQuery::getStudent2($model, $seme);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            list($houtei, $hr, $schregNo) = explode("-", $row["VALUE"]);
            $idou = (in_array($schregNo, $opt_idou, true)) ? "●" : "　";
            if (!in_array($row["VALUE"], $selectdata, true)) {
                $opt_right[] = array('label' => $idou.$row["LABEL"],
                           'value' => $row["VALUE"]);
            } else {
                $opt_left[] = array('label' => $idou.$row["LABEL"],
                           'value' => $row["VALUE"]);
            }
        }
        $result->free();
    }

    //一覧リスト（右）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);
        
    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJD429B");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataText");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
}

//ラジオ作成
function paternRadio(&$objForm, $name, $value, $extra, $multi, $count, $valArray)
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
        $ret[$name.$valArray[$i - 1]] = $objForm->ge($name, $valArray[$i - 1]);
    }

    return $ret;
}
?>
