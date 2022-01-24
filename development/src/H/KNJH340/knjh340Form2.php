<?php

require_once('for_php7.php');

class knjh340Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh340index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $model->oyear = ($model->oyear) ? $model->oyear : CTRL_YEAR;

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = $db->getRow(knjh340Query::getList($model, "ONE"), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        $arg["data"]["YEAR"] = $model->oyear;

        //区分コンボボックス
        $query = knjh340Query::getCourse($model);
        makeCombo($objForm, $arg, $db, $query, $model->course_div, "COURSE_DIV", "", 1);

        //学年コンボボックス
        $query = knjh340Query::getGrade($model);
        makeCombo($objForm, $arg, $db, $query, $model->grade, "GRADE", "", 1);

        //模試科目コンボボックス
        $query = knjh340Query::getMockSubclass();
        makeCombo($objForm, $arg, $db, $query, $model->mock_subclass_cd, "MOCK_SUBCLASS_CD", "", 1);

        //満点
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PERFECT"] = createTextBox($objForm, $Row["PERFECT"], "PERFECT", 3, 3, $extra);

        //合格点
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PASS_SCORE"] = createTextBox($objForm, $Row["PASS_SCORE"], "PASS_SCORE", 3, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            if ($model->cmd != "reset") {
                $arg["reload"]  = "parent.left_frame.location.href='knjh340index.php?cmd=list2';";
            } else {
                $arg["reload"]  = "parent.left_frame.location.href='knjh340index.php?cmd=list&clear=no';";
            }
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh340Form2.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = createBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = createBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
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
function createTextBox(&$objForm, $value, $name, $size, $maxlen, $extra)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $value,
                        "extrahtml" => $extra) );
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
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

?>
