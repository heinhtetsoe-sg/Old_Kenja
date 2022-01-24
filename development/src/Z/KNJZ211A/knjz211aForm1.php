<?php

require_once('for_php7.php');

class knjz211aForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz211aindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //観点評価区分コンボ作成
        $query = knjz211aQuery::getDiv($model);
        $result = $db->query($query);
        $exists = $result->fetchRow(DB_FETCHMODE_ASSOC);
        if($exists) {
            $extra = "onchange=\"return btn_submit('change');\"";
            makeCmb($objForm, $arg, $db, $query, "DIV", $model->field["DIV"], $extra, 1);
        } else {
            $opt = array();
            $opt[] = array("label" => "1：学年観点評価", "value" =>"1");
            if ($model->field["DIV"] == "") {
                $model->field["DIV"] = $opt[0]["value"];
            }
            $extra = "onChange=\"return btn_submit('change');\"";
            $arg["DIV"] = knjCreateCombo($objForm, "DIV", $model->field["DIV"], $opt, $extra, 1);
        }

        $arg["DISPLAY_VIEWCD"] = true;
        if($model->field["DIV"] == "2") {
            $arg["DISPLAY_VIEWCD"] = false;
        }

        //学年コンボ作成
        $query = knjz211aQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, 1);

        //校種取得
        $model->schoolKind = $db->getOne(knjz211aQuery::getSchoolKind($model));

        //教科コンボ作成
        $query = knjz211aQuery::getClassMst($model);
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank");

        $disabled = ($model->field["CLASSCD"] == 'all')?' disabled = "disabled"':'';

        //科目コンボ作成
        $query = knjz211aQuery::getSubclassMst($model);
        $extra = "onchange=\"return btn_submit('change')\" ".$disabled;
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //観点コンボ作成
        $query = knjz211aQuery::getViewcd($model);
        $extra = "onchange=\"return btn_submit('change')\" ".$disabled;
        makeCmb($objForm, $arg, $db, $query, "VIEWCD", $model->field["VIEWCD"], $extra, 1, "blank");
        makeCmb2($objForm, $arg, $db, $query, "VIEWCD2", $model->field["VIEWCD2"], $extra, 1, $model->field["VIEWCD"], "blank");

        //学期コンボ
        $query = knjz211aQuery::getSemester();
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, "");

        //段階値数取得
        $countAssess = $db->getOne(knjz211aQuery::selectCountQuery($model));

        //条件が変更されたとき、初期値を取得
        if ($model->cmd == "change" || $model->cmd == "reset") {
            $model->field["MAX_ASSESSLEVEL"] = ($countAssess > 0) ? $countAssess : "";
        }
        $extra = "style=\"text-align: center\" onblur=\"this.value=NumCheck(this.value)\";";
        $arg["MAX_ASSESSLEVEL"] = knjCreateTextBox($objForm, $model->field["MAX_ASSESSLEVEL"], "MAX_ASSESSLEVEL", 1, 1, $extra);

        $model->data = array();
        if ($model->field["MAX_ASSESSLEVEL"]) {
            $counter = 0;
            //一覧表示
            $result = $db->query(knjz211aQuery::selectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //段階値テキストボックス
                if (!isset($model->warning)) {
                    $value = ($countAssess == 0) ? "" : $row["ASSESSLEVEL"];
                } else {
                    $value = $model->fields["ASSESSLEVEL"][$counter];
                }
                $extra = "style=\"text-align: center\" onblur=\"this.value=toInteger(this.value)\";";
                $row["ASSESSLEVEL"] = knjCreateTextBox($objForm, $value, "ASSESSLEVEL-".$counter, 4, 2, $extra);

                //下限値テキストボックス
                if ($row["ROW_NUM"] != '1') {
                    $extra = "style=\"text-align: right\" onblur=\"isNumb(this, ".($row["ROW_NUM"] -1).");\"";
                    $value = (!isset($model->warning)) ? $row["ASSESSLOW"] : $model->fields["ASSESSLOW"][$counter];
                    $row["ASSESSLOW"] = knjCreateTextBox($objForm, $value, "ASSESSLOW-".$counter, 4, 2, $extra);
                } else {
                    $row["ASSESSLOW"] = '0';
                }

                //記号テキストボックス
                $extra = "style=\"text-align: center\"";
                if ($countAssess == 0) {
                    $value = "";
                } else {
                    $value = (!isset($model->warning)) ? $row["ASSESSMARK"] : $model->fields["ASSESSMARK"][$counter];
                }
                $row["ASSESSMARK"] = knjCreateTextBox($objForm, $value, "ASSESSMARK-".$counter, 4, 4, $extra);

                //上限値の表示
                if ($counter == 0 ) {
                    $row["ASSESSHIGH"] = "100";
                    $row["ASSESSHIGHTEXT"]  = "<span id=\"ASSESSHIGH_ID";
                    $row["ASSESSHIGHTEXT"] .= $row["ROW_NUM"];
                    $row["ASSESSHIGHTEXT"] .= "\">";
                    $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                    $row["ASSESSHIGHTEXT"] .= "</span>";
                } else {
                    if (isset($model->warning)) {
                        $row["ASSESSHIGH"] = $model->fields["ASSESSHIGH"][$counter];
                    }
                    $row["ASSESSHIGHTEXT"]  = "<span id=\"ASSESSHIGH_ID";
                    $row["ASSESSHIGHTEXT"] .= $row["ROW_NUM"];
                    $row["ASSESSHIGHTEXT"] .= "\">";
                    $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                    $row["ASSESSHIGHTEXT"] .= "</span>";
                }
                //段階値の上限値をhiddenで保持
                knjCreateHidden($objForm, "Assesshighvalue".$row["ROW_NUM"], $row["ASSESSHIGH"]);

                //上限値を配列で取得
                $model->data["ASSESSHIGH"][] = $row["ASSESSHIGH"];
                $model->data["ROW_NUM"][] = $row["ROW_NUM"];
                if($model->field["DIV"] != "2") {
                    $row["UNIT"] = "％";
                }
                $counter++;
                $arg["data"][] = $row;
            }
            $result->free();
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJZ211A");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz211aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    if($name == 'CLASSCD'){
        $opt[] = array('label' => "--基本--", 'value' => "all");
        if ($value == 'all') $value_flg = true;
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {
    //確定ボタン
    $extra = "onclick=\"return btn_submit('kakutei');\"";
    $arg["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);
    //更新ボタンを作成する
    $disable = ($model->field["MAX_ASSESSLEVEL"] > 0) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    $disabled = ($model->field["CLASSCD"] == 'all')?' disabled = "disabled"':'';
    //右からコピーボタンを作成する
    $extra = "onclick=\"return btn_submit('copy');\"" . $disabled;
    $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "右からコピー", $extra);
}

//コンボ作成
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size,$viewCd, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if($row['VALUE']!=$viewCd){
            $opt[] = array('label' => $row["LABEL"],
                'value' => $row["VALUE"]);

            if ($value == $row["VALUE"]) $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
