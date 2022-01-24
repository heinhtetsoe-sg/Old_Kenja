<?php

require_once('for_php7.php');

class knjd139Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd139index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種を表示
            $arg["usePrgSchoolkind"] = 1; 
            //校種コンボ作成
            $query = knjd139Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('main')\"";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, "", $model);
        }else{
            $model->field["SCHOOL_KIND"] = "P";
        }

        //学期コンボ作成
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."09";
        }else{
            $setNameCd = "ZP09";
        }
        $query = knjd139Query::getNameMst($setNameCd);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, "", $model);

        //年組コンボ作成
        $query = knjd139Query::getGradeHrclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1, $model);

        //教科コンボ作成
        $query = knjd139Query::getClassMst($model, "", $model->field["GRADE_HR_CLASS"]);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank", $model);

        //科目コンボ作成
        $query = knjd139Query::getSubclassMst($model->field["CLASSCD"], $model->field["GRADE_HR_CLASS"], $model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank", $model);

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $result = $db->query(knjd139Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if ($row["ATTENDNO"] != ""){
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            
            //観点取得
            $model->data["STATUS"] =array();
            $kanten = $db->query(knjd139Query::selectViewcdQuery($model,$row["SCHREGNO"]));
            while ($row2 = $kanten->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->data["STATUS"][] = $row2["STATUS"];
                if($model->data["STATUS"] != "") {
                    foreach ($model->data["STATUS"] as $key => $value) {
                        $key = $key + 1;
                        if ($key == 1) {
                            $number = "①";
                        } else if ($key == 2) {
                            $number = "②";
                        } else if ($key == 3) {
                            $number = "③";
                        } else if ($key == 4) {
                            $number = "④";
                        } else if ($key == 5) {
                            $number = "⑤";
                        } else if ($key == 6) {
                            $number = "⑥";
                        }
                        $row["STATUS".$key] = $number.$value;
                    }
                }
            }

            //備考
            $extra = " onPaste=\"return showPaste(this);\"";
            $value = (!isset($model->warning)) ? $row["REMARK1"] : $model->fields["REMARK1"][$counter];
            $row["REMARK1"] = KnjCreateTextArea($objForm, "REMARK1-".$counter, 6, 41.5, "soft", $extra, $value);
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;

        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJD139");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        //CSVボタン
        knjCreateHidden($objForm, "cmd");
        //CSVボタンのリンク先のURL
        knjCreateHidden($objForm, "URL_CSV", REQUESTROOT."/X/KNJX_D139/knjx_d139index.php?SEND_PRGID=KNJD139A&SEND_SCHOOL_KIND={$model->field["SCHOOL_KIND"]}&SEND_SEMESTER={$model->field["SEMESTER"]}&SEND_GRADE_HR_CLASS={$model->field["GRADE_HR_CLASS"]}&SEND_CLASSCD={$model->field["CLASSCD"]}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}");

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd139Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", &$model) {
    $opt = array();
    $value_flg = false;
    $dataFlg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        $dataFlg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        if ($model->Properties["use_prg_schoolkind"] != "1") {
            if(!$dataFlg){
                //名称マスタ「ZP09」が取得できなかった場合、名称マスタ「Z009」を参照
                $result = $db->query(knjd139Query::getNameMst("Z009"));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                    if ($value == $row["VALUE"]) $value_flg = true;
                }
                $result->free();
            }
        }
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //CSVボタン
    $extra = "onClick=\"openCsvgamen();\"";
    $arg["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "CSV入出力", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
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
}
?>
