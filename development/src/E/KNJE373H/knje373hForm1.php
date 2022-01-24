<?php

require_once('for_php7.php');

class knje373hForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje373hForm1", "POST", "knje373hindex.php", "", "knje373hForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学校種別ラジオボタン 1:出身学校 2:塾
        $opt_disp = array(1, 2);
        $model->field["SCHOOLDIV"] = ($model->field["SCHOOLDIV"] == "") ? "1" : $model->field["SCHOOLDIV"];
        $extra = array("id=\"SCHOOLDIV1\" onchange=\"return btn_submit('knje373h')\"", "id=\"SCHOOLDIV2\" onchange=\"return btn_submit('knje373h')\"");
        $radioArray = knjCreateRadio($objForm, "SCHOOLDIV", $model->field["SCHOOLDIV"], $extra, $opt_disp, get_count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //在学・卒業ラジオボタン 1:全て 2:在学生 3:卒業生
        $opt_disp = array(1, 2, 3);
        $model->field["GRD_DIV"] = ($model->field["GRD_DIV"] == "") ? "1" : $model->field["GRD_DIV"];
        $extra = array("id=\"GRD_DIV1\" onchange=\"return btn_submit('knje373h')\"", "id=\"GRD_DIV2\" onchange=\"return btn_submit('knje373h')\"", "id=\"GRD_DIV3\" onchange=\"return btn_submit('knje373h')\"");
        $radioArray = knjCreateRadio($objForm, "GRD_DIV", $model->field["GRD_DIV"], $extra, $opt_disp, get_count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //地区コンボ
        $model->field["DISTRICTCD"] = ($model->field["DISTRICTCD"] == "") ? "ALL" : $model->field["DISTRICTCD"];
        $extra = "onChange=\"return btn_submit('knje373h');\"";
        $query = knje373hQuery::getDistrict($model);
        makeCmb($objForm, $arg, $db, $query, "DISTRICTCD", $model->field["DISTRICTCD"], $extra, 1, "EXCEPT");

        //表示区分ラジオボタン 1:上段：合格内定進路～ 2:上段：希望受験進路～
        $opt_disp = array(1, 2);
        $model->field["ENT_DIV"] = ($model->field["ENT_DIV"] == "") ? "1" : $model->field["ENT_DIV"];
        $extra = array("id=\"ENT_DIV1\" onchange=\"return btn_submit('knje373h')\"", "id=\"ENT_DIV2\" onchange=\"return btn_submit('knje373h')\"");
        $radioArray = knjCreateRadio($objForm, "ENT_DIV", $model->field["ENT_DIV"], $extra, $opt_disp, get_count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $schKindStr);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knje373hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank != "") {
        $opt[] = array("label" => "全て", "value" => "ALL");
        if ($blank == "EXCEPT") {
            $opt[] = array("label" => "以外", "value" => "EXCEPT");
            if ($value == "EXCEPT") {
                $value_flg = true;
            }
        }
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //表示切替
    if ($model->field["SCHOOLDIV"] == 1) {
        $arg["data"]["TITLE_RIGHT"] = "出身学校一覧";
    } else {
        $arg["data"]["TITLE_RIGHT"] = "出身塾一覧";
    }

    //初期化
    $opt_left = $opt_right = array();

    //出身学校 or 出身塾一覧取得
    $query = knje373hQuery::getSchool($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //一覧リスト（右側）
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:100%;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:100%;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $schKindStr)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJE373H");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useQualifiedManagementFlg", $model->Properties["useQualifiedManagementFlg"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
}
