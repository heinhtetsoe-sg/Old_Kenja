<?php

require_once('for_php7.php');

class knjl670hModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;
    public $applicantDiv;
    public $schoolKind;
    public $isTestSubj;

    public function knjl670hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;
        $this->applicantDiv = "2";

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("TESTDIV"                =>  VARS::post("TESTDIV"),                //入試日程
                             "TESTDIV1"               =>  VARS::post("TESTDIV1"),               //類別
                             "PASS_POINT"             =>  VARS::post("PASS_POINT"),             //合格点
                             "SIM_START"              =>  VARS::post("SIM_START"),              //受験番号（開始）
                             "SIM_END"                =>  VARS::post("SIM_END"),                //受験番号（終了）
                             "TOKIO_PROBABILITY"      =>  VARS::post("TOKIO_PROBABILITY"),      //併願(東京)
                             "OTHER_PROBABILITY"      =>  VARS::post("OTHER_PROBABILITY"),      //併願(東京以外)
                             "GENERAL_PROBABILITY1"   =>  VARS::post("GENERAL_PROBABILITY1"),   //一般確率1
                             "GENERAL_PROBABILITY2"   =>  VARS::post("GENERAL_PROBABILITY2"),   //一般確率2
                             "POSTPONE_PROBABILITY1"  =>  VARS::post("POSTPONE_PROBABILITY1"),  //併願(東京以外)
                             "POSTPONE_PROBABILITY2"  =>  VARS::post("POSTPONE_PROBABILITY2"),  //併願(東京以外)
                             "POSTPONE_PROBABILITY3"  =>  VARS::post("POSTPONE_PROBABILITY3")   //帳票種類
                        );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->isTestSubj = $this->field["TESTDIV"] == "02" ? true : false;  //testdiv="02"以外は小論文のみ。
    }

    public function clean()
    {
    }

    public function &getUpdateModel()
    {
        //科目試験の時は、合格点をチェック
        if ($this->isTestSubj) {
            if (is_null($this->field["PASS_POINT"]) || preg_match('/^[0-9]+$/', $this->field["PASS_POINT"]) != 1) {
                $this->setWarning("MSG907", "( 合格点 )".$this->field["PASS_POINT"]);
                return false;
            }
        }

        //DB接続
        $db = Query::dbCheckOut();
        //更新処理
        $datas = array();
        $query = knjl670hQuery::getJudgeExamee($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $datas[] = $row;
        }
        //DB切断
        Query::dbCheckIn($db);

        if (get_count($datas) == 0) {
            $this->setWarning("MSG303", "更新するデータがありません。得点入力、序列確定処理が行われたか、確認してください。");
            return false;
        }
        //クリア処理
        if (knjl670hQuery::getUpdateQuery($this, $datas, true)) {
            $result = knjl670hQuery::getUpdateQuery($this, $datas);
            if ($result) {
                $this->setMessage("MSG201");
            } else {
                $this->setMessage("MSG203", "登録処理中にエラーが発生しました");
            }
        } else {
            $this->setMessage("MSG203", "クリア処理中にエラーが発生しました");
        }
    }

    public function &getClearModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        //更新処理
        $datas = array();
        $query = knjl670hQuery::getJudgeExamee($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $datas[] = $row;
        }
        //DB切断
        Query::dbCheckIn($db);

        if (get_count($datas) == 0) {
            $this->setWarning("MSG303", "更新するデータがありません。得点入力、序列確定処理が行われたか、確認してください。");
            return false;
        }
        //クリア処理
        $result = knjl670hQuery::getUpdateQuery($this, $datas, true);
        if ($result) {
            $this->setMessage("MSG201", $prtStr);
        } else {
            $this->setMessage("MSG203", "クリア処理中にエラーが発生しました");
        }
    }
}
