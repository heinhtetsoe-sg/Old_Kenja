<?php

require_once('for_php7.php');

class knjl571aModel extends model {
    var $field = array();
    var $sort  = array();
    var $cmd;
    var $applicantdiv;
    var $ObjYear;
    var $hid_examno;
    var $getSort;

    function knjl571aModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->getSort);
            //ソート処理の初期化
            $this->sort["SRT_POINT"] = 1;
            $this->sort["SRT_NAISIN5"] = 1;
            $this->sort["SRT_NAISIN9"] = 1;
            $this->sort["SRT_PROMISED"] = 1;
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->field = array("TESTDIV"            =>  VARS::post("TESTDIV"),            //受験区分
                             "DESIREDIV"          =>  VARS::post("DESIREDIV"),          //志望区分
                             "SIBORI"             =>  VARS::post("SIBORI"),             //絞り込み分類 1:得点 2:内申
                             "POINT_FROM"         =>  VARS::post("POINT_FROM"),         //得点 FROM
                             "POINT_TO"           =>  VARS::post("POINT_TO"),           //得点 TO
                             "IGNORE_GOUHI_LEFT"  =>  VARS::post("IGNORE_GOUHI_LEFT"),  //合否入力済みを除く(得点)
                             "SEL_TOTAL"          =>  VARS::post("SEL_TOTAL"),          //内申合計 1:5科合計 2:9科合計
                             "TOTAL5_FROM"        =>  VARS::post("TOTAL5_FROM"),        //5科合計 FROM
                             "TOTAL5_TO"          =>  VARS::post("TOTAL5_TO"),          //5科合計 TO
                             "TOTAL9_FROM"        =>  VARS::post("TOTAL9_FROM"),        //9科合計 FROM
                             "TOTAL9_TO"          =>  VARS::post("TOTAL9_TO"),          //9科合計 TO
                             "IGNORE_GOUHI_RIGHT" =>  VARS::post("IGNORE_GOUHI_RIGHT"), //合否入力済みを除く(内申)
                             "INPUT"              =>  VARS::post("INPUT"),              //入力項目 1:合否 2:奨学生 3:特進勧誘
                             "CHECKALL"           =>  VARS::post("CHECKALL")            //
                             );

        if (VARS::get("sort")) {
            $this->getSort = VARS::get("sort");
            $this->sort[$this->getSort] = $this->sort[$this->getSort] == 1 ? 2 : 1;
            $this->field = array("TESTDIV"            =>  VARS::get("TESTDIV"),            //受験区分
                                 "DESIREDIV"          =>  VARS::get("DESIREDIV"),          //志望区分
                                 "SIBORI"             =>  VARS::get("SIBORI"),             //絞り込み分類 1:得点 2:内申
                                 "POINT_FROM"         =>  VARS::get("POINT_FROM"),         //得点 FROM
                                 "POINT_TO"           =>  VARS::get("POINT_TO"),           //得点 TO
                                 "IGNORE_GOUHI_LEFT"  =>  VARS::get("IGNORE_GOUHI_LEFT"),  //合否入力済みを除く(得点)
                                 "SEL_TOTAL"          =>  VARS::get("SEL_TOTAL"),          //内申合計 1:5科合計 2:9科合計
                                 "TOTAL5_FROM"        =>  VARS::get("TOTAL5_FROM"),        //5科合計 FROM
                                 "TOTAL5_TO"          =>  VARS::get("TOTAL5_TO"),          //5科合計 TO
                                 "TOTAL9_FROM"        =>  VARS::get("TOTAL9_FROM"),        //9科合計 FROM
                                 "TOTAL9_TO"          =>  VARS::get("TOTAL9_TO"),          //9科合計 TO
                                 "IGNORE_GOUHI_RIGHT" =>  VARS::get("IGNORE_GOUHI_RIGHT"), //合否入力済みを除く(内申)
                                 "INPUT"              =>  VARS::get("INPUT"),              //入力項目 1:合否 2:奨学生 3:特進勧誘
                                 "CHECKALL"           =>  VARS::get("CHECKALL")            //
                                 );
        }

        $this->hid_examno       = VARS::post("HID_EXAMNO");

        $this->data = array();
        $array       = explode(",", $this->hid_examno);
        foreach ($array as $key => $examno) {
            $this->data["CHECKED"][$examno]                 = strlen(VARS::post("HID_CHECKED_".$examno)) ? VARS::post("HID_CHECKED_".$examno) : VARS::post("CHECKED_".$examno);
            $this->data["PASS_UNPASS"][$examno]         = strlen(VARS::post("HID_PASS_UNPASS_".$examno)) ? VARS::post("HID_PASS_UNPASS_".$examno) : VARS::post("PASS_UNPASS_".$examno);
            $this->data["SCHOLARSHIP_STUDENT"][$examno] = strlen(VARS::post("HID_SCHOLARSHIP_STUDENT_".$examno)) ? VARS::post("HID_SCHOLARSHIP_STUDENT_".$examno) : VARS::post("SCHOLARSHIP_STUDENT_".$examno);
            $this->data["SP_SOLICIT"][$examno]          = strlen(VARS::post("HID_SP_SOLICIT_".$examno)) ? VARS::post("HID_SP_SOLICIT_".$examno) : VARS::post("SP_SOLICIT_".$examno);
        }
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //更新
        knjl571aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
