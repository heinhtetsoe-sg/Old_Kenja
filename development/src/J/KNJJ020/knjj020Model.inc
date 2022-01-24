<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj020Model extends Model {
    var $cmd;
    var $selectdata;
    var $staffcd;       //職員コード
    /**
     * メンバ変数の初期化
     */
    function knjj020Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }
    function init() {
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),     //年度
                             "SCHKIND"           =>  VARS::post("SCHKIND"),  //校種
                             "GAKKI"             =>  VARS::post("GAKKI"),    //学期
                             "CLUBCD"            =>  VARS::post("CLUBCD"),   //部クラブコード
                             "DBNAME"            =>  VARS::post("DBNAME")    //DB変更に備えて
                             );

        if(!isset($this->field['YEAR'])){
            $this->field['YEAR'] = CTRL_YEAR;
        }
        $this->year = $this->field['YEAR'];

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    //入力項目チェック
    function validateInput() {
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        } 
    }
         
    /**
     * アップデート時の内部メソッド
     */
    function &update() {
        $result = knjj020Query::getUpdateQuery($this);
        if ( !isset($model->warning)) {
            $this->setWarning('MSG201');
        }
        return $result;
    }

    /**
     * 次年度へのコピー（部クラブコンボで「－全て－」を選択時）
     */
    function &getCopyModel(){
        $ret_val = knjj020Query::cntClubAdviserDatNextYear($this);
        if ($ret_val) {
            $this->setMessage("MSG203", "次年度のデータが既に存在しています。");
            return false;
        }

        knjj020Query::getCopyQuery($this);
        $this->setMessage('MSG201');
    }
}
?>
