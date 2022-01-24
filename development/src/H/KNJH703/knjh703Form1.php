<?php

require_once('for_php7.php');

class knjh703Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh703index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年組コンボ
        $query = knjh703Query::getHrClass($model);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade_hr_class, "GRADE_HR_CLASS", $extra, 1);

        //データ取得
        $schList = array();
        if (!isset($model->warning) && $model->grade_hr_class != "") {
            //生徒一覧
            $query = knjh703Query::getStudentInfo($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schList[] = $row;
            }
            $result->free();
        }

        //更新時のチェックでエラーの場合、画面情報をセット
        if (isset($model->warning)) {
            //生徒一覧
            for ($counter = 0; $counter < $model->data_cnt; $counter++) {
                $row = array();
                foreach ($model->fields as $key => $val) {
                    $row[$key] = $val[$counter];
                }
                $schList[] = $row;
            }
        }
        if ($model->cmd == "bunri") {
            $schList = array();
            //生徒一覧
            for ($counter = 0; $counter < $model->data_cnt; $counter++) {
                $row = array();
                foreach ($model->fields as $key => $val) {
                    $row[$key] = $val[$counter];
                }
                $schList[] = $row;
            }
        }

        //生徒一覧の表示件数
        knjCreateHidden($objForm, "DATA_CNT", get_count($schList));

        //生徒一覧を表示
        foreach ($schList as $counter => $row) {
            knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);

            //学年
            $setData["GRADE"] = preg_replace('/^[0]/', '', $row["GRADE"]);
            knjCreateHidden($objForm, "GRADE"."-".$counter, $row["GRADE"]);

            //組
            $setData["HR_CLASS"] = $row["HR_CLASS"];
            knjCreateHidden($objForm, "HR_CLASS"."-".$counter, $row["HR_CLASS"]);

            //出席番号
            $setData["ATTENDNO"] = preg_replace('/^[0]/', '', $row["ATTENDNO"]);
            knjCreateHidden($objForm, "ATTENDNO"."-".$counter, $row["ATTENDNO"]);

            //氏名
            $setData["NAME"] = $row["NAME"];
            knjCreateHidden($objForm, "NAME"."-".$counter, $row["NAME"]);

            //性別
            $setData["SEX"] = $row["SEX"];
            knjCreateHidden($objForm, "SEX"."-".$counter, $row["SEX"]);

            //文理区分コンボボックス
            $query = knjh703Query::getNameMst("H319");
            $extra = " onChange=\"changeSubclassBunri()\"";
            $setData["BUNRIDIV"] = makeCmbReturn($objForm, $arg, $db, $query, $row["BUNRIDIV"], "BUNRIDIV"."-".$counter, $extra, 1);
            knjCreateHidden($objForm, "H_BUNRIDIV"."-".$counter, $row["BUNRIDIV"]);

            //選択科目コンボボックス
            $query = knjh703Query::getSubclass($row["BUNRIDIV"]);
            $extra = "";
            $setData["SUBCLASSCD"] = makeCmbReturn($objForm, $arg, $db, $query, $row["SUBCLASSCD"], "SUBCLASSCD"."-".$counter, $extra, 1);
            knjCreateHidden($objForm, "H_SUBCLASSCD"."-".$counter, $row["SUBCLASSCD"]);

            //辞退チェックボックス
            $extra  = ($row["DECLINE_FLG"] == "1")? "checked" : "";
            $extra .= " id=\"DECLINE_FLG\"";
            $setData["DECLINE_FLG"] = knjCreateCheckBox($objForm, "DECLINE_FLG"."-".$counter, "1", $extra, "");
            knjCreateHidden($objForm, "H_DECLINE_FLG"."-".$counter, $row["DECLINE_FLG"]);

            $arg["data"][] = $setData;
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

        View::toHTML($model, "knjh703Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeCmbReturn(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //一括更新ボタン
    $link = REQUESTROOT."/H/KNJH703/knjh703index.php?cmd=replace&GRADE_HR_CLASS=".$model->grade_hr_class;
    $extra = "onclick=\"Page_jumper('$link');\"";
    $arg["button"]["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "H_GRADE_HR_CLASS");
}
