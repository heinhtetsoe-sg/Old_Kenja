<?php

require_once('for_php7.php');

class knjd219bform1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd219bindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["sepa"]["YEAR"] = CTRL_YEAR;

        /**********/
        /* コンボ */
        /**********/
        //対象
        //学期コンボ
        $extra = "style=\"width:70px\" onChange=\"return btn_submit('change');\" ";
        $query = knjd219bQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);
        //学年コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjd219bQuery::getGrade();
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);
        //コースグループコンボ
        $query = knjd219bQuery::getGroupCd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GROUP_CD"], "GROUP_CD", $extra, 1);
        //科目コンボ
        $query = knjd219bQuery::getSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //テスト種別一覧リストToリスト
        $testcdArray = makeListToList($objForm, $arg, $db, $model);
        knjCreateHidden($objForm, "TEST_COUNT", get_count($testcdArray));

        /**********/
        /* リスト */
        /**********/
        makeList($objForm, $arg, $db, $model, $testcdArray);

        /**********/
        /* ボタン */
        /**********/
        makeBtn($objForm, $arg, $model, $testcdArray);

        /**********/
        /* hidden */
        /**********/
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (AUTHORITY < DEF_UPDATE_RESTRICT) {
            $arg["Closing"] = " closing_window(); " ;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd219bForm1.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************/
//テスト種別一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //テスト種別数の取得
    //処理対象テスト種別(左のリスト）
    $testcdArray = array();
    if ($model->cmd == 'read') {
        $testcdArray = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
    } else {
        $query = knjd219bQuery::getPerTest($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testcdArray[] = $row["TESTCD"];
        }
        $result->free();
    }
    //テスト種別一覧
    $leftList = $rightList = array();
    $query = knjd219bQuery::getTestkindcd($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $testcdArray)) {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    $result->free();
    //テスト種別一覧（実力）
    $query = knjd219bQuery::getProficiencyCd($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $testcdArray)) {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //テスト種別一覧作成
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
    $arg["sepa"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 5);

    //処理対象テスト種別作成
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
    $arg["sepa"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 5);

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

    return $testcdArray;
}
//リスト作成
function makeList(&$objForm, &$arg, $db, $model, $testcdArray) {
    //警告メッセージを表示しない場合
    if (!isset($model->warning) && $model->cmd != "read") {
        $query = knjd219bQuery::getListPercent($model);
        $result = $db->query($query);
    } else {
        $row =& $model->field;
        $result = "";
    }

    $total  = ""; //合計点
    $adjust = ""; //調整点

    $i = 0;
    foreach ($testcdArray as $key => $code) {
        $i++;
        $ar = explode("-", $code);
        $testdiv = $ar[0];
        $testcd  = $ar[1];

        if ($result != "") {
            if ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //レコードを連想配列のまま配列$arg[data]に追加していく。 
                array_walk($row, "htmlspecialchars_array");
            }
        }

        //No
        $row["SEQ"] = $i;

        //テスト種別名称
        if ($testdiv == "2") {
            $query = knjd219bQuery::getProficiencyCd($model, $testcd); //実力
        } else {
            $query = knjd219bQuery::getTestkindcd($model, $code);
        }
        $testRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $row["TESTCD_SHOW"] = $testRow["LABEL"];
        //hiddenで保持
        knjCreateHidden($objForm, "TESTCD".$i, $code);

        //満点(考査)
        if ($testdiv == "2") {
            //実力科目
            $query = knjd219bQuery::getProficiencySubclasscd($model, $testcd);
            $model->field["PROFICIENCY_SUBCLASS_CD"] = $db->getOne($query);
            knjCreateHidden($objForm, "PROFICIENCY_SUBCLASS_CD", $model->field["PROFICIENCY_SUBCLASS_CD"]);
            //対象実力科目なし
            $row["MARK"] = (strlen($model->field["PROFICIENCY_SUBCLASS_CD"])) ? "" : "※";
            //実力満点
            $query = knjd219bQuery::getProficiencyPerfect($model, $testcd); //実力
        } else {
            $query = knjd219bQuery::getPerfect($model, $testcd);
            //対象実力科目なし
            $row["MARK"] = "";
        }
        $perfect = $db->getOne($query);
        if ($perfect == "") $perfect = 100;
        $row["PERFECT_SHOW"] = $perfect;
        //hiddenで保持
        knjCreateHidden($objForm, "PERFECT".$i, $perfect);

        //割合
        $percent = ($result != "") ? $row["PERCENT"] : $row["PERCENT".$i];
        $extra = "STYLE=\"text-align: right\" onblur=\"isNumb(this, 'per');\" ";
        $row["PERCENT_TEXT"] = knjCreateTextBox($objForm, $percent, "PERCENT".$i, 4, 3, $extra);

        $total  = $row["TOTAL"];  //合計点
        $adjust = $row["ADJUST"]; //調整点

        $arg["data"][] = $row;
    }

    //合計点
    $arg["totaldata"]["TOTAL_SHOW"] = "<span id=\"totalID\">{$total}</span>";
    //hiddenで保持
    knjCreateHidden($objForm, "TOTAL", $total);
    //調整点
    $extra = "STYLE=\"text-align: right\" onblur=\"isNumb(this, 'adj');\" ";
    $arg["totaldata"]["ADJUST_TEXT"] = knjCreateTextBox($objForm, $adjust, "ADJUST", 4, 3, $extra);
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["sepa"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $testcdArray) {
    //照会ボタン
    $url = REQUESTROOT."/D/KNJD219B/knjd219bindex.php?cmd=inquiry&SEMESTER={$model->field["SEMESTER"]}&TESTKINDCD={$model->field["TESTKINDCD"]}&DIV={$model->field["DIV"]}&GRADE={$model->field["GRADE"]}";
    $extra = "onClick=\" wopen('{$url}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    //$arg["button"]["btn_inquiry"] = knjCreateBtn($objForm, "btn_inquiry", "照 会", $extra);
    //読込ボタン
    $extra = "onclick=\"return btn_submit('read');\" ";
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
    //計算ボタン
    $testCnt = get_count($testcdArray);
    $extra = "onclick=\"return keisan({$testCnt});\" ";
    $arg["button"]["btn_keisan"] = knjCreateBtn($objForm, "btn_keisan", "シュミレーション", $extra);
    //確定ボタン
    $extra = "onclick=\"return btn_submit('update');\" ";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "確 定", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\" ";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return closeWin();\" ";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    //シュミレーション実行後、確定ボタン有効とする
    knjCreateHidden($objForm, "SIM_FLG");
}
?>
