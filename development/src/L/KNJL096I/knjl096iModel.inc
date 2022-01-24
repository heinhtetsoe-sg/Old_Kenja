<?php

class knjl096iModel extends model
{
    public $field = array();
    public $cmd;
    public $ObjYear;
    public $data;

    public function knjl096iModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "EXAM_TYPE"        =>  "1",                            //受験型
                             "SEX"              =>  VARS::post("SEX"),              //判定対象
                             "BORDER_SCORE"     =>  VARS::post("BORDER_SCORE")      //合格点
                             );

        if (VARS::get("APPLICANTDIV") && $this->cmd = "edit") {
            $this->field = array("APPLICANTDIV"     =>  VARS::get("APPLICANTDIV"),     //入試制度
                                 "TESTDIV"          =>  VARS::get("TESTDIV"),          //入試区分
                                 "EXAM_TYPE"        =>  VARS::get("EXAM_TYPE"),        //受験型
                                 "SEX"              =>  VARS::get("SEX"),              //判定対象
                                 "BORDER_SCORE"     =>  VARS::get("BORDER_SCORE")      //合格点
                                 );
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if ($this->field["APPLICANTDIV"] == "") {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if ($this->field["TESTDIV"] == "") {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if ($this->field["BORDER_SCORE"] == "") {
            $this->setWarning("MSG301", "( 合格点 )");
            return false;
        }

        //満点のチェック
        $db = Query::dbCheckOut();
        $query = knjl096iQuery::getPerfectScore($this);
        $perfectScore = $db->getOne($query);
        Query::dbCheckIn($db);
        if (isset($temp)) {
            if ($this->field["BORDER_SCORE"] > $perfectScore) {
                $this->setWarning("MSG901", "( 合格点 )");
                return false;
            }
        }

        return true;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            //更新処理
            knjl096iQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
