<?php

require_once('for_php7.php');

class knjd419Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd419index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //教科・科目
        $arg["SUBCLASS"] = $db->getOne(knjd419Query::getSubclassName($model));

        //項目名取得
        $model->item = $db->getRow(knjd419Query::getGuidanceItemName($model), DB_FETCHMODE_ASSOC);

        //単元項目名
        $arg["ITEM_REMARK1"] = $model->item["ITEM_REMARK1"];

        //単元名
        $query = knjd419Query::getUnitcdList($model);
        if ($model->unit_aim_div == "1") {
            $extra = "onchange=\"return btn_submit('edit2');\"";
            $unit7 = makeCmb($objForm, $arg, $db, $query, "UNITCD", $model->field2["UNITCD"], $extra, 1);
        } else {
            $unitcd = $unitname = "";
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $unitcd .= ($unitcd) ? ','.$row["VALUE"] : $row["VALUE"];
                $unitname .= ($unitname) ? ','.$row["LABEL"] : $row["LABEL"];
            }
            $result->free();

            knjCreateHidden($objForm, "UNITCD", $unitcd);
            $arg["UNITCD"] = $unitname;
        }

        if ($model->unit_aim_div != "1") {
            //項目数取得
            $data = $db->getRow(knjd419Query::getHreportGuidanceGroupHdat($model), DB_FETCHMODE_ASSOC);

            //項目数テキスト
            if ($data["GROUPCD"] && !$model->fieldH["GROUP_REMARK_CNT"] && !$data["GROUP_REMARK_CNT"]) {
                $model->fieldH["GROUP_REMARK_CNT"] = "1";
                $model->setcnt = "1";
            } else if ($data["GROUPCD"] && !$model->fieldH["GROUP_REMARK_CNT"]) {
                $model->fieldH["GROUP_REMARK_CNT"] = $data["GROUP_REMARK_CNT"];
                $model->setcnt = $data["GROUP_REMARK_CNT"];
            } else if (!$data["GROUPCD"] && !$model->fieldH["GROUP_REMARK_CNT"]) {
                $model->fieldH["GROUP_REMARK_CNT"] = "1";
                $model->setcnt = "1";
            }
            $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["GROUP_REMARK_CNT"] = knjCreateTextBox($objForm, $model->fieldH["GROUP_REMARK_CNT"], "GROUP_REMARK_CNT", 3, 2, $extra);

            //確定ボタン
            $extra = "onclick=\"return cnt_check();\"";
            $arg["REMARK_CNT_BTN"] = knjCreateBtn($objForm, "REMARK_CNT_BTN", "確 定", $extra);
        }

        //所見データ
        if (!isset($model->warning) && isset($model->subclasscd)) {
            $result = $db->query(knjd419Query::getHreportGuidanceGroupDat($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rowD["GROUP_REMARK".$row["SEQ"]] = $row["GROUP_REMARK"];
            }
            $result->free();

            $rowU = $db->getRow(knjd419Query::getHreportGuidanceGroupUnitDat($model, $model->field2["UNITCD"]), DB_FETCHMODE_ASSOC);
            $rowY = $db->getRow(knjd419Query::getHreportGuidanceGroupYdat($model), DB_FETCHMODE_ASSOC);
            $rowP = $db->getRow(knjd419Query::getHreportGuidanceGroupDat($model, "51"), DB_FETCHMODE_ASSOC);
        } else {
            $rowD =& $model->field;
            $rowU =& $model->field2;
            $rowY =& $model->field3;
            $rowP =& $model->field4;
        }

        //セット
        $tmp = array();
        $pattern = $model->pattern[$model->unit_aim_div][$model->guidance_pattern];
        $createGroupRemarkCnt = array();
        for ($i=0; $i < get_count($pattern); $i++) {
            list ($label, $value, $moji, $gyo, $seqadd) = $pattern[$i];

            if ($model->unit_aim_div != "1" || $model->guidance_pattern != "7") {
                $arg["pattern7Igai"] = "1";
                $tmp["UNITCD7"] = "";
            } else {
                if ($i == 0) {
                    $tmp["UNITCD7"] = "<tr height=\"30\"><th class=\"no_search\" nowrap><b>{$arg["ITEM_REMARK1"]}</b></th><td bgcolor=\"#ffffff\">&nbsp;{$unit7}</td></tr>";
                } else {
                    $tmp["UNITCD7"] = "";
                }
            }
            if ($model->unit_aim_div == "1") {
                //項目名
                $tmp["ITEM_REMARK"] = $model->item[$label];
                //所見テキスト
                $val = ($value == "GROUP_PROCEDURE") ? $rowP[$value] : (($value == "GROUP_YEAR_TARGET") ? $rowY[$value] : $rowU[$value]);
                $extra = ($value == "GROUP_PROCEDURE") ? "style=\"height:275px;\"" : "style=\"height:73px;\"";
                $tmp["GROUP_REMARK"] = KnjCreateTextArea($objForm, $value, $gyo, ($moji* 2) + 1, "soft", $extra, $val);
                $tmp["GROUP_REMARK_COMMENT"] = "(全角".$moji."文字X".$gyo."行まで)";
                $arg["data"][] = $tmp;
            } else {
                if ($value == "") {
                    if ($model->guidance_pattern == 'A') {
                        $createGroupRemarkCnt[] = $pattern[$i];
                    } else {
                        for ($j=1; $j <= $model->fieldH["GROUP_REMARK_CNT"]; $j++) {
                            //項目名
                            $seq = $seqadd + $j;
                            $tmp["ITEM_REMARK"] = $model->item[$label].$seq;
                            //所見テキスト
                            $extra = "style=\"height:73px;\"";
                            $tmp["GROUP_REMARK"] = KnjCreateTextArea($objForm, "GROUP_REMARK".$seq, $gyo, ($moji* 2) + 1, "soft", $extra, $rowD["GROUP_REMARK".$seq]);
                            $tmp["GROUP_REMARK_COMMENT"] = "";

                            $arg["data"][] = $tmp;
                        }
                    }
                } else {
                    //項目名
                    $tmp["ITEM_REMARK"] = $model->item[$label];
                    //所見テキスト
                    $val = ($value == "GROUP_PROCEDURE") ? $rowP[$value] : (($value == "GROUP_YEAR_TARGET") ? $rowY[$value] : "");
                    $extra = ($value == "GROUP_PROCEDURE") ? "style=\"height:275px;\"" : "style=\"height:73px;\"";
                    $tmp["GROUP_REMARK"] = KnjCreateTextArea($objForm, $value, $gyo, ($moji* 2) + 1, "soft", $extra, $val);
                    $tmp["GROUP_REMARK_COMMENT"] = ($value == "GROUP_PROCEDURE") ? "" : "(全角".$moji."文字X".$gyo."行まで)";

                    $arg["data"][] = $tmp;
                }
            }
        }
        if (get_count($createGroupRemarkCnt) > 0) {
            for ($j=1; $j <= $model->fieldH["GROUP_REMARK_CNT"]; $j++) {
                for ($i = 0; $i < get_count($createGroupRemarkCnt); $i++) {
                    list ($label, $value, $moji, $gyo, $seqadd) = $pattern[$i];
                    //項目名
                    $seq = $seqadd + $j;
                    $tmp["ITEM_REMARK"] = $model->item[$label].$j;
                    //所見テキスト
                    $extra = "style=\"height:73px;\"";
                    $tmp["GROUP_REMARK"] = KnjCreateTextArea($objForm, "GROUP_REMARK".$seq, $gyo, ($moji* 2) + 1, "soft", $extra, $rowD["GROUP_REMARK".$seq]);
                    $tmp["GROUP_REMARK_COMMENT"] = "";

                    $arg["data"][] = $tmp;
                }
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knjd419index.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd419Form2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $arg[$name];
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT || !$model->subclasscd) ? "disabled" : "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT || !$model->subclasscd) ? "disabled" : "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //印刷ボタン
    $extra = ($model->subclasscd) ? "onclick=\"return newwin('" . SERVLET_URL . "');\"" : "disabled";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    if (strlen($model->grade_hr_class) == "2") {
        $ghr_cd = $model->grade_hr_class;
        list ($grade, $hr_class) = explode('-', $model->grade_hr_class2);
    } else {
        $ghr_cd = '00';
        list ($grade, $hr_class) = explode('-', $model->grade_hr_class);
    }

    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD419");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "SEMESTER", $model->semester);
    knjCreateHidden($objForm, "GAKUBU_SCHOOL_KIND", $model->gakubu_school_kind);
    knjCreateHidden($objForm, "GHR_CD", $ghr_cd);
    knjCreateHidden($objForm, "GRADE", $grade);
    knjCreateHidden($objForm, "HR_CLASS", $hr_class);
    knjCreateHidden($objForm, "CONDITION", $model->condition);
    knjCreateHidden($objForm, "GROUPCD", $model->groupcd);
    knjCreateHidden($objForm, "CLASSCD", $model->classcd);
    knjCreateHidden($objForm, "SCHOOL_KIND", $model->school_kind);
    knjCreateHidden($objForm, "CURRICULUM_CD", $model->curriculum_cd);
    knjCreateHidden($objForm, "SUBCLASSCD", $model->subclasscd);
    knjCreateHidden($objForm, "setcnt");
    knjCreateHidden($objForm, "UNIT_AIM_DIV", $model->unit_aim_div);
}
?>
