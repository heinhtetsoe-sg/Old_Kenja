<?php

require_once('for_php7.php');


class knjz030eModel extends Model
{
    public $cmd;
    public $control;
    public $selectdata;
    public $year;          //入試年度
    public $applicantdiv;
    public $desirediv;
    public $testdiv1;
    public $sucDesirediv;

    public function knjz030eModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->desirediv);
            unset($this->testdiv1);
            unset($this->sucDesirediv);
        }

        $temp = VARS::request("ENTEXAMYEAR");
        if (isset($temp)) {
            $this->year = VARS::request("ENTEXAMYEAR");
        }
        if ($this->year == "") {
            $this->year  = CTRL_YEAR + 1;
        }

        $tmpa = VARS::get("APPLICANTDIV");
        if (isset($tmpa)) {
            $this->applicantdiv = $tmpa;
        }

        $temp = VARS::request("DESIREDIV");
        if (isset($temp)) {
            $this->desirediv = VARS::request("DESIREDIV");
        }

        $temp = VARS::request("TESTDIV1");
        if (isset($temp)) {
            $this->testdiv1 = VARS::request("TESTDIV1");
        }

        $temp = VARS::request("SUC_DESIREDIV");
        if (isset($temp)) {
            $this->sucDesirediv = VARS::request("SUC_DESIREDIV");
        }

        //選択コース
        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");

        //初期値がセットされていない場合
        if ($this->applicantdiv == "") {
            $db = Query::dbCheckOut();
            $appNamespare       = $db->getOne(knjz030eQuery::getNameSpare2($this, "L003"));
            $this->applicantdiv = $db->getOne(knjz030eQuery::getNameMstSet($this, "L003", $appNamespare));
            Query::dbCheckIn($db);
        }
    }

    //更新前チェック
    public function validateInput()
    {
        //必須項目
        if ($this->desirediv == '' || $this->testdiv1 == '' || $this->sucDesirediv == '') {
            $this->setWarning("MSG301", $setName);
            return false;
        }

        return true;
    }

    //更新用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        knjz030eQuery::getUpdateQuery($this);
        return;
    }

    //変数をクリアします
    public function clean()
    {
    }

    //次年度のデータを作成
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();

        $this->nextYear = $this->year + 1;
        $message = '';
        $flg = true;
        if (!checkThisYear($db, $this->year)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!checkNextYear($db, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if (!checkNameMstNextYear($db, $this->nextYear)) {
            $message = $message . '次年度の名称マスタ（L045・L058）が存在しません。';
            $flg = false;
        }
        if ($flg) {
            //次年度コピー
            $query = knjz030eQuery::getCopyQuery($db, $this->year, $this->nextYear);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        Query::dbCheckIn($db);
    }
}

//今年度データチェック
function checkThisYear($db, $year)
{
    $result = $db->getOne(knjz030eQuery::getExists2("ENTEXAM_SLIDE_GROUP_YMST", $year));
    $ret = ($result > 0) ? true : false;
    return $ret;
}

//次年度データチェック
function checkNextYear($db, $nextYear)
{
    $result1 = $db->getOne(knjz030eQuery::getExists2("ENTEXAM_SLIDE_GROUP_YMST", $nextYear));
    $ret = ($result1 == 0) ? true : false;
    return $ret;
}

//次年度データチェック（名称マスタ）
function checkNameMstNextYear($db, $nextYear)
{
    $result1 = $db->getOne(knjz030eQuery::getExists2("V_NAME_MST", $nextYear, "L045"));
    $result2 = $db->getOne(knjz030eQuery::getExists2("V_NAME_MST", $nextYear, "L058"));
    $ret = ($result1 > 0 && $result2 > 0) ? true : false;
    return $ret;
}
