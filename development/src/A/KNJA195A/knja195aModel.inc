<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja195aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $select_opt  = array(); //セレクトデータ
    var $selectleft;
    var $schoolName;

    /**
     * メンバ変数の初期化
     */
    function knja195aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("KUBUN"            =>  VARS::post("KUBUN"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                             "G_YEAR"           =>  VARS::post("G_YEAR"),
                             "KANJI"            =>  VARS::post("KANJI"),
                             "KANA"             =>  VARS::post("KANA"),
                             "OUTPUT"           =>  VARS::post("OUTPUT"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') $this->select_opt = array();

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        
        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne(knja195aQuery::getNameMst("Z010", "00", "NAME1"));
        Query::dbCheckIn($db);
        
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
