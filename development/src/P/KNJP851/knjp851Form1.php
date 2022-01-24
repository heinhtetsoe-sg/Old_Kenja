<?php

require_once('for_php7.php');
class knjp851Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //ＤＢ接続
        $db = Query::dbCheckOut();

        //対象年度
        $model->field["YEAR"] = ($model->field["YEAR"] != "") ? $model->field["YEAR"] : CTRL_YEAR;
        $extra = "";
        $query =  knjp851Query::getSchoolMstYearQuery($model);
        $arg["TOP"]["YEAR"] = makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, "1");
        //対象校種
        $extra = " onchange=\"return btn_submit('main');\" ";
        $query =  knjp851Query::getSchkind($model);
        $arg["TOP"]["SCHOOL_KIND"] = makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, "1", "ALL");
        //対象学年
        $extra = " onchange=\"return btn_submit('main');\" ";
        $query =  knjp851Query::getGrade($model);
        $arg["TOP"]["GRADE"] = makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, "1", "ALL");
        //対象クラス
        $extra = " onchange=\"return btn_submit('main');\" ";
        $query =  knjp851Query::getHrClass($model);
        $arg["TOP"]["HR_CLASS"] = makeCmb($objForm, $arg, $db, $query, $model->field["HR_CLASS"], "HR_CLASS", $extra, "1", "ALL");

        //対象種別コンボ
        $patternOpt = array();
        $patternOpt[]   = "入金計画・実績情報";
        $patternOpt[]   = "減免情報";
        $patternOpt[]   = "寮情報";
        $patternOpt[]   = "学校徴収金（預り金）情報";
        foreach ($patternOpt as $index => $label) {
            $value = $index + 1;
            $opt[] = array('value' => $value, 'label' => sprintf("%02d", $value)."：".$label);
        }
        $extra = " onchange=\"return btn_submit('main');\" ";
        $model->field["PATTERN_DIV"] = ($model->field["PATTERN_DIV"] != "") ? $model->field["PATTERN_DIV"] : "1";
        $arg["TOP"]["PATTERN_DIV"] = knjCreateCombo($objForm, "PATTERN_DIV", $model->field["PATTERN_DIV"], $opt, $extra, 1);

        $divDisable01 = "";
        if ($model->field["PATTERN_DIV"] != "1") {
            //対象種別が01以外は選択不可
            $divDisable01 = " disabled ";
        }

        //抽出基準ラジオ
        $opt = array(1, 2);
        $model->field["DATE_DIV"] = ($model->field["DATE_DIV"] == "") ? "1" : $model->field["DATE_DIV"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"DATE_DIV{$val}\" onchange=\"return btn_submit('');\" ".$divDisable01);
        }
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //抽出基準(詳細条件)
        for ($i = 1; $i <= 2; $i++) {
            $dateChkKey = "DATE_CHK{$i}";
            $extra = " id=\"{$dateChkKey}\" ";
            $checked = ($model->field["DATE_DIV"] == $i && $model->field[$dateChkKey] == '1') ? " checked " : "";
            $disabled = ($model->field["DATE_DIV"] != $i) ? " disabled " : "";
            $arg[$dateChkKey] = knjCreateCheckBox($objForm, $dateChkKey, "1", $checked.$extra.$disabled.$divDisable01);
        }

        if ($model->Properties["use_output_sum_by_schregno"] == "1") {
            //合計出力チェックボックス
            $arg["disp_chk_sum"] = "1";
            $checked = ($model->field["SUM_CHK"] == "1") ? " checked " : "";
            $extra = " id=\"SUM_CHK\" ";
            $arg["SUM_CHK"] = knjCreateCheckBox($objForm, "SUM_CHK", "1", $checked.$extra.$divDisable01);
        }


        //出力対象月全チェック
        $extra = " id=\"MONTH_CHK_ALL\" onclick=\"checkAll('monthchk')\"";
        $checked = $model->field["MONTH_CHK_ALL"] == '1' ? " checked " : "";
        $arg["MONTH_CHK_ALL"] = knjCreateCheckBox($objForm, "MONTH_CHK_ALL", $model->field["MONTH_CHK_ALL"], $checked.$extra.$divDisable01);

        //出力対象月チェックボックス
        $monthChkArray = array();
        for ($i = 4; $i <= 15; $i++) {
            $id = "MONTH_CHK_{$i}";
            $extra = " id=\"{$id}\" class=\"monthchk\" ";

            $monthChkArray["MONTH_ID"]  = $id;
            $monthVal = ($i <= 12) ? $i : $i - 12;
            $value = sprintf("%02d", $monthVal);
            $label = mb_convert_kana($monthVal, "N");
            $checked = (in_array($value, $model->selectedMonth)) ? " checked " : "";
            $monthChkArray["MONTH_CHK"] = knjCreateCheckBox($objForm, "MONTH_CHK", $value, $checked.$extra.$divDisable01);
            $monthChkArray["MONTH_VAL"] = $label;

            $arg["monthlist"][] = $monthChkArray;
        }

        //学校徴収金情報ラジオ
        $opt = array(1, 2);
        $model->field["IN_OUT_DIV"] = ($model->field["IN_OUT_DIV"] == "") ? "1" : $model->field["IN_OUT_DIV"];
        $divDisable04 = "";
        if ($model->field["PATTERN_DIV"] != "4") {
            //対象種別が04以外は選択不可
            $divDisable04 = " disabled ";
        }
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"IN_OUT_DIV{$val}\" ".$divDisable04);
        }
        $radioArray = knjCreateRadio($objForm, "IN_OUT_DIV", $model->field["IN_OUT_DIV"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SELECTED_MONTH");

        //ＤＢ切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp851index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp851Form1.html", $arg);
    }
}
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "--全て--", "value" => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $result->free();

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //実行
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
