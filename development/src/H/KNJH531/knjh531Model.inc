<?php
class knjh531Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjh531Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        //初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->proficiencyCd);
        }

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),            //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),        //学期
                             "GRADE"            =>  VARS::post("GRADE"),           //学年
                             "PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),  //データ種別
                             "DBNAME"           =>  VARS::post("DBNAME")           //DB変更に備えて
                            );
        //マスタから戻ってきた
        if (VARS::get("year_code")) {
            $this->field["YEAR"]            = VARS::get("year_code");
            $this->field["SEMESTER"]        = VARS::get("semester_code");
            $this->field["GRADE"]           = VARS::get("grade_code");
            $this->field["PROFICIENCYDIV"]  = VARS::get("proficiencydiv_code");
        }

        $this->proficiencyCd = VARS::post("SELECT_DATA");
    }

    //更新
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データ削除
        $db->query(knjh531Query::getUpdateFlgClearQuery($this));
        //データ作成
        knjh531Query::getProficiencyInsQuery($db, $this->proficiencyCd, $this);

        $db->commit();
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");

        return;
    }

    function clean()
    {
    }
}
?>
