<?php

require_once('for_php7.php');

class knjp739Model extends Model {
    var $cmd;
    var $schFlg;
    var $arr_schregData = array();

    function knjp739Model() {
    }
 
    function init() {
        $this->cmd = VARS::request("cmd");

        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");

        if ($this->cmd == "search") {
            $this->search = array ( "SEARCH_DIV"            => VARS::get("LEFT_SEARCH_DIV"),
                                    "HR_CLASS_HYOUJI_FLG"   => VARS::get("LEFT_HR_CLASS_HYOUJI_FLG"),
                                    "GRADE"                 => VARS::get("LEFT_GRADE"),
                                    "HR_CLASS"              => VARS::get("LEFT_HR_CLASS"),
                                    "ENT_YEAR"              => VARS::get("LEFT_ENT_YEAR"),
                                    "GRD_YEAR"              => VARS::get("LEFT_GRD_YEAR"),
                                    "SCHREGNO"              => VARS::get("LEFT_SCHREGNO"),
                                    "NAME"                  => VARS::get("LEFT_NAME"),
                                    "NAME_KANA"             => VARS::get("LEFT_NAME_KANA"),
                                    "A028"                  => VARS::get("LEFT_A028"),
                                    "PAID_UMU"              => VARS::get("LEFT_PAID_UMU")
                                    );
        }

        if ($this->cmd == "") {
            unset($this->search);
        }

        //フィールド
        $this->field = array("PAID_FLG"             => VARS::post("PAID_FLG"),
                             "PAID_LIMIT_MONTH"     => VARS::post("PAID_LIMIT_MONTH"),
                             "SORT_DIV"             => VARS::post("SORT_DIV"),
                             "PAID_DATE"            => VARS::post("PAID_DATE"),
                             "PLAN_PAID_MONEY_DIV"  => VARS::post("PLAN_PAID_MONEY_DIV")    //入金方法
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種取得
        $db = Query::dbCheckOut();
        $this->schFlg = "";
        $schCnt = 0;
        $query = knjp739Query::getSchoolKind($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($schCnt != 0) $this->schFlg = "1";
            $this->schoolKind = $row["SCHOOL_KIND"];
            $schCnt++;
        }
        Query::dbCheckIn($db);

        if ($this->cmd == "update") {
            foreach ($this->arr_schregData as $key => $val) {
                $this->field["PAID_FLG-".$val] = VARS::post("PAID_FLG-".$val);
            }
        }
    }

    //データチェック
    function dataCheck() {
        //空文字チェック
        if(strlen($this->field["PAID_DATE"]) == 0){
            $this->setWarning("MSG301","(入金日)");
            return false;
        }
        if(strlen($this->field["PLAN_PAID_MONEY_DIV"]) == 0){
            $this->setWarning("MSG301","(入金方法)");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if (!$this->dataCheck()) {
            return;
        }
        knjp739Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>
