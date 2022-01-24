<?php

require_once('for_php7.php');


class knjd061tForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        $db = Query::dbCheckOut();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd061tForm1", "POST", "knjd061tindex.php", "", "knjd061tForm1");

        $opt=array();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        if (is_numeric($model->control["学期数"])){
            //年度,学期コンボの設定
            for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ){
                $opt[]= array("label" => $model->control["学期名"][$i+1],
                              "value" => sprintf("%d", $i+1)
                             );
            }
            if (!$model->field["SEMESTER"]) $model->field["SEMESTER"] = CTRL_SEMESTER;
            $seme = $model->field["SEMESTER"];
        }
        //学年末の場合、$semeを今学期にする。
        if( isset($seme) ){
            $seme  = ($seme == 9) ? CTRL_SEMESTER : $seme;
            $sseme = $model->control["学期開始日付"][$seme];
            $eseme = $model->control["学期終了日付"][$seme];
            $semeflg = ($seme == 9) ? CTRL_SEMESTER : $seme;
        }

        $opt[]= array("label" => $model->control["学期名"][9],
                       "value" => sprintf("%d", 9)
                      );
        $value = isset($model->field["SEMESTER"]) ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $extra = "onchange=\"return btn_submit('knjd061t');\"";
        makeCmb($objForm, $arg, $db, null, "SEMESTER", $value, $extra, "1", $opt, $model);

        //学年コンボボックスを作成する
        $query = knjd061tQuery::getSelectGrade($model);
        $extra = "onChange=\"return btn_submit('knjd061t');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, "1", null, $model);

        //テスト種別リスト
        $query = knjd061tQuery::getTest($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "", "1", null, $model);

        //素点・評価ラジオボタン
        $opt_record = array(1, 2);
        $model->field["RECORD_DIV"] = ($model->field["RECORD_DIV"] == "") ? "1" : $model->field["RECORD_DIV"];
        $extra = array("id=\"RECORD_DIV1\"", "id=\"RECORD_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "RECORD_DIV", $model->field["RECORD_DIV"], $extra, $opt_record, get_count($opt_record));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;



        //クラス一覧リスト
        makeClassList($objForm, $arg, $db, $model);

        //単位保留チェックボックスを作成する
        $extra = ($model->field["OUTPUT4"]=="1") ? "checked" : "";
        $arg["data"]["OUTPUT4"] = knjCreateCheckBox($objForm, "OUTPUT4", isset($model->field["OUTPUT4"])?$model->field["OUTPUT4"]:1, $extra);

        //不振の基準評定
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $extraRight = " STYLE=\"text-align: right\"";
        $arg["data"]["OUTPUT5"] = knjCreateTextBox($objForm, $model->field["HYOTEI"] ? $model->field["HYOTEI"] : '1', "HYOTEI", 2, 2, $extra.$extraRight);

        //評定平均
        $extra = "onblur=\"this.value=toFloat(this.value)\";";
        $extraRight = " STYLE=\"text-align: right\"";
        $arg["data"]["ASSESS"] = knjCreateTextBox($objForm, $model->field["ASSESS"] ? $model->field["ASSESS"] : '4.3', "ASSESS", 3, 3, $extra.$extraRight);

        //出欠集計範囲ラジオボタン 1:累計 2:学期
        $opt_attend = array(1, 2);
        $model->field["ATTEND"] = ($model->field["ATTEND"] == "") ? "1" : $model->field["ATTEND"];
        $click = " onclick=\"return btn_submit('knjd061t');\"";
        $extra = array("id=\"ATTEND1\"".$click, "id=\"ATTEND2\"".$click);
        $radioArray = knjCreateRadio($objForm, "ATTEND", $model->field["ATTEND"], $extra, $opt_attend, get_count($opt_attend));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計日付（開始日付）
        if($model->field["ATTEND"]){
            $model->field["SDATE"] = ($model->field["ATTEND"] == '1') ? $model->control["学期開始日付"][9] : $model->control["学期開始日付"][$model->field["SEMESTER"]];
        } else {
            $model->field["SDATE"] = $model->control["学期開始日付"][9];
        }
        $arg["el"]["SDATE"] = $model->field["SDATE"];

        //出欠集計日付（終了日付）
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $value);

        //印刷ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $seme, $sseme, $eseme, $semeflg, $model->testTable, CTRL_YEAR, $model);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd061tForm1.html", $arg);
    }
}



//クラス一覧リスト作成
function makeClassList(&$objForm, &$arg, $db, &$model)
{
    $row1 = array();
    $query = knjd061tQuery::getAuth($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 20);

    //出力対象教科リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);

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
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $opt = null, $model) {
    if ($opt == null) {
        $opt = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
    }

    if ($name == "GRADE") {
        $opt = array();
                $result = $db->query($query);
                $grade_flg = true;
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                    if( $value==$row["VALUE"] ) $grade_flg = false;
                }
                if( $grade_flg ) $value = $opt[0]["value"];
                $result->free();
    }

    if ($name == "TESTKINDCD") {
        $opt = array();
        if ($model->field["SEMESTER"] == "9" && $model->testTable == "TESTITEM_MST_COUNTFLG") {
            $opt[] = array('label' => '0000  評価成績', 'value' => '0');
        } else {
            $result = $db->query($query);
            $test_flg = true;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
                if( $model->field["TESTKINDCD"] == $row["VALUE"] ) $test_flg = false;
            }
            if ($model->testTable == "TESTITEM_MST_COUNTFLG") {
                $opt[] = array('label' => '0000  評価成績', 'value' => '0');
                $test_flg = false;
            }

            if($test_flg) $model->field["TESTKINDCD"] = $opt[0]["value"];
            $result->free();
        }
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}


function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, 'btn_print', 'プレビュー／印刷', $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);
}

function makeHidden(&$objForm, $seme, $sseme, $eseme, $semeflg, $testTable, $control_year, $model) {
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD061T");
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "SEME_DATE", $seme);               //学期
        knjCreateHidden($objForm, "SEME_SDATE", $sseme);             //学期開始日付
        knjCreateHidden($objForm, "SEME_EDATE", $eseme);             //学期終了日付
        knjCreateHidden($objForm, "SEME_FLG", $semeflg);             //学期
        knjCreateHidden($objForm, "COUNTFLG", $testTable);           //テスト種別
        knjCreateHidden($objForm, "YEAR", $control_year);            //年度
        knjCreateHidden($objForm, "SDATE", $model->field["SDATE"]);  //出欠集計範囲（開始日付）
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
}
?>
