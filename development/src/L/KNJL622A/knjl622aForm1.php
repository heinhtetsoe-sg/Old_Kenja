<?php
class knjl622aForm1
{
    function main(&$model){

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["top"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl622aQuery::getNameMst($model->ObjYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
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
        $result = $db->query(knjl622aQuery::getNameMst($model->ObjYear, $namecd1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["TESTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["TESTDIV"] = $row["VALUE"];
            if ($model->field["TESTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] && $value_flg) ? $model->field["TESTDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('edit');\"";
        $arg["top"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //受験番号
        $extra = "";
        $arg["top"]["EXAMNO_FROM"] = knjCreateTextBox($objForm, $model->field["EXAMNO_FROM"], "EXAMNO_FROM", 4, 4, $extra);
        $arg["top"]["EXAMNO_TO"]   = knjCreateTextBox($objForm, $model->field["EXAMNO_TO"], "EXAMNO_TO", 4, 4, $extra);

        //会場番号
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        $result = $db->query(knjl622aQuery::getHall($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["EXAMHALLCD"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $model->field["EXAMHALLCD"] = ($model->field["EXAMHALLCD"] && $value_flg) ? $model->field["EXAMHALLCD"] : $opt[0]["value"];
        $extra = "";
        $arg["top"]["EXAMHALLCD"] = knjCreateCombo($objForm, "EXAMHALLCD", $model->field["EXAMHALLCD"], $opt, $extra, 1);

        //コース
        $course_opt = array();
        $course_opt[] = array('label' => '', 'value' => '');
        $result = $db->query(knjl622aQuery::getCourse($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $course_opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        //選抜方式
        $senbatu_opt = array();
        $result = $db->query(knjl622aQuery::getSenbatu());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $senbatu_opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        
        if ($model->field["APPLICANTDIV"] == "2") {
            $arg['APPLICANTDIV2']=true;
        }
        //データ取得
        if ($model->cmd == 'clear' || $model->cmd == 'search') {
            $receptList = array();
            if ($model->isWarning()) {
                for ($i=0; $i < count($model->line["RECEPTNO"]); $i++) { 
                    $receptList[$model->line["RECEPTNO"][$i]]["HALLSEATCD"] = $model->line["HALLSEATCD"][$i];
                    $receptList[$model->line["RECEPTNO"][$i]]["RESERVE1"]   = $model->line["RESERVE1"][$i];
                    $receptList[$model->line["RECEPTNO"][$i]]["RESERVE2"]   = $model->line["RESERVE2"][$i];
                    $receptList[$model->line["RECEPTNO"][$i]]["RESERVE3"]   = $model->line["RESERVE3"][$i];
                    $receptList[$model->line["RECEPTNO"][$i]]["RESERVE4"]   = $model->line["RESERVE4"][$i];
                    $receptList[$model->line["RECEPTNO"][$i]]["RESERVE5"]   = $model->line["RESERVE5"][$i];
                }
            }

            $query  = knjl622aQuery::selectQuery($model);
            $result = $db->query($query);
            while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // 入力チェックでエラーがあった場合は、更新を押下した時点の値を出力する
                if ($receptList[$row["RECEPTNO"]]) {
                    $row["HALLSEATCD"] = $receptList[$row["RECEPTNO"]]["HALLSEATCD"];
                    $row["RESERVE1"]   = $receptList[$row["RECEPTNO"]]["RESERVE1"];
                    $row["RESERVE2"]   = $receptList[$row["RECEPTNO"]]["RESERVE2"];
                    $row["RESERVE3"]   = $receptList[$row["RECEPTNO"]]["RESERVE3"];
                    $row["RESERVE4"]   = $receptList[$row["RECEPTNO"]]["RESERVE4"];
                    $row["RESERVE5"]   = $receptList[$row["RECEPTNO"]]["RESERVE5"];
                }

                // 志望コース
                $row["COURSE1"] = knjCreateCombo($objForm, "COURSE1[]", $row["COURSE1"], $course_opt, "", 1);
                $row["COURSE2"] = knjCreateCombo($objForm, "COURSE2[]", $row["COURSE2"], $course_opt, "", 1);
                
                if ($model->field["APPLICANTDIV"] == "2") {
                    // 選抜方式
                    $row["SENBATU"] = knjCreateCombo($objForm, "SENBATU[]", $row["SENBATU"], $senbatu_opt, "", 1);
                }
                // 座席番号
                $extra = " onblur=\"return checkHallSeat(this);\" ";
                $row["HALLSEATCD"] = knjCreateTextBox($objForm, $row["HALLSEATCD"], "HALLSEATCD[]", 4, 4, $extra);
                // 面接番号 予備２～５
                $extra = "";
                $row["RESERVE1"] = knjCreateTextBox($objForm, $row["RESERVE1"], "RESERVE1[]", 4, 4, $extra);
                $row["RESERVE2"] = knjCreateTextBox($objForm, $row["RESERVE2"], "RESERVE2[]", 4, 4, $extra);
                $row["RESERVE3"] = knjCreateTextBox($objForm, $row["RESERVE3"], "RESERVE3[]", 4, 4, $extra);
                $row["RESERVE4"] = knjCreateTextBox($objForm, $row["RESERVE4"], "RESERVE4[]", 4, 4, $extra);
                $row["RESERVE5"] = knjCreateTextBox($objForm, $row["RESERVE5"], "RESERVE5[]", 4, 4, $extra);

                $arg["data"][] = $row;
            }
            $result->free();

            //データが無ければ更新ボタン等を無効
            if (!is_array($arg["data"])) {
                $model->setWarning("MSG303");
            }
        }

        //検索ボタン
        $extra = " onclick=\"return btn_submit('search');\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

        //更新ボタン
        $disable  = (is_array($arg["data"])) ? "" : " disabled";
        $extra = " onclick=\"return btn_submit('update');\"".$disable;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = " onclick=\"return btn_submit('clear');\"".$disable;
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタン
        $extra = " onclick=\"return btn_submit('close');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        
        
        /**************/
        /* ＣＳＶ作成 */
        /**************/
        //ファイル
        $extra = "".$disable;
        $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);
        //取込ボタン
        $extra = "onclick=\"return btn_submit('csvInput');\"".$disable;
        $arg["csv"]["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra);
        //出力ボタン
        $extra = "onclick=\"return btn_submit('csvOutput');\"".$disable;
        $arg["csv"]["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra);
        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = " checked";
        } else {
            $check_header = ($model->cmd == "") ? " checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl622aForm1", "POST", "knjl622aindex.php", "", "knjl622aForm1");

        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl622aForm1.html", $arg); 

    }
}
?>
