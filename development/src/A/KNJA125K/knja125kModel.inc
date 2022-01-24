<?php

require_once('for_php7.php');

class knja125kModel extends Model
{
    public $cmd;
    public $exp_year;
    public $exp_semester;
    public $schregno;
    public $name;
    public $field = array();

    public function knja125kModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);

            $this->exp_year     = CTRL_YEAR;
            $this->exp_semester = CTRL_SEMESTER;
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "INDEPENDENCE_REMARK"  =>  VARS::post("INDEPENDENCE_REMARK"),
                             "FIELD1"               =>  VARS::post("FIELD1"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("KINYUURYOKUTYPE") != "") {
            $this->kinyuuryokuType = VARS::get("KINYUURYOKUTYPE");
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

        $db = Query::dbCheckOut();
        $this->z010 = $db->getOne(knja125kQuery::getZ010());
        Query::dbCheckIn($db);

        //テキストサイズ一覧
        if ($this->Properties["useSpecial_Support_School"] == "1") {
            $this->textSize["TOTALSTUDYACT"]    = array("moji" => 21, "gyo" => 4,  "title" => "指導の重点等（学年）");
            $this->textSize["TOTALSTUDYVAL"]    = array("moji" => 21, "gyo" => 4,  "title" => "指導の重点等（個人）");
            $this->textSize["TOTALREMARK"]      = array("moji" => 21, "gyo" => 50, "title" => "指導上参考となる事項");
            $this->textSize["ATTENDREC_REMARK"] = array("moji" => 21, "gyo" => 6,  "title" => "出欠状況の備考");
            $this->textSize["INDEPENDENCE_REMARK"] = array("moji" => 21, "gyo" => 6,  "title" => "自立活動に重点を置いた指導");
            $this->textSize["FIELD1"]           = array("moji" => 12, "gyo" => 7,  "title" => "入学時の障害の状態等");
        } else {
            $this->textSize["TOTALSTUDYACT"]    = array("moji" => 8, "gyo" => 5,  "title" => "指導の重点等（学年）");
            $this->textSize["TOTALSTUDYVAL"]    = array("moji" => 8, "gyo" => 5,  "title" => "指導の重点等（個人）");
            $this->textSize["TOTALREMARK"]      = array("moji" => 10, "gyo" => 50, "title" => "指導上参考となる事項");
            $this->textSize["ATTENDREC_REMARK"] = array("moji" => 8, "gyo" => 4,  "title" => "出欠状況の備考");
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数チェック
        foreach ($this->textSize as $key => $val) {
            if ($this->validate_row_cnt($this->field[$key], ((int)$val["moji"] * 2)) > $val["gyo"]) {
                $this->setWarning("MSG915", "({$val["title"]}は{$val["gyo"]}行までです)");
                return false;
            }
        }

        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knja125kQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
