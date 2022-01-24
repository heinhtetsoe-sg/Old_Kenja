<?php
class knjl051oForm1
{
    public function main(&$model)
    {
        $objForm = new form;
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl051oQuery::getNameMst($model->ObjYear, "L003");
        $extra = "onchange=\"btn_submit('main');\" tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試区分
        $query = knjl051oQuery::getTestdivMst($model->ObjYear);
        $extra = "onchange=\"btn_submit('main');\" tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //受験型
        $query = knjl051oQuery::getNameMst($model->ObjYear, "L005", "2");
        $extra = "onchange=\"btn_submit('main');\" tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->exam_type, $extra, 1, "");

        //受験科目
        $query = knjl051oQuery::getTestSubclasscd($model);
        $extra = "onchange=\"btn_submit('main');\" tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "");

        //会場
        $query = knjl051oQuery::getHallDat($model);
        $extra = "onchange=\"btn_submit('main');\" tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "BLANK");

        //一覧表示
        $arr_receptno = array();
        if (($model->cmd == "read" || $model->cmd == "main" || $model->cmd == "csvInputMain") && $model->examhallcd != "") {
            //データ取得
            $result = $db->query(knjl051oQuery::selectQuery($model));

            if ($result->numRows() == 0) {
                $model->setMessage("MSG303", "\\n座席番号登録が行われていないか、志願者数確定処理が行われていません。");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                 array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                $extra = "onchange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\" onkeydown=\"keyChangeEntToTab(this);\"";
                $row["SCORE"] = knjCreateTextBox($objForm, $row["SCORE"], "SCORE[]", 3, 3, $extra, "1");

                $arg["data"][] = $row;
            }
        }

        Query::dbCheckIn($db);

        $extra = "onclick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onclick=\"btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**************/
        /* ＣＳＶ作成 */
        /**************/
        //ファイル
        $extra = "";
        $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);
        //実行
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["csv"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = " checked";
        } else {
            $check_header = ($model->cmd == "main") ? " checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);
        //CSV取込書出種別ラジオボタン 1:取込 2:書出
        $opt_shubetsu = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["csv"][$key] = $val;
        }

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_EXAM_TYPE");
        knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
        knjCreateHidden($objForm, "HID_EXAMHALLCD");

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl051oindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl051oForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
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
