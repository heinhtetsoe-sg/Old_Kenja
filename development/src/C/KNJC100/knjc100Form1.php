<?php

require_once('for_php7.php');


class knjc100Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc100Form1", "POST", "knjc100index.php", "", "knjc100Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //勤怠表種別ラジオボタンを作成
        $opt[0]=1;
        $opt[1]=2;
        $objForm->ae( array("type"       => "radio",
                            "name"       => "RADIO",
                            "value"      => isset($model->field["RADIO"])?$model->field["RADIO"]:1,
                            "extrahtml"  => "onclick=\"kintai(this);\"",
                            "multiple"   => $opt));

        $arg["data"]["RADIO1"] = $objForm->ge("RADIO",1);
        $arg["data"]["RADIO2"] = $objForm->ge("RADIO",2);

        //カレンダーコントロール１
        $value = isset($model->field["DATE1"])?$model->field["DATE1"]:$model->control["学籍処理日"];
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm,"DATE1",$value);

        //カレンダーコントロール２
        if ($model->field["RADIO"] == "2") {
            $dis_date2 = "disabled";
            $arg["Dis_Date"]  = " dis_date(true); " ;
        } else {
            $dis_date2 = "";
            $arg["Dis_Date"]  = " dis_date(false); " ;
        }

        $value2 = isset($model->field["DATE2"])?$model->field["DATE2"]:$model->control["学籍処理日"];
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm,"DATE2",$value2);

        //学期期間日付取得
        $opt_seme=array();
        if (is_numeric($model->control["学期数"])) {
            for ($i = 0; $i < (int) $model->control["学期数"]; $i++) {
                $opt_seme[$i*2] = $model->control['学期開始日付'][$i+1];
                $opt_seme[$i*2+1] = $model->control['学期終了日付'][$i+1];
                //学期を取得する
                if ($model->field["RADIO"] == "2") {
                    if (($opt_seme[$i*2] <= $value) && ($value <= $opt_seme[$i*2+1])) {
                        $seme = $i+1;
                    }
                } else {
                    if (($opt_seme[$i*2] <= $value) && ($value2 <= $opt_seme[$i*2+1])) {
                        $seme = $i+1;
                    }
                }
            }
        }

        $semester = $model->control['学期開始日付'][1] ."," .$model->control['学期終了日付'][1];
        $semester = $semester ."," .$model->control['学期開始日付'][2] ."," .$model->control['学期終了日付'][2];
        $semester = $semester ."," .$model->control['学期開始日付'][3] ."," .$model->control['学期終了日付'][3];


        //学期テキストボックスを作成する
        if (isset($model->field["GAKKINAME"])) {
            $gakkiname = $model->control["学期名"][$seme];
        } else {
            $gakkiname = $model->control["学期名"][$model->control["学期"]];
        }
        $arg["data"]["GAKKINAME"] = $gakkiname;

        //クラス一覧リスト作成する
        $query = knjc100Query::getAuth($model->control["年度"],$seme);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        $extra = "multiple style=\"width:150px\" width=\"150px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, isset($row1) ? $row1 : array(), $extra, 15);

        //出力対象クラスリストを作成する
        $extra = "multiple style=\"width:150px\" width=\"150px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $value, array(), $extra, 15);

        //読込ボタンを作成する
        $extra = "onclick=\"return btn_submit('knjc100');\"";
        $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読　込", $extra);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //出力する情報チェックボックスを作成
        $extra = " checked onclick=\"kubun();\"";
        $arg["data"]["OUTPUT1"] = knjCreateCheckBox($objForm, "OUTPUT1", isset($model->field["OUTPUT1"]) ? $model->field["OUTPUT1"] : "1", $extra);

        $extra = " checked onclick=\"kubun();\"";
        $arg["data"]["OUTPUT2"] = knjCreateCheckBox($objForm, "OUTPUT2", isset($model->field["OUTPUT2"]) ? $model->field["OUTPUT2"] : "1", $extra);

        $extra = " checked onclick=\"kubun();\"";
        $arg["data"]["OUTPUT3"] = knjCreateCheckBox($objForm, "OUTPUT3", isset($model->field["OUTPUT3"]) ? $model->field["OUTPUT3"] : "1", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC100");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //年度データ
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        //学期
        knjCreateHidden($objForm, "SEMESTER", $seme);
        //学期開始日
        knjCreateHidden($objForm, "SEME_DATE", $semester);
        //学期データ
        knjCreateHidden($objForm, "GAKKINAME", $gakkiname);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc100Form1.html", $arg); 

    }

}
?>

