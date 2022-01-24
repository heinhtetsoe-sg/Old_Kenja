<?php

require_once('for_php7.php');


class knjl250cForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl250cForm1", "POST", "knjl250cindex.php", "", "knjl250cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl250cQuery::getApctDiv($model));

        //プレテスト区分コンボ
        $query = knjl250cQuery::getNameMst("L104");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PRE_TESTDIV", $model->field["PRE_TESTDIV"], $extra, 1, "");

        //印刷順序ラジオボタン 1:成績順 2:受付番号順
        $opt = array(1, 2);
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"");
        $model->field["SORT"] = strlen($model->field["SORT"]) ? $model->field["SORT"] : "1";
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->isGojo) {
            $arg["isGojo"] = 1;
        } else {
            $arg["isCollege"] = 1;
        }

        //成績順ラジオボタン 1:２科計 2:３科計 五條は（1:国算理、2:４教科、3:国算英）
        $opt = array(1, 2, 3);
        $extra = array("id=\"GOKEI1\"", "id=\"GOKEI2\"", "id=\"GOKEI3\"");
        $model->field["GOKEI"] = strlen($model->field["GOKEI"]) ? $model->field["GOKEI"] : "1";
        $radioArray = knjCreateRadio($objForm, "GOKEI", $model->field["GOKEI"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //氏名を出力しないチェックボックス
        $extra  = "id=\"OUTPUT\"";
        $extra .= (strlen($model->field["OUTPUT"]) || $model->cmd == "") ? " checked" : "";
        $arg["data"]["OUTPUT"] = knjCreateCheckBox($objForm, "OUTPUT", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl250cForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL250C");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "APPLICANTDIV", "1");
    knjCreateHidden($objForm, "cmd");
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $value_flg = false;
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
