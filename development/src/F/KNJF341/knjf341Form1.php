<?php

require_once('for_php7.php');

class knjf341Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf341index.php", "", "edit");

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //教育委員会判定
        $query = knjf341Query::z010Abbv1();
        $model->z010Abbv1 = $db->getOne($query);

        if ($model->z010Abbv1 == "1" || $model->z010Abbv1 == "2") {
            $arg["Z010ABBV1"] = "1";
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //題名
        $arg["TITLE"] = $model->title;

        //更新する内容があった場合に日付を入力させるポップアップ
        if ($model->cmd == "fixed") {
            $arg["reload"] = " fixed('".REQUESTROOT."')";
        }

        //確定日付ありは入力不可
        $disabled = $model->fixedData ? " disabled " : "";

        //県への報告用登録日付(テーブルは報告履歴テーブルのみ)
        if (!isset($model->execute_date)) {
            $model->execute_date = str_replace("-", "/", CTRL_DATE);
        }
        $arg["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", $model->execute_date, "");

        $arg["maxPx"] = "1100";//横幅のピクセル値

        $targets1     = 0;//対象者数
        $examinee1    = 0;//受検者数
        $remark1      = 0;//有所見者
        $uricsugar1   = 0;//糖
        $albuminuria1 = 0;//蛋白
        $uricbleed1   = 0;//潜血
        $targets2     = 0;//再検査対象者数
        $examinee2    = 0;//再検査実施数
        $targets3     = 0;//精密検査該当者
        $examinee3    = 0;//精密検査受検者
        $normal       = 0;//精密検査異常なし
        $careful      = 0;//精密検査要観察
        $treatment    = 0;//精密検査要治療

        $query = knjf341Query::readQuery($model);
        $result = $db->query($query);
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setSize = 4;
            $textBoxArray = array();

            if ($model->cmd == "recalc") {
                if (($Row["ALBUMINURIA1CD"] != null && $Row["ALBUMINURIA1CD"] != "00") ||
                    ($Row["URICSUGAR1CD"]   != null && $Row["URICSUGAR1CD"]   != "00") ||
                    ($Row["URICBLEED1CD"]   != null && $Row["URICBLEED1CD"]   != "00")) {
                    //受検者数
                    $examinee1++;
                }
                if (((int)$Row["ALBUMINURIA1CD"] >= 2) ||
                    ((int)$Row["URICSUGAR1CD"]   >= 2) ||
                    ((int)$Row["URICBLEED1CD"]   >= 2)) {
                    //有所見者
                    $remark1++;
                    //再検査対象者数
                    $targets2++;
                }
                if ((int)$Row["ALBUMINURIA1CD"] >= 2) {
                    //蛋白
                    $albuminuria1++;
                }
                if ((int)$Row["URICSUGAR1CD"] >= 2) {
                    //糖
                    $uricsugar1++;
                }
                if ((int)$Row["URICBLEED1CD"] >= 2) {
                    //潜血
                    $uricbleed1++;
                }
                if (($Row["ALBUMINURIA2CD"]  != null && $Row["ALBUMINURIA2CD"] != "00") ||
                    ($Row["URICSUGAR2CD"]    != null && $Row["URICSUGAR2CD"]   != "00") ||
                    ($Row["URICBLEED2CD"]    != null && $Row["URICBLEED2CD"]   != "00")) {
                    //再検査実施数
                    $examinee2++;
                }
                if (((int)$Row["ALBUMINURIA2CD"] >= 2) ||
                    ((int)$Row["URICSUGAR2CD"]   >= 2) ||
                    ((int)$Row["URICBLEED2CD"]   >= 2)) {
                    //精密検査該当者
                    $targets3++;

                    if ($Row["DET_REMARK2"]  != null && $Row["DET_REMARK2"] != "00") {
                        //精密検査受検者
                        $examinee3++;
                    }
                    if ($Row["DET_REMARK2"] != "01") {
                        //精密検査異常なし
                        $normal++;
                    } elseif ($Row["DET_REMARK2"] != "02") {
                        //精密検査要観察
                        $careful++;
                    } elseif ($Row["DET_REMARK2"] != "03") {
                        //精密検査要治療
                        $treatment++;
                    }
                }
            } else {
                foreach ($Row as $keyName => $datum) {
                    if (in_array($keyName, array('PERCENT1', 'PERCENT2', 'PERCENT3')) === true) {
                        continue;
                    }

                    $val = ($Row[$keyName] == "")? "0" : $Row[$keyName] ;
                    $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" ";
                    $textBoxArray[$keyName] = knjCreateTextBox($objForm, $val, $keyName, $setSize, $setSize, $disabled.$extra, "1");
                }

                //計算値は個別に計算して表示項目としてセット
                $textBoxArray['PERCENT1'] = sprintf('%.1f%%', ($Row['TARGETS1'] > 0)? (round($Row['EXAMINEE1'] * 100 / (double)$Row['TARGETS1'], 1)): 0);
                $textBoxArray['PERCENT2'] = sprintf('%.1f%%', ($Row['TARGETS2'] > 0)? (round($Row['EXAMINEE2'] * 100 / (double)$Row['TARGETS2'], 1)): 0);
                $textBoxArray['PERCENT3'] = sprintf('%.1f%%', ($Row['TARGETS3'] > 0)? (round($Row['EXAMINEE3'] * 100 / (double)$Row['TARGETS3'], 1)): 0);
            }

            $targets1++;
        }

        if ($model->cmd == "recalc") {
            $extra                            = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" ";
            $textBoxArray["EDBOARD_SCHOOLCD"] = knjCreateTextBox($objForm, $model->schoolcd, "EDBOARD_SCHOOLCD", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["YEAR"]             = knjCreateTextBox($objForm, CTRL_YEAR, "YEAR", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["TARGETS1"]         = knjCreateTextBox($objForm, $targets1, "TARGETS1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["EXAMINEE1"]        = knjCreateTextBox($objForm, $examinee1, "EXAMINEE1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["REMARK1"]          = knjCreateTextBox($objForm, $remark1, "REMARK1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["URICSUGAR1"]       = knjCreateTextBox($objForm, $uricsugar1, "URICSUGAR1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["ALBUMINURIA1"]     = knjCreateTextBox($objForm, $albuminuria1, "ALBUMINURIA1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["URICBLEED1"]       = knjCreateTextBox($objForm, $uricbleed1, "URICBLEED1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["TARGETS2"]         = knjCreateTextBox($objForm, $targets2, "TARGETS2", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["EXAMINEE2"]        = knjCreateTextBox($objForm, $examinee2, "EXAMINEE2", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["TARGETS3"]         = knjCreateTextBox($objForm, $targets3, "TARGETS3", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["EXAMINEE3"]        = knjCreateTextBox($objForm, $examinee3, "EXAMINEE3", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["NORMAL"]           = knjCreateTextBox($objForm, $normal, "NORMAL", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["CAREFUL"]          = knjCreateTextBox($objForm, $careful, "CAREFUL", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["TREATMENT"]        = knjCreateTextBox($objForm, $treatment, "TREATMENT", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["PERCENT1"]         = sprintf('%.1f%%', ($targets1 > 0)? (round($examinee1 * 100 / (double)$targets1, 1)): 0);
            $textBoxArray["PERCENT2"]         = sprintf('%.1f%%', ($targets2 > 0)? (round($examinee2 * 100 / (double)$targets2, 1)): 0);
            $textBoxArray["PERCENT3"]         = sprintf('%.1f%%', ($targets3 > 0)? (round($examinee3 * 100 / (double)$targets3, 1)): 0);
        }

        $arg["data"][] = $textBoxArray;

        //報告済み日付
        $tempLastHoukokuDate = $db2->getOne(knjf341Query::getMaxFixedDate($model));
        $lastHoukokuDate = '';
        if ($tempLastHoukokuDate != '') {
            $tempSplitted = explode('-', $tempLastHoukokuDate);
            $lastHoukokuDate = '＜' . (int)$tempSplitted[0] . '年' . (int)$tempSplitted[1] . '月' . (int)$tempSplitted[2] . '日　報告済＞';
        }
        $arg["EXE_DATES"] = $lastHoukokuDate;

        //県への報告
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告", $extra);

        //確定データ
        $query = knjf341Query::getFixed($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "FIXED_DATA", $model->fixedData, $extra, 1, 1);

        //再計算ボタン
        $extra = $disabled."onclick=\"return btn_submit('recalc');\"";
        $arg["btn_recalc"] = knjCreateBtn($objForm, "btn_recalc", "再計算", $extra);
        //更新ボタン
        $extra = $disabled."onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = $disabled."onclick=\"return btn_submit('reset');\"";
        $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return OnClose();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "FIXED_DATE");
        knjCreateHidden($objForm, "UPDATED[]", $model->updated);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "PRGID", "KNJF341");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjf341Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank != "") {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["LABEL"] = $name == "FIXED_DATA" ? str_replace("-", "/", $row["LABEL"]) : $row["LABEL"];
        $row["VALUE"] = $name == "FIXED_DATA" ? str_replace("-", "/", $row["VALUE"]) : $row["VALUE"];
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
