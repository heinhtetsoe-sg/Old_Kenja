<?php

require_once('for_php7.php');

class knjd131mModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade;
    public $semester;
    public $field = array();
    public $exp_year;
    public $school_kind;
    public $getPro;
    public $arySchooleventAttend;

    public function knjd131mModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->semester);
        }

        $this->field = array("COMMUNICATION"       =>  VARS::post("COMMUNICATION"),       //備考
                             "SCHOOLEVENT_NAME1"   =>  VARS::post("SCHOOLEVENT_NAME1"),   //学校行事の記録 名称1
                             "SCHOOLEVENT_NAME2"   =>  VARS::post("SCHOOLEVENT_NAME2"),   //学校行事の記録 名称2
                             "SCHOOLEVENT_NAME3"   =>  VARS::post("SCHOOLEVENT_NAME3"),   //学校行事の記録 名称3
                             "SCHOOLEVENT_NAME4"   =>  VARS::post("SCHOOLEVENT_NAME4"),   //学校行事の記録 名称4
                             "SCHOOLEVENT_NAME5"   =>  VARS::post("SCHOOLEVENT_NAME5"),   //学校行事の記録 名称5
                             "SCHOOLEVENT_NAME6"   =>  VARS::post("SCHOOLEVENT_NAME6"),   //学校行事の記録 名称6
                             "SCHOOLEVENT_ATTEND1" =>  VARS::post("SCHOOLEVENT_ATTEND1"), //学校行事の記録 参加1
                             "SCHOOLEVENT_ATTEND2" =>  VARS::post("SCHOOLEVENT_ATTEND2"), //学校行事の記録 参加2
                             "SCHOOLEVENT_ATTEND3" =>  VARS::post("SCHOOLEVENT_ATTEND3"), //学校行事の記録 参加3
                             "SCHOOLEVENT_ATTEND4" =>  VARS::post("SCHOOLEVENT_ATTEND4"), //学校行事の記録 参加4
                             "SCHOOLEVENT_ATTEND5" =>  VARS::post("SCHOOLEVENT_ATTEND5"), //学校行事の記録 参加5
                             "SCHOOLEVENT_ATTEND6" =>  VARS::post("SCHOOLEVENT_ATTEND6"), //学校行事の記録 参加6
                             "SPECIALACTREMARK"    =>  "",                                //学校行事の記録
                             );

        if (VARS::post("SEMESTER") != "") {
            $this->semester = VARS::post("SEMESTER");
        }

        $this->cmd = VARS::request("cmd");
        $this->exp_year = CTRL_YEAR;

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade    = VARS::get("GRADE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->semester);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種を取得
        $db = Query::dbCheckOut();
        $this->school_kind = $db->getOne(knjd131mQuery::getSchoolKind($this->grade));
        if (!strlen($this->school_kind)) {
            $this->school_kind = "H";
        }
        Query::dbCheckIn($db);

        //備考
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->school_kind]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 32;
            $this->getPro["COMMUNICATION"]["gyou"] = 4;
        }

        //学校行事の記録 名称
        for ($idx = 1; $idx <= 6; $idx++) {
            $this->getPro["SCHOOLEVENT_NAME{$idx}"]["moji"] = 6;
            $this->getPro["SCHOOLEVENT_NAME{$idx}"]["gyou"] = 1;
        }

        //学校行事の記録 参加
        $this->arySchooleventAttend = array(
            "1" => "参加",
            "2" => "不参加",
        );
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if (!strlen($this->semester)) {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }

        //文字数チェック
        if ($this->semester == "1" || $this->semester == "2") {
            if (strlen($this->field["COMMUNICATION"]) > 0) {
                if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
                    $this->setWarning("MSG915", "(備考は{$this->getPro["COMMUNICATION"]["gyou"]}行{$this->getPro["COMMUNICATION"]["moji"]}文字までです)");
                    return false;
                }
            }
        }

        if ($this->semester == "3") {
            //学校行事の記録
            for ($idx = 1; $idx <= 6; $idx++) {
                //学校行事の記録 名称
                if (strlen($this->field["SCHOOLEVENT_NAME{$idx}"]) > 0) {
                    if ($this->validate_row_cnt($this->field["SCHOOLEVENT_NAME{$idx}"], ($this->getPro["SCHOOLEVENT_NAME{$idx}"]["moji"] * 2)) > $this->getPro["SCHOOLEVENT_NAME{$idx}"]["gyou"]) {
                        $gyoStr = $this->getPro["SCHOOLEVENT_NAME{$idx}"]["gyo"] > 1 ? $this->getPro["SCHOOLEVENT_NAME{$idx}"]["gyo"]."行" : "";
                        $this->setWarning("MSG915", "({$idx} 学校行事の記録 名称は{$gyoStr}{$this->getPro["SCHOOLEVENT_NAME{$idx}"]["moji"]}文字までです)");
                        return false;
                    }
                }
                if (strlen($this->field["SCHOOLEVENT_NAME{$idx}"]) == 0 && strlen($this->field["SCHOOLEVENT_ATTEND{$idx}"]) > 0) {
                    $this->setWarning("MSG301", "(学校行事の記録 {$idx}: 「参加/不参加」が選択されている場合、名称は必須です)");
                    return false;
                }
            }
        }

        return true;
    }

    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    public function &update()
    {
        if ($this->semester == "3") {
            $arySpecialactremark = array();
            for ($idx = 1; $idx <= 6; $idx++) {
                $attendStr = "";
                $key = $this->field["SCHOOLEVENT_ATTEND{$idx}"];
                if (array_key_exists($key, $this->arySchooleventAttend)) {
                    $attendStr = $this->arySchooleventAttend[$key];
                }
                $arySpecialactremark[$idx] = $this->field["SCHOOLEVENT_NAME{$idx}"]."　".$attendStr;
            }

            for ($idx = 6; $idx >= 1; $idx--) {
                //最終行が全角スペースのみのとき削除
                if ($arySpecialactremark[$idx] == "　") {
                    unset($arySpecialactremark[$idx]);
                } else {
                    break;
                }
            }
            //学校行事の記録
            $this->field["SPECIALACTREMARK"] = implode("\r\n", $arySpecialactremark);
        }

        knjd131mQuery::getUpdateQuery($this, $this->field, $this->schregno);
        return;
    }
}
