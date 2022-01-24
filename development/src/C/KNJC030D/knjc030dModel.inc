<?php

require_once('for_php7.php');

class knjc030dModel extends Model {

    var $cmd;
    var $field = array();
    var $reset = array();
    var $auth;
    var $attendItem = array();

    function knjc030dModel()
    {
        $this->field = array();
    }

    function init()
    {
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd      = VARS::request("cmd");
        $this->field    = $_POST;
        $this->reset    = $_POST;

        if ($this->cmd == "update" || $this->cmd == "updateHrAte" || $this->cmd == "cancelHrAte") {
            $this->field["DATE"]            = VARS::post("HIDDEN_DATE");
            $this->field["HR_CLASS_TYPE"]   = VARS::post("HIDDEN_HR_CLASS_TYPE");
            $this->field["GRADE_HR_CLASS"]  = VARS::post("HIDDEN_GRADE_HR_CLASS");
        }

        //データベース接続
        $db = Query::dbCheckOut();

        //校種
        $this->schoolkind = "";
        if ($this->Properties["useSchool_KindField"] == "1" && $this->field["GRADE_HR_CLASS"]) {
            if ($this->field["HR_CLASS_TYPE"] == "2" && $this->Properties["useFi_Hrclass"] != "1") {
                $this->schoolkind = "";
            } else if ($this->Properties["use_prg_schoolkind"] != "1") {
                $this->schoolkind = SCHOOLKIND;
            } else {
                $this->schoolkind = $db->getOne(knjc030dQuery::getSchoolKind($this));
            }
        }

        //出欠入力対象の項目
        $setFieldName = $attendItem = array();
        $setFieldName[] =  array("6",   "NONOTICE", "");
        $setFieldName[] =  array("5",   "NOTICE",   "");
        $setFieldName[] =  array("2",   "SUSPEND",  "出停");
        $setFieldName[] =  array("3",   "MOURNING", "忌引");
        $setFieldName[] =  array("1",   "ABSENT",   "公欠");
        $setFieldName[] =  array("4",   "SICK",     "");
        $setFieldName[] =  array("15",  "LATE",     "遅刻");
        $setFieldName[] =  array("16",  "EARLY",    "早退");

        $tmpItem = array();
        $c001Namecd1 = ($this->schoolkind) ? "C".$this->schoolkind."01" : "C001";
        $result = $db->query(knjc030dQuery::getNameMst2($c001Namecd1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //欠席の詳細出欠項目（大分類）
            $nonotice = false;
            if ($row["NAMECD2"] == "6") {
                $result006 = $db->query(knjc030dQuery::getNameMst2('C006', $row["NAMECD2"]));
                while ($row006 = $result006->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $attendItem["CNT".$row006["NAMECD2"]] = array($row006["NAMECD2"], $row006["NAME1"]);
                    $nonotice = true;
                }
            }

            if($nonotice && $row["NAMECD2"] == "6") {
            } else {
                foreach ($setFieldName as $order => $val) {
                    list ($cd, $field, $label) = $val;
                    if ($cd == $row["NAMECD2"]) {
                        $name = (strlen($label)) ? $label : $row["NAME1"];
                        $tmpItem[$order][$field] = array($cd, $name);
                    }
                }
            }
        }

        ksort($tmpItem);

        foreach ($tmpItem as $order => $array) {
            foreach ($array as $field => $val) {
                $attendItem[$field] = $val;
            }
        }
        $this->attendItem = $attendItem;

        //DB切断
        Query::dbCheckIn($db);
    }

    /* 入力項目チェック */
    function validateInput()
    {
        //必須チェック
        if(!isset($this->field["SCHREGNO"])){
            $this->setMessage("MSG304");
            return false;
        }
        if(!isset($this->field["DATE"])){
            $this->setMessage("MSG304");
            return false;
        }

        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update()
    {
        $result = knjc030dQuery::getUpdateQuery($this);
        return $result;
    }

    /* 更新処理 */
    function &getUpdateHrAteModel()
    {
        if ($this->validateInput()){
            if($this->updateHrAte()){
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* HRATEを更新 */
    function &updateHrAte()
    {
        $result = knjc030dQuery::getHrateUpdateQuery($this);
        return $result;
    }
}
?>
