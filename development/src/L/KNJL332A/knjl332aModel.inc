<?php

require_once('for_php7.php');

class knjl332aModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;

    public function knjl332aModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;
        $this->examType = 1;

        $this->field = array("APPLICANTDIV"    =>  VARS::post("APPLICANTDIV"),     //受験校種
                             "TESTDIV"         =>  VARS::post("TESTDIV"),          //試験回
                             "PRINT_TYPE"      =>  VARS::post("PRINT_TYPE"),       //印刷種別
                             "SIZE"            =>  VARS::post("SIZE"),             //用紙サイズ
                             "SHDIV"           =>  VARS::post("SHDIV"),            //専併区分
                             "PASS_COURSE"     =>  VARS::post("PASS_COURSE"),      //合否
                             "ENT_COURSE"      =>  VARS::post("ENT_COURSE"),       //入学コース
                             "RECEPTNO_FROM"   =>  VARS::post("RECEPTNO_FROM"),    //受験番号from
                             "RECEPTNO_TO"     =>  VARS::post("RECEPTNO_TO"),      //受験番号to
                             "BLANK"           =>  VARS::post("BLANK"),            //在中なし
                             "COMMENT"         =>  VARS::post("COMMENT")           //在中文章
                     );

        $this->cmd = VARS::request("cmd");
    }
}
