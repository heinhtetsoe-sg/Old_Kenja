<?php

require_once('for_php7.php');

class knjd129fForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd129findex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd129fQuery::getSemester();
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ作成
        $query = knjd129fQuery::getGradeHrclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);

        //学校校種を取得
        $model->schoolkind = $db->getOne(knjd129fQuery::getSchoolKind($model));

        //科目(名称マスタD061)作成
        $query = knjd129fQuery::getSubclass($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //入力選択ラジオボタン 1:値選択 2:データクリア
        $opt_nyuryoku = array(1, 2);
        $model->nyuryoku = ($model->nyuryoku == "") ? "1" : $model->nyuryoku;
        $extra = array("id=\"NYURYOKU1\" onClick=\"myHidden()\"", "id=\"NYURYOKU2\" onClick=\"myHidden()\"");
        $radioArray = knjCreateRadio($objForm, "NYURYOKU", $model->nyuryoku, $extra, $opt_nyuryoku, get_count($opt_nyuryoku));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //入力値選択ラジオボタン
        $query = knjd129fQuery::getNameMst('D060');
        $result = $db->query($query);
        $kantenArray = array();
        $kantenCnt = 1;
        $opt_data = array();
        $extra = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $kantenArray[$kantenCnt]["VAL"] = $row["VALUE"];
            $kantenArray[$kantenCnt]["LABEL"] = $row["LABEL"];
            $kantenArray[$kantenCnt]["SHOW"] = $row["VALUE"] . ":" . $row["LABEL"];
            $opt_data[] = $kantenCnt;
            $extra[] = "id=\"TYPE_DIV{$kantenCnt}\"";
            $arg["TYPE_SHOW{$kantenCnt}"] = $kantenArray[$kantenCnt]["SHOW"];
            $kantenCnt++;
        }
        $result->free();
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //特別活動評価の値を取得    
        //$optValue = array();
        $count = 0;
        $query = knjd129fQuery::getNameMst('D060');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //hidden
            knjCreateHidden($objForm, "NAMECD2-".$count, $row["VALUE"]);
            knjCreateHidden($objForm, "NAME1-".$count, $row["LABEL"]);
            $count++;
            /*$optValue[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);*/
        }
        $result->free();
        knjCreateHidden($objForm, "D060_COUNTER", $count);

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $result = $db->query(knjd129fQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //フィールド名+カウントをセット
            $setName = "VALUE-".$counter;
        
            //学籍番号をHiddenで保持
            knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);

            //出席番号
            if ($row["ATTENDNO"] != ""){
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            
            //評価
            //$extra  = " onPaste=\"return showPaste(this);\"";
            //$extra .= " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\" onChange=\"valueCheck(this.value, '".$setName."');\"";
            $extra = "STYLE=\"text-align: center\" readonly=\"readonly\" onClick=\"kirikae(this, 'VALUE-".$counter."')\" oncontextmenu=\"kirikae2(this, 'VALUE-".$counter."')\"; ";
            $value = (!isset($model->warning)) ? $row["VALUE"] : $model->fields["VALUE"][$counter];

            knjCreateHidden($objForm, "VALUE"."_FORM_ID"."-".$counter, $value);
            //評価名セット
            if ($value) {
                $value_name = $db->getOne($query = knjd129fQuery::getNameMst('D060', $value));
            } else {
                $value_name = "";
            }
            $row["VALUE"] = knjCreateTextBox($objForm, $value_name, "VALUE-".$counter, 4, 4, $extra);
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;

        }
        //件数
        knjCreateHidden($objForm, "COUNTER", $counter);

        $dataArray = array();
        foreach ($kantenArray as $key => $val) {
            $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$key."')\"",
                                 "NAME" => $val["SHOW"]);
        }

        $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
        $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
        foreach ($dataArray as $key => $val) {
            $setData["CLICK_NAME"] = $val["NAME"];
            $setData["CLICK_VAL"] = $val["VAL"];
            $arg["menu"][] = $setData;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJD129F");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        //入力用
        $hiddenSetVal = "";
        $hiddenSetShow = "";
        $hiddenSetCheck = "";
        $sep = "";
        foreach ($kantenArray as $key => $val) {
//            $hiddenSetVal .= $sep.$key;
            $hiddenSetVal .= $sep.$val["VAL"];
            $hiddenSetShow .= $sep.$val["LABEL"];
            $sep = ",";
        }
        knjCreateHidden($objForm, "SETVAL", $hiddenSetVal);
        knjCreateHidden($objForm, "SETSHOW", $hiddenSetShow);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd129fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
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

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {
    //更新ボタンを作成する
    if ($model->callPrgid != "" && $model->auth != "") {
        $disabled = ($model->auth > DEF_REFER_RESTRICT) ? "" : " disabled";
    } else {
        $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    }
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
