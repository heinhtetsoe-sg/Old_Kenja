<?php

class knjl429iModel extends Model {

    var $schoolKind;
    var $cmd;
    var $field = array();

    /**
     * メンバ変数の初期化
     */
    function knjl429iModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->schoolKind = VARS::get("URL_SCHOOLKIND");
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        
        $this->field = array("DISP"             =>  VARS::post("DISP"),              //抽出区分 1:全員 2:合格者のみ
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期コード
                             "FORMDIV"         =>  VARS::post("FORMDIV"),          //出力内容
                             "DBNAME"           =>  VARS::post("DBNAME"),             //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //入試制度
        if (VARS::post("APPLICANTDIV")) {
            $this->field["APPLICANTDIV"] = VARS::post("APPLICANTDIV");
        }
        //入試区分
        if (VARS::post("TESTDIV")) {
            $this->field["TESTDIV"] = VARS::post("TESTDIV");
        }
        
        //入試区分
        if (VARS::post("EXAMHALLCD")) {
            $this->field["EXAMHALLCD"] = VARS::post("EXAMHALLCD");
        }

    }

}
?>
