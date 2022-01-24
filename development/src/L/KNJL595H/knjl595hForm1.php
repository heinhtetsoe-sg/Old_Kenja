<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjl595hForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度コンボボックス
        $opt_year   = array();
        $opt_year[] = array("label" => (CTRL_YEAR),     "value" => CTRL_YEAR);
        $opt_year[] = array("label" => (CTRL_YEAR + 1), "value" => (CTRL_YEAR + 1));
        $extra = "onChange=\"return btn_submit('main');\"";
        $model->field["YEAR"] = ($model->field["YEAR"] == "") ? substr(CTRL_DATE, 0, 4): $model->field["YEAR"];
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt_year, $extra, 1);

        //出力対象ラジオボタンを作成する
        $opt        = array(1,2); //1:入学者 2:保護者
        if ($model->field["OUTPUTDIV"] == '') {
            $model->field["OUTPUTDIV"] = "1";
        }
        $extra      = array("id=\"OUTPUTDIV1\"", "id=\"OUTPUTDIV2\"",  "id=\"OUTPUTDIV3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUTDIV", $model->field["OUTPUTDIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        $extra  = "id=\"OUTPUTSCORE\" ";
        $extra .= strlen($model->field["OUTPUTSCORE"]) ? "checked" : "";
        $arg["OUTPUTSCORE"] = knjCreateCheckBox($objForm, "OUTPUTSCORE", "1", $extra);

        /******************/
        /* コンボボックス */
        /******************/

        //学校種別
        $query = knjl595hQuery::getNameMst($model->field["YEAR"], "L003");
        $extra = " onChange=\"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試判別
        $query = knjl595hQuery::getDistinctId($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "DISTINCT_ID", $model->field["DISTINCT_ID"], $extra, 1, "");

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        //実行
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
        //終了
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SELECTED_DATA");

        //DB切断
        Query::dbCheckIn($db);
        $arg["start"]  = $objForm->get_start("main", "POST", "knjl595hindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjl595hForm1.html", $arg);
    }
}
/********************************************** ここから下関数 ****************************************************/
//////////////
//コンボ作成//
//////////////
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
