<?php

require_once('for_php7.php');

/********************************************************************/
/* 出席簿印刷（公簿）指定可                         山城 2005/03/30 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：欠番と14校時のチェックボックスを追加     山内 2005/06/07 */
// ･NO002：仲本 2006/02/08 10～14校時用フォームのチェックボツクスをカット
// ･NO003：仲本 2006/02/08 コアタイム出力ラジオおよび日曜日のみ出力チェックボックスを追加
/********************************************************************/

class knjc043tForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc043tForm1", "POST", "knjc043tindex.php", "", "knjc043tForm1");

        if ($model->Properties["hibiNyuuryokuNasi"] == '1') {
            $arg["hibiNyuuryokuNasi"] = '1';
        } else {
            $arg["hibiNyuuryokuAri"] = '1';
        }

        //カレンダーコントロール１
        if ($model->Properties["hibiNyuuryokuNasi"] == '1') {
            $date1 = str_replace('-', '/', $model->control['学期開始日付'][9]);
            $arg["txt"]["DATE1"] = $date1;
            $arg["el"]["DATE1"] = knjCreateHidden($objForm, "DATE1", $date1);
        } else {
            $value = isset($model->field["DATE1"])?$model->field["DATE1"]:$model->control["学籍処理日"];
            $arg["el"]["DATE1"] = View::popUpCalendar($objForm, "DATE1", $value);
        }

        //カレンダーコントロール２
        $value2 = isset($model->field["DATE2"])?$model->field["DATE2"]:$model->control["学籍処理日"];
        $arg["el"]["DATE2"] = View::popUpCalendar($objForm, "DATE2", $value2);

        //コアタイム出力ラジオ---1:コアタイム出力,2:全て出力---NO003
        $opt_radio1[0]=1;
        $opt_radio1[1]=2;

        if (!$model->field["RADIO1"]) {
            $model->field["RADIO1"] = 1;
        }
        for ($i = 1; $i <= 2; $i++) {
            $name = "RADIO1_".$i;
            $objForm->ae(array("type"       => "radio",
                                "name"       => "RADIO1",
                                "value"      => $model->field["RADIO1"],
                                "multiple"   => $opt_radio1,
                                "extrahtml"  =>"onclick=\"Check('this');\" id=\"$name\"" ));

            $arg["data"][$name] = $objForm->ge("RADIO1", $i);
        }

        //日曜日のみ出力チェックボックス---NO003
        $extra  = ($model->field["CHECK1"] == "1") ? "checked='checked' " : "";
        $extra .= " id=\"CHECK1\"";
        if ($model->field["RADIO1"] != 2) {
            $extra .= " disabled ";
        }
        $arg["data"]["CHECK1"] = knjCreateCheckBox($objForm, "CHECK1", "1", $extra, "");

        //学期期間日付取得
        $opt_seme=array();
        if (is_numeric($model->control["学期数"])) {
            for ($i = 0; $i < (int) $model->control["学期数"]; $i++) {
                $opt_seme[$i*2] = $model->control['学期開始日付'][$i+1];
                $opt_seme[$i*2+1] = $model->control['学期終了日付'][$i+1];
                //学期を取得する
                if (($opt_seme[$i*2] <= $value) && ($value2 <= $opt_seme[$i*2+1])) {
                    $seme = $i+1;
                }
            }
        }

        $semester = $model->control['学期開始日付'][1] ."," .$model->control['学期終了日付'][1];
        $semester = $semester ."," .$model->control['学期開始日付'][2] ."," .$model->control['学期終了日付'][2];
        $semester = $semester ."," .$model->control['学期開始日付'][3] ."," .$model->control['学期終了日付'][3];


        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjc043tQuery::getAuth($model, $model->control["年度"], $model->control["学期"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae(array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //集計票チェックボックス
        if ($model->cmd == '') {
            $model->field["OUTPUT1"] = "1";
        }
        $extra  = ($model->field["OUTPUT1"] == "1") ? "checked='checked' " : "";
        $extra .= " id=\"OUTPUT1\"";
        $arg["data"]["OUTPUT1"] = knjCreateCheckBox($objForm, "OUTPUT1", "1", $extra, "");

        //明細票チェックボックス
        if ($model->cmd == '') {
            $model->field["OUTPUT2"] = "1";
        }
        $extra  = ($model->field["OUTPUT2"] == "1") ? "checked='checked' " : "";
        $extra .= " id=\"OUTPUT2\"";
        $extra .= " onclick=\"Check('this');\" ";
        $arg["data"]["OUTPUT2"] = knjCreateCheckBox($objForm, "OUTPUT2", "1", $extra, "");

        //校時別科目一覧表チェックボックス
        $extra  = ($model->field["CHECK2"] == "1") ? "checked='checked' " : "";
        $extra .= " id=\"CHECK2\"";
        $arg["data"]["CHECK2"] = knjCreateCheckBox($objForm, "CHECK2", "1", $extra, "");

        //出力番号選択チェックボックス
        $extra  = ($model->field["OUTPUT4"] == "1") ? "checked='checked' " : "";
        $extra .= " id=\"OUTPUT4\"";
        $arg["data"]["OUTPUT4"] = knjCreateCheckBox($objForm, "OUTPUT4", "1", $extra, "");

        //「未」選択チェックボックス
        if ($cmd == '') {
            $model->field["OUTPUT5"] = "1";
        }
        $extra  = ($model->field["OUTPUT5"] == "1") ? "checked='checked' " : "";
        $extra .= " id=\"OUTPUT5\"";
        $arg["data"]["OUTPUT5"] = knjCreateCheckBox($objForm, "OUTPUT5", "1", $extra, "");

        //「SHR」「終礼」の科目を出力しない
        $extra  = ($model->field["SHR_SYUREI"] == "1") ? "checked='checked' " : "";
        $extra .= " id=\"SHR_SYUREI\"";
        $arg["data"]["SHR_SYUREI"] = knjCreateCheckBox($objForm, "SHR_SYUREI", "1", $extra, "");

        //1日出欠判定チェック
        $ibaraki = "";
        $arg["data"]["ibaraki"] = $ibaraki;
        $extra  = ($model->field["ONEDAY_ATTEND_CHECK"] == "1") ? "checked='checked' " : "";
        $extra .= " id=\"ONEDAY_ATTEND_CHECK\"";
        $arg["data"]["ONEDAY_ATTEND_CHECK"] = knjCreateCheckBox($objForm, "ONEDAY_ATTEND_CHECK", "1", $extra, "");

        //印刷ボタンを作成する
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ));

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        ////////////////////////////////////////////////////////////////////////////////////
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC043");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        //年度データ
        $arg["data"]["YEAR"] = $model->control["年度"];
        //学期
        knjCreateHidden($objForm, "SEMESTER", $model->control["学期"]);
        $arg["data"]["SEMESTER"] = $model->control["学期名"][$model->control["学期"]];
        //$arg["data"]["SEMESTER"] = $model->control["学期名"][$seme];

        //学期開始日
        knjCreateHidden($objForm, "SEME_DATE", $semester);
        //年度開始日
        knjCreateHidden($objForm, "CHK_SDATE", $model->control['学期開始日付'][9]);
        //年度終了日
        knjCreateHidden($objForm, "CHK_EDATE", $model->control['学期終了日付'][9]);
        //ログイン日付
        knjCreateHidden($objForm, "DATE3", CTRL_DATE);

        //教育課程対応
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
        knjCreateHidden($objForm, "knjc043tMeisaiPrintAttendDayDat", $model->Properties["knjc043tMeisaiPrintAttendDayDat"]);
        knjCreateHidden($objForm, "use_attendDayDiCd_00", $model->Properties["use_attendDayDiCd_00"]);

        // 明細表（４０名＋出欠備考欄）を出力
        $rtnRow = knjc043tQuery::getNameMst();
        $schoolName = $rtnRow["NAME1"];
        if ($schoolName == "jisyukan") {
            $objForm->ae(array("type"      => "hidden",
                                "name"      => "OUTPUT_DIREMARK",
                                "value"     => "1"
                                ));
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc043tForm1.html", $arg);
    }
}
