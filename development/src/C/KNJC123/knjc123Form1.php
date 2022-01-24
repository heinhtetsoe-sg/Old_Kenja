<?php

require_once('for_php7.php');


class knjc123Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc123Form1", "POST", "knjc123index.php", "", "knjc123Form1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR,
                            ) );

        //学期テキストボックスの設定
        $arg["data"]["GAKKI"] = $model->control["学期名"][CTRL_SEMESTER];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => CTRL_SEMESTER,
                            ) );

        //学年コンボボックスを作成する
        $db = Query::dbCheckOut();
        $opt_grade=array();
        $query = knjc123Query::getSelectGrade();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grade[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        if($model->field["GRADE"]=="") $model->field["GRADE"] = $opt_grade[0]["value"];
        $opt_grade[]= array("label" => "-- 全て --", "value" => "99");
        
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE",
                            "size"       => 1,
                            "value"      => $model->field["GRADE"],
                            "options"    => $opt_grade ) );

        $arg["data"]["GRADE"] = $objForm->ge("GRADE");

        /**************/
        /*ラジオボタン*/
        /**************/
        
        //生徒リスト
        $opt = array(1, 2, 3);
        if (!$model->field["SHUBETU"]) $model->field["SHUBETU"] = 1;
        for ($i = 1; $i <= 3; $i++) {
            $name = "SHUBETU".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "SHUBETU",
                                "value"      => $model->field["SHUBETU"],
                                "multiple"   => $opt,
                                "extrahtml"  =>"onclick=\"Check('this');\" id=\"$name\"" ) );

            $arg["data"][$name] = $objForm->ge("SHUBETU",$i);
        }

        /******************/
        /*テキストボックス*/
        /******************/ 

        //共通のextra
        //初期値
        $extra = "disabled style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        //チェックボタン選択時
        if ($model->field["SHUBETU"] == 3) {
            $dis_check1 = ($model->field["SHUBETU"] == 3) ? "" : "disabled";
            $extra = $dis_check1."style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        }
        //出欠状況/遅刻
        $value = $model->field["SYUKKETU_SYUKKETU_TIKOKU"];
        $arg["data"]["SYUKKETU_SYUKKETU_TIKOKU"] = knjCreateTextBox($objForm, $value, "SYUKKETU_SYUKKETU_TIKOKU", 3, 3, $extra);
        //出欠状況/早退
        $value = $model->field["SYUKKETU_SYUKKETU_SOUTAI"];
        $arg["data"]["SYUKKETU_SYUKKETU_SOUTAI"] = knjCreateTextBox($objForm, $value, "SYUKKETU_SYUKKETU_SOUTAI", 3, 3, $extra);

        //カレンダーコントロール
        $model->field["SDATE"] = $model->field["SDATE"] == "" ? str_replace("-", "/", $model->control["学期開始日付"][9]) : $model->field["SDATE"];
        $model->field["EDATE"] = $model->field["EDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EDATE"];
        $arg["el"]["SDATE"] = $model->field["SDATE"];
        $arg["el"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"]);

        /***********/
        /*  ボタン */
        /***********/
        
        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );
        
        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJC123");
        knjCreateHidden($objForm, "COUNTFLG",      $model->testTable);
        knjCreateHidden($objForm, "CHK_SDATE",     $model->control["学期開始日付"][9]);
        knjCreateHidden($objForm, "CHK_EDATE",     $model->control["学期終了日付"][9]);
        knjCreateHidden($objForm, "SDATE",         $model->field["SDATE"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc123Form1.html", $arg); 
    }
}
?>
