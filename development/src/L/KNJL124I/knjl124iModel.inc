<?php

class knjl124iModel extends model
{
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $data;

    function knjl124iModel()
    {
    }

    function init()
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
    function validateInput() {
        //必須チェック
        if ($this->field["APPLICANTDIV"] == "") {
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        if ($this->field["TESTDIV"] == "") {
            $this->setWarning("MSG301","( 入試区分 )");
            return false;
        }
        if ($this->field["BORDER_SCORE"] == "") {
            $this->setWarning("MSG301","( 合格点 )");
            return false;
        }

        $db = Query::dbCheckOut();
        //満点のチェック
        $query = knjl124iQuery::getPerfectScore($this);
        $perfectScore = $db->getOne($query);
        if (isset($temp)) {
            if ($this->field["BORDER_SCORE"] > $perfectScore) {
                $this->setWarning("MSG901","( 合格点 )");
                return false;
            }
        }

        //内定区分取得
        $this->naiteiDivArr = array();
        $query = knjl124iQuery::getEntexamSettingMst($this, "L025");

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->naiteiDivArr[$row["VALUE"]] = $row["VALUE"];
        }
        Query::dbCheckIn($db);

        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            //更新処理
            knjl124iQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
