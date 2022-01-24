<?php

require_once('for_php7.php');


class knjl090tModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受検番号
    var $judgement;         //判定区分
    var $procedurediv;      //手続区分
    var $entdiv;            //入学区分
    var $checkexam;
    var $field = array();           //フィールドデータ
    var $cflg;                      //変更フラグ（同じ受検番号を編集中チェック）

    function knjl090tModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examno);
            unset($this->judgement);
            unset($this->procedurediv);
            unset($this->entdiv);
            unset($this->checkexam);
            unset($this->cflg);
        }

        $this->cmd              = VARS::request("cmd");
        $this->year             = CTRL_YEAR + 1;

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
                             "SUC_COURSECD"         => "1",                                      //合格過程コード
                             "SUC_MAJORCD"          => VARS::post("SUC_MAJORCD"),                //合格学科コード
                             "SUC_COURSECODE"       => "0000",                                   //合格コースコード
                             "JUDGEMENT"            => VARS::post("JUDGEMENT"),                  //合否判定
                             "PROCEDUREDIV"         => VARS::post("PROCEDUREDIV"),               //手続区分
                             "ENTDIV"               => VARS::post("ENTDIV"),                     //入学区分
                             "SUCCESS_NOTICENO"     => VARS::post("SUCCESS_NOTICENO"),           //合格通知No.
                             "FAILURE_NOTICENO"     => VARS::post("FAILURE_NOTICENO"),           //不合格通知No.
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
        if (strlen($this->field["SUCCESS_NOTICENO"]) && strlen($this->field["FAILURE_NOTICENO"])) {
            $this->setWarning("MSG901","( 通知発行No. )\\n合格・不合格の両方は入力できません。");
            return false;
        }
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }        
        //存在チェック
        $row = knjl090tQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","受検番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->field["SUCCESS_NOTICENO"]) > 4) {
            $this->setWarning("MSG915","合格通知No.は4バイトまでです。");
            return false;
        }
        if (strlen($this->field["FAILURE_NOTICENO"]) > 4) {
            $this->setWarning("MSG915","不合格通知No.は4バイトまでです。");
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
            $result = knjl090tQuery::getUpdateQuery($this);
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