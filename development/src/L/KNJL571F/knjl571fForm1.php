<?php

require_once('for_php7.php');


class knjl571fForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl571fQuery::getNameMst("L003", $model->year);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //レイアウト切替
        if ($model->applicantdiv == "1") {
            $arg["applicantdiv1"] = 1;
        } else {
            $arg["applicantdiv2"] = 1;
        }

        //入試区分コンボ
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjl571fQuery::getNameMst($namecd1, $model->year);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //志望区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl571fQuery::getExamcourse($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSE", $model->examcourse, $extra, 1, "BLANK");

        //専併区分コンボ
        $opt = array();
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl571fQuery::getNameMst("L006", $model->year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $opt[] = array('label' => '9:全体',
                       'value' => '9');
        $arg["TOP"]["SHDIV"] = knjCreateCombo($objForm, "SHDIV", $model->shdiv, $opt, $extra, 1);


        //表示順序ラジオボタン 1:成績順 2:受験番号順
        $opt_sort = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array("id=\"SORT1\" onclick=\"btn_submit('main');\"", "id=\"SORT2\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //合否コンボ
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl571fQuery::getNameMst("L013", $model->year);
        makeCmb($objForm, $arg, $db, $query, "JUDGEDIV", $model->judgediv, $extra, 1, "BLANK");

        //合格コースコンボ
        $query = knjl571fQuery::getExamcourse($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examCourseOpt[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
        }
        $result->free();

        //判定コンボ
        $jugedivOpt = array();
        $extra = " style=\"display:none;\" ";
        $query = knjl571fQuery::getNameMst("L013", $model->year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $jugedivOpt[] = array('label' => $row["NAME1"],
                                     'value' => $row["VALUE"]);
        }
        $result->free();
        $arg["TOP"]["JUDGEDIVOPT"] = knjCreateCombo($objForm, "JUDGEDIVOPT", "", $jugedivOpt, $extra, 1);


        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {

            //データ取得
            $result = $db->query(knjl571fQuery::SelectQuery($model));

            //データ表示
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["RECEPTNO"] . "-" . $row["EXAMNO"];

                //合格コースコンボ
                $extra = ($row["JUDGEDIV"] == "3") ? "" : " style=\"display:none;\" ";
                $sucCourse = $row['SUC_COURSECD'].'-'.$row['SUC_MAJORCD'].'-'.$row['SUC_COURSECODE'];
                $row["SUC_COURSE"] = knjCreateCombo($objForm, "SUC_COURSE"."-".$row["RECEPTNO"], $sucCourse, $examCourseOpt, $extra, 1);

                //判定テキスト
                $extra = " OnChange=\"CheckJudged(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onKeyDown=\"keyChangeEntToTab(this)\"";
                $row["JUDGEDIV"] = knjCreateTextBox($objForm, $row["JUDGEDIV"], "JUDGEDIV[]", 2, 1, $extra);

                //更新チェック
                $disJdg = ($row["PROCEDUREDIV1"] == "1") ? " disabled" : "";
                $extra = "onclick=\"bgcolorYellow(this, '{$row["RECEPTNO"]}');\"";
                $row["CHK_DATA"] = knjCreateCheckBox($objForm, "CHK_DATA"."-".$row["RECEPTNO"], "on", $extra.$disJdg);

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $arr_examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl571findex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl571fForm1.html", $arg);
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
function makeBtn(&$objForm, &$arg, $arr_examno) {

    //更新ボタン
    $disBtn = (0 < get_count($arr_examno)) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disBtn);
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
}
?>
