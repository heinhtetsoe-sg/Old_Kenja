<?php

require_once('for_php7.php');

class knjc033kSubForm2
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjc033kindex.php", "", "sel");
        $arg["jscript"] = "";

        //DB接続
        $db = Query::dbCheckOut();

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //チェックボックス
        makeCheckBox($objForm, $arg);

        //更新用データ
        makeInputData($objForm, $arg, $model);

        /* ボタン作成 */
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $arg, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc033kSubForm2.html", $arg); 
    }
}

//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, &$model)
{
    //タイトル表示
    makeTitle($arg, $db, $model);

    //生徒一覧
    $opt_left = $opt_right = array();

    $result   = $db->query(knjc033kQuery::GetStudent($model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        /* 締め日 */
        $model->appointed_day = getInitData($row["APPOINTED_DAY"], $model->appointed_day);
        /* 授業時数 */
        $model->lesson = getInitData($row["LESSON"], $model->lesson);
        /* 対象生徒リスト */
        $opt_right[]  = array("label" => $row["HR_NAMEABBV"]."  ".$row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"], 
                              "value" => $row["SCHREGNO"]);
    }
    $result->free();

    if ($model->appointed_day == "") {
        $model->appointed_day = getFinalDay($db, $model);
    }
    //対象生徒
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ";
    $arg["main_part"]["LEFT_PART"]   = createCombo($objForm, "left_select", "left", $opt_left, $extra, 20);
    //生徒一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ";
    $arg["main_part"]["RIGHT_PART"]  = createCombo($objForm, "right_select", "left", $opt_right, $extra, 20);
    //対象へ一括
    $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_ADD_ALL"] = createBtn($objForm, "SEL_ADD_ALL", "≪", $extra);
    //対象へ
    $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_ADD"]     = createBtn($objForm, "SEL_ADD", "＜", $extra);
    //一覧へ
    $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_DEL"]     = createBtn($objForm, "SEL_DEL", "＞", $extra);
    //一覧へ一括
    $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_DEL_ALL"] = createBtn($objForm, "SEL_DEL_ALL", "≫", $extra);

}

//生徒リストToリストヘッダ作成
function makeTitle(&$arg, $db, $model)
{
    $chaircdName = $db->getOne(knjc033kQuery::getChairName($model));
    $arg["info"] = array("TOP"        => CTRL_YEAR."年度　".$model->field["SEMESTER"]."学期　".$model->field["CHAIRCD"]."　".$chaircdName,
                         "LEFT_LIST"  => "対象者一覧",
                         "RIGHT_LIST" => "生徒一覧");
}

//最終日取得
function getFinalDay($db, $model)
{
    $year = CTRL_YEAR;
    if ($model->field["MONTH"] != "" && $model->field["MONTH"] < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $model->field["MONTH"], 1, $year ));
    $semeday = $db->getRow(knjc033kQuery::selectSemesAll($model->field["SEMESTER"]),DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $model->field["MONTH"] &&
        $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }
    return $lastday;
}

//テキストデータ初期値算出
function getInitData($dbData, $modelData)
{
    $rtnData = $modelData;
    if ($dbData != "" && $dbData > $modelData) {
        $rtnData = $dbData;
    }

    return $rtnData;
}

//チェックボックス
function makeCheckBox(&$objForm, &$arg)
{
    for ($i = 0; $i < 3; $i++) {
        $extra = "";
        if ($i == 0) {
            $extra = "onClick=\"return check_all(this);\"";
        } 
        $arg["data"]["RCHECK".$i] = createCheckBox($objForm, "RCHECK".$i, 1, $extra, "");
    }
}

//入力フィールド作成
function makeInputData(&$objForm, &$arg, &$model)
{
    $extra = "onblur=\" this.value = toInteger(this.value)\";";
    $arg["data"]["APPOINTED_DAY"] = createText($objForm, "APPOINTED_DAY", $model->appointed_day, $extra, 2, 2);
    $arg["data"]["LESSON"] = createText($objForm, "LESSON", $model->lesson, $extra, 2, 3);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //更新ボタン
    $arg["btn_update"] = createBtn($objForm, "btn_update", "更 新", "onclick=\"return doSubmit()\"");
    //戻るボタン
    $link  = REQUESTROOT."/C/KNJC033K/knjc033kindex.php?";
    $link .= "cmd=back&SCHREGNO=".$model->schregno."&MONTHCD=".$model->field["MONTHCD"];
    $link .= "&SUBCLASSCD=".$model->field["SUBCLASSCD"]."&CHAIRCD=".$model->field["CHAIRCD"];
    $arg["btn_back"] = createBtn($objForm, "btn_back", "戻 る", "onclick=\"window.open('$link','_self');\"");
}

//hidden作成
function makeHidden(&$objForm, &$arg, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("selectdata"));

    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
    $objForm->ae(createHiddenAe("MONTHCD", $model->field["MONTHCD"]));
    $objForm->ae(createHiddenAe("SUBCLASSCD", $model->field["SUBCLASSCD"]));
    $objForm->ae(createHiddenAe("CHAIRCD", $model->field["CHAIRCD"]));
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
function createText(&$objForm, $name, $value, $extra, $size, $maxlen)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "extrahtml" => $extra,
                        "value"     => $value));
    return $objForm->ge($name);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
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

