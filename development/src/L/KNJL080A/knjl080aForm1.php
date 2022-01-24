<?php

require_once('for_php7.php');

class knjl080aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //初期画面判定
        $defaultFlg = (!$model->applicantdiv && !$model->testdiv) ? true : false;

        //校種区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl080aQuery::getNameMst($model->ObjYear, 'L003');
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //試験コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl080aQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //表示順
        $opt = array(1, 2, 3);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SORT{$val}\" onchange=\"return btn_submit('main');\" tabindex=-1");
        }
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //合格コース
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl080aQuery::getPassCourseCmb($model);
        makeCmb($objForm, $arg, $db, $query, "PASS_COURSE", $model->passCourse, $extra, 1, "ALL");

        //タイトル
        $arg["COURSE1_EVENT"] = " id=\"COURSE1\" onClick=\"changeAllCourse('COURSE1')\" onMouseOver=\"changeColor('on', '1', 'COURSE1')\" onMouseOut=\"changeColor('off', '1', 'COURSE1')\" ";
        $arg["COURSE2_EVENT"] = " id=\"COURSE2\" onClick=\"changeAllCourse('COURSE2')\" onMouseOver=\"changeColor('on', '1', 'COURSE2')\" onMouseOut=\"changeColor('off', '1', 'COURSE2')\" ";
        knjCreateHidden($objForm, "SELECT_COURSE");

        //合格コース一覧
        $passCourse = array();
        $namecd1= 'L'.$model->skArray[$model->applicantdiv].'13';
        $query = knjl080aQuery::getNameMst($model->ObjYear, $namecd1);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMESPARE1"] == "1") {
                $passCourse[] = $row["VALUE"];
            }
        }
        $result->free();
        knjCreateHidden($objForm, "HID_PASS_COURSE", implode(",", $passCourse));

        //一覧表示
        $arr_ReceptNo = array();
        $dataFlg = false;
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl080aQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 && !$defaultFlg) {
                $model->setMessage("MSG303");
            }

            $hidenLinkRecept = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                $row["ROWID"] = "ROW_".$row["RECEPTNO"];
                if ($hidenLinkRecept[$row["EXAMNO"]]) {
                    $hidenLinkRecept[$row["EXAMNO"]] .= "_".$row["RECEPTNO"];
                } else {
                    $hidenLinkRecept[$row["EXAMNO"]] = $row["EXAMNO"].":".$row["RECEPTNO"];
                }

                //HIDDENに保持する用
                $arr_ReceptNo[] = $row["RECEPTNO"];
                knjCreateHidden($objForm, "EXAMNO-".$row["RECEPTNO"], $row["EXAMNO"]);
                knjCreateHidden($objForm, "DEFAULT_SCORE-".$row["RECEPTNO"], $row["INPUT_SHDIV"]);
                knjCreateHidden($objForm, "SHDIV-".$row["RECEPTNO"], $row["SHDIV"]);
                knjCreateHidden($objForm, "TESTDIV-".$row["RECEPTNO"], $row["TESTDIV"]);

                for ($i = 1; $i <= 2; $i++) {
                    knjCreateHidden($objForm, "COURSENAME{$i}-".$row["RECEPTNO"], $row["COURSENAME".$i."_2"]);
                    knjCreateHidden($objForm, "COURSECD{$i}-".$row["RECEPTNO"], $row["COURSECD".$i]);

                    $id = "COURSE".$i."_".$row["RECEPTNO"];
                    $flg = ($row["SHDIV"] < $i) ? 1 : 0;
                    $bgcolor = ($flg || !in_array($row["COURSECD".$i], $passCourse)) ? "class=\"no_search\"" : (($row["INPUT_SHDIV"] == $i) ? "bgcolor=\"pink\"" : "bgcolor=\"#ffffff\"");
                    if ($flg || !in_array($row["COURSECD".$i], $passCourse)) {
                        $row["EVENT".$i] = $bgcolor." ";
                    } else {
                        $row["EVENT".$i] = $bgcolor." id=\"{$id}\" onClick=\"selectCourse('{$id}')\" onMouseOver=\"changeColor('on', '{$flg}', '{$id}')\" onMouseOut=\"changeColor('off', '{$flg}', '{$id}')\" ";
                    }
                }

                //入学コーステキスト
                $extra  = "style=\"text-align: center\"; onKeyDown=\"keyChangeEntToTab(this, '{$row["RECEPTNO"]}')\"; onblur=\"checkNum(this, '{$row["RECEPTNO"]}')\"; ";
                if (!$row["SHDIV"]) {
                    $extra .= " disabled";
                }
                $row["INPUT_SHDIV"] = knjCreateTextBox($objForm, $row["INPUT_SHDIV"], "INPUT_SHDIV-".$row["RECEPTNO"], 3, 3, $extra);

                $arg["data"][] = $row;
                $dataFlg = true;
            }
            $result->free();
        }

        $disabled = ($dataFlg) ? "" : " disabled";

        //ボタン作成
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"".$disabled;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"".$disabled;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_ReceptNo));
        knjCreateHidden($objForm, "HID_LINK_RECEPTNO", implode(",", $hidenLinkRecept));

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL080A");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl080aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl080aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
