<?php

require_once('for_php7.php');

class knjl326aModel extends Model
{
    public $cmd;
    public $examyear;
    public $applicantdiv;
    public $testdiv;
    public $noticeType;

    public function knjl326aModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->examyear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->noticeType);
        }

        $this->cmd              = VARS::request("cmd");
        $this->examyear         = CTRL_YEAR + 1;
        $this->applicantdiv     = VARS::post("APPLICANTDIV");
        $this->testdiv          = VARS::post("TESTDIV");
        $this->noticeType       = VARS::post("NOTICE_TYPE");
        $this->honordiv         = VARS::post("HONORDIV");
        $this->noticeDate       = VARS::post("NOTICE_DATE");
        $this->sLimitDate       = VARS::post("S_LIMIT_DATE");
        $this->hLimitDate       = VARS::post("H_LIMIT_DATE");
        $this->receptnoFrom     = VARS::post("RECEPTNO_FROM");
        $this->receptnoTo       = VARS::post("RECEPTNO_TO");
        $this->shdiv            = VARS::post("SHDIV");
        $this->wish_course      = VARS::post("WISH_COURSE");
        $this->passCourse       = VARS::post("PASS_COURSE");

        //初期値セット
        if (VARS::get("PROGRAMID") || $this->cmd == "change") {
            $this->noticeDate   = CTRL_DATE;
            $this->sLimitDate   = CTRL_DATE;
            $this->hLimitDate   = CTRL_DATE;
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if (!strlen($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 受験校種 )");
            return false;
        }
        if (!strlen($this->testdiv)) {
            $this->setWarning("MSG301", "( 試験区分 )");
            return false;
        }
        if (!strlen($this->noticeType)) {
            $this->setWarning("MSG301", "( 通知種別 )");
            return false;
        }
        if (!strlen($this->shdiv) || $this->shdiv == "ALL") {
            $this->setWarning("MSG301", "( 専併区分 )");
            return false;
        }
        //併願の場合は、受験番号を入力（範囲の開始の方だけ）
        if ($this->shdiv == "2" && !strlen($this->receptnoFrom)) {
            $this->setWarning("MSG301", "( 受験番号（開始） )");
            return false;
        }
        if ($this->shdiv == "2" && !strlen($this->passCourse)) {
            $this->setWarning("MSG301", "( 入学コース選択 )");
            return false;
        }

        if ($this->shdiv == "2") {
            $db = Query::dbCheckOut();
            //入学コースチェック（念のため）
            $examnoArray = array();
            $result = $db->query(knjl326aQuery::getPassCourseList($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $examnoArray[] = $row;
            }
            $result->free();
            Query::dbCheckIn($db);
            if (get_count($examnoArray) == 0) {
                $this->setWarning("MSG901", "( 入学コース選択 )");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjl326aQuery::getUpdateQuery($this);
//            $this->setMessage("MSG201");
        }
    }
}
