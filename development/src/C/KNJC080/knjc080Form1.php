<?php

require_once('for_php7.php');


class knjc080Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc080Form1", "POST", "knjc080index.php", "", "knjc080Form1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        $opt = array(1, 2, 3);
        $model->field["RADIO"] = ($model->field["RADIO"] == "") ? "1" : $model->field["RADIO"];
        $extra = array("onclick=\"kintai(this);\"", "onclick=\"kintai(this);\"", "onclick=\"kintai(this);\"");
        $radioArray = knjCreateRadio($objForm, "RADIO", $model->field["RADIO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["RADIO"] == 1 || !(isset($model->field["RADIO"]))) {     //通常時間割選択時
            $dis_date1  = "";                        //指定日付FROM使用可
            $dis_date2  = "";                        //指定日付TO使用可
        } else if ($model->field["RADIO"] == 2 ) {
            $dis_date1  = "disabled";                //指定日付FROM使用不可
            $dis_date2  = "disabled";                //指定日付TO使用不可
        } else {
            $dis_date1  = "";                        //指定日付FROM使用可
            $dis_date2  = "disabled";                //指定日付テキスト使用不可
        }

        //カレンダーコントロール１
        $value = isset($model->field["DATE1"])?$model->field["DATE1"]:$model->control["学籍処理日"];
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm,"DATE1",$value);

        //カレンダーコントロール２
        $value2 = isset($model->field["DATE2"])?$model->field["DATE2"]:$model->control["学籍処理日"];
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm,"DATE2",$value2);

        //学期開始日・終了日を取得する
        $semester = $model->control['学期開始日付'][1] ."," .$model->control['学期終了日付'][1];
        $semester = $semester ."," .$model->control['学期開始日付'][2] ."," .$model->control['学期終了日付'][2];
        $semester = $semester ."," .$model->control['学期開始日付'][3] ."," .$model->control['学期終了日付'][3];

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
        knjCreateHidden($objForm, "PRGID", "KNJC080");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //年度データ
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        //学期（hidden）
        knjCreateHidden($objForm, "SEMESTER", $model->control["学期"]);
        //学期開始日(hidden)
        knjCreateHidden($objForm, "SEME_DATE", $semester);
        //今学期（hidden）
        knjCreateHidden($objForm, "SEMESTER_DEFAULT", $model->control["学期"]);

        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc080Form1.html", $arg); 

    }

}

?>
