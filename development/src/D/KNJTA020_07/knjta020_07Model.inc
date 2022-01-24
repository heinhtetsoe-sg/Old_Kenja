<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjta020_07Model extends Model {

    var $cmd;
    var $field = array();
    var $rireki = array();
    var $sendAuth;          //親画面の権限
    var $sendShoriDiv;      //親画面の処理区分
    var $sendShinseiYear;   //親画面の申請年度
    var $shinseiYear;   //申請年度
    var $auth;      //権限
    var $kojinNo;   //個人番号
    var $sanshouYear;   //参照年度
    var $sanshouDate;   //参照日付
    var $sendSanshouYear;   //子画面の参照年度
    var $sendSanshouDate;   //子画面の参照年度
    var $sendPrgid;   //コール元プログラムＩＤ
    var $kojin_count;
    var $kyuhuMax;          //以下,給付用
    var $getTaiyoGendoGk;
    var $field2 = array();
    var $sendKyuhuDiv;
    var $sectioncd;
    var $maxKyuhuDiv;
    var $maxSeqLp;
    var $updKyuhuCnt;

    function init() {
        //別画面からコール
        if (VARS::get("SEND_AUTH")) {
            $this->sendAuth = VARS::get("SEND_AUTH");
            $this->kojinNo = VARS::get("SEND_KOJIN_NO");
            $this->sendShoriDiv = VARS::get("SEND_SHORI_DIV");
            $this->sendShinseiYear = VARS::get("SEND_SHINSEI_YEAR");
            $this->sendPrgid = VARS::get("SEND_PRGID");
            $this->field = array();
            $this->rireki = array();
            $this->kojin_count = "";
            unset($this->setSetaiSeq);
            $this->kyuhuMax = "";
            $this->sendKyuhuDiv = VARS::get("SEND_KYUHU_DIV");
            $this->sectioncd = "";
        }
        $this->auth = $this->sendAuth ? $this->sendAuth : AUTHORITY;

        $this->cmd         = VARS::request("cmd");

        //申請年度
        $this->shinseiYear = (VARS::get("SEND_AUTH")) ? $this->sendShinseiYear : $this->shinseiYear;

        if (VARS::get("SEND_SETAI_SEQ")) {
            $this->setSetaiSeq = VARS::get("SEND_SETAI_SEQ");
        }

        //メイン画面(表示or入力項目)
        $this->field = array("KOJIN_NO"                 => VARS::post("KOJIN_NO"),
                             "FAMILY_NAME"              => VARS::post("FAMILY_NAME"),
                             "FIRST_NAME"               => VARS::post("FIRST_NAME"),
                             "FAMILY_NAME_KANA"         => VARS::post("FAMILY_NAME_KANA"),
                             "FIRST_NAME_KANA"          => VARS::post("FIRST_NAME_KANA"),
                             "BIRTHDAY"                 => common::getDateHenkan(VARS::post("BIRTHDAY"), "wymd"),
                             "NENREI"                   => VARS::post("NENREI"),
                             "KIKON_FLG"                => VARS::post("KIKON_FLG"),

                             "ZIPCD"                    => VARS::post("ZIPCD"),
                             "CITYCD"                   => VARS::post("CITYCD"),
                             "ADDR1"                    => VARS::post("ADDR1"),
                             "ADDR2"                    => VARS::post("ADDR2"),
                             "TELNO1"                   => VARS::post("TELNO1"),
                             "TELNO2"                   => VARS::post("TELNO2"),
    //02:修直
                             "SHINSEI_YEAR"             => common::getDateHenkan(VARS::post("SHINSEI_G").VARS::post("SHINSEI_YY"), "wy"),
                             "UKE_YEAR"                 => common::getDateHenkan(VARS::post("UKE_G").(VARS::post("UKE_YY") ? sprintf("%02d", VARS::post("UKE_YY")) : ""), "wy"),
                             "UKE_NO"                   => VARS::post("UKE_NO"),
                             "UKE_EDABAN"               => VARS::post("UKE_EDABAN"),
                             "SHINSEI_DATE"             => common::getDateHenkan(VARS::post("SHINSEI_DATE"), "wymd"),
                             "SHORI_JYOUKYOU1"          => VARS::post("SHORI_JYOUKYOU1") ? VARS::post("SHORI_JYOUKYOU1") : $this->field["SHORI_JYOUKYOU1"],

                             "SHUUGAKU_NO"              => VARS::post("SHUUGAKU_NO"),
                             "SHIKIN_SHOUSAI_DIV"       => VARS::post("SHIKIN_SHOUSAI_DIV"),

                             "H_SCHOOL_CD"              => VARS::request("H_SCHOOL_CD"),
                             "KATEI"                    => VARS::request("KATEI"),
                             "GRADE"                    => VARS::post("GRADE"),
                             "H_GRAD_YM"                => common::getDateHenkan(VARS::post("H_GRAD_YM"), "wym"),

                             "TSUUGAKU_DIV"             => VARS::post("TSUUGAKU_DIV"),

                             "YOYAKU_KIBOU_GK"          => VARS::post("YOYAKU_KIBOU_GK"),
                             "S_YOYAKU_KIBOU_YM"        => common::getDateHenkan(VARS::post("S_YOYAKU_KIBOU_YM"), "wym"),
                             "E_YOYAKU_KIBOU_YM"        => common::getDateHenkan(VARS::post("E_YOYAKU_KIBOU_YM"), "wym"),
                             "S_TAIYO_YM"               => common::getDateHenkan(VARS::post("S_TAIYO_YM"), "wym"),
                             "E_TAIYO_YM"               => common::getDateHenkan(VARS::post("E_TAIYO_YM"), "wym"),

                             "HEIKYUU_SHOUGAKU_STATUS1" => VARS::post("HEIKYUU_SHOUGAKU_STATUS1"),
                             "HEIKYUU_SHOUGAKU_STATUS2" => VARS::post("HEIKYUU_SHOUGAKU_STATUS2"),
                             "HEIKYUU_SHOUGAKU_REMARK2" => VARS::post("HEIKYUU_SHOUGAKU_REMARK2"),
                             "HEIKYUU_SHOUGAKU_STATUS3" => VARS::post("HEIKYUU_SHOUGAKU_STATUS3"),
                             "HEIKYUU_SHOUGAKU_REMARK3" => VARS::post("HEIKYUU_SHOUGAKU_REMARK3"),
                             "HEIKYUU_SHOUGAKU_GYOUMUCD2" => VARS::post("HEIKYUU_SHOUGAKU_GYOUMUCD2"),
                             "HEIKYUU_SHOUGAKU_GYOUMUCD3" => VARS::post("HEIKYUU_SHOUGAKU_GYOUMUCD3"),

                             "RENTAI_CD"                => VARS::post("RENTAI_CD"),
                             "SHINKEN1_CD"              => VARS::post("SHINKEN1_CD"),
                             "SHINKEN2_CD"              => VARS::post("SHINKEN2_CD"),

                             "SHITAKU_CANCEL_CHOKU_FLG" => VARS::post("SHITAKU_CANCEL_CHOKU_FLG"),

                             "SHINSEI_KANRYOU_FLG"      => VARS::post("SHINSEI_KANRYOU_FLG"),
    //03:支直
                             "STK_SHINSEI_YEAR"             => common::getDateHenkan(VARS::post("STK_SHINSEI_G").VARS::post("STK_SHINSEI_YY"), "wy"),
                             "STK_UKE_YEAR"                 => common::getDateHenkan(VARS::post("STK_UKE_G").(VARS::post("STK_UKE_YY") ? sprintf("%02d", VARS::post("STK_UKE_YY")) : ""), "wy"),
                             "STK_UKE_NO"                   => VARS::post("STK_UKE_NO"),
                             "STK_UKE_EDABAN"               => VARS::post("STK_UKE_EDABAN"),
                             "STK_SHINSEI_DATE"             => common::getDateHenkan(VARS::post("STK_SHINSEI_DATE"), "wymd"),
                             "SHORI_JYOUKYOU2"              => VARS::post("SHORI_JYOUKYOU2") ? VARS::post("SHORI_JYOUKYOU2") : $this->field["SHORI_JYOUKYOU2"],

                             "STK_SHUUGAKU_NO"              => VARS::post("STK_SHUUGAKU_NO"),
                             "STK_SHIKIN_SHOUSAI_DIV"       => VARS::post("STK_SHIKIN_SHOUSAI_DIV"),

                             "STK_H_SCHOOL_CD"              => VARS::request("STK_H_SCHOOL_CD"),
                             "STK_KATEI"                    => VARS::request("STK_KATEI"),
                             "STK_GRADE"                    => VARS::post("STK_GRADE"),

                             "STK_SHITAKUKIN_TAIYO_DIV" => VARS::post("STK_SHITAKUKIN_TAIYO_DIV"),

                             "STK_HEIKYUU_SHITAKU_STATUS1" => VARS::post("STK_HEIKYUU_SHITAKU_STATUS1"),
                             "STK_HEIKYUU_SHITAKU_STATUS2" => VARS::post("STK_HEIKYUU_SHITAKU_STATUS2"),
                             "STK_HEIKYUU_SHITAKU_REMARK2" => VARS::post("STK_HEIKYUU_SHITAKU_REMARK2"),
                             "STK_HEIKYUU_SHITAKU_STATUS3" => VARS::post("STK_HEIKYUU_SHITAKU_STATUS3"),
                             "STK_HEIKYUU_SHITAKU_REMARK3" => VARS::post("STK_HEIKYUU_SHITAKU_REMARK3"),
                             "STK_HEIKYUU_SHITAKU_GYOUMUCD2" => VARS::post("STK_HEIKYUU_SHITAKU_GYOUMUCD2"),
                             "STK_HEIKYUU_SHITAKU_GYOUMUCD3" => VARS::post("STK_HEIKYUU_SHITAKU_GYOUMUCD3"),

                             "STK_RENTAI_CD"                => VARS::post("STK_RENTAI_CD"),
                             "STK_SHINKEN1_CD"              => VARS::post("STK_SHINKEN1_CD"),
                             "STK_SHINKEN2_CD"              => VARS::post("STK_SHINKEN2_CD"),

                             "STK_SHINSEI_KANRYOU_FLG"  => VARS::post("STK_SHINSEI_KANRYOU_FLG"),
                             "REMARK"                   => VARS::post("REMARK"),
                             "KETTEI_FLG"               => VARS::post("KETTEI_FLG"),
                             "STK_KETTEI_FLG"           => VARS::post("STK_KETTEI_FLG")
                             );
        $this->field["UKE_NO"] = $this->field["UKE_NO"] ? sprintf("%04d", $this->field["UKE_NO"]) : "";
        $this->field["UKE_EDABAN"] = $this->field["UKE_EDABAN"] ? sprintf("%03d", $this->field["UKE_EDABAN"]) : "";
        $this->field["STK_UKE_NO"] = $this->field["STK_UKE_NO"] ? sprintf("%04d", $this->field["STK_UKE_NO"]) : "";
        $this->field["STK_UKE_EDABAN"] = $this->field["STK_UKE_EDABAN"] ? sprintf("%03d", $this->field["STK_UKE_EDABAN"]) : "";

        $this->field2 = array();
        $this->field2["KAKEI_KYUHEN_FLG"]  = VARS::post("KAKEI_KYUHEN_FLG");
        $this->field2["KAKEI_KYUHEN_DATE"] = common::getDateHenkan(VARS::post("KAKEI_KYUHEN_DATE"), "wymd");
        $this->field2["KYUHU_SHINSEI_DIV"] = VARS::post("KYUHU_SHINSEI_DIV");
        $this->field2["KYUHU_MAX_SEQ"]     = VARS::post("KYUHU_MAX_SEQ");
        //給付
        for ($seq = 1; $seq <= 2; $seq++) {
            $setSeq = $seq == 1 ? "" : "_2";
            $this->field2["KYUHU_SHINSEI_YEAR".$setSeq] = common::getDateHenkan(VARS::post("KYUHU_SHINSEI_G".$setSeq).VARS::post("KYUHU_SHINSEI_YY".$setSeq), "wy");
            $this->field2["KYUHU_SHINSEI_DATE".$setSeq] = common::getDateHenkan(VARS::post("KYUHU_SHINSEI_DATE".$setSeq), "wymd");
            $this->field2["KYUHU_UKE_YEAR".$setSeq]     = common::getDateHenkan(VARS::post("KYUHU_UKE_G".$setSeq).(VARS::post("KYUHU_UKE_YY".$setSeq) ? sprintf("%02d", VARS::post("KYUHU_UKE_YY".$setSeq)) : ""), "wy");
            $this->field2["KYUHU_UKE_NO".$setSeq]       = VARS::post("KYUHU_UKE_NO".$setSeq);
            $this->field2["KYUHU_UKE_EDABAN".$setSeq]   = VARS::post("KYUHU_UKE_EDABAN".$setSeq);

            $this->field2["KYUHU_SHUUGAKU_NO".$setSeq]  = VARS::post("KYUHU_SHUUGAKU_NO".$setSeq);

            $this->field2["SHORI_JYOUKYOU3".$setSeq]    = VARS::post("SHORI_JYOUKYOU3".$setSeq) ? VARS::post("SHORI_JYOUKYOU3".$setSeq) : $this->field2["SHORI_JYOUKYOU3".$setSeq];
            $this->field2["HOGOSHA_CD".$setSeq]         = VARS::post("HOGOSHA_CD".$setSeq);
            $this->field2["HOGOSHA2_CD".$setSeq]        = VARS::post("HOGOSHA2_CD".$setSeq);

            $this->field2["KYUHU_KAISUU".$setSeq]       = VARS::post("KYUHU_KAISUU".$setSeq);
            $this->field2["KYUHU_H_SCHOOL_CD".$setSeq]  = VARS::request("KYUHU_H_SCHOOL_CD".$setSeq);
            $this->field2["KYUHU_RITSUCD".$setSeq]      = VARS::post("KYUHU_RITSUCD".$setSeq);
            $this->field2["KYUHU_KATEI".$setSeq]        = VARS::request("KYUHU_KATEI".$setSeq);
            $this->field2["KATEI_DIV".$setSeq]          = VARS::request("KATEI_DIV".$setSeq);
            $this->field2["KYUHU_GRADE".$setSeq]        = VARS::post("KYUHU_GRADE".$setSeq);
            $this->field2["KYUHU_HR_CLASS".$setSeq]     = VARS::post("KYUHU_HR_CLASS".$setSeq);
            $this->field2["KYUHU_ATTENDNO".$setSeq]     = VARS::post("KYUHU_ATTENDNO".$setSeq);
                                 
            $this->field2["SHOTOKUWARI_DIV".$setSeq]    = VARS::post("SHOTOKUWARI_DIV".$setSeq);
            $this->field2["SHOTOKUWARI_GK".$setSeq]     = VARS::post("SHOTOKUWARI_GK".$setSeq);
            $this->field2["SHOTOKUWARI_GK_CHECK_FLG".$setSeq] = VARS::post("SHOTOKUWARI_GK_CHECK_FLG".$setSeq);
                                 
            $this->field2["KYOUDAI_BIRTHDAY".$setSeq]         = common::getDateHenkan(VARS::post("KYOUDAI_BIRTHDAY".$setSeq), "wymd");
            $this->field2["KYOUDAI_TSUZUKIGARA_CD".$setSeq]   = VARS::post("KYOUDAI_TSUZUKIGARA_CD".$setSeq);
            $this->field2["KYOUDAI_FAMILY_NAME".$setSeq]      = VARS::post("KYOUDAI_FAMILY_NAME".$setSeq);
            $this->field2["KYOUDAI_FIRST_NAME".$setSeq]       = VARS::post("KYOUDAI_FIRST_NAME".$setSeq);
            $this->field2["KYOUDAI_FAMILY_NAME_KANA".$setSeq] = VARS::post("KYOUDAI_FAMILY_NAME_KANA".$setSeq);
            $this->field2["KYOUDAI_FIRST_NAME_KANA".$setSeq]  = VARS::post("KYOUDAI_FIRST_NAME_KANA".$setSeq);
                                 
            $this->field2["HEIKYUU_SHOUGAKU_FLG1".$setSeq] = VARS::post("HEIKYUU_SHOUGAKU_FLG1".$setSeq);
            $this->field2["HEIKYUU_SHOUGAKU_FLG2".$setSeq] = VARS::post("HEIKYUU_SHOUGAKU_FLG2".$setSeq);
            $this->field2["KANRYOU_FLG".$setSeq]           = VARS::post("KANRYOU_FLG".$setSeq);
            $this->field2["CANCEL_FLG".$setSeq]            = VARS::post("CANCEL_FLG".$setSeq);
            $this->field2["KYUHU_KETTEI_DATE".$setSeq]     = VARS::post("KYUHU_KETTEI_DATE".$setSeq);
            $this->field2["KYUHU_KETTEI_FLG".$setSeq]      = VARS::post("KYUHU_KETTEI_FLG".$setSeq);
            $this->field2["KYUHU_REMARK".$setSeq]          = VARS::post("KYUHU_REMARK".$setSeq);
            $this->field2["KYUHU_YOTEI_GK".$setSeq]        = VARS::post("KYUHU_YOTEI_GK".$setSeq);
            
            $this->field2["KYUHU_UKE_NO".$setSeq]          = $this->field2["KYUHU_UKE_NO".$setSeq] ? sprintf("%04d", $this->field2["KYUHU_UKE_NO".$setSeq]) : "";
            $this->field2["KYUHU_UKE_EDABAN".$setSeq]      = $this->field2["KYUHU_UKE_EDABAN".$setSeq] ? sprintf("%03d", $this->field2["KYUHU_UKE_EDABAN".$setSeq]) : "";
        }
    }
    //入力項目チェック(Form1)
    function getChkForm() {
        //キー
        if (!strlen($this->field["SHINSEI_YEAR"])) {
            $this->setWarning("MSG301", "( 申請年度 )");
            return false;
        }
        if ((!strlen($this->field["UKE_YEAR"]) && !strlen($this->field["UKE_NO"]) && !strlen($this->field["UKE_EDABAN"]) && !strlen($this->field["SHINSEI_KANRYOU_FLG"])) ||
            ( strlen($this->field["UKE_YEAR"]) && !strlen($this->field["UKE_NO"]) && !strlen($this->field["UKE_EDABAN"]) && !strlen($this->field["SHINSEI_KANRYOU_FLG"])) ||
            ( strlen($this->field["UKE_YEAR"]) &&  strlen($this->field["UKE_NO"]) && !strlen($this->field["UKE_EDABAN"])) ||
            ( strlen($this->field["UKE_YEAR"]) &&  strlen($this->field["UKE_NO"]) &&  strlen($this->field["UKE_EDABAN"]))) {
        } else {
            $this->setWarning("MSG301", "( 受付番号 )");
            return false;
        }
        if (!strlen($this->field["SHINSEI_DATE"])) {
            $this->setWarning("MSG301", "( 申請日 )");
            return false;
        }
        //必須
        if (!strlen($this->field["FAMILY_NAME"]) || !strlen($this->field["FIRST_NAME"])) {
            $this->setWarning("MSG301", "( 氏名 )");
            return false;
        }
        if (!strlen($this->field["FAMILY_NAME_KANA"]) || !strlen($this->field["FIRST_NAME_KANA"])) {
            $this->setWarning("MSG301", "( フリガナ )");
            return false;
        }
        if (!strlen($this->field["H_SCHOOL_CD"])) {
            $this->setWarning("MSG301", "( 高校 )");
            return false;
        }
        if (!strlen($this->field["H_GRAD_YM"])) {
            $this->setWarning("MSG301", "( 卒業予定月 )");
            return false;
        }
        if (!strlen($this->field["YOYAKU_KIBOU_GK"])) {
            $this->setWarning("MSG301", "( 貸与希望額 )");
            return false;
        }
        if (!strlen($this->field["S_YOYAKU_KIBOU_YM"]) || !strlen($this->field["E_YOYAKU_KIBOU_YM"])) {
            $this->setWarning("MSG301", "( 貸与希望期間 )");
            return false;
        }
        if (!strlen($this->field["S_TAIYO_YM"]) || !strlen($this->field["E_TAIYO_YM"])) {
            $this->setWarning("MSG301", "( 本年度貸与期間 )");
            return false;
        }
        //申請年度と受付番号の年度チェック
        if (strlen($this->field["SHINSEI_YEAR"]) && strlen($this->field["UKE_YEAR"])) {
            if ($this->field["SHINSEI_YEAR"] != $this->field["UKE_YEAR"]) {
                $this->setWarning("MSG203", "修直の申請年度と受付番号の年度が一致していません。");
                return false;
            }
        }
        //貸与希望期間チェック
        if ($this->field["S_YOYAKU_KIBOU_YM"] > $this->field["E_YOYAKU_KIBOU_YM"]) {
            $this->setWarning("MSG916", "( 貸与希望期間 )");
            return false;
        }
        //本年度貸与期間のチェック
        list($syear, $smonth) = explode("-", $this->field["S_TAIYO_YM"]);
        list($eyear, $emonth) = explode("-", $this->field["E_TAIYO_YM"]);
        if ($smonth == "01" || $smonth == "02" || $smonth == "03") {
            $s_taiyo_nendo = $syear - 1;
        } else {
            $s_taiyo_nendo = $syear;
        }
        if ($emonth == "01" || $emonth == "02" || $emonth == "03") {
            $e_taiyo_nendo = $eyear - 1;
        } else {
            $e_taiyo_nendo = $eyear;
        }
        if ($s_taiyo_nendo != $this->field["SHINSEI_YEAR"] || $e_taiyo_nendo != $this->field["SHINSEI_YEAR"]) { 
            $this->setWarning("MSG916", "本年度貸与期間の年度と申請年度が一致していません。");
            return false;
        }
        if ($this->field["S_TAIYO_YM"] > $this->field["E_TAIYO_YM"]) {
            $this->setWarning("MSG916", "( 本年度貸与期間 )");
            return false;
        }
        //文字数
        if (60 < strlen($this->field["FAMILY_NAME"]) || 60 < strlen($this->field["FIRST_NAME"])) {
            $this->setWarning("MSG915", "( 氏名 )は60バイトまでです。");
            return false;
        }
        if (120 < strlen($this->field["FAMILY_NAME_KANA"]) || 120 < strlen($this->field["FIRST_NAME_KANA"])) {
            $this->setWarning("MSG915", "( フリガナ )は120バイトまでです。");
            return false;
        }
        if (150 < strlen($this->field["ADDR1"])) {
            $this->setWarning("MSG915", "( 住所1 )は150バイトまでです。");
            return false;
        }
        if (150 < strlen($this->field["ADDR2"])) {
            $this->setWarning("MSG915", "( 住所2 )は150バイトまでです。");
            return false;
        }
        if (120 < strlen($this->field["HEIKYUU_SHOUGAKU_REMARK2"])) {
            $this->setWarning("MSG915", "( 奨学金・併給状況 )( 受給中 )は120バイトまでです。");
            return false;
        }
        if (120 < strlen($this->field["HEIKYUU_SHOUGAKU_REMARK3"])) {
            $this->setWarning("MSG915", "( 奨学金・併給状況 )( 申請中 )は120バイトまでです。");
            return false;
        }
        if (2400 < strlen($this->field["REMARK"])) {
            $this->setWarning("MSG915", "( 備考 )は2400バイトまでです。");
            return false;
        }
        //支度
        if (!strlen($this->field["SHITAKU_CANCEL_CHOKU_FLG"])) {
            $stk = "STK_";
            //キー
            if (!strlen($this->field[$stk."SHINSEI_YEAR"])) {
                $this->setWarning("MSG301", "( 申請年度 )");
                return false;
            }
            if ((!strlen($this->field[$stk."UKE_YEAR"]) && !strlen($this->field[$stk."UKE_NO"]) && !strlen($this->field[$stk."UKE_EDABAN"]) && !strlen($this->field[$stk."SHINSEI_KANRYOU_FLG"])) ||
                ( strlen($this->field[$stk."UKE_YEAR"]) && !strlen($this->field[$stk."UKE_NO"]) && !strlen($this->field[$stk."UKE_EDABAN"]) && !strlen($this->field[$stk."SHINSEI_KANRYOU_FLG"])) ||
                ( strlen($this->field[$stk."UKE_YEAR"]) &&  strlen($this->field[$stk."UKE_NO"]) && !strlen($this->field[$stk."UKE_EDABAN"])) ||
                ( strlen($this->field[$stk."UKE_YEAR"]) &&  strlen($this->field[$stk."UKE_NO"]) &&  strlen($this->field[$stk."UKE_EDABAN"]))) {
            } else {
                $this->setWarning("MSG301", "( 受付番号 )");
                return false;
            }
            if (!strlen($this->field[$stk."SHINSEI_DATE"])) {
                $this->setWarning("MSG301", "( 申請日 )");
                return false;
            }
            //文字数
            if (120 < strlen($this->field[$stk."HEIKYUU_SHITAKU_REMARK2"])) {
                $this->setWarning("MSG915", "( 支度金・併給状況 )( 受給中 )は120バイトまでです。");
                return false;
            }
            if (120 < strlen($this->field[$stk."HEIKYUU_SHITAKU_REMARK3"])) {
                $this->setWarning("MSG915", "( 支度金・併給状況 )( 申請中 )は120バイトまでです。");
                return false;
            }
            //申請年度と受付番号の年度チェック
            if (strlen($this->field[$stk."SHINSEI_YEAR"]) && strlen($this->field[$stk."UKE_YEAR"])) {
                if ($this->field[$stk."SHINSEI_YEAR"] != $this->field[$stk."UKE_YEAR"]) {
                    $this->setWarning("MSG203", "支直の申請年度と受付番号の年度が一致していません。");
                    return false;
                }
            }
        }
        //学校コード登録チェック
        $db = Query::dbCheckOut();
        $query = knjta020_07Query::getSchoolInfo($this->field["H_SCHOOL_CD"], 'check');
        $schoolCheck = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if(!isset($schoolCheck["NAME"])) {
            $this->setWarning("MSG303", "学校データに存在する学校コード（高校）を入力してください。");
            return false;
        }
        $query = knjta020_07Query::getSchoolInfo($this->field["STK_H_SCHOOL_CD"], 'check');
        $schoolCheck2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if(!isset($schoolCheck2["NAME"])) {
            $this->setWarning("MSG303", "学校データに存在する学校コード（高校）を入力してください。");
            return false;
        }
        //特殊学校チェック
        if($schoolCheck["SCHOOL_DIV"] === '09') {
            $this->setWarning("MSG203", "指定の学校コードは".$schoolCheck["SCHOOL_DIV_NAME"]."のため、選択できません。");
            return false;
        }
        if($schoolCheck2["SCHOOL_DIV"] === '09') {
            $this->setWarning("MSG203", "指定の学校コードは".$schoolCheck2["SCHOOL_DIV_NAME"]."のため、選択できません。");
            return false;
        }
        //課程コード登録チェック
        if (strlen($this->field["KATEI"])) {
            $query = knjta020_07Query::getSchoolKateiCnt($this->field["H_SCHOOL_CD"], $this->field["KATEI"]);
            $kateiCnt = $db->getOne($query);
            if ($kateiCnt < 1) {
                $this->setWarning("MSG303", "( 課程 )が間違っています。");
                return false;
            }
        }
        if (strlen($this->field["STK_KATEI"])) {
            $query = knjta020_07Query::getSchoolKateiCnt($this->field["STK_H_SCHOOL_CD"], $this->field["STK_KATEI"]);
            $kateiCnt = $db->getOne($query);
            if ($kateiCnt < 1) {
                $this->setWarning("MSG303", "( 課程 )が間違っています。");
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }
    //入力項目チェック(subForm1)
    function validateInput() {
        return true;
    }
    //更新処理
    function &getUpdateModel() {
        if ($this->getChkSectioncd("TAIYO")) {
            if ($this->getChkForm()) {
                $db = Query::dbCheckOut();
                $query = knjta020_07Query::getMaxKojinNo();
                $this->maxKojinNo = $db->getOne($query);
                Query::dbCheckIn($db);

                knjta020_07Query::update($this);
                if (!isset($this->kojinNo)) {//新規登録ボタン
                    $this->kojinNo = $this->maxKojinNo;
                }
                if (strlen($this->sendShoriDiv)) {//新規
                    unset($this->sendShoriDiv);
                }
                $this->setMessage("MSG201");
                $this->shinseiYear = $this->field["SHINSEI_YEAR"];
            }
        }
    }
    //削除処理
    function &getDeleteModel() {
        if ($this->getChkSectioncd("TAIYO")) {
            if (!$this->field["KOJIN_NO"]) {
                $this->setWarning("MSG303");
            } else {
                $updData = array();
                $updData[] = array("", "SGK_", $this->field["SHIKIN_SHOUSAI_DIV"]);
                $updData[] = array("STK_", "STK_", $this->field["STK_SHIKIN_SHOUSAI_DIV"]);
                foreach ($updData as $key => $val) {
                    knjta020_07Query::delete($this, $val[0], $val[1], $val[2]);
                    $this->setMessage("MSG201");
                }
            }
        }
    }
    
    /***********/
    /* 給付 ****/
    /***********/
    //担当課チェック処理
    function getChkSectioncd($flg) {
        //権限チェック
        if ($this->auth != DEF_UPDATABLE) {
            $db = Query::dbCheckOut();

            for ($seq = 1; $seq <= $this->updKyuhuCnt; $seq++) {
                $setSeq = $seq == 1 ? "" : "_".$seq;
                //給付チェック
                if ($flg == "KYUHU") {
                    //学校登録チェック
                    $query = knjta020_07Query::getSchoolInfo($this->field2["KYUHU_H_SCHOOL_CD".$setSeq], 'check');
                    $schoolCheck2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    //高等課
                    if ($this->sectioncd === '0001') {
                        if ($schoolCheck2["RITSUCD"] === '3') {
                            $this->setWarning("MSG300", "担当課外の学校データでは更新することはできません。");
                            return false;
                        }
                    //文教課
                    } else if ($this->sectioncd === '0002') {
                        if ($schoolCheck2["RITSUCD"] !== '3' && $schoolCheck2["RITSUCD"] != "") {
                            $this->setWarning("MSG300", "担当課外の学校データでは更新することはできません。");
                            return false;
                        }
                    //未設定
                    } else {
                        $this->setWarning("MSG300", "担当課が設定されていません。");
                        return false;
                    }
                //貸与チェック
                } else {
                    //高等課
                    $sectionFlg = "true";
                    if ($this->sectioncd === '0001') {
                    //文教課
                    } else if ($this->sectioncd === '0002') {
                        $this->setWarning("MSG300", "担当課外の申請データは更新することはできません。");
                        return false;
                    //未設定
                    } else {
                        $this->setWarning("MSG300", "担当課が設定されていません。");
                        return false;
                    }
                }
            }
            Query::dbCheckIn($db);
        }
        return true;
    }

    //入力項目チェック(給付金)
    function getChkKyuhuForm() {
        $db = Query::dbCheckOut();
        //キー
        if (!strlen($this->field["KOJIN_NO"])) {
            $this->setWarning("MSG303", "新規で給付申請を行なって下さい。");
            return false;
        }
        for ($seq = 1; $seq <= $this->updKyuhuCnt; $seq++) {
            $setSeq = $seq == 1 ? "" : "_".$seq;
            if (2 < strlen($this->field2["KYUHU_GRADE".$setSeq])) {
                $this->setWarning("MSG915", "( 学年 {$seq}回目)は2バイトまでです。");
                return false;
            }
            if (3 < strlen($this->field2["KYUHU_HR_CLASS".$setSeq])) {
                $this->setWarning("MSG915", "( クラス {$seq}回目)は3バイトまでです。");
                return false;
            }
            if (3 < strlen($this->field2["KYUHU_ATTENDNO".$setSeq])) {
                $this->setWarning("MSG915", "( 出席番号 {$seq}回目)は3バイトまでです。");
                return false;
            }
            $shinseiYear = "KYUHU_SHINSEI_YEAR".$setSeq;
            $ukeYear = "KYUHU_UKE_YEAR".$setSeq;
            $ukeNo = "KYUHU_UKE_NO".$setSeq;
            $ukeEda = "KYUHU_UKE_EDABAN".$setSeq;
            $kanryou = "KANRYOU_FLG".$setSeq;
            $kHschool = "KYUHU_H_SCHOOL_CD".$setSeq;
            $ritsuCd = "KYUHU_RITSUCD".$setSeq;
            $katei = "KYUHU_KATEI".$setSeq;
            $kaisuu = "KYUHU_KAISUU".$setSeq;
            $wariDiv = "SHOTOKUWARI_DIV".$setSeq;

            if (!strlen($this->field2[$shinseiYear])) {
                $this->setWarning("MSG301", "( 申請年度 {$seq}回目)");
                return false;
            }
            if ((!strlen($this->field2[$ukeYear]) && !strlen($this->field2[$ukeNo]) && !strlen($this->field2[$ukeEda]) && !strlen($this->field2[$kanryou])) ||
                ( strlen($this->field2[$ukeYear]) && !strlen($this->field2[$ukeNo]) && !strlen($this->field2[$ukeEda]) && !strlen($this->field2[$kanryou])) ||
                ( strlen($this->field2[$ukeYear]) &&  strlen($this->field2[$ukeNo]) && !strlen($this->field2[$ukeEda])) ||
                ( strlen($this->field2[$ukeYear]) &&  strlen($this->field2[$ukeNo]) &&  strlen($this->field2[$ukeEda]))) {
            } else {
                $this->setWarning("MSG301", "( 受付番号 {$seq}回目)");
                return false;
            }
            if (!strlen($this->field2["KYUHU_SHINSEI_DATE".$setSeq])) {
                $this->setWarning("MSG301", "( 申請日 {$seq}回目)");
                return false;
            }
            if ($this->field2["KAKEI_KYUHEN_FLG".$setSeq] == '1' && !strlen($this->field2["KAKEI_KYUHEN_DATE".$setSeq])
            ) {
                $this->setWarning("MSG301", "( 家計急変日 {$seq}回目)");
                return false;
            }
            if (!$this->field2["KAKEI_KYUHEN_FLG".$setSeq] && strlen($this->field2["KAKEI_KYUHEN_DATE".$setSeq])
            ) {
                $this->setWarning("MSG301", "( 家計急変チェック {$seq}回目)");
                return false;
            }

            //申請年度と受付番号の年度チェック
            if (strlen($this->field2[$shinseiYear]) && strlen($this->field2[$ukeYear])) {
                if ($this->field2[$shinseiYear] != $this->field2[$ukeYear]) {
                    $this->setWarning("MSG203", "給付の申請年度と受付番号の年度が一致していません。{$seq}回目");
                    return false;
                }
            }
            //学校登録チェック
            $query = knjta020_07Query::getSchoolInfo($this->field2[$kHschool], 'check');
            $schoolCheck2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if(!isset($schoolCheck2["NAME"])) {
                $this->setWarning("MSG303", "学校データに存在する学校コード（高校）を入力してください。{$seq}回目");
                return false;
            }
            //公私区分（国公私立区分）
            if (!strlen($this->field2[$ritsuCd])) {
                $this->field2[$ritsuCd] = strlen($schoolCheck2["RITSUCD"]) ? $schoolCheck2["RITSUCD"] : "";
                if (!strlen($this->field2[$ritsuCd])) {
                    $this->setWarning("MSG203", "更新対象の高校の国公私立区分が取得できません。学校データの国公私立区分を確認してください。{$seq}回目");
                    return false;
                }
            }
            //課程コード登録チェック
            if (strlen($this->field2[$katei])) {
                $query = knjta020_07Query::getSchoolKateiCnt($this->field2[$kHschool], $this->field2[$katei]);
                $kateiCnt = $db->getOne($query);
                if ($kateiCnt < 1) {
                    $this->setWarning("MSG303", "( 課程 {$seq}回目)が間違っています。");
                    return false;
                }
            }
            //給付回数NULLチェック
            if ($this->field2[$kaisuu] == "") {
                $this->setWarning("MSG203", "給付回数が選択されていません。{$seq}回目");
                return false;
            }
            //給付回数チェック
            if ($this->field2[$kaisuu] > $this->kyuhuMax) {
                $this->setWarning("MSG203", "給付回数制限を超えています。{$seq}回目");
                return false;
            }
            //給付回数重複チェック
            $query = knjta020_07Query::getKyuhuKaisuRow($this, $seq);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if ($this->field2[$kaisuu] == $row["KYUHU_KAISUU"]) {
                    $this->setWarning("MSG203", "他の申請年度の給付回数と重複しています。{$seq}回目");
                    return false;
                }
            }
            $result->free();
            //所得割区分チェック
            if ($this->field2[$wariDiv] == "") {
                $this->setWarning("MSG301", "所得割区分を選択して下さい。{$seq}回目");
                return false;
            }
            //兄弟姉妹の年齢、続柄チェック
            if ($this->field2[$wariDiv] === '3') {
                if ($this->field2["KYOUDAI_BIRTHDAY".$setSeq] == "") {
                    $this->setWarning("MSG301", "兄弟姉妹の生年月日を入力して下さい。{$seq}回目");
                    return false;
                }
                if ($this->field2["KYOUDAI_TSUZUKIGARA_CD".$setSeq] == "") {
                    $this->setWarning("MSG301", "兄弟姉妹の続柄を選択して下さい。{$seq}回目");
                    return false;
                }
                if (60 < strlen($this->field2["KYOUDAI_FAMILY_NAME".$setSeq]) || 60 < strlen($this->field2["KYOUDAI_FIRST_NAME".$setSeq])) {
                    $this->setWarning("MSG915", "( 兄弟姉妹の氏名 {$seq}回目)は60バイトまでです。");
                    return false;
                }
                if (120 < strlen($this->field2["KYOUDAI_FAMILY_NAME_KANA".$setSeq]) || 120 < strlen($this->field2["KYOUDAI_FIRST_NAME_KANA".$setSeq])) {
                    $this->setWarning("MSG915", "( 兄弟姉妹のフリガナ {$seq}回目)は120バイトまでです。");
                    return false;
                }
            }
            //保護者(申請者)、保護者2住所のチェック
            $hogoCd = "HOGOSHA_CD".$setSeq;
            $hogo2Cd = "HOGOSHA2_CD".$setSeq;
            if (strlen($this->field2[$hogoCd]) || strlen($this->field2[$hogo2Cd])) {
                $prefflg = false;
                if (strlen($this->field2[$hogoCd])) {
                    $setName = 'の';
                    $query = knjta020_07Query::getHogoshaPref($this->field2[$hogo2Cd]);
                    $prefName = $db->getOne($query);
                    if ($prefName === '京都府') {
                        $prefflg = true;
                    }
                }
                if (strlen($this->field2[$hogo2Cd])) {
                    $setName = '、保護者いずれも';
                    $query = knjta020_07Query::getHogoshaPref($this->field2[$hogo2Cd]);
                    $prefName = $db->getOne($query);
                    if ($prefName === '京都府') {
                        $prefflg = true;
                    }
                }
                if (!$prefflg) {
                    $this->setWarning("MSG203", "保護者(申請者 {$seq}回目)".$setName."住所が京都府内ではありません。");
                    return false;
                }
            }
            //保護者(申請者)のチェック(入力完了時)
            if (strlen($this->field2[$kanryou])) {
                if ($this->field2[$hogoCd] == "") {
                    $this->setWarning("MSG203", "保護者(申請者 {$seq}回目)が設定されていません。\\n\\n※保護者(申請者)ボタンが押せない場合、一度も更新していないことが原因です。その場合は入力完了のチェックをはずし、一度更新して下さい。");
                    return false;
                }
            }
            //給付金予定額チェック
            $kYoteiGk = "KYUHU_YOTEI_GK".$setSeq;
            if ($this->field2[$kYoteiGk] == "") {
                $this->setWarning("MSG301", "( 奨学給付金 {$seq}回目)");
                return false;
            }
            if (strlen($this->field2[$kYoteiGk]) > 6) {
                $this->setWarning("MSG203", "奨学給付金は6バイトまでです。{$seq}回目");
                return false;
            }
        }

        Query::dbCheckIn($db);
        return true;
    }
    
    //２回目処理するかどうか
    function setKyuhuKaisuu() {
        $this->updKyuhuCnt = 2;
        if ($this->field2["KYUHU_SHINSEI_YEAR_2"] == "" &&
            $this->field2["KYUHU_UKE_YEAR"] == "" &&
            $this->field2["KYUHU_UKE_NO_2"] == "" &&
            $this->field2["KYUHU_UKE_EDABAN_2"] == "" &&
            $this->field2["KYUHU_SHINSEI_DATE_2"] == "" &&
            $this->field2["KYUHU_H_SCHOOL_CD_2"] == "" &&
            $this->field2["KYUHU_KATEI_2"] == "" &&
            $this->field2["KYUHU_KAISUU_2"] == ""
        ) {
            $this->updKyuhuCnt = 1;
        }
    }

    //更新処理(給付金)
    function &getUpdateKyuhuModel() {
        $this->setKyuhuKaisuu();
        if ($this->getChkSectioncd("KYUHU")) {
            if ($this->getChkKyuhuForm()) {
                knjta020_07Query::updateKyuhu($this);
                if (strlen($this->sendKyuhuDiv)) {//新規
                    unset($this->sendKyuhuDiv);
                }
                $this->setMessage("MSG201");
            }
        }
    }
    //削除処理(給付金)
    function &getDeleteKyuhuModel() {
        $this->setKyuhuKaisuu();
        if ($this->getChkSectioncd("KYUHU")) {
            if (!$this->field["KOJIN_NO"]) {
                $this->setWarning("MSG303");
            } else {
                knjta020_07Query::deleteKyuhuMain($this);
                $this->setMessage("MSG201");
            }
        }
    }
    
}
?>
