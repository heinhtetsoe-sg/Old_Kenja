<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjl140bForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度学期表示
        $arg["YEAR"] = $model->examyear;

        //出力対象ラジオボタンを作成する
        $opt        = array(1,2); //1:入学者 2:保護者 3:通学手段情報
        if ($model->schoolName == "bunkyo") {
            $arg["bunkyo"] = "1";
            $opt[] = "3";
        }
        if ($model->field["OUTPUTDIV"] == '') {
            $model->field["OUTPUTDIV"] = "1";
        }
        $extra      = array("id=\"OUTPUTDIV1\"", "id=\"OUTPUTDIV2\"",  "id=\"OUTPUTDIV3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUTDIV", $model->field["OUTPUTDIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        if ($model->schoolName != "Tamagawa-sei") {
            $extra  = "id=\"OUTPUTSCORE\" ";
            $extra .= strlen($model->field["OUTPUTSCORE"]) ? "checked" : "";
            $arg["OUTPUTSCORE"] = knjCreateCheckBox($objForm, "OUTPUTSCORE", "1", $extra);
        }

        if ($model->schoolName == "bunkyo") {
            $extra = $model->field["OUTPUTDIV3TEMPL"] == "1" ? "checked" : "";
            $extra .= " id=\"OUTPUTDIV3TEMPL\"";

            $arg["OUTPUTDIV3TEMPL"] = knjCreateCheckBox($objForm, "OUTPUTDIV3TEMPL", "1", $extra, "");
        }

        //コンボ作成
        $query = knjl140bQuery::getGoukaku($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, '', "GOUKAKU", $extra, 1, $model);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SELECTED_DATA");
        knjCreateHidden($objForm, "SCHOOLNAME", $model->schoolName);
        if ($model->Properties["useScoreMongonDeviation"] == "1") {
            $arg["SCORE"] = "偏差値も出力";
        } else {
            $arg["SCORE"] = "成績も出力";
        }

        //DB切断
        Query::dbCheckIn($db);
        $arg["start"]  = $objForm->get_start("main", "POST", "knjl140bindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjl140bForm1.html", $arg);
    }
}
/********************************************** ここから下関数 ****************************************************/
//////////////
//コンボ作成//
//////////////
function makeCmb(&$objForm, &$arg, $db, $query, $value, $name, $extra, $size, $model, $blank = "")
{
    $result = $db->query($query);
    $opt    = array();
    $serch  = array();
    if ($model->schoolName == "bunkyo") {
        $opt[] = array("label" => "-- 中学全て --", "value" => "ALL_J");
        $opt[] = array("label" => "-- 高校全て --", "value" => "ALL_H");
    } elseif ($model->schoolName == "sundaikoufu") {
    } else {
        $query2 = knjl140bQuery::getVNameMst($model, "L003");
        $result2 = $db->query($query2);
        while ($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => "-- ".$row["NAME1"]."全て --", "value" => "ALL_".$row["NAMECD2"]);
        }
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    if ($name == "GAKKI") {
        $value = ($value) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//////////////////////
//リストToリスト作成//
//////////////////////
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1[]= array('label' => '受験番号',
                   'value' => 'SCHREGNO');
    $row1[]= array('label' => 'かな氏名',
                   'value' => 'NAME_KANA');
    $row1[]= array('label' => '男女',
                   'value' => 'SEX');

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
//////////////
//ボタン作成//
//////////////
function makeBtn(&$objForm, &$arg)
{
    //実行
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}
