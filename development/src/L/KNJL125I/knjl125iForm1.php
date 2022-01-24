<?php
class knjl125iForm1
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->examyear;
        knjCreateHidden($objForm, "YEAR", $model->examyear);

        //入試制度
        $extra = "onChange=\"return btn_submit('main')\"";
        $query = knjl125iQuery::getNameMst($model->examyear, "L003", $model->applicantdiv);
        $arg["TOP"]["APPLICANTDIV"] = makeCmb($objForm, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分
        $query = knjl125iQuery::getTestDiv($model);
        $extra = "onchange=\"return btn_submit('main');\" ";
        $arg["TOP"]["TESTDIV"] = makeCmb($objForm, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "blank");

        //表示順 (1:成績順 2:受験番号順)
        $opt = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $addExtra = " onchange=\"return btn_submit('main');\" ";
        $extra = array("id=\"SORT1\" {$addExtra}", "id=\"SORT2\" {$addExtra}");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //抽出 (1:全員, 2:男子, 3:女子)
        $opt = array(1, 2, 3);
        $model->field["DISP_DIV"] = ($model->field["DISP_DIV"] == "") ? "1" : $model->field["DISP_DIV"];
        $addExtra = " onchange=\"return btn_submit('main');\" ";
        $extra = array("id=\"DISP_DIV1\" {$addExtra}", "id=\"DISP_DIV2\" {$addExtra}", "id=\"DISP_DIV3\" {$addExtra}");
        $radioArray = knjCreateRadio($objForm, "DISP_DIV", $model->field["DISP_DIV"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //合否コンボ
        $query = knjl125iQuery::getEntexamSettingMstL013($model);
        $extra = "onchange=\"return btn_submit('judge');\"";
        $arg["JUDGEDIV"] = makeCmb($objForm, $db, $query, "JUDGEDIV", $model->field["JUDGEDIV"], $extra, $size, "blank");

        if ($model->applicantdiv != "" && $model->field["TESTDIV"] != "") {
            //一覧表示
            $query = knjl125iQuery::selectQuery($model);
            $result = $db->query($query);
            //取得件数の判定
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            } else {
                //更新チェックボックス(ヘッダ)
                if ($model->cmd == "main" || $model->cmd == "reset") {
                    $checkdAll = "";
                    $model->checkAll = "";
                    $model->checkVal     = array();
                }
                $checkdAll = ($model->checkAll) ? " checked " : "";
                $extra = " id=\"CHECKALL\" onchange=\"checkAll(this);\" ".$checkdAll;
                $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "1", $extra);
            }
            $model->recept_arr   = array();
            $model->receptExamno = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $chkFlg = ($model->checkAll || $model->checkVal[$row["RECEPTNO"]]) ? true : false;
                $checked = ($chkFlg) ? " checked " : "";
                $bgcolor = ($chkFlg) ? "#ffff00" : "#ffffff";
                $row["BGCOLOR"] = "style=\"background-color:{$bgcolor}\"";

                //チェックボックス
                $extra = " class=\"check-elems\" onchange=\"changeColor(this);\" ";
                $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK_".$row["RECEPTNO"], $row["RECEPTNO"], $checked.$extra);

                $arg["data"][] = $row;
                $model->recept_arr[] = $row["RECEPTNO"];
                $model->receptExamno[$row["RECEPTNO"]] = $row["EXAMNO"];
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl125iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjl125iForm1.html", $arg, "main5_JqueryOnly.html");
    }
}

//コンボ作成
function makeCmb(&$objForm, $db, $query, $name, &$value, $extra, $size, $blank = "")
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
        $i++;
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $disabled = ($model->field["TESTDIV"] != "" && $model->field["JUDGEDIV"] != "") ? "" : " disabled ";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disabled;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}
