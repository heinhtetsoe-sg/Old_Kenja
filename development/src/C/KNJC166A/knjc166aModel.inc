<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjc166aModel extends Model
{
    public $cmd;
    public $setDefDate;
    public $base_date;
    public $gradeOrHr;
    public $selClass;

    /**
     * メンバ変数の初期化
     */
    public function knjc166aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->setDefDate);
            unset($this->base_date);
        }
        $this->cmd   = VARS::request("cmd");
        $this->grade = VARS::request("GRADE");
        $this->base_date = VARS::post("BASE_DATE");
        $this->gradeOrHr = VARS::post("GRADE_OR_HR");
        $this->selClass = array();
        if ($this->gradeOrHr == "2") {
            $this->selClass = explode(",", VARS::post("SELECT_DATA"));
        }
    }

    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        $db = Query::dbCheckOut();

        //基準日時点の学期を取得
        $query = knjc166aQuery::getSemesBaseDate($this);
        $this->baseDateSemester = $db->getOne($query);

        Query::dbCheckIn($db);

        if ($this->gradeOrHr == "2") {
            $this->selClass = explode(",", VARS::post("SELECT_DATA"));
        }
        knjc166aQuery::update($this);
        $this->setMessage("MSG202");
    }
}
