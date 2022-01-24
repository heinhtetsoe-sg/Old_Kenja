<?php

require_once('for_php7.php');

class knjx091Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR"]         = CTRL_YEAR;
        $arg["data"]["SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //DB接続
        $db = Query::dbCheckOut();

        /****************/
        /* ラジオボタン */
        /****************/
        //出力方式 (1:1科目/1行 2:複数科目/1行)
        $opt = array(1, 2);
        $model->field["FORM"] = ($model->field["FORM"] == "") ? "1" : $model->field["FORM"];
        $extra = array("id=\"FORM1\" onclick =\" return btn_submit('main');\"", "id=\"FORM2\" onclick =\" return btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "FORM", $model->field["FORM"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力取込種別 (1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /******************/
        /* コンボボックス */
        /******************/
        //年度・学年
        $opt = array();
        $value = $model->field["YEAR_GRADE"];
        $value_flg = false;
        $default = "";
        $query = knjx091query::getYearGrade();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            $yg = explode('-', $row["VALUE"]);

            if ($default == "" && $yg[0] == CTRL_YEAR) {
                $default = $row["VALUE"];
            }
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : ($default ? $default : $opt[0]["value"]);
        $extra = ($model->field["FORM"] == "2") ? "onchange=\"btn_submit('main');\"" : "disabled";
        $arg["data"]["YEAR_GRADE"] = knjCreateCombo($objForm, "YEAR_GRADE", $value, $opt, $extra, 1);

        list($year, $grade) = explode('-', $value);

        //処理名
        $opt   = array();
        $opt[] = array("label" => "更新","value" => "1");
        $opt[] = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt, $extra, 1);

        //年度＆学期
        $opt = array();
        $value = $model->field["YEAR"].'-'.$model->field["SEMESTER"];
        $value_flg = false;
        $query = knjx091query::getSelectFieldSQL();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field["FORM"] == "2") {
                list($y, $s) = explode('-', $row["VALUE"]);
                if ($year != $y) {
                    continue;
                }
            }
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "onchange=\"btn_submit('main');\"";
        $arg["data"]["YEAR_SEMESTER"] = knjCreateCombo($objForm, "YEAR_SEMESTER", $value, $opt, $extra, 1);

        //学年
        if ($model->field["FORM"] == "1") {
            $opt   = array();
            $opt[] = array("label" => "(全て出力)","value" => "99");
            $value = $model->field["GRADE"];
            $value_flg = false;
            $query = knjx091query::getGrade($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
                if ($value == $row["VALUE"]) {
                    $value_flg = true;
                }
            }
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
            $extra = "";
            $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $value, $opt, $extra, 1);
        } else {
            $grade_label = $db->getOne(knjx091query::getGrade($model, $grade));
            $arg["data"]["GRADE"] = $grade_label;
            knjCreateHidden($objForm, "GRADE", $grade);
        }

        //科目
        $opt   = array();
        if ($model->Properties["useCurriculumcd"] == '1') {
            $opt[] = array("label" => "(全て出力)","value" => "99-X-9-999999");
        } else {
            $opt[] = array("label" => "(全て出力)","value" => "999999");
        }
        $value = $model->field["SUBCLASS"];
        $value_flg = false;
        $query = knjx091query::getSubclassStdDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["SUBCLASS"] = knjCreateCombo($objForm, "SUBCLASS", $value, $opt, $extra, 1);

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        if ($model->field["HEADER"] == "on") {
            $extra = "checked";
        } else {
            $extra = ($model->cmd == "") ? "checked" : "";
        }

        if ($model->field["FORM"] == "2") {
            $extra = "checked onclick=\"chkHeader(this);\"";
        }
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra." id=\"HEADER\"");

        /************/
        /* ファイル */
        /************/
        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        /**********/
        /* ボタン */
        /**********/
        //講座名簿から自動履修登録
        $extra = "onclick=\"return btn_submit('create');\"";
        $arg["btn_cre"] = knjCreateBtn($objForm, "btn_cre", "講座名簿から自動履修登録", $extra);

        //実行
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx091index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx091Form1.html", $arg);
    }
}
