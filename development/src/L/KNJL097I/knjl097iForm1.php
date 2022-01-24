<?php

class knjl097iForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] == "") ? "1" : $model->field["APPLICANTDIV"];
        $extra = "onchange=\"return btn_submit('main');\" ";
        $query = knjl097iQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" ";
        $query = knjl097iQuery::getTestDiv($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "BLANK");

        //表示順ラジオボタン 1:成績順 2:受験番号順
        $opt_sort = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $extra = array("id=\"SORT1\" onclick=\"btn_submit('main');\"", "id=\"SORT2\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt_sort, count($opt_sort));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //抽出ラジオボタン 1:全員 2:男子 3:女子
        $opt_sex = array(1, 2, 3);
        $model->field["SEX"] = ($model->field["SEX"] == "") ? "1" : $model->field["SEX"];
        $extra = array("id=\"SEX1\" onclick=\"btn_submit('main');\"", "id=\"SEX2\" onclick=\"btn_submit('main');\"", "id=\"SEX3\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "SEX", $model->field["SEX"], $extra, $opt_sex, count($opt_sex));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //合否コンボ
        $extra = "onchange=\"return btn_submit('judge');\" ";
        $query = knjl097iQuery::getEntexamSettingMst($model, "L013");
        makeCmb($objForm, $arg, $db, $query, "JUDGEDIV", $model->field["JUDGEDIV"], $extra, 1, "BLANK");

        //一覧表示
        $arr_receptno = array();
        if ($model->field["APPLICANTDIV"] != "" && $model->field["TESTDIV"] != "") {
            if (!$model->isWarning()) $model->score = array();

            //データ取得
            $query = knjl097iQuery::SelectQuery($model);
            $result = $db->query($query);

            //取得件数の判定
            if ($result->numRows() == 0 ) {
               $model->setMessage("MSG303");
            } else {
                //更新チェックボックス(ヘッダ)
                if ($model->cmd == "main") {
                    $checked = "";
                    $model->checkAll = "";
                } else {
                    $checked = ($model->checkAll == "1") ? " checked": "";
                }
                $extra = " id=\"CHECKALL\" onchange=\"setSelChk(this);\" ".$checked;
                $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "1", $extra);
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //更新チェックボックス(ヘッダ)
                $checked = ($model->checkAll == "1" || $model->data["CHECKED"][$row["RECEPTNO"]] == "1") ? " checked": "";
                $extra = "id=\"CHECKED_{$row["RECEPTNO"]}\"".$checked.$disflg;
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED_".$row["RECEPTNO"], "1", $extra);

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl097iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl097iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    if ($blank == "ALL") $opt[] = array("label" => "全て", "value" => "ALL");
    $value_flg = false;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    $disabled = ($model->field["TESTDIV"] != "" && $model->field["JUDGEDIV"] != "") ? "" : " disabled ";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disabled;
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
}
?>
