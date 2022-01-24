<?php

require_once('for_php7.php');

class knja050Model extends Model
{
    public $cmd;
    public $next_year;
    public $this_year;
    public $staffcd;
    public $sec_competence;
    public $syoribi;
    public $grd_syoribi;
    public $this_semester;
    public $min_semester;
    public $max_semestername;
    public $next_semester;
    public $model;
    public $grade;
    public $schoolName;
    public $secondarySchoolDiv;
    public $sotugyouSinkyu = array();
    public $setGrdNoDaichouNoOrder;

    public function knja050Model()
    {
    }

    public function init()
    {
        $this->field = array("WHICH_WAY"        => VARS::request("WHICH_WAY"),      //連番か学籍番号
                             "WHICH_WAY_DIV"    => VARS::request("WHICH_WAY_DIV"),  //連番取得順
                             "GRD_NO"           => VARS::request("GRD_NO"),         //連番開始番号
                             "E_APPDATE"        => VARS::request("E_APPDATE")       //変更開始日
                             );

        $this->this_semester    = CTRL_SEMESTER;                    //現在処理学期
        $this->this_year        = CTRL_YEAR;                        //現在年度
        $this->next_year        = CTRL_YEAR+1;                      //設定年度
        $this->last_year        = CTRL_YEAR-1;                      //昨年度

        $this->school_kind = VARS::request("SCHOOL_KIND");
        $this->grade = VARS::request("GRADE");
        $this->mode  = VARS::request("MODE");
        $this->change_grade = VARS::post("change_grade");

        $db = Query::dbCheckOut();

        $query  = knja050Query::getSemesterRange(CTRL_YEAR);
        $result = $db->query($query);
        $row    = $result->fetchRow(DB_FETCHMODE_ASSOC);

        $this->max_semester = $row["MAX_SEMESTER"];         //処理年度MAX学期
        $this->min_semester = $row["MIN_SEMESTER"];         //処理年度MIN学期

        $query  = knja050Query::getNextMinSemester($this->next_year);
        $getNextSemester = $db->getOne($query);

        $this->next_semester = $getNextSemester;        //設定年度MIN学期

        $query  = knja050Query::getSchInformation(CTRL_YEAR);
        $result = $db->query($query);
        $row    = $result->fetchRow(DB_FETCHMODE_ASSOC);

        $this->schooldiv    = $row["SCHOOLDIV"];            //学校区分
        $this->grade_hval   = $row["grade_hval"];           //学年数上限
        $this->foundedyear  = $row["FOUNDE"];               //創立年度
        $this->term         = (int)CTRL_YEAR - $this->foundedyear - 1;

        //学校情報
        $query  = knja050Query::getNameMstA023($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->sotugyouSinkyu[] = $row["NAME2"];
        }

        $query  = knja050Query::getNameMstZ010();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName           = $rtnRow["NAME1"];     // 学校区分
        $this->secondarySchoolDiv   = $rtnRow["NAMESPARE2"];// 1:併設校 2:中等教育学校

        $query  = knja050Query::checkSchregAddressDat();
        $this->schregAddressDatColumns = $db->getCol($query);

        Query::dbCheckIn($db);

        $this->max_semestername   = knja050Query::getMaxSemester($this);   //MAX学期名

        $this->cmd = VARS::request("cmd");

        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck(STAFFCD, PROGRAMID);

        if (VARS::request("PROGRAMID")!="") {
            unset($this->syoribi);
            unset($this->grd_syoribi);
        }

        if (VARS::request("syoribi")!="") {
            $this->syoribi = VARS::request("syoribi");
        }
        if (VARS::request("grd_syoribi")!="") {
            $this->grd_syoribi = VARS::request("grd_syoribi");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //卒業生台帳番号採番のソート順設定
        $this->setGrdNoDaichouNoOrder[1] = array("name" => "卒業日",   "column" => "T1.GRD_DATE");
        $this->setGrdNoDaichouNoOrder[2] = array("name" => "年組番",   "column" => "T2.GRADE:T2.HR_CLASS:T2.ATTENDNO");
        $this->setGrdNoDaichouNoOrder[3] = array("name" => "学科",     "column" => "T2.COURSECD:T2.MAJORCD");
        $this->setGrdNoDaichouNoOrder[4] = array("name" => "氏名かな", "column" => "SET_NAME_KANA");
    }

    //台帳番号だけ更新
    public function &getOneUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knja050Query::updDivNull($db, $this);

        knja050Query::getSchregno($db, $this, $this->schooldiv, $this->field["WHICH_WAY"], $this->field["WHICH_WAY_DIV"], $this->this_semester, $this->field["GRD_NO"], $this->mode, $this->schoolName);

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    //実行
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        if (0 < $db->getOne(knja050Query::checkClassForm($this))) {
            $this->setWarning("MSG203", "進級・留年生に卒業年月日が入っています。");
            return false;
        }
        Query::dbCheckIn($db);

        if (knja050Query::getUpdateQuery($this)) {
            $this->setMessage("MSG201");
        }
    }
}
