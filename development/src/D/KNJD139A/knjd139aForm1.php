<?php

require_once('for_php7.php');

class knjd139aForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd139aindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $setNameCd = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $query = knjd139aQuery::getNameMst($setNameCd);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        
        //学期(観点データ以外用)
        $model->field["SEMESTER2"] = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];//初期値
        
        //教科コンボ作成
        $query = knjd139aQuery::selectSubclassQuery($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank");

        //科目コンボ作成
        $query = knjd139aQuery::getSubclassMst($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");
        

        //講座コンボ
        $query = knjd139aQuery::selectChairQuery($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");

        //初期化
        $model->data = array();
        $counter = 0;

        //生徒を抽出する日付
        $sdate = str_replace("/","-",$model->control["学期開始日付"][$model->field["SEMESTER"]]);
        $edate = str_replace("/","-",$model->control["学期終了日付"][$model->field["SEMESTER"]]);
        $execute_date = ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) ? CTRL_DATE : $edate;//初期値

        //一覧表示
        $result = $db->query(knjd139aQuery::selectQuery($model, $execute_date));
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
            $query = knjd139aQuery::getGrade($model, $row["SCHREGNO"]);
            $getGrade = $db->getOne($query);
            $kanten = $db->query(knjd139aQuery::selectViewcdQuery($model,$row["SCHREGNO"], $execute_date, $getGrade));
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
        knjCreateHidden($objForm, "PRGID", "KNJD139A");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        //CSV出力画面
        knjCreateHidden($objForm, "URL_CSVIO", REQUESTROOT."/X/KNJX_D139A/knjx_d139aindex.php?SEND_PRGID=KNJD139A&SEND_SEMESTER={$model->field["SEMESTER"]}&SEND_CLASSCD={$model->field["CLASSCD"]}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEND_CHAIRCD={$model->field["CHAIRCD"]}");

        
        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd139aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
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

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //CSV入出力ボタン
    $extra = "onClick=\"openCsvgamen();\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV入出力", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
    //更新ボタンを作成する
    $disabled = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";
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
