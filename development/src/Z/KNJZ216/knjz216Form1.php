<?php

require_once('for_php7.php');

class knjz216Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz216index.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //模試
        $query = knjz216Query::getMockcd();
        $extra = "onChange=\"return btn_submit('leftChange');\"";
        makeCmb($objForm, $arg, $db, $query, $model->leftMockcd, "LEFT_MOCKCD", $extra, "BLANK");

        //学年
        $query = knjz216Query::getGrade("MOCK_SUBCLASS_GROUP_MST");
        $extra = "onChange=\"return btn_submit('leftChange');\"";
        makeCmb($objForm, $arg, $db, $query, $model->leftGrade, "LEFT_GRADE", $extra, "BLANK");

        //科目数
        $query = knjz216Query::getGroupDiv();
        $extra = "onChange=\"return btn_submit('leftChange');\"";
        makeCmb($objForm, $arg, $db, $query, $model->leftGroupDiv, "LEFT_GROUP_DIV", $extra, "BLANK");

        //リスト作成
        makeList($arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz216Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $result = $db->query($query);
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data1"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $query = knjz216Query::getList($model);
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");

        $arg["data"][] = $row;
    }

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //コピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["copy"] = knjCreateBtn($objForm, "btn_copy", "前年度コピー", $extra);
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

?>
