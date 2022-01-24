<?php

require_once('for_php7.php');
class knjd418Form1 {

    function main(&$model) {

        $arg["jscript"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjd418index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            //年度コンボ
            $query = knjd418Query::getYear();
            $extra = "onchange=\"return btn_submit('combo');\"";
            makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1, $model);
        } else {
            $arg["YEAR"] = CTRL_YEAR;
            knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
            $model->year = CTRL_YEAR;
        }

        //前年度・学期からコピーボタン
        $title = ($model->Properties["useGradeKindCompGroupSemester"] == "1") ? "前年度・学期からコピー" : "前年度からコピー";
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", $title, $extra);

        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            //学期コンボ
            $query = knjd418Query::getSemester($model);
            $extra = "onchange=\"return btn_submit('combo');\"";
            makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, $model);
        }

        //学部コンボ
        $query = knjd418Query::getSchoolKind();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "GAKUBU_SCHOOL_KIND", $model->gakubu_school_kind, $extra, 1, $model);

        //一覧表示
        $query = knjd418Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"][] = $row;
        }

        //Hidden作成
        knjCreateHidden($objForm, "cmd");

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "update") {
            $arg["jscript"] = "window.open('knjd418index.php?cmd=edit','right_frame')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd418Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, &$model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == 'YEAR') {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else if ($name == 'SEMESTER') {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
