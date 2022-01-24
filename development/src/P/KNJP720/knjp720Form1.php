<?php

require_once('for_php7.php');

class knjp720Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjp720index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $query = knjp720Query::getSchKind($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolkind, $extra, 1);

        //交付種別コンボ
        $query = knjp720Query::getReductionDivMst($model, $model->schoolkind);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "REDUCTION_DIV_CD", $model->reduction_div_cd, $extra, 1);

        //学年コンボ
        $query = knjp720Query::getGrade($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1, "blank");

        //年組コンボ
        $query = knjp720Query::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->grade_hr_class, $extra, 1, "blank");

        //クラブコンボ
        $query = knjp720Query::getClub($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "CLUBCD", $model->clubcd, $extra, 1, "blank");

        //一覧取得
        $query = knjp720Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            foreach (array("S_YEAR_MONTH", "E_YEAR_MONTH") as $key) {
               $row[$key] = str_replace("-", "/", $row[$key]);
            }

            //期間
            $to = (strlen($row["S_YEAR_MONTH"]) && strlen($row["E_YEAR_MONTH"])) ? " ～ " : "";
            $row["KIKAN"] = $row["S_YEAR_MONTH"].$to.$row["E_YEAR_MONTH"];

            //備考
            $conv_remark = mb_convert_encoding($row["REMARK"], 'SJIS', 'UTF-8');
            if (strlen($conv_remark) > 40) {
                $remark = substr($conv_remark,0,40);
                $row["REMARK_SHOW"] = mb_convert_encoding(substr($conv_remark,0,40), 'UTF-8', 'SJIS').'...';
            } else {
                $row["REMARK_SHOW"] = $row["REMARK"];
            }

            //リンク
            $row["NAME_SHOW"] = View::alink("knjp720index.php", $row["NAME_SHOW"], "target=\"right_frame\"",
                                        array("SCHREGNO"            => $row["SCHREGNO"],
                                              "SCHOOL_KIND"         => $row["SCHOOL_KIND"],
                                              "REDUCTION_DIV_CD"    => $row["REDUCTION_DIV_CD"],
                                              "S_YEAR_MONTH"        => $row["S_YEAR_MONTH"],
                                              "GRADE"               => $model->grade,
                                              "GRADE_HR_CLASS"      => $model->grade_hr_class,
                                              "CLUBCD"              => $model->clubcd,
                                              "cmd"                 => "edit",
                                              "FLG"                 => "ON"));

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "RIGHT_FRAME", $model->right_frame);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "change") {
            unset($model->schregno);
            $arg["reload"] = "window.open('knjp720index.php?cmd={$model->right_frame}&SCHOOL_KIND={$model->schoolkind}&REDUCTION_DIV_CD={$model->reduction_div_cd}&GRADE={$model->grade}&GRADE_HR_CLASS={$model->grade_hr_class}&CLUBCD={$model->clubcd}','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp720Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
