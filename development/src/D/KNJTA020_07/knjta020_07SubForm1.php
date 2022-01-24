<?php

require_once('for_php7.php');


class knjta020_07SubForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjta020_07SubForm1", "POST", "knjta020_07index.php", "", "knjta020_07SubForm1");
        //DB接続
        $db = Query::dbCheckOut();
        //カレンダー呼び出し
        $my = new mycalendar();

        //更新後ポップアップ閉じる
        if ($model->cmd == "submain" && !isset($model->warning)) {
            $arg["reload"] = " btn_submit('');";
        }

        //更新前データを取得
        $Row = array();
        if (isset($model->kojinNo)) {
//echo "subFormＤＢデータ:" .$model->kojinNo ."<BR>";
            $query = knjta020_07Query::getKojinInfo($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //初期値
            $Row["KIKON_FLG"] = ($Row["KIKON_FLG"] == "") ? "1" : $Row["KIKON_FLG"];
            $Row["TSUUGAKU_DIV"] = ($Row["TSUUGAKU_DIV"] == "") ? "1" : $Row["TSUUGAKU_DIV"];
            $Row["STK_SHITAKUKIN_TAIYO_DIV"] = ($Row["STK_SHITAKUKIN_TAIYO_DIV"] == "") ? "1" : $Row["STK_SHITAKUKIN_TAIYO_DIV"];
        }

        //変更フラグ
        $repFlg = false;
        $repFlgSGK = false;
        $repFlgSTK = false;

        //画面データと比較・・・注意：初期値も考慮すること
        if ($Row["FAMILY_NAME"]         != $model->field["FAMILY_NAME"] || 
            $Row["FIRST_NAME"]          != $model->field["FIRST_NAME"] || 
            $Row["FAMILY_NAME_KANA"]    != $model->field["FAMILY_NAME_KANA"] || 
            $Row["FIRST_NAME_KANA"]     != $model->field["FIRST_NAME_KANA"] || 
            $Row["BIRTHDAY"]            != $model->field["BIRTHDAY"] || 
            $Row["KIKON_FLG"]           != $model->field["KIKON_FLG"] || 
            $Row["ZIPCD"]               != $model->field["ZIPCD"] || 
            $Row["CITYCD"]              != $model->field["CITYCD"] || 
            $Row["ADDR1"]               != $model->field["ADDR1"] || 
            $Row["ADDR2"]               != $model->field["ADDR2"] || 
            $Row["TELNO1"]              != $model->field["TELNO1"] || 
            $Row["TELNO2"]              != $model->field["TELNO2"] || 
            $Row["TSUUGAKU_DIV"]        != $model->field["TSUUGAKU_DIV"] || 
            $Row["REMARK"]              != $model->field["REMARK"]
        ) {
            if ($model->cmd == "subForm1") $repFlg = true;
        }
        if ($model->cmd == "subForm1") {
            $repFlgSGK = getRepFlg($Row, $model, "");
            if (!strlen($model->field["SHITAKU_CANCEL_CHOKU_FLG"])) {
                $repFlgSTK = getRepFlg($Row, $model, "STK_");
            }
        }

        //チェック表示フラグ
        $arg["showKojinCheck"] = isset($model->kojinNo) ? "1" : "";
        $arg["unshowKojinCheck"] = isset($model->kojinNo) ? "" : "1";

        //個人履歴作成チェックボックス
        if (strlen($arg["showKojinCheck"])) {
            $extra  = "id=\"KOJIN_CHECK\"";
            $extra .= ($model->rireki["KOJIN_CHECK"] == "1" || $repFlg) ? " checked" : "";
            $arg["data"]["KOJIN_CHECK"] = knjCreateCheckBox($objForm, "KOJIN_CHECK", "1", $extra);
            $extra  = "id=\"SGK_CHECK\"";
            $extra .= ($model->rireki["SGK_CHECK"] == "1" || $repFlgSGK) ? " checked" : "";
            $arg["data"]["SGK_CHECK"] = knjCreateCheckBox($objForm, "SGK_CHECK", "1", $extra);
            $extra  = "id=\"STK_CHECK\"";
            $extra .= ($model->rireki["STK_CHECK"] == "1" || $repFlgSTK) ? " checked" : "";
            $arg["data"]["STK_CHECK"] = knjCreateCheckBox($objForm, "STK_CHECK", "1", $extra);
        } else {
            knjCreateHidden($objForm, "KOJIN_CHECK", "1");
            knjCreateHidden($objForm, "SGK_CHECK", "1");
            knjCreateHidden($objForm, "STK_CHECK", "1");
        }

        //開始日付(レコード追加用)
        $arg["data"]["ISSUEDATE"] = $my->MyCalendarWin3($objForm, "ISSUEDATE", $model->rireki["ISSUEDATE"]);

        //MAX開始日付(レコード更新用)
        $arg["data"]["MAX_ISSUEDATE"] = common::getDateHenkan($Row["ISSUEDATE"], "ymdhan2");
        $arg["data"]["SGK_ISSUEDATE"] = common::getDateHenkan($Row["SGK_ISSUEDATE"], "ymdhan2");
        $arg["data"]["STK_ISSUEDATE"] = common::getDateHenkan($Row["STK_ISSUEDATE"], "ymdhan2");
        knjCreateHidden($objForm, "MAX_ISSUEDATE", $Row["ISSUEDATE"]);
        knjCreateHidden($objForm, "SGK_ISSUEDATE", $Row["SGK_ISSUEDATE"]);
        knjCreateHidden($objForm, "STK_ISSUEDATE", $Row["STK_ISSUEDATE"]);
//echo "MAX開始日付:" .$Row["ISSUEDATE"] ."<BR>";

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjta020_07SubForm1.html", $arg); 
    }
}
//デフォルトチェック
function getRepFlg($Row, $model, $stk) {
    $rtn = false;
    if ($stk == "STK_") {
        //支度
        if ($Row[$stk."SHITAKUKIN_TAIYO_DIV"]       != $model->field[$stk."SHITAKUKIN_TAIYO_DIV"] || 
            $Row[$stk."HEIKYUU_SHITAKU_STATUS1"]    != $model->field[$stk."HEIKYUU_SHITAKU_STATUS1"] || 
            $Row[$stk."HEIKYUU_SHITAKU_STATUS2"]    != $model->field[$stk."HEIKYUU_SHITAKU_STATUS2"] || 
            $Row[$stk."HEIKYUU_SHITAKU_STATUS3"]    != $model->field[$stk."HEIKYUU_SHITAKU_STATUS3"] || 
            $Row[$stk."HEIKYUU_SHITAKU_GYOUMUCD2"]  != $model->field[$stk."HEIKYUU_SHITAKU_GYOUMUCD2"] || 
            $Row[$stk."HEIKYUU_SHITAKU_GYOUMUCD3"]  != $model->field[$stk."HEIKYUU_SHITAKU_GYOUMUCD3"] || 
            $Row[$stk."HEIKYUU_SHITAKU_REMARK2"]    != $model->field[$stk."HEIKYUU_SHITAKU_REMARK2"] || 
            $Row[$stk."HEIKYUU_SHITAKU_REMARK3"]    != $model->field[$stk."HEIKYUU_SHITAKU_REMARK3"]
        ) {
            $rtn = true;
        }
    } else {
        //修学
        if ($Row[$stk."NENREI"]                     != $model->field[$stk."NENREI"] || 
            $Row[$stk."H_GRAD_YM"]                  != $model->field[$stk."H_GRAD_YM"] || 
            $Row[$stk."YOYAKU_KIBOU_GK"]            != $model->field[$stk."YOYAKU_KIBOU_GK"] || 
            $Row[$stk."S_YOYAKU_KIBOU_YM"]          != $model->field[$stk."S_YOYAKU_KIBOU_YM"] || 
            $Row[$stk."E_YOYAKU_KIBOU_YM"]          != $model->field[$stk."E_YOYAKU_KIBOU_YM"] || 
            $Row[$stk."S_TAIYO_YM"]                 != $model->field[$stk."S_TAIYO_YM"] || 
            $Row[$stk."E_TAIYO_YM"]                 != $model->field[$stk."E_TAIYO_YM"] || 
            $Row[$stk."HEIKYUU_SHOUGAKU_STATUS1"]   != $model->field[$stk."HEIKYUU_SHOUGAKU_STATUS1"] || 
            $Row[$stk."HEIKYUU_SHOUGAKU_STATUS2"]   != $model->field[$stk."HEIKYUU_SHOUGAKU_STATUS2"] || 
            $Row[$stk."HEIKYUU_SHOUGAKU_STATUS3"]   != $model->field[$stk."HEIKYUU_SHOUGAKU_STATUS3"] || 
            $Row[$stk."HEIKYUU_SHOUGAKU_GYOUMUCD2"] != $model->field[$stk."HEIKYUU_SHOUGAKU_GYOUMUCD2"] || 
            $Row[$stk."HEIKYUU_SHOUGAKU_GYOUMUCD3"] != $model->field[$stk."HEIKYUU_SHOUGAKU_GYOUMUCD3"] || 
            $Row[$stk."HEIKYUU_SHOUGAKU_REMARK2"]   != $model->field[$stk."HEIKYUU_SHOUGAKU_REMARK2"] || 
            $Row[$stk."HEIKYUU_SHOUGAKU_REMARK3"]   != $model->field[$stk."HEIKYUU_SHOUGAKU_REMARK3"]
        ) {
            $rtn = true;
        }
    }
    //両方
    if ($Row[$stk."SHINSEI_YEAR"]        != $model->field[$stk."SHINSEI_YEAR"] || 
        $Row[$stk."UKE_YEAR"]            != $model->field[$stk."UKE_YEAR"] || 
        $Row[$stk."UKE_NO"]              != $model->field[$stk."UKE_NO"] || 
        $Row[$stk."UKE_EDABAN"]          != $model->field[$stk."UKE_EDABAN"] || 
        $Row[$stk."SHINSEI_DATE"]        != $model->field[$stk."SHINSEI_DATE"] || 
        $Row[$stk."H_SCHOOL_CD"]         != $model->field[$stk."H_SCHOOL_CD"] || 
        $Row[$stk."KATEI"]               != $model->field[$stk."KATEI"] || 
        $Row[$stk."GRADE"]               != $model->field[$stk."GRADE"]
    ) {
        $rtn = true;
    }
    return $rtn;
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "更 新", $extra);
    //戻る
    $extra = "onClick=\"parent.closeit();\"";
    //$extra = "onClick=\"return btn_submit('');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
