<?php

require_once('for_php7.php');


class knjl072nForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('app');\" tabindex=-1";
        $query = knjl072nQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl072nQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //志望区分コンボボックス
        $query = knjl072nQuery::getExamcourse($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSE", $model->examcourse, $extra, 1, "BLANK");

        //専併区分コンボボックス
        //入試区分「2：1次入試Ｂ日程」の場合、専併区分「3：併願」固定
        $shdivKotei = ($model->testdiv == "2") ? "3" : "";
        $query = knjl072nQuery::getNameMst("L006", $model->ObjYear, $shdivKotei);
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->shdiv, $extra, 1);

        //表示順序ラジオボタン 1:成績順 2:受験番号順
        $opt_sort = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array("id=\"SORT1\" onclick=\"btn_submit('main');\"", "id=\"SORT2\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt_sort, get_count($opt_sort));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //合否名
        //JAVASCRIPTで変更時にラベル表示する用。
        $arrJudgeName = array();
        $judgement_name = $seq = "";
        $result = $db->query(knjl072nQuery::getNameMstL013("L013", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMECD2"] != "1" && $row["NAMECD2"] != "2") {
                continue;
            }

            $arrJudgeName[$row["NAMECD2"]] = $row["NAME1"];
            $arg["data2"][] = array("judgement_cd" => $row["NAMECD2"], "judgement_name" => $row["NAME1"]);
            $judgement_name .= $seq .$row["NAMECD2"].":".$row["NAME1"];
            $seq = ",";
        }
        $arg["TOP"]["JUDGE"] = $judgement_name;

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->examcourse != "") {
            if (!$model->isWarning()) {
                $model->score = array();
            }

            //データ取得
            $result = $db->query(knjl072nQuery::selectQuery($model));

            if ($result->numRows() == 0) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //合否テキストボックス
                $value = ($model->isWarning()) ? $model->score[$row["EXAMNO"]]["JUDGEMENT"] : $row["JUDGEMENT"];
                if (($value == "3" || $row["PROCEDUREDIV"] == "1" || $row["ENTDIV"] == "1")) {
                    $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:right; visibility:hidden;\" onblur=\"this.value = toInteger(this.value);setName(this,".$row["EXAMNO"].",'0');\"";
                } else {
                    $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:right;\" onblur=\"this.value = toInteger(this.value);setName(this,".$row["EXAMNO"].",'0');\"";
                }
                $objForm->ae(array("type"        => "text",
                                    "name"        => "JUDGEMENT",
                                    "extrahtml"   => $extra,
                                    "size"        => "2",
                                    "maxlength"   => "1",
                                    "multiple"    => "1",
                                    "value"       => $value));
                $row["JUDGEMENT"] = $objForm->ge("JUDGEMENT");

                //innerHTML用ID
                $row["JUDGEMENT_ID"] = "JUDGEMENT_NAME" .$row["EXAMNO"];

                if ($model->isWarning()) {
                    $row["JUDGEMENT_NAME"] = $arrJudgeName[$model->score[$row["EXAMNO"]]["JUDGEMENT"]];
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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl072nindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl072nForm1.html", $arg);
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
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAMCOURSE");
    knjCreateHidden($objForm, "HID_SHDIV");

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
