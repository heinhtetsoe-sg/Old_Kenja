<?php

require_once('for_php7.php');

class knjl022bModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $field = array();   //フィールドデータ

    function knjl022bModel()
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
        $this->field = array(//実力テスト
                             "DETAIL_REMARK3"       =>  VARS::post("DETAIL_REMARK3"),           //実力テスト1
                             "DETAIL_REMARK4"       =>  VARS::post("DETAIL_REMARK4"),           //実力テスト2
                             //確約区分
                             "KAKUYAKU_SET"         =>  VARS::post("KAKUYAKU_SET"),             //確約区分セットデータ
                             //推薦
                             "DETAIL_REMARK2"       =>  VARS::post("DETAIL_REMARK2")            //推薦備考
                            );
    }

    //入力項目チェック
    function validateInput()
    {
        if (strlen($this->field["DETAIL_REMARK2"]) > 90) {
            $this->setWarning("MSG915","推薦備考は90バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl022bQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

}
?>