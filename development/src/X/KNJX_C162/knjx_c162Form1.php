<?php

require_once('for_php7.php');

class knjx_c162Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力";

        //DB接続
        $db = Query::dbCheckOut();
        
        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //ヘッダ有チェックボックス
        $extra  = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //学期コンボボックスを作成する
        $query = knjx_c162Query::getSemester($model);
        $extra = "onchange=\"return btn_submit('')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //校種コンボ作成
        $query = knjx_c162Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('')\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, "ALL");

        //学年コンボ作成
        $query = knjx_c162Query::getSelectGrade($model);
        $extra = "onchange=\"return btn_submit('')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "ALL");

        //年組コンボ作成
        $query = knjx_c162Query::getHrClass($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "ALL");
        
        //カレンダーコントロール
        if ($model->Properties["knjc162NenkanAttendance"] == "1") {
            //集計範囲は学期毎の固定値となるよう修正
            $model->field["SDATE"] = CTRL_YEAR."/04/01";
            $model->field["EDATE"] = (CTRL_YEAR+1)."/03/31";
            if ($model->field["SEMESTER"] == "1") {
                $model->field["SDATE"] = CTRL_YEAR."/04/01";
                $model->field["EDATE"] = CTRL_YEAR."/08/31";
            } elseif ($model->field["SEMESTER"] == "2") {
                $model->field["SDATE"] = CTRL_YEAR."/09/01";
                $model->field["EDATE"] = CTRL_YEAR."/12/31";
            } elseif ($model->field["SEMESTER"] == "3") {
                $model->field["SDATE"] = (CTRL_YEAR+1)."/01/01";
                $model->field["EDATE"] = (CTRL_YEAR+1)."/03/31";
            }
            $arg["el"]["SDATE"] = knjCreateTextBox($objForm, $model->field["SDATE"], "SDATE", 10, 10, "disabled");
            $arg["el"]["EDATE"] = knjCreateTextBox($objForm, $model->field["EDATE"], "EDATE", 10, 10, "disabled");
            knjCreateHidden($objForm, "SDATE", $model->field["SDATE"]);
            knjCreateHidden($objForm, "EDATE", $model->field["EDATE"]);
        } else {
            $model->field["SDATE"] = $model->field["SDATE"] == "" ? str_replace("-", "/", $model->control["学期開始日付"][9]) : $model->field["SDATE"];
            $model->field["EDATE"] = $model->field["EDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EDATE"];
            $arg["el"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);
            $arg["el"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"]);
        }

        //注意・超過のタイトル
        $arg["data"]["TYUI_TYOUKA_TITLE"] = "欠課数上限値（履修／修得）";

        //注意・超過ラジオ
        $opt = array(1, 2); //1:注意 2:超過
        $model->field["TYUI_TYOUKA"] = ($model->field["TYUI_TYOUKA"] == "") ? "1" : $model->field["TYUI_TYOUKA"];
        $extra = array("id=\"TYUI_TYOUKA1\"", "id=\"TYUI_TYOUKA2\"");
        $radioArray = knjCreateRadio($objForm, "TYUI_TYOUKA", $model->field["TYUI_TYOUKA"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx_c162index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_c162Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "（全て出力）",
                       "value" => "ALL");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "") ? CTRL_SEMESTER : $value;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg)
{
    //CSVボタン
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "実 行", "onclick=\" return newwin('" . SERVLET_URL . "', 'csv');\"");
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", str_replace("-", "/", CTRL_DATE));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJX_C162");
    knjCreateHidden($objForm, "COUNTFLG", $model->testTable);
    knjCreateHidden($objForm, "SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
    knjCreateHidden($objForm, "SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
    knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
    knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "knjc162NenkanAttendance", $model->Properties["knjc162NenkanAttendance"]);
}
