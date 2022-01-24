<?php
/**
 * 定数定義
*/

class knja142cModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;//コントロールマスタ
    public $staffcd;//職員コード
    public $selectStudent;
    public $selectStudentLabel;
    public $schoolName;

    /**
     * メンバ変数の初期化
     */
    public function knja142cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"   => VARS::post("YEAR"), //年度
                             "DBNAME" => VARS::post("DBNAME")//DB変更に備えて
        );

        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");

        //学校を判断
        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne(knja142cQuery::getSchoolName());
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
