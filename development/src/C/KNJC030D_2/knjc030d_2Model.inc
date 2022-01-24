<?php

require_once('for_php7.php');

class knjc030d_2Model extends Model {

    var $cmd;
    var $field = array();
    var $reset = array();
    var $auth;
    var $hr_class_type;
    var $grade_hr_class;
    var $date;
    var $semester;
    var $title;

    function knjc030d_2Model()
    {
        $this->field = array();
    }

    function init()
    {
        if (VARS::get("prgid")) {
            unset($this->auth);
            unset($this->hr_class_type);
            unset($this->grade_hr_class);
            unset($this->date);
            unset($this->semester);
            unset($this->title);
        }

        if(VARS::get("auth") != ""){
            $this->auth = VARS::get("auth");
        }
        if(VARS::get("HR_CLASS_TYPE") != ""){
            $this->hr_class_type = VARS::get("HR_CLASS_TYPE");
        }
        if(VARS::get("GRADE_HR_CLASS") != ""){
            $this->grade_hr_class = VARS::get("GRADE_HR_CLASS");
        }
        if(VARS::get("DATE") != ""){
            $this->date = VARS::get("DATE");
        }
        if(VARS::get("SEMESTER") != ""){
            $this->semester = VARS::get("SEMESTER");
        }
        if(VARS::get("TITLE") != ""){
            $this->title = VARS::get("TITLE");
        }

        $this->cmd = VARS::request("cmd");
        $this->field    = $_POST;

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /* 入力項目チェック */
    function validateInput()
    {
        //必須チェック
        if(!isset($this->field["SCHREGNO"])){
            $this->setMessage("MSG304");
            return false;
        }

        $db = Query::dbCheckOut();

        //チェック項目の重複チェック
        foreach($this->field["SCHREGNO"] as $key => $schregno){

            $check_cnt = 0;
            if(substr($this->title,0,3) == 'CNT') {

                $subl_cd = substr($this->title,3);
                $subm_cd = $db->getCol(knjc030d_2Query::getNameMst('C007', $subl_cd));
                foreach($subm_cd as $submKey) {
                    if($this->field["SUBDATA_{$schregno}_CNT{$submKey}"] == "1") {
                        $check_cnt++;
                    }
                }
            } else {

                $subl_cd = $db->getCol(knjc030d_2Query::getNameMst('C006', $this->field["DI_CD"]));
                foreach($subl_cd as $sublKey) {
                    if($this->field["SUBDATA_{$schregno}_CNT{$sublKey}"] == "1") {
                        $check_cnt++;
                    }
                }
            }
            if($check_cnt > 1){
                $this->setMessage("MSG901");
                return false;
            }
        }

        Query::dbCheckIn($db);

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
        $result = knjc030d_2Query::getUpdateQuery($this);
        return $result;
    }
}
?>
