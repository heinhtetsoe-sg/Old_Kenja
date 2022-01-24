<?php

require_once('for_php7.php');

class knjh530Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjh530Model()
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
                             "YEAR_ADD"         =>  VARS::post("YEAR_ADD"),        //年度追加
                             "SEMESTER"         =>  VARS::post("SEMESTER"),        //学期
                             "GRADE"            =>  VARS::post("GRADE"),           //学年
                             "COPY_YEAR"        =>  VARS::post("COPY_YEAR"),       //コピー先年度
                             "PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),  //データ種別
                             "DBNAME"           =>  VARS::post("DBNAME")           //DB変更に備えて
                            );
        //マスタから戻ってきた
        if (VARS::get("year_code")) {
            $this->field["YEAR"]            = VARS::get("year_code");
            $this->field["SEMESTER"]        = VARS::get("semester_code");
            $this->field["GRADE"]           = VARS::get("grade_code");
            $this->field["PROFICIENCYDIV"]  = VARS::get("proficiencydiv_code");
            $this->field["YEAR_ADD"]        = VARS::get("year_add_code");
        }

        $this->proficiencyCd = VARS::post("SELECT_DATA");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データ削除
        $db->query(knjh530Query::makeDelProficiencyQuery($this));
        //データ作成
        knjh530Query::getProficiencyInsQuery($db, $this->proficiencyCd, $this);

        $db->commit();
        Query::dbCheckIn($db);

        $this->field["YEAR_ADD"] = "";
        $this->setMessage("MSG201");

        return;
    }

    //更新
    function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データ作成
        $delQuery = knjh530Query::getProficiencyCopyDelQuery($this);
        $db->query($delQuery);
        $copyQuery = knjh530Query::getProficiencyCopyQuery($this);
        $db->query($copyQuery);

        $db->commit();
        Query::dbCheckIn($db);

        $this->field["YEAR"] = $this->field["COPY_YEAR"];
        $this->field["COPY_YEAR"] = "";
        $this->setMessage("MSG201");

        return;
    }

    function clean()
    {
    }
}
?>
