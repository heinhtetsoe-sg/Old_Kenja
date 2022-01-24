<?php

require_once('for_php7.php');

class knjz210eForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz210eindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期数取得 //単独メニュー時、使用しない
        $setNameCd = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $semester_count = $db->getOne(knjz210eQuery::getSemester($setNameCd));

        //観点評価段階値
        $opt = array();

        $query = knjz210eQuery::getNameMst("Z054");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }
        $result->free();

        //名称マスタ未設定の場合
        if (get_count($opt) == 0) {
            $opt[] = array("label" => "1：学年観点評価", "value" =>"1");
        }

        if ($model->field["DIV"] == "") {
            $model->field["DIV"] = $opt[0]["value"];
        }
        $extra = "onChange=\"return btn_submit('main');\"";
        $arg["DIV"] = knjCreateCombo($objForm, "DIV", $model->field["DIV"], $opt, $extra, 1);

        //学年コンボ作成
        $query = knjz210eQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, 1);

        //教科コンボ作成
        $query = knjz210eQuery::getClassMst("", $model->field["GRADE"], $model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank");

        //科目コンボ作成
        $query = knjz210eQuery::getSubclassMst($model->field["CLASSCD"], $model->field["GRADE"], $model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //観点コンボ作成
        if ($model->field["DIV"] === '1') {
            $arg["kanten"] = 1;
            $query = knjz210eQuery::getViewcd($model);
            $extra = "onchange=\"return btn_submit('main')\"";
            makeCmb($objForm, $arg, $db, $query, "VIEWCD", $model->field["VIEWCD"], $extra, 1, "blank");
        }

        //段階値の最大値取得
        $countAssess = $db->getOne(knjz210eQuery::selectCountQuery($model));
        $getCount = "";
        //既にデータがある場合
        if ($countAssess > 0 && $model->cmd == "main") {
            $model->field["MAX_ASSESSLEVEL"] = $countAssess;
        } else {
            //V_NAME_MST (D029)の確認
            $getCount = $db->getOne(knjz210eQuery::getAssesslevel("D029"));
            if ($getCount == 0) {
                $getCount = "3";
            }
            if ($model->cmd == "kakutei") {
                $model->field["MAX_ASSESSLEVEL"] = ($model->field["MAX_ASSESSLEVEL"] != "") ? $model->field["MAX_ASSESSLEVEL"] : $getCount;
            } else {
                $model->field["MAX_ASSESSLEVEL"] = $getCount;
            }
        }
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["MAX_ASSESSLEVEL"] = knjCreateTextBox($objForm, $model->field["MAX_ASSESSLEVEL"], "MAX_ASSESSLEVEL", 1, 1, $extra);

        //観点コード数取得
        $viewcd_count = $db->getOne(knjz210eQuery::getcountViewcd($model));

        //学期ごとにviewflgの数を確認
        $model->viewflg_check = "";
        for ($checksemester = 1; $checksemester <= $semester_count; $checksemester++) {
            (int)$model->viewflg_check += $db->getOne(knjz210eQuery::getViewflg($model, $checksemester));
        }

        //初期化
        $model->data = array();
        $counter = 0;
        //一覧表示
        $result = $db->query(knjz210eQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            
            //段階値
            if ($counter == 0 ) {
                $extra = "\" onblur=\"setAssesshigh(this, ".$row["NAMESPARE2"].", ".$model->viewflg_check.");\"";
            } else {
                $extra = "";
            }
            if ($countAssess == 0) {
                $value = $row["NAMESPARE2"];
            } else {
                $value = (!isset($model->warning)) ? $row["ASSESSLEVEL"] : $model->fields["ASSESSLEVEL"][$counter];
            }
            //比較用に段階値をセット
            $setrowAssessLevel = $value;
            $row["ASSESSLEVEL"] = knjCreateTextBox($objForm, $value, "ASSESSLEVEL-".$counter, 4, 2, $extra);

            //下限値のテキストボックス
            if ($row["NAMESPARE2"] != '1') {
                $extra = "\" onblur=\"isNumb(this, ".($row["NAMESPARE2"] -1).");\"";
                $value = (!isset($model->warning)) ? $row["ASSESSLOW"] : $model->fields["ASSESSLOW"][$counter];
                $row["ASSESSLOW"] = knjCreateTextBox($objForm, $value, "ASSESSLOW-".$counter, 4, 2, $extra);
            } else {
                $row["ASSESSLOW"] = '1';
            }
            
            //記号
            $extra = "";
            if ($countAssess == 0) {
                $value = $row["NAMESPARE1"];
            } else {
                $value = (!isset($model->warning)) ? $row["ASSESSMARK"] : $model->fields["ASSESSMARK"][$counter];
            }
            $row["ASSESSMARK"] = knjCreateTextBox($objForm, $value, "ASSESSMARK-".$counter, 4, 4, $extra);

            //上限値の表示
            if ($counter == 0 ) {
                //段階値が最大の時の計算は段階値 * フラグチェック数 (評定区分の時は全ての観点コードのフラグチェック数)
                if (!$row["ASSESSHIGH"]) {
                    //段階値のテキストにセットされている値で上限値をセット
                    if ($setrowAssessLevel) {
                        $row["ASSESSHIGH"] = $setrowAssessLevel * $model->viewflg_check;
                    }
                }
                $row["ASSESSHIGHTEXT"]  = "<span id=\"ASSESSHIGH_ID";
                $row["ASSESSHIGHTEXT"] .= $row["NAMESPARE2"];
                $row["ASSESSHIGHTEXT"] .= "\">";
                $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                $row["ASSESSHIGHTEXT"] .= "</span>";
            } else {
                if ($row["ASSESSHIGH"] != "") {
                    $row["ASSESSHIGH"] = $row["ASSESSHIGH"];
                }
                $row["ASSESSHIGHTEXT"]  = "<span id=\"ASSESSHIGH_ID";
                $row["ASSESSHIGHTEXT"] .= $row["NAMESPARE2"];
                $row["ASSESSHIGHTEXT"] .= "\">";
                $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                $row["ASSESSHIGHTEXT"] .= "</span>";
                
            }
            //段階値の上限値をhiddenで保持
            knjCreateHidden($objForm, "Assesshightvalue".$row["NAMESPARE2"], $row["ASSESSHIGH"]);
            
            //上限値を配列で取得
            $model->data["ASSESSHIGH"][] = $row["ASSESSHIGH"];
            $model->data["NAMESPARE2"][] = $row["NAMESPARE2"];
            $counter++;
            $arg["data"][] = $row;

        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJZ210E");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz210eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
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
    //学年教科評定設定ボタンを作成する
    /*$extra = "onclick=\" wopen('".REQUESTROOT."/Z/KNJZ210F/knjz210findex.php?&SEND_PRGRID=KNJZ210F&SEND_AUTH={$model->auth}&SEND_CLASSCD={$model->field["CLASSCD"]}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEND_GRADE={$model->field["GRADE"]}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"closeWin();\"";
    $arg["btn_settei"] = knjCreateBtn($objForm, "btn_settei", "学年教科評定設定", $extra);*/
    //確定を作成する
    $extra = "onclick=\"return btn_submit('kakutei');\"";
    $arg["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $link = REQUESTROOT."/Z/KNJZ210D/knjz210dindex.php?&SEND_PRGRID=KNJZ210D&SEND_AUTH={$model->auth}&SEND_CLASSCD={$model->field["CLASSCD"]}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEND_GRADE={$model->field["GRADE"]}";
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
