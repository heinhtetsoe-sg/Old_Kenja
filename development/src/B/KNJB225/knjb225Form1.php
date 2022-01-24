<?php

require_once('for_php7.php');

class knjb225Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb225Form1", "POST", "knjb225index.php", "", "knjb225Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjb225Query::getSemester($model);
        $extra = "onchange=\"return btn_submit('knjb225')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学籍在籍データ件数
        $cnt = $db->getOne(knjb225Query::checkRegdDat());
        $flg = ($cnt > 0) ? "" : 1;

        //各名称のMAX値取得
        $max_grade_len = 0;
        $max_major_len = 0;
        $result = $db->query(knjb225Query::getGradeCouse($model, $flg));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $max_grade_len = ($max_grade_len < mb_strwidth($row["GRADE_NAME1"])) ? mb_strwidth($row["GRADE_NAME1"]) : $max_grade_len;
            $max_major_len = ($max_major_len < mb_strwidth($row["COURSENAME"].$row["MAJORNAME"])) ? mb_strwidth($row["COURSENAME"].$row["MAJORNAME"]) : $max_major_len;
        }
        $result->free();

        //コースコンボ作成
        $opt = array();
        $result = $db->query(knjb225Query::getGradeCouse($model, $flg));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学年、課程学科名称の桁数取得
            $grade_len = mb_strwidth($row["GRADE_NAME1"]);
            $major_len = mb_strwidth($row["COURSENAME"].$row["MAJORNAME"]);
            //学年、課程学科名称の空埋め数
            $grade_spcnt = $max_grade_len - $grade_len;
            $major_spcnt = $max_major_len - $major_len;

            $opt[] = array("label" => $row["GRADE_NAME1"].str_repeat("&nbsp;",$grade_spcnt)."&nbsp;".
                                      "(".$row["COURSECD"].$row["MAJORCD"].")&nbsp;".
                                      $row["COURSENAME"].$row["MAJORNAME"].str_repeat("&nbsp;",$major_spcnt)."&nbsp;".
                                      "(".$row["COURSECODE"].")&nbsp;".$row["COURSECODENAME"],
                           "value" => $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"]);
        }
        $result->free();

        $extra = "";
        $arg["data"]["GRADE_COURSE"] = knjCreateCombo($objForm, "GRADE_COURSE", $model->field["GRADE_COURSE"], $opt, $extra, 1);

        //帳票選択ラジオボタン 1:予定 2:履修
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb225Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJB225");
    knjCreateHidden($objForm, "GRADE");
    knjCreateHidden($objForm, "COURSECD");
    knjCreateHidden($objForm, "MAJORCD");
    knjCreateHidden($objForm, "COURSECODE");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}
?>
