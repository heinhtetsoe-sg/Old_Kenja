<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp391kModel extends Model {
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $edate;          //終了日付 NO001
    var $selectleft;
    var $hrclass;

    /**
     * メンバ変数の初期化
     */
    function knjp391kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATE1"    =>  VARS::post("DATE1"),    //支払日
                             "DATE2"    =>  VARS::post("DATE2"),    //登録日
                             "OUTPUT"   =>  VARS::post("OUTPUT"),   //処理ラジオ
                             "MONEY"    =>  VARS::post("MONEY"),    //金額 NO001
                             "DBNAME"   =>  VARS::post("DBNAME")    //DB変更に備えて
                     );

        $this->selectleft = VARS::post("selectleft");
        $this->hrclass = VARS::post("HR_CLASS");

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function getInsertModel()
    {
        $leftArray = explode(",", $this->selectleft);
        $sep = "";
        $selectleft = "(";
        foreach ($leftArray as $key => $val) {
            $valArray = explode(":", $val);
            $selectleft .= $sep."'".$valArray[3]."'";
            $sep = ",";
        }
        $selectleft .= $sep ? ")" : "'')";

        $db = Query::dbCheckOut();
        if ($this->field["OUTPUT"] == "1") {
            if ($this->control["学期開始日付"][9] > $this->field["DATE1"] ||
                $this->field["DATE1"] > $this->control["学期終了日付"][9]) {
                $this->setWarning("MSG916", "日付が年度外です。");
                return false;
            }
            $query = knjp391kQuery::deletePai_M11_Query();
            $db->query($query);
            $query = knjp391kQuery::deletePai_M21_Query();
            $db->query($query);
            $query = knjp391kQuery::deletePai_M31_Query();
            $db->query($query);
            $query = knjp391kQuery::deleteRePai_S_Query();
            $db->query($query);
            $query = knjp391kQuery::deletePai_S_Query();
            $db->query($query);
            $query = knjp391kQuery::insertPai_M11_Query($this, $selectleft);
            $db->query($query);
            $query = knjp391kQuery::insertPai_M21_Query($this, $selectleft);
            $db->query($query);
            $query = knjp391kQuery::insertPai_S_Query($this, $selectleft);
            $db->query($query);
            $query = knjp391kQuery::insertPai_M31_Query($this, $selectleft);
            $db->query($query);
        } else {
            $query = knjp391kQuery::delete_Grant_Query();
            $db->query($query);
            $query = knjp391kQuery::insert_Grant_Query($this);
            $db->query($query);
        }
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
