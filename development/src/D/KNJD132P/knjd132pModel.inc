<?php

require_once('for_php7.php');
class knjd132pModel extends Model {
    var $cmd;
    var $exp_year;
    var $schregno;
    var $name;
    var $field = array();

    function knjd132pModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }
        $this->semester = "9";//固定

        $this->field = array("SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),    //備考
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        //左フレームから取得
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //入力文字初期値の設定
        //備考
        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 17;
            $this->specialactremark_gyou = 4;
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        //文字数チェック
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ($this->specialactremark_moji * 2)) > $this->specialactremark_gyou) {
            $this->setWarning("MSG915","(備考は{$this->specialactremark_gyou}行までです)");
            return false;
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        //HREPORTREMARK_DAT
        knjd132pQuery::getInsertQuery($this);

        return ;
    }
}
?>
