<?php

require_once('for_php7.php');


class knjl072bForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('app');\" tabindex=-1";
        $query = knjl072bQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl072bQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //特別措置者(インフルエンザ)
        $extra = "id=\"SPECIAL_REASON_DIV\" onclick=\"return btn_submit('main');\" tabindex=-1 ";
        $extra .= strlen($model->special_reason_div) ? "checked='checked' " : "";
        $arg["TOP"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //表示順序ラジオボタン 1:成績順 2:受験番号順
        $opt_sort = array(1, 2);
        $model->sort = ($model->sort == "") ? "2" : $model->sort;
        $extra = array("id=\"SORT1\" onclick=\"btn_submit('main');\"", "id=\"SORT2\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //抽出平均点FROM
        $extra = "style=\"text-align:right\" onblur=\"this.value=toFloat(this.value);\"";
        $arg["TOP"]["AVG_FROM"] = knjCreateTextBox($objForm, $model->avg_from, "AVG_FROM", 5, 5, $extra);

        //抽出平均点TO
        $extra = "style=\"text-align:right\" onblur=\"this.value=toFloat(this.value);\"";
        $arg["TOP"]["AVG_TO"] = knjCreateTextBox($objForm, $model->avg_to, "AVG_TO", 5, 5, $extra);

        //抽出合否区分コンボ
        $query = knjl072bQuery::getJudgmentDiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "JUDGMENT_DIV_SEARCH", $model->judgment_div_search, $extra, 1, "BLANK");

        //合否詳細区分マスタ
        $query = knjl072bQuery::getJudgmentDiv($model, $model->judgment_div_search);
        $jdgRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //変更合否区分コンボ
        $query = knjl072bQuery::getJudgmentDiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "JUDGMENT_DIV", $model->judgment_div, $extra, 1, "");

        //width
        $arg["TOP"]["WIDTH_TAN"] = ($model->testdiv == "1") ? "220" : "240";

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && ($model->judgment_div_search != "" || $model->avg_from != "" || $model->avg_to != ""))
        {
            if (!$model->isWarning()) $model->score = array();

            //データ取得
            $result = $db->query(knjl072bQuery::simSql($model, $jdgRow));

            if ($result->numRows() == 0 && $model->cmd != "read") {
               $model->setMessage("MSG303");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                $key = $row["RECEPTNO"] . "-" . $row["EXAMNO"];
                $extra = ($row["PROCEDUREDIV1"] == "1" || $row["PROCEDUREDIV"] == "1") ? "disabled" : "";
                $extra .= " onclick=\"bgcolorPink(this, '{$row["EXAMNO"]}');\"";
                $row["CHK_DATA"] = knjCreateCheckBox($objForm, "CHK_DATA", $key, $extra, 1);
//                $row["CHK_DATA"] = knjCreateCheckBox($objForm, "CHK_DATA", "on", $extra, 1);

                $arg["data"][] = $row;
            }
        }

        //抽出人数
        $arg["TOP"]["DATA_CNT"] = get_count($arr_examno);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl072bindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl072bForm1.html", $arg);
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
        if ($name == "RECOM_KIND" && $blank == "BLANK") continue;
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
    //抽出ボタン
    $extra = "onclick=\"return btn_submit('search');\"";
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "抽 出", $extra);
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
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
}
?>
