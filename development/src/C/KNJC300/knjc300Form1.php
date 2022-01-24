<?php

require_once('for_php7.php');

class knjc300Form1
{
    function main(&$model) {
        
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc300index.php", "", "main");
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //echo "php:12=".$model->cmd."<BR>";
        //echo "php:13=".CTRL_DATE."<BR>";
        //echo "php:14=".$model->senddate."<BR>";
        //echo "現在はテストのため講座取得に使用しているのはログオン日付。本番はシステム日付？"."<BR>"."切り替える場合はQueryの198,199行目のコメントを入れ替えること。(php:line15)"."<BR>";
        
        //リセット時に保管日数とラジオボタンを初期値に戻す
        if($model->cmd == "reset"){
            $model->add_days = "1";
            $model->katei = "1";
            $model->joken = "1";
            $model->selectdata = "";
            $model->hmrm = "";
            $model->kamoku = "";
        }
        
        //ラジオ変更時に選択データとホームルームを初期値に戻す
        if($model->cmd == "changeRadio"){
            $model->selectdata = "";
            $model->hmrm = "";
            $model->field["C_KAMK"] = "";
            $model->field["C_KOZA"] = "";
            $model->field["C_CLII"] = "";
            $model->kamoku = "";
        }
        
        //コンボの選択データを初期値に戻す
        if($model->cmd == "combo"){
            $model->selectdata = "";
            $model->kamoku = "";
        }
        
        //発信者名
        if ($model->field["T_NAME"] == ""){
            $model->field["T_NAME"] = STAFFCD;
        }
        
        $extra = "";
        $query = knjc300Query::getStaff();
        $arg["data"]["T_NAME"] = makeCombo($objForm, $arg, $db, $query, $model->field["T_NAME"], "T_NAME", $extra, 1, "");
        
        //日数コンボ
        if ($model->field["NUM_DAYS"] == ""){
            $model->field["NUM_DAYS"] = $opt[0]["NUM_DAYS"];
        }
        
        //$extra = "onchange=\"return btn_submit('change');\"";
        //$query = knjc300Query::getDays();
        //$arg["data"]["NUM_DAYS"] = makeCombo($objForm, $arg, $db, $query, $model->field["NUM_DAYS"], "NUM_DAYS", $extra, 2, "");
        
        $result = $db->query(knjc300Query::getDays());
        $list = $result->fetchRow(DB_FETCHMODE_ASSOC);
        for ($i = $list["FROM"] ; $i <= $list["TO"] ;$i++){
            $row[] = array('label' => $i."日",
                           'value' => $i);
        }
        
        $result->free();
        
        //生徒一覧コンボ作成
        $extra = "onchange=\"return btn_submit('change');\"";
        $arg["data"]["NUM_DAYS"] = knjCreateCombo($objForm, "NUM_DAYS", $model->field["NUM_DAYS"], $row, $extra, 2);
        
        
        //今日
        //$today   = getdate();
        //$day     = $today["mday"];
        //$month   = $today["mon"];
        //$year    = $today["year"];
        //$date    = mktime (0, 0, 0, $month, $day, $year);
        //$model->senddate = date("Y-m-d", $date);    //登録した日付を保管
        $model->senddate = CTRL_DATE;
        
        //有効期限
        //$today   = getdate();
        //$day     = $today["mday"] + $model->add_days;
        //$month   = $today["mon"];
        //$year    = $today["year"];
        //$date    = mktime (0, 0, 0, $month, $day, $year);
        //$setDate = date("Y/m/d", $date);
        //$arg["data"]["TO_DATE"] = common::DateConv1($setDate,0);
        //$model->termdate = date("Y-m-d", $date);    //有効期限を保管
        list($year,$month,$day) = explode('-',CTRL_DATE);
        $day = $day + $model->add_days;
        $date = mktime (0, 0, 0, $month, $day, $year);
        $setDate = date("Y/m/d", $date);
        $arg["data"]["TO_DATE"] = common::DateConv1($setDate,0);
        $model->termdate = date("Y-m-d", $date);    //有効期限を保管
        
        //伝言内容
        $extra = "style=\"ime-mode:active\"; ";
        $arg["data"]["D_TEXT1"] = knjCreateTextBox($objForm, $model->field["D_TEXT1"], "D_TEXT1", 60, 30, "$extra");
        $arg["data"]["D_TEXT2"] = knjCreateTextBox($objForm, $model->field["D_TEXT2"], "D_TEXT2", 60, 30, "$extra");
        //$arg["data"]["D_TEXT1"] = createTextBox($objForm, $model->field["D_TEXT1"], "D_TEXT1", 60, 30, "$extra");
        //$arg["data"]["D_TEXT2"] = createTextBox($objForm, $model->field["D_TEXT2"], "D_TEXT2", 60, 30, "$extra");
        
        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        
        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTER;
        
        //ラジオ（課程）
        $katei = array(2, 1);
        $model->katei = $model->katei ? $model->katei : "1";
        $extraRadio = array("id=\"KATEI1\" onClick=\"btn_submit('changeRadio')\"", "id=\"KATEI2\" onClick=\"btn_submit('changeRadio')\"");
        $radioArray = knjCreateRadio($objForm, "KATEI", $model->katei, $extraRadio, $katei, get_count($katei));
        //$radioArray = createRadio($objForm, "KATEI", $model->katei, $extraRadio, $katei, get_count($katei));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //ラジオ（抽出条件）
        $joken = array(3, 2, 1);
        $model->joken = $model->joken ? $model->joken : "1";
        $extraRadio = array("id=\"JOKEN1\" onClick=\"btn_submit('changeRadio')\" ", "id=\"JOKEN2\" onClick=\"btn_submit('changeRadio')\" ", "id=\"JOKEN3\" onClick=\"btn_submit('changeRadio')\" ");
        $radioArray = knjCreateRadio($objForm, "JOKEN", $model->joken, $extraRadio, $joken, get_count($joken));
        //$radioArray = createRadio($objForm, "JOKEN", $model->joken, $extraRadio, $joken, get_count($joken));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //ＨＲクラスコンボ
        $extra = "onchange=\"return btn_submit('combo')\" ";
        $query = knjc300Query::getHRclass();
        $arg["data"]["C_HMRM"] = makeCombo($objForm, $arg, $db, $query, $model->field["C_HMRM"], "C_HMRM", $extra, 1, "BLANK");
        
        //科目／講座コンボ
        //科目
        $extra = "onchange=\"return btn_submit('combo')\" ";
        $query = knjc300Query::getKamoku($model);
        $arg["data"]["C_KAMK"] = makeCombo($objForm, $arg, $db, $query, $model->field["C_KAMK"], "C_KAMK", $extra, 1, "BLANK");
        //講座
        $subclasscd = $model->field["C_KAMK"];
        $extra = "onchange=\"return btn_submit('combo')\" ";
        $query = knjc300Query::getKouza($model, $subclasscd);
        $arg["data"]["C_KOZA"] = makeCombo($objForm, $arg, $db, $query, $model->field["C_KOZA"], "C_KOZA", $extra, 1, "BLANK");
        
        //科目選択時の講座のコードリスト作成
        $result = $db->query($query);
        $work = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($work == ""){
                $work = $row["VALUE"];
            } else {
                $work .= ",".$row["VALUE"];
            }
        }
        $model->kamoku = $work;
        
        //クラブ／委員会コンボ
        $extra = "onchange=\"return btn_submit('combo')\" ";
        $query = knjc300Query::getClubCommittee();
        $arg["data"]["C_CLII"] = makeCombo($objForm, $arg, $db, $query, $model->field["C_CLII"], "C_CLII", $extra, 1, "BLANK");
        
        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);
        
        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc300Form1.html", $arg); 
        
    }
}

//コンボ作成　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);
    
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    //return createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    
    //課程・ホームルーム
    $course = ($model->katei == "") ? "1" : $model->katei;
    $homeroom = ($model->hmrm == "") ? "" : $model->hmrm;
    
    //生徒一覧
    $row1 = array();
    $select = "0";
    if ($model->selectdata != ""){
        $select = "1";
    }
    
    $kamoku = "";
    if ($model->field["C_KAMK"] != ""){
        $kamoku = $model->field["C_KAMK"];
    }
    
    $kouza = "";
    if ($model->field["C_KOZA"] != ""){
        $kouza = $model->field["C_KOZA"];
    }
    
    $clco = "";
    if ($model->field["C_CLII"] != ""){
        $clco = $model->field["C_CLII"];
    }
    
    //$Row = $db->getOne(knjc300Query::getSelectCourse());
    //$course = ($model->field["COURSE"] == "") ? $Row : $model->field["COURSE"];
    
    $result = $db->query(knjc300Query::getStudent($course,$select,$homeroom,$kamoku,$kouza,$clco,$model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    
    //生徒一覧コンボ作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);
    //$arg["data"]["CATEGORY_NAME"] = createCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);
    //$query = knjc300Query::getStudent($course);
    //$arg["data"]["CATEGORY_NAME"] = makeCombo($objForm, $arg, $db, $query, $hoge, "CATEGORY_NAME", $extra, 20, "");
    
    
    //出力対象作成
    $row2 = array();
    $select = "2";
    if ($model->selectdata != ""){
        //$Row = $db->getOne(knjc300Query::getSelectCourse());
        $course = ($model->katei == "") ? "1" : $model->katei;
        $result = $db->query(knjc300Query::getStudent($course,$select,$homeroom,$kamoku,$kouza,$clco,$model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row2[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
    }
    
    //出力対象コンボ作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED1"] = knjCreateCombo($objForm, "CATEGORY_SELECTED1", "", $row2, $extra, 20);
    //$arg["data"]["CATEGORY_SELECTED1"] = createCombo($objForm, "CATEGORY_SELECTED1", "", $row2, $extra, 20);
    
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //$arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);
    // > ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //$arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);
    // < ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //$arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //$arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //確定ボタン
    $extra =  "onclick=\"return btn_submit('insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_reset", "　　　　　　　　　　　　確　定　　　　　　　　　　　　", $extra);
    //$arg["button"]["btn_insert"] = createBtn($objForm, "btn_reset", "　　　　　　　　　　　　確　定　　　　　　　　　　　　", $extra);
    //取消ボタン
    $extra =  "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取　消", $extra);
    //$arg["button"]["btn_reset"] = createBtn($objForm, "btn_reset", "取　消", $extra);
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終　了", "onclick=\"closeWin();\"");
    //$arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終　了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    knjCreateHidden($objForm, "cmd");
    //createHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    //createHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "CATEGORY_SELECTED", explode(",", $model->selectdata));
    //createHidden($objForm, "CATEGORY_SELECTED", explode(",", $model->selectdata));
}

?>
