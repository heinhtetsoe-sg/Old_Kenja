<?php

require_once('for_php7.php');

class knjf323SubForm4 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4", "POST", "knjf323index.php", "", "subform4");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjf323Query::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$model->schregno.'　'.$name;

        //年度コンボ
        $query = knjf323Query::getYearList($model);
        $extra = "onchange=\"return btn_submit('subform4')\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)) {
            //データ取得 -- MEDEXAM_CARE_HDAT
            $Row = $db->getRow(knjf323Query::getCareHData($model, $model->field["YEAR"], "04"), DB_FETCHMODE_ASSOC);
            //データ取得 -- MEDEXAM_CARE_DAT
            $query = knjf323Query::getCareData($model, $model->field["YEAR"], "04");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["CHECK".$row["CARE_KIND"]."_".$row["CARE_ITEM"]."_".$row["CARE_SEQ"]] = 1;
                if (strlen($row["CARE_REMARK1"]) > 0) {
                    if ($row["CARE_KIND"]."_".$row["CARE_ITEM"] == '01_03') {
                        //診断根拠
                        $array = explode(',', $row["CARE_REMARK1"]);
                        if (in_array(1, $array)) $Row["REASON".$row["CARE_SEQ"]."_1"] = 1;
                        if (in_array(2, $array)) $Row["REASON".$row["CARE_SEQ"]."_2"] = 1;
                        if (in_array(3, $array)) $Row["REASON".$row["CARE_SEQ"]."_3"] = 1;
                    } else {
                        $Row["TEXT".$row["CARE_KIND"]."_".$row["CARE_ITEM"]."_".$row["CARE_SEQ"]] = $row["CARE_REMARK1"];
                    }
                }
                if (strlen($row["CARE_REMARK2"]) > 0) $Row["DETAIL".$row["CARE_KIND"]."_".$row["CARE_ITEM"]."_".$row["CARE_SEQ"]] = $row["CARE_REMARK2"];
            }
            $result->free();
        } else {
            $Row =& $model->field;
        }


        $hdat = $db->getRow(knjf323Query::selectQuery($model), DB_FETCHMODE_ASSOC);


        //項目情報
        $cnt = array();
        $cnt[1][1] = array("cnt" => 3,  "txt" => array());
        $cnt[1][2] = array("cnt" => 6,  "txt" => array(1, 6));
        $cnt[1][3] = array("cnt" => 12, "txt" => array(6, 7, 8, 9, 10, 11, 12));
        $cnt[1][4] = array("cnt" => 5,  "txt" => array(3,4,5));
        $cnt[2][1] = array("cnt" => 2,  "txt" => array(2));
        $cnt[2][2] = array("cnt" => 2,  "txt" => array(2));
        $cnt[2][3] = array("cnt" => 2,  "txt" => array(2));
        $cnt[2][4] = array("cnt" => 2,  "txt" => array(2));

        //種別
        for ($i=1; $i <= 2; $i++) {
            //項目
            for ($j=1; $j <= 4; $j++) {
                //SEQ
                for ($k=1; $k <= $cnt[$i][$j]["cnt"]; $k++) {

                    $cd = sprintf("%02d", $i)."_".sprintf("%02d", $j)."_".sprintf("%02d", $k);

                    $disable = "";
                    if ($i == 1 && $j == 1 && $hdat["CARE_FLG04"] != 1) {
                        $disable = " disabled";
                    } else if ($i == 1 && $j == 2 && $hdat["CARE_FLG05"] != 1) {
                        $disable = " disabled";
                    }

                    //チェックボックス
                    $name = "CHECK".$cd;
                    $extra  = ($Row[$name] == "1") ? "checked" : "";
                    $extra .= " id=\"{$name}\" onclick=\"OptionUse(this);\"";
                    $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra.$disable, "");

                    if (in_array($k, $cnt[$i][$j]["txt"])) {
                        if ($i == 2) {
                            //テキストエリア
                            $name = "TEXT".$cd;
                            list ($moji, $gyo) = array(40, 2);
                            $extra  = ($Row["CHECK".$cd] == 1) ? "" : "disabled";
                            $extra .= " style=\"height:{calcHeight($moji, $gyo)}px;\"";
                            $arg["data"][$name] = KnjCreateTextArea($objForm, $name, $gyo, ((int)$moji * 2 + 1), "soft", $extra.$disable, $Row[$name]);
                            $arg["data"]["COMMENT".$cd] = "（全角".$moji."文字X".$gyo."行まで）";
                        } else {
                            //テキスト
                            $name = ($i == 1 && $j == 3) ? "DETAIL".$cd : "TEXT".$cd;
                            $moji = 20;
                            $extra = ($Row["CHECK".$cd] == 1) ? "" : "disabled";
                            $arg["data"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, ((int)$moji * 2), ((int)$moji * 2), $extra.$disable);
                        }
                    }

                    if ($i == 1 && $j == 3) {
                        for ($l=1; $l <= 3 ; $l++) {
                            //チェックボックス
                            $name = "REASON".sprintf("%02d", $k)."_".$l;
                            $extra  = ($Row[$name] == "1") ? "checked" : "";
                            $extra .= ($Row["CHECK".$cd] == 1) ? "" : " disabled";
                            $extra .= " id=\"{$name}\"";
                            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra, "");

                        }
                    }
                }
            }
        }

        //医師記入欄
        list ($moji, $gyo) = array(25, 7);
        $extra = "style=\"height:{calcHeight($moji, $gyo)}px;\"";
        $arg["data"]["TEXT02_05_00"] = KnjCreateTextArea($objForm, "TEXT02_05_00", $gyo, ((int)$moji * 2 + 1), "soft", $extra, $Row["TEXT02_05_00"]);
        $arg["data"]["COMMENT02_05_00"] = "（全角".$moji."文字X".$gyo."行まで）";
        //診断のきっかけ
        list ($moji, $gyo) = array(40, 7);
        $extra = "style=\"height:{calcHeight($moji, $gyo)}px;\"";
        $arg["data"]["TEXT02_06_00"] = KnjCreateTextArea($objForm, "TEXT02_06_00", $gyo, ((int)$moji * 2 + 1), "soft", $extra, $Row["TEXT02_06_00"]);
        $arg["data"]["COMMENT02_06_00"] = "（全角".$moji."文字X".$gyo."行まで）";

        //extra
        $extra = "onBlur=\"this.value=toTelNo(this.value);\"";

        //（緊急時連絡先）保護者名
        $arg["data"]["EMERGENCYNAME"] = knjCreateTextBox($objForm, $Row["EMERGENCYNAME"], "EMERGENCYNAME", (10 * 2), (10 * 2), "");
        //（緊急時連絡先）保護者電話番号
        $arg["data"]["EMERGENCYTELNO"] = knjCreateTextBox($objForm, $Row["EMERGENCYTELNO"], "EMERGENCYTELNO", 14, 14, $extra);
        //（緊急時連絡先）医療機関名
        $arg["data"]["EMERGENCYNAME2"] = knjCreateTextBox($objForm, $Row["EMERGENCYNAME2"], "EMERGENCYNAME2", (35 * 2), (40 * 2), "");
        //（緊急時連絡先）医療機関電話番号
        $arg["data"]["EMERGENCYTELNO2"] = knjCreateTextBox($objForm, $Row["EMERGENCYTELNO2"], "EMERGENCYTELNO2", 14, 14, $extra);

        //記載日
        $date = str_replace("-", "/", $Row["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $date);
        //医師名
        $arg["data"]["DOCTOR"] = knjCreateTextBox($objForm, $Row["DOCTOR"], "DOCTOR", (10 * 2), (10 * 2), "");
        //医療機関名
        $arg["data"]["HOSPITAL"] = knjCreateTextBox($objForm, $Row["HOSPITAL"], "HOSPITAL", (40 * 2), (40 * 2), "");

        //緊急時対応プラン
        list ($moji, $gyo) = array(40, 17);
        $extra = "style=\"height:{calcHeight($moji, $gyo)}px;\"";
        $arg["data"]["REMARK"] = KnjCreateTextArea($objForm, "REMARK", $gyo, ((int)$moji * 2 + 1), "soft", $extra, $Row["REMARK"]);
        $arg["data"]["COMMENT_REMARK"] = "（全角".$moji."文字X".$gyo."行まで）";

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf323SubForm4.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //前年度からコピーボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->field["YEAR"] == CTRL_YEAR) ? "onclick=\"return btn_submit('subform4_copy');\"" : "disabled";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->field["YEAR"] == CTRL_YEAR) ? "onclick=\"return btn_submit('subform4_update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = ($model->field["YEAR"] == CTRL_YEAR) ? "onclick=\"return btn_submit('subform4_clear');\"" : "disabled";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //印刷ボタン
    $extra = "onclick=\"return newwin('".SERVLET_URL."');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJF323");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
}

//テキストエリアの高さ
function calcHeight($moji, $gyo) {
    $height = (int)$gyo * 13.5 + ((int)$gyo - 1) * 3 + 5;
    return $height;
}
?>
