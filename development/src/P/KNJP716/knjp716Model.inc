<?php

require_once('for_php7.php');

class knjp716Model extends Model {

    var $cmd;
    var $exp_grpcd;
    var $selectExp_grpcd;
    var $name;
    var $field = array();
    var $year;
    var $grade;
    var $grade2;
    
    function knjp716Model()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->exp_grpcd);
            unset($this->selectExp_grpcd);
            unset($this->name);
            unset($this->grade);
            unset($this->grade2);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::post("GRADE")) {
            $this->grade = VARS::post("GRADE");
        }
        if (VARS::get("COLLECT_GRP_CD")) {
            $this->exp_grpcd = sprintf("%04d",VARS::get("COLLECT_GRP_CD"));
            $this->selectExp_grpcd = sprintf("%04d",VARS::get("COLLECT_GRP_CD"));
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        if (VARS::request("GRADE2")) {
            $this->grade2 = VARS::request("GRADE2");
        }
        $this->field = array("GRADE"            => VARS::post("GRADE2"),
                             "CLASS"            => VARS::post("selectdata"));
        $this->taisyouYear = $this->grade2 != "00" && strlen($this->grade2) > 0 ? CTRL_YEAR : (CTRL_YEAR + 1);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if(strlen($this->field["GRADE"]) == 0){
            $this->setWarning("MSG301","(学年)");
            return false;
        }
        if(!$this->exp_grpcd){
            $this->setWarning("MSG301","会計グループを選択して下さい。");
            return false;
        }

        //追加時の重複チェック
        $db = Query::dbCheckOut();

        //入金済みデータの件数
        $paid_money  = $db->getOne(knjp716Query::getPaidMoney($this->taisyouYear, "COLLECT_MONEY_PAID_M_DAT", $this->field["COLLECT_GRP_CD"]));
        $paid_money += $db->getOne(knjp716Query::getPaidMoney($this->taisyouYear, "COLLECT_MONEY_PAID_S_DAT", $this->field["COLLECT_GRP_CD"]));
        if ($paid_money > 0) {
            $this->setWarning("入金済みデータが存在します。");
            return false;
        }
        Query::dbCheckIn($db);

        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput($this->cmd)){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $query = knjp716Query::getMcdScd($this);
            $result = $db->query($query);
            $this->field["COLLECT_M_CD"] = "";
            $this->field["COLLECT_S_CD"] = "";
            $sepM = "";
            $sepS = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->field["COLLECT_M_CD"] .= $sepM.$row["COLLECT_L_CD"].$row["COLLECT_M_CD"];
                $sepM = ",";
                if ($row["COLLECT_S_CD"] != "00") {
                    $this->field["COLLECT_S_CD"] .= $sepS.$row["COLLECT_L_CD"].$row["COLLECT_M_CD"].$row["COLLECT_S_CD"];
                    $sepS = ",";
                }
            }
            $result->free();
            knjp716Query::getDeleteQuery($this, $db);
            knjp716Query::getUpdateQuery($this, $db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput($this->cmd)){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            knjp716Query::getDeleteQuery($this, $db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->exp_grpcd = "";
            $this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
        return;
    }

}
?>
