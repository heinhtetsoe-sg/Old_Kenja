<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knja040Form1
{
    public function main($model)
    {
        $objForm = new form();

        //DB接続
        $db  = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        //動作条件チェック
        } elseif ($db->getOne(knja040Query::checktoStart()) == "0") {
            $arg["check"] = "Show_ErrMsg(1);";
        }
        //最終学期チェック
        if (CTRL_SEMESTER != $model->controls["学期数"]) {
            $arg["max_semes_cl"] = " max_semes_cl(); ";
        }

        //初期化
        if ($model->cmd == "schoolKind") {
            unset($model->field["GUARD_ISSUEDATE"]);
            unset($model->field["GUARD_EXPIREDATE"]);
        }

        //対象年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $arg["data"]["SEMESTER"] = $model->controls["学期名"][CTRL_SEMESTER];
        $arg["data"]["NEWYEAR"]  = $model->new_year;
        $arg["data"]["NOWDATE"]  = knja040Query::getMaxUpdate($db);

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力
        $opt_shubetsu = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"]=="") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        createRadio($objForm, $arg, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));

        //対象データラジオボタン 1:入学者 2:保護者
        $opt_div = array(1, 2);
        $model->field["DATADIV"] = ($model->field["DATADIV"]=="") ? "1" : $model->field["DATADIV"];
        $click = "onClick =\" shorimei_show(this)\"";
        $extra = array("id=\"DATADIV1\"".$click, "id=\"DATADIV2\"".$click);
        createRadio($objForm, $arg, "DATADIV", $model->field["DATADIV"], $extra, $opt_div, get_count($opt_div));

        //校種
        $query = knja040Query::getA023($model);
        $extra = "onChange=\"return btn_submit('schoolKind');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //学校取得
        $query = knja040Query::checkSchool();
        $schoolDiv = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //処理名コンボボックス
        $opt_shori   = array();
        $opt_shori[] = array("label" => "更新","value" => "1");
        if ($schoolDiv["NAMESPARE2"] == "1") {
            $opt_shori[] = array("label" => "削除","value" => "2");
        }
        $arg["data"]["SHORI_MEI"] = createCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", 1);

        //学年コンボボックス
        $query = knja040Query::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "");

        //課程学科コンボ
        $query = knja040Query::getCourseMajor(($model->new_year));
        makeCombo($objForm, $arg, $db, $query, $model->coursemajor, "COURSEMAJOR", "", 1);

        //肩書き出力
        $extra  = $model->field["GUARD_ADDR_FLG"] ? " checked " : "";
        $extra .= " id=\"GUARD_ADDR_FLG\"";
        $arg["data"]["GUARD_ADDR_FLG"] = knjCreateCheckBox($objForm, "GUARD_ADDR_FLG", "1", $extra);

        //学校マスタ取得
        $query = knja040Query::getSchoolMst($model);
        $schData = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //開始日
        $disabled = "";
        $sDate = str_replace("-", "/", $schData["ENTRANCE_DATE"]);
        $value = $model->field["GUARD_ISSUEDATE"] ? $model->field["GUARD_ISSUEDATE"] : $sDate;
        $arg["data"]["GUARD_ISSUEDATE"] = View::popUpCalendarAlp($objForm, "GUARD_ISSUEDATE", $value, $disabled);

        //終了日
        $disabled = "";
        if ($model->field["GUARD_EXPIREDATE"]) {
            $value = $model->field["GUARD_EXPIREDATE"];
        } else {
            $eDate = preg_split("{-}", $schData["GRADUATE_DATE"]);
            $query = knja040Query::getA023GradeRange($model->schoolKind);
            $addYear = $db->getOne($query);
            //中高一貫は、＋6年
            if ($model->schoolKind == "J" && $schoolDiv["NAMESPARE2"] == "1") {
                $value = ((int)$eDate[0] + 3 + (int)$addYear)."/03/31";
            } else {
                $value = ((int)$eDate[0] + (int)$addYear)."/03/31";
            }
        }
        $arg["data"]["GUARD_EXPIREDATE"] = View::popUpCalendarAlp($objForm, "GUARD_EXPIREDATE", $value, $disabled);

        //ヘッダ有無
        $extra = "checked id=\"HEADERCHECK\"";
        $arg["data"]["HEADERCHECK"] = createCheckBox($objForm, "HEADERCHECK", "1", $extra, "");

        //ファイルからの取り込み
        $arg["data"]["FILE"] = createFile($objForm, "FILE", 2048000);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knja040index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja040Form1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) {
            $ext = $extra[$i-1];
        } else {
            $ext = $extra;
        }
        
        $objForm->ae(array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));

        $arg["data"][$name.$i]  = $objForm->ge($name, $i);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["button"]["BTN_OK"] = createBtn($objForm, "btn_ok", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["BTN_CLEAR"] = createBtn($objForm, "btn_cancel", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm)
{
    $objForm->ae(createHiddenAe("cmd"));
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae(array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{
    $objForm->ae(array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}

//File作成
function createFile(&$objForm, $name, $size, $extra = "")
{
    $objForm->add_element(array("type"      => "file",
                                "name"      => $name,
                                "size"      => $size,
                                "extrahtml" => $extra ));

    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae(array("type"      => "button",
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
