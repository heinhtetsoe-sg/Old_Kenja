<?php

require_once('for_php7.php');

class knjm430kForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $db = Query::dbCheckOut();

        //TOP***************************************************************************************

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR;

        //出力順ラジオボタン(1:学籍番号順 2:クラス番号順)
        $opt = array(1, 2);
        $model->order = ($model->order == "") ? "1" : $model->order;
        $extra = array("id=\"ORDER1\""." onclick=\"btn_submit('change_order');\"", "id=\"ORDER2\""." onclick=\"btn_submit('change_order');\"");
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->order, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //出力順が変わったら、科目（講座）リストはクリアされる
        if ($model->cmd == "change_order") $model->sub = "";

        //科目(講座）リスト
        $query = knjm430kQuery::getChrSubCd($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $model->sub, "SELSUB", $extra, 1, "BLANK");

        //学級・講座リスト
        $query = knjm430kQuery::selectChairQuery($model);
        $extra = "onChange=\"btn_submit('change_chaircd')\";";
        makeCmb($objForm, $arg, $db, $query, $model->chair, "CHAIRCD", $extra, 1, "BLANK");

        //明細ヘッダデータ作成
        $model->testcdArray = makeHead($objForm, $arg, $db, $model);

        //DATA***************************************************************************************

        //集計項目
        $term_data = array();

        //データ配列
        $dataArray = array();
        $sch_array = array();
        $class_date = array();
        $name_date = array();
        $arg["footer_th"] = 1;

        //成績データのリスト
        if ($model->chair != "") {
            
            $result  = $db->query(knjm430kQuery::GetRecordDatdata($model));

            //件数カウント用初期化
            $ca = 0;        //データ全件数
            while ($row_array = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $ca++;
                //学籍番号
                $sch_array[$ca] = $row_array["SCHREGNO"];
                //学年、組、番号
                $class_date[$ca] = $row_array["HR_NAME"] . "-" . $row_array["ATTENDNO"];
                //生徒氏名
                $name_date[$ca] = $row_array["NAME_SHOW"];

                //成績・集計項目
                foreach ($model->testcdArray as $key => $codeArray) {
                    $value = "VALUE_DI".$key;
                    $col = "SCORE".$key;
                    $dataArray[$col][$ca] = $row_array[$value] == "*" ? $row_array[$value] : $row_array[$col];
                    $dataArray["SEM_PASS_FLG".$key][$ca] = $row_array["SEM_PASS_FLG".$key];
                    $dataArray["PASS_SCORE".$key][$ca] = $row_array["PASS_SCORE".$key];
                    if (is_numeric($row_array[$col])) {
                        $term_data[$col][] = $row_array[$col];
                    }
                }
            }
            $result->free();


            //成績レコードが存在する場合
            if ($ca != 0) {
                //ページの１行目のカレント行番号（配列指標）
                if ($model->line == "") {
                    $currentline = 1;
                } else {
                    if ($model->cmd == "change") {
                        $currentline = 1;
                    } else {
                        $currentline = $model->line;
                    }
                }

                //50件表示ループ
                $counts = 1;                            //カレントページ内での行数
                $pageline = 0;                          //このページの最後の行の全件数の中での行数
                $lineall = $currentline + 50;

                $colorFlg = false; //５行毎に背景色を変更
                $count = $db->getOne(knjm430kQuery::getTestCnt($model));
                $count = $count -1;

                for ($pageline=$currentline; $pageline<$lineall; $pageline++,$counts++) {

                    //カレント行が全件数を超えたらループ終り
                    if ($pageline > $ca) {
                        break;
                    }

                    //学籍番号
                    $row["SCHREGNO"] = $sch_array[$pageline];
                    knjCreateHidden($objForm, "SCHREGNO_".$counts, $sch_array[$pageline]);

                    //クラス番号（学年－クラス－出席番号）
                    $row["GRA_HR_ATTEND"] =  $class_date[$pageline];

                    //生徒氏名
                    $row["NAME_SHOW"] =  $name_date[$pageline];

                    //成績
                    $meisai = "";
                    $row["COLOR"] = "#cccccc";
                    foreach ($model->testcdArray as $key => $codeArray) {
                        if($count == $key){
                            $width = "*";
                        } else {
                            $width = "225";
                        }
                        $col = "SCORE".$key;
                        //受験許可フラグ
                        if ($db->getOne(knjm430kQuery::GetSemPassFlg($model, $row["SCHREGNO"], $codeArray["TESTCD"])) == "1") {
                            //テキスト
                            $extra = "STYLE=\"text-align: right\" onblur=\"check(this)\"";
                            $row[$col] = knjCreateTextBox($objForm, $dataArray[$col][$pageline], $col."_".$counts, 3, 5, $extra);
                            knjCreateHidden($objForm, "HIDDEN".$key."_".$counts, true);
                            $row["COLOR"] = "#ffffff";
                        //その他
                        } else {
                            //表示
                            $row[$col] = $dataArray[$col][$pageline];
                            knjCreateHidden($objForm, $col."_".$counts, $dataArray[$col][$pageline]);
                            knjCreateHidden($objForm, "HIDDEN".$key."_".$counts, false);
                        }
                        $meisai .= "<td width=".$width." align=\"center\" >".$row[$col]."</td>";
                        //合格点
                        knjCreateHidden($objForm, "PASS_SCORE".$key."_".$counts, $dataArray["PASS_SCORE".$key][$pageline]);
                    }
                    $row["MEISAI"] = $meisai;
                    $arg["data"][] = $row;
                    $arg["footer_th"] = 0;
                }
            } else {    //成績データが存在しない場合（最初に開いた時も）
                $currentline = 0;
            }
        }

        //平均・最高点・最低点表示
        $count = $db->getOne(knjm430kQuery::getTestCnt($model));
        $count_val = 0;
        foreach ($model->testcdArray as $key => $codeArray) {
            $scoreAvg = $scoreMax = $scoreMin = "";
            $col = "SCORE".$key;
            if ($ca != 0) {
                if (isset($term_data[$col])) {
                    //平均
                    $scoreAvg = round((array_sum($term_data[$col])/get_count($term_data[$col]))*10)/10;
                    //最高点と最低点を求める
                    array_multisort ($term_data[$col], SORT_NUMERIC);
                    $max = get_count($term_data[$col])-1;
                    //最高点
                    $scoreMax = $term_data[$col][$max];
                    //最低点
                    $scoreMin = $term_data[$col][0];
                }
            }
            
            $count_val++;
            if($count == $count_val){
                //平均
                $arg["SCORE_AVG"] .= "<th width=* >".$scoreAvg."</th> ";
                //最高点
                $arg["SCORE_MAX"] .= "<th width=* >".$scoreMax."</th> ";
                //最低点
                $arg["SCORE_MIN"] .= "<th width=* >".$scoreMin."</th> ";
            } else {
                //平均
                $arg["SCORE_AVG"] .= "<th width=225 >".$scoreAvg."</th> ";
                //最高点
                $arg["SCORE_MAX"] .= "<th width=225 >".$scoreMax."</th> ";
                //最低点
                $arg["SCORE_MIN"] .= "<th width=225 >".$scoreMin."</th> ";
            }
        }


        //BUTTON***************************************************************************************

        //件数表示
        if ($ca == 0) {
            $arg["page_count"] = "0-0 / 0";
            $disabled = "disabled ";
        } else {
            $arg["page_count"] = $currentline . "-" . --$pageline . " / " . $ca;
            $disabled = "";
        }
        //ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => $disabled ."onClick=\"btn_submit('update');\"" ) );

        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onClick=\"btn_submit('reset');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_pre",
                            "value"       => "前ページ",
                            "extrahtml"   => "onClick=\"btn_submit('pre');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => "次ページ",
                            "extrahtml"   => "onClick=\"btn_submit('next');\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_update");
        $arg["btn_reset"]  = $objForm->ge("btn_reset");
        $arg["btn_end"]    = $objForm->ge("btn_end");

        //「前ページ」「次ページ」ボタンはレコード件数がゼロの時は表示しない
        if ($ca != 0) {
            if ($currentline != 1) {
                $arg["btn_pre"]  = $objForm->ge("btn_pre");
            }
            if ($pageline < $ca) {
                $arg["btn_next"] = $objForm->ge("btn_next");
            }
        }

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "line", $currentline);
        knjCreateHidden($objForm, "linecounts", --$counts);

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);


        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm430kindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm430kForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model) {
    $sem = "";
    $semCol = 0;
    $semArray = array();
    $head1 = "";
    $testcdArray = array();
    $head2 = "";
    if ($model->chair != "") {
        $count = $db->getOne(knjm430kQuery::getTestCnt($model));
        $semCount = $db->getOne(knjm430kQuery::getSemeCnt($model));
        $count_val = 0;
        $result = $db->query(knjm430kQuery::getTest($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学期名称
            if ($sem != $row["SEMESTER"]) {
                $sem = $row["SEMESTER"];
                $semCol = 0;
            }
            $count_val++;
            $semCol++;
            if($semCount == $semCol){
                $semArray[$row["SEMESTER"]] = "<th colspan={$semCount} width=* >".$row["SEMESTERNAME"]."</th> ";
            } else {
                $semArray[$row["SEMESTER"]] = "<th colspan={$semCount} width=450>".$row["SEMESTERNAME"]."</th> ";
            }
            //考査種別名称
            $testcdArray[] = array("TESTCD" => $row["TESTCD"], "TESTITEMNAME" => $row["TESTITEMNAME"], "SEMESTER" => $row["SEMESTER"], "SEMESTERNAME" => $row["SEMESTERNAME"]);
            if($count == $count_val){
                $head2 .= "<th width=* >".$row["TESTITEMNAME"]."</th> ";
            } else {
                $head2 .= "<th width=225 >".$row["TESTITEMNAME"]."</th> ";
            }

        }
        $result->free();
    }
    foreach ($semArray as $key => $val) {
        $head1 .= $val;
    }
    $arg["HEAD1"] = $head1; //学期名称
    $arg["HEAD2"] = $head2; //考査種別名称
    $arg["FOOT_COLSPAN"] = 1 +get_count($testcdArray);
    return $testcdArray;
}
?>
