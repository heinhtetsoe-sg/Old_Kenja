<?php

require_once('for_php7.php');

class knja113Form2
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja113index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //カレンダー呼び出し
        $my = new mycalendar();

        if ($model->warning) {
            $schregno = $model->field["SCHREGNO"];
        } elseif ($model->flg) {
            $schregno = $model->schregno;
        } else {
            $schregno = $model->field["SCHREGNO"];
            if (!isset($model->warning)) {
                $model->field = array();
                unset($model->schregno);
            }
        }

        //学籍番号テキスト
        $arg["data"]["SCHREGNO"] = knjCreateTextBox($objForm, $schregno, "SCHREGNO", 9, 8, "");

        //入力番号検索ボタン
        $extra = " onClick=\"btn_submit('search');\"";
        $arg["button"]["btn_input"] = knjCreateBtn($objForm, "btn_input", "入力番号検索", $extra);
        if ($model->std_div == "1") {
            //新入生検索ボタン
            $extra = "onclick=\"wopen('../../X/KNJXSEARCH_FRESHMAN/knjxSearch_Freshmanindex.php?PATH=/A/KNJA113/knja113index.php&cmd=&target=KNJA113','search', 0, 0, 700, 600);\"";
            $arg["button"]["btn_zaigaku"] = knjCreateBtn($objForm, "btn_zaigaku", "新入生検索", $extra);
        } else {
            //在学生検索ボタン
            $extra = "onclick=\"wopen('../../X/KNJXSEARCH2/index.php?PATH=/A/KNJA113/knja113index.php&cmd=&target=KNJA113','search', 0, 0, 700, 600);\"";
            $arg["button"]["btn_zaigaku"] = knjCreateBtn($objForm, "btn_zaigaku", "在校生検索", $extra);
        }

        //生徒情報表示
        $arg["data"]["SCHDATA"] = $db->getOne(knja113Query::getSchData($model, $schregno));

        //警告メッセージを表示しない場合
        //すでにあるデータの更新の場合
        if (isset($model->schoolkind) && isset($model->scholarship) && isset($model->schregno) && isset($model->from_date)  && isset($model->flg) && !isset($model->warning)) {
            $Row = $db->getRow(knja113Query::getReductionSchoolStdDat($model, $model->schregno, $model->from_date, "0", ""), DB_FETCHMODE_ASSOC);
            if ($Row["FROM_DATE"]) {
                list($yyyy, $mm, $dd) = explode("-", $Row["FROM_DATE"]);
                $Row["FROM_DATE"] = $yyyy."/".$mm;
            }
            if ($Row["TO_DATE"]) {
                list($yyyy, $mm, $dd) = explode("-", $Row["TO_DATE"]);
                $Row["TO_DATE"] = $yyyy."/".$mm;
            }
        } else {
            $Row =& $model->field;
        }

        if (!$model->schoolkind) {
            $sk = $db->getRow(knja113Query::getSchKind($model), DB_FETCHMODE_ASSOC);
            $schoolkind = $sk["VALUE"];
            $RD = $db->getRow(knja113Query::getScholarshipMst($model, $schoolkind), DB_FETCHMODE_ASSOC);
            $scholarship = $RD["VALUE"];

            //校種表示
            $arg["data"]["SCHOOL_KIND"] = $sk["LABEL"];
            //交付種別表示
            $arg["data"]["SCHOLARSHIP"] = $RD["LABEL"];
        } else {
            $schoolkind = $model->schoolkind;
            $scholarship = $model->scholarship;

            //校種表示
            $arg["data"]["SCHOOL_KIND"] = $db->getOne(knja113Query::getSchKind($model, $schoolkind));
            //交付種別表示
            $arg["data"]["SCHOLARSHIP"] = $db->getOne(knja113Query::getScholarshipMst($model, $schoolkind, $scholarship));
        }

        //開始日付
        $arg["data"]["FROM_DATE"] = str_replace("\n", "", $my->MyMonthWin2($objForm, "FROM_DATE", $Row["FROM_DATE"]));

        //終了日付
        $arg["data"]["TO_DATE"] = str_replace("\n", "", $my->MyMonthWin2($objForm, "TO_DATE", $Row["TO_DATE"]));

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 50, 50, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOL_KIND", $schoolkind);
        knjCreateHidden($objForm, "SCHOLARSHIP", $scholarship);
        knjCreateHidden($objForm, "FLG");
        knjCreateHidden($objForm, "GRADE", $model->grade);
        knjCreateHidden($objForm, "GRADE_HR_CLASS", $model->grade_hr_class);
        knjCreateHidden($objForm, "CLUBCD", $model->clubcd);
        knjCreateHidden($objForm, "RIGHT_FRAME", "edit");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "edit" || $model->cmd == "edit2") {
            $arg["reload"] = "window.open('knja113index.php?cmd=list&SCHOOL_KIND=".$schoolkind."&SCHOLARSHIP=".$scholarship."&GRADE=".$model->grade."&GRADE_HR_CLASS=".$model->grade_hr_class."&CLUBCD=".$model->clubcd."&RIGHT_FRAME=edit','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja113Form2.html", $arg);
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
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";

    //追加ボタン
    $extra = "onclick=\"return btn_submit('add')\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "登 録", $extra.$disable);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update')\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra.$disable);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete')\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disable);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$disable);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //一括更新ボタン
    $extra = "onclick=\"return btn_submit('Ikkatsu');\"";
    $arg["button"]["btn_Ikkatsu"] = KnjCreateBtn($objForm, "btn_Ikkatsu", "一括更新", $extra);

    //CSVボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A113/knjx_a113index.php','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "CSV入出力", $extra);
}
