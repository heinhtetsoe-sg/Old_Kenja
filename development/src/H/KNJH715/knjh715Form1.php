<?php

require_once('for_php7.php');

class knjh715Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh715Form1", "POST", "knjh715index.php", "", "knjh715Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //出力条件チェックボックス
        $extra = " id=\"JITAI\"";
        $arg["data"]["JITAI"] = knjCreateCheckBox($objForm, "JITAI", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjh715Form1.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList($objForm, &$arg, $db, $model)
{
    //表示切替
    $arg["data"]["TITLE_LEFT"] = "出力対象クラス";

    //初期化
    $opt_left = $opt_right = array();

    //年組取得
    $arg["data"]["TITLE_RIGHT"] = "クラス一覧";
    $query = knjh715Query::getHrClass($model);//クラス一覧取得

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //一覧リスト（右側）
        $opt_right[] = array('label'=> $row["LABEL"],'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left', 1)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', 1)\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', 1);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', 1);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', 1);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', 1);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn($objForm, &$arg)
{
    //印刷
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden($objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJH715");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "selectleftval");
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
}
