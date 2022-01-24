<?php

require_once('for_php7.php');


class knjwa170Model extends Model {

    var $cmd;

    //検索結果
    var $leftYear;
    var $leftSearchdiv;
    var $leftNamekana;
    var $leftBelonging;
    var $leftApplicant;
    var $search = array();
    var $field = array();
    var $str_date;
    var $end_date;
    var $print_date;
    var $print;
    var $print_field = array();
    var $print_div;
    var $belonging = array();
    var $hr_class = array();
    var $grade = array();
    var $month_div;
    var $checkBoxVal;
    var $select_data 	= array();

    function knjwa170Model() {
    }
 
    function init()
    {
        $this->cmd = VARS::request("cmd");
        
        //$this->print_div = VARS::post("PRINT_DIV");
        $this->print_div = "2";
        $this->str_date = VARS::post("STR_DATE");
        $this->end_date = VARS::post("END_DATE");
        $this->print_date = VARS::post("DATE");
        $this->month_div = VARS::post("MONTH_DIV");
        
        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS")); //年組
        unset($this->select_data);
        $this->select_data[] = explode(",", VARS::post("selectdata"));

        if ($this->cmd == "search") {
            $this->search = array ( "SCHREGNO1"     => VARS::get("LEFT_SCHREGNO1"),
                                    "SCHREGNO2"     => VARS::get("LEFT_SCHREGNO2"),
                                    "NAME"          => VARS::get("LEFT_NAME"),
                                    "NAME_KANA"     => VARS::get("LEFT_NAME_KANA"),
                                    "EXE_YEAR"      => VARS::get("LEFT_EXE_YEAR"),
                                    "BELONGING_DIV" => VARS::get("LEFT_BELONGING_DIV"));
        }
        
        if ($this->cmd == "update") {
            $this->field = array ( "PRINT_CHECK"        => VARS::post("PRINT_CHECK"),
                                   "ENT_DATE"           => VARS::post("ENT_DATE"),
                                   "GRD_SCHEDULE_DATE"  => VARS::post("GRD_SCHEDULE_DATE"),
                                   "STATION_FROM"       => VARS::post("STATION_FROM"),
                                   "STATION_TO"         => VARS::post("STATION_TO"),
                                   "STATION_VIA"        => VARS::post("STATION_VIA")
            );
            
        }

        $this->checkBoxVal = $this->cmd == "monthChange" && VARS::post("PRINT_CHECK") ? VARS::post("PRINT_CHECK") : array();

        if ($this->cmd == "") {
            unset($this->search);
            unset($this->check_seito);
            unset($this->check_tugaku);
            unset($this->check_untin);
            unset($this->str_date);
            unset($this->end_date);
            unset($this->print_date);
            unset($this->print_field);
            unset($this->print_div);
            unset($this->month_div);
            unset($this->checkBoxVal);
        }
        
        if (AUTHORITY == DEF_REFER_RESTRICT 
            || AUTHORITY == DEF_UPDATE_RESTRICT) 
        {
            //DB接続
            $db     = Query::dbCheckOut();

            $this->belonging = $db->getRow(knjwa170Query::getStaff(), DB_FETCHMODE_ASSOC);
            
            $this->hr_class = array();
            $this->grade = array();
            $result = $db->query(knjwa170Query::getHrclass($this));
            while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->hr_class[] = $row["VALUE"];
                list($grade, $hr_class) = preg_split("/-/", $row["VALUE"]);
                $this->grade[] = $grade;
            }
            $result->free();

            //DB切断
            Query::dbCheckIn($db);
        }

    }
    
    //更新処理
    function &getUpdateModel()
    {
        $res = knjwa170Query::getUpdateQuery($this);
        $this->print_field = $res;
        $this->print = "on";
        return;
    }
}
?>
