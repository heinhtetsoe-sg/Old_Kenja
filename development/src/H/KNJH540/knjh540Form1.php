<?php

require_once('for_php7.php');

class knjh540Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh540index.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //参照年度コンボボックス
        $query = knjh540Query::getYear("RYEAR");
        makeCombo($objForm, $arg, $db, $query, $model->ryear, "RYEAR", "", 1);

        //コピーボタン
        $extra = "onClick=\"return btn_submit('copy');\"";
        $arg["COPYBTN"] = createBtn($objForm, "COPYBTN", "左の年度データをコピー", $extra);

        //対象年度
        $query = knjh540Query::getYear("OYEAR");
        $extra = "onChange=\" return btn_submit('changeOyear')\"";
        makeCombo($objForm, $arg, $db, $query, $model->oyear, "OYEAR", $extra, 1);

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "changeOyear") {
            $arg["reload"] = "window.open('knjh540index.php?cmd=edit', 'right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh540Form1.html", $arg); 
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

    if ($name == "OYEAR") {
        $value = ($value) ? $value : CTRL_YEAR;
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }
    $arg[$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $result = $db->query(knjh540Query::getList($model));

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        $row["CLICK_DATA"] = $row["COURSE_DIV"].":".$row["GRADE"].":".$row["PROFICIENCY_SUBCLASS_CD"];
        $row["COURSE_NAME"] = $row["COURSE_DIV"].":".$row["COURSE_NAME"];
        $arg["data"][] = $row;
    }

    $result->free();
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
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
