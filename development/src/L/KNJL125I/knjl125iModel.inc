<?php
class knjl125iModel extends model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $applicantdiv;
    public $examyear;
    public $hid_examno;
    public $hid_classcd;

    public function knjl125iModel()
    {
    }

    public function init()
    {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
        }

        $this->examyear      = CTRL_YEAR+1;
        $this->applicantdiv = "2";  //高校固定

        $this->cmd = VARS::request("cmd");


        $this->field = array("TESTDIV"      =>  VARS::post("TESTDIV"),      //受験区分
                             "SORT"         =>  VARS::post("SORT"),         //表示順
                             "DISP_DIV"     =>  VARS::post("DISP_DIV"),     //抽出
                             "JUDGEDIV"     =>  VARS::post("JUDGEDIV"),     //合否コンボ
                             );

        $this->checkAll = VARS::post("CHECKALL");

        if (isset($this->recept_arr)) {
            foreach ($this->recept_arr as $receptno) {
                $this->checkVal[$receptno] = VARS::post("CHECK_".$receptno);
            }
        }
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjl125iQuery::updateQuery($db, $this);

        $db->commit();
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }
}
