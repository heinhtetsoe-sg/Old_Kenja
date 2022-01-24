<?php

require_once('for_php7.php');

class knja113Form1
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knja113index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //新入生・在学生ラジオ
        $opt = array(1, 2);
        //$model->std_div = ($model->std_div == "") ? "2" : $model->std_div;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"STD_DIV{$val}\" onClick=\"btn_submit('change');\" ");
        }
        $radioArray = knjCreateRadio($objForm, "STD_DIV", $model->std_div, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //年度切替
        //$model->exeYear = ($model->std_div == "1") ? (CTRL_YEAR + 1) : CTRL_YEAR;

        //年度表示
        $arg["YEAR"] = $model->exeYear;

        //校種コンボ
        $query = knja113Query::getSchKind($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolkind, $extra, 1);

        //交付種別コンボ
        $query = knja113Query::getScholarshipMst($model, $model->schoolkind);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOLARSHIP", $model->scholarship, $extra, 1);

        //学年コンボ
        $query = knja113Query::getGrade($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1, "blank");

        //年組コンボ
        $query = knja113Query::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->grade_hr_class, $extra, 1, "blank");

        //クラブコンボ
        $query = knja113Query::getClub($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "CLUBCD", $model->clubcd, $extra, 1, "blank");

        //一覧取得
        $schCount = 0;
        $query = knja113Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            foreach (array("FROM_DATE", "TO_DATE") as $key) {
                if ($row[$key]) {
                    list($yyyy, $mm, $dd) = explode("-", $row[$key]);
                    $row[$key] = $yyyy."/".$mm;
                }
            }

            //期間
            $to = (strlen($row["FROM_DATE"]) && strlen($row["TO_DATE"])) ? " ～ " : "";
            $row["KIKAN"] = $row["FROM_DATE"].$to.$row["TO_DATE"];

            //備考
            $conv_remark = mb_convert_encoding($row["REMARK"], 'SJIS', 'UTF-8');
            if (strlen($conv_remark) > 40) {
                $remark = substr($conv_remark, 0, 40);
                $row["REMARK_SHOW"] = mb_convert_encoding(substr($conv_remark, 0, 40), 'UTF-8', 'SJIS').'...';
            } else {
                $row["REMARK_SHOW"] = $row["REMARK"];
            }

            //リンク
            $row["NAME_SHOW"] = View::alink(
                "knja113index.php",
                $row["NAME_SHOW"],
                "target=\"right_frame\"",
                array("SCHREGNO"            => $row["SCHREGNO"],
                                              "SCHOOL_KIND"         => $row["SCHOOL_KIND"],
                                              "SCHOLARSHIP"         => $row["SCHOLARSHIP"],
                                              "FROM_DATE"           => $row["FROM_DATE"],
                                              "GRADE"               => $model->grade,
                                              "GRADE_HR_CLASS"      => $model->grade_hr_class,
                                              "CLUBCD"              => $model->clubcd,
                                              "cmd"                 => "edit",
                                              "FLG"                 => "ON")
            );

            $arg["data"][] = $row;
            $schCount++;
        }
        $result->free();

        //人数セット
        $arg["SCHREG_COUNT"] = $schCount.'人';

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "RIGHT_FRAME", $model->right_frame);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "change") {
            unset($model->schregno);
            $arg["reload"] = "window.open('knja113index.php?cmd={$model->right_frame}&SCHOOL_KIND={$model->schoolkind}&SCHOLARSHIP={$model->scholarship}&GRADE={$model->grade}&GRADE_HR_CLASS={$model->grade_hr_class}&CLUBCD={$model->clubcd}','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja113Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
