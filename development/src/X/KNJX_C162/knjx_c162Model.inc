<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx_c162Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード

    /**
     * メンバ変数の初期化
     */
    public function knjx_c162Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SDATE"            =>  VARS::post("SDATE"),            //対象日(開始)
                             "EDATE"            =>  VARS::post("EDATE"),            //対象日(終了)
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "TYUI_TYOUKA"      =>  VARS::post("TYUI_TYOUKA"),      //注意・超過ラジオボタン
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                             );

        $this->cmd          = VARS::request("cmd");
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        
        if (VARS::get("SEND_PRGID")) {
            $db = Query::dbCheckOut();
            $this->field["SEMESTER"]        = (VARS::get("SEND_SEMESTER")) ? VARS::get("SEND_SEMESTER") : $this->field["SEMESTER"];
            $this->field["GRADE"]           = (VARS::get("SEND_GRADE")) ? VARS::get("SEND_GRADE") : $this->field["GRADE"];
            $this->field["SCHOOL_KIND"]     = $db->getOne(knjx_c162Query::getSchoolKind($this->field["GRADE"]));
            Query::dbCheckIn($db);
        }
    }
    public function &getMainModel()
    {
    }
}
