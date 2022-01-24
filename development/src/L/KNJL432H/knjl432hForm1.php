<?php

require_once('for_php7.php');

class knjl432hForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl432hindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl432hQuery::getNameMst($model->year, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試回数コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl432hQuery::getSettingMst($model, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //受験コースコンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl432hQuery::getEntExamCourse($model);
        makeCmb($objForm, $arg, $db, $query, "TOTALCD", $model->totalcd, $extra, 1, "BLANK");

        //受験型コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl432hQuery::getExamType($model);
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->exam_type, $extra, 1);

        //テキスト名
        $text_name = array("1" => "INTERVIEW_A"
                          ,"2" => "INTERVIEW_REMARK");
        $setTextField = "";
        $textSep = "";
        foreach ($text_name as $code => $col) {
            $setTextField .= $textSep.$col."-";
            $textSep = ",";
        }

        //一覧表示
        $arr_receptno = array();
        if ($model->cmd == "read" || $model->cmd == "update") {
            //データ取得
            $result = $db->query(knjl432hQuery::selectQuery($model));

            //データなし
            if ($result->numRows() == 0) {
                $model->setMessage("MSG303");
            }

            //データ表示
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"].'-'.$count.'-'.$row["EXAMNO"];

                if ($row["ROUNIN_FLG"]) {
                    $row["ROUNIN_FLG"] = "style=\"color:red\"";
                }

                //欠席者は、入力不可
                $disInput = ($row["JUDGEDIV"] == "4") ? " disabled" : "";

                //エラー時は画面の値をセット
                if (isset($model->warning)) {
                    $row["INTERVIEW_A"] = $model->interView_Value[$count];
                    $row["INTERVIEW_REMARK"] = $model->interView_Remark[$count];
                }
                //面接評定
                $extra = "style=\"text-align: center\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toAlpha(this.value);this.value=this.value.toUpperCase();\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["INTERVIEW_A"] = knjCreateTextBox($objForm, $row["INTERVIEW_A"], "INTERVIEW_A-".$count, 1, 1, $extra.$disInput);
                //面接
                $extra = " onPaste=\"return showPaste(this);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["INTERVIEW_REMARK"] = knjCreateTextBox($objForm, $row["INTERVIEW_REMARK"], "INTERVIEW_REMARK-".$count, 30, 15, $extra.$disInput);

                $arg["data"][] = $row;
                $count++;
            }
        }

        knjCreateHidden($objForm, "COUNT", $count);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl432hForm1.html", $arg);
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

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //読込ボタン
    $extra  = "onclick=\"return btn_submit('read');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV0");
    knjCreateHidden($objForm, "HID_TOTALCD");
    knjCreateHidden($objForm, "HID_EXAM_TYPE");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL432H");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
