<?php

require_once('for_php7.php');

class knjl090wModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $applicantdiv;      //入試制度
    var $examno;            //受検番号
    var $judgement;         //判定区分
    var $procedurediv;      //手続区分
    var $entdiv;            //入学区分
    var $checkexam;
    var $field = array();           //フィールドデータ
    var $cflg;                      //変更フラグ（同じ受検番号を編集中チェック）

    function knjl090wModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->examno);
            unset($this->judgement);
            unset($this->procedurediv);
            unset($this->entdiv);
            unset($this->checkexam);
            unset($this->cflg);
        }

        $this->cmd              = VARS::request("cmd");
        $this->year             = CTRL_YEAR + 1;
        $this->applicantdiv     = VARS::post("APPLICANTDIV");

        if (strlen(VARS::post("EXAMNO"))) {
            if ($this->examno != VARS::post("EXAMNO")) {
                $this->cflg = "false";
                unset($this->judgememt);
            } elseif ($this->cmd == "change" || $this->cmd == "change_testdiv2") { //編集中フラグ
                $this->cflg = "true";
            } else {
                $this->cflg = "false";
            }
            $this->examno = VARS::post("EXAMNO");
            $this->examno = sprintf("%05d",$this->examno);
        } else {
            unset($this->judgememt);
        }

        $this->procedurediv     = VARS::post("PROCEDUREDIV");
        $this->entdiv           = VARS::post("ENTDIV");

        //フィールド
        $this->field = array("APPLICANTDIV"         => VARS::post("APPLICANTDIV"),               //入試制度
                             "COURSEMAJOR"          => VARS::post("COURSEMAJOR"),                //志望学科
                             "SUC_COURSECD"         => substr(VARS::post("COURSEMAJOR"), 0, 1),  //合格過程コード
                             "SUC_MAJORCD"          => substr(VARS::post("COURSEMAJOR"), 1, 3),  //合格学科コード
                             "SUC_COURSECODE"       => substr(VARS::post("COURSEMAJOR"), 4, 4),  //合格コースコード
                             "JUDGEMENT"            => VARS::post("JUDGEMENT"),                  //合否判定
                             "YOUSIKI3_REMARK"      => VARS::post("YOUSIKI3_REMARK"),            //別紙様式３の備考
                             "YOUSIKI5_TENKYOBI"    => VARS::post("YOUSIKI5_TENKYOBI"),          //別紙様式５の転居の年月日
                             "YOUSIKI5_TENKYOTI"    => VARS::post("YOUSIKI5_TENKYOTI"),          //別紙様式５の転居地
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),               //手続区分
                             "ENTDIV"               => VARS::post("ENTDIV"),                     //入学区分
                             "INTERVIEW_ATTEND_FLG" => VARS::post("INTERVIEW_ATTEND_FLG"),       //面接出欠フラグ
                             "REMARK1"              => VARS::post("REMARK1"),                    //備考１
                             "REMARK2"              => VARS::post("REMARK2")                     //備考２
                            );
    }


    //入力項目チェック
    function validateInput()
    {
        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受検番号 )");
            return false;
        }
        if ($this->field["JUDGEMENT"] == 1) {
            if ((!strlen($this->field["SUC_MAJORCD"]))) {
                $this->setWarning("MSG301", "( 学科 )");
                return false;
            }
        }
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }        
        //存在チェック
        $row = knjl090wQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","受検番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->field["YOUSIKI3_REMARK"]) > 60) {
            $this->setWarning("MSG915","別紙様式３の備考は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["YOUSIKI5_TENKYOTI"]) > 90) {
            $this->setWarning("MSG915","別紙様式５の転居地は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 60) {
            $this->setWarning("MSG915","備考１は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 60) {
            $this->setWarning("MSG915","備考２は60バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl090wQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        } else {
            $this->cmd = "main";
        }
        return;
    }
}
?>