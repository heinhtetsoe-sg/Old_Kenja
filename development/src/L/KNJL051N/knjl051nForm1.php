<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl051nForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl051nindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl051nQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl051nQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //志望区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl051nQuery::getEntExamCourse($model);
        makeCmb($objForm, $arg, $db, $query, "TOTALCD", $model->totalcd, $extra, 1, "BLANK");

        $extra = "onClick=\"btn_submit('read');\" tabindex=-1";
        $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読込み", $extra);
        $extra = "onClick=\"btn_submit('back');\" tabindex=-1";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        $extra = "onClick=\"btn_submit('next');\" tabindex=-1";
        $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //テキスト名
        $text_name = array("1" => "INTERVIEW_VALUE"
                          ,"2" => "INTERVIEW_REMARK"
                          ,"3" => "INTERVIEW_VALUE2"
                          ,"4" => "INTERVIEW_REMARK2");
        $setTextField = "";
        $textSep = "";
        foreach ($text_name as $code => $col) {
            $setTextField .= $textSep.$col."-";
            $textSep = ",";
        }

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->totalcd != "") {
            //データ取得
            $searchCnt = $db->getOne(knjl051nQuery::selectQuery($model, "COUNT"));
            $checkCnt = 50;
            if ($searchCnt == 0) {
                $model->setMessage("MSG303");
            } else {
                $result = $db->query(knjl051nQuery::selectQuery($model, ""));
                $count = 0;
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");

                    //HIDDENに保持する用
                    $arr_receptno[] = $row["RECEPTNO"].'-'.$count;
                    //エラー時は画面の値をセット
                    if (isset($model->warning)) {
                        $row["INTERVIEW_VALUE"] = $model->interView_Value[$count];
                        $row["INTERVIEW_REMARK"] = $model->interView_Remark[$count];
                        $row["INTERVIEW_VALUE2"] = $model->interView_Value2[$count];
                        $row["INTERVIEW_REMARK2"] = $model->interView_Remark2[$count];
                    }
                    //面接
                    $extra = " onPaste=\"return showPaste(this);\" onblur=\"this.value=toInterViewAlpha(this.value);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $row["INTERVIEW_VALUE"] = knjCreateTextBox($objForm, $row["INTERVIEW_VALUE"], "INTERVIEW_VALUE-".$count, 1, 1, $extra);
                    $extra = " onPaste=\"return showPaste(this);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $row["INTERVIEW_REMARK"] = knjCreateTextBox($objForm, $row["INTERVIEW_REMARK"], "INTERVIEW_REMARK-".$count, 30, 30, $extra);
                    $extra = " onPaste=\"return showPaste(this);\" onblur=\"this.value=toInterViewAlpha(this.value);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $row["INTERVIEW_VALUE2"] = knjCreateTextBox($objForm, $row["INTERVIEW_VALUE2"], "INTERVIEW_VALUE2-".$count, 1, 1, $extra);
                    $extra = " onPaste=\"return showPaste(this);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $row["INTERVIEW_REMARK2"] = knjCreateTextBox($objForm, $row["INTERVIEW_REMARK2"], "INTERVIEW_REMARK2-".$count, 30, 30, $extra);

                    $arg["data"][] = $row;
                    $count++;
                }
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
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl051nForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
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
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //ＣＳＶ出力ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_TOTALCD");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL051N");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
