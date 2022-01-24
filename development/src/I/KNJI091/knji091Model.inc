<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knji091Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $leftlistschno;     //生徒の学籍番号保管（左リスト）
    var $rightlistschno;    //生徒の学籍番号保管（右リスト）

    /**
     * メンバ変数の初期化
     */
    function knji091Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),  //校種
                             "GR_CL"            =>  VARS::post("GR_CL"),        //年組
                             "GAKKI"            =>  VARS::post("GAKKI"),        //学期コード
                             "DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
        $this->leftlistschno    = VARS::post("left_list_schno");
        $this->rightlistschno   = VARS::post("right_list_schno");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新処理
    function &getUpdateModel()
    {
        $this->update();
        $this->setMessage("MSG201");
    }
    function &update()
    {
        //学籍番号でKIN_GRD_LEDGER_SETUP_DATを一旦削除(DELETE)し、追加(INSERT)
        knji091Query::getDeleteQuery($this);
        knji091Query::getInsertQuery($this);
        return $result;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
