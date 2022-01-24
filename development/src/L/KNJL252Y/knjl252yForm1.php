<?php

require_once('for_php7.php');


class knjl252yForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db           = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl252yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $query = knjl252yQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //グループコンボボックス
        $query = knjl252yQuery::getHallName($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "BLANK");

        //一覧表示
        $model->data = array();
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->examhallcd != "") {
            $counter = 0;
            //データ取得
            $result    = $db->query(knjl252yQuery::SelectQuery($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                //HIDDENに保持する用
                $arr_receptno[] = $row["EXAMNO"];
                //受験番号を配列で取得
                $model->data["EXAMNO"][] = $row["EXAMNO"];
                //対象データをセット
                $model->fields["INTERVIEW_VALUE2"][$counter] = $row["INTERVIEW_VALUE2"];
                $model->fields["INTERVIEW_REMARK2"][$counter] = $row["INTERVIEW_REMARK2"];
                //評価
                $opt = array();
                $opt[] = array('label' => "", 'value' => "");
                $query = knjl252yQuery::getHyouteiData();
                $resultCombo = $db->query($query);
                while ($comboRow = $resultCombo->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array('label' => $comboRow["LABEL"],
                                   'value' => $comboRow["VALUE"]);
                }
                $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\"";
                $row["INTERVIEW_VALUE2"] = knjCreateCombo($objForm, "INTERVIEW_VALUE2-".$counter, $model->fields["INTERVIEW_VALUE2"][$counter], $opt, $extra, 1);
                //行動の観察
                $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\"";
                $row["INTERVIEW_REMARK2"] = knjCreateTextBox($objForm, $model->fields["INTERVIEW_REMARK2"][$counter], "INTERVIEW_REMARK2-".$counter, 100, 150, $extra);
                
                $arg["data"][] = $row;
                $counter++;
            }
        }
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl252yindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl252yForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
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

    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL252Y");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
