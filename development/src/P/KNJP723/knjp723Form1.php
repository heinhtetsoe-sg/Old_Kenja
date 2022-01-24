<?php

require_once('for_php7.php');

class knjp723Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp723index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //申請月
        $optMonth = array();
        $optMonth[] = array ("label" => "4月申請", "value" => "04");
        $optMonth[] = array ("label" => "7月申請", "value" => "07");
        $model->month = (strlen($model->month)) ? $model->month : $optMonth[0]["value"];
        $extra = "onChange=\"return btn_submit('edit');\"";
        $arg["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->month, $optMonth, $extra, "1");

        //処理年度コンボ
        $extra = "onChange=\"return btn_submit('edit');\"";
        $query = knjp723Query::getYear($model);
        $model->year = $model->year ? $model->year : CTRL_YEAR;
        $arg["YEAR"] = makeCmbReturn($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1, "");

        //学期
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjp723Query::getSemester($model);
        $model->semester = $model->semester ? $model->semester : CTRL_SEMESTER;
        $arg["SEMESTER"] = makeCmbReturn($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "BLANK");

        //校種コンボ
        $arg["schkind"] = "1";
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjp723Query::getSchkind($model);
        $arg["SCHOOL_KIND"] = makeCmbReturn($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //学年
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjp723Query::getGrade($model);
        $arg["GRADE"] = makeCmbReturn($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "");

        //年組
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjp723Query::getHrClass($model);
        $arg["HR_CLASS"] = makeCmbReturn($objForm, $arg, $db, $query, $model->hr_class, "HR_CLASS", $extra, 1, "BLANK");

        //ソート順
        $opt = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SORT{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;


        //更新
        $extra = " id=\"UPDATE_CHK_ALL\" onClick=\"return check_all(this);\" ";
        $arg["UPDATE_CHK_ALL"] = knjCreateCheckBox($objForm, "UPDATE_CHK_ALL", "1", $extra);

        //一覧を取得
        $model->setList = array();
        $hiddenSetKey = "";
        $sep = "";
        $query = knjp723Query::getSchregList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setKey = $row["SCHREGNO"];
            if (isset($model->warning)) {
                foreach ($model->fieldList as $fieldName) {
                    $row[$fieldName] = $model->field[$setKey][$fieldName];
                }
            }
            $model->setList[] = $row;
            //貼り付け用
            $hiddenSetKey .= $sep.$setKey;
            $sep = ",";
        }
        $result->free();
        knjCreateHidden($objForm, "hiddenSetKey", $hiddenSetKey);

        //一覧を表示
        $updFlg = false;
        foreach ($model->setList as $counter => $Row) {
            $setData = array();
            $setData["ATTENDNO"] = $Row["HR_NAME"]."<BR>".$Row["ATTENDNO"]."番";
            $setData["SCHREGNO"] = $Row["SCHREGNO"];
            $setData["NAME"] = $Row["NAME"];
            $setData["NAME_KANA"] = $Row["NAME_KANA"];

            $setKey = $Row["SCHREGNO"];
            $setData["SET_KEY"] = $setKey;
            $setData["BG_COLOR"] = "#FFFFFF";

            //更新
            $extra = " ";
            $setData["UPDATE_CHK"] = knjCreateCheckBox($objForm, "UPDATE_CHK-{$setKey}", "1", $extra);

            //資格認定番号
            $extra = " onPaste=\"return showPaste(this, {$counter});\" ";
            $setData["PASSNO"] = knjCreateTextBox($objForm, $Row["PASSNO"], "PASSNO-{$setKey}", 20, 19, $extra);

            //申請意思
            $extra = ($Row["INTENTION_YES_FLG"] == "1") ? "checked" : "";
            $setData["INTENTION_YES_FLG"] = knjCreateCheckBox($objForm, "INTENTION_YES_FLG-{$setKey}", "1", $extra);
            $extra = ($Row["INTENTION_NO_FLG"] == "1") ? "checked" : "";
            $setData["INTENTION_NO_FLG"] = knjCreateCheckBox($objForm, "INTENTION_NO_FLG-{$setKey}", "1", $extra);

            //申請書
            $extra = ($Row["FORMS_YES_FLG"] == "1") ? "checked" : "";
            $setData["FORMS_YES_FLG"] = knjCreateCheckBox($objForm, "FORMS_YES_FLG-{$setKey}", "1", $extra);
            $extra = ($Row["FORMS_NO_FLG"] == "1") ? "checked" : "";
            $setData["FORMS_NO_FLG"] = knjCreateCheckBox($objForm, "FORMS_NO_FLG-{$setKey}", "1", $extra);

            //添付書類
            //保護者（父）
            $extra = ($Row["FATHER_TAX_CERTIFICATE_FLG"] == "1") ? "checked" : "";
            $setData["FATHER_TAX_CERTIFICATE_FLG"] = knjCreateCheckBox($objForm, "FATHER_TAX_CERTIFICATE_FLG-{$setKey}", "1", $extra);
            $extra = ($Row["FATHER_SPECIAL_TAX_DEC_FLG"] == "1") ? "checked" : "";
            $setData["FATHER_SPECIAL_TAX_DEC_FLG"] = knjCreateCheckBox($objForm, "FATHER_SPECIAL_TAX_DEC_FLG-{$setKey}", "1", $extra);
            $extra = ($Row["FATHER_TAX_NOTICE_FLG"] == "1") ? "checked" : "";
            $setData["FATHER_TAX_NOTICE_FLG"] = knjCreateCheckBox($objForm, "FATHER_TAX_NOTICE_FLG-{$setKey}", "1", $extra);
            //保護者（母）
            $extra = ($Row["MOTHER_TAX_CERTIFICATE_FLG"] == "1") ? "checked" : "";
            $setData["MOTHER_TAX_CERTIFICATE_FLG"] = knjCreateCheckBox($objForm, "MOTHER_TAX_CERTIFICATE_FLG-{$setKey}", "1", $extra);
            $extra = ($Row["MOTHER_SPECIAL_TAX_DEC_FLG"] == "1") ? "checked" : "";
            $setData["MOTHER_SPECIAL_TAX_DEC_FLG"] = knjCreateCheckBox($objForm, "MOTHER_SPECIAL_TAX_DEC_FLG-{$setKey}", "1", $extra);
            $extra = ($Row["MOTHER_TAX_NOTICE_FLG"] == "1") ? "checked" : "";
            $setData["MOTHER_TAX_NOTICE_FLG"] = knjCreateCheckBox($objForm, "MOTHER_TAX_NOTICE_FLG-{$setKey}", "1", $extra);
            //主たる生計維持者
            $extra = ($Row["MAINTAINER_TAX_CERTIFICATE_FLG"] == "1") ? "checked" : "";
            $setData["MAINTAINER_TAX_CERTIFICATE_FLG"] = knjCreateCheckBox($objForm, "MAINTAINER_TAX_CERTIFICATE_FLG-{$setKey}", "1", $extra);
            $extra = ($Row["MAINTAINER_SPECIAL_TAX_DEC_FLG"] == "1") ? "checked" : "";
            $setData["MAINTAINER_SPECIAL_TAX_DEC_FLG"] = knjCreateCheckBox($objForm, "MAINTAINER_SPECIAL_TAX_DEC_FLG-{$setKey}", "1", $extra);
            $extra = ($Row["MAINTAINER_TAX_NOTICE_FLG"] == "1") ? "checked" : "";
            $setData["MAINTAINER_TAX_NOTICE_FLG"] = knjCreateCheckBox($objForm, "MAINTAINER_TAX_NOTICE_FLG-{$setKey}", "1", $extra);

            //奨学給付金事前申込書の有無
            $extra = ($Row["SCHOLARSHIP_PAYMENT_YES_NO_FLG"] == "1") ? "checked" : "";
            $setData["SCHOLARSHIP_PAYMENT_YES_NO_FLG"] = knjCreateCheckBox($objForm, "SCHOLARSHIP_PAYMENT_YES_NO_FLG-{$setKey}", "1", $extra);

            //備考
            $extra = " ";
            $setData["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK-{$setKey}", 30, 30, $extra);

            $arg["data"][] = $setData;
            $updFlg = true;
        } //foreach

        //ボタン作成
        makeBtn($objForm, $arg, $model, $updFlg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp723Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmbReturn(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $updFlg) {
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $extra .= $updFlg ? "" : " disabled ";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
    //CSV
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);
    knjCreateHidden($objForm, "H_HR_CLASS");
}
?>
