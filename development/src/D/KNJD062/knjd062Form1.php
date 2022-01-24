<?php

require_once('for_php7.php');


class knjd062Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd062Form1", "POST", "knjd062index.php", "", "knjd062Form1");

        $opt=array();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //学期コンボボックスを作成する
        if (is_numeric($model->control["学期数"])){
            //年度,学期コンボの設定
            for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ){
                $opt[]= array("label" => $model->control["学期名"][$i+1], 
                              "value" => sprintf("%d", $i+1)
                             );            
            }
            if (!$model->field["SEMESTER"]) $model->field["SEMESTER"] = $model->control["学期"];
            $seme = $model->field["SEMESTER"];
        }
        //学年末の場合、$semeを今学期にする。
        if( isset($seme) ){
            switch ($seme) {
                case 9:
                    $seme  = $model->control["学期"];
                    $sseme = $model->control["学期開始日付"][9];
                    $eseme = $model->control["学期終了日付"][9];
                    $semeflg = $model->control["学期"];
                    break;
                case 1:
                    $sseme = $model->control["学期開始日付"][1];
                    $eseme = $model->control["学期終了日付"][1];
                    $semeflg = 1;
                    break;
                case 2:
                    $sseme = $model->control["学期開始日付"][2];
                    $eseme = $model->control["学期終了日付"][2];
                    $semeflg = 2;
                    break;
                case 3:
                    $sseme = $model->control["学期開始日付"][3];
                    $eseme = $model->control["学期終了日付"][3];
                    $semeflg = 3;
                    break;
                default:
                    $this->warning = "学期データが不正です。";
                    return false;
            }
        }

        $opt[]= array("label" => $model->control["学期名"][9], 
                       "value" => sprintf("%d", 9)
                      );

        $objForm->ae( array("type"      => "select",
                            "name"      => "SEMESTER",
                            "size"      => "1",
                            "value"     => isset($model->field["SEMESTER"])?$model->field["SEMESTER"]:$model->control["学期"],
                            "extrahtml" => "onchange=\"return btn_submit('gakki');\"",    
                            "options"   => $opt));

        $arg["data"]["SEMESTER"] = $objForm->ge("SEMESTER");

        //学年コンボボックスを作成する
        $opt_schooldiv = "学年";

        $db = Query::dbCheckOut();
        $opt_grade=array();
        $query = knjd062Query::getSelectGrade($model);
        $result = $db->query($query);
        $grade_flg = true;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $grade_show= sprintf("%d",$row["GRADE"]);
            $opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
                                 'value' => $row["GRADE"]);
            if( $model->field["GRADE"]==$row["GRADE"] ) $grade_flg = false;
        }
        if( $grade_flg ) $model->field["GRADE"] = $opt_grade[0]["value"];
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "select",
                            "name"      => "GRADE",
                            "size"      => "1",
                            "value"     => $model->field["GRADE"],
                            "extrahtml" => "onChange=\"return btn_submit('knjd062');\"",
                            "options"   => $opt_grade));

        $arg["data"]["GRADE"] = $objForm->ge("GRADE");

        //テスト種別コンボボックス
        $db = Query::dbCheckOut();
        $opt_kind = array();
        if ($model->field["SEMESTER"] == "9" && $model->testTable == "TESTITEM_MST_COUNTFLG") {
            $opt_kind[] = array('label' => '0000  評価成績', 'value' => '0');

        } else {
            $query = knjd062Query::getTest($model);
            $result = $db->query($query);
            $test_flg = true;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_kind[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
                if( $model->field["TESTKINDCD"] == $row["VALUE"] ) $test_flg = false;

            }
            if ($model->testTable == "TESTITEM_MST_COUNTFLG") {
                $opt_kind[] = array('label' => '0000  評価成績', 'value' => '0');
                $test_flg = false;
            }

            if($test_flg) $model->field["TESTKINDCD"] = $opt_kind[0]["value"];
            $result->free();
            Query::dbCheckIn($db);
        }


        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTKINDCD",
                            "size"       => "1",
                            "value"      => $model->field["TESTKINDCD"],
                            "options"    => $opt_kind));

        $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

        //クラス一覧リスト作成する
        $db = Query::dbCheckOut();
        $query = knjd062Query::getAuth($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "select",
                            "name"      => "CLASS_NAME",
                            "extrahtml" => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"",
                            "size"      => "20",
                            "options"   => isset($row1)?$row1:array()));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリストを作成する
        $objForm->ae( array("type"      => "select",
                            "name"      => "CLASS_SELECTED",
                            "extrahtml" => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"",
                            "size"      => "20",
                            "options"   => array()));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_rights",
                            "value"     => ">>",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_lefts",
                            "value"     => "<<",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_right1",
                            "value"     => "＞",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_left1",
                            "value"     => "＜",
                            "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //チェックボックスを作成する
        if($model->field["OUTPUT4"]=="1")
        {
            $check = "checked";
        } else {
            $check = "";
        }
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "OUTPUT4",
                            "extrahtml"  => "".$check,    
                            "value"      => isset($model->field["OUTPUT4"])?$model->field["OUTPUT4"]:1));

        $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT4");

        //出力番号選択チェックボックスを作成する
        if($model->field["OUTPUT5"]=="1")
        {
            $check = "checked";
        } else {
            $check = "";
        }
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "OUTPUT5",
                            "extrahtml"  => "".$check,    
                            "value"      => isset($model->field["OUTPUT5"])?$model->field["OUTPUT5"]:1));

        $arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT5");

        //出欠集計日付
        $value = isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"];
        //カレンダーコントロール
        $arg["el"]["DATE"] = View::popUpCalendar($objForm,"DATE",$value);

        //印刷ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_print",
                            "value"     => "プレビュー／印刷",
                            "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD062"
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEME_DATE",
                            "value"     => $seme
                            ) );

        //学期開始日付
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEME_SDATE",
                            "value"     => $sseme
                            ) );

        //学期終了日付
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEME_EDATE",
                            "value"     => $eseme
                            ) );

        //学期終了日付
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEME_FLG",
                            "value"     => $semeflg
                            ) );

        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->control["年度"]
                            ) );

        //テスト種別使用テーブル
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "COUNTFLG",
                            "value"     => $model->testTable
                            ) );

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd062Form1.html", $arg); 
    }
}
?>
