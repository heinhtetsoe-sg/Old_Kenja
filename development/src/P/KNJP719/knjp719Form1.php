<?php

require_once('for_php7.php');

class knjp719Form1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp719Form1", "POST", "knjp719index.php", "", "knjp719Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp719Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1, "");

        //学生ラジオボタン
        $opt = array(1, 2);
        $model->dataDiv = ($model->dataDiv == "") ? "2" : $model->dataDiv;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATADIV{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATADIV", $model->dataDiv, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
        
        if ($model->dataDiv == "1"){
            //新入生の場合、次年度を参照する
            $model->year = $arg["YEAR"] + 1;
        } else{
            $model->year = CTRL_YEAR;
        }

        //入金グループコンボ
        $query = knjp719Query::getCollectGrp($model);
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, "COLLECT_GRP_CD", $model->field["COLLECT_GRP_CD"], $extra, 1, "BLANK");

        //入金パターンコンボ
        $query = knjp719Query::getCollectPatternCd($model);
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, "LIST_COLLECT_PATTERN_CD", $model->field["LIST_COLLECT_PATTERN_CD"], $extra, 1, "BLANK");

        //入金パターンコンボ
        $query = knjp719Query::getCollectPatternCd($model, "DIS");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "COLLECT_PATTERN_CD", $model->field["COLLECT_PATTERN_CD"], $extra, 1, "BLANK");

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp719Form1.html", $arg); 
    }
}
/*****************************************************************************************************************/
/***************************************** 以下関数 **************************************************************/
/*****************************************************************************************************************/
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //生徒一覧
    $rightList = $pNameArr = array();
    if ($model->dataDiv == "1"){
        //新入生
        $query = knjp719Query::getStudentRightFresh($model, $model->field["SEMESTER"]);
    } else{
        //在校生
        $query = knjp719Query::getStudentRight($model, $model->field["SEMESTER"]);
    }
    $result = $db->query($query);
    while ($rowP = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //全角スペースセット用
        $pNameArr[$rowP["COLLECT_PATTERN_CD"]] = mb_strlen($rowP["COLLECT_PATTERN_NAME"]);
    }
    arsort($pNameArr);//降順にソート
    $blankArr = array();
    $maxPsize = current($pNameArr) + 1;
    foreach ($pNameArr as $ptternCd => $mbSize) {
        $setBlank = "";
        $cntSize = $maxPsize - $mbSize;
        for ($i=0; $i < $cntSize; $i++) {
            $setBlank .= "　";
        }
        $blankArr[$ptternCd] = $setBlank;
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setLabel = $row["LABEL1"].$row["COLLECT_PATTERN_CD"].":".$row["COLLECT_PATTERN_NAME"].$blankArr[$row["COLLECT_PATTERN_CD"]].$row["NAME_SHOW"];
        $rightList[] = array('label' => $setLabel,
                             'value' => $row["VALUE"]);
    }
    $result->free();

    //生徒一覧作成
    $extra = "multiple style=\"width:450px\" width=\"450px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

    //出力対象生徒作成
    $extra = "multiple style=\"width:450px\" width=\"450px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

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
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
