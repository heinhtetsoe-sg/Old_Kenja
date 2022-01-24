<?php

require_once('for_php7.php');

class knje372bForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje372bindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //参照年度コンボボックス
        $query = knje372bQuery::getYear("RYEAR");
        makeCombo($objForm, $arg, $db, $query, $model->ryear, "RYEAR", "", 1);

        //コピーボタン
        $extra = "onClick=\"return btn_submit('copy');\"";
        $arg["COPYBTN"] = knjCreateBtn($objForm, "COPYBTN", "左の年度データをコピー", $extra);

        //入試年度
        $query = knje372bQuery::getYear("OYEAR");
        $extra = "onChange=\" return btn_submit('changeOyear')\"";
        makeCombo($objForm, $arg, $db, $query, $model->oyear, "OYEAR", $extra, 1);

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "changeOyear") {
            $arg["reload"] = "window.open('knje372bindex.php?cmd=edit', 'right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje372bForm1.html", $arg); 
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
    $value = ($value) ? $value : CTRL_YEAR;

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $result = $db->query(knje372bQuery::getList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        $aHash = array("cmd"                   => "edit",
                       "RECOMMENDATION_CD"     => $row["RECOMMENDATION_CD"],
                       "DEPARTMENT_S"          => $row["DEPARTMENT_S"],
                       "DEPARTMENT_H"          => $row["DEPARTMENT_H"],
                       "DISP_ORDER"            => $row["DISP_ORDER"],
                       "DEPARTMENT_LIST_ORDER" => $row["DEPARTMENT_LIST_ORDER"]);

        $row["RECOMMENDATION_CD"] = View::alink("knje372bindex.php", $row["RECOMMENDATION_CD"], " target=\"right_frame\" ", $aHash);
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
?>
