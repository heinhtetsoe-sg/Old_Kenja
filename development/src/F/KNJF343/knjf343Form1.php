<?php

require_once('for_php7.php');

class knjf343Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf343index.php", "", "edit");

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //教育委員会判定
        $query = knjf343Query::z010Abbv1();
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

        $target1     = 0;  //一次対象者数
        $examinee1   = 0;  //一次受検者数
        $haveRemark1 = 0;  //有所見者数
        $Remark1Cnt1 = 0;  //要精検
        $Remark1Cnt2 = 0;  //主治医管理
        $Remark1Cnt3 = 0;  //放置可
        $Remark1Cnt4 = 0;  //その他
        $target2     = 0;  //精密検査該当者
        $examinee2   = 0;  //精密検査受験者数
        $Remark2Cnt1 = 0;  //異常なし
        $Remark2Cnt2 = 0;  //要医療
        $Remark2Cnt3 = 0;  //要観察
        $Remark2Cnt4 = 0;  //放置可
        $Remark2Cnt5 = 0;  //その他

        $query = knjf343Query::readQuery($model);
        $result = $db->query($query);
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setSize = 4;
            $textBoxArray = array();

            if ($model->cmd == "recalc") {
                if ($Row["HEART_MEDEXAM"] != null && $Row["HEART_MEDEXAM"] != "00") {
                    //一次受検者数
                    $examinee1++;

                    if ($Row["HEART_MEDEXAM"] != "01") {
                        //有所見者数
                        $haveRemark1++;
                    }
                }
                if ($Row["HEART_MEDEXAM"] == "02") {
                    //要精検
                    $Remark1Cnt1++;
                    //精密検査該当者
                    $target2++;
                } elseif ($Row["HEART_MEDEXAM"] == "04") {
                    //主治医管理
                    $Remark1Cnt2++;
                } elseif ($Row["HEART_MEDEXAM"] == "03") {
                    //放置可
                    $Remark1Cnt3++;
                } elseif ($Row["HEART_MEDEXAM"] == "99") {
                    //その他
                    $Remark1Cnt4++;
                }
                if ($Row["MANAGEMENT_DIV"] != null && $Row["MANAGEMENT_DIV"] != "00") {
                    //精密検査受験者数
                    $examinee2++;
                }
                if ($Row["MANAGEMENT_DIV"] == "01") {
                    //異常なし
                    $Remark2Cnt1++;
                } elseif ($Row["MANAGEMENT_DIV"] == "02") {
                    //要医療
                    $Remark2Cnt2++;
                } elseif ($Row["MANAGEMENT_DIV"] == "03") {
                    //要観察
                    $Remark2Cnt3++;
                } elseif ($Row["MANAGEMENT_DIV"] == "04") {
                    //放置可
                    $Remark2Cnt4++;
                } elseif ($Row["MANAGEMENT_DIV"] == "99") {
                    //その他
                    $Remark2Cnt5++;
                }
            } else {
                foreach ($Row as $keyName => $datum) {
                    if (in_array($keyName, array('PERCENT1', 'HAVE_REMARK_PERCENT', 'PERCENT2')) === true) {
                        continue;
                    }

                    $val = ($Row[$keyName] == "")? "0" : $Row[$keyName] ;
                    $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" ";
                    $textBoxArray[$keyName] = knjCreateTextBox($objForm, $val, $keyName, $setSize, $setSize, $disabled.$extra, "1");
                }

                //計算値は個別に計算して表示項目としてセット
                $textBoxArray['PERCENT1']            = sprintf('%.1f%%', ($Row['TARGET1']   > 0)? (round($Row['EXAMINEE1']    * 100 / (double)$Row['TARGET1'], 1)): 0);
                $textBoxArray['HAVE_REMARK_PERCENT'] = sprintf('%.1f%%', ($Row['EXAMINEE1'] > 0)? (round($Row['HAVE_REMARK1'] * 100 / (double)$Row['EXAMINEE1'], 1)): 0);
                $textBoxArray['PERCENT2']            = sprintf('%.1f%%', ($Row['TARGET2']   > 0)? (round($Row['EXAMINEE2']    * 100 / (double)$Row['TARGET2'], 1)): 0);
            }

            $target1++;
        }

        if ($model->cmd == "recalc") {
            $extra                               = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" ";
            $textBoxArray["EDBOARD_SCHOOLCD"]    = knjCreateTextBox($objForm, $model->schoolcd, "EDBOARD_SCHOOLCD", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["YEAR"]                = knjCreateTextBox($objForm, CTRL_YEAR, "YEAR", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["TARGET1"]             = knjCreateTextBox($objForm, $target1, "TARGET1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["EXAMINEE1"]           = knjCreateTextBox($objForm, $examinee1, "EXAMINEE1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["HAVE_REMARK1"]        = knjCreateTextBox($objForm, $haveRemark1, "HAVE_REMARK1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["REMARK1_CNT1"]        = knjCreateTextBox($objForm, $Remark1Cnt1, "REMARK1_CNT1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["REMARK1_CNT2"]        = knjCreateTextBox($objForm, $Remark1Cnt2, "REMARK1_CNT2", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["REMARK1_CNT3"]        = knjCreateTextBox($objForm, $Remark1Cnt3, "REMARK1_CNT3", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["REMARK1_CNT4"]        = knjCreateTextBox($objForm, $Remark1Cnt4, "REMARK1_CNT4", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["TARGET2"]             = knjCreateTextBox($objForm, $target2, "TARGET2", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["EXAMINEE2"]           = knjCreateTextBox($objForm, $examinee2, "EXAMINEE2", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["REMARK2_CNT1"]        = knjCreateTextBox($objForm, $Remark2Cnt1, "REMARK2_CNT1", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["REMARK2_CNT2"]        = knjCreateTextBox($objForm, $Remark2Cnt2, "REMARK2_CNT2", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["REMARK2_CNT3"]        = knjCreateTextBox($objForm, $Remark2Cnt3, "REMARK2_CNT3", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["REMARK2_CNT4"]        = knjCreateTextBox($objForm, $Remark2Cnt4, "REMARK2_CNT4", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["REMARK2_CNT5"]        = knjCreateTextBox($objForm, $Remark2Cnt5, "REMARK2_CNT5", $setSize, $setSize, $disabled.$extra, "1");
            $textBoxArray["PERCENT1"]            = sprintf('%.1f%%', ($target1     > 0)? (round($examinee1   * 100 / (double)$target1, 1)): 0);
            $textBoxArray["HAVE_REMARK_PERCENT"] = sprintf('%.1f%%', ($examinee1   > 0)? (round($haveRemark1 * 100 / (double)$examinee1, 1)): 0);
            $textBoxArray["PERCENT2"]            = sprintf('%.1f%%', ($target2     > 0)? (round($examinee2   * 100 / (double)$target2, 1)): 0);
        }

        $arg["data"][] = $textBoxArray;

        //報告済み日付
        $tempLastHoukokuDate = $db2->getOne(knjf343Query::getMaxFixedDate($model));
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
        $query = knjf343Query::getFixed($model);
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
        knjCreateHidden($objForm, "PRGID", "KNJF343");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjf343Form1.html", $arg);
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
