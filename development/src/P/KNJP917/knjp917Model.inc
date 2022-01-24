<?php

require_once('for_php7.php');

class knjp917Model extends Model {
    var $cmd;
    var $updLMcd = array();
    var $updLMScd = array();
    var $updLMName = array();

    var $search = array();
    var $control = array();        //コントロールのデータを保持


    function knjp917Model() {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control);
    }
 
    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->sendList);
            unset($this->schregno);
            unset($this->schoolKind);
            unset($this->year);
        }

        $this->cmd = VARS::request("cmd");

        $this->schoolCd = sprintf("%012d", SCHOOLCD);

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno     = VARS::get("SCHREGNO");
            $this->schoolKind   = VARS::get("SCHOOL_KIND");
            $this->year         = CTRL_YEAR;
        }

        if ($this->cmd == "search") {
            $this->search = array ( "HR_CLASS_HYOUJI_FLG"   => VARS::post("HR_CLASS_HYOUJI_FLG"),
                                    "GRADE"                 => VARS::post("GRADE"),
                                    "HR_CLASS"              => VARS::post("HR_CLASS"),
                                    "ENT_YEAR"              => VARS::post("ENT_YEAR"),
                                    "GRD_YEAR"              => VARS::post("GRD_YEAR"),
                                    "SCHREGNO"              => VARS::post("SCHREGNO"),
                                    "NAME"                  => VARS::post("NAME"),
                                    "NAME_KANA"             => VARS::post("NAME_KANA")
                                    );
        }

        if ($this->cmd == "") {
            unset($this->search);
            $this->search["HR_CLASS_HYOUJI_FLG"] = "1";
        }

        //左画面から生徒リストを取得(印刷用)
        if ($this->cmd == "sendList") {
            $this->sendList = VARS::get("sendList");
            $this->sortFlg  = VARS::get("sortFlg");
            unset($this->schregno);
            unset($this->schoolKind);
            $this->year  = CTRL_YEAR;
        }

        //フィールド
        $this->field = array("MAX_BENE"             => VARS::post("MAX_BENE"),          //給付上限
                             "OUTGO_DATE"           => VARS::post("OUTGO_DATE"),        //返金日付
                             "KYUFU_TOTAL"          => VARS::post("KYUFU_TOTAL"),       //給付合計金額
                             "HID_KYUFU_TOTAL"      => VARS::post("HID_KYUFU_TOTAL"),   //給付合計金額
                             "HID_KYUFU_SAGAKU"     => VARS::post("HID_KYUFU_SAGAKU"),  //給付差額
                             "PRINT_DATE"           => VARS::post("PRINT_DATE")         //出力日
                            );

        //画面リサイズ用
        $this->windowHeight = VARS::post("windowHeight");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //更新用データセット
        foreach ($this->updLMScd as $lmsCd => $outGoMoney) {
            $this->field["CHECK_BOX:".$lmsCd] = VARS::post("CHECK_BOX:".$lmsCd);
            $this->field["KYUFU:".$lmsCd]     = VARS::post("KYUFU:".$lmsCd);
        }
        //データ保持用（警告後セット用）
        foreach ($this->updLMcd as $lmCd => $outGoMoney) {
            $this->field["HID_KYUFU_LM_TOTAL:".$lmCd]   = VARS::post("HID_KYUFU_LM_TOTAL:".$lmCd);
        }
    }

    //データチェック
    function dataCheck() {
        //空文字チェック
        if(strlen($this->field["OUTGO_DATE"]) == 0){
            $this->setWarning("MSG301","(返金日付)");
            return false;
        }

        //金額チェック、細目毎の上限を超えていないか
        foreach ($this->updLMScd as $lmsCd => $outGoMoney) {
            if ($outGoMoney < $this->field["KYUFU:".$lmsCd]) {
                list($mName, $sName) = explode("----", $this->updLMName[$lmsCd]);

                $this->setWarning("MSG901", "{$mName}\\n{$sName}");
                return false;
            }
        }

        //給付上限チェック
        if ($this->field["MAX_BENE"] < $this->field["KYUFU_TOTAL"]) {
            $this->setWarning("MSG203", "給付金上限額を超えています。");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if (!$this->dataCheck()) {
            return;
        }

        //給付伝票を全て削除
        knjp917Query::getDeleteQuery($this);

        knjp917Query::getUpdateQuery($this);

        $this->setMessage("MSG201");
        return;
    }
}
?>
