<?php
/**
 * 定数定義
*/

class knje013aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $selectdata;
    public $fromSeq;
    public $toSeq;
        
    /**
     * メンバ変数の初期化
     */
    public function knje013aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    public function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "CHOICE"               =>  VARS::post("CHOICE"),               //出力指定
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "IDOU_DATE"            =>  VARS::post("IDOU_DATE"),            //異動日
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );
        $this->fromSeq = "101";
        $this->toSeq   = "106";
        $this->cmd         = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
    }

    //コピー
    public function &getUpdateModel()
    {
        knje013aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
