<?php

require_once('for_php7.php');

class knjp700Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp700index.php", "", "right_list");

        $db = Query::dbCheckOut();

        $arg["YEAR"]        = $model->year;
        $arg["YEAR_ADD"]    = $model->year_add;

        //ALLチェック
        $arg["CHECKALL"] = $this->createCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //データ作成
        $this->makeData($objForm, $arg, $db, $model);

        //実行ボタンを作成する
        $arg["btn_execute"] = $this->createBtn($objForm, "btn_execute", "実 行", "onclick=\"return btn_submit('execute');\"");

        //削除ボタンを作成する
        $arg["btn_end"] = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "YEAR_ADD", $model->year_add);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp700Form1.html", $arg);
    }

    //
    function makeData(&$objForm, &$arg, $db, $model)
    {
        $setval = array();  //出力データ配列
        $getData = array();
        $getData = $this->setData($model);
        $disabled = "";
        for ($i = 0; $i < get_count($getData); $i++) {
            $query = knjp700Query::cnt_table($getData[$i]["VALUE"]);
            $exist_flg = $db->getOne($query) > 0 ? true : false;
            if ($exist_flg) {
                $setval["KEKKA"]   = $db->getOne(knjp700Query::getkekka($model->year, $model->year_add, $getData[$i]["VALUE"]));
            } else {
                $setval["KEKKA"]   = '今年度データなし';
            }
            $setval["MSTNAME"] = $getData[$i]["NAME"];
            if ($setval["KEKKA"] != "") {
                $disabled = "disabled";
            } else {
                $disabled = "";
            }
            $setval["CHECKED"] = $this->createCheckBox($objForm, "CHECKED", $getData[$i]["VALUE"], $disabled, "1");
            $arg["data"][] = $setval;
        }
    }

    //表示用データ配列作成
    function setData($model)
    {
        $setD = array();
        $setD[] = ( array("NAME" => "入金科目マスタ",                   "VALUE" => "COLLECT_L_MST"));
        $setD[] = ( array("NAME" => "入金項目マスタ",                   "VALUE" => "COLLECT_M_MST"));
        $setD[] = ( array("NAME" => "入金グループマスタ",               "VALUE" => "COLLECT_GRP_MST"));
        $setD[] = ( array("NAME" => "入金グループデータ",               "VALUE" => "COLLECT_GRP_DAT"));
        $setD[] = ( array("NAME" => "学校入金方法基本データ",           "VALUE" => "COLLECT_DEFAULT_SETTINGS_MST"));
        $setD[] = ( array("NAME" => "入金項目別入金計画データ",         "VALUE" => "COLLECT_MONTH_GRP_DAT"));
        $setD[] = ( array("NAME" => "自治体補助金マスタ",               "VALUE" => "REDUCTION_MST"));
        $setD[] = ( array("NAME" => "就学支援金マスタ",                 "VALUE" => "REDUCTION_COUNTRY_MST"));
        $setD[] = ( array("NAME" => "学校減免マスタ",                   "VALUE" => "REDUCTION_SCHOOL_MST"));
        $setD[] = ( array("NAME" => "徴収金マスタ(収入・支出)",         "VALUE" => "LEVY_L_MST"));
        $setD[] = ( array("NAME" => "徴収金項目マスタ(収入・支出)",     "VALUE" => "LEVY_M_MST"));
        $setD[] = ( array("NAME" => "徴収金細目マスタ(収入・支出)",     "VALUE" => "LEVY_S_MST"));
        $setD[] = ( array("NAME" => "引落／返金フォーマットCSV出力設定","VALUE" => "COLLECT_SCHOOL_BANK_MST"));
        $setD[] = ( array("NAME" => "CSV取込ヘッダ",                    "VALUE" => "COLLECT_CSV_HEAD_CAPTURE_DAT"));
        $setD[] = ( array("NAME" => "CSV取込情報",                      "VALUE" => "COLLECT_CSV_INFO_DAT"));
        $setD[] = ( array("NAME" => "CSV取込項目マスタ",                "VALUE" => "COLLECT_CSV_GRP_MST"));
        $setD[] = ( array("NAME" => "CSV取込項目データ",                "VALUE" => "COLLECT_CSV_GRP_DAT"));
        $setD[] = ( array("NAME" => "特待区分マスタ",                   "VALUE" => "SCHOLARSHIP_MST"));
        if ($model->Properties["useSIGELsystem"] == "1") {
            $setD[] = ( array("NAME" => "SIGELコースマッピング",            "VALUE" => "COLLECT_SGL_COURSE_MAPPING_DAT"));
            $setD[] = ( array("NAME" => "SIGELスカラシップマッピング",      "VALUE" => "COLLECT_SGL_SCHOLARSHIP_MAPPING_DAT"));
            $setD[] = ( array("NAME" => "SIGEL科目項目マッピング",          "VALUE" => "LEVY_SGL_LMS_MAPPING_MST"));
        }
        return $setD;
    }

    //チェックボックス作成
    function createCheckBox(&$objForm, $name, $value, $extra, $multi) {

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));

        return $objForm->ge($name);
    }

    //ボタン作成
    function createBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae( array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
    }

    //Hidden作成ae
    function createHiddenAe($name, $value = "")
    {
        $opt_hidden = array();
        $opt_hidden = array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value);
        return $opt_hidden;
    }

}
?>
