<?php

require_once('for_php7.php');


class knjg045Model extends Model
{

    public $field  = array();    //画面データ格納
    public $cmd;                 //コマンド
    public $diaryDate;           //日付
    public $control;             //コントロールマスタ
    public $data  = array();
    public $di_cd;
    public $grade;
    public $att_field = array(); //欠席状況

    public function knjg045Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->diaryDate  = VARS::post("DIARY_DATE");
        if ($this->diaryDate == "") {
            $this->diaryDate  = str_replace("-", "/", CTRL_DATE);
        }
        $this->field = array("SCHOOL_KIND"                  =>  VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->field["SCHOOL_KIND"],
                             "WEATHER"                      =>  VARS::post("WEATHER"),
                             "WEATHER2"                     =>  VARS::post("WEATHER2"),
                             "NEWS"                         =>  VARS::post("NEWS"),
                             "GRADE_HR_CLASS"               =>  VARS::post("GRADE_HR_CLASS"),
                             "STAFFCD1"                     =>  VARS::post("STAFFCD1"),
                             "STAFFNAME_SHOW1"              =>  VARS::post("STAFFNAME_SHOW1"),
                             "STAFFCD2"                     =>  VARS::post("STAFFCD2"),
                             "STAFFNAME_SHOW2"              =>  VARS::post("STAFFNAME_SHOW2"),
                             "STAFFCD3"                     =>  VARS::post("STAFFCD3"),
                             "STAFFNAME_SHOW3"              =>  VARS::post("STAFFNAME_SHOW3"),
                             "STAFFCD4"                     =>  VARS::post("STAFFCD4"),
                             "STAFFNAME_SHOW4"              =>  VARS::post("STAFFNAME_SHOW4"),
                             "STAFFCD5"                     =>  VARS::post("STAFFCD5"),
                             "STAFFNAME_SHOW5"              =>  VARS::post("STAFFNAME_SHOW5"),
                             "STAFFCD6"                     =>  VARS::post("STAFFCD6"),
                             "STAFFNAME_SHOW6"              =>  VARS::post("STAFFNAME_SHOW6"),
                     );
        $this->data["STAFFCD"]  =  VARS::request("STAFFCD");
        for ($i = 0; $i < get_count($this->data["STAFFCD"]); $i++) {
            $this->fields["CHECK"][]            = VARS::post("CHECK"."_".$i);
            $this->fields["COUNT_CHECK1"][]     = VARS::post("COUNT_CHECK1"."_".$i);
            $this->fields["COUNT_CHECK2"][]     = VARS::post("COUNT_CHECK2"."_".$i);
            $this->fields["COUNT_CHECK3"][]     = VARS::post("COUNT_CHECK3"."_".$i);
            $this->fields["COUNT_CHECK4"][]     = VARS::post("COUNT_CHECK4"."_".$i);
            $this->fields["COUNT_CHECK5"][]     = VARS::post("COUNT_CHECK5"."_".$i);
        }

        //出欠コード
        $this->di_cd = array('4' => '病　欠',
                             '5' => '事故欠',
                             '6' => '無　届',
                             '2' => '出　停',
                             '3' => '忌　引');

        //学年
        $this->grade = array();
        $db = Query::dbCheckOut();
        $year = ($this->year) ? $this->year : CTRL_YEAR;
        if ($this->Properties["useSchool_KindField"] == "1") {
            $firstSchool_kind = $db->getOne(knjg045Query::getA023($this));
            $this->field["SCHOOL_KIND"] = ($this->field["SCHOOL_KIND"]) ? $this->field["SCHOOL_KIND"] : $firstSchool_kind;
        }
        $query = knjg045Query::getGrade($this, $year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->grade[$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();
        Query::dbCheckIn($db);

        //欠席状況
        foreach ($this->di_cd as $dkey => $dval) {
            foreach ($this->grade as $gkey => $gval) {
                $this->att_field[$dkey][$gkey] =  VARS::post("CNT_".$dkey."_".$gkey);
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->validate_row_cnt($this->field["NEWS"], (91 * 2)) > 5) {
            $this->setWarning("MSG915", "(記事は5行までです)");
            return false;
        }

        return true;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->cmd == "update") {
            if ($this->validateInput()) {
                knjg045Query::getDeleteQuery($this);
                knjg045Query::getInsertQuery($this);
            }
        } elseif ($this->cmd == "delete") {
            knjg045Query::getDeleteQuery($this, "delete");
        }
            //$this->setMessage("MSG201");
    }

    // 編集完了用のモデルを返します
    public function &getUpdateDetailModel()
    {
        $this->setDiaryDate =  VARS::post("setDiaryDate");
        $this->setStaffDiv  =  VARS::post("setStaffDiv");
        $this->setGradeHrClass =  VARS::post("setGradeHrClass");
        $this->setcounter =  VARS::post("setcounter");
        //入力チェック
        for ($i = 0; $i < $this->setcounter; $i++) {
            $this->fields["STAFFCD"][$i]          = VARS::post("STAFFCD"."_".$i);
            $this->fields["CHECK"][$i]            = VARS::post("CHECK"."_".$i);
            $this->fields["COUNT"][$i]            = VARS::post("COUNT"."_".$i);
            $this->fields["COUNT_CHECK1"][$i]     = VARS::post("COUNT_CHECK1"."_".$i);
            $this->fields["COUNT_CHECK2"][$i]     = VARS::post("COUNT_CHECK2"."_".$i);
            $this->fields["COUNT_CHECK3"][$i]     = VARS::post("COUNT_CHECK3"."_".$i);
            $this->fields["COUNT_CHECK4"][$i]     = VARS::post("COUNT_CHECK4"."_".$i);
            $this->fields["COUNT_CHECK5"][$i]     = VARS::post("COUNT_CHECK5"."_".$i);
        }
        knjg045Query::getUpdateDetail($this);
        //$this->setMessage("MSG201");

        if ($this->setStaffDiv === '1') {
            $this->cmd = 'kesseki-A';
        } elseif ($this->setStaffDiv === '2') {
            $this->cmd = 'chikoku-A';
        } elseif ($this->setStaffDiv === '3') {
            $this->cmd = 'soutai-A';
        } elseif ($this->setStaffDiv === '4') {
            $this->cmd = 'shuchou-A';
        } elseif ($this->setStaffDiv === '5') {
            $this->cmd = 'hoketsu-A';
        } elseif ($this->setStaffDiv === '6') {
            $this->cmd = 'etc_hoketsu-A';
        }
    }
}
