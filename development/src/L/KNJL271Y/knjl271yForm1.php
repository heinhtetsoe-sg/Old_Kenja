<?php

require_once('for_php7.php');


class knjl271yForm1
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
        $query = knjl271yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl271yQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //表示順序ラジオボタン 1:成績順 2:受験番号順
        $opt_sort = array(1, 2);
        $model->sort = ($model->sort) ? $model->sort : "2";
        $extra = array("id=\"SORT1\" onclick=\"btn_submit('main');\"", "id=\"SORT2\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //試験出力情報ラジオボタン 1:傾斜配点 2:通常得点
        $opt_rate_div = array(1, 2);
        $model->rate_div = ($model->rate_div) ? $model->rate_div : "1";
        $extra = array("id=\"RATE_DIV1\" onclick=\"btn_submit('main');\"", "id=\"RATE_DIV2\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "RATE_DIV", $model->rate_div, $extra, $opt_rate_div, get_count($opt_rate_div));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //判定名
        //JAVASCRIPTで変更時にラベル表示する用。
        $arrJudgeName = array();
        $judgediv_name = $seq = "";
        $result = $db->query(knjl271yQuery::getNameMst("L013", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMECD2"] == "4") continue; //欠席は入力不可

            $arg["data2"][] = array("judgediv_cd" => $row["NAMECD2"], "judgediv_name" => $row["NAME1"]);
            $arrJudgeName[$row["NAMECD2"]] = $row["NAME1"];

            $judgediv_name .= $seq .$row["NAMECD2"].":".$row["NAME1"];
            $seq = ",";
        }
        $arg["TOP"]["JUDGE"] = $judgediv_name;

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {

            if (!$model->isWarning()) $model->score = array();

            //データ取得
            $result = $db->query(knjl271yQuery::SelectQuery($model));

            if ($result->numRows() == 0 ){
               $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //判定テキストボックス
                $name  = "JUDGEDIV";
                $value = ($model->isWarning()) ? $model->score[$row["EXAMNO"]][$name] : $row[$name];
                $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" onblur=\"this.value = toInteger(this.value);setName(this,".$row["EXAMNO"].",'0');\"";
                //欠席または手続済みの人は表示のみ
                $extra .= ($value == "4" || $row["PROCEDUREDIV"] == "1" || $row["ENTDIV"] == "1") ? " style=\"text-align:right; visibility:hidden;\"" : " style=\"text-align:right;\"";
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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl271yindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl271yForm1.html", $arg);
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
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
