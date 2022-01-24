<?php

require_once('for_php7.php');

class knjl021eModel extends Model {
    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();   //フィールドデータ

    function knjl021eModel() {}

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->checkexam);
        }

        $this->cmd      = VARS::request("cmd");
        $this->year     = CTRL_YEAR + 1;
        $this->examno   = VARS::post("EXAMNO");
        $this->applicantdiv = VARS::post("APPLICANTDIV");

        if (in_array($this->cmd, array('reference', 'back', 'next', 'back1', 'next1'))) {
            $this->examno = VARS::post("EXAMNO");
        }

        //フィールド
        $this->field = array("TESTDIV"              =>  VARS::post("TESTDIV"),                  //入試区分
                             "CONFIDENTIAL_RPT01"   =>  VARS::post("CONFIDENTIAL_RPT01"),       //国語
                             "CONFIDENTIAL_RPT02"   =>  VARS::post("CONFIDENTIAL_RPT02"),       //社会
                             "CONFIDENTIAL_RPT03"   =>  VARS::post("CONFIDENTIAL_RPT03"),       //数学
                             "CONFIDENTIAL_RPT04"   =>  VARS::post("CONFIDENTIAL_RPT04"),       //理科
                             "CONFIDENTIAL_RPT05"   =>  VARS::post("CONFIDENTIAL_RPT05"),       //音楽
                             "CONFIDENTIAL_RPT06"   =>  VARS::post("CONFIDENTIAL_RPT06"),       //美術
                             "CONFIDENTIAL_RPT07"   =>  VARS::post("CONFIDENTIAL_RPT07"),       //保体
                             "CONFIDENTIAL_RPT08"   =>  VARS::post("CONFIDENTIAL_RPT08"),       //技術・家庭
                             "CONFIDENTIAL_RPT09"   =>  VARS::post("CONFIDENTIAL_RPT09"),       //外国語
                             "TOTAL5"               =>  VARS::post("TOTAL5"),                   //５科合計(3年)
                             "TOTAL_ALL"            =>  VARS::post("TOTAL_ALL"),                //９科合計(3年)
                             "ABSENCE_DAYS"         =>  VARS::post("ABSENCE_DAYS"),             //欠席数(1年)
                             "ABSENCE_DAYS2"        =>  VARS::post("ABSENCE_DAYS2"),            //欠席数(2年)
                             "ABSENCE_DAYS3"        =>  VARS::post("ABSENCE_DAYS3"),            //欠席数(3年)
                             "DE003REMARK1"         =>  VARS::post("DE003REMARK1"),             //行動の記録
                             "REMARK1"              =>  VARS::post("REMARK1"),                  //備考
                             "REMARK2"              =>  VARS::post("REMARK2")                   //要注意
                            );
        //教科(1,2年)
        for ($i = 1; $i <= 11; $i++) {
            $num = sprintf("%02d", $i);
            $this->field["CONF1_RPT{$num}"] = VARS::post("CONF1_RPT{$num}");
            $this->field["CONF2_RPT{$num}"] = VARS::post("CONF2_RPT{$num}");
        }
    }

    //入力項目チェック
    function validateInput() {
        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //行数チェック
        if ($this->validate_row_cnt($this->field["REMARK1"], (20 * 2)) > 2) {
            $this->setWarning("MSG915","(備考は2行までです)");
            $this->checked = array();
            return false;
        }
        //桁数チェック
        if (strlen($this->field["DE003REMARK1"]) > 2) {
            $this->setWarning("MSG915","( 行動の記録は2バイトまでです )");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjl021eQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }
}
?>
