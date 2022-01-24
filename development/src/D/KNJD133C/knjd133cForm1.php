<?php

require_once('for_php7.php');

class knjd133cForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        if ($model->cmd == "back") {
            $model->field["SUBCLASSCD"] = $model->subclasscd;
            $model->field["CHAIRCD"]    = $model->chaircd;
        }

        //学期コンボ
        if ($model->Properties["KNJD133C_semesCombo"] == "1") {
            $arg["KNJD133C_semesCombo"] = "1";
            knjCreateHidden($objForm, "KNJD133C_semesCombo", $model->Properties["KNJD133C_semesCombo"]);
            $query = knjd133cQuery::getSemesterCmb();
            $extra = "onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, "");
        }

        //校種コンボ
        $query = knjd133cQuery::getSchoolKind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, "");

        //名称マスタ D校種08チェック
        $model->che_school_kind = "D".$model->field["SCHOOL_KIND"]."08";
        $model->count = $db->getone(knjd133cquery::getNameMstche($model));

        //科目コンボ
        $query = knjd133cQuery::selectSubclassQuery($model);
        $extra = "onchange=\"return btn_submit('subclasscd');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "BLANK");

        //講座コンボ
        $query = knjd133cQuery::selectChairQuery($model);
        $extra = "onchange=\"return btn_submit('chaircd');\"";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "BLANK");

        //学期開始日、終了日
        $seme = $db->getRow(knjd133cQuery::getSemester($model), DB_FETCHMODE_ASSOC);
        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        if ($seme["SDATE"] <= CTRL_DATE && CTRL_DATE <= $seme["EDATE"]) {
            $execute_date = CTRL_DATE;  //初期値
        } else {
            $execute_date = $seme["EDATE"];     //初期値
        }

        // 生徒数カウント
        $query = knjd133cQuery::selectQuery($model, $execute_date);
        $result = $db->query($query);
        $counter = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $counter++;
        }
        $result->free();

        //ヘッダ作成
        $arg["item"] = array();
        $allwidth = 320;
        for ($i = 0; $i < get_count($model->itemMst); $i++) {
            if ($model->itemMst[$i]["PATTERN_SHOW_FLG"] == "1") {
                $extra  = "onclick=\"return btn_submit('teikei', '{$model->itemMst[$i]["COLUMNNAME"]}', '{$model->itemMst[$i]["PATTERN_DATA_DIV"]}', '{$model->itemMst[$i]["ITEMNAME"]}');\"";
                $extra .= ($model->field["CHAIRCD"] && $counter > 0) ? "" : " disabled";
                $model->itemMst[$i]["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);
            }

            $width = calcWidth($model->itemMst[$i]["moji"]);
            $model->itemMst[$i]["WIDTH"] = ($i == get_count($model->itemMst) - 1) ? "*" : $width;

            if ($model->itemMst[$i]["gyou"] == 1) {
                $model->itemMst[$i]["COMMENT"] = "(全角{$model->itemMst[$i]["moji"]}文字まで)";
            } else {
                $model->itemMst[$i]["COMMENT"] = "(全角{$model->itemMst[$i]["moji"]}文字X{$model->itemMst[$i]["gyou"]}行まで)";
            }
            $arg["item"][] = $model->itemMst[$i];
            $allwidth += (int)$width;
        }
        if (!isset($arg["item"])) {
            $arg["item"][] = array("1");
        }
        $allwidth += 30;
        $arg["ALLWIDTH"] = ($allwidth > 700) ? $allwidth : 700;

        //初期化
        $model->data = array();
        $counter = 0;
        $grade = array();
        //一覧表示
        $colorFlg = false;
        $query = knjd133cQuery::selectQuery($model, $execute_date);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //学年ごとの連番取得
            $grade[$row["GRADE"]][] = $counter;

            //クラス-出席番号(表示)
            if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            /*** テキストエリア ***/
            $row["item"] = array();

            for ($i = 0; $i < get_count($model->itemMst); $i++) {
                $m = $model->itemMst[$i];
                $key = $m["COLUMNNAME"];
                $name = $key."-".$counter;
                $model->data[$name] = $row[$key];
                $moji = $m["moji"];
                $gyou = $m["gyou"];
                $row[$key] = $model->cmd != "csvInputMain" ? $row[$key] : $model->data_arr[$row["SCHREGNO"]][$key];
                $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row[$key] : $model->fields[$key][$counter];
                $extra = "onPaste=\"return showPaste(this);\" onfocus=\"focused(this);\" id=\"{$name}\" ";
                $item = array();
                if ($gyou == 1) {
                    $item["data"] = knjCreateTextBox($objForm, $value, $name, ((int)$moji * 2), ((int)$moji * 2), $extra);
                } else {
                    $height = (int)$gyou * 13.5 + ((int)$gyou - 1) * 3 + 5;
                    $extra .= "style=\"height:{$height}px;\" ";

                    $item["data"] = KnjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2 + 1), "soft", $extra, $value);
                }

                $item["WIDTH"] = $m["WIDTH"];
                $row["item"][] = $item;
            }

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        //学年ごとの連番を格納
        foreach ($grade as $key => $val) {
            knjCreateHidden($objForm, "counter_array-".$key, implode(',', $val));
        }

        //CSV処理作成
        makeCsv($objForm, $arg, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $counter);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "itemMstJson", $model->itemMstJson);

        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd133cindex.php", "", "main");
        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd133cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
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
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function calcWidth($mojisu)
{
    return 210 + (($mojisu > 10) ? (((int)$mojisu - 10) * 17.3) : 0);
}

//CSV処理作成
function makeCsv(&$objForm, &$arg, $model)
{
    //ファイル
    $extra = "";
    $dis = "";
    if ($model->field["CHAIRCD"] == '') {
        $dis = " disabled=\"disabled\" ";
    }
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra.$dis, 1024000);
    //取込ボタン
    $extra = "onclick=\"return btn_submit('csvInput');\"";
    $arg["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra.$dis);
    //出力ボタン
    $extra = "onclick=\"return btn_submit('csvOutput');\"";
    $arg["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra.$dis);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $counter)
{

    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //一括更新ボタンを作成する
    $link = REQUESTROOT."/D/KNJD133C/knjd133cindex.php?cmd=replace&CHAIRCD=".$model->field["CHAIRCD"]."&SUBCLASSCD=".$model->field["SUBCLASSCD"];
    $extra = ($model->field["CHAIRCD"]) ? "onclick=\"Page_jumper('$link');\"" : "disabled";
    $arg["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);
}
