<?php

require_once('for_php7.php');

class knjf321Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
           $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf321index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $date = ($model->work_date) ? str_replace("/", "-", $model->work_date) : CTRL_DATE;
            $query = knjf321Query::getRow($model->staffcd, $date);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //デフォルト値
        $model->year = ($model->year) ? $model->year : CTRL_YEAR;

        //執務日付作成
        $Row["WORK_DATE"] = ($Row["WORK_DATE"]) ? $Row["WORK_DATE"] : CTRL_DATE;
        $arg["data"]["WORK_DATE"] = View::popUpCalendar($objForm, "WORK_DATE", str_replace("-","/",$Row["WORK_DATE"]));

        //時間帯
        //午前
        $extra = "id=\"REMARK1_901\"";
        if ($Row["REMARK1_901"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK1_901"] = knjCreateCheckBox($objForm, "REMARK1_901", "1", $extra);
        //午後
        $extra = "id=\"REMARK2_901\"";
        if ($Row["REMARK2_901"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK2_901"] = knjCreateCheckBox($objForm, "REMARK2_901", "1", $extra);
        //全日
        $extra = "id=\"REMARK3_901\"";
        if ($Row["REMARK3_901"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK3_901"] = knjCreateCheckBox($objForm, "REMARK3_901", "1", $extra);

        //執務者コンボ作成
        $Row["STAFFCD"] = ($Row["STAFFCD"]) ? $Row["STAFFCD"] : STAFFCD;
        $query = knjf321Query::getStaffMst($model);
        makeCmb($objForm, $arg, $db, $query, "STAFFCD", $Row["STAFFCD"], "", 1);

        //執務場所
        //学校
        $extra = "id=\"REMARK1_000\"";
        if ($Row["REMARK1_000"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK1_000"] = knjCreateCheckBox($objForm, "REMARK1_000", "1", $extra);
        //校外施設
        $extra = "id=\"REMARK2_000\"";
        if ($Row["REMARK2_000"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK2_000"] = knjCreateCheckBox($objForm, "REMARK2_000", "1", $extra);
        //診療所
        $extra = "id=\"REMARK3_000\"";
        if ($Row["REMARK3_000"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK3_000"] = knjCreateCheckBox($objForm, "REMARK3_000", "1", $extra);
        //研修会
        $extra = "id=\"REMARK4_000\"";
        if ($Row["REMARK4_000"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK4_000"] = knjCreateCheckBox($objForm, "REMARK4_000", "1", $extra);
        //講習会
        $extra = "id=\"REMARK5_000\"";
        if ($Row["REMARK5_000"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK5_000"] = knjCreateCheckBox($objForm, "REMARK5_000", "1", $extra);
        //その他チェック
        $extra = "id=\"REMARK6_000\" onclick=\"etc_check('000');\"";
        if ($Row["REMARK6_000"] == "1") {
            $extra .= "checked='checked' ";
            $disabled_etc0 = "";
        } else {
            $extra .= "";
            $disabled_etc0 = "disabled";
        }
        $arg["data"]["REMARK6_000"] = knjCreateCheckBox($objForm, "REMARK6_000", "1", $extra);
        //その他テキスト
        $extra = "";
        $arg["data"]["REMARK7_000"] = knjCreateTextBox($objForm, $Row["REMARK7_000"], "REMARK7_000", 60, 30, $extra.$disabled_etc0);

        //1.学校保健委員会等
        $extra = "id=\"REMARK1_001\" onclick=\"etc_check('001');\"";
        if ($Row["REMARK1_001"] == "1") {
            $extra .= "checked='checked' ";
            $disabled1 = "";
        } else {
            $extra .= "";
            $disabled1 = "disabled";
        }
        $arg["data"]["REMARK1_001"] = knjCreateCheckBox($objForm, "REMARK1_001", "1", $extra);
        //(1)学校保健計画立案チェック
        $extra = "id=\"REMARK2_001\"";
        if ($Row["REMARK2_001"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK2_001"] = knjCreateCheckBox($objForm, "REMARK2_001", "1", $extra.$disabled1);
        //(2)その他チェック
        $extra = "id=\"REMARK3_001\" onclick=\"etc_check('001');\"";
        if ($Row["REMARK3_001"] == "1") {
            $extra .= "checked='checked' ";
            $disabled_etc1 = "";
        } else {
            $extra .= "";
            if ($disabled1 == "") {
                $disabled_etc1 = "disabled";
            }
        }
        $arg["data"]["REMARK3_001"] = knjCreateCheckBox($objForm, "REMARK3_001", "1", $extra.$disabled1);
        //(2)その他テキスト
        $extra = "";
        $arg["data"]["REMARK4_001"] = knjCreateTextBox($objForm, $Row["REMARK4_001"], "REMARK4_001", 60, 30, $extra.$disabled1.$disabled_etc1);

        //2.学校環境衛生に関する指導助言
        $extra = "id=\"REMARK1_002\"";
        if ($Row["REMARK1_002"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK1_002"] = knjCreateCheckBox($objForm, "REMARK1_002", "1", $extra);

        //3.定期健康診断
        $extra = "id=\"REMARK1_003\" onclick=\"etc_check('003');\"";
        if ($Row["REMARK1_003"] == "1") {
            $extra .= "checked='checked' ";
            $disabled3 = "";
        } else {
            $extra .= "";
            $disabled3 = "disabled";
        }
        $arg["data"]["REMARK1_003"] = knjCreateCheckBox($objForm, "REMARK1_003", "1", $extra);
        //(1)定期健康相談チェック
        $extra = "id=\"REMARK2_003\"";
        if ($Row["REMARK2_003"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK2_003"] = knjCreateCheckBox($objForm, "REMARK2_003", "1", $extra.$disabled3);
        //(2)結核検診チェック
        $extra = "id=\"REMARK3_003\"";
        if ($Row["REMARK3_003"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK3_003"] = knjCreateCheckBox($objForm, "REMARK3_003", "1", $extra.$disabled3);
        //(3)心臓検診チェック
        $extra = "id=\"REMARK4_003\" onclick=\"etc_check('003');\"";
        if ($Row["REMARK4_003"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK4_003"] = knjCreateCheckBox($objForm, "REMARK4_003", "1", $extra.$disabled3);
        //(4)その他チェック
        $extra = "id=\"REMARK5_003\" onclick=\"etc_check('003');\"";
        if ($Row["REMARK5_003"] == "1") {
            $extra .= "checked='checked' ";
            $disabled_etc3 = "";
        } else {
            $extra .= "";
            if ($disabled3 == "") {
                $disabled_etc3 = "disabled";
            }
        }
        $arg["data"]["REMARK5_003"] = knjCreateCheckBox($objForm, "REMARK5_003", "1", $extra.$disabled3);
        //(4)その他テキスト
        $extra = "";
        $arg["data"]["REMARK6_003"] = knjCreateTextBox($objForm, $Row["REMARK6_003"], "REMARK6_003", 60, 30, $extra.$disabled3.$disabled_etc3);

        //4.臨時健康診断
        $extra = "id=\"REMARK1_004\" onclick=\"etc_check('004');\"";
        if ($Row["REMARK1_004"] == "1") {
            $extra .= "checked='checked' ";
            $disabled4 = "";
        } else {
            $extra .= "";
            $disabled4 = "disabled";
        }
        $arg["data"]["REMARK1_004"] = knjCreateCheckBox($objForm, "REMARK1_004", "1", $extra);
        //(1)夏季施設等チェック
        $extra = "id=\"REMARK2_004\"";
        if ($Row["REMARK2_004"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK2_004"] = knjCreateCheckBox($objForm, "REMARK2_004", "1", $extra.$disabled4);
        //(2)修学旅行等チェック
        $extra = "id=\"REMARK3_004\"";
        if ($Row["REMARK3_004"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK3_004"] = knjCreateCheckBox($objForm, "REMARK3_004", "1", $extra.$disabled4);
        //(3)その他チェック
        $extra = "id=\"REMARK4_004\" onclick=\"etc_check('004');\"";
        if ($Row["REMARK4_004"] == "1") {
            $extra .= "checked='checked' ";
            $disabled_etc4 = "";
        } else {
            $extra .= "";
            if ($disabled4 == "") {
                $disabled_etc4 = "disabled";
            }
        }
        $arg["data"]["REMARK4_004"] = knjCreateCheckBox($objForm, "REMARK4_004", "1", $extra.$disabled4);
        //(3)その他テキスト
        $extra = "";
        $arg["data"]["REMARK5_004"] = knjCreateTextBox($objForm, $Row["REMARK5_004"], "REMARK5_004", 60, 30, $extra.$disabled4.$disabled_etc4);

        //5.伝染病及び食中毒発生時の指導助言及び予防処置
        $extra = "id=\"REMARK1_005\"";
        if ($Row["REMARK1_005"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK1_005"] = knjCreateCheckBox($objForm, "REMARK1_005", "1", $extra);

        //6.健康相談及び保健指導
        $extra = "id=\"REMARK1_006\" onclick=\"etc_check('006');\"";
        if ($Row["REMARK1_006"] == "1") {
            $extra .= "checked='checked' ";
            $disabled6 = "";
        } else {
            $extra .= "";
            $disabled6 = "disabled";
        }
        $arg["data"]["REMARK1_006"] = knjCreateCheckBox($objForm, "REMARK1_006", "1", $extra);
        //(1)健康相談チェック
        $extra = "id=\"REMARK2_006\"";
        if ($Row["REMARK2_006"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK2_006"] = knjCreateCheckBox($objForm, "REMARK2_006", "1", $extra.$disabled6);
        //(2)保健指導チェック
        $extra = "id=\"REMARK3_006\"";
        if ($Row["REMARK3_006"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK3_006"] = knjCreateCheckBox($objForm, "REMARK3_006", "1", $extra.$disabled6);
        //(3)保険講話チェック
        $extra = "id=\"REMARK4_006\" onclick=\"etc_check('006');\"";
        if ($Row["REMARK4_006"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK4_006"] = knjCreateCheckBox($objForm, "REMARK4_006", "1", $extra.$disabled6);
        //(4)その他チェック
        $extra = "id=\"REMARK5_006\" onclick=\"etc_check('006');\"";
        if ($Row["REMARK5_006"] == "1") {
            $extra .= "checked='checked' ";
            $disabled_etc6 = "";
        } else {
            $extra .= "";
            if ($disabled6 == "") {
                $disabled_etc6 = "disabled";
            }
        }
        $arg["data"]["REMARK5_006"] = knjCreateCheckBox($objForm, "REMARK5_006", "1", $extra.$disabled6);
        //(4)その他テキスト
        $extra = "";
        $arg["data"]["REMARK6_006"] = knjCreateTextBox($objForm, $Row["REMARK6_006"], "REMARK6_006", 60, 30, $extra.$disabled6.$disabled_etc6);

        //7.校長の求めによる救急処置
        $extra = "id=\"REMARK1_007\"";
        if ($Row["REMARK1_007"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK1_007"] = knjCreateCheckBox($objForm, "REMARK1_007", "1", $extra);

        //8.学校保健に関する研修会、講習会等
        $extra = "id=\"REMARK1_008\"";
        if ($Row["REMARK1_008"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REMARK1_008"] = knjCreateCheckBox($objForm, "REMARK1_008", "1", $extra);

        //9.その他
        $extra = "id=\"REMARK1_009\" onclick=\"etc_check('009');\"";
        if ($Row["REMARK1_009"] == "1") {
            $extra .= "checked='checked' ";
            $disabled9 = "";
        } else {
            $extra .= "";
            $disabled9 = "disabled";
        }
        $arg["data"]["REMARK1_009"] = knjCreateCheckBox($objForm, "REMARK1_009", "1", $extra);
        //その他テキスト
        $extra = "";
        $arg["data"]["REMARK2_009"] = knjCreateTextBox($objForm, $Row["REMARK2_009"], "REMARK2_009", 60, 30, $extra.$disabled9);

        //記事テキストボックス
        $extra = "style=\"height:170px;\"";
        $arg["data"]["NEWS_STORY"] = knjCreateTextArea($objForm, "NEWS_STORY", 12, 41, "soft", $extra, $Row["NEWS_STORY"]);

        //特記事項テキストボックス
        $extra = "style=\"height:170px;\"";
        $arg["data"]["SPECIAL_REPORT"] = knjCreateTextArea($objForm, "SPECIAL_REPORT", 12, 41, "soft", $extra, $Row["SPECIAL_REPORT"]);

        //印影校長
        $extra  = "id=INEI_PRI";
        $extra .= $model->field["INEI_PRI"] == "1" ? " checked " : "";
        $arg["data"]["INEI_PRI"] = knjCreateCheckBox($objForm, "INEI_PRI", "1", $extra);

        //印影執務者
        $extra  = "id=INEI_STF";
        $extra .= $model->field["INEI_STF"] == "1" ? " checked " : "";
        $arg["data"]["INEI_STF"] = knjCreateCheckBox($objForm, "INEI_STF", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjf321index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf321Form2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
    //追加
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", " onclick=\"return btn_submit('add');\"");
    //修正
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");
    //削除
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");
    //取消
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", PROGRAMID);
}
?>
