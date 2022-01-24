<?php

require_once('for_php7.php');

class knjl070eForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl070eQuery::getNameMst($model->ObjYear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試区分
        $query = knjl070eQuery::getNameMst($model->ObjYear, "L004");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //音楽フラグ
        $model->musicFlg = false;
        $model->nameCdL004Row = $db->getRow(knjl070eQuery::getNameMst($model->ObjYear, "L004", $model->testdiv), DB_FETCHMODE_ASSOC);
        if ($model->nameCdL004Row["NAMESPARE3"] == "2") $model->musicFlg  = true;
        if ($model->musicFlg) {
            $arg['MUSIC'] = '1';
            $arg['TOTAL_WIDTH'] = '1280';
        } else {
            $arg['MUSIC'] = '';
            $arg['TOTAL_WIDTH'] = '1080';
        }

        //合格区分
        $query = knjl070eQuery::getNameMst($model->ObjYear, "L045");
        $extra = "onChange=\"return btn_submit('main')\"";
        $setL045 = makeCmb($objForm, $arg, $db, $query, "L045", $val, $extra, 1, "BLANK", "retOpt");

        //合格コース
        $query = knjl070eQuery::getNameMst($model->ObjYear, "L058");
        $extra = "onChange=\"return btn_submit('main')\"";
        $setL058 = makeCmb($objForm, $arg, $db, $query, "L058", $val, $extra, 1, "BLANK", "retOpt");

        //合否
        $query = knjl070eQuery::getNameMst($model->ObjYear, "L013");
        $extra = "onChange=\"return btn_submit('main')\"";
        $setL013 = makeCmb($objForm, $arg, $db, $query, "L013", $val, $extra, 1, "BLANK", "retOpt");

        //スライドグループ
        $slideDesiredivArr = array();
        $slideTestdiv1Arr = array();
        $query = knjl070eQuery::getSlide($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dt = $row['DESIREDIV'].'-'.$row['TESTDIV1'];
            if ($slideTestdiv1Arr[$dt] == '') {
                $slideTestdiv1Arr[$dt] = array();
            }
            if (!in_array($row['SUC_TESTDIV1'], $slideTestdiv1Arr[$dt])) {
                $slideTestdiv1Arr[$dt][] = $row['SUC_DESIREDIV'];
            }
            $slideDesiredivArr[$row['DESIREDIV'].'-'.$row['TESTDIV1'].'-'.$row['SUC_DESIREDIV']][] = $row['SUC_TESTDIV1'];
        }

        $setL058Obj = array();
        $slideJs = "var slideJs = {};\n";
        $slideJs .= "slideJs[\"TESTDIV1\"] = {};\n";
        $checkSlide = array();
        foreach ($slideTestdiv1Arr as $key => $arr) {
            $s = "[";
            $comma = "";
            foreach ($arr as $cd) {
                if (isset($checkSlide[$key][$cd])) {
                    continue;
                }
                $setL058Obj[$key][0] = array('label' => '', 'value' => '');
                $checkSlide[$key][$cd] = true;
                $name = "";
                foreach ($setL058 as $row) {
                    if ($row["value"] == $cd) {
                        $name = $row["label"];
                        break;
                    }
                }
                $setL058Obj[$key][] = array('label' => $name, 'value' => $cd);
                $s .= $comma. "{value: \"".$cd."\", name: \"".$name."\" }";
                $comma = ", ";
            }
            $s .= "]";
            $slideJs .= "slideJs[\"TESTDIV1\"][\"".$key."\"] = ".$s.";\n";
        }

        $setL045Obj = array();
        $slideJs .= "slideJs[\"DESIREDIV\"] = {};\n";
        foreach ($slideDesiredivArr as $key => $arr) {
            $s = "[";
            $comma = "";
            foreach ($arr as $cd) {
                $setL045Obj[$key][0] = array('label' => '', 'value' => '');
                $name = "";
                foreach ($setL045 as $row) {
                    if ($row["value"] == $cd) {
                        $name = $row["label"];
                        break;
                    }
                }
                $setL045Obj[$key][] = array('label' => $name, 'value' => $cd);
                $s .= $comma. "{value: \"".$cd."\", name: \"".$name."\" }";
                $comma = ", ";
            }
            $s .= "]";
            $slideJs .= "slideJs[\"DESIREDIV\"][\"".$key."\"] = ".$s.";\n";
        }
        $arg["slideJs"] = $slideJs;

        //一覧表示
        $model->examnoArray = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl070eQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $examNo = $row["EXAMNO"];

                //HIDDENに保持する用
                $model->examnoArray[] = $row["EXAMNO"];
                //disabeled
                $setDisabled = $row["JUDGEDIV"] == '3' ? " disabled " : "";
                $setDisabled2 = "";
                if ($model->nameCdL004Row["NAMESPARE1"] == '1') {
                    if ($row["JUDGEDIV"] != '3') {
                        $setDisabled2 =  " disabled ";
                    }
                } else {
                    if ($row["JUDGEDIV"] == '1') {
                        $setDisabled2 =  " disabled ";
                    }
                }
                $setDisabled3 = $model->nameCdL004Row["NAMESPARE1"] == '1' && $row["JUDGEDIV"] != '3' ? " disabled " : "";

                //合否(RECEPT)
                $extra = "onchange=\"changeJudgeDiv(this, '".$examNo."');\"";
                $row["JUDGEDIV"] = knjCreateCombo($objForm, "JUDGEDIV_{$examNo}", $row["JUDGEDIV"], $setL013, $extra, 1);

                //合格コース
                $setLL058 = $setL058;
                if (is_array($slideDesiredivArr[$row['DESIREDIV'].'-'.$row['TESTDIV1'].'-'.$valRemark4])) {
                    foreach ($setLL058 as $key => $val) {
                        if (in_array($val["value"], $slideDesiredivArr[$row['DESIREDIV'].'-'.$row['TESTDIV1'].'-'.$valRemark4]) || $val["value"] == '') {
                        } else {
                            unset($setLL058[$key]);
                        }
                    }
                }
                knjCreateHidden($objForm, "HID_TESTDIV1_{$examNo}", $row["TESTDIV1"]);
                knjCreateHidden($objForm, "HID_DESIREDIV_{$examNo}", $row["DESIREDIV"]);
                knjCreateHidden($objForm, "HID_REMARK5_{$examNo}", $row["REMARK5"]);
                $defRemark5 = $row["REMARK5"];
                $extra = "onchange=changeScore(this);";
                $setOpt = isset($setL058Obj[$row['DESIREDIV'].'-'.$row['TESTDIV1']]) ? $setL058Obj[$row['DESIREDIV'].'-'.$row['TESTDIV1']] : $setLL058;
                $row["REMARK5"] = knjCreateCombo($objForm, "REMARK5_{$examNo}", $row["REMARK5"], $setOpt, $extra.$setDisabled.$setDisabled2, 1);

                //合格区分
                $setLL045 = $setL045;
                if (is_array($slideTestdiv1Arr[$row['DESIREDIV'].'-'.$row['TESTDIV1']])) {
                    foreach ($setLL045 as $key => $val) {
                        if (in_array($val["value"], $slideTestdiv1Arr[$row['DESIREDIV'].'-'.$row['TESTDIV1']]) || $val["value"] == '') {
                        } else {
                            unset($setLL045[$key]);
                        }
                    }
                }
                $valRemark4 = $row["REMARK4"];
                knjCreateHidden($objForm, "HID_REMARK4_{$examNo}", $valRemark4);
                $extra = "onchange=changeScore(this);";
                $setNotOpt = $defRemark5 ? $setLL045 : array();
                $setOpt = isset($setL045Obj[$row['DESIREDIV'].'-'.$row['TESTDIV1'].'-'.$defRemark5]) ? $setL045Obj[$row['DESIREDIV'].'-'.$row['TESTDIV1'].'-'.$defRemark5] : $setNotOpt;
                $row["REMARK4"] = knjCreateCombo($objForm, "REMARK4_{$examNo}", $valRemark4, $setOpt, $extra.$setDisabled, 1);

                //出願専攻
                $opt = array();
                $value_flg = false;
                $opt[] = array('label' => '', 'value' => '');
                if ($row["HOPE1"] != '') $opt[] = array('label' => '1:'.$row["HOPE1"], 'value' => '1');
                if ($row["HOPE2"] != '') $opt[] = array('label' => '2:'.$row["HOPE2"], 'value' => '2');
                $opt[] = array('label' => '9:声楽', 'value' => '9');
                $extra = "onchange=changeScore(this);";
                $row["HOPE"] = knjCreateCombo($objForm, "HOPE_{$examNo}", $row["HOPE"], $opt, $extra, 1);

                //合否(BASE)
                knjCreateHidden($objForm, "HID_JUDGEMENT2_{$examNo}", $row["JUDGEMENT"]);
                $extra = "onchange=changeScore(this);";
                $row["JUDGEMENT2"] = knjCreateCombo($objForm, "JUDGEMENT2_{$examNo}", $row["JUDGEMENT"], $setL013, $extra.$setDisabled.$setDisabled2, 1);

                $row["TESTDIV1"] = optToName($setL045, $row["TESTDIV1"]);

                $dataflg = true;

                $arg["data"][] = $row;
            }

        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model);
        knjCreateHidden($objForm, "HID_L004NMSP1", $model->nameCdL004Row["NAMESPARE1"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl070eindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjl070eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", $retDiv="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "L045" || $name == "L058" || $name == "L013") {
            $row["LABEL"] = $row["NAME1"];
        }
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $opt;
    }
}

function optToName($opt,$val){
    for($i=0;$i<get_count($opt);$i++){
        if($opt[$i]['value']==$val){
            return $opt[$i]['label'];
        }
    }
    return '';
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg) {
    $disable  = ($dataflg) ? "" : " disabled";

    //全て合格ボタン
    $extra = "onclick=\"return interviewAllOk();\"".$disable;
    $arg["button"]["btn_allok"] = knjCreateBtn($objForm, "btn_allok", "全て合格", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CHANGE_SCORE");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL070E");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
}
?>
