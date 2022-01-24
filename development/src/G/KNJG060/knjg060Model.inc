<?php

require_once('for_php7.php');

class knjg060Model extends Model {
    var $cmd;

    //検索結果
    var $leftYear;
    var $leftSearchdiv;
    var $leftNamekana;
    var $leftApplicant;
    var $search = array();
    var $field = array();
    var $str_date;
    var $end_date;
    var $print_date;
    var $print;
    var $print_field = array();
    var $print_div;
    var $hr_class = array();
    var $grade = array();
    var $month_div;
    var $checkBoxVal;
    var $grade_hr_class;
    var $select_data = array();
    var $subSchregno;
    var $isRisshi;
    var $isFreshman;

    function knjg060Model() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        //$this->print_div = VARS::post("PRINT_DIV");
        $this->print_div = "2";
        $this->str_date = VARS::post("STR_DATE");
        $this->end_date = VARS::post("END_DATE");
        $this->print_date = VARS::post("DATE");
        $this->month_div = VARS::post("MONTH_DIV");

        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS")); //年組
        //一旦選択済は残す
        $this->select_data = explode(",", VARS::post("SELECT_DATA"));
        if (VARS::post("SELECT_DATA") == "") {
            $this->select_data = array();
        }

        if ($this->cmd == "search") {
            $this->search = array ( "SCHREGNO1"     => VARS::get("LEFT_SCHREGNO1"),
                                    "SCHREGNO2"     => VARS::get("LEFT_SCHREGNO2"),
                                    "NAME"          => VARS::get("LEFT_NAME"),
                                    "NAME_KANA"     => VARS::get("LEFT_NAME_KANA"),
                                    "EXE_YEAR"      => VARS::get("LEFT_EXE_YEAR")
                                   );
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

        //新入生・在学生
        if (VARS::post("STUDENT_RADIO")){
            $this->studentRadio = VARS::post("STUDENT_RADIO");
        }
        //クラス
        if (VARS::post("GRADE_HR_CLASS")){
            $this->grade_hr_class = VARS::post("GRADE_HR_CLASS");
        }

        //通学
        if (VARS::post("TICKET_RADIO")){
            $this->ticketRadio = VARS::post("TICKET_RADIO");
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

        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            //DB接続
            $db = Query::dbCheckOut();

            $this->hr_class = array();
            $this->grade = array();
            $result = $db->query(knjg060Query::getHrclass($this));
            while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->hr_class[] = $row["VALUE"];
                list($grade, $hr_class) = preg_split("/-/", $row["VALUE"]);
                $this->grade[] = $grade;
            }
            $result->free();

            //DB切断
            Query::dbCheckIn($db);
         }

        if (VARS::get("SCHREGNO")) {
            $this->subSchregno = VARS::get("SCHREGNO");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        // 学校コードを取得　立志舎：NAME1:risshisha
        $rtnRow = knjg060Query::getNameMstZ010();
        $this->isRisshi     = $rtnRow["NAME1"] == "risshisha" ? "1" : "";

        // 新入生のラジオの表示
        $this->isFreshman = "";
        if ($this->Properties["use060_Freshman_Hyouji"] == "1") {
            $this->isFreshman = "1";
        }
    }

    //更新処理
    function &getUpdateModel() {
        $res = knjg060Query::getUpdateQuery($this);
        $this->print_field = $res;
        $this->print = "on";
        return;
    }
}
?>
