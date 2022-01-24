<?php

require_once('for_php7.php');

class knjf014Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf014index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        $tyousei2_height  = "70";
        $data_width_width = "240";

        //特別支援学校
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $tyousei2_height = "100";
            $data_width_width = "600";
            $arg["useSpecial_Support_School"] = "1";
        }
        $arg["tyousei2_height"]  = $tyousei2_height;
        $arg["data_width_width"] = $data_width_width;

        //学期
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjf014Query::getSemester($model);
        $arg["SEMESTER"] = makeCmbReturn($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //年組
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjf014Query::getHrClass($model);
        $arg["HR_CLASS"] = makeCmbReturn($objForm, $arg, $db, $query, $model->hr_class, "HR_CLASS", $extra, 1, "BLANK");

        //種類
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjf014Query::getMonth($model);
        $arg["MONTH"] = makeCmbReturn($objForm, $arg, $db, $query, $model->month, "MONTH", $extra, 1, "");

        //一覧を取得
        $medexamList = array();
        if (!isset($model->warning) && $model->hr_class != "") {
            $query = knjf014Query::getMedexamMonthList($model);
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

        //視力コンボ用
        $queryF017 = knjf014Query::getNameMst($model, "F017", "4");

        //講座一覧の表示件数
        knjCreateHidden($objForm, "DATA_CNT", get_count($medexamList));

        //一覧を表示
        foreach ($medexamList as $counter => $Row) {

            //学籍番号(KEY)
            knjCreateHidden($objForm, "REGD_SCHREGNO"."-".$counter, $Row["REGD_SCHREGNO"]);
            //学校種別(P,J,H)
            knjCreateHidden($objForm, "SCHOOL_KIND"."-".$counter, $Row["SCHOOL_KIND"]);

            //出席番号(No.)
            //氏名
            $setData["ATTENDNO"] = $Row["ATTENDNO"];
            $setData["NAME_SHOW"] = $Row["NAME_SHOW"];
            knjCreateHidden($objForm, "ATTENDNO"."-".$counter, $Row["ATTENDNO"]);
            knjCreateHidden($objForm, "NAME_SHOW"."-".$counter, $Row["NAME_SHOW"]);

            //身長
            //体重
            $extra = "onblur=\"return Num_Check(this);\"";
            $setData["HEIGHT"] = knjCreateTextBox($objForm, $Row["HEIGHT"], "HEIGHT"."-".$counter, 5, 5, $extra);
            $setData["WEIGHT"] = knjCreateTextBox($objForm, $Row["WEIGHT"], "WEIGHT"."-".$counter, 5, 5, $extra);

            //視力・右裸眼(文字)
            //視力・左裸眼(文字)
            //視力・右矯正(文字)
            //視力・左矯正(文字)
            $extra = "";
            $setData["R_BAREVISION_MARK"] = makeCmbReturn($objForm, $arg, $db, $queryF017, $Row["R_BAREVISION_MARK"], "R_BAREVISION_MARK"."-".$counter, $extra, 1, "BLANK");
            $setData["L_BAREVISION_MARK"] = makeCmbReturn($objForm, $arg, $db, $queryF017, $Row["L_BAREVISION_MARK"], "L_BAREVISION_MARK"."-".$counter, $extra, 1, "BLANK");
            $setData["R_VISION_MARK"]     = makeCmbReturn($objForm, $arg, $db, $queryF017, $Row["R_VISION_MARK"], "R_VISION_MARK"."-".$counter, $extra, 1, "BLANK");
            $setData["L_VISION_MARK"]     = makeCmbReturn($objForm, $arg, $db, $queryF017, $Row["L_VISION_MARK"], "L_VISION_MARK"."-".$counter, $extra, 1, "BLANK");

            //視力・右裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_BAREVISION-{$counter}\"";
            $setData["R_BAREVISION"] = knjCreateTextBox($objForm, $Row["R_BAREVISION"], "R_BAREVISION"."-".$counter, 5, 5, $extra);
            //視力・右矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"R_VISION-{$counter}\"";
            $setData["R_VISION"]     = knjCreateTextBox($objForm, $Row["R_VISION"],     "R_VISION"."-".$counter,     5, 5, $extra);
            //視力・左裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_BAREVISION-{$counter}\"";
            $setData["L_BAREVISION"] = knjCreateTextBox($objForm, $Row["L_BAREVISION"], "L_BAREVISION"."-".$counter, 5, 5, $extra);
            //視力・左矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\" id=\"L_VISION-{$counter}\"";
            $setData["L_VISION"]     = knjCreateTextBox($objForm, $Row["L_VISION"],     "L_VISION"."-".$counter,     5, 5, $extra);

            $setData["COUNTER"] = $counter;

           //----------------------------------------------

            $arg["data"][] = $setData;

            knjCreateHidden($objForm, "UPDATED"."-".$counter, $Row["UPDATED"]);

        } //foreach

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjf014Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmbReturn(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

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
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    $btnSize = "";
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$btnSize);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$btnSize);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra.$btnSize);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "H_HR_CLASS");
    knjCreateHidden($objForm, "H_MONTH");
    knjCreateHidden($objForm, "H_SEMESTER");
}
?>
