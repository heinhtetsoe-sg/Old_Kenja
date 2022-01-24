<?php

require_once('for_php7.php');

class knjb1220_chair_stdForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb1220_chair_stdForm1", "POST", "knjb1220_chair_stdindex.php", "", "knjb1220_chair_stdForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->exeYear;

        //学期コンボ
        $query = knjb1220_chair_stdQuery::getSemester($model);
        $extra = "onchange=\"return btn_submit('subMain')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        knjCreateHidden($objForm, "H_SEMESTER");

        //履修履歴コンボ
        $query = knjb1220_chair_stdQuery::getRirekiCode($model);
        $extra = "onChange=\"return btn_submit('subMain');\"";
        makeCmb($objForm, $arg, $db, $query, "RIREKI_CODE", $model->field["RIREKI_CODE"], $extra, 1);
        knjCreateHidden($objForm, "H_RIREKI_CODE");

        //生徒情報
        $query = knjb1220_chair_stdQuery::getSchregInfo($model);
        $model->schregInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["SCHREG_INFO"] = $model->schregInfo["HR_NAME"]."　{$model->schregInfo["ATTENDNO"]}番　{$model->schregInfo["NAME"]}({$model->schregInfo["SCHREGNO"]})";

        //科目コンボ
        $query = knjb1220_chair_stdQuery::getSubclassStdSelect($model);
        $extra = "onchange=\"return btn_submit('subMain')\"";
        $model->subClassInState = makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "ALL");
        knjCreateHidden($objForm, "H_SUBCLASSCD");

        //基本時間割コンボ
        $query = knjb1220_chair_stdQuery::getSchPatternH($model);
        $opt = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[]= array('label' => $row["SEMESTERNAME"]." Seq".$row["BSCSEQ"].":".$row["TITLE"],
                          'value' => $row["YEAR"].",".$row["BSCSEQ"].",".$row["SEMESTER"]);
        }
        $result->free();
        $extra = "onchange=\"return btn_submit('subMain')\"";
        $model->field["SCH_PTRN"] = $model->field["SCH_PTRN"] ? $model->field["SCH_PTRN"] : $opt[0]["value"];
        $arg["data"]["SCH_PTRN"] = knjCreateCombo($objForm, "SCH_PTRN", $model->field["SCH_PTRN"], $opt, $extra, 1);
        knjCreateHidden($objForm, "H_SCH_PTRN");

        //radio
        $opt = array(1, 2);
        $model->field["CHAIRDIV"] = ($model->field["CHAIRDIV"] == "") ? "2" : $model->field["CHAIRDIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"CHAIRDIV{$val}\" onClick=\"btn_submit('subMain')\"");
        }
        $radioArray = knjCreateRadio($objForm, "CHAIRDIV", $model->field["CHAIRDIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        knjCreateHidden($objForm, "H_CHAIRDIV");

        //基本講座
        $query = knjb1220_chair_stdQuery::getMainChair($model);
        $model->mainChair = getMainChair($db, $model, $query);
        //受講講座
        $query = knjb1220_chair_stdQuery::getMainStdChair($model);
        $model->mainStdChair = getMainChair($db, $model, $query);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //戻る
        $extra = "onClick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID",         "KNJB1220_CHAIR_STD");
        knjCreateHidden($objForm, "selectChair");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb1220_chair_stdForm1.html", $arg);
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('label' => "全　て",
                       'value' => "");
    }
    $value_flg = false;
    $retInstate = "(";
    $retInstate2 = "(";
    $sep = "";
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
            $retInstate2 .= "'{$row["VALUE"]}'";
        }
        $retInstate .= $sep."'{$row["VALUE"]}'";
        $sep = ",";
    }
    $result->free();
    $retInstate .= ")";
    $retInstate2 .= ")";
    $retInstate = $retInstate == "()" ? "('')" : $retInstate;
    $retInstate2 = $retInstate2 == "()" ? "('')" : $retInstate2;

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $value ? $retInstate2 : $retInstate;
}

//講座データ取得
function getMainChair($db, $model, $query) {
    $retChair = array();
    $result = $db->query($query);
    $weekName = array("土", "日", "月", "火", "水", "木", "金");
    $befChair = "";
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if (!$befChair) {
            $retChair[$row["CHAIRCD"]] .= $row["CHAIRCD"]."　".$row["CHAIRNAME"]."(".$weekName[$row["DAYCD"]].$row["PERIODNAME"];
        } else if ($befChair == $row["CHAIRCD"]) {
            $retChair[$row["CHAIRCD"]] .= ",".$weekName[$row["DAYCD"]].$row["PERIODNAME"];
        } else if ($befChair != $row["CHAIRCD"]) {
            $retChair[$befChair] .= $setEnd;
            $retChair[$row["CHAIRCD"]] .= $row["CHAIRCD"]."　".$row["CHAIRNAME"]."(".$weekName[$row["DAYCD"]].$row["PERIODNAME"];
        }
        $befChair = $row["CHAIRCD"];
        $setEnd = ")(".$row["STD_CNT"]."/".$row["CAPACITY"].")";
    }
    $result->free();
    if ($retChair[$befChair]) {
        $retChair[$befChair] .= $setEnd;
    }

    return $retChair;
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $optLeft = array();
    $leftInstate = "(";
    $sep = "";
    $query = knjb1220_chair_stdQuery::getChairStd($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setLabelArray = preg_split("/　/", $model->mainStdChair[$row["CHAIRCD"]]);
        $setGroup = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        $setLabel = $setLabelArray[0].$setGroup."　".$setLabelArray[1];
        $optLeft[] = array('label' => $setLabel,
                           'value' => $row["CHAIRCD"]);
        $leftInstate .= $sep."'{$row["CHAIRCD"]}'";
        $sep = ",";
    }
    $result->free();
    $leftInstate .= ")";
    $leftInstate = $leftInstate == "()" ? "('')" : $leftInstate;

    $optRight = array();
    if ($model->field["CHAIRDIV"] == "2") {
        $query = knjb1220_chair_stdQuery::getChairDat($model, $leftInstate);
    } else {
        $query = knjb1220_chair_stdQuery::getChairDat2($model, $leftInstate);
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->mainChair[$row["CHAIRCD"]]) {
            $setLabelArray = preg_split("/　/", $model->mainChair[$row["CHAIRCD"]]);
            $setGroup = ((int)$row["GROUPCD"] * 1) == 0 ? str_replace("0", "&nbsp;", $row["GROUPCD"])."&nbsp;&nbsp;&nbsp;" : "({$row["GROUPCD"]})";
            $setLabel = $setLabelArray[0].$setGroup."　".$setLabelArray[1];
            $optRight[] = array('label' => $setLabel,
                                'value' => $row["GROUPCD"]."_".$row["CHAIRCD"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:330px\" width:\"330px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $optRight, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:330px\" width:\"330px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $optLeft, $extra, 20);

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
?>
