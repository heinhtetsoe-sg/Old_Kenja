<?php

require_once('for_php7.php');

class knjh400_allergiesForm3
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form3", "POST", "knjh400_allergiesindex.php", "", "form3");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjh400_allergiesQuery::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$model->schregno.'　'.$name;

        //年度コンボ
        $query = knjh400_allergiesQuery::getYearList($model);
        $extra = "onchange=\"return btn_mit('form3')\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)) {
            //データ取得 -- MEDEXAM_CARE_HDAT
            $Row = $db->getRow(knjh400_allergiesQuery::getCareHData($model, $model->field["YEAR"], "03"), DB_FETCHMODE_ASSOC);
            //データ取得 -- MEDEXAM_CARE_DAT
            $query = knjh400_allergiesQuery::getCareData($model, $model->field["YEAR"], "03");
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
        $cnt[1][1] = array("cnt" => 5,  "txt" => array(5));
        $cnt[1][2] = array("cnt" => 4,  "txt" => array(4));
        $cnt[2][1] = array("cnt" => 3,  "txt" => array());
        $cnt[2][2] = array("cnt" => 2,  "txt" => array());

        //種別
        for ($i=1; $i <= 2; $i++) {
            //項目
            for ($j=1; $j <= 2; $j++) {
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
                        $moji = 20;
                        $extra = ($Row["CHECK".$cd] == 1) ? "" : "disabled";
                        $arg["data"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, ((int)$moji * 2), ((int)$moji * 2), $extra);
                    }
                }
            }
        }

        //その他の配慮・管理事項
        $height = 2 * 13.5 + (2 - 1) * 3 + 5;
        $arg["data"]["TEXT02_03_00"] = KnjCreateTextArea($objForm, "TEXT02_03_00", 2, (40 * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row["TEXT02_03_00"]);

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
        View::toHTML($model, "knjh400_allergiesForm3.html", $arg);
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
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->field["YEAR"] == CTRL_YEAR) ? "onclick=\"return btn_mit('form3_copy');\"" : "disabled";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_mit('form3_update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_mit('form3_clear');\"";
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
    knjCreateHidden($objForm, "RELANO", $model->relano);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
}
