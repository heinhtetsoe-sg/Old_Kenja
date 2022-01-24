<?php

require_once('for_php7.php');

class knjz202Form1
{   
    function main(&$model)
    {
        $objForm        = new form;
        $db = Query::dbCheckOut();
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz202index.php", "", "main");
        $arg["jscript"] = "";
        $arg["Closing"] = "";
        
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        //事前処理チェック
        if (!knjz202Query::ChecktoStart($db)) {
            $arg["Closing"] = " closing_window(2);";
        }

        //処理年度
        $opt_year = array();
        $result = $db->query(knjz202Query::getYear());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_year[] = array("label" => $row["YEAR"]."年度",
                                "value" => $row["YEAR"]);
        }

        //初期値は処理年度
        if ($model->year == "") $model->year = CTRL_YEAR;

        $objForm->ae( array("type"      => "select",
                            "name"      => "year",
                            "value"     => $model->year,
                            "options"   => $opt_year,
                            "extrahtml" => "onChange=\"btn_submit('chg_year');\""));
                                            
        $arg["data"]["YEAR"] = $objForm->ge("year");

        //上限値算定日付
        if ($model->date == "") $model->date = CTRL_DATE;//初期値は学籍処理日
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "date", str_replace("-","/",$model->date));

        //学校マスタの情報を取得。
        $knjSchoolMst = knjz202Query::getSchoolMst($db, $model->year, $model);
        //欠課数上限値（実授業数）
        $dis_absence = "off";
        if ($knjSchoolMst["JUGYOU_JISU_FLG"] == "2") {
            $dis_absence = "on";
            //上限値算定日付
            $query = knjz202Query::getAppointedDate($model->year, "2"); // 1:年間、2:随時
            $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["data"]["APPOINTED_DATE"] = str_replace("-","/",$rtnRow["APPOINTED_DATE"]);
            $arg["data"]["UPDATED"] = str_replace("-","/",$rtnRow["UPDATED"]);
        }
        //事前処理チェック
        if ($dis_absence == "off") {
            $arg["Closing3"] = " closing_window(3);";
        }

        /********************/
        /* チェックボックス */
        /********************/
        $extra = ($model->field["ABSENCE_WARN_CHECK"] == "on") ? "checked='checked' " : "";
        $arg["data"]["ABSENCE_WARN_CHECK"] = knjCreateCheckBox($objForm, "ABSENCE_WARN_CHECK", "on", $extra);

        $extra = ($model->field["ABSENCE_WARN_CHECK2"] == "on") ? "checked='checked' " : "";
        $arg["data"]["ABSENCE_WARN_CHECK2"] = knjCreateCheckBox($objForm, "ABSENCE_WARN_CHECK2", "on", $extra);

        $extra = ($model->field["ABSENCE_WARN_CHECK3"] == "on") ? "checked='checked' " : "";
        $arg["data"]["ABSENCE_WARN_CHECK3"] = knjCreateCheckBox($objForm, "ABSENCE_WARN_CHECK3", "on", $extra);

        /************/
        /* 固定文字 */
        /************/
        //欠課数オーバーのタイトル
        if (in_array("1", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN"]  = $model->control["学期名"]["1"];
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN2"] = $model->control["学期名"]["2"];
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN3"] = $model->control["学期名"]["3"];
        }
        //欠課数オーバーの前警告
        $query = knjz202Query::getNameMst("C042", "01"); // 1:回、1以外:週間
        $namespare1 = $db->getOne($query);
        $arg["data"]["ABSENCE_WARN_KAI"] = ($namespare1 == "1") ? "回" : "週間";

        /********************/
        /* テキストボックス */
        /********************/
        //欠課数オーバ
        if (in_array("1", $model->control["SEMESTER"])) {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ABSENCE_WARN"] = knjCreateTextBox($objForm, $model->field["ABSENCE_WARN"], "ABSENCE_WARN", 2, 2, $extra);
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ABSENCE_WARN2"] = knjCreateTextBox($objForm, $model->field["ABSENCE_WARN2"], "ABSENCE_WARN2", 2, 2, $extra);
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ABSENCE_WARN3"] = knjCreateTextBox($objForm, $model->field["ABSENCE_WARN3"], "ABSENCE_WARN3", 2, 2, $extra);
        }


        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('execute');\"" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_cancel",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"] = array("BTN_OK"     => $objForm->ge("btn_ok"),
                               "BTN_CLEAR"  => $objForm->ge("btn_cancel") );  

        //HIDDEN
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //実授業数の設定チェック
        $objForm->ae( array("type"      => "hidden",
                            "value"     => $dis_absence,
                            "name"      => "dis_absence") );

        //既に欠課数オーバーが登録されているかチェックする
        $query = knjz202Query::countGetAbsenceWarn($model);
        $cnt = $db->getOne($query);
        $query = knjz202Query::countGetAbsenceWarnSpecial($model);
        $cnt_special = $db->getOne($query);
        if ($cnt > 0 || $cnt_special > 0) {
            $exists_flg = 'aru';
        } else {
            $exists_flg = 'nai';
        }
        knjCreateHidden($objForm, "ABSENCE_WARN_EXISTS_FLG", $exists_flg);


        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz202Form1.html", $arg); 
    }
}
?>
