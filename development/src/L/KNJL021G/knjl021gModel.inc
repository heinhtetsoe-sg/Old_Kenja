<?php

require_once('for_php7.php');

class knjl021gModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $field = array();   //フィールドデータ

    function knjl021gModel()
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

        //フィールド
        $this->field = array("CONFIDENTIAL_RPT01"   =>  VARS::post("CONFIDENTIAL_RPT01"),       //国語
                             "CONFIDENTIAL_RPT02"   =>  VARS::post("CONFIDENTIAL_RPT02"),       //社会
                             "CONFIDENTIAL_RPT03"   =>  VARS::post("CONFIDENTIAL_RPT03"),       //数学
                             "CONFIDENTIAL_RPT04"   =>  VARS::post("CONFIDENTIAL_RPT04"),       //理科
                             "CONFIDENTIAL_RPT05"   =>  VARS::post("CONFIDENTIAL_RPT05"),       //音楽
                             "CONFIDENTIAL_RPT06"   =>  VARS::post("CONFIDENTIAL_RPT06"),       //美術
                             "CONFIDENTIAL_RPT07"   =>  VARS::post("CONFIDENTIAL_RPT07"),       //保険
                             "CONFIDENTIAL_RPT08"   =>  VARS::post("CONFIDENTIAL_RPT08"),       //技術
                             "CONFIDENTIAL_RPT09"   =>  VARS::post("CONFIDENTIAL_RPT09"),       //外国
                             "CONFIDENTIAL_RPT10"   =>  VARS::post("CONFIDENTIAL_RPT10"),       //未使用
                             "TOTAL_ALL"            =>  VARS::post("TOTAL_ALL"),                //合計
                             "AVERAGE_ALL"          =>  VARS::post("AVERAGE_ALL"),              //平均
                             //欠席の記録
                             "ABSENCE_DAYS"         =>  VARS::post("ABSENCE_DAYS"),             //欠席日数(1年)
                             "ABSENCE_DAYS2"        =>  VARS::post("ABSENCE_DAYS2"),            //欠席日数(2年)
                             "ABSENCE_DAYS3"        =>  VARS::post("ABSENCE_DAYS3"),            //欠席日数(3年)
                             "ABSENCE_REMARK"       =>  VARS::post("ABSENCE_REMARK"),           //欠席理由(1年)
                             "ABSENCE_REMARK2"      =>  VARS::post("ABSENCE_REMARK2"),          //欠席理由(2年)
                             "ABSENCE_REMARK3"      =>  VARS::post("ABSENCE_REMARK3"),          //欠席理由(3年)
                             //実力テスト・模擬試験
                             "JITURYOKU_KOKUGO"     =>  VARS::post("JITURYOKU_KOKUGO"),         //実力・国語
                             "JITURYOKU_EIGO"       =>  VARS::post("JITURYOKU_EIGO"),           //実力・英語
                             "JITURYOKU_SUUGAKU"    =>  VARS::post("JITURYOKU_SUUGAKU"),        //実力・数学
                             "JITURYOKU_TOTAL"      =>  VARS::post("JITURYOKU_TOTAL"),          //実力・合計
                             "MOGI_HENSATI"         =>  VARS::post("MOGI_HENSATI"),             //模擬・偏差値
                            );
    }

    //入力項目チェック
    function validateInput()
    {
        if (strlen($this->field["ABSENCE_REMARK"]) > 30 || strlen($this->field["ABSENCE_REMARK2"]) > 30 || strlen($this->field["ABSENCE_REMARK3"]) > 30) {
            $this->setWarning("MSG915","欠席理由は30バイトまでです。");
            return false;
        }
        //文字数チェック（整数部：2桁、小数部：1桁まで）
        if ($this->field["AVERAGE_ALL"] != "" && is_numeric($this->field["AVERAGE_ALL"])) {
            list ($int, $decimal) = explode('.', $this->field["AVERAGE_ALL"]);
            if (strlen($int) > 2) {
                $this->setWarning("MSG915","内申平均の整数部は2バイトまでです。");
                return false;
            } else if (strlen($decimal) > 1) {
                $this->setWarning("MSG915","内申平均の小数部は1バイトまでです。");
                return false;
            }
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl021gQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

}
?>