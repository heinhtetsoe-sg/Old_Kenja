<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjg100Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $select_opt  = array(); //セレクトデータ
    var $selectleft;

    /**
     * メンバ変数の初期化
     */
    function knjg100Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("SCHKIND"               =>  VARS::post("SCHKIND"),              //校種
                             "YEAR"                  =>  VARS::post("YEAR"),                 //年度
                             "GAKKI"                 =>  VARS::post("GAKKI"),                //学期コード
                             "KUBUN"                 =>  VARS::post("KUBUN"),                //クラス・個人表示指定
                             "GRADE"                 =>  VARS::post("GRADE"),                //学年
                             "GRADE_HR_CLASS"        =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "KISAI_DATE"            =>  VARS::post("KISAI_DATE"),           //記載日付
                             "DBNAME"                =>  VARS::post("DBNAME")                //DB変更に備えて
                             );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') $this->select_opt = array();

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
