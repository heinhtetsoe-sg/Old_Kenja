<?php

require_once('for_php7.php');

class knjl052fForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl052findex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl052fQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //レイアウト切替
        if ($model->applicantdiv == "1") {
            $arg["applicantdiv1"] = 1;
        } else {
            $arg["applicantdiv2"] = 1;
        }

        //ヘッダー(出身学校)
        $arg["TOP"]["HEADER_FINSCHOOL"] = ($model->applicantdiv == "1") ? "小学校名" : "中学校名";

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        if ($model->applicantdiv == "1") {
            $query = knjl052fQuery::getTestdivL024($model);
        } else {
            $query = knjl052fQuery::getNameMst($model->ObjYear, "L004");
        }
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //高校のみ
        if ($model->applicantdiv == "2") {
            //入試回数コンボボックス
            $query = knjl052fQuery::getTestdiv0($model->ObjYear, $model->testdiv);
            $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
            makeCmb($objForm, $arg, $db, $query, "TESTDIV0", $model->testdiv0, $extra, 1, "BLANK");

            //志望区分コンボボックス
            $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
            $query = knjl052fQuery::getEntExamCourse($model);
            makeCmb($objForm, $arg, $db, $query, "TOTALCD", $model->totalcd, $extra, 1, "BLANK");
        }

        //中学
        if ($model->applicantdiv == "1") {
            //受験型コンボ「6:グローバル」「7:サイエンス」「8:スポーツ」
            $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
            $query = knjl052fQuery::getExamType($model);
            makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->exam_type, $extra, 1, "BLANK");
        }

        //特別措置者(インフルエンザ)
        $extra = "id=\"SPECIAL_REASON_DIV\" onchange=\"return btn_submit('main');\" tabindex=-1 ";
        $extra .= strlen($model->special_reason_div) ? "checked='checked' " : "";
        $arg["TOP"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //テキスト名
        $text_name = array("1" => "INTERVIEW_VALUE"
                          ,"2" => "INTERVIEW_REMARK");
        $setTextField = "";
        $textSep = "";
        foreach ($text_name as $code => $col) {
            $setTextField .= $textSep.$col."-";
            $textSep = ",";
        }

        //一覧表示フラグ
        if ($model->applicantdiv == "1") {
            $listFlg = $model->exam_type != "";
        } else {
            $listFlg = $model->testdiv0 != "" && $model->totalcd != "";
        }

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $listFlg) {
            //データ取得
            $result = $db->query(knjl052fQuery::SelectQuery($model));
            $count = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"].'-'.$count.'-'.$row["EXAMNO"];
                //エラー時は画面の値をセット
                if (isset($model->warning)) {
                    $row["INTERVIEW_VALUE"] = $model->interView_Value[$count];
                    $row["INTERVIEW_REMARK"] = $model->interView_Remark[$count];
                }
                //面接評定
                $extra = "style=\"text-align: center\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toInterViewInteger(this.value);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["INTERVIEW_VALUE"] = knjCreateTextBox($objForm, $row["INTERVIEW_VALUE"], "INTERVIEW_VALUE-".$count, 2, 1, $extra);
                //面接
                $extra = " onPaste=\"return showPaste(this);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["INTERVIEW_REMARK"] = knjCreateTextBox($objForm, $row["INTERVIEW_REMARK"], "INTERVIEW_REMARK-".$count, 30, 30, $extra);

                $arg["data"][] = $row;
                $count++;
            }

            if ($count == 0) {
                $model->setMessage("MSG303");
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
        View::toHTML($model, "knjl052fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
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
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV0");
    knjCreateHidden($objForm, "HID_TOTALCD");
    knjCreateHidden($objForm, "HID_EXAM_TYPE");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL052F");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
