<?php

require_once('for_php7.php');
class knjp100kModel extends Model {
    var $field = array();
    var $cmd;
    var $control_data;
    var $year;
    var $appli_cd;
    var $appli_name;
    var $selectdata;
    var $selectDiv;

    function knjp100kModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->appli_cd);
            unset($this->appli_name);
            unset($this->hrclass);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        
        if(VARS::get("SCHREGNO") != ""){
            $this->schregno = VARS::get("SCHREGNO");
        }

        if(VARS::get("init") != ""){
            unset($this->schregno);
        }

        if(VARS::request("APPLICATIONCD") != ""){
            $this->appli_cd = VARS::request("APPLICATIONCD");
        }

        if(VARS::get("APPLI_NAME") != ""){
            $this->appli_name = VARS::get("APPLI_NAME");
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;

        $this->field = array("APPLICATIONCD"    => VARS::post("APPLICATIONCD"),     //申込コード
                             "APPLI_MONEY_DUE"  => VARS::post("APPLI_MONEY_DUE"),   //納入必要金額
                             "APPLIED_DATE"     => VARS::post("APPLIED_DATE"),      //申込日
                             "APPLI_PAID_MONEY" => VARS::post("APPLI_PAID_MONEY"),  //入金額
                             "APPLI_PAID_DIV"   => VARS::post("APPLI_PAID_DIV"),    //入金区分
                             "APPLI_PAID_DATE"  => VARS::post("APPLI_PAID_DATE"));  //納期日

        if (VARS::get("ALL_EDIT")) {
            $this->field = array("APPLICATIONCD"    => VARS::get("APPLICATIONCD2"),
                                 "APPLI_MONEY_DUE"  => VARS::get("APPLI_MONEY_DUE2"),
                                 "APPLIED_DATE"     => VARS::get("APPLIED_DATE2"),
                                 "APPLI_PAID_MONEY" => VARS::get("APPLI_PAID_MONEY2"),
                                 "APPLI_PAID_DIV"   => VARS::get("APPLI_PAID_DIV2"),
                                 "APPLI_PAID_DATE"  => VARS::get("APPLI_PAID_DATE2"));
        }

        if ($this->cmd == "all_edit") {
            $this->select_opt = array();
        }

        if (VARS::post("HRCLASS") != "") {
            $this->hrclass = VARS::post("HRCLASS");
        }
        if(VARS::get("GRADE") && VARS::get("HR_CLASS")) {
           $this->hrclass = VARS::get("GRADE") ."-".VARS::get("HR_CLASS");
        }
        if ($this->cmd == "selectChange") {
            $this->selectdata = array();
        } else {
            $this->selectdata = VARS::post("selectdata");
        }
        $this->selectDiv = VARS::post("SELECT_DIV");
    }

    function validateInput()
    {
        if ($this->cmd != "all_add" && $this->cmd != "all_update" && $this->cmd != "all_delete") {
            if ($this->schregno == "") {
                $this->warning = "左の生徒一覧から生徒を選択してください";
                return false;
            }
        }
        if  (($this->cmd == "update" || $this->cmd == "delete") && $this->appli_cd != $this->field["APPLICATIONCD"]) {
            $this->setWarning("MSG308");
            return false;
        }
        if (strlen($this->field["APPLICATIONCD"]) == 0) {
            $this->setWarning("MSG301","(申込コード)");
            return false;
        }
#2005/06/22
#        if ($this->field["APPLI_MONEY_DUE"] < $this->field["APPLI_PAID_MONEY"]) {
#            $this->setWarning("MSG913");
#            return false;
#        }
        if ($this->cmd != "delete" && $this->cmd != "all_delete") {
            if (strlen($this->field["APPLI_MONEY_DUE"]) == 0) {
                $this->setWarning("MSG301","(納入必要金額)");
                return false;
            }
        }

        //入力項目の１レコード取得
        $row = knjp100kQuery::getRow2($this);
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    function &getInsertModel()
    {
        if ($this->validateInput()){
            $db = Query::dbCheckOut();

            $query = knjp100kQuery::getInsertQuery($this->field, $this->schregno);
            $db->query($query);

            $this->appli_cd = $this->field["APPLICATIONCD"];
            $this->appli_name = $db->getOne(knjp100kQuery::getAppliName($this->appli_cd));
            
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $db = Query::dbCheckOut();

            $query = knjp100kQuery::getUpdateQuery($this->field, $this->schregno);
            $db->query($query);

            $this->appli_cd = $this->field["APPLICATIONCD"];
            $this->appli_name = $db->getOne(knjp100kQuery::getAppliName($this->appli_cd));
            
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel()
    {
        if ($this->validateInput()){
            $db = Query::dbCheckOut();

            $query = knjp100kQuery::getDeleteQuery($this->field, $this->schregno);
            $db->query($query);
            Query::dbCheckIn($db);

            $this->appli_cd = "";
            $this->appli_name = "";

            $this->setMessage("MSG201");
        }
        return;
    }

    //一括更新時の更新データ取得
    function getSelectData($db) {
        if ($this->selectDiv == "1") {
            return $this->selectdata;
        } else {
            $retData = "";
            $query = knjp100kQuery::getExeData($this);
            $result = $db->query($query);
            $sep = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $retData .= $sep.$row["SCHREGNO"];
                $sep = ",";
            }
            $result->free();
            return $retData;
        }
    }

    //一括追加
    function &getAllAddModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $exeData = $this->getSelectData($db);
            $data = explode(",", $exeData);

            foreach ($data as $schregno)
            {
                //データが存在しないデータに対してのみ追加
                $query = knjp100kQuery::CheckExistData($this->field["APPLICATIONCD"], $schregno);

                if (!is_array($db->getRow($query))) {
                    $query = knjp100kQuery::getInsertQuery($this->field, $schregno);
                    $db->query($query);
                }
            }
            $db->commit();
            $this->setMessage("MSG201");
            Query::dbCheckIn($db);
        }   
    }

    //一括更新
    function &getAllUpdateModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $exeData = $this->getSelectData($db);
            $data = explode(",", $exeData);

            foreach ($data as $schregno)
            {
                //データが存在するデータに対してのみ更新
                $query = knjp100kQuery::CheckExistData($this->field["APPLICATIONCD"], $schregno);

                if (is_array($db->getRow($query))) {
                    $query = knjp100kQuery::getUpdateQuery($this->field, $schregno);
                    $db->query($query);
                }
            }
            $db->commit();
            $this->setMessage("MSG201");
            Query::dbCheckIn($db);
        }
    }

    //一括削除
    function &getAllDeleteModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $exeData = $this->getSelectData($db);
            $data = explode(",", $exeData);

            foreach ($data as $schregno)
            {
                //データが存在するデータに対してのみ削除
                $query = knjp100kQuery::CheckExistData($this->field["APPLICATIONCD"], $schregno);

                if (is_array($db->getRow($query))) {
                    $query = knjp100kQuery::getDeleteQuery($this->field, $schregno);
                    $db->query($query);
                }
            }
            $db->commit();
            $this->setMessage("MSG201");
            $this->appli_cd = "";
            $this->appli_name = "";

            Query::dbCheckIn($db);
        }
    }
}
?>
