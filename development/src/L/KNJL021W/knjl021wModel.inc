<?php

require_once('for_php7.php');

class knjl021wModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受検番号
    public $applicantdiv;      //入試制度
    public $field = array();   //フィールドデータ

    public function knjl021wModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;

        $this->field["SEX"] = VARS::post("SEX"); //性別

        //教科(1,2,3年)
        $this->kyouka_count = 10;
        for ($i = 1; $i <= $this->kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $this->field["CONF1_RPT{$num}"] = VARS::post("CONF1_RPT{$num}");
            $this->field["CONF2_RPT{$num}"] = VARS::post("CONF2_RPT{$num}");
            $this->field["CONF3_RPT{$num}"] = VARS::post("CONF3_RPT{$num}");
            $this->field["KANTEN1_RPT{$num}"] = VARS::post("KANTEN1_RPT{$num}");
            $this->field["KANTEN2_RPT{$num}"] = VARS::post("KANTEN2_RPT{$num}");
            $this->field["KANTEN3_RPT{$num}"] = VARS::post("KANTEN3_RPT{$num}");
            $this->field["KANTEN4_RPT{$num}"] = VARS::post("KANTEN4_RPT{$num}");
            $this->field["KANTEN5_RPT{$num}"] = VARS::post("KANTEN5_RPT{$num}");
        }
        //出欠状況(1,2,3年)
        for ($i = 1; $i <= 3; $i++) {
            $this->field["ATTEND_KESSEKI{$i}"] = VARS::post("ATTEND_KESSEKI{$i}");
            $this->field["ATTEND_RIYUU{$i}"] = VARS::post("ATTEND_RIYUU{$i}");
        }
        //２．総合的な学習の時間の記録（学習活動、観点、評価）
        for ($i = 1; $i <= 3; $i++) {
            $this->field["SHOKEN_SOUGAKU{$i}"] = VARS::post("SHOKEN_SOUGAKU{$i}");
        }
        //３．特別活動の記録
        for ($i = 1; $i <= 3; $i++) {
            $this->field["SHOKEN_TOKUBETU{$i}"] = VARS::post("SHOKEN_TOKUBETU{$i}");
        }
        //３．行動の記録
        for ($i = 1; $i <= 10; $i++) {
            $this->field["SHOKEN_KOUDOU{$i}"] = VARS::post("SHOKEN_KOUDOU{$i}");
        }
        //健康状況
        $this->field["SHOKEN_KENKOU"] = VARS::post("SHOKEN_KENKOU");
        //備考、５．その他参考となる諸事項
        $this->field["REMARK1"] = VARS::post("REMARK1");
        $this->field["REMARK2"] = VARS::post("REMARK2");
    }

    //入力項目チェック
    public function validateInput()
    {
        if (!($this->field["SEX"])) {
            $this->setWarning("MSG301", "( 性別 )");
            return false;
        }

        if (strlen($this->field["ATTEND_RIYUU1"]) > 90 || strlen($this->field["ATTEND_RIYUU2"]) > 90 || strlen($this->field["ATTEND_RIYUU3"]) > 90) {
            $this->setWarning("MSG915", "欠席理由は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["SHOKEN_SOUGAKU1"]) > 750 || strlen($this->field["SHOKEN_SOUGAKU2"]) > 750 || strlen($this->field["SHOKEN_SOUGAKU3"]) > 750) {
            $this->setWarning("MSG915", "総合的な学習の時間の記録は750バイトまでです。");
            return false;
        }
        if (strlen($this->field["SHOKEN_KENKOU"]) > 150) {
            $this->setWarning("MSG915", "健康状況は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 240) {
            $this->setWarning("MSG915", "備考は240バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 1680) {
            $this->setWarning("MSG915", "その他参考となる諸事項は1680バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl021wQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }
}
