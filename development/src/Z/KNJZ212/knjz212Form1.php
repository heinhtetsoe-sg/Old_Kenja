<?php

require_once('for_php7.php');


class knjz212Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz212index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjz212Query::getSemester();
        $extra = "onClick=\"checkCmb('cmbChange')\" onChange=\"btn_submit('cmbChange');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1);

        //学年コンボ
        $query = knjz212Query::getGrade($model);
        $extra = "onClick=\"checkCmb('cmbChange')\" onChange=\"btn_submit('cmbChange');\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1);

        //データセット
        setData($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz212Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }
    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];
    $arg[$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//データ設定
function setData(&$objForm, &$arg, $db, $model)
{
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        $query = knjz212Query::getSchoolKind($model);
        $model->schoolkind = $db->getOne($query);
    }
    $result = $db->query(knjz212Query::Getdata($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $extra = " onChange=\"Setflg(this, 'change');\" onblur=\"valCheck(this, 'blur');\" id=\"".$row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."\"";
        } else {
            $extra = " onChange=\"Setflg(this, 'change');\" onblur=\"valCheck(this, 'blur');\" id=\"".$row["SUBCLASSCD"]."\"";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $setData["SUBCLASSNAME"] = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."：".$row["SUBCLASSNAME"];
        } else {
            $setData["SUBCLASSNAME"] = $row["SUBCLASSCD"]."：".$row["SUBCLASSNAME"];
        }
        $setData["SUBCD"]        = $row["SUBCLASSCD"];
        $setData["ADJUST"]       = createTextBox($objForm, $row["VALUE"], "ADJUST", 3, 3, $extra);
        $arg["data"][] = $setData;
    }
    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    $arg["btn_copy"]   = createBtn($objForm, "btn_copy", "前年度からコピー", "onClick=\"btn_submit('copy');\"");
    $arg["btn_update"] = createBtn($objForm, "btn_update", "更 新", "onClick=\"btn_submit('update');\"");
    $arg["btn_reset"]  = createBtn($objForm, "btn_reset", "取 消", "onClick=\"btn_submit('reset');\"");
    $arg["btn_end"]    = createBtn($objForm, "btn_end", "終 了", "onclick=\"btn_submit('end');\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd", $model->cmd));
    $objForm->ae(createHiddenAe("change_flg", "false"));
    $objForm->ae(createHiddenAe("update_data"));
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

//テキスト作成
function createTextBox(&$objForm, $data, $name, $size, $maxlen, $extra)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $data,
                        "extrahtml" => $extra) );
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
