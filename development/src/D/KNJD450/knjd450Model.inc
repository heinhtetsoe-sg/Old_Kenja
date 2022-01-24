<?php

require_once('for_php7.php');
class knjd450Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjd450Model()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

       if (VARS::get("PROGRAMID")!="") {
           unset($this->schregno);
           unset($this->name);
           unset($this->getSubclassName);
           unset($this->getSemester);
           unset($this->getDiv);
           unset($this->getWritingDate);
           unset($this->getClasscd);
           unset($this->getSchoolkind);
           unset($this->getCurriculumcd);
           unset($this->getSubclasscd);
           unset($this->getUpdFlg);
           unset($this->setGyou);
       }
        $this->field = array("WRITING_DATE" =>  VARS::post("WRITING_DATE")  //作成年月日
                            ,"REMARK1"      =>  VARS::post("REMARK1")       //授業中の様子
                            ,"REMARK2"      =>  VARS::post("REMARK2")       //支援目標
                            ,"REMARK3"      =>  VARS::post("REMARK3")       //具体的支援の方略
                            ,"REMARK4"      =>  VARS::post("REMARK4")       //支援の評価
                            ,"WRT_DATE"     =>  VARS::post("WRT_DATE")      //作成日
                             );

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }
        //左フレームから送られてくる年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //左フレームから送られてくる学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }

        //履歴用GETパラメータ
        if (VARS::get("SUBCLASSNAME") != "") {
            $this->getSubclassName = VARS::get("SUBCLASSNAME");
        }
        if (VARS::get("UPD_FLG") != "") {
            $this->getUpdFlg = VARS::get("UPD_FLG");
        }
        if (VARS::get("SEMESTER") != "") {
            $this->getSemester = VARS::get("SEMESTER");
        }
        if (VARS::get("DIV") != "") {
            $this->getDiv = VARS::get("DIV");
        }
        if (VARS::get("WRITING_DATE") != "") {
            $this->getWritingDate = VARS::get("WRITING_DATE");
        }
        if (VARS::get("CLASSCD") != "") {
            $this->getClasscd = VARS::get("CLASSCD");
        }
        if (VARS::get("SCHOOL_KIND") != "") {
            $this->getSchoolkind = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("CURRICULUM_CD") != "") {
            $this->getCurriculumcd = VARS::get("CURRICULUM_CD");
        }
        if (VARS::get("SUBCLASSCD") != "") {
            $this->getSubclasscd = VARS::get("SUBCLASSCD");
        }
    }
    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="" || $this->getUpdFlg=="") {
            $this->setWarning("MSG304");
            return false;
        }

        $writing_date   = str_replace("/", "-", $this->field["WRITING_DATE"]);
        $wrt_date       = str_replace("/", "-", $this->field["WRT_DATE"]);

        if ($this->cmd == "update") {
            //必須チェック
            if ($writing_date == "") {
                $this->setWarning("MSG304", "(作成年月日)");
                return false;
            }

            if ($writing_date != $wrt_date) {
                $this->setWarning("MSG308", "（作成年月日と作成日が異なります）");
                return false;
            }
        }

        if ($this->cmd == "add") {
            //必須チェック
            if ($wrt_date == "") {
                $this->setWarning("MSG304", "(作成日)");
                return false;
            }

            //作成年月日取得
            $db = Query::dbCheckOut();
            $date_array = $db->getCol(knjd450Query::getWritingDate($this));
            Query::dbCheckIn($db);
            //重複チェック
            if (in_array($wrt_date, $date_array)) {
                $this->setWarning("MSG302", "（作成日）");
                return false;
            }
        }

        //文字数・行数チェック
        if ($this->validate_row_cnt($this->field["REMARK1"], (31 * 2)) > $this->setGyou) {
            $this->setWarning("MSG915","(授業中の様子は".$this->setGyou."行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], (23 * 2)) > $this->setGyou) {
            $this->setWarning("MSG915","(支援目標は".$this->setGyou."行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK3"], (31 * 2)) > $this->setGyou) {
            $this->setWarning("MSG915","(具体的支援の方略は".$this->setGyou."行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK4"], (17 * 2)) > $this->setGyou) {
            $this->setWarning("MSG915","(支援の評価は".$this->setGyou."行までです)");
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
        $this->field["WRITING_DATE"] = str_replace("/", "-", $this->field["WRT_DATE"]);
    }

    function &update()
    {
        knjd450Query::getUpdateQuery($this);
        return $result;
    }
}
?>
