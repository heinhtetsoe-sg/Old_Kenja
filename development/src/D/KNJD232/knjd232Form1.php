<?php

require_once('for_php7.php');

/********************************************************************/
/* 成績判定会議資料                                 山城 2005/08/16 */
/* 変更履歴                                                         */
/* ･NO001：出欠状況の回数指定のデフォルトを無しに   山城 2005/08/25 */
/* ･NO002：出力学期を指定可能にする                 山城 2005/10/06 */
/********************************************************************/

class knjd232Form1
{
    function main(&$model){

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd232Form1", "POST", "knjd232index.php", "", "knjd232Form1");

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

        //学期コンボボックスを作成する NO002
        $db = Query::dbCheckOut();
        $opt_seme = array();
        $query = knjd232Query::getSelectSeme();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_seme[] = array('label' => $row["SEMESTERNAME"],
                                'value' => $row["SEMESTER"]);
        }
        if($model->field["GAKKI2"]=="") $model->field["GAKKI2"] = CTRL_SEMESTER;

        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI2",
                            "size"       => 1,
                            "value"      => $model->field["GAKKI2"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd232');\"",
                            "options"    => $opt_seme ) );

        $arg["data"]["GAKKI2"] = $objForm->ge("GAKKI2");

        //学年コンボボックスを作成する
        $opt_schooldiv = "学年";

        $db = Query::dbCheckOut();
        $opt_grade=array();
        $query = knjd232Query::getSelectGrade($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grade[] = array('label' => $row["GRADE_NAME1"],
                                 'value' => $row["GRADE"]);
        }
        if($model->field["GRADE"]=="") $model->field["GRADE"] = $opt_grade[0]["value"];
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE",
                            "size"       => 1,
                            "value"      => $model->field["GRADE"],
                            "options"    => $opt_grade ) );

        $arg["data"]["GRADE"] = $objForm->ge("GRADE");

        /***********/
        /*  項目１ */
        /***********/
        //成績優良者チェックボックスを作成する
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT1",
                            "extrahtml" => "id=\"OUTPUT1\"",
                            "value"     => "on"));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT1");

        //評定平均（以上）テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "ASSESS1",
                            "size"       => "3",
                            "value"      => 4.3,
                            "extrahtml"  => "STYLE=\"text-align: right\" onBlur=\"return OverCheck(this);\"" ) );

        $arg["data"]["ASSESS1"] = $objForm->ge("ASSESS1");

        //詳細リストチェックボックスを作成する
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT8",
                            "extrahtml" => "id=\"OUTPUT8\"",
                            "value"     => "on"));

        $arg["data"]["OUTPUT8"] = $objForm->ge("OUTPUT8");

        /***********/
        /*  項目２ */
        /***********/
        //成績不振者チェックボックスを作成する
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT2",
                            "extrahtml" => "id=\"OUTPUT2\"",
                            "value"     => "on"));

        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT2");

        //教科・科目/総合的な時間
        if (!$model->field["KYOUKA_SOUGOU1"] && !$model->field["KYOUKA_SOUGOU2"]) {
            $model->field["KYOUKA_SOUGOU1"] = '1';
        }
        $extra = ($model->field["KYOUKA_SOUGOU1"] == "1") ? $extra = "checked='checked' id=\"KYOUKA_SOUGOU1\"" : "id=\"KYOUKA_SOUGOU1\"";
        $arg["data"]["KYOUKA_SOUGOU1"] = knjCreateCheckBox($objForm, "KYOUKA_SOUGOU1", "1", $extra);
        $extra = ($model->field["KYOUKA_SOUGOU2"] == "1") ? $extra = "checked='checked' id=\"KYOUKA_SOUGOU2\"" : "id=\"KYOUKA_SOUGOU2\"";
        $arg["data"]["KYOUKA_SOUGOU2"] = knjCreateCheckBox($objForm, "KYOUKA_SOUGOU2", "1", $extra);

        //成績不良評定テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "ASSESS2",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => isset($model->field["ASSESS2"])?$model->field["ASSESS2"]:"1") );

        $arg["data"]["ASSESS2"] = $objForm->ge("ASSESS2");

        //成績不良科目数テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "COUNT2",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => isset($model->field["COUNT2"])?$model->field["COUNT2"]:"1") );

        $arg["data"]["COUNT2"] = $objForm->ge("COUNT2");

        //成績不良未履修科目数テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "UNSTUDY2",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => isset($model->field["UNSTUDY2"])?$model->field["UNSTUDY2"]:"1") );

        $arg["data"]["UNSTUDY2"] = $objForm->ge("UNSTUDY2");

        //詳細リストチェックボックスを作成する
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT7",
                            "extrahtml" => "id=\"OUTPUT7\"",
                            "value"     => "on"));

        $arg["data"]["OUTPUT7"] = $objForm->ge("OUTPUT7");

        /***********/
        /*  項目３ */
        /***********/
        //皆勤者チェックボックスを作成する
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT3",
                            "extrahtml" => "id=\"OUTPUT3\"",
                            "value"     => "on"));

        $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");

        /***********/
        /*  項目４ */
        /***********/
        //出欠状況（優良）チェックボックスを作成する
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT4",
                            "extrahtml" => "id=\"OUTPUT4\"",
                            "value"     => "on"));

        $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT4");

        //遅刻数テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "LATE4",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["LATE4"]) );
//                          "value"      => isset($model->field["LATE4"])?$model->field["LATE4"]:"0") );

        $arg["data"]["LATE4"] = $objForm->ge("LATE4");

        //早退数テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "EARLY4",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["EARLY4"]) );
//                          "value"      => isset($model->field["EARLY4"])?$model->field["EARLY4"]:"0") );
        
        $arg["data"]["EARLY4"] = $objForm->ge("EARLY4");

        //欠席数テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "ABSENT4",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["ABSENT4"]) );
//                          "value"      => isset($model->field["ABSENT4"])?$model->field["ABSENT4"]:"0") );

        $arg["data"]["ABSENT4"] = $objForm->ge("ABSENT4");

        //欠課数テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "SUBCLASS_ABSENT4",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["SUBCLASS_ABSENT4"]) );
//                          "value"      => isset($model->field["SUBCLASS_ABSENT4"])?$model->field["SUBCLASS_ABSENT4"]:"0") );

        $arg["data"]["SUBCLASS_ABSENT4"] = $objForm->ge("SUBCLASS_ABSENT4");

        /***********/
        /*  項目５ */
        /***********/
        //出欠状況を作成する
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT5",
                            "extrahtml" => "id=\"OUTPUT5\"",
                            "value"     => "on"));

        $arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT5");

        //遅刻数テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "LATE5",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["LATE5"]) );
//                          "value"      => isset($model->field["LATE5"])?$model->field["LATE5"]:"1") );

        $arg["data"]["LATE5"] = $objForm->ge("LATE5");

        //遅刻ソート順テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "ORDER1",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["ORDER1"]) );

        $arg["data"]["ORDER1"] = $objForm->ge("ORDER1");

        //早退数テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "EARLY5",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["EARLY5"]) );
//                          "value"      => isset($model->field["EARLY5"])?$model->field["EARLY5"]:"1") );
        
        $arg["data"]["EARLY5"] = $objForm->ge("EARLY5");

        //早退ソート順テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "ORDER2",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["ORDER2"]) );

        $arg["data"]["ORDER2"] = $objForm->ge("ORDER2");

        //欠席数テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "ABSENT5",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["ABSENT5"]) );
//                          "value"      => isset($model->field["ABSENT5"])?$model->field["ABSENT5"]:"1") );

        $arg["data"]["ABSENT5"] = $objForm->ge("ABSENT5");

        //欠席ソート順テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "ORDER3",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["ORDER3"]) );

        $arg["data"]["ORDER3"] = $objForm->ge("ORDER3");

        //欠課数テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "SUBCLASS_ABSENT5",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["SUBCLASS_ABSENT5"]) );
//                          "value"      => isset($model->field["SUBCLASS_ABSENT5"])?$model->field["SUBCLASS_ABSENT5"]:"1") );

        $arg["data"]["SUBCLASS_ABSENT5"] = $objForm->ge("SUBCLASS_ABSENT5");

        //欠課ソート順テキストボックスを作成する
        $objForm->ae( array("type"       => "text",
                            "name"       => "ORDER4",
                            "size"       => "3",
                            "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"      => $model->field["ORDER4"]) );

        $arg["data"]["ORDER4"] = $objForm->ge("ORDER4");

        /***********/
        /*  項目６ */
        /***********/
        //出欠状況を作成する
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT6",
                            "extrahtml" => "id=\"OUTPUT6\"",
                            "value"     => "on"));

        $arg["data"]["OUTPUT6"] = $objForm->ge("OUTPUT6");

        /***********/
        /*  日付   */
        /***********/
        $arg["el"]["DATE"]=View::popUpCalendar($objForm,"DATE",isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);

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

        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD232"
                            ) );

        //学期開始日
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SDATE",
                            "value"     => $model->control["学期開始日付"][$model->field["GAKKI2"]]
                            ) );

        //学期終了日
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "EDATE",
                            "value"     => $model->control["学期終了日付"][$model->field["GAKKI2"]]
                            ) );

        //教育課程コード
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "useCurriculumcd",
                            "value"     => $model->Properties["useCurriculumcd"]
                            ) );

        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd232Form1.html", $arg); 
    }
}
?>
