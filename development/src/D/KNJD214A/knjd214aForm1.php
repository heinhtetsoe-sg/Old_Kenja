<?php

require_once('for_php7.php');

class knjd214aForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd214aForm1", "POST", "knjd214aindex.php", "", "knjd214aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //実行確認表示
        if ($model->cmd == "confirm" && $model->Properties["useProvFlg"] == '1') {
            $msg = "成績データ（コピー先）は、\\n";
            $query = knjd214aQuery::getConfirmMsgSql($model, CTRL_YEAR, CTRL_SEMESTER);
            $confirmRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (0 < $confirmRow["VALUE_CNT"]) {
                if (0 < $confirmRow["PROV_FLG_CNT2"] && 0 < $confirmRow["PROV_FLG_CNT1"]) {
                    $msg .= "【本評定と仮評定が混在】です。";
                } elseif (0 < $confirmRow["PROV_FLG_CNT2"]) {
                    $msg .= "【本評定】です。";
                } elseif (0 < $confirmRow["PROV_FLG_CNT1"]) {
                    $msg .= "【仮評定】です。";
                }
            } else {
                $msg .= "【評定なし】です。";
            }
            $arg["confirm_msg"] = "confirmMsg('$msg');";
        }

        //ログイン年度・学期
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $arg["data"]["CTRL_SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //学年コンボ
        $query = knjd214aQuery::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "onChange=\"return btn_submit('knjd214a');\"", 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //クリア処理とコピー処理のラジオボタン 1:クリア処理 2:コピー処理
        $model->field["SHORI"] = $model->field["SHORI"] ? $model->field["SHORI"] : '1';
        $opt_shori = array(1, 2);
        $extra = array("id=\"SHORI1\" onClick=\"disCmb();\"", "id=\"SHORI2\" onClick=\"disCmb();\"");
        $radioArray = knjCreateRadio($objForm, "SHORI", $model->field["SHORI"], $extra, $opt_shori, get_count($opt_shori));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //総学等も含む
        $extra  = (strlen($model->field["SOUGAKU"])) ? "checked " : "";
        $extra .= " id=\"SOUGAKU\"";
        $arg["data"]["SOUGAKU"] = knjCreateCheckBox($objForm, "SOUGAKU", "on", $extra);

        //評定換算する
        $extra  = (strlen($model->field["CONVERT"]) || $model->cmd == "") ? "checked " : "";
        $extra .= " id=\"CONVERT\"";
        $arg["data"]["CONVERT"] = knjCreateCheckBox($objForm, "CONVERT", "on", $extra);

        //コピー元になる評価コンボ作成
        $query = "";
        $extra = $model->field["SHORI"] == "1" ? "disabled" : "";
        testCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], $extra, 1, $model);

        //仮評定フラグ対応
        knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
        if ($model->Properties["useProvFlg"] == '1') {
            $arg["useProvFlg"] = "1";
            //仮評定フラグラジオ 1:仮評定 2:本評定(学年評定)・・・本評定はNULLで更新する
            $opt = array(1, 2);
            $disabled = $model->field["SHORI"] == "1" ? " disabled" : "";
            $extra = array("id=\"KARI_DIV1\"".$disabled, "id=\"KARI_DIV2\"".$disabled);
            $model->field["KARI_DIV"] = $model->field["KARI_DIV"] ? $model->field["KARI_DIV"] : '1';
            $radioArray = knjCreateRadio($objForm, "KARI_DIV", $model->field["KARI_DIV"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
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
        View::toHTML($model, "knjd214aForm1.html", $arg);
    }
}

//コピー元になる評価コンボ作成
function testCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
{
    //学期マスタ
    $optSem = array();
    $query = knjd214aQuery::getSemester();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optSem[$row["VALUE"]] = $row["LABEL"];
    }
    $result->free();
    //テスト項目マスタ
    $opt = array();
    foreach ($optSem as $sem => $semName) {
        $query = knjd214aQuery::getTestItem($model, $sem);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $sem .$row["VALUE"] ."：" .$semName .$row["LABEL"],
                           'value' => $sem .$row["VALUE"]);
        }
        $result->free();
        //下記の学校の時は、コピー元になる評価で「9-9900（SCORE）」を選択できるようにする。
        //中京（修正要望）・智辯・鳥取・熊本
        if ($sem == "9" && $model->schoolName != 'chukyo' && $model->schoolName != 'CHIBEN' && $model->schoolName != 'tottori' && $model->schoolName != 'kumamoto') {
            continue;
        }
        $opt[] = array('label' => $sem ."9900" ."：" .$semName,
                       'value' => $sem ."9900");
        //熊本の時、コピー元になる評価に「１・２学期の平均(19900,29900の平均)」を追加
        if ($model->schoolName == 'kumamoto' && $sem == '2') {
            $avgName  = "";
            $avgName .= $optSem["1"];
            $avgName .= "・";
            $avgName .= $optSem["2"];
            $avgName .= "の平均";
            $opt[] = array('label' => $model->motoTestcdSem12Avg ."：" .$avgName,
                           'value' => $model->motoTestcdSem12Avg);
        }
    }

    $value = ($value == "") ? CTRL_SEMESTER ."9900" : $value;

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "")
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "") ? CTRL_SEMESTER : $value;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    if ($name == "GRADE") {
        $opt[] = array('label' => '全学年', 'value' => '999');
    }

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //対象者(左のリスト）
    $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
    //クラス一覧
    $leftList = $rightList = array();
    $result = $db->query(knjd214aQuery::getAuth($model));
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
    $arg["data"]["CLASS_NAME"] = createCombo($objForm, "CLASS_NAME", "", $rightList, $extra, 8);

    //出力対象作成
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = createCombo($objForm, "CLASS_SELECTED", "", $leftList, $extra, 8);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $arg["button"]["btn_exe"] = createBtn($objForm, "btn_exe", "実 行", "onclick=\"return doSubmit();\"");
    //閉じるボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("selectdata"));
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae(array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae(array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model)
{
    //履歴一覧
    $count = 0;
    $query = knjd214aQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //削除欄
        $delKey = $row["CALC_DATE"] . "," . $row["CALC_TIME"];
        $extra  = "onclick=\"chgBgcolor(this);\"";
        $multi = "1";
        $row["DEL_CHECK"] = knjCreateCheckBox($objForm, "DEL_CHECK", $delKey, $extra, $multi);

        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        //処理クラス
        if (strlen($row["SELECT_HR_CLASS"])) {
            $array = explode(",", $row["SELECT_HR_CLASS"]);
            $nameList = "";
            $seq = "";
            foreach ($array as $key => $val) {
                $name = $db->getOne(knjd214aQuery::getRirekiHrName($row["YEAR"], $row["SEMESTER"], $val));
                $nameList .= $seq . $name;
                $seq = "<BR>";
            }
            $row["SELECT_HR_CLASS"] = $nameList;
        }
        $row["SHORI_DIV"] = $row["SHORI_DIV"] == "1" ? "クリア" : "コピー";
        //熊本の時、コピー元になる評価に「１・２学期の平均(19900,29900の平均)」を追加
        if ($model->schoolName == 'kumamoto' && $row["MOTO_TESTCD"] == $model->motoTestcdSem12Avg) {
            $avgName  = "";
            $avgName .= $db->getOne(knjd214aQuery::getRirekiSemesterName($row["YEAR"], "1"));
            $avgName .= "・";
            $avgName .= $db->getOne(knjd214aQuery::getRirekiSemesterName($row["YEAR"], "2"));
            $avgName .= "の平均";
            $row["MOTO_TESTCD"] = $avgName;
        } else {
            $row["MOTO_TESTCD"] = strlen($row["MOTO_TESTCD"]) ? $db->getOne(knjd214aQuery::getRirekiTestName($model, $row["YEAR"], $row["MOTO_TESTCD"])) : "";
        }
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            if ($row["KARI_DIV"] == "1") {
                $row["KARI_DIV"] = "仮";
            } elseif ($row["KARI_DIV"] == "2") {
                $row["KARI_DIV"] = "本";
            } else {
                $row["KARI_DIV"] = "";
            }
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
