<?php

require_once('for_php7.php');


class knjl072yForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $result = $db->query(knjl072yQuery::getNameMst2("L003", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             if($row["NAMECD2"] == "2") $applicantdiv = $row["NAMECD2"].":".$row["NAME1"];
        }
        $arg["TOP"]["APPLICANTDIV"] = $applicantdiv;

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl072yQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //志望区分コンボボックス
        $query = knjl072yQuery::getDesirediv($model);
        makeCmb($objForm, $arg, $db, $query, "DESIREDIV", $model->desirediv, $extra, 1);

        //表示順序ラジオボタン 1:成績順 2:受験番号順
        $opt_sort = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
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

        //判定名
        //JAVASCRIPTで変更時にラベル表示する用。
        $arrJudgeName = array();
        $judgekind_name1 = $judgekind_name2 = $seq1 = $seq2 = "";
        $result = $db->query(knjl072yQuery::getNameMst2("L025", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $arrJudgeName[$row["NAMECD2"]] = $row["NAME1"];
            $arg["data2"][] = array("judgekind_cd" => $row["NAMECD2"], "judgekind_name" => $row["NAME1"]);
            if ($row["NAMESPARE2"] == "1") {
                $judgekind_name2 .= $seq2 .$row["NAMECD2"].":".$row["NAME1"];
                $seq2 = ", ";
            } else {
                $judgekind_name1 .= $seq1 .$row["NAMECD2"].":".$row["NAME1"];
                $seq1 = ", ";
            }
        }
        $arg["TOP"]["JUDGE"] = $judgekind_name1 ."<BR>" .$judgekind_name2;

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "")
        {
            if (!$model->isWarning()) $model->score = array();

            //データ取得
            $result = $db->query(knjl072yQuery::SelectQuery($model));

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303");
            }

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //判定テキストボックス
                $name  = "JUDGE_KIND";
                $value = ($model->isWarning()) ? $model->score[$row["EXAMNO"]][$name] : $row[$name];
                $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"this.value = toInteger(this.value);setName(this,".$row["EXAMNO"].",'0');\"";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "extrahtml"   => $extra,
                                    "size"        => "2",
                                    "maxlength"   => "1",
                                    "multiple"    => "1",
                                    "value"       => $value));

                $row[$name] = $objForm->ge($name);

                //innerHTML用ID
                $row["JUDGEKIND_ID"] = "JUDGEKIND_NAME" .$row["EXAMNO"];

                if ($model->isWarning()) $row["JUDGEKIND_NAME"] = $arrJudgeName[$model->score[$row["EXAMNO"]]["JUDGE_KIND"]];

                //スポーツ希望チェック用
                knjCreateHidden($objForm, "SPORTS_FLG" .$row["EXAMNO"], $row["SPORTS_FLG"]);

                $row["SPORTS_FLG"] = ($row["SPORTS_FLG"] == "1") ? '有' : "";

                //T特別奨希望者
                knjCreateHidden($objForm, "SPORTS_FLG2" .$row["EXAMNO"], $row["SPORTS_FLG2"]);
                $row["SPORTS_FLG2"] = ($row["SPORTS_FLG2"] == "1") ? '有' : "";

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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl072yindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl072yForm1.html", $arg);
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

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    if ($name == "DESIREDIV") {
        if ($value == '9') $value_flg = true;
        $opt[] = array('label' => '9:全て', 'value' => '9');
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
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_DESIREDIV");
}
?>
