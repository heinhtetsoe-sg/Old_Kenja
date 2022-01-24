<?php

require_once('for_php7.php');

class knjd214uForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd214uForm1", "POST", "knjd214uindex.php", "", "knjd214uForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度・学期
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $arg["data"]["CTRL_SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //文言
        $arg["data"]["TARGET_NAME"] = "学年評定";

        //学期コンボ
        $query = knjd214uQuery::getSemester("seme");
        $extra = "onChange=\"return btn_submit('knjd214u');\"";
        $semArray = makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $query = knjd214uQuery::getSelectGrade($model);
        $extra = "onChange=\"return btn_submit('knjd214u');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //対象者ラジオボタン 1:クラス 2:個人
        $opt = array(1, 2);
        $extra = "onClick=\"return btn_submit('knjd214u');\"";
        $model->field["CHOICE"] = $model->field["CHOICE"] ? $model->field["CHOICE"] : '1';
        $radioArray = knjCreateRadio($objForm, "CHOICE", $model->field["CHOICE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //対象者により処理が変わる
        if ($model->field["CHOICE"] == "2") {
            $arg["gr_class"] = "ON";
            //クラスコンボボックス
            $extra = "onChange=\"return btn_submit('knjd214u');\"";
            $query = knjd214uQuery::getAuthClass($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        }

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //科目一覧リストToリスト作成
        makeListToListKamoku($objForm, $arg, $db, $model);

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
        $extra = (strlen($model->field["CONVERT"]) || $model->cmd == "") ? "checked " : "";
        $arg["data"]["CONVERT"] = knjCreateCheckBox($objForm, "CONVERT", "on", $extra);

        //コピー元になる評価コンボ作成
        $query = knjd214uQuery::getTestItem($model);
        $extra = $model->field["SHORI"] == "1" ? "disabled" : "";
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], $extra, 1);

        //仮評定フラグラジオ 1:仮評定 2:本評定(学年評定)・・・本評定はNULLで更新する
        $opt = array(1, 2);
        $extra  = "onClick=\"disCmbKari();\"";
        $extra .= $model->field["SHORI"] == "1" ? " disabled" : "";
        $model->field["KARI_DIV"] = $model->field["KARI_DIV"] ? $model->field["KARI_DIV"] : '1';
        $radioArray = knjCreateRadio($objForm, "KARI_DIV", $model->field["KARI_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //欠課時数オーバーは未履修にする
        $extra = (strlen($model->field["KEEKA_OVER"])) ? "checked " : "";
        $extra .= ($model->field["SHORI"] == "2" && $model->field["KARI_DIV"] == "2") ? "" : " disabled";
        $arg["data"]["KEEKA_OVER"] = knjCreateCheckBox($objForm, "KEEKA_OVER", "on", $extra);

        //追指導は参照する
        $extra = (strlen($model->field["SIDOU"])) ? "checked " : "";
        $arg["data"]["SIDOU"] = knjCreateCheckBox($objForm, "SIDOU", "on", $extra);

        //追指導表示
        $query = knjd214uQuery::getSidouInputCount($model);
        if (0 < $db->getOne($query)) {
            $arg["useSidou"] = 1;
        }

        //仮評定の保存先コンボ
        $query = knjd214uQuery::getTestItem($model, "kari");
        $extra = ($model->field["SHORI"] == "1" || $model->field["KARI_DIV"] == "2") ? " disabled" : "";
        makeCmb($objForm, $arg, $db, $query, "KARI_TESTCD", $model->field["KARI_TESTCD"], $extra, 1, "blank");

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
        View::toHTML($model, "knjd214uForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == "GRADE") {
        $opt[] = array('label' => '全学年', 'value' => '999');
        if ($value == '999') $value_flg = true;
    }

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else if ($name == "TESTKINDCD") {
        $maxNo = 0 < get_count($opt) ? get_count($opt) - 1 : 0;
        $value = ($value && $value_flg) ? $value : $opt[$maxNo]["value"]; //初期値：学年評価
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    if ($name == "SEMESTER") {
        return $opt;
    }
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //対象者(左のリスト）
    $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
    //クラス一覧
    $leftList = $rightList = array();
    if ($model->field["CHOICE"] == "1") {
        $arg["CHANGENAME"] = "クラス";
        $query = knjd214uQuery::getAuthClass($model);
    } else {
        $arg["CHANGENAME"] = "生徒";
        $query = knjd214uQuery::getAuthStudent($model);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $array)) {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    $chdt = $model->field["CHOICE"];

    //クラス一覧作成
    $extra = "multiple style=\"width:275px\" width=\"275px\" ondblclick=\"move1('left',{$chdt})\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $rightList, $extra, 8);

    //出力対象作成
    $extra = "multiple style=\"width:275px\" width=\"275px\" ondblclick=\"move1('right',{$chdt})\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $leftList, $extra, 8);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left',{$chdt});\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left',{$chdt});\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right',{$chdt});\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right',{$chdt});\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//科目一覧リストToリスト作成
function makeListToListKamoku(&$objForm, &$arg, $db, $model) {
    //対象者(左のリスト）
    $array  = (strlen($model->selectdataKamoku) ? explode(",", $model->selectdataKamoku) : array());
    //科目一覧
    $leftList = $rightList = array();
    $query = knjd214uQuery::getListKamoku($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $array)) {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    $chdt = "0";

    //科目一覧作成
    $extra = "multiple style=\"width:275px\" width=\"275px\" ondblclick=\"move1Kamoku('left',{$chdt})\"";
    $arg["data"]["KAMOKU_NAME"] = knjCreateCombo($objForm, "KAMOKU_NAME", "", $rightList, $extra, 8);

    //出力対象作成
    $extra = "multiple style=\"width:275px\" width=\"275px\" ondblclick=\"move1Kamoku('right',{$chdt})\"";
    $arg["data"]["KAMOKU_SELECTED"] = knjCreateCombo($objForm, "KAMOKU_SELECTED", "", $leftList, $extra, 8);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"movesKamoku('left',{$chdt});\"";
    $arg["button"]["btn_kamoku_lefts"] = knjCreateBtn($objForm, "btn_kamoku_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1Kamoku('left',{$chdt});\"";
    $arg["button"]["btn_kamoku_left1"] = knjCreateBtn($objForm, "btn_kamoku_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1Kamoku('right',{$chdt});\"";
    $arg["button"]["btn_kamoku_right1"] = knjCreateBtn($objForm, "btn_kamoku_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"movesKamoku('right',{$chdt});\"";
    $arg["button"]["btn_kamoku_rights"] = knjCreateBtn($objForm, "btn_kamoku_rights", ">>", $extra);
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
    knjCreateHidden($objForm, "selectdata2");
    knjCreateHidden($objForm, "selectdataKamoku");
}
//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model) {
    //履歴一覧
    $count = 0;
    $query = knjd214uQuery::getListRireki($model);
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
                if (strlen($val) == 8) {
                    $name = $val;//学籍番号
                } else {
                    $name = $db->getOne(knjd214uQuery::getRirekiHrName($row["YEAR"], $row["SEMESTER"], $val));
                }
                $nameList .= $seq . $name;
                $seq = "<BR>";

                break;
            }
            $row["SELECT_HR_CLASS"] = $nameList . (1 < get_count($array) ? "他" : "");
        }
        //処理科目
        if (strlen($row["SELECT_SUBCLASS"])) {
            $array = explode(",", $row["SELECT_SUBCLASS"]);
            $nameList = "";
            $seq = "";
            foreach ($array as $key => $val) {
                $name = $val;//科目コード
                $nameList .= $seq . $name;
                $seq = "<BR>";

                break;
            }
            $row["SELECT_SUBCLASS"] = $nameList . (1 < get_count($array) ? "他" : "");
        }
        $row["SHORI_DIV"] = $row["SHORI_DIV"] == "1" ? "クリア" : "コピー";
        $row["MOTO_TESTCD"] = strlen($row["MOTO_TESTCD"]) ? $db->getOne(knjd214uQuery::getRirekiTestName($row["YEAR"], $row["MOTO_TESTCD"])) : "";
        $row["KARI_TESTCD"] = strlen($row["KARI_TESTCD"]) ? $db->getOne(knjd214uQuery::getRirekiTestName($row["YEAR"], $row["KARI_TESTCD"])) : "";
        if ($row["KARI_DIV"] == "1") {
            $row["KARI_DIV"] = "仮";
        } else if ($row["KARI_DIV"] == "2") {
            $row["KARI_DIV"] = "本";
        } else {
            $row["KARI_DIV"] = "";
        }
        $row["CONVERT_FLG"] = strlen($row["CONVERT_FLG"]) ? "レ" : "";
        $row["SIDOU_FLG"] = strlen($row["SIDOU_FLG"]) ? "レ" : "";
        $row["SOUGAKU_FLG"] = strlen($row["SOUGAKU_FLG"]) ? "レ" : "";
        $row["KEEKA_OVER_FLG"] = strlen($row["KEEKA_OVER_FLG"]) ? "レ" : "";
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
