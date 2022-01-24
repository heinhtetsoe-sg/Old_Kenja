<?php
class knja352Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //DB接続
        $db = Query::dbCheckOut();

        /**********/
        /* コンボ */
        /**********/
        //年度
        $arg["TOP"]["YEAR"] = $model->control["年度"]."年度";
        $arg["TOP"]["SEMESTER"] = $model->control["学期名"][$model->semester];

        //校種
        $query = knja352Query::getSchoolKind($model);
        $extra = "";
        $arg["TOP"]["SCHOOL_KIND"] = makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, "1");

        //出力対象
        $opt = array(1, 2, 3);
        $model->field["OUTPUT_DIV"] = ($model->field["OUTPUT_DIV"] == "") ? "1" : $model->field["OUTPUT_DIV"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT_DIV{$val}\" onClick=\"btn_submit('main')\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //卒業年度コンボ
        $query = knja352Query::getYearHist($model);
        $model->field["GRD_YEAR"] = ($model->field["GRD_YEAR"] == "") ? $model->year - 1 : $model->field["GRD_YEAR"];
        $extra = ($model->field["OUTPUT_DIV"] == "3") ? "" : " disabled ";
        $arg["TOP"]["GRD_YEAR"] = makeCmb($objForm, $arg, $db, $query, "GRD_YEAR", $model->field["GRD_YEAR"], $extra, "1");

        //新入生詳細選択
        $opt = array();
        $opt[] = array("label" => "クラス編成前", "value" => "1");
        $opt[] = array("label" => "クラス編成後", "value" => "2");
        $model->field["FRESHMAN_DIV"] = ($model->field["FRESHMAN_DIV"] == "") ? "1" : $model->field["FRESHMAN_DIV"];
        $extra = ($model->field["OUTPUT_DIV"] == "1") ? "" : " disabled ";
        $arg["data"]["FRESHMAN_DIV"] = knjCreateCombo($objForm, "FRESHMAN_DIV", $model->field["FRESHMAN_DIV"], $opt, $extra, 1);

        //発行日
        $arg["data"]["ISSUE_DATE"] = View::popUpCalendarAlp($objForm, "ISSUE_DATE", str_replace("-", "/", $model->field["ISSUE_DATE"]), $extra);
        //有効期限
        $arg["data"]["LIMIT_DATE"] = View::popUpCalendarAlp($objForm, "LIMIT_DATE", str_replace("-", "/", $model->field["LIMIT_DATE"]), $extra);


        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            if ($model->cmd == "") {
                $check_header = "checked";
            } else {
                $check_header = "";
            }
        }

        $extra = " id=\"HEADER\"";
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "HEADER",
                            "value"     => "on",
                            "extrahtml" =>$check_header.$extra ));
        $arg["data"]["HEADER"] = $objForm->ge("HEADER");

        /**********/
        /* ボタン */
        /**********/
        makeBtn($objForm, $arg, $model);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knja352index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja352Form1.html", $arg);
    }
}
/******************************************************* 以下関数 *******************************************************/

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);


    //指定した出力対象ごとに出力ボタンをdisabledにする連携データを設定する
    $disabled = array();
    $diabledArray["1"] = array();
    $diabledArray["2"] = array(5);
    $diabledArray["3"] = array(1, 4, 5);

    //CSV出力ボタン
    for ($i = 1; $i <= 5; $i++) {
        $disabled = (in_array($i, $diabledArray[$model->field["OUTPUT_DIV"]])) ? " disabled " :"";
        $extra = "onclick=\"return btn_submit('exec{$i}');\"";
        $arg["button"]["btn_exec{$i}"] = knjCreateBtn($objForm, "btn_exec{$i}", "CSV出力", $extra.$disabled);
    }
}

//コンボ作成関数
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

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
