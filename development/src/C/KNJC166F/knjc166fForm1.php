<?php

require_once('for_php7.php');
class knjc166fForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc166fForm1", "POST", "knjc166findex.php", "", "knjc166fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //異動対象日付
        $model->field["DATE"] = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"]=View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //表示パターンにより、表示を切り替える。
        if ($model->Properties["knjc166fDispType"] == "1") {
            $arg["NODISP_TYPECHKBOX"] = "1";
        } else {
            $arg["DISP_TYPECHKBOX"] = "1";
        }

        //1:皆勤 2:精勤
        $radioValue = array(1, 2);
        if (!$model->field["OUTPUT_KAIKIN"]) $model->field["OUTPUT_KAIKIN"] = 1;
        $extra = array("id=\"OUTPUT_KAIKIN1\" onclick=\"return btn_submit('knjc166f');\"", "id=\"OUTPUT_KAIKIN2\" onclick=\"return btn_submit('knjc166f');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KAIKIN", $model->field["OUTPUT_KAIKIN"], $extra, $radioValue, get_count($radioValue));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        if ($model->field["OUTPUT_KAIKIN"] == '2') {
            $arg["data"]["TEXT_KAIKIN"] = "精勤";
        } else {
            $arg["data"]["TEXT_KAIKIN"] = "皆勤";
        }

        $z010 = $db->getOne(knjc166fQuery::getZ010());
        //1:学年毎皆勤者 2:累計皆勤者
        $radioValue = array(1, 2);
        if (!$model->field["OUTPUT"]) {
            $model->field["OUTPUT"] = "2";
        }
        $extra = array("id=\"OUTPUT1\" onclick=\"return btn_submit('knjc166f');\"", "id=\"OUTPUT2\" onclick=\"return btn_submit('knjc166f');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $radioValue, get_count($radioValue));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->Properties["knjc166fDispType"] != "1") {
            //累計皆勤者除く
            $extra = $model->field["OUTPUT1_RUIKEI"] == "1" ? "checked" : "";
            $extra .= " id=\"OUTPUT1_RUIKEI\"";
            $arg["data"]["OUTPUT1_RUIKEI"] = knjCreateCheckBox($objForm, "OUTPUT1_RUIKEI", "1", $extra, "");
        }

        /********************/
        /* テキストボックス */
        /********************/

        $dispFlg = 0;
        if ($z010 != 'teihachi') {
            if ($model->Properties["knjc166fDispType"] == "1") {
                if ($model->field["OUTPUT_KAIKIN"] == '2') {
                    if ($model->field["OUTPUT"] == "2") {
                        $arg["DISP_CONDITION_T2"] = "1";
                        $dispFlg = 1;
                        $model->field["KEKKA_KESSEKI_CNT"] = $model->field["KEKKA_KESSEKI_CNT"] == "" ? "9" : $model->field["KEKKA_KESSEKI_CNT"];
                        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
                        $arg["data"]["KEKKA_KESSEKI_CNT"] = knjCreateTextBox($objForm, $model->field["KEKKA_KESSEKI_CNT"], "KEKKA_KESSEKI_CNT", 3, 3, $extra);
                    } else {
                        $arg["DISP_CONDITION_T1"] = "1";
                        $dispFlg = 1;
                        $model->field["KEKKA_KESSEKI_CNT"] = $model->field["KEKKA_KESSEKI_CNT"] == "" ? "3" : $model->field["KEKKA_KESSEKI_CNT"];
                        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
                        $arg["data"]["KEKKA_KESSEKI_CNT"] = knjCreateTextBox($objForm, $model->field["KEKKA_KESSEKI_CNT"], "KEKKA_KESSEKI_CNT", 3, 3, $extra);
                    }
                }
            } else {
                $arg["DISP_CONDITION"] = "1";
                $dispFlg = 1;
            }
        }
        knjCreateHidden($objForm, "DISP_CONDITION", $dispFlg);

        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        knjCreateHidden($objForm, "bunkyoKansanCount",  "3");
        $arg["bunkyoKansanCount"] = "3";
        if ($model->field["OUTPUT_KAIKIN"] == '2') {
            // 精勤
            $arg["nozoku"] = "1";
            $val = "11";
            if ($z010 == 'musashinohigashi') {
                $val = "8";
            }
            $arg["data"]["BUNKYO_KANSAN_KESSEKI"] = knjCreateTextBox($objForm, $val, "BUNKYO_KANSAN_KESSEKI", 3, 3, $extra);
            $arg["data"]["BUNKYO_KANSAN_KESSEKI_NOZOKU"] = knjCreateTextBox($objForm, "2", "BUNKYO_KANSAN_KESSEKI_NOZOKU", 3, 3, $extra);
        } else {
            // 皆勤
            $val = "";
            if ($z010 == 'bunkyo') {
                if ($model->field["OUTPUT"] == "2") {
                    $val = "2";
                } else {
                    $val = "0";
                }
            } else {
                $val = "2";
            }
            $arg["data"]["BUNKYO_KANSAN_KESSEKI"] = knjCreateTextBox($objForm, $val, "BUNKYO_KANSAN_KESSEKI", 3, 3, $extra);
            $arg["data"]["BUNKYO_KANSAN_KESSEKI_NOZOKU"] = knjCreateTextBox($objForm, "", "BUNKYO_KANSAN_KESSEKI_NOZOKU", 3, 3, $extra);
        }

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 0);\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //CSVボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 1);\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "CTRL_YEAR",  CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC166F");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "SDATE", $model->control["学期開始日付"][CTRL_SEMESTER]);    //学期開始日
        knjCreateHidden($objForm, "EDATE", $model->control["学期終了日付"][CTRL_SEMESTER]);    //学期終了日
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "AUTH_RESTRICT", (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) ? "1" : "0");
        knjCreateHidden($objForm, "STAFFCD", STAFFCD);
        knjCreateHidden($objForm, "OUTPUTCSV", $model->pdfcsvflg);
        knjCreateHidden($objForm, "knjc166fDispType", $model->Properties["knjc166fDispType"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc166fForm1.html", $arg); 
    }
}
?>
