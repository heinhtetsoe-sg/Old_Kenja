<?php

require_once('for_php7.php');

class knjf016Form1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knjf016index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //年組コンボ
        $extra = "onChange=\"btn_submit('edit');\"";
        if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $query = knjf016Query::getGhrCd($model);
            $arg["HR_CLASS_TITLE"] = "実クラス";
        } else if ($model->Properties["useFi_Hrclass"] == "1") {
            $query = knjf016Query::getFiGradeHrclass($model);
            $arg["HR_CLASS_TITLE"] = "FIクラス";
        } else {
            $query = knjf016Query::getHrClass($model);
            $arg["HR_CLASS_TITLE"] = "年組";
        }
        $arg["HR_CLASS"] = makeCmbReturn($objForm, $arg, $db, $query, $model->hr_class, "HR_CLASS", $extra, 1, "BLANK");

        //回数コンボ
        $opt = array();
        for ($i = 1; $i <= $model->Properties["KenkouSindanMaxNo"]; $i++) {
            $opt[] = array('label' => $i.'回目', 'value' => $i);
        }
        $model->no = ($model->no) ? $model->no : $opt[0]["value"];
        $extra = ($model->hr_class) ? "onchange=\"return btn_submit('edit');\"" : "disabled";
        $arg["NO"] = knjCreateCombo($objForm, "NO", $model->no, $opt, $extra, 1);

        //一覧を取得
        $medexamList = array();
        if (!isset($model->warning) && $model->hr_class != "") {
            if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
                $query = knjf016Query::getMedexamListSS($model);
            } else if ($model->Properties["useFi_Hrclass"] == "1") {
                $query = knjf016Query::getMedexamListFi($model);
            } else {
                $query = knjf016Query::getMedexamList($model);
            }
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $medexamList[] = $row;
            }
            $result->free();
        }

        //更新・削除時のチェックでエラーの場合、画面情報をセット
        if (isset($model->warning)) {
            for ($counter = 0; $counter < $model->data_cnt; $counter++) {
                $Row = array();
                foreach ($model->fields as $key => $val) {
                    $Row[$key] = $val[$counter];
                }
                $medexamList[] = $Row;
            }
        }

        //データ一覧の表示件数
        knjCreateHidden($objForm, "DATA_CNT", get_count($medexamList));

        //一覧を表示
        foreach ($medexamList as $counter => $Row) {
            //学籍番号(KEY)
            knjCreateHidden($objForm, "REGD_SCHREGNO"."-".$counter, $Row["REGD_SCHREGNO"]);

            //学校種別(P,J,H)
            knjCreateHidden($objForm, "SCHOOL_KIND"."-".$counter, $Row["SCHOOL_KIND"]);

            //出席番号(No.)
            $setData["ATTENDNO"] = $Row["ATTENDNO"];
            knjCreateHidden($objForm, "ATTENDNO"."-".$counter, $Row["ATTENDNO"]);

            //氏名
            $setData["NAME_SHOW"] = $Row["NAME_SHOW"];
            knjCreateHidden($objForm, "NAME_SHOW"."-".$counter, $Row["NAME_SHOW"]);

            //健康診断実施日付
            $Row["DATE"] = str_replace("-", "/", $Row["DATE"]);
            $setData["DATE"] = View::popUpCalendar($objForm, "DATE"."-".$counter, $Row["DATE"]);
            knjCreateHidden($objForm, "HIDDENDATE"."-".$counter, $Row["DATE"]);

            //身長
            $id = "height-".$counter;
            $extra = "onblur=\"return Num_Check(this);\" id=\"{$id}\"";
            $setData["HEIGHT"] = knjCreateTextBox($objForm, $Row["HEIGHT"], "HEIGHT"."-".$counter, 5, 5, $extra);

            //体重
            $id = "weight-".$counter;
            $extra = "onblur=\"return Num_Check(this);\" id=\"{$id}\"";
            $setData["WEIGHT"] = knjCreateTextBox($objForm, $Row["WEIGHT"], "WEIGHT"."-".$counter, 5, 5, $extra);

            //座高
            $extra = "onblur=\"return Num_Check(this);\"";
            $setData["SITHEIGHT"] = knjCreateTextBox($objForm, $Row["SITHEIGHT"], "SITHEIGHT"."-".$counter, 5, 5, $extra);

            //視力
            $vision_array = array("R_BAREVISION_MARK", "L_BAREVISION_MARK", "R_VISION_MARK", "L_VISION_MARK");
            foreach ($vision_array as $vision) {
                $extra = "style=\"text-align:center;\" onblur=\"return Mark_Check(this);\"";
                $setData[$vision] = knjCreateTextBox($objForm, $Row[$vision], $vision."-".$counter, 1, 1, $extra);
            }

            $arg["data"][] = $setData;

            knjCreateHidden($objForm, "UPDATED"."-".$counter, $Row["UPDATED"]);

        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjf016Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmbReturn(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") $opt[] = array ("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//配列作成
function makeArrayReturn($db, $query) {
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    return $opt;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    if ($model->no == "1") {
        $extra .= " disabled ";
    }
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "H_HR_CLASS");
    knjCreateHidden($objForm, "H_NO");
}
?>
