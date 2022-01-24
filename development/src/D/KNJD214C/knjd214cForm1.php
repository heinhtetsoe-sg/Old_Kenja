<?php

require_once('for_php7.php');

class knjd214cForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd214cForm1", "POST", "knjd214cindex.php", "", "knjd214cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度・学期
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $arg["data"]["CTRL_SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //処理対象ラジオボタン 1:学期末評価 2:学年評定
        $opt = array(1, 2);
        $extra = "onClick=\"return btn_submit('knjd214c');\"";
        $model->field["COPY_SAKI_DIV"] = $model->field["COPY_SAKI_DIV"] ? $model->field["COPY_SAKI_DIV"] : '2';
        $radioArray = knjCreateRadio($objForm, "COPY_SAKI_DIV", $model->field["COPY_SAKI_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学期コンボ
        $query = knjd214cQuery::getSemester("seme");
        $extra = "onChange=\"return btn_submit('knjd214c');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $query = knjd214cQuery::getSelectGrade($model);
        $extra = "onChange=\"return btn_submit('knjd214c');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //クリア処理とコピー処理のラジオボタン 1:クリア処理 2:コピー処理
        $opt = array(1, 2);
        $extra = "onClick=\"disCmb();\"";
        $model->field["SHORI"] = $model->field["SHORI"] ? $model->field["SHORI"] : '1';
        $radioArray = knjCreateRadio($objForm, "SHORI", $model->field["SHORI"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //総学等も含む
        $extra = (strlen($model->field["SOUGAKU"])) ? "checked " : "";
        $arg["data"]["SOUGAKU"] = knjCreateCheckBox($objForm, "SOUGAKU", "on", $extra);

        //評定換算する
        $extra = strlen($model->field["CONVERT"]) ? "checked " : "";
        $arg["data"]["CONVERT"] = knjCreateCheckBox($objForm, "CONVERT", "on", $extra);

        //コピー元になる評価コンボ作成
        $query = "";
        $extra = $model->field["SHORI"] == "1" ? "disabled" : "";
        testCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], $extra, 1, $model);

        //仮評定フラグラジオ 1:仮評定 2:本評定(学年評定)・・・本評定はNULLで更新する
        $opt = array(1, 2);
        $extra  = $model->field["SHORI"] == "1" ? " disabled" : "";
        $model->field["KARI_DIV"] = $model->field["KARI_DIV"] ? $model->field["KARI_DIV"] : '1';
        $radioArray = knjCreateRadio($objForm, "KARI_DIV", $model->field["KARI_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        if ($model->field["COPY_SAKI_DIV"] == "2") {
            $arg["showFlg"] = "1";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd214cForm1.html", $arg); 
    }
}

//コピー元になる評価コンボ作成
function testCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
    //学期マスタ
    $optSem = array();
    $query = knjd214cQuery::getSemester();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optSem[$row["VALUE"]] = $row["LABEL"];
    }
    $result->free();
    //文言
    $arg["data"]["TARGET_NAME"] = ($model->field["COPY_SAKI_DIV"] == "1") ? $optSem[$model->field["SEMESTER"]]."末評価" : "学年評定";
/***
    //評価の算出方法
    if ($model->calcMethod == "3") {
        $calcStr = "(切り捨て)";
    } else if ($model->calcMethod == "2") {
        $calcStr = "(切り上げ)";
    } else {
        $calcStr = "(四捨五入)";
    }
***/
    //テスト項目マスタ
    $opt = array();
    foreach($optSem as $sem => $semName) {
        if ($model->Properties["useSiteiGakkibunHyoukaAvg"] == '1' && $sem >= 3) continue;
        $query = knjd214cQuery::getTestItem($model, $sem);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field["COPY_SAKI_DIV"] == "1" && $sem != $model->field["SEMESTER"]) continue;
            $opt[] = array('label' => $sem .$row["VALUE"] ."：" .$semName .$row["LABEL"],
                           'value' => $sem .$row["VALUE"]);
        }
        $result->free();
        if ($model->field["COPY_SAKI_DIV"] == "1") continue;
        if ($model->field["COPY_SAKI_DIV"] == "2" && $sem == "9") continue;
        //コピー元が『9900：学期評価』のみ、指定学期分の平均をコピーする(例．１・２学期評価の平均)
        if ($model->Properties["useSiteiGakkibunHyoukaAvg"] == '1') {
            $avgName = "";
            $seq = "";
            for ($s = 1; $s <= $sem; $s++) {
                $avgName .= $seq .$optSem[$s];
                $seq = "/";
            }
            $avgName .= (1 < $sem) ? "の平均" : "";
            $opt[] = array('label' => $sem ."9900" ."：" .$avgName,
                           'value' => $sem ."9900");
        } else {
            $opt[] = array('label' => $sem ."9900" ."：" .$semName,
                           'value' => $sem ."9900");
        }
    }

    $value = ($value == "") ? CTRL_SEMESTER ."9900" : $value;

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "GRADE") {
            $opt[] = array('label' => sprintf("%d", $row["VALUE"]) . "学年",
                           'value' => $row["VALUE"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == "GRADE") {
        $opt[] = array('label' => '全学年', 'value' => '999');
        if ($value == '999') $value_flg = true;
    }

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //対象者(左のリスト）
    $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
    //クラス一覧
    $leftList = $rightList = array();
    $result = $db->query(knjd214cQuery::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $array)) {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $rightList, $extra, 8);

    //出力対象作成
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $leftList, $extra, 8);

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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //実行ボタン
    $arg["button"]["btn_exe"] = knjCreateBtn($objForm, "btn_exe", "実 行", "onclick=\"return doSubmit();\"");
    //閉じるボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}

//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model) {
    //履歴一覧
    $count = 0;
    $query = knjd214cQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //削除欄
        $delKey = $row["CALC_DATE"] . "," . $row["CALC_TIME"];
        $extra = "";
        $multi = "1";
        $row["DEL_CHECK"] = knjCreateCheckBox($objForm, "DEL_CHECK", $delKey, $extra, $multi);

        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        //処理クラス
        if (strlen($row["SELECT_HR_CLASS"])) {
            $array = explode(",", $row["SELECT_HR_CLASS"]);
            $nameList = "";
            $seq = "";
            foreach ($array as $key => $val) {
                $name = $db->getOne(knjd214cQuery::getRirekiHrName($row["YEAR"], $row["SEMESTER"], $val));
                $nameList .= $seq . $name;
                $seq = "<BR>";
            }
            $row["SELECT_HR_CLASS"] = $nameList;
        }
        $row["SHORI_DIV"] = $row["SHORI_DIV"] == "1" ? "クリア" : "コピー";
        //コピー元になる評価
        if (strlen($row["MOTO_TESTCD"])) {
            $sem = substr($row["MOTO_TESTCD"], 0, 1);
            $kindItem = substr($row["MOTO_TESTCD"], 1, 4);
            //コピー元が『9900：学期評価』のみ、指定学期分の平均をコピーする(例．１・２学期評価の平均)
            if ($model->Properties["useSiteiGakkibunHyoukaAvg"] == '1' && $sem != "9" && $kindItem == "9900") {
                $avgName = "";
                $seq = "";
                for ($s = 1; $s <= $sem; $s++) {
                    $avgName .= $seq .$db->getOne(knjd214cQuery::getRirekiSemesterName($row["YEAR"], $s));
                    $seq = "/";
                }
                $avgName .= (1 < $sem) ? "の平均" : "";
                $row["MOTO_TESTCD"] = $avgName;
            } else if ($sem != "9" && $kindItem == "9900") {
                $row["MOTO_TESTCD"] = $db->getOne(knjd214cQuery::getRirekiSemesterName($row["YEAR"], $sem));
            } else {
                $row["MOTO_TESTCD"] = $db->getOne(knjd214cQuery::getRirekiTestName($model, $row["YEAR"], $row["MOTO_TESTCD"]));
            }
        } else {
            $row["MOTO_TESTCD"] = "";
        }
        //評定フラグ
        if ($row["KARI_DIV"] == "1") {
            $row["KARI_DIV"] = "仮";
        } else if ($row["KARI_DIV"] == "2") {
            $row["KARI_DIV"] = "本";
        } else {
            $row["KARI_DIV"] = "";
        }
        $row["CONVERT_FLG"] = strlen($row["CONVERT_FLG"]) ? "レ" : "";
        $row["SOUGAKU_FLG"] = strlen($row["SOUGAKU_FLG"]) ? "レ" : "";
        $arg['data2'][] = $row;
        $count++;
    }
    $result->free();
    //実行履歴データ削除ボタン
    $extra  = "onclick=\"return btn_submit('del_rireki');\"";
    $extra .= (0 < $count) ? "" : " disabled";
    $arg["button"]["btn_del_rireki"] = knjCreateBtn($objForm, "btn_del_rireki", "実行履歴データ削除", $extra);
}
?>
