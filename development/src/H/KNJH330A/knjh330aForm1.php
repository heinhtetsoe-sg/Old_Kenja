<?php

require_once('for_php7.php');

class knjh330aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh330aindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //参照年度コンボ
        $query = knjh330aQuery::getYear("COPYYEAR");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->copyyear, "COPYYEAR", $extra, 1);

        //コピーボタン
        $extra = "onClick=\"return btn_submit('copy');\"";
        $arg["COPYBTN"] = knjCreateBtn($objForm, "COPYBTN", "左の年度データをコピー", $extra);

        //対象年度コンボ
        $query = knjh330aQuery::getYear("MOCKYEAR");
        $extra = "onChange=\" return btn_submit('changeMockyear')\"";
        makeCmb($objForm, $arg, $db, $query, $model->mockyear, "MOCKYEAR", $extra, 1);

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "changeMockyear") {
            $arg["reload"] = "window.open('knjh330aindex.php?cmd=edit', 'right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh330aForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    if ($name == "MOCKYEAR") {
        $value = ($value) ? $value : CTRL_YEAR;
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $result = $db->query(knjh330aQuery::getList($model));

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
         array_walk($row, "htmlspecialchars_array");
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
