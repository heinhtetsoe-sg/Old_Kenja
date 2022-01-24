<?php

require_once('for_php7.php');

class knjl050fModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $appHold;
    public $testdiv;
    public $testdiv0;
    public $examno;
    public $ObjYear;
    public $examcourse;
    public $target;
    public $special_reason_div;
    public $exam_type;

    public function knjl050fModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testdiv0       = strlen(VARS::post("TESTDIV0")) ? VARS::post("TESTDIV0") : VARS::post("HID_TESTDIV0");
        $this->testsubclasscd = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->examcourse     = strlen(VARS::post("EXAMCOURSE")) ? VARS::post("EXAMCOURSE") : VARS::post("HID_EXAMCOURSE");
        $this->exam_type      = VARS::post("EXAM_TYPE");

        $this->target         = VARS::post("TARGET");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        $this->tmp_score      = VARS::post("SCORE");    //配列
        $this->tmp_score3     = VARS::post("SCORE3");   //配列
        $this->tmp_remark2    = VARS::post("INTERVIEW_REMARK2");   //配列
        $this->hid_receptno   = VARS::post("HID_RECEPTNO"); //"RECEPTNO"-"EXAMNO"で取得

        $this->s_receptno   = VARS::post("s_receptno");
        $this->e_receptno   = VARS::post("e_receptno");
    }

    public function &getMainModel()
    {
    }

    //入力項目チェック
    public function validateInput()
    {
        //空文字チェック
        if (!($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!($this->testdiv)) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if (!($this->testsubclasscd)) {
            $this->setWarning("MSG301", "( 受験科目 )");
            return false;
        }

        //文字数チェック
        if ($this->applicantdiv == "1" && $this->ieeFlg) {
            foreach ($this->remark2 as $receptno_examno => $remark2) {
                list($receptno, $examno) = explode("-", $receptno_examno);

                if (strlen($remark2) > 75) {
                    $this->setWarning("MSG915", "備考は75バイトまでです。\\n受験番号({$receptno})");
                    return false;
                }
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->score = $this->score3 = $this->remark2 = array();

        //得点を再配置
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno_examno) {
            list($receptno, $examno) = explode("-", $receptno_examno);

            $this->score[$receptno]  = $this->tmp_score[$key];
            $this->score3[$receptno] = $this->tmp_score3[$key];
            $this->remark2[$receptno_examno] = $this->tmp_remark2[$key];
        }
        if ($this->validateInput()) {
            $result = knjl050fQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201", "\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
            }
        }
    }
}
