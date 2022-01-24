<?php

require_once('for_php7.php');


/**
 * 定数定義
*/
class knjh539aModel extends Model {
    var $cmd;
    var $control;
    var $z010;

    /**
     * メンバ変数の初期化
     */
    function knjh539aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->field["SEMESTER"]        = VARS::post("SEMESTER");       //学期
        $this->field["GRADE"]           = VARS::post("GRADE");          //学年
        $this->field["PROFICIENCYDIV"]  = VARS::post("PROFICIENCYDIV"); //区分
        $this->field["PROFICIENCYCD"]   = VARS::post("PROFICIENCYCD");  //テスト
        $this->rankDivArray = array("01" => "GRADE", "02" => "CLASS", "03" => "COURSE", "04" => "MAJORCD", "05" => "GROUP_CD");
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        $db = Query::dbCheckOut();
        $this->z010 = $db->getOne(knjh539aQuery::getZ010());
        Query::dbCheckIn($db);

        knjh539aQuery::executeRireki($this); //実行履歴

        if ($this->Properties["useProficiencyWeighting"] == "1") {
            $dataDiv = array(1, 2);
        } else {
            $dataDiv = array(1);
        }
        foreach ($dataDiv as $key => $val) {
            knjh539aQuery::UpdateProficiencyRankDat($this, $val);
            knjh539aQuery::UpdateProficiencyAverageDat($this, $val);
        }
        $this->setMessage("MSG202");
    }
}
?>
