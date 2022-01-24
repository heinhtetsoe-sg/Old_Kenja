<?php
class knjj511Model extends Model
{
    public $year;
    public $field = array();
    public $cmd;
    public $itemcd;
    public $sex;
    public $maxLebel;
    public $totalLevel;

    public function knjj511Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->itemcd);
            unset($this->sex);
        }
        $this->year = CTRL_YEAR;
        $this->field = array("ITEMCD"          => VARS::post("ITEMCD"),           //種別
                             "SEX"             => VARS::post("SEX"),              //性別
                             "RECORD_LOW_10"   => VARS::post("RECORD_LOW_10"),    //10 下限
                             "RECORD_HIGH_10"  => VARS::post("RECORD_HIGH_10"),   //10 上限
                             "RECORD_LOW_9"    => VARS::post("RECORD_LOW_9"),     //9 下限
                             "RECORD_HIGH_9"   => VARS::post("RECORD_HIGH_9"),    //9 上限
                             "RECORD_LOW_8"    => VARS::post("RECORD_LOW_8"),     //8 下限
                             "RECORD_HIGH_8"   => VARS::post("RECORD_HIGH_8"),    //8 上限
                             "RECORD_LOW_7"    => VARS::post("RECORD_LOW_7"),     //7 下限
                             "RECORD_HIGH_7"   => VARS::post("RECORD_HIGH_7"),    //7 上限
                             "RECORD_LOW_6"    => VARS::post("RECORD_LOW_6"),     //6 下限
                             "RECORD_HIGH_6"   => VARS::post("RECORD_HIGH_6"),    //6 上限
                             "RECORD_LOW_5"    => VARS::post("RECORD_LOW_5"),     //5 下限
                             "RECORD_HIGH_5"   => VARS::post("RECORD_HIGH_5"),    //5 上限
                             "RECORD_LOW_4"    => VARS::post("RECORD_LOW_4"),     //4 下限
                             "RECORD_HIGH_4"   => VARS::post("RECORD_HIGH_4"),    //4 上限
                             "RECORD_LOW_3"    => VARS::post("RECORD_LOW_3"),     //3 下限
                             "RECORD_HIGH_3"   => VARS::post("RECORD_HIGH_3"),    //3 上限
                             "RECORD_LOW_2"    => VARS::post("RECORD_LOW_2"),     //2 下限
                             "RECORD_HIGH_2"   => VARS::post("RECORD_HIGH_2"),    //2 上限
                             "RECORD_LOW_1"    => VARS::post("RECORD_LOW_1"),     //1 下限
                             "RECORD_HIGH_1"   => VARS::post("RECORD_HIGH_1"),    //1 上限
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("ITEMCD")) {
            $this->itemcd = VARS::get("ITEMCD");
        }
        if (VARS::get("SEX")) {
            $this->sex = VARS::get("SEX");
        }

        $this->maxLebel  = 10;
        $this->totalLevel = array(10 => "10",
                                  9 => "9",
                                  8 => "8",
                                  7 => "7",
                                  6 => "6",
                                  5 => "5",
                                  4 => "4",
                                  3 => "3",
                                  2 => "2",
                                  1 => "1"
                                 );
    }

    //入力項目チェック
    public function validateInput()
    {

        //必須チェック
        if (trim($this->field["ITEMCD"]) == "") {
            $this->setWarning("MSG301", "(種別)");
            return false;
        }
        if (trim($this->field["SEX"]) == "") {
            $this->setWarning("MSG301", "(性別)");
            return false;
        }
        
        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjj511Query::getSportsItemScoreBaseMst($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //更新対象がなければエラー
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //追加時 既にレコードがあればエラー
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }

        //上限・下限
        if ($this->cmd == "update" || $this->cmd == "add") {
            foreach ($this->totalLevel as $lebel => $val) {
                $low  = "RECORD_LOW_".$val;
                $high = "RECORD_HIGH_".$val;
                //必須チェック
                if (!strlen($this->field[$low]) || !strlen($this->field[$high])) {
                    $this->setWarning("MSG301", "（".$val."）");
                    return false;
                }
                if ($val!= "10" && $val!= "1") {
                    //大小チェック
                    if (trim($this->field[$low]) > trim($this->field[$high])) {
                        $this->setWarning("MSG901", "（".$val."の大小が不正です）"); //大小関係
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjj511Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    public function &getInsertModel()
    {
        if ($this->validateInput()) {
            knjj511Query::getInsertQuery($this);
            $this->itemcd = $this->field["ITEMCD"];
            $this->sex = $this->field["SEX"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knjj511Query::getDeleteQuery($this);
            unset($this->itemcd);
            unset($this->sex);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
