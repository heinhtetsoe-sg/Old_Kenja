<?php

require_once('for_php7.php');

class knjg102aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjg102aForm1", "POST", "knjg102aindex.php", "", "knjg102aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;
 
        //クラス方式選択 (1:法定クラス 2:複式クラス)
        $opt = array(1, 2);
        if ($model->field["HR_CLASS_TYPE"] == "") $model->field["HR_CLASS_TYPE"] = ($model->Properties["useFi_Hrclass"] == "1") ? "2" : "1";
        $extra = array("id=\"HR_CLASS_TYPE1\"", "id=\"HR_CLASS_TYPE2\"");
        $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学年コンボボックス
        $opt = array();
        $query = knjg102aQuery::getGrade($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        $opt[] = array('label' => "全て", 'value' => "99");

        if ($model->field["GRADE"] == "") $model->field["GRADE"] = $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjg102a');\"";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt, $extra, 1);

        //異動対象日付
        $model->field["DATE"] = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"]=View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //記載日付
        $model->field["KISAI_DATE"] = isset($model->field["KISAI_DATE"]) ? $model->field["KISAI_DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["KISAI_DATE"] = View::popUpCalendar($objForm , "KISAI_DATE", $model->field["KISAI_DATE"]);

        //ふりがな出力チェックボックス
        $extra = "checked id=\"KANA_PRINT\"";
        $arg["data"]["KANA_PRINT"] = knjCreateCheckBox($objForm, "KANA_PRINT", "1", $extra);

        //出力対象選択ラジオボタン (1:皆勤者 2:精勤者)
        $opt = array(1, 2);
        $model->field["KAIKINSYA"] = ($model->field["KAIKINSYA"] == "") ? "1" : $model->field["KAIKINSYA"];
        $extra = array("id=\"KAIKINSYA1\"", "id=\"KAIKINSYA2\"");
        $radioArray = knjCreateRadio($objForm, "KAIKINSYA", $model->field["KAIKINSYA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //生徒項目名切替処理
        $sch_label = "";
        if (!($model->field["GRADE"] == "99" && $model->Properties["useSchool_KindField"] != "1")) {
            //テーブルの有無チェック
            $query = knjg102aQuery::checkTableExist();
            $table_cnt = $db->getOne($query);
            if ($table_cnt > 0 && ($model->field["GRADE"] || ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
                //生徒項目名取得
                $sch_label = $db->getOne(knjg102aQuery::getSchName($model));
            }
        }
        $arg["data"]["SCH_LABEL"] = (strlen($sch_label) > 0) ? $sch_label : '生徒';

        /********************/
        /* テキストボックス */
        /********************/
        //extra
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";

        //皆勤者/欠席
        $value = $model->field["KAIKIN_KESSEKI"];
        $value = $value ? $value : "0";
        $arg["data"]["KAIKIN_KESSEKI"] = knjCreateTextBox($objForm, $value, "KAIKIN_KESSEKI", 3, 3, $extra);

        //精勤者/欠席
        $value = $model->field["SEIKIN_KESSEKI"];
        $value = $value ? $value : "5";
        $arg["data"]["SEIKIN_KESSEKI"] = knjCreateTextBox($objForm, $value, "SEIKIN_KESSEKI", 3, 3, $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "CTRL_YEAR",  CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJG102A");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "SDATE", $model->control["学期開始日付"][CTRL_SEMESTER]);    //学期開始日
        knjCreateHidden($objForm, "EDATE", $model->control["学期終了日付"][CTRL_SEMESTER]);    //学期終了日
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg102aForm1.html", $arg); 
    }
}
?>
