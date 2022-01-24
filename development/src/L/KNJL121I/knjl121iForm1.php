<?php
class knjl121iForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->examYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl121iQuery::getNameMst($model->examYear, 'L003', '2');
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl121iQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        // 内定区分列幅
        $colWidthNormal = "160";
        $colWidthLast = "*";

        //内定区分名称取得
        $headerJudgeKind = array();
        $arrJudgeKindCd = array();
        $result = $db->query(knjl121iQuery::getEntexamSettingMst($model, "L025"));
        $keyJK = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $headerJudgeKind[$keyJK]["TITLE"] = $row["ABBV1"]."(".$row["NAME1"].")";
            $headerJudgeKind[$keyJK]["COLWIDTH"] = $colWidthNormal;
            $arrJudgeKindCd[] = $row["SEQ"];
            $keyJK++;
        }
        $headerJudgeKind[$keyJK - 1]["COLWIDTH"] = $colWidthLast;
        $arg["headerJK"] = $headerJudgeKind;

        //一覧表示
        $arr_ExamNo = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl121iQuery::SelectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_ExamNo[] = $row["EXAMNO"];

                $keyJK = 0;
                $checkboxJudgeKind = array();
                foreach ($arrJudgeKindCd as $val) {
                    //内定区分checkbox
                    $checked1 = ($row["JUDGE_KIND"] == $val) ? " checked": "";
                    $extra = "id=\"JUDGE_KIND_{$val}-{$row["EXAMNO"]}\" onclick=\"check_check(this);\" ".$checked1;
                    $checkboxJudgeKind[$keyJK]["FORM"] = knjCreateCheckBox($objForm, "JUDGE_KIND_{$val}-".$row["EXAMNO"], $val, $extra);
                    $checkboxJudgeKind[$keyJK]["COLWIDTH"] = $colWidthNormal;
                    $keyJK++;
                }
                $checkboxJudgeKind[$keyJK - 1]["COLWIDTH"] = $colWidthLast;
                $row["checkboxJK"] = $checkboxJudgeKind;

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_ExamNo));
        knjCreateHidden($objForm, "HID_JUDGEKINDCD", implode(",",$arrJudgeKindCd));

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL121I");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl121iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl121iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $value_flg = ($blank == "ALL") ? true : false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($blank == "ALL") $opt[] = array("label" => "全て", "value" => "ALL");

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
