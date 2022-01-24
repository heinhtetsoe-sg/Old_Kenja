<?php

require_once('for_php7.php');

class knjd181jModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;
    public $selectdata;
    public $maxSemester;

    public function knjd181jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "DESC_DATE"        =>  VARS::post("DESC_DATE"),        //記載日付
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "PRINT_SIDE1"      =>  VARS::post("PRINT_SIDE1"),      //1.表紙
                             "PRINT_SIDE2"      =>  VARS::post("PRINT_SIDE2"),      //2.成績
                             "PRINT_SIDE3"      =>  VARS::post("PRINT_SIDE3"),      //3.所見
                             "PRINT_SIDE4"      =>  VARS::post("PRINT_SIDE4"),      //4.修了証
                             "NOT_PRINT_ATTEND" =>  VARS::post("NOT_PRINT_ATTEND"), //出欠の記録を出力しない
                             "NOTOUTPUT_EDUTARGET" =>  VARS::post("NOTOUTPUT_EDUTARGET"), //教育目標印字なし(京都のみ)
                             "OUINRAN_SHUTURYOKU"  =>  VARS::post("OUINRAN_SHUTURYOKU"),  //押印欄を出力する
                             "SHOKEN_SHUTURYOKU"   =>  VARS::post("SHOKEN_SHUTURYOKU"),   //所見を表示しない
                             "DBNAME"              =>  VARS::post("DBNAME")               //DB変更に備えて
                     );

        //学期制取得
        $db = Query::dbCheckOut();
        $this->maxSemester = $db->getOne(knjd181jQuery::getMaxSemester());
        Query::dbCheckIn($db);

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");
        $this->getPropertiesAll();
    }
}
