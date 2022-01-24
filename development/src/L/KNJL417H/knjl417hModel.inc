<?php
class knjl417hModel extends model
{
    public $cmd;
    public $examYear;
    public $applicantdiv;
    public $testdiv;
    public $s_examno;
    public $e_examno;
    // var $field = array();
    public $hid_kyokaseq;
    public $hid_5kyokaseq;
    public $hid_3kyokaseq;
    public $recordarry;

    public function knjl417hModel()
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
                                "APPLICANTDIV"      => VARS::post("APPLICANTDIV"),
                                "EXAMCOURSECD"      => VARS::post("EXAMCOURSECD"),
                                );

        if ($this->arr_examno) {
            $this->examData = array();
            foreach ($this->arr_examno as $examno) {
                $this->examData[$examno]["LAST_SCHOLAR"] = VARS::post("LAST_SCHOLAR-".$examno); //最終内諾判定
                $this->examData[$examno]["LAST_SCHOLAR_DIV"] = VARS::post("LAST_SCHOLAR_DIV-".$examno); //最終内諾判定区分
            }
        }

        //開始受験番号
        $this->s_examno = VARS::post("S_EXAMNO");

        //受験番号の開始番号を設定
        if ($this->cmd == "next" || $this->cmd == "back") {
            $db = Query::dbCheckOut();

            $query = knjl417hQuery::selectQuery($this, "ALL");
            $result = $db->query($query);
            $examAllList = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $examAllList[] = $row["EXAMNO"];
            }
            //開始受験番号をセット
            if ($this->s_examno != "" && count($examAllList) > 0) {
                $this->s_examno = $this->getStartExamno($this->s_examno, $examAllList);
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
            knjl417hQuery::getUpdateQuery($this);
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
            if ($data["LAST_SCHOLAR"] == "" xor $data["LAST_SCHOLAR_DIV"] == "") { //片方の値のみが入力されている場合エラー
                $this->setWarning("MSG901", "最終特別奨学生判定・最終特別奨学生判定区分が片方のみ入力されている志願者が存在します。");
                return false;
            }
        }

        return true;
    }

    public function clean()
    {
    }
}
