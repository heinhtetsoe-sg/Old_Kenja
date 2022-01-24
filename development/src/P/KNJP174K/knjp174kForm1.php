<?php

require_once('for_php7.php');

class knjp174kForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //権限チェック
        authCheck($arg);

        //ＤＢ接続
        $db = Query::dbCheckOut();

        //学年コンボボックス
        $query = knjp174kQuery::getGrade();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1);

        //相殺区分ラジオ
        $opt = array(1, 2);
        $model->reducDiv = ($model->reducDiv == "") ? "1" : $model->reducDiv;
        $extra = array("id=\"REDUC_DIV1\"", "id=\"REDUC_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "REDUC_DIV", $model->reducDiv, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //基本開始月コンボボックス
        $defM = "04";
        $query = knjp174kQuery::getMonth();
        $extra = "";
        $model->paidMonthS = $model->paidMonthS ? $model->paidMonthS : $defM;
        makeCmb($objForm, $arg, $db, $query, $model->paidMonthS, "S_MONTH", $extra, 1);

        //基本終了月コンボボックス
        $defM = $db->getOne(knjp174kQuery::getdefMonth());
        $query = knjp174kQuery::getMonth();
        $extra = "";
        $model->paidMonthE = $model->paidMonthE ? $model->paidMonthE : $defM;
        makeCmb($objForm, $arg, $db, $query, $model->paidMonthE, "E_MONTH", $extra, 1);

        //異動日付
        $model->grdDate = $model->grdDate ? $model->grdDate : CTRL_DATE;
        $arg["data"]["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE", str_replace("-", "/", $model->grdDate),"");

        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //ＤＢ切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knjp174kindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp174kForm1.html", $arg);
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
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

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //実行
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('execute');\"");
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

?>
