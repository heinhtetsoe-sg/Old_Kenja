<?php

require_once('for_php7.php');

class knjh331Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh331index.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //調査書種類ラジオボタンを作成する
        $opt[0]=1;
        $opt[1]=2;
        $extra = "onClick=\"return btn_submit('divChange')\"";
        createRadio($objForm, $arg, "TARGET_DIV", $model->target_div, $extra, $opt, get_count($opt));

        //模試グループリスト
        makeMockTargetList($arg, $db, $model);

        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "divChange") {
            $arg["reload"] = "window.open('knjh331index.php?cmd=edit&init=1', 'right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh331Form1.html", $arg); 
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, &$value, $extra, $multi, $count)
{
    $value = isset($value) ? $value : "1";

    $objForm->ae( array("type"      => "radio",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));
    for ($i = 1; $i <= $count; $i++) {
        $arg[$name.$i] = $objForm->ge($name, $i);
    }
}

//目標値リスト
function makeMockTargetList(&$arg, $db, $model)
{
    $result = $db->query(knjh331Query::getList($model));

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
         $arg["data"][] = $row;
    }
    $result->free();
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
