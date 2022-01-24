<?php

require_once('for_php7.php');

class knjl521jForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl521jindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //学校種別コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl521jQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試種別コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl521jQuery::getTestdiv($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        // //高校のみ
        // if ($model->applicantdiv == "2") {
        //     //入試回数コンボボックス
        //     $query = knjl521jQuery::getTestdiv0($model->ObjYear, $model->testdiv);
        //     $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        //     makeCmb($objForm, $arg, $db, $query, "TESTDIV0", $model->testdiv0, $extra, 1, "BLANK");
        // 
        //     //志望区分コンボボックス
        //     $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        //     $query = knjl521jQuery::getEntExamCourse($model);
        //     makeCmb($objForm, $arg, $db, $query, "TOTALCD", $model->totalcd, $extra, 1, "BLANK");
        // }

        //入試方式コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl521jQuery::getExamType($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMTYPE", $model->examtype, $extra, 1, "BLANK");

        // //特別措置者(インフルエンザ)
        // $extra = "id=\"SPECIAL_REASON_DIV\" onchange=\"return btn_submit('main');\" tabindex=-1 ";
        // $extra .= strlen($model->special_reason_div) ? "checked='checked' " : "";
        // $arg["TOP"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //テキスト名
        $text_name = array("1" => "NAISINTEN"
                          ,"2" => "KESSEKI"
                          ,"3" => "REMARK");
        $setTextField = "";
        $textSep = "";
        foreach ($text_name as $code => $col) {
            $setTextField .= $textSep.$col."-";
            $textSep = ",";
        }

        //一覧表示フラグ
        $listFlg = $model->examtype != "";

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $listFlg) {
            //データ取得
            $result = $db->query(knjl521jQuery::SelectQuery($model));
            $count = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"].'-'.$count.'-'.$row["EXAMNO"];
                //エラー時は画面の値をセット
                if (isset($model->warning)) {
                    $recptr = $model->recordarry[$count];
                    if (get_count($recptr) > 0) {
                        $row["NAITEI"] = $recptr["naitei_chk"];
                        $row["NAISINTEN"] = $recptr["naisinten_value"];
                        $row["KESSEKI"] = $recptr["kesseki_value"];
                        $row["CHK_A"] = $recptr["chk_a"];
                        $row["CHK_F"] = $recptr["chk_f"];
                        $row["CHK_T"] = $recptr["chk_t"];
                        $row["CHK_B"] = $recptr["chk_b"];
                        $row["CHK_J"] = $recptr["chk_j"];
                    }
                }
                //内定
                $extra  = "id=\"NAITEI-".$count."\"";
                $extra .= ($row["NAITEI"] == "1") ? " checked": "";
                $row["NAITEI"] = knjCreateCheckBox($objForm, "NAITEI-".$count, "1", $extra);
                //内申点
                $extra = "style=\"text-align: center\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["NAISINTEN"] = knjCreateTextBox($objForm, $row["NAISINTEN"], "NAISINTEN-".$count, 3, 3, $extra);
                //欠席
                $extra = "style=\"text-align: center\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["KESSEKI"] = knjCreateTextBox($objForm, $row["KESSEKI"], "KESSEKI-".$count, 3, 3, $extra);
                //評価区分A
                $extra  = "id=\"CHK_A-".$count."\"";
                $extra .= ($row["CHK_A"] == "1") ? " checked": "";
                $row["CHK_A"] = knjCreateCheckBox($objForm, "CHK_A-".$count, "1", $extra);
                //評価区分F
                $extra  = "id=\"CHK_F-".$count."\"";
                $extra .= ($row["CHK_F"] == "1") ? " checked": "";
                $row["CHK_F"] = knjCreateCheckBox($objForm, "CHK_F-".$count, "1", $extra);
                //評価区分T
                $extra  = "id=\"CHK_T-".$count."\"";
                $extra .= ($row["CHK_T"] == "1") ? " checked": "";
                $row["CHK_T"] = knjCreateCheckBox($objForm, "CHK_T-".$count, "1", $extra);
                //評価区分B
                $extra  = "id=\"CHK_B-".$count."\"";
                $extra .= ($row["CHK_B"] == "1") ? " checked": "";
                $row["CHK_B"] = knjCreateCheckBox($objForm, "CHK_B-".$count, "1", $extra);
                //評価区分L
                $extra  = "id=\"CHK_J-".$count."\"";
                $extra .= ($row["CHK_J"] == "1") ? " checked": "";
                $row["CHK_J"] = knjCreateCheckBox($objForm, "CHK_J-".$count, "1", $extra);
                //備考
                $extra = " onPaste=\"return showPaste(this);\" onblur=\"this.value=toStringCheck(this.value);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["REMARK"] = knjCreateTextBox($objForm, $row["REMARK"], "REMARK-".$count, 30, 30, $extra);

                $arg["data"][] = $row;
                $count++;
            }

            if ($count == 0) {
                $model->setMessage("MSG303");
            }
        }

        knjCreateHidden($objForm, "COUNT", $count);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl521jForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV0");
    knjCreateHidden($objForm, "HID_TOTALCD");
    knjCreateHidden($objForm, "HID_EXAM_TYPE");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL521J");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
