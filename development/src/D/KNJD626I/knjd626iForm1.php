<?php

require_once('for_php7.php');

class knjd626iForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd626iForm1", "POST", "knjd626iindex.php", "", "knjd626iForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        
        //学期コンボ
        $extra = "onChange=\"return btn_submit('change');\"";
        $query = knjd626iQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        
        //学年コンボ作成
        $extra = "onChange=\"return btn_submit('change');\"";
        $query = knjd626iQuery::getGradeHrClass($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        
        //表示指定ラジオボタン 1:教科 2:科目
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('change')\"", "id=\"DISP2\" onClick=\"return btn_submit('change')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //教科コンボ
        if ($model->field["DISP"] == "2") {
            $query = knjd626iQuery::getClass($model);
            $extra = "onChange=\"return btn_submit('change');\"";
            makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "BLANK");
        }

        //男女チェックボックス
        $extra = "id=\"SEX\"";
        $extra .= ($model->field["SEX"] == "1") ? " checked" : "";
        $arg["data"]["SEX"] = knjCreateCheckBox($objForm, "SEX", "1", $extra);
        
        //担当者チェックボックス
        $extra = "id=\"TANTOU\" onChange=\"return btn_submit('knjd626i');\"";
        $extra .= ($model->field["TANTOU"] == "1") ? " checked" : "";
        $extra .= ($model->field["DISP"] == "1") ? " disabled" : "";
        $arg["data"]["TANTOU"] = knjCreateCheckBox($objForm, "TANTOU", "1", $extra);

        //スモールクラスチェックボックス
        $extra = "id=\"S_CLASS\"";
        $extra .= ($model->field["S_CLASS"] == "1") ? " checked" : "";
        if ($model->field["DISP"] == "1" || $model->field["TANTOU"] == "1") {
            $extra .= " disabled";
        }
        $arg["data"]["S_CLASS"] = knjCreateCheckBox($objForm, "S_CLASS", "1", $extra);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjd626iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank != "") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $arg["data"]["TITLE_RIGHT"] = $model->field["DISP"] == "1" ? "教科一覧" : "科目一覧";

    //初期化
    $opt_left = $opt_right = array();

    $selectleft = ($model->selectleft != "") ? explode(",", $model->selectleft) : array();
    $selectleftval = ($model->selectleftval != "") ? explode(",", $model->selectleftval) : array();
    
    //左リストで選択されたものを再セット
    $selectleft = ($model->selectleft != "") ? explode(",", $model->selectleft) : array();
    $selectleftval = ($model->selectleftval != "") ? explode(",", $model->selectleftval) : array();
    for ($i = 0; $i < get_count($selectleft); $i++) {
        $opt_left[] = array("label" => $selectleftval[$i],
                            "value" => $selectleft[$i]);
    }

    //リスト取得
    if ($model->field["DISP"] == "1") {
        $query = knjd626iQuery::getClass($model);
    } else {
        $query = knjd626iQuery::getSubclass($model);
    }

    //教科数カウント
    $maxCnt = 0;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $selectleft)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        if ($model->field["DISP"] == "1") {
            $maxCnt++;
        }
    }
    
    //全教科数
    if ($model->field["DISP"] == "1") {
        knjCreateHidden($objForm, "MAXCNT", $maxCnt);
    }
    
    $result->free();
    //一覧リスト（右）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right')\"";
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
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "selectleftval");
    knjCreateHidden($objForm, "PRGID", "KNJD626I");
}
