<?php
class knjl671aForm1
{
    function main(&$model){

        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl671aForm1", "POST", "knjl671aindex.php", "", "knjl671aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["top"]["YEAR"] = $model->objYear;

        //入試制度(校種)
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl671aQuery::getNameMst($model->objYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["VALUE"].":".$row["LABEL"];
            $opt[] = array('label' => $label, 'value' => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["APPLICANTDIV"] = $row["VALUE"];
            if ($model->field["APPLICANTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('edit');\"";
        $arg["top"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //入試区分
        $opt = array();
        $value_flg = false;
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $result = $db->query(knjl671aQuery::getNameMst($model->objYear, $namecd1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["VALUE"].":".$row["LABEL"];
            $opt[] = array('label' => $label, 'value' => $row["VALUE"]);
            if ($model->field["TESTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] && $value_flg) ? $model->field["TESTDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('edit');\"";
        $arg["top"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //合格コース
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl671aQuery::getEntexamCourseMst($model->objYear, $model->field["APPLICANTDIV"], $model->field["TESTDIV"]));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            if ($model->field["EXAMCOURSECD"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["EXAMCOURSECD"] = ($model->field["EXAMCOURSECD"] && $value_flg) ? $model->field["EXAMCOURSECD"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('edit');\"";
        $arg["top"]["EXAMCOURSECD"] = knjCreateCombo($objForm, "EXAMCOURSECD", $model->field["EXAMCOURSECD"], $opt, $extra, 1);

        //ソート表示文字作成
        $model->sortColumn = $model->sortColumn ? $model->sortColumn : "0";
        $model->sortOrder = $model->sortOrder ? $model->sortOrder : "0";
        if ($model->cmd == "sort") {
            if ($model->sortOrder == "0") {
                $model->sortOrder = "1";
            } else if ($model->sortOrder == "1"){
                $model->sortOrder = "2";
            } else if ($model->sortOrder == "2") {
                $model->sortOrder = "0";
                $model->sortColumn = "0";
            }
        }
        $order = array("", "▼", "▲");
        // 受験番号ソート
        if ($model->sortColumn == "0") {
            $arg["SORT"] = $order[$model->sortOrder];
        }

        //入試区分(列用項目名取得)
        $testDivList = array();
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $result = $db->query(knjl671aQuery::getNameMst($model->objYear, $namecd1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['TESTDIV_COLUMN_NAME'] = "TESTDIV_".$row['VALUE'];
            $setOrder = $model->sortColumn == $row['VALUE'] ? $order[$model->sortOrder] : "";
            $row['TESTDIV_NAME'] = "<a href=\"javascript:postSort('{$row['VALUE']}');\" STYLE=\"color:white\">{$row['LABEL']}{$setOrder}</a>";
            // $testDivList[$row['VALUE']] = $row;
            $testDivList[] = $row;
        }
        $arg["data2"]["TESTDIV"] = $testDivList;

        // 特別奨学生マスタ取得
        $spScholarshipMst = array();
        $result = $db->query(knjl671aQuery::getEntexamSpScholarshipMst($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $spScholarshipMst[$row['TESTDIV']][] = $row;
        }

        //データ取得
        if ($model->cmd == 'edit' || $model->cmd == 'sort' || $model->cmd == 'clear' || $model->cmd == '') {
            $receptList = array();
            if ($model->isWarning()) {
                for ($i=0; $i < count($model->line["EXAMNO"]); $i++) { 
                    $receptList[$model->line["EXAMNO"][$i]]["SP_SCHOLAR_CD"] = $model->line["SP_SCHOLAR_CD"][$i];
                }
            }

            $query  = knjl671aQuery::selectQuery($model, $testDivList);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // 入力チェックでエラーがあった場合は、更新を押下した時点の値を出力する
                if ($receptList[$row["EXAMNO"]]) {
                    $row["SP_SCHOLAR_CD"] = $receptList[$row["EXAMNO"]]["SP_SCHOLAR_CD"];
                }
                // 点数を配列へ変換
                $total4List = array();
                for ($i=0; $i < count($testDivList); $i++) { 
                    $testDiv = $testDivList[$i];
                    $total4List[]['TOTAL4'] = $row[$testDiv['TESTDIV_COLUMN_NAME']];
                    if ($row['TESTDIV'] == $testDiv['VALUE']) {
                        // 合格回
                        $row['TESTDIV_NAME'] = $testDiv['LABEL'];
                    }
                }
                $row['TESTDIV_LIST'] = $total4List;

                // 特別奨学生コード(コンボ作成)
                $opt = array();
                $opt[] = array('label' => "", 'value' => "");
                $value_flg = false;

                if ($spScholarshipMst[$model->field["TESTDIV"]]) {
                    for ($i=0; $i < count($spScholarshipMst[$model->field["TESTDIV"]]); $i++) {
                        $scholarship = $spScholarshipMst[$model->field["TESTDIV"]][$i];

                        $opt[] = array('label' => $scholarship["SP_SCHOLAR_CD"].":".$scholarship["SP_SCHOLAR_NAME"], 'value' => $scholarship["SP_SCHOLAR_CD"]);
                        if ($row["SP_SCHOLAR_CD"] == $scholarship["SP_SCHOLAR_CD"]) {
                            $value_flg = true;
                        }
                    }
                }
                $row["SP_SCHOLAR_CD"] = ($row["SP_SCHOLAR_CD"] && $value_flg) ? $row["SP_SCHOLAR_CD"] : $opt[0]["value"];
                $extra = "";
                $row["SP_SCHOLAR_CD_SEL"] = knjCreateCombo($objForm, "SP_SCHOLAR_CD[]", $row["SP_SCHOLAR_CD"], $opt, $extra, 1);

                $arg["data"][] = $row;
            }
            $result->free();

            //データが無ければ更新ボタン等を無効
            if (!is_array($arg["data"])) {
                $model->setWarning("MSG303");
            }
        }

        //更新ボタン
        $disable  = (is_array($arg["data"])) ? "" : " disabled";
        $extra = " onclick=\"return btn_submit('update');\"".$disable;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = " onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタン
        $extra = " onclick=\"return btn_submit('close');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->objYear);
        knjCreateHidden($objForm, "SORT_COLUMN");
        knjCreateHidden($objForm, "SORT_ORDER", $model->sortOrder);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl671aForm1.html", $arg); 

    }
}
?>
