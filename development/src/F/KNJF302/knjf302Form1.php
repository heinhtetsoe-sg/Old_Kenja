<?php

require_once('for_php7.php');

class knjf302Form1
{
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf302index.php", "", "edit");

        //DB接続
        $db     = Query::dbCheckOut();
        $db2    = Query::dbCheckOut2();

        //教育委員会判定
        $query = knjf302Query::z010Abbv1();
        $model->z010Abbv1 = $db->getOne($query);

        //更新する内容があった場合に日付を入力させるポップアップ
        if ($model->cmd == "fixed" && ($model->z010Abbv1 == "1" || $model->z010Abbv1 == "2")) {
            $arg["reload"] = " fixed('".REQUESTROOT."')";
        }

        //確定日付ありは入力不可
        $disabled = $model->fixedData ? " disabled " : "";

        //年度
        $query = knjf302Query::getYear($model);
        $extra = "onchange=\"return btn_submit('changeYear');\"";
        $model->field["YEAR"] = $model->field["YEAR"] ? $model->field["YEAR"] : CTRL_YEAR;
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1);

        if ($model->z010Abbv1 == "1" || $model->z010Abbv1 == "2") {
            //V_SCHOOL_MSTから学校コードを取得
            $query = knjf302Query::getSchoolMst($model);
            $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->schoolcd = $rtnRow["KYOUIKU_IINKAI_SCHOOLCD"];
            $arg["Z010ABBV1"] = "1";
        } else {
            $model->schoolcd = "000000000000";
        }

        //学年
        $query = knjf302Query::getGrade($model);
        $extra = "onchange=\"return btn_submit('changeYear');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "ALL");

        //タイトル
        $arg["TITLE"] = "感染症発生状況入力管理画面";

        //登校日
        $extra = "btn_submit('changeDate')";
        $model->field["ADDITION_DATE"] = $model->field["ADDITION_DATE"] ? $model->field["ADDITION_DATE"] : CTRL_DATE;
        $arg["ADDITION_DATE"] = View::popUpCalendar2($objForm, "ADDITION_DATE", str_replace("-", "/", $model->field["ADDITION_DATE"]), "reload=true", $extra);

        //学期
        if ($model->field["ADDITION_DATE"]) {
            $query = knjf302Query::getSemesMst($model);
            $semester = $db->getOne($query);
            $model->semester = $semester;
        } else {
            $model->semester = "";
        }

        //前日へボタンを作成する
        $extra = "style=\"width:60px\"onclick=\"return btn_submit('read_before');\"";
        $arg["btn_before"] = knjCreateBtn($objForm, "btn_before", "<< 前日", $extra);

        //翌日へボタンを作成する
        $extra = "style=\"width:60px\"onclick=\"return btn_submit('read_next');\"";
        $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", "翌日 >>", $extra);

        //作成日
        $arg["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", str_replace("-", "/", $model->field["EXECUTE_DATE"]), "");

        //項目名(列)
        $nameArray = array("EDBOARD_SCHOOLCD",
                           "YEAR",
                           "ADDITION_DATE",
                           "GRADE",
                           "HR_CLASS",
                           "ABSENCE01",
                           "ABSENCE02",
                           "ABSENCE03",
                           "ABSENCE04",
                           "ABSENCE05",
                           "ABSENCE06",
                           "ABSENCE07",
                           "ABSENCE08",
                           "ATTENDSUSPEND01",
                           "ATTENDSUSPEND02",
                           "ATTENDSUSPEND03",
                           "ATTENDSUSPEND04",
                           "ATTENDSUSPEND05",
                           "ATTENDSUSPEND06",
                           "ATTENDSUSPEND07",
                           "ATTENDSUSPEND08",
                           "ATTENDSUSPEND09",
                           "ATTENDSUSPEND10",
                           "ATTENDSUSPEND11",
                           "TOTALSUM01",
                           "TOTALSUM02",
                           "TOTALSUM03",
                           "TOTALSUM04",
                           "TOTALSUM04_PERCENT",
                           "TOTALSUM05",
                           "TOTALSUM05_PERCENT",
                           "TOTALSUM06");

        unset($model->fields["CODE"]);

        if ($model->field["YEAR"] && $model->field["ADDITION_DATE"] && !isset($model->warning)) {
            $changeGrade = "";
            $gradeTotalRow = array();
            $totalRow = array();
            $count = 0;
            $countGrade = 0;
            $result = $db->query(knjf302Query::ReadQuery($model));
            while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if ($model->cmd == "syuukei") {
                    if ($changeGrade && $changeGrade != $Row["GRADE"]) {
                        $countGrade++;
                    }
                    $totalAbsence = 0;
                    $totalAttendsuspend = 0;
                    foreach ($model->fields as $fieldsKey => $fieldsVal) {
                        $Row[$fieldsKey] = $fieldsVal[$count+$countGrade];
                        if (strpos($fieldsKey, "ABSENCE") !== false) {
                            $totalAbsence += (int)$fieldsVal[$count+$countGrade];
                        }
                        if (strpos($fieldsKey, "ATTENDSUSPEND") !== false) {
                            $totalAttendsuspend += (int)$fieldsVal[$count+$countGrade];
                        }
                    }
                    $Row["TOTALSUM02"] = $totalAbsence;
                    $Row["TOTALSUM03"] = $totalAttendsuspend;
                }
                if ($changeGrade && $changeGrade != $Row["GRADE"]) {
                    $model->fields["CODE"][] = $changeGrade."-999";
                    $gradeTotalRow["HR_NAME"] = ((int)$changeGrade * 1)."年 合計";

                    foreach ($nameArray as $name) {
                        $setSize = "4";
                        $val = ($gradeTotalRow[$name] == "")? "0" : $gradeTotalRow[$name] ;
                        $objForm->ae( array("type"        => "text",
                                            "name"        => $name,
                                            "size"        => $setSize,
                                            "maxlength"   => $setSize,
                                            "multiple"    => "1",
                                            "extrahtml"   => $disabled."style=\"text-align:right; background-color:cccccc;\" readonly onblur=\"this.value = toInteger(this.value)\" ",
                                            "value"       => $val ));
                        $gradeTotalRow[$name] = $objForm->ge($name);
                    }
                    $arg["data"][] = $gradeTotalRow;
                    $gradeTotalRow = array();
                }

                $model->fields["CODE"][] = $Row["GRADE"]."-".$Row["HR_CLASS"];
                $changeGrade = $Row["GRADE"];

                //text
                foreach ($nameArray as $name) {
                    $readOnly = "";
                    $readStyle = "";
                    $setSize = "3";
                    if ($name == "TOTALSUM04" ||
                        $name == "TOTALSUM04_PERCENT" ||
                        $name == "TOTALSUM05" ||
                        $name == "TOTALSUM05_PERCENT" ||
                        $name == "TOTALSUM06"
                    ) {
                        $readOnly = " readonly ";
                        $readStyle = "; background-color:cccccc;";
                        $setSize = "4";
                    }
                    $gradeTotalRow[$name] += $Row[$name];
                    $totalRow[$name] += $Row[$name];
                    $val = ($Row[$name] == "")? "0" : $Row[$name] ;
                    $objForm->ae( array("type"        => "text",
                                        "name"        => $name,
                                        "size"        => $setSize,
                                        "maxlength"   => $setSize,
                                        "multiple"    => "1",
                                        "extrahtml"   => $disabled.$readOnly."style=\"text-align:right{$readStyle}\" onblur=\"this.value = toInteger(this.value)\" ",
                                        "value"       => $val ));
                    $Row[$name] = $objForm->ge($name);
                }

                $arg["data"][] = $Row;
                $count++;
            }
            if ($changeGrade && $changeGrade != $Row["GRADE"]) {
                $model->fields["CODE"][] = $changeGrade."-999";
                $gradeTotalRow["HR_NAME"] = ((int)$changeGrade * 1)."年 合計";

                foreach ($nameArray as $name) {
                    $setSize = "4";
                    $val = ($gradeTotalRow[$name] == "")? "0" : $gradeTotalRow[$name] ;
                    $objForm->ae( array("type"        => "text",
                                        "name"        => $name,
                                        "size"        => $setSize,
                                        "maxlength"   => $setSize,
                                        "multiple"    => "1",
                                        "extrahtml"   => $disabled."style=\"text-align:right; background-color:cccccc;\" readonly onblur=\"this.value = toInteger(this.value)\" ",
                                        "value"       => $val ));
                    $gradeTotalRow[$name] = $objForm->ge($name);
                }
                $arg["data"][] = $gradeTotalRow;
                $gradeTotalRow = array();
            }

            $model->fields["CODE"][] = "99-999";
            $totalRow["HR_NAME"] = "総合計";
            foreach ($nameArray as $name) {
                $setSize = "4";
                $val = ($totalRow[$name] == "")? "0" : $totalRow[$name] ;
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "size"        => $setSize,
                                    "maxlength"   => $setSize,
                                    "multiple"    => "1",
                                    "extrahtml"   => $disabled."style=\"text-align:right; background-color:cccccc;\" readonly onblur=\"this.value = toInteger(this.value)\" ",
                                    "value"       => $val ));
                $totalRow[$name] = $objForm->ge($name);
            }
            $arg["data"][] = $totalRow;
            $totalRow = array();

            //報告履歴
            $query = knjf302Query::getReport($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->field["REPORT"], "REPORT", $extra, 1, "BLANK");
        }

        //報告済み日付
        $query = knjf302Query::getReport($model);
        $setExeDate = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setExeDate .= $sep.str_replace("-", "/", $row["VALUE"]);
            $sep = ",";
        }
        $result->free();
        $arg["EXE_DATES"] = $setExeDate;

        //県への報告ボタン
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告", $extra);

        //文書番号
        $query = knjf302Query::getTuutatu($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db2, $query, $model->docNumber, "DOC_NUMBER", $extra, 1, "BLANK");

        //確定データ
        $query = knjf302Query::getFixed($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->fixedData, "FIXED_DATA", $extra, 1, "BLANK");

        //再計算ボタン
        $extra = $disabled."onclick=\"return btn_submit('recalc');\"";
        $arg["btn_recalc"] = knjCreateBtn($objForm, "btn_recalc", "再計算", $extra);

        //集計ボタン
        $extra = $disabled."onclick=\"return btn_submit('syuukei');\"";
        $arg["btn_syuukei"] = knjCreateBtn($objForm, "btn_syuukei", "集 計", $extra);

        //更新ボタン
        $extra = $disabled."onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = $disabled."onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "FIXED_DATE");
        knjCreateHidden($objForm, "UPDATED[]", $model->updated);
        knjCreateHidden($objForm, "SEMESTER", $model->semester);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "PRGID", "KNJF302");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        if ($model->cmd == "houkokuPrint") {
//            $arg["jscript"] = " newwin('" . SERVLET_URL . "');";
        }

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf302Form1.html", $arg); 

    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["LABEL"] = $name == "FIXED_DATA" ? str_replace("-", "/", $row["LABEL"]) : $row["LABEL"];
        $row["VALUE"] = $name == "FIXED_DATA" ? str_replace("-", "/", $row["VALUE"]) : $row["VALUE"];
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
