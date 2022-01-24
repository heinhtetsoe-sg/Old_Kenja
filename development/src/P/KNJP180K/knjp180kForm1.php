<?php

require_once('for_php7.php');

class knjp180kForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knjp180kindex.php", "", "main");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR."年度";

        $query = knjp180kQuery::getHist();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setData["HIST_YEAR"] = $row["YEAR"];

            $row["EXE_TIME"] = str_replace("-", "/", $row["EXE_TIME"]);
            $exeTime = explode(".", $row["EXE_TIME"]);
            $setData["HIST_DATE"] = $exeTime[0];

            $setData["HIST_STAFF"] = $row["STAFFNAME"];

            $arg["HIST"][] = $setData;
        }
        $result->free();

        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp180kForm1.html", $arg);
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
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
