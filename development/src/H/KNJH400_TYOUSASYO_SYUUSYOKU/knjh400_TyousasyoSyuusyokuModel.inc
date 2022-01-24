<?php

require_once('for_php7.php');

class knjh400_TyousasyoSyuusyokuModel extends Model
{
    public $cmd;
    public $schregno;    //primary
    public $grade;
    public $hr_class;
    public $name;
    public $attendno;
    public $field = array();
    public $check;
    public $annual;                    //学年(年次)
    public $mode;                      //在籍卒業モード
    public $grd_year;                  //卒業年度
    public $grd_semester;              //卒業学期
    public $exp_year;       //左フレームから送られてくる年度
    public $exp_semester;   //左フレームから送られてくる学期
    public $schoolName;
    public $schoolCode;
    public $remarkValue;
    public $useQualifiedMst; //資格マスタを使うのかのフラグ
    public $getPrgId;  //コールされたか
    public $gyou;
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $selectPattern;
    public $referPattern;
    public $maxPattern;

    public function knjh400_TyousasyoSyuusyokuModel()
    {
        $this->year = CTRL_YEAR;

        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        $this->schregno = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->schregno;

        $this->year = VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->year;
        $this->exp_year = $this->year;

        $this->semester = VARS::request("SEMESTER") != "" ? VARS::request("SEMESTER") : $this->semester;
        $this->exp_semester = $this->semester;

        $this->top["GYOUSYA"] = VARS::post("GYOUSYA");

        if (VARS::request("GRADE") !="") {
            $this->grade           = VARS::request("GRADE");
        }
        if (VARS::get("NAME")!="") {
            $this->name            = VARS::get("NAME");
            $this->attendno        = VARS::get("ATTENDNO");
        }
        if (VARS::post("ANNUAL")) {   //学年（年次）
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->field = array("JOBHUNT_REC"          =>  VARS::post("jobhunt_rec"),           //就職用特別活動記録
                             "JOBHUNT_ABSENCE"      =>  VARS::post("jobhunt_absence"),       //就職用欠席理由
                             "JOBHUNT_HEALTHREMARK" =>  VARS::post("jobhunt_healthremark"),  //就職用身体状況備考
                             "JOBHUNT_RECOMMEND"    =>  VARS::post("jobhunt_recommend"),     //就職用推薦事由
                             "UPDATED"              =>  VARS::post("updated"));              //更新日付
        $this->check = VARS::post("CHECK");

        //在籍卒業モード
        if (VARS::get("mode")) {
            $this->mode = VARS::get("mode");             //在籍卒業モード
        }
        if (VARS::get("GRD_YEAR")) {
            $this->grd_year = VARS::get("GRD_YEAR");           //卒業年度
        }
        if (VARS::get("GRD_SEMESTER")) {
            $this->grd_semester = VARS::get("GRD_SEMESTER");           //卒業学期
        }

        $this->selectPattern    = VARS::post("SELECT_PATTERN");
        if ($this->selectPattern == "" && VARS::get("SELECT_PATTERN")) {
            $this->selectPattern = VARS::get("SELECT_PATTERN");
        }
        $this->referPattern     = VARS::post("REFER_PATTERN");

        //生徒一覧を更新
        if (VARS::get("init")) {
            unset($this->schregno);     //学籍番号
            unset($this->name);         //生徒氏名
            unset($this->attendno);     //出席番号
        }

        //学校名取得
        $query = knjh400_TyousasyoSyuusyokuQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];

        //「特記事項なし」とか「異常なし」とか備考のところの値を名称マスタより取得
        $query = knjh400_TyousasyoSyuusyokuQuery::getRemarkValue();
        $this->remarkValue = $db->getOne($query);
        Query::dbCheckIn($db);
        if (!$this->remarkValue) {
            $this->remarkValue = "異常なし";
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->tyousasyoEMPTokuBetuFieldSize = (strlen($this->Properties["tyousasyoEMPTokuBetuFieldSize"])) ? $this->Properties["tyousasyoEMPTokuBetuFieldSize"] : 0;
        $this->useQualifiedMst            = (strlen($this->Properties["useQualifiedMst"]))            ? $this->Properties["useQualifiedMst"] : 0;

        $this->gyou = array();
        if ($this->schoolName == 'miyagiken') {
            $this->gyou["JOBHUNT_RECOMMEND"]["GYOU"] = 16;
            $this->gyou["JOBHUNT_RECOMMEND"]["MOJI"] = 38;
        } elseif ($this->tyousasyoEMPTokuBetuFieldSize == 1) {
            $this->gyou["JOBHUNT_RECOMMEND"]["GYOU"] = 13;
            $this->gyou["JOBHUNT_RECOMMEND"]["MOJI"] = 38;
        } else {
            $this->gyou["JOBHUNT_RECOMMEND"]["GYOU"] = 10;
            $this->gyou["JOBHUNT_RECOMMEND"]["MOJI"] = 38;
        }

        if ($this->tyousasyoEMPTokuBetuFieldSize == 1) {
            $this->gyou["JOBHUNT_REC"]["GYOU"] = 10;
            $this->gyou["JOBHUNT_REC"]["MOJI"] = 20;
        } else {
            $this->gyou["JOBHUNT_REC"]["GYOU"] = 8;
            $this->gyou["JOBHUNT_REC"]["MOJI"] = 20;
        }

        if ($this->tyousasyoEMPTokuBetuFieldSize == 1) {
            $this->gyou["JOBHUNT_ABSENCE"]["GYOU"] = 6;
            $this->gyou["JOBHUNT_ABSENCE"]["MOJI"] = 10;
        } else {
            $this->gyou["JOBHUNT_ABSENCE"]["GYOU"] = 4;
            $this->gyou["JOBHUNT_ABSENCE"]["MOJI"] = 10;
        }

        $this->gyou["JOBHUNT_HEALTHREMARK"]["GYOU"] = 3;
        $this->gyou["JOBHUNT_HEALTHREMARK"]["MOJI"] = 14;

        if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $this->maxPattern = 3;
        }
    }

    public function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knjh400_TyousasyoSyuusyokuQuery::getProgrampath());
        Query::dbCheckIn($db);

        return $programpath;
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }
}
