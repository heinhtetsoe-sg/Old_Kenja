<?php

require_once('for_php7.php');


class knje370Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje370Form1", "POST", "knje370index.php", "", "knje370Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knje370Query::getYear();
        $extra = "onChange=\"return btn_submit('changeYear'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //学期取得
        if ($model->field["YEAR"] == CTRL_YEAR) {
            $semester = CTRL_SEMESTER;
        } else {
            $semester = $db->getOne(knje370Query::getMaxSemester($model));
        }
        knjCreateHidden($objForm, "SEMESTER", $semester);

        //既卒
        $kisotsu = $db->getOne(knje370Query::checkGradCnt($model, $semester));

        //帳票ラジオボタン 1:進学 2:就職
        $model->field["OUT_DIV"] = $model->field["OUT_DIV"] ? $model->field["OUT_DIV"] : '1';
        $opt_outdiv = array(1, 2);
        $extra2 = " onclick=\"btn_submit('')\"";
        $extra = array("id=\"OUT_DIV1\"".$extra2, "id=\"OUT_DIV2\"".$extra2);
        $radioArray = knjCreateRadio($objForm, "OUT_DIV", $model->field["OUT_DIV"], $extra, $opt_outdiv, get_count($opt_outdiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力指定
        $opt = array(1, 2);
        $model->field["DATA_DIV"] = ($model->field["DATA_DIV"] == "") ? "1" : $model->field["DATA_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATA_DIV{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATA_DIV", $model->field["DATA_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //画面サイズ切替
        $arg["ALLWIDTH"] = ($model->field["DATA_DIV"] == "2") ? "650" : "550";

        //生徒毎に出力する
        $extra  = $model->field["KAIPAGE"] == "1" ? " checked " : "";
        $extra .= " id=\"KAIPAGE\" ";
        $arg["data"]["KAIPAGE"] = knjCreateCheckBox($objForm, "KAIPAGE", "1", $extra);

        if ($model->field["DATA_DIV"] == "1") {
            $arg["data"]["GRADE_HR"] = "クラス一覧";
        } else {
            //クラスコンボ
            $query = knje370Query::getAuth($model, $semester, $kisotsu);
            $extra = "onChange=\"btn_submit('changeGradeHr')\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR", $model->field["GRADE_HR"], $extra, 1);
        }

        //対象（合否・進路先）コンボ
        $extra = "";
        $opt = array();
        $opt[] = array('label' => "（全て）", 'value' => "E000-ALL");
        $query = knje370Query::getNamespare("E005");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = "（";
            $value = "";
            $lSep  = "";
            $query = knje370Query::getNameMst2("E005", $row["VALUE"]);
            $result2 = $db->query($query);
            while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $label = $label.$lSep.$row2["LABEL"];
                $value = $row2["NAMESPARE"];
                $lSep  = "・";
            }
            $result2->free();
            $label = $label."）";
            $opt[] = array('label' => $label,
                           'value' => "E005-MIX-".$value);
        }
        $result->free();
        $query = knje370Query::getGouhi($model);
//        makeCmb($objForm, $arg, $db, $query, "GOUHI", $model->field["GOUHI"], $extra, 1, "all");
        makeCmb2($objForm, $arg, $opt, $db, $query, "GOUHI", $model->field["GOUHI"], $extra, 1);

        //クラス一覧リスト作成する
        if ($model->field["DATA_DIV"] == "1") {
            $query = knje370Query::getAuth($model, $semester, $kisotsu);
        } else {
            $query = knje370Query::getStudentLeft($model, $semester);
            $result = $db->query($query);
            $sentakuZumi = explode("','", $model->selectdata);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rowLeft[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
            }
            $result->free();

            if ($model->field["GRADE_HR"] == "ZZZZZ") {
                $query = knje370Query::getGradStudent($model, $semester);
            } else {
                $query = knje370Query::getStudent($model, $semester);
            }
        }
        $result = $db->query($query);
        $sentakuZumi = explode("','", $model->selectdata);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->cmd == "changeGradeHr") {
                list ($grad_flg, $ghratt, $schregno) = explode('-', $row["VALUE"]);
                if (!in_array($schregno, $sentakuZumi)) {
                    $rowRight[] = array('label' => $row["LABEL"],
                                        'value' => $row["VALUE"]);
                }
            } else {
                if ($model->field["DATA_DIV"] == "1") {
                    if (!in_array($row["VALUE"], $sentakuZumi)) {
                        $rowRight[] = array('label' => $row["LABEL"],
                                            'value' => $row["VALUE"]);
                    } else {
                        $rowLeft[]  = array('label' => $row["LABEL"],
                                            'value' => $row["VALUE"]);
                    }
                } else {
                    if ($model->warning) {
                        list ($grad_flg, $ghratt, $schregno) = explode('-', $row["VALUE"]);
                        if (!in_array($schregno, $sentakuZumi)) {
                            $rowRight[] = array('label' => $row["LABEL"],
                                                'value' => $row["VALUE"]);
                        }
                    } else {
                        $rowRight[] = array('label' => $row["LABEL"],
                                            'value' => $row["VALUE"]);
                    }
                }
            }
        }
        $result->free();

        $value = "";
        $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, isset($rowRight)?$rowRight:array(), $extra, 15);

        //出力対象クラスリストを作成する
        $value = "";
        $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $value, isset($rowLeft)?$rowLeft:array(), $extra, 15);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);


        //項目一覧リストTOリスト
        $opt_type = $opt_type_left = $opt_type_right = array();
        $query = knje370Query::getKubun();
        $result = $db->query($query);
        while ($g_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_type[] = $g_row;
        }

        $selecttypedata = ($model->selecttypedata != "") ? explode(",",$model->selecttypedata) : array();
        for ($i = 0; $i < get_count($opt_type); $i++) {
            if (!in_array($opt_type[$i]["VALUE"],$selecttypedata)) continue;
            $opt_type_left[]  = array("label" => $opt_type[$i]["LABEL"],
                                 "value" => $opt_type[$i]["VALUE"]);
        }
        for ($i = 0; $i < get_count($opt_type); $i++) {
            if (in_array($opt_type[$i]["VALUE"],$selecttypedata)) continue;
            $opt_type_right[] = array("label" => $opt_type[$i]["LABEL"],
                                 "value" => $opt_type[$i]["VALUE"]);
        }

        $disflg = "";
        if ($model->field["OUT_DIV"] == '2') {
            $disflg = " disabled='disabled' ";
        }
        //出力項目選択一覧リスト
        $extra = "id=\"CATEGORY_NAME_TYPE\" multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1_type('left')\"".$disflg;
        $arg["data"]["CATEGORY_NAME_TYPE"] = knjcreateCombo($objForm, "CATEGORY_NAME_TYPE", "", isset($opt_type_right)?$opt_type_right:array(), $extra, 6);

        //出力対象一覧リスト
        $extra = "id=\"CATEGORY_SELECTED_TYPE\" multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1_type('right')\"".$disflg;
        $arg["data"]["CATEGORY_SELECTED_TYPE"] = knjcreateCombo($objForm, "CATEGORY_SELECTED_TYPE", "", isset($opt_type_left)?$opt_type_left:array(), $extra, 6);

        //対象取消ボタン（全部）
        $extra = "id=\"btn_rights_type\" style=\"height:20px;width:40px\" onclick=\"moves_type('right');\"".$disflg;
        $arg["button"]["btn_rights_type"] = knjcreateBtn($objForm, "btn_rights_type", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "id=\"btn_lefts_type\" style=\"height:20px;width:40px\" onclick=\"moves_type('left');\"".$disflg;
        $arg["button"]["btn_lefts_type"] = knjcreateBtn($objForm, "btn_lefts_type", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "id=\"btn_right1_type\" style=\"height:20px;width:40px\" onclick=\"move1_type('right');\"".$disflg;
        $arg["button"]["btn_right1_type"] = knjcreateBtn($objForm, "btn_right1_type", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "id=\"btn_left1_type\" style=\"height:20px;width:40px\" onclick=\"move1_type('left');\"".$disflg;
        $arg["button"]["btn_left1_type"] = knjcreateBtn($objForm, "btn_left1_type", "＜", $extra);

        knjCreateHidden($objForm, "selecttypedata");

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //ＣＳＶボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE370");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "SELECTDATA_TYPE_CNT", get_count($opt_type));

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje370Form1.html", $arg); 
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $all="") {
    $opt = array();
    if ($all) $opt[] = array('label' => "（全て）", 'value' => "E000-ALL");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成2
function makeCmb2(&$objForm, &$arg, $opt, $db, $query, $name, &$value, $extra, $size) {
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
