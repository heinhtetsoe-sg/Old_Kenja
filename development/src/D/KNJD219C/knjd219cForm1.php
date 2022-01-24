<?php

require_once('for_php7.php');

class knjd219cform1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd219cindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        /**********/
        /* ラジオ */
        /**********/
        //区分ラジオ 1:学年 2:クラス 3:コース 4:学科 5:コースグループ
        $model->field["DIV"] = $model->field["DIV"] ? $model->field["DIV"] : '5';
        $opt_div = array(1, 2, 3, 4, 5);
        $extra = "onClick=\"return btn_submit('change')\"";
        $label = array($extra." id=\"DIV1\"", $extra." id=\"DIV2\"", $extra." id=\"DIV3\"", $extra." id=\"DIV4\"", $extra." id=\"DIV5\"");
        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $label, $opt_div, get_count($opt_div));
        foreach ($radioArray as $key => $val) {
            $arg["sepa"][$key] = $val;
        }
        //表示切替
        if ($model->field["DIV"] == '2') {
            $arg["show2"] = $model->field["DIV"];
        } elseif ($model->field["DIV"] == '3') {
            $arg["show3"] = $model->field["DIV"];
        } elseif ($model->field["DIV"] == '5') {
            $arg["show5"] = $model->field["DIV"];
        } else {
        }

        /**********/
        /* コンボ */
        /**********/
        //参照
        //学期コンボ
        $extra = "style=\"width:70px\" onChange=\"return btn_submit('change');\" ";
        $query = knjd219cQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["PRE_SEMESTER"], "PRE_SEMESTER", $extra, 1);
        //対象
        //学期コンボ
        $extra = "style=\"width:70px\" onChange=\"return btn_submit('change');\" ";
        $query = knjd219cQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);
        //学年コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjd219cQuery::getGrade();
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);
        //クラスコンボ
        $query = knjd219cQuery::getHrClass($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["HR_CLASS"], "HR_CLASS", $extra, 1);
        //課程学科コースコンボ
        $query = knjd219cQuery::getCourse($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSE"], "COURSE", $extra, 1);
        //コースグループコンボ
        $query = knjd219cQuery::getGroupCd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GROUP_CD"], "GROUP_CD", $extra, 1);
        //科目コンボ
        $query = knjd219cQuery::getSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //事前処理チェック（序列確定処理）
        $recordAverage = array();
        if (strlen($model->field["SUBCLASSCD"])) {
            $recordAverage = $db->getRow(knjd219cQuery::getRecordAverage($model), DB_FETCHMODE_ASSOC);
        }
        knjCreateHidden($objForm, "COUNT", $recordAverage["COUNT"]);

        /**********/
        /* リスト */
        /**********/
        makeList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        makeBtn($objForm, $arg, $model);

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
        View::toHTML($model, "knjd219cForm1.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************/
//リスト作成
function makeList(&$objForm, &$arg, $db, $model)
{
    //警告メッセージを表示しない場合
    if (!isset($model->warning) && $model->cmd != "sim" && $model->cmd != "standard") {
        $query = knjd219cQuery::getList($model);
        $result = $db->query($query);
    } else {
        $row =& $model->field;
        $result = "";
    }

    //標準の下限値
    $standardLow = array(1 => 0
                        ,2 => 28
                        ,3 => 29
                        ,4 => 56
                        ,5 => 82);

    for ($i = 1; $i <= $model->field["ASSESSLEVELCNT"]; $i++) {
        if ($result != "") {
            if ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //レコードを連想配列のまま配列$arg[data]に追加していく。
                array_walk($row, "htmlspecialchars_array");

                //小数点を取り除く。
                $ar =explode(".", $row["ASSESSLOW"]);
                $row["ASSESSLOW"] = $ar[0];
                $ar =explode(".", $row["ASSESSHIGH"]);
                $row["ASSESSHIGH"] = $ar[0];
                $ar =explode(".", $row["STANDARD_ASSESSLOW"]);
                $row["STANDARD_ASSESSLOW"] = $ar[0];
            }
        }

        //段階値
        $row["ASSESSLEVEL"] = $i;

        //標準の下限値
        $standardVal = ($result != "") ? $row["STANDARD_ASSESSLOW"] : $row["STANDARD_ASSESSLOW".$i];
        $row["STANDARD_ASSESSLOW_SHOW"] = $standardVal;
        //hiddenで保持
        knjCreateHidden($objForm, "STANDARD_ASSESSLOW".$i, $standardVal);

        //下限
        $lowVal = ($result != "") ? $row["ASSESSLOW"] : $row["ASSESSLOW".$i];
        if ($row["ASSESSLEVEL"] == "1") {
            $extra = "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\" ";
        } else {
            $extra = "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);isNumb(this,".($row["ASSESSLEVEL"] - 1).",'ELSE');\" ";
        }
        $row["ASSESSLOWTEXT"] = knjCreateTextBox($objForm, $lowVal, "ASSESSLOW".$row["ASSESSLEVEL"], 4, 3, $extra);

        //上限
        if ($row["ASSESSLEVEL"] == $model->field["ASSESSLEVELCNT"]) {
            $highVal = ($result != "") ? $row["ASSESSHIGH"] : $row["ASSESSHIGH".$i];
            $extra = "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\" ";
            $row["ASSESSHIGHTEXT"] = knjCreateTextBox($objForm, $highVal, "ASSESSHIGH".$row["ASSESSLEVEL"], 4, 3, $extra);
        } else {
//            $highVal = ($result != "") ? $row["ASSESSHIGH"] : ($row["ASSESSLOW".($i + 1)] - 1);
            $highVal = ($result != "") ? $row["ASSESSHIGH"] : $row["ASSESSHIGH".$i];
            $row["ASSESSHIGHTEXT"]  = "<span id=\"strID";
            $row["ASSESSHIGHTEXT"] .= $row["ASSESSLEVEL"];
            $row["ASSESSHIGHTEXT"] .= "\">";
            $row["ASSESSHIGHTEXT"] .= $highVal;
            $row["ASSESSHIGHTEXT"] .= "</span>";
            //hiddenで保持
            knjCreateHidden($objForm, "ASSESSHIGH".$i, $highVal);
        }

        //人数
        $percentVal = ($result != "") ? $row["PERCENT_CNT"] : $row["PERCENT_CNT".$i];
        $row["PERCENT_CNT_SHOW"]  = "<span id=\"percentCntID";
        $row["PERCENT_CNT_SHOW"] .= $row["ASSESSLEVEL"];
        $row["PERCENT_CNT_SHOW"] .= "\">";
        $row["PERCENT_CNT_SHOW"] .= $percentVal;
        $row["PERCENT_CNT_SHOW"] .= "</span>";

        //人数割合
        $percentVal = ($result != "") ? $row["PERCENT"] : $row["PERCENT".$i];
        $row["PERCENT_SHOW"]  = "<span id=\"percentID";
        $row["PERCENT_SHOW"] .= $row["ASSESSLEVEL"];
        $row["PERCENT_SHOW"] .= "\">";
        $row["PERCENT_SHOW"] .= $percentVal;
        $row["PERCENT_SHOW"] .= "</span>";
        //hiddenで保持
        knjCreateHidden($objForm, "PERCENT".$i, $percentVal);

        $arg["data"][] = $row;
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
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
function makeBtn(&$objForm, &$arg, $model)
{
    //コピーボタン
    $extra = "onclick=\"return btn_submit('copy');\" ";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左のデータをコピー", $extra);
    //照会ボタン
    $url = REQUESTROOT."/D/KNJD219C/knjd219cindex.php?cmd=inquiry&SEMESTER={$model->field["SEMESTER"]}&TESTKINDCD={$model->field["TESTKINDCD"]}&DIV={$model->field["DIV"]}&GRADE={$model->field["GRADE"]}";
    $extra = "onClick=\" wopen('{$url}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    //$arg["button"]["btn_inquiry"] = knjCreateBtn($objForm, "btn_inquiry", "照 会", $extra);
    //標準ボタン
    $extra = "onclick=\"return btn_submit('standard');\" ";
    $arg["button"]["btn_standard"] = knjCreateBtn($objForm, "btn_standard", "標準の下限値計算", $extra);
    //計算ボタン
    $extra = "onclick=\"return btn_submit('sim');\" ";
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
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    //シュミレーション実行後、確定ボタン有効とする
    knjCreateHidden($objForm, "SIM_FLG", ($model->cmd != "sim") ? "off" : "on");
}
