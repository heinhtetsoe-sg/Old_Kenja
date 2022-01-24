<?php

require_once('for_php7.php');

class knjz070_2aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $ibclasscd = "";
    var $ibprg_course = "";
    var $ibcurriculum_cd = "";
    var $ibsubclasscd = "";

    function knjz070_2aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        list($ibclasscd, $ibprg_course) = explode('-', VARS::post("IBCLASS_SCHOOL"));
        $ibsubclasscd = (VARS::post("IBSUBCLASS")) ? sprintf("%04d", VARS::post("IBSUBCLASS")) : "";

        $this->field = array("IBCLASSCD"                =>  $ibclasscd,
                             "IBPRG_COURSE"             =>  $ibprg_course,
                             "IBCURRICULUM_CD"          =>  VARS::post("IBCURRICULUM_CD"),
                             "IBSUBCLASSCD"             =>  (VARS::post("IBCLASS_SCHOOL") && VARS::post("IBSUBCLASS") ? $ibclasscd : "").$ibsubclasscd,
                             "IBSUBCLASSNAME"           =>  VARS::post("IBSUBCLASSNAME"),
                             "IBSUBCLASSABBV"           =>  VARS::post("IBSUBCLASSABBV"),
                             "IBSUBCLASSNAME_ENG"       =>  VARS::post("IBSUBCLASSNAME_ENG"),
                             "IBSUBCLASSABBV_ENG"       =>  VARS::post("IBSUBCLASSABBV_ENG"),
                             "IBSUBCLASSORDERNAME1"     =>  VARS::post("IBSUBCLASSORDERNAME1"),
                             "IBSUBCLASSORDERNAME2"     =>  VARS::post("IBSUBCLASSORDERNAME2"),
                             "IBSUBCLASSORDERNAME3"     =>  VARS::post("IBSUBCLASSORDERNAME3"),
                             "IBSHOWORDER"              =>  VARS::post("IBSHOWORDER"),
                             "IBSHOWORDER2"             =>  VARS::post("IBSHOWORDER2"),
                             "IBSHOWORDER3"             =>  VARS::post("IBSHOWORDER3"),
                             "IBSUBCLASSCD2"            =>  VARS::post("IBSUBCLASSCD2"),
                             "IBSUBCLASSCD3"            =>  VARS::post("IBSUBCLASSCD3"),
                             "IBELECTDIV"               =>  VARS::post("IBELECTDIV"),
                             "UPDATED"                  =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("mode") == "1") {
            unset($this->ibclasscd);
            unset($this->ibprg_course);
            unset($this->ibcurriculum_cd);
            unset($this->ibsubclasscd);
        }

        if (VARS::get("IBCLASSCD")) {
            $this->ibclasscd = VARS::get("IBCLASSCD");
        }
        if (VARS::get("IBPRG_COURSE")) {
            $this->ibprg_course = VARS::get("IBPRG_COURSE");
        }
        if (VARS::get("IBCURRICULUM_CD")) {
            $this->ibcurriculum_cd = VARS::get("IBCURRICULUM_CD");
        }
        if (VARS::get("IBSUBCLASSCD")) {
            $this->ibsubclasscd = VARS::get("IBSUBCLASSCD");
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "add") {
            //必須チェック
            if (trim($this->field["IBCLASSCD"]) == "") {
                $this->setWarning("MSG301", "(IB教科)");
                return false;
            }
            if (trim($this->field["IBCURRICULUM_CD"]) == "") {
                $this->setWarning("MSG301", "(IB教育課程コード)");
                return false;
            }
            if (trim($this->field["IBSUBCLASSCD"]) == "") {
                $this->setWarning("MSG301", "(IB科目コード)");
                return false;
            }
        }
        if (trim($this->field["IBSUBCLASSNAME"]) == "") {
            $this->setWarning("MSG301", "(IB科目名)");
            return false;
        }
        if (trim($this->field["IBSUBCLASSNAME_ENG"]) == "") {
            $this->setWarning("MSG301", "(IB科目名英字)");
            return false;
        }
        if (trim($this->field["IBSUBCLASSABBV_ENG"]) == "") {
            $this->setWarning("MSG301", "(IB科目略称英字)");
            return false;
        }

        if ($this->cmd == "update") {
            //キー値の変更をチェック
            if ($this->ibclasscd        == "" ||
                $this->ibprg_course     == "" ||
                $this->ibcurriculum_cd  == "" ||
                $this->ibsubclasscd     == "") {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            if ($this->ibclasscd        != $this->field["IBCLASSCD"] ||
                $this->ibprg_course     != $this->field["IBPRG_COURSE"] ||
                $this->ibcurriculum_cd  != $this->field["IBCURRICULUM_CD"] ||
                $this->ibsubclasscd     != $this->field["IBSUBCLASSCD"]) {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
        }

        //１レコード取得
        $row = knjz070_2aQuery::getRow($this, $this->field["IBCLASSCD"], $this->field["IBPRG_COURSE"], $this->field["IBCURRICULUM_CD"], $this->field["IBSUBCLASSCD"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(IB科目コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["IBSUBCLASSNAME"]) > 60) {
            $this->setWarning("MSG915","(IB科目名は60バイトまでです)");
            return false;
        } else if (strlen($this->field["IBSUBCLASSNAME"]) == 0) {
            $this->setWarning("MSG301","(IB科目名)");
            return false;
        } else if (strlen($this->field["IBSUBCLASSABBV"]) > 90){
            $this->setWarning("MSG915","(IB科目略称は90バイトまでです)");
            return false;
        } else if (strlen($this->field["IBSUBCLASSABBV"]) > $this->set_maxabbv){
            $this->setWarning("MSG915","(IB科目略称は".$this->set_maxabbv."バイトまでです)");
            return false;
        } else if (strlen($this->field["IBSUBCLASSNAME_ENG"]) > 50) {
            $this->setWarning("MSG915","(IB科目名英字は50バイトまでです)");
            return false;
        } else if (strlen($this->field["IBSUBCLASSABBV_ENG"]) > 30) {
            $this->setWarning("MSG915","(IB科目略称英字は30バイトまでです)");
            return false;
        } else if (strlen($this->field["IBSUBCLASSORDERNAME1"]) > 60) {
            $this->setWarning("MSG915","(IB調査書用科目名は60バイトまでです)");
            return false;
        } else if (strlen($this->field["IBSUBCLASSORDERNAME2"]) > 60) {
            $this->setWarning("MSG915","(IB通知表用科目名は60バイトまでです)");
            return false;
        } else if (strlen($this->field["IBSUBCLASSORDERNAME3"]) > 60) {
            $this->setWarning("MSG915","(IB科目名その他３は60バイトまでです)");
            return false;
        } else if (strlen($this->field["IBSHOWORDER"]) > 2) {
            $this->setWarning("MSG915","(IB表示順)");
            return false;
        } else if (strlen($this->field["IBSHOWORDER2"]) > 2) {
            $this->setWarning("MSG915","(IB調査書用表示順)");
            return false;
        } else if (strlen($this->field["IBSHOWORDER3"]) > 2) {
            $this->setWarning("MSG915","(IB通知表用表示順)");
            return false;
        } else if (strlen($this->field["IBSUBCLASSCD2"]) > 6) {
            $this->setWarning("MSG915","(IB調査書・指導要録用科目グループコード)");
            return false;
        } else if (strlen($this->field["IBSUBCLASSCD3"]) > 6) {
            $this->setWarning("MSG915","(IB通知表用科目グループコード)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            //更新
            knjz070_2aQuery::getUpdateQuery($this, $this->field);

            $this->ibclasscd        = $this->field["IBCLASSCD"];
            $this->ibprg_course     = $this->field["IBPRG_COURSE"];
            $this->ibcurriculum_cd  = $this->field["IBCURRICULUM_CD"];
            $this->ibsubclasscd     = $this->field["IBSUBCLASSCD"];
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //キー値の変更をチェック
        if ($this->ibclasscd        == "" ||
            $this->ibprg_course     == "" ||
            $this->ibcurriculum_cd  == "" ||
            $this->ibsubclasscd     == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->ibclasscd        != $this->field["IBCLASSCD"] ||
            $this->ibprg_course     != $this->field["IBPRG_COURSE"] ||
            $this->ibcurriculum_cd  != $this->field["IBCURRICULUM_CD"] ||
            $this->ibsubclasscd     != $this->field["IBSUBCLASSCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        //存在チェック
        $row = knjz070_2aQuery::getRow($this, $this->ibclasscd, $this->ibprg_course, $this->ibcurriculum_cd, $this->ibsubclasscd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        if (knjz070_2aQuery::IsExistingYdat($this)) {
            $this->setWarning("MSG919", "IB科目年度データに登録されています。");
            return;
        }

        //削除
        knjz070_2aQuery::getDeleteQuery($this, $this->field);
        $this->setMessage("MSG201");
    }
}
?>
