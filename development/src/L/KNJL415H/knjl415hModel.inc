<?php
class knjl415hModel extends model
{
    public $cmd;
    public $examYear;
    public $applicantdiv;
    public $testdiv;
    public $s_examOrg;
    public $s_receptno;
    public $hid_kyokaseq;
    public $hid_5kyokaseq;
    public $hid_3kyokaseq;
    public $recordarry;

    public function knjl415hModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->cmd);
            unset($this->examYear);
            unset($this->field);
        }

        $this->cmd      = VARS::request("cmd");
        $this->examYear = CTRL_YEAR + 1;
        $this->field    = array(
                                "APPLICANTDIV"  => VARS::post("APPLICANTDIV"),
                                "EXAMCOURSECD"  => VARS::post("EXAMCOURSECD"),
                                "MOCK_LINE"     => VARS::post("MOCK_LINE")
                                );
        //開始受験番号
        $this->s_examOrg = VARS::post("S_EXAMNO");
        $this->s_receptno = $this->s_examOrg != "" ? sprintf("%07s", VARS::post("S_EXAMNO")) : "";

        if ($this->arr_examno) {
            $this->examData = array();
            foreach ($this->arr_examno as $examno) {
                $this->examData[$examno]["CD006_REMARK10"] = VARS::post("CD006_REMARK10-".$examno); //最終内諾判定
                $this->examData[$examno]["CD006_REMARK11"] = VARS::post("CD006_REMARK11-".$examno); //最終内諾出願区分
                $this->examData[$examno]["CD006_REMARK12"] = VARS::post("CD006_REMARK12-".$examno); //最終内諾判定区分
            }
        }

        //受験番号の開始番号を設定
        if ($this->cmd == "next" || $this->cmd == "back") {
            $db = Query::dbCheckOut();

            $query = knjl415hQuery::selectQuery($this, "ALL");
            $result = $db->query($query);
            $examAllList = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $examAllList[] = $row["EXAMNO"];
            }
            //開始受験番号をセット
            if ($this->s_receptno != "" && count($examAllList) > 0) {
                $s_exno = $db->getOne(knjl415hQuery::getExamNo($this, $this->s_receptno));
                $s_exno = $this->getStartExamno($s_exno, $examAllList);
                if ($s_exno != "") {
                    $this->s_receptno = $db->getOne(knjl415hQuery::getReceptNo($this, $s_exno));
                }
            }

            Query::dbCheckOut($db);
        }
    }

    //開始受験番号の取得
    public function getStartExamno($s_examno, $examAllList)
    {
        $currentIndex = array_search($s_examno, $examAllList);
        if ($this->cmd == "next") {
            //指定受験番号が母集団に無い場合、直近の受験番号を開始位置とする
            if (!$currentIndex) {
                $findFollowExamno = function ($ele) use ($s_examno) {
                    return $s_examno < $ele;
                };
                $tmp = array_filter($examAllList, $findFollowExamno);
                if (count($tmp) > 0) {
                    $nearExamno = min($tmp);
                    $s_examno = $nearExamno;
                }
                //50件先に受験番号があるならそれを開始位置とする
            } elseif (isset($examAllList[$currentIndex + 50])) {
                $s_examno = $examAllList[$currentIndex + 50];
            }
        } elseif ($this->cmd == "back") {
            if (!$currentIndex) {
                $findPrecedingExamno = function ($ele) use ($s_examno) {
                    return $s_examno > $ele;
                };
                $tmp = array_filter($examAllList, $findPrecedingExamno);
                if (count($tmp) > 0) {
                    $nearExamno = max($tmp);
                    $s_examno = $nearExamno;
                }
            } elseif (isset($examAllList[$currentIndex - 50])) {
                $s_examno = $examAllList[$currentIndex - 50];
            }
        }
        return $s_examno;
    }

    //受験番号をセット
    public function &getMainModel()
    {
        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            //更新処理
            knjl415hQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //データチェック
        if (!is_array($this->examData)) {
            $this->setWarning("MSG303");
            return false;
        }

        //相関チェック
        foreach ($this->examData as $examno => $data) {
            if ($data["CD006_REMARK10"] == "" || $data["CD006_REMARK11"] == "" || $data["CD006_REMARK12"] == "") { //どれかが未選択状態の場合はエラー
                $this->setWarning("MSG901", "最終内諾判定、最終内諾出願区分、最終内諾判定のいずれかが未入力の志願者が存在します。");
                return false;
            }
        }

        return true;
    }

    public function clean()
    {
    }
}
