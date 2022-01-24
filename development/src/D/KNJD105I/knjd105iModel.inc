<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd105iModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd105iModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "selected"             =>  VARS::post("selected"),             //選択対象
                             "COMMENT1"             =>  VARS::post("COMMENT1"),             //コメント
                             "COMMENT2"             =>  VARS::post("COMMENT2"),             //コメント
                             "COMMENT3"             =>  VARS::post("COMMENT3"),             //コメント
                             "COMMENT4"             =>  VARS::post("COMMENT4"),             //コメント
                             "COMMENT5"             =>  VARS::post("COMMENT5"),             //コメント
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();

        //学校名取得
        $query = knjd105iQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    function clean() {}

    //入力項目チェック
    function validateInput()
    {
        for ($i = 1; $i <= 5; $i++) {
            if (strlen($this->field["COMMENT".$i]) > 210) {
                $this->setWarning("MSG915"."(コメント".$i."210バイトまでです)");
                return false;
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
    }

    function &update()
    {
        knjd105iQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
