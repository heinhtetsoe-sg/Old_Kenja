<?php

require_once('for_php7.php');


class knjj010_3Model extends Model
{
    public $cmd;
    public $year;
    public $clubcd;

    public function knjj010_3Model()
    {
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        if (VARS::get("mode") == "1" || $this->cmd == "changeYear") {
            unset($this->clubcd);
            unset($this->year);
            unset($this->copyYear);
        }

        if (VARS::get("CLUBCD") != "") {
            $this->clubcd = VARS::get("CLUBCD");
        }

        if (VARS::request("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }

        if (VARS::request("COPY_YEAR") != "") {
            $this->copyYear = VARS::request("COPY_YEAR");
        }

        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
    }

    public function validateInput()
    {
        return true;
    }

    public function getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->selectData = explode(",", VARS::post("selectdata"));
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    public function update()
    {
        knjj010_3Query::getUpdateQuery($this);
    }

    //コピー
    public function getCopyModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjj010_3Query::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //変数をクリアします
    public function clean()
    {
    }
}
