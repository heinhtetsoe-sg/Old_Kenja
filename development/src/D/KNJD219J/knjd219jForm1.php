<?php

require_once('for_php7.php');

class knjd219jform1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd219jindex.php", "", "main");
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
        $query = knjd219jQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);
        //校種コンボ
        $extra = "style=\"width:70px\" onChange=\"return btn_submit('change');\" ";
        $query = knjd219jQuery::getNameMstA023($model, "A023");
        //初回のみ、学年から校種を逆算出(※学年がrequestされているため、行う)
        if ($model->field["SCHOOL_KIND"] == "" && $model->field["GRADE"] != "") {
            $query = knjd219jQuery::getSchKindFromGrade($model);
            $model->field["SCHOOL_KIND"] = $db->getOne($query);
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1);
        //学年コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjd219jQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "ALL2");
        //コースグループコンボ
        $query = knjd219jQuery::getGroupCd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GROUP_CD"], "GROUP_CD", $extra, 1, "ALL4");
        //科目コンボ
        $subquery = knjd219jQuery::getNameMstA023($model, "A023", $model->field["SCHOOL_KIND"]);
        $selSchKind = $db->getOne($subquery);
        $query = knjd219jQuery::getSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, $selSchKind);
        //学期成績コンボ
        $query = knjd219jQuery::getCombined($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["COMBINED"], "COMBINED", $extra, 1);

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
        View::toHTML($model, "knjd219jForm1.html", $arg);
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
        $query = knjd219jQuery::getPerTest($model);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testcdArray[] = $row["TESTCD"];
        }
        $result->free();
    }

    //テスト種別一覧
    $leftList = $rightList = array();
    $dataFlg = false;

    //テスト種別一覧(指定科目)
    $query = knjd219jQuery::getTestCd($model, 'shitei');

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $testcdArray)) {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
        $dataFlg = true;
    }
    $result->free();

    if($dataFlg == false){
        //テスト種別一覧(基本設定)
        $query = knjd219jQuery::getTestCd($model, 'kihon');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            if (in_array($row["VALUE"], $testcdArray)) {
                $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            } else {
                $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            }
        }
        $result->free();
    }

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
function makeList(&$objForm, &$arg, $db, &$model, $testcdArray) {
    //警告メッセージを表示しない場合
    if (!isset($model->warning) && $model->cmd != "read") {
        $query = knjd219jQuery::getListPercent($model);
        $result = $db->query($query);
    } else {
        $row =& $model->field;
        $result = "";
    }
    $i = 0;

    $model->sdivCnt = array();
    $model->rowSpanSdiv = array("01" => "1");
    $testCdSort = array();
    if ($model->Properties["KNJD219J_SeisekiSanshutsuPattern"] == "1") {
        //テストコードの並び替え
        foreach ($testcdArray as $key => $code) {
            list($seme, $test, $sdiv) = explode("-", $code);
            $testCdSort[$sdiv.$code] = $code;
            $model->sdivCnt[$sdiv] += 1;
        }
        ksort($testCdSort);
        foreach ($testcdArray as $key => $code) {
            list($seme, $test, $sdiv) = explode("-", $code);
            $testCdSort[$sdiv.$code] = $code;
        }
    } else {
        $testCdSort = $testcdArray;
    }

    foreach ($testCdSort as $key => $code) {
        list($seme, $test, $sdiv) = explode("-", $code);

        $i++;
        $ar = explode("-", $code);

        $testcd  = $ar[1];

        if ($result != "") {
            if ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //レコードを連想配列のまま配列$arg[data]に追加していく。 
                array_walk($row, "htmlspecialchars_array");
            }
        }

        //No
        $row["SEQ"] = $i;

        //テスト種別名称(指定科目)
        $query = knjd219jQuery::getTestCd($model, 'shitei', $code);
        $testRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if($testRow["LABEL"] == ""){
            //テスト種別名称(基本設定)
            $query = knjd219jQuery::getTestCd($model, 'kihon', $code);
            $testRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        $row["TESTCD_SHOW"] = $testRow["LABEL"];

        //hiddenで保持
        knjCreateHidden($objForm, "TESTCD".$i, $code);

        //満点(考査)
        $query = knjd219jQuery::getPerfect($model, $code);
        //対象実力科目なし
        $row["MARK"] = "";

        $perfect = $db->getOne($query);
        if ($perfect == "") $perfect = 100;
        $row["PERFECT_SHOW"] = $perfect;
        //hiddenで保持
        knjCreateHidden($objForm, "PERFECT".$i, $perfect);

        $row["PERCENT_TEXT"] = "";
        $row["PERCENT_ROW"] = "";
        //割合
        if ($model->Properties["KNJD219J_SeisekiSanshutsuPattern"] == "1" && $model->rowSpanSdiv[$sdiv] == "1") {
            $percent = ($result != "") ? $row["PERCENT"] : $row["PERCENT".$i];
            $extra = "STYLE=\"text-align: right\" onblur=\"isNumb(this, 'per');\" ";
            $row["PERCENT_TEXT"] = knjCreateTextBox($objForm, $percent, "PERCENT".$i, 4, 3, $extra);
            $row["PERCENT_ROW"] = " rowspan=\"{$model->sdivCnt[$sdiv]}\"";
            $model->rowSpanSdiv[$sdiv] = "2";
        } else if ($model->Properties["KNJD219J_SeisekiSanshutsuPattern"] != "1" || !$model->rowSpanSdiv[$sdiv]) {
            $percent = ($result != "") ? $row["PERCENT"] : $row["PERCENT".$i];
            $extra = "STYLE=\"text-align: right\" onblur=\"isNumb(this, 'per');\" ";
            $row["PERCENT_TEXT"] = knjCreateTextBox($objForm, $percent, "PERCENT".$i, 4, 3, $extra);
        }

        $arg["data"][] = $row;
    }

}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    $value_flg = false;

    //ALL1,ALL2,...,ALL9まで指定可。
    if ($name == "SUBCLASSCD" && $blank != "") {
        $cutwkstr = array();
        $cutwkstr = explode(":", $blank);
        $setcd = "00-".$cutwkstr[0]."-00-000000";
        $opt[] = array("label" => $setcd.":基本設定(".$cutwkstr[1].")", "value" => $setcd);
        if ($value == $setcd) $value_flg = true;
    } else if (preg_match("/^ALL/", $blank) && strlen($blank) == 4) {
        $cutlen = substr($blank, 3, 1);
        $setcd = substr("000000000", 0, $cutlen);
        $opt[] = array("label" => "全て", "value" => $setcd);
        if ($value == $setcd) $value_flg = true;
    }

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
    //読込ボタン
    $extra = "onclick=\"return btn_submit('read');\" ";
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
    //確定ボタン
    $extra = "onclick=\"return btn_submit('update');\" ";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "確 定", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\" ";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削除", $extra);
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
