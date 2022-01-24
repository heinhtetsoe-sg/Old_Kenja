<?php

require_once('for_php7.php');

class knja143tForm1 {
    function main(&$model) {

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja143tForm1", "POST", "knja143tindex.php", "", "knja143tForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //学期マスタ
        $query = knja143tQuery::getSemeMst(CTRL_YEAR, CTRL_SEMESTER);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);

        //年度
        knjCreateHidden($objForm, "YEAR", $Row_Mst["YEAR"]);
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];

        //学期
        knjCreateHidden($objForm, "GAKKI", $Row_Mst["SEMESTER"]);
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //学年コンボ
        $query = knja143tQuery::getGrade($model, CTRL_YEAR, CTRL_SEMESTER);
        $extra = "onchange=\"return btn_submit('changeGrade');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");

        //発行日
        $model->field["ISSUED_DATE"] = (!isset($model->field["ISSUED_DATE"])) ? str_replace("-","/",CTRL_DATE): $model->field["ISSUED_DATE"];
        $arg["data"]["ISSUED_DATE"]=View::popUpCalendar($objForm,"ISSUED_DATE",$model->field["ISSUED_DATE"]);

        //GRADE_CD取得
        $gradeCd = $db->getOne(knja143tQuery::getGradeCd($model->field["GRADE"]));

        //有効期限
        if ($gradeCd == "01") {
            $grdYear = CTRL_YEAR + 3;
        } else if ($gradeCd == "02") {
            $grdYear = CTRL_YEAR + 2;
        } else {
            $grdYear = CTRL_YEAR + 1;
        }
        $model->field["YUKOU_KIGEN"] = (!isset($model->field["YUKOU_KIGEN"]) || $model->cmd == "changeGrade") ? $grdYear."/03": $model->field["YUKOU_KIGEN"];
        $extra = "onblur=\"isDateYukouKigen(this)\"";
        $arg["data"]["YUKOU_KIGEN"] = knjCreateTextBox($objForm, $model->field["YUKOU_KIGEN"], "YUKOU_KIGEN", 7, 7, $extra);

        //行
        $opt = array();
        $value_flg = false;
        $opt[] = array('label' => "１行",'value' => 1);
        $opt[] = array('label' => "２行",'value' => 2);
        $opt[] = array('label' => "３行",'value' => 3);
        $opt[] = array('label' => "４行",'value' => 4);
        $opt[] = array('label' => "５行",'value' => 5);
        $extra = "";
        $arg["data"]["POROW"] = knjCreateCombo($objForm, "POROW", $model->field["POROW"], $opt, $extra, 1);

        //列
        $opt = array();
        $opt[] = array('label' => "１列",'value' => 1);
        $opt[] = array('label' => "２列",'value' => 2);
        $extra = "";
        $arg["data"]["POCOL"] = knjCreateCombo($objForm, "POCOL", $model->field["POCOL"], $opt, $extra, 1);

        //クラス
        $query = knja143tQuery::getAuth($model, CTRL_YEAR, CTRL_SEMESTER);
        $class_flg = false;
        $row1 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"]) || !$class_flg) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $extra = "onchange=\"return btn_submit('knja143t');\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //生徒一覧リスト
        $opt_right = array();
        $opt_left  = array();

        $query = knja143tQuery::getSchno($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["NAME"],
                                 'value' => $row["SCHREGNO"]);
        }
        $result->free();

        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //ボタン//
        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA143T");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja143tForm1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('label' => "全　て",
                       'value' => "");
    }
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "YEAR_SEMESTER") ? CTRL_YEAR.":".CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
