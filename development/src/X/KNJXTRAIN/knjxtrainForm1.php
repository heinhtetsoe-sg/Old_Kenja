<?php

require_once('for_php7.php');

class knjxtrainForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "knjxtrainindex.php", "", "detail");
        
        //データ取得
        $row = knjxtrainQuery::getSchreg_Envir_dat($model);

        //DB OPEN
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //学籍番号
        $arg["SCHREGNO"] = $model->schregno;

        //氏名
        $query = knjxtrainQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["NAME"] = $schName;

        //通学：所要時間
        $extra = "onBlur=\"return to_Integer(this);\"";
        $arg["data"]["COMMUTE_HOURS"] = knjCreateTextBox($objForm, $row["COMMUTE_HOURS"], "COMMUTE_HOURS", 2, 2, $extra);

        //通学：所要分
        $extra = "onBlur=\"return to_Integer(this);\"";
        $arg["data"]["COMMUTE_MINUTES"] = knjCreateTextBox($objForm, $row["COMMUTE_MINUTES"], "COMMUTE_MINUTES", 2, 2, $extra);

        //通学方法
        $query = knjxtrainQuery::getV_name_mst("H100",$model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "HOWTOCOMMUTECD", $row["HOWTOCOMMUTECD"], $extra, 1, "0");

        //途中経由駅
        for ($i = 1; $i <= 7; $i++) {
            $josya = "JOSYA_" . $i;
            $rosen = "ROSEN_" . $i;
            $gesya = "GESYA_" . $i;
            $flg   = "FLG_" . $i;
            $extra = " class=\"right_side\" style=\"width:270px;\"";

            $hidden_josya = "HIDDEN_JOSYA_" . $i;
            $hidden_rosen = "HIDDEN_ROSEN_" . $i;
            $hidden_gesya = "HIDDEN_GESYA_" . $i;


            if ($row[$flg] == '1') {
                $josya_cd = $row[$hidden_josya] ? $row[$hidden_josya] : $row[$josya];
                $rosen_cd = $row[$hidden_rosen] ? $row[$hidden_rosen] : $row[$rosen];
                $gesya_cd = $row[$hidden_gesya] ? $row[$hidden_gesya] : $row[$gesya];

                $query = knjxtrainQuery::getStationName($josya_cd);
                list($josya_mei,$rosen_mei) = $db->getRow($query);
                $query = knjxtrainQuery::getStationName($gesya_cd);
                list($gesya_mei,$rosen_mei) = $db->getRow($query);
            } else {
                $josya_mei = $row[$josya];
                $rosen_mei = $row[$rosen];
                $gesya_mei = $row[$gesya];
            }

            $arg["data"][$josya] = knjCreateTextBox($objForm, $josya_mei, $josya, 20, 20, $extra);
            $arg["data"][$rosen] = knjCreateTextBox($objForm, $rosen_mei, $rosen, 20, 20, $extra);
            $arg["data"][$gesya] = knjCreateTextBox($objForm, $gesya_mei, $gesya, 20, 20, $extra);
            knjCreateHidden($objForm, $flg, $row[$flg]);
            knjCreateHidden($objForm, $hidden_josya, $josya_cd);
            knjCreateHidden($objForm, $hidden_rosen, $rosen_cd);
            knjCreateHidden($objForm, $hidden_gesya, $gesya_cd);
        }

        //終了ボタンを作成する
        if ($model->buttonFlg) {
            $extra = "onclick=\"closeWin()\"";
        } else {
            $extra = "onclick=\"return parent.closeit()\"";
        }
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJXTRAIN");
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxtrainForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
    } elseif ($blank == "0") {
        $opt[] = array('label' => "",
                       'value' => "0");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>