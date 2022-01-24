<?php

require_once('for_php7.php');

class knjh569Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh569Form1", "POST", "knjh569index.php", "", "knjh569Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjh569Query::getSemester();
        if ($model->field["SELECT_DIV"] == "2" || $model->field["SELECT_DIV"] == "3" ) {
            $extra = "onchange=\"return btn_submit('knjh569'), AllClearList();\"";
        } else {
            $extra = "onchange=\"return btn_submit('knjh569');\"";
        }
        if ($model->field["SEMESTER"] == "") $model->field["SEMESTER"] = CTRL_SEMESTER;
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //データ種別コンボ
        $query = knjh569Query::getDataDiv();
        $extra = "onchange=\"return btn_submit('knjh569')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYDIV", $model->field["PROFICIENCYDIV"], $extra, 1);

        //テスト名称コンボ
        $query = knjh569Query::getProName($model);
        $extra = "onchange=\"return btn_submit('knjh569')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYCD", $model->field["PROFICIENCYCD"], $extra, 1);

        //順位区分ラジオボタン 1:学年順位 2:クラス順位 3:コース順位
        $opt_div = array(1, 2, 3);
        $model->field["SELECT_DIV"] = ($model->field["SELECT_DIV"] == "") ? "1" : $model->field["SELECT_DIV"];
        $extra = array("id=\"SELECT_DIV1\" onclick=\"return btn_submit('knjh569')\"", "id=\"SELECT_DIV2\" onclick=\"return btn_submit('knjh569')\"", "id=\"SELECT_DIV3\" onclick=\"return btn_submit('knjh569')\"");
        $radioArray = knjCreateRadio($objForm, "SELECT_DIV", $model->field["SELECT_DIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //表示切替
        if ($model->field["SELECT_DIV"] == 2 || $model->field["SELECT_DIV"] == 3) $arg["class_course"] = '1';

        //高さ調整
        $arg["height"] = ($model->field["SELECT_DIV"] == 1) ? "200" : "50";

        //学年コンボ
        $query = knjh569Query::getGrade($model, $model->field["SEMESTER"]);
        if ($model->field["SELECT_DIV"] == "2" || $model->field["SELECT_DIV"] == "3") {
            $extra = "onchange=\"return btn_submit('chgGrade'), AllClearList();\"";
        } else {
            $extra = "onchange=\"return btn_submit('chgGrade');\"";
        }
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //実力科目コンボ
        $query = knjh569Query::getProfSubclassMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCY_SUBCLASS_CD", $model->field["PROFICIENCY_SUBCLASS_CD"], $extra, 1, "BLANK");

        //上位出力人数
        $model->field["PRINT_COUNT"] = $model->field["PRINT_COUNT"] == "" ? "50" : $model->field["PRINT_COUNT"];
        $extra  = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align: right;\" ";
        $arg["data"]["PRINT_COUNT"] = knjCreateTextBox($objForm, $model->field["PRINT_COUNT"], "PRINT_COUNT", 3, 3, $extra);

        if ($model->field["SELECT_DIV"] == "2" || $model->field["SELECT_DIV"] == "3") {
            //リストToリスト作成
            makeListToList($objForm, $arg, $db, $model);
        }

        $istosajosi = $db->getOne(knjh569Query::getZ010($model)) == "tosajoshi" ? true : false;
        $schoolKind = $db->getOne(knjh569Query::getSchoolKind($model));
        //基準点(表示のみ)
        //土佐女子の高校の場合とその他で処理を分ける。
        if ($istosajosi && $schoolKind == "H") {
            //2019年度1学期の登録データで、テストコードが選択した試験と同じものがあるか、チェック。
            $findflg = false;
            //学年順位を指定した場合のみ、チェック
            if ($model->field["SELECT_DIV"] == "1") {
                //2019年度の出力データのみが対象。年度違いで同じコードの場合があるのでチェック。
                if (CTRL_YEAR == "2019" && $model->field["SEMESTER"] == "1") {
                    $query = knjh569Query::getProficiencyMstDat($model, "2019");
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        if ($row["PROFICIENCYDIV"]."-".$row["PROFICIENCYCD"] == $model->field["PROFICIENCYDIV"]."-".$model->field["PROFICIENCYCD"]) {
                            $findflg = true;
                            break;
                        }
                    }
                }
            }
            //上記チェックで引っかかれば、RANK_DATA_DIVを変更する。
            if ($findflg) {
                $model->field["RANK_DATA_DIV"] = "01";
            } else {
                $model->field["RANK_DATA_DIV"] = "03";
            }
        } else {
            //土佐女子判定導入前の値
            $model->field["RANK_DATA_DIV"] = "01";
        }
        if ($model->field["RANK_DATA_DIV"] == "04") {
            $arg["data"]["JUNI"] = "傾斜総合点";
        } else if ($model->field["RANK_DATA_DIV"] == "03") {
            $arg["data"]["JUNI"] = "偏差値";
        } else if ($model->field["RANK_DATA_DIV"] == "02") {
            $arg["data"]["JUNI"] = "平均点";
        } else {
            $arg["data"]["JUNI"] = "総合点";
        }
        knjCreateHidden($objForm, "RANK_DATA_DIV", $model->field["RANK_DATA_DIV"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh569Form1.html", $arg); 
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //対象一覧リスト
    $optS = $optN = array();
    $query = ($model->field["SELECT_DIV"] == "2") ? knjh569Query::getHrClass($model) : knjh569Query::getCourse($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->warning && in_array($row["VALUE"], explode(',', $model->selectdata))) {
            $optS[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        } else {
            $optN[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $optN, $extra, 15);

    //出力対象一覧リスト
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $optS, $extra, 15);

    //一覧タイトル
    $arg["data"]["NAME_LIST"] = ($model->field["SELECT_DIV"] == "2") ? 'クラス一覧' : 'コース一覧';

    //対象選択ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "(全て出力)", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //CSVボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "CHANGE", $model->field["SELECT_DIV"]);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
