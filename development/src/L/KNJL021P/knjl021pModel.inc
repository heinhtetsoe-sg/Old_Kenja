<?php

require_once('for_php7.php');

class knjl021pModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $field = array();   //フィールドデータ

    function knjl021pModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;

        //教科(1,2,3年)
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $this->field["CONF1_RPT{$num}"] = VARS::post("CONF1_RPT{$num}");
            $this->field["CONF2_RPT{$num}"] = VARS::post("CONF2_RPT{$num}");
            $this->field["CONF3_RPT{$num}"] = VARS::post("CONF3_RPT{$num}");
        }
        //クラス人員・順位
        $this->field["CLASS_JININ"] = VARS::post("CLASS_JININ");
        $this->field["JUNI"] = VARS::post("JUNI");
        //出欠状況(1,2,3年)
        for ($i = 1; $i <= 3; $i++) {
            $this->field["ATTEND_SHUSSEKI_SUBEKI{$i}"] = VARS::post("ATTEND_SHUSSEKI_SUBEKI{$i}");
            $this->field["ATTEND_KESSEKI{$i}"] = VARS::post("ATTEND_KESSEKI{$i}");
            $this->field["ATTEND_TIKOKU{$i}"] = VARS::post("ATTEND_TIKOKU{$i}");
            $this->field["ATTEND_SOUTAI{$i}"] = VARS::post("ATTEND_SOUTAI{$i}");
            $this->field["ATTEND_RIYUU{$i}"] = VARS::post("ATTEND_RIYUU{$i}");
        }
        //特別活動の記録(1,2,3年)・総合的な学習の時間の記録(1,2,3年)
        for ($i = 1; $i <= 3; $i++) {
            $this->field["SHOKEN_TOKUBETU{$i}"] = VARS::post("SHOKEN_TOKUBETU{$i}");
            $this->field["SHOKEN_SOUGAKU{$i}"] = VARS::post("SHOKEN_SOUGAKU{$i}");
        }
        //健康状況・総合所見・その他の活動の記録
        $this->field["SHOKEN_KENKOU"] = VARS::post("SHOKEN_KENKOU");
        $this->field["SHOKEN_SOUGOU"] = VARS::post("SHOKEN_SOUGOU");
        $this->field["SHOKEN_SONOTA"] = VARS::post("SHOKEN_SONOTA");
        //備考
        $this->field["REMARK1"] = VARS::post("REMARK1");
    }

    //入力項目チェック
    function validateInput()
    {
        if (strlen($this->field["ATTEND_RIYUU1"]) > 90 || strlen($this->field["ATTEND_RIYUU2"]) > 90 || strlen($this->field["ATTEND_RIYUU3"]) > 90) {
            $this->setWarning("MSG915","欠席理由は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["SHOKEN_TOKUBETU1"]) > 150 || strlen($this->field["SHOKEN_TOKUBETU2"]) > 150 || strlen($this->field["SHOKEN_TOKUBETU3"]) > 150) {
            $this->setWarning("MSG915","特別活動の記録は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["SHOKEN_SOUGAKU1"]) > 150 || strlen($this->field["SHOKEN_SOUGAKU2"]) > 150 || strlen($this->field["SHOKEN_SOUGAKU3"]) > 150) {
            $this->setWarning("MSG915","総合的な学習の時間の記録は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["SHOKEN_KENKOU"]) > 150) {
            $this->setWarning("MSG915","健康状況は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["SHOKEN_SOUGOU"]) > 150) {
            $this->setWarning("MSG915","総合所見は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["SHOKEN_SONOTA"]) > 150) {
            $this->setWarning("MSG915","その他の活動の記録は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 240) {
            $this->setWarning("MSG915","備考は240バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl021pQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

}
?>