<?php

require_once('for_php7.php');

class knjs520form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjs520index.php", "", "main");

        $db = Query::dbCheckOut();

        //年度コンボボックス
        $optNendo = array();
        $query = knjs520Query::selectYearQuery();
        $result = $db->query($query);
        
        //年度コンボ対象配列
        $optNendoKouho = array();
        if ($model->field["YEAR"])  {
            $optNendoKouho[$model->field["YEAR"]] = "";
        }
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optNendoKouho[$row1["VALUE"]] = "";
        }
        $result->free();

        // 休業名 (名称マスタS001)
        $optS001 = array(
                   "VACATION_NAME01" => "春休み（始業式前）",
                   "VACATION_NAME02" => "夏休み",
                   "VACATION_NAME03" => "秋休み",
                   "VACATION_NAME04" => "冬休み",
                   "VACATION_NAME05" => "春休み（終了式後）"
                  );
        $query = knjs520Query::getNameMst("S001");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["LABEL"]) {
                $optS001["VACATION_NAME".$row["VALUE"]] = $row["LABEL"];
            }
        }
        $result->free();
        $arg["data"]["VACATION_NAME01"] = $optS001["VACATION_NAME01"];
        $arg["data"]["VACATION_NAME02"] = $optS001["VACATION_NAME02"];
        $arg["data"]["VACATION_NAME03"] = $optS001["VACATION_NAME03"];
        $arg["data"]["VACATION_NAME04"] = $optS001["VACATION_NAME04"];
        $arg["data"]["VACATION_NAME05"] = $optS001["VACATION_NAME05"];

        //降順に並び替えし、コンボへ追加して表示する
        krsort($optNendoKouho);
        
        foreach ($optNendoKouho as $key => $val) {
            $optNendo[] = array('label' => $key,
                                'value' => $key);
        }
        
        if (get_count($optNendo) > 0) {
            foreach ($optNendo as $key => $val) {
                $ctrlFlg = ($val["value"] == CTRL_YEAR) ? true : $ctrlFlg;
            }
            if (strlen($model->field["YEAR"]) == 0 && $ctrlFlg) {
                $Data = CTRL_YEAR;
            } else if (strlen($model->field["YEAR"]) == 0 && !$ctrlFlg) {
                $Data = $optNendo[0]["value"];
            }
        }

        $model->field["YEAR"] = (strlen($model->field["YEAR"]) == 0) ? $Data : $model->field["YEAR"];
        $extra = "onchange=\"return btn_submit('main');\"";
        $setYear = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $optNendo, $extra, 1);
        
        //textbox
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $setYearAdd = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

        //button
        $extra = "onclick=\"return add('year_add');\"";
        $setYearBtn = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        $arg["YEAR"] = array( "VAL" => $setYear."&nbsp;&nbsp;".
                                       $setYearAdd."&nbsp;".$setYearBtn);

        //学校区分
        $query = knjs520Query::getSchoolkind($model);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row2["LABEL"],
                           'value' => $row2["VALUE"]);
        }
        $model->field["SCHOOL_KIND"] = ($model->field["SCHOOL_KIND"]) ? $model->field["SCHOOL_KIND"] : $opt[0]["value"];
        
        
        $extra = "onchange=\"return btn_submit('main');\"";
        $arg["data"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $opt, $extra, 1);

        //休日情報取得
        if (!isset($model->warning) && $model->field["YEAR"] != "" && $model->field["SCHOOL_KIND"] != "") {
            $query = knjs520Query::getHolidayBaseMst($model->field["YEAR"], $model->field["SCHOOL_KIND"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }
        
        //休日のチェックボックス
        $query = knjs520Query::getCountdata($model->field["SCHOOL_KIND"],$model->field["YEAR"]);
        $getCount = $db->getOne($query);
        
        //法定休日
        $model->field["SCHOOL_KIND"] = ($model->field["SCHOOL_KIND"]) ? $model->field["SCHOOL_KIND"] : $opt[0]["value"];
        $extra = (strlen($row["LEGAL_HOLIDAY_FLG"]) || $getCount == 0) ?  "checked " : "";
        $arg["data"]["LEGAL_HOLIDAY_FLG"] = knjCreateCheckBox($objForm, "LEGAL_HOLIDAY_FLG", "1", $extra);

        //第１土曜日
        $checked = strlen($row["FIRST_SATURDAY_FLG"]) ? "checked" : "";
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "FIRST_SATURDAY_FLG",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["FIRST_SATURDAY_FLG"] = $objForm->ge("FIRST_SATURDAY_FLG");
        
        //第２土曜日
        $extra = (strlen($row["SECOND_SATURDAY_FLG"]) || $getCount == 0) ? " checked " : "";
        $arg["data"]["SECOND_SATURDAY_FLG"] = knjCreateCheckBox($objForm, "SECOND_SATURDAY_FLG", "1", $extra);
        
        //第３土曜日
        $checked = strlen($row["THIRD_SATURDAY_FLG"]) ? "checked" : "";
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "THIRD_SATURDAY_FLG",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["THIRD_SATURDAY_FLG"] = $objForm->ge("THIRD_SATURDAY_FLG");

        //第４土曜日
        $extra = (strlen($row["FOUR_SATURDAY_FLG"]) || $getCount == 0) ? " checked " : "";
        $arg["data"]["FOUR_SATURDAY_FLG"] = knjCreateCheckBox($objForm, "FOUR_SATURDAY_FLG", "1", $extra);

        //第５土曜日
        $checked = strlen($row["FIVE_SATURDAY_FLG"]) ? "checked" : "";
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "FIVE_SATURDAY_FLG",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["FIVE_SATURDAY_FLG"] = $objForm->ge("FIVE_SATURDAY_FLG");
        
        //休業期間
        //春休み（始業前）
        $arg["data"]["BEFORE_SPRING_VACATION_SDATE"] = View::popUpCalendar($objForm, "BEFORE_SPRING_VACATION_SDATE",
                                                                            str_replace("-","/",$row["BEFORE_SPRING_VACATION_SDATE"]),"");
        $arg["data"]["BEFORE_SPRING_VACATION_EDATE"] = View::popUpCalendar($objForm, "BEFORE_SPRING_VACATION_EDATE",
                                                                            str_replace("-","/",$row["BEFORE_SPRING_VACATION_EDATE"]),"");
        //夏休み
        $arg["data"]["SUMMER_VACATION_SDATE"] = View::popUpCalendar($objForm, "SUMMER_VACATION_SDATE",
                                                                            str_replace("-","/",$row["SUMMER_VACATION_SDATE"]),"");
        $arg["data"]["SUMMER_VACATION_EDATE"] = View::popUpCalendar($objForm, "SUMMER_VACATION_EDATE",
                                                                            str_replace("-","/",$row["SUMMER_VACATION_EDATE"]),"");
        //秋休み
        //学期制を取得し、2学期の場合のみ表示
        $query = knjs520Query::getSemesterdiv($model->field["YEAR"]);
        $Semesterdiv = $db->getOne($query);
        if ($Semesterdiv == "2") {
            //画面上のテキストボックス表示用
            $arg["test"] = "1";
            
            $arg["data"]["AUTUMN_VACATION_SDATE"] = View::popUpCalendar($objForm, "AUTUMN_VACATION_SDATE",
                                                                                str_replace("-","/",$row["AUTUMN_VACATION_SDATE"]),"");
            $arg["data"]["AUTUMN_VACATION_EDATE"] = View::popUpCalendar($objForm, "AUTUMN_VACATION_EDATE",
                                                                                str_replace("-","/",$row["AUTUMN_VACATION_EDATE"]),"");
        }
        //冬休み
        $arg["data"]["WINTER_VACATION_SDATE"] = View::popUpCalendar($objForm, "WINTER_VACATION_SDATE",
                                                                            str_replace("-","/",$row["WINTER_VACATION_SDATE"]),"");
        $arg["data"]["WINTER_VACATION_EDATE"] = View::popUpCalendar($objForm, "WINTER_VACATION_EDATE",
                                                                            str_replace("-","/",$row["WINTER_VACATION_EDATE"]),"");
        //春休み（終了式後）
        $arg["data"]["AFTER_SPRING_VACATION_SDATE"] = View::popUpCalendar($objForm, "AFTER_SPRING_VACATION_SDATE",
                                                                            str_replace("-","/",$row["AFTER_SPRING_VACATION_SDATE"]),"");
        $arg["data"]["AFTER_SPRING_VACATION_EDATE"] = View::popUpCalendar($objForm, "AFTER_SPRING_VACATION_EDATE",
                                                                            str_replace("-","/",$row["AFTER_SPRING_VACATION_EDATE"]),"");
        
        //行事予定登録処理、EVENT_MSTデータ件数確認
        //EVENT_MSTにデータが存在するかを確認
        $query = knjs520Query::getEventcount($model, $model->field["SCHOOL_KIND"], $model->field["YEAR"]);
        $getEvent = $db->getOne($query);
        knjCreateHidden($objForm, "GETEVENT", $getEvent);
        
        
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);
        
        //行事予定登録ボタン
        $extra = "onclick=\"return btn_submit('yotei');\"";
        $arg["button"]["btn_yotei"] = knjCreateBtn($objForm, 'btn_yotei', '行事予定反映', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //Hidden作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );  

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);
        Query::dbCheckIn($db);        
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs520Form1.html", $arg);
    }
}

?>
