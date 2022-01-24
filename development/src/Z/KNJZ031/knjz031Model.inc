<?php

require_once('for_php7.php');

class knjz031Model extends Model
{
    public $field = array();
    public $cmd;
    public $staffcd;          //セキュリティー用スタッフコード
    public $sec_competence;   //権限保持

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        unset($this->field);
        $this->field["SEME_SEP_NUM"] = VARS::post("SEME_SEP_NUM");
        $this->field["SEM_KEY"] = VARS::post("sem_key");
        $this->field["SEMESTER_NAME"] = VARS::post("SEMESTER_NAME");
        for ($i = 1; $i <= VARS::post("COUNT_SDATE"); $i++) {
            $this->field["SDATE"][] = VARS::post("SDATE" . $i);
            $this->field["EDATE"][] = VARS::post("EDATE" . $i);
        }

        if (VARS::post("YEAR")) {
            $this->field["YEAR"] = VARS::post("YEAR");
        } else {
            $this->field["YEAR"] = CTRL_YEAR;
        }
    }

    public function check()
    {
        foreach ($this->field["SDATE"] as $val) {
            if (!$val) {
                return false;
            }
        }
        return true;
    }

    public function &getUpdateModel()
    {
        if ($this->field["SEM_KEY"] && $this->check()) {
            $db = Query::dbCheckOut();
            $query = knjz031Query::getDeleteQuery($this->field["YEAR"]);
            $db->query($query);
            foreach ($this->field["SEM_KEY"] as $key => $val) {
                list($sem_cd, $ditail_cd, $sdate_from_key, $edate_from_key)  = explode(",", $val);//keyesは学期コードと詳細学期コードの配列
                $sdate = str_replace("/", "-", $this->field["SDATE"][$key]);

                $edate = str_replace("/", "-", $this->field["EDATE"][$key]);

                $semester_name = $this->field["SEMESTER_NAME"][$key];

                $insert_array = array();
                $insert_array = array("SEM_CD"        => $sem_cd,
                                      "SEM_DITAIL_CD" => $ditail_cd,
                                      "SEMESTER_NAME" => $semester_name,
                                      "SDATE"         => $sdate,
                                      "EDATE"         => $edate);

                $query = knjz031Query::getInsertQuery($insert_array, $this->field["YEAR"]);

                $db->query($query);
            }
            Query::dbCheckIn($db);
        } else {
            $this->setWarning("入力項目に不備があります。");
            return false;
        }
        $this->setMessage("MSG201");
    }
}
