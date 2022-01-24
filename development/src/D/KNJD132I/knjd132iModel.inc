<?php

require_once('for_php7.php');
class knjd132iModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $annual;                    //学年(年次)
    var $exp_year;       //左フレームから送られてくる年度
    var $exp_semester;   //左フレームから送られてくる学期
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjd132iModel()
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

        $this->field = array("SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION")
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
        $this->schoolkind = $db->getOne(knjd132iQuery::getSchoolkind($this->grade));
        Query::dbCheckIn($db);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_{$this->schoolkind}"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_{$this->schoolkind}"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 50;  //デフォルトの値
            $this->specialactremark_gyou = 5;   //デフォルトの値
        }
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_{$this->schoolkind}"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_{$this->schoolkind}"]);
            $this->communication_moji = (int)trim($moji);
            $this->communication_gyou = (int)trim($gyou);
        } else {
            $this->communication_moji = 50;  //デフォルトの値
            $this->communication_gyou = 2;   //デフォルトの値
        }
    }

    function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knjd132iQuery::getProgrampath());
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
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ($this->specialactremark_moji * 2)) > $this->specialactremark_gyou) {
            $this->setWarning("MSG915","(特別活動・資格等の記録は{$this->specialactremark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->communication_moji * 2)) > $this->communication_gyou) {
            $this->setWarning("MSG915","(通信欄は{$this->communication_gyou}行までです)");
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
        knjd132iQuery::getInsertUpdateQuery($this, $this->schregno, $this->exp_semester);
        return true;
    }
}
?>
