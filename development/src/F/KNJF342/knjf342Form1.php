<?php

require_once('for_php7.php');

class knjf342Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf342index.php", "", "edit");

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //教育委員会判定
        $query = knjf342Query::z010Abbv1();
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

        $rowCount = 0;
        $query = knjf342Query::readQuery($model);
        $result = $db->query($query);
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //画面に表示する値
            $textArray['ATTEND_NAME']         = $Row['ATTEND_NAME'];
            $textArray['NAME']                = $Row['NAME'];
            $textArray['URI2_DIV_NAME']       = $Row['URI2_DIV_NAME'];
            $textArray['URI2_REMARK']         = $Row['URI2_REMARK'];
            $textArray['URI2_SIDOU_DIV_NAME'] = $Row['URI2_SIDOU_DIV_NAME'];
            $arg["data"][] = $textArray;

            //submit用の値
            $rowPrefix = sprintf('%08d__', $rowCount);
            knjCreateHidden($objForm, "{$rowPrefix}SCHREGNO", $Row['SCHREGNO']);
            knjCreateHidden($objForm, "{$rowPrefix}GRADE", $Row['GRADE']);
            knjCreateHidden($objForm, "{$rowPrefix}NAME", $Row['NAME']);
            knjCreateHidden($objForm, "{$rowPrefix}URI2_DIV_NAME", $Row['URI2_DIV_NAME']);
            knjCreateHidden($objForm, "{$rowPrefix}URI2_REMARK", $Row['URI2_REMARK']);
            knjCreateHidden($objForm, "{$rowPrefix}URI2_SIDOU_DIV_NAME", $Row['URI2_SIDOU_DIV_NAME']);

            $rowCount++;
        }

        //報告済み日付
        $tempLastHoukokuDate = $db2->getOne(knjf342Query::getMaxFixedDate($model));
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
        $query = knjf342Query::getFixed($model);
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
        knjCreateHidden($objForm, "PRGID", "KNJF342");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "ROW_COUNT", $rowCount);//データ行数

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjf342Form1.html", $arg);
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
