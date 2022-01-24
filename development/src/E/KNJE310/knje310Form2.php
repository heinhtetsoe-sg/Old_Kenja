<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje310Form2.php 56587 2017-10-22 12:54:51Z maeshiro $

class knje310Form2 {
    function _ae(&$fm, &$ag, $ary) {
        $name = $ary["name"];
        $fm->ae($ary);
        $ag[$name] = $fm->ge($name);
    }

    function main(&$model) {
        $year = $model->year;
        $schregno = $model->schregno;

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje310index.php", "", "edit");


        if ($model->seq != "" && isset($model->schregno) && isset($model->seq) && !isset($model->warning) && $model->cmd != "search") {
            // 警告メッセージを表示しない場合
            $Row = knje310Query::getOneRow($year, $model->seq, $model->schregno);
            $temp_cd = $Row["SCHREGNO"];
        }else{
            $Row =& $model->field;
        }

        // NAME_MSTの配列を取得
        $opt_school_sort = knje310Query::getNameMst("E001", "off");
        $opt_juken_howto = knje310Query::getNameMst("E002");
        $opt_senkou_kai = knje310Query::getNameMst("E003");
        $opt_senkou_fin = knje310Query::getNameMst2("E004");
        $opt_decision = knje310Query::getNameMst("E005");
        $opt_planstat = knje310Query::getNameMst("E006");

        // 学校・会社情報
        if ($model->cmd == "search") {
            $Row2 = knje310Query::getCollegeOrCompanyMst(trim($Row["STAT_CD"]), $Row["SCHOOL_SORT"]);
            $Row["STAT_CD"]     = $Row2["STAT_CD"];
            $Row["STAT_NAME"]   = $Row2["STAT_NAME"];
            if ("04" < $Row["SCHOOL_SORT"]) {
            } else {
                $Row["BUNAME"]  = $Row2["BUNAME"];
            }
        }

        // SCHREG_ATTENDREC_DAT情報（欠席点）
        $Row["ATTEND"] = knje310Query::getAttend($model->schregno);

        // SCHREG_STUDYREC_DAT情報（評定平均値点）
        $Row["AVG"] = knje310Query::getAvg($model->schregno);

        //----
        // 登録日
        $toroku_date = isset($Row["TOROKU_DATE"]) ? $Row["TOROKU_DATE"] : $model->control_data["学籍処理日"];
        $arg["data"]["TOROKU_DATE"] = View::popUpCalendar($objForm, "TOROKU_DATE", str_replace("-", "/", $toroku_date), "");

        //----
        // 分類
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "SCHOOL_SORT",
                            "size"        => 1,
                            "value"       => $Row["SCHOOL_SORT"],
                            "extrahtml"   => "onChange=\"myDisableText(this);\" ",
                            "options"     => $opt_school_sort
                            ));

        //----進路先
        // 学校・会社コード
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "STAT_CD",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "value"       => trim($Row["STAT_CD"] )));

        // 学校・会社名
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "STAT_NAME",
                            "size"        => 70,
                            "extrahtml"   => "class=\"necessary\"",
                            "value"       => $Row["STAT_NAME"]));

        //----学校情報
        // 学　　部
        $dis_buname = ("04" < $Row["SCHOOL_SORT"]) ? "disabled class=\"unedit_ope\"" : "";
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "BUNAME",
                            "size"        => 60,
                            "extrahtml"   => $dis_buname,
                            "value"       => $Row["BUNAME"] ));

        // 受験方法
        $dis_juken_howto = ("04" < $Row["SCHOOL_SORT"]) ? "disabled " : "";
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "JUKEN_HOWTO",
                            "size"        => 1,
                            "value"       => $Row["JUKEN_HOWTO"],
                            "extrahtml"   => $dis_juken_howto,
                            "options"     => $opt_juken_howto
                            ));

        // 推薦基準
        $dis_recommend = ("04" < $Row["SCHOOL_SORT"]) ? "disabled class=\"unedit_ope\"" : "";
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "RECOMMEND",
                            "size"        => 60,
                            "extrahtml"   => $dis_recommend,
                            "value"       => $Row["RECOMMEND"] ));

        //----
        // 校内選考会
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "SENKOU_KAI",
                            "size"        => 1,
                            "value"       => $Row["SENKOU_KAI"],
                            "options"     => $opt_senkou_kai
                            ));

        //----
        // 校内選考結果
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "NM_SENKOU_FIN",
                            "size"        => 10,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => $opt_senkou_fin[$Row["SENKOU_FIN"]] ));

        //----
        // 求人番号
        $dis_senkou_no = ("04" < $Row["SCHOOL_SORT"]) ? "" : "disabled class=\"unedit_ope\"";
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "SENKOU_NO",
                            "size"        => 10,
                            "maxlength"   => 5,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" " .$dis_senkou_no,
                            "value"       => $Row["SENKOU_NO"] ));

        //----
        // 備考
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "REMARK",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "value"       => $Row["REMARK"]));

        //----
        // 受験結果
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "DECISION",
                            "size"        => 1,
                            "value"       => $Row["DECISION"],
                            "options"     => $opt_decision
                            ));

        //----
        // 進路状況
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "select",
                            "name"        => "PLANSTAT",
                            "size"        => 1,
                            "value"       => $Row["PLANSTAT"],
                            "options"     => $opt_planstat
                            ));

        //----成績情報
        // 欠席点
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "SW_ATTEND",
                            "size"        => 5,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => $Row["ATTEND"] ));

        // 評定平均値点
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "SW_AVG",
                            "size"        => 5,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => round($Row["AVG"] * 8) ));

        // 統一テスト
        $dis_test = ("04" < $Row["SCHOOL_SORT"]) ? "disabled class=\"unedit_ope\"" : "";
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "TEST",
                            "size"        => 5,
                            "extrahtml"   => "onblur=\"this.value=toFloat(this.value)\" " .$dis_test,
                            "value"       => $Row["TEST"] ));

        // 総合点
        knje310Form2::_ae($objForm, $arg["data"], array(
                            "type"        => "text",
                            "name"        => "SW_SEISEKI",
                            "size"        => 5,
                            "extrahtml"   => "disabled class=\"unedit\"",
                            "value"       => $Row["SEISEKI"] ));

        // ==ボタン==
        // ====特殊ボタン====
        // 入力番号検索ボタン
        knje310Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_input",
                            "value"       => "入力番号検索",
                            "extrahtml"   => "onclick=\"myBtnSubmit('search');\"" ) );

        // 学校・会社マスタ検索ボタン
        knje310Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_college",
                            "value"       => "進路先マスタ検索",
                            "extrahtml"   => "onclick=\"myBtnWopen(this);\"" ) );

        // 計 算ボタン
        $dis_btn_sum = ("04" < $Row["SCHOOL_SORT"]) ? "disabled " : "";
        knje310Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_sum",
                            "value"       => "計 算",
                            "extrahtml"   => "onclick=\"return myBtnSum();\" " .$dis_btn_sum ) );

        // ====標準ボタン====
        knje310Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "登 録",
                            "extrahtml"   => "onclick=\"return myBtnSubmit('add');\" " ) );

        knje310Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return myBtnSubmit('update');\" " ) );

        knje310Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return myBtnSubmit('delete');\" " ) );

        knje310Form2::_ae($objForm, $arg["button"], array(
                            "type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return myBtnReset('edit');\"" ) );

        knje310Form2::_ae($objForm, $arg["button"], array(
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
                            "name"      => "YEAR",
                            "value"     => $model->year) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEQ",
                            "value"     => $model->seq) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SENKOU_FIN",
                            "value"     => $Row["SENKOU_FIN"]));
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTEND",
                            "value"     => $Row["ATTEND"]));
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "AVG",
                            "value"     => round($Row["AVG"] * 10 / 10,1)));// DBアップデート用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "AVG_SUM",
                            "value"     => round($Row["AVG"] * 8,2)));// 計算用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEISEKI",
                            "value"     => $Row["SEISEKI"]));

        if ($temp_cd=="") $temp_cd = $model->field["temp_cd"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "temp_cd",
                            "value"     => $temp_cd) );

        $cd_change = false;
        if ($temp_cd==$Row["SCHREGNO"] ) $cd_change = true;

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "clear" && ($cd_change==true || $model->isload != 1) && !isset($model->warning)) {
            $arg["reload"]  = "window.open('knje310index.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";
        }

        View::toHTML($model, "knje310Form2.html", $arg);
    }
}
?>
