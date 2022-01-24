<?php

require_once('for_php7.php');

class knjl386qForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl386qindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();
        
        //コピー用年度コンボ
        $yearQuery = knjl386qQuery::getCopyYear();
        $yearResult = $db->query($yearQuery);
        $opt = array();
        $exist = array();
        $opt[0] = array("value"    => "",
                        "label"    => "");
        while($yearRow = $yearResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("value"  =>  $yearRow["YEAR"],
                           "label"  =>  $yearRow["YEAR"]);
        }
        $extra = " ";
        
        $arg["COPY_YEAR"] = knjCreateCombo($objForm, "COPY_YEAR", $model->left_field["COPY_YEAR"], $opt, $extra, 1);

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "changeMockyear") {
            $arg["reload"] = "window.open('knjl386qindex.php?cmd=edit', 'right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl386qForm1.html", $arg); 
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
    $result = $db->query(knjl386qQuery::getList($model));

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

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //年度コピーボタン
    $extra = " onclick=\"btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の年度からコピー", $extra);
    //CSV出力ボタン
    $extra = " onclick=\"btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
}

?>
