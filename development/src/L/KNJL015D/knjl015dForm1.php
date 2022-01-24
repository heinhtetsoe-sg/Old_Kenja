<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl015dForm1
{
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl015dindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //受験種類コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl015dQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //中学校コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl015dQuery::getFinSchool($model);
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOLCD", $model->finschool_cd, $extra, 1, "");

        //テキスト名
        $text_name = array("1" => "SELECTION_REMARK"
                          ,"2" => "SELECTION_REMARK2");
        $setTextField = "";
        $textSep = "";
        foreach ($text_name as $code => $col) {
            $setTextField .= $textSep.$col."-";
            $textSep = ",";
        }

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->finschool_cd != "") {
            //データ取得
            $searchCnt = $db->getOne(knjl015dQuery::SelectQuery($model, "COUNT"));
            if ($searchCnt == 0) {
                $model->setMessage("MSG303");
            } else {
                $result = $db->query(knjl015dQuery::SelectQuery($model, ""));
                $count = 0;
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");

                    //HIDDENに保持する用
                    $arr_receptno[] = $row["EXAMNO"].'-'.$count;
                    //エラー時は画面の値をセット
                    if (isset($model->warning)) {
                        $row["REMARK1"] = $model->selection_Remark[$count];
                        $row["REMARK2"] = $model->selection_Remark2[$count];
                    }
                    //選抜
                    $extra = " onPaste=\"return showPaste(this);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $row["SELECTION_REMARK"] = knjCreateTextBox($objForm, $row["REMARK1"], "SELECTION_REMARK-".$count, 42, 33, $extra);
                    $extra = " onPaste=\"return showPaste(this);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $row["SELECTION_REMARK2"] = knjCreateTextBox($objForm, $row["REMARK2"], "SELECTION_REMARK2-".$count, 42, 33, $extra);

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
        View::toHTML($model, "knjl015dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($name == "FINSCHOOLCD") $opt[] = array("label" => "-- 全て --", "value" => "ZZZZZZZ");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
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
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //プレビュー印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'print_one');\"";
    $arg["btn_printone"] = knjCreateBtn($objForm, "btn_printone", "プレビュー／印刷", $extra);

}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_FINSCHOOLCD");
    knjCreateHidden($objForm, "PRINTTYPE");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL015D");
    
    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
