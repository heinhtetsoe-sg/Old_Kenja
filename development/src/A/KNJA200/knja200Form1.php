<?php

require_once('for_php7.php');

class knja200Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja200Form1", "POST", "knja200index.php", "", "knja200Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //作成日
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $value);

        //学期コンボボックスを作成する
        $query = knja200Query::getSemester(CTRL_YEAR);
        $extra = "onchange=\"return btn_submit('knja200');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        if ($model->Properties["dispMTokuHouJituGrdMixChkRad"] == "1") {
            $arg["dispJituGrdMix"] = "1";
            //法定/実クラスラジオ
            $opt_change = array(1, 2);
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick =\" return btn_submit('knja200');\"", "id=\"HR_CLASS_TYPE2\" onclick =\" return btn_submit('knja200');\"");
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt_change, get_count($opt_change));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }

            //学年混合(チェックボックス)
            $extra  = $model->field["GAKUNEN_KONGOU"] == "1" ? "checked" : "";
            $extra .= " id=\"GAKUNEN_KONGOU\" onclick=\"return btn_submit('knja200');\"";
            if ($model->field["HR_CLASS_TYPE"] != "1") {
                $extra .= "disabled ";
            }
            $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");
        }

        //クラス選択コンボボックスを作成する
        $query = knja200Query::getAuth($model, $model->field["SEMESTER"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //電話番号チェックボックスを作成する
        $model->field["TEL"] = $model->getSetDefaultVal($model->field["TEL"], "1", $model->PrgDefaultVal["TEL"], $model->cmd);
        if ($model->field["TEL"] == "1") {
            $check_tel = "checked";
        } else {
            $check_tel = "";
        }

        $extra = $check_tel." id=\"TEL\"";
        $arg["data"]["TEL"] = knjCreateCheckBox($objForm, "TEL", "1", $extra, "");

        //出身学校チェックボックス
        $model->field["SCHOOLNAME"] = $model->getSetDefaultVal($model->field["SCHOOLNAME"], "", $model->PrgDefaultVal["SCHOOLNAME"], $model->cmd);
        $extra = " id=\"SCHOOLNAME\" onchange=\"return btn_submit('knja200');\"";
        $extra .= $model->field["SCHOOLNAME"] == "1" ? " checked " : "";
        $arg["data"]["SCHOOLNAME"] = knjCreateCheckBox($objForm, "SCHOOLNAME", "1", $extra);

        //出身学校/ふりがなラジオ
        $disflg = $model->field["SCHOOLNAME"] == "1" ? "" : " disabled ";
        $opt = array(1, 2);
        $model->field["PRINT_INFO"] = ($model->field["PRINT_INFO"] == "") ? "1" : $model->field["PRINT_INFO"];
        $extra = array("id=\"PRINT_INFO1\"".$disflg, "id=\"PRINT_INFO2\"".$disflg);
        $radioArray = knjCreateRadio($objForm, "PRINT_INFO", $model->field["PRINT_INFO"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /**********/
        /* ラジオ */
        /**********/
        //フォーム選択 (1:5列×5行 2:6列×7行 2:8列×6行)
        $opt = array(1, 2, 3);
        $model->field["FORM_SENTAKU"] = $model->getSetDefaultVal($model->field["FORM_SENTAKU"], "1", $model->PrgDefaultVal["FORM_SENTAKU"], $model->cmd);
        $extra = array("id=\"FORM_SENTAKU1\"", "id=\"FORM_SENTAKU2\"", "id=\"FORM_SENTAKU3\"");
        $radioArray = knjCreateRadio($objForm, "FORM_SENTAKU", $model->field["FORM_SENTAKU"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        if (AUTHORITY == DEF_UPDATABLE) {
            //Default更新ボタンを作成する
            $extra = " onclick=\"return updDefault('defaultUpd', 'KNJA200');\" accesskey=?";
            $arg["button"]["defaultUpd"] = knjCreateBtn($objForm, "defaultUpd", "初期値設定", $extra);
        }
        knjCreateHidden($objForm, "UPD_DEFAULT_VALUE");

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA200");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "dispMTokuHouJituGrdMixChkRad", $model->Properties["dispMTokuHouJituGrdMixChkRad"]);
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
        knjCreateHidden($objForm, "useFi_Hrclass", $model->Properties["useFi_Hrclass"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja200Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
