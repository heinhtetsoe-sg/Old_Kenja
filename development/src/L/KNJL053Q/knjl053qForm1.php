<?php

require_once('for_php7.php');

class knjl053qForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl053qQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        if (SCHOOLKIND == "J") {
            $query = knjl053qQuery::getNameMst("L024", $model->ObjYear);
        } else {
            $query = knjl053qQuery::getNameMst("L004", $model->ObjYear);
        }
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //表示内容ラジオボタン
        $opt = array(1, 2, 3);
        $model->field["SHOWDIV"] = ($model->field["SHOWDIV"] == "") ? "1" : $model->field["SHOWDIV"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SHOWDIV{$val}\" onClick=\"btn_submit('main')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SHOWDIV", $model->field["SHOWDIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        if (SCHOOLKIND != "J") {
            //一般入試選択で「基準テストを含める・含めない・のみ」ラジオボタン
            if ($model->testdiv == "5") {
                $arg["isIppan"] = ($model->testdiv == "5") ? 1 : "";
                $opt = array(1, 2, 3);
                $model->field["KIJUN_TEST_DIV"] = ($model->field["KIJUN_TEST_DIV"] == "") ? "1" : $model->field["KIJUN_TEST_DIV"];
                $extra = array();
                foreach ($opt as $key => $val) {
                    array_push($extra, " id=\"KIJUN_TEST_DIV{$val}\" onClick=\"btn_submit('main')\"");
                }
                $radioArray = knjCreateRadio($objForm, "KIJUN_TEST_DIV", $model->field["KIJUN_TEST_DIV"], $extra, $opt, get_count($opt));
                foreach ($radioArray as $key => $val) {
                    $arg["TOP"][$key] = $val;
                }
            }
        }

        //配列(受験科目)
        $model->subclassList = array();
        $result = $db->query(knjl053qQuery::getTestSubclasscd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->subclassList[$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();

        //ヘッダー
        foreach ($model->subclassList as $subclassCd => $subclassName) {
            $arg["HEADER"]["SUBCLASS"] .= "<td colspan=\"3\" nowrap>{$subclassName}</td>";
            $arg["HEADER"]["NYURYOKU_ATTEND"] .= "<td width=\"50\" nowrap>1回目</td>";
            $arg["HEADER"]["NYURYOKU_ATTEND"] .= "<td width=\"50\" nowrap>2回目</td>";
            $arg["HEADER"]["NYURYOKU_ATTEND"] .= "<td width=\"50\" nowrap>欠席</td>";
        }
        $add_width = get_count($model->subclassList) > 3 ? (get_count($model->subclassList) - 3) * 155 : 0;
        $arg["MAIN_WIDTH"] = 950 + $add_width;

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $result = $db->query(knjl053qQuery::selectQuery($model));
            if ($result->numRows() == 0) {
                if ($model->messageFlg != 0) {
                    $model->setMessage("MSG201", "\\n条件に該当するデータはありません。");
                } else {
                    $model->setMessage("MSG303");
                }
            }
            $model->messageFlg = 0;

            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //背景色
                $row["BGCOLOR"] = "#ffffff";

                //判定フラグ
                $judgeFlg = 0;

                $meisai = "";
                foreach ($model->subclassList as $subclassCd => $subclassName) {
                    $scoreName1 = "SCORE1_".$subclassCd;
                    $scoreName2 = "SCORE2_".$subclassCd;
                    $attendName = "ATTEND_".$subclassCd;
                    $perfectName = "PERFECT_".$subclassCd;

                    //判定
                    //得点が合わない
                    if ($row[$scoreName1] != $row[$scoreName2]) {
                        $judgeFlg = 1;
                    }
                    //欠席なのに得点入力あり
                    if ($row[$attendName] === '0' && (strlen($row[$scoreName1]) || strlen($row[$scoreName2]))) {
                        $judgeFlg = 1;
                    }
                    //欠席してないのに得点が入っていない
                    if ($row[$attendName] !== '0' && (!strlen($row[$scoreName1]) || !strlen($row[$scoreName2]))) {
                        $judgeFlg = 1;
                    }

                    //欠席の時
                    if ($row[$attendName] === '0') {
                        $row[$scoreName1] = '*';
                    }
                    if ($row[$attendName] === '0') {
                        $row[$scoreName2] = '*';
                    }

                    if ($model->field["SHOWDIV"] == "2" || $model->field["SHOWDIV"] == "3") {
                        //得点1
                        $extra = " id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;background-color:".$row["BGCOLOR"]."\" onblur=\"CheckScore(this, '{$row[$perfectName]}');\" ";
                        $row[$scoreName1] = knjCreateTextBox($objForm, $row[$scoreName1], $scoreName1."[]", 3, 3, $extra);
                        //得点2
                        $extra = " id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;background-color:".$row["BGCOLOR"]."\" onblur=\"CheckScore(this, '{$row[$perfectName]}');\" ";
                        $row[$scoreName2] = knjCreateTextBox($objForm, $row[$scoreName2], $scoreName2."[]", 3, 3, $extra);
                    }
                    //欠席
                    $row[$attendName] = ($row[$attendName] === '0') ? '欠' : '';

                    $meisai .= "<td width=\"50\" align=\"center\" nowrap>".$row[$scoreName1]."</td>";
                    $meisai .= "<td width=\"50\" align=\"center\" nowrap>".$row[$scoreName2]."</td>";
                    $meisai .= "<td width=\"50\" align=\"center\" nowrap>".$row[$attendName]."</td>";
                }
                $row["MEISAI"] = $meisai;
                //判定
                $row["JUDGE"] = ($judgeFlg != 0) ? "×" : "○";
                $row["BGCOLOR"] = ($judgeFlg != 0) ? "#fcffcc" : "#ffffff";

                $arg["data"][] = $row;
                $count++;
            }
        }

        //ボタン作成
        //更新ボタン
        if (get_count($arr_receptno) > 0 &&get_count($model->subclassList) > 0 && ($model->field["SHOWDIV"] == "2" || $model->field["SHOWDIV"] == "3")) {
            $extra = "onclick=\"return btn_submit('update');\"";
            $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        }
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");

        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl053qindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl053qForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
