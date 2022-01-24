<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje340Form2.php 56587 2017-10-22 12:54:51Z maeshiro $

class knje340Form2 {
    function _ae(&$fm, &$ag, $ary) {
        $name = $ary["name"];
        $fm->ae($ary);
        $ag[$name] = $fm->ge($name);
    }

    function _aeAs(&$fm, &$ag, $nm, $ary) {
        $name = $ary["name"];
        $fm->ae($ary);
        $ag[$nm] = $fm->ge($name);
    }

    function main(&$model) {
        $year = $model->control_data["年度"];
        $schregno = $model->schregno;

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje340index.php", "", "edit");


        if ($model->seq != "" && isset($model->schregno) && isset($model->seq) && !isset($model->warning) && $model->cmd != "search") {
            // 警告メッセージを表示しない場合
            $Row = knje340Query::getOneRow($year, $model->seq, $model->schregno);
            $temp_cd = $Row["SCHREGNO"];
            $Row["HOUR_S_JOB"]      = substr($Row["JOB_STIME"],0,2);
            $Row["MINUTE_S_JOB"]    = substr($Row["JOB_STIME"],3,2);
            $Row["HOUR_E_JOB"]      = substr($Row["JOB_ETIME"],0,2);
            $Row["MINUTE_E_JOB"]    = substr($Row["JOB_ETIME"],3,2);
            $Row["HOUR_S_JOBEX"]    = substr($Row["JOBEX_STIME"],0,2);
            $Row["MINUTE_S_JOBEX"]  = substr($Row["JOBEX_STIME"],3,2);
            $Row["HOUR_E_JOBEX"]    = substr($Row["JOBEX_ETIME"],0,2);
            $Row["MINUTE_E_JOBEX"]  = substr($Row["JOBEX_ETIME"],3,2);
        }else{
            $Row =& $model->field;
            $Row["HOUR_S_JOB"]      = $model->field["HOUR_S_JOB"];
            $Row["MINUTE_S_JOB"]    = $model->field["MINUTE_S_JOB"];
            $Row["HOUR_E_JOB"]      = $model->field["HOUR_E_JOB"];
            $Row["MINUTE_E_JOB"]    = $model->field["MINUTE_E_JOB"];
            $Row["HOUR_S_JOBEX"]    = $model->field["HOUR_S_JOBEX"];
            $Row["MINUTE_S_JOBEX"]  = $model->field["MINUTE_S_JOBEX"];
            $Row["HOUR_E_JOBEX"]    = $model->field["HOUR_E_JOBEX"];
            $Row["MINUTE_E_JOBEX"]  = $model->field["MINUTE_E_JOBEX"];
            $Row["STAT_NAME"]       = $model->field["COMPANY_NAME"];
        }

        //----
        // NAME_MSTの配列を取得
        $opt_howtoexam = knje340Query::getNameMst($year, "E002");
        $opt_decision = knje340Query::getNameMst($year, "E005");
        $opt_planstat = knje340Query::getNameMst($year, "E006");

        //----
        // 会社情報
        $Row2 = knje340Query::getCompanyMst(trim($Row["STAT_CD"]));
        if ($model->cmd == "search") {
// debug echo "f2.".date("r")."[".trim($Row["STAT_CD"])."]<br>";
            $Row["STAT_NAME"]       = $Row2["COMPANY_NAME"];
            $Row["STAT_CD"]         = $Row2["COMPANY_CD"];
            $Row["SHUSHOKU_ADDR"]   = $Row2["SHUSHOKU_ADDR"];
            $Row["TELNO"]           = $Row2["TELNO"];
        }

        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "STAT_CD",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "value"       => trim($Row["STAT_CD"] )));

        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "COMPANY_NAME",
                            "size"        => 80,
                            "extrahtml"   => "class=\"necessary\"",
                            "value"       => $Row["STAT_NAME"]));
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "SHUSHOKU_ADDR",
                            "size"        => 80,
                            "value"       => $Row["SHUSHOKU_ADDR"] ));
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "ZIPCD",
                            "size"        => 10,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => $Row2["ZIPCD"] ));
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "ADDR1",
                            "size"        => 76,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => $Row2["ADDR1"] ));
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "ADDR2",
                            "size"        => 76,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => $Row2["ADDR2"] ));
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "TELNO",
                            "size"        => 16,
                            "maxlength"   => 16,
                            "value"       => $Row["TELNO"] ));

        //----
        // 登録日
        $arg["data"]["HAND_DATE"] = View::popUpCalendar($objForm, "HAND_DATE", str_replace("-", "/", $Row["HAND_DATE"]), "");

        //----
        // 応募方法
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "HOWTOEXAM",
                            "size"        => 1,
                            "value"       => $Row["HOWTOEXAM"],
                            "extrahtml"   => "onChange=\"myDisableText(this);\" ",
                            "options"     => $opt_howtoexam
                            ));

        // その他理由
        $dis_remark = "";
        if ($Row["HOWTOEXAM"] != "99") {
            $dis_remark = "disabled class=\"unedit_ope\"";
        }
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "HOWTOEXAM_REMARK",
                            "size"        => 80,
                            "maxlength"   => 80,
                            "extrahtml"   => $dis_remark,
                            "value"       => $Row["HOWTOEXAM_REMARK"]));

        //----会社訪問
        // 日
        $job_date1 = isset($Row["JOB_DATE1"]) ? $Row["JOB_DATE1"] : $model->control_data["学籍処理日"];
        $arg["data"]["JOB_DATE1"] = View::popUpCalendar($objForm, "JOB_DATE1", str_replace("-", "/", $job_date1), "");

        // 時間（自）
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "HOUR_S_JOB",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["HOUR_S_JOB"]));

        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "MINUTE_S_JOB",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["MINUTE_S_JOB"]));

        // 時間（至）
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "HOUR_E_JOB",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["HOUR_E_JOB"]));

        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "MINUTE_E_JOB",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["MINUTE_E_JOB"]));

        //----
        // 携帯品
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "JOB_REMARK",
                            "size"        => 80,
                            "maxlength"   => 80,
                            "value"       => $Row["JOB_REMARK"]));

        //----
        // 内容
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "textarea",
                            "name"        => "JOB_CONTENT",
                            "cols"        => 75,
                            "rows"        => 2,
                            "wrap"        => "hard",
                            "value"       => $Row["JOB_CONTENT"] ));

        //----
        // 備考
        $extra = "style=\"height:90px;\"";
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "textarea",
                            "name"        => "JOB_THINK",
                            "cols"        => 51,
                            "rows"        => 6,
                            "wrap"        => "hard",
                            "extrahtml"   => $extra,
                            "value"       => $Row["JOB_THINK"] ));

        //----入社試験
        // 日
        $arg["data"]["JOBEX_DATE1"] = View::popUpCalendar($objForm, "JOBEX_DATE1", str_replace("-", "/", $Row["JOBEX_DATE1"]), "");

        // 時間（自）
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "HOUR_S_JOBEX",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["HOUR_S_JOBEX"]));

        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "MINUTE_S_JOBEX",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["MINUTE_S_JOBEX"]));

        // 時間（至）
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "HOUR_E_JOBEX",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["HOUR_E_JOBEX"]));

        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "MINUTE_E_JOBEX",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["MINUTE_E_JOBEX"]));

        //----
        // 携帯品
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "JOBEX_REMARK",
                            "size"        => 80,
                            "maxlength"   => 80,
                            "value"       => $Row["JOBEX_REMARK"]));

        //----
        // 内容
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "textarea",
                            "name"        => "JOBEX_CONTENT",
                            "cols"        => 75,
                            "rows"        => 2,
                            "wrap"        => "hard",
                            "value"       => $Row["JOBEX_CONTENT"] ));

        //----
        // 感想
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "textarea",
                            "name"        => "JOBEX_THINK",
                            "cols"        => 75,
                            "rows"        => 4,
                            "wrap"        => "hard",
                            "value"       => $Row["JOBEX_THINK"] ));

        //----
        // 受験結果
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "DECISION",
                            "size"        => 1,
                            "value"       => $Row["DECISION"],
                            "options"     => $opt_decision
                            ));

        //----
        // 進路状況
        knje340Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "PLANSTAT",
                            "size"        => 1,
                            "value"       => $Row["PLANSTAT"],
                            "options"     => $opt_planstat
                            ));
        // ==ボタン==
        // ====特殊ボタン====
        // 入力番号検索ボタン
        knje340Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_input",
                            "value"       => "入力番号検索",
                            "extrahtml"   => "onclick=\"myBtnSubmit('search');\"" ) );

        // 学校マスタ検索ボタン
        knje340Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_company",
                            "value"       => "会社マスタ検索",
                            "extrahtml"   => "onclick=\"wopen('../../X/KNJXSEARCH9/index.php?PATH=/E/KNJE340/knje340index.php&cmd=&target=KNJE340','search',0,0,790,470);\"" ) );

        // ====標準ボタン====
        knje340Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "登 録",
                            "extrahtml"   => "onclick=\"return myBtnSubmit('add');\"" ) );

        knje340Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return myBtnSubmit('update');\"" ) );

        knje340Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return myBtnSubmit('delete');\"" ) );

        knje340Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return myBtnReset('edit');\"" ) );

        knje340Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        // hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]));
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEQ",
                            "value"     => $model->seq) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SONOTA",
                            "value"     => "99") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STAT_KIND",
                            "value"     => $Row["STAT_KIND"]));

        if ($temp_cd=="") $temp_cd = $model->field["temp_cd"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "temp_cd",
                            "value"     => $temp_cd) );

        $cd_change = false;
        if ($temp_cd==$Row["SCHREGNO"] ) $cd_change = true;

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "clear" && ($cd_change==true || $model->isload != 1) && !isset($model->warning)) {
            $arg["reload"]  = "window.open('knje340index.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";
        }

        View::toHTML($model, "knje340Form2.html", $arg);
    }
}
?>
