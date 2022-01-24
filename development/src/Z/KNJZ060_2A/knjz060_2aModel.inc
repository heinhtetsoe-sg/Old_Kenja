<?php

require_once('for_php7.php');

class knjz060_2aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $ibclasscd = "";
    var $ibprg_course = "";

    function knjz060_2aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("IBCLASSCD"            =>  VARS::post("IBCLASSCD"),
                             "IBPRG_COURSE"         =>  VARS::post("IBPRG_COURSE"),
                             "IBCLASSNAME"          =>  VARS::post("IBCLASSNAME"),
                             "IBCLASSABBV"          =>  VARS::post("IBCLASSABBV"),
                             "IBCLASSNAME_ENG"      =>  VARS::post("IBCLASSNAME_ENG"),
                             "IBCLASSABBV_ENG"      =>  VARS::post("IBCLASSABBV_ENG"),
                             "IBCLASSORDERNAME1"    =>  VARS::post("IBCLASSORDERNAME1"),
                             "IBCLASSORDERNAME2"    =>  VARS::post("IBCLASSORDERNAME2"),
                             "IBCLASSORDERNAME3"    =>  VARS::post("IBCLASSORDERNAME3"),
                             "IBSUBCLASSES"         =>  VARS::post("IBSUBCLASSES"),
                             "IBSHOWORDER"          =>  VARS::post("IBSHOWORDER"),
                             "IBSHOWORDER2"         =>  VARS::post("IBSHOWORDER2"),
                             "IBSHOWORDER3"         =>  VARS::post("IBSHOWORDER3"),
                             "IBSHOWORDER4"         =>  VARS::post("IBSHOWORDER4"),
                             "IBELECTDIV"           =>  VARS::post("IBELECTDIV"),
                             "IBSPECIALDIV"         =>  VARS::post("IBSPECIALDIV"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("mode") == "1") {
            unset($this->ibclasscd);
            unset($this->ibprg_course);
        }

        if (VARS::get("IBCLASSCD")) {
            $this->ibclasscd = VARS::get("IBCLASSCD");
        }
        if (VARS::get("IBPRG_COURSE")) {
            $this->ibprg_course = VARS::get("IBPRG_COURSE");
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "add") {
            //必須チェック
            if (trim($this->field["IBCLASSCD"]) == "") {
                $this->setWarning("MSG301", "(IB教科コード)");
                return false;
            }
            if (trim($this->field["IBPRG_COURSE"]) == "") {
                $this->setWarning("MSG301", "(IBコース)");
                return false;
            }
        }
        if (trim($this->field["IBCLASSNAME"]) == "") {
            $this->setWarning("MSG301", "(IB教科名)");
            return false;
        }
        if (trim($this->field["IBCLASSNAME_ENG"]) == "") {
            $this->setWarning("MSG301", "(IB教科名英字)");
            return false;
        }
        if (trim($this->field["IBCLASSABBV_ENG"]) == "") {
            $this->setWarning("MSG301", "(IB教科略称英字)");
            return false;
        }

        //教科コードのゼロ埋め
        if (strlen($this->field["IBCLASSCD"]) > 0 && strlen($this->field["IBCLASSCD"]) < 2) {
            $this->field["IBCLASSCD"] = sprintf("%02d", $this->field["IBCLASSCD"]);
        }

        if ($this->cmd == "update") {
            //キー値の変更をチェック
            if ($this->ibclasscd == "") {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            if ($this->ibclasscd != $this->field["IBCLASSCD"]) {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            if ($this->ibprg_course == "") {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            if ($this->field["IBPRG_COURSE"] != $this->ibprg_course) {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
        }

        //１レコード取得
        $row = knjz060_2aQuery::getRow($this, $this->field["IBCLASSCD"], $this->field["IBPRG_COURSE"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(IB教科コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["IBCLASSNAME"]) > 90) {
            $this->setWarning("MSG915", "(IB教科名) 90バイトまで");
            return false;
        } else if (strlen($this->field["IBCLASSABBV"]) > 90){
            $this->setWarning("MSG915", "(IB教科略称) 90バイトまで");
            return false;
        } else if (strlen($this->field["IBCLASSABBV"]) > $this->set_maxabbv){
            $this->setWarning("MSG915", "(IB教科略称) ".$this->set_maxabbv."バイトまで");
            return false;
        } else if (strlen($this->field["IBCLASSNAME_ENG"]) > 40) {
            $this->setWarning("MSG915", "(IB教科名英字) 40バイトまで");
            return false;
        } else if (strlen($this->field["IBCLASSABBV_ENG"]) > 30) {
            $this->setWarning("MSG915", "(IB教科略称英字) 30バイトまで");
            return false;
        } else if (strlen($this->field["IBCLASSORDERNAME1"]) > 60) {
            $this->setWarning("MSG915", "(IB調査書用教科名) 60バイトまで");
            return false;
        } else if (strlen($this->field["IBCLASSORDERNAME2"]) > 60) {
            $this->setWarning("MSG915", "(IB教科名その他２) 60バイトまで");
            return false;
        } else if (strlen($this->field["IBCLASSORDERNAME3"]) > 60) {
            $this->setWarning("MSG915", "(IB教科名その他３) 60バイトまで");
            return false;
        } else if (strlen($this->field["IBSUBCLASSES"]) > 2) {
            $this->setWarning("MSG915", "(IB科目数) 2バイトまで");
            return false;
        } else if (strlen($this->field["IBSHOWORDER"]) > 2) {
            $this->setWarning("MSG915", "(IB表示順) 2バイトまで");
            return false;
        } else if (strlen($this->field["IBSHOWORDER2"]) > 2) {
            $this->setWarning("MSG915", "(IB調査書用表示順) 2バイトまで");
            return false;
        } else if (strlen($this->field["IBSHOWORDER3"]) > 2) {
            $this->setWarning("MSG915", "(IB通知表用表示順) 2バイトまで");
            return false;
        } else if (strlen($this->field["IBSHOWORDER4"]) > 2) {
            $this->setWarning("MSG915", "(IB成績一覧用表示順) 2バイトまで");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            //更新
            knjz060_2aQuery::getUpdateQuery($this, $this->field);

            $this->ibclasscd = $this->field["IBCLASSCD"];
            $this->ibprg_course = $this->field["IBPRG_COURSE"];
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->ibclasscd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->ibprg_course == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        //キー値の変更をチェック
        if ($this->ibclasscd != $this->field["IBCLASSCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        if ($this->field["IBPRG_COURSE"] != $this->ibprg_course) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        //存在チェック
        $row = knjz060_2aQuery::getRow($this, $this->ibclasscd, $this->ibprg_course);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        if (knjz060_2aQuery::IsExistingYdat($this)) {
            $this->setWarning("MSG919", "IB教科年度データに登録されています。");
            return;
        }
        if (knjz060_2aQuery::IsExistingSubclass($this)) {
            $this->setWarning("MSG919", "IB科目登録がされています。");
            return;
        }

        //削除
        knjz060_2aQuery::getDeleteQuery($this, $this->field);
        $this->setMessage("MSG201");
    }
}
?>
