<?php

require_once('for_php7.php');


class knjd155aForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd155aindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        if ($model->cmd == "back") {
            $model->field["SEMESTER"]       = $model->semester;
            $model->field["GRADE_HR_CLASS"] = $model->grade_hr_class;
            $model->field["TESTKINDCD"]     = $model->testkindcd;
        }

        //学期コンボ作成
        $query = knjd155aQuery::getSemesterList();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //テスト種別コンボ作成
        $query = knjd155aQuery::getTestList($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], $extra, 1, 1);

        //年組コンボ作成
        $query = knjd155aQuery::getGradeHrclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd155aQuery::selectQuery($model));
        $arg["REMARK1_TYUI"] = "(全角{$model->hexam_record_remark_dat_remark1_moji}文字{$model->hexam_record_remark_dat_remark1_gyou}行まで)";
        $arg["REMARK_TITLE"] = $model->Properties["kojinSeisekihyouSyokenTitle"] ? $model->Properties["kojinSeisekihyouSyokenTitle"] : '通信欄';

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if ($row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //備考
            $value = (!isset($model->warning)) ? $row["REMARK"] : $model->fields["REMARK"][$counter];
            $height = (int)$model->hexam_record_remark_dat_remark1_gyou * 13.5 + ((int)$model->hexam_record_remark_dat_remark1_gyou -1) * 3 + 5;
            $row["REMARK"] = KnjCreateTextArea($objForm, "REMARK-".$counter, $model->hexam_record_remark_dat_remark1_gyou, ((int)$model->hexam_record_remark_dat_remark1_moji * 2 + 1), "soft", "style=\"height:{$height}px;\" onPaste=\"return showPaste(this);\"", $value);

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "hexam_record_remark_dat_remark1_gyou", $model->hexam_record_remark_dat_remark1_gyou);
        knjCreateHidden($objForm, "hexam_record_remark_dat_remark1_moji", $model->hexam_record_remark_dat_remark1_moji);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd155aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if ($blank != "") {
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{

    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //データCSVボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX155CSV/knjx155csvindex.php?','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

    //一括更新ボタンを作成する
    $link = REQUESTROOT."/D/KNJD155A/knjd155aindex.php?cmd=replace&SEMESTER=".$model->field["SEMESTER"]."&GRADE_HR_CLASS=".$model->field["GRADE_HR_CLASS"]."&TESTKINDCD=".$model->field["TESTKINDCD"];
    $extra = ($model->field["SEMESTER"] && $model->field["GRADE_HR_CLASS"] && $model->field["TESTKINDCD"]) ? "onclick=\"Page_jumper('$link');\"" : "disabled";
    $arg["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);
}
