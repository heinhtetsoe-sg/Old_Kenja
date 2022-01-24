<?php

require_once('for_php7.php');

class knjl015gForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl015gQuery::getNameMst($model, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl015gQuery::getNameMst($model, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //専併区分コンボボックス（専願のみ）
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl015gQuery::getNameMst($model, "L006", "1");
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->shdiv, $extra, 1);

        //志望コースコンボボックス（敬愛1000:総合進学コースのみ　柏原4000:スポーツコースのみ）
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl015gQuery::getEntExamCourse($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->examcoursecd, $extra, 1);

        //志望クラブコンボ（柏原のみ）
        if ($model->isKasiwara == "1") {
            $arg["isKasiwara"] = 1;
            $arg["TOP"]["HOPE_CLUB_CD_LABEL"] = "※ 志望クラブ";
            $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
            $query = knjl015gQuery::getNameMst($model, "L037");
            makeCmb($objForm, $arg, $db, $query, "HOPE_CLUB_CD", $model->hope_club_cd, $extra, 1);
        }

        //一覧表示
        $list_examno = $sep = "";
        $disable = " disabled";
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->shdiv != "" && $model->examcoursecd != "") {
            //データ取得
            $query = knjl015gQuery::SelectQuery($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $list_examno .= $sep.$row["EXAMNO"];

                //クラブコンボ
                if ($model->isKasiwara == "1") {
                    $query = knjl015gQuery::getNameMst2($model, "L037", "ABBV1", $model->hope_club_cd);
                } else {
                    $query = knjl015gQuery::getNameMst2($model, "L037", "ABBV1");
                }
                $extra = "";
                $row["CLUB"] = makeCmbReturn($objForm, $arg, $db, $query, "CLUB-".$row["EXAMNO"], $row["CLUB"], $extra, 1, "BLANK");

                //ランクコンボ
                $query = knjl015gQuery::getNameMst2($model, "L025", "NAME1");
                $extra = "";
                $row["RANK"] = makeCmbReturn($objForm, $arg, $db, $query, "RANK-".$row["EXAMNO"], $row["RANK"], $extra, 1, "BLANK");

                $arg["data"][] = $row;
                $sep = ",";
                $disable = "";
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $disable);

        //hidden作成
        makeHidden($objForm, $model, $list_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl015gindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl015gForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = ($blank) ? 1 : 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

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

//コンボ作成（表内）
function makeCmbReturn(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $disable) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $list_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAMNO", $list_examno);
}
?>
