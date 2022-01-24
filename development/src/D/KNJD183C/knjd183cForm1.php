<?php

require_once('for_php7.php');

class knjd183cForm1
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
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $arg["KNJD183C_semesCombo"] = "1";
            knjCreateHidden($objForm, "KNJD183C_semesCombo", $model->Properties["KNJD183C_semesCombo"]);
            $query = knjd183cQuery::getSemesterCmb();
            $extra = "onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, "");
        }

        //科目コンボ
        $query = knjd183cQuery::selectSubclassQuery($model);
        $extra = "onchange=\"return btn_submit('subclasscd');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "BLANK");

        //講座コンボ
        $query = knjd183cQuery::selectChairQuery($model);
        $extra = "onchange=\"return btn_submit('chaircd');\"";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "BLANK");

        //学期開始日、終了日
        $seme = $db->getRow(knjd183cQuery::getSemester($model), DB_FETCHMODE_ASSOC);
        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        if ($seme["SDATE"] <= CTRL_DATE && CTRL_DATE <= $seme["EDATE"]) {
            $execute_date = CTRL_DATE;  //初期値
        } else {
            $execute_date = $seme["EDATE"];     //初期値
        }

        // 生徒数カウント
        $query = knjd183cQuery::selectQuery($model, $execute_date);
        $result = $db->query($query);
        $counter = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $counter++;
        }
        $result->free();

        //オプションの値を取得
        $nyuryokuPattern = "";
        $result = $db->query(knjd183cQuery::getNyuryokuPattern());
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nyuryokuPattern= $row1["REMARK1"];
        }
        $result->free();

        $soutanMojiA = 55;  //「総探のみ」Aパターン入力制限数
        $soutanMojiB = 35;  //「総探のみ」Bパターン入力制限数
        $doubleMojiA = 25;  //「両方使用」Aパターン入力制限数
        $doubleMojiB = 15;  //「両方使用」Bパターン入力制限数

        $maxTextCommentTemp = "(全角num1文字×4行まで、自立活動欄対象者は全角num2文字×4行まで)";
        $allwidthPattern = 700;
        $maxTextComment  = "";
        if ($nyuryokuPattern == '1' && $model->field["CHAIRCD"] != null) {
            //総探のみ使用、講座選択時、1行55文字のテキストエリアを表示するために横幅を拡張
            $allwidthPattern = 1150;
            $maxTextComment = str_replace("num1", $soutanMojiA, $maxTextCommentTemp);
            $maxTextComment = str_replace("num2", $soutanMojiB, $maxTextComment);
        } elseif ($nyuryokuPattern == '3') {
            //両方使用
            $maxTextComment = str_replace("num1", $doubleMojiA, $maxTextCommentTemp);
            $maxTextComment = str_replace("num2", $doubleMojiB, $maxTextComment);
        }

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

            $model->itemMst[$i]["COMMENT"] = $maxTextComment;

            $arg["item"][] = $model->itemMst[$i];
            $allwidth += (int)$width;
        }
        if (!isset($arg["item"])) {
            $arg["item"][] = array("1");
        }
        $allwidth += 30;

        $arg["ALLWIDTH"] = ($allwidth > $allwidthPattern) ? $allwidth : $allwidthPattern;

        //初期化
        $model->data = array();
        $counter = 0;
        $grade = array();

        $colorFlg = false;
        $query = knjd183cQuery::selectQuery($model, $execute_date);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //通知表パターン取得
            $handicap = "";
            $result2 = $db->query(knjd183cQuery::getHandCap($row["SCHREGNO"]));
            while ($row1 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $handicap= $row1["HANDICAP"];
            }
            $result2->free();

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
                $row[$key] = $model->cmd != "csvInputMain" ? $row[$key] : $model->data_arr[$row["SCHREGNO"]][$key];
                $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row[$key] : $model->fields[$key][$counter];
                $extra = "onPaste=\"return showPaste(this);\" onfocus=\"focused(this);\" id=\"{$name}\" ";

                $gyou = 4;
                $moji = 0;
                if ($handicap != '002') {
                    //通知表Aパターン
                    if ($nyuryokuPattern == '1') {
                        //総探のみ使用
                        $moji = $soutanMojiA;
                    } elseif ($nyuryokuPattern == '3') {
                        //両方使用
                        $moji = $doubleMojiA;
                    }
                } else {
                    //通知表Bパターン
                    if ($nyuryokuPattern == '1') {
                        //総探のみ使用
                        $moji = $soutanMojiB;
                    } elseif ($nyuryokuPattern == '3') {
                        //両方使用
                        $moji = $doubleMojiB;
                    }
                }

                if ($nyuryokuPattern != '1' && $nyuryokuPattern != '3') {
                    //資格・検定のみ使用、両方表示しないのときは何もしない
                    $extra .= " disabled ";
                }
                $extra .= " onblur = \" return chackMozi(this, $moji, $gyou ) \" ";

                $item = array();
                if ($gyou == 1) {
                    $item["data"] = knjCreateTextBox($objForm, $value, $name, ($moji * 2), ($moji * 2), $extra);
                } elseif ($moji != 0) {
                    $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;
                    $extra .= "style=\"height:{$height}px;\" ";

                    $item["data"] = KnjCreateTextArea($objForm, $name, $gyou, ($moji * 2 + 1), "soft", $extra, $value);
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
        makeBtn($objForm, $arg, $model, $counter, $moji);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "itemMstJson", $model->itemMstJson);

        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd183cindex.php", "", "main");
        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd183cForm1.html", $arg);
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
    return 170 + (($mojisu > 10) ? (((int)$mojisu - 10) * 13.5) : 0);
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
function makeBtn(&$objForm, &$arg, $model, $counter, $moji)
{
    //更新ボタン
    $extra = ($moji == 0) ? "disabled" : ((AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled");
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //一括更新ボタンを作成する
    $link = REQUESTROOT."/D/KNJD183C/knjd183cindex.php?cmd=replace&CHAIRCD=".$model->field["CHAIRCD"]."&SUBCLASSCD=".$model->field["SUBCLASSCD"];
    $extra = ($moji == 0) ? "disabled" : (($model->field["CHAIRCD"]) ? "onclick=\"Page_jumper('$link');\"" : "disabled");
    $arg["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);
}
