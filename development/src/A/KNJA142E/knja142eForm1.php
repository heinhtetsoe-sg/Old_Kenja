<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knja142eForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja142eForm1", "POST", "knja142eindex.php", "", "knja142eForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        
        //学期コンボ
        $query = knja142eQuery::getSemeMst();
        $extra = "onChange=\"return btn_submit('knja142e');\"";
        makeCmb($objForm, $arg, $db, $query, "GAKKI", $model->field["GAKKI"], $extra, 1);

        //校種コンボ
        $query = knja142eQuery::getSchkind($model);
        $extra = "onChange=\"return btn_submit('knja142e');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //表示指定ラジオボタン 1:クラス 2:個人
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knja142e')\"", "id=\"DISP2\" onClick=\"return btn_submit('knja142e')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年組
        $query = knja142eQuery::getAuth(CTRL_YEAR, $model->field["GAKKI"], $model->field["SCHOOL_KIND"]);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //発行日付
        if($model->schoolName == "mieken"){
            $arg["SDATE"] = 1;
            if( !isset($model->field["SDATE"]) ) 
                $model->field["SDATE"] = str_replace("-","/",CTRL_DATE);
            $arg["data"]["SDATE"]=View::popUpCalendar($objForm,"SDATE",$model->field["SDATE"]);
        }

        //有効期限  
        if( !isset($model->field["EDATE"]) ) 
            $model->field["EDATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["EDATE"]=" ".View::popUpCalendar($objForm,"EDATE",$model->field["EDATE"]);

        //出力帳票ラジオ 1:表面印刷 2:裏面印刷
        $opt_disp = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //顔写真を表示
        $extra = "checked id=\"PRINT_IMAGE\"";
        $arg["data"]["PRINT_IMAGE"] = knjCreateCheckBox($objForm, "PRINT_IMAGE", "on", $extra, "");
        
        //その他(送付先)
        if ($model->field["DISP"] == 2) {
            $arg["SEND_ADDR"] = 1;
            $extra  = "id=\"SEND_ADDR\"";
            $extra .= ($model->field["SEND_ADDR"] == "on") ? " checked " : "";
            $arg["data"]["SEND_ADDR"] = knjCreateCheckBox($objForm, "SEND_ADDR", "on", $extra, "");
        }

        //出力枚数テキストボックス
        $objForm->ae( array("type"        => "text",
                            "name"        => "MAISUU",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "style=\"text-align: right;\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => isset($model->field["MAISUU"])?$model->field["MAISUU"]:1 ));
        $arg["data"]["MAISUU"] = $objForm->ge("MAISUU");

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja142eForm1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA142E");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "selectleftval");
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "SCHOOLNAME" , $model->schoolName);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array ("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "GAKKI") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //表示切替
    if ($model->field["DISP"] == 2) {
        $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
        $arg["data"]["TITLE_RIGHT"] = "生徒一覧";
        $arg["GRADE_HR_CLASS"] = 1;
    } else {
        $arg["data"]["TITLE_LEFT"]  = "出力対象クラス";
        $arg["data"]["TITLE_RIGHT"] = "クラス一覧";
    }

    //初期化
    $opt_left = $opt_right =array();

    //年組取得
    if ($model->field["DISP"] == 1) {
        $query = knja142eQuery::getAuth(CTRL_YEAR, $model->field["GAKKI"], $model->field["SCHOOL_KIND"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //一覧リスト（右側）
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();
    }

    //個人指定
    if ($model->field["DISP"] == 2) {
        $selectleft = ($model->selectleft != "") ? explode(",", $model->selectleft) : array();
        $selectleftval = ($model->selectleftval != "") ? explode(",", $model->selectleftval) : array();

        //生徒取得
        $query = knja142eQuery::getSchno($model, CTRL_YEAR, $model->field["GAKKI"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //クラス名称調整
            $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
            $len = $zenkaku * 2 + $hankaku;
            $hr_name = $row["LABEL"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

            if ($model->cmd == 'change') {
                if (!in_array($row["VALUE"], $selectleft)) {
                    $opt_right[] = array('label' => $row["LABEL"],
                                         'value' => $row["VALUE"]);
                }
            } else {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //左リストで選択されたものを再セット
        if ($model->cmd == 'change') {
            for ($i = 0; $i < get_count($selectleft); $i++) {
                $opt_left[] = array("label" => $selectleftval[$i],
                                    "value" => $selectleft[$i]);
            }
        }
    }

    $disp = $model->field["DISP"];

    //一覧リスト（右）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left', $disp)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', $disp)\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $disp);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $disp);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $disp);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $disp);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

?>
