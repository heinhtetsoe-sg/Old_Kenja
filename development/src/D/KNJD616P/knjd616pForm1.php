<?php

require_once('for_php7.php');


class knjd616pForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd616pForm1", "POST", "knjd616pindex.php", "", "knjd616pForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd616pQuery::getSemester(0);
        $extra = "onchange=\"return btn_submit('knjd616pChseme'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query = knjd616pQuery::getSemester("", $model->field["SEMESTER"]);
        $result = $db->query($query);
        $semeSdate = $semeEdate = CTRL_DATE;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semeSdate = $row["SDATE"];
            $semeEdate = $row["EDATE"];
        }
        $result->free();

        //学年コンボ作成
        $query = knjd616pQuery::getSchoolName();
        $schoolName = $db->getOne($query);
        $query = knjd616pQuery::getGrade($seme);
        $extra = "onchange=\"return btn_submit('knjd616p'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //テストコンボ作成
        $query = knjd616pQuery::getTest($model->field["SEMESTER"], $model->field["GRADE"]);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "", 1);

        //出欠集計日付作成
        if ($model->field["SDATE"] == "" || $model->cmd == 'knjd616pChseme') {
            $model->field["SDATE"] = str_replace("-", "/", $semeSdate);
        }
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //クラスリストToリスト作成
        makeClassList($objForm, $arg, $db, $model, $seme);

        //帳票パターン
        $opt_rank = array(1, 2, 3, 4, 5, 6);
        $model->field["OUTPUT_PATERN"] = ($model->field["OUTPUT_PATERN"] == "") ? "4" : $model->field["OUTPUT_PATERN"];
        $extra = array("id=\"OUTPUT_PATERN1\" onclick=\"return chkPattern();\" ", "id=\"OUTPUT_PATERN2\" onclick=\"return chkPattern();\" ", "id=\"OUTPUT_PATERN3\" onclick=\"return chkPattern();\" ", "id=\"OUTPUT_PATERN4\" onclick=\"return chkPattern();\" ", "id=\"OUTPUT_PATERN5\" onclick=\"return chkPattern();\" ", "id=\"OUTPUT_PATERN6\" onclick=\"return chkPattern();\" ");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_PATERN", $model->field["OUTPUT_PATERN"], $extra, $opt_rank, get_count($opt_rank));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //総合順位出力ラジオボタン 1.学級 2:学年 3:コース 4:学科 5:コースグループ
        $opt_rank = array(1, 2, 3, 4, 5);
        $model->field["OUTPUT_RANK"] = ($model->field["OUTPUT_RANK"] == "") ? "5" : $model->field["OUTPUT_RANK"];
        $extra = array("id=\"OUTPUT_RANK2\"", "id=\"OUTPUT_RANK3\"", "id=\"OUTPUT_RANK4\"", "id=\"OUTPUT_RANK5\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_RANK", $model->field["OUTPUT_RANK"], $extra, $opt_rank, get_count($opt_rank));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $model->field["OUTPUT_KIJUN"] = $model->field["OUTPUT_KIJUN"] ? $model->field["OUTPUT_KIJUN"] : '1';
        $opt_kijun = array(1, 2);
        $extra = array("id=\"OUTPUT_KIJUN1\"", "id=\"OUTPUT_KIJUN2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $model->field["OUTPUT_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KEKKA"] = knjCreateTextBox($objForm, $model->field["KEKKA"], "KEKKA", 3, 3, $extra);

        $fields = array("KKESSEKI", "KCHIKOKU_SOUTAI", "KCHIKOKU", "KSOUTAI", "KKEKKA");
        foreach ($fields  as $field) {
            if ($model->field[$field] == '') {
                $model->field[$field] = "0";
            } 
            $value = $model->field[$field];
            $arg["data"][$field] = knjCreateTextBox($objForm, $value, $field, 3, 3, $extra);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $seme);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd616pForm1.html", $arg); 
    }
}

function makeClassList(&$objForm, &$arg, $db, $model, $seme) {

    //クラス一覧リストを作成する
    $query = knjd616pQuery::getHrClass($seme, $model->field["GRADE"]);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px;height:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt1, $extra, 20);

    //対象クラスリストを作成する
    $extra = "multiple style=\"width:230px;height:230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
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
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'print');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model, $seme) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJD616P");
    knjCreateHidden($objForm, "cmd");

    //日付範囲チェック用
    $sseme = $model->control["学期開始日付"][9];
    $eseme = $model->control["学期終了日付"][9];
    if($model->field["SEMESTER"] == '9'){
        $semeflg = CTRL_SEMESTER;
    } else {
        $semeflg = $model->field["SEMESTER"];
    }
    knjCreateHidden($objForm, "YEAR_SDATE", $sseme);
    knjCreateHidden($objForm, "YEAR_EDATE", $eseme);
    knjCreateHidden($objForm, "SEME_FLG", $semeflg);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
//    knjCreateHidden($objForm, "OUTPUT_PATERN", "5");

}

?>
