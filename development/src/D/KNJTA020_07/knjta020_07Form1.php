<?php

require_once('for_php7.php');


class knjta020_07Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjta020_07index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();
        //カレンダー呼び出し
        $my = new mycalendar();

        //担当課情報取得
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $query = knjta020_07Query::getSectioncd();
            $model->sectioncd = $db->getOne($query);
            if ($model->sectioncd == "") {
                $arg["jscript"] = "OnSectionError();";
            }
        }

        //１レコード取得し配列にセット
        $Row = array();
        $model->kojin_count = $db->getOne(knjta020_07Query::checkKojinInfo($model));
        if (isset($model->kojinNo) && !isset($model->warning) && $model->cmd != "subForm" && $model->cmd != "stk_cancel" && $model->cmd != "kateiGet" && $model->cmd != "kateiGet2" && $model->cmd != "KyuhuSubmit") {
            $query = knjta020_07Query::getKojinInfo($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            //給付情報取得
            $query = knjta020_07Query::getKyuhuInfo($model, "", "");
            $KyuhuRes = $db->query($query);
            while ($KyuhuWk = $KyuhuRes->fetchRow(DB_FETCHMODE_ASSOC)){
                $setSeq = $row["KYUHU_SEQ"] == 1 ? '' : '_'.$row["KYUHU_SEQ"];
                foreach ($KyuhuWk as $fieldName => $val) {
                    $KyuhuRow[$fieldName.$setSeq] = $KyuhuWk[$fieldName];
                }
                //画面開いた時にセット
                if (!$model->field2["KYUHU_SHINSEI_YEAR".$setSeq]) {
                    $model->field2["KYUHU_SHINSEI_YEAR".$setSeq] = $KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq];
                }
            }
        } else {
            $Row =& $model->field;
            $KyuhuRow =& $model->field2;
        }

        //給付情報有無チェック
        $model->maxKyuhuDiv = $db->getOne(knjta020_07Query::getKyuhuShinseiDiv($model));
        $maxSeq = 0;
        $set_Kyuhu_name = "";
        if ($model->sendKyuhuDiv === '7' || $model->sendKyuhuDiv === '8' 
            || $model->maxKyuhuDiv === '7' || $model->maxKyuhuDiv === '8') {
            $arg["Kyuhu_Hyouji_1"] = "1";
            $maxSeq = 1;
            if ($model->sendKyuhuDiv === '8' || $model->maxKyuhuDiv === '8') {
                $arg["Kyuhu_Hyouji_2"] = "1";
                $maxSeq = 2;
            }
            $set_Kyuhu_name = '+給付';
        }

        //給付分割回数
        knjCreateHidden($objForm, "KYUHU_MAX_SEQ", $maxSeq);

        //更新ポップアップ
        if ($model->cmd == "subForm" && !isset($model->warning)) {
            $arg["reload"] = " loadCheck('".REQUESTROOT."')";
        }

        //入力不可
        $disabled = ($Row["SHITAKU_CANCEL_CHOKU_FLG"] == "1") ? " disabled" : "";
        $readonly = ($Row["SHITAKU_CANCEL_CHOKU_FLG"] == "1") ? " readonly" : "";

        /************/
        /* ヘッダー */
        /************/
        //申請区分
        $shikinName = $db->getOne(knjta020_07Query::getShikinName("2"));//2:修直＋支直
        $shinseiName = "新規申請";
        $arg["data"]["SHINSEI_NAME"] = $shinseiName;
        $arg["data"]["SHINSEI_DIV"] = $shinseiName."（{$shikinName}{$set_Kyuhu_name}）";
        //貸与予約番号
        $query = knjta020_07Query::getYoyakuInfo($Row["KOJIN_NO"]);
        $arg["data"]["YOYAKU_SHUUGAKU_NO"] = $db->getOne($query);

        /************/
        /* 個人情報 */
        /************/
        //個人番号
        $extra = "STYLE=\"background-color:silver;\" readonly";
        $arg["data"]["KOJIN_NO"] = knjCreateTextBox($objForm, $Row["KOJIN_NO"], "KOJIN_NO", 7, 7, $extra);
        //氏名・フリガナ
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["FAMILY_NAME"] = knjCreateTextBox($objForm, $Row["FAMILY_NAME"], "FAMILY_NAME", 20, 20, $extra);
        $arg["data"]["FIRST_NAME"] = knjCreateTextBox($objForm, $Row["FIRST_NAME"], "FIRST_NAME", 20, 20, $extra);
        $arg["data"]["FAMILY_NAME_KANA"] = knjCreateTextBox($objForm, $Row["FAMILY_NAME_KANA"], "FAMILY_NAME_KANA", 20, 40, $extra);
        $arg["data"]["FIRST_NAME_KANA"] = knjCreateTextBox($objForm, $Row["FIRST_NAME_KANA"], "FIRST_NAME_KANA", 20, 40, $extra);
        //生年月日
        $arg["data"]["BIRTHDAY"] = $my->MyCalendarWin3($objForm, "BIRTHDAY", $Row["BIRTHDAY"]);
        //年齢
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["NENREI"] = knjCreateTextBox($objForm, $Row["NENREI"], "NENREI", 2, 2, $extra);
        //婚姻区分 1:未婚 2:既婚
        $opt = array(1, 2);
        $Row["KIKON_FLG"] = ($Row["KIKON_FLG"] == "") ? "1" : $Row["KIKON_FLG"];
        $extra = array("id=\"KIKON_FLG1\"", "id=\"KIKON_FLG2\"");
        $radioArray = knjCreateRadio($objForm, "KIKON_FLG", $Row["KIKON_FLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        //郵便番号 -付
        $arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $Row["ZIPCD"], "ADDR1", "CITYCD");
        //市町村コード
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["CITYCD"] = knjCreateTextBox($objForm, $Row["CITYCD"], "CITYCD", 5, 5, $extra);
        //住所1
        $extra = "STYLE=\"ime-mode: active;width:100%;\"";
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $Row["ADDR1"], "ADDR1", 50, 50, $extra);
        //住所2
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $Row["ADDR2"], "ADDR2", 50, 50, $extra);
        //電話番号 -付
        $extra = "STYLE=\"ime-mode: inactive;\"";
        $arg["data"]["TELNO1"] = knjCreateTextBox($objForm, $Row["TELNO1"], "TELNO1", 14, 14, $extra);
        //携帯番号 -付
        $arg["data"]["TELNO2"] = knjCreateTextBox($objForm, $Row["TELNO2"], "TELNO2", 14, 14, $extra);

        /***********/
        /* 02:修直 */
        /***********/
        //西暦年をGYY表記に変換
        $gy = common::getDateHenkan($Row["SHINSEI_YEAR"], "gy");
        $Row["SHINSEI_G"] = substr($gy,0,1);
        $Row["SHINSEI_YY"] = substr($gy,1,2);
        $gy = common::getDateHenkan($Row["UKE_YEAR"], "gy");
        $Row["UKE_G"] = substr($gy,0,1);
        $Row["UKE_YY"] = substr($gy,1,2);
        //処理状況1
        $arg["data"]["SHORI_JOUKYOU1"] = getShoriJoukyo($db, $Row, "", $Row["SHORI_JYOUKYOU1"]);
        //処理状況CD
        knjCreateHidden($objForm, "SHORI_JYOUKYOU1", $Row["SHORI_JYOUKYOU1"]);
        //異動情報
        $query = knjta020_07Query::getShori2($Row["SHINSEI_YEAR"], $Row["KOJIN_NO"]);
        $setShori2 = $db->getOne($query);
        $arg["data"]["SHORI_JOUKYOU1_2"] = $setShori2;
        //貸与計画情報
        $query = knjta020_07Query::getKariteishi($Row["SHUUGAKU_NO"], $Row["SHINSEI_YEAR"]);
        $kariteishi = $db->getOne($query);
        $arg["data"]["KARITEISHI"] = $kariteishi > 0 ? "仮停止中" : "";

        //申請年度-元号
        $extra = "";
        $query = knjta020_07Query::getGengou();
        makeCombo($objForm, $arg, $db, $query, $Row["SHINSEI_G"], "SHINSEI_G", $extra, 1, "BLANK");
        //申請年度-年
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["SHINSEI_YY"] = knjCreateTextBox($objForm, $Row["SHINSEI_YY"], "SHINSEI_YY", 2, 2, $extra);
        //受付番号-元号
        $extra = "";
        $query = knjta020_07Query::getGengou();
        makeCombo($objForm, $arg, $db, $query, $Row["UKE_G"], "UKE_G", $extra, 1, "BLANK");
        //受付番号-年
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
        $arg["data"]["UKE_YY"] = knjCreateTextBox($objForm, $Row["UKE_YY"] ? sprintf("%d", $Row["UKE_YY"]) : "", "UKE_YY", 2, 2, $extra);
        //受付番号-受付番号
        $arg["data"]["UKE_NO"] = knjCreateTextBox($objForm, $Row["UKE_NO"] ? sprintf("%d", $Row["UKE_NO"]) : "", "UKE_NO", 4, 4, $extra);
        //受付番号-枝番
        $arg["data"]["UKE_EDABAN"] = knjCreateTextBox($objForm, $Row["UKE_EDABAN"] ? sprintf("%d", $Row["UKE_EDABAN"]) : "", "UKE_EDABAN", 3, 3, $extra);
        //申請日
        $arg["data"]["SHINSEI_DATE"] = $my->MyCalendarWin3($objForm, "SHINSEI_DATE", $Row["SHINSEI_DATE"]);
        //修学生番号
        $arg["data"]["SHUUGAKU_NO"] = $Row["SHUUGAKU_NO"];
        knjCreateHidden($objForm, "SHUUGAKU_NO", $Row["SHUUGAKU_NO"]);
        //資金区分詳細・・・初期値(02:修直)
        if ($Row["SHIKIN_SHOUSAI_DIV"] == "") $Row["SHIKIN_SHOUSAI_DIV"] = "02";
        $query = knjta020_07Query::getShikinShousaiName($Row["SHIKIN_SHOUSAI_DIV"]);
        $arg["data"]["SHIKIN_SHOUSAI_NAME"] = $db->getOne($query);
        knjCreateHidden($objForm, "SHIKIN_SHOUSAI_DIV", $Row["SHIKIN_SHOUSAI_DIV"]);
        //高校
        $query = knjta020_07Query::getSchoolInfo($Row["H_SCHOOL_CD"], "");
        $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["H_SCHOOL_NAME"] = $schoolRow["NAME"];
        $arg["data"]["H_SCHOOL_RITSU"] = $schoolRow["RITSU"];
        knjCreateHidden($objForm, "RITSUCD", $schoolRow["RITSUCD"]);

        //学校検索(共通)
        $schoolCdVal = "document.forms[0]['H_SCHOOL_CD'].value";
        $textExtra = "STYLE=\"ime-mode: inactive;\" onBlur=\"schoolSelectEvent();\"";
        $dbClickEvent = "1";
        $arg["data"]["H_SCHOOL_CD"] = View::popUpSchoolCd($objForm, "H_SCHOOL_CD", $Row["H_SCHOOL_CD"], $schoolCdVal, "btn_kensaku", "btn_kakutei", "H_SCHOOL_CD", "H_SCHOOL_NAME", "", "H_SCHOOL_RITSU", "", "", $textExtra, $dbClickEvent);

        if ($model->cmd == "kateiGet") {
            //課程
            $extra = "onchange=\"keisanGradYm();\"";
            $query = knjta020_07Query::getKatei($model->field["H_SCHOOL_CD"]);
            $response = makeCombo2($objForm, $arg, $db, $query, $model->field["KATEI"], "KATEI", $extra, 1, "BLANK");
            $response .= "::".$schoolRow["NAME"];
            $response .= "::".$schoolRow["RITSU"];
            $response .= "::".$schoolRow["RITSUCD"];

            //課程
            $Row["STK_H_SCHOOL_CD"] = $Row["STK_H_SCHOOL_CD"] ? $Row["STK_H_SCHOOL_CD"] : $Row["H_SCHOOL_CD"];
            $extra = "onchange=\"keisanGradYm();\"";
            $query = knjta020_07Query::getKatei($model->field["STK_H_SCHOOL_CD"]);
            $response .= "::".makeCombo2($objForm, $arg, $db, $query, $model->field["STK_KATEI"], "STK_KATEI", $extra, 1, "BLANK");

            $query = knjta020_07Query::getSchoolInfo($Row["STK_H_SCHOOL_CD"], "");
            $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $response .= "::".$schoolRow["NAME"];
            $response .= "::".$schoolRow["RITSU"];
            $response .= "::".$schoolRow["RITSUCD"];

            echo $response;
            die();
        }
        //課程
        $extra = "onchange=\"keisanGradYm();\"";
        $query = knjta020_07Query::getKatei($Row["H_SCHOOL_CD"]);
        makeCombo($objForm, $arg, $db, $query, $Row["KATEI"], "KATEI", $extra, 1, "BLANK");
        //学年
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value); keisanGradYm();\"";
        $arg["data"]["GRADE"] = knjCreateTextBox($objForm, $Row["GRADE"], "GRADE", 2, 2, $extra);
        //卒業予定年月
        $arg["data"]["H_GRAD_YM"] = $my->MyMonthWin3($objForm, "H_GRAD_YM", $Row["H_GRAD_YM"]);
        //通学区分 1:自宅 2:自宅外
        $opt = array(1, 2);
        $Row["TSUUGAKU_DIV"] = ($Row["TSUUGAKU_DIV"] == "") ? "1" : $Row["TSUUGAKU_DIV"];
        $extra = array("id=\"TSUUGAKU_DIV1\" onclick=\"getTaiyoMasterGk('');\"", "id=\"TSUUGAKU_DIV2\" onclick=\"getTaiyoMasterGk('');\"");
        $radioArray = knjCreateRadio($objForm, "TSUUGAKU_DIV", $Row["TSUUGAKU_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        //貸与希望額
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["YOYAKU_KIBOU_GK"] = knjCreateTextBox($objForm, $Row["YOYAKU_KIBOU_GK"], "YOYAKU_KIBOU_GK", 9, 9, $extra);
        //貸与希望期間
        $arg["data"]["S_YOYAKU_KIBOU_YM"] = $my->MyMonthWin3($objForm, "S_YOYAKU_KIBOU_YM", $Row["S_YOYAKU_KIBOU_YM"]);
        $arg["data"]["E_YOYAKU_KIBOU_YM"] = $my->MyMonthWin3($objForm, "E_YOYAKU_KIBOU_YM", $Row["E_YOYAKU_KIBOU_YM"]);
        //本年度貸与期間
        $arg["data"]["S_TAIYO_YM"] = $my->MyMonthWin3($objForm, "S_TAIYO_YM", $Row["S_TAIYO_YM"]);
        $arg["data"]["E_TAIYO_YM"] = $my->MyMonthWin3($objForm, "E_TAIYO_YM", $Row["E_TAIYO_YM"]);
        //他の奨学金と併給状況
        //受給していない
        $extra  = "id=\"HEIKYUU_SHOUGAKU_STATUS1\"";
        $extra .= ($Row["HEIKYUU_SHOUGAKU_STATUS1"] == "1") ? " checked" : "";
        $arg["data"]["HEIKYUU_SHOUGAKU_STATUS1"] = knjCreateCheckBox($objForm, "HEIKYUU_SHOUGAKU_STATUS1", "1", $extra);
        //受給中
        $extra  = "id=\"HEIKYUU_SHOUGAKU_STATUS2\"";
        $extra .= ($Row["HEIKYUU_SHOUGAKU_STATUS2"] == "1") ? " checked" : "";
        $arg["data"]["HEIKYUU_SHOUGAKU_STATUS2"] = knjCreateCheckBox($objForm, "HEIKYUU_SHOUGAKU_STATUS2", "1", $extra);
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["HEIKYUU_SHOUGAKU_REMARK2"] = knjCreateTextBox($objForm, $Row["HEIKYUU_SHOUGAKU_REMARK2"], "HEIKYUU_SHOUGAKU_REMARK2", 40, 40, $extra);
        $extra = "";
        $query = knjta020_07Query::getGyoumucd();
        makeCombo($objForm, $arg, $db, $query, $Row["HEIKYUU_SHOUGAKU_GYOUMUCD2"], "HEIKYUU_SHOUGAKU_GYOUMUCD2", $extra, 1, "BLANK");
        //申請中
        $extra  = "id=\"HEIKYUU_SHOUGAKU_STATUS3\"";
        $extra .= ($Row["HEIKYUU_SHOUGAKU_STATUS3"] == "1") ? " checked" : "";
        $arg["data"]["HEIKYUU_SHOUGAKU_STATUS3"] = knjCreateCheckBox($objForm, "HEIKYUU_SHOUGAKU_STATUS3", "1", $extra);
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["HEIKYUU_SHOUGAKU_REMARK3"] = knjCreateTextBox($objForm, $Row["HEIKYUU_SHOUGAKU_REMARK3"], "HEIKYUU_SHOUGAKU_REMARK3", 40, 40, $extra);
        $extra = "";
        $query = knjta020_07Query::getGyoumucd();
        makeCombo($objForm, $arg, $db, $query, $Row["HEIKYUU_SHOUGAKU_GYOUMUCD3"], "HEIKYUU_SHOUGAKU_GYOUMUCD3", $extra, 1, "BLANK");

        //親権者CMB
        $extra = "";
        $query = knjta020_07Query::getShinkenCmb($model);
        makeCombo($objForm, $arg, $db, $query, $Row["SHINKEN1_CD"], "SHINKEN1_CD", $extra, 1, "BLANK");
        //親権者CMB
        $extra = "";
        $query = knjta020_07Query::getShinkenCmb($model);
        makeCombo($objForm, $arg, $db, $query, $Row["SHINKEN2_CD"], "SHINKEN2_CD", $extra, 1, "BLANK");
        //親権者CMB
        $extra = "";
        $query = knjta020_07Query::getShinkenCmb($model);
        makeCombo($objForm, $arg, $db, $query, $Row["RENTAI_CD"], "RENTAI_CD", $extra, 1, "BLANK");

        //修学支度金貸与について、貸与予約をしていたが、貸与を希望しない
        $extra  = "id=\"SHITAKU_CANCEL_CHOKU_FLG\"";
        $extra .= ($Row["SHITAKU_CANCEL_CHOKU_FLG"] == "1") ? " checked" : "";
        $extra .= " onclick=\"return btn_submit('stk_cancel');\"";
        $arg["data"]["SHITAKU_CANCEL_CHOKU_FLG"] = knjCreateCheckBox($objForm, "SHITAKU_CANCEL_CHOKU_FLG", "1", $extra);
        //入力完了
        $extra  = "id=\"SHINSEI_KANRYOU_FLG\"";
        $extra .= ($Row["SHINSEI_KANRYOU_FLG"] == "1") ? " checked" : "";
        $arg["data"]["SHINSEI_KANRYOU_FLG"] = knjCreateCheckBox($objForm, "SHINSEI_KANRYOU_FLG", "1", $extra);

        /***********/
        /* 03:支直 */
        /***********/
        //$stk = "STK_";
        //西暦年をGYY表記に変換
        $gy = common::getDateHenkan($Row["STK_SHINSEI_YEAR"], "gy");
        $Row["STK_SHINSEI_G"] = substr($gy,0,1);
        $Row["STK_SHINSEI_YY"] = substr($gy,1,2);
        $gy = common::getDateHenkan($Row["STK_UKE_YEAR"], "gy");
        $Row["STK_UKE_G"] = substr($gy,0,1);
        $Row["STK_UKE_YY"] = substr($gy,1,2);
        //処理状況2
        $arg["data"]["STK_SHORI_JOUKYOU1"] = getShoriJoukyo($db, $Row, "STK_", $Row["SHORI_JYOUKYOU2"]);
        //処理状況CD
        knjCreateHidden($objForm, "SHORI_JYOUKYOU2", $Row["SHORI_JYOUKYOU2"]);

        //申請年度-元号
        $extra = "";
        $query = knjta020_07Query::getGengou();
        makeCombo($objForm, $arg, $db, $query, $Row["STK_SHINSEI_G"], "STK_SHINSEI_G", $extra.$disabled, 1, "BLANK");
        //申請年度-年
        $extra = "STYLE=\"ime-mode: inactive;\"onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["STK_SHINSEI_YY"] = knjCreateTextBox($objForm, $Row["STK_SHINSEI_YY"], "STK_SHINSEI_YY", 2, 2, $extra.$readonly);
        //受付番号-元号
        $extra = "";
        $query = knjta020_07Query::getGengou();
        makeCombo($objForm, $arg, $db, $query, $Row["STK_UKE_G"], "STK_UKE_G", $extra.$disabled, 1, "BLANK");
        //受付番号-年
        $extra = "STYLE=\"ime-mode: inactive;\"onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
        $arg["data"]["STK_UKE_YY"] = knjCreateTextBox($objForm, $Row["STK_UKE_YY"] ? sprintf("%d", $Row["STK_UKE_YY"]) : "", "STK_UKE_YY", 2, 2, $extra.$readonly);
        //受付番号-受付番号
        $arg["data"]["STK_UKE_NO"] = knjCreateTextBox($objForm, $Row["STK_UKE_NO"] ? sprintf("%d", $Row["STK_UKE_NO"]) : "", "STK_UKE_NO", 4, 4, $extra.$readonly);
        //受付番号-枝番
        $arg["data"]["STK_UKE_EDABAN"] = knjCreateTextBox($objForm, $Row["STK_UKE_EDABAN"] ? sprintf("%d", $Row["STK_UKE_EDABAN"]) : "", "STK_UKE_EDABAN", 3, 3, $extra.$readonly);
        //申請日
        $arg["data"]["STK_SHINSEI_DATE"] = $my->MyCalendarWin3($objForm, "STK_SHINSEI_DATE", $Row["STK_SHINSEI_DATE"], $readonly);
        //修学生番号
        $arg["data"]["STK_SHUUGAKU_NO"] = $Row["STK_SHUUGAKU_NO"];
        knjCreateHidden($objForm, "STK_SHUUGAKU_NO", $Row["STK_SHUUGAKU_NO"]);
        //資金区分詳細・・・初期値(03:支直)
        if ($Row["STK_SHIKIN_SHOUSAI_DIV"] == "") $Row["STK_SHIKIN_SHOUSAI_DIV"] = ($model->sendPrgid == "KNJTA020_08") ? "05" : "03";
        $query = knjta020_07Query::getShikinShousaiName($Row["STK_SHIKIN_SHOUSAI_DIV"]);
        $arg["data"]["STK_SHIKIN_SHOUSAI_NAME"] = $db->getOne($query);
        knjCreateHidden($objForm, "STK_SHIKIN_SHOUSAI_DIV", $Row["STK_SHIKIN_SHOUSAI_DIV"]);
        //高校
        $query = knjta020_07Query::getSchoolInfo($Row["STK_H_SCHOOL_CD"], "");
        $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["STK_H_SCHOOL_NAME"] = $schoolRow["NAME"];
        $arg["data"]["STK_H_SCHOOL_RITSU"] = $schoolRow["RITSU"];
        knjCreateHidden($objForm, "STK_RITSUCD", $schoolRow["RITSUCD"]);

        //学校検索(共通)
        $schoolCdVal = "document.forms[0]['STK_H_SCHOOL_CD'].value";
        $textExtra = "STYLE=\"ime-mode: inactive;\"onBlur=\"schoolSelectEvent();\"";
        $dbClickEvent = "1";
        $arg["data"]["STK_H_SCHOOL_CD"] = View::popUpSchoolCd($objForm, "STK_H_SCHOOL_CD", $Row["STK_H_SCHOOL_CD"], $schoolCdVal, "stk_btn_kensaku", "stk_btn_kakutei", "STK_H_SCHOOL_CD", "STK_H_SCHOOL_NAME", "", "STK_H_SCHOOL_RITSU", "", "", $textExtra.$readonly, $dbClickEvent);

        //課程
        $extra = "";
        $query = knjta020_07Query::getKatei($Row["STK_H_SCHOOL_CD"]);
        makeCombo($objForm, $arg, $db, $query, $Row["STK_KATEI"], "STK_KATEI", $extra.$disabled, 1, "BLANK");
        //学年
        $extra = "STYLE=\"ime-mode: inactive;\"onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["STK_GRADE"] = knjCreateTextBox($objForm, $Row["STK_GRADE"], "STK_GRADE", 2, 2, $extra.$readonly);
        //支度金貸与区分 1:国公立 2:私立
        $opt = array(1, 2);
        $Row["STK_SHITAKUKIN_TAIYO_DIV"] = ($Row["STK_SHITAKUKIN_TAIYO_DIV"] == "") ? "1" : $Row["STK_SHITAKUKIN_TAIYO_DIV"];
        $extra = array("id=\"STK_SHITAKUKIN_TAIYO_DIV1\"", "id=\"STK_SHITAKUKIN_TAIYO_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "STK_SHITAKUKIN_TAIYO_DIV", $Row["STK_SHITAKUKIN_TAIYO_DIV"], $extra.$disabled, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        //他の奨学金と併給状況
        //受給していない
        $extra  = "id=\"STK_HEIKYUU_SHITAKU_STATUS1\"";
        $extra .= ($Row["STK_HEIKYUU_SHITAKU_STATUS1"] == "1") ? " checked" : "";
        $arg["data"]["STK_HEIKYUU_SHITAKU_STATUS1"] = knjCreateCheckBox($objForm, "STK_HEIKYUU_SHITAKU_STATUS1", "1", $extra.$disabled);
        //受給中
        $extra  = "id=\"STK_HEIKYUU_SHITAKU_STATUS2\"";
        $extra .= ($Row["STK_HEIKYUU_SHITAKU_STATUS2"] == "1") ? " checked" : "";
        $arg["data"]["STK_HEIKYUU_SHITAKU_STATUS2"] = knjCreateCheckBox($objForm, "STK_HEIKYUU_SHITAKU_STATUS2", "1", $extra.$disabled);
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["STK_HEIKYUU_SHITAKU_REMARK2"] = knjCreateTextBox($objForm, $Row["STK_HEIKYUU_SHITAKU_REMARK2"], "STK_HEIKYUU_SHITAKU_REMARK2", 40, 40, $extra.$readonly);
        $extra = "";
        $query = knjta020_07Query::getGyoumucd();
        makeCombo($objForm, $arg, $db, $query, $Row["STK_HEIKYUU_SHITAKU_GYOUMUCD2"], "STK_HEIKYUU_SHITAKU_GYOUMUCD2", $extra.$disabled, 1, "BLANK");
        //申請中
        $extra  = "id=\"STK_HEIKYUU_SHITAKU_STATUS3\"";
        $extra .= ($Row["STK_HEIKYUU_SHITAKU_STATUS3"] == "1") ? " checked" : "";
        $arg["data"]["STK_HEIKYUU_SHITAKU_STATUS3"] = knjCreateCheckBox($objForm, "STK_HEIKYUU_SHITAKU_STATUS3", "1", $extra.$disabled);
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["STK_HEIKYUU_SHITAKU_REMARK3"] = knjCreateTextBox($objForm, $Row["STK_HEIKYUU_SHITAKU_REMARK3"], "STK_HEIKYUU_SHITAKU_REMARK3", 40, 40, $extra.$readonly);
        $extra = "";
        $query = knjta020_07Query::getGyoumucd();
        makeCombo($objForm, $arg, $db, $query, $Row["STK_HEIKYUU_SHITAKU_GYOUMUCD3"], "STK_HEIKYUU_SHITAKU_GYOUMUCD3", $extra.$disabled, 1, "BLANK");

        //親権者CMB
        $extra = "";
        $query = knjta020_07Query::getShinkenCmb($model);
        makeCombo($objForm, $arg, $db, $query, $Row["STK_SHINKEN1_CD"], "STK_SHINKEN1_CD", $extra.$disabled, 1, "BLANK");
        //親権者CMB
        $extra = "";
        $query = knjta020_07Query::getShinkenCmb($model);
        makeCombo($objForm, $arg, $db, $query, $Row["STK_SHINKEN2_CD"], "STK_SHINKEN2_CD", $extra.$disabled, 1, "BLANK");
        //親権者CMB
        $extra = "";
        $query = knjta020_07Query::getShinkenCmb($model);
        makeCombo($objForm, $arg, $db, $query, $Row["STK_RENTAI_CD"], "STK_RENTAI_CD", $extra.$disabled, 1, "BLANK");

        //入力完了
        $extra  = "id=\"STK_SHINSEI_KANRYOU_FLG\"";
        $extra .= ($Row["STK_SHINSEI_KANRYOU_FLG"] == "1") ? " checked" : "";
        $arg["data"]["STK_SHINSEI_KANRYOU_FLG"] = knjCreateCheckBox($objForm, "STK_SHINSEI_KANRYOU_FLG", "1", $extra.$disabled);

        /************/
        /* フッター */
        /************/
        //備考
        $gyou = 5;
        $moji = 55;
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;
        $extra = "STYLE=\"ime-mode: active;height:{$height}px;\" ";
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", $gyou, ($moji * 2 + 1), "soft", $extra, $Row["REMARK"]);

        //再度個人データを取得
        $kjnRow = array();
        if (isset($model->kojinNo)) {
            $query = knjta020_07Query::getKojinInfo($model);
            $kjnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //貸与不成立理由
        $query = knjta020_07Query::getTaiyoFailInfo($Row["KOJIN_NO"], $Row["SHINSEI_YEAR"], $kjnRow["SHIKIN_SHOUSAI_DIV"], $kjnRow["SGK_ISSUEDATE"]);
        $taiyoFailRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["TAIYO_FAIL_DIV"] = $taiyoFailRow["TAIYO_FAIL_DIV"];
        $arg["data"]["TAIYO_FAIL_REMARK"] = $taiyoFailRow["TAIYO_FAIL_REMARK"];
        //世帯SEQ
        $arg["data"]["SETAI_SEQ_SHOW"] = $Row["SETAI_SEQ_SHOW"];

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db, $Row, $kjnRow);

        //hidden作成
        makeHidden($objForm);

        /***********/
        /* 給付 ****/
        /***********/
        //給付申請未作成で処理区分が7：給付申請の時、直近の給付申請データ情報を取得
        //前年度コピーなら前年度のSEQに合わせて設定すれば良いので、ループは不要。
        if (($model->sendKyuhuDiv === '7' || $model->sendKyuhuDiv === '8') && $KyuhuRow["KYUHU_SHINSEI_YEAR"] == "" && $model->cmd == "main") {
            $query = knjta020_07Query::getKyuhuZennenDat($model, $Row["SHINSEI_YEAR"]);
            $KyuhuZennenRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($KyuhuZennenRow["SHINSEI_YEAR"] != "") {
                //以下のデータをセット
                $KyuhuRow["KYUHU_SHUUGAKU_NO"] = $KyuhuZennenRow["SHUUGAKU_NO"];
                $KyuhuRow["KYUHU_H_SCHOOL_CD"] = $KyuhuZennenRow["H_SCHOOL_CD"];
                $KyuhuRow["KYUHU_KATEI"]       = $KyuhuZennenRow["KATEI"];
                $KyuhuRow["HOGOSHA_CD"]        = $KyuhuZennenRow["HOGOSHA_CD"];
                $KyuhuRow["HOGOSHA2_CD"]       = $KyuhuZennenRow["HOGOSHA2_CD"];
                $KyuhuRow["SHOTOKUWARI_DIV"]   = $KyuhuZennenRow["SHOTOKUWARI_DIV"];
                $KyuhuRow["KYOUDAI_BIRTHDAY"]  = $KyuhuZennenRow["KYOUDAI_BIRTHDAY"];
                $KyuhuRow["KYOUDAI_TSUZUKIGARA_CD"] = $KyuhuZennenRow["KYOUDAI_TSUZUKIGARA_CD"];
                $KyuhuRow["KYOUDAI_FAMILY_NAME"]    = $KyuhuZennenRow["KYOUDAI_FAMILY_NAME"];
                $KyuhuRow["KYOUDAI_FIRST_NAME"]     = $KyuhuZennenRow["KYOUDAI_FIRST_NAME"];
                $KyuhuRow["KYOUDAI_FAMILY_NAME_KANA"] = $KyuhuZennenRow["KYOUDAI_FAMILY_NAME_KANA"];
                $KyuhuRow["KYOUDAI_FIRST_NAME_KANA"]  = $KyuhuZennenRow["KYOUDAI_FIRST_NAME_KANA"];
                $KyuhuRow["KYUHU_REMARK"]             = $KyuhuZennenRow["REMARK"];
                //カウントアップ
                $KyuhuRow["KYUHU_KAISUU"] = ++$KyuhuZennenRow["KYUHU_KAISUU"];
            }
        }

        if (($model->sendKyuhuDiv === '7' || $model->sendKyuhuDiv === '8') && $KyuhuRow["KYUHU_SHINSEI_YEAR"] != CTRL_YEAR) {
            $query = knjta020_07Query::getKyuhuZennenDat($model, CTRL_YEAR);
            $KyuhuZennenRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //バックアップ
            $backKyuhuRow = $KyuhuRow;
            //データをクリア
            $KyuhuRow = array();
            //初期値設定
            $KyuhuRow["KYUHU_SHINSEI_YEAR"] = CTRL_YEAR;
            $KyuhuRow["KYUHU_SHUUGAKU_NO"] = $backKyuhuRow["KYUHU_SHUUGAKU_NO"];
            $KyuhuRow["KYUHU_H_SCHOOL_CD"] = $backKyuhuRow["KYUHU_H_SCHOOL_CD"];
            $KyuhuRow["KYUHU_KAISUU"] = ++$KyuhuZennenRow["KYUHU_KAISUU"];
            $KyuhuRow["KYUHU_GRADE"] = ++$KyuhuZennenRow["GRADE"];
            $KyuhuRow["KYUHU_GRADE"] = $KyuhuRow["KYUHU_GRADE"] < 10 ? "0".$KyuhuRow["KYUHU_GRADE"] : $KyuhuRow["KYUHU_GRADE"];
        }

        //２回目ありのデータ
        $setSeq = '';
        knjCreateHidden($objForm, "SCHOOL_SEARCH_SEQ");
        for ($seq = 1; $seq <= $maxSeq; $seq++) { 
            $setSeq = $seq == 1 ? '' : '_'.$seq;

            $yval = "";
            if ($seq == 1) {
                $yval = CTRL_YEAR;
            }
            //初期表示
            $KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq] = ($KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq] == "") ? $yval : $KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq];
            //西暦年をGYY表記に変換
            $gy = common::getDateHenkan($KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq], "gy");
            $KyuhuRow["KYUHU_SHINSEI_G".$setSeq] = substr($gy,0,1);
            $KyuhuRow["KYUHU_SHINSEI_YY".$setSeq] = substr($gy,1,2);
            $gy = common::getDateHenkan($KyuhuRow["KYUHU_UKE_YEAR".$setSeq], "gy");
            $KyuhuRow["KYUHU_UKE_G".$setSeq] = substr($gy,0,1);
            $KyuhuRow["KYUHU_UKE_YY".$setSeq] = substr($gy,1,2);
            
            //処理状況3
            $getShoriJyouKyou = "";
            if ($KyuhuRow["SHORI_JYOUKYOU3".$setSeq]) {
                $query = knjta020_07Query::getKyuhuShoriJyouKyou($KyuhuRow["SHORI_JYOUKYOU3".$setSeq]);
                $getShoriJyouKyou = $db->getOne($query);
            }
            if (strlen($KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq])) {
                $nendo = common::getDateHenkan($KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq].'-04-01', "nendhan");
                $getShoriJyouKyou = $nendo ."<BR>" .$getShoriJyouKyou;
            }
            $arg["data2"]["SHORI_JOUKYOU3".$setSeq] = $getShoriJyouKyou;
            //処理状況CD
            knjCreateHidden($objForm, "SHORI_JYOUKYOU3".$setSeq, $KyuhuRow["SHORI_JYOUKYOU3".$setSeq]);

            //申請年度-元号
            $extra = "";
            $query = knjta020_07Query::getGengou();
            makeCombo3($objForm, $arg, $db, $query, $KyuhuRow["KYUHU_SHINSEI_G".$setSeq], "KYUHU_SHINSEI_G".$setSeq, $extra, 1, "BLANK");
            //申請年度-年
            $extra = "STYLE=\"ime-mode: inactive;\"onblur=\"this.value=toInteger(this.value);\"";
            $arg["data2"]["KYUHU_SHINSEI_YY".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYUHU_SHINSEI_YY".$setSeq], "KYUHU_SHINSEI_YY".$setSeq, 2, 2, $extra);
            //受付番号-元号
            $extra = "";
            $query = knjta020_07Query::getGengou();
            makeCombo3($objForm, $arg, $db, $query, $KyuhuRow["KYUHU_UKE_G".$setSeq], "KYUHU_UKE_G".$setSeq, $extra, 1, "BLANK");
            //受付番号-年
            $extra = "STYLE=\"ime-mode: inactive;\"onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
            $arg["data2"]["KYUHU_UKE_YY".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYUHU_UKE_YY".$setSeq] ? sprintf("%d", $KyuhuRow["KYUHU_UKE_YY".$setSeq]) : "", "KYUHU_UKE_YY".$setSeq, 2, 2, $extra);
            //受付番号-受付番号
            $arg["data2"]["KYUHU_UKE_NO".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYUHU_UKE_NO".$setSeq] ? sprintf("%d", $KyuhuRow["KYUHU_UKE_NO".$setSeq]) : "", "KYUHU_UKE_NO".$setSeq, 4, 4, $extra);
            //受付番号-枝番
            $arg["data2"]["KYUHU_UKE_EDABAN".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYUHU_UKE_EDABAN".$setSeq] ? sprintf("%d", $KyuhuRow["KYUHU_UKE_EDABAN".$setSeq]) : "", "KYUHU_UKE_EDABAN".$setSeq, 3, 3, $extra);
            //申請日
            $arg["data2"]["KYUHU_SHINSEI_DATE".$setSeq] = $my->MyCalendarWin3($objForm, "KYUHU_SHINSEI_DATE".$setSeq, $KyuhuRow["KYUHU_SHINSEI_DATE".$setSeq]);
            //修学生番号
            $arg["data2"]["KYUHU_SHUUGAKU_NO".$setSeq] = $KyuhuRow["KYUHU_SHUUGAKU_NO".$setSeq];
            knjCreateHidden($objForm, "KYUHU_SHUUGAKU_NO".$setSeq, $KyuhuRow["KYUHU_SHUUGAKU_NO".$setSeq]);
            //資金区分 (固定)
            $arg["data2"]["KYUHU_SHIKIN_SHOUSAI_NAME".$setSeq] = '給付';

            //「家計急変」は分割1回目のみ表示
            if ($seq == 1) {
                //家計急変
                //家計急変チェックボックス
                $extra  = " id=\"KAKEI_KYUHEN_FLG\" ";
                $extra .= " onclick=\"return btn_submit('KyuhuSubmit');\"";
                $checked = $KyuhuRow["KAKEI_KYUHEN_FLG"] == '1' ? " checked " : "";
                $arg["data2"]["KAKEI_KYUHEN_FLG"] = knjCreateCheckBox($objForm, "KAKEI_KYUHEN_FLG", "1", $checked.$extra);

                //家計急変日
                $extra = "onBlur = \"key7AndCheck(this); return btn_submit('KyuhuSubmit');\"";
                $arg["data2"]["KAKEI_KYUHEN_DATE"] = $my->MyCalendarWin3($objForm, "KAKEI_KYUHEN_DATE", $KyuhuRow["KAKEI_KYUHEN_DATE"], $extra, "1", "", "KyuhuSubmit");
            }
            //学校検索(共通)
            $hSchoolCd = "";
            if ($seq == 1) {
                $hSchoolCd = $Row["H_SCHOOL_CD"];
            }
            $KyuhuRow["KYUHU_H_SCHOOL_CD".$setSeq] = ($KyuhuRow["KYUHU_H_SCHOOL_CD".$setSeq] == "") ? $hSchoolCd : $KyuhuRow["KYUHU_H_SCHOOL_CD".$setSeq];
            $schoolCdVal = "document.forms[0]['KYUHU_H_SCHOOL_CD".$setSeq."'].value";
            $textExtra = "STYLE=\"ime-mode: inactive;\" onBlur=\"schoolSelectEventData({$setSeq});\"";
            $dbClickEvent = "2";
            $arg["data2"]["KYUHU_H_SCHOOL_CD".$setSeq] = View::popUpSchoolCd($objForm, "KYUHU_H_SCHOOL_CD".$setSeq, $KyuhuRow["KYUHU_H_SCHOOL_CD".$setSeq], $schoolCdVal, "kyuhu_btn_kensaku".$setSeq, "kyuhu_btn_kakutei".$setSeq, "KYUHU_H_SCHOOL_CD".$setSeq, "KYUHU_H_SCHOOL_NAME".$setSeq, "", "KYUHU_H_SCHOOL_RITSU".$setSeq, "", "", $textExtra, $dbClickEvent, "kyuhu", $model->sectioncd, $seq);

            //高校
            $query = knjta020_07Query::getSchoolInfo($KyuhuRow["KYUHU_H_SCHOOL_CD".$setSeq], "");
            $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["data2"]["KYUHU_H_SCHOOL_NAME".$setSeq] = $schoolRow["NAME"];
            $arg["data2"]["KYUHU_H_SCHOOL_RITSU".$setSeq] = $schoolRow["RITSU"];
            knjCreateHidden($objForm, "KYUHU_RITSUCD".$setSeq, $schoolRow["RITSUCD"]);

            if ($model->cmd == "kateiGet2") {
                //課程(給付)
                $extra = "onchange=\"return btn_submit('KyuhuSubmit');\"";
                $query = knjta020_07Query::getKatei($model->field2["KYUHU_H_SCHOOL_CD".$setSeq]);
                //学校コードを変更した場合、課程を選択させるように一度初期化する(マスタの金額が変わってくるため)
                $model->field2["KYUHU_KATEI".$setSeq] = "";
                $response = makeCombo2($objForm, $arg, $db, $query, $model->field2["KYUHU_KATEI".$setSeq], "KYUHU_KATEI".$setSeq, $extra, 1, "BLANK");
                $response .= "::".$schoolRow["NAME"];
                $response .= "::".$schoolRow["RITSU"];
                $response .= "::".$schoolRow["RITSUCD"];

                echo $response;
                die();
            }

            //課程
            $katei = "";
            if ($seq == 1) {
                $katei = $Row["KATEI"];
            }
            $KyuhuRow["KYUHU_KATEI".$setSeq] = ($KyuhuRow["KYUHU_KATEI".$setSeq] == "") ? $katei : $KyuhuRow["KYUHU_KATEI".$setSeq];
            $extra = "onchange=\"return btn_submit('KyuhuSubmit');\"";
            $query = knjta020_07Query::getKatei($KyuhuRow["KYUHU_H_SCHOOL_CD".$setSeq]);
            makeCombo3($objForm, $arg, $db, $query, $KyuhuRow["KYUHU_KATEI".$setSeq], "KYUHU_KATEI".$setSeq, $extra, 1, "BLANK");        
            
            //課程区分
            $query = knjta020_07Query::getKateiDivName($KyuhuRow["KYUHU_KATEI".$setSeq]);
            $kateiDivName = $db->getOne($query);
            $setttei_nasi = '<font color="red">設定なし</font>';
            $kateiDivName = ($kateiDivName != "") ? $kateiDivName : $setttei_nasi;
            $arg["data2"]["KYUHU_KATEI_DIV_NAME".$setSeq] = $kateiDivName;

            //給付金および貸与限度額のマスタの金額一覧を取得
           //給付予定額
            $minMonth = 4;
            $maxMonth = 6;
            $monthCnt = 3;
            $calcDenominator = 4;
            $calcNumerator = 1;
            if ($seq == 2) {
                $minMonth = 7;
                $maxMonth = 15;
                $monthCnt = 9;
                $calcNumerator = 3;
            }
            //給付金
            $query = knjta020_07Query::getKyuhuMasterGkList($KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq], $KyuhuRow["KYUHU_H_SCHOOL_CD".$setSeq], $KyuhuRow["KYUHU_KATEI".$setSeq]);
            $result = $db->query($query);
            $counter = 0;
            while ($masterKyuhuRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                knjCreateHidden($objForm, "KYUHU_GET_SHOTOKUWARI_DIV".$setSeq.$counter, $masterKyuhuRow["GET_SHOTOKUWARI_DIV"]);
                $setGk = getKyuhuGk($KyuhuRow, $masterKyuhuRow["GET_KYUHU_GK"], $minMonth, $maxMonth, $monthCnt, $calcDenominator, $calcNumerator);
                knjCreateHidden($objForm, "KYUHU_GET_KYUHU_GK".$setSeq.$counter, $setGk);
                $counter++;
            }
            $result->free();
            knjCreateHidden($objForm, "KYUHU_GET_COUNT".$setSeq, $counter);
            //貸与限度額
            $query = knjta020_07Query::getTaiyoMasterGkList($KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq], $KyuhuRow["KYUHU_H_SCHOOL_CD".$setSeq], $KyuhuRow["KYUHU_KATEI".$setSeq]);
            $result = $db->query($query);
            $counter2 = 0;
            while ($masterTaiyoRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                knjCreateHidden($objForm, "TAIYO_GET_TSUUGAKU_DIV".$setSeq.$counter2, $masterTaiyoRow["GET_TSUUGAKU_DIV"]);
                knjCreateHidden($objForm, "TAIYO_GET_SHOTOKUWARI_DIV".$setSeq.$counter2, $masterTaiyoRow["GET_SHOTOKUWARI_DIV"]);
                knjCreateHidden($objForm, "TAIYO_GET_GENDO_GK".$setSeq.$counter2, $masterTaiyoRow["GET_GENDO_GK"]);
                $counter2++;
            }
            $result->free();
            knjCreateHidden($objForm, "TAIYO_GET_COUNT".$setSeq, $counter2);

            //学年
            $grade = "";
            if ($seq == 1) {
                $grade = $Row["GRADE"];
            }
            $KyuhuRow["KYUHU_GRADE".$setSeq] = ($KyuhuRow["KYUHU_GRADE".$setSeq] == "") ? $grade : $KyuhuRow["KYUHU_GRADE".$setSeq];
            $extra = "STYLE=\"ime-mode: inactive;\"onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
            $arg["data2"]["KYUHU_GRADE".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYUHU_GRADE".$setSeq], "KYUHU_GRADE".$setSeq, 2, 2, $extra);
            //クラス
            $extra = " STYLE=\"text-align:right;\"";
            $arg["data2"]["KYUHU_HR_CLASS".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYUHU_HR_CLASS".$setSeq], "KYUHU_HR_CLASS".$setSeq, 2, 3, $extra);
            //出席番号
            $extra = " STYLE=\"text-align:right;\"";
            $arg["data2"]["KYUHU_ATTENDNO".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYUHU_ATTENDNO".$setSeq], "KYUHU_ATTENDNO".$setSeq, 2, 3, $extra);
            
            //給付回数
            $query = knjta020_07Query::getKyuhuMax($KyuhuRow["KYUHU_KATEI".$setSeq]);
            $model->kyuhuMax = $db->getOne($query);
            $opt = array();
            $opt[0] = array ("label" => "", "value" => "");
            for ($i = 1; $i <= $model->kyuhuMax; $i++) {
                $opt[$i] = array ("label" => "$i".'回目', "value" => "$i");
            }
            $extra = "";
            $arg["data2"]["KYUHU_KAISUU".$setSeq] = knjCreateCombo($objForm, "KYUHU_KAISUU".$setSeq, $KyuhuRow["KYUHU_KAISUU".$setSeq], $opt, $extra, 1);

            //保護者(申請者)CMB
            $extra = "";
            $query = knjta020_07Query::getShinkenCmb($model);
            makeCombo3($objForm, $arg, $db, $query, $KyuhuRow["HOGOSHA_CD".$setSeq], "HOGOSHA_CD".$setSeq, $extra, 1, "BLANK");
            
            //保護者CMB
            $extra = "";
            $query = knjta020_07Query::getShinkenCmb($model);
            makeCombo3($objForm, $arg, $db, $query, $KyuhuRow["HOGOSHA2_CD".$setSeq], "HOGOSHA2_CD".$setSeq, $extra, 1, "BLANK");

            //保護者口座登録情報
            $query = knjta020_07Query::getHogoshaKouzaCount($model->kojinNo);
            $kouzaCount = $db->getOne($query);
            if ($kouzaCount > 0) {
                $arg["data2"]["HOGOSHA_KOUZA_INFO".$setSeq] = '口座登録済';
            } else {
                $arg["data2"]["HOGOSHA_KOUZA_INFO".$setSeq] = '口座未登録';
            }

            //所得割額
            $extra = "STYLE=\"ime-mode: inactive;\"onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
            $arg["data2"]["SHOTOKUWARI_GK".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["SHOTOKUWARI_GK".$setSeq], "SHOTOKUWARI_GK".$setSeq, 7, 7, $extra);

            //所得割額確認済みチェック
            $extra  = "id=\"SHOTOKUWARI_GK_CHECK_FLG".$setSeq."\"";
            $extra .= ($KyuhuRow["SHOTOKUWARI_GK_CHECK_FLG".$setSeq] == "1") ? " checked" : "";
            $extra .= " onclick=\"shotokuwariCheck();\"";
            $arg["data2"]["SHOTOKUWARI_GK_CHECK_FLG".$setSeq] = knjCreateCheckBox($objForm, "SHOTOKUWARI_GK_CHECK_FLG".$setSeq, "1", $extra);

            //所得割区分
            //所得割区分名称をセット
            $query = knjta020_07Query::getShotokuwariDivName();
            $result = $db->query($query);
            $i = "1";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $arg["data2"]["SHOTOKUWARI_DIV_NAME".$setSeq.$i] = $row["SHOTOKUWARI_DIV_NAME"];
                $i++;
            }
            $result->free();
            
            $opt = array(1, 2, 3);
            $KyuhuRow["SHOTOKUWARI_DIV".$setSeq] = ($KyuhuRow["SHOTOKUWARI_DIV".$setSeq] == "") ? "2" : $KyuhuRow["SHOTOKUWARI_DIV".$setSeq];
            $extra = array("id=\"SHOTOKUWARI_DIV{$setSeq}1\" onclick=\"change_text('{$setSeq}');\"", "id=\"SHOTOKUWARI_DIV{$setSeq}2\" onclick=\"change_text('{$setSeq}');\"", "id=\"SHOTOKUWARI_DIV{$setSeq}3\" onclick=\"change_text('{$setSeq}');\"");
            $radioArray = knjCreateRadio($objForm, "SHOTOKUWARI_DIV".$setSeq, $KyuhuRow["SHOTOKUWARI_DIV".$setSeq], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data2"][$key] = $val;

            //第２子生年月日
            if ($KyuhuRow["SHOTOKUWARI_DIV".$setSeq] != "3") {
                $extra = "onBlur = \"key7AndCheck(this); dateExtra('', '{$setSeq}');\" STYLE=\"background-color:silver;\" disabled";
            } else {
                $extra = "onBlur = \"key7AndCheck(this); dateExtra('', '{$setSeq}');\"";
            }
            $arg["data2"]["KYOUDAI_BIRTHDAY".$setSeq] = $my->MyCalendarWin3($objForm, "KYOUDAI_BIRTHDAY".$setSeq, $KyuhuRow["KYOUDAI_BIRTHDAY".$setSeq], $extra, "", "main");

            //年齢の表示(申請年度の7月1日現在)
            $setDate = $KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq].'0701';
            $setBirthday = str_replace('-', '', $KyuhuRow["KYOUDAI_BIRTHDAY".$setSeq]);
            if ($KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq] != "" && $setBirthday != "") {
                $arg["data2"]["KYUHU_NENREI_HYOUJI".$setSeq] = (int) (($setDate - $setBirthday ) / 10000 );
            } else {
                $arg["data2"]["KYUHU_NENREI_HYOUJI".$setSeq] = '　';
            }
            
            //第２子続柄
            if ($KyuhuRow["SHOTOKUWARI_DIV".$setSeq] != "3") {
                $extra = "STYLE=\"background-color:silver;\" disabled";
            } else {
                $extra = "";
            }
            $query = knjta020_07Query::getNameMst('T006', $model);
            makeCombo3($objForm, $arg, $db, $query, $KyuhuRow["KYOUDAI_TSUZUKIGARA_CD".$setSeq], "KYOUDAI_TSUZUKIGARA_CD".$setSeq, $extra, 1, "BLANK");

            //第２子氏名・フリガナ
            if ($KyuhuRow["SHOTOKUWARI_DIV".$setSeq] != "3") {
                $extra = "STYLE=\"ime-mode: active; background-color:silver;\" disabled";
            } else {
                $extra = "STYLE=\"ime-mode: active;\"";
            }
            $arg["data2"]["KYOUDAI_FAMILY_NAME".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYOUDAI_FAMILY_NAME".$setSeq], "KYOUDAI_FAMILY_NAME".$setSeq, 20, 20, $extra);
            $arg["data2"]["KYOUDAI_FIRST_NAME".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYOUDAI_FIRST_NAME".$setSeq], "KYOUDAI_FIRST_NAME".$setSeq, 20, 20, $extra);
            $arg["data2"]["KYOUDAI_FAMILY_NAME_KANA".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYOUDAI_FAMILY_NAME_KANA".$setSeq], "KYOUDAI_FAMILY_NAME_KANA".$setSeq, 20, 40, $extra);
            $arg["data2"]["KYOUDAI_FIRST_NAME_KANA".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYOUDAI_FIRST_NAME_KANA".$setSeq], "KYOUDAI_FIRST_NAME_KANA".$setSeq, 20, 40, $extra);

            //併給区分 (高校生給付型奨学金、母子家庭奨学金)
            $extra  = "id=\"HEIKYUU_SHOUGAKU_FLG1".$setSeq."\" onclick=\"heikyuCheck1('{$setSeq}'); getTaiyoMasterGk();\"";
            $extra .= ($KyuhuRow["HEIKYUU_SHOUGAKU_FLG1".$setSeq] == "1") ? " checked" : "";
            $arg["data2"]["HEIKYUU_SHOUGAKU_FLG1".$setSeq] = knjCreateCheckBox($objForm, "HEIKYUU_SHOUGAKU_FLG1".$setSeq, "1", $extra);
            
            $extra  = "id=\"HEIKYUU_SHOUGAKU_FLG2".$setSeq."\" onclick=\"heikyuCheck2('{$setSeq}'); getTaiyoMasterGk();\"";
            $extra .= ($KyuhuRow["HEIKYUU_SHOUGAKU_FLG2".$setSeq] == "1") ? " checked" : "";
            $arg["data2"]["HEIKYUU_SHOUGAKU_FLG2".$setSeq] = knjCreateCheckBox($objForm, "HEIKYUU_SHOUGAKU_FLG2".$setSeq, "1", $extra);

            //課程情報（奨学給付金、貸与限度額用に取得）
            $query = knjta020_07Query::getKateiInfo($KyuhuRow, $seq);
            $getKateiRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //奨学給付金　決定時のみ表示
            //給付申請データ確認
            $query = knjta020_07Query::getKyuhuInfo($model, "", $seq);
            $getShinseiRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $query = knjta020_07Query::getMasterKyuhuGkDat($KyuhuRow, $seq, $getKateiRow["KATEI_DIV"], $getKateiRow["KOUSHI_DIV"]);
            $getMasterKyuhuGk = $db->getOne($query);
            $query = knjta020_07Query::getKeikakuKyuhuGkDat($getShinseiRow, $seq);
            $getKeikakuKyuhuGk = $db->getOne($query);
            $comment = "";
            $cancel_comment = "";
            //給付計画データが未作成
            if ($getKeikakuKyuhuGk == "") {
                //給付決定済みで計画データ未作成の場合はコメントを表示
                if ($getShinseiRow["KYUHU_SHUUGAKU_NO"] != "" && $getShinseiRow["KYUHU_KETTEI_DATE"] != "" && $getShinseiRow["KYUHU_KETTEI_FLG"] != "") {
                    $getKeikakuKyuhuGk = '<font color="red">支払計画未作成</font>';
                }
                if ($getMasterKyuhuGk == "") {
                    $comment = '<font color="red">　(マスタより奨学給付金が取得できません)</font>';
                }
            //給付計画データ作成済み
            } else {
                if ($getMasterKyuhuGk == "") {
                    $comment = '<font color="red">　(マスタより奨学給付金が取得できません)</font>';
                }
            }
            if ($getShinseiRow["CANCEL_FLG"] != "") {
                $cancel_comment = '<font color="red">　給付申請キャンセル済み</font>';
            }
            $arg["data2"]["KYUHU_GK".$setSeq] = $getKeikakuKyuhuGk;
            
            //給付予定額
            $arg["data2"]["KYUHU_GK_NAME".$setSeq] = ($KyuhuRow["KYUHU_KETTEI_FLG".$setSeq] == "1" && $KyuhuRow["KYUHU_KETTEI_DATE".$setSeq] != "") ? '<font color="red">奨学給付金決定額</font>' : '奨学給付金予定額';
            $setKyuhuGk = getKyuhuGk($KyuhuRow, $getMasterKyuhuGk, $minMonth, $maxMonth, $monthCnt, $calcDenominator, $calcNumerator);
            if ($model->cmd === 'KyuhuSubmit') {
                $KyuhuRow["KYUHU_YOTEI_GK".$setSeq] = $setKyuhuGk;
            } else {
                $KyuhuRow["KYUHU_YOTEI_GK".$setSeq] = ($KyuhuRow["KYUHU_YOTEI_GK".$setSeq] == "") ? $setKyuhuGk : $KyuhuRow["KYUHU_YOTEI_GK".$setSeq];
            }
            $extra = "STYLE=\"ime-mode: inactive;\"onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
            $arg["data2"]["KYUHU_YOTEI_GK".$setSeq] = knjCreateTextBox($objForm, $KyuhuRow["KYUHU_YOTEI_GK".$setSeq], "KYUHU_YOTEI_GK".$setSeq, 6, 6, $extra);
            $arg["data2"]["MASTER_COMMENT".$setSeq] = $comment;
            $arg["data2"]["CANCEL_COMMENT".$setSeq] = $cancel_comment;

            //減額申請表示
            if (is_array($getShinseiRow)) {
                //貸与限度額取得
                $query = knjta020_07Query::getTaiyoGendoGkDat($Row["TSUUGAKU_DIV"], $KyuhuRow, $seq, $getKateiRow["KATEI_DIV"], $getKateiRow["KOUSHI_DIV"]);
                $getTaiyoGendoGk = $db->getOne($query);
                if ($getTaiyoGendoGk == "") {
                    $getTaiyoGendoGk = '<font color="red">マスタの貸与限度額が取得できません</font>';
                }
                $arg["data2"]["TAIYO_GENDO_GK".$setSeq] = $getTaiyoGendoGk;
                //減額申請データ取得
                $query = knjta020_07Query::getShinseiGengaku($Row["SHUUGAKU_NO"], $model);
                $getGengakuRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (is_array($getGengakuRow)) {
                    $arg["data2"]["GENGAKU_DOUI_STATUS"] = '減額申請済';
                } else {
                    $arg["data2"]["GENGAKU_DOUI_STATUS"] = '減額申請未';
                }
            }

            //入力完了
            $extra  = "id=\"KANRYOU_FLG{$setSeq}\"";
            $extra .= ($KyuhuRow["KANRYOU_FLG".$setSeq] == "1") ? " checked" : "";
            $arg["data2"]["KANRYOU_FLG".$setSeq] = knjCreateCheckBox($objForm, "KANRYOU_FLG".$setSeq, "1", $extra);

            //備考
            $gyou = 5;
            $moji = 55;
            $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;
            $extra = "STYLE=\"ime-mode: active;height:{$height}px;\" ";
            $arg["data2"]["KYUHU_REMARK".$setSeq] = knjCreateTextArea($objForm, "KYUHU_REMARK".$setSeq, $gyou, ($moji * 2 + 1), "soft", $extra, $KyuhuRow["KYUHU_REMARK".$setSeq]);

            //ボタン作成
            makeBtn2($objForm, $arg, $model, $db, $Row, $kjnRow, $KyuhuRow, $seq);

        }

        /*******************/
        /* 給付処理終了 ****/
        /*******************/

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjta020_07Form1.html", $arg); 
    }
}
//処理状況
function getShoriJoukyo($db, $Row, $stk, $syoriJyouKyou) {
    $rtn = "";
    if ($stk == "" && $Row["SHUUGAKU_NO"] != "" && $Row["SHINSEI_YEAR"] != "") {
        $query = knjta020_07Query::getShuugakuJyoukyou($Row["SHUUGAKU_NO"], $Row["SHINSEI_YEAR"]);
        $rtn = $db->getOne($query);
    } else if ($stk != "" && $Row["STK_SHUUGAKU_NO"] != "" && $Row["STK_SHINSEI_YEAR"] != ""){
        $query = knjta020_07Query::getShuugakuJyoukyou($Row["STK_SHUUGAKU_NO"], $Row["STK_SHINSEI_YEAR"]);
        $rtn = $db->getOne($query);
    }
    if (!$rtn) {
        $query = knjta020_07Query::getSyoriName($syoriJyouKyou);
        $rtn = $db->getOne($query);
    }
    if (strlen($Row[$stk."SHINSEI_YEAR"])) {
        $nendo = common::getDateHenkan($Row[$stk."SHINSEI_YEAR"].'-04-01', "nendhan");
        $rtn = $nendo ."<BR>" .$rtn;
    }
    return $rtn;
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if ($name == "KATEI") {
            knjCreateHidden($objForm, "SMCNT".$row["VALUE"], $row["SMCNT"]);
        }
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SHINSEI_G" || $name == "UKE_G" || $name == "STK_SHINSEI_G" || $name == "STK_UKE_G") {
        $value = ($value && $value_flg) ? $value : 4;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成
function makeCombo2(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if ($name == "KATEI") {
            knjCreateHidden($objForm, "SMCNT".$row["VALUE"], $row["SMCNT"]);
        }
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SHINSEI_G" || $name == "UKE_G") {
        $value = ($value && $value_flg) ? $value : 4;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成(給付用)
function makeCombo3(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
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

    if ($name == "KYUHU_SHINSEI_G" || $name == "KYUHU_UKE_G") {
        $value = ($value && $value_flg) ? $value : 4;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data2"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db, $Row, $kjnRow) {
    $disBtn = strlen($Row["SHINSEI_YEAR"]) ? "" : " disabled";

    //担当課チェック
    $sectionFlg = "true";
    //権限チェック
    if ($model->auth != DEF_UPDATABLE) {
        //高等課
        if ($model->sectioncd === '0001') {
            $sectionFlg = "true";
        //文教課
        } else if ($model->sectioncd === '0002') {
            $sectionFlg = "false";
            $disBtn = " disabled";
        //未設定
        } else {
            $sectionFlg = "false";
            $disBtn = " disabled";
        }
    }

    //生徒履歴修正
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_KOJIN/knjtx_kojinindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["UKE_EDABAN"]}&SEND_UN_UPD={$kjnRow["SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["btn_seito"] = knjCreateBtn($objForm, "btn_seito", "生徒履歴修正", "onclick=\"$subdata\"".$disBtn);
    //申請履歴情報
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_SHINSEI/knjtx_shinseiindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["SGK_ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["btn_shinsei"] = knjCreateBtn($objForm, "btn_shinsei", "申請履歴情報", "onclick=\"$subdata\"".$disBtn);
    //親権者又は未成年後見人
    $extra = "style=\"height:35px;width:100px;text-align:center;\" ";
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_SHINKENSHA/knjtx_shinkenshaindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}&SEND_TYPE=1";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["SGK_ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["btn_shinken1"] = knjCreateBtn($objForm, "btn_shinken1", "親権者又は\n未成年後見人", $extra."onclick=\"$subdata\"".$disBtn);
    //親権者
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_SHINKENSHA/knjtx_shinkenshaindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}&SEND_TYPE=2";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["SGK_ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["btn_shinken2"] = knjCreateBtn($objForm, "btn_shinken2", "親権者", $extra."onclick=\"$subdata\"".$disBtn);
    //連帯保証人
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_SHINKENSHA/knjtx_shinkenshaindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}&SEND_TYPE=3";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["SGK_ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["btn_rentai"] = knjCreateBtn($objForm, "btn_rentai", "連帯保証人", $extra."onclick=\"$subdata\"".$disBtn);
    //親権者又は未成年後見人
    if ($sectionFlg == "true") {
        $disBtnSTK = strlen($Row["STK_SHINSEI_YEAR"]) ? "" : " disabled";
    } else {
        $disBtnSTK = "disabled";
    }
    $extra = "style=\"height:35px;width:100px;text-align:center;\" ";
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_SHINKENSHA/knjtx_shinkenshaindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}&SEND_TYPE=1";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["STK_SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["STK_UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["STK_UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["STK_UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["STK_ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["STK_SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["STK_SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["stk_btn_shinken1"] = knjCreateBtn($objForm, "stk_btn_shinken1", "親権者又は\n未成年後見人", $extra."onclick=\"$subdata\"".$disBtnSTK);
    //親権者
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_SHINKENSHA/knjtx_shinkenshaindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}&SEND_TYPE=2";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["STK_SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["STK_UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["STK_UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["STK_UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["STK_ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["STK_SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["STK_SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["stk_btn_shinken2"] = knjCreateBtn($objForm, "stk_btn_shinken2", "親権者", $extra."onclick=\"$subdata\"".$disBtnSTK);
    //連帯保証人
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_SHINKENSHA/knjtx_shinkenshaindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}&SEND_TYPE=3";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["STK_SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["STK_UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["STK_UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["STK_UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["STK_ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["STK_SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["STK_SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["stk_btn_rentai"] = knjCreateBtn($objForm, "stk_btn_rentai", "連帯保証人", $extra."onclick=\"$subdata\"".$disBtnSTK);
    //世帯状況情報
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_SETAI/knjtx_setaiindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["SGK_ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["btn_setai"] = knjCreateBtn($objForm, "btn_setai", "世帯状況情報", "onclick=\"$subdata\"".$disBtn);
    //振込先情報
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_FURIKOMI/knjtx_furikomiindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["btn_furikomi"] = knjCreateBtn($objForm, "btn_furikomi", "振込先情報", "onclick=\"$subdata\"".$disBtn);

    $query = knjta020_07Query::getSetaiCnt($model, $kjnRow);
    $setaiCnt = $db->getOne($query);
    $keisanDis = $setaiCnt > 0 ? "" : " disabled ";
    //審査計算書1
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_TAIYO_SHINSA/knjtx_taiyo_shinsaindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["SGK_ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["btn_taiyo_shinsa"] = knjCreateBtn($objForm, "btn_taiyo_shinsa", "審査計算書1", $keisanDis."onclick=\"$subdata\"".$disBtn);
    //審査計算書2
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_YUSHI_SHINSA/knjtx_yushi_shinsaindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}";
    $subdata .= "&SEND_SHINSEI_YEAR={$kjnRow["SHINSEI_YEAR"]}&SEND_UKE_YEAR={$kjnRow["UKE_YEAR"]}&SEND_UKE_NO={$kjnRow["UKE_NO"]}&SEND_UKE_EDABAN={$kjnRow["UKE_EDABAN"]}&SEND_ISSUEDATE={$kjnRow["SGK_ISSUEDATE"]}&SEND_UN_UPD={$kjnRow["SEND_UN_UPD"]}&SEND_SHIKIN_SHOUSAI_DIV={$kjnRow["SHIKIN_SHOUSAI_DIV"]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button"]["btn_yushi_shinsa"] = knjCreateBtn($objForm, "btn_yushi_shinsa", "審査計算書2", $keisanDis."onclick=\"$subdata\"".$disBtn);
    //審査計算書の「判定結果」等のチェック用
    $query = knjta020_07Query::getShinsaCnt($model, $kjnRow, "1");
    knjCreateHidden($objForm, "SHINSA_HANTEI1", $db->getOne($query) > 0 ? "OK" : "NG");
    $query = knjta020_07Query::getShinsaCnt($model, $kjnRow, "2");
    knjCreateHidden($objForm, "SHINSA_HANTEI2", $db->getOne($query) > 0 ? "OK" : "NG");
    $query = knjta020_07Query::getShinsaCnt($model, $kjnRow, "3");
    knjCreateHidden($objForm, "SHINSA_HANTEI3", $db->getOne($query) > 0 ? "OK" : "NG");
    knjCreateHidden($objForm, "SHINSA_YEAR", $kjnRow["SHINSEI_YEAR"]);
    //更新
    if ($sectionFlg == "true") {
        $disUpdBtn = $kjnRow["SEND_UN_UPD"] == "1" ? " disabled" : "";
    } else {
        $disUpdBtn = "disabled";
    }
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "更 新", $extra.$disUpdBtn);
    //削除
    $disDelBtn = $Row["KETTEI_FLG"] == "1" || $Row["STK_KETTEI_FLG"] == "1" || strlen($model->sendShoriDiv) ? " disabled" : $disBtn;
    knjCreateHidden($objForm, "KETTEI_FLG", $Row["KETTEI_FLG"]);
    knjCreateHidden($objForm, "STK_KETTEI_FLG", $Row["STK_KETTEI_FLG"]);
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_upd", "削 除", $extra.$disDelBtn);
    //取消
    $extra = "onclick=\"return btn_submit('cancel');\"";
    $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", $extra);
    //終了
    $extra = "onclick=\"btnCloseWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//ボタン作成(給付用)
function makeBtn2(&$objForm, &$arg, $model, $db, $Row, $kjnRow, $KyuhuRow, $seq) {
    $setSeq = $seq == 1 ? '' : '_'.$seq;
    $disBtn = strlen($KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq]) ? "" : " disabled";
    if (strlen($model->sendKyuhuDiv)) {
        $disBtn = " disabled";
    }
    //給付の公私区分を取得
    $query = knjta020_07Query::getKyuhuSchoolDistcd($model, $seq, $setSeq);
    $getKoushi = $db->getOne($query);
    
    //担当課チェック
    $sectionFlg = "true";
    //権限チェック
    if ($model->auth != DEF_UPDATABLE) {
        //高等課
        if ($model->sectioncd === '0001') {
            if ($getKoushi === '3') {
                $sectionFlg = "false";
                $disBtn = " disabled";
            }
        //文教課
        } else if ($model->sectioncd === '0002') {
            if ($getKoushi !== '3' && $getKoushi != "") {
                $sectionFlg = "false";
                $disBtn = " disabled";
            }
        //未設定
        } else {
            $sectionFlg = "false";
            $disBtn = " disabled";
        }
    }

    //給付申請履歴情報
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_KYUHU_SHINSEI/knjtx_kyuhu_shinseiindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}";
    $subdata .= "&SEND_SHINSEI_YEAR={$KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq]}&SEND_UKE_YEAR={$KyuhuRow["KYUHU_UKE_YEAR".$setSeq]}&SEND_UKE_NO={$KyuhuRow["KYUHU_UKE_NO".$setSeq]}&SEND_UKE_EDABAN={$KyuhuRow["KYUHU_UKE_EDABAN".$setSeq]}&SEND_UN_UPD={$KyuhuRow["SEND_UN_UPD".$setSeq]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button2"]["btn_shinsei".$setSeq] = knjCreateBtn($objForm, "btn_shinsei".$setSeq, "給付申請履歴情報", "onclick=\"$subdata\"".$disBtn);

    //保護者(申請者)
    $btnSize = " style=\"height:35px;width:100px;text-align:center;\"";
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_SHINKENSHA/knjtx_shinkenshaindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}&SEND_TYPE=4";
    $subdata .= "&SEND_SHINSEI_YEAR={$KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq]}&SEND_UKE_YEAR={$KyuhuRow["KYUHU_UKE_YEAR".$setSeq]}&SEND_UKE_NO={$KyuhuRow["KYUHU_UKE_NO".$setSeq]}&SEND_UKE_EDABAN={$KyuhuRow["KYUHU_UKE_EDABAN".$setSeq]}&SEND_UN_UPD={$KyuhuRow["SEND_UN_UPD".$setSeq]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button2"]["btn_hogosha".$setSeq] = knjCreateBtn($objForm, "btn_hogosha".$setSeq, "&nbsp;&nbsp;保護者\n(申請者)", $extra."onclick=\"$subdata\"".$disBtn.$btnSize);

    //保護者2
    $btnSize = " style=\"height:35px;width:100px;text-align:center;\"";
    $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_SHINKENSHA/knjtx_shinkenshaindex.php?cmd=main";
    $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}&SEND_TYPE=5";
    $subdata .= "&SEND_SHINSEI_YEAR={$KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq]}&SEND_UKE_YEAR={$KyuhuRow["KYUHU_UKE_YEAR".$setSeq]}&SEND_UKE_NO={$KyuhuRow["KYUHU_UKE_NO".$setSeq]}&SEND_UKE_EDABAN={$KyuhuRow["KYUHU_UKE_EDABAN".$setSeq]}&SEND_UN_UPD={$KyuhuRow["SEND_UN_UPD".$setSeq]}";
    $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $arg["button2"]["btn_hogosha2".$setSeq] = knjCreateBtn($objForm, "btn_hogosha2".$setSeq, "保護者", $extra."onclick=\"$subdata\"".$disBtn.$btnSize);

    //以降、一か所だけ作成するボタン(画面の最後に表示。処理状況3の1回目/2回目どちらでも最終行の1箇所にだけ表示)
    if ($seq == 1) {
        //振込先情報
        $subdata  = "wopen('".REQUESTROOT."/X/KNJTX_FURIKOMI/knjtx_furikomiindex.php?cmd=main";
        $subdata .= "&SEND_PRGID=KNJTA020_07&SEND_AUTH={$model->auth}&SEND_KOJIN_NO={$model->kojinNo}";
        $subdata .= "&SEND_SHINSEI_YEAR={$KyuhuRow["KYUHU_SHINSEI_YEAR".$setSeq]}&SEND_UKE_YEAR={$KyuhuRow["KYUHU_UKE_YEAR".$setSeq]}&SEND_UKE_NO={$KyuhuRow["KYUHU_UKE_NO".$setSeq]}&SEND_UKE_EDABAN={$KyuhuRow["KYUHU_UKE_EDABAN".$setSeq]}&SEND_UN_UPD={$KyuhuRow["SEND_UN_UPD"]}";
        $subdata .= "&SEND_SHORI_DIV={$model->sendShoriDiv}&SEND_KYUHU_DIV={$model->sendKyuhuDiv}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $arg["button2"]["btn_furikomi"] = knjCreateBtn($objForm, "btn_furikomi", "振込先情報", "onclick=\"$subdata\"".$disBtn);

        //更新
        if ($KyuhuRow["CANCEL_FLG"] == "1" || $KyuhuRow["CANCEL_FLG_2"] == "1") {
            $disUpdBtn = "disabled";
        } else {
            $disUpdBtn = $kjnRow["SEND_UN_UPD"] == "1" ? " disabled" : "";
            $disUpdBtn = $kjnRow["SEND_UN_UPD_2"] == "1" ? " disabled" : $disUpdBtn;
        }
        $extra = "onclick=\"return btn_submit('kyuhu_update');\"";
        $arg["button2"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "更 新", $extra.$disUpdBtn);
        //削除
        if (($KyuhuRow["KYUHU_KETTEI_FLG"] == "1" || $KyuhuRow["CANCEL_FLG"] == "1") 
            || ($KyuhuRow["KYUHU_KETTEI_FLG_2"] == "1" || $KyuhuRow["CANCEL_FLG_2"] == "1") ) {
            $disDelBtn = "disabled";
        } else {
            $disDelBtn = $disBtn;
        }
        
        $extra = "onclick=\"return btn_submit('kyuhu_delete');\"";
        $arg["button2"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disDelBtn);
        //取消
        $extra = "onclick=\"return btn_submit('cancel');\"";
        $arg["button2"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", $extra);
        //終了
        $extra = "onclick=\"btnCloseWin();\"";
        $arg["button2"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    } else if ($seq == 2) {
        $btnSize = " style=\"height:20px;width:190px;text-align:center;\"";
        $subdata = "copyDefDataTo2nd()";
        $arg["button2"]["btn_copydata"] = knjCreateBtn($objForm, "btn_copydata", "処理状況3 1回目からコピー", $extra."onclick=\"$subdata\"".$disBtn.$btnSize);
    }
    knjCreateHidden($objForm, "KYUHU_KETTEI_FLG".$setSeq, $KyuhuRow["KYUHU_KETTEI_FLG".$setSeq]);
    knjCreateHidden($objForm, "CANCEL_FLG".$setSeq, $KyuhuRow["CANCEL_FLG".$setSeq]);
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
//給付金額
function getKyuhuGk($KyuhuRow, $getMasterKyuhuGk, $minMonth, $maxMonth, $monthCnt, $calcDenominator, $calcNumerator) {
    if ($KyuhuRow["KAKEI_KYUHEN_FLG"] == '1' && 0 < strlen($KyuhuRow["KAKEI_KYUHEN_DATE"])) {
        $kyuhenYMD = explode("-", $KyuhuRow["KAKEI_KYUHEN_DATE"]);
        $kyufuMonth = $kyuhenYMD[1];
        $month = ((int)$kyufuMonth * 1) > 3 ? ((int)$kyufuMonth * 1) : ((int)$kyufuMonth * 1) + 12;
        $month = $month < $minMonth ? $minMonth : $month;

        $taisyouMonthCnt = 0;
        if ($minMonth <= $month && $month <= $maxMonth) {
            $taisyouMonthCnt = (int)$maxMonth - ((int)$month * 1) + 1;
        } else {
            $taisyouMonthCnt = 0;
        }

        if ($minMonth == $month) {
            $setKyuhuGk = floor((int)$getMasterKyuhuGk / (int)$calcDenominator * (int)$calcNumerator);
        } else {
            $setKyuhuGk = floor((int)$getMasterKyuhuGk / (int)$calcDenominator * (int)$calcNumerator / (int)$monthCnt) * $taisyouMonthCnt;
        }
    } else {
        $setKyuhuGk = floor((int)$getMasterKyuhuGk / (int)$calcDenominator * (int)$calcNumerator);
    }
    return $setKyuhuGk;
}
?>
