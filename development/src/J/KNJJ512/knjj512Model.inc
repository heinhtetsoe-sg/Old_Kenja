<?php
class knjj512Model extends Model
{
    public $year;
    public $field = array();
    public $cmd;
    public $itemcd;
    public $age;
    public $maxLebel;
    public $totalMark;

    public function knjj512Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->age);
        }
        $this->year = CTRL_YEAR;
        $this->field = array("AGE"                  => VARS::post("AGE"),                   //年齢
                             "TOTAL_SCORE_LOW_A"    => VARS::post("TOTAL_SCORE_LOW_A"),     //A 下限
                             "TOTAL_SCORE_HIGH_A"   => VARS::post("TOTAL_SCORE_HIGH_A"),    //A 上限
                             "TOTAL_SCORE_LOW_B"    => VARS::post("TOTAL_SCORE_LOW_B"),     //B 下限
                             "TOTAL_SCORE_HIGH_B"   => VARS::post("TOTAL_SCORE_HIGH_B"),    //B 上限
                             "TOTAL_SCORE_LOW_C"    => VARS::post("TOTAL_SCORE_LOW_C"),     //C 下限
                             "TOTAL_SCORE_HIGH_C"   => VARS::post("TOTAL_SCORE_HIGH_C"),    //C 上限
                             "TOTAL_SCORE_LOW_D"    => VARS::post("TOTAL_SCORE_LOW_D"),     //D 下限
                             "TOTAL_SCORE_HIGH_D"   => VARS::post("TOTAL_SCORE_HIGH_D"),    //D 上限
                             "TOTAL_SCORE_LOW_E"    => VARS::post("TOTAL_SCORE_LOW_E"),     //E 下限
                             "TOTAL_SCORE_HIGH_E"   => VARS::post("TOTAL_SCORE_HIGH_E")     //E 上限
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("AGE")) {
            $this->age = VARS::get("AGE");
        }


        $this->maxLebel  = 5;
        $this->totalMark = array(5 => "A",
                                 4 => "B",
                                 3 => "C",
                                 2 => "D",
                                 1 => "E"
                                 );
    }

    //入力項目チェック
    public function validateInput()
    {

        //必須チェック
        if (trim($this->field["AGE"]) == "") {
            $this->setWarning("MSG301", "(年齢)");
            return false;
        }

        // //キーの更新チェック
        // if (($this->cmd == "update" || $this->cmd == "delete") && ($this->age != $this->field["AGE"])) {
        //     $this->setWarning("MSG308");
        //     return false;
        // }
        // if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->field["AGE"])) {
        //     $this->setWarning("MSG308");
        //     return false;
        // }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjj512Query::getSportsTotalValueBaseMst($this);
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
        foreach ($this->totalMark as $lebel => $mark) {
            $low  = "TOTAL_SCORE_LOW_".$mark;
            $high = "TOTAL_SCORE_HIGH_".$mark;
            //必須チェック
            if (!strlen($this->field[$low]) || !strlen($this->field[$high])) {
                $this->setWarning("MSG301", "（".$mark."）");
                return false;
            }
            if ($mark != "A" && $mark != "E") {
                //大小チェック
                if (trim($this->field[$low]) > trim($this->field[$high])) {
                    $this->setWarning("MSG901", "（".$mark."の大小が不正です）"); //大小関係
                    return false;
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
            knjj512Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    public function &getInsertModel()
    {
        if ($this->validateInput()) {
            knjj512Query::getInsertQuery($this);
            $this->itemcd = $this->field["ITEMCD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knjj512Query::getDeleteQuery($this);
            unset($this->itemcd);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
