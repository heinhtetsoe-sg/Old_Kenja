<?php

require_once('for_php7.php');

class knjz333Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }


        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjz333index.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //サブ基本メニュー名
        if (!isset($model->warning) && $model->cmd != "change_menu" && $model->cmd != "main") {
            //メニュー名
            $query = knjz333Query::getMenuName($model);
            $model->field["MENUNAME"] = $db->getOne($query);
        }
        $extra = "";
        $arg["MENUNAME"] = knjCreateTextBox($objForm, $model->field["MENUNAME"], "MENUNAME", 30, 30, $extra);

        //サブ基本メニューIDコンボ
        $extra = "onchange=\"return btn_submit('subMain')\"";
        $array1 = range("A", "Z");
        foreach($array1 as $key1 => $val1){
            //登録されているタイトルを取得したい
            //データがあるかカウント
            $cntQuery = knjz333Query::getCnt($val1, "0000");
            $cnt = $db->getOne($cntQuery);
            
            if($cnt > 0){
                $titleQuery = knjz333Query::getTitle($val1, "0000");
                $titleRow = $db->getRow($titleQuery, DB_FETCHMODE_ASSOC);
                $menuarray[$val1] = $titleRow["SUBID"]." : ".$titleRow["SUBNAME"];
            }else{
                $menuarray[$val1] = "S".$val1."0000 : 未使用";
            }
        }
        
        makeCombo($objForm, $arg, $menuarray, $model->field["MENUID"], "MENUID", $extra, 1);

        //サブメニュー名
        if (!isset($model->warning) && $model->cmd != "change_menu" && $model->cmd != "main") {
            $query = knjz333Query::getSubMenuName($model);
            $model->field["SUB_MENUNAME"] = $db->getOne($query);
        }
        $extra = "";
        $arg["SUB_MENUNAME"] = knjCreateTextBox($objForm, $model->field["SUB_MENUNAME"], "SUB_MENUNAME", 30, 30, $extra);

        //個人サブメニューコンボ
        $extra = "onchange=\"return btn_submit('subMain')\"";
        $array2 = range(1000, 9000, 1000);
        $arg["ID"] = "S".$model->field["MENUID"];
        //サブタイトルを取得したい
        foreach($array2 as $key2 => $val2){
            //データがあるかカウント
            $cntQuery = knjz333Query::getCnt($model->field["MENUID"], $val2);
            $cnt = $db->getOne($cntQuery);
            
            if($cnt > 0){
                //タイトル取得
                $titleQuery = knjz333Query::getTitle($model->field["MENUID"], $val2);
                $titleRow = $db->getRow($titleQuery, DB_FETCHMODE_ASSOC);
                $subarray[$val2] = $titleRow["SUBID"]." : ".$titleRow["SUBNAME"];
            }else{
                $subarray[$val2] = "S".$model->field["MENUID"].$val2." : 未使用";
            }
        }
        
        makeCombo($objForm, $arg, $subarray, $model->field["SUB_MENUID"], "SUB_MENUID", $extra, 1);

        //タイトル名
        $extra = "";
        $arg["TITLE_NAME"] = knjCreateTextBox($objForm, $model->field["TITLE_NAME"], "TITLE_NAME", 30, 30, $extra);
        
        //処理月　配列の中身は1～12
        if (!isset($model->warning) && $model->cmd != "change_menu" && $model->cmd != "main") {
            $query = knjz333Query::getSyoriMonth($model);
            $model->field["SYORI_MONTH"] = $db->getOne($query);
            $model->field["SYORI_MONTH"] = $model->field["SYORI_MONTH"];
            
        }
        for($i = 0; $i<14; $i++){
            if($i == 0){
                $id[0] = "上部固定";
            }else if($i == 13){
                $id[99] = "下部固定";
            }else{
                $id[$i] = $i;
            }
        }
        makeCombo($objForm, $arg, $id, $model->field["SYORI_MONTH"], "SYORI_MONTH", $extra, 1, "SYORI");

        //非表示FLG
        //DB確認
        if($model->cmd == "subMain"){
            $query = knjz333Query::getHideFlg($model);
            $model->field["HIDE_FLG"] = $db->getOne($query);
        }
        if ($model->field["HIDE_FLG"] != "") {
            $check = " checked";
        }else{
            $check = "";
        }
        $extra = $check;
        $extra .= " onclick = \"chgLBL(this); return btn_submit('main');\" ";
        $arg["HIDE_FLG"] = knjCreateCheckBox($objForm, "HIDE_FLG", $model->field["HIDE_FLG"], $extra);
    

        //初期化ボタン　MENU_SUB_DEFAULT_MSTにRootがなかったら使用不可にする
        $query = knjz333Query::getDefaultMst($model, $model->field["MENUID"]);
        $cnt = $db->getOne($query);

        $extra = "onclick=\"return btn_submit('format');\"";
        if($cnt != 1){
            $extra .= " disabled ";
        }
        $arg["button"]["btn_format"] = knjCreateBtn($objForm, "btn_format", "初期化", $extra);



    //右側
        //基本メニューコンボ
        $query = knjz333Query::getBaseMenuCmb($model);
        $extra = "onchange=\"return btn_submit('change_menu')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SELECT_MENUNAME"], "SELECT_MENUNAME", $extra, 1, "SORT");

        //サブメニューコンボ
        $query = knjz333Query::getSubMenuCmb($model);
        $extra = "onchange=\"return btn_submit('change_menu')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SELECT_SUB_MENUID"], "SELECT_SUB_MENUID", $extra, 1);

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz333Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "SORT") {
        $opt[] = array("label" => "並び替え",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　
function makeCombo(&$objForm, &$arg, $array, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    if ($blank == "SYORI") {
    }


    foreach($array as $key => $val){
        //$val = str_pad($val,2,"0",STR_PAD_LEFT);
        $opt[] = array ("label" => $val,
                        "value" => $key);
        if ($value == $key) $value_flg = true;
    }

    //$value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model) {
    //左リスト(溜める式)
    $selectdata      = ($model->selectdata != "")       ? explode(",", $model->selectdata)      : array();
    $selectdataLabel = ($model->selectdataLabel != "")  ? explode(",", $model->selectdataLabel) : array();

    //左リスト
    $opt_left = array();
    if ($model->cmd == 'change_menu' || $model->cmd == 'main') {
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt_left[] = array("label" => $selectdataLabel[$i],
                                "value" => $selectdata[$i]);
        }
    } else {
        $result = $db->query(knjz333Query::getStaffMenuCmb($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
        }
        $result->free();
    }

    //右リスト
    $opt_right = array();
    $result = $db->query(knjz333Query::getListMenuCmb($model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[]= array("label" => $row["LABEL"],
                            "value" => $row["VALUE"]);
    }
    $result->free();

    //生徒一覧リスト(右)
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"moveStudent('left', 0)\"";
    $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "RIGHT_PART", "", $opt_right, $extra, 35);

    //対象者一覧リスト(左)
    $setSort = $model->field["SELECT_MENUNAME"] ? "1" : "0";
    $extra = "id=LEFT_LIST multiple style=\"width:100%\" width:\"100%\" ondblclick=\"moveStudent('right', $setSort)\"";
    $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "LEFT_PART", "", $opt_left, $extra, 35);

    //対象選択ボタン
    $extra = "onclick=\"return moveStudent('sel_add_all', 0);\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //対象選択ボタン
    $extra = "onclick=\"return moveStudent('left', 0);\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //対象取消ボタン
    $extra = "onclick=\"return moveStudent('right', $setSort);\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタン
    $extra = "onclick=\"return moveStudent('sel_del_all', $setSort);\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

    //対象選択ボタン
    $extra = "onclick=\"return moveUpDown('LEFT_LIST', 'up');\"";
    $arg["main_part"]["SEL_UP"] = knjCreateBtn($objForm, "SEL_UP", "△", $extra);
    //対象選択ボタン
    $extra = "onclick=\"return moveUpDown('LEFT_LIST', 'down');\"";
    $arg["main_part"]["SEL_DOWN"] = knjCreateBtn($objForm, "SEL_DOWN", "▽", $extra);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //タイトル
    $extra = "onclick=\"return titleAdd();\"";
    $arg["TITLE_BTN"] = knjCreateBtn($objForm, "TITLE_BTN", "タイトル追加", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataLabel");
}
?>
