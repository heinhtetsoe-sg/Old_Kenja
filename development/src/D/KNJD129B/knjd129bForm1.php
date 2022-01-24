<?php

require_once('for_php7.php');


class knjd129bForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd129bindex.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //サブミット
        $extra = "onchange=\"return btn_submit('')\"";

        /**********/
        /* コンボ */
        /**********/
        //学年
        $query = knjd129bQuery::getGrade();
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_GRADE");
        //学期
        $query = knjd129bQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_SEMESTER");
        //テスト
        $query = knjd129bQuery::getTest($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND", $model->field["TESTKIND"], $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_TESTKIND");
        //教科
        $query = knjd129bQuery::getClassMst($model);
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_CLASSCD");
        //科目
        $query = knjd129bQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");

        //テスト名取得
        $semename = $testname = array();
        $query = knjd129bQuery::getTestName($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $semename[$row["VALUE_SEME"]] = $row["SEMESTERNAME"];
            $testname[$row["VALUE_TEST"]] = $row["TESTITEMNAME"];
            $arg["SEMENAME".$row["VALUE_SEME"]] = $row["SEMESTERNAME"];
            $arg["TESTNAME".$row["VALUE_TEST"]] = $row["TESTITEMNAME"];
        }

        //指定科目の講座を配列に保持
        $opt_chair_cmb = array();
        $result = $db->query(knjd129bQuery::selectChairQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair_cmb[] = $row["CHAIRCD"];
        }

        /****************/
        /* 算出元表示用 */
        /****************/
        //配列("指定学期テスト" => "算出元テスト")
        $motocdArray = array("10101" => "10201"
                            ,"10201" => "10101"
                            ,"19900" => "29900"
                            ,"20101" => "20201"
                            ,"20201" => "20101"
                            ,"29900" => "19900"
                            ,"30101" => "30201"
                            ,"30201" => "30101"
                            ,"39900" => "29900");
        //算出元テスト
        $testcdMoto = $motocdArray[$model->field["SEMESTER"].$model->field["TESTKIND"]];
        //指定学期テスト
        $testname_moto = (strlen($model->field["SUBCLASSCD"])) ? $semename[substr($testcdMoto, 0, 1)] ."-". $testname[$testcdMoto] : "";
        $arg["TESTNAME_MOTO"] = "算出元：" . $testname_moto;

        //出欠コード
        $backcolor = array( "1"  => "#3399ff",
                            "2"  => "#66cc33",
                            "3"  => "#66cc33",
                            "4"  => "#ff0099",
                            "5"  => "#ff0099",
                            "6"  => "#ff0099",
                            "8"  => "#3399ff",
                            "9"  => "#66cc33",
                            "10" => "#66cc33",
                            "11" => "#ff0099",
                            "12" => "#ff0099",
                            "13" => "#ff0099",
                            "14" => "#ff0099");

        //試験名称NAMECD2
        $testcdArray = array("10101" => "SEM1_INTR_SCORE"
                            ,"10201" => "SEM1_TERM_SCORE"
                            ,"19900" => "SEM1_VALUE"
                            ,"20101" => "SEM2_INTR_SCORE"
                            ,"20201" => "SEM2_TERM_SCORE"
                            ,"29900" => "SEM2_VALUE"
                            ,"30101" => "SEM3_INTR_SCORE"
                            ,"30201" => "SEM3_TERM_SCORE"
                            ,"39900" => "SEM3_VALUE");

        //配列(項目名：算出用)
        $motocdArray = array("10101" => "SEM1_TERM_SCORE"
                            ,"10201" => $testcdArray["10101"]
                            ,"19900" => "SEM2_VALUE"
                            ,"20101" => "SEM2_TERM_SCORE"
                            ,"20201" => $testcdArray["20101"]
                            ,"29900" => "SEM1_VALUE"
                            ,"30101" => "SEM3_TERM_SCORE"
                            ,"30201" => $testcdArray["30101"]
                            ,"39900" => "SEM2_VALUE");

        //時間割講座テストより試験日を抽出
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;//初期値
        } else {
            $execute_date = $edate;//初期値
        }
        $result = $db->query(knjd129bQuery::selectExecuteDateQuery($model, $opt_chair_cmb));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $execute_date = $row["EXECUTEDATE"];
        }

        //科目平均を作成
        $avgArray = array();
        foreach ($testcdArray as $code => $col) {
            $query = knjd129bQuery::getAverage(CTRL_YEAR, $code, $model->field["SUBCLASSCD"], $model->field["GRADE"], $model);
            $average = $db->getOne($query);
            //表示用
            $arg[$col."_AVG"] = $average;
            //計算用
            $avgArray[$col."_AVG"] = $average;
        }//foreach

        //計算用
        $testcd = $model->field["SEMESTER"].$model->field["TESTKIND"];
        $testCol = $testcdArray[$testcd];
        $motoCol = $motocdArray[$testcd];

        //初期化
        $model->data=array();
        $counter=0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd129bQuery::selectQuery($model, $execute_date, $opt_chair_cmb));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //表示切替
            $showFlg = false;
            foreach ($testcdArray as $code => $col) {
                if ($model->show_all != "on" && 
                    strlen($row[$col."_PASS"]) && 
                    $code == $model->field["SEMESTER"].$model->field["TESTKIND"]) {
                    $showFlg = true;
                }
            }
            if ($showFlg) {
                continue;
            }
            //見込点を計算
            if ($model->cmd == "calculate" && $model->fields["CHK_BOX"][$counter] == "on") {
                //見込点がないこと(未処理であること)!strlen($row[$testCol."_PASS"]) && 
                //科目平均・算出元成績があること
                if (strlen($avgArray[$testCol."_AVG"]) && 
                    strlen($avgArray[$motoCol."_AVG"]) && 
                    strlen($row[$motoCol])) {
                    //計算式
                    $score = $avgArray[$testCol."_AVG"] / $avgArray[$motoCol."_AVG"] * $row[$motoCol];
                    //出欠情報
                    if (strlen($row[$testCol."_ATTEND"]) && $backcolor[$row[$testCol."_ATTEND"]] == "#ff0099") {
                        //病気の時、80%
                        $score_pass = $score * 0.8;
                    } else {
                        //公欠・忌引の時、100%
                        $score_pass = $score;
                    }
                    $row[$testCol."_PASS"]  = round($score_pass);   //見込点
                    $row[$testCol."_SCORE"] = round($score);        //調査用
                }
            }
            //学籍番号をHiddenで保持
            knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);
            //クラス-出席番(表示)
            if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
            }
            //名前
            $row["NAME_SHOW"]   = $row["NAME_SHOW"];
            //５行毎に背景色を変更
            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            //各項目を作成
            foreach ($testcdArray as $code => $col) {
                $sem = substr($code, 0, 1);
                $kind = substr($code, 1, 2);
                $row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
                //異動情報
                if ($sem != "9" && (strlen($row["TRANSFER_SDATE"]) || strlen($row["TRANSFER_EDATE"]))) {
                    //学期期間中すべて異動期間の場合
                    if (strtotime($row["TRANSFER_SDATE"]) <= strtotime($model->control["学期開始日付"][$sem])
                              && strtotime($row["TRANSFER_EDATE"]) >= strtotime($model->control["学期終了日付"][$sem])) {
                        $row[$col."_COLOR"]="#ffff00";
                    //一部
                    } elseif ((strtotime($row["TRANSFER_SDATE"]) >= strtotime($model->control["学期開始日付"][$sem]))
                           && (strtotime($row["TRANSFER_SDATE"]) <= strtotime($model->control["学期終了日付"][$sem]))) {
                        $row[$col."_COLOR"]="#ffff00";
                    } elseif ((strtotime($row["TRANSFER_EDATE"]) >= strtotime($model->control["学期開始日付"][$sem]))
                           && (strtotime($row["TRANSFER_EDATE"]) <= strtotime($model->control["学期終了日付"][$sem]))) {
                        $row[$col."_COLOR"]="#ffff00";
                    }
                //卒業日付
                } elseif ($sem != "9" && strlen($row["GRD_DATE"])) {
                    //学期期間中すべて卒業の場合(学期開始日付以前に卒業している場合）
                    if (strtotime($row["GRD_DATE"]) <= strtotime($model->control["学期開始日付"][$sem])) {
                        $row[$col."_COLOR"]="#ffff00";
                    //一部
                    } elseif (strtotime($row["GRD_DATE"]) > strtotime($model->control["学期開始日付"][$sem])
                             && strtotime($row["GRD_DATE"]) <= strtotime($model->control["学期終了日付"][$sem])) {
                        $row[$col."_COLOR"]="#ffff00";
                    }
                }
                //在籍情報がない場合
                if ($sem != "9" && !strlen($row["CHAIR_SEM".$sem])) {
                    if ($sem <= CTRL_SEMESTER) $row[$col."_COLOR"]="#ffff00";
                }
                //出欠情報
                if (strlen($row[$col."_ATTEND"])) {
                    $row[$col."_COLOR"] = $backcolor[$row[$col."_ATTEND"]];
                }

                //指定学期テスト
                if ($code == $model->field["SEMESTER"].$model->field["TESTKIND"]) {
                    //見込点指定チェックボックスを作成
                    $extra = (strlen($row[$col."_PASS"])) ? "checked" : "";
                    $row["CHK_BOX"] = knjCreateCheckBox($objForm, "CHK_BOX"."-".$counter, "on", $extra);
                    //テキストボックスを作成
                    $value = (strlen($row[$col."_PASS"])) ? $row[$col."_PASS"] : "";
                    $extra = "STYLE=\"text-align: right;\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" ";
                    $row[$col] = knjCreateTextBox($objForm, $value, $col."-".$counter, 3, 3, $extra);
                    //考査満点マスタ
                    $query = knjd129bQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $code, $row["GRADE"], $row["COURSE"], $model);
                    $perfect = ($model->usePerfect == 'true') ? $db->getOne($query) : 100;
                    if ($perfect == "") $perfect = 100;
                    //満点チェック用を作成
                    knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);
                    //調査用を作成
                    $value = (strlen($row[$col."_SCORE"])) ? $row[$col."_SCORE"] : "";
                    knjCreateHidden($objForm, $col."_SCORE"."-".$counter, $value);
                //ラベルのみ
                } else {
                    $value = (strlen($row[$col."_PASS"])) ? "(".$row[$col."_PASS"].")" : $row[$col];
                    $row[$col] = "<font color=\"#000000\">".$value."</font>";
                }
            }//foreach
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            $counter++;
            $arg["data"][] = $row;
        }//while

        knjCreateHidden($objForm, "COUNT", $counter);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $counter);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd129bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
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
function makeBtn(&$objForm, &$arg, $model, $counter) {
    $disBtn = (0 < $counter) ? "" : " disabled";
    //処理済表示ボタン
    $chg_val = ($model->show_all == "on")?  "しない" : "" ;
    $extra = "onclick=\"return btn_submit('show_all');\"";
    $arg["btn_show"] = knjCreateBtn($objForm, "btn_show", "処理済みを表示".$chg_val, $extra);
    //見込点算出ボタン
    $extra = "onclick=\"return btn_submit('calculate');\"";
    $arg["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "見込点算出", $extra.$disBtn);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disBtn);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    //表示切替フラグ
    knjCreateHidden($objForm, "shw_flg", $model->show_all);
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}
?>
