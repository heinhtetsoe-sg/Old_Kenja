<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje330Form2.php 56587 2017-10-22 12:54:51Z maeshiro $

class knje330Form2 {
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
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje330index.php", "", "edit");


        if ($model->seq != "" && isset($model->schregno) && isset($model->seq) && !isset($model->warning) && $model->cmd != "search") {
            // 警告メッセージを表示しない場合
            $Row = knje330Query::getOneRow($year, $model->seq, $model->schregno);
            $temp_cd = $Row["SCHREGNO"];
            $Row["HOUR_S"]   = substr($Row["STAT_STIME"], 0, 2);
            $Row["MINUTE_S"] = substr($Row["STAT_STIME"], 3, 2);
            $Row["HOUR_E"]   = substr($Row["STAT_ETIME"], 0, 2);
            $Row["MINUTE_E"] = substr($Row["STAT_ETIME"], 3, 2);
        }else{
            $Row =& $model->field;
            $Row["HOUR_S"]   = $model->field["HOUR_S"];
            $Row["MINUTE_S"] = $model->field["MINUTE_S"];
            $Row["HOUR_E"]   = $model->field["HOUR_E"];
            $Row["MINUTE_E"] = $model->field["MINUTE_E"];
            $Row["STAT_NAME"] = $model->field["SCHOOL_NAME"];
        }

        //----
        // NAME_MSTの配列を取得
        $opt_school_sort = knje330Query::getNameMst($year, "E001");
        $opt_howtoexam = knje330Query::getNameMst($year, "E002");
        $opt_decision = knje330Query::getNameMst($year, "E005");
        $opt_planstat = knje330Query::getNameMst($year, "E006");

        //----
        // 学校情報
        $Row2 = knje330Query::getCollegeMst(trim($Row["STAT_CD"]));
        if ($model->cmd == "search") {
// debug echo "f2.".date("r")."[".trim($Row["STAT_CD"])."]<br>";
            $Row["STAT_NAME"]   = $Row2["SCHOOL_NAME"];
            $Row["STAT_CD"]     = $Row2["SCHOOL_CD"];
            $Row["BUNAME"]      = $Row2["BUNAME"];
            $Row["SCHOOL_SORT"] = $Row2["SCHOOL_SORT"];
            $Row["AREA_NAME"]   = $Row2["AREA_NAME"];
            $Row["TELNO"]       = $Row2["TELNO"];
        }

        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "STAT_CD",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "value"       => trim($Row["STAT_CD"] )));

        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "SCHOOL_NAME",
                            "size"        => 80,
                            "extrahtml"   => "class=\"necessary\"",
                            "value"       => $Row["STAT_NAME"]));
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "BUNAME",
                            "size"        => 80,
                            "value"       => $Row["BUNAME"] ));
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "KANAME",
                            "size"        => 80,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => $Row2["KANAME"] ));
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "ZIPCD",
                            "size"        => 10,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => $Row2["ZIPCD"] ));
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "ADDR1",
                            "size"        => 76,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => $Row2["ADDR1"] ));
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "ADDR2",
                            "size"        => 76,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => $Row2["ADDR2"] ));
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "TELNO",
                            "size"        => 16,
                            "maxlength"   => 16,
                            "value"       => $Row["TELNO"] ));
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "SCHOOL_SORT",
                            "size"        => 1,
                            "value"       => $Row["SCHOOL_SORT"],
                            "options"     => $opt_school_sort));
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "AREA_NAME",
                            "size"        => 20,
                            "value"       => $Row["AREA_NAME"] ));

        //----
        // 登録日
        $arg["data"]["HAND_DATE"] = View::popUpCalendar($objForm, "HAND_DATE", str_replace("-", "/", $Row["HAND_DATE"]), "");

        //----
        // 試験日
        $stat_date1 = isset($Row["STAT_DATE1"]) ? $Row["STAT_DATE1"] : $model->control_data["学籍処理日"];
        $arg["data"]["STAT_DATE1"] = View::popUpCalendar($objForm, "STAT_DATE1", str_replace("-", "/", $stat_date1), "");

        // 試験時間（自）
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "HOUR_S",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["HOUR_S"]));

        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "MINUTE_S",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["MINUTE_S"]));

        // 試験時間（至）
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "HOUR_E",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["HOUR_E"]));

        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "MINUTE_E",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"myCheckTime(this)\"; ",
                            "value"       => $Row["MINUTE_E"]));

        // ２次試験日
        $arg["data"]["STAT_DATE2"] = View::popUpCalendar($objForm, "STAT_DATE2", str_replace("-", "/", $Row["STAT_DATE2"]), "");

        //----
        // 応募方法
        knje330Form2::_ae($objForm, $arg["data"], array(
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
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "HOWTOEXAM_REMARK",
                            "size"        => 80,
                            "maxlength"   => 80,
                            "extrahtml"   => $dis_remark,
                            "value"       => $Row["HOWTOEXAM_REMARK"]));

        //----
        // 試験内容
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "CONTENTEXAM",
                            "size"        => 80,
                            "maxlength"   => 80,
                            "value"       => $Row["CONTENTEXAM"]));

        //受験理由
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "textarea",
                            "name"        => "REASONEXAM",
                            "cols"        => 75,
                            "rows"        => 2,
                            "wrap"        => "hard",
                            "value"       => $Row["REASONEXAM"] ));

        // 備考
        $extra = "style=\"height:90px;\"";
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "textarea",
                            "name"        => "THINKEXAM",
                            "cols"        => 51,
                            "rows"        => 6,
                            "wrap"        => "hard",
                            "extrahtml"   => $extra,
                            "value"       => $Row["THINKEXAM"] ));

        // 受験結果
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "DECISION",
                            "size"        => 1,
                            "value"       => $Row["DECISION"],
                            "options"     => $opt_decision
                            ));

        // 進路状況
        knje330Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "PLANSTAT",
                            "size"        => 1,
                            "value"       => $Row["PLANSTAT"],
                            "options"     => $opt_planstat
                            ));
        // ==ボタン==
        // ====特殊ボタン====
        // 入力番号検索ボタン
        knje330Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_input",
                            "value"       => "入力番号検索",
                            "extrahtml"   => "onclick=\"myBtnSubmit('search');\"" ) );

        // 学校マスタ検索ボタン
        knje330Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_college",
                            "value"       => "学校マスタ検索",
                            "extrahtml"   => "onclick=\"wopen('../../X/KNJXSEARCH8/index.php?PATH=/E/KNJE330/knje330index.php&cmd=&target=KNJE330','search',0,0,790,470);\"" ) );

        // ====標準ボタン====
        knje330Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "登 録",
                            "extrahtml"   => "onclick=\"return myBtnSubmit('add');\"" ) );

        knje330Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return myBtnSubmit('update');\"" ) );

        knje330Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return myBtnSubmit('delete');\"" ) );

        knje330Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return myBtnReset('edit');\"" ) );

        knje330Form2::_ae($objForm, $arg["button"], array(
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
            $arg["reload"]  = "window.open('knje330index.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";
        }

        View::toHTML($model, "knje330Form2.html", $arg);
    }
}
?>
