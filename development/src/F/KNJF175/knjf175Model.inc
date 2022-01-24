<?php

require_once('for_php7.php');

class knjf175Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $isKumamoto;

    public function knjf175Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("SCHKIND"          =>  VARS::post("SCHKIND"),  //校種
                             "SDATE"            =>  VARS::post("SDATE"),    //対象日(開始)
                             "EDATE"            =>  VARS::post("EDATE"),    //対象日(終了)
                             "PRINT"            =>  VARS::post("PRINT"),    //欠席者一覧印刷
                             "SIKIBETU_PTRN"    =>  VARS::post("SIKIBETU_PTRN"),    //生徒識別
                             "DBNAME"           =>  VARS::post("DBNAME")    //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjf175Query::getZ010();
        $name1 = $db->getOne($query);

        $this->isKumamoto = $name1 == "kumamoto" ? true : false;

        //DB切断
        Query::dbCheckIn($db);

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function clean()
    {
    }
}
