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

class knjc043bForm1
{
    public function main(&$model)
    {

        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc043bForm1", "POST", "knjc043bindex.php", "", "knjc043bForm1");

        $opt=array();

        //カレンダーコントロール１
        $value = isset($model->field["DATE1"])?$model->field["DATE1"]:$model->control["学籍処理日"];
        $arg["el"]["DATE1"] = View::popUpCalendar($objForm, "DATE1", $value);

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
        $dis_check1 = ($model->field["RADIO1"] == 2) ? "" : "disabled";
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "CHECK1",
                            "checked"    => false,
                            "extrahtml"  => $dis_check1." id=\"CHECK1\"",
                            "value"      => isset($model->field["CHECK1"])?$model->field["CHECK1"]:"1"));

        $arg["data"]["CHECK1"] = $objForm->ge("CHECK1");

        //学期期間日付取得
        $opt_seme=array();
        if (is_numeric($model->control["学期数"])) {
            for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ) {
                $opt_seme[$i*2] = $model->control['学期開始日付'][$i+1];
                $opt_seme[$i*2+1] = $model->control['学期終了日付'][$i+1];
                //学期を取得する
                if ( ($opt_seme[$i*2] <= $value) && ($value2 <= $opt_seme[$i*2+1]) ) {
                    $seme = $i+1;
                }
            }
        }

        $semester = $model->control['学期開始日付'][1] ."," .$model->control['学期終了日付'][1];
        $semester = $semester ."," .$model->control['学期開始日付'][2] ."," .$model->control['学期終了日付'][2];
        $semester = $semester ."," .$model->control['学期開始日付'][3] ."," .$model->control['学期終了日付'][3];


        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjc043bQuery::getAuth($model->control["年度"], $model->control["学期"]);
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

        $z010Row = knjc043bQuery::getNameMst();
        if ($model->cmd == "") {
            $model->field["OUTPUT1"] = $z010Row["NAME1"] == "kwansei" ? "" : "1";
        }
        //集計票チェックボックス
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "OUTPUT1",
                            "checked"    => $model->field["OUTPUT1"] == "1",
                            "extrahtml"  => "id=\"OUTPUT1\"",
                            "value"      => "1"));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT1");

        //明細票チェックボックス
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "OUTPUT2",
                            "checked"    => true,
                            "value"      => isset($model->field["OUTPUT2"])?$model->field["OUTPUT2"]:"1",
                            "extrahtml"  => "onclick=\"Check('this');\" id=\"OUTPUT2\"" ));

        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT2");

        //校時別科目一覧表チェックボックス
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "CHECK2",
                            "checked"    => false,
                            "extrahtml"  => "id=\"CHECK2\"",
                            "value"      => isset($model->field["CHECK2"])?$model->field["CHECK2"]:"1"));

        $arg["data"]["CHECK2"] = $objForm->ge("CHECK2");


        //出力番号選択チェックボックス
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "OUTPUT4",
                            "checked"    => false,
                            "extrahtml"  => "id=\"OUTPUT4\"",
                            "value"      => isset($model->field["OUTPUT4"])?$model->field["OUTPUT4"]:"1"));

        $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT4");

        //「未」選択チェックボックス
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "OUTPUT5",
                            "checked"    => true,
                            "extrahtml"  => "id=\"OUTPUT5\"",
                            "value"      => isset($model->field["OUTPUT5"])?$model->field["OUTPUT5"]:"1"));

        $arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT5");


        //「SHR」「終礼」の科目を出力しない
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "SHR_SYUREI",
                            "checked"    => false,
                            "extrahtml"  => "id=\"SHR_SYUREI\"",
                            "value"      => isset($model->field["SHR_SYUREI"])?$model->field["SHR_SYUREI"]:"1"));

        $arg["data"]["SHR_SYUREI"] = $objForm->ge("SHR_SYUREI");


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
        knjCreateHidden($objForm, "PRGID", "KNJC043B");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]); //年度データ

        $arg["data"]["YEAR"] = $model->control["年度"];

        knjCreateHidden($objForm, "SEMESTER", $model->control["学期"]); //学期

        $arg["data"]["SEMESTER"] = $model->control["学期名"][$model->control["学期"]];

        knjCreateHidden($objForm, "SEME_DATE", $semester); //学期開始日
        knjCreateHidden($objForm, "CHK_SDATE", $model->control['学期開始日付'][9]); //年度開始日
        knjCreateHidden($objForm, "CHK_EDATE", $model->control['学期終了日付'][9]); //年度終了日
        knjCreateHidden($objForm, "DATE3", CTRL_DATE); //ログイン日付
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]); //TESTITEM_MST_COUNTFLG / TESTITEM_MST_COUNTFLG_NEW
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]); //教育課程対応
        knjCreateHidden($objForm, "knjc043bPrintLateEarly", $model->Properties["knjc043bPrintLateEarly"]);
        knjCreateHidden($objForm, "knjc043bKessekiTitleKotei", $model->Properties["knjc043bKessekiTitleKotei"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc043bForm1.html", $arg);
    }
}
