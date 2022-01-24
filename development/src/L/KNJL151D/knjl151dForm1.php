<?php

require_once('for_php7.php');

class knjl151dForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $opt = array();
        $opt[] = array('value' => CTRL_YEAR,     'label' => CTRL_YEAR);
        $opt[] = array('value' => CTRL_YEAR + 1, 'label' => CTRL_YEAR + 1);
        $model->ObjYear = ($model->ObjYear == "") ? substr(CTRL_DATE, 0, 4): $model->ObjYear;
        $extra = "onChange=\" return btn_submit('read');\"";
        $arg["TOP"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->ObjYear, $opt, $extra, 1);

        //事前チェック（評価）
        $valArray = $db->getCol(knjl151dQuery::getNameMst($model->ObjYear, "L027"));

        $evalcodestr = array();
        if (get_count($valArray) == 0) {
            $arg["val_check"] = "errorPreCheck();";
        } else {
            foreach ($valArray as $wkkey => $wkval) {
                $evalcodestr[] = $wkval;
            }
        }
        knjCreateHidden($objForm, "VAL_LIST", implode(',', $valArray));

        //志望区分（入試区分）
        $maxTestDiv = $db->getOne(knjl151dQuery::getMaxTestDiv($model));
        $query = knjl151dQuery::getTestDivList($model->ObjYear);
        $extra = "onChange=\"return btn_submit('read')\"";
        $model->testdiv = ($model->cmd == "main") ? $maxTestDiv: $model->testdiv;
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //評価項目
        $query = knjl151dQuery::getNameMst($model->ObjYear, "L009");
        $extra = "onChange=\"return btn_submit('read')\"";
        makeCmb($objForm, $arg, $db, $query, "EVALTYPE", $model->evaltype, $extra, 1);

        if ($model->evaltype == "1") {
            //面接
            $arg["TOP"]["EVALCODETITLE"]  = "面接評価コード";
            $arg["TOP"]["EVALCODESTR"]  = implode("　", $evalcodestr);
            $arg["TOP"]["VALUE_TITLE"]  = "面接評価";
            $arg["TOP"]["REMARK_TITLE"] = "面接所見";
        } else {
            //小論文
            $arg["TOP"]["EVALCODETITLE"]  = "小論文評価値";
            $arg["TOP"]["EVALCODESTR"]  = "0～100";
            $arg["TOP"]["VALUE_TITLE"]  = "小論文評価";
            $arg["TOP"]["REMARK_TITLE"] = "小論文テーマ";
        }

        //初期化
        if ($model->cmd == "main") {
            $model->s_examno = "";
            $model->e_examno = "";
        }

        //一覧表示
        $arr_examno = array();
        $s_examno = $e_examno = "";
        $examno = array();
        $dataflg = false;
        if ($model->testdiv != "") {
            //データ取得
            $query = knjl151dQuery::selectQuery($model, "list");

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

                if ($model->evaltype == "1") {
                    //面接評価テキストボックス
                    $value = $row["INTERVIEW_VALUE"];
                    $extra = " OnChange=\"Setflg(this);\" id=\"INTERVIEW_VAL_".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"CheckValue(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                    $row["INTERVIEW_VAL"] = knjCreateTextBox($objForm, $value, "INTERVIEW_VAL_".$row["EXAMNO"], 3, 1, $extra);
                    //面接所見テキストボックス
                    $value = $row["INTERVIEW_REMARK"];
                    $extra = " OnChange=\"Setflg(this);\" id=\"INTERVIEW_RMK_".$row["EXAMNO"]."\" style=\"text-align:left;\" onblur=\"CheckEvalRemark(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                    $row["INTERVIEW_RMK"] = knjCreateTextBox($objForm, $value, "INTERVIEW_RMK_".$row["EXAMNO"], 101, 100, $extra);
                } else {
                    //小論文評価テキストボックス
                    $value = $row["INTERVIEW_VALUE2"];
                    $extra = " OnChange=\"Setflg(this);\" id=\"INTERVIEW_VAL_".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"CheckValue(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                    $row["INTERVIEW_VAL"] = knjCreateTextBox($objForm, $value, "INTERVIEW_VAL_".$row["EXAMNO"], 3, 3, $extra);
                    //小論文テーマテキストボックス
                    $value = $row["INTERVIEW_REMARK2"];
                    $extra = " OnChange=\"Setflg(this);\" id=\"INTERVIEW_RMK_".$row["EXAMNO"]."\" style=\"text-align:left;\" onblur=\"CheckEvalRemark(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                    $row["INTERVIEW_RMK"] = knjCreateTextBox($objForm, $value, "INTERVIEW_RMK_".$row["EXAMNO"], 101, 30, $extra);
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
            $exam_array = $db->getCol(knjl151dQuery::selectQuery($model, "examno"));
            $examno["min"] = $exam_array[0];
            $examno["max"] = end($exam_array);

            //初期化
            if (in_array($model->cmd, array("next", "back")) && $dataflg) {
                $model->e_examno = "";
                $model->s_examno = "";
            }
        }

        //開始受験番号
        if ($s_examno) {
            $model->s_examno = $s_examno;
        }
        $extra="";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->s_examno, "S_EXAMNO", 5, 5, $extra);

        //終了受験番号
        if ($e_examno) {
            $model->e_examno = $e_examno;
        }
        $extra="";
        $arg["TOP"]["E_EXAMNO"] = knjCreateTextBox($objForm, $model->e_examno, "E_EXAMNO", 5, 5, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $valArray, $dataflg, $examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno, getEvalCode($db, $model));

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl151dindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl151dForm1.html", $arg);
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
    //読込ボタン
    $extra  = "style=\"width:64px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('read2');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", " 読込み ", $extra);
    //読込ボタン（前の受験番号検索）
    $extra  = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('back');\"";
    $extra .= ($examno["min"] != $model->s_examno) ? "" : " disabled";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
    //読込ボタン（後の受験番号検索）
    $extra  = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('next');\"";
    $extra .= ($examno["max"] != $model->e_examno) ? "" : " disabled";
    $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

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

//面接/評価コード取得
function getEvalCode($db, $model)
{
    $retarr = array();
    $result = $db->query(knjl151dQuery::getNameMst($model->ObjYear, "L027"));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $retarr[] = $row["VALUE"];
    }
    return $retarr;
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno, $evalcdarry)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "EXAM_TYPE", $model->exam_type);
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EVALTYPE");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL151D");
    knjCreateHidden($objForm, "HID_EVALCD_INTERVIEW", implode(",", $evalcdarry));
}
