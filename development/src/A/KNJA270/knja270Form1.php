<?php

require_once('for_php7.php');

class knja270Form1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja270Form1", "POST", "knja270index.php", "", "knja270Form1");

        //権限チェック
        if ($model->auth < DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knja270Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //出力種別ラジオボタンを作成する
        $opt = array(1, 2, 3, 4, 5);
        if($model->field["OUTPUT"]=="") $model->field["OUTPUT"] = "3";
        $extra = array("onclick=\"change(this);\" id=\"OUTPUT1\"", "onclick=\"change(this);\" id=\"OUTPUT2\"", "onclick=\"change(this);\" id=\"OUTPUT3\"", "onclick=\"change(this);\" id=\"OUTPUT4\"", "onclick=\"change(this);\" id=\"OUTPUT5\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //対象年度コンボボックスを作成する
        $query = knja270Query::getSelectYear();
        $extra = "onChange=\"return btn_submit('cmbchange');\"".$dis_year;
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);
        knjCreateHidden($objForm, "YEAR_HID", $model->control["年度"]);

        //学期コンボボックスを作成する
        $gakki_sdate = $gakki_fdate = $optSeme = array();
        $query = knja270Query::getSemesterMst($model->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $optSeme[] = array("label" => $row["SEMESTERNAME"], 
                               "value" => $row["SEMESTER"]);

            $gakki_sdate[$row["SEMESTER"]] = str_replace("-","/",$row["SDATE"]);
            $gakki_fdate[$row["SEMESTER"]] = str_replace("-","/",$row["EDATE"]);
        }

        if($model->field["GAKKI"]=="") $model->field["GAKKI"] = $model->control["学期"];
        if($model->field["OUTPUT"]==1 || $model->field["OUTPUT"]==2) $model->field["GAKKI"] = $model->date_gakki;

        if($model->field["OUTPUT"]==3 || $model->field["OUTPUT"]==4){
            $dis_gakki = "";
        } else {
            $dis_gakki = "disabled";
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->field["GAKKI"],
                            "extrahtml"  => "onChange=\"return btn_submit('cmbchange');\"".$dis_gakki,
                            "options"    => $optSeme));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


        //disabled設定
        if($model->field["OUTPUT"]==3 || $model->field["OUTPUT"]==4){
            $disp_onoff = "";
        }else {
            $disp_onoff = "disabled";
        }
        if($model->field["OUTDIV"] == "on" || $model->cmd == "" || $model->cmd == "cmbchange"){
            $check = "checked";
        }else {
            $check = "";
        }

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTDIV",
                            "value"     => "on",
                            "extrahtml" => $check.$disp_onoff." id=\"OUTDIV\"") );
        $arg["data"]["OUTDIV"] = $objForm->ge("OUTDIV");


        knjCreateHidden($objForm, "GAKKI_SDATE", $gakki_sdate[$model->field["GAKKI"]]); //学期開始日付の取得
        knjCreateHidden($objForm, "GAKKI_FDATE", $gakki_fdate[$model->field["GAKKI"]]); //学期終了日付の取得

//NO001==>
        // '1:毎 2:累計' 選択ラジオ ---> 科目別のみ選択可能
        // 1:毎 -----> 指定学期の集計
        // 2:累計 ---> 指定学期までの集計 ---> この場合のみ'出欠集計日付'が指定可能
        $opt_ruikei[0]=1;
        $opt_ruikei[1]=2;
        if($model->field["RUIKEI"]=="") $model->field["RUIKEI"] = 1;
        $dis_ruikei = ($model->field["OUTPUT"]==4) ? "" : " disabled";

        for ($i = 1; $i <= 2; $i++) {
            $name = "RUIKEI".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "RUIKEI",
                                "value"      => $model->field["RUIKEI"],
                                "extrahtml"  => "onclick=\"dis_ruikei(this);\"" .$dis_ruikei." id=\"$name\"",
                                "multiple"   => $opt_ruikei));

            $arg["data"][$name] = $objForm->ge("RUIKEI",$i);
        }


        //出欠集計日付 ---> '2:累計'の場合のみ指定可能
        if($model->field["ATTENDDATE"]=="") $model->field["ATTENDDATE"] = $gakki_fdate[$model->field["GAKKI"]];
        $dis_attenddate = ($model->field["OUTPUT"]==4 && $model->field["RUIKEI"]==2) ? "" : " disabled";
        $arg["data"]["ATTENDDATE"] = View::popUpCalendar2($objForm,"ATTENDDATE",$model->field["ATTENDDATE"],"","",$dis_attenddate);
//NO001<==


        //日付を任意に選択する

        if($model->field["OUTPUT"]==1 || $model->field["OUTPUT"]==2){
            $dis_date = "";
        } else {
            $dis_date = "disabled";
        }

        if($model->field["DATE"]=="") $model->field["DATE"] = $model->control["学期開始日付"][$model->control["学期"]];
        if($model->field["DATE2"]=="") $model->field["DATE2"] = $model->control["学期終了日付"][$model->control["学期"]];

        $arg["data"]["DATE"] = View::popUpCalendar2($objForm,"DATE",$model->field["DATE"],"reload=true","btn_submit('datechange')",$dis_date);
        $arg["data"]["DATE2"] = View::popUpCalendar2($objForm,"DATE2",$model->field["DATE2"],"reload=false","",$dis_date);
        knjCreateHidden($objForm, "DATE_SEME", $model->date_gakki); //学期の取得
        knjCreateHidden($objForm, "DATE_SEME2", $model->date_gakki2); //学期の取得

        //欠席者
        if($model->field["OUTPUT"]==5){
            $disParts5 = "";
        } else {
            $disParts5 = "disabled";
        }
        //日付
        $model->field["ABBDATE"] = ($model->field["ABBDATE"]) ? $model->field["ABBDATE"] : str_replace("-","/", CTRL_DATE);
        $arg["data"]["ABBDATE"] = View::popUpCalendar2($objForm, "ABBDATE", $model->field["ABBDATE"], "", "", $disParts5);
        //校時
        $query = knja270Query::getPeriodcd($model);
        $extra = "".$disParts5;
        makeCmb($objForm, $arg, $db, $query, "PERIODCD", $model->field["PERIODCD"], $extra, 1, "BLANK");


        //クラス一覧リスト作成する
        $kaishi_hid = $shuryo_hid = "";
        if($model->field["OUTPUT"]==1 || $model->field["OUTPUT"]==2){
            $select_gakki = $model->date_gakki;
            $select_date = str_replace("/","",$model->field["DATE"]);
            $query = knja270Query::getSemesterMst($model->field["YEAR"]);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $kaishi_hid = str_replace("-","",$row["SDATE"]);
                $shuryo_hid = str_replace("-","",$row["EDATE"]);
                if ($select_date >= $kaishi_hid && $shuryo_hid >= $select_date){
                    $select_gakki = $row["SEMESTER"] ;
                }
            }
        }
        if($model->field["OUTPUT"]==3 || $model->field["OUTPUT"]==4) $select_gakki = $model->field["GAKKI"];
        if($model->field["OUTPUT"] == "5") $select_gakki = CTRL_SEMESTER;
        $opt_class_left = $opt_class_right = array();
        $opt_class=array();
        $query = knja270Query::getAuth($model, $model->field["YEAR"],$select_gakki);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (!in_array($row["VALUE"], $model->select_data["selectdata"])){
                $opt_class_right[]= array('label' => $row["LABEL"],
                                          'value' => $row["VALUE"]);
            } else {
                $opt_class_left[]= array('label' => $row["LABEL"],
                                         'value' => $row["VALUE"]);
            }
        }
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => $opt_class_right));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


        //出力対象クラスリストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => $opt_class_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //実行ボタン
        $btnName = "ＣＳＶ出力";
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knja270Query::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            $btnName = "エクセル出力";
        } else {
            $extra = "onclick=\"return btn_submit('csv');\"";
        }
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", $btnName, $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "TODAY", date("Y/m/d"));
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA270");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "useVirus", $model->virus);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        //学期開始日付,学期終了日付の取得
        $kaishi_hid = $shuryo_hid = array();
        $seme_cnt = 0;
        $query = knja270Query::getSemesterMst($model->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $kaishi_hid[] = str_replace("-","",$row["SDATE"]);
            $shuryo_hid[] = str_replace("-","",$row["EDATE"]);
            $seme_cnt++;
        }
        $seme_s = implode($kaishi_hid, ",");
        $seme_e = implode($shuryo_hid, ",");
        knjCreateHidden($objForm, "SEME_S", $seme_s);
        knjCreateHidden($objForm, "SEME_E", $seme_e);
        //学期数の取得
        knjCreateHidden($objForm, "GAKKI_SUU", $seme_cnt);
        knjCreateHidden($objForm, "Radio_No", $model->field["OUTPUT"]);
        //教育課程用
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja270Form1.html", $arg); 
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
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
