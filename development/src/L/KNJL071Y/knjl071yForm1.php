<?php

require_once('for_php7.php');


class knjl071yForm1
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
        $query = knjl071yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $model->testdiv = ($model->applicantdiv == $model->appHold) ? $model->testdiv : "";
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjl071yQuery::getNameMst($namecd1, $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //志望区分コンボボックス
        $query = knjl071yQuery::getDesirediv($model);
        makeCmb($objForm, $arg, $db, $query, "DESIREDIV", $model->desirediv, $extra, 1);

        //推薦区分および専併区分コンボボックス
        $isShdiv = false;
        if ($model->applicantdiv == "2" && $model->testdiv == "2") {
            $arg["TOP"]["RECOM_KIND_NAME"] = "※ 推薦区分";
        } else if ($model->applicantdiv == "2" && ($model->testdiv == "1" || $model->testdiv == "3")) {
            $arg["TOP"]["RECOM_KIND_NAME"] = "※ 専併区分";
            $isShdiv = true;
        } else {
            $arg["TOP"]["RECOM_KIND_NAME"] = "";
        }
        $extra .= " style=\"width:120px\"";
        $blank = ($model->applicantdiv == "2") ? "" : "BLANK";
        $namecd1 = ($model->applicantdiv == "2" && $model->testdiv == "2") ? "L023" : "L006";
        $query = knjl071yQuery::getNameMst($namecd1, $model->ObjYear, $isShdiv);
        makeCmb($objForm, $arg, $db, $query, "RECOM_KIND", $model->recom_kind, $extra, 1, $blank);

        //表示順序ラジオボタン 1:成績順 2:受験番号順
        $opt_sort = array(1, 2);
        $defSort = ($model->applicantdiv == "1") ? "2" : "1";
        $model->sort = ($model->sort == "" || $model->cmd == "app") ? $defSort : $model->sort;
        $extra = array("id=\"SORT1\" onclick=\"btn_submit('main');\"", "id=\"SORT2\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //対象者ラジオボタン 1:外部生のみ 2:内部生のみ 3:全て
        $opt_inout = array(1, 2, 3);
        $model->inout = ($model->inout) ? $model->inout : "1";
        $extra = array("id=\"INOUT1\" onclick=\"btn_submit('main');\"", "id=\"INOUT2\" onclick=\"btn_submit('main');\"", "id=\"INOUT3\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "INOUT", $model->inout, $extra, $opt_inout, get_count($opt_inout));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //対象者(帰国生)ラジオボタン 1:帰国生除く 2:帰国生のみ
        $opt_kikoku = array(1, 2);
        $model->kikoku = ($model->applicantdiv != "1" && $model->inout != "2" && $model->kikoku) ? $model->kikoku : "1";
        $disKikoku = ($model->applicantdiv != "1" && $model->inout != "2") ? "" : " disabled";
        $extra = array("id=\"KIKOKU1\" onclick=\"btn_submit('main');\"{$disKikoku}", "id=\"KIKOKU2\" onclick=\"btn_submit('main');\"{$disKikoku}");
        $radioArray = knjCreateRadio($objForm, "KIKOKU", $model->kikoku, $extra, $opt_kikoku, get_count($opt_kikoku));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //傾斜配点出力ラジオボタン 1:する 2:しない
        $opt_rate_div = array(1, 2);
        $model->rate_div = ($model->rate_div) ? $model->rate_div : "2";
        $extra = array("id=\"RATE_DIV1\" onclick=\"btn_submit('main');\"", "id=\"RATE_DIV2\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "RATE_DIV", $model->rate_div, $extra, $opt_rate_div, get_count($opt_rate_div));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //スライド表示フラグ
        $isSlideShow = false;
        $result    = $db->query(knjl071yQuery::getCourseQuery($model, $model->desirediv, "2"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $isSlideShow = true;
        }
        $result->free();

        if ($model->applicantdiv == "2") $arg["slideShow"] = 1;

        //特別判定表示フラグ
        $isSpecialShow = ($model->applicantdiv == "2" && $model->testdiv == "1") ? true : false;

        //判定名
        //JAVASCRIPTで変更時にラベル表示する用。
        $arrJudgeName = array();
        $judgediv_name = $seq = "";
        $result = $db->query(knjl071yQuery::getNameMst2("L013", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (!$isSlideShow && $row["NAMECD2"] == "3") continue; //スライドは表示しない
            if ($row["NAMECD2"] == "4") continue; //欠席は入力不可
            if (!$isSpecialShow && $row["NAMECD2"] == "5") continue; //特別判定は表示しない
            $arrJudgeName[$row["NAMECD2"]] = $row["NAME1"];
            $arg["data2"][] = array("judgediv_cd" => $row["NAMECD2"], "judgediv_name" => $row["NAME1"]);
            $judgediv_name .= $seq .$row["NAMECD2"].":".$row["NAME1"];
            $seq = ",";
        }
        $arg["TOP"]["JUDGE"] = $judgediv_name;

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->desirediv != "")
        {
            if (!$model->isWarning()) $model->score = array();

            //データ取得
            $result = $db->query(knjl071yQuery::SelectQuery($model));

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //判定テキストボックス
                $name  = "JUDGEDIV";
                $value = ($model->isWarning()) ? $model->score[$row["EXAMNO"]][$name] : $row[$name];
                $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"this.value = toInteger(this.value);setName(this,".$row["EXAMNO"].",'0','".$row["SLIDE_FLG"]."','".$row["SHIFT_DESIRE_FLG"]."');\"";
                $extra .= ($value == "4") ? " style=\"visibility:hidden;\"" : "";//欠席は表示のみ
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $extra,
                                    "size"        => "2",
                                    "maxlength"   => "1",
                                    "multiple"    => "1",
                                    "value"       => $value));

                $row[$name] = $objForm->ge($name);

                //innerHTML用ID
                $row["JUDGEDIV_ID"] = "JUDGEDIV_NAME" .$row["EXAMNO"];

                if ($model->isWarning()) $row["JUDGEDIV_NAME"] = $arrJudgeName[$model->score[$row["EXAMNO"]]["JUDGEDIV"]];

                //スライド希望
                $row["SLIDE_FLG"] = ($isSlideShow && strlen($row["SLIDE_FLG"])) ? "有" : "";

                //特別判定希望
                $row["SHIFT_DESIRE_FLG"] = ($isSpecialShow && strlen($row["SHIFT_DESIRE_FLG"])) ? "有" : "";

                if ($model->applicantdiv == "2" && $model->testdiv == "2") {
                    //高校・推薦入試は、成績(内申)は表示する。順位は空白とする。
                    $row["TOTAL_RANK4"] = "";
                } else if ($row["ATTEND_ALL_FLG"] != "1") {
                    //全科目受験フラグ(ATTEND_ALL_FLG)が '1' 以外は、成績、順位は空白で表示する。
                    $row["TOTAL4"] = "";
                    $row["TOTAL_RANK4"] = "";
                }

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl071yindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl071yForm1.html", $arg);
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
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_DESIREDIV");
    knjCreateHidden($objForm, "HID_RECOM_KIND");

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
