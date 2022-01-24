<?php

require_once('for_php7.php');

class knjd133hForm1
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

        //評価セット
        if ($model->Properties["useOnlyTotalstudyTime"] == '1') {
            unset($arg["useTime"]);
            unset($arg["unUseTime"]);
            $arg["useOnlyTime"] = "1";
        } elseif ($model->Properties["useTotalstudyTime"] == '1') {
            unset($arg["useOnlyTime"]);
            unset($arg["unUseTime"]);
            $arg["useTime"] = "1";
        } else {
            unset($arg["useOnlyTime"]);
            unset($arg["useTime"]);
            $arg["unUseTime"] = "1";
        }

        //仮評定欄表示
        if ($model->Properties["useProvFlg"] == '1' && $model->Properties["useKnjd133hOnlyGradValue"] != '2') {
            $arg["useProvFlg"] = "1";
        } else {
            unset($arg["useProvFlg"]);
        }

        //単位・評定切替
        if ($model->Properties["useKnjd133hOnlyGradValue"] == '2') {
            unset($arg["useCredit"]);
            unset($arg["useGradValue"]);
        } elseif ($model->Properties["useKnjd133hOnlyGradValue"] == '1') {
            $arg["useGradValue"] = "1";
            unset($arg["useCredit"]);
        } else {
            $arg["useCredit"] = "1";
            unset($arg["useGradValue"]);
        }

        //学期コンボ
        $query = knjd133hQuery::getSemester();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //名称マスタ D校種08チェック
        $model->che_school_kind = "D".$model->urlSchoolKind."08";
        $model->count = $db->getone(knjd133hquery::getNameMstche($model));

        //科目コンボ
        $query = knjd133hQuery::selectSubclassQuery($model, $model->field["SEMESTER"]);
        $extra = "onchange=\"return btn_submit('subclasscd');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "BLANK");

        //講座コンボ
        $query = knjd133hQuery::selectChairQuery($model, $model->field["SEMESTER"], $model->field["SUBCLASSCD"]);
        $extra = "onchange=\"return btn_submit('chaircd');\"";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "BLANK");

        //ALLチェック(仮評定)
        $extra = "onClick=\"return check_all_prov_flg(this);\"";
        $arg["CHECKALL_PROV_FLG"] = knjCreateCheckBox($objForm, "CHECKALL_PROV_FLG", "", $extra);

        //ALLチェック(単位自動)
        $extra = "onClick=\"return check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra);

        //学期開始日、終了日
        $seme = $db->getRow(knjd133hQuery::getSemester($model->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        if ($seme["SDATE"] <= CTRL_DATE && CTRL_DATE <= $seme["EDATE"]) {
            $execute_date = CTRL_DATE;  //初期値
        } else {
            $execute_date = $seme["EDATE"];     //初期値
        }

        //最大文字数取得
        if ($model->Properties["useOnlyTotalstudyTime"] == '1') {
            $setMojiSize = $model->getPro2["TOTALSTUDYTIME"]["moji"];
        } elseif ($model->Properties["useTotalstudyTime"] == '1') {
            $setMojiSize = ($model->getPro["TOTALSTUDYACT"]["moji"] < $model->getPro2["TOTALSTUDYTIME"]["moji"]) ? $model->getPro2["TOTALSTUDYTIME"]["moji"] : $model->getPro["TOTALSTUDYACT"]["moji"];
        } else {
            $setMojiSize = $model->getPro["TOTALSTUDYACT"]["moji"];
        }

        //全体幅
        if ($model->Properties["useKnjd133hOnlyGradValue"] == '2') {
            $all_width = ($setMojiSize > "45") ? 1050 + ($setMojiSize - 45) * 14.8 : "1050";
        } else {
            $all_width = ($setMojiSize > "30") ? 1050 + ($setMojiSize - 30) * 14.8 : "1050";
        }
        $arg["ALL_WIDTH"] = $all_width;

        //テキスト欄幅
        if ($model->Properties["useKnjd133hOnlyGradValue"] == '2') {
            $setwidth = "*";
        } else {
            $setwidth = ($setMojiSize < "21") ? "310" : $setMojiSize * 14.8;
        }
        $arg["SETWIDTH"] = $setwidth;

        $comment = "";
        //初期化
        $model->data = array();
        $counter = 0;
        $grade = array();
        //一覧表示
        $colorFlg = false;
        
        $query = knjd133hQuery::selectQuery($model, $execute_date, $model->field["SEMESTER"], $model->field["SUBCLASSCD"], $model->field["CHAIRCD"]);
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
            //学習内容
            foreach ($model->getPro as $key => $val) {
                $model->data[$key."-".$counter] = $row[$key];

                if ($val["gyou"] == 1) {
                    if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] == '1') {
                        $extra = "style=\"background-color:#D0D0D0;\" readonly";
                    } else {
                        $extra = "onPaste=\"return showPaste(this);\"";
                        $row[$key] = $model->cmd != "csvInputMain" ? $row[$key] : $model->data_arr[$row["SCHREGNO"]][$key];
                    }
                    $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row[$key] : $model->fields[$key][$counter];

                    $row[$key] = knjCreateTextBox($objForm, $value, $key."-".$counter, ($val["moji"] * 2), ($val["moji"] * 2), $extra);
                    $comment = "(全角{$val["moji"]}文字まで)";
                } else {
                    $height = $val["gyou"] * 13.5 + ($val["gyou"] - 1) * 3 + 5;
                    if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] == '1') {
                        $extra = "style=\"height:{$height}px;background-color:#D0D0D0;\" readonly";
                    } else {
                        $extra = "style=\"height:{$height}px;\" onPaste=\"return showPaste(this);\"";
                        $row[$key] = $model->cmd != "csvInputMain" ? $row[$key] : $model->data_arr[$row["SCHREGNO"]][$key];
                    }
                    $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row[$key] : $model->fields[$key][$counter];

                    $row[$key] = KnjCreateTextArea($objForm, $key."-".$counter, $val["gyou"], ($val["moji"] * 2 + 1), "soft", $extra, $value);
                    $comment = "(全角{$val["moji"]}文字X{$val["gyou"]}行まで)";
                }
            }

            $arg["TOTALSTUDYACT_COMMENT"] = $comment;
            $row["TOTALSTUDYACT_COMMENT"] = $comment;

            //評価
            foreach ($model->getPro2 as $key => $val) {
                $model->data[$key."-".$counter] = $row[$key];

                if ($val["gyou"] == 1) {
                    if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] == '1') {
                        $extra = "style=\"background-color:#D0D0D0;\" readonly";
                    } else {
                        $extra = "onPaste=\"return showPaste(this);\"";
                        $row[$key] = $model->cmd != "csvInputMain" ? $row[$key] : $model->data_arr[$row["SCHREGNO"]][$key];
                    }
                    $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row[$key] : $model->fields[$key][$counter];

                    $row[$key] = knjCreateTextBox($objForm, $value, $key."-".$counter, ($val["moji"] * 2), ($val["moji"] * 2), $extra);
                    $comment = "(全角{$val["moji"]}文字まで)";
                } else {
                    $height = $val["gyou"] * 13.5 + ($val["gyou"] - 1) * 3 + 5;
                    if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] == '1') {
                        $extra = "style=\"height:{$height}px;background-color:#D0D0D0;\" readonly";
                    } else {
                        $extra = "style=\"height:{$height}px;\" onPaste=\"return showPaste(this);\"";
                        $row[$key] = $model->cmd != "csvInputMain" ? $row[$key] : $model->data_arr[$row["SCHREGNO"]][$key];
                    }
                    $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row[$key] : $model->fields[$key][$counter];

                    $row[$key] = KnjCreateTextArea($objForm, $key."-".$counter, $val["gyou"], ($val["moji"] * 2 + 1), "soft", $extra, $value);
                    $comment = "(全角{$val["moji"]}文字X{$val["gyou"]}行まで)";
                }
            }

            $arg["TOTALSTUDYTIME_COMMENT"] = $comment;
            $row["TOTALSTUDYTIME_COMMENT"] = $comment;

            /*** チェックボックス ***/

            //仮評定
            $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row["PROV_FLG"] : $model->fields["PROV_FLG"][$counter];
            $extra = ($value == "1") ? "checked" : "";
            $row["PROV_FLG"] = knjCreateCheckBox($objForm, "PROV_FLG-".$counter, "1", $extra);

            //単位自動・・・チェックありの場合、単位マスタの単位数をセットし更新
            if ((isset($model->warning) || $model->cmd == "value_set") && $model->fields["CHK_CALC_CREDIT"][$counter] == "on") {
                $extra = "checked";
            } else {
                $extra = "";
            }
            $row["CHK_CALC_CREDIT"] = knjCreateCheckBox($objForm, "CHK_CALC_CREDIT-".$counter, "on", $extra);

            /*** テキストボックス ***/

            //学年評定
            $model->data["GRAD_VALUE"."-".$counter] = $row["GRAD_VALUE"];
            $row["GRAD_VALUE"] = $model->cmd != "csvInputMain" ? $row["GRAD_VALUE"] : $model->data_arr[$row["SCHREGNO"]]["GRAD_VALUE"];
            $value = (!isset($model->warning)) ? $row["GRAD_VALUE"] : $model->fields["GRAD_VALUE"][$counter];
            $extra = " onBlur=\"return tmpSet(this);\" STYLE=\"text-align:right;\" onPaste=\"return showPaste(this);\"";
            $row["GRAD_VALUE"] = knjCreateTextBox($objForm, $value, "GRAD_VALUE-".$counter, 3, 2, $extra);

            //履修単位
            $model->data["COMP_CREDIT"."-".$counter] = $row["COMP_CREDIT"];
            $row["COMP_CREDIT"] = $model->cmd != "csvInputMain" ? $row["COMP_CREDIT"] : $model->data_arr[$row["SCHREGNO"]]["COMP_CREDIT"];
            $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row["COMP_CREDIT"] : $model->fields["COMP_CREDIT"][$counter];
            $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\" onPaste=\"return showPaste(this);\"";
            $row["COMP_CREDIT"] = knjCreateTextBox($objForm, $value, "COMP_CREDIT-".$counter, 3, 2, $extra);

            //修得単位
            $model->data["GET_CREDIT"."-".$counter] = $row["GET_CREDIT"];
            $row["GET_CREDIT"] = $model->cmd != "csvInputMain" ? $row["GET_CREDIT"] : $model->data_arr[$row["SCHREGNO"]]["GET_CREDIT"];
            $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row["GET_CREDIT"] : $model->fields["GET_CREDIT"][$counter];
            $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\" onPaste=\"return showPaste(this);\"";
            $row["GET_CREDIT"] = knjCreateTextBox($objForm, $value, "GET_CREDIT-".$counter, 3, 2, $extra);

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            //rowspan
            $row["ROWSPAN"] = ($model->Properties["useOnlyTotalstudyTime"] != '1' && $model->Properties["useTotalstudyTime"] == '1') ? "2": "";

            //幅セット
            $row["SETWIDTH"] = $setwidth;

            $counter++;
            $arg["data"][] = $row;
        }

        //学年ごとの連番を格納
        foreach ($grade as $key => $val) {
            knjCreateHidden($objForm, "counter_array-".$key, implode(',', $val));
        }

        //CSV処理作成
        makeCsv($objForm, $arg);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $counter);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //貼付機能の文字数チェック用
        knjCreateHidden($objForm, "TOTALSTUDYACT_moji", $model->getPro["TOTALSTUDYACT"]["moji"]);
        knjCreateHidden($objForm, "TOTALSTUDYACT_gyou", $model->getPro["TOTALSTUDYACT"]["gyou"]);
        knjCreateHidden($objForm, "useOnlyTotalstudyTime", $model->Properties["useOnlyTotalstudyTime"]);
        knjCreateHidden($objForm, "useTotalstudyTime", $model->Properties["useTotalstudyTime"]);
        knjCreateHidden($objForm, "TOTALSTUDYTIME_moji", $model->getPro2["TOTALSTUDYTIME"]["moji"]);
        knjCreateHidden($objForm, "TOTALSTUDYTIME_gyou", $model->getPro2["TOTALSTUDYTIME"]["gyou"]);
        knjCreateHidden($objForm, "useKnjd133hOnlyGradValue", $model->Properties["useKnjd133hOnlyGradValue"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd133hindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd133hForm1.html", $arg);
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

//CSV処理作成
function makeCsv(&$objForm, &$arg)
{
    //ファイル
    $extra = "";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);
    //取込ボタン
    $extra = "onclick=\"return btn_submit('csvInput');\"";
    $arg["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra);
    //出力ボタン
    $extra = "onclick=\"return btn_submit('csvOutput');\"";
    $arg["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $counter)
{
    if ($model->Properties["useOnlyTotalstudyTime"] == '1') {
        if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] != '1') {
            if ($model->Properties["tutisyoTeikei_Button_Hyouji"] == "1") {
                //定型文選択ボタン
                $extra  = "onclick=\"return btn_submit('teikei2');\"";
                $extra .= ($model->field["CHAIRCD"] && $counter > 0) ? "" : " disabled";
                $arg["btn_teikei2"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);
            }
        }
    } elseif ($model->Properties["useTotalstudyTime"] == '1') {
        if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] != '1') {
            if ($model->Properties["tutisyoTeikei_Button_Hyouji"] == "1") {
                //学習内容定型文選択ボタン
                $extra  = "onclick=\"return btn_submit('teikei');\"";
                $extra .= ($model->field["CHAIRCD"] && $counter > 0) ? "" : " disabled";
                $arg["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "学習内容定型文選択", $extra);
                //評価定型文選択ボタン
                $extra  = "onclick=\"return btn_submit('teikei2');\"";
                $extra .= ($model->field["CHAIRCD"] && $counter > 0) ? "" : " disabled";
                $arg["btn_teikei2"] = knjCreateBtn($objForm, "btn_teikei", "評価定型文選択", $extra);
                //一括更新1
                $link = REQUESTROOT."/D/KNJD133H/knjd133hindex.php?cmd=replace1&SEMESTER=".$model->field["SEMESTER"]."&SUBCLASSCD=".$model->field["SUBCLASSCD"]."&CHAIRCD=".$model->field["CHAIRCD"]."&GRADE=".$model->subField['GRADE'];
                $extra = "onclick=\"Page_jumper('{$link}');\"";
                $extra .= ($model->field["CHAIRCD"] && $counter > 0) ? "" : " disabled";
                $arg["button"]["btn_replace1"] = knjCreateBtn($objForm, "btn_replace1", "評価文一括更新", $extra);
            }
        }
    } else {
        //定型文選択ボタン
        if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] != '1') {
            if ($model->Properties["tutisyoTeikei_Button_Hyouji"] == "1") {
                $extra  = "onclick=\"return btn_submit('teikei');\"";
                $extra .= ($model->field["CHAIRCD"] && $counter > 0) ? "" : " disabled";
                $arg["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);
            }
        }
    }

    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
