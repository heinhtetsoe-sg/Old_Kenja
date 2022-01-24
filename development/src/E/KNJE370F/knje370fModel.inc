<?php

require_once('for_php7.php');

class knje370fModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $exp_year;       //左フレームから送られてくる年度
    var $exp_semester;   //左フレームから送られてくる学期
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knje370fModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->schregno);
            unset($this->name);
        }
        if (VARS::get("URL_SCHOOLKIND")!="") {
            $this->urlSchoolKind;
        }

        $this->field = array(
                                "CHURCH_NAME"         =>  VARS::post("CHURCH_NAME"),
                                "BAPTISM_DAY"         =>  VARS::post("BAPTISM_DAY"),
                                "HOUSHI_TOU"          =>  VARS::post("HOUSHI_TOU"),
                                "REMARK"              =>  VARS::post("REMARK")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR")!="") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->grade    = VARS::get("GRADE");
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }
        

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->schregno);
            unset($this->name);
        }
        
        //校種取得
        $db = Query::dbCheckOut();
        $this->schoolkind = $db->getOne(knje370fQuery::getSchoolkind($this->grade));
        Query::dbCheckIn($db);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //文字数
        $this->church_name_moji= 50;
        $this->church_name_gyou= 1;
        $this->houshi_tou_moji = 30;
        $this->houshi_tou_gyou = 3;
        $this->remark_moji     = 50;
        $this->remark_gyou     = 2;
        
    }

    function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knje370fQuery::getProgrampath());
        Query::dbCheckIn($db);

        return $programpath;
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }

        //行数チェック
        if ($this->validate_row_cnt($this->field["CHURCH_NAME"], ($this->church_name_moji * 2)) > $this->church_name_gyou) {
            $this->setWarning("MSG915","(教会名は{$this->church_name_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["HOUSHI_TOU"], ($this->houshi_tou_moji * 2)) > $this->houshi_tou_gyou) {
            $this->setWarning("MSG915","(奉仕等は{$this->houshi_tou_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK"], ($this->remark_moji * 2)) > $this->remark_gyou) {
            $this->setWarning("MSG915","(備考は{$this->remark_gyou}行までです)");
            return false;
        }
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {

        knje370fQuery::getInsertUpdateQuery($this, $this->schregno);
        return true;
    }
}
?>
