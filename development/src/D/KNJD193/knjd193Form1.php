<?php

require_once('for_php7.php');

class knjd193Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd193Form1", "POST", "knjd193index.php", "", "knjd193Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd193Query::getSemester();
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);

        //年組コンボ
        $query = knjd193Query::getAuth($model);
        $extra = "onchange=\"return btn_submit('grade')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HRCLASS", $model->grade, $extra, 1);

        //テスト種別コンボ
        $query  = knjd193Query::getName($model->semester);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->testkindcd, "", 1, $model);

        //序列の基準点ラジオボタン 1:総合点 2:平均点
        $shokiti = $model->field["JORETU_DIV"] ? $model->field["JORETU_DIV"] : '1';
        if ($model->cmd == 'grade') {
            $query = knjd193Query::getSchoolKing($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row["SCHOOL_KIND"] == 'H' && $row["GRADE_CD"] >= '02') {
                $shokiti = 2;
            } else {
                $shokiti = 1;
            }
        }
        $opt_joretu = array(1, 2);
        $extra = "onClick=\"return btn_submit('main')\"";
        $label = array($extra." id=\"JORETU_DIV1\"", $extra." id=\"JORETU_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "JORETU_DIV", $shokiti, $label, $opt_joretu, get_count($opt_joretu));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //科目数テキストボックス
        $dis = '';
        if ($shokiti == 2) {
            $value = '';
            $dis = ' disabled="disabled" ';
        } elseif($model->kamoku_su) {
            $value = $model->kamoku_su;
        } else {
            $value = 15;
        }
        $extra = " onBlur=\"return toInteger(this.value);\"" . $dis;
        $arg["data"]["KAMOKU_SU"] = knjCreateTextBox($objForm, $value, "KAMOKU_SU", 2, 2, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd193Form1.html", $arg); 
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

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //プレビュー/印刷
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJD193");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
}
?>
