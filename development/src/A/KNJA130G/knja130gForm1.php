<?php

require_once('for_php7.php');

class knja130gForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja130gForm1", "POST", "knja130gindex.php", "", "knja130gForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期テキストボックスを作成する
        $arg["data"]["GAKKI"] = $model->control["学期名"][$model->control["学期"]];
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //出力順ラジオボタンを作成
        $radioValue = array(1, 2);
        $disable = 0;
        if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;
        if ($model->field["OUTPUT"] == 1) $disable = 1;
        $extra = array("id=\"OUTPUT1\" onclick =\" return btn_submit('clickchange');\"", "id=\"OUTPUT2\" onclick =\" return btn_submit('clickchange');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $radioValue, get_count($radioValue));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($disable == 1) {
            $arg["student"] = 1;
        } else {
            $arg["hr_class"] = 2;
        }
        //宮城県の場合、画面切り替え
        $z010name1 = "";
        $query = knja130gQuery::getNameMst("Z010", "00");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $z010name1 = $row["NAME1"];
        }
        $result->free();
        if ($z010name1 == 'miyagiken') {
            $arg["not_miyagiken"] = "";
        } else {
            $arg["not_miyagiken"] = "1";
        }

        //クラス選択コンボボックスを作成する
        if ($disable == 1) {
            $query = knja130gQuery::getHrClass();
        } else {
            $query = knja130gQuery::getGrade();
        }
        $row1 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        if ($model->cmd == 'clickchange' ) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            $model->cmd = 'knja130g';
        }

        $extra = "onchange=\"return btn_submit('knja130g'),AllClearList();\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //対象者リストを作成する
        makeStudentList($objForm, $arg, $db, $model, $disable);

        //帳票種別チェックボックス
        $name = array("KOSEKI", "SEITO", "SIMEI", "SCHZIP", "ADDR2", "SCHOOLZIP", "ZENKI_KATSUDO", "ZENKI_GAKUSHU", "KOUKI_KATSUDO", "KOUKI_GAKUSHU", "TANI");
        foreach ($name as $key => $val) {
            if ($val == "KOSEKI" || $val == "SCHZIP" || $val == "ADDR2" || $val == "SCHOOLZIP") {
                $extra = $model->field[strtolower($val)] == "1" ? "checked" : "";
            } else {
                $extra = ($model->field[strtolower($val)] == "1" || $model->cmd == "") ? "checked" : "";
            }
            $extra .= " onclick=\"kubun();\" id=\"$val\"";

            $arg["data"][$val] = knjCreateCheckBox($objForm, strtolower($val), "1", $extra, "");
        }

        //未履修科目出力・履修のみ科目出力のデフォルト値設定
        if ($model->field["MIRISYU"] == "" || $model->field["RISYU"] == "" || $model->field["RISYUTOUROKU"] == "") {
            $query = knja130gQuery::getRisyuMirsyu($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                // 値がYなら「しない」それ以外は「する」
                $model->field["MIRISYU"] = "Y" == $row["NAMESPARE1"] ? "2" : "1";
                $model->field["RISYU"] = "Y" == $row["NAMESPARE2"] ? "2" : "1";
                $model->field["RISYUTOUROKU"] = "Y" == $row["NAMESPARE3"] ? "2" : "1";
            }
            $result->free();
            $model->field["MIRISYU"] = isset($model->field["MIRISYU"]) ? $model->field["MIRISYU"] : "2"; // デフォルトは「しない」
            $model->field["RISYU"] = isset($model->field["RISYU"]) ? $model->field["RISYU"] : "1"; // デフォルトは「する」
            $model->field["RISYUTOUROKU"] = isset($model->field["RISYUTOUROKU"]) ? $model->field["RISYUTOUROKU"] : "2"; // デフォルトは「しない」
        }

        //未履修科目出力ラジオボタンを作成する 1:する 2:しない
        $opt_mirisyu = array(1, 2);
        $extra  = array("id=\"MIRISYU1\" onclick=\"checkRisyu();\"", "id=\"MIRISYU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "MIRISYU", $model->field["MIRISYU"], $extra, $opt_mirisyu, get_count($opt_mirisyu));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //履修のみ科目出力ラジオボタン 1:する 2:しない
        $optRisyu = array(1, 2);
        $extra  = array("id=\"RISYU1\" onclick=\"checkRisyu();\"", "id=\"RISYU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "RISYU", $model->field["RISYU"], $extra, $optRisyu, get_count($optRisyu));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //履修登録のみ科目出力ラジオボタン 1:する 2:しない
        $optRisyuT = array(1, 2);
        $extra  = array("id=\"RISYUTOUROKU1\" onclick=\"checkRisyu();\"", "id=\"RISYUTOUROKU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "RISYUTOUROKU", $model->field["RISYUTOUROKU"], $extra, $optRisyuT, get_count($optRisyuT));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        if (!isset($model->warning) && $model->cmd == 'print'){
            $model->cmd = 'knja130g';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja130gForm1.html", $arg); 
    }
}
//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model, $disable) {

    if ($disable == 1) {
        $query = "SELECT T2.SCHREGNO AS SCHREGNO,".
                 " T2.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ".
                 "FROM SCHREG_BASE_MST T1 ".
                 "INNER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ".
                 "WHERE (((T2.YEAR)='" .$model->control["年度"] ."') AND ".
                 "((T2.SEMESTER)='" .$model->control["学期"] ."') AND ".
                 "((T2.GRADE || T2.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."'))".
                 "ORDER BY ATTENDNO";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if(in_array($row["SCHREGNO"], $model->select_data["selectdata"])) {
                $opt2[]= array('label' => $row["NAME"],
                               'value' => $row["SCHREGNO"]);
            } else {
                $opt1[]= array('label' => $row["NAME"],
                               'value' => $row["SCHREGNO"]);
            }
        }
    } else {
        $query = common::getHrClassAuth(CTRL_YEAR,CTRL_SEMESTER,AUTHORITY,STAFFCD);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (substr($row["VALUE"],0,2) != $model->field["GRADE_HR_CLASS"]) continue;
            if(in_array($row["VALUE"], $model->select_data["selectdata"])) {
                $opt2[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            } else {
                $opt1[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
        }
    }
    $result->free();

    $extra = "multiple style=\"height:250px;width:250px\" ondblclick=\"move1('left',$disable)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

    //生徒一覧リストを作成する
    $extra = "multiple style=\"height:250px;width:250px\" ondblclick=\"move1('right',$disable)\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", isset($opt2)?$opt2:array(), $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right',$disable);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left',$disable);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right',$disable);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $disable);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    if($model->inei == ""){
        //CSVボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    }
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA130G");
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);
    knjCreateHidden($objForm, "seitoSidoYorokuFormType", $model->Properties["seitoSidoYorokuFormType"]);
    knjCreateHidden($objForm, "seitoSidoYorokuFieldSize", $model->Properties["seitoSidoYorokuFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuSougouFieldSize", $model->Properties["seitoSidoYorokuSougouFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuSpecialactremarkFieldSize", $model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuKinsokuForm", $model->Properties["seitoSidoYorokuKinsokuForm"]);
    knjCreateHidden($objForm, "INEI", $model->inei);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyactSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyvalSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuZaisekiMae", $model->Properties["seitoSidoYorokuZaisekiMae"]);
    knjCreateHidden($objForm, "seitoSidoYorokuKoumokuMei", $model->Properties["seitoSidoYorokuKoumokuMei"]);
    knjCreateHidden($objForm, "seitoSidoYorokuHyotei0ToBlank", $model->Properties["seitoSidoYorokuHyotei0ToBlank"]);
    knjCreateHidden($objForm, "seitoSidoYorokuYoshiki2PrintOrder", $model->Properties["seitoSidoYorokuYoshiki2PrintOrder"]);
    knjCreateHidden($objForm, "useCurriculumcd" , $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintAnotherStudyrec", $model->Properties["seitoSidoYorokuNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintAnotherAttendrec", $model->Properties["seitoSidoYorokuNotPrintAnotherAttendrec"]);
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useProvFlg" , $model->Properties["useProvFlg"]);
    knjCreateHidden($objForm, "useGakkaSchoolDiv" , $model->Properties["useGakkaSchoolDiv"]);
    knjCreateHidden($objForm, "seitoSidoYorokuHozonkikan" , $model->Properties["seitoSidoYorokuHozonkikan"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J" , $model->Properties["HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J"]);
}

?>
