<?php

require_once('for_php7.php');


class knjd129mForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd129mindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd129mQuery::getSemester();
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, '');
        $model->schSeme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //学年コンボ
        $query = knjd129mQuery::getGrade();
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, '');

        //コースコンボ
        $query = knjd129mQuery::getCourse($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["COURSE"], "COURSE", $extra, 1, '');

        //テストコンボ
        $query = knjd129mQuery::getTest($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["TESTIK"], "TESTIK", $extra, 1, '');

        //科目コンボ
        $query = knjd129mQuery::getSubclassCd($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["SUBCLASS_CD"], "SUBCLASS_CD", $extra, 1, 'BLANK');

        //明細ヘッダデータ作成
        $model->testcdArray = makeHead($objForm, $arg, $db, $model);

        //明細データ作成
        makeMeisai($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd129mForm1.html", $arg);
    }
}

function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                        "value" => "");
    }
    $flg = 0;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($row["VALUE"] == $value) {
            $flg = 1;
        }
    }
    $result->free();

    $value = $flg == 1 ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model)
{
    //科目別設定があるか
    $testSubCnt = $db->getOne(knjd129mQuery::getTestSubCnt($model));
    if ($testSubCnt == "") {
        $testSubCnt = 0;
    }

    $count = 0;
    $sem = "";
    $col = 0;
    $semArray = array();
    $head1 = "";
    $head2 = "";
    $head9 = "";
    $testcdArray = array();

    $result = $db->query(knjd129mQuery::getTestName($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $count++;
        if ($sem != $row["SEMESTER"]) {
            $sem = $row["SEMESTER"];
            $col = 0;
        }
        $col++;
        $semArray[$row["SEMESTER"]] = "<th colspan={$col} ><font size=2>".$row["SEMESTERNAME"]."</font></th> ";

        if ($row["SEMESTER"] != "9") {
            $head2 .= "<th width=70 ><font size=2>".$row["TESTITEMNAME"]."</font></th> ";
        } else {
            if ($row["TESTCD"] == "9990009") {
                //仮評定フラグ対応
                if ($model->Properties["useProvFlg"] == '1') {
                    $head9 .= "<th rowspan=2 width=25><font size=2>仮<br>評<br>定</font></th> ";
                    $count++;
                }
                $head9 .= "<th rowspan=2 width=* ><font size=2>".$row["TESTITEMNAME"]."</font></th> ";
            } else {
                $head9 .= "<th rowspan=2 width=70 ><font size=2>".$row["TESTITEMNAME"]."</font></th> ";
            }
        }
        $testcdArray[] = array("TESTCD" => $row["TESTCD"], "SEMESTERNAME" => $row["SEMESTERNAME"], "TESTITEMNAME" => $row["TESTITEMNAME"], "SIDOU_INPUT" => $row["SIDOU_INPUT"], "SIDOU_INPUT_INF" => $row["SIDOU_INPUT_INF"], "CONTROL_FLG" => $row["CONTROL_FLG"], "SEMESTER" => $row["SEMESTER"], "SDATE" => $row["SDATE"], "EDATE" => $row["EDATE"]);
    }
    $result->free();
    foreach ($semArray as $key => $val) {
        if ($key == "9") {
            continue;
        }
        $head1 .= $val;
    }
    $arg["HEAD1"] = $head1 . $head9; //学期名称
    $arg["HEAD2"] = $head2; //考査種別名称
    $arg["ALL_WIDTH"] = 1930; //画面全体幅
    $arg["FOOT_COLSPAN"] = 2 + $count; //画面全体幅
    return $testcdArray;
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model)
{

    //初期化
    $model->data = array();
    $counter = 0;
    //一覧表示
    $colorFlg = false;
    $result = $db->query(knjd129mQuery::selectQuery($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍＋科目
        $rowKey = $row["SCHREGNO"]."-".$row["SUBCLASSCD"];

        //checkboxフラグ
        $score_flg = false;
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $scoreRow = array();
            $query = knjd129mQuery::getScore(CTRL_YEAR, $testcd, $model, $rowKey);
            $scoreRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($testcd == $model->field["SEMESTER"].str_replace("-", "", $model->field["TESTIK"])) {
                //(素点)(ＡＡ)(得点)テキストいずれか入力済みの時
                if ($scoreRow["SCORE_FLG"] == "2") {
                    $score_flg = true;
                }
            }
        }
        //処理済を表示しない(素点)(ＡＡ)(得点)
        if (!$model->dispZumi) {
            if ($score_flg) {
                continue;
            }
        }

        //学籍番号をHiddenで保持
        knjCreateHidden($objForm, "KEY"."_".$counter, $rowKey);

        //クラス-出席番(表示)
        if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
            $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
        }
        //５行毎に背景色を変更
        if ($counter % 5 == 0) {
            $colorFlg = !$colorFlg;
        }
        $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

        //各項目を作成
        $meisai = "";
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];
            $col = "SCORE" .$testcd;

            //成績データ
            $scoreRow = array();
            $query = knjd129mQuery::getScore(CTRL_YEAR, $testcd, $model, $rowKey);
            $scoreRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $score = $scoreRow["SCORE"];

            //追指導データ 1:追指導入力
            $colorFlgPink = false;
            $fontcolorFlgRed = false;

            //テキストボックスを作成
            if ($testcd == $model->field["SEMESTER"].str_replace("-", "", $model->field["TESTIK"])) {
                if ($model->cmd == "sanSyutu" && strlen($score) == 0) {
                    $score = getScoreSansyutu($db, $testcd, $model, $row["SCHREGNO"], $rowKey, $row["SUBCLASSCD"], $row["GROUP_CD"]);
                }
                $row["FONTCOLOR"] = $fontcolorFlgRed ? "#ff0000" : "#000000";
                $extra = " style=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"this.value=toInteger(this.value);\" ";
                $row[$col] = knjCreateTextBox($objForm, $score, "SCORE_".$rowKey.$testcd, 3, 3, $extra);
            //ラベルのみ
            } else {
                $row["FONTCOLOR"] = $fontcolorFlgRed ? "#ff0000" : "#000000";
                $setScore = $scoreRow["DIV"] == "1" ? "({$score})" : $score;
                $row[$col] = "<font color={$row["FONTCOLOR"]}>".$setScore."</font>";
            }
            //仮評定
            if ($testcd == "9990009") {
                if ($model->Properties["useProvFlg"] == '1') {
                    $chk = $row["PROV_FLG"] == '1' ? ' checked="checked" ' : '';
                    $dis = $controlFlg == '1' ? '' : ' disabled="disabled" ';
                    $row["PROV_FLG"] = knjCreateCheckBox($objForm, "PROV_FLG"."-".$counter, "1", $chk.$dis);
                    $meisai .= "<td width=25 align=\"center\" bgcolor={$row["COLOR"]}>".$row["PROV_FLG"]."</td>";
                }
            }
            $row["BGCOLOR"] = $row["COLOR"]; //通常の背景色
            if ($testcd != "9990009" && $colorFlgYellow) {
                $row["BGCOLOR"] = "#ffff00";
            } //異動
            if ($colorFlgPink) {
                $row["BGCOLOR"] = "#ffc0cb";
            } //指導
            if ($testcd == "9990009") {
                $meisai .= "<td width=* align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";
            } else {
                $meisai .= "<td width=70 align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";
            }
        }
        $row["MEISAI"] = $meisai;

        //checkbox
        $extra = $score_flg ? " checked " : "";
        $row["CHK_BOX"] = knjCreateCheckBox($objForm, "CHK_BOX_".$rowKey, "1", $extra);

        $counter++;
        $arg["data"][] = $row;
    }
    $result->free();
    //件数
    knjCreateHidden($objForm, "COUNTER", $counter);
}

function getScoreSansyutu($db, $testcd, $model, $schregNo, $rowKey, $subclassCd, $groupCd)
{
    $score = "";
    //個人素点
    $query = knjd129mQuery::getRecordRankSdiv($model, $rowKey);
    $result = $db->query($query);
    $testArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $testArray[$row["SEMESTER"].$row["TESTKINDCD"].$row["TESTITEMCD"]] = $row["SCORE"];
        $testDiArray[$row["SEMESTER"].$row["TESTKINDCD"].$row["TESTITEMCD"]] = $row["VALUE_DI"];
    }
    $result->free();

    if ($model->field["SEMESTER"] == "1") {
        if (str_replace("-", "", $model->field["TESTIK"]) == $model->TYUKAN) {
            $rate = ($testDiArray["10101"] == "*") ? 0.7 : 1;
            if (strlen($testArray["10201"]) > 0) {
                //1学期中間欠試は、1学期期末参照
                $score = floor($testArray["10201"] * $rate);
            } elseif (strlen($testArray["10201"]) == 0) {
                if (strlen($testArray["20101"]) > 0 && strlen($testArray["20201"]) > 0 && strlen($testArray["30201"]) > 0) {
                    //1学期欠試は、2,3学期参照
                    $score = floor(($testArray["20101"] + $testArray["20201"] + $testArray["30201"]) / 3 * $rate);
                } elseif (strlen($testArray["20101"]) > 0 && strlen($testArray["20201"]) > 0) {
                    //1,3学期欠試は、2学期参照
                    $score = floor(($testArray["20101"] + $testArray["20201"]) / 2 * $rate);
                } elseif (strlen($testArray["30201"]) > 0) {
                    //1,2学期欠試は、3学期参照
                    $score = floor($testArray["30201"] * $rate);
                } else {
                    //1,2,3学期欠試は、見込点なし・・・TODO
                }
            }
        } elseif (str_replace("-", "", $model->field["TESTIK"]) == $model->KIMATU) {
            $rate = ($testDiArray["10201"] == "*") ? 0.7 : 1;
            if (strlen($testArray["10101"]) > 0) {
                //1学期期末欠試は、1学期中間参照
                $score = floor($testArray["10101"] * $rate);
            } elseif (strlen($testArray["10101"]) == 0) {
                if (strlen($testArray["20101"]) > 0 && strlen($testArray["20201"]) > 0 && strlen($testArray["30201"]) > 0) {
                    //1学期欠試は、2,3学期参照
                    $score = floor(($testArray["20101"] + $testArray["20201"] + $testArray["30201"]) / 3 * $rate);
                } elseif (strlen($testArray["20101"]) > 0 && strlen($testArray["20201"]) > 0) {
                    //1,3学期欠試は、2学期参照
                    $score = floor(($testArray["20101"] + $testArray["20201"]) / 2 * $rate);
                } elseif (strlen($testArray["30201"]) > 0) {
                    //1,2学期欠試は、3学期参照
                    $score = floor($testArray["30201"] * $rate);
                } else {
                    //1,2,3学期欠試は、見込点なし・・・TODO
                }
            }
        }
    } elseif ($model->field["SEMESTER"] == "2") {
        if (str_replace("-", "", $model->field["TESTIK"]) == $model->TYUKAN) {
            $rate = ($testDiArray["20101"] == "*") ? 0.7 : 1;
            if (strlen($testArray["20201"]) > 0) {
                //2学期中間欠試は、2学期期末参照
                $score = floor($testArray["20201"] * $rate);
            } elseif (strlen($testArray["20201"]) == 0) {
                if (strlen($testArray["10101"]) > 0 && strlen($testArray["10201"]) > 0 && strlen($testArray["30201"]) > 0) {
                    //2学期欠試は、1,3学期参照
                    $score = floor(($testArray["10101"] + $testArray["10201"] + $testArray["30201"]) / 3 * $rate);
                } elseif (strlen($testArray["10101"]) > 0 && strlen($testArray["10201"]) > 0) {
                    //2,3学期欠試は、1学期参照
                    $score = floor(($testArray["10101"] + $testArray["10201"]) / 2 * $rate);
                } elseif (strlen($testArray["30201"]) > 0) {
                    //1,2学期欠試は、3学期参照
                    $score = floor($testArray["30201"] * $rate);
                } else {
                    //1,2,3学期欠試は、見込点なし・・・TODO
                }
            }
        } elseif (str_replace("-", "", $model->field["TESTIK"]) == $model->KIMATU) {
            $rate = ($testDiArray["20201"] == "*") ? 0.7 : 1;
            if (strlen($testArray["20101"]) > 0) {
                //2学期期末欠試は、2学期中間参照
                $score = floor($testArray["20101"] * $rate);
            } elseif (strlen($testArray["20101"]) == 0) {
                if (strlen($testArray["10101"]) > 0 && strlen($testArray["10201"]) > 0 && strlen($testArray["30201"]) > 0) {
                    //2学期欠試は、1,3学期参照
                    $score = floor(($testArray["10101"] + $testArray["10201"] + $testArray["30201"]) / 3 * $rate);
                } elseif (strlen($testArray["10101"]) > 0 && strlen($testArray["10201"]) > 0) {
                    //2,3学期欠試は、1学期参照
                    $score = floor(($testArray["10101"] + $testArray["10201"]) / 2 * $rate);
                } elseif (strlen($testArray["30201"]) > 0) {
                    //1,2学期欠試は、3学期参照
                    $score = floor($testArray["30201"] * $rate);
                } else {
                    //1,2,3学期欠試は、見込点なし・・・TODO
                }
            }
        }
    } else {
        if (str_replace("-", "", $model->field["TESTIK"]) == $model->KIMATU) {
            $rate = ($testDiArray["30201"] == "*") ? 0.7 : 1;
            if (strlen($testArray["10101"]) > 0 && strlen($testArray["10201"]) > 0 && strlen($testArray["20101"]) > 0 && strlen($testArray["20201"]) > 0) {
                //3学期欠試は、1,2学期参照
                $score = floor(($testArray["10101"] + $testArray["10201"] + $testArray["20101"] + $testArray["20201"]) / 4 * $rate);
            } elseif (strlen($testArray["10101"]) > 0 && strlen($testArray["10201"]) > 0) {
                //2,3学期欠試は、1学期参照
                $score = floor(($testArray["10101"] + $testArray["10201"]) / 2 * $rate);
            } elseif (strlen($testArray["20101"]) > 0 && strlen($testArray["20201"]) > 0) {
                //1,3学期欠試は、2学期参照
                $score = floor(($testArray["20101"] + $testArray["20201"]) / 2 * $rate);
            } else {
                //1,2,3学期欠試は、見込点なし・・・TODO
            }
        }
    }
    return $score;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "blank") {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
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
    //算出ボタン
    $extra = "onclick=\"return btn_submit('sanSyutu');\"";
    $arg["btn_sansyutu"] = knjCreateBtn($objForm, "btn_sansyutu", "算 出", $extra);
    //表示切替ボタン
    $setBtnName = $model->dispZumi ? "処理済みを表示しない" : "処理済みを表示";
    $extra = "onclick=\"return btn_submit('disp');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", $setBtnName, $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}
