<?php
class knji100c_03Form1 {

    function main(&$model) {

        $objForm      = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knji100c_03index.php", "", "edit");

        $db = Query::dbCheckOut();

/********************************************************************************/
/********************************************************************************/
/*******        *****************************************************************/
/******* 左半分 *****************************************************************/
/*******        *****************************************************************/
/********************************************************************************/
/********************************************************************************/
        //年組のコンボボックス
        $opt = array();
        $value = $model->grade_hr_class;
        $value_flg = false;
        $query = knji100c_03Query::getAuth();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('edit')\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $value, $opt, $extra, 1);
        $model->grade_hr_class = $value;

        $schregno = array();
        if (isset($model->selectdata_l)){
            $schregno = explode(",", $model->selectdata_l);
        }

        //対象外の生徒取得
        $opt_idou = array();
        $result = $db->query(knji100c_03Query::getSchnoIdou($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();

        //リストtoリスト右
        $opt_right = array();
        $result = $db->query(knji100c_03Query::getStudent_right($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array(substr($row["VALUE"],9), $schregno)) {
                $idou = (in_array(substr($row["VALUE"],9), $opt_idou)) ? "●" : "　";
                $opt_right[] = array('label' => $row["HR_NAME"].$idou.$row["ATTENDNO"].'番'.$idou.$row["NAME_SHOW"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //リストtoリスト左
        $opt_left = array();
        if ($model->selectdata_l) {
            $result = $db->query(knji100c_03Query::getStudent_left($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $idou = (in_array(substr($row["VALUE"],9), $opt_idou)) ? "●" : "　";
                $opt_left[] = array('label' => $row["HR_NAME"].$idou.$row["ATTENDNO"].'番'.$idou.$row["NAME_SHOW"], 'value' => $row["VALUE"]);
            }
            $result->free();
        }

        //出力対象生徒一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "left_select_l",
                            "size"        => "30",
                            "value"       => "",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:180px\" WIDTH=\"180\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_left));
        //生徒一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "right_select_l",
                            "size"        => "30",
                            "value"       => "",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:180px\" WIDTH=\"180\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_right));

        //全て追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all_l",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"moves('left');\"" ) );
        //追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_l",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"move1('left');\"" ) );
        //削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_l",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"move1('right');\"" ) );
        //全て削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all_l",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"moves('right');\"" ) ); 

/********************************************************************************/
/********************************************************************************/
/*******        *****************************************************************/
/******* 右半分 *****************************************************************/
/*******        *****************************************************************/
/********************************************************************************/
/********************************************************************************/

        //サブシステムコンボボックス
        $opt = array();
        $value = $model->subsystem;
        $value_flg = false;
        $opt[] = array('label' => "", 'value' => "");
        $query = knji100c_03Query::getSubSystem();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $requestroot = REQUESTROOT;
        $extra = " onChange=\"Page_jumper('{$requestroot}');\"";
        $arg["data"]["SUBSYSTEM"] = knjCreateCombo($objForm, "SUBSYSTEM", $value, $opt, $extra, 1);
        $model->subsystem = $value;

        //対象月コンボボックス
        makeMonthSemeCmb($objForm, $arg, $db, $model);

        $opt_left = $opt_right = $item = array();
        if (isset($model->selectdata_r)){
            $item = explode(",", $model->selectdata_r);
        }

        //書出し項目一覧
        if($model->selectdata_r){
            foreach($item as $key){
                $val = $model->item[$key];
                $opt_left[] = array("label" => $val, "value" => $key);
            }
        }

        //項目一覧
        foreach($model->item as $key => $val){
            if (!in_array($key, $item)){
                $opt_right[] = array("label" => $val,"value" => $key);
            }
        }

        //書き出し項目一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "left_select_r",
                            "size"        => "30",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:180\" WIDTH=\"180\" ondblclick=\"move('right','left_select_r','right_select_r')\" ",
                            "options"     => $opt_left));
        //項目一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "right_select_r",
                            "size"        => "30",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:180\" WIDTH=\"180\" ondblclick=\"move('left','left_select_r','right_select_r')\" ",
                            "options"     => $opt_right));
        //全て追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all_r",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','left_select_r','right_select_r');\"" ) );
        //追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_r",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','left_select_r','right_select_r');\"" ) );
        //削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_r",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','left_select_r','right_select_r');\"" ) );
        //全て削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all_r",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','left_select_r','right_select_r');\"" ) ); 

/**********************************************************************************/
/**********************************************************************************/
/*******          *****************************************************************/
/******* ここまで *****************************************************************/
/*******          *****************************************************************/
/**********************************************************************************/
/**********************************************************************************/
        $arg["main_part"] = array( "LEFT_PART_L"   => $objForm->ge("left_select_l"),
                                   "RIGHT_PART_L"  => $objForm->ge("right_select_l"),
                                   "SEL_ADD_ALL_L" => $objForm->ge("sel_add_all_l"),
                                   "SEL_ADD_L"     => $objForm->ge("sel_add_l"),
                                   "SEL_DEL_L"     => $objForm->ge("sel_del_l"),
                                   "SEL_DEL_ALL_L" => $objForm->ge("sel_del_all_l"),

                                   "LEFT_PART_R"   => $objForm->ge("left_select_r"),
                                   "RIGHT_PART_R"  => $objForm->ge("right_select_r"),
                                   "SEL_ADD_ALL_R" => $objForm->ge("sel_add_all_r"),
                                   "SEL_ADD_R"     => $objForm->ge("sel_add_r"),
                                   "SEL_DEL_R"     => $objForm->ge("sel_del_r"),
                                   "SEL_DEL_ALL_R" => $objForm->ge("sel_del_all_r"));

        //出力設定ラジオボタン作成
        $output = array(OUT_CODE_NAME, OUT_CODE_ONLY, OUT_NAME_ONLY);
        $model->output = ($model->output== "") ? OUT_CODE_NAME : $model->output;
        foreach($output as $key => $val){
            $name = "RADIO".($key+1);
            $objForm->ae( array("type"       => "radio",
                                "name"       => "OUTPUT",
                                "value"      => $model->output,
                                "extrahtml"  => "id=".$name ) );

            $arg[$name] = $objForm->ge("OUTPUT", $val);
        }

        //保存ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_csv",
                            "value"       => "ＣＳＶ書出し",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_CSV"    =>$objForm->ge("btn_csv"),
                               "BTN_END"    =>$objForm->ge("btn_end"));

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata_r");
        knjCreateHidden($objForm, "selectdata_l");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "SCHREGNO");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knji100c_03Form1.html", $arg);
    }
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knji100c_03Query::getSemeMonth();
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month  = array();
    $opt_month[] = array("label" => "", "value" => "");

    for ($dcnt = 0; $dcnt < count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $getdata = $db->getRow(knji100c_03Query::getMonthName($month), DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
            }
        }
    }
    if($model->month == "" || $model->month == NULL){
        $model->month = "";
    }

    $extra = "onchange=\"return btn_submit('edit')\"";
    $arg["data"]["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->month, $opt_month, $extra, 1);

    $rtnMonth = "";
    for ($i = 0; $i < count($opt_month); $i++) {
        $rtnMonth = ($opt_month[$i]["value"] == $model->month) ? $opt_month[$i]["label"] : $rtnMonth;
    }
    knjCreateHidden($objForm, "MONTH_NAME", $rtnMonth);

    return;
}
?>