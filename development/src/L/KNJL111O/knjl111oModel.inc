<?php

require_once('for_php7.php');


class knjl111oModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $field = array();   //フィールドデータ

    function knjl111oModel()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;

        //フィールド
        $this->field = array("APPLICANTDIV"     => VARS::post("APPLICANTDIV"),              //入試制度
                             "TESTDIV"          => VARS::post("TESTDIV"),                   //入試区分
                             "EXAM_TYPE"        => VARS::post("EXAM_TYPE"),                 //受験型
                             "EXAMNO"           => VARS::post("EXAMNO"),                    //受験番号
                             "RECEPTNO"         => VARS::post("RECEPTNO")                   //受付番号
                            );

        $db = Query::dbCheckOut();

        $query = knjl111oQuery::GetName($this->year, "L009");
        $result = $db->query($query);
        $this->kamoku = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->meisaiField["SCORE".$row["VALUE"]]  = VARS::post("SCORE".$row["VALUE"]);
            $this->kamoku[$row["VALUE"]] = $row["VALUE"];
        }
        $result->free();
        $this->meisaiField["SCOREA"]  = VARS::post("SCOREA");
        $this->kamoku["A"] = "A";

        Query::dbCheckIn($db);
    }


    //入力項目チェック
    function validateInput($db)
    {

        $query = knjl111oQuery::getRecept($this);
        $setInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($setInfo["RECEPTNO"] != $this->field["RECEPTNO"]) {
            $this->setWarning("検索を実行してから再度処理して下さい。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($this->validateInput($db)) {
            knjl111oQuery::getDeleteQuery($db, $this);
            knjl111oQuery::getUpdateQuery($db, $this);
            $this->cmd = "main";
            $this->setMessage("MSG201");
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    } 

    //更新処理
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($this->validateInput($db)) {
            knjl111oQuery::getDeleteQuery($db, $this);
            $this->cmd = "main";
            $this->setMessage("MSG201");
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>