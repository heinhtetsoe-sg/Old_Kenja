<?php

require_once('for_php7.php');

class knjb050Form1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb050Form1", "POST", "knjb050index.php", "", "knjb050Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //ラジオボタンを作成//時間割種別（基本時間割/通常時間割）
        $opt = array(1, 2);
        $model->field["RADIO"] = ($model->field["RADIO"] == "") ? "1" : $model->field["RADIO"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"RADIO{$val}\" onClick=\"jikanwari(this);\"");
        }
        $radioArray = knjCreateRadio($objForm, "RADIO", $model->field["RADIO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if( $model->field["RADIO"] == 2 ) {     //通常時間割選択時
            $dis_jikan = "disabled";                //時間割選択コンボ使用不可
            $dis_date  = "";                        //指定日付テキスト使用可
            $arg["Dis_Date"]  = " dis_date(false); " ;
        } else {                                //基本時間割選択時
            $dis_jikan = "";                        //時間割選択コンボ使用可
            $dis_date  = "disabled";                //指定日付テキスト使用不可
            $arg["Dis_Date"]  = " dis_date(true); " ;
        }

        //時間割選択コンボボックスを作成
        $query = knjb050Query::getBscHdQuery($model);
        $extra = "".$dis_jikan;
        makeCmb($objForm, $arg, $db, $query, "TITLE", $model->field["TITLE"], $extra, 1, "BLANK");

        //指定日付テキストボックスを作成
        if ($model->field["RADIO"] == 2){
            if (!isset($model->field["DATE"]))
                $model->field["DATE"] = $model->control["学籍処理日"];
            //指定日を含む指定週の開始日(月曜日)と終了日(日曜日)を取得
            common::DateConv2($model->field["DATE"],$OutDate1,$OutDate2,1);
            $model->field["DATE2"] = $OutDate2;
        } else {
            $model->field["DATE"] = "";
            $model->field["DATE2"] = "";
        }
        $arg["data"]["DATE"] = View::popUpCalendar($objForm,"DATE",$model->field["DATE"],"reload=true");

        $extra = " disabled";
        $arg["data"]["DATE2"] = knjCreateTextBox($objForm, $model->field["DATE2"], "DATE2", 12, 12, $extra);

        //ラジオボタンを作成//出力区分（1:職員, 2:施設）
        $opt = array(1, 2);
        $model->field["KUBUN"] = ($model->field["KUBUN"] == "") ? "1" : $model->field["KUBUN"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"KUBUN{$val}\" onClick=\"shutu_kubun(this);\"");
        }
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if (isset($model->field["KUBUN"])) {
            switch ($model->field["KUBUN"]) {
                case 1:
                    $dis_section = "";
                    $dis_faccd   = "disabled";
                    break;
                case 2:
                    $dis_section = "disabled";
                    $dis_faccd   = "";
                    break;
            }
        } else {
            $dis_section = "";
            $dis_faccd   = "disabled";
        }

        //所属選択コンボボックスを作成
        $query = knjb050Query::getSectQuery();
        $extra = "".$dis_section;
        makeCmb($objForm, $arg, $db, $query, "SECTION_CD_NAME1", $model->field["SECTION_CD_NAME1"], $extra, 1, "");

        $extra = "".$dis_section;
        makeCmb($objForm, $arg, $db, $query, "SECTION_CD_NAME2", $model->field["SECTION_CD_NAME2"], $extra, 1, "");

        //施設選択コンボボックスを作成
        $query = knjb050Query::getFacility();
        $extra = "".$dis_faccd;
        makeCmb($objForm, $arg, $db, $query, "FACCD_NAME1", $model->field["FACCD_NAME1"], $extra, 1, "");

        $extra = "".$dis_faccd;
        makeCmb($objForm, $arg, $db, $query, "FACCD_NAME2", $model->field["FACCD_NAME2"], $extra, 1, "");

        /**********/
        /* ラジオ */
        /**********/
        //出力項目(上段)
        $opt = array(1, 2); //1:科目名 2:講座名
        $model->field["SUBCLASS_CHAIR_DIV"] = ($model->field["SUBCLASS_CHAIR_DIV"] == "") ? "1" : $model->field["SUBCLASS_CHAIR_DIV"];
        $extra = array("id=\"SUBCLASS_CHAIR_DIV1\"", "id=\"SUBCLASS_CHAIR_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "SUBCLASS_CHAIR_DIV", $model->field["SUBCLASS_CHAIR_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJB050");
        knjCreateHidden($objForm, "cmd");

        //年度
        $arg["data"]["YEAR"] = $model->control["年度"];

        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        //JavaScriptで参照するため
        knjCreateHidden($objForm, "GAKUSEKI", $model->control["学籍処理日"]);
        knjCreateHidden($objForm, "T_YEAR");
        knjCreateHidden($objForm, "T_BSCSEQ");
        knjCreateHidden($objForm, "T_SEMESTER");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb050Form1.html", $arg); 
    }

}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "TITLE") {
            $opt[] = array('label' => $row["SEMESTERNAME"]." Seq".$row["BSCSEQ"].":".$row["TITLE"],
                           'value' => $row["YEAR"].",".$row["BSCSEQ"].",".$row["SEMESTER"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if ($value === $row["VALUE"]) $value_flg = true;
    }

    if (($name == "SECTION_CD_NAME2") || ($name == "FACCD_NAME2")) {
        $value = ($value != "" && $value_flg) ? $value : $opt[get_count($opt)-1]["value"];
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
