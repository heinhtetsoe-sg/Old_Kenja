<?php
class knjl120iModel extends model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $applicantdiv;
    public $ObjYear;
    public $hid_examno;
    public $hid_classcd;

    public function knjl120iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
        }

        $this->examyear      = CTRL_YEAR+1;
        $this->applicantdiv = "2";  //固定値

        $this->cmd = VARS::request("cmd");


        $this->field = array("TESTDIV"      =>  VARS::post("TESTDIV"),      //受験区分
                             "DISP_DIV"     =>  VARS::post("DISP_DIV"),    //志望区分
                             "S_EXAMNO"     =>  VARS::post("S_EXAMNO"),     //受験番号 FROM
                             "E_EXAMNO"     =>  VARS::post("E_EXAMNO"),     //受験番号 TO
                             );

        $this->hid_examno       = VARS::post("HID_EXAMNO");
        if (VARS::post("yomikomiFlg") != "") {
            $this->yomikomiFlg = VARS::post("yomikomiFlg");
        }

        $array       = explode(",", $this->hid_examno);

        //ポイントマスタ取得
        $this->ajaxTesiDiv = VARS::post("AJAX_TESTDIV");
        $this->ajaxPointCd  = VARS::post("AJAX_POINTCD");
        $this->ajaxInputVal = VARS::post("AJAX_INPUT_VAL");
    }

    public function &getMainModel()
    {
    }

    //値セット
    public function setMainFieldValue()
    {
        foreach ($this->arr_examno as $examno) {
            $this->mainField[$examno]["SCORE1"]         =  VARS::post("SCORE1_{$examno}");
            $this->mainField[$examno]["SCORE2"]         =  VARS::post("SCORE2_{$examno}");
            $this->mainField[$examno]["SCORE3"]         =  VARS::post("SCORE3_{$examno}");
            $this->mainField[$examno]["SCORE4"]         =  VARS::post("SCORE4_{$examno}");
            $this->mainField[$examno]["SCORE5"]         =  VARS::post("SCORE5_{$examno}");
            $this->mainField[$examno]["SCORE6"]         =  VARS::post("SCORE6_{$examno}");
            $this->mainField[$examno]["SCORE7"]         =  VARS::post("SCORE7_{$examno}");
            $this->mainField[$examno]["SCORE8"]         =  VARS::post("SCORE8_{$examno}");
            $this->mainField[$examno]["SCORE9"]         =  VARS::post("SCORE9_{$examno}");
            $this->mainField[$examno]["TOTAL_ALL"]      =  VARS::post("TOTAL_ALL_{$examno}");
            $this->mainField[$examno]["TOTAL3"]         =  VARS::post("TOTAL3_{$examno}");
            $this->mainField[$examno]["ABSENCE_DAYS"]   =  VARS::post("ABSENCE_DAYS_{$examno}");
            $this->mainField[$examno]["ABSENCE_DAYS2"]  =  VARS::post("ABSENCE_DAYS2_{$examno}");
            $this->mainField[$examno]["ABSENCE_DAYS3"]  =  VARS::post("ABSENCE_DAYS3_{$examno}");

            $this->mainField[$examno]["TOTAL_HYOUTEI"]  =  VARS::post("TOTAL_HYOUTEI_{$examno}");
            $this->mainField[$examno]["ACHIEVEMENT"]    =  VARS::post("ACHIEVEMENT_{$examno}");
            $this->mainField[$examno]["REPORT_PLUS"]    =  VARS::post("REPORT_PLUS_{$examno}");
            $this->mainField[$examno]["REPORT_MINUS"]   =  VARS::post("REPORT_MINUS_{$examno}");
            $this->mainField[$examno]["SELF_REC_PLUS"]  =  VARS::post("SELF_REC_PLUS_{$examno}");
            $this->mainField[$examno]["SELF_REC_MINUS"] =  VARS::post("SELF_REC_MINUS_{$examno}");
            $this->mainField[$examno]["DOUSOU_PLUS"]    =  VARS::post("DOUSOU_PLUS_{$examno}");
            $this->mainField[$examno]["QUALIFIED_ENG"]  =  VARS::post("QUALIFIED_ENG_{$examno}");
            $this->mainField[$examno]["TYOUSEI_PLUS"]   =  VARS::post("TYOUSEI_PLUS_{$examno}");
            $this->mainField[$examno]["TYOUSEI_MINUS"]  =  VARS::post("TYOUSEI_MINUS_{$examno}");

            $this->mainField[$examno]["TOTAL9_PLUS"]          =  VARS::post("TOTAL9_PLUS_{$examno}");
            $this->mainField[$examno]["TOTAL9_MINUS"]         =  VARS::post("TOTAL9_MINUS_{$examno}");
            $this->mainField[$examno]["TOTAL3_PLUS"]          =  VARS::post("TOTAL3_PLUS_{$examno}");
            $this->mainField[$examno]["TOTAL3_MINUS"]         =  VARS::post("TOTAL3_MINUS_{$examno}");
            $this->mainField[$examno]["ABSENCE_TOTAL_PLUS"]   =  VARS::post("ABSENCE_TOTAL_PLUS_{$examno}");
            $this->mainField[$examno]["ABSENCE_TOTAL_MINUS"]  =  VARS::post("ABSENCE_TOTAL_MINUS_{$examno}");
            $this->mainField[$examno]["HYOUTEI1_2_PLUS"]      =  VARS::post("HYOUTEI1_2_PLUS_{$examno}");
            $this->mainField[$examno]["HYOUTEI1_2_MINUS"]     =  VARS::post("HYOUTEI1_2_MINUS_{$examno}");
            $this->mainField[$examno]["INTERVIEW_PLUS"]       =  VARS::post("INTERVIEW_PLUS_{$examno}");
            $this->mainField[$examno]["INTERVIEW_MINUS"]      =  VARS::post("INTERVIEW_MINUS_{$examno}");

            $this->mainField[$examno]["TOTAL_PLUS"]     =  VARS::post("TOTAL_PLUS_{$examno}");
            $this->mainField[$examno]["TOTAL_MINUS"]    =  VARS::post("TOTAL_MINUS_{$examno}");
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //post値をセット
        $this->setMainFieldValue();

        //更新
        if ($this->field["DISP_DIV"] == "1") {
            knjl120iQuery::updateQuery1($db, $this);
        } elseif ($this->field["DISP_DIV"] == "2") {
            knjl120iQuery::updateQuery2($db, $this);
        }

        $this->setMessage("MSG201");

        $db->commit();
        Query::dbCheckIn($db);
    }

    //設定ポイント取得
    public function getPoint($pointcd, $inputVal)
    {
        $pointLevelArray = $this->pointInfo[$row["POINTCD"]];

        //各レベルの下限・上限の範囲チェック
        foreach ($pointLevelArray as $level => $pointData) {
            if ($pointData["POINTLOW"] <= $inputVal && $inputVal <= $pointData["POINTHIGH"]) {
                return array($row["PLUS_POINT"], $row["MINUS_POINT"]);
            }
        }

        return array(0, 0);
    }

    //序列確定処理
    public function getExecModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjl120iQuery::updateExecQuery($db, $this);

        $this->setMessage("MSG201");

        $db->commit();
        Query::dbCheckIn($db);
    }

    //コピー処理
    public function copyApplicantConfrpt($db)
    {
        knjl120iQuery::copyApplicantConfrpt($db, $this);
    }
}
