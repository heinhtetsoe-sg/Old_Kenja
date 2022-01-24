<?php

require_once('for_php7.php');

class knjg102Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjg102Form1", "POST", "knjg102index.php", "", "knjg102Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;
        
        // 皆勤欠席
        if (!$model->field["KAIKIN_KESSEKI"]) {
            $model->field["KAIKIN_KESSEKI"] = "0";
        }
        $arg["data"]["KAIKIN_KESSEKI"] = $model->field["KAIKIN_KESSEKI"];
        
        // 精勤欠席
        if (!$model->field["SEIKIN_KESSEKI"]) {
            $model->field["SEIKIN_KESSEKI"] = "3";
        }
        $arg["data"]["SEIKIN_KESSEKI"] = $model->field["SEIKIN_KESSEKI"];
        
        // 欠席換算
        if (!$model->field["KESSEKI_KANSAN"]) {
            $model->field["KESSEKI_KANSAN"] = "3";
        }
        $arg["data"]["KESSEKI_KANSAN"] = $model->field["KESSEKI_KANSAN"];
 
        //クラス方式選択 (1:法定クラス 2:複式クラス)
        $opt = array(1, 2);
        if ($model->field["HR_CLASS_TYPE"] == "") $model->field["HR_CLASS_TYPE"] = ($model->Properties["useFi_Hrclass"] == "1") ? "2" : "1";
        $extra = array("id=\"HR_CLASS_TYPE1\"", "id=\"HR_CLASS_TYPE2\"");
        $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学年コンボボックス
        $opt = array();
        $query = knjg102Query::getGrade($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        $opt[] = array('label' => "全て", 'value' => "99");

        if ($model->field["GRADE"] == "") $model->field["GRADE"] = $opt[0]["value"];
        $extra = "";
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

        //在籍期間全てチェックボックス
        $extra  = ($model->field["ZAISEKI_ALL"] == "1") ? "checked" : "";
        $extra .= " id=\"ZAISEKI_ALL\"";
        $disabled = ($model->field["SYUKKETU_SYUKEI"] == "1") ? " disabled" : "";
        $arg["data"]["ZAISEKI_ALL"] = knjCreateCheckBox($objForm, "ZAISEKI_ALL", "1", $extra.$disabled, "");

        //出力対象選択ラジオボタン (1:皆勤者 2:精勤者)
        $opt = array(1, 2);
        $model->field["KAIKINSYA"] = ($model->field["KAIKINSYA"] == "") ? "1" : $model->field["KAIKINSYA"];
        $extra = array("id=\"KAIKINSYA1\"", "id=\"KAIKINSYA2\"");
        $radioArray = knjCreateRadio($objForm, "KAIKINSYA", $model->field["KAIKINSYA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********************/
        /* テキストボックス */
        /********************/
        //extra
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";

        //皆勤者/遅刻
        $value = $model->field["KAIKIN_KAIKIN_TIKOKU"];
        $value = $value ? $value : "2";
        $arg["data"]["KAIKIN_KAIKIN_TIKOKU"] = knjCreateTextBox($objForm, $value, "KAIKIN_KAIKIN_TIKOKU", 3, 2, $extra);
        //精勤者/遅刻
        $value = $model->field["KAIKIN_SEIKIN_TIKOKU"];
        $value = $value ? $value : "8";
        $arg["data"]["KAIKIN_SEIKIN_TIKOKU"] = knjCreateTextBox($objForm, $value, "KAIKIN_SEIKIN_TIKOKU", 3, 3, $extra);

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
        knjCreateHidden($objForm, "PRGID", "KNJG102");
        knjCreateHidden($objForm, "KESSEKI_KANSAN", $model->field["KESSEKI_KANSAN"]);
        knjCreateHidden($objForm, "KAIKIN_KESSEKI", $model->field["KAIKIN_KESSEKI"]);
        knjCreateHidden($objForm, "SEIKIN_KESSEKI", $model->field["SEIKIN_KESSEKI"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "SDATE", $model->control["学期開始日付"][CTRL_SEMESTER]);    //学期開始日
        knjCreateHidden($objForm, "EDATE", $model->control["学期終了日付"][CTRL_SEMESTER]);    //学期終了日
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg102Form1.html", $arg); 
    }
}
?>
