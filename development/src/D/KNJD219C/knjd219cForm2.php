<?php

require_once('for_php7.php');

class knjd219cForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd219cindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //表示切替(1:学年 2:クラス 3:コース)
        $div_name = "";
        if ($model->field["DIV"] == '2') {
            $arg["show2"] = $model->field["DIV"];
            $div_name = "クラス";
        } elseif ($model->field["DIV"] == '3') {
            $arg["show3"] = $model->field["DIV"];
            $div_name = "コース";
        } else {
            $arg["show1"] = $model->field["DIV"];
            $div_name = "学年";
        }

        /**********/
        /* ヘッダ */
        /**********/
        $arg["sepa"]["TEST_NAME"]   = $db->getOne(knjd219cQuery::getTestName($model));
        $arg["sepa"]["DIV_NAME"]    = $div_name;
        $arg["sepa"]["GRADE_NAME"]  = $db->getOne(knjd219cQuery::getGradeName($model));

        /**********/
        /* リスト */
        /**********/
        makeList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        makeBtn($objForm, $arg, $model);

        /**********/
        /* hidden */
        /**********/
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["Closing"] = " closing_window(); " ;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd219cForm2.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************/
//リスト作成
function makeList(&$objForm, &$arg, $db, $model)
{
    $query = knjd219cQuery::getListInquiry($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //レコードを連想配列のまま配列$arg[data]に追加していく。
        array_walk($row, "htmlspecialchars_array");

        //表示
        $arg["data"][] = $row;
    }
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //終了ボタン
    $extra = "onclick=\"return closeWin();\" ";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}
