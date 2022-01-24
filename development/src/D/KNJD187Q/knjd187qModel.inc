<?php
/**
 * 定数定義
*/

class knjd187qModel extends Model
{
    public $field      = array();
    public $select_data      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    public function knjd187qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    public function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト名
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "PRINT_ZENKI"      =>  VARS::post("PRINT_ZENKI"),      //前期科目のみ出力
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
