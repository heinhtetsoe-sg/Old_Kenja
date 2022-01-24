<?php

require_once('for_php7.php');

class knjl051dForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //事前チェック（評価）
        $valArray = $db->getCol(knjl051dQuery::getNameMst($model->ObjYear, "L027"));
        if (get_count($valArray) == 0) {
            $arg["val_check"] = "errorPreCheck();";
        }
        knjCreateHidden($objForm, "VAL_LIST", implode(',', $valArray));

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //受験種別（入試区分）
        $query = knjl051dQuery::getNameMst($model->ObjYear, "L004");
        $extra = "onChange=\"return btn_submit('read')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //会場コンボボックス
        $extra = " onChange=\"return btn_submit('read');\"";
        $query = knjl051dQuery::getHallData($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1);

        //初期化
        if ($model->cmd == "main") {
            $model->s_examno = "";
            $model->e_examno = "";
        }

        //一覧表示
        $arr_examno = array();
        $s_examno = $e_examno = "";
        $examno = array();
        $arryignoreExam = array();
        $dataflg = false;
        if ($model->testdiv != "" && $model->examhallcd != "") {
            //データ取得
            $query = knjl051dQuery::selectQuery($model, "list");
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
                $model->e_examno = "";
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["EXAMNO"], "perf" => 100);

                //得点テキストボックス
                $value = ($model->isWarning()) ? $model->interview[$row["EXAMNO"]] : $row["INTERVIEW_VALUE"];
                $extra  = (strlen($row["ATTEND"])) ? "disabled" : "";
                $extra .= " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"CheckValue(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $row["INTERVIEW_VALUE"] = knjCreateTextBox($objForm, $value, "INTERVIEW_VALUE_".$row["EXAMNO"], 3, 1, $extra);
                knjCreateHidden($objForm, "HID_INTERVIEW_VALUE_".$row["EXAMNO"], $value);
                if (strlen($row["ATTEND"]) > 0) {
                    $arryignoreExam[] = $row["EXAMNO"];
                }

                //開始・終了受験番号
                if ($s_examno == "") {
                    $s_examno = $row["EXAMNO"];
                }
                $e_examno = $row["EXAMNO"];
                $dataflg = true;

                $arg["data"][] = $row;
            }

            //受験番号の最大値・最小値取得
            $exam_array = $db->getCol(knjl051dQuery::selectQuery($model, "examno"));
            $examno["min"] = $exam_array[0];
            $examno["max"] = end($exam_array);

            //初期化
            if (in_array($model->cmd, array("next", "back")) && $dataflg) {
                $model->e_examno = "";
                $model->s_examno = "";
            }
        }
        knjCreateHidden($objForm, "HID_IGNORE_EXAMLIST", implode(",", $arryignoreExam));

        //開始受験番号
        if ($s_examno) {
            $model->s_examno = $s_examno;
        }
        $arg["TOP"]["S_EXAMNO"] = $model->s_examno;
        knjCreateHidden($objForm, "S_EXAMNO", $model->s_examno);

        //終了受験番号
        if ($e_examno) {
            $model->e_examno = $e_examno;
        }
        $arg["TOP"]["E_EXAMNO"] = $model->e_examno;
        knjCreateHidden($objForm, "E_EXAMNO", $model->e_examno);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $valArray, $dataflg, $examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl051dindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl051dForm1.html", $arg);
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
function makeBtn(&$objForm, &$arg, $model, $valArray, $dataflg, $examno)
{
    //読込ボタン（前の受験番号検索）
    $extra  = "style=\"width:70px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('back');\"";
    $extra .= ($examno["min"] != $model->s_examno) ? "" : " disabled";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", " 前画面へ ", $extra);
    //読込ボタン（後の受験番号検索）
    $extra  = "style=\"width:70px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('next');\"";
    $extra .= ($examno["max"] != $model->e_examno) ? "" : " disabled";
    $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", " 次画面へ ", $extra);

    $disable  = ($dataflg &&get_count($valArray) > 0) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //CSV出力ボタン
    $extra = "onclick=\"return btn_submit('csv');\"".$disable;
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "EXAM_TYPE", $model->exam_type);
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL051D");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
}
