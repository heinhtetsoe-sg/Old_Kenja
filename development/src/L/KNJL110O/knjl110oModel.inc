<?php

require_once('for_php7.php');


class knjl110oModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $field = array();   //フィールドデータ

    function knjl110oModel()
    {
    }

    function init()
    {
        $this->cmd              = VARS::request("cmd");
        $this->year             = CTRL_YEAR + 1;
        $this->lineCntArr       = array('1' => 'L',
                                        '2' => 'M',
                                        '3' => 'N');

        //フィールド
        $this->field = array("APPLICANTDIV"     => VARS::post("APPLICANTDIV"),              //入試制度
                             "TESTDIV"          => VARS::post("TESTDIV"),                   //入試区分
                             "EXAM_TYPE"        => VARS::post("EXAM_TYPE")                  //受験型
                            );

        $db = Query::dbCheckOut();

        $query = knjl110oQuery::GetName($this->year, "L009");
        $result = $db->query($query);
        $this->kamoku = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->meisaiField["M_AVG".$row["VALUE"]]  = VARS::post("M_AVG".$row["VALUE"]);
            $this->meisaiField["W_AVG".$row["VALUE"]]  = VARS::post("W_AVG".$row["VALUE"]);
            $this->meisaiField["T_AVG".$row["VALUE"]]  = VARS::post("T_AVG".$row["VALUE"]);
            $this->meisaiField["MAX".$row["VALUE"]]    = VARS::post("MAX".$row["VALUE"]);
            $this->meisaiField["MIN".$row["VALUE"]]    = VARS::post("MIN".$row["VALUE"]);
            $this->kamoku[$row["VALUE"]] = $row["VALUE"];
        }
        $this->meisaiField["M_AVGA"]  = VARS::post("M_AVGA");
        $this->meisaiField["W_AVGA"]  = VARS::post("W_AVGA");
        $this->meisaiField["T_AVGA"]  = VARS::post("T_AVGA");
        $this->meisaiField["MAXA"]    = VARS::post("MAXA");
        $this->meisaiField["MINA"]    = VARS::post("MINA");
        $this->kamoku["A"] = "A";

        $this->meisaiField["MINS"]    = VARS::post("MINS");
        $this->kamoku["S"] = "S";
        $this->meisaiField["MINT"]    = VARS::post("MINT");
        $this->kamoku["T"] = "T";
        foreach ($this->lineCntArr as $val) {
            $this->meisaiField["MIN{$val}"]   = VARS::post("MIN{$val}");
            $this->kamoku[$val] = $val;
        }

        $result->free();
        Query::dbCheckIn($db);
    }


    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl110oQuery::getUpdateQuery($this);
            $this->cmd = "main";
            $this->setMessage("MSG201");
        }
        return;
    } 
}
?>