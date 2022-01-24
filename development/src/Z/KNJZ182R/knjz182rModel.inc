<?php

require_once('for_php7.php');

class knjz182rModel extends Model {
    var $cmd;
    var $field      = array();

    //メンバ変数の初期化
    function knjz182rModel() {
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            $this->field = array();
            $this->testcd       = "";
            $this->div          = "";
            $this->grade        = "";
            $this->hr_class     = "";
            $this->coursecd     = "";
            $this->groupcd      = "";
            $this->coursecode   = "";
            $this->setAssesslevelCount = "";
        }

        $this->cmd = VARS::request("cmd");

        //左画面コンボ変更時
        if ($this->cmd === 'list_change' || $this->cmd === 'copy') {
            $this->field = array("PRE_TESTCD"       =>  VARS::post("PRE_TESTCD"),   //参照:学期・テスト種別
                                 "TESTCD"           =>  VARS::post("TESTCD"),       //対象:学期・テスト種別
                                 "DIV"              =>  VARS::post("DIV")           //区分
                                );
        }
        //右画面コンボ変更時
        $this->grade = VARS::post("GRADE");
        if ($this->div === '5') {
            $this->groupcd = VARS::post("GROUP_CD");
        } else if ($this->div === '3') {
            $this->course = VARS::post("COURSE");
            list($coursecd, $majorcd, $coursecode) = explode("-", $this->course);
            $this->coursecd        = $coursecd;
            $this->groupcd         = $majorcd;
            $this->coursecode      = $coursecode;
        }
        //段階数
        $this->field2["ASSESSLEVELCNT"] = VARS::post("ASSESSLEVELCNT");
        //右画面の各項目
        for ($i = 1; $i <= $this->setAssesslevelCount; $i++) { 
            //記号
            $assessmark = "RANK_MARK_".$i;
            $this->field2[$assessmark]  = VARS::post($assessmark);
            //下限値
            $assesslow  = "RANK_LOW_".$i;
            $this->field2[$assesslow]   = VARS::post($assesslow);
            //上限値
            $assesshigh = "RANK_HIGH_".$i;
            $this->field2[$assesshigh]  = VARS::post($assesshigh);
        }
        //リンク用から取得
        if (VARS::get("SEND_FLG")) {
            $this->testcd       = VARS::get("SEND_TESTCD");
            $this->div          = VARS::get("SEND_DIV");
            $this->grade        = VARS::get("SEND_GRADE");
            $this->hr_class     = VARS::get("SEND_HR_CLASS");
            $this->coursecd     = VARS::get("SEND_COURSECD");
            $this->groupcd      = VARS::get("SEND_MAJORCD");
            $this->coursecode   = VARS::get("SEND_COURSECODE");
            $this->course = $this->coursecd.'-'.$this->groupcd.'-'.$this->coursecode;
        }
        //プロパティファイル
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->testcd == "") {
            $this->setWarning("MSG304", "( テスト種別 )");
            return false;
        }
        if ($this->div == "") {
            $this->setWarning("MSG304", "( 区分 )");
            return false;
        }
        if ($this->grade == "") {
            $this->setWarning("MSG304", "( 学年 )");
            return false;
        }
        if ($this->div === '5') {
            if ($this->groupcd == "") {
                $this->setWarning("MSG304", "( コースグループ )");
                return false;
            }
        } else if ($this->div === '3') {
            if ($this->coursecd == "" || $this->groupcd == "" ||  $this->coursecode == "") {
                $this->setWarning("MSG304", "( コース )");
                return false;
            }
        }
        return true;
    }

    //更新
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($this->validateInput()) {
            knjz182rQuery::delete($this, $db);
            if ($this->cmd === 'update') {
                knjz182rQuery::update($this, $db);
            }
            $this->setMessage("MSG201");
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //データコピー
    function &getCopyModel() {
        $result = knjz182rQuery::getCopyQuery($this);
        $this->setMessage("MSG201");

        return true;
    }
}
?>
