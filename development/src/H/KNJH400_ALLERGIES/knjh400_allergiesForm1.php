<?php

require_once('for_php7.php');

class knjh400_allergiesForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjh400_allergiesindex.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjh400_allergiesQuery::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$model->schregno.'　'.$name;

        //年度コンボ
        $query = knjh400_allergiesQuery::getYearList($model);
        $extra = "onchange=\"return btn_mit('form1')\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)) {
            //データ取得 -- MEDEXAM_CARE_HDAT
            $Row = $db->getRow(knjh400_allergiesQuery::getCareHData($model, $model->field["YEAR"], "01"), DB_FETCHMODE_ASSOC);
            //データ取得 -- MEDEXAM_CARE_DAT
            $query = knjh400_allergiesQuery::getCareData($model, $model->field["YEAR"], "01");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["CHECK".$row["CARE_KIND"]."_".$row["CARE_ITEM"]."_".$row["CARE_SEQ"]] = 1;
                if (strlen($row["CARE_REMARK1"]) > 0) {
                    $Row["TEXT".$row["CARE_KIND"]."_".$row["CARE_ITEM"]."_".$row["CARE_SEQ"]] = $row["CARE_REMARK1"];
                }
            }
            $result->free();
        } else {
            $Row =& $model->field;
        }

        //項目情報
        $cnt = array();
        $cnt[1][1] = array("cnt" => 4,  "txt" => array());
        $cnt[1][2] = array("cnt" => 4,  "txt" => array(4));
        $cnt[1][3] = array("cnt" => 4,  "txt" => array(4));
        $cnt[1][4] = array("cnt" => 2,  "txt" => array());
        $cnt[2][1] = array("cnt" => 3,  "txt" => array());
        $cnt[2][2] = array("cnt" => 3,  "txt" => array(3));
        $cnt[2][3] = array("cnt" => 2,  "txt" => array());
        $cnt[2][4] = array("cnt" => 0,  "txt" => array());

        //種別
        for ($i=1; $i <= 2; $i++) {
            //項目
            for ($j=1; $j <= 4; $j++) {
                if ($cnt[$i][$j]["cnt"] == 0) {
                    continue;
                }
                //SEQ
                for ($k=1; $k <= $cnt[$i][$j]["cnt"]; $k++) {
                    $cd = sprintf("%02d", $i)."_".sprintf("%02d", $j)."_".sprintf("%02d", $k);

                    //チェックボックス
                    $name = "CHECK".$cd;
                    $extra = ($Row[$name] == "1") ? "checked" : "";
                    $extra .= " id=\"{$name}\" onclick=\"OptionUse(this);\"";
                    $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra, "");

                    if (in_array($k, $cnt[$i][$j]["txt"])) {
                        //テキスト
                        $name = "TEXT".$cd;
                        $moji = ($i == 2) ? 40 : 20;
                        $extra = ($Row["CHECK".$cd] == 1) ? "" : "disabled";
                        $arg["data"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, ((int)$moji * 2), ((int)$moji * 2), $extra);
                    }
                }
            }
        }

        //急性発作時の対応テキスト
        $height = 15 * 13.5 + (15 - 1) * 3 + 5;
        $arg["data"]["TEXT01_05_00"] = KnjCreateTextArea($objForm, "TEXT01_05_00", 15, (20 * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row["TEXT01_05_00"]);
        //その他の配慮・管理事項テキスト
        $height = 7 * 13.5 + (7 - 1) * 3 + 5;
        $arg["data"]["TEXT02_04_00"] = KnjCreateTextArea($objForm, "TEXT02_04_00", 7, (40 * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row["TEXT02_04_00"]);

        //extra
        $extra = "onBlur=\"this.value=toTelNo(this.value);\"";

        //（緊急時連絡先）保護者名
        $arg["data"]["EMERGENCYNAME"] = knjCreateTextBox($objForm, $Row["EMERGENCYNAME"], "EMERGENCYNAME", (10 * 2), (10 * 2), "");
        //（緊急時連絡先）保護者電話番号
        $arg["data"]["EMERGENCYTELNO"] = knjCreateTextBox($objForm, $Row["EMERGENCYTELNO"], "EMERGENCYTELNO", 14, 14, $extra);
        //（緊急時連絡先）医療機関名
        $arg["data"]["EMERGENCYNAME2"] = knjCreateTextBox($objForm, $Row["EMERGENCYNAME2"], "EMERGENCYNAME2", (40 * 2), (40 * 2), "");
        //（緊急時連絡先）医療機関電話番号
        $arg["data"]["EMERGENCYTELNO2"] = knjCreateTextBox($objForm, $Row["EMERGENCYTELNO2"], "EMERGENCYTELNO2", 14, 14, $extra);

        //記載日
        $date = str_replace("-", "/", $Row["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $date);
        //医師名
        $arg["data"]["DOCTOR"] = knjCreateTextBox($objForm, $Row["DOCTOR"], "DOCTOR", (10 * 2), (10 * 2), "");
        //医療機関名
        $arg["data"]["HOSPITAL"] = knjCreateTextBox($objForm, $Row["HOSPITAL"], "HOSPITAL", (40 * 2), (40 * 2), "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh400_allergiesForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
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
function makeBtn(&$objForm, &$arg, $model)
{
    //前年度からコピーボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->field["YEAR"] == CTRL_YEAR) ? "onclick=\"return btn_mit('form1_copy');\"" : "disabled";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_mit('form1_update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_mit('form1_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
}
