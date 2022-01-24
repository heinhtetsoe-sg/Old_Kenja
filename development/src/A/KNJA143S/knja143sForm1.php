<?php

require_once('for_php7.php');

// kanji=漢字

class knja143sForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja143sForm1", "POST", "knja143sindex.php", "", "knja143sForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //印刷ラジオボタン 1:表 2:裏
        $opt_data = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //1:個人,2:クラス表示指定
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knja143s')\"", "id=\"DISP2\" onClick=\"return btn_submit('knja143s')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["DISP"] == 1) {
            $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
            $arg["data"]["TITLE_RIGHT"] = "生徒一覧";
        }
        if ($model->field["DISP"] == 2) {
            $arg["data"]["TITLE_LEFT"]  = "出力対象クラス";
            $arg["data"]["TITLE_RIGHT"] = "クラス一覧";
        }

        //年度・学期
        $ctrl_year      = CTRL_YEAR;
        $ctrl_semester  = CTRL_SEMESTER;

        $query = knja143sQuery::getSemeMst($ctrl_year, $ctrl_semester);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //発行日
        if (!isset($model->field["PRINT_DATE"])) {
            $model->field["PRINT_DATE"] = str_replace("-","/",CTRL_DATE);
        }
        $arg["data"]["PRINT_DATE"] = View::popUpCalendar($objForm, "PRINT_DATE", $model->field["PRINT_DATE"]);

        //提出期限
        $arg["data"]["LIMIT_DATE"] = View::popUpCalendar($objForm, "LIMIT_DATE", $model->field["LIMIT_DATE"]);

        //クラス一覧リスト
        $row1 = array();
        $class_flg = false;
        $query = knja143sQuery::getAuth($model, $ctrl_year, $ctrl_semester);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }

        //1:個人表示指定用
        $opt_left = array();
        if ($model->field["DISP"] == 1) {
            if ($model->field["GRADE_HR_CLASS"]=="" || !$class_flg) {
                $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            }
            $extra = "onChange=\"return btn_submit('change_class');\"";
            $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

            $row1 = array();
            //生徒単位
            $selectleft = ($model->selectleft != "") ? explode(",", $model->selectleft) : array();
            $selectleftval = ($model->selectleftval != "") ? explode(",", $model->selectleftval) : array();
            $query = knja143sQuery::getSchno2($model,$ctrl_year,$ctrl_semester);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"], 
                                                             "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

                if ($model->cmd == 'change_class' ) {
                    if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
                        $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
                                        'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                    }
                } else {
                    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            }
            //左リストで選択されたものを再セット
            if ($model->cmd == 'change_class' ) {
                for ($i = 0; $i < get_count($selectleft); $i++) {
                    $opt_left[] = array("label" => $selectleftval[$i],
                                        "value" => $selectleft[$i]);
                }
            }
        }

        $result->free();

        $chdt = $model->field["DISP"];

        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left',$chdt)\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $row1, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right',$chdt)\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $chdt);\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left',$chdt);\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right',$chdt);\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left',$chdt);\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        makeBtn($objForm, $arg);

        makeHidden($objForm, $Row_Mst, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja143sForm1.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row_Mst, $model)
{
    knjCreateHidden($objForm, "YEAR", $Row_Mst["YEAR"]);
    knjCreateHidden($objForm, "SEMESTER", $Row_Mst["SEMESTER"]);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA143S");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "selectleftval");
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useFamilyDat" , $model->Properties["useFamilyDat"]);
}
?>
