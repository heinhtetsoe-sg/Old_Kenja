<?php

require_once('for_php7.php');

class knjd669jForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjd669jForm1", "POST", "knjd669jindex.php", "", "knjd669jForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $extra = "onChange=\"return btn_submit('knjd669jChg');\"";
        $query = knjd669jQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $extra = "onChange=\"return btn_submit('knjd669j');\"";
        $query = knjd669jQuery::getGrade();
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //年組コンボ
        $extra = "onChange=\"return btn_submit('knjd669j');\"";
        $query = knjd669jQuery::getHrclass($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["IDOU_DATE"] == "") $model->field["IDOU_DATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["IDOU_DATE"] = View::popUpCalendar($objForm, "IDOU_DATE", $model->field["IDOU_DATE"]);

        //印刷日付
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //学校校種を取得
        $model->schoolkind = $db->getOne(knjd669jQuery::getSchoolKind($model));

        //テスト種別コンボ
        $extra = "onChange=\"return btn_submit('knjd669jChg');\"";
        $query = knjd669jQuery::getTestitem($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND_ITEMCD", $model->field["TESTKIND_ITEMCD"], $extra, 1);

        if (substr($model->field["TESTKIND_ITEMCD"],0,2) == '99') {
            $arg["showPrintName"] = "1";
            //生徒の氏名・順位を表示する
            $check  = ($model->field["PRINT_NAME"] == "1" || $model->cmd == '' || $model->cmd == 'knjd669jChg') ? "checked" : "";
            $check .= " id=\"PRINT_NAME\"";
            $value  = isset($model->field["PRINT_NAME"]) ? $model->field["PRINT_NAME"] : 1;
            $arg["data"]["PRINT_NAME"] = knjCreateCheckBox($objForm, "PRINT_NAME", $value, $check, "");
        }

        //人数0を表示しない
        $extra = ($model->field["NOT_PRINT_COUNT0"] == "1" || $model->cmd == '') ? "checked" : "";
        $extra .= " id=\"NOT_PRINT_COUNT0\"";
        $value  = isset($model->field["NOT_PRINT_COUNT0"]) ? $model->field["NOT_PRINT_COUNT0"] : "1";
        $arg["data"]["NOT_PRINT_COUNT0"] = knjCreateCheckBox($objForm, "NOT_PRINT_COUNT0", $value, $extra, "");

        //クラス一覧リスト
        makeClassList($objForm, $arg, $db, $model);

        //総合順位出力ラジオボタン 1:学級 2:学年 3:コース 4:クラス
        $opt_rank = array(1, 2, 3, 4); //1:学級はカットになった為htmlのところでカットした(帳票の関係で送る値を変えないため)
        $model->field["OUTPUT_RANK"] = ($model->field["OUTPUT_RANK"] == "") ? "2" : $model->field["OUTPUT_RANK"];
        $extra = array("id=\"OUTPUT_RANK1\"", "id=\"OUTPUT_RANK2\"", "id=\"OUTPUT_RANK3\"", "id=\"OUTPUT_RANK4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_RANK", $model->field["OUTPUT_RANK"], $extra, $opt_rank, get_count($opt_rank));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd669jForm1.html", $arg); 
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
    if ($name != "SEMESTER") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//クラス一覧リスト作成
function makeClassList(&$objForm, &$arg, $db, &$model)
{
    $row1 = array();
    $query = knjd669jQuery::getStudent($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[] = array('label' => $row["SCHREGNO"]." ".$row["ATTENDNO"]."番 ".$row["NAME_SHOW"],
                        'value' => $row["SCHREGNO"]);
    }
    $result->free();

    //一覧リストを作成する
    $extra = "multiple style=\"width:230px; height:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["SCHREG_NAME"] = knjCreateCombo($objForm, "SCHREG_NAME", "", $row1, $extra, 20);

    //出力対象教科リストを作成する
    $extra = "multiple style=\"width:230px; height:230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["SCHREG_SELECTED"] = knjCreateCombo($objForm, "SCHREG_SELECTED", "", array(), $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $db) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJD669J");
}
?>
